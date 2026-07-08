# AI Service 通用 Skill 服务代理框架 设计方案

## 背景与目标

### 现状

ai-service 当前是一个 AIGW 专用网关，`/v1/*` 路由将请求转发到 AIGW，具备 API Key 鉴权、用量统计、RBAC 等完整能力。

LOFTER 部门内部的 AI Skill（如 lofter-data-analyst）需要访问多种内部服务：
- **brainmaker 知识库**：HTTP API，需要网易 Auth 系统动态 Token 鉴权
- **Doris 查询引擎**：MySQL 协议，账号密码认证
- 未来可能扩展：Hive、ES、内部 HTTP 服务等

这些服务的凭据不能直接暴露给 Skill 侧（本地 CLI），需要一个统一的代理层来持有凭据并做鉴权。

### 目标

将 ai-service 从「AIGW 专用网关」扩展为「通用 Skill 服务代理平台」：
1. Skill 侧只持有一个 `skt-` 格式的平台 Token，无需感知后端服务的凭据细节
2. 平台统一持有各后端服务的凭据，包括自动刷新网易 Auth Token
3. 支持两类后端服务：HTTP 代理转发型、DB 直连查询型
4. 大结果集自动导出 Excel 上传 NOS，返回 CDN URL

---

## 整体架构

```
Skill CLI（本地）
  │  Authorization: Bearer skt-xxx
  ▼
┌─────────────────────────────────────────────────────┐
│  ai-service（扩展后）                                │
│                                                     │
│  /v1/*          → 现有 AIGW 代理（不变）              │
│  /svc/{code}/*  → 通用服务代理（新增）                │
│                                                     │
│  ┌─ Skill Token 鉴权 ──────────────────────────┐    │
│  │  skt- Token → LRU 缓存 → DB 验证             │    │
│  │  检查 status、expiresAt、permissions          │    │
│  └──────────────────────────────────────────────┘    │
│                                                     │
│  ┌─ 服务路由分发 ───────────────────────────────┐    │
│  │  http_proxy → HttpProxyHandler               │    │
│  │  db_query   → DbQueryHandler                 │    │
│  └──────────────────────────────────────────────┘    │
│                                                     │
│  ┌─ Token Provider（Auth 自动刷新）────────────┐    │
│  │  内存缓存 → DB 缓存 → Auth API 刷新          │    │
│  └──────────────────────────────────────────────┘    │
│                                                     │
│  ┌─ NOS Client（大结果集导出）─────────────────┐    │
│  │  Excel 生成 → NOS 上传 → CDN URL            │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
       │                        │
       ▼                        ▼
  brainmaker                 Doris
  （HTTP + Auth Token）      （MySQL 协议）
```

---

## 数据模型

### 新增表 1：`ai_skill_tokens` — Skill 侧鉴权 Token

```prisma
model SkillToken {
  id          BigInt    @id @default(autoincrement())
  token       String    @unique @db.VarChar(128)   // skt- 前缀
  name        String    @db.VarChar(128)
  userId      BigInt    @map("user_id")
  permissions Json                                  // ["*"] 或 ["brainmaker", "doris"]
  status      Int       @default(1) @db.TinyInt     // 1=有效 0=禁用
  expiresAt   DateTime? @map("expires_at")
  createdAt   DateTime  @default(now()) @map("created_at")
  updatedAt   DateTime  @updatedAt @map("updated_at")

  user User @relation(fields: [userId], references: [id])

  @@index([userId])
  @@map("ai_skill_tokens")
}
```

**与 `ai_api_keys` 的区别**：不绑定 App（AIGW），不绑定 AIGW 凭证，只控制「能调哪些注册服务」。

**分配策略**：一个用户只能有一个有效 Token，自助申请时自动吊销旧 Token。

### 新增表 2：`ai_services` — 注册服务端点（含凭据字段）

> **⚠️ 与原设计不同：** 实际实现将凭据字段直接合并到 `ai_services` 表，删除了单独的 `ai_service_credentials` 表。

```prisma
model Service {
  id                   BigInt    @id @default(autoincrement())
  code                 String    @unique @db.VarChar(64)         // 路由标识，如 brainmaker、doris
  name                 String    @db.VarChar(128)
  type                 String    @db.VarChar(32)                 // http_proxy | db_query
  config               Json                                      // 按 type 不同，见下方说明
  credentialType       String?   @map("credential_type") @db.VarChar(32) // static_token | auth_key | db_password
  credentialConfig     Json?     @map("credential_config")               // 加密存储，见下方说明
  cachedToken          String?   @map("cached_token") @db.Text
  cachedTokenExpiresAt DateTime? @map("cached_token_expires_at")
  status               Int       @default(1) @db.TinyInt
  createdAt            DateTime  @default(now()) @map("created_at")
  updatedAt            DateTime  @updatedAt @map("updated_at")

  @@map("ai_services")
}
```

