## Why

基础设施层（数据模型、工具库、环境配置）已在上一个 change 中就绪。现在需要实现 ai-service 的**核心业务价值**：让内部用户和业务系统能够以 OpenAI 兼容的方式访问 AIGW，而无需了解 AIGW 的 appcode/key 鉴权机制。

本次变更完成从"能运行的空壳"到"可用的 AI 接入服务"的关键跃升：部门内任何业务系统只需一个 `svc-` 前缀的 API Key 和我们的 BaseURL，即可接入 AIGW 支持的全部模型（GPT、Claude、DeepSeek、Gemini 等），且请求会自动携带用户身份，复用 AIGW 的用量统计和费用报表能力。

## What Changes

- 实现 AIGW 代理核心路由 `/api/proxy/[...path]`，支持所有 HTTP 方法和流式 SSE 响应
- 实现 API Key 验证中间件（基于 LRU Cache + Prisma DB 的二级验证）
- 实现 AIGW 请求构建器（注入 Authorization、X-Aigw-Meta user_code 和计费标签）
- 实现异步请求日志（流式响应结束后提取 usage 写入 request_logs）
- 实现 Admin API：App 凭证管理、API Key 生成与吊销、使用统计查询
- 实现管理后台基础页面（App 列表、Key 管理）

## Capabilities

### New Capabilities

- `aigw-proxy`: AIGW 代理层核心能力，包括 API Key 鉴权、凭证注入、请求透传、SSE 流式支持、异步请求日志

### Modified Capabilities

- `core-libs`: 扩展 `cache.ts` 的导出，增加基于 LRU Cache 的 `lookupApiKey` 方法（含 DB 回源逻辑）

## Impact

- `src/app/api/proxy/[...path]/route.ts`：新增，代理核心路由
- `src/lib/auth-key.ts`：新增，API Key 验证逻辑
- `src/lib/aigw-client.ts`：新增，AIGW 上游请求封装
- `src/lib/request-logger.ts`：新增，异步日志写入
- `src/app/api/admin/apps/route.ts`：新增，App CRUD
- `src/app/api/admin/keys/route.ts`：新增，API Key 管理
- `src/app/api/admin/stats/route.ts`：新增，使用统计查询
- `src/app/(admin)/`：新增，管理后台页面（layout + 各功能页）
