import { db } from "@/lib/db";
import { logger } from "@/lib/logger";

/**
 * GET /readyz
 * Kubernetes Readiness Probe
 * 验证服务就绪：检查 DB 连接是否可用
 * 只有 DB 可达才返回 200，否则返回 503 让 LB 摘流
 */
export async function GET() {
  const start = Date.now();

  try {
    await db.$queryRaw`SELECT 1`;
    const duration = Date.now() - start;

    logger.debug({ duration }, "Readiness check passed");

    return Response.json(
      {
        status: "ok",
        checks: { db: "ok" },
        duration: `${duration}ms`,
      },
      { status: 200 }
    );
  } catch (err) {
    const duration = Date.now() - start;
    logger.error({ err, duration }, "Readiness check failed: DB unreachable");

    return Response.json(
      {
        status: "error",
        checks: { db: "fail" },
        duration: `${duration}ms`,
      },
      { status: 503 }
    );
  }
}
