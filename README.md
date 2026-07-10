# CodeMaker OpenAI / Claude Proxy

将 OpenAI 兼容的 `/v1/chat/completions` 与 Anthropic 兼容的 `/v1/messages` 请求代理到网易 CodeMaker LLM 服务。

支持 Continue、Cline、Claude Code 等常见 AI 工具。

**特性亮点：**

- 自动 Prompt Caching — 通过 `codebase_chat_stream` 端点注入 `cache_control` 标记，多轮对话中输入 token 计费降低约 80~90%
- 用量可视化查询 — 内置 `/v1/usage` 接口，支持终端 ASCII 和浏览器图形化展示
- 会话自动管理 — 登录态持久化、token 自动续期、失效自动重登

## 快速启动

**前置条件:** Node.js >= 18

### 方式一：一键安装，后台运行（推荐）

安装后代理在后台持续运行，关闭终端也不受影响。

**macOS / Linux：**

```bash
curl -fsSL "https://lofter.lf127.net/codemaker-proxy/install.sh?v=$(date +%s)" | bash
```

安装完成后执行以下命令使当前终端生效：

```bash
source ~/.zshrc
```

之后随时可用：

```bash
codemaker-proxy-start   # 后台启动，不占用当前终端
codemaker-proxy-stop    # 停止
codemaker-proxy-status  # 查看是否在运行
codemaker-proxy-log     # 实时查看日志
codemaker-proxy-update  # 更新到最新版
```

**Windows** — 在**管理员** PowerShell 中执行（注册为系统服务，开机自启）：

```powershell
iex (New-Object Net.WebClient).DownloadString('https://lofter.lf127.net/codemaker-proxy/install.ps1')
```

之后在普通 PowerShell 中随时可用：

```powershell
Start-CodemakerProxy      # 启动服务
Stop-CodemakerProxy       # 停止服务
Get-CodemakerProxyStatus  # 查看状态
Watch-CodemakerProxyLog   # 实时日志
Update-CodemakerProxy     # 更新（需管理员）
```

### 方式二：直接运行（前台）

下载 `codemaker-proxy.mjs` 后直接运行，适合临时使用或调试：

```bash
# 本地已有文件时
node codemaker-proxy.mjs

# 或一行命令下载并运行
curl -fsSL https://lofter.lf127.net/codemaker-proxy/codemaker-proxy.mjs -o codemaker-proxy.mjs && node codemaker-proxy.mjs
```

首次运行会自动打开浏览器完成登录，之后会复用已有会话。登录状态保存在 `~/.codemaker-proxy/state.json`，token 过期时自动续期。

### 指定端口

```bash
node codemaker-proxy.mjs --port 8080
```

或通过环境变量：

```bash
PORT=8080 node codemaker-proxy.mjs
```

### （开发者）已克隆源码时的后台启动方式

若已克隆本项目，可通过以下方式注入后台管理命令（路径按需修改）：

```bash
export CODEMAKER_PROXY_HOME="$HOME/Documents/code/coder-maker-proxy"
[[ -f "$CODEMAKER_PROXY_HOME/scripts/codemaker-proxy-daemon.sh" ]] && source "$CODEMAKER_PROXY_HOME/scripts/codemaker-proxy-daemon.sh"
```

将上述内容添加到 `~/.zshrc` 末尾，然后执行 `source ~/.zshrc` 或新开终端即可。

> **提示：** 不设置 `CODEMAKER_PROXY_HOME` 时，脚本也会自动查找 `~/.local/bin/codemaker-proxy.mjs`（即 install.sh 的安装位置）。

### 指定运行模式（查看访问明细日志）

默认模式为 `standard`。  
如果你想确认每次调用是否成功，建议使用 `verbose` 模式：

```bash
node codemaker-proxy.mjs --mode verbose
```

或通过环境变量：

```bash
RUN_MODE=verbose node codemaker-proxy.mjs
```

`verbose` 会打印两类日志：
- `access`：本地代理收到请求并返回响应的状态、耗时
- `upstream`：代理访问 CodeMaker 上游的状态、耗时、模型、缓存命中、重试情况

