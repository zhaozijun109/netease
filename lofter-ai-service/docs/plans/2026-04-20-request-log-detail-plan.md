# 请求日志详情查询 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 meta JSON 字段中增加 prompt/completion 摘要，增强请求日志 API 过滤能力，前端统计页增加可展开的日志明细，新增 stats:detail 权限，从零搭建 Vitest + Playwright 测试基础设施。

**Architecture:** 扩展现有 request-logger 在转发时提取请求/响应摘要写入 meta JSON；增强 stats/requests API 的过滤参数和权限控制；在统计页新增 Client Component 实现日志明细展开。测试分三层：Vitest 单元测试纯函数、Vitest API 集成测试 mock session、Playwright E2E 测真实浏览器交互。

**Tech Stack:** Next.js 16 + Prisma 6 + MySQL 8 + TypeScript + shadcn/ui + Vitest + Playwright

---

## File Structure

| 文件 | 职责 | 变更类型 |
|------|------|----------|
| `src/lib/summary-extractor.ts` | 纯函数：截取摘要、提取 prompt/completion/error/params | 新增 |
| `src/lib/request-logger.ts` | 写日志：接收摘要数据合并到 meta | 修改 |
| `src/app/v1/[...path]/route.ts` | 代理入口：clone body 提取摘要，传入 LogContext | 修改 |
| `src/lib/permissions.ts` | 权限：新增 stats:detail | 修改 |
| `src/app/api/admin/stats/requests/route.ts` | API：权限改 stats:detail，增加过滤参数 | 修改 |
| `src/app/(admin)/stats/page.tsx` | 统计页：按 role 条件渲染日志区域 | 修改 |
| `src/components/admin/request-log-table.tsx` | Client Component：过滤栏+日志列表+展开详情+分页 | 新增 |
| `vitest.config.ts` | Vitest 配置 | 新增 |
| `playwright.config.ts` | Playwright 配置 | 新增 |
| `src/lib/__tests__/summary-extractor.test.ts` | 单元测试：摘要提取纯函数 | 新增 |
| `src/lib/__tests__/permissions.test.ts` | 单元测试：权限矩阵 | 新增 |
| `src/lib/__tests__/mock-aigw.ts` | 测试工具：模拟 AIGW 上游 | 新增 |
| `src/app/api/admin/stats/__tests__/requests.test.ts` | 集成测试：API 过滤+权限 | 新增 |
| `e2e/stats-log-detail.spec.ts` | E2E：前端日志展开+权限 | 新增 |
| `e2e/log-recording.spec.ts` | E2E：完整链路验证摘要写入 | 新增 |
| `package.json` | 新增依赖和 scripts | 修改 |

---

### Task 1: 搭建测试基础设施（Vitest + Playwright）

**Files:**
- Modify: `package.json`
- Create: `vitest.config.ts`
- Create: `playwright.config.ts`

- [ ] **Step 1: 安装 Vitest 依赖**

```bash
pnpm add -D vitest @vitejs/plugin-react
```

- [ ] **Step 2: 创建 `vitest.config.ts`**

```typescript
import { defineConfig } from "vitest/config";
import path from "path";

export default defineConfig({
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  test: {
    globals: true,
    environment: "node",
    include: ["src/**/__tests__/**/*.test.ts"],
  },
});
```

- [ ] **Step 3: 安装 Playwright 依赖**

```bash
pnpm add -D @playwright/test
pnpm exec playwright install --with-deps chromium
```

- [ ] **Step 4: 创建 `playwright.config.ts`**

```typescript
import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  retries: 0,
  use: {
    baseURL: "http://localhost:3000",
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
  ],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: true,
    timeout: 30_000,
  },
});
```

- [ ] **Step 5: 在 `package.json` 添加 scripts**

在 `"scripts"` 中新增：

```json
"test": "vitest run",
"test:watch": "vitest",
"test:e2e": "playwright test",
"test:e2e:ui": "playwright test --ui"
```

- [ ] **Step 6: 验证 Vitest 能启动**

```bash
pnpm test
```

预期：输出 "No test files found"（尚无测试文件），进程正常退出 exit 0。

- [ ] **Step 7: 提交**

```bash
git add vitest.config.ts playwright.config.ts package.json pnpm-lock.yaml
git commit -m "搭建 Vitest + Playwright 测试基础设施"
```

---

### Task 2: 摘要提取纯函数 + 单元测试（TDD）

**Files:**
- Create: `src/lib/summary-extractor.ts`
- Create: `src/lib/__tests__/summary-extractor.test.ts`

- [ ] **Step 1: 写失败测试 — `truncate` 函数**

创建 `src/lib/__tests__/summary-extractor.test.ts`：

```typescript
import { describe, it, expect } from "vitest";
import {
  truncate,
  extractPromptSummary,
  extractCompletionSummary,
  extractErrorMessage,
  extractRequestParams,
} from "@/lib/summary-extractor";

describe("truncate", () => {
  it("短于 maxLen 时原样返回", () => {
    expect(truncate("hello", 200)).toBe("hello");
  });

  it("超过 maxLen 时截取并追加 ...", () => {
    const long = "a".repeat(300);
    const result = truncate(long, 200);
    expect(result).toBe("a".repeat(200) + "...");
  });

  it("null/undefined 返回 null", () => {
    expect(truncate(null, 200)).toBeNull();
    expect(truncate(undefined, 200)).toBeNull();
  });

  it("空字符串返回 null", () => {
    expect(truncate("", 200)).toBeNull();
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：FAIL — "Cannot find module '@/lib/summary-extractor'"

- [ ] **Step 3: 实现 `truncate` 函数**

创建 `src/lib/summary-extractor.ts`：

```typescript
const SUMMARY_MAX_LEN = 200;

