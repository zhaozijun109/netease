import type { NextRequest } from "next/server";
import { validateSkillToken, hasServicePermission } from "@/lib/skill-token";
import { handleHttpProxy } from "@/lib/http-proxy-handler";
import { handleDbQuery } from "@/lib/db-query-handler";
import { logSvcRequest } from "@/lib/svc-logger";
import { truncate } from "@/lib/summary-extractor";
import { db } from "@/lib/db";
import { logger } from "@/lib/logger";

type RouteParams = { params: Promise<{ serviceCode: string; path: string[] }> };

async function handleSvc(req: NextRequest, { params }: RouteParams): Promise<Response> {
  const startTime = Date.now();
  const { serviceCode, path } = await params;
  const log = logger.child({ serviceCode, method: req.method, path: `/${(path ?? []).join("/")}` });

  // Step 1: 验证 Skill Token
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  const rawToken = authHeader.slice(7).trim();
  const tokenData = await validateSkillToken(rawToken);
  if (!tokenData) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  if (!hasServicePermission(tokenData, serviceCode)) {
    return Response.json({ error: "Forbidden: no permission for this service" }, { status: 403 });
  }

  log.info({ userEmail: tokenData.userEmail, tokenPrefix: rawToken.slice(0, 12) + "..." }, "svc 请求进入");

  // ── 提取请求 body 摘要 ──────────────────────────────────────
  let requestSummary: string | null = null;
  try {
    const bodyText = await req.clone().text();
    requestSummary = truncate(bodyText, 200);
  } catch {
    // 无 body 或读取失败，跳过
  }

  // Step 2: 查服务
  const service = await db.service.findUnique({
    where: { code: serviceCode },
  });
  if (!service || service.status === 0) {
    return Response.json({ error: `Service "${serviceCode}" not found` }, { status: 404 });
  }
  if (!service.credentialType && service.type !== "static_content") {
    return Response.json(
      { error: `Service "${serviceCode}" has no credential configured` },
      { status: 503 }
    );
  }

  // Step 3: 按 type 分发
  let response: Response;
  try {
    if (service.type === "http_proxy") {
      response = await handleHttpProxy(req, service, path ?? []);
    } else if (service.type === "db_query") {
      response = await handleDbQuery(req, service);
    } else if (service.type === "static_content") {
      const config = service.config as Record<string, unknown>;
      const content = (config?.content as string) ?? "";
      const contentType = (config?.contentType as string) ?? "text/plain";
      response = new Response(content, {
        status: 200,
        headers: { "Content-Type": `${contentType}; charset=utf-8` },
      });
    } else {
      response = Response.json(
        { error: `Unknown service type: ${service.type}` },
        { status: 500 }
      );
    }
  } catch (err) {
    const isTimeout = err instanceof Error && err.name === "TimeoutError";
    const errMessage = err instanceof Error ? err.message : String(err);
    log.error({ err, isTimeout }, "svc 处理异常");
    logSvcRequest(
      { serviceCode, userEmail: tokenData.userEmail, path: req.nextUrl.pathname, startTime, requestSummary },
      isTimeout ? 504 : 502,
      truncate(errMessage, 200)
    );
    return Response.json(
      { error: isTimeout ? "Gateway Timeout" : errMessage },
      { status: isTimeout ? 504 : 502 }
    );
  }

  // Step 4: 异步写日志（提取响应摘要）
  log.info({ status: response.status, latencyMs: Date.now() - startTime }, "svc 请求完成");

  // 提取响应 body 摘要（需要 clone 避免消费原始 body）
  let responseSummary: string | null = null;
  try {
    const resText = await response.clone().text();
    responseSummary = truncate(resText, 200);
  } catch {
    // 响应 body 读取失败，跳过
  }

  logSvcRequest(
    { serviceCode, userEmail: tokenData.userEmail, path: req.nextUrl.pathname, startTime, requestSummary },
    response.status,
    responseSummary
  );

  return response;
}

export const GET = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const POST = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const PUT = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const PATCH = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const DELETE = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
