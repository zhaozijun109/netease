import { describe, it, expect } from "vitest";
import {
  truncate,
  extractPromptSummary,
  extractCompletionSummary,
  extractErrorMessage,
  extractRequestParams,
} from "@/lib/summary-extractor";

describe("truncate", () => {
  it("短于 maxLen 时原样返回", () => {
    expect(truncate("hello", 200)).toBe("hello");
  });
  it("超过 maxLen 时截取并追加 ...", () => {
    const long = "a".repeat(300);
    expect(truncate(long, 200)).toBe("a".repeat(200) + "...");
  });
  it("null/undefined 返回 null", () => {
    expect(truncate(null, 200)).toBeNull();
    expect(truncate(undefined, 200)).toBeNull();
  });
  it("空字符串返回 null", () => {
    expect(truncate("", 200)).toBeNull();
  });
});

describe("extractPromptSummary", () => {
  it("提取最后一条 user message 的 content", () => {
    const body = {
      messages: [
        { role: "system", content: "You are helpful." },
        { role: "user", content: "第一个问题" },
        { role: "assistant", content: "回答1" },
        { role: "user", content: "第二个问题，这才是最后一条" },
      ],
    };
    expect(extractPromptSummary(body)).toBe("第二个问题，这才是最后一条");
  });
  it("多模态 content（数组）拼接 text 部分", () => {
    const body = {
      messages: [{
        role: "user",
        content: [
          { type: "text", text: "看这张图" },
          { type: "image_url", image_url: { url: "data:..." } },
          { type: "text", text: "帮我分析" },
        ],
      }],
    };
    expect(extractPromptSummary(body)).toBe("看这张图\n帮我分析");
  });
  it("无 messages 返回 null", () => {
    expect(extractPromptSummary({ input: "text" })).toBeNull();
  });
  it("超长 content 被截取", () => {
    const body = { messages: [{ role: "user", content: "x".repeat(300) }] };
    expect(extractPromptSummary(body)).toBe("x".repeat(200) + "...");
  });
});

describe("extractCompletionSummary", () => {
  it("从 choices[0].message.content 提取", () => {
    const body = { choices: [{ message: { role: "assistant", content: "这是回复" } }] };
    expect(extractCompletionSummary(body)).toBe("这是回复");
  });
  it("无 choices 返回 null", () => {
    expect(extractCompletionSummary({ error: "bad" })).toBeNull();
  });
  it("超长截取", () => {
    const body = { choices: [{ message: { content: "y".repeat(300) } }] };
    expect(extractCompletionSummary(body)).toBe("y".repeat(200) + "...");
  });
});

describe("extractErrorMessage", () => {
  it("从 error.message 提取", () => {
    const body = { error: { message: "Model not found", type: "invalid_request" } };
    expect(extractErrorMessage(body)).toBe("Model not found");
  });
  it("无 error 返回 null", () => {
    expect(extractErrorMessage({ choices: [] })).toBeNull();
  });
});

describe("extractRequestParams", () => {
  it("提取 temperature / max_tokens / stream", () => {
    const body = { messages: [], temperature: 0.7, max_tokens: 4096, stream: true, model: "gpt-4" };
    expect(extractRequestParams(body)).toEqual({ temperature: 0.7, max_tokens: 4096, stream: true });
  });
  it("缺少的参数不出现在结果中", () => {
    const body = { messages: [], temperature: 0.5 };
    expect(extractRequestParams(body)).toEqual({ temperature: 0.5 });
  });
  it("无有效参数返回 null", () => {
    expect(extractRequestParams({ messages: [] })).toBeNull();
  });
});
