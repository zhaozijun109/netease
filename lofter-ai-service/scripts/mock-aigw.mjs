/**
 * 本地 AIGW Mock Server（仅用于本地验证）
 * 模拟 AIGW 的 /v1/chat/completions 和 /v1/models 接口
 * 运行：node scripts/mock-aigw.mjs
 */
import { createServer } from "http";

const PORT = 4000;

function sseChunks(content) {
  const words = content.split(" ");
  const chunks = [];
  let id = 1;
  for (const word of words) {
    chunks.push(
      `data: ${JSON.stringify({
        id: `chatcmpl-mock${id++}`,
        object: "chat.completion.chunk",
        model: "gpt-4o-mini",
        choices: [{ delta: { content: word + " " }, index: 0, finish_reason: null }],
      })}\n\n`
    );
  }
  // 最后一个 chunk 含 usage
  chunks.push(
    `data: ${JSON.stringify({
      id: `chatcmpl-mockfinal`,
      object: "chat.completion.chunk",
      model: "gpt-4o-mini",
      choices: [{ delta: {}, index: 0, finish_reason: "stop" }],
      usage: { prompt_tokens: 10, completion_tokens: 8, total_tokens: 18 },
    })}\n\n`
  );
  chunks.push("data: [DONE]\n\n");
  return chunks;
}

const server = createServer((req, res) => {
  const url = req.url ?? "/";
  const auth = req.headers["authorization"] ?? "";
  console.log(`[mock-aigw] ${req.method} ${url}  auth=${auth.slice(0, 30)}...`);

  // 验证 Authorization 格式
  if (!auth.startsWith("Bearer ")) {
    res.writeHead(401, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ error: { message: "Unauthorized", type: "auth_error" } }));
    return;
  }

  // GET /v1/models（凭证验证探测用）
  if (req.method === "GET" && url === "/v1/models") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ object: "list", data: [{ id: "gpt-4o-mini", object: "model" }] }));
    return;
  }

  // POST /v1/chat/completions
  if (req.method === "POST" && url === "/v1/chat/completions") {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      let parsed = {};
      try { parsed = JSON.parse(body); } catch {}

      const isStream = parsed.stream === true;
      const replyContent = "Hello from mock AIGW!";

      if (isStream) {
        res.writeHead(200, {
          "Content-Type": "text/event-stream",
          "Cache-Control": "no-cache",
          Connection: "keep-alive",
          "X-Aigw-Meta": req.headers["x-aigw-meta"] ?? "",
        });
        const chunks = sseChunks(replyContent);
        let i = 0;
        const send = () => {
          if (i < chunks.length) {
            res.write(chunks[i++]);
            setTimeout(send, 30);
          } else {
            res.end();
          }
        };
        send();
      } else {
        const response = {
          id: "chatcmpl-mock123",
          object: "chat.completion",
          model: parsed.model ?? "gpt-4o-mini",
          choices: [{ message: { role: "assistant", content: replyContent }, finish_reason: "stop", index: 0 }],
          usage: { prompt_tokens: 10, completion_tokens: 8, total_tokens: 18 },
        };
        res.writeHead(200, {
          "Content-Type": "application/json",
          "X-Aigw-Meta": req.headers["x-aigw-meta"] ?? "",
        });
        res.end(JSON.stringify(response));
      }
    });
    return;
  }

  res.writeHead(404, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ error: { message: "Not found" } }));
});

server.listen(PORT, () => {
  console.log(`[mock-aigw] listening on http://localhost:${PORT}`);
  console.log(`[mock-aigw] endpoints: GET /v1/models  POST /v1/chat/completions`);
});
