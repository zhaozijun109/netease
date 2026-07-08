## Why

这是一个全新工程的初始化。部门需要一个统一的 AI 基础服务，作为公司 AIGW 的透明代理层，让内部用户和业务系统可以像使用 OpenAI API 一样接入 AI 能力——只需替换 BaseURL 和 API Key，无需理解 AIGW 的 appcode/key 鉴权机制。

本次变更聚焦于**项目脚手架和基础设施搭建**，为后续功能开发奠定基础。

## What Changes

- 建立 Next.js 14 (App Router) 项目结构（已完成脚手架初始化）
- 集成 shadcn/ui 组件库（已完成初始化和常用组件安装）
- 定义 Prisma 数据库 Schema：apps、api_keys、request_logs 三张核心表
- 搭建多环境配置体系（test / pre / prod），通过 `APP_ENV` 切换
- 创建核心工具库：数据库连接、日志（pino）、LRU 缓存、环境配置加载
- 建立项目目录规范和代码组织结构

## Capabilities

### New Capabilities
- `data-model`: 数据库 Schema 定义，包括 apps（AIGW 应用凭证）、api_keys（自发行密钥）、request_logs（请求日志）
- `env-config`: 多环境配置管理，支持 test/pre/prod 三套环境，包含数据库、AIGW 地址等配置
- `core-libs`: 核心工具库，包括 Prisma 客户端单例、pino 日志、LRU 缓存封装

### Modified Capabilities
<!-- 全新工程，无已有 capability 需要修改 -->

## Impact

- `/app/prisma/schema.prisma`: 新增完整数据库模型定义
- `/app/src/lib/`: 新增 db.ts、logger.ts、cache.ts、env.ts 等核心库文件
- `/app/.env.test`, `.env.pre`, `.env.prod`: 新增多环境配置文件
- `/app/package.json`: 核心依赖已安装（prisma, pino, lru-cache, next-auth）
