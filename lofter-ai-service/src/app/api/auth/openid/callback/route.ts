import { type NextRequest, NextResponse } from "next/server";
import { randomBytes } from "crypto";
import { isRedirectError } from "next/dist/client/components/redirect-error";
import { verifyOpenIDResponse, parseAssocFromCookie } from "@/lib/openid";
import { openidTokenStore } from "@/lib/openid-token-store";
import { signIn } from "@/auth";
import { env } from "@/lib/env";
import { ROUTES } from "@/lib/routes";

/**
 * GET /api/auth/openid/callback
 * 接收网易 OpenID Server 认证成功后的回跳，完成签名校验并创建登录 Session
 */
export async function GET(request: NextRequest) {
  const requestUrl = new URL(request.url);
  const { searchParams } = requestUrl;
  const origin = requestUrl.origin;

  try {
    // 从签名 Cookie 恢复 assoc（不依赖进程内存，热重载/多实例均可用）
    const assocCookieValue = request.cookies.get("_oid_assoc")?.value;
    const assoc = assocCookieValue
      ? parseAssocFromCookie(assocCookieValue, env.auth.secret)
      : null;

    if (!assoc) {
      throw new Error("登录会话已过期，请重新点击登录按钮");
    }

    const callbackAssocHandle = searchParams.get("openid.assoc_handle");
    if (callbackAssocHandle !== assoc.assocHandle) {
      throw new Error("登录状态不一致，请重新点击登录按钮");
    }

    // 本地 HMAC-SHA256 签名校验（无需再请求 OpenID Server）
    const userInfo = verifyOpenIDResponse(searchParams, assoc.macKey);

    // 邮件白名单过滤
    const allowedSuffix = env.openid.allowedEmailSuffix;
    if (allowedSuffix && userInfo.email) {
      const suffixes = allowedSuffix.split(",").map((s) => s.trim()).filter(Boolean);
      if (!suffixes.some((suffix) => userInfo.email.endsWith(suffix))) {
        throw new Error(`邮箱 ${userInfo.email} 无权访问本系统`);
      }
    }

    // 生成一次性临时 token，桥接 OpenID 回调 → NextAuth Session 创建
    const token = randomBytes(32).toString("hex");
    openidTokenStore.set(token, userInfo);

    // 读取登录前保存的目标 URL，登录成功后跳转回去；默认跳 /apps
    const savedCallbackUrl = request.cookies.get("_oid_callback_url")?.value;
    const redirectTo = savedCallbackUrl || ROUTES.home;
    const redirectToAbsolute = new URL(redirectTo, origin).toString();

    // 通过 NextAuth credentials provider 创建 JWT Session
    // signIn 内部调用 Next.js redirect()，会抛出 NEXT_REDIRECT，需在 catch 中放行
    await signIn("credentials", {
      openid_token: token,
      redirect: true,
      redirectTo: redirectToAbsolute,
    });

    return NextResponse.redirect(redirectToAbsolute);
  } catch (err) {
    if (isRedirectError(err)) throw err;

    const message = err instanceof Error ? err.message : "未知错误";
    console.error("[OpenID] Callback verification failed:", message);

    const errorUrl = new URL("/login", origin);
    errorUrl.searchParams.set("error", encodeURIComponent(message));
    return NextResponse.redirect(errorUrl.toString());
  }
}
