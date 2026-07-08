import { test, expect } from "@playwright/test";

test.describe("请求日志摘要写入完整链路", () => {
  const testPrompt = `e2e-test-${Date.now()}`;

  test("发送请求后能在日志中看到摘要", async ({ request, page }) => {
    const apiRes = await request.post("/v1/chat/completions", {
      headers: {
        Authorization: "Bearer <test-api-key>",
        "Content-Type": "application/json",
      },
      data: {
        model: "gpt-4",
        messages: [{ role: "user", content: testPrompt }],
        temperature: 0.1,
        max_tokens: 50,
      },
    });

    expect([200, 502]).toContain(apiRes.status());

    // 等待异步日志写入
    await new Promise((resolve) => setTimeout(resolve, 2000));

    await page.goto("/stats");
    await expect(page.getByText("请求日志")).toBeVisible();
    await page.waitForSelector("table tbody tr", { timeout: 10_000 });

    const statsRes = await request.get("/api/admin/stats/requests?page_size=5");
    const statsJson = await statsRes.json();

    const latestLog = statsJson.data?.[0];
    expect(latestLog).toBeDefined();
    expect(latestLog.meta).toBeDefined();
    const meta = latestLog.meta;
    const hasSummary = meta.prompt_summary != null || meta.error_message != null;
    expect(hasSummary).toBe(true);
  });
});