示例（简化）：

```text
2026-02-10T07:00:00.000Z [upstream] endpoint=/api/v1/gpt/codebase_chat_stream/CodeChat.codebase attempt=1 status=200 duration=412ms model=claude-sonnet-4-5 stream=true retrying=no error=-
2026-02-10T07:00:00.010Z [upstream] cache_read=6603 cache_creation=12 input=3 output=15
2026-02-10T07:00:00.010Z [access] POST /v1/chat/completions -> 200 430ms from ::1
```
## 配置 AI 工具

启动后将 AI 工具的 API Base URL 设置为：

```
http://localhost:3031
```

### Claude Code

将 Claude Code 环境变量设置为：

```bash
export ANTHROPIC_BASE_URL="http://localhost:3031"
export ANTHROPIC_API_KEY="dummy"
```

说明：
- 代理会监听 Anthropic 兼容路径（`/v1/messages` 等）。
- 当前不强制校验 `x-api-key` / `ANTHROPIC_AUTH_TOKEN`，可使用任意占位值。
- 无需配置 `ENABLE_ANTHROPIC_API` 之类的开关，新接口默认启用。

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/models` | CodeMaker 官方用户模型列表（与 Chat 内可选模型一致），需已登录 |
| `POST` | `/v1/chat/completions` | OpenAI 兼容的 Chat Completions 代理 |
| `POST` | `/v1/messages` | Anthropic 兼容的 Messages 接口 |
| `POST` | `/v1/messages/count_tokens` | Anthropic 兼容的 token 统计接口 |
| `GET` | `/v1/usage` | 用量/配额查询（支持 JSON、ASCII、HTML 多种格式） |
| `GET` | `/auth/login` | 浏览器登录（GET，便于 curl 或浏览器直接触发，支持 query 参数） |
| `POST` | `/auth/login` | 浏览器登录（启动时自动执行） |
| `POST` | `/auth/token` | 直接注入 token（无浏览器环境） |
| `GET` | `/health` | 健康检查 |

### 用量查询 `GET /v1/usage`

查询当前用户在 CodeMaker 上的月度用量和配额信息。

**Query 参数：**

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `quota_type` | `global` | 配额类型 |
| `format` | 自动检测 | 输出格式：`summary`、`raw`、`text`、`html` |

**输出格式说明：**

- **`summary`** — JSON 摘要（CLI / curl 默认），包含 `usage_cost`、`monthly_quota`、`remaining`、`usage_percent` 等字段
- **`raw`** — 透传上游原始 JSON 数据
- **`text`** — 终端友好的 ASCII 可视化，带进度条和状态标识
- **`html`** — 浏览器友好的可视化卡片页面，带彩色进度条和状态徽章

未指定 `format` 时，根据请求的 `Accept` 头自动选择：浏览器访问返回 HTML，其他返回 JSON。

**使用示例：**

```bash
# 终端 ASCII 可视化（推荐日常使用）
curl http://localhost:3031/v1/usage?format=text

# JSON 摘要
curl http://localhost:3031/v1/usage

# 浏览器直接打开（自动返回 HTML 页面）
open http://localhost:3031/v1/usage
```

**终端输出示例：**

```
  CodeMaker Usage Report
  ========================================
  User:       xxx
  Month:      2026-03
  ──────────────────────────────────────────
  Used:       12.34 / 50.00
  Remaining:  37.66
  Status:     [OK]
  ──────────────────────────────────────────
  [========-----------------------] 24.7%
  ========================================
