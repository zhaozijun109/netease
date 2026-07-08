# Skill 服务代理框架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 将 ai-service 扩展为通用 Skill 服务代理平台，支持 HTTP 代理和 DB 查询两类后端服务，通过独立的 `skt-` Token 鉴权，大结果集自动上传 NOS。

**Architecture:** 新增 `/svc/{serviceCode}/*` 路由平行于现有 `/v1/*`，复用 LRU 缓存和请求日志机制。核心模块：SkillToken 验证、TokenProvider（Auth 动态刷新）、HttpProxyHandler、DbQueryHandler、NosClient。Admin Dashboard 新增服务管理页和 Skill Token 自助申请页。

**Tech Stack:** Next.js 16 + Prisma 6 + mysql2 + ExcelJS + @nos-sdk/nos-node-sdk，TypeScript 全程类型安全。

---

## 文件结构

**新增文件：**
- `prisma/schema.prisma` — 新增 2 个 model（SkillToken, Service；ServiceCredential 已合并入 Service）
- `src/lib/skill-token.ts` — skt- Token 验证 + LRU 缓存
- `src/lib/token-provider.ts` — Auth Key → 动态 Token 自动刷新
- `src/lib/nos-client.ts` — NOS 上传封装
- `src/lib/http-proxy-handler.ts` — HTTP 服务代理转发
- `src/lib/db-query-handler.ts` — Doris/MySQL SQL 执行 + 大结果集处理
- `src/lib/svc-logger.ts` — /svc/* 专用请求日志（复用 RequestLog 表）
- `src/app/svc/[serviceCode]/[...path]/route.ts` — 代理路由入口
- `src/app/api/admin/services/route.ts` — 服务列表/创建
- `src/app/api/admin/services/[code]/route.ts` — 服务详情/更新/删除
- `src/app/api/admin/services/[code]/credential/route.ts` — 凭据管理
- `src/app/api/admin/services/[code]/test/route.ts` — 连通性测试
- `src/app/api/admin/skill-tokens/route.ts` — 管理员 Token 列表/吊销
- `src/app/api/admin/skill-tokens/[id]/route.ts` — 管理员吊销单个 Token
- `src/app/api/skill-tokens/mine/route.ts` — 用户自助申请/查看/吊销
- `src/app/(admin)/services/page.tsx` — 服务管理 UI 页面
- `src/app/(admin)/skill-tokens/page.tsx` — Skill Token UI 页面

**修改文件：**
- `prisma/schema.prisma` — 追加新 model
- `src/lib/permissions.ts` — 新增 `services:manage`、`skill-tokens:manage` 权限
- `src/lib/env.ts` — 新增 NOS 配置
- `src/app/(admin)/layout.tsx` 或导航组件 — 新增菜单项
- `.env.example`（如有）或 `.env` — 新增 NOS 环境变量

---

## Task 1: 安装依赖 + 新增环境变量

**Files:**
- Modify: `package.json`
- Modify: `.env`（或 `.env.local`）
- Modify: `src/lib/env.ts`

- [x] **Step 1: 安装三个新依赖**

```bash
cd /Users/liushichuan/Documents/code/ai-service
pnpm add mysql2 exceljs @nos-sdk/nos-node-sdk
```

预期输出：`Done in ...s`，`package.json` dependencies 中出现三个新包。

- [x] **Step 2: 在 `.env` 追加 NOS 配置**

在 `.env`（或 `.env.local`）末尾追加：

```env
# NOS（大结果集 Excel 导出）
NOS_ACCESS_KEY=
NOS_ACCESS_SECRET=
NOS_ENDPOINT=
NOS_BUCKET=lofter
NOS_HOST=
NOS_OBJECT_ORIGIN=
```

- [x] **Step 3: 在 `src/lib/env.ts` 追加 nosConfig**

在文件末尾 `export const env = {` 块之前插入：

```typescript
/** NOS 对象存储配置（大结果集 Excel 导出用） */
export const nosConfig = {
  get accessKey() { return getEnvVar("NOS_ACCESS_KEY"); },
  get accessSecret() { return getEnvVar("NOS_ACCESS_SECRET"); },
  get endpoint() { return getEnvVar("NOS_ENDPOINT"); },
  get bucket() { return getEnvVar("NOS_BUCKET", "lofter"); },
  get host() { return getEnvVar("NOS_HOST"); },
  get objectOrigin() { return getEnvVar("NOS_OBJECT_ORIGIN"); },
} as const;
```

并在 `export const env = { ... }` 中追加 `nos: nosConfig`。

- [x] **Step 4: 验证 TypeScript 编译无报错**

```bash
pnpm exec tsc --noEmit
```

预期：无错误输出。

- [x] **Step 5: Commit**

```bash
git add package.json pnpm-lock.yaml src/lib/env.ts .env
git commit -m "安装 mysql2/exceljs/nos-sdk 依赖，新增 NOS 环境变量配置"
```

---

## Task 2: 扩展 Prisma Schema（3 个新 Model）

**Files:**
- Modify: `prisma/schema.prisma`

- [x] **Step 1: 在 schema.prisma 末尾追加 SkillToken model**

```prisma
/// Skill 侧鉴权 Token（skt- 前缀，独立于 AIGW ApiKey）
model SkillToken {
  id          BigInt    @id @default(autoincrement())
  token       String    @unique @db.VarChar(128)
  name        String    @db.VarChar(128)
  userId      BigInt    @map("user_id")
  permissions Json
  status      Int       @default(1) @db.TinyInt
  expiresAt   DateTime? @map("expires_at")
  createdAt   DateTime  @default(now()) @map("created_at")
  updatedAt   DateTime  @updatedAt @map("updated_at")

  user User @relation(fields: [userId], references: [id])

  @@index([userId], map: "idx_skill_tokens_user_id")
  @@map("ai_skill_tokens")
}
```

- [x] **Step 2: 追加 Service model**

```prisma
/// 注册的后端服务端点
model Service {
  id        BigInt   @id @default(autoincrement())
  code      String   @unique @db.VarChar(64)
  name      String   @db.VarChar(128)
  type      String   @db.VarChar(32)
  config    Json
  status    Int      @default(1) @db.TinyInt
  createdAt DateTime @default(now()) @map("created_at")
  updatedAt DateTime @updatedAt @map("updated_at")

  credential ServiceCredential?

  @@map("ai_services")
}
```

- [x] **Step 3: 追加 ServiceCredential model**

```prisma
/// 服务凭据（持有后端服务的鉴权信息）
model ServiceCredential {
  id                   BigInt    @id @default(autoincrement())
  serviceId            BigInt    @unique @map("service_id")
  type                 String    @db.VarChar(32)
  config               Json
  cachedToken          String?   @map("cached_token") @db.Text
  cachedTokenExpiresAt DateTime? @map("cached_token_expires_at")
  updatedAt            DateTime  @updatedAt @map("updated_at")

  service Service @relation(fields: [serviceId], references: [id])

  @@map("ai_service_credentials")
}
```

- [x] **Step 4: 在 User model 中追加 skillTokens 关系字段**

在 `model User { ... }` 的 `apiKeys ApiKey[]` 行下方追加：

```prisma
  skillTokens SkillToken[]
