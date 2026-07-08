import { getToken } from "next-auth/jwt";
import type { Session } from "next-auth";
import { env } from "@/lib/env";

/**
 * 请求中获取当前登录用户的方式（汇总）：
 *
 * 链路 A：标准 OpenID 登录（创建 JWT Session）
 *   - 登录：/api/auth/openid/login → OpenID Server → callback → openid_token → Credentials.authorize → JWT Cookie
 *   - Server Component：getAuthUser()        // 来自 @/auth，先读 JWT，再读 MF Header
 *   - API Route：requirePermission(perm, req) // admin-auth 内部依次尝试 JWT → MF Header
 *
 * 链路 B：微前端 Header 认证（无 JWT，每请求独立验签）
 *   - middleware：检测 Header 存在则透传（不创建 Session，不重定向）
 *   - Server Component：getAuthUser()        // 来自 @/auth，JWT 无则从 Header 实时验签
 *   - API Route：getMicroFrontendSession(req) // 来自 @/auth，Header 验签 + DB 查询
 *
 * 入库逻辑统一：两条链路都经 userFromOpenIDUserInfo 按 email upsert User。
 */

/**
 * 从请求中获取 Session，支持两种认证方式：
 * 1. Cookie：NextAuth 默认的 session cookie
 * 2. Header：Authorization: Bearer <token>，适用于 API 客户端、脚本等无法携带 Cookie 的场景
 *
 * @param request - NextRequest 或 Request，需包含 headers（Cookie 或 Authorization）
 * @returns Session 或 null
 */
export async function getSessionFromRequest(
  request: Request
): Promise<Session | null> {
  // Cookie 名称取决于请求是否走 HTTPS，而非 NODE_ENV。
  // 本地使用 next start 时 NODE_ENV=production 但协议仍为 http，
  // 若误用 __Secure- 前缀会导致找不到 cookie，返回 Unauthorized。
  const isHttps = new URL(request.url).protocol === "https:";
  const cookieName = isHttps
    ? "__Secure-authjs.session-token"
    : "authjs.session-token";

  const token = await getToken({
    req: request,
    secret: env.auth.secret,
    secureCookie: isHttps,
    cookieName,
    salt: cookieName,
  });

  if (!token) return null;

  return {
    user: {
      id: token.sub ?? "",
      name: token.name ?? null,
      email: token.email ?? null,
      image: token.picture ?? null,
      role: token.role as "admin" | "developer" | "guest" | undefined,
    },
    expires: token.exp ? new Date(token.exp * 1000).toISOString() : "",
  };
}
