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
import { AddAppDialog } from "./add-app-dialog";
import { AppActions } from "./app-actions";

export default async function AppsPage() {
  const [user, apps] = await Promise.all([
    getAuthUser(),
    db.app.findMany({
      orderBy: { createdAt: "desc" },
      select: {
        id: true,
        appCode: true,
        appId: true,
        name: true,
        description: true,
        requireUserCode: true,
        status: true,
        createdAt: true,
        _count: { select: { apiKeys: { where: { status: 1 } } } },
      },
    }),
  ]);
  const role = (user?.role ?? "guest") as UserRole;
  const canManage = hasPermission(role, "apps:manage");

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">AIGW Apps</h1>
          <p className="text-sm text-muted-foreground mt-1">
            管理 AIGW 应用凭证，每个 App 对应一组 AIGW app_id / app_key
          </p>
        </div>
        {canManage && <AddAppDialog />}
      </div>

      {apps.length === 0 ? (
        <p className="text-sm text-muted-foreground py-8 text-center">
          {canManage ? "暂无 App，点击右上角「新增 App」开始" : "暂无 App"}
        </p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>App Code</TableHead>
              <TableHead>名称</TableHead>
              <TableHead>App ID</TableHead>
              <TableHead>强制 User Code</TableHead>
              <TableHead>活跃 Keys</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              {canManage && <TableHead className="text-right">操作</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {apps.map((app) => (
              <TableRow key={Number(app.id)} className={app.status === 0 ? "opacity-50" : ""}>
                <TableCell className="font-mono text-sm">{app.appCode}</TableCell>
                <TableCell>
                  <div>{app.name}</div>
                  {app.description && (
                    <div className="text-xs text-muted-foreground">{app.description}</div>
                  )}
                </TableCell>
                <TableCell className="font-mono text-xs text-muted-foreground">
                  {app.appId}
                </TableCell>
                <TableCell>
                  {app.requireUserCode ? (
                    <Badge variant="secondary">必须</Badge>
                  ) : (
                    <Badge variant="outline">可选</Badge>
                  )}
                </TableCell>
                <TableCell>{app._count.apiKeys}</TableCell>
                <TableCell>
                  {app.status === 1 ? (
                    <Badge>启用</Badge>
                  ) : (
                    <Badge variant="destructive">禁用</Badge>
                  )}
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {new Date(app.createdAt).toLocaleDateString("zh-CN")}
                </TableCell>
                {canManage && (
                  <TableCell className="text-right">
                    <AppActions appCode={app.appCode} isActive={app.status === 1} requireUserCode={app.requireUserCode} />
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
