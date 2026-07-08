## ADDED Requirements

### Requirement: Apps 表存储 AIGW 应用凭证
系统 SHALL 维护一张 `apps` 表，用于存储每个 AIGW 应用的凭证信息和配置。每条记录对应一个 `app_code`，包含其 `app_id`、`app_key`（加密存储）以及应用级别的配置选项。

#### Scenario: 创建新应用记录
- **WHEN** 管理员注册一个新的 AIGW 应用
- **THEN** 系统创建一条 apps 记录，包含 app_code（唯一）、app_id、app_key、display_name
- **AND** app_code 字段具有唯一约束

#### Scenario: 应用级配置 - require_user_code
- **WHEN** 管理员创建或编辑应用记录
- **THEN** 可以设置 `require_user_code` 字段（默认为 true）
- **AND** 该字段控制使用此 app_code 的 svc- 类型 Key 是否必须传递 X-User-Code header

#### Scenario: 应用状态管理
- **WHEN** 管理员需要禁用某个应用
- **THEN** 可以将 `is_active` 字段设置为 false
- **AND** 该应用下所有 API Key 的请求 SHALL 被拒绝

### Requirement: API Keys 表存储自发行密钥
系统 SHALL 维护一张 `api_keys` 表，用于存储为用户和服务生成的 API Key。支持 `personal`（sk- 前缀）和 `service`（svc- 前缀）两种类型。

#### Scenario: 创建个人 Key
- **WHEN** 为某用户在指定 app_code 下生成个人 Key
- **THEN** 系统创建一条 type=personal 的记录，Key 格式为 `sk-{appCode}-{32位hex}`
- **AND** 记录关联 user_email 字段

#### Scenario: 创建服务 Key
- **WHEN** 为某业务系统在指定 app_code 下生成服务 Key
- **THEN** 系统创建一条 type=service 的记录，Key 格式为 `svc-{appCode}-{32位hex}`
- **AND** 记录关联 service_name 字段标识业务系统

#### Scenario: Key 唯一性
- **WHEN** 生成新的 API Key
- **THEN** `api_key` 字段 SHALL 具有唯一约束
- **AND** 使用 crypto.randomBytes(16) 生成 hex 部分，碰撞概率可忽略

#### Scenario: Key 状态管理
- **WHEN** 需要吊销某个 Key
- **THEN** 可以将 `is_active` 字段设置为 false
- **AND** 后续使用该 Key 的请求 SHALL 被拒绝

### Requirement: Request Logs 表记录请求日志
系统 SHALL 维护一张 `request_logs` 表，用于记录每次代理请求的关键信息，支持使用统计和审计。

#### Scenario: 记录请求基本信息
- **WHEN** 一次 API 代理请求完成（成功或失败）
- **THEN** 系统写入一条日志记录，包含 app_code、api_key_id、user_code、model、endpoint、status_code、created_at

#### Scenario: 记录扩展元数据
- **WHEN** 请求日志需要记录 token 用量等可变信息
- **THEN** 系统将这些信息存入 `meta` JSON 字段
- **AND** meta 字段可包含 prompt_tokens、completion_tokens、total_tokens 等
