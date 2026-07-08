# 登录与鉴权流程说明

## 1. 未登录用户打开 → 登录的完整流程

```
用户访问需登录页面（如 /apps）
        ↓
(admin) layout 内 await auth() → session 为 null
        ↓
redirect("/login")  →  用户看到 /login 页面
        ↓
用户点击「网易 OpenID 登录」→ 跳转 GET /api/auth/openid/login?callbackUrl=...
        ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ /api/auth/openid/login 按优先级二选一：                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ 路径 A：微前端 Header 模式（优先）                                         │
│   • 请求带 lofter-admin-open-id + lofter-admin-open-id-sign 且配置了公钥  │
│   • getMicroFrontendUser(request) 校验签名 → 得到 userInfo                │
│   • 邮件后缀白名单过滤 → openidTokenStore.set(token, userInfo)            │
│   • signIn("credentials", { openid_token: token, redirectTo })           │
│   • NextAuth authorize 用 token 从 store 取 userInfo → userFromOpenIDUserInfo 入库 → 建 Session │
│   • 浏览器 302 到 callbackUrl 或 /apps，Cookie 写入 JWT                   │
├─────────────────────────────────────────────────────────────────────────┤
│ 路径 B：标准 OpenID 重定向模式（无 MF 或校验不通过时）                     │
│   • getCachedAssoc() / fetchAssociation() 拿到 assoc                     │
│   • 302 重定向到网易 OpenID 登录页，并 Set-Cookie: _oid_assoc, _oid_callback_url │
│   • 用户在网易页登录后，OpenID Server 302 回 GET /api/auth/openid/callback?... │
│   • callback：parseAssocFromCookie + verifyOpenIDResponse → userInfo     │
│   • 邮件白名单过滤 → openidTokenStore.set(token, userInfo)               │
│   • signIn("credentials", { openid_token: token, redirectTo })          │
│   • authorize 同上 → 建 Session → 302 到 callbackUrl 或 /apps，Cookie 写入 JWT │
└─────────────────────────────────────────────────────────────────────────┘
        ↓
用户浏览器带上 session cookie，再次访问 /apps → auth() 有 session → 正常渲染
```

**涉及文件：**

- 入口/重定向：`src/app/login/page.tsx`（跳 `/api/auth/openid/login`）
- 登录 API：`src/app/api/auth/openid/login/route.ts`（MF 或 OpenID 重定向）
- 回调 API：`src/app/api/auth/openid/callback/route.ts`（仅标准 OpenID 路径会用到）
- 建 Session：`src/auth.ts`（authorize、userFromOpenIDUserInfo）

---

## 2. 请求进来后，判断用户是否登录的流程

当前**没有**使用 Next.js 的 `middleware.ts` 做全局登录校验，而是**按路由/布局**在各自处理函数里判断：

| 场景 | 判断方式 | 未登录时行为 |
|------|----------|--------------|
| **管理后台页面**（如 `/apps`、`/apps/keys`） | `(admin)/app/lofter-ai-service/layout.tsx` 内 `const session = await auth(); if (!session) redirect("/login");` | 重定向到 `/login` |
| **API Route（需登录）** | 在 handler 开头调用 `requireAdminSession(req)` 或 `requirePermission(permission, req)` | 返回 401 JSON |
| **API Route（需特定权限）** | `requirePermission("keys:view", req)` 等 | 未登录 401，已登录无权限 403 |
| **任意有 request 的 Server 逻辑** | `getSessionFromRequest(request)`，看返回值是否 null | 由调用方决定（重定向或 401） |

**判断逻辑本质：**

- 从请求中读取 **NextAuth 的 JWT**（Cookie：`authjs.session-token` 或 `__Secure-authjs.session-token`）。
- `auth()` / `getSessionFromRequest(request)` 内部都是通过 `getToken({ req, secret, cookieName, ... })` 解析该 JWT，得到 `session.user`（含 id、name、email、role）。
- 无有效 JWT → `auth()` / `getSessionFromRequest()` 返回 null → 视为未登录。

**涉及文件：**

- `src/auth.ts`：NextAuth 配置、JWT 策略
- `src/lib/auth-session.ts`：`getSessionFromRequest(request)`（读 Cookie 中的 JWT）
- `src/lib/admin-auth.ts`：`requirePermission`、`requireAdminSession`（内部用 getSession，返回 401/403）
- `src/app/(admin)/app/lofter-ai-service/layout.tsx`：页面级 `auth()` + `redirect("/login")`

---

## 3. 业务请求处理函数中获取当前用户：统一方式及用法

**结论：有统一约定，按“是否有 request”选一个用。**

| 场景 | 统一用法 | 说明 |
|------|----------|------|
| **Server Component（无 Request 对象）** | `const session = await auth();` | 从 `@/auth` 导入；读 Cookie，返回 `Session \| null`，`session.user` 即当前用户 |
| **API Route / Route Handler（有 Request）** | `const session = await getSessionFromRequest(request);` | 从 `@/lib/auth-session` 导入；读请求 Cookie 中的 JWT，返回 `Session \| null` |
| **需要“必须登录 + 权限”时** | 先 `const err = await requirePermission("xxx", request); if (err) return err;` 再取用户 | 从 `@/lib/admin-auth` 导入；未登录 401、无权限 403；通过后可用 `getSessionFromRequest(request)` 或 `getAdminEmail(request)` 取当前用户 |

**当前没有导出一个“getCurrentUser(request)”的单一函数**；统一约定就是上面两种取 Session 的方式，再从 `session.user` 拿 id/email/name/role。

**示例：**

```ts
// 在 API Route 中：仅需“当前用户是谁”
import { getSessionFromRequest } from "@/lib/auth-session";

export async function GET(request: Request) {
  const session = await getSessionFromRequest(request);
  if (!session) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  const userId = session.user?.id;
  const email = session.user?.email;
  // ...
}
```

```ts
// 在 API Route 中：需要“必须是管理员”
import { requireAdminSession, getAdminEmail } from "@/lib/admin-auth";

export async function PATCH(request: Request) {
  const unauth = await requireAdminSession(request);
  if (unauth) return unauth;

  const operatorEmail = await getAdminEmail(request); // 当前登录用户 email，用于审计
  // ...
}
```

```ts
// 在 Server Component 中（如页面、layout）
import { auth } from "@/auth";

export default async function Page() {
  const session = await auth();
  if (!session) redirect("/login");
  const user = session.user; // { id, name, email, role, ... }
  // ...
}
```

**涉及文件：**

- `@/auth`：`auth`、`getMicroFrontendUser`（后者仅登录阶段用）
- `@/lib/auth-session`：`getSessionFromRequest(request)`（业务里取当前用户的统一入口之一）
- `@/lib/admin-auth`：`requirePermission`、`requireAdminSession`、`getAdminEmail`（鉴权 + 取当前用户）
