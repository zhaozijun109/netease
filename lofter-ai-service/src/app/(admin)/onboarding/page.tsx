import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { serverConfig } from "@/lib/env";

// ── 数据定义 ──────────────────────────────────────────────────────────────────

const ONBOARDING_STEPS = [
  { step: "1", from: "申请者 → 管理员", desc: "提交接入申请（业务名称、使用场景）" },
  { step: "2", from: "管理员 → AIGW 平台", desc: "在 AIGW 平台申请 App，获得 app_id / app_key" },
  { step: "3", from: "管理员 → AI Service", desc: "在本平台录入 AIGW App 凭证" },
  { step: "4", from: "管理员 → AI Service", desc: "创建 API Key（personal 或 service 类型）" },
  { step: "5", from: "管理员 → 申请者", desc: "通过安全渠道将 API Key 发送给申请者" },
  { step: "6", from: "申请者", desc: "将 API Key 配置到业务服务（环境变量 / K8s Secret）" },
  { step: "7", from: "申请者 → AI Service", desc: "调用 /v1/* 接口，验证接入正常" },
];

const KEY_TYPES = [
  {
    type: "Personal Key",
    prefix: "sk-",
    scenarios: ["个人开发调试", "探索性实验", "非生产环境测试"],
    features: ["绑定到具体用户邮箱", "用量归因到个人", "不建议用于生产服务"],
    variant: "secondary" as const,
  },
  {
    type: "Service Key",
    prefix: "svc-",
    scenarios: ["后端服务、API 调用", "生产环境部署", "自动化流水线"],
    features: ["绑定到业务系统名称", "支持多用户共用", "需通过 X-User-Code 传递用户标识"],
    variant: "default" as const,
  },
];

const ERROR_CODES = [
  { code: "400", label: "Bad Request", cause: "请求参数错误（如缺少 X-User-Code）", action: "检查请求头和参数" },
  { code: "401", label: "Unauthorized", cause: "API Key 无效、缺失或已吊销", action: "检查 Authorization 头，联系管理员" },
  { code: "403", label: "Forbidden", cause: "无权访问（越权操作）", action: "确认 Key 权限" },
  { code: "404", label: "Not Found", cause: "资源不存在", action: "检查路径或资源 ID" },
  { code: "502", label: "Bad Gateway", cause: "AIGW 网关异常", action: "稍后重试，或联系管理员" },
  { code: "504", label: "Gateway Timeout", cause: "AIGW 请求超时（默认 120s）", action: "稍后重试，检查请求复杂度" },
];

const FAQS = [
  {
    q: "API Key 丢失怎么办？",
    a: "联系管理员吊销旧 Key，重新创建新 Key。Key 只在创建时完整展示，无法找回。",
  },
  {
    q: "Service Key 调用时返回 400，提示 X-User-Code header is required？",
    a: "该 App 配置了强制用户追踪，必须在请求头中添加 X-User-Code: <用户标识>。",
  },
  {
    q: "可以用 Personal Key 跑生产服务吗？",
    a: "不建议。Personal Key 绑定个人用户，用量归因不清晰，建议生产环境统一使用 Service Key。",
  },
  {
    q: "模型调用返回 502？",
    a: "下游 AIGW 出现异常，请稍等片刻后重试。如持续出现，联系管理员排查。",
  },
  {
    q: "如何查看 token 用量？",
    a: "具有管理后台权限的用户可在「统计」页面查看。也可联系管理员提供按 App 或用户维度的用量报告。",
  },
  {
    q: "能支持哪些模型？",
    a: "AI Service 透传所有 AIGW 支持的模型，具体可用模型取决于你的 App 在 AIGW 侧的权限，请咨询管理员。",
  },
];

// ── 子组件 ────────────────────────────────────────────────────────────────────

function SectionTitle({ children }: { children: React.ReactNode }) {
  return <h2 className="text-base font-semibold">{children}</h2>;
}

function Card({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={`rounded-lg border bg-muted/40 px-4 py-3 text-sm ${className}`}>
      {children}
    </div>
  );
}

function CodeBlock({ children }: { children: React.ReactNode }) {
  return (
    <pre className="rounded-md border bg-muted/60 px-4 py-3 text-xs font-mono overflow-x-auto whitespace-pre-wrap break-all">
      {children}
    </pre>
  );
}

// ── 页面 ──────────────────────────────────────────────────────────────────────

