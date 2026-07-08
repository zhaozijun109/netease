## 1. 环境配置

- [x] 1.1 更新 `.env.example` 和 `.env.test`，补充所有必要的环境变量（AIGW_APP_CODES、AIGW_APP_KEYS 等）
- [x] 1.2 创建 `src/lib/env.ts`，封装环境变量读取，提供类型安全的配置对象和默认值
- [x] 1.3 更新 `package.json` 的 name 字段为 `ai-service`，添加 `prisma:generate` 和 `prisma:push` 脚本

## 2. 数据库模型

- [x] 2.1 编写 `prisma/schema.prisma`，定义 App 模型（ai_apps 表），包含 app_code、app_id、app_key、display_name、require_user_code、is_active 等字段，添加唯一约束和索引
- [x] 2.2 编写 ApiKey 模型（ai_api_keys 表），包含 api_key、type（personal/service）、app_code 外键、user_email、service_name、is_active 等字段，添加唯一约束和索引
- [x] 2.3 编写 RequestLog 模型（ai_request_logs 表），包含 app_code、api_key_id、user_code、model、endpoint、status_code、meta（JSON）、created_at 等字段，添加索引
- [x] 2.4 运行 `prisma generate` 验证 schema 正确性，确认 Client 生成到 `src/generated/prisma/`
- [ ] 2.5 运行 `prisma db push` 在测试环境创建表结构（⚠️ 需要内网环境执行）

## 3. 核心工具库

- [x] 3.1 创建 `src/lib/db.ts`，实现 Prisma Client 单例（开发环境通过 globalThis 缓存，生产环境直接创建）
- [x] 3.2 创建 `src/lib/logger.ts`，实现 pino 日志单例（开发环境用 pino-pretty，生产环境输出 JSON，日志级别受 LOG_LEVEL 控制）
- [x] 3.3 创建 `src/lib/cache.ts`，实现 LRU Cache 工具（max=1000，TTL=5min），导出 keyCache 实例和 invalidateKey 方法

## 4. 验证

- [x] 4.1 创建 `src/app/api/health/route.ts` 健康检查端点，验证 db、logger、env、cache 模块可以正常 import 和运行
- [x] 4.2 运行 `pnpm build` 验证编译通过，所有路由正常生成
