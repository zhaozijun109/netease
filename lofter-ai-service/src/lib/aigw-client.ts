import { aigwConfig } from "@/lib/env";
import { logger } from "@/lib/logger";

/**
 * 允许透传给 AIGW 的 request header 白名单（小写）
 * Authorization 由本服务负责替换，不透传原值
 */
const PASSTHROUGH_REQUEST_HEADERS = new Set([
  "content-type",
  "anthropic-beta",
  "accept",
  "accept-language",
  "cache-control",
  "x-aigw-meta", // 允许调用方覆盖（实际会被我们的值覆盖）
]);

type AppCredentials = {
  appId: string;
  appKey: string;
  appCode: string;
};

/**
 * 构造发往 AIGW 的请求头：
 * 1. 过滤调用方 header，只透传白名单内的
 * 2. 注入 AIGW 认证凭证（app_id.app_key）
 * 3. 注入用户身份和计费标签到 X-Aigw-Meta
 */
export function buildAigwHeaders(
  app: AppCredentials,
  userCode: string | null,
  incomingHeaders: Headers
): Headers {
  const headers = new Headers();

  // 透传白名单 header
  for (const [key, value] of incomingHeaders.entries()) {
    if (PASSTHROUGH_REQUEST_HEADERS.has(key.toLowerCase())) {
      headers.set(key, value);
    }
  }

  // 注入 AIGW Authorization
  headers.set("Authorization", `Bearer ${app.appId}.${app.appKey}`);

  // 注入用户信息和计费标签
  // first_tag 固定为 app_code，用于 AIGW 报表按业务线分组
  const metaParts: string[] = [];
  if (userCode) metaParts.push(`user_code=${userCode}`);
  metaParts.push(`first_tag=${app.appCode}`);
  headers.set("X-Aigw-Meta", metaParts.join("; "));

  return headers;
}

/**
 * 验证 AIGW App 凭证是否有效
 * 向 AIGW GET /v1/models 发探测请求，凭证正确时返回 2xx/4xx(非401/403)
 * 返回 { ok: true } 或 { ok: false, status, message }
 */
export async function verifyAigwCredentials(
  appId: string,
  appKey: string
): Promise<{ ok: true } | { ok: false; status: number; message: string }> {
  // HTTP headers only accept ASCII printable characters
  const isAsciiPrintable = (s: string) => /^[\x20-\x7E]+$/.test(s);
  if (!isAsciiPrintable(appId)) {
    return { ok: false, status: 422, message: "app_id 包含非 ASCII 字符，无法用于鉴权请求头。请填写 AIGW 分配的英文标识符。" };
  }
  if (!isAsciiPrintable(appKey)) {
    return { ok: false, status: 422, message: "app_key 包含非 ASCII 字符，请检查填写的密钥是否正确。" };
  }

  const baseUrl = aigwConfig.baseUrl;
  const url = `${baseUrl}/v1/models`;

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 10_000);

  try {
    const res = await fetch(url, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${appId}.${appKey}`,
        Accept: "application/json",
      },
      signal: controller.signal,
    });
    console.log("🐠 - verifyAigwCredentials - res:", res)

    if (res.status === 401 || res.status === 403) {
      return { ok: false, status: res.status, message: "Invalid app_id or app_key" };
    }
    return { ok: true };
  } catch (err) {
    const isTimeout = err instanceof Error && err.name === "AbortError";
    logger.warn({ err, appId }, isTimeout ? "AIGW verify timeout" : "AIGW verify failed");
    return {
      ok: false,
      status: 502,
      message: isTimeout ? "AIGW verification timed out" : "Failed to reach AIGW",
    };
  } finally {
    clearTimeout(timer);
  }
}

/**
 * 将请求转发到 AIGW 上游
 * - 支持流式请求 body（如文件上传），通过 duplex:'half' 实现
 * - 支持可配置超时（AIGW_TIMEOUT_MS，默认 120 秒）
 */
export async function forwardToAigw(
  pathSegments: string[],
  method: string,
  headers: Headers,
  body: ReadableStream | null
): Promise<Response> {
  const baseUrl = aigwConfig.baseUrl.replace(/\/$/, "");
  const url = `${baseUrl}/v1/${pathSegments.join("/")}`;

  const controller = new AbortController();
  const timeoutMs = aigwConfig.timeoutMs;
  const timer = setTimeout(() => {
    controller.abort();
    logger.warn({ url, timeoutMs }, "AIGW request timed out");
  }, timeoutMs);

  const fetchOptions: RequestInit & { duplex?: string } = {
    method,
    headers,
    signal: controller.signal,
  };

  if (body && method !== "GET" && method !== "HEAD") {
    fetchOptions.body = body;
    // Node.js 18+ 要求显式声明 duplex 才能使用流式请求 body
    fetchOptions.duplex = "half";
  }

  try {
    return await fetch(url, fetchOptions);
  } finally {
    clearTimeout(timer);
  }
}
