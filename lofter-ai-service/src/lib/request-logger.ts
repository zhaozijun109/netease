import { db } from "@/lib/db";
import { logger } from "@/lib/logger";
import type { ApiKeyType } from "@/generated/prisma";
import { extractCompletionSummary, extractErrorMessage } from "@/lib/summary-extractor";

export type LogContext = {
  appCode: string;
  apiKeyId: bigint;
  keyType: ApiKeyType;
  userCode: string | null;
  /** 请求路径，如 /v1/chat/completions */
  path: string;
  /** 请求开始时间戳（Date.now()） */
  startTime: number;
  /** 请求 body 提取的摘要数据（在 route.ts 中填充） */
  promptSummary?: string | null;
  requestParams?: Record<string, unknown> | null;
};

type UsageData = {
  prompt_tokens?: number;
  completion_tokens?: number;
  total_tokens?: number;
};

type SummaryData = {
  prompt_summary?: string | null;
  completion_summary?: string | null;
  request_params?: Record<string, unknown> | null;
  error_message?: string | null;
};

async function writeLog(
  ctx: LogContext,
  statusCode: number,
  model: string | null,
  usage: UsageData | null,
  summary: SummaryData = {}
): Promise<void> {
  const meta: Record<string, unknown> = {};
  if (usage) Object.assign(meta, usage);
  if (ctx.promptSummary) meta.prompt_summary = ctx.promptSummary;
  if (ctx.requestParams) meta.request_params = ctx.requestParams;
  if (summary.completion_summary) meta.completion_summary = summary.completion_summary;
  if (summary.error_message) meta.error_message = summary.error_message;

  await db.requestLog.create({
    data: {
      appCode: ctx.appCode,
      apiKeyId: ctx.apiKeyId,
      keyType: ctx.keyType,
      userCode: ctx.userCode,
      model,
      path: ctx.path,
      statusCode,
      latencyMs: Date.now() - ctx.startTime,
      meta: Object.keys(meta).length > 0 ? (meta as object) : undefined,
    },
  });
}

/**
 * 非流式响应日志（fire-and-forget）
 * 从响应 JSON 的 usage 字段提取 token 用量，并提取 completion 摘要
 */
export function logNonStreaming(
  ctx: LogContext,
  statusCode: number,
  responseBody: unknown
): void {
  let model: string | null = null;
  let usage: UsageData | null = null;
  const summary: SummaryData = {};

  if (responseBody && typeof responseBody === "object") {
    const body = responseBody as Record<string, unknown>;
    if (typeof body.model === "string") model = body.model;
    if (body.usage && typeof body.usage === "object") {
      usage = body.usage as UsageData;
    }
    if (statusCode >= 400) {
      summary.error_message = extractErrorMessage(responseBody);
    } else {
      summary.completion_summary = extractCompletionSummary(responseBody);
    }
  }

  writeLog(ctx, statusCode, model, usage, summary).catch((err) => {
    logger.warn({ err }, "Failed to write request log");
  });
}

/**
 * 流式 SSE 响应日志（fire-and-forget）
 * 消费 tee 出的副本流，从最后一个含 usage 的 chunk 提取 token 数，
 * 并累积 delta.content 生成 completion 摘要
 *
 * AIGW 在流式响应的最后一个 chunk 附加了 usage 字段：
 * data: {..., "usage": {"prompt_tokens":92,"completion_tokens":6,"total_tokens":98}}
 */
export function logStreaming(
  ctx: LogContext,
  statusCode: number,
  logStream: ReadableStream<Uint8Array>
): void {
  (async () => {
    const reader = logStream.getReader();
    const decoder = new TextDecoder();
    let lastUsage: UsageData | null = null;
    let model: string | null = null;
    let contentBuffer = ""; // 累积 delta.content

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const text = decoder.decode(value, { stream: true });
        for (const line of text.split("\n")) {
          if (!line.startsWith("data: ") || line === "data: [DONE]") continue;
          try {
            const json = JSON.parse(line.slice(6)) as Record<string, unknown>;
            if (typeof json.model === "string") model = json.model;
            if (json.usage) lastUsage = json.usage as UsageData;
            // 累积 completion content
            const choices = json.choices as Array<Record<string, unknown>> | undefined;
            if (choices?.[0]) {
              const delta = choices[0].delta as Record<string, unknown> | undefined;
              if (delta && typeof delta.content === "string") {
                contentBuffer += delta.content;
              }
            }
          } catch {
            // 忽略单行解析失败
          }
        }
      }
    } catch (err) {
      logger.warn({ err }, "Error reading SSE log stream");
    } finally {
      reader.releaseLock();
    }

    const summary: SummaryData = {};
    if (contentBuffer) {
      const { truncate } = await import("@/lib/summary-extractor");
      summary.completion_summary = truncate(contentBuffer, 200);
    }

    await writeLog(ctx, statusCode, model, lastUsage, summary);
  })().catch((err) => {
    logger.warn({ err }, "Failed to write streaming request log");
  });
}
