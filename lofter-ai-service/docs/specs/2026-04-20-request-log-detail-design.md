# 请求日志详情查询功能设计

## 背景

当前 ai-service 的统计页面能看到聚合统计（按 App/Model/User 分组的请求数和 token 数），
也能通过 `/api/admin/stats/requests` 获取请求列表，但存在以下问题：

1. **个人 Key 场景**：能按 user_code 过滤看到请求列表，但看不到请求/响应的具体内容
2. **服务 Key 场景**：能按 app_code 看到调用量，但 user_code 经常为空，也看不到具体内容
3. **缺少过滤能力**：无法按模型、状态码、时间范围、key 类型等维度筛选

## 目标

- 在现有 `meta` JSON 字段中增加 prompt/completion 摘要（各 200 字符），支持回溯任意请求的大致内容
- 增强请求日志 API 的过滤能力，覆盖 app、user、model、status、时间、key 类型等维度
- 在前端统计页增加请求日志明细区域，支持点击展开查看详情
- 新增 `stats:detail` 权限，仅管理员可查看请求日志明细
- 从零搭建 Vitest + Playwright 测试基础设施，E2E 验证完整链路

## 设计决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 摘要存储位置 | 扩展现有 meta JSON 字段 | 零 schema 迁移，JSON 天然适合可变结构，API 无需改返回结构 |
| 摘要长度 | 各 200 字符 | 能看出用户问了什么、AI 回了什么方向，存储压力小（~400 字节/条） |
| 服务 Key 用户缺失 | 不强求，缺了记 null | 不改调用方行为 |
| 日志查询入口 | 现有统计页列表点击展开 | 无需新页面，交互自然 |
| 权限控制 | 新增 stats:detail，仅 admin | 摘要含 prompt/completion 内容，需限制可见范围 |
| 数据保留 | 跟表走，不做特殊清理 | 简单，后续按需加 |
| E2E 方案 | Vitest + Playwright 全栈 | 项目内闭环，不依赖外部系统环境 |

## 详细设计

### 1. 数据采集层

**改动文件**：`src/lib/request-logger.ts`、`src/app/v1/[...path]/route.ts`

#### meta 字段新增结构

现有 meta 只有 token 统计：

```json
{ "prompt_tokens": 150, "completion_tokens": 80, "total_tokens": 230 }
```

扩展后：

```json
{
  "prompt_tokens": 150,
  "completion_tokens": 80,
  "total_tokens": 230,
  "prompt_summary": "你好，请帮我分析一下这段代码的性能问题...",
  "completion_summary": "这段代码的主要性能瓶颈在于循环中的数据库查询...",
  "request_params": {
    "temperature": 0.7,
    "max_tokens": 4096,
    "stream": false
  },
  "error_message": null
}
```

| 新增字段 | 类型 | 说明 |
|----------|------|------|
| `prompt_summary` | string \| null | 最后一条 user message content 的前 200 字符 |
| `completion_summary` | string \| null | assistant 回复 content 的前 200 字符 |
| `request_params` | object \| null | 请求参数摘要：temperature、max_tokens、stream |
| `error_message` | string \| null | 错误响应时 error.message 的前 200 字符 |

#### prompt 摘要提取

在 `route.ts` 的 `handleProxy` 中，转发前读取 request body 提取摘要：

- 在转发前先 `await req.clone().json()` 提取摘要，原始 req.body 流式透传给 AIGW 不受影响
  （当前 `handleProxy` 直接将 `req.body` 传给 `forwardToAigw`，clone 不会消费原始 body）
- 从 `messages` 数组中取最后一条 `role: "user"` 的 content
- content 为 string 时直接截取前 200 字符
- content 为数组（多模态）时，取所有 `type: "text"` 的 text 拼接后截取
- 非 chat/completions 路径（如 embeddings）：prompt_summary 为 null
- 同时提取 `temperature`、`max_tokens`、`stream` 作为 request_params

提取结果注入 `LogContext`，传递给日志函数。

#### completion 摘要提取

**非流式**：从 `responseBody.choices[0].message.content` 截取前 200 字符

**流式**：在 `logStreaming` 消费 tee'd stream 时，拼接所有 `choices[0].delta.content`，
最终截取前 200 字符。需要新增一个 buffer 变量累积 delta content。