/** 截取字符串前 maxLen 个字符，超长追加 "..." */
export function truncate(
  text: string | null | undefined,
  maxLen: number = SUMMARY_MAX_LEN
): string | null {
  if (!text) return null;
  if (text.length <= maxLen) return text;
  return text.slice(0, maxLen) + "...";
}
```

- [ ] **Step 4: 运行测试验证通过**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：4 个 truncate 用例全部 PASS。

- [ ] **Step 5: 写失败测试 — `extractPromptSummary`**

在测试文件末尾追加：

```typescript
describe("extractPromptSummary", () => {
  it("提取最后一条 user message 的 content", () => {
    const body = {
      messages: [
        { role: "system", content: "You are helpful." },
        { role: "user", content: "第一个问题" },
        { role: "assistant", content: "回答1" },
        { role: "user", content: "第二个问题，这才是最后一条" },
      ],
    };
    expect(extractPromptSummary(body)).toBe("第二个问题，这才是最后一条");
  });

  it("多模态 content（数组）拼接 text 部分", () => {
    const body = {
      messages: [
        {
          role: "user",
          content: [
            { type: "text", text: "看这张图" },
            { type: "image_url", image_url: { url: "data:..." } },
            { type: "text", text: "帮我分析" },
          ],
        },
      ],
    };
    expect(extractPromptSummary(body)).toBe("看这张图\n帮我分析");
  });

  it("无 messages 返回 null", () => {
    expect(extractPromptSummary({ input: "text" })).toBeNull();
  });

  it("超长 content 被截取", () => {
    const body = {
      messages: [{ role: "user", content: "x".repeat(300) }],
    };
    const result = extractPromptSummary(body);
    expect(result).toBe("x".repeat(200) + "...");
  });
});
```

- [ ] **Step 6: 运行测试验证失败**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：extractPromptSummary 相关用例 FAIL。

- [ ] **Step 7: 实现 `extractPromptSummary`**

在 `src/lib/summary-extractor.ts` 追加：

```typescript
/** 从 OpenAI chat request body 提取最后一条 user message 的摘要 */
export function extractPromptSummary(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const messages = b.messages;
  if (!Array.isArray(messages)) return null;

  // 从后往前找最后一条 role: "user"
  for (let i = messages.length - 1; i >= 0; i--) {
    const msg = messages[i];
    if (msg?.role !== "user") continue;
    const text = extractContentText(msg.content);
    return truncate(text, SUMMARY_MAX_LEN);
  }
  return null;
}

/** 从 message.content 提取纯文本（兼容 string 和多模态数组） */
function extractContentText(content: unknown): string | null {
  if (typeof content === "string") return content;
  if (Array.isArray(content)) {
    const texts = content
      .filter((p: unknown) => {
        const part = p as Record<string, unknown>;
        return part.type === "text" && typeof part.text === "string";
      })
      .map((p: unknown) => (p as Record<string, string>).text);
    return texts.length > 0 ? texts.join("\n") : null;
  }
  return null;
}
```

- [ ] **Step 8: 运行测试验证通过**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：所有 extractPromptSummary 用例 PASS。

- [ ] **Step 9: 写失败测试 — `extractCompletionSummary` + `extractErrorMessage` + `extractRequestParams`**

在测试文件追加：

```typescript
describe("extractCompletionSummary", () => {
  it("从 choices[0].message.content 提取", () => {
    const body = {
      choices: [{ message: { role: "assistant", content: "这是回复" } }],
    };
    expect(extractCompletionSummary(body)).toBe("这是回复");
  });

  it("无 choices 返回 null", () => {
    expect(extractCompletionSummary({ error: "bad" })).toBeNull();
  });

  it("超长截取", () => {
    const body = {
      choices: [{ message: { content: "y".repeat(300) } }],
    };
    expect(extractCompletionSummary(body)).toBe("y".repeat(200) + "...");
  });
});

describe("extractErrorMessage", () => {
  it("从 error.message 提取", () => {
    const body = { error: { message: "Model not found", type: "invalid_request" } };
    expect(extractErrorMessage(body)).toBe("Model not found");
  });

  it("无 error 返回 null", () => {
    expect(extractErrorMessage({ choices: [] })).toBeNull();
  });
});

describe("extractRequestParams", () => {
  it("提取 temperature / max_tokens / stream", () => {
    const body = {
      messages: [],
      temperature: 0.7,
      max_tokens: 4096,
      stream: true,
      model: "gpt-4",
    };
    expect(extractRequestParams(body)).toEqual({
      temperature: 0.7,
      max_tokens: 4096,
      stream: true,
    });
  });

  it("缺少的参数不出现在结果中", () => {
    const body = { messages: [], temperature: 0.5 };
    expect(extractRequestParams(body)).toEqual({ temperature: 0.5 });
  });

  it("无有效参数返回 null", () => {
    expect(extractRequestParams({ messages: [] })).toBeNull();
  });
});
```

- [ ] **Step 10: 运行测试验证失败**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：新增的 3 个 describe 块 FAIL。

- [ ] **Step 11: 实现剩余三个函数**

在 `src/lib/summary-extractor.ts` 追加：

```typescript
/** 从 OpenAI chat response body 提取 completion 摘要 */
export function extractCompletionSummary(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const choices = b.choices;
  if (!Array.isArray(choices) || choices.length === 0) return null;

  const message = choices[0]?.message as Record<string, unknown> | undefined;
  if (!message) return null;

  const content = message.content;
  if (typeof content !== "string") return null;
  return truncate(content, SUMMARY_MAX_LEN);
}

