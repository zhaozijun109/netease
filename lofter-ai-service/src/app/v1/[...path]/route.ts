import { type NextRequest } from "next/server";
import { validateApiKey, resolveUserCode } from "@/lib/auth-key";
import { buildAigwHeaders, forwardToAigw } from "@/lib/aigw-client";
import {
  logNonStreaming,
  logStreaming,
  type LogContext,
} from "@/lib/request-logger";
import { logger } from "@/lib/logger";
import { corsConfig } from "@/lib/env";
import { extractPromptSummary, extractRequestParams } from "@/lib/summary-extractor";

/** 转发时从 AIGW 响应头中剥除的 header */
const STRIP_RESPONSE_HEADERS = new Set([
  "content-encoding", // 避免解压/重压缩问题
  "content-length",   // 流式时长度失效
  "transfer-encoding",
  "connection",
  "keep-alive",
]);

type RouteParams = { params: Promise<{ path: string[] }> };

/**
 * 构造 CORS 响应头
 * 根据 CORS_ALLOWED_ORIGINS 环境变量决定允许的来源
 */
function buildCorsHeaders(requestOrigin: string | null): Headers {
  const headers = new Headers();
  const allowed = corsConfig.allowedOrigins;

  if (allowed.includes("*")) {
    headers.set("Access-Control-Allow-Origin", "*");
  } else if (requestOrigin && allowed.includes(requestOrigin)) {
    headers.set("Access-Control-Allow-Origin", requestOrigin);
    headers.set("Vary", "Origin");
  }

  headers.set(
    "Access-Control-Allow-Methods",
    "GET, POST, PUT, PATCH, DELETE, OPTIONS"
  );
  headers.set(
    "Access-Control-Allow-Headers",
    "Authorization, Content-Type, X-User-Code, anthropic-beta"
  );
  headers.set("Access-Control-Max-Age", "86400");

  return headers;
}

async function handleProxy(
  req: NextRequest,
  pathSegments: string[],
  method: string
): Promise<Response> {
  const startTime = Date.now();
  const origin = req.headers.get("Origin");
  const corsHeaders = buildCorsHeaders(origin);

  // ── Step 1: 提取 API Key ──────────────────────────────────────────
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) {
    return Response.json(
      { error: "Unauthorized" },
      { status: 401, headers: corsHeaders }
    );
  }
  const rawKey = authHeader.slice(7).trim();

  // ── Step 2: 验证 API Key（LRU Cache + DB）───────────────────────
  const keyData = await validateApiKey(rawKey);
  if (!keyData) {
    return Response.json(
      { error: "Unauthorized" },
      { status: 401, headers: corsHeaders }
    );
  }

  // ── Step 3: 解析 user_code ────────────────────────────────────────
  let userCode: string | null;
  try {
    userCode = resolveUserCode(keyData, req);
  } catch (err) {
    const e = err as Error & { status?: number };
    if (e.status === 400) {
      return Response.json(
        { error: e.message },
        { status: 400, headers: corsHeaders }
      );
    }
    throw err;
  }

  const logCtx: LogContext = {
    appCode: keyData.app.appCode,
    apiKeyId: keyData.apiKeyId,
    keyType: keyData.type,
    userCode,
    path: `/v1/${pathSegments.join("/")}`,
    startTime,
  };

  // ── Step 3.5: 提取请求摘要（clone 不消费原始 body）──────────────
  let promptSummary: string | null = null;
  let requestParams: Record<string, unknown> | null = null;
  try {
    const clonedBody = await req.clone().json();
    promptSummary = extractPromptSummary(clonedBody);
    requestParams = extractRequestParams(clonedBody);
  } catch {
    // 非 JSON body（如文件上传）跳过摘要提取
  }
  logCtx.promptSummary = promptSummary;
  logCtx.requestParams = requestParams;

  // ── Step 4: 构造 AIGW 请求头 ─────────────────────────────────────
  const aigwHeaders = buildAigwHeaders(
    {
      appId: keyData.app.appId,
      appKey: keyData.app.appKey,
      appCode: keyData.app.appCode,
    },
    userCode,
    req.headers
  );

  // ── Step 5: 转发到 AIGW ───────────────────────────────────────────
  let upstream: Response;
  try {
    upstream = await forwardToAigw(
      pathSegments,
      method,
      aigwHeaders,
      req.body
    );
  } catch (err) {
    const isTimeout = err instanceof Error && err.name === "AbortError";
    logger.error({ err, pathSegments }, isTimeout ? "AIGW request timeout" : "Failed to forward request to AIGW");
    return Response.json(
      { error: isTimeout ? "Gateway Timeout" : "Bad Gateway" },
      { status: isTimeout ? 504 : 502, headers: corsHeaders }
    );
  }

  // ── Step 6: 构造响应头（过滤干扰字段，注入 CORS）────────────────
  const responseHeaders = new Headers(corsHeaders);
  for (const [key, value] of upstream.headers.entries()) {
    if (!STRIP_RESPONSE_HEADERS.has(key.toLowerCase())) {
      responseHeaders.set(key, value);
    }
  }

  // ── Step 7: 区分流式/非流式，透传响应并异步写日志 ─────────────────
  const isStreaming = upstream.headers
    .get("content-type")
    ?.includes("text/event-stream");

  if (isStreaming && upstream.body) {
    // 流式：tee() 一路给调用方，一路给日志消费
    const [clientStream, logStream] = upstream.body.tee();
    logStreaming(logCtx, upstream.status, logStream);
    return new Response(clientStream, {
      status: upstream.status,
      headers: responseHeaders,
    });
  }

  // 非流式：读取完整 body，异步写日志，再返回
  const responseText = await upstream.text();
  let parsedBody: unknown = null;
  try {
    parsedBody = JSON.parse(responseText);
  } catch {
    // 非 JSON 响应（如文件下载）跳过 usage 解析
  }
  logNonStreaming(logCtx, upstream.status, parsedBody);

  return new Response(responseText, {
    status: upstream.status,
    headers: responseHeaders,
  });
}

// ── CORS Preflight ─────────────────────────────────────────────────────────

export async function OPTIONS(req: NextRequest) {
  const origin = req.headers.get("Origin");
  return new Response(null, {
    status: 204,
    headers: buildCorsHeaders(origin),
  });
}

// ── 导出所有 HTTP 方法 ─────────────────────────────────────────────────

export async function GET(req: NextRequest, { params }: RouteParams) {
  const { path } = await params;
  return handleProxy(req, path, "GET");
}

export async function POST(req: NextRequest, { params }: RouteParams) {
  const { path } = await params;
  return handleProxy(req, path, "POST");
}

export async function PUT(req: NextRequest, { params }: RouteParams) {
  const { path } = await params;
  return handleProxy(req, path, "PUT");
}

export async function PATCH(req: NextRequest, { params }: RouteParams) {
  const { path } = await params;
  return handleProxy(req, path, "PATCH");
}

export async function DELETE(req: NextRequest, { params }: RouteParams) {
  const { path } = await params;
  return handleProxy(req, path, "DELETE");
}