```

- [x] **Step 5: 推送 schema 到数据库**

```bash
pnpm db:push
```

预期：`Your database is now in sync with your Prisma schema.`

- [x] **Step 6: 重新生成 Prisma Client**

```bash
pnpm prisma:generate
```

- [x] **Step 7: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 8: Commit**

```bash
git add prisma/schema.prisma src/generated/
git commit -m "新增 SkillToken/Service/ServiceCredential 三张表的 Prisma schema"
```

---

## Task 3: 新增权限 + SkillToken 验证模块

**Files:**
- Modify: `src/lib/permissions.ts`
- Create: `src/lib/skill-token.ts`

- [x] **Step 1: 在 permissions.ts 中扩展 Permission 类型**

在 `export type Permission =` 末尾追加两个权限（注意保留最后一项的分号）：

```typescript
  | "services:manage"       // 管理注册服务（admin/ops）
  | "skill-tokens:manage";  // 管理员查看/吊销所有 Skill Token
```

在 `ROLE_PERMISSIONS` 中更新：

```typescript
const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  admin:     ["apps:view", "apps:manage", "keys:view", "keys:manage", "stats:view", "users:manage", "services:manage", "skill-tokens:manage"],
  developer: ["apps:view",               "keys:view", "keys:manage", "stats:view"],
  ops:       ["apps:view",               "keys:view", "keys:manage", "stats:view", "services:manage", "skill-tokens:manage"],
  guest:     [                                                        "stats:view"],
};
```

- [x] **Step 2: 创建 `src/lib/skill-token.ts`**

```typescript
import crypto from "crypto";
import { LRUCache } from "lru-cache";
import { db } from "@/lib/db";

export type CachedSkillTokenData = {
  id: bigint;
  userId: bigint;
  userEmail: string | null;
  permissions: string[];
};

const skillTokenCache = new LRUCache<string, CachedSkillTokenData>({
  max: 500,
  ttl: 1000 * 60 * 5,
});

export async function validateSkillToken(
  rawToken: string
): Promise<CachedSkillTokenData | null> {
  const cached = skillTokenCache.get(rawToken);
  if (cached) return cached;

  const record = await db.skillToken.findUnique({
    where: { token: rawToken },
    include: { user: { select: { email: true } } },
  });

  if (!record || record.status === 0) return null;
  if (record.expiresAt && record.expiresAt < new Date()) return null;

  const data: CachedSkillTokenData = {
    id: record.id,
    userId: record.userId,
    userEmail: record.user.email,
    permissions: record.permissions as string[],
  };

  skillTokenCache.set(rawToken, data);
  return data;
}

export function hasServicePermission(
  tokenData: CachedSkillTokenData,
  serviceCode: string
): boolean {
  return tokenData.permissions.includes("*") || tokenData.permissions.includes(serviceCode);
}

export function invalidateSkillToken(token: string): void {
  skillTokenCache.delete(token);
}

export function generateSkillToken(): string {
  return `skt-${crypto.randomBytes(24).toString("hex")}`;
}
```

- [x] **Step 3: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 4: Commit**

```bash
git add src/lib/permissions.ts src/lib/skill-token.ts
git commit -m "新增 services/skill-tokens 权限，实现 SkillToken 验证模块"
```

---

## Task 4: TokenProvider + NosClient + HttpProxyHandler

**Files:**
- Create: `src/lib/token-provider.ts`
- Create: `src/lib/nos-client.ts`
- Create: `src/lib/http-proxy-handler.ts`

- [x] **Step 1: 创建 `src/lib/token-provider.ts`**

```typescript
import { db } from "@/lib/db";
import type { ServiceCredential } from "@/generated/prisma";

// 内存缓存：credentialId → { token, expiresAt }
const tokenMemCache = new Map<bigint, { token: string; expiresAt: Date }>();

const BUFFER_MS = 10 * 60 * 1000; // 提前 10 分钟视为过期

type AuthKeyConfig = { account: string; key: string; authUrl: string; project?: string };
type StaticTokenConfig = { token: string; headerName: string };

