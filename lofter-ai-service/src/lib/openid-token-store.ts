import { LRUCache } from "lru-cache";
import type { OpenIDUserInfo } from "./openid";
import type { AssocData } from "./openid";

/**
 * OpenID 关联数据缓存
 * 关联信息有效期 86400 秒，缓存复用避免每次登录都发起关联请求
 */
let currentAssoc: AssocData | null = null;

export function getCachedAssoc(): AssocData | null {
  if (!currentAssoc) return null;
  // 提前 60 秒过期，避免边界问题
  if (Date.now() >= currentAssoc.expiresAt - 60_000) {
    currentAssoc = null;
    return null;
  }
  return currentAssoc;
}

export function setCachedAssoc(assoc: AssocData): void {
  currentAssoc = assoc;
}

/**
 * OpenID 登录 token 临时存储
 * 用于桥接 OpenID 回调校验成功 → NextAuth 创建 Session 的过程
 * TTL: 60 秒，用完即删
 */
export const openidTokenStore = new LRUCache<string, OpenIDUserInfo>({
  max: 200,
  ttl: 1000 * 60, // 60 秒
});