export default function OnboardingPage() {
  const serviceUrl = serverConfig.serverUrl;
  const serviceIntUrl = serverConfig.serverIntUrl;

  return (
    <div className="space-y-10 max-w-4xl">

      {/* 页头 */}
      <div>
        <h1 className="text-2xl font-bold">接入申请指南</h1>
        <p className="text-sm text-muted-foreground mt-1">
          面向业务开发者的完整接入说明——从申请资源到发起第一个 AI 请求
        </p>
      </div>

      {/* 服务端点 */}
      <Card className="space-y-1">
        <p className="font-medium">服务端点</p>
        <div className="flex flex-wrap gap-x-6 gap-y-1 text-muted-foreground">
          <span>外网：<code className="ml-1 text-foreground font-mono text-xs">{serviceIntUrl}</code></span>
          <span>办公网：<code className="ml-1 text-foreground font-mono text-xs">{serviceUrl}</code></span>
        </div>
      </Card>

      {/* ── 1. 服务概述 ── */}
      <section className="space-y-3">
        <SectionTitle>1. 服务概述</SectionTitle>
        <p className="text-sm text-muted-foreground">
          AI Service 是 AIGW（AI Gateway）的统一接入层。业务侧只需持有 API Key，无需接触底层 AIGW 凭证。
        </p>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {[
            { label: "统一鉴权", desc: "API Key 代理访问 AIGW" },
            { label: "用量追踪", desc: "按 App、用户维度记录" },
            { label: "文件存储", desc: "配套 NOS 上传，支持多模态" },
            { label: "CORS 支持", desc: "可从浏览器端直接调用" },
          ].map((item) => (
            <div key={item.label} className="rounded-lg border p-3 space-y-1">
              <p className="text-sm font-medium">{item.label}</p>
              <p className="text-xs text-muted-foreground">{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── 2. 接入流程总览 ── */}
      <section className="space-y-3">
        <SectionTitle>2. 接入流程总览</SectionTitle>
        <p className="text-xs text-muted-foreground">
          ⚠️ API Key 只在创建时完整展示一次，请妥善保管。
        </p>
        <div className="rounded-md border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50">
                <TableHead className="w-12">步骤</TableHead>
                <TableHead className="w-52">操作方</TableHead>
                <TableHead>说明</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {ONBOARDING_STEPS.map((s) => (
                <TableRow key={s.step}>
                  <TableCell>
                    <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                      {s.step}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm font-medium">{s.from}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">{s.desc}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </section>

      {/* ── 3. Key 类型选择 ── */}
      <section className="space-y-3">
        <SectionTitle>3. 选择 Key 类型</SectionTitle>
        <p className="text-sm text-muted-foreground">
          申请前确认你的使用场景，选择合适的类型。<strong>生产环境推荐使用 Service Key</strong>。
        </p>
        <div className="grid gap-4 sm:grid-cols-2">
          {KEY_TYPES.map((kt) => (
            <div key={kt.type} className="rounded-lg border p-4 space-y-3">
              <div className="flex items-center gap-2">
                <span className="font-semibold text-sm">{kt.type}</span>
                <Badge variant="outline" className="font-mono text-xs">{kt.prefix}…</Badge>
              </div>
              <div className="space-y-1">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">适用场景</p>
                <ul className="text-sm space-y-0.5">
                  {kt.scenarios.map((s) => <li key={s} className="text-muted-foreground">· {s}</li>)}
                </ul>
              </div>
              <div className="space-y-1">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">特点</p>
                <ul className="text-sm space-y-0.5">
                  {kt.features.map((f) => <li key={f} className="text-muted-foreground">· {f}</li>)}
                </ul>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── 4. 申请材料 ── */}
      <section className="space-y-3">
        <SectionTitle>4. 提交申请材料</SectionTitle>
        <p className="text-sm text-muted-foreground">向管理员提交以下信息，App Code 和 AIGW 凭证由管理员在 AIGW 平台申请后确定，申请时无需填写。</p>

        <div className="space-y-2">
          <p className="text-sm font-medium">新业务接入（首次）</p>
          <CodeBlock>{`业务名称：<系统 / 模块名称>
使用场景：<简述用途，例如：用于内容推荐，调用 claude-3-5-haiku 模型>
Key 类型：service（推荐）或 personal
是否需要用户追踪：是（默认）/ 否
申请人：<姓名 + 邮箱>`}
          </CodeBlock>
        </div>

        <div className="space-y-2">
          <p className="text-sm font-medium">已有 App，追加 Key</p>
          <CodeBlock>{`App Code：<已有的 appCode>
Key 类型：service 或 personal
服务名称：<业务系统名称，service key 必填>
申请人：<姓名 + 邮箱>`}
          </CodeBlock>
        </div>

        <Card>
          <p className="font-medium mb-1">你会收到的信息</p>
          <p className="text-muted-foreground mb-2">管理员通过安全渠道发送（Key 只展示一次，请立即保存）：</p>
          <CodeBlock>{`API Key：sk-{appCode}-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
         或
         svc-{appCode}-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

App Code：{appCode}
服务地址：${serviceUrl}`}
          </CodeBlock>
        </Card>
      </section>

      {/* ── 5. 发起请求 ── */}
      <section className="space-y-4">
        <SectionTitle>5. 发起 AI 请求</SectionTitle>
        <p className="text-sm text-muted-foreground">
          AI Service 兼容 OpenAI API 格式，所有请求发送到 <code className="font-mono text-xs bg-muted px-1 py-0.5 rounded">/v1/*</code> 路径。
        </p>

        {/* 基础调用 */}
        <div className="space-y-2">
          <p className="text-sm font-medium">基础调用（curl）</p>
          <CodeBlock>{`curl ${serviceUrl}/v1/messages \\
  -H "Authorization: Bearer sk-lofter-xxxxxxxxxxxx" \\
  -H "Content-Type: application/json" \\
  -d '{
    "model": "claude-3-5-haiku-20241022",
    "max_tokens": 1024,
    "messages": [{"role": "user", "content": "你好"}]
  }'`}
          </CodeBlock>
        </div>

        {/* Service Key */}
        <Card className="space-y-1">
          <p className="font-medium">Service Key：必须传递用户标识</p>
          <p className="text-muted-foreground">App 配置了 <code className="font-mono text-xs">require_user_code=true</code> 时，请求头必须携带：</p>
          <code className="block font-mono text-xs bg-muted/60 rounded px-3 py-2 mt-1">
            X-User-Code: user@corp.com
          </code>
          <p className="text-muted-foreground text-xs mt-1">缺少此 Header 时服务返回 <code className="font-mono">400 Bad Request</code>。</p>
        </Card>

        {/* 代码示例 */}
        <div className="space-y-3">
          <p className="text-sm font-medium">代码示例</p>

          <div className="space-y-1">
            <p className="text-xs text-muted-foreground font-medium">Python（anthropic SDK）</p>
            <CodeBlock>{`import anthropic

client = anthropic.Anthropic(
    api_key="sk-lofter-xxxxxxxxxxxx",
    base_url="${serviceUrl}",
)
message = client.messages.create(
    model="claude-3-5-haiku-20241022",
    max_tokens=1024,
    messages=[{"role": "user", "content": "Hello"}],
)
print(message.content)`}
            </CodeBlock>
          </div>

          <div className="space-y-1">
            <p className="text-xs text-muted-foreground font-medium">TypeScript（anthropic SDK）</p>
            <CodeBlock>{`import Anthropic from "@anthropic-ai/sdk";

const client = new Anthropic({
  apiKey: "svc-lofter-xxxxxxxxxxxx",
  baseURL: "${serviceUrl}",
  defaultHeaders: { "X-User-Code": "user@corp.com" },
});
const message = await client.messages.create({
  model: "claude-3-5-haiku-20241022",
  max_tokens: 1024,
  messages: [{ role: "user", content: "Hello" }],
});`}
            </CodeBlock>
          </div>
        </div>
      </section>

      {/* ── 6. 文件上传 ── */}
      <section className="space-y-3">
        <SectionTitle>6. 文件上传（可选）</SectionTitle>
        <p className="text-sm text-muted-foreground">
          多模态场景可通过以下三步将文件上传至 NOS，获得文件 URL 后传入 AI 请求。
        </p>
        <div className="grid gap-3 sm:grid-cols-3">
          {[
            { step: "①", title: "获取上传凭证", desc: "POST /api/nos/token，返回 token、uploadUrl、uploadLogId" },
            { step: "②", title: "上传文件到 NOS", desc: "使用返回的 uploadUrl 直接 PUT 上传" },
            { step: "③", title: "通知上传完成", desc: "POST /api/nos/upload-complete，传入 upload_log_id" },
          ].map((s) => (
            <div key={s.step} className="rounded-lg border p-3 space-y-1">
              <p className="text-sm font-semibold">{s.step} {s.title}</p>
              <p className="text-xs text-muted-foreground">{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── 7. 安全须知 ── */}
      <section className="space-y-3">
        <SectionTitle>7. 安全须知</SectionTitle>
        <div className="rounded-md border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50">
                <TableHead className="w-32">规范</TableHead>
                <TableHead>说明</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                { rule: "不要硬编码", desc: "Key 不能写入代码仓库，不能明文写在配置文件中" },
                { rule: "使用密钥管理", desc: "通过环境变量或 K8s Secret 注入" },
                { rule: "定期轮换", desc: "建议每 90 天更换一次 Service Key" },
                { rule: "最小权限", desc: "不同业务使用不同的 Key，不共用" },
                { rule: "及时吊销", desc: "人员离职或 Key 泄露时立即联系管理员" },
              ].map((r) => (
                <TableRow key={r.rule}>
                  <TableCell className="text-sm font-medium">{r.rule}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">{r.desc}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </section>

      {/* ── 8. 错误码 ── */}
      <section className="space-y-3">
        <SectionTitle>8. 错误码说明</SectionTitle>
        <div className="rounded-md border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50">
                <TableHead className="w-20">状态码</TableHead>
                <TableHead className="w-36">含义</TableHead>
                <TableHead>原因</TableHead>
                <TableHead>处理建议</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {ERROR_CODES.map((e) => (
                <TableRow key={e.code}>
                  <TableCell>
                    <span className="font-mono text-xs font-semibold">{e.code}</span>
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">{e.label}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">{e.cause}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">{e.action}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </section>

      {/* ── 9. 常见问题 ── */}
      <section className="space-y-3">
        <SectionTitle>9. 常见问题</SectionTitle>
        <div className="space-y-3">
          {FAQS.map((faq) => (
            <div key={faq.q} className="rounded-lg border px-4 py-3 space-y-1">
              <p className="text-sm font-medium">Q：{faq.q}</p>
              <p className="text-sm text-muted-foreground">A：{faq.a}</p>
            </div>
          ))}
        </div>
      </section>

    </div>
  );
}