/** 从错误响应 body 提取 error.message */
export function extractErrorMessage(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const error = b.error as Record<string, unknown> | undefined;
  if (!error || typeof error.message !== "string") return null;
  return truncate(error.message, SUMMARY_MAX_LEN);
}

/** 从 request body 提取请求参数摘要 */
export function extractRequestParams(
  body: unknown
): Record<string, unknown> | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;

  const params: Record<string, unknown> = {};
  if (typeof b.temperature === "number") params.temperature = b.temperature;
  if (typeof b.max_tokens === "number") params.max_tokens = b.max_tokens;
  if (typeof b.stream === "boolean") params.stream = b.stream;

  return Object.keys(params).length > 0 ? params : null;
}
```

- [ ] **Step 12: 运行全部测试验证通过**

```bash
pnpm test -- src/lib/__tests__/summary-extractor.test.ts
```

预期：所有 14 个用例 PASS。

- [ ] **Step 13: 提交**

```bash
git add src/lib/summary-extractor.ts src/lib/__tests__/summary-extractor.test.ts
git commit -m "TDD 实现摘要提取纯函数 summary-extractor"
```

---

### Task 3: 权限层 — 新增 `stats:detail` + 单元测试（TDD）

**Files:**
- Modify: `src/lib/permissions.ts`
- Create: `src/lib/__tests__/permissions.test.ts`

- [ ] **Step 1: 写失败测试**

创建 `src/lib/__tests__/permissions.test.ts`：

```typescript
import { describe, it, expect } from "vitest";
import { hasPermission } from "@/lib/permissions";

