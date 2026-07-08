import { db } from "@/lib/db";
import { cronConfig } from "@/lib/env";
import { logger } from "@/lib/logger";

/**
 * POST /api/cron/cleanup-logs
 *
 * 清理超过保留期的请求日志。
 * 通过 Authorization: Bearer {CRON_SECRET} 进行鉴权。
 *
 * 使用分批删除（每批 2000 条）避免大批量 DELETE 导致长时间锁表。
 *
 * 调用示例（crontab 每天凌晨 3 点执行）：
 *   0 3 * * * curl -s -X POST https://your-domain/api/cron/cleanup-logs \
 *     -H "Authorization: Bearer $CRON_SECRET" >> /var/log/cleanup-logs.log 2>&1
 */
export async function POST(req: Request) {
  // 鉴权
  const authHeader = req.headers.get("authorization") ?? "";
  const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : "";
  if (!token || token !== cronConfig.secret) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }

  const retentionDays = cronConfig.logRetentionDays;
  const cutoff = new Date(Date.now() - retentionDays * 24 * 60 * 60 * 1000);

  logger.info({ cutoff, retentionDays }, "Starting request log cleanup");

  const BATCH_SIZE = 2000;
  let totalDeleted = 0;

  try {
    // 分批删除，避免一次性大量 DELETE 锁表
    while (true) {
      // 先查出待删除的 id（走索引 idx_request_logs_created_at）
      const ids = await db.requestLog.findMany({
        where: { createdAt: { lt: cutoff } },
        select: { id: true },
        take: BATCH_SIZE,
      });

      if (ids.length === 0) break;

      const { count } = await db.requestLog.deleteMany({
        where: { id: { in: ids.map((r) => r.id) } },
      });

      totalDeleted += count;
      logger.info({ batchDeleted: count, totalDeleted }, "Deleted log batch");

      // 本批不足 BATCH_SIZE，说明已删完
      if (ids.length < BATCH_SIZE) break;
    }

    logger.info({ totalDeleted, cutoff, retentionDays }, "Request log cleanup completed");

    return Response.json({
      ok: true,
      deletedCount: totalDeleted,
      cutoff: cutoff.toISOString(),
      retentionDays,
    });
  } catch (err) {
    logger.error({ err }, "Request log cleanup failed");
    return Response.json({ error: "Internal server error" }, { status: 500 });
  }
}