/**
 * 获取后端服务鉴权 Token。
 * - auth_key 类型：自动刷新，两层缓存（内存 + DB）
 * - static_token 类型：直接返回配置中的 token
 * - db_password 类型：不适用，返回 null
 */
export async function getServiceToken(
  credential: ServiceCredential
): Promise<string | null> {
  if (credential.type === "static_token") {
    return (credential.config as StaticTokenConfig).token;
  }
  if (credential.type !== "auth_key") return null;

  const now = Date.now();

  // 1. 内存缓存
  const mem = tokenMemCache.get(credential.id);
  if (mem && mem.expiresAt.getTime() - now > BUFFER_MS) {
    return mem.token;
  }

  // 2. DB 缓存
  if (
    credential.cachedToken &&
    credential.cachedTokenExpiresAt &&
    credential.cachedTokenExpiresAt.getTime() - now > BUFFER_MS
  ) {
    tokenMemCache.set(credential.id, {
      token: credential.cachedToken,
      expiresAt: credential.cachedTokenExpiresAt,
    });
    return credential.cachedToken;
  }

  // 3. 调 Auth API 刷新
  const cfg = credential.config as AuthKeyConfig;
  const res = await fetch(`${cfg.authUrl}/api/v2/tokens`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ user: cfg.account, key: cfg.key, ttl: 86400 }),
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) {
    throw new Error(`Auth API 刷新 Token 失败: ${res.status} ${await res.text()}`);
  }
  const data = (await res.json()) as { token: string };
  const newToken = data.token;
  const expiresAt = new Date(now + 86400 * 1000 - BUFFER_MS);

  // 4. 写 DB + 内存缓存
  await db.serviceCredential.update({
    where: { id: credential.id },
    data: { cachedToken: newToken, cachedTokenExpiresAt: expiresAt },
  });
  tokenMemCache.set(credential.id, { token: newToken, expiresAt });

  return newToken;
}

/** 服务重启或凭据更新时清除内存缓存 */
export function invalidateTokenCache(credentialId: bigint): void {
  tokenMemCache.delete(credentialId);
}
```

- [x] **Step 2: 创建 `src/lib/nos-client.ts`**

```typescript
import { nosConfig } from "@/lib/env";

// eslint-disable-next-line @typescript-eslint/no-require-imports
const { NosClient } = require("@nos-sdk/nos-node-sdk") as {
  NosClient: new (opts: Record<string, unknown>) => {
    putObject(opts: { objectKey: string; body: Buffer; length: number }): Promise<void>;
  };
};

let _client: ReturnType<typeof NosClient> | null = null;

function getClient() {
  if (!_client) {
    _client = new NosClient({
      accessKey: nosConfig.accessKey,
      accessSecret: nosConfig.accessSecret,
      endpoint: nosConfig.endpoint,
      host: nosConfig.host,
      defaultBucket: nosConfig.bucket,
      protocol: "https",
    });
  }
  return _client;
}

/**
 * 上传 Buffer 到 NOS，返回公开 CDN URL
 * @param buffer 文件内容
 * @param objectKey 对象路径，如 ai-service/query-results/2026-04-14/abc.xlsx
 */
export async function uploadToNos(buffer: Buffer, objectKey: string): Promise<string> {
  const client = getClient();
  await client.putObject({ objectKey, body: buffer, length: buffer.length });
  return `${nosConfig.objectOrigin}/${objectKey}`;
}
```

- [x] **Step 3: 创建 `src/lib/http-proxy-handler.ts`**

```typescript
import { getServiceToken } from "@/lib/token-provider";
import type { Service, ServiceCredential } from "@/generated/prisma";

type AuthKeyConfig = { account: string; project?: string };
type StaticTokenConfig = { headerName: string };
type HttpProxyConfig = { baseUrl: string; timeout?: number; maxTimeout?: number };

const STRIP_HEADERS = new Set(["content-encoding", "content-length", "transfer-encoding", "connection", "keep-alive"]);

/**
 * 将请求转发到注册的 HTTP 服务，自动注入鉴权 Header
 */
export async function handleHttpProxy(
  req: Request,
  service: Service,
  credential: ServiceCredential,
  pathSegments: string[]
): Promise<Response> {
  const cfg = service.config as HttpProxyConfig;
  const defaultTimeout = cfg.timeout ?? 30000;
  const maxTimeout = cfg.maxTimeout ?? 120000;

  const requestedTimeout = parseInt(req.headers.get("X-Query-Timeout") ?? "0", 10);
  const timeoutMs = requestedTimeout > 0
    ? Math.min(requestedTimeout, maxTimeout)
    : defaultTimeout;

  // 获取鉴权 Token
  const token = await getServiceToken(credential);

  // 构造转发 Headers
  const headers = new Headers();
  // 透传 Content-Type 等安全 headers
  for (const [k, v] of req.headers.entries()) {
    if (!["authorization", "host"].includes(k.toLowerCase())) {
      headers.set(k, v);
    }
  }

  // 注入后端服务鉴权
  if (token) {
    if (credential.type === "auth_key") {
      const authCfg = credential.config as AuthKeyConfig;
      headers.set("X-Access-Token", token);
      headers.set("X-Auth-User", authCfg.account);
      if (authCfg.project) headers.set("X-Auth-Project", authCfg.project);
    } else if (credential.type === "static_token") {
      const stCfg = credential.config as StaticTokenConfig;
      headers.set(stCfg.headerName, token);
    }
  }

  const targetUrl = `${cfg.baseUrl}/${pathSegments.join("/")}`;
  const upstream = await fetch(targetUrl, {
    method: req.method,
    headers,
    body: req.body,
    signal: AbortSignal.timeout(timeoutMs),
    // @ts-expect-error Node fetch duplex
    duplex: "half",
  });

  const responseHeaders = new Headers();
  for (const [k, v] of upstream.headers.entries()) {
    if (!STRIP_HEADERS.has(k.toLowerCase())) responseHeaders.set(k, v);
  }

  return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
}
```

- [x] **Step 4: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 5: Commit**

```bash
git add src/lib/token-provider.ts src/lib/nos-client.ts src/lib/http-proxy-handler.ts
git commit -m "实现 TokenProvider、NosClient、HttpProxyHandler 核心模块"
```

---

## Task 5: DbQueryHandler + SvcLogger

**Files:**
- Create: `src/lib/db-query-handler.ts`
- Create: `src/lib/svc-logger.ts`

- [x] **Step 1: 创建 `src/lib/db-query-handler.ts`**

```typescript
import { randomUUID } from "crypto";
import { format } from "date-fns";
import mysql from "mysql2/promise";
import ExcelJS from "exceljs";
import { uploadToNos } from "@/lib/nos-client";
import type { Service, ServiceCredential } from "@/generated/prisma";