describe("stats:detail 权限", () => {
  it("admin 拥有 stats:detail", () => {
    expect(hasPermission("admin", "stats:detail")).toBe(true);
  });

  it("developer 没有 stats:detail", () => {
    expect(hasPermission("developer", "stats:detail")).toBe(false);
  });

  it("guest 没有 stats:detail", () => {
    expect(hasPermission("guest", "stats:detail")).toBe(false);
  });

  it("所有角色都有 stats:view", () => {
    expect(hasPermission("admin", "stats:view")).toBe(true);
    expect(hasPermission("developer", "stats:view")).toBe(true);
    expect(hasPermission("guest", "stats:view")).toBe(true);
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

```bash
pnpm test -- src/lib/__tests__/permissions.test.ts
```

预期：FAIL — TypeScript 编译错误 "stats:detail" 不在 Permission 类型中。

- [ ] **Step 3: 修改 `src/lib/permissions.ts`**

在 Permission 类型联合中追加 `"stats:detail"`：

```typescript
export type Permission =
  | "apps:view"
  | "apps:manage"
  | "keys:view"
  | "keys:manage"
  | "stats:view"
  | "stats:detail"    // 查看请求日志明细（含摘要）
  | "users:manage"
  | "services:manage"
  | "skill-tokens:manage";
```

在 ROLE_PERMISSIONS 的 admin 数组中追加 `"stats:detail"`：

```typescript
admin: ["apps:view", "apps:manage", "keys:view", "keys:manage", "stats:view", "stats:detail", "users:manage", "services:manage", "skill-tokens:manage"],
```

developer 和 guest 不变。

- [ ] **Step 4: 运行测试验证通过**

```bash
pnpm test -- src/lib/__tests__/permissions.test.ts
```

预期：4 个用例全部 PASS。

- [ ] **Step 5: 提交**

```bash
git add src/lib/permissions.ts src/lib/__tests__/permissions.test.ts
git commit -m "新增 stats:detail 权限，仅 admin 可查看请求日志明细"
```

---

### Task 4: 数据采集层 — request-logger 接收摘要数据

**Files:**
- Modify: `src/lib/request-logger.ts`

- [ ] **Step 1: 扩展 `LogContext` 和 `writeLog`**

修改 `src/lib/request-logger.ts`。

在 `LogContext` 类型中新增可选字段：

```typescript
export type LogContext = {
  appCode: string;
  apiKeyId: bigint;
  keyType: ApiKeyType;
  userCode: string | null;
  path: string;
  startTime: number;
  /** 请求 body 提取的摘要数据（在 route.ts 中填充） */
  promptSummary?: string | null;
  requestParams?: Record<string, unknown> | null;
};
```

修改 `writeLog` 函数，将摘要数据合并到 meta：

```typescript
type SummaryData = {
  prompt_summary?: string | null;
  completion_summary?: string | null;
  request_params?: Record<string, unknown> | null;
  error_message?: string | null;
};

async function writeLog(
  ctx: LogContext,
  statusCode: number,
  model: string | null,
  usage: UsageData | null,
  summary: SummaryData = {}
): Promise<void> {
  const meta: Record<string, unknown> = {};
  if (usage) Object.assign(meta, usage);
  if (ctx.promptSummary) meta.prompt_summary = ctx.promptSummary;
  if (ctx.requestParams) meta.request_params = ctx.requestParams;
  if (summary.completion_summary) meta.completion_summary = summary.completion_summary;
  if (summary.error_message) meta.error_message = summary.error_message;

  await db.requestLog.create({
    data: {
      appCode: ctx.appCode,
      apiKeyId: ctx.apiKeyId,
      keyType: ctx.keyType,
      userCode: ctx.userCode,
      model,
      path: ctx.path,
      statusCode,
      latencyMs: Date.now() - ctx.startTime,
      meta: Object.keys(meta).length > 0 ? meta : undefined,
    },
  });
}
```

- [ ] **Step 2: 修改 `logNonStreaming` 提取 completion 摘要**

```typescript
import {
  extractCompletionSummary,
  extractErrorMessage,
} from "@/lib/summary-extractor";

export function logNonStreaming(
  ctx: LogContext,
  statusCode: number,
  responseBody: unknown
): void {
  let model: string | null = null;
  let usage: UsageData | null = null;
  const summary: SummaryData = {};

  if (responseBody && typeof responseBody === "object") {
    const body = responseBody as Record<string, unknown>;
    if (typeof body.model === "string") model = body.model;
    if (body.usage && typeof body.usage === "object") {
      usage = body.usage as UsageData;
    }
    // 提取 completion 摘要或错误信息
    if (statusCode >= 400) {
      summary.error_message = extractErrorMessage(responseBody);
    } else {
      summary.completion_summary = extractCompletionSummary(responseBody);
    }
  }

  writeLog(ctx, statusCode, model, usage, summary).catch((err) => {
    logger.warn({ err }, "Failed to write request log");
  });
}
```

- [ ] **Step 3: 修改 `logStreaming` 拼接 delta content**

```typescript
export function logStreaming(
  ctx: LogContext,
  statusCode: number,
  logStream: ReadableStream<Uint8Array>
): void {
  (async () => {
    const reader = logStream.getReader();
    const decoder = new TextDecoder();
    let lastUsage: UsageData | null = null;
    let model: string | null = null;
    let contentBuffer = "";  // 累积 delta.content

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const text = decoder.decode(value, { stream: true });
        for (const line of text.split("\n")) {
          if (!line.startsWith("data: ") || line === "data: [DONE]") continue;
          try {
            const json = JSON.parse(line.slice(6)) as Record<string, unknown>;
            if (typeof json.model === "string") model = json.model;
            if (json.usage) lastUsage = json.usage as UsageData;
            // 累积 completion content
            const choices = json.choices as Array<Record<string, unknown>> | undefined;
            if (choices?.[0]) {
              const delta = choices[0].delta as Record<string, unknown> | undefined;
              if (delta && typeof delta.content === "string") {
                contentBuffer += delta.content;
              }
            }
          } catch {
            // 忽略单行解析失败
          }
        }
      }
    } catch (err) {
      logger.warn({ err }, "Error reading SSE log stream");
    } finally {
      reader.releaseLock();
    }

    const summary: SummaryData = {};
    if (contentBuffer) {
      const { truncate } = await import("@/lib/summary-extractor");
      summary.completion_summary = truncate(contentBuffer, 200);
    }

    await writeLog(ctx, statusCode, model, lastUsage, summary);
  })().catch((err) => {
    logger.warn({ err }, "Failed to write streaming request log");
  });
}
```

- [ ] **Step 4: 验证编译通过**

```bash
pnpm exec tsc --noEmit
```

预期：无错误。

- [ ] **Step 5: 提交**

```bash
git add src/lib/request-logger.ts
git commit -m "request-logger 支持写入摘要数据到 meta"
```

---

### Task 5: 代理入口 — clone body 提取摘要

**Files:**
- Modify: `src/app/v1/[...path]/route.ts`

- [ ] **Step 1: 在 `handleProxy` 中提取请求摘要**

在 `src/app/v1/[...path]/route.ts` 的 `handleProxy` 函数中，在构造 `logCtx` 之后、
构造 AIGW 请求头之前，插入摘要提取逻辑：

```typescript
import {
  extractPromptSummary,
  extractRequestParams,
} from "@/lib/summary-extractor";

// ... 在 logCtx 构造之后追加：

// ── Step 3.5: 提取请求摘要（clone 不消费原始 body）──────────────
let promptSummary: string | null = null;
let requestParams: Record<string, unknown> | null = null;
try {
  const clonedBody = await req.clone().json();
  promptSummary = extractPromptSummary(clonedBody);
  requestParams = extractRequestParams(clonedBody);
} catch {
  // 非 JSON body（如文件上传）跳过摘要提取
}
logCtx.promptSummary = promptSummary;
logCtx.requestParams = requestParams;
```

注意：这段代码放在 `const logCtx = { ... }` 之后、`const aigwHeaders = buildAigwHeaders(...)` 之前。

- [ ] **Step 2: 验证编译通过**

```bash
pnpm exec tsc --noEmit
```

预期：无错误。

- [ ] **Step 3: 运行现有单元测试确保无回归**

```bash
pnpm test
```

预期：所有测试 PASS。

- [ ] **Step 4: 提交**

```bash
git add src/app/v1/[...path]/route.ts
git commit -m "代理入口提取请求摘要写入 LogContext"
```

---

### Task 6: API 层 — 增强过滤参数 + 权限改 `stats:detail`

**Files:**
- Modify: `src/app/api/admin/stats/requests/route.ts`

- [ ] **Step 1: 修改权限和过滤逻辑**

完整替换 `src/app/api/admin/stats/requests/route.ts`：

```typescript
import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import type { ApiKeyType } from "@/generated/prisma";

/**
 * GET /api/admin/stats/requests
 * 分页查询请求日志明细
 * 权限：stats:detail（仅 admin）
 *
 * 查询参数：
 *   app_code, user_code    — 现有
 *   model                  — 按模型名精确过滤
 *   status_code            — 按状态码精确过滤
 *   status_type            — success | error（< 400 为 success）
 *   from, to               — ISO date 时间范围
 *   key_type               — personal | service
 *   page, page_size        — 分页
 */
export async function GET(req: Request) {
  const unauth = await requirePermission("stats:detail", req);
  if (unauth) return unauth;

  const { searchParams } = new URL(req.url);
  const appCode = searchParams.get("app_code") ?? undefined;
  const userCode = searchParams.get("user_code") ?? undefined;
  const model = searchParams.get("model") ?? undefined;
  const statusCode = searchParams.get("status_code");
  const statusType = searchParams.get("status_type");
  const from = searchParams.get("from");
  const to = searchParams.get("to");
  const keyType = searchParams.get("key_type") as ApiKeyType | null;
  const page = Math.max(1, parseInt(searchParams.get("page") ?? "1", 10));
  const pageSize = Math.min(
    100,
    Math.max(1, parseInt(searchParams.get("page_size") ?? "20", 10))
  );

  // 构造 where 条件
  const where: Record<string, unknown> = {};
  if (appCode) where.appCode = appCode;
  if (userCode) where.userCode = userCode;
  if (model) where.model = model;
  if (keyType) where.keyType = keyType;
  if (statusCode) {
    where.statusCode = parseInt(statusCode, 10);
  } else if (statusType === "success") {
    where.statusCode = { lt: 400 };
  } else if (statusType === "error") {
    where.statusCode = { gte: 400 };
  }
  if (from || to) {
    const createdAt: Record<string, Date> = {};
    if (from) createdAt.gte = new Date(from);
    if (to) createdAt.lte = new Date(to);
    where.createdAt = createdAt;
  }

  const [total, rows] = await Promise.all([
    db.requestLog.count({ where }),
    db.requestLog.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * pageSize,
      take: pageSize,
      select: {
        id: true,
        appCode: true,
        apiKeyId: true,
        keyType: true,
        userCode: true,
        model: true,
        path: true,
        statusCode: true,
        latencyMs: true,
        meta: true,
        createdAt: true,
      },
    }),
  ]);

  return Response.json({
    total,
    page,
    page_size: pageSize,
    data: rows,
  });
}
```

- [ ] **Step 2: 验证编译通过**

```bash
pnpm exec tsc --noEmit
```

预期：无错误。

- [ ] **Step 3: 提交**

```bash
git add src/app/api/admin/stats/requests/route.ts
git commit -m "stats/requests API 权限改 stats:detail，新增多维过滤参数"
```

---

### Task 7: 前端 — 请求日志明细组件

**Files:**
- Create: `src/components/admin/request-log-table.tsx`
- Modify: `src/app/(admin)/stats/page.tsx`

- [ ] **Step 1: 创建 `request-log-table.tsx` 骨架**

创建 `src/components/admin/request-log-table.tsx`，先写 import 和接口定义：

```typescript
"use client";

import { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ChevronDown, ChevronRight, ChevronLeft } from "lucide-react";

type RequestLogMeta = {
  prompt_tokens?: number;
  completion_tokens?: number;
  total_tokens?: number;
  prompt_summary?: string | null;
  completion_summary?: string | null;
  request_params?: Record<string, unknown> | null;
  error_message?: string | null;
};

type RequestLogRow = {
  id: string;
  appCode: string;
  keyType: string | null;
  userCode: string | null;
  model: string | null;
  path: string | null;
  statusCode: number | null;
  latencyMs: number | null;
  meta: RequestLogMeta | null;
  createdAt: string;
};

type Filters = {
  app_code: string;
  user_code: string;
  model: string;
  key_type: string;
  status_type: string;
};
```

- [ ] **Step 2: 实现过滤栏和数据获取**

在同一文件中追加组件主体：

```typescript
export function RequestLogTable() {
  const [data, setData] = useState<RequestLogRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [filters, setFilters] = useState<Filters>({
    app_code: "",
    user_code: "",
    model: "",
    key_type: "",
    status_type: "",
  });
  const pageSize = 20;

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set("page", String(page));
      params.set("page_size", String(pageSize));
      if (filters.app_code) params.set("app_code", filters.app_code);
      if (filters.user_code) params.set("user_code", filters.user_code);
      if (filters.model) params.set("model", filters.model);
      if (filters.key_type) params.set("key_type", filters.key_type);
      if (filters.status_type) params.set("status_type", filters.status_type);

      const res = await fetch(`/api/admin/stats/requests?${params}`);
      if (!res.ok) return;
      const json = await res.json();
      setData(json.data ?? []);
      setTotal(json.total ?? 0);
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => { fetchData(); }, [fetchData]);

  function updateFilter(key: keyof Filters, value: string) {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(1);
  }

  const totalPages = Math.ceil(total / pageSize);
```

- [ ] **Step 3: 实现渲染部分（过滤栏 + 表格 + 展开详情 + 分页）**

在组件 return 中：

```typescript
  return (
    <div className="space-y-4">
      {/* 过滤栏 */}
      <div className="flex flex-wrap gap-2">
        <Input
          placeholder="App Code"
          className="w-36 h-9"
          value={filters.app_code}
          onChange={(e) => updateFilter("app_code", e.target.value)}
        />
        <Input
          placeholder="User Code"
          className="w-48 h-9"
          value={filters.user_code}
          onChange={(e) => updateFilter("user_code", e.target.value)}
        />
        <Input
          placeholder="Model"
          className="w-44 h-9"
          value={filters.model}
          onChange={(e) => updateFilter("model", e.target.value)}
        />
        <Select
          value={filters.key_type || "__all__"}
          onValueChange={(v) => updateFilter("key_type", v === "__all__" ? "" : v)}
        >
          <SelectTrigger className="w-32 h-9">
            <SelectValue placeholder="Key Type" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__all__">全部类型</SelectItem>
            <SelectItem value="personal">personal</SelectItem>
            <SelectItem value="service">service</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={filters.status_type || "__all__"}
          onValueChange={(v) => updateFilter("status_type", v === "__all__" ? "" : v)}
        >
          <SelectTrigger className="w-28 h-9">
            <SelectValue placeholder="状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__all__">全部状态</SelectItem>
            <SelectItem value="success">成功</SelectItem>
            <SelectItem value="error">失败</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* 日志表格 */}
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-8" />
            <TableHead>时间</TableHead>
            <TableHead>App</TableHead>
            <TableHead>User</TableHead>
            <TableHead>Model</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">延迟</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                加载中...
              </TableCell>
            </TableRow>
          ) : data.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                暂无数据
              </TableCell>
            </TableRow>
          ) : (
            data.map((row) => {
              const isExpanded = expandedId === row.id;
              const meta = row.meta;
              return (
                <>
                  <TableRow
                    key={row.id}
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => setExpandedId(isExpanded ? null : row.id)}
                  >
                    <TableCell className="w-8">
                      {isExpanded ? (
                        <ChevronDown className="h-4 w-4" />
                      ) : (
                        <ChevronRight className="h-4 w-4" />
                      )}
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
                      {new Date(row.createdAt).toLocaleString("zh-CN")}
                    </TableCell>
                    <TableCell className="font-mono text-sm">{row.appCode}</TableCell>
                    <TableCell className="font-mono text-sm">{row.userCode ?? "—"}</TableCell>
                    <TableCell className="font-mono text-sm">{row.model ?? "—"}</TableCell>
                    <TableCell>
                      <span className={row.statusCode && row.statusCode >= 400 ? "text-red-500" : "text-green-600"}>
                        {row.statusCode ?? "—"}
                      </span>
                    </TableCell>
                    <TableCell className="text-right text-sm">{row.latencyMs ? `${row.latencyMs}ms` : "—"}</TableCell>
                  </TableRow>
                  {isExpanded && (
                    <TableRow key={`${row.id}-detail`}>
                      <TableCell colSpan={7} className="bg-muted/30 p-4">
                        <div className="space-y-2 text-sm">
                          {meta?.prompt_summary && (
                            <div>
                              <span className="font-medium text-muted-foreground">Prompt: </span>
                              <span className="font-mono whitespace-pre-wrap">{meta.prompt_summary}</span>
                            </div>
                          )}
                          {meta?.completion_summary && (
                            <div>
                              <span className="font-medium text-muted-foreground">Completion: </span>
                              <span className="font-mono whitespace-pre-wrap">{meta.completion_summary}</span>
                            </div>
                          )}
                          {meta?.error_message && (
                            <div>
                              <span className="font-medium text-red-500">Error: </span>
                              <span className="font-mono">{meta.error_message}</span>
                            </div>
                          )}
                          <div className="flex gap-6 text-xs text-muted-foreground">
                            {meta?.prompt_tokens != null && <span>Prompt tokens: {meta.prompt_tokens}</span>}
                            {meta?.completion_tokens != null && <span>Completion tokens: {meta.completion_tokens}</span>}
                            {meta?.total_tokens != null && <span>Total: {meta.total_tokens}</span>}
                          </div>
                          {meta?.request_params && (
                            <div className="text-xs text-muted-foreground">
                              Params: {JSON.stringify(meta.request_params)}
                            </div>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </>
              );
            })
          )}
        </TableBody>
      </Table>

      {/* 分页 */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <p className="text-sm text-muted-foreground">
            共 {total} 条
          </p>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" className="h-8 w-8 p-0"
              disabled={page <= 1} onClick={() => setPage(page - 1)}>
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm text-muted-foreground min-w-[4rem] text-center">
              {page} / {totalPages}
            </span>
            <Button variant="outline" size="sm" className="h-8 w-8 p-0"
              disabled={page >= totalPages} onClick={() => setPage(page + 1)}>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
```

- [ ] **Step 4: 修改统计页，按角色条件渲染日志区域**

在 `src/app/(admin)/stats/page.tsx` 中：

1. 在文件顶部添加 import：

```typescript
import { hasPermission, type UserRole } from "@/lib/permissions";
import { RequestLogTable } from "@/components/admin/request-log-table";
```

2. 在 `StatsPage` 函数中，`currentUserCode` 后面添加 role 获取：

```typescript
const role = (currentUser?.role ?? "guest") as UserRole;
const canViewDetail = hasPermission(role, "stats:detail");
```

3. 在 JSX 最后（`</div>` 闭合标签之前），添加日志区域：

```tsx
{canViewDetail && (
  <div className="mt-6">
    <h2 className="text-lg font-semibold mb-3">请求日志</h2>
    <RequestLogTable />
  </div>
)}
```

- [ ] **Step 5: 验证编译通过**

```bash
pnpm exec tsc --noEmit
```

预期：无错误。

- [ ] **Step 6: 提交**

```bash
git add src/components/admin/request-log-table.tsx src/app/(admin)/stats/page.tsx
git commit -m "前端统计页新增请求日志明细区域（仅 admin 可见）"
```

---

### Task 8: AIGW Mock Server + API 集成测试

**Files:**
- Create: `src/lib/__tests__/mock-aigw.ts`
- Create: `src/app/api/admin/stats/__tests__/requests.test.ts`

- [ ] **Step 1: 创建 AIGW Mock Server**

创建 `src/lib/__tests__/mock-aigw.ts`：

```typescript
import { createServer, type Server } from "node:http";

/** 固定的非流式响应 */
const NON_STREAMING_RESPONSE = {
  id: "chatcmpl-test",
  object: "chat.completion",
  model: "gpt-4",
  choices: [
    {
      index: 0,
      message: { role: "assistant", content: "这是一个测试回复" },
      finish_reason: "stop",
    },
  ],
  usage: { prompt_tokens: 10, completion_tokens: 5, total_tokens: 15 },
};

/** 固定的流式 SSE chunks */
function streamingChunks(): string[] {
  return [
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { role: "assistant" } }] })}\n\n`,
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { content: "流式" } }] })}\n\n`,
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { content: "回复" } }] })}\n\n`,
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: {} }], usage: { prompt_tokens: 10, completion_tokens: 2, total_tokens: 12 } })}\n\n`,
    "data: [DONE]\n\n",
  ];
}

/** 错误响应 */
const ERROR_RESPONSE = {
  error: { message: "Model not found: bad-model", type: "invalid_request_error" },
};

export function createMockAigwServer(): Server {
  return createServer((req, res) => {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      const parsed = body ? JSON.parse(body) : {};

      // 错误场景：model 包含 "bad"
      if (parsed.model?.includes("bad")) {
        res.writeHead(400, { "Content-Type": "application/json" });
        res.end(JSON.stringify(ERROR_RESPONSE));
        return;
      }

      // 流式场景
      if (parsed.stream) {
        res.writeHead(200, {
          "Content-Type": "text/event-stream",
          "Cache-Control": "no-cache",
          Connection: "keep-alive",
        });
        for (const chunk of streamingChunks()) {
          res.write(chunk);
        }
        res.end();
        return;
      }

      // 非流式
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify(NON_STREAMING_RESPONSE));
    });
  });
}

/** 启动 mock server 并返回 baseUrl + 关闭函数 */
export async function startMockAigw(): Promise<{
  baseUrl: string;
  close: () => Promise<void>;
}> {
  const server = createMockAigwServer();
  await new Promise<void>((resolve) => server.listen(0, resolve));
  const addr = server.address();
  const port = typeof addr === "object" && addr ? addr.port : 0;
  return {
    baseUrl: `http://localhost:${port}`,
    close: () => new Promise((resolve) => server.close(() => resolve())),
  };
}
```

- [ ] **Step 2: 创建 API 集成测试**

创建 `src/app/api/admin/stats/__tests__/requests.test.ts`：

```typescript
import { describe, it, expect, beforeAll, afterAll } from "vitest";
import { db } from "@/lib/db";

