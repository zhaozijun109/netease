import { createServer, type Server } from "node:http";

// 非流式响应：固定的 OpenAI 格式 JSON（含 usage）
const NON_STREAMING_RESPONSE = {
  id: "chatcmpl-test",
  object: "chat.completion",
  model: "gpt-4",
  choices: [
    {
      index: 0,
      message: { role: "assistant", content: "这是一个测试回复" },
      finish_reason: "stop",
    },
  ],
  usage: { prompt_tokens: 10, completion_tokens: 5, total_tokens: 15 },
};

// 流式响应：多个 SSE chunk，最后一个含 usage
function streamingChunks(): string[] {
  return [
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { role: "assistant" } }] })}\n\n`,
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { content: "流式" } }] })}\n\n`,
    `data: ${JSON.stringify({ id: "chatcmpl-test", model: "gpt-4", choices: [{ index: 0, delta: { content: "回复" } }] })}\n\n`,
    `data: ${JSON.stringify({
      id: "chatcmpl-test",
      model: "gpt-4",
      choices: [{ index: 0, delta: {} }],
      usage: { prompt_tokens: 10, completion_tokens: 2, total_tokens: 12 },
    })}\n\n`,
    "data: [DONE]\n\n",
  ];
}

// 错误响应：model 包含 "bad" 时返回 400
const ERROR_RESPONSE = {
  error: { message: "Model not found: bad-model", type: "invalid_request_error" },
};

/** 创建模拟 AIGW 的 HTTP Server */
export function createMockAigwServer(): Server {
  return createServer((req, res) => {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      const parsed = body ? JSON.parse(body) : {};

      // 模拟错误场景：model 包含 "bad"
      if (parsed.model?.includes("bad")) {
        res.writeHead(400, { "Content-Type": "application/json" });
        res.end(JSON.stringify(ERROR_RESPONSE));
        return;
      }

      // 流式响应
      if (parsed.stream) {
        res.writeHead(200, {
          "Content-Type": "text/event-stream",
          "Cache-Control": "no-cache",
          Connection: "keep-alive",
        });
        for (const chunk of streamingChunks()) {
          res.write(chunk);
        }
        res.end();
        return;
      }

      // 非流式响应
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify(NON_STREAMING_RESPONSE));
    });
  });
}

/** 启动 mock server，返回 baseUrl 和关闭函数 */
export async function startMockAigw(): Promise<{
  baseUrl: string;
  close: () => Promise<void>;
}> {
  const server = createMockAigwServer();
  await new Promise<void>((resolve) => server.listen(0, resolve));
  const addr = server.address();
  const port = typeof addr === "object" && addr ? addr.port : 0;
  return {
    baseUrl: `http://localhost:${port}`,
    close: () => new Promise((resolve) => server.close(() => resolve())),
  };
}
