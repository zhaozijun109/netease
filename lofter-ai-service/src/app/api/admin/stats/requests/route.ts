import { db } from "@/lib/db";
import { requirePermission, getAdminEmail, getAdminRole } from "@/lib/admin-auth";
import { hasPermission } from "@/lib/permissions";
import type { ApiKeyType } from "@/generated/prisma";

/**
 * GET /api/admin/stats/requests
 * 分页查询请求日志明细
 * 权限：stats:detail（admin + developer）
 *
 * - admin：可查所有用户数据
 * - developer：强制只查自己的数据（忽略 user_code 参数）
 *
 * 查询参数：
 *   app_code, user_code    — 现有（developer 忽略 user_code）
 *   model                  — 按模型名精确过滤
 *   status_code            — 按状态码精确过滤
 *   status_type            — success | error（< 400 为 success）
 *   from, to               — ISO date 时间范围
 *   key_type               — personal | service
 *   page, page_size        — 分页
 */
export async function GET(req: Request) {
  const unauth = await requirePermission("stats:detail", req);
  if (unauth) return unauth;

  const role = await getAdminRole(req);
  const isAdmin = hasPermission(role, "users:manage");

  const { searchParams } = new URL(req.url);
  const appCode = searchParams.get("app_code") ?? undefined;

  // developer 强制只查自己的数据
  let userCode: string | undefined;
  if (isAdmin) {
    userCode = searchParams.get("user_code") ?? undefined;
  } else {
    const email = await getAdminEmail(req);
    userCode = email ?? undefined;
  }
  const model = searchParams.get("model") ?? undefined;
  const statusCode = searchParams.get("status_code");
  const statusType = searchParams.get("status_type");
  const from = searchParams.get("from");
  const to = searchParams.get("to");
  const keyType = searchParams.get("key_type") as ApiKeyType | null;
  const page = Math.max(1, parseInt(searchParams.get("page") ?? "1", 10));
  const pageSize = Math.min(
    100,
    Math.max(1, parseInt(searchParams.get("page_size") ?? "20", 10))
  );

  // 构造 where 条件
  const where: Record<string, unknown> = {};
  if (appCode) where.appCode = appCode;
  if (userCode) where.userCode = userCode;
  if (model) where.model = model;
  if (keyType) where.keyType = keyType;
  if (statusCode) {
    where.statusCode = parseInt(statusCode, 10);
  } else if (statusType === "success") {
    where.statusCode = { lt: 400 };
  } else if (statusType === "error") {
    where.statusCode = { gte: 400 };
  }
  if (from || to) {
    const createdAt: Record<string, Date> = {};
    if (from) createdAt.gte = new Date(from);
    if (to) createdAt.lte = new Date(to);
    where.createdAt = createdAt;
  }

  const [total, rows] = await Promise.all([
    db.requestLog.count({ where }),
    db.requestLog.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * pageSize,
      take: pageSize,
      select: {
        id: true,
        appCode: true,
        apiKeyId: true,
        keyType: true,
        userCode: true,
        model: true,
        path: true,
        statusCode: true,
        latencyMs: true,
        meta: true,
        createdAt: true,
      },
    }),
  ]);

  // BigInt 无法被 JSON.stringify 序列化，转为 string
  const serialized = rows.map((row) => ({
    ...row,
    id: String(row.id),
    apiKeyId: row.apiKeyId ? String(row.apiKeyId) : null,
  }));

  return Response.json({
    total,
    page,
    page_size: pageSize,
    data: serialized,
  });
}