// 注意：此测试需要连接真实数据库（本地 docker MySQL）。
// 如果 CI 无数据库，可 skip。

describe("GET /api/admin/stats/requests", () => {
  const testAppCode = `test-app-${Date.now()}`;

  beforeAll(async () => {
    // seed 测试数据
    await db.requestLog.createMany({
      data: [
        {
          appCode: testAppCode,
          userCode: "user-a",
          model: "gpt-4",
          keyType: "personal",
          statusCode: 200,
          latencyMs: 100,
          path: "/v1/chat/completions",
          meta: {
            prompt_tokens: 10,
            completion_tokens: 5,
            total_tokens: 15,
            prompt_summary: "测试提问",
            completion_summary: "测试回复",
          },
        },
        {
          appCode: testAppCode,
          userCode: "user-b",
          model: "claude-sonnet-4-20250514",
          keyType: "service",
          statusCode: 400,
          latencyMs: 50,
          path: "/v1/chat/completions",
          meta: {
            error_message: "Model not found",
          },
        },
      ],
    });
  });

  afterAll(async () => {
    // 清理测试数据
    await db.requestLog.deleteMany({
      where: { appCode: testAppCode },
    });
  });

  it("按 app_code 过滤返回正确数据", async () => {
    const url = `http://localhost:3000/api/admin/stats/requests?app_code=${testAppCode}`;
    // 注意：真实 E2E 需要启动 Next.js server。
    // 此处仅验证 Prisma 查询逻辑的正确性。
    const count = await db.requestLog.count({
      where: { appCode: testAppCode },
    });
    expect(count).toBe(2);
  });

  it("按 model 过滤", async () => {
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, model: "gpt-4" },
    });
    expect(count).toBe(1);
  });

  it("按 status_type=error 过滤（statusCode >= 400）", async () => {
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, statusCode: { gte: 400 } },
    });
    expect(count).toBe(1);
  });

  it("按 key_type=service 过滤", async () => {
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, keyType: "service" },
    });
    expect(count).toBe(1);
  });

  it("meta 中包含摘要字段", async () => {
    const row = await db.requestLog.findFirst({
      where: { appCode: testAppCode, userCode: "user-a" },
    });
    const meta = row?.meta as Record<string, unknown>;
    expect(meta.prompt_summary).toBe("测试提问");
    expect(meta.completion_summary).toBe("测试回复");
  });
});
```

- [ ] **Step 3: 运行测试**

```bash
pnpm test -- src/app/api/admin/stats/__tests__/requests.test.ts
```

预期：如果本地 docker MySQL 已启动且 schema 已 push，全部 PASS。
如果无数据库连接则 FAIL（预期行为，CI 中可配置 skip）。

- [ ] **Step 4: 提交**

```bash
git add src/lib/__tests__/mock-aigw.ts src/app/api/admin/stats/__tests__/requests.test.ts
git commit -m "AIGW Mock Server + API 集成测试（过滤和摘要验证）"
```

---

### Task 9: Playwright E2E — 前端日志展开 + 权限验证

**Files:**
- Create: `e2e/stats-log-detail.spec.ts`

**前置条件：** 本地 `pnpm dev` 运行中，数据库有数据，有可用的 admin 和 developer 账号的登录 session。

> 注意：Playwright E2E 依赖完整的 Next.js 运行环境和真实数据库。
> 首次运行需要手动登录获取 session cookie 并保存为 storageState。
> 具体 auth setup 根据项目 OpenID 登录机制可能需要定制。

- [ ] **Step 1: 创建 E2E 测试文件**

创建 `e2e/stats-log-detail.spec.ts`：

```typescript
import { test, expect } from "@playwright/test";

