import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";

/**
 * GET /api/admin/stats/usage?from=&to=&app_code=&user_code=
 * 按 app_code + model + userCode 聚合 token 用量
 */
export async function GET(req: Request) {
  const unauth = await requirePermission("stats:view", req);
  if (unauth) return unauth;

  const { searchParams } = new URL(req.url);
  const appCode = searchParams.get("app_code") ?? undefined;
  const userCode = searchParams.get("user_code") ?? undefined;
  const from = searchParams.get("from")
    ? new Date(searchParams.get("from")!)
    : new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  const to = searchParams.get("to")
    ? new Date(searchParams.get("to")!)
    : new Date();

  const logs = await db.requestLog.findMany({
    where: {
      ...(appCode && { appCode }),
      ...(userCode && { userCode }),
      createdAt: { gte: from, lte: to },
    },
    select: {
      appCode: true,
      model: true,
      userCode: true,
      statusCode: true,
      meta: true,
    },
  });

  const usageMap = new Map<
    string,
    {
      appCode: string;
      model: string;
      userCode: string | null;
      requestCount: number;
      successCount: number;
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    }
  >();

  for (const log of logs) {
    const key = `${log.appCode}::${log.model ?? "unknown"}::${log.userCode ?? ""}`;
    const entry = usageMap.get(key) ?? {
      appCode: log.appCode,
      model: log.model ?? "unknown",
      userCode: log.userCode ?? null,
      requestCount: 0,
      successCount: 0,
      promptTokens: 0,
      completionTokens: 0,
      totalTokens: 0,
    };

    entry.requestCount += 1;
    if (log.statusCode && log.statusCode < 400) entry.successCount += 1;

    const meta = log.meta as Record<string, number> | null;
    if (meta) {
      entry.promptTokens += meta.prompt_tokens ?? 0;
      entry.completionTokens += meta.completion_tokens ?? 0;
      entry.totalTokens += meta.total_tokens ?? 0;
    }

    usageMap.set(key, entry);
  }

  return Response.json({
    from: from.toISOString(),
    to: to.toISOString(),
    data: Array.from(usageMap.values()).sort(
      (a, b) => b.totalTokens - a.totalTokens
    ),
  });
}
