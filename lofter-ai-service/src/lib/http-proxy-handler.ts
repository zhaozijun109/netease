import { getServiceToken } from "@/lib/token-provider";
import { logger } from "@/lib/logger";
import type { Service } from "@/generated/prisma";

type AuthKeyConfig = { account: string; project?: string };
type StaticTokenConfig = { headerName: string };
type HttpProxyConfig = { baseUrl: string; timeout?: number; maxTimeout?: number };

const STRIP_HEADERS = new Set([
  "content-encoding", "content-length", "transfer-encoding",
  "connection", "keep-alive", "upgrade", "te", "trailer", "proxy-authenticate", "proxy-authorization",
]);

/**
 * 将请求转发到注册的 HTTP 服务，自动注入鉴权 Header
 */
export async function handleHttpProxy(
  req: Request,
  service: Service,
  pathSegments: string[]
): Promise<Response> {
  const cfg = service.config as HttpProxyConfig;
  const defaultTimeout = cfg.timeout ?? 30000;
  const maxTimeout = cfg.maxTimeout ?? 120000;

  const requestedTimeout = parseInt(req.headers.get("X-Query-Timeout") ?? "0", 10);
  const timeoutMs = requestedTimeout > 0
    ? Math.min(requestedTimeout, maxTimeout)
    : defaultTimeout;

  const token = await getServiceToken(service);

  const headers = new Headers();
  for (const [k, v] of req.headers.entries()) {
    if (!["authorization", "host"].includes(k.toLowerCase())) {
      headers.set(k, v);
    }
  }

  if (token) {
    if (service.credentialType === "auth_key") {
      const authCfg = service.credentialConfig as Record<string, unknown> as AuthKeyConfig;
      headers.set("X-Access-Token", token);
      headers.set("X-Auth-User", authCfg.account);
      if (authCfg.project) headers.set("X-Auth-Project", authCfg.project);
    } else if (service.credentialType === "static_token") {
      const stCfg = service.credentialConfig as Record<string, unknown> as StaticTokenConfig;
      headers.set(stCfg.headerName, token);
    }
  }

  const targetUrl = `${cfg.baseUrl}/${pathSegments.join("/")}`;
  const log = logger.child({ service: service.code, targetUrl, method: req.method });
  log.info({ timeoutMs }, "http proxy 开始转发");

  const fetchStart = Date.now();
  const upstream = await fetch(targetUrl, {
    method: req.method,
    headers,
    body: req.body,
    signal: AbortSignal.timeout(timeoutMs),
    // @ts-expect-error Node fetch duplex
    duplex: "half",
  });
  log.info({ status: upstream.status, latencyMs: Date.now() - fetchStart }, "http proxy 上游响应");

  const responseHeaders = new Headers();
  for (const [k, v] of upstream.headers.entries()) {
    if (!STRIP_HEADERS.has(k.toLowerCase())) responseHeaders.set(k, v);
  }

  return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
}
