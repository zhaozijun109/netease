import { db } from "@/lib/db";
import { keyCache, type CachedKeyData } from "@/lib/cache";

/**
 * 验证 API Key 并返回完整的 key + app 信息
 * 优先从 LRU Cache 命中，未命中则查库并写入缓存
 *
 * @returns CachedKeyData 验证通过；null 表示 key 无效或已禁用
 */
export async function validateApiKey(
  rawKey: string
): Promise<CachedKeyData | null> {
  // 1. Cache 命中
  const cached = keyCache.get(rawKey);
  if (cached) return cached;

  // 2. DB 查询
  const keyRecord = await db.apiKey.findUnique({
    where: { apiKey: rawKey },
    include: { app: true, user: true },
  });

  // key 不存在、key 已禁用（status=0）、app 已禁用（status=0）— 均返回 null
  if (!keyRecord || keyRecord.status === 0 || keyRecord.app.status === 0) {
    return null;
  }

  const data: CachedKeyData = {
    apiKeyId: keyRecord.id,
    apiKey: keyRecord.apiKey,
    type: keyRecord.type as "personal" | "service",
    // personal key 优先取关联 User 的 email，否则降级用 name 字段
    userEmail:
      keyRecord.type === "personal"
        ? (keyRecord.user?.email ?? keyRecord.name)
        : null,
    app: {
      appCode: keyRecord.app.appCode,
      appId: keyRecord.app.appId,
      appKey: keyRecord.app.appKey,
      requireUserCode: keyRecord.app.requireUserCode,
      name: keyRecord.app.name,
    },
  };

  // 3. 写入缓存
  keyCache.set(rawKey, data);
  return data;
}

/**
 * 解析用户身份标识（user_code），用于注入 AIGW 的用量统计
 *
 * - personal key：直接取 key.userEmail
 * - service key + require_user_code=true：从 X-User-Code header 取
 * - service key + require_user_code=false：返回 null（允许匿名）
 *
 * @throws Error with status=400 when X-User-Code is required but missing
 */
export function resolveUserCode(
  keyData: CachedKeyData,
  req: Request
): string | null {
  if (keyData.type === "personal") {
    return keyData.userEmail;
  }

  // service key
  if (keyData.app.requireUserCode) {
    const userCode = req.headers.get("X-User-Code");
    if (!userCode?.trim()) {
      const err = new Error(
        "X-User-Code header is required for this app"
      ) as Error & { status: number };
      err.status = 400;
      throw err;
    }
    return userCode.trim();
  }

  return null;
}
