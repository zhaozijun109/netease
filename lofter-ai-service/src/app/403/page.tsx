import Link from "next/link";
import { Button } from "@/components/ui/button";
import { getAuthUser, signOut } from "@/auth";

export default async function ForbiddenPage() {
  const user = await getAuthUser();

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-center space-y-4 max-w-md px-4">
        <div className="text-6xl font-bold text-muted-foreground">403</div>
        <h1 className="text-2xl font-semibold">无权访问</h1>
        <p className="text-muted-foreground text-sm">
          你的账号（{user?.email ?? "未知"}）没有管理员权限。
          <br />
          请联系管理员将你的账号提升为管理员后再试。
        </p>
        <div className="flex items-center justify-center gap-3 pt-2">
          <Button variant="outline" render={<Link href="/" />}>
            返回首页
          </Button>
          {user && (
            <form
              action={async () => {
                "use server";
                await signOut({ redirectTo: "/login" });
              }}
            >
              <Button variant="ghost" size="sm" type="submit">
                切换账号
              </Button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