`config` 结构：

```jsonc
// type = http_proxy
{
  "baseUrl": "https://ext-idc-ai.nie.netease.com",
  "timeout": 30000,           // 请求超时（ms），默认 30000
  "maxTimeout": 120000        // X-Query-Timeout 覆盖上限
}

// type = db_query
{
  "driver": "mysql",          // 固定 mysql（Doris 兼容 MySQL 协议）
  "host": "doris.internal",
  "port": 9030,
  "database": "lofter",       // 默认库
  "queryTimeout": 60000,      // SQL 执行超时（ms）
  "maxTimeout": 300000,       // X-Query-Timeout 覆盖上限
  "largeResultThreshold": 1000 // 超出此行数改为 NOS 文件导出
}
```

> **注：`ai_service_credentials` 表已删除。** 凭据字段（`credentialType`、`credentialConfig`、`cachedToken`、`cachedTokenExpiresAt`）直接存于 `ai_services` 表中。

`credentialConfig` 结构（敏感字段服务端加密存储）：

```jsonc
// type = auth_key（网易 Auth 系统，动态 Token）
{
  "account": "_lofter_ai",
  "key": "加密存储的 auth key",
  "authUrl": "http://int-auth.nie.netease.com",
  "project": "space_1774419617421"   // X-Auth-Project Header 值
}

// type = db_password（直连数据库）
{
  "username": "reader",
  "password": "加密存储的密码"
}

// type = static_token（固定 Token）
{
  "token": "加密存储的 token",
  "headerName": "X-Access-Token"
}
```

---

## 路由设计

### 新增路由

| 路由 | 方法 | 说明 |
|------|------|------|
| `/svc/{serviceCode}/{...path}` | ALL | 通用服务代理入口 |
| `/api/admin/services` | GET, POST | 服务列表 / 创建服务 |
| `/api/admin/services/{code}` | GET, PUT, DELETE | 服务详情 / 更新 / 删除 |
| ~~`/api/admin/services/{code}/credential`~~ | ~~GET, PUT~~ | ~~已删除：凭据通过 PUT /api/admin/services/{code} 统一更新~~ |
| `/api/admin/skill-tokens` | GET, POST | Skill Token 列表 / 管理员创建 |
| `/api/admin/skill-tokens/{id}` | DELETE | 吊销指定 Token |
| `/api/skill-tokens/mine` | GET, POST, DELETE | 用户自助查看 / 申请 / 吊销自己的 Token |

### `/svc/{serviceCode}/{...path}` 处理流程

```
请求到达
  │
  ├─ 提取 Authorization: Bearer skt-xxx
  ├─ LRU 缓存命中？→ 直接用
  └─ DB 查询 ai_skill_tokens
       检查 status=1、expiresAt 未过期
       检查 permissions 包含 serviceCode 或 ["*"]
  │
  ├─ 查 ai_services 表（内存缓存，5min TTL）
  │   status=1 且 code 匹配
  │
  ├─ 按 type 分发：
  │   http_proxy → HttpProxyHandler
  │   db_query   → DbQueryHandler
  │
  └─ 异步写请求日志（service_code、user_id、耗时、结果状态）
```

---

## 核心模块设计

### Token Provider（`src/lib/token-provider.ts`）

负责统一管理后端服务的鉴权凭据，对 `auth_key` 类型实现动态 Token 自动刷新。

> **参数变更：** 实际实现中参数从 `ServiceCredential` 改为 `Service`，直接从 service 的 `credentialType`/`credentialConfig` 字段读取凭据，缓存字段（`cachedToken`/`cachedTokenExpiresAt`）也直接更新到 `ai_services` 表。

```
getServiceToken(service: Service): Promise<string | null>

刷新策略：
1. 查内存 Map（key = service.id）
2. 命中且 expiresAt > now + 10min → 返回缓存 Token
3. 查 DB service.cachedToken + service.cachedTokenExpiresAt，同样判断
4. 过期或无缓存 → 调 POST {authUrl}/api/v2/tokens
   body: { user: account, key: key, ttl: 86400 }
5. 写 DB service.cachedToken + service.cachedTokenExpiresAt（now + 24h - 10min buffer）
6. 写内存 Map
7. 返回 token

其他类型：
- static_token：直接返回 credentialConfig.token
- db_password：不经过此模块，DbQueryHandler 直接使用连接信息
```

### HttpProxyHandler（`src/lib/http-proxy-handler.ts`）

