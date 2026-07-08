import { getToken } from "next-auth/jwt";
import { NextRequest } from "next/server";
import { env } from "@/lib/env";

const SESSION_COOKIE_NAME =
  process.env.NODE_ENV === "production"
    ? "__Secure-authjs.session-token"
    : "authjs.session-token";

/**
 * GET /api/auth/session-token
 *
 * 返回当前登录用户的 Session JWT，供 API 客户端在 Header 中使用：
 *   Authorization: Bearer <token>
 *
 * 使用场景：
 * 1. 用户通过浏览器完成 OpenID 登录后，Cookie 中已有 session
 * 2. 调用本接口（需携带 Cookie）获取 token
 * 3. 在 curl、Postman、脚本等场景中使用 Authorization: Bearer <token> 调用其他 API
 *
 * 未登录时返回 401
 */
export async function GET(request: NextRequest) {
  const token = await getToken({
    req: request,
    secret: env.auth.secret,
    secureCookie: process.env.NODE_ENV === "production",
    cookieName: SESSION_COOKIE_NAME,
    salt: SESSION_COOKIE_NAME,
    raw: true,
  });

  if (!token) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }

  return Response.json({ token });
}
