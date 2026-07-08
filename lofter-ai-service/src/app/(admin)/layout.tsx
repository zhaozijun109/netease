import { redirect } from "next/navigation";
import { getAuthUser, signOut } from "@/auth";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Toaster } from "@/components/ui/sonner";
import { hasPermission, ROLE_LABELS, ROLE_BADGE_VARIANT, type UserRole } from "@/lib/permissions";
import { ROUTES } from "@/lib/routes";

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = await getAuthUser();

  if (!user) {
    redirect("/login");
  }

  const role = (user.role ?? "guest") as UserRole;

  return (
    <div className="min-h-screen bg-background">
      {/* 顶部导航 */}
      <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <div className="flex items-center gap-6">
            <span className="font-semibold text-sm">AI 基础服务</span>
            <nav className="flex items-center gap-1">
              {/* 统计：所有角色可见 */}
              <Button variant="ghost" size="sm" render={<Link href={ROUTES.stats} />}>
                统计
              </Button>
              {/* Keys：admin / developer */}
              {hasPermission(role, "keys:view") && (
                <Button variant="ghost" size="sm" render={<Link href={ROUTES.keys} />}>
                  Keys
                </Button>
              )}
              {/* Apps：admin / developer */}
              {hasPermission(role, "apps:view") && (
                <Button variant="ghost" size="sm" render={<Link href={ROUTES.apps} />}>
                  Apps
                </Button>
              )}
              {/* 用户管理：admin only */}
              {hasPermission(role, "users:manage") && (
                <Button variant="ghost" size="sm" render={<Link href={ROUTES.users} />}>
                  用户
                </Button>
              )}
              {/* AIGW API 文档：所有角色可见 */}
              <Button variant="ghost" size="sm" render={<Link href={ROUTES.aigwApis} />}>
                AIGW API
              </Button>
              {/* 接入申请指南：所有角色可见 */}
              <Button variant="ghost" size="sm" render={<Link href={ROUTES.onboarding} />}>
                接入指南
              </Button>
              {/* 服务管理：admin */}
              {hasPermission(role, "services:manage") && (
                <Button variant="ghost" size="sm" render={<Link href={ROUTES.services} />}>
                  服务管理
                </Button>
              )}
              {/* Skill Token：所有登录用户可见 */}
              <Button variant="ghost" size="sm" render={<Link href={ROUTES.skillTokens} />}>
                Skill Token
              </Button>
              {/* AIGW 产品手册：外链 */}
              <Button variant="ghost" size="sm" render={<a href="https://aigw.doc.nie.netease.com/" target="_blank" rel="noreferrer" />}>
                AIGW 文档 ↗
              </Button>
            </nav>
          </div>
          <div className="flex items-center gap-3">
            <Badge variant={ROLE_BADGE_VARIANT[role]} className="text-xs">
              {ROLE_LABELS[role]}
            </Badge>
            <span className="text-sm text-muted-foreground">
              {user.name ?? user.email ?? "未知用户"}
            </span>
            <form
              action={async () => {
                "use server";
                await signOut({ redirectTo: "/login" });
              }}
            >
              <Button variant="ghost" size="sm" type="submit">
                退出
              </Button>
            </form>
          </div>
        </div>
      </header>

      <Separator />

      <main className="container mx-auto px-4 py-6">{children}</main>
      <Toaster />
    </div>
  );
}
