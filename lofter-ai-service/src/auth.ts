import NextAuth from "next-auth";
import Credentials from "next-auth/providers/credentials";
import { headers } from "next/headers";
import type { Session } from "next-auth";
import { openidTokenStore } from "@/lib/openid-token-store";
import { db } from "@/lib/db";
import { env } from "@/lib/env";
import { verifyMicroFrontendHeaders } from "@/lib/openid";
import type { OpenIDUserInfo } from "@/lib/openid";

const MF_OPEN_ID_HEADER = "lofter-admin-open-id";
const MF_OPEN_ID_SIGN_HEADER = "lofter-admin-open-id-sign";

/**
 * 校验微前端 Header（LOFTER-ADMIN-OPEN-ID / LOFTER-ADMIN-OPEN-ID-SIGN）并返回当前登录用户信息。
 * 仅依赖 Header + 公钥验签，与 NextAuth session / JWT 完全无关。
 * 未配置公钥、缺少 Header 或校验失败时返回 null。
 */
export function getMicroFrontendUser(request: Request): OpenIDUserInfo | null {
  const openIdHeader = request.headers.get(MF_OPEN_ID_HEADER);
  const openIdSignHeader = request.headers.get(MF_OPEN_ID_SIGN_HEADER);
  const mfPublicKey = env.openid.microFrontendPublicKey;
  if (!openIdHeader || !openIdSignHeader || !mfPublicKey) return null;
  try {
    return verifyMicroFrontendHeaders(
      openIdHeader,
      openIdSignHeader,
      mfPublicKey,
      env.openid.microFrontendSignTimeoutMs
    );
  } catch {
    return null;
  }
}

/** 根据 OpenID 用户信息 upsert 库表并返回用户对象 */
async function userFromOpenIDUserInfo(
  userInfo: OpenIDUserInfo
): Promise<{
  id: string;
  name: string | null;
  email: string;
  role: "admin" | "developer" | "guest";
} | null> {
  const adminEmails = env.auth.adminEmails;
  const isAdminEmail = adminEmails.includes(userInfo.email);

  const dbUser = await db.user.upsert({
    where: { email: userInfo.email },
    create: {
      email: userInfo.email,
      name: userInfo.fullname || userInfo.nickname || null,
      role: isAdminEmail ? "admin" : "guest",
    },
    update: {
      name: userInfo.fullname || userInfo.nickname || null,
    },
    select: { id: true, role: true },
  });

  return {
    id: String(dbUser.id),
    name: userInfo.fullname || userInfo.nickname || null,
    email: userInfo.email,
    role: dbUser.role as "admin" | "developer" | "guest",
  };
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  trustHost: true,
  providers: [
    Credentials({
      credentials: {
        openid_token: { label: "OpenID Token", type: "text" },
      },
      async authorize(credentials) {
        // openid_token 桥接（标准 OpenID 回调传入）
        const token = credentials?.openid_token as string | undefined;
        if (!token) return null;

        const userInfo = openidTokenStore.get(token);
        if (!userInfo) return null;

        openidTokenStore.delete(token);
        return userFromOpenIDUserInfo(userInfo);
      },
    }),
  ],
  pages: {
    signIn: "/login",
  },
  session: {
    strategy: "jwt",
    maxAge: 60 * 60 * 24 * 7, // 7 天
  },
  callbacks: {
    async jwt({ token, user }) {
      if (user?.role) {
        // 首次登录：直接写入 role
        token.role = user.role;
      } else if (token.sub) {
        // 后续刷新：从 DB 实时读取最新 role，确保管理员手动修改角色后立即生效
        const dbUser = await db.user.findUnique({
          where: { id: BigInt(token.sub) },
          select: { role: true },
        });
        if (dbUser) {
          token.role = dbUser.role;
        }
      }
      return token;
    },
    session({ session, token }) {
      if (session.user) {
        session.user.role = token.role as "admin" | "developer" | "guest" | undefined;
      }
      return session;
    },
  },
});

/**
 * 从请求中获取微前端 Header 认证的 Session。
 * 每次请求独立验签，不依赖 JWT Cookie。
 * 供 API Route 的权限校验（admin-auth）使用。
 */
export async function getMicroFrontendSession(
  request: Request
): Promise<Session | null> {
  const mfUserInfo = getMicroFrontendUser(request);
  if (!mfUserInfo) return null;
  const user = await userFromOpenIDUserInfo(mfUserInfo);
  if (!user) return null;
  return {
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      image: null,
      role: user.role,
    },
    expires: "",
  };
}

/**
 * 获取当前登录用户（Server Component 专用）。
 *
 * 优先级：
 *   1. NextAuth JWT Session（标准 OpenID 登录）
 *   2. 微前端 Header 实时验签（无需 JWT Cookie，每请求独立校验）
 *
 * 两条链路完全独立，仅共用 userFromOpenIDUserInfo 构造 user 结构。
 */
export async function getAuthUser() {
  // 路径 1：标准 JWT Session
  const session = await auth();
  if (session?.user) return session.user;

  // 路径 2：微前端 Header 实时验签
  const headersList = await headers();
  const openIdHeader = headersList.get(MF_OPEN_ID_HEADER);
  const signHeader = headersList.get(MF_OPEN_ID_SIGN_HEADER);
  const mfPublicKey = env.openid.microFrontendPublicKey;
  if (!openIdHeader || !signHeader || !mfPublicKey) return null;
  try {
    const mfUserInfo = verifyMicroFrontendHeaders(
      openIdHeader,
      signHeader,
      mfPublicKey,
      env.openid.microFrontendSignTimeoutMs
    );
    return userFromOpenIDUserInfo(mfUserInfo);
  } catch {
    return null;
  }
}
