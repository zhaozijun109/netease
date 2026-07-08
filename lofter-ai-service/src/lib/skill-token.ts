import crypto from "crypto";
import { LRUCache } from "lru-cache";
import { db } from "@/lib/db";

export type CachedSkillTokenData = {
  id: bigint;
  userId: bigint;
  userEmail: string | null;
  permissions: string[];
};

const skillTokenCache = new LRUCache<string, CachedSkillTokenData>({
  max: 500,
  ttl: 1000 * 60 * 5,
});

export async function validateSkillToken(
  rawToken: string
): Promise<CachedSkillTokenData | null> {
  const cached = skillTokenCache.get(rawToken);
  if (cached) return cached;

  const record = await db.skillToken.findUnique({
    where: { token: rawToken },
    include: { user: { select: { email: true } } },
  });

  if (!record || record.status === 0) return null;
  if (record.expiresAt && record.expiresAt < new Date()) return null;

  const data: CachedSkillTokenData = {
    id: record.id,
    userId: record.userId,
    userEmail: record.user.email,
    permissions: record.permissions as string[],
  };

  skillTokenCache.set(rawToken, data);
  return data;
}

export function hasServicePermission(
  tokenData: CachedSkillTokenData,
  serviceCode: string
): boolean {
  return tokenData.permissions.includes("*") || tokenData.permissions.includes(serviceCode);
}

export function invalidateSkillToken(token: string): void {
  skillTokenCache.delete(token);
}

export function generateSkillToken(): string {
  return `skt-${crypto.randomBytes(24).toString("hex")}`;
}
