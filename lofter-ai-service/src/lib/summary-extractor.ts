// 摘要提取工具函数，用于从 OpenAI 请求/响应体中提取关键信息

const SUMMARY_MAX_LEN = 200;

/** 截断字符串，超出 maxLen 时追加 "..."，空值返回 null */
export function truncate(text: string | null | undefined, maxLen: number = SUMMARY_MAX_LEN): string | null {
  if (!text) return null;
  if (text.length <= maxLen) return text;
  return text.slice(0, maxLen) + "...";
}

// --- Round 2/3 实现 ---

/** 从 content 字段提取纯文本，支持字符串和多模态数组 */
function extractContentText(content: unknown): string | null {
  if (typeof content === "string") return content;
  if (Array.isArray(content)) {
    const texts = content
      .filter((p: unknown) => {
        const part = p as Record<string, unknown>;
        return part.type === "text" && typeof part.text === "string";
      })
      .map((p: unknown) => (p as Record<string, string>).text);
    return texts.length > 0 ? texts.join("\n") : null;
  }
  return null;
}

/** 从 OpenAI chat 请求体提取最后一条 user 消息内容（取前 200 字符） */
export function extractPromptSummary(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const messages = b.messages;
  if (!Array.isArray(messages)) return null;
  // 从后往前找最后一条 user 消息
  for (let i = messages.length - 1; i >= 0; i--) {
    const msg = messages[i];
    if (msg?.role !== "user") continue;
    const text = extractContentText(msg.content);
    return truncate(text, SUMMARY_MAX_LEN);
  }
  return null;
}

/** 从 OpenAI chat 响应体提取 choices[0].message.content（取前 200 字符） */
export function extractCompletionSummary(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const choices = b.choices;
  if (!Array.isArray(choices) || choices.length === 0) return null;
  const message = choices[0]?.message as Record<string, unknown> | undefined;
  if (!message) return null;
  const content = message.content;
  if (typeof content !== "string") return null;
  return truncate(content, SUMMARY_MAX_LEN);
}

/** 从错误响应体提取 error.message（取前 200 字符） */
export function extractErrorMessage(body: unknown): string | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const error = b.error as Record<string, unknown> | undefined;
  if (!error || typeof error.message !== "string") return null;
  return truncate(error.message, SUMMARY_MAX_LEN);
}

/** 从请求体提取 temperature / max_tokens / stream，三者都不存在时返回 null */
export function extractRequestParams(body: unknown): Record<string, unknown> | null {
  if (!body || typeof body !== "object") return null;
  const b = body as Record<string, unknown>;
  const params: Record<string, unknown> = {};
  if (typeof b.temperature === "number") params.temperature = b.temperature;
  if (typeof b.max_tokens === "number") params.max_tokens = b.max_tokens;
  if (typeof b.stream === "boolean") params.stream = b.stream;
  return Object.keys(params).length > 0 ? params : null;
}
