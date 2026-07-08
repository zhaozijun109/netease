## Context

基础设施层已就绪：数据库表（ai_apps、ai_api_keys、ai_request_logs）、核心工具库（db.ts、logger.ts、cache.ts、env.ts）均已完成并验证。

本次变更要在这个地基上实现**代理核心**，将 ai-service 变为一个真正可用的 OpenAI 兼容代理。

### 代理请求全链路

```
调用方
  │  Authorization: Bearer svc-lofter-{hex}
  │  X-User-Code: yangzhi08        (服务 Key 必须传)
  │  Content-Type: application/json
  │  body: { model, messages, stream, ... }  ← 原封不动
  ▼
[1] API Key 验证
    LRU Cache 命中 → 直接使用
    未命中 → DB 查 api_keys JOIN apps → 写入 Cache (TTL 5min)
    Key 无效/禁用/App 禁用 → 401
  ▼
[2] User Code 解析
    type=personal  → user_code = key.user_email
    type=service && require_user_code=true  → X-User-Code header (无则 400)
    type=service && require_user_code=false → user_code = null
  ▼
[3] 构造 AIGW 请求
    删除原 Authorization
    注入 Authorization: Bearer {app.app_id}.{app.app_key}
    注入 X-Aigw-Meta: user_code={user_code}; first_tag={app.app_code}
    透传 Content-Type、anthropic-beta 等其他 header
    body 原样透传（不做任何修改）
  ▼
[4] 转发到 AIGW
    URL: {AIGW_BASE_URL}/v1/{...path}
    使用 Node.js fetch，duplex:'half'（支持流式请求 body）
  ▼
[5] 异步写日志（不阻塞响应）
    非流式：从响应 body 提取 usage 后写日志
    流式：tee stream，在流结束后提取最后一个 chunk 的 usage 写日志
  ▼
透传 AIGW 响应给调用方
    status code、Content-Type、Transfer-Encoding 等原样透传
```

### 已有基础
- Next.js 16 + App Router，`src/app/v1/[...path]` catch-all 路由已在框架支持范围内
- Prisma Client 单例（db.ts）
- LRU Cache 实例（cache.ts），max=1000，TTL=5min
- pino 日志实例（logger.ts）
- `AIGW_BASE_URL` 环境变量（env.ts）

## Goals / Non-Goals

**Goals:**
- 实现 `/v1/[...path]` 代理路由，支持所有 HTTP 方法（POST/GET/DELETE）
- 实现 API Key 验证（LRU Cache + DB 二级）
- 支持流式（SSE）和非流式响应透传
- 异步写入 request_logs（不影响响应延迟）
- 实现 Admin API（App 管理、Key 生成/吊销、使用统计）
- 实现基础管理后台页面

**Non-Goals:**
- AIGW 上游重试（AIGW 自身已有重试机制）
- 多实例缓存同步（LRU Cache 单机有效，内部系统可接受 5min 不一致窗口）
- 用量限速/按 Key 限流（初期不做）
- 完整的错误码映射（直接透传 AIGW 的错误响应）

## Decisions

### Decision 1: catch-all 路由透传，路径与 AIGW 保持一致

使用 `src/app/v1/[...path]/route.ts` 的 Next.js catch-all 路由，将 `/v1/*` 一对一映射到 `{AIGW_BASE_URL}/v1/*`，调用方与 AIGW 只有域名区别，不做任何路径重写。

**理由：** AIGW 支持 50+ 个接口（chat/completions、embeddings、images、files、batches、videos 等），逐一适配维护成本极高。透传方案让调用方可以使用 AIGW 支持的任何接口，且后续 AIGW 新增接口无需我们改代码。路径与 AIGW 完全一致，方便调用方将 base URL 从 AIGW 切换到本服务，无需任何路径改动。

**权衡：** 无法做细粒度的接口级权限控制。内部系统暂不需要，后续如有需要可以在中间件层增加路径白名单。

### Decision 2: 流式响应用 ReadableStream tee 实现非阻塞日志

流式响应（SSE）需要在透传给调用方的同时，缓冲最后一个包含 `usage` 字段的 chunk 用于日志。使用 `stream.tee()` 将上游流一分为二：一路透传给调用方，一路在后台消费提取 usage。

```typescript
const [clientStream, logStream] = upstream.body!.tee();
// logStream 在后台异步消费，不阻塞 clientStream 返回
extractUsageAndLog(logStream, logContext); // fire and forget
return new Response(clientStream, { ... });
```

**权衡：** tee() 会在内存中保持两份流的缓冲。对于普通文本响应（通常几十KB以内）可接受。视频/文件等大响应不走 chat/completions 接口，影响可控。

### Decision 3: 日志写入使用 fire-and-forget，不等待 DB 写入

```typescript
// 不 await，不影响响应时序
writeRequestLog({ ... }).catch(err => logger.error(err, 'Failed to write request log'));
```

**理由：** 日志是可丢失的辅助数据（AIGW 侧已有用量统计），不应为日志写入增加响应延迟或引入写入失败导致的请求报错。

### Decision 4: Admin API 使用 next-auth session 认证，不复用 API Key 体系

代理路由使用 `sk-/svc-` API Key 认证，Admin API 使用 `next-auth` session（登录态）认证。两套认证体系各司其职，不混用。

**理由：** API Key 是给机器调用设计的；Admin 操作（如生成 Key、查看全量 app 凭证）属于人工操作，应该有更强的会话管理（超时、CSRF 保护）。

### Decision 5: Key 生成使用 `crypto.randomBytes(16).toString('hex')`

生成 32 位 hex 随机串，前缀 `sk-{appCode}-` 或 `svc-{appCode}-`。格式携带 appCode 信息，便于调试时快速定位归属。

碰撞概率：2^128 分之一，实际可忽略不计，无需碰撞检测重试（但数据库有唯一约束兜底）。

### Decision 6: AIGW 的 app_key 明文存储（复用 init-scaffold 的决策）

与 API Key 明文存储的理由相同：内部系统，数据库有网络隔离。app_key 是最敏感的字段，后续可以通过 AES 加密升级。

## Risks / Trade-offs

### [Node.js fetch 在流式大响应下的内存压力] → tee() 缓冲问题
tee() 的日志分支如果消费慢，会导致背压累积。对于文本 LLM 响应（通常 < 1MB），可接受。如果未来支持视频生成等大文件接口，需要改为只提取响应头中的用量信息，跳过 body tee。

### [单机 LRU Cache 在多实例部署时的 Key 吊销延迟] → 最多 5min 不一致
Key 被吊销后，各实例的 Cache 最多还会使用 5 分钟。对内部系统可接受。
若需即时生效，可后续引入 `invalidateKey` 接口 + Redis pub/sub 广播。

### [Admin 后台暂无细粒度权限] → 登录即管理员
当前管理后台任何登录用户都有完整管理权限。初期团队规模小，可接受。
后续可在 next-auth session 中增加 role 字段。