type DbQueryConfig = {
  host: string; port: number; database: string;
  queryTimeout?: number; maxTimeout?: number; largeResultThreshold?: number;
};
type DbPasswordConfig = { username: string; password: string };

// 连接池缓存，按 serviceId
const pools = new Map<bigint, mysql.Pool>();

function getPool(serviceId: bigint, cfg: DbQueryConfig, cred: DbPasswordConfig): mysql.Pool {
  if (!pools.has(serviceId)) {
    pools.set(serviceId, mysql.createPool({
      host: cfg.host, port: cfg.port, database: cfg.database,
      user: cred.username, password: cred.password,
      connectionLimit: 5, connectTimeout: 10000,
    }));
  }
  return pools.get(serviceId)!;
}

const SELECT_RE = /^\s*(\/\*[\s\S]*?\*\/\s*)*\s*SELECT\s/i;

export type DbQueryResult =
  | { type: "inline"; columns: string[]; rows: unknown[][]; rowCount: number; truncated: boolean }
  | { type: "file"; url: string; rowCount: number };

/**
 * 执行 SQL 查询，自动处理大结果集（上传 NOS）
 */
export async function handleDbQuery(
  req: Request,
  service: Service,
  credential: ServiceCredential
): Promise<Response> {
  const body = await req.json() as { sql?: string; database?: string };
  if (!body.sql) {
    return Response.json({ error: "sql is required" }, { status: 400 });
  }
  if (!SELECT_RE.test(body.sql)) {
    return Response.json({ error: "只允许 SELECT 查询" }, { status: 400 });
  }

  const cfg = service.config as DbQueryConfig;
  const cred = credential.config as DbPasswordConfig;
  const threshold = cfg.largeResultThreshold ?? 1000;
  const defaultTimeout = cfg.queryTimeout ?? 60000;
  const maxTimeout = cfg.maxTimeout ?? 300000;
  const requestedTimeout = parseInt(req.headers.get("X-Query-Timeout") ?? "0", 10);
  const timeoutMs = requestedTimeout > 0 ? Math.min(requestedTimeout, maxTimeout) : defaultTimeout;

  const pool = getPool(service.id, cfg, cred);
  const conn = await pool.getConnection();

  try {
    await conn.query(`SET SESSION MAX_EXECUTION_TIME=${timeoutMs}`);
    const db = body.database ?? cfg.database;
    if (db) await conn.query(`USE \`${db}\``);

    const [rows, fields] = await conn.query({ sql: body.sql, rowsAsArray: true }) as [unknown[][], mysql.FieldPacket[]];
    const columns = fields.map((f) => f.name);
    const allRows = rows as unknown[][];

    // 截断保护
    const truncated = allRows.length > 50000;
    const safeRows = truncated ? allRows.slice(0, 50000) : allRows;

    if (safeRows.length <= threshold) {
      const result: DbQueryResult = { type: "inline", columns, rows: safeRows, rowCount: safeRows.length, truncated };
      return Response.json(result);
    }

    // 大结果集：生成 Excel 上传 NOS
    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet("result");
    sheet.addRow(columns);
    for (const row of safeRows) sheet.addRow(row as ExcelJS.Row);

    const buf = Buffer.from(await workbook.xlsx.writeBuffer());
    const date = format(new Date(), "yyyy-MM-dd");
    const objectKey = `ai-service/query-results/${date}/${randomUUID()}.xlsx`;
    const url = await uploadToNos(buf, objectKey);

    const result: DbQueryResult = { type: "file", url, rowCount: safeRows.length };
    return Response.json(result);
  } finally {
    conn.release();
  }
}
```

- [x] **Step 2: 创建 `src/lib/svc-logger.ts`**

```typescript
import { db } from "@/lib/db";
import { logger } from "@/lib/logger";

export type SvcLogContext = {
  serviceCode: string;
  userEmail: string | null;
  path: string;
  startTime: number;
};

/**
 * 异步写 /svc/* 请求日志，复用 ai_request_logs 表。
 * appCode 格式：svc:{serviceCode}，统计页面可通过前缀区分。
 */
