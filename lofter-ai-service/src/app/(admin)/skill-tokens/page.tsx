import { getAuthUser } from "@/auth";
import { db } from "@/lib/db";
import { hasPermission, type UserRole } from "@/lib/permissions";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ApplyTokenDialog } from "./apply-token-dialog";
import { RevokeTokenButton } from "./revoke-token-button";

export default async function SkillTokensPage() {
  const user = await getAuthUser();
  const role = (user?.role ?? "guest") as UserRole;
  const isAdmin = hasPermission(role, "skill-tokens:manage");

  const tokens = isAdmin
    ? await db.skillToken.findMany({
        orderBy: { createdAt: "desc" },
        include: { user: { select: { email: true, name: true } } },
      })
    : user?.email
      ? await db.skillToken.findMany({
          where: { user: { email: user.email } },
          orderBy: { createdAt: "desc" },
        })
      : [];

  // 普通用户：是否已有有效 Token（用于申请按钮文案判断）
  const hasActiveToken = !isAdmin && tokens.some((t) => t.status === 1);

  // 管理员自己的 Token（用于管理员视图底部申请区域）
  const adminOwnTokens = isAdmin && user?.email
    ? await db.skillToken.findMany({
        where: { user: { email: user.email } },
        orderBy: { createdAt: "desc" },
        take: 1,
      })
    : [];
  const adminHasActiveToken = adminOwnTokens.some((t) => t.status === 1);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Skill Token</h1>
          <p className="text-sm text-muted-foreground mt-1">
            {isAdmin ? "管理所有用户的 Skill 访问凭证" : "管理你的 Skill 访问凭证"}
          </p>
        </div>
        {/* 普通用户：标题右侧申请按钮 */}
        {!isAdmin && (
          <ApplyTokenDialog hasToken={hasActiveToken} />
        )}
      </div>

      {!isAdmin && (
        <div className="rounded-lg border bg-muted/40 px-4 py-3 text-sm space-y-2">
          <p className="font-medium">关于 Skill Token</p>
          <p className="text-muted-foreground">
            Token 用于访问 Skill 服务，申请后仅展示一次，请妥善保存。
          </p>
          <p className="text-muted-foreground text-xs">
            配置到 Skill CLI：
            <code className="font-mono">
              python lofter_data.py config api_key &quot;skt-xxx&quot;
            </code>
          </p>
        </div>
      )}

      <div className="rounded-md border overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50">
              {isAdmin && <TableHead>用户</TableHead>}
              <TableHead>Token（脱敏）</TableHead>
              <TableHead>权限</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tokens.map((t) => (
              <TableRow key={t.id.toString()}>
                {isAdmin && "user" in t && (
                  <TableCell className="text-xs text-muted-foreground">
                    {(t as typeof t & { user: { email: string } }).user.email}
                  </TableCell>
                )}
                <TableCell className="font-mono text-xs">
                  {t.token.slice(0, 8)}****
                </TableCell>
                <TableCell className="text-xs">
                  {JSON.stringify(t.permissions)}
                </TableCell>
                <TableCell>
                  <Badge variant={t.status === 1 ? "default" : "secondary"}>
                    {t.status === 1 ? "有效" : "已吊销"}
                  </Badge>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {t.createdAt.toLocaleDateString("zh-CN")}
                </TableCell>
                <TableCell>
                  {t.status === 1 && (
                    <RevokeTokenButton
                      tokenId={Number(t.id)}
                      isAdmin={isAdmin}
                    />
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {tokens.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">暂无 Token</p>
      )}

      {/* 管理员视图底部：管理员申请自己的 Token */}
      {isAdmin && (
        <div className="rounded-lg border bg-muted/40 px-4 py-4 space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">申请我自己的 Skill Token</p>
              <p className="text-xs text-muted-foreground mt-0.5">
                管理员也可以申请自己的 Token 用于本地调试和开发。
              </p>
            </div>
            <ApplyTokenDialog hasToken={adminHasActiveToken} />
          </div>
        </div>
      )}
    </div>
  );
}
