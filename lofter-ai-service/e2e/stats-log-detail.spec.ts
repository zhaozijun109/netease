import { test, expect } from "@playwright/test";

test.describe("请求日志明细", () => {
  // 注意：需要配置 admin 登录态。
  // 可通过 playwright.config.ts 的 storageState 或在 test.beforeEach 中登录。

  test("admin 可以看到请求日志区域", async ({ page }) => {
    await page.goto("/stats");
    await expect(page.getByText("使用统计")).toBeVisible();
    await expect(page.getByText("请求日志")).toBeVisible();
  });

  test("点击日志行展开详情", async ({ page }) => {
    await page.goto("/stats");
    await expect(page.getByText("请求日志")).toBeVisible();
    await page.waitForSelector("table tbody tr", { timeout: 10_000 });
    const firstRow = page.locator("table tbody tr").first();
    await firstRow.click();
    await expect(
      page.getByText(/Prompt:|Completion:|Error:|Prompt tokens:/)
    ).toBeVisible();
  });
});

test.describe("developer 权限限制", () => {
  test.skip("developer 看不到请求日志区域", async ({ page }) => {
    await page.goto("/stats");
    await expect(page.getByText("使用统计")).toBeVisible();
    await expect(page.getByText("请求日志")).not.toBeVisible();
  });
});
