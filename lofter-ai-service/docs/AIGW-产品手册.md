# AIGW 产品手册

> 大模型统一接入服务，致力于构建标准化、智能化、安全可控的 AI 能力服务网络
>
> 来源：https://aigw.doc.nie.netease.com/
>
> 由网易互娱-技术中心-效能研发部维护

---

## 目录

- [1. 介绍](#1-介绍)
- [2. 联系我们](#2-联系我们)
- [3. 合规声明](#3-合规声明)
- [4. SLO说明](#4-slo说明)
- [5. 账号申请](#5-账号申请)
- [6. 开发指南](#6-开发指南)
  - [6.1 基本说明](#61-基本说明)
  - [6.2 身份认证](#62-身份认证)
  - [6.3 限流](#63-限流)
  - [6.4 积分Credit](#64-积分credit)
  - [6.5 预扣积分机制](#65-预扣积分机制)
  - [6.6 认证与管理](#66-认证与管理)
  - [6.7 信控](#67-信控)
  - [6.8 计费](#68-计费)
  - [6.9 功能增强](#69-功能增强)
- [7. 供应商指南](#7-供应商指南)
  - [7.1 通用能力](#71-通用能力)
  - [7.2 OpenAI](#72-openai)
  - [7.3 Google Gemini](#73-google-gemini)
  - [7.4 Anthropic Claude](#74-anthropic-claude)
  - [7.5 Moonshot](#75-moonshot)
  - [7.6 Deepseek](#76-deepseek)
  - [7.7 Aliyun 通义千问](#77-aliyun-通义千问)
  - [7.8 Ark火山 豆包系列](#78-ark火山-豆包系列)
  - [7.9 其他供应商](#79-其他供应商)
- [8. AIGW功能指南](#8-aigw功能指南)
  - [8.1 安全能力](#81-安全能力)
  - [8.2 开放计费](#82-开放计费)
  - [8.3 计费标签](#83-计费标签)
  - [8.4 用户信息注入](#84-用户信息注入)
  - [8.5 API接口](#85-api接口)
- [9. FAQ](#9-faq)
  - [9.1 常见问题](#91-常见问题)
  - [9.2 API报错](#92-api报错)
  - [9.3 私有化部署模型说明](#93-私有化部署模型说明)

---

## 1. 介绍

AI Gateway（AIGW）是大模型统一接入服务，致力于构建标准化、智能化、安全可控的 AI 能力服务网络。

**支持的模型包括：**
- OpenAI (GPT)
- Anthropic (Claude)
- VertexAI (Gemini)
- DeepSeek
- 内部私有部署模型（Qwen、R1等）

**核心功能：**
- OpenAI 兼容格式接入
- 身份认证和限流
- 计费统计
- 资源选路

**其他特性：**
1. 开箱即用，自动计费
2. 限流和资源使用控制
3. 官方 SDK 完全支持
4. 兼容开源库（Langchain、ChatBox、Interpreter）

**部分用户：** BrainMaker、CodeMaker、DreamMaker、互娱 AILab、WebGPT 等产品。

---

## 2. 联系我们

- **AIGW 值班**：grp.aigw@corp.netease.com
- **用户群**：5126662

**产品管理：**
- 产品经理：林香鑫
- 策划：谢诗浩、石昱星

**技术团队：** 叶嘉祺、肖晗、马安驰

**创作类大模型负责人**（美术、音频、视频）：谢诗浩、张新楠

---

## 3. 合规声明

### 一、大模型安全合规说明

平台已对引入的外部商业 AI 服务进行梳理，涵盖：备案情况、内容安全、数据安全、用户数据使用、生成内容标识及知识产权等方面。

### 二、应用场景安全合规风险提醒

#### 2.1 对外部提供服务（to B/to C）

- **大模型及算法备案**：使用已备案的外部 AI 模型
- **内容安全审核**：对用户输入输出进行过滤
- **内容标识**：生成的文本、图片、音视频需标注为"AI生成"
- **个人信息保护**：获得明确用户同意，特别涉及敏感信息
- **数据跨境问题**：评估数据存储区域的合规性
- **商用性评估**：确认生成内容商用权及知识产权归属

#### 2.2 AI 能力作为内部工具使用

- 禁止上传公司敏感数据到外部服务
- 评估数据处理合规性（训练使用、跨境传输等）
- 进行商用性评估

#### 2.3 内部员工个人使用

- 关注个人信息保护
- 防止公司敏感信息泄露

### 三、相关法律法规及国家标准

《互联网信息服务算法推荐管理规定》、《互联网信息服务深度合成管理规定》、《生成式人工智能服务管理暂行办法》、《个人信息保护法》等。

### 四、Netease Overseas AI Guidelines

在满足以下条件时，员工可无需批准自行使用 AI：
- 不向第三方 AI 模型披露网易敏感或机密信息
- 不披露个人信息
- 不披露网易知识产权
- 非 GLO 批准的第三方工具输出不应直接用于关键项目或消费产品

**最佳实践：**
- 验证优先："AI 经常出错，人工监督始终必需"
- 文档化和公开：记录 AI 使用情况，避免隐瞒
- 谨慎第三方 IP：许多 AI 模型训练于含未授权第三方 IP 的数据集
- 考虑替代方案：探索企业级或商业许可版本

---

## 4. SLO说明

AIGW 的服务质量影响因素首先是各个模型供应商本身的服务水平，其次是 AIGW 服务的服务质量。

### 模型使用阶段分类

| 阶段 | 说明 |
|------|------|
| 稳定生产阶段 | 功能和可用性均稳定，有多个实际业务落地，服务质量较为稳定 |
| 准生产阶段 | 功能基本稳定但落地业务较少，服务质量未充分验证 |
| 测试阶段 | 功能不够稳定，不建议在生产环境使用 |
| 体验阶段 | 可用资源量少，仅提供试用 |

虽然AIGW采取多区域、多实例部署等措施，但在供应商本身故障、基础设施故障（海外链路、云故障等）等场景仍难以避免服务质量受影响。

**建议**：在请求时使用重试机制；对AI服务高度依赖的客户应主动沟通保障方案。

---

## 5. 账号申请

### 正式账号申请流程

1. **获取 AIGW 账号**：访问 https://modelspace.netease.com/model_access/app_manage
2. **申请账号积分**：
   - 艺设用户：通过 OA 流程申请
   - 非艺设用户：先采购 ICC 申请单，再将 app_code 绑定 ICC 充值
3. **查看开发指南**：访问 AIGW 文档站点学习使用

### 体验账号

- 仅限 3 个工作日可用
- 专用于测试网络连通性
- 不可续期
- 联系方式：grp.aigw@corp.netease.com

### 测试示例

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
  --header 'Authorization: Bearer {app_id}.{app_key}' \
  --header 'Content-Type: application/json' \
  --data-raw '{"model": "gpt-4-1106-preview", "messages": [{"role": "user", "content": "hi"}]}'
```

### 常见问题

- **查看积分余额**：在 ModelSpace 管理页面输入 app code
- **查看账号 KEY**：AIGW 不保留 app_key，需重置
- **特殊账户**：_dm 开头账号等特殊情况

---

## 6. 开发指南

### 6.1 基本说明

#### 环境说明 - 域名列表

当前仅提供 API 服务接口。

**生产环境：**
- 内网（idc 推荐）：`aigw-int.netease.com`
- 外网（办公网可用）：`aigw.netease.com`

> aigw 的域名只提供给公司内部使用，若要给外部人使用模型服务，建议做代理转发。

#### 如何选择域名

从网络流量成本和连通性速度考虑，应优先使用 `aigw-int.netease.com`。

测试内网连通：

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
  --header 'Authorization: Bearer {app_id}.{app_key_prod}' \
  --header 'Content-Type: application/json' \
  --data-raw '{"model": "gpt-4-1106-preview", "messages": [{"role": "user", "content": "hi"}]}'
```

`aigw.netease.com` 的 LBC 白名单为 savpn 和 office。

**互娱用户推荐：**
- 工位测试：使用 `aigw-int.netease.com`（推荐）或 `aigw.netease.com`
- 研发机房：仅可使用 `aigw.netease.com`
- 海外地区：使用 `aigw-int.netease.com`

**集团用户推荐：**
- 工位测试：使用 `aigw.netease.com`
- 机房：使用 `aigw-int.netease.com`

> ip 白名单没有维护逻辑，对 aigw 有风险，因此不支持外网 IP 白名单。

#### 模型说明

可用模型信息详见 AIGW 模型资源文档。注意不同模型支持的接口不同，例如 gpt-3.5 和 gpt-4 均不支持补全接口（completions）。

#### 模型停用

模型停用表示该模型完全不可用。停用日期详见官方文档。应在停用前迁移至其他可用模型。

#### 重试机制

代理请求供应商接口时，状态响应码为 429 或 5xx 会自动重试。重试时间间隔为 0.5/1.0/2.0/2.0/2.0 秒，共耗时 7.5 秒（六次请求包含初次）。超时不重试。

#### 流式响应

本服务实现 SSE（Server-Sent Events），代理时提供相同的流式响应。

#### 上下文长度

上下文长度限制与各供应商一致。

---

### 6.2 身份认证

使用 AIGW 无需对接 OpenAI 等供应商的认证体系，仅需获取本服务的身份认证信息。提供两种认证方式。

#### 认证方式一：OpenAI 格式兼容的 APP 账号认证（首选）

在 modelspace 的 APP 管理申请账号，获得两个凭证：

- **App ID**：用户唯一代号，非敏感信息。示例：`m2x3d00k-dei9-q7`
- **App Key**：用户密钥，敏感信息。示例：`4emjaubh3ja4ob2jlai4uvls82tk317d`

凭证通过点号连接：`{{app_id}}.{{app_key}}`

完整示例：`m2x3d00k-dei9-q7.4emjaubh3ja4ob2jlai4uvls82tk317d`

放入请求头：`Authorization: Bearer m2x3d00k-dei9-q7.4emjaubh3ja4ob2jlai4uvls82tk317d`

```bash
curl --location --request POST 'https://aigw-int.nie.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer m2x3d00k-dei9-q7.4emjaubh3ja4ob2jlai4uvls82tk317d' \
--header 'Content-Type: application/json' \
--data-raw '{
    ...
}'
```

#### 认证方式二：结合 Auth 体系的账号认证

APP 账号可授权给 Auth 账号使用，降低泄露风险。

**角色权限表：**

| 角色 | 权限说明 |
|------|---------|
| 管理员 | 拥有 App 绝大多数管理权限 |
| 操作员 | 拥有查看权限，无修改权限 |
| 访问员 | 仅能通过 App 访问大模型 |

**必需请求头：**
- `X-AIGW-APP`：项目申请的 app_code
- `X-Access-Token`：在 console-auth.nie.netease.com 获取 v2 Token

```bash
curl --location --request POST 'https://aigw-int.nie.netease.com/v1/chat/completions' \
--header 'X-AIGW-APP: _dep305_trial' \
--header 'X-Access-Token: your_auth_token' \
--header 'Content-Type: application/json' \
--data-raw '{
    ...
}'
```

---

### 6.3 限流

AIGW 对各模型的调用实施限流管制。正式使用模型需联系值班人员调整配额。

**限流的三个主要目的：**
- 防止API滥用导致服务过载
- 避免误用产生非预期费用
- 确保用户公平访问API

**限流处理方案：**

1. **重试机制**：采用指数退避方式，当响应码为429或5xx时进行重试
2. **手动调整**：app管理员可在 ModelSpace 平台进行限流调整
   - 单应用最高可设至全局限流的60%
   - 超额需联系AIGW团队
   - 调整存在延迟，需数分钟后才能生效

**三种限流方式：**

| 方式 | 说明 |
|------|------|
| RPM | 每分钟请求数限制（60 rpm = 每秒可累积一次请求机会） |
| TPM | 每分钟Token消耗限制（包含输入输出总和） |
| Concurrency | 并发任务数（主要用于美术类模型） |

**Q&A：**
- 普通模型用量咨询：评估所需TPM/RPM → 未超额找管理员调整 → 超额联系AIGW值班
- 私有模型调整：联系 grp.aigw@corp.netease.com
- TPM计算：包含输入和输出的全部Token

---

### 6.4 积分Credit

AIGW 实现了积分（Credit）机制来控制每个 App 账号每月可使用的资源上限。

#### 积分机制与费用扣减流程

- 每个App账号均有月度Credit上限，超额时请求被拒（返回402状态码），直到下月自动重置
- 费用扣减采取**异步模式**：请求先被处理返回结果，之后系统异步计算费用并按比例扣除（1 RMB = 100 Credits）
- 费用更新通常在五分钟内完成

**示例流程：**
- 月初余额：10,000 Credits
- 发起请求（费用5 RMB → 500 Credits），结果已返回，异步扣费中
- 扣费完成后，余额变为9,500 Credits
- 若下次请求需1,000 Credits但余额仅500，该请求被限制

**风险管理：**
- 预警配置：https://modelspace.netease.com/model_access/alarm_config

#### 积分额度申请与生效流程（OA流程）

1. 登录公司OA系统，搜索"AI服务额度申请"
2. 填写申请信息：申请原因、App Code、每月积分限额、费用承担成本中心编码
3. 填写明细（申请明细不对任何东西生效，按示例格式填写即可）
4. 提交审批（直接上级和成本中心负责人）
5. 审批通过后约10分钟内新额度生效

**常见问题：**
- OA审批流程固定：申请人→AIGW管理员→直接上级→成本中心负责人→归档
- 特定模型（Tripo3D、Runway、MiniMax海螺视频、Midjourney悠船）单独计算；其他模型合并在"其他模型"条目
- Credit在每月1号重置；未再申请时保持原额度

**信息查询：**
- Credit余额：https://modelspace.netease.com/model_access/app_manage
- 消耗数据：https://modelspace.netease.com/model_access/operation_dashboard

---

### 6.5 预扣积分机制

原先的积分扣减发生在异步阶段，可能导致实际消耗超过预设额度。预扣积分机制应用于高成本、耗时较长的服务：

- MiniMax 视频生成接口（不含LLM）
- 悠船
- Runway
- Kling
- Tripo

**机制说明：**
1. **任务创建时**：系统计算任务消耗的积分并作为预扣积分存储
2. **请求验证时**：将"实际消耗积分+预扣积分"与总额度比较
3. **任务完成时**：根据终态释放预扣积分
   - 成功：约1-2分钟内更新为实际消耗积分
   - 失败：立即释放
   - 超时：标记失败后释放
4. **API查询时**：`query_app`接口返回的余额字段已计入预扣积分

---

### 6.6 认证与管理

AIGW 平台对账号体系和认证方式进行了调整，核心目标：
1. 简化概念：用 App 统一代替用户组
2. 丰富认证方式：新增基于 `X-Access-Token` + `App Code` 的认证
3. 与 IAM 系统对接：权限策略托管到 IAM 平台

**核心概念：**
- **App（应用账号）**：`app_code` 为账号名称，`app_id`、`app_key` 兼容原有认证方式
- **角色与权限策略**：管理员、操作员、访问员
- **成本中心信息**：每个 App 可关联 `cost_code`，用于计费和费用分摊

**新旧认证方式对比：**
- 旧方式：`Authorization: Bearer {app_id}.{app_key}`
- 新方式：`X-Access-Token`（标识调用人身份）+ `X-AIGW-APP`（标识 App 账号）

---

### 6.7 信控

信控是AIGW提供的临时额度机制。当APP余额耗尽时启用，确保业务不中断，同时要求用户尽快补充积分。

**核心概念：**
- 信控额度：100积分 = 1元
- 自动激活：余额低于0时自动启用
- 有效期限：1-7天（从激活时刻起计算）
- 服务保障：信控期间服务不中断，但需在有效期内补充积分

> 信控额度耗尽或过期后，付费模型请求将被拒绝。

#### 信控配置

**操作路径：** APP管理中心 → 选择APP → 操作 → 设置 → 信控 → 信控配置

| 配置项 | 说明 | 取值范围 | 默认值 |
|------|------|--------|------|
| 信控开关 | 是否启用信控功能 | 启用/未启用 | 未启用 |
| 信控额度 | 可使用的临时额度，100积分=1元 | 0-最大额度 | 最大额度 |
| 有效期 | 从激活时刻起计算的有效天数 | 1-7天 | 7天 |

#### 信控额度上限规则

**上月有用量的APP：**
- 计算规则：上月日均用量7倍
- 更新周期：每月初自动更新
- 公式：最大额度（积分） = 上月总消耗（积分） ÷ 30 × 7

**新建APP（无历史用量）：**
- 计算规则：基于当前配额的1/4
- 封顶限制：最高50,000积分（¥500）
- 公式：最大额度（积分） = MIN(当前配额 ÷ 4, 50,000)

#### 信控状态说明

| 状态 | 说明 | 操作建议 |
|------|------|--------|
| 未启用 | 信控功能未开启 | 建议启用以保障服务连续性 |
| 正常 | 余额降为0时，信控将自动启用 | 无需操作，保持监控 |
| 欠费中 | 信控使用中，余额为负，服务正常 | 尽快补充积分恢复正常额度 |
| 信控已失效 | 信控已失效，服务已停止 | 立即补充积分恢复服务 |
| 信控已过期 | 信控已过期，服务已停止 | 立即补充积分恢复服务 |

#### 信控通知

| 场景 | 触发条件 | 通知对象 | 紧急程度 |
|------|--------|--------|---------|
| 信控启用 | 余额低于0，信控自动激活 | APP管理员 | 警告 |
| 信控额度提醒 | 信控激活后每日10:00AM | APP管理员 | 警告 |

#### 信控常见问题

- **Q: 信控什么时候自动激活？** A: 当APP余额低于0时，如果已配置并启用信控，系统会自动激活。
- **Q: 信控额度用完了怎么办？** A: 服务将立即停止，必须补充积分才能恢复。
- **Q: 信控过期后服务会怎样？** A: 无论额度是否用完，服务都会立即停止。私有部署模型不受影响。
- **Q: 信控会自动续期吗？** A: 不会。信控是临时额度，不应长期依赖。

---

### 6.8 计费

本服务处理需要消耗 token 的请求。若响应成功，会计算 token 数并进行计费统计。

- 计费标准与各供应商保持一致
- AIGW 服务为互娱公共基础服务，不收取服务费、管理费等其他费用
- 费用每月分摊至 OA 提单的成本中心
- Token 计算的 BPE 文件每隔 12 小时自动刷新

---

### 6.9 功能增强

AIGW 相对于官方接口的功能增强点：

1. **OpenAI 流式请求增强**：官方接口在流式请求时不返回 token 消耗字段，AIGW 会返回文字 token 数量（不含图片）
2. **Anthropic 图片链接支持**：官方不支持图片 http 链接方式，AIGW 提供此支持

---

## 7. 供应商指南

### 7.1 通用能力

#### 7.1.1 Claude Code 使用

Claude Code（cc）是 Anthropic 出品的命令行智能编程工具，可配置为使用 aigw 作为模型服务端。

> cc 是非开源客户端工具，对 aigw 完全黑盒。aigw 仅提供模型接口对接能力。

**快速开始（需 Node.js 18+）：**

```bash
npm install -g @anthropic-ai/claude-code
cd your-project-directory
ANTHROPIC_BASE_URL=https://aigw.netease.com ANTHROPIC_AUTH_TOKEN=app_id.app_key claude
```

**网络要求：** 需在办公网或 savpn 网段权限内

**核心参数配置：**
- `ANTHROPIC_BASE_URL`: https://aigw.netease.com 或 https://aigw-int.netease.com
- `ANTHROPIC_AUTH_TOKEN`: 鉴权头部（app_id.app_key 格式）
- `ANTHROPIC_MODEL`: 可选，cc 优先选用的模型
- `ANTHROPIC_SMALL_FAST_MODEL`: 可选，次选较小快速模型

**支持的 Claude 模型（截止 20260206）：**
- claude-opus-4-6
- claude-3-haiku-20240307
- claude-3-sonnet-20240229
- claude-3-opus-20240229
- claude-3-5-sonnet-20240620
- claude-3-5-sonnet-20241022
- claude-3-5-haiku-20241022
- claude-3-7-sonnet-20250219
- claude-opus-4-20250514
- claude-sonnet-4-20250514
- claude-opus-4-1-20250805
- claude-opus-4-5-20251101
- claude-sonnet-4-5-20250929
- claude-haiku-4-5-20251001

**其他支持的模型：**
- 有道 Deepseek：deepseek-chat-yd, deepseek-v3.1-chat-yd-250821, deepseek-v3.2-chat-yd-251201
- Moonshot：kimi-k2.5

**常用命令：**

| 命令 | 功能 |
|------|------|
| `claude -h` | 帮助 |
| `claude update` | 更新 cc |
| `claude -v` | 查看版本 |
| `claude -c` | 恢复最近对话 |
| `claude -r` | 选择恢复历史对话 |
| `/cost` | 查看成本 |
| `/compact` | 压缩会话内容 |
| `/clear` | 清除会话内容 |

**鉴权方式：**
1. App 鉴权：直接使用 app_id.app_key
2. Auth 鉴权：通过权限中心获取 auth_token，配置在 settings.json

**常见问题：** tool_reference 工具异常时添加环境变量 `ENABLE_TOOL_SEARCH=false`

#### 7.1.2 文件系列接口

AIGW 实现了与 OpenAI 格式兼容的文件接口，支持通过 OpenAI SDK 上传文件。

**功能说明：**
- 过期时间：固定七天
- 存储方式：使用云厂商服务（当前支持 GCS）
- 访问权限：公开访问（注意数据安全）

**支持的上传目的（Purpose）：**

| Purpose | 说明 | 大小限制 |
|---------|------|---------|
| user_data | 用户数据场景，不限文件类型，在 chat.completion 中使用 | 512MB |
| batch | 用于 batch API | - |
| voice_clone | 音色克隆场景，需设置 storage_channel: minimax | 20MB |

**上传文件示例：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/files' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: multipart/form-data' \
--form 'purpose="user_data"' \
--form 'file=@"/your/path/229k.png"' \
--form 'storage_channel="gcs"'
```

上传成功后获取文件 ID，拼接为链接：`https://aigw-file-link.netease.com/{file_id}`

**其他文件接口：**
- 列出文件：`GET /v1/files?after={file_id}&limit=1&order=asc`
- 获取文件信息：`GET /v1/files/{file_id}`
- 下载文件内容：`GET /v1/files/{file_id}/content`
- 删除文件：`DELETE /v1/files/{file_id}`

#### 7.1.3 Batch 文件上传

批量任务需先上传文件。此场景下需额外添加 `model` 参数，且不能设置 `storage_channel` 参数。

上传的文件必须为 jsonl 格式：

```
{"custom_id": "request-1", "method": "POST", "url": "/v1/chat/completions", "body": {"model": "claude-opus-4-1-20250805","messages": [{"role": "user", "content": "hi"}]}}
```

**GCP 系列模型限制：**
- 文件大小限制：256MB
- 行数限制：10万行以内

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/files' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: multipart/form-data' \
--form 'purpose="batch"' \
--form 'file=@"batch_claude_chat.jsonl"' \
--form 'endpoint="/v1/chat/completions"' \
--form 'model="claude-sonnet-4-20250514"'
```

#### 7.1.4 Codex CLI 使用

Codex CLI 是轻量级终端 AI 编程助手。

**安装与配置：**

```bash
npm install -g @openai/codex
mkdir ~/.codex
```

配置文件 `~/.codex/config.toml`：

```toml
model='gpt-5-codex-2025-09-15'
model_provider="aigw"

[model_providers.aigw]
name = "aigw"
base_url = "https://aigw.netease.com/v1"
wire_api = "chat"
env_key = "AIGW_API_KEY"
```

```bash
export AIGW_API_KEY=app_id.app_key
codex
```

#### 7.1.5 批量任务接口

AIGW 实现了 OpenAI 格式兼容的 batch 系列接口。

**支持的模型：**
- claude-opus-4-1-20250805
- claude-opus-4-20250514
- claude-sonnet-4-20250514
- claude-3-7-sonnet-20250219
- claude-3-5-sonnet-20241022
- claude-3-5-haiku-20241022

**特点：** Batch 任务成本为普通请求的一半。批量任务一般在24小时内完成。

**创建任务：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/batches' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "input_file_id": "{file_id}",
    "completion_window": "24h",
    "endpoint": "/v1/chat/completions",
    "metadata": {}
}'
```

**其他接口：**
- 获取任务信息：`GET /v1/batches/{batch_id}`
- 列出任务信息：`GET /v1/batches?after={id}&limit=10`
- 取消任务：`POST /v1/batches/{batch_id}/cancel`

**SDK 示例：**

```python
import openai
import time

APP_ID = '{{your_app_id}}'
APP_KEY = '{{your_app_key}}'
END_POINT = 'https://aigw-int.netease.com/v1'

client = openai.OpenAI(
    api_key=f'{APP_ID}.{APP_KEY}',
    base_url=END_POINT,
)

# 创建批量任务
resp = client.batches.create(
    input_file_id=input_file_id,
    completion_window='24h',
    endpoint='/v1/chat/completions',
    metadata={'test': 'Hello World'}
)
created_batch_id = resp.id

# 轮循任务状态
while True:
    time.sleep(2)
    created_batch_info = client.batches.retrieve(batch_id=created_batch_id)
    if created_batch_info.status == 'completed':
        print(f"Job {created_batch_id} has completed")
        break
    elif created_batch_info.status == 'failed':
        print(f"Job {created_batch_id} failed")
        break
```

---

### 7.2 OpenAI

#### 7.2.1 接口说明

##### 对话补全 chat.completions

请求路径：`/v1/chat/completions`

与 OpenAI 官方接口基本保持一致。响应体中的 `model` 字段返回 OpenAI 模型代号。

**流式 token 计算：** AIGW 在流式响应的最后一条 chunk 增加了 `usage` 字段：

```json
data: {"id": "chatcmpl-xxx", "object": "chat.completion.chunk", "created": 1716448206, "model": "gpt-4o-2024-05-13", "choices": [{"index": 0, "delta": {}, "finish_reason": "stop"}], "usage": {"prompt_tokens": 92, "completion_tokens": 6, "total_tokens": 98}}
data: [DONE]
```

**gpt-4o-mini 说明：** 解析图片的实际费用成本与 4o 相同，但 prompt 单价约为 4o 的三十三分之一，相同图片消耗的 token 数约为 4o 的三十三倍。

##### 嵌入 embeddings

请求路径：`/v1/embeddings`（推荐）或 `/v1/engines//embeddings`

参数 input 支持：字符串、字符串数组、整数数组。
参数 encoding_format：`float`（默认）或 `base64`。

##### 文生图 images/generations

请求路径：`/v1/images/generations`

##### 模型列表和详情接口

- `/v1/models`：模型列表接口
- `/v1/models/{model}`：模型详情接口

#### 7.2.2 功能使用

使用 API、SDK 和 app 时，OpenAI 服务端点需要 `/v1` 前缀：
- AIGW: `https://aigw-int.netease.com/v1`

**结构化输出示例（Python）：**

```python
class CalendarEvent(BaseModel):
    name: str
    date: str
    participants: list[str]

resp = client.beta.chat.completions.parse(
    model=model,
    messages=messages,
    max_tokens=2000,
    response_format=CalendarEvent,
)
print(resp.choices[0].message.parsed)
```

#### 7.2.3 API

**基础请求示例：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "max_tokens": 10,
  "model": "gpt-3.5-turbo",
  "temperature": 0.8,
  "top_p": 1,
  "presence_penalty": 1,
  "messages": [{"role": "user", "content": "hi"}],
  "stream": false
}'
```

**GPT-4V 多模态请求：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "max_tokens": 10,
  "model": "gpt-4o-2024-05-13",
  "messages": [{
    "role": "user",
    "content": [
      {"type": "text", "text": "describe this image"},
      {"type": "image_url", "image_url": {"detail": "low", "url": "https://example.com/image.png"}}
    ]
  }],
  "stream": false
}'
```

> GPT-4V 系列 max_tokens 未传值时默认为较小值（实测为 16 token），可能导致响应被截断。

**文本转语音：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/audio/speech' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "tts-1-hd",
  "input": "欢迎使用 AI Gateway 服务！",
  "voice": "nova",
  "response_format": "mp3",
  "speed": 1.0
}'
```

**Tool 功能（Function Calling）示例：**

```python
import openai
import json

APP_ID = '{{your_app_id}}'
APP_KEY = '{{your_app_key}}'
END_POINT = 'https://aigw-int.netease.com/v1'

def get_current_weather(location, format='celsius'):
    if location == 'San Francisco, CA':
        return 'sunny, 30' + format
    return 'rain, 20' + format

tools = [{
    "type": "function",
    "function": {
        "name": "get_current_weather",
        "description": "Get the current weather",
        "parameters": {
            "type": "object",
            "properties": {
                "location": {"type": "string", "description": "The city and state"},
                "format": {"type": "string", "enum": ["celsius", "fahrenheit"]}
            },
            "required": ["location", "format"]
        }
    }
}]

client = openai.OpenAI(api_key=f'{APP_ID}.{APP_KEY}', base_url=END_POINT)
resp = client.chat.completions.create(
    model='gpt-4o-2024-05-13',
    messages=[{"role": "user", "content": "What's the weather in SF?"}],
    tools=tools, stream=False, max_tokens=2000,
)
```

**PDF 文件处理（仅 gpt-5 和 o3-pro 系列支持）：**

```python
completion = client.chat.completions.create(
    model="gpt-5-nano",
    messages=[{
        "role": "user",
        "content": [
            {"type": "file", "file": {"file_url": pdf_url}},
            {"type": "text", "text": "简要描述"},
        ],
    }],
)
```

#### 7.2.4 SDK

**OpenAI Python SDK 示例：**

```python
import openai

APP_ID = '{{your_app_id}}'
APP_KEY = '{{your_app_key}}'
END_POINT = 'https://aigw-int.netease.com/v1'

client = openai.OpenAI(api_key=f'{APP_ID}.{APP_KEY}', base_url=END_POINT)

# 对话补全
resp = client.chat.completions.create(
    model='gpt-4o-2024-05-13',
    messages=[{"role": "user", "content": "hi"}],
    stream=False, max_tokens=100,
)
print(resp.choices[0].message.content)

# 文生图
resp = client.images.generate(
    model="dall-e-3",
    prompt="a white siamese cat",
    size="1024x1024", quality="standard", n=1,
)

# 文本转语音
resp = client.audio.speech.create(
    model="tts-1-hd", voice="nova",
    input="欢迎使用 AI Gateway 服务！",
)
resp.stream_to_file("output.mp3")
```

**LangChain 集成：**

```bash
pip install -qU "langchain[openai]"
```

#### 7.2.5 APP 集成

AIGW URL：
- 内网（IDC推荐）：`https://aigw-int.netease.com/v1`
- 外网（办公网可用）：`https://aigw.netease.com/v1`

**支持的应用：** Auto-GPT、ChatBox、Interpreter、沉浸式翻译、Cursor、Open Claw/有道龙虾

#### 7.2.6 思维链模型

o* 系列与 gpt 模型在参数和应用场景存在区别。

**o1-preview / o1-mini 限制：**
- 仅支持文本输入（无图像）
- 不兼容 system 角色
- 不支持流式、工具调用、结构化输出
- 用 `max_completion_tokens` 替代 `max_tokens`

**o1 / o3-mini 改进：**
- o1 支持图像输入
- 支持 system 角色
- o3-mini 支持流式
- 两者支持工具调用和结构化输出
- 新增 `reasoning_effort` 参数（low/medium/high）

**gpt-5-* 系列：**
- reasoning_effort 新增 minimal 选项
- 新增 verbosity 参数（low/medium/high）

#### 7.2.7 生图模型

**支持的模型：** dall-e-3、gpt-image-1

**文生图：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/images/generations' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "gpt-image-1",
  "prompt": "white cat run",
  "n": 1, "size": "1024x1024", "quality": "auto"
}'
```

**图片编辑（仅 gpt-image-1 支持）：**

```bash
# 单张图片
curl -X POST "https://aigw-int.netease.com/v1/images/edits" \
  -H "Authorization: Bearer {{your_app_id}}.{{your_app_key}}" \
  -F "image=@image.png" \
  -F "model=gpt-image-1" \
  -F "prompt=xxxx"

# 多张图片
curl -X POST "https://aigw-int.netease.com/v1/images/edits" \
  -H "Authorization: Bearer {{your_app_id}}.{{your_app_key}}" \
  -F "image[]=@img1.png" \
  -F "image[]=@img2.png" \
  -F "model=gpt-image-1" \
  -F "prompt=描述"

# 带 Mask
curl -X POST "https://aigw-int.netease.com/v1/images/edits" \
  -H "Authorization: Bearer {{your_app_id}}.{{your_app_key}}" \
  -F "image[]=@image.png" \
  -F "mask=@mask.png" \
  -F "model=gpt-image-1" \
  -F "prompt=描述"
```

#### 7.2.8 视频模型 (Sora 2)

Sora 2 是 OpenAI 推出的视频生成模型。当前为 Preview 阶段，不适合大规模生产。

**支持的输出规格：**
- 分辨率：720×1280（竖屏）、1280×720（横屏）
- 时长：4秒、8秒、12秒
- 格式：MP4

**内容限制：**
- 仅支持18岁以下受众的内容
- 拒绝版权角色和版权音乐
- 不能生成真实人物
- 生成的视频在24小时后过期

**创建视频：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/videos' \
--header 'Authorization: Bearer xxxx' \
--form 'model="sora-2"' \
--form 'prompt="一只小兔子在草地上奔跑"' \
--form 'size="720x1280"' \
--form 'seconds="4"'
```

**查询视频状态：**

```bash
curl --location --request GET 'https://aigw-int.netease.com/v1/videos/{video_id}' \
--header 'Authorization: Bearer xxxx'
```

**下载视频：**

```bash
curl --location --request GET 'https://aigw-int.netease.com/v1/videos/{video_id}/content' \
--header 'Authorization: Bearer xxxx' --output video.mp4
```

**Python SDK 示例：**

```python
from openai import OpenAI

client = OpenAI(api_key="xxxx", base_url="https://aigw-int.netease.com/v1/")

video = client.videos.create(
    model="sora-2",
    prompt="A video of a cool cat on a motorcycle",
    size="1280x720"
)

# 轮循状态
while video.status not in ["completed", "failed", "cancelled"]:
    time.sleep(20)
    video = client.videos.retrieve(video.id)

if video.status == "completed":
    content = client.videos.download_content(video.id, variant="video")
    content.write_to_file("video.mp4")
```

---

### 7.3 Google Gemini

#### 兼容格式与原生格式

- **原生格式**：仅支持部分模型（gemini-2.0-flash-thinking-exp-1219 之前），功能有限
- **兼容格式**（推荐）：可无缝兼容 openai 生态，后续更多能力将以此方式实现

#### 兼容格式开发 - 普通对话

**支持的参数：**

```json
{
  "messages": [{"role": "user", "content": "hi"}],
  "stream": false,
  "model": "{{vertex_model}}",
  "temperature": 0.7,
  "max_tokens": 200,
  "frequency_penalty": 0.1,
  "presence_penalty": 0.1,
  "stop": "xxx",
  "top": 0.1,
  "seed": 12345,
  "response_format": {"type": "json_object"}
}
```

> Temperature 范围匹配 OpenAI [0, 2]，Gemini 范围会按比例缩放。

**Python 示例：**

```python
import openai

APP_ID = '{{your_app_id}}'
APP_KEY = '{{your_app_key}}'
END_POINT = 'https://aigw-int.netease.com/v1'

client = openai.OpenAI(api_key=f'{APP_ID}.{APP_KEY}', base_url=END_POINT)

resp = client.chat.completions.create(
    model='gemini-1.5-flash-preview-0514',
    messages=[{'role': 'user', 'content': 'hi'}],
    stream=False, max_tokens=100,
)
print(resp.choices[0].message.content)
```

**可用模型：** gemini-pro, gemini-pro-vision, gemini-1.5-pro-preview-0409, gemini-1.5-flash-preview-0514, gemini-2.0-flash-exp, gemini-exp-1206, gemini-2.0-flash-thinking-exp-1219

#### Veo 视频生成

Google 的视频生成模型，详见供应商文档。

---

### 7.4 Anthropic Claude

#### 概述

aigw 提供 anthropic 的 claude 3 及以上的模型能力。

**混合资源池：** 修改请求路径为 `/v1/chat/completions` 可使用 AWS Claude 和 GCP Claude 的混合资源，AIGW 实现自动路由分配。

#### 收费

aigw 收费标准和 anthropic 保持一致，可参考 anthropic 模型信息页和 aigw 模型信息的价格明细。

#### API

**文本对话：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "claude-3-sonnet-20240229",
  "temperature": 2, "top_p": 0.8,
  "max_tokens": 100,
  "stream": true,
  "messages": [{"role": "user", "content": "hi"}]
}'
```

**图片对话：**

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "claude-3-sonnet-20240229",
  "max_tokens": 100, "stream": false,
  "messages": [{
    "role": "user",
    "content": [
      {"type": "text", "text": "describe this image"},
      {"type": "image_url", "image_url": {"url": "https://example.com/image.jpg"}}
    ]
  }]
}'
```

> Anthropic 官方仅支持 base64 格式图片，AIGW 额外支持图片 URL。

**PDF 文件处理：**

```python
completion = client.chat.completions.create(
    model="claude-opus-4-20250514",
    messages=[{
        "role": "user",
        "content": [
            {"type": "file", "file": {"file_data": f"data:application/pdf;base64,{pdf_data}"}},
            {"type": "text", "text": "Brief description"},
        ],
    }],
)
```

> Anthropic 官方仅支持 base64 格式 PDF，AIGW 额外支持 URL 引用。

#### 工具调用

> 请求参数里有 tool 调用结果时，必须同时附加 tools 参数。

使用方式与 OpenAI Function Calling 基本一致，参见 OpenAI 部分示例。

#### Token 计算

在 OpenAI 格式下，流式响应的 token 信息附加在最后一条 JSON 中：

```
data: {..., "usage": {"prompt_tokens": 20, "completion_tokens": 35, "total_tokens": 55}}
data: [DONE]
```

#### Prompt 缓存

通过 aigw 的兼容格式，触发 Claude prompt caching 与官方实现一致。

**费用：**
- 5分钟缓存写入：1.25× 常规 prompt 价格
- 缓存命中：0.1× 常规 prompt 价格

**基本结构：**

```json
{
  "cache_control": {
    "type": "ephemeral"
  }
}
```

**缓存限制：** 每次请求最多 4 个 cache blocks，每个 block 至少约 1000-4000 tokens（因模型而异），5分钟 TTL。

**多工具缓存：** 只需在最后一个 tool 上标注 `cache_control`，前面的 tools 自动包含在该 cache block 中。

#### 思维链模型

Claude 3.7 采用混合模型架构，同时支持标准模式和扩展思维模式。

**启用扩展思维：**

```json
{
  "thinking": {
    "type": "enabled",
    "budget_tokens": 1024
  }
}
```

**自适应思维模式（Claude Opus 4.6 起推荐）：**

```json
{
  "thinking": {"type": "adaptive"},
  "reasoning_effort": "max"
}
```

**响应字段：**
- `reasoning_content`: 思维链输出
- `thinking_signature`: 思维链签名
- `redacted_thinking`: 安全标记触发时的加密输出

> 多轮对话时强烈建议把上一次响应里的思维相关输出再次传递，工具调用时为强制要求。

#### Beta 功能

在 chat.completions 请求中添加头部：`anthropic-beta: feature1,feature2`

| Beta 功能 | 说明 |
|-----------|------|
| context-1m-2025-08-07 | 启用100万上下文窗口 |
| fine-grained-tool-streaming-2025-05-14 | 流式输出时工具调用更流畅 |
| effort-2025-11-24 | Claude Opus 4.5专用，支持推理力度参数（low/medium/high） |
| output-128k-2025-02-19 | Claude Sonnet 3.7专用，AIGW默认启用 |

> 这些功能后续可能会更改，不建议使用到生产环境。

---

### 7.5 Moonshot

官方 API 接口上下文最大为 128K，不支持超长上下文（如200万字以上）。

包含子章节：概览、数据安全说明、API、工具调用、Token 计算、Kimi 2.5 使用。

---

### 7.6 Deepseek

#### 概览

官方文档参考：https://api-docs.deepseek.com/zh-cn/

包含子章节：概览、数据安全说明、API、工具调用。

#### API

请求路径：`/v1/chat/completions`

**DeepSeek-Chat 模型示例：**

```bash
curl --location --request POST 'https://aigw-int.nie.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "deepseek-chat",
  "max_tokens": 100,
  "stream": false,
  "temperature": 2,
  "top_p": 0.8,
  "messages": [{"role": "user", "content": "hi"}]
}'
```

**DeepSeek-Reasoner 模型：**

- `max_tokens`：控制最终答案长度（不含推理输出），默认 4K，最大 8K
- `reasoning_content`：输出思维链
- 上下文长度：支持 64K 最大；推理输出不计入限制
- 不支持：Function calls、JSON output、temperature、top_p 等参数

```python
import openai

client = openai.OpenAI(
    api_key=f'{APP_ID}.{APP_KEY}',
    base_url='https://aigw-int.netease.com/v1',
)

resp = client.chat.completions.create(
    model='deepseek-reasoner',
    messages=[{'role': 'user', 'content': '告诉我 1+1=?'}],
    stream=False, max_tokens=500,
)
print(resp.choices[0].message.content)
if resp.choices[0].message.reasoning_content:
    print(resp.choices[0].message.reasoning_content)
```

**流式请求：**

```python
chunks = client.chat.completions.create(
    model='deepseek-reasoner',
    messages=[{'role': 'user', 'content': '告诉我 1+1=?'}],
    stream=True, max_tokens=500,
)
for chunk in chunks:
    if hasattr(chunk.choices[0].delta, 'reasoning_content') and chunk.choices[0].delta.reasoning_content:
        print('reasoning_content: ' + chunk.choices[0].delta.reasoning_content)
    elif chunk.choices[0].delta.content:
        print('content: ' + chunk.choices[0].delta.content)
```

---

### 7.7 Aliyun 通义千问

包含以下子模型系列：
- 通义千问：概览、API、OpenAI SDK、加白功能
- 通义千问 VL（视觉语言）
- 通义千问 ASR（语音识别）
- 通义千问 Omni
- 通义星尘
- 思维链模型
- 缓存（隐式缓存）

---

### 7.8 Ark火山 豆包系列

#### 概述及普通对话

请求路径：`/v1/chat/completions`

**模型列表及能力：**

| 模型名称 | 识别图片 | Tool | 联网搜索 | 备注 |
|---------|---------|------|---------|------|
| doubao-1.5-pro-32k | ❌ | ✅ | ❌ | 动态版本→250115 |
| doubao-1.5-vision-pro-32k | ✅ | ❌ | ❌ | 动态版本→250115 |
| doubao-seed-1.6 | ✅ | ✅ | ❌ | 动态版本→250615 |
| doubao-seed-1.6-thinking | ✅ | ✅ | ❌ | 动态版本→250615 |

```bash
curl --location --request POST 'https://aigw-int.netease.com/v1/chat/completions' \
--header 'Authorization: Bearer {{your_app_id}}.{{your_app_key}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "model": "doubao-1.5-pro-32k-250115",
  "stream": false,
  "max_tokens": 100,
  "temperature": 1,
  "messages": [{"role": "user", "content": "hi"}]
}'
```

> temperature 参数传入时和 openai 相同（取值范围 [0,2] 默认为1），实际再除以2传给 ark 接口。

**Ark火山其他系列：**
- Deepseek 系列（含思维链、混合思维链）
- 视觉智能（方舟视觉大模型、智能绘图）
- 语音模型

---

### 7.9 其他供应商

#### MiniMax
- 大语言模型：概览、API、工具调用、联网搜索
- 海螺视频
- 语音模型

#### 百度
- Deepseek 系列（含思维链、联网搜索）

#### 有道
- Deepseek 系列（含思维链）

#### 智谱
- 概述、API

#### Tmax（私有部署）
- DeepSeek 系列、Gemma 系列、Internvl 系列、QwQ 系列
- 概览、数据安全说明、API、token计算

#### Midjourney悠船
悠船是 Midjourney 中国区的服务平台，模型与官方 MJ 保持一致。
- 概览、费用、数据保存及隔离、限流和错误响应、API

#### 可灵AI
- 概览、API、限流

#### Tripo（3D）
- 概览、API、限流

#### Runway
- 接口文档、限流、计费

#### BlackForestLabs
- 概览、API

#### Tencent
- 概览、API

#### Rodin
- 概览、API

#### Hitem3d
- 概览、API信息

---

## 8. AIGW功能指南

### 8.1 安全能力

#### 审核方式

**同步审核：** 需通过API对app_code开启设置，实时内容审核。

**异步审核：** AIGW将prompt和completion数据异步发送至kafka进行审核，保存结果但不影响原始请求/响应。**默认开启**。

#### 内容脱敏

当前支持模型：moonshot 和 minimax（后续将全面支持）。

- 通过公司 DAP 的分类分级服务对内容进行脱敏
- 流式模式仅支持对请求脱敏
- 脱敏会改写 prompt，可能影响业务效果

**脱敏示例：**
- 电话号码：`18816611188` → `18*******88`
- 密码：转化为 `{PASSWORD}`

---

### 8.2 开放计费

> 暂不对外放开使用

用于管理虚拟模型的计费类型、价格配置和数据上报。

**使用流程：**
1. 获取虚拟模型代号（由AIGW管理员分配）
2. 创建 token_type
3. 配置价格
4. 上报计费数据

**积分计算公式：**
```
RMB元 = token_amt / 1000 × weight
积分消耗 = token_amt / 1000 × weight × 100
```

**接口列表：**
- 查询配置：`GET /aigw/v1/open-bill?model=xxx`
- 创建 token_type：`POST /aigw/v1/open-bill/token-type`
- 配置价格：`POST /aigw/v1/open-bill/price`
- 直接上报：`POST /aigw/v1/open-bill/report`
- 预扣费：`POST /aigw/v1/open-bill/pre-report`
- 确认计费：`POST /aigw/v1/open-bill/commit-report`
- 回滚：`POST /aigw/v1/open-bill/rollback-report`
- 查询计费数据：`POST /aigw/v1/open-bill/query`

> 预扣积分在24小时后自动释放，建议及时调用 commit 或 rollback。

---

### 8.3 计费标签

AIGW 支持三个固定标签字段用于标记计费数据：`first_tag`、`second_tag`、`third_tag`。

**通过请求头传入：**

```bash
curl -X POST https://aigw-int.netease.com/v1/chat/completions \
  -H "Authorization: Bearer <your_token>" \
  -H "X-Aigw-Meta: first_tag=feature_a; second_tag=user_group_1; third_tag=campaign_2024" \
  -H "Content-Type: application/json" \
  -d '{"model": "deepseek-chat", "messages": [{"role": "user", "content": "Hello"}]}'
```

**X-Aigw-Meta 格式：**
- 多个字段使用分号 `;` 分隔
- 每个字段格式为 `key=value`
- 可以只传部分字段
- 可与其他字段组合：`X-Aigw-Meta: first_tag=abc; billing_trace_id=task_123456789012`

**标签值格式规范：** 最大长度 32 字符，支持中文、英文、数字、-、_

**数据流向：** 计费日志（BillingTokens）→ ELK / Kafka → 下游数据系统

---

### 8.4 用户信息注入

支持业务方在请求时传入个人用户标识（`user_code`），实现个人数据资产打通和细粒度成本归属。

**注入方式优先级：**

| 优先级 | 方式 | 说明 |
|------|------|------|
| 1 | app+user_token 认证 | auth_user 自动填充，无需额外传参 |
| 2 | 业务方注入 | 通过请求头或请求体传入 user_code |

**方式一：通过请求头：**

```bash
curl -X POST https://aigw-int.netease.com/v1/chat/completions \
  -H "Authorization: Bearer <your_token>" \
  -H "X-Aigw-Meta: user_code=yangzhi08" \
  -H "Content-Type: application/json" \
  -d '{"model": "deepseek-chat", "messages": [{"role": "user", "content": "Hello"}]}'
```

**方式二：通过请求体：**

```json
{
  "model": "deepseek-chat",
  "messages": [{"role": "user", "content": "Hello"}],
  "aigw": {
    "user_info": {
      "user_code": "yangzhi08",
      "record_mode": "lax"
    }
  }
}
```

**record_mode 验证模式：**
- `lax`（默认）：验证失败时请求继续处理，但 user_code 不写入计费数据
- `strict`：验证失败时返回 HTTP 400 错误

> 不允许同时通过请求头和请求体传入 user_code，否则返回 400 错误。

---

### 8.5 API接口

AIGW 侧提供的实现自定义功能的接口。使用 Auth 系统认证，请求头传递 `X-Access-Token`。

**获取Token：** https://console-auth.nie.netease.com/mymessage/mymessage （v2 token，七天有效期）

**请求规范：** 统一增加 `/aigw` 前缀

#### create_app - 创建APP

```bash
POST /aigw/v1/apps
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | app唯一标识，正则 `^[a-z0-9._-]{1,64}$` |
| name | string | 否 | app名称，可为中文 |
| type | string | 是 | project:项目账号；auth_user:个人账号 |
| cost_code | string | 是 | 成本中心代号 |
| cost_name | string | 是 | 成本中心名称 |
| credit.quota | int | 否 | 积分额度，默认0 |
| usage | string | 是 | 用途标识 |

```bash
curl --location --request POST 'https://aigw-int.netease.com/aigw/v1/apps' \
--header 'X-Access-Token: {{your_auth_token}}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "code": "_louv_test",
  "name": "_louv_测试",
  "type": "project",
  "project": "dep655",
  "cost_code": "HYJS008401",
  "cost_name": "互动娱乐事业群-技术中心-效能研发部-研发组",
  "usage": "测试 app 创建"
}'
```

响应：

```json
{
  "code": "_louv_test",
  "app_id": "sxdd9s7x3bjkfbie",
  "app_key": "x5h9v76kd1uzn85xkj9di9wj6xjyk2ix",
  "authorization_header": "Bearer sxdd9s7x3bjkfbie.x5h9v76kd1uzn85xkj9di9wj6xjyk2ix"
}
```

#### query_app - 查询APP信息

```bash
GET /aigw/v1/apps/{code}?query_roles=app_manager
```

返回 app 详情、积分余额、成员列表等。

#### query_apps_of_member - 查询成员所有APP

```bash
GET /aigw/v1/apps?member_code={member_code}
```

#### update_app - 更新APP信息

```bash
PATCH /aigw/v1/apps/{code}
```

支持更新 name、active、usage 字段。

#### add_member_to_app - 添加成员角色

```bash
PATCH /aigw/v1/apps/{code}/add_member
```

#### remove_member_from_app - 移除成员角色

```bash
PATCH /aigw/v1/apps/{code}/remove_member
```

#### desensitize - APP脱敏审核配置

```bash
PATCH /aigw/v1/apps/{code}/desensitize
```

参数：`open_req_desensitization`、`open_resp_desensitization`（bool）

---

## 9. FAQ

### 9.1 常见问题

#### 技术相关

- **Header 透传**：除了身份校验的字段，其它都会透传到各供应商。
- **Anthropic Computer Use**：不支持。AIGW 采用 OpenAI 格式，而非 Claude 原生格式。
- **模型身份识别**：模型声称自己是其他模型属于训练语料问题，是正常现象。
- **输出质量问题**：
  - `presence_penalty` 过高导致乱码
  - `frequency_penalty` 过高导致重复输出
  - 聊天总结推荐 `temperature` 设为 0.1
  - `top_p` 一般保持 0.7
- **流式中断计费**：大概率还是按没中断的情况下计费。

#### 账户与权限

- **App Key 管理**：AIGW 不存储 Key，需重置。重置会导致正在使用的服务中断。
- **App Code 创建**：跨事业部用户选 ICC 预付费，互娱内用户选后付费。不建议用 dm 开头命名。

#### 计费相关

- **积分重置规则**：
  - OA 申请：每月 1 号自动重置
  - ICC 申请：积分不重置，可延续使用
- **成本中心变更**：重新提 OA 流程重新选择成本中心，归档后的 app 成本会变成新申请的成本中心。
- **积分评估方法**：输入 Token 数 × 输入单价 + 输出 Token 数 × 输出单价（最小单位：1 Token）

#### 模型相关

- **Latest 版本**：动态版本模型在版本迭代时不需要手动切换模型代号，非 Latest 需手动切换。
- **输出被截断**：调大 `max_tokens` 参数。注意默认值通常不是最大值。
- **缓存支持**：仅 Claude、GLM、官方 DeepSeek 支持缓存。Claude 需加 `cache_control` 参数。
- **图片 Token 计算**：火山模型 = min(图片宽 × 高 ÷ 784, 单图限制)
- **DeepSeek 结构化输出**：火山 v3 不支持，v3.1 支持 JSON Schema。
- **思维链模型**：DeepSeek-v3.2-latest 支持关闭思考过程 `"thinking":{"type":"disabled"}`。
- **Gemini 2.5 Pro 内容为空**：可能陷入"过度思考"，尝试调整 `think_budget`。
- **风控加白**：在请求 `extra_body` 中加参数 `data_inspection` 和 `ark_moderation_scene`。

---

### 9.2 API报错

| 错误信息 | 解决方案 |
|---------|---------|
| `no permission to access model` | 确认 auth 账号是否已加入用户组，在 modelspace 查看 app code 成员 |
| `please try again later due to token limit (TPM/RPM)` | 超过限制，客户端重试或在 modelspace 调整限流 |
| `credit limit reached` | 积分超上限，需重新申请（OA 或 ICC） |
| `invalid Authorization value` | 检查 key 格式、auth_token 是否过期、app_key 是否被重置 |
| `invalid authorization method` | 检查 key 格式，确保加了 "Bearer" |
| `at least one contents field is required` | 检查 prompt 是否包含 user 角色消息 |
| `all messages must have non-empty content` | 修改 assistant 输入为 string 格式 |
| `llm model received multi-modal messages` | LLM 不支持多模态，检查输入内容 |
| `Cannot fetch content from the provided URL` | 通过文件接口上传或转为 gs 链接 |
| `Unsupported parameter: 'temperature'` | gpt5 以上不支持调整 temperature |
| `maxOutputTokens value of 65537` | 改为 65536 |
| `请求 gemini-3-pro-image 超时` | 资源问题，建议超时时间调到 3 分钟 |
| `Invalid signature in thinking block` | thinking_signature 入参内容不对 |
| `Free allocated quota exceeded` | 免费额度已用完，联系 AIGW 或更换模型 |

---

### 9.3 私有化部署模型说明

#### 公司私有部署模型

- **有道模型**：私有部署，支持接口调用，按使用 token 计费
- **Tmax模型**：项目私有部署，无法开放个人接口调用，按 GPU 卡时计费

#### 私有化部署特点

- 需由项目自行承担成本接入
- 适用于数据敏感场景
- 计费方式为按小时计价（占用显卡资源，与实际使用量无关）
- 不是降本方案

#### 费用表（单实例报价）

| 模型名称 | 尺寸(B) | TPM | RPM | 预估费用（元/小时） |
|---------|--------|-----|-----|-----------------|
| qwen2-72b-int4 | 72 | 100K | 25 | 12.8 |
| qwen2.5-32b | 32 | 40k | 10 | 6.2 |
| qwen2.5-72b-int4 | 72 | 40k | 10 | 6.2 |
| qwen-qwq | 32 | 50k | 10 | 6.4 |
| deepseek-r1-distilled-32b | 32 | 50K | 10 | 6.2 |
| deepseek-r1-pvt | 671 | 14K | 1 | 49.6 |

提高性能需增加实例个数。

---

## 相关阅读

- [地表编程最强：Claude 3.7 sonnet 测评](https://km.netease.com/v4/section/tm599/detail/blog/239580)
- [大模型上新: 性价比之王 DeepSeek-V3 它来了！](https://km.netease.com/v4/detail/blog/238186)
- [OpenAI 访问被禁？还有AIGW！一站搞定GPT、Claude 等50+前沿大模型](https://km.netease.com/v4/detail/blog/227337)

---

*文档最后修订：2026-03-04*
*Copyright © 2024-present 网易互娱-技术中心-效能研发部*
