import { getToken } from "next-auth/jwt";
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const MF_OPEN_ID_HEADER = "lofter-admin-open-id";
const MF_OPEN_ID_SIGN_HEADER = "lofter-admin-open-id-sign";

export async function middleware(request: NextRequest) {
  const { pathname, search } = request.nextUrl;

  // 检查 JWT Session（Edge 兼容，直接读 Cookie 验签）
  const isHttps = request.nextUrl.protocol === "https:";
  const cookieName = isHttps
    ? "__Secure-authjs.session-token"
    : "authjs.session-token";

  const token = await getToken({
    req: request,
    secret: process.env.NEXTAUTH_SECRET!,
    secureCookie: isHttps,
    cookieName,
    salt: cookieName,
  });

  if (token) return NextResponse.next();

  // 微前端 Header 存在时直接信任，透传请求（完整签名校验在 Server Component / API Route 中完成）
  if (
    request.headers.get(MF_OPEN_ID_HEADER) &&
    request.headers.get(MF_OPEN_ID_SIGN_HEADER)
  ) {
    return NextResponse.next();
  }

  // 无任何认证凭据，跳转到登录页
  const callbackUrl = encodeURIComponent(pathname + search);
  return NextResponse.redirect(
    new URL(`/login?callbackUrl=${callbackUrl}`, request.url)
  );
}

export const config = {
  matcher: [
    "/apps/:path*",
    "/stats/:path*",
    "/keys/:path*",
    "/users/:path*",
    "/aigw-apis/:path*",
  ],
};