export function logSvcRequest(
  ctx: SvcLogContext,
  statusCode: number,
  meta?: Record<string, unknown>
): void {
  db.requestLog.create({
    data: {
      appCode: `svc:${ctx.serviceCode}`,
      userCode: ctx.userEmail,
      path: ctx.path,
      statusCode,
      latencyMs: Date.now() - ctx.startTime,
      meta: meta ?? undefined,
    },
  }).catch((err) => {
    logger.warn({ err }, "Failed to write svc request log");
  });
}
```

- [x] **Step 3: 安装 date-fns（ExcelJS 使用）**

```bash
pnpm add date-fns
pnpm exec tsc --noEmit
```

- [x] **Step 4: Commit**

```bash
git add src/lib/db-query-handler.ts src/lib/svc-logger.ts
git commit -m "实现 DbQueryHandler（SQL 执行 + NOS 大结果集导出）和 SvcLogger"
```

---

## Task 6: /svc 代理路由入口

**Files:**
- Create: `src/app/svc/[serviceCode]/[...path]/route.ts`

- [x] **Step 1: 创建路由文件（骨架）**

```typescript
import type { NextRequest } from "next/server";
import { validateSkillToken, hasServicePermission } from "@/lib/skill-token";
import { handleHttpProxy } from "@/lib/http-proxy-handler";
import { handleDbQuery } from "@/lib/db-query-handler";
import { logSvcRequest } from "@/lib/svc-logger";
import { db } from "@/lib/db";

type RouteParams = { params: Promise<{ serviceCode: string; path: string[] }> };

async function handleSvc(req: NextRequest, { params }: RouteParams): Promise<Response> {
  const startTime = Date.now();
  const { serviceCode, path } = await params;

  // Step 1: 验证 Skill Token
  const authHeader = req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  const rawToken = authHeader.slice(7).trim();
  const tokenData = await validateSkillToken(rawToken);
  if (!tokenData) {
    return Response.json({ error: "Unauthorized" }, { status: 401 });
  }
  if (!hasServicePermission(tokenData, serviceCode)) {
    return Response.json({ error: "Forbidden: no permission for this service" }, { status: 403 });
  }

  // Step 2: 查服务（含凭据）
  const service = await db.service.findUnique({
    where: { code: serviceCode },
    include: { credential: true },
  });
  if (!service || service.status === 0) {
    return Response.json({ error: `Service "${serviceCode}" not found` }, { status: 404 });
  }
  if (!service.credential) {
    return Response.json({ error: `Service "${serviceCode}" has no credential configured` }, { status: 503 });
  }

  // Step 3: 按 type 分发
  let response: Response;
  try {
    if (service.type === "http_proxy") {
      response = await handleHttpProxy(req, service, service.credential, path ?? []);
    } else if (service.type === "db_query") {
      response = await handleDbQuery(req, service, service.credential);
    } else {
      response = Response.json({ error: `Unknown service type: ${service.type}` }, { status: 500 });
    }
  } catch (err) {
    const isTimeout = err instanceof Error && err.name === "TimeoutError";
    logSvcRequest({ serviceCode, userEmail: tokenData.userEmail, path: req.nextUrl.pathname, startTime }, isTimeout ? 504 : 502);
    return Response.json({ error: isTimeout ? "Gateway Timeout" : "Bad Gateway" }, { status: isTimeout ? 504 : 502 });
  }

  // Step 4: 异步写日志
  const meta: Record<string, unknown> = {};
  logSvcRequest({ serviceCode, userEmail: tokenData.userEmail, path: req.nextUrl.pathname, startTime }, response.status, meta);

  return response;
}

export const GET = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const POST = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const PUT = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const PATCH = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
export const DELETE = (req: NextRequest, ctx: RouteParams) => handleSvc(req, ctx);
```

- [x] **Step 2: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 3: 手动测试路由存在（无需真实后端）**

```bash
pnpm dev
# 另开终端
curl -s http://localhost:3000/svc/test-svc/foo \
  -H "Authorization: Bearer invalid-token" | jq .
# 预期: { "error": "Unauthorized" }
```

- [x] **Step 4: Commit**

```bash
git add src/app/svc/
git commit -m "新增 /svc/{serviceCode}/* 代理路由入口，完成鉴权 + 分发逻辑"
```

---

## Task 7: Admin API — 服务管理

**Files:**
- Create: `src/app/api/admin/services/route.ts`
- Create: `src/app/api/admin/services/[code]/route.ts`
- Create: `src/app/api/admin/services/[code]/credential/route.ts`
- Create: `src/app/api/admin/services/[code]/test/route.ts`

- [x] **Step 1: 创建 `src/app/api/admin/services/route.ts`**

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

// GET /api/admin/services — 服务列表
export async function GET(req: Request) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;

  const services = await db.service.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true, code: true, name: true, type: true,
      status: true, createdAt: true,
      credential: { select: { type: true, updatedAt: true } },
    },
  });
  return json(services);
}

// POST /api/admin/services — 创建服务
export async function POST(req: Request) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;

  const body = await req.json() as { code?: string; name?: string; type?: string; config?: unknown };
  if (!body.code || !body.name || !body.type || !body.config) {
    return json({ error: "code, name, type, config are required" }, { status: 400 });
  }
  if (!["http_proxy", "db_query"].includes(body.type)) {
    return json({ error: 'type must be "http_proxy" or "db_query"' }, { status: 400 });
  }

  const exists = await db.service.findUnique({ where: { code: body.code } });
  if (exists) return json({ error: `Service code "${body.code}" already exists` }, { status: 409 });

  const service = await db.service.create({
    data: { code: body.code, name: body.name, type: body.type, config: body.config as object },
  });
  return json(service, { status: 201 });
}
```

- [x] **Step 2: 创建 `src/app/api/admin/services/[code]/route.ts`**

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

type Params = { params: Promise<{ code: string }> };

// GET /api/admin/services/{code}
export async function GET(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({
    where: { code },
    include: { credential: { select: { id: true, type: true, updatedAt: true } } },
  });
  if (!service) return json({ error: "Not found" }, { status: 404 });
  return json(service);
}

