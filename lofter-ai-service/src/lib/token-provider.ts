import { db } from "@/lib/db";
import { logger } from "@/lib/logger";
import type { Service } from "@/generated/prisma";

// 内存缓存：serviceId → { token, expiresAt }
const tokenMemCache = new Map<bigint, { token: string; expiresAt: Date }>();

const BUFFER_MS = 10 * 60 * 1000; // 提前 10 分钟视为过期

type AuthKeyConfig = { account: string; key: string; authUrl: string; project?: string };
type StaticTokenConfig = { token: string; headerName: string };

/**
 * 获取后端服务鉴权 Token。
 * - auth_key 类型：自动刷新，两层缓存（内存 + DB）
 * - static_token 类型：直接返回配置中的 token
 * - db_password 类型：不适用，返回 null
 */
export async function getServiceToken(
  service: Service
): Promise<string | null> {
  const log = logger.child({ service: service.code, credentialType: service.credentialType });

  if (service.credentialType === "static_token") {
    log.debug("token 命中 static_token 配置");
    return (service.credentialConfig as Record<string, unknown> as StaticTokenConfig).token;
  }
  if (service.credentialType !== "auth_key") return null;

  const now = Date.now();

  // 1. 内存缓存
  const mem = tokenMemCache.get(service.id);
  if (mem && mem.expiresAt.getTime() - now > BUFFER_MS) {
    log.debug("token 命中内存缓存");
    return mem.token;
  }

  // 2. DB 缓存
  if (
    service.cachedToken &&
    service.cachedTokenExpiresAt &&
    service.cachedTokenExpiresAt.getTime() - now > BUFFER_MS
  ) {
    log.debug("token 命中 DB 缓存");
    tokenMemCache.set(service.id, {
      token: service.cachedToken,
      expiresAt: service.cachedTokenExpiresAt,
    });
    return service.cachedToken;
  }

  // 3. 调 Auth API 刷新
  const cfg = service.credentialConfig as Record<string, unknown> as AuthKeyConfig;
  log.info({ authUrl: cfg.authUrl }, "token 缓存未命中，调 Auth API 刷新");
  const res = await fetch(`${cfg.authUrl}/api/v2/tokens`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ user: cfg.account, key: cfg.key, ttl: 86400 }),
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) {
    const respText = await res.text();
    log.error({ status: res.status, body: respText }, "Auth API 刷新 Token 失败");
    throw new Error(`Auth API 刷新 Token 失败: ${res.status} ${respText}`);
  }
  const data = (await res.json()) as { token: string };
  const newToken = data.token;
  const expiresAt = new Date(now + 86400 * 1000); // 真实 token 到期时间
  log.info({ expiresAt: expiresAt.toISOString() }, "Auth API Token 刷新成功");

  // 4. 写 DB + 内存缓存
  await db.service.update({
    where: { id: service.id },
    data: { cachedToken: newToken, cachedTokenExpiresAt: expiresAt },
  });
  tokenMemCache.set(service.id, { token: newToken, expiresAt });

  return newToken;
}

/** 服务重启或凭据更新时清除内存缓存 */
export function invalidateTokenCache(serviceId: bigint): void {
  tokenMemCache.delete(serviceId);
}