> **参数变更：** 实际实现删除了独立的 `credential` 参数，改从 `service.credentialType`/`service.credentialConfig` 读取凭据信息。

```
handleHttpProxy(req, service, pathSegments):
1. 调 TokenProvider.getServiceToken(service)
2. 按 service.credentialType 注入鉴权 Header：
   auth_key → X-Access-Token: {token}
              X-Auth-User: {account}
              X-Auth-Project: {project}
   static_token → {headerName}: {token}
3. 转发请求到 service.config.baseUrl + "/" + pathSegments.join("/")
4. 超时：取 X-Query-Timeout Header（≤ maxTimeout）或默认 timeout
5. 透传响应（状态码 + body + 必要 Headers）
```

### DbQueryHandler（`src/lib/db-query-handler.ts`）

Doris 使用 MySQL 协议，通过 `mysql2` 连接池访问。

> **参数变更：** 实际实现删除了独立的 `credential` 参数，从 `service.credentialConfig` 直接读取连接凭据。

```
handleDbQuery(req, service):
请求格式：POST /svc/doris/query
Body: { sql: string, database?: string }

处理流程：
1. 检查 SQL 必须以 SELECT 开头（正则，忽略注释和大小写）
2. 取连接超时：X-Query-Timeout Header（≤ maxTimeout）或默认 queryTimeout
3. 用 mysql2 连接池执行查询（连接池按 serviceId 缓存）
4. 结果集判断：
   ≤ largeResultThreshold(1000) 行
     → 返回 { columns: string[], rows: any[][], rowCount: number }
   > 1000 行
     → 生成 xlsx（ExcelJS）
     → 上传 NOS：ai-service/query-results/{YYYY-MM-DD}/{queryId}.xlsx
     → 返回 { type: "file", url: string, rowCount: number }

安全限制：
- 只允许 SELECT 语句
- 执行超时强制终止
- 结果集上限 50000 行（超出截断并在响应中标注 truncated: true）
```

### NOS Client（`src/lib/nos-client.ts`）

```
uploadBuffer(buffer: Buffer, objectKey: string): Promise<string>

使用 @nos-sdk/nos-node-sdk，配置从环境变量读取：
NOS_ACCESS_KEY / NOS_ACCESS_SECRET / NOS_ENDPOINT / NOS_BUCKET / NOS_HOST / NOS_OBJECT_ORIGIN

返回公开 CDN URL：{NOS_OBJECT_ORIGIN}/{objectKey}
```

---

## Skill Token 自助申请链路

```
用户通过网易 OpenID 登录 Dashboard
  │
  ▼
「Skill 访问」页面（/skill-tokens）
  │  点击「申请 Skill Token」
  ▼
POST /api/skill-tokens/mine
  │  服务端逻辑：
  │  1. 取当前登录 userId
  │  2. 吊销该用户的旧有效 Token（status = 0）
  │  3. 生成新 Token：skt- + 48位随机串
  │  4. 写入 ai_skill_tokens（permissions = ["*"]）
  │  5. 返回完整 Token 明文（仅此一次）
  ▼
用户复制 Token，配置到 Skill CLI：
  python lofter_data.py config api_key "skt-xxx"
  python lofter_data.py config server_url "https://ai-service.internal"
```

**限制**：
- Token 明文仅在创建响应中返回一次，DB 存储哈希或明文（按现有 api_key 惯例保持一致）
- 一个用户同时只能有一个有效 Skill Token
- 管理员可在 `/api/admin/skill-tokens` 查看所有 Token 并强制吊销

---

## Admin Dashboard 新增页面

### 服务管理页（`/services`）

- 服务列表：code、name、type、状态
- 创建/编辑服务：通过统一表单配置 code、name、type、config JSON 及凭据（credentialType + credentialConfig），无独立凭据 Tab
- 测试连接：保存后可触发一次连通性测试（http_proxy 发送 HEAD 请求验证可达；db_query 执行 `SELECT 1`）；失败时展示错误信息，服务仍可保存但标注「连接异常」

**实际实现的交互组件：**

| 文件 | 说明 |
|------|------|
| `src/app/(admin)/services/page.tsx` | 服务管理主页（Server Component，列表 + 触发入口） |
| `src/app/(admin)/services/add-service-dialog.tsx` | 新建服务 Dialog（含凭据配置，单一表单无 Tabs） |
| `src/app/(admin)/services/service-actions.tsx` | 配置 / 测试连接 / 删除操作组件 |

> **安全防护：** PUT `/api/admin/services/{code}` 会检测 `credentialConfig` 中是否含有 `"****"` 脱敏占位符，有则返回 400 拒绝保存，防止前端回显脱敏值被误提交覆盖真实凭据。

