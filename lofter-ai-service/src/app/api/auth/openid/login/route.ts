import { NextResponse } from "next/server";
import {
  fetchAssociation,
  buildLoginUrl,
  signAssocForCookie,
} from "@/lib/openid";
import { getCachedAssoc, setCachedAssoc } from "@/lib/openid-token-store";
import { env } from "@/lib/env";

/**
 * GET /api/auth/openid/login
 *
 * 标准 OpenID 重定向模式：获取/复用关联数据，跳转至网易 OpenID 登录页，
 * 并将关联数据通过签名 Cookie 随浏览器带到 callback。
 *
 * 注：微前端 Header 认证已由 middleware + getAuthUser() 独立处理，
 * 无需经过此登录流程。
 */
export async function GET(request: Request) {
  const requestUrl = new URL(request.url);
  const proto = requestUrl.protocol.replace(":", "");
  const host =
    request.headers.get("x-forwarded-host") ||
    request.headers.get("host") ||
    requestUrl.host;
  const origin = `${proto}://${host}`;
  const callbackUrl = requestUrl.searchParams.get("callbackUrl");

  // ── 标准 OpenID 重定向模式 ────────────────────────────────────────────────
  try {
    // 复用未过期的内存缓存，避免频繁向 OpenID Server 发起关联请求
    let assoc = getCachedAssoc();
    if (!assoc) {
      assoc = await fetchAssociation();
      setCachedAssoc(assoc);
    }

    const returnTo = `${origin}/api/auth/openid/callback`;
    const realm = origin + "/";

    const loginUrl = buildLoginUrl({ assocHandle: assoc.assocHandle, returnTo, realm });

    const response = NextResponse.redirect(loginUrl);

    // 将 assoc 数据写入签名的 httpOnly Cookie，用于 callback 阶段的本地 HMAC 验证
    // sameSite=lax 允许跨站顶层导航（OpenID 回跳）携带 Cookie
    response.cookies.set("_oid_assoc", signAssocForCookie(assoc, env.auth.secret), {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 86400,
      path: "/",
    });

    // 将登录前的目标 URL 存入 Cookie，callback 完成后跳转回去
    if (callbackUrl) {
      response.cookies.set("_oid_callback_url", callbackUrl, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
        maxAge: 86400,
        path: "/",
      });
    }

    return response;
  } catch (err) {
    console.error("[OpenID] Failed to initiate login:", err);
    const errorUrl = new URL("/login", origin);
    errorUrl.searchParams.set("error", "openid_init_failed");
    return NextResponse.redirect(errorUrl.toString());
  }
}