// PUT /api/admin/services/{code}
export async function PUT(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const body = await req.json() as { name?: string; config?: unknown; status?: number };
  const service = await db.service.update({
    where: { code },
    data: {
      ...(body.name && { name: body.name }),
      ...(body.config && { config: body.config as object }),
      ...(body.status !== undefined && { status: body.status }),
    },
  });
  return json(service);
}

// DELETE /api/admin/services/{code}
export async function DELETE(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  await db.service.delete({ where: { code } });
  return json({ ok: true });
}
```

- [x] **Step 3: 创建 `src/app/api/admin/services/[code]/credential/route.ts`**

> **⚠️ 重构说明：此文件在实现过程中已删除。**
> 数据模型重构后（凭据字段合并入 `ai_services` 表），独立的 credential 路由不再需要。
> 凭据现通过 `PUT /api/admin/services/{code}`（Step 2）统一更新，body 包含 `credentialType` + `credentialConfig`。

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";
import { invalidateTokenCache } from "@/lib/token-provider";

type Params = { params: Promise<{ code: string }> };

// GET — 返回脱敏的凭据信息（不含 key/password 明文）
export async function GET(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({ where: { code }, include: { credential: true } });
  if (!service) return json({ error: "Service not found" }, { status: 404 });
  if (!service.credential) return json(null);
  const { cachedToken: _, config: __, ...safe } = service.credential;
  // 返回 config 中脱敏版本（保留 account/headerName 等非敏感字段，遮盖 key/password）
  const cfg = service.credential.config as Record<string, unknown>;
  const safeCfg = Object.fromEntries(
    Object.entries(cfg).map(([k, v]) =>
      ["key", "password", "token"].includes(k) ? [k, "****"] : [k, v]
    )
  );
  return json({ ...safe, config: safeCfg });
}

// PUT — 创建或更新凭据
export async function PUT(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({ where: { code } });
  if (!service) return json({ error: "Service not found" }, { status: 404 });
  const body = await req.json() as { type?: string; config?: unknown };
  if (!body.type || !body.config) return json({ error: "type and config are required" }, { status: 400 });

  const credential = await db.serviceCredential.upsert({
    where: { serviceId: service.id },
    create: { serviceId: service.id, type: body.type, config: body.config as object },
    update: { type: body.type, config: body.config as object, cachedToken: null, cachedTokenExpiresAt: null },
  });
  // 清除内存中的 Token 缓存
  invalidateTokenCache(credential.id);
  return json({ ok: true });
}
```

- [x] **Step 4: 创建 `src/app/api/admin/services/[code]/test/route.ts`**

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";
import mysql from "mysql2/promise";

type Params = { params: Promise<{ code: string }> };

// POST /api/admin/services/{code}/test — 连通性测试
export async function POST(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({ where: { code }, include: { credential: true } });
  if (!service || !service.credential) return json({ error: "Service or credential not found" }, { status: 404 });

  try {
    if (service.type === "http_proxy") {
      const cfg = service.config as { baseUrl: string };
      const res = await fetch(cfg.baseUrl, { method: "HEAD", signal: AbortSignal.timeout(5000) });
      return json({ ok: true, status: res.status });
    }
    if (service.type === "db_query") {
      const cfg = service.config as { host: string; port: number; database: string };
      const cred = service.credential.config as { username: string; password: string };
      const conn = await mysql.createConnection({
        host: cfg.host, port: cfg.port, database: cfg.database,
        user: cred.username, password: cred.password, connectTimeout: 5000,
      });
      await conn.query("SELECT 1");
      await conn.end();
      return json({ ok: true });
    }
    return json({ error: "Unknown service type" }, { status: 400 });
  } catch (err) {
    return json({ ok: false, error: String(err) }, { status: 200 });
  }
}
```

- [x] **Step 5: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 6: Commit**

```bash
git add src/app/api/admin/services/
git commit -m "新增服务管理 Admin API（CRUD + 凭据管理 + 连通性测试）"
```

---

## Task 8: Admin API — Skill Token 管理 + 用户自助申请

**Files:**
- Create: `src/app/api/admin/skill-tokens/route.ts`
- Create: `src/app/api/admin/skill-tokens/[id]/route.ts`
- Create: `src/app/api/skill-tokens/mine/route.ts`

- [x] **Step 1: 创建 `src/app/api/admin/skill-tokens/route.ts`**

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

// GET /api/admin/skill-tokens — 管理员查看所有 Token
export async function GET(req: Request) {
  const unauth = await requirePermission("skill-tokens:manage", req);
  if (unauth) return unauth;

  const tokens = await db.skillToken.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true, name: true, token: true, permissions: true,
      status: true, expiresAt: true, createdAt: true,
      user: { select: { email: true, name: true } },
    },
  });
  // 脱敏：只展示前 8 位
  return json(tokens.map((t) => ({ ...t, token: `${t.token.slice(0, 8)}****` })));
}
```

- [x] **Step 2: 创建 `src/app/api/admin/skill-tokens/[id]/route.ts`**

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { invalidateSkillToken } from "@/lib/skill-token";
import { json } from "@/lib/utils";

type Params = { params: Promise<{ id: string }> };