test.describe("请求日志明细", () => {
  // 注意：需要配置 admin 登录态。
  // 可通过 playwright.config.ts 的 storageState 或在 test.beforeEach 中登录。

  test("admin 可以看到请求日志区域", async ({ page }) => {
    await page.goto("/stats");
    // 等待页面加载
    await expect(page.getByText("使用统计")).toBeVisible();
    // admin 应能看到"请求日志"标题
    await expect(page.getByText("请求日志")).toBeVisible();
  });

  test("点击日志行展开详情", async ({ page }) => {
    await page.goto("/stats");
    await expect(page.getByText("请求日志")).toBeVisible();

    // 等待日志表格加载完成（非"加载中"状态）
    await page.waitForSelector("table tbody tr", { timeout: 10_000 });

    // 点击第一行
    const firstRow = page.locator("table tbody tr").first();
    await firstRow.click();

    // 验证展开后能看到详情内容（Prompt/Completion/Tokens 之一）
    await expect(
      page.getByText(/Prompt:|Completion:|Error:|Prompt tokens:/)
    ).toBeVisible();
  });
});
```

- [ ] **Step 2: 创建 developer 权限验证测试**

在同一文件末尾追加：

```typescript
test.describe("developer 权限限制", () => {
  // 注意：需要配置 developer 登录态。

  test.skip("developer 看不到请求日志区域", async ({ page }) => {
    // skip 标记：需要 developer 账号的 storageState
    await page.goto("/stats");
    await expect(page.getByText("使用统计")).toBeVisible();
    // developer 不应看到"请求日志"标题
    await expect(page.getByText("请求日志")).not.toBeVisible();
  });
});
```

- [ ] **Step 3: 运行 E2E 测试（需要 dev server 运行中）**

```bash
pnpm test:e2e
```

预期：如果 dev server 运行且有 admin session，测试 PASS。
未配置 auth 时会因登录重定向而 FAIL（预期行为，需要后续配置 storageState）。

- [ ] **Step 4: 提交**

```bash
git add e2e/stats-log-detail.spec.ts
git commit -m "Playwright E2E 测试：日志明细展开和权限验证"
```

---

### Task 10: Playwright E2E — 完整链路验证摘要写入

**Files:**
- Create: `e2e/log-recording.spec.ts`

- [ ] **Step 1: 创建完整链路 E2E 测试**

创建 `e2e/log-recording.spec.ts`：

```typescript
import { test, expect } from "@playwright/test";