**错误响应**（statusCode >= 400）：从 `responseBody.error.message` 截取前 200 字符，
存入 `error_message`。

#### 摘要截取工具函数

新增 `src/lib/summary-extractor.ts`，提供纯函数：

```typescript
/** 截取字符串前 maxLen 个字符，超长追加 "..." */
export function truncate(text: string | null | undefined, maxLen: number): string | null;

/** 从 OpenAI chat request body 提取 prompt 摘要 */
export function extractPromptSummary(body: unknown): string | null;

/** 从 OpenAI chat response body 提取 completion 摘要 */
export function extractCompletionSummary(body: unknown): string | null;

/** 从 OpenAI chat response body 提取 error message */
export function extractErrorMessage(body: unknown): string | null;

/** 从 request body 提取请求参数摘要 */
export function extractRequestParams(body: unknown): Record<string, unknown> | null;
```

### 2. API 层

**改动文件**：`src/app/api/admin/stats/requests/route.ts`

#### 权限变更

接口权限从 `stats:view` 改为 `stats:detail`。

#### 新增查询参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `model` | string | 按模型名精确过滤 |
| `status_code` | number | 按状态码精确过滤 |
| `status_type` | `success` \| `error` | success: statusCode < 400; error: >= 400 |
| `from` | ISO date string | 起始时间（含） |
| `to` | ISO date string | 结束时间（含） |
| `key_type` | `personal` \| `service` | 按 key 类型过滤 |

现有 `app_code` 和 `user_code` 保持不变。

#### 返回结构

不变——meta 字段本来就透传，新增的摘要字段自动包含在内。

### 3. 权限层

**改动文件**：`src/lib/permissions.ts`

新增权限 `stats:detail`，仅 admin 角色拥有：

```typescript
export type Permission =
  | ... existing ...
  | "stats:detail";  // 查看请求日志明细（含摘要）

const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  admin:     [...existing, "stats:detail"],
  developer: [...existing],  // 无 stats:detail
  guest:     ["stats:view"],  // 无 stats:detail
};
```

### 4. 前端

**改动文件**：`src/app/(admin)/stats/page.tsx`（拆出客户端组件）

#### 页面结构

在现有聚合统计下方，新增"请求日志"区域（仅 admin 可见）：

```
┌─────────────────────────────────────────┐
│  现有：概览卡片 + 按 App/Model/User 聚合  │
├─────────────────────────────────────────┤
│  新增：请求日志（仅 admin）               │
│  ┌─ 过滤栏 ─────────────────────────┐   │
│  │ App Code ▼  User Code  Model ▼   │   │
│  │ Key Type ▼  Status ▼   时间范围   │   │
│  └──────────────────────────────────┘   │
│  ┌─ 日志列表 ───────────────────────┐   │
│  │ 时间  App  User  Model  Status ms│   │
│  │ ▶ 2026-04-20 10:32  ...          │   │
│  │ ▼ 2026-04-20 10:31  ...          │   │
│  │   ┌─ 展开详情 ──────────────┐    │   │
│  │   │ Prompt: 你好，请帮我... │    │   │
│  │   │ Completion: 好的，这... │    │   │
│  │   │ Tokens: 150/80/230     │    │   │
│  │   │ Params: temp=0.7       │    │   │
│  │   │ Error: (无)            │    │   │
│  │   └────────────────────────┘    │   │
│  │ ▶ 2026-04-20 10:30  ...         │   │
│  └──────────────────────────────────┘   │
│  < 1 2 3 ... >  分页                    │
└─────────────────────────────────────────┘
```

#### 组件拆分

- `src/app/(admin)/stats/page.tsx` — Server Component，保持现有聚合统计，
  通过 role 判断是否渲染日志区域
- `src/components/request-log-table.tsx` — Client Component（新增），
  包含过滤栏、日志列表、展开/收起、分页。通过 fetch 调用 `/api/admin/stats/requests`

#### 交互细节

- 过滤栏：使用 shadcn/ui 的 Select 和 Input 组件
- 日志列表行：点击整行展开/收起
- 展开详情区：摘要文本用 monospace 字体，截断处显示 `...`
- 分页：默认 20 条/页，使用 URL searchParams 保持状态