// DELETE /api/admin/skill-tokens/{id} — 强制吊销
export async function DELETE(req: Request, { params }: Params) {
  const unauth = await requirePermission("skill-tokens:manage", req);
  if (unauth) return unauth;
  const { id } = await params;

  const record = await db.skillToken.findUnique({ where: { id: BigInt(id) } });
  if (!record) return json({ error: "Not found" }, { status: 404 });

  await db.skillToken.update({ where: { id: BigInt(id) }, data: { status: 0 } });
  invalidateSkillToken(record.token);
  return json({ ok: true });
}
```

- [x] **Step 3: 创建 `src/app/api/skill-tokens/mine/route.ts`**

```typescript
import { db } from "@/lib/db";
import { getSessionFromRequest } from "@/lib/auth-session";
import { getMicroFrontendSession } from "@/auth";
import { generateSkillToken, invalidateSkillToken } from "@/lib/skill-token";
import { json } from "@/lib/utils";

async function getSession(req: Request) {
  const session = await getSessionFromRequest(req);
  if (session) return session;
  return getMicroFrontendSession(req);
}

// GET /api/skill-tokens/mine — 查看自己的 Token 状态
export async function GET(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  const token = await db.skillToken.findFirst({
    where: { userId: user.id, status: 1 },
    select: { id: true, name: true, permissions: true, status: true, expiresAt: true, createdAt: true },
    orderBy: { createdAt: "desc" },
  });
  return json(token ?? null);
}

// POST /api/skill-tokens/mine — 申请新 Token（吊销旧的）
export async function POST(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  // 吊销旧 Token
  const oldTokens = await db.skillToken.findMany({ where: { userId: user.id, status: 1 } });
  for (const t of oldTokens) {
    await db.skillToken.update({ where: { id: t.id }, data: { status: 0 } });
    invalidateSkillToken(t.token);
  }

  // 生成新 Token
  const rawToken = generateSkillToken();
  const record = await db.skillToken.create({
    data: {
      token: rawToken,
      name: `${session.user.email} 的 Skill Token`,
      userId: user.id,
      permissions: ["*"],
    },
    select: { id: true, token: true, name: true, permissions: true, createdAt: true },
  });

  // 完整 Token 仅此一次返回
  return json(record, { status: 201 });
}

// DELETE /api/skill-tokens/mine — 自助吊销
export async function DELETE(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  const tokens = await db.skillToken.findMany({ where: { userId: user.id, status: 1 } });
  for (const t of tokens) {
    await db.skillToken.update({ where: { id: t.id }, data: { status: 0 } });
    invalidateSkillToken(t.token);
  }
  return json({ ok: true });
}
```

- [x] **Step 4: 验证编译**

```bash
pnpm exec tsc --noEmit
```

- [x] **Step 5: Commit**

```bash
git add src/app/api/admin/skill-tokens/ src/app/api/skill-tokens/
git commit -m "新增 Skill Token 管理 API（管理员吊销 + 用户自助申请）"
```

---

## Task 9: Admin Dashboard UI（服务管理页 + Skill Token 页）

**Files:**
- Create: `src/app/(admin)/services/page.tsx`
- Create: `src/app/(admin)/skill-tokens/page.tsx`
- Modify: `src/app/(admin)/layout.tsx` 或导航组件（追加菜单项）

- [x] **Step 1: 确认导航组件路径**

```bash
find /Users/liushichuan/Documents/code/ai-service/src/app/\(admin\) -name "*.tsx" | head -20
```

找到 sidebar/nav 组件，定位需要修改的文件。

- [x] **Step 2: 在导航中追加「服务管理」和「Skill Token」菜单项**

找到导航链接数组，追加：

```typescript
{ href: "/services", label: "服务管理", icon: ServerIcon },
{ href: "/skill-tokens", label: "Skill Token", icon: KeyRoundIcon },
```

（图标从 `lucide-react` 导入：`import { Server as ServerIcon, KeyRound as KeyRoundIcon } from "lucide-react"`）

- [x] **Step 3: 创建 `src/app/(admin)/services/page.tsx`**

服务管理页骨架（Server Component，数据服务端获取）：

```typescript
import { getAuthUser } from "@/auth";
import { db } from "@/lib/db";
import { hasPermission, type UserRole } from "@/lib/permissions";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { redirect } from "next/navigation";

