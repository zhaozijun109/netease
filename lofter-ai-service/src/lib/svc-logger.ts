import { db } from "@/lib/db";
import { logger } from "@/lib/logger";
import { truncate } from "@/lib/summary-extractor";

export type SvcLogContext = {
  serviceCode: string;
  userEmail: string | null;
  path: string;
  startTime: number;
  /** 请求 body 摘要（在 route.ts 中提取） */
  requestSummary?: string | null;
};

/**
 * 异步写 /svc/* 请求日志，复用 ai_request_logs 表。
 * appCode 格式：svc:{serviceCode}，统计页面可通过前缀区分。
 */
export function logSvcRequest(
  ctx: SvcLogContext,
  statusCode: number,
  responseSummary?: string | null
): void {
  const meta: Record<string, unknown> = {};
  if (ctx.requestSummary) meta.prompt_summary = ctx.requestSummary;
  if (responseSummary) meta.completion_summary = truncate(responseSummary, 200);

  db.requestLog
    .create({
      data: {
        appCode: `svc:${ctx.serviceCode}`,
        userCode: ctx.userEmail,
        path: ctx.path,
        statusCode,
        latencyMs: Date.now() - ctx.startTime,
        meta: Object.keys(meta).length > 0 ? (meta as Record<string, unknown>) as never : undefined,
      },
    })
    .catch((err) => {
      logger.warn({ err }, "Failed to write svc request log");
    });
}
