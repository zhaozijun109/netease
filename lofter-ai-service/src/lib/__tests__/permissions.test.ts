import { describe, it, expect } from "vitest";
import { hasPermission } from "@/lib/permissions";

describe("stats:detail 权限", () => {
  it("admin 拥有 stats:detail", () => {
    expect(hasPermission("admin", "stats:detail")).toBe(true);
  });
  it("developer 没有 stats:detail", () => {
    expect(hasPermission("developer", "stats:detail")).toBe(false);
  });
  it("guest 没有 stats:detail", () => {
    expect(hasPermission("guest", "stats:detail")).toBe(false);
  });
  it("所有角色都有 stats:view", () => {
    expect(hasPermission("admin", "stats:view")).toBe(true);
    expect(hasPermission("developer", "stats:view")).toBe(true);
    expect(hasPermission("guest", "stats:view")).toBe(true);
  });
});
