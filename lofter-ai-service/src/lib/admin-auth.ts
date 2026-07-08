import { auth, getMicroFrontendSession } from "@/auth";
import { getSessionFromRequest } from "@/lib/auth-session";
import { hasPermission, type Permission } from "@/lib/permissions";

/**
 * 获取当前请求的 Session，按优先级依次尝试：
 *   1. JWT（Cookie 或 Authorization: Bearer）
 *   2. 微前端 Header 实时验签（无需 JWT Cookie）
 * - 未传入 request 时：使用 NextAuth auth()（仅 Cookie，用于 Server Component）
 */
async function getSession(request?: Request) {
  if (request) {
    const jwtSession = await getSessionFromRequest(request);
    if (jwtSession) return jwtSession;
    return getMicroFrontendSession(request);
  }
  return auth();
}

/**
 * 检查当前请求是否拥有指定权限
 *
 * - 未登录      → 401 Unauthorized
 * - 已登录但无权 → 403 Forbidden
 * - 有权限      → 返回 null，调用方继续处理
 *
 * @param permission - 所需权限
 * @param request - 请求对象，传入时支持从 Authorization: Bearer 头验证
 */
export async function requirePermission(
  permission: Permission,
  request?: Request
): Promise<Response | null> {
  const session = await getSession(request);

  if (!session) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }

  if (!hasPermission(session.user?.role, permission)) {
    return Response.json({ error: "Forbidden" }, { status: 403 });
  }

  return null;
}

/**
 * 向下兼容：仅管理员可用（等价于 requirePermission("users:manage")）
 *
 * @param request - 请求对象，传入时支持从 Authorization: Bearer 头验证
 */
export async function requireAdminSession(
  request?: Request
): Promise<Response | null> {
  return requirePermission("users:manage", request);
}

/**
 * 返回当前登录用户的 email（用于审计日志等）
 * 调用前必须确保已通过权限校验
 *
 * @param request - 请求对象，传入时支持从 Authorization: Bearer 头验证
 */
export async function getAdminEmail(request?: Request): Promise<string | null> {
  const session = await getSession(request);
  return session?.user?.email ?? null;
}

/**
 * 返回当前登录用户的角色
 *
 * @param request - 请求对象，传入时支持从 Authorization: Bearer 头验证
 */
export async function getAdminRole(request?: Request): Promise<string | null> {
  const session = await getSession(request);
  return session?.user?.role ?? null;
}