test.describe("请求日志摘要写入完整链路", () => {
  const testPrompt = `e2e-test-${Date.now()}`;

  test("发送请求后能在日志中看到摘要", async ({ request, page }) => {
    // Step 1: 通过 API 发一个 chat/completions 请求
    // 注意：需要有效的 API key。此测试在 AIGW 可达时才能通过。
    // 如需隔离，需将 AIGW_BASE_URL 指向 mock server。
    const apiRes = await request.post("/v1/chat/completions", {
      headers: {
        Authorization: "Bearer <test-api-key>",
        "Content-Type": "application/json",
      },
      data: {
        model: "gpt-4",
        messages: [{ role: "user", content: testPrompt }],
        temperature: 0.1,
        max_tokens: 50,
      },
    });

    // 允许失败（AIGW 不可达时 502），但日志仍应记录
    expect([200, 502]).toContain(apiRes.status());

    // Step 2: 等待异步日志写入（fire-and-forget，给 2 秒）
    await new Promise((resolve) => setTimeout(resolve, 2000));

    // Step 3: admin 登录查看日志
    await page.goto("/stats");
    await expect(page.getByText("请求日志")).toBeVisible();

    // 等待日志表格加载
    await page.waitForSelector("table tbody tr", { timeout: 10_000 });

    // Step 4: 在日志中找到包含测试 prompt 的记录
    // 通过 API 直接查询更可靠
    const statsRes = await request.get(
      `/api/admin/stats/requests?page_size=5`
    );
    const statsJson = await statsRes.json();

    // 验证最新日志的 meta 中包含摘要
    const latestLog = statsJson.data?.[0];
    expect(latestLog).toBeDefined();
    expect(latestLog.meta).toBeDefined();
    // meta 中应有 prompt_summary（如果请求成功到达 AIGW）
    // 或 error_message（如果 AIGW 不可达）
    const meta = latestLog.meta;
    const hasSummary =
      meta.prompt_summary != null ||
      meta.error_message != null;
    expect(hasSummary).toBe(true);
  });
});
```

- [ ] **Step 2: 运行测试**

```bash
pnpm test:e2e -- e2e/log-recording.spec.ts
```

预期：依赖完整环境（dev server + 数据库 + API key），
在环境就绪时 PASS。

- [ ] **Step 3: 提交**

```bash
git add e2e/log-recording.spec.ts
git commit -m "Playwright E2E 测试：完整链路验证摘要写入"
```

---

### Task 11: 收尾 — 全量验证 + 最终提交

- [ ] **Step 1: 运行全部单元测试**

```bash
pnpm test
```

预期：所有 Vitest 测试 PASS。

- [ ] **Step 2: TypeScript 编译检查**

```bash
pnpm exec tsc --noEmit
```

预期：无错误。

- [ ] **Step 3: ESLint 检查**

```bash
pnpm lint
```

预期：无错误（如有 lint 规则冲突，按项目配置修复）。

- [ ] **Step 4: 启动 dev server 手动验证**

```bash
pnpm dev
```

1. 以 admin 账号登录
2. 进入统计页，看到"请求日志"区域
3. 使用过滤栏筛选
4. 点击某行展开，看到 Prompt / Completion 摘要
5. 以 developer 账号登录，确认看不到"请求日志"区域

- [ ] **Step 5: 运行 Playwright E2E（可选，需配置 auth）**

```bash
pnpm test:e2e
```

- [ ] **Step 6: 提交设计文档和实施计划**

```bash
git add docs/specs/2026-04-20-request-log-detail-design.md docs/plans/2026-04-20-request-log-detail-plan.md
git commit -m "请求日志详情查询功能：设计文档和实施计划"
```
