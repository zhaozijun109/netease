## Context

这是一个全新工程的脚手架搭建阶段。Next.js 16 项目已初始化在仓库根目录，shadcn/ui 组件库已安装，核心依赖（Prisma、pino、lru-cache、next-auth）已就位。

当前需要完成的是**基础设施层**：数据库模型、环境配置体系、核心工具库。这些是后续所有业务功能（API Key 管理、请求代理、管理后台）的地基。

### 已有基础
- Next.js 16 + App Router + Turbopack
- Prisma 7.x（schema 已初始化，仅含 generator 和 datasource）
- shadcn/ui 常用组件已安装（button, card, dialog, table, tabs 等）
- pnpm 作为包管理器
- `.env.example` 和 `.env.test` 已创建

### 约束
- 数据库：MySQL（测试环境已有连接信息：`10.172.117.52:3331/frontend`）
- 需要与已有数据库共存（`frontend` 库），表名需加前缀或使用清晰命名避免冲突
- 三环境体系（test / pre / prod）

## Goals / Non-Goals

**Goals:**
- 定义完整的 Prisma 数据库模型（apps、api_keys、request_logs）
- 建立多环境配置加载机制
- 实现核心工具库（db.ts、logger.ts、cache.ts、env.ts）
- 确保 `prisma generate` 和 `prisma db push` 能在测试环境正常运行
- 建立清晰的 `src/lib/` 目录结构

**Non-Goals:**
- API 路由实现（代理、Key 管理接口等留给后续 change）
- 管理后台页面开发
- next-auth 登录流程配置
- CI/CD 和部署配置
- 日志持久化和日志平台对接

## Decisions

### Decision 1: 数据库表命名使用 `ai_` 前缀

因为使用的是共享数据库 `frontend`，需要避免表名冲突。所有表使用 `ai_` 前缀：
- `ai_apps`
- `ai_api_keys`
- `ai_request_logs`

在 Prisma 中通过 `@@map("ai_xxx")` 实现，模型名保持简洁（`App`、`ApiKey`、`RequestLog`）。

**为什么不用独立数据库：** 测试环境的数据库创建需要走审批流程，复用现有 `frontend` 库可以快速启动。后续如有需要可以迁移到独立库，Prisma 的 `@@map` 使得迁移成本很低。

### Decision 2: API Key 的 key_hash 方案 — 明文存储

经过权衡，API Key 采用**明文存储**而非哈希存储：

- **理由：** 这是内部系统而非面向公网的 SaaS，Key 本质上是内部凭证。明文存储使得管理后台可以展示完整 Key、支持按 Key 搜索、方便调试。
- **替代方案：** 存储 SHA-256 哈希，查询时先哈希再比对。但这会增加复杂度，且内部系统的安全需求不需要到这个级别。
- **安全补偿：** 数据库访问已有网络隔离（内网）、账号权限控制；管理后台通过 next-auth 认证。

### Decision 3: 环境配置加载策略

采用 Next.js 原生 `.env` 加载 + 自定义 `env.ts` 封装的混合方案：

```
加载优先级：.env.local > .env.{NODE_ENV} > .env
```

- `.env.example`：提交到 Git，作为配置模板
- `.env.test` / `.env.pre` / `.env.prod`：各环境的实际配置，不提交
- `src/lib/env.ts`：封装环境变量读取，提供类型安全和默认值

**APP_ENV vs NODE_ENV：** 直接使用 Next.js 的 `NODE_ENV` 体系（development / production），不再额外引入 `APP_ENV`。环境区分通过不同的 `.env` 文件在部署时指定。这样更简单，不需要自定义配置加载器。

### Decision 4: Prisma Client 输出位置

Prisma Client 生成到 `src/generated/prisma/`（已在 schema 中配置）。

- **理由：** 与 Next.js 的 `src/` 目录结构一致，import 路径直观（`@/generated/prisma`）
- 该目录已加入 `.gitignore`，由 `prisma generate` 自动生成

### Decision 5: 日志方案

使用 pino 作为统一日志库，提供一个预配置的 logger 单例：

- 开发环境：通过 `pino-pretty` 输出可读日志（彩色、缩进）
- 生产环境：输出 JSON 格式，便于日志平台采集
- 日志级别：通过 `LOG_LEVEL` 环境变量控制，默认 `info`
- 使用 pino 的 `transport` 配置来区分开发/生产的输出格式

### Decision 6: LRU Cache 配置

使用 `lru-cache` 包（v11+）实现 API Key 验证结果缓存：

- 最大容量：1000 条
- TTL：5 分钟（300,000ms）
- 缓存内容：Key 验证结果（Key 记录 + 关联的 App 记录）
- 提供 `invalidateKey(apiKey)` 方法用于 Key 状态变更时主动失效

导出为一个简单的工具模块 `src/lib/cache.ts`，避免过度抽象。

### Decision 7: 目录结构规范

```
src/
├── app/                    # Next.js App Router 路由
│   ├── api/                # API Routes（后续实现）
│   ├── (admin)/            # 管理后台页面（后续实现）
│   ├── layout.tsx
│   ├── page.tsx
│   ├── globals.css
│   └── favicon.ico
├── components/
│   └── ui/                 # shadcn/ui 组件（已有）
├── lib/                    # 核心工具库
│   ├── db.ts               # Prisma 客户端单例
│   ├── logger.ts           # pino 日志实例
│   ├── cache.ts            # LRU 缓存
│   ├── env.ts              # 环境变量封装
│   └── utils.ts            # 通用工具（已有，cn() 函数）
├── generated/
│   └── prisma/             # Prisma Client 自动生成（gitignore）
└── types/                  # 共享类型定义（后续按需添加）
```

## Risks / Trade-offs

### [共享数据库表名冲突] → `ai_` 前缀 + Prisma @@map
虽然前缀能减少冲突概率，但 `frontend` 库未来可能有同名表。如果发生冲突，需要考虑迁移到独立数据库。当前风险较低。

### [明文存储 API Key] → 网络隔离 + 管理后台认证
内部系统安全等级可接受。如果未来系统开放范围扩大，需要升级为哈希存储。迁移路径：新增 `key_hash` 字段 → 双写过渡 → 切换查询逻辑 → 删除明文字段。

### [内存 LRU 缓存在多实例部署时不共享] → 可接受
每个实例有独立缓存，Key 变更后最多有 5 分钟的不一致窗口。对于内部系统这是可接受的。如果未来需要即时失效，可以引入 Redis pub/sub 通知机制。

### [Prisma 版本较新 (v7)] → 关注 API 变化
Prisma 7.x 是最新大版本，部分 API 可能与网上常见教程不同。需要参考官方文档。好处是可以利用新特性。

## Open Questions

- ~~AIGW 的 `app_key` 在数据库中是否需要加密存储？~~ 暂不加密，原因同 API Key 明文存储的理由。后续可以加。
- `request_logs` 表的数据量增长策略？是否需要分表或定期归档？→ 暂不考虑，初期数据量不大。
