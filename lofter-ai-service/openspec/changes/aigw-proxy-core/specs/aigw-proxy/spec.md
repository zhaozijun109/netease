## Requirements

### Requirement: API Key 验证

系统 SHALL 对所有代理请求进行 API Key 验证，拒绝无效或已禁用的请求。

#### Scenario: 有效 Key 允许通过

- **WHEN** 请求携带 `Authorization: Bearer {valid_key}` header
- **AND** 该 Key 在 `api_keys` 表中存在且 `is_active=true`
- **AND** 关联的 App `is_active=true`
- **THEN** 系统允许请求继续处理

#### Scenario: 无效 Key 返回 401

- **WHEN** 请求缺少 `Authorization` header，或 Key 不存在，或 Key/App 已禁用
- **THEN** 系统返回 HTTP 401，body 为 `{"error": "Unauthorized"}`
- **AND** 不向 AIGW 发起任何请求

#### Scenario: Key 验证结果缓存

- **WHEN** 同一个 Key 在 5 分钟内被多次使用
- **THEN** 系统从 LRU Cache 命中验证结果，不查询数据库
- **AND** Key 被吊销时，系统主动从 Cache 中删除该 Key，使吊销立即生效

---

### Requirement: User Code 解析

系统 SHALL 根据 Key 类型解析用户身份，用于注入 AIGW 的用户统计。

#### Scenario: 个人 Key 自动提取 user_code

- **WHEN** 请求使用 `type=personal` 的 Key（`sk-` 前缀）
- **THEN** 系统将 `user_code` 设为该 Key 关联的 `user_email`
- **AND** 无需调用方传递任何额外 header

#### Scenario: 服务 Key 要求传递 X-User-Code

- **WHEN** 请求使用 `type=service` 的 Key（`svc-` 前缀）
- **AND** 关联 App 的 `require_user_code=true`
- **AND** 请求未携带 `X-User-Code` header
- **THEN** 系统返回 HTTP 400，body 为 `{"error": "X-User-Code header is required for this app"}`

#### Scenario: 服务 Key 不强制 user_code

- **WHEN** 请求使用 `type=service` 的 Key
- **AND** 关联 App 的 `require_user_code=false`
- **THEN** user_code 为空，系统不向 AIGW 注入 user_code，但仍注入 first_tag

---

### Requirement: AIGW 凭证注入

系统 SHALL 在转发请求时，将调用方的 API Key 替换为 AIGW 凭证，并注入用户信息。

#### Scenario: 注入 AIGW Authorization

- **WHEN** 代理请求准备转发到 AIGW
- **THEN** 系统移除调用方的 `Authorization` header
- **AND** 注入 `Authorization: Bearer {app.app_id}.{app.app_key}`

#### Scenario: 注入用户身份和计费标签

- **WHEN** 代理请求准备转发到 AIGW
- **THEN** 系统注入 `X-Aigw-Meta: user_code={user_code}; first_tag={app.app_code}`
- **AND** user_code 为空时，只注入 `X-Aigw-Meta: first_tag={app.app_code}`

#### Scenario: 透传调用方 header

- **WHEN** 调用方传入 `anthropic-beta`、`Content-Type` 等业务 header
- **THEN** 系统将这些 header 透传给 AIGW
- **AND** 不修改业务 header 的值

---

### Requirement: 请求体透传

系统 SHALL 将调用方的请求体原封不动地转发给 AIGW，不做任何解析或修改。

#### Scenario: 请求参数完整透传

- **WHEN** 调用方发送包含 `model`、`messages`、`stream`、`temperature` 等参数的请求体
- **THEN** AIGW 接收到的请求体与调用方发送的完全一致

---

### Requirement: 流式响应（SSE）透传

系统 SHALL 支持 AIGW 的流式 SSE 响应，并在不影响响应时序的前提下记录日志。

#### Scenario: SSE 流实时透传

- **WHEN** AIGW 返回 `Content-Type: text/event-stream` 响应
- **THEN** 系统将每个 SSE chunk 实时转发给调用方
- **AND** 不缓冲全部响应后再发送

#### Scenario: 流式响应中提取 usage 用于日志

- **WHEN** 流式响应结束，AIGW 在最后一个 chunk 中携带 `usage` 字段
- **THEN** 系统从该 chunk 提取 `prompt_tokens`、`completion_tokens`、`total_tokens`
- **AND** 将用量数据异步写入 request_logs 的 `meta` 字段

---

### Requirement: 异步请求日志

系统 SHALL 对每次代理请求记录日志，且不因日志写入影响响应延迟。

#### Scenario: 日志写入不阻塞响应

- **WHEN** AIGW 响应返回后
- **THEN** 系统以 fire-and-forget 方式异步写入 `request_logs`
- **AND** 日志写入失败时，仅打印 warn 日志，不影响本次请求的响应

#### Scenario: 日志记录关键字段

- **WHEN** 一次代理请求完成（成功或失败）
- **THEN** 系统写入一条 request_logs 记录
- **AND** 包含 `app_code`、`api_key_id`、`user_code`、`model`（从请求 body 解析）、`endpoint`（请求路径）、`status_code`、`duration`（毫秒）
- **AND** 若有 token 用量，写入 `meta.prompt_tokens`、`meta.completion_tokens`、`meta.total_tokens`

---

### Requirement: Admin API Key 管理

系统 SHALL 提供管理 API，支持 API Key 的生成与吊销。所有 Admin API 须经 next-auth session 认证。

#### Scenario: 生成服务 Key

- **WHEN** 管理员调用 `POST /api/admin/keys`，传入 `type=service`、`app_code`、`service_name`
- **THEN** 系统生成格式为 `svc-{appCode}-{32位hex}` 的 Key
- **AND** 将完整 Key 在响应中返回一次（之后不再展示）
- **AND** 数据库中存储明文 Key

#### Scenario: 生成个人 Key

- **WHEN** 管理员调用 `POST /api/admin/keys`，传入 `type=personal`、`app_code`、`user_email`
- **THEN** 系统生成格式为 `sk-{appCode}-{32位hex}` 的 Key
- **AND** Key 与 `user_email` 关联存储

#### Scenario: 吊销 Key

- **WHEN** 管理员调用 `PATCH /api/admin/keys/:id`，设置 `is_active=false`
- **THEN** 系统将数据库中该 Key 的 `is_active` 更新为 false
- **AND** 系统从 LRU Cache 中主动删除该 Key 的缓存
- **AND** 之后使用该 Key 的请求立即返回 401

---

### Requirement: Admin App 凭证管理

系统 SHALL 提供管理 API，支持 AIGW App 凭证的录入与管理。

#### Scenario: 录入 App 凭证

- **WHEN** 管理员调用 `POST /api/admin/apps`，传入 `app_code`、`app_id`、`app_key`、`display_name`
- **THEN** 系统在 `ai_apps` 表创建记录
- **AND** 响应中不返回 `app_key` 字段

#### Scenario: 禁用 App

- **WHEN** 管理员对某 App 设置 `is_active=false`
- **THEN** 使用该 App 下任意 Key 的请求 SHALL 返回 401
