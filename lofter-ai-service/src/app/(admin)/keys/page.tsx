import { db } from "@/lib/db";
import { getAuthUser } from "@/auth";
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
import { CreateKeyDialog } from "./create-key-dialog";
import { KeyActions } from "./key-actions";
import { SearchInput } from "@/components/admin/search-input";
import { UrlSelect } from "@/components/admin/url-select";
import { Pagination } from "@/components/admin/pagination";
import type { ApiKeyType } from "@/generated/prisma";

const PAGE_SIZE = 20;

export default async function KeysPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const q = typeof params.q === "string" ? params.q.trim() : "";
  const typeFilter = typeof params.type === "string" ? params.type : "";
  const appFilter = typeof params.app === "string" ? params.app : "";
  const page = Math.max(1, Number(params.page) || 1);

  const where = {
    ...(q && {
      OR: [
        { name: { contains: q } },
        { app: { appCode: { contains: q } } },
      ],
    }),
    ...(typeFilter && { type: typeFilter as ApiKeyType }),
    ...(appFilter && { app: { appCode: appFilter } }),
  };

  const [user, total, keys, apps] = await Promise.all([
    getAuthUser(),
    db.apiKey.count({ where }),
    db.apiKey.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * PAGE_SIZE,
      take: PAGE_SIZE,
      select: {
        id: true,
        apiKey: true,
        type: true,
        name: true,
        description: true,
        status: true,
        createdBy: true,
        createdAt: true,
        app: { select: { appCode: true, name: true } },
      },
    }),
    db.app.findMany({
      where: { status: 1 },
      orderBy: { appCode: "asc" },
      select: { appCode: true, name: true },
    }),
  ]);

  const role = (user?.role ?? "guest") as UserRole;
  const canManage = hasPermission(role, "keys:manage");
  const isAdmin = role === "admin";

  const typeOptions = [
    { value: "personal", label: "个人 sk-" },
    { value: "service", label: "服务 svc-" },
  ];
  const appOptions = apps.map((a) => ({ value: a.appCode, label: `${a.name} (${a.appCode})` }));

  return (
    <div className="space-y-4">
      {/* 页头 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">API Keys</h1>
          <p className="text-sm text-muted-foreground mt-1">
            管理 sk-（个人）和 svc-（服务）API Key
          </p>
        </div>
        {canManage && <CreateKeyDialog apps={apps} isAdmin={isAdmin} />}
      </div>

      {/* 搜索与过滤栏 */}
      <div className="flex flex-wrap items-center gap-2">
        <SearchInput placeholder="搜索名称或 App Code…" />
        <UrlSelect
          param="type"
          options={typeOptions}
          placeholder="全部类型"
          className="w-28 h-9"
        />
        <UrlSelect
          param="app"
          options={appOptions}
          placeholder="全部 App"
          className="w-44 h-9"
        />
      </div>

      {/* 表格 */}
      {keys.length === 0 ? (
        <div className="py-16 text-center text-sm text-muted-foreground">
          {q || typeFilter || appFilter
            ? "没有找到匹配的 Key"
            : canManage
              ? "暂无 Key，点击右上角「生成 Key」开始"
              : "暂无 Key"}
        </div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Key（脱敏）</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>App</TableHead>
              <TableHead>名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              {canManage && <TableHead className="text-right">操作</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {keys.map((key) => (
              <TableRow key={Number(key.id)} className={key.status === 0 ? "opacity-50" : ""}>
                <TableCell className="font-mono text-xs">
                  {key.apiKey.slice(0, 12)}****
                </TableCell>
                <TableCell>
                  <Badge variant={key.type === "personal" ? "secondary" : "outline"}>
                    {key.type === "personal" ? "个人 sk-" : "服务 svc-"}
                  </Badge>
                </TableCell>
                <TableCell className="font-mono text-xs">{key.app.appCode}</TableCell>
                <TableCell className="text-sm">{key.name}</TableCell>
                <TableCell className="text-sm text-muted-foreground max-w-[200px] truncate">
                  {key.description ?? "—"}
                </TableCell>
                <TableCell>
                  {key.status === 1 ? (
                    <Badge>活跃</Badge>
                  ) : (
                    <Badge variant="destructive">已吊销</Badge>
                  )}
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {new Date(key.createdAt).toLocaleDateString("zh-CN")}
                </TableCell>
                {canManage && (
                  <TableCell className="text-right">
                    <KeyActions keyId={Number(key.id)} isActive={key.status === 1} />
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Pagination total={total} pageSize={PAGE_SIZE} currentPage={page} />
    </div>
  );
}
