import { db } from "@/lib/db";
import { getAuthUser } from "@/auth";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ROLE_LABELS, ROLE_BADGE_VARIANT, ALL_ROLES, type UserRole } from "@/lib/permissions";
import { UserRoleSelect, UserStatusToggle } from "./user-actions";
import { AddUserDialog } from "./add-user-dialog";
import { SearchInput } from "@/components/admin/search-input";
import { UrlSelect } from "@/components/admin/url-select";
import { Pagination } from "@/components/admin/pagination";

const PAGE_SIZE = 20;

export default async function UsersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const q = typeof params.q === "string" ? params.q.trim() : "";
  const roleFilter = typeof params.role === "string" ? params.role : "";
  const page = Math.max(1, Number(params.page) || 1);

  const where = {
    ...(q && {
      OR: [
        { email: { contains: q } },
        { name: { contains: q } },
      ],
    }),
    ...(roleFilter && { role: roleFilter as never }),
  };

  const [currentUser, total, users] = await Promise.all([
    getAuthUser(),
    db.user.count({ where }),
    db.user.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * PAGE_SIZE,
      take: PAGE_SIZE,
      select: {
        id: true,
        email: true,
        name: true,
        role: true,
        status: true,
        createdAt: true,
        _count: { select: { apiKeys: true } },
      },
    }),
  ]);

  const selfEmail = currentUser?.email;
  const adminCount = await db.user.count({ where: { role: "admin" } });

  const roleOptions = ALL_ROLES.map((r) => ({ value: r, label: ROLE_LABELS[r] }));

  return (
    <div className="space-y-4">
      {/* 页头 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">用户管理</h1>
        </div>
        <div className="flex items-center gap-3">
          <div className="text-right text-sm text-muted-foreground">
            <div>{total} 位用户 · {adminCount} 位管理员</div>
          </div>
          <AddUserDialog />
        </div>
      </div>

      {/* 角色说明 */}
      <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-muted-foreground p-3 bg-muted/50 rounded-lg">
        <span className="font-medium text-foreground">角色权限：</span>
        <span><Badge className="mr-1 h-5 text-xs">管理员</Badge>完整权限（含用户管理、新增 App）</span>
        <span><Badge variant="secondary" className="mr-1 h-5 text-xs">开发</Badge>管理 Keys + 查看 Apps</span>
        <span><Badge variant="outline" className="mr-1 h-5 text-xs">运营</Badge>管理 Keys + 查看 Apps</span>
        <span><Badge variant="outline" className="mr-1 h-5 text-xs">访客</Badge>仅统计</span>
      </div>

      {/* 搜索与过滤栏 */}
      <div className="flex flex-wrap items-center gap-2">
        <SearchInput placeholder="搜索邮箱或姓名…" />
        <UrlSelect
          param="role"
          options={roleOptions}
          placeholder="全部角色"
          className="w-28 h-9"
        />
      </div>

      {/* 表格 */}
      {users.length === 0 ? (
        <div className="py-16 text-center text-sm text-muted-foreground">
          {q || roleFilter ? "没有找到匹配的用户" : "暂无用户，用户通过 OpenID 登录后自动注册"}
        </div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>邮箱</TableHead>
              <TableHead>姓名</TableHead>
              <TableHead>角色</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>API Keys</TableHead>
              <TableHead>注册时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.map((user) => (
              <TableRow
                key={Number(user.id)}
                className={user.status === 0 ? "opacity-50" : ""}
              >
                <TableCell className="font-mono text-sm">
                  <span>{user.email}</span>
                  {user.email === selfEmail && (
                    <span className="ml-1.5 text-xs text-muted-foreground">（我）</span>
                  )}
                </TableCell>
                <TableCell className="text-sm">
                  {user.name ?? <span className="text-muted-foreground">—</span>}
                </TableCell>
                <TableCell>
                  <Badge variant={ROLE_BADGE_VARIANT[user.role as UserRole]}>
                    {ROLE_LABELS[user.role as UserRole] ?? user.role}
                  </Badge>
                </TableCell>
                <TableCell>
                  {user.status === 1 ? (
                    <Badge variant="outline">正常</Badge>
                  ) : (
                    <Badge variant="destructive">已禁用</Badge>
                  )}
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {user._count.apiKeys}
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {new Date(user.createdAt).toLocaleDateString("zh-CN")}
                </TableCell>
                <TableCell>
                  <div className="flex items-center justify-end gap-2">
                    <UserRoleSelect
                      userId={Number(user.id)}
                      currentRole={user.role as UserRole}
                      userEmail={user.email}
                      selfEmail={selfEmail}
                    />
                    <UserStatusToggle
                      userId={Number(user.id)}
                      currentStatus={user.status}
                      userEmail={user.email}
                      selfEmail={selfEmail}
                    />
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Pagination total={total} pageSize={PAGE_SIZE} currentPage={page} />
    </div>
  );
}
