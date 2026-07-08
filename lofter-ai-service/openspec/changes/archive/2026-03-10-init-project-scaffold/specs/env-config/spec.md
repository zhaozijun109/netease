## ADDED Requirements

### Requirement: 三环境配置体系
系统 SHALL 支持 test、pre、prod 三个环境，通过环境变量 `APP_ENV` 进行切换。每个环境拥有独立的数据库连接、AIGW 地址等配置。

#### Scenario: 环境变量决定配置加载
- **WHEN** 应用启动时 `APP_ENV` 为 "test"、"pre" 或 "prod"
- **THEN** 系统加载对应的 `.env.{APP_ENV}` 配置文件
- **AND** 未设置 `APP_ENV` 时默认为 "test"

#### Scenario: 环境配置包含必要字段
- **WHEN** 加载某环境的配置
- **THEN** 配置 SHALL 包含：DATABASE_URL（MySQL 连接串）、AIGW_BASE_URL（AIGW 网关地址）、NEXTAUTH_SECRET、NEXTAUTH_URL
- **AND** 可选包含：LOG_LEVEL（日志级别，默认 info）

#### Scenario: 环境配置文件不提交到版本控制
- **WHEN** 项目代码提交到 Git
- **THEN** `.env.*` 文件 SHALL 被 .gitignore 排除
- **AND** 提供 `.env.example` 作为配置模板
