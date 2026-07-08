import { db } from "@/lib/db";
import { getAuthUser } from "@/auth";
import { hasPermission, type UserRole } from "@/lib/permissions";
import { RequestLogTable } from "@/components/admin/request-log-table";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

function fmtTokens(n: number) {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`;
  return String(n);
}

export default async function StatsPage() {
  const since = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  const currentUser = await getAuthUser();
  const currentUserCode = currentUser?.email ?? null;
  const role = (currentUser?.role ?? "guest") as UserRole;
  const canViewDetail = hasPermission(role, "stats:detail");
  const isAdmin = hasPermission(role, "users:manage");

  const logs = await db.requestLog.findMany({
    where: {
      createdAt: { gte: since },
      // developer 只查自己的数据
      ...(!isAdmin && currentUserCode ? { userCode: currentUserCode } : {}),
    },
    select: {
      appCode: true,
      model: true,
      userCode: true,
      statusCode: true,
      meta: true,
    },
  });

  // 全局汇总
  let totalRequests = 0;
  let successCount = 0;
  let totalTokens = 0;

  // 按 App 聚合
  const byApp = new Map<string, { requests: number; tokens: number }>();
  // 按 Model 聚合
  const byModel = new Map<string, { requests: number; tokens: number }>();
  // 按 User 聚合
  const byUser = new Map<string, { requests: number; tokens: number }>();

  for (const log of logs) {
    totalRequests += 1;
    if (log.statusCode && log.statusCode < 400) successCount += 1;

    const meta = log.meta as Record<string, number> | null;
    const t = meta?.total_tokens ?? 0;
    totalTokens += t;

    // by app
    const appEntry = byApp.get(log.appCode) ?? { requests: 0, tokens: 0 };
    appEntry.requests += 1;
    appEntry.tokens += t;
    byApp.set(log.appCode, appEntry);

    // by model
    const modelKey = log.model ?? "unknown";
    const modelEntry = byModel.get(modelKey) ?? { requests: 0, tokens: 0 };
    modelEntry.requests += 1;
    modelEntry.tokens += t;
    byModel.set(modelKey, modelEntry);

    // by user
    const userKey = log.userCode ?? "(anonymous)";
    const userEntry = byUser.get(userKey) ?? { requests: 0, tokens: 0 };
    userEntry.requests += 1;
    userEntry.tokens += t;
    byUser.set(userKey, userEntry);
  }

  const successRate =
    totalRequests > 0
      ? ((successCount / totalRequests) * 100).toFixed(1)
      : "—";

  const byAppRows = Array.from(byApp.entries())
    .map(([appCode, v]) => ({ appCode, ...v }))
    .sort((a, b) => b.tokens - a.tokens);

  const byModelRows = Array.from(byModel.entries())
    .map(([model, v]) => ({ model, ...v }))
    .sort((a, b) => b.tokens - a.tokens);

  const byUserRows = Array.from(byUser.entries())
    .map(([userCode, v]) => ({ userCode, ...v }))
    .sort((a, b) => b.tokens - a.tokens);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">使用统计</h1>
        <p className="text-sm text-muted-foreground mt-1">最近 7 天</p>
      </div>

      {/* 概览卡片：仅 admin */}
      {isAdmin && (
        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                总请求数
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{totalRequests.toLocaleString()}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                成功率
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">
                {typeof successRate === "string" ? successRate : `${successRate}%`}
                {totalRequests > 0 && "%"}
              </p>
              {totalRequests > 0 && (
                <p className="text-xs text-muted-foreground mt-1">
                  ≈ {totalRequests - successCount} 次失败
                </p>
              )}
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                总 Tokens
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{fmtTokens(totalTokens)}</p>
            </CardContent>
          </Card>
        </div>
      )}

      {totalRequests === 0 ? (
        <p className="text-sm text-muted-foreground py-8 text-center">
          最近 7 天暂无请求记录
        </p>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {/* 按 App / Model / User 分组：仅 admin */}
          {isAdmin && (
            <>
              {/* 按 App 分组 */}
              <div>
                <h2 className="text-lg font-semibold mb-3">按 App 分组</h2>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>App Code</TableHead>
                      <TableHead className="text-right">请求数</TableHead>
                      <TableHead className="text-right">Tokens</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {byAppRows.map((row) => (
                      <TableRow key={row.appCode}>
                        <TableCell className="font-mono text-sm">{row.appCode}</TableCell>
                        <TableCell className="text-right">{row.requests.toLocaleString()}</TableCell>
                        <TableCell className="text-right">{fmtTokens(row.tokens)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* 按模型分组 */}
              <div>
                <h2 className="text-lg font-semibold mb-3">按模型分组</h2>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>模型</TableHead>
                      <TableHead className="text-right">请求数</TableHead>
                      <TableHead className="text-right">Tokens</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {byModelRows.map((row) => (
                      <TableRow key={row.model}>
                        <TableCell className="font-mono text-sm">{row.model}</TableCell>
                        <TableCell className="text-right">{row.requests.toLocaleString()}</TableCell>
                        <TableCell className="text-right">{fmtTokens(row.tokens)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* 按用户分组 */}
              <div className="md:col-span-2">
                <h2 className="text-lg font-semibold mb-3">按用户分组</h2>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>User Code</TableHead>
                      <TableHead className="text-right">请求数</TableHead>
                      <TableHead className="text-right">Tokens</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {byUserRows.map((row) => (
                      <TableRow key={row.userCode}>
                        <TableCell className="font-mono text-sm">
                          {row.userCode === "(anonymous)" ? "—" : row.userCode}
                        </TableCell>
                        <TableCell className="text-right">{row.requests.toLocaleString()}</TableCell>
                        <TableCell className="text-right">{fmtTokens(row.tokens)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </>
          )}

          {/* 我的用量 */}
          <div className="md:col-span-2">
            <h2 className="text-lg font-semibold mb-3">我的用量</h2>
            {currentUserCode ? (
              (() => {
                const myStats = byUser.get(currentUserCode);
                return myStats ? (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>User Code</TableHead>
                        <TableHead className="text-right">请求数</TableHead>
                        <TableHead className="text-right">Tokens</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      <TableRow>
                        <TableCell className="font-mono text-sm">{currentUserCode}</TableCell>
                        <TableCell className="text-right">{myStats.requests.toLocaleString()}</TableCell>
                        <TableCell className="text-right">{fmtTokens(myStats.tokens)}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                ) : (
                  <p className="text-sm text-muted-foreground">最近 7 天暂无个人请求记录</p>
                );
              })()
            ) : (
              <p className="text-sm text-muted-foreground">无法获取当前用户信息</p>
            )}
          </div>
        </div>
      )}
      {canViewDetail && (
        <div className="mt-6">
          <h2 className="text-lg font-semibold mb-3">请求日志</h2>
          <RequestLogTable lockedUserCode={isAdmin ? undefined : (currentUserCode ?? undefined)} />
        </div>
      )}
    </div>
  );
}
