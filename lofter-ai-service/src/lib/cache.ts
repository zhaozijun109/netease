import { LRUCache } from "lru-cache";

/**
 * API Key 验证结果缓存的数据结构
 * 包含代理层所需的全部信息，避免每次请求都查库
 */
export type CachedKeyData = {
  apiKeyId: bigint;
  apiKey: string;
  type: "personal" | "service";
  /** personal key：取关联 User.email；无关联 User 时取 key.name */
  userEmail: string | null;
  app: {
    appCode: string;
    appId: string;
    appKey: string;
    requireUserCode: boolean;
    name: string;
  };
};

/**
 * API Key 验证结果缓存
 * - max: 最多缓存 1000 条记录
 * - ttl: 5 分钟后自动过期
 */
export const keyCache = new LRUCache<string, CachedKeyData>({
  max: 1000,
  ttl: 1000 * 60 * 5, // 5 minutes
});

/**
 * 主动失效某个 API Key 的缓存（用于 key 禁用/吊销时立即生效）
 */
export function invalidateKey(apiKey: string): void {
  keyCache.delete(apiKey);
}