export default async function ServicesPage() {
  const user = await getAuthUser();
  const role = (user?.role ?? "guest") as UserRole;
  if (!hasPermission(role, "services:manage")) redirect("/403");

  const services = await db.service.findMany({
    orderBy: { createdAt: "desc" },
    include: { credential: { select: { type: true, updatedAt: true } } },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">服务管理</h1>
        <p className="text-sm text-muted-foreground mt-1">
          注册和管理 Skill 可调用的后端服务端点
        </p>
      </div>
      <div className="rounded-md border overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50">
              <TableHead>Code</TableHead>
              <TableHead>名称</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>凭据</TableHead>
              <TableHead>状态</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {services.map((s) => (
              <TableRow key={s.id.toString()}>
                <TableCell className="font-mono text-xs">{s.code}</TableCell>
                <TableCell>{s.name}</TableCell>
                <TableCell>
                  <Badge variant="outline">{s.type}</Badge>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {s.credential ? s.credential.type : "未配置"}
                </TableCell>
                <TableCell>
                  <Badge variant={s.status === 1 ? "default" : "secondary"}>
                    {s.status === 1 ? "启用" : "停用"}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      {services.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">
          暂无注册服务，请通过 Admin API 创建
        </p>
      )}
    </div>
  );
}
```

- [x] **Step 4: 创建 `src/app/(admin)/skill-tokens/page.tsx`**

```typescript
import { getAuthUser } from "@/auth";
import { db } from "@/lib/db";
import { hasPermission, type UserRole } from "@/lib/permissions";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

export default async function SkillTokensPage() {
  const user = await getAuthUser();
  const role = (user?.role ?? "guest") as UserRole;
  const isAdmin = hasPermission(role, "skill-tokens:manage");

  // 管理员看所有，普通用户只看自己的
  const tokens = isAdmin
    ? await db.skillToken.findMany({
        orderBy: { createdAt: "desc" },
        include: { user: { select: { email: true, name: true } } },
      })
    : user?.email
      ? await db.skillToken.findMany({
          where: { user: { email: user.email } },
          orderBy: { createdAt: "desc" },
        })
      : [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Skill Token</h1>
          <p className="text-sm text-muted-foreground mt-1">
            {isAdmin ? "管理所有用户的 Skill 访问凭证" : "管理你的 Skill 访问凭证"}
          </p>
        </div>
      </div>
      {!isAdmin && (
        <div className="rounded-lg border bg-muted/40 px-4 py-3 text-sm space-y-2">
          <p className="font-medium">申请 Skill Token</p>
          <p className="text-muted-foreground">
            通过 <code className="font-mono text-xs bg-muted px-1 rounded">POST /api/skill-tokens/mine</code> 申请，Token 仅在创建时展示一次。
          </p>
          <p className="text-muted-foreground text-xs">
            配置到 Skill CLI：<code className="font-mono">python lofter_data.py config api_key "skt-xxx"</code>
          </p>
        </div>
      )}
      <div className="rounded-md border overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50">
              {isAdmin && <TableHead>用户</TableHead>}
              <TableHead>Token（脱敏）</TableHead>
              <TableHead>权限</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tokens.map((t) => (
              <TableRow key={t.id.toString()}>
                {isAdmin && (
                  <TableCell className="text-xs text-muted-foreground">
                    {"user" in t ? (t as { user: { email: string } }).user.email : ""}
                  </TableCell>
                )}
                <TableCell className="font-mono text-xs">{t.token.slice(0, 8)}****</TableCell>
                <TableCell className="text-xs">{JSON.stringify(t.permissions)}</TableCell>
                <TableCell>
                  <Badge variant={t.status === 1 ? "default" : "secondary"}>
                    {t.status === 1 ? "有效" : "已吊销"}
                  </Badge>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {t.createdAt.toLocaleDateString("zh-CN")}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      {tokens.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">暂无 Token</p>
      )}
    </div>
  );
}
```

- [x] **Step 5: 验证编译 + 本地检查页面渲染**

```bash
pnpm exec tsc --noEmit
pnpm dev
# 浏览器访问 http://localhost:3000/services 和 /skill-tokens
# 确认页面正常渲染，未登录时重定向到登录页
```

- [x] **Step 6: Commit**

```bash
git add src/app/\(admin\)/services/ src/app/\(admin\)/skill-tokens/
git commit -m "新增服务管理页和 Skill Token 页 Admin Dashboard UI"
```

---

## 自检：Spec 覆盖验证

| Spec 要求 | 对应 Task |
|-----------|----------|
| 2 张新 DB 表（SkillToken + Service，凭据已合并入 Service） | Task 2 |
| skt- Token 验证 + LRU 缓存 | Task 3 |
| Auth Key → Token 动态刷新 | Task 4 |
| NOS 上传封装 | Task 4 |
| HTTP 代理转发 + Header 注入 | Task 4 |
| DB 查询 + 大结果集 NOS 导出 | Task 5 |
| /svc/* 路由入口 + 权限检查 | Task 6 |
| 服务 Admin API（CRUD + 凭据 + 测试） | Task 7 |
| Skill Token Admin API + 用户自助申请 | Task 8 |
| Dashboard UI（服务管理 + Skill Token 页） | Task 9 |
| NOS 环境变量配置 | Task 1 |
| 请求日志复用 ai_request_logs | Task 5（SvcLogger） |

---

## Reconciliation Log

> 记录实际实现与原计划的差异，由文档更新 Agent 于 2026-04-14 同步。所有 Task 均已完成（checkbox 已全部勾选）。

| # | 差异点 | 原计划 | 实际实现 |
|---|--------|--------|----------|
| 1 | **Prisma Schema** | 新增 3 个 model（SkillToken、Service、ServiceCredential） | 新增 2 个 model（SkillToken、Service），凭据字段（`credentialType`、`credentialConfig`、`cachedToken`、`cachedTokenExpiresAt`）直接合并入 Service，`ai_service_credentials` 表未创建 |
| 2 | **Task 7 Step 3** | 创建独立的 `credential/route.ts` 文件 | 该文件已删除；凭据通过 `PUT /api/admin/services/{code}` 统一管理 |
| 3 | **TokenProvider** | `getToken(credential: ServiceCredential)` | `getServiceToken(service: Service)`，内存缓存 key 改为 `service.id` |
| 4 | **HttpProxyHandler** | `handleHttpProxy(req, service, credential, pathSegments)` | 删除 `credential` 参数，从 `service.credentialType`/`service.credentialConfig` 直接读取 |
| 5 | **DbQueryHandler** | `handleDbQuery(req, service, credential)` | 删除 `credential` 参数，从 `service.credentialConfig` 直接读取 |
| 6 | **Admin UI 组件** | `page.tsx` 骨架（仅服务器渲染列表） | 新增了 `add-service-dialog.tsx`、`service-actions.tsx`、`apply-token-dialog.tsx`、`revoke-token-button.tsx` 等完整交互组件 |
| 7 | **安全防护** | 未提及 | PUT 接口增加 `credentialConfig` 脱敏占位符检测，含 `"****"` 时返回 400 |
| 8 | **NOS 环境变量** | 写入 `.env` 或 `.env.local` | 实际同步写入 `.env.test`、`.env.pre`、`.env.prod` 三个环境 |
