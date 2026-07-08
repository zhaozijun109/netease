/**
 * 权限管理模块
 *
 * 角色说明：
 *   admin     - 管理员：完整权限，管理 Apps / Keys / Services / 用户 / 统计
 *   developer - 开发：可管理 Keys、查看 Apps 和个人统计明细，不能管理用户
 *   guest     - 访客：只能查看统计
 */

export type UserRole = "admin" | "developer" | "guest";

export type Permission =
  | "apps:view"    // 查看 App 列表
  | "apps:manage"  // 新增 / 启停 App（含敏感凭证）
  | "keys:view"    // 查看 API Keys 列表
  | "keys:manage"  // 创建 / 吊销 API Key
  | "stats:view"    // 查看使用统计
  | "stats:detail"  // 查看请求日志明细（含摘要）
  | "users:manage"  // 管理用户角色与状态
  | "services:manage"       // 管理注册服务（admin）
  | "skill-tokens:manage";  // 管理员查看/吊销所有 Skill Token

const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  admin:     ["apps:view", "apps:manage", "keys:view", "keys:manage", "stats:view", "stats:detail", "users:manage", "services:manage", "skill-tokens:manage"],
  developer: ["apps:view",               "keys:view", "keys:manage", "stats:view", "stats:detail"],
  guest:     [                                                        "stats:view"],
};

export function hasPermission(
  role: UserRole | string | undefined | null,
  permission: Permission
): boolean {
  if (!role) return false;
  return (ROLE_PERMISSIONS[role as UserRole] ?? []).includes(permission);
}

/** 中文标签 */
export const ROLE_LABELS: Record<UserRole, string> = {
  admin:     "管理员",
  developer: "开发",
  guest:     "访客",
};

/** 角色徽章样式（对应 shadcn Badge variant） */
export const ROLE_BADGE_VARIANT: Record<
  UserRole,
  "default" | "secondary" | "outline" | "destructive"
> = {
  admin:     "default",
  developer: "secondary",
  guest:     "outline",
};

export const ALL_ROLES: UserRole[] = ["admin", "developer", "guest"];
