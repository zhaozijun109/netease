import { describe, it, expect, beforeAll, afterAll } from "vitest";

// 集成测试需要数据库连接，无 DATABASE_URL 时跳过
const hasDb = !!process.env.DATABASE_URL;
const describeIfDb = hasDb ? describe : describe.skip;

/**
 * 集成测试：验证 Prisma 对 requestLog 的查询逻辑
 * 测试前插入种子数据，测试后清理，不依赖 HTTP handler
 * 无 DATABASE_URL 环境变量时自动跳过
 */
describeIfDb("GET /api/admin/stats/requests", () => {
  // 延迟 import，避免在无数据库时 Prisma 初始化报错
  const getDb = async () => (await import("@/lib/db")).db;
  // 用时间戳隔离测试数据，避免与其他数据混淆
  const testAppCode = `test-app-${Date.now()}`;

  beforeAll(async () => {
    const db = await getDb();
    await db.requestLog.createMany({
      data: [
        {
          appCode: testAppCode,
          userCode: "user-a",
          model: "gpt-4",
          keyType: "personal",
          statusCode: 200,
          latencyMs: 100,
          path: "/v1/chat/completions",
          meta: {
            prompt_tokens: 10,
            completion_tokens: 5,
            total_tokens: 15,
            prompt_summary: "测试提问",
            completion_summary: "测试回复",
          },
        },
        {
          appCode: testAppCode,
          userCode: "user-b",
          model: "claude-sonnet-4-20250514",
          keyType: "service",
          statusCode: 400,
          latencyMs: 50,
          path: "/v1/chat/completions",
          meta: { error_message: "Model not found" },
        },
      ],
    });
  });

  afterAll(async () => {
    const db = await getDb();
    await db.requestLog.deleteMany({ where: { appCode: testAppCode } });
  });

  it("按 app_code 过滤返回正确数据", async () => {
    const db = await getDb();
    const count = await db.requestLog.count({ where: { appCode: testAppCode } });
    expect(count).toBe(2);
  });

  it("按 model 过滤", async () => {
    const db = await getDb();
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, model: "gpt-4" },
    });
    expect(count).toBe(1);
  });

  it("按 status_type=error 过滤（statusCode >= 400）", async () => {
    const db = await getDb();
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, statusCode: { gte: 400 } },
    });
    expect(count).toBe(1);
  });

  it("按 key_type=service 过滤", async () => {
    const db = await getDb();
    const count = await db.requestLog.count({
      where: { appCode: testAppCode, keyType: "service" },
    });
    expect(count).toBe(1);
  });

  it("meta 中包含摘要字段", async () => {
    const db = await getDb();
    const row = await db.requestLog.findFirst({
      where: { appCode: testAppCode, userCode: "user-a" },
    });
    const meta = row?.meta as Record<string, unknown>;
    expect(meta.prompt_summary).toBe("测试提问");
    expect(meta.completion_summary).toBe("测试回复");
  });
});
