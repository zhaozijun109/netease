## Requirements

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

### Requirement: 多行/证书类环境变量书写规范
含换行的值（如 PEM 公钥、证书）在 `.env*` 中 SHALL 使用**单行 + 字面量 `\n`** 表示换行，禁止在 .env 中写真实多行或使用脚本拼接（如 `'...' +`）。

#### Scenario: PEM 公钥格式
- **GIVEN** 需配置 OPENID_MF_PUBLIC_KEY 等 PEM 格式公钥
- **WHEN** 写入 `.env.local`、`.env.test`、`.env.pre`、`.env.prod` 或 CI 环境变量
- **THEN** 整段 PEM 为单行，换行处使用双引号包裹内的 `\n`（如 `"-----BEGIN PUBLIC KEY-----\nMIIB...\n-----END PUBLIC KEY-----"`）
- **AND** 应用层读取后通过 `replace(/\\n/g, "\n")` 还原为多行 PEM，供 crypto 等使用

#### Rationale
多行写在部分 env 解析器或 CI 中会被截断或解析异常；单行 `\n` 在各环境一致、可复制粘贴且代码已支持还原。

### Requirement: 微前端作为登录用户信息来源
微前端主应用通过 Header（如 LOFTER-ADMIN-OPEN-ID / LOFTER-ADMIN-OPEN-ID-SIGN）注入的登录信息，是**另一种获取当前登录用户的方式**；与标准 OpenID 重定向登录、从 cookie/session 获取用户信息等**等效**，验证通过后即可作为当前登录用户使用。

#### Scenario: 微前端 Header 校验通过即视为已登录用户
- **GIVEN** 请求携带微前端主应用注入的 OpenID 与签名 Header，且已配置对应 RSA 公钥（OPENID_MF_PUBLIC_KEY）
- **WHEN** 服务端使用公钥校验签名通过且未超时
- **THEN** 解析出的用户信息（如 email、昵称等）SHALL 视为当前登录用户
- **AND** 可与标准 OpenID 回调、Credentials 等登录方式同等用于建立 Session（如 next-auth authorize 优先使用 getMicroFrontendUser(request)）

#### Scenario: 与 cookie/其他来源等效，入库逻辑统一
- **GIVEN** 登录信息来自微前端 Header 校验或来自 cookie/OpenID 回调 token 等任一来源
- **WHEN** 需要将用户信息持久化（入库）
- **THEN** 系统 SHALL 使用同一套入库逻辑（如按 email upsert User 表、同步 name/role 等）
- **AND** 不因来源是 Header 或 cookie 而分支不同的写库逻辑
