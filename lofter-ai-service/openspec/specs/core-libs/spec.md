## Requirements

### Requirement: Prisma 客户端单例
系统 SHALL 提供一个全局的 Prisma Client 单例，避免在开发模式热重载时创建过多数据库连接。

#### Scenario: 开发环境连接复用
- **WHEN** Next.js 开发服务器热重载触发模块重新加载
- **THEN** Prisma Client 实例 SHALL 从 globalThis 缓存中复用
- **AND** 不会创建新的数据库连接池

#### Scenario: 生产环境正常初始化
- **WHEN** 应用在生产环境启动
- **THEN** 系统创建一个新的 Prisma Client 实例
- **AND** 不使用 globalThis 缓存

### Requirement: 结构化日志
系统 SHALL 使用 pino 作为日志库，输出 JSON 格式的结构化日志，支持按环境配置日志级别。

#### Scenario: 日志级别受环境控制
- **WHEN** 环境变量 `LOG_LEVEL` 设置为 "debug"、"info"、"warn" 或 "error"
- **THEN** pino 实例的日志级别 SHALL 设置为对应值
- **AND** 未设置时默认为 "info"

#### Scenario: 开发环境可读日志
- **WHEN** 在开发环境（NODE_ENV=development）运行
- **THEN** 日志输出 SHALL 使用 pino-pretty 进行格式化
- **AND** 生产环境保持 JSON 格式输出

### Requirement: API Key 内存缓存
系统 SHALL 使用 LRU Cache 缓存 API Key 的验证结果，减少数据库查询压力。

#### Scenario: 缓存命中
- **WHEN** 收到一个 API 请求，其 Key 在缓存中存在且未过期
- **THEN** 系统直接使用缓存的验证结果（Key 信息 + 关联的 App 信息）
- **AND** 不查询数据库

#### Scenario: 缓存未命中
- **WHEN** 收到一个 API 请求，其 Key 不在缓存中
- **THEN** 系统查询数据库验证 Key
- **AND** 将结果写入缓存（TTL 可配置，默认 5 分钟）

#### Scenario: 缓存容量限制
- **WHEN** 缓存条目数达到上限（默认 1000）
- **THEN** LRU 策略自动淘汰最久未使用的条目
