## 1. API Key 验证层

- [ ] 1.1 创建 `src/lib/auth-key.ts`，实现 `validateApiKey(rawKey: string)` 函数：
  - 从 LRU Cache 查找，命中则直接返回 `{ keyRecord, appRecord }`
  - 未命中则查 DB（`api_keys` JOIN `apps`），结果写入 Cache
  - Key 不存在、`is_active=false` 或 App `is_active=false` 时返回 `null`
- [ ] 1.2 在 `auth-key.ts` 中实现 `resolveUserCode(keyRecord, req: Request)` 函数：
  - `type=personal`：返回 `keyRecord.user_email`
  - `type=service && app.require_user_code=true`：取 `X-User-Code` header，无则抛 400 错误
  - `type=service && app.require_user_code=false`：返回 `null`

## 2. AIGW 请求构建器

- [ ] 2.1 创建 `src/lib/aigw-client.ts`，实现 `buildAigwHeaders(app, userCode, incomingHeaders)` 函数：
  - 删除原 `Authorization` header
  - 注入 `Authorization: Bearer {app.app_id}.{app.app_key}`
  - 注入 `X-Aigw-Meta: user_code={userCode}; first_tag={app.app_code}`（userCode 为 null 时只注入 first_tag）
  - 透传 `Content-Type`、`anthropic-beta` 等白名单 header
- [ ] 2.2 在 `aigw-client.ts` 中实现 `forwardToAigw(path, method, headers, body)` 函数：
  - 拼接 URL：`{env.AIGW_BASE_URL}/v1/{path.join('/')}`
  - 使用 `fetch` 发起请求，传入原始 `body`（`ReadableStream`）
  - 返回原始 `Response` 对象

## 3. 异步请求日志

- [ ] 3.1 创建 `src/lib/request-logger.ts`，实现 `logRequest(context, responseStream?)` 函数：
  - 非流式：直接从响应 JSON 的 `usage` 字段提取 token 数
  - 流式：消费 tee 出的副本流，从最后一个非 `[DONE]` 的 SSE chunk 中提取 `usage`
  - 将结果异步写入 `request_logs` 表，使用 fire-and-forget（不 await，catch 错误打 pino warn 日志）
  - 记录字段：`app_code`、`api_key_id`、`user_code`、`model`（从请求 body 解析）、`endpoint`、`status_code`、`duration`（毫秒）、`meta.prompt_tokens`、`meta.completion_tokens`、`meta.total_tokens`

## 4. 代理核心路由

- [ ] 4.1 创建 `src/app/api/proxy/[...path]/route.ts`，导出 `GET`、`POST`、`DELETE`、`PUT`、`PATCH` handler：
  - 从 `Authorization: Bearer {key}` 提取 key，无 header 则返回 401
  - 调用 `validateApiKey`，失败返回 `{ error: "Unauthorized" }` 401
  - 调用 `resolveUserCode`，失败返回 `{ error: "X-User-Code header required" }` 400
  - 调用 `buildAigwHeaders` 和 `forwardToAigw`
  - 对流式响应（`Content-Type: text/event-stream`）：`tee()` 分流，副本交 `logRequest` 异步处理
  - 对非流式响应：透传后异步调用 `logRequest`
  - 过滤响应头（去除 `content-encoding`，避免解压/重压缩问题）
  - 透传 AIGW 的 `status code`

## 5. Admin API

- [ ] 5.1 创建 `src/app/api/admin/apps/route.ts`：
  - `GET /api/admin/apps`：列出所有 App（含 `app_code`、`display_name`、`is_active`，不返回 `app_key`）
  - `POST /api/admin/apps`：创建 App 记录（body: `app_code`、`app_id`、`app_key`、`display_name`、`require_user_code?`）
- [ ] 5.2 创建 `src/app/api/admin/apps/[appCode]/route.ts`：
  - `PATCH /api/admin/apps/:appCode`：更新 `display_name`、`require_user_code`、`is_active`
  - `DELETE /api/admin/apps/:appCode`：软删除（`is_active=false`）
- [ ] 5.3 创建 `src/app/api/admin/keys/route.ts`：
  - `GET /api/admin/keys`：列出 Key（支持 `?app_code=&type=` 过滤，不返回完整 key，仅返回前 8 位 + `****`）
  - `POST /api/admin/keys`：生成新 Key（body: `type`、`app_code`、`user_email?`、`service_name?`、`label?`）
    - 生成格式：`sk-{appCode}-{crypto.randomBytes(16).toString('hex')}` 或 `svc-{appCode}-{hex}`
    - 仅在创建时返回完整 key（之后不再展示）
- [ ] 5.4 创建 `src/app/api/admin/keys/[id]/route.ts`：
  - `PATCH /api/admin/keys/:id`：更新 `label`、`is_active`（用于吊销）
  - 吊销时调用 `cache.delete(apiKey)` 主动失效缓存
- [ ] 5.5 创建 `src/app/api/admin/stats/route.ts`：
  - `GET /api/admin/stats/usage`：按 `app_code` + `model` 聚合 token 用量，支持 `?from=&to=` 时间范围
  - `GET /api/admin/stats/requests`：分页查询 request_logs，支持 `?app_code=&user_code=&page=&page_size=`

## 6. 管理后台页面

- [ ] 6.1 创建 `src/app/(admin)/layout.tsx`：next-auth session 保护，未登录重定向 `/login`
- [ ] 6.2 创建 `src/app/(admin)/apps/page.tsx`：App 列表页，含新增/禁用操作
- [ ] 6.3 创建 `src/app/(admin)/keys/page.tsx`：Key 管理页，含生成/吊销操作，新建成功后展示一次完整 key
- [ ] 6.4 创建 `src/app/(admin)/stats/page.tsx`：用量统计页，按 App / 用户 / 模型维度展示

## 7. 验证

- [ ] 7.1 使用 curl 测试非流式请求，验证响应正确，request_logs 有对应记录
- [ ] 7.2 使用 curl 测试流式请求（`"stream": true`），验证 SSE 正常透传，request_logs 有 token 用量
- [ ] 7.3 测试 Key 不存在、Key 禁用、App 禁用三种 401 场景
- [ ] 7.4 测试服务 Key 未传 X-User-Code 时返回 400
- [ ] 7.5 使用 OpenAI SDK 配置 `baseURL` 为本服务，验证完整兼容性
- [ ] 7.6 运行 `pnpm build` 验证编译通过
