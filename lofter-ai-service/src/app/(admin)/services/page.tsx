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
import { redirect } from "next/navigation";
import { AddServiceDialog } from "./add-service-dialog";
import { ServiceActions } from "./service-actions";

export default async function ServicesPage() {
  const user = await getAuthUser();
  const role = (user?.role ?? "guest") as UserRole;
  if (!hasPermission(role, "services:manage")) redirect("/403");

  const canManage = hasPermission(role, "services:manage");

  const services = await db.service.findMany({
    orderBy: { createdAt: "desc" },
  });

  // credentialConfig 脱敏：key/password/token 字段遮盖
  const maskedServices = services.map((s) => ({
    ...s,
    credentialConfig: s.credentialConfig
      ? Object.fromEntries(
          Object.entries(s.credentialConfig as Record<string, unknown>).map(([k, v]) =>
            ["key", "password", "token"].includes(k) ? [k, "****"] : [k, v]
          )
        )
      : null,
  }));

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">服务管理</h1>
          <p className="text-sm text-muted-foreground mt-1">
            注册和管理 Skill 可调用的后端服务端点
          </p>
        </div>
        <AddServiceDialog canManage={canManage} />
      </div>
      <div className="rounded-md border overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50">
              <TableHead>Code</TableHead>
              <TableHead>名称</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>凭据</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {maskedServices.map((s) => (
              <TableRow key={s.id.toString()}>
                <TableCell className="font-mono text-xs">{s.code}</TableCell>
                <TableCell>{s.name}</TableCell>
                <TableCell>
                  <Badge variant="outline">{s.type}</Badge>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {s.credentialType ?? "未配置"}
                </TableCell>
                <TableCell>
                  <Badge variant={s.status === 1 ? "default" : "secondary"}>
                    {s.status === 1 ? "启用" : "停用"}
                  </Badge>
                </TableCell>
                <TableCell>
                  <ServiceActions
                    code={s.code}
                    name={s.name}
                    type={s.type}
                    config={s.config}
                    status={s.status}
                    credentialType={s.credentialType}
                    credentialConfig={s.credentialConfig}
                    canManage={canManage}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      {services.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">
          暂无注册服务，点击「新增服务」开始添加
        </p>
      )}
    </div>
  );
}