```

## 启动流程

```
node codemaker-proxy.mjs
  │
  ├─ 加载 ~/.codemaker-proxy/state.json
  │
  ├─ 有 token → 验证有效性
  │               ├─ 有效 → 启动服务器
  │               └─ 无效 → renewToken
  │                          ├─ 成功 → 启动服务器
  │                          └─ 失败 ─┐
  │                                    │
  ├─ 无凭证 ──────────────────────────→ 打开浏览器登录 → 启动服务器
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `3031` | 监听端口 |
| `RUN_MODE` | `standard` | 运行模式（`standard` 或 `verbose`） |
| `CODE_MAKER_API_URL` | `https://api-code-maker.nie.netease.com` | CodeMaker API 地址 |
| `AUTH_URL` | `https://auth.nie.netease.com` | 认证服务地址 |
| `DEFAULT_CM_ENDPOINT` | `/api/v1/gpt/codebase_chat_stream/CodeChat.codebase` | 上游 API 端点（已默认使用支持 Prompt Caching 的 codebase 端点） |
| `DEFAULT_MODEL` | `claude-sonnet-4-5` | 默认模型 |
| `DEFAULT_TEMPERATURE` | `0` | 默认温度 |
| `DEFAULT_MAX_TOKENS` | `4096` | 默认最大 token 数 |
| `DEFAULT_PRESENCE_PENALTY` | `0` | 默认 presence penalty |
| `LOGIN_TIMEOUT` | `120000` | 登录超时（毫秒） |
| `REQUEST_TIMEOUT` | `300000` | 上游请求超时（毫秒） |
| `BODY_LIMIT` | `50mb` | 请求体大小限制（支持 b/kb/mb/gb 单位） |
| `MODELS_API_URL` | `https://codemaker-webview-prod.nie.netease.com/proxy/cm/get_user_models` | 模型列表接口完整 URL，一般无需修改 |
| `CODEMAKER_PLUGIN_VERSION` | `26.3.4` | 请求头 `codemaker-version` / `plugin_version`，与官方插件版本对齐，可按需覆盖 |
| `REPORTS_BATCH_URL` | （空） | 若需与智聊/插件统计一致，可设为 CodeMaker WebView 的 reports:batch 地址（如 `https://codemaker-webview-prod.nie.netease.com/proxy/gpt/reports:batch`），每次补全成功后会异步补发一条 `CodeChat.token_used` 埋点 |

## 更多文档

- [Prompt Caching 方案设计与验证报告](docs/prompt-caching-report.md)：codebase_chat_stream 端点切换、cache_control 注入策略、缓存命中率验证和成本节省估算。
- [CodeMaker 使用数据记录与上报分析](docs/codemaker-usage-reporting-analysis.md)：插件侧 Sigma 上报与后端统计的简要分析，以及在使用 proxy 时如何补发一致数据、避免暴露「只桥接未使用插件」的可行方案。

## 本地开发

```bash
git clone <repo-url>
cd codemaker-proxy
pnpm install
pnpm start        # tsx 开发模式
pnpm build        # 构建单文件 dist/codemaker-proxy.mjs
pnpm typecheck    # 类型检查
```

## 项目结构

```
src/
├── index.ts              # 入口：自动化初始化 + 启动
├── server.ts             # HTTP 服务器 & 路由分发
├── config.ts             # 集中配置
├── types.ts              # 类型定义
├── routes/
│   ├── auth.ts           # 认证路由
│   ├── models.ts         # 模型列表 GET /v1/models
│   ├── completions.ts    # OpenAI 代理路由
│   ├── anthropic.ts      # Anthropic 兼容路由
│   └── usage.ts          # 用量查询 GET /v1/usage（JSON / ASCII / HTML）
├── services/
│   ├── auth.ts           # 认证逻辑（登录、token 续期、会话管理）
│   ├── proxy.ts          # OpenAI 转发、Prompt Caching 注入、共享上游调用
│   ├── reports-batch.ts  # 补全成功后向 reports:batch 补发埋点
│   ├── anthropic.ts      # Anthropic 协议转换与转发
│   └── state.ts          # 状态管理（持久化到 ~/.codemaker-proxy/state.json）
└── utils/
    ├── http.ts           # HTTP 工具函数
    ├── logger.ts         # 运行模式与日志
    ├── trace.ts          # traceId 生成
    ├── platform.ts       # 跨平台浏览器打开
    ├── token-usage.ts    # 上游 token 与缓存用量统计
    └── upstream-error.ts # 上游错误处理
```