### 5. E2E 自动化验证

#### 测试基础设施搭建

**新增依赖**：
- `vitest`（devDependency）
- `@playwright/test`（devDependency）
- Playwright 浏览器：仅安装 chromium（`pnpm exec playwright install --with-deps chromium`）

**新增配置文件**：
- `vitest.config.ts` — 路径别名（@/ → src/）、测试环境配置
- `playwright.config.ts` — baseURL、webServer（自动启动 next dev）、仅 chromium

**新增 npm scripts**：
```json
{
  "test": "vitest run",
  "test:watch": "vitest",
  "test:e2e": "playwright test",
  "test:e2e:ui": "playwright test --ui"
}
```

#### 测试分层

**第一层：Vitest 单元测试 — 纯逻辑**

`src/lib/__tests__/summary-extractor.test.ts`：
- 普通文本截取：输入 300 字符 → 输出 200 字符 + "..."
- 空值/null/undefined → 返回 null
- 多模态 content（数组）→ 拼接 text 类型后截取
- 非 chat 请求 body（无 messages）→ 返回 null
- 错误响应提取 → error.message 前 200 字符

`src/lib/__tests__/permissions.test.ts`：
- admin 有 stats:detail → true
- developer 有 stats:detail → false
- guest 有 stats:detail → false
- 所有角色有 stats:view → true

**第二层：Vitest API 集成测试 — mock AIGW + 数据库**

`src/app/api/admin/stats/__tests__/requests.test.ts`：
- 向数据库插入带摘要的测试 RequestLog
- 验证各过滤参数正确过滤
- 验证权限：mock admin session → 200；mock developer session → 403；未登录 → 401

**第三层：Playwright E2E — 真实浏览器**

`e2e/stats-log-detail.spec.ts`：
- seed 测试数据到数据库
- admin 登录 → 导航统计页 → 看到"请求日志"区域 → 点击某行展开 → 验证摘要文本可见
- developer 登录 → 统计页不显示"请求日志"区域

`e2e/log-recording.spec.ts`：
- 通过 API 发 chat/completions 请求（AIGW 被 mock）
- 等待异步日志写入
- admin 登录查看日志 → 验证 prompt_summary 和 completion_summary 与预期匹配

#### AIGW Mock 策略

新增 `src/lib/__tests__/mock-aigw.ts`，用 Node.js HTTP server 模拟 AIGW 上游：
- 非流式端点：返回固定 OpenAI 格式 JSON（含 usage）
- 流式端点：返回固定 SSE chunks（最后一个 chunk 含 usage）
- 错误端点：返回 400 + error body

通过环境变量 `AIGW_BASE_URL` 指向 mock server，Vitest 集成测试和 Playwright E2E 共用。

## 影响范围

| 文件 | 变更类型 |
|------|----------|
| `src/lib/summary-extractor.ts` | 新增 |
| `src/lib/request-logger.ts` | 修改：writeLog 接收摘要数据 |
| `src/app/v1/[...path]/route.ts` | 修改：提取 request body 摘要 |
| `src/lib/permissions.ts` | 修改：新增 stats:detail |
| `src/app/api/admin/stats/requests/route.ts` | 修改：权限 + 过滤参数 |
| `src/app/(admin)/stats/page.tsx` | 修改：条件渲染日志区域 |
| `src/components/request-log-table.tsx` | 新增 |
| `vitest.config.ts` | 新增 |
| `playwright.config.ts` | 新增 |
| `src/lib/__tests__/summary-extractor.test.ts` | 新增 |
| `src/lib/__tests__/permissions.test.ts` | 新增 |
| `src/app/api/admin/stats/__tests__/requests.test.ts` | 新增 |
| `src/lib/__tests__/mock-aigw.ts` | 新增 |
| `e2e/stats-log-detail.spec.ts` | 新增 |
| `e2e/log-recording.spec.ts` | 新增 |
| `package.json` | 修改：新增依赖和 scripts |

## 不做的事

- 不存储完整 prompt/completion 全文
- 不强制 service key 传 X-User-Code
- 不做数据保留/清理策略
- 不做 quota/rate limiting
- 不做费用计算