### Skill Token 页（`/skill-tokens`，路由唯一，按角色渲染不同内容）

**管理员视图**（role = admin/ops）：
- 所有用户的 Token 列表：用户名、Token 脱敏、permissions、创建时间、状态
- 强制吊销任意用户的 Token

**普通用户视图**（role = developer/guest）：
- 仅展示自己的当前 Token 状态（有/无、是否过期）
- 「申请 / 重新申请」按钮（触发吊销旧 Token + 生成新 Token）
- Token 明文仅在生成后展示一次，附复制按钮

**实际实现的交互组件：**

| 文件 | 说明 |
|------|------|
| `src/app/(admin)/skill-tokens/page.tsx` | Skill Token 主页（按角色渲染不同内容） |
| `src/app/(admin)/skill-tokens/apply-token-dialog.tsx` | 申请 Token Dialog（含一次性明文展示 + 复制按钮） |
| `src/app/(admin)/skill-tokens/revoke-token-button.tsx` | 吊销按钮（管理员强制吊销 / 用户自助吊销） |

---

## 新增环境变量

```env
# NOS（大结果集 Excel 导出）
NOS_ACCESS_KEY=
NOS_ACCESS_SECRET=
NOS_ENDPOINT=
NOS_BUCKET=lofter
NOS_HOST=
NOS_OBJECT_ORIGIN=   # CDN 域名前缀，用于拼接公开 URL
```

> **实际部署：** NOS 环境变量已同步写入 `.env.test`、`.env.pre`、`.env.prod` 三个环境文件。

---

## 依赖新增

```json
{
  "@nos-sdk/nos-node-sdk": "^0.2.6",   // NOS 上传
  "mysql2": "^3.x",                     // Doris / MySQL 查询
  "exceljs": "^4.x"                     // Excel 文件生成
}
```

---

## 请求日志

复用现有 `ai_request_logs` 表结构，`appCode` 字段存 `svc:{serviceCode}`（如 `svc:doris`），与 AIGW 的 appCode 格式不同，统计页面可通过前缀区分。`userCode` 存 Token 关联的用户邮箱，`path` 存完整请求路径，`meta` 存查询类型（inline/file）和行数等补充信息。

---

## 不在本次范围内

- lofter-data-analyst Skill 的 SKILL.md 和 Python CLI（独立项目）
- Hive 查询引擎接入（待 Hive 接口确认后作为新 Service 注册）
- Skill Token 细粒度权限管理（当前默认全服务 `["*"]`）
- 凭据字段的加密存储（当前与现有 app_key 保持一致，明文存 DB；后续可统一加密）
---

## Reconciliation Log

> 记录实际实现与原设计方案的差异，由文档更新 Agent 于 2026-04-14 同步。

| # | 差异点 | 原设计 | 实际实现 |
|---|--------|--------|----------|
| 1 | **数据模型** | `ai_services` + `ai_service_credentials` 两张表（1:1 关系） | 合并为 `ai_services` 单张表，凭据字段（`credentialType`、`credentialConfig`、`cachedToken`、`cachedTokenExpiresAt`）直接加在 Service 上，删除 `ServiceCredential` model |
| 2 | **路由** | 独立的 `GET/PUT /api/admin/services/{code}/credential` 接口 | 该接口已删除；凭据通过 `PUT /api/admin/services/{code}` 统一更新（body 包含 `credentialType` + `credentialConfig`） |
| 3 | **TokenProvider 参数** | `getToken(credential: ServiceCredential)` | `getServiceToken(service: Service)`，直接从 service 读取凭据字段 |
| 4 | **HttpProxyHandler 参数** | `handleHttpProxy(req, service, credential, pathSegments)` | 删除独立 `credential` 参数，改用 `service.credentialType`/`service.credentialConfig` |
| 5 | **DbQueryHandler 参数** | `handleDbQuery(req, service, credential)` | 删除独立 `credential` 参数，从 `service.credentialConfig` 直接读取 |
| 6 | **Admin Dashboard 组件** | 仅描述「新增页面」，凭据配置为独立表单 | 实现了完整组件：`add-service-dialog.tsx`、`service-actions.tsx`（单一表单无 Tabs）、`apply-token-dialog.tsx`（一次性明文展示）、`revoke-token-button.tsx` |
| 7 | **安全防护** | 未提及 | PUT `/api/admin/services/{code}` 检测 `credentialConfig` 中的 `"****"` 脱敏占位符，有则返回 400 |
| 8 | **NOS 环境变量** | 仅提到 `.env` | 实际同步写入 `.env.test`、`.env.pre`、`.env.prod` 三个环境 |
