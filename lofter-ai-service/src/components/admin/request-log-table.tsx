"use client";

import { useState, useEffect, useCallback, Fragment } from "react";
import { ChevronDown, ChevronRight, ChevronLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

// 请求日志元数据
type RequestLogMeta = {
  prompt_tokens?: number;
  completion_tokens?: number;
  total_tokens?: number;
  prompt_summary?: string | null;
  completion_summary?: string | null;
  request_params?: Record<string, unknown> | null;
  error_message?: string | null;
};

// 请求日志行
type RequestLogRow = {
  id: string;
  appCode: string;
  keyType: string | null;
  userCode: string | null;
  model: string | null;
  path: string | null;
  statusCode: number | null;
  latencyMs: number | null;
  meta: RequestLogMeta | null;
  createdAt: string;
};

// 过滤条件
type Filters = {
  app_code: string;
  user_code: string;
  model: string;
  key_type: string;
  status_type: string;
};

const PAGE_SIZE = 20;

const DEFAULT_FILTERS: Filters = {
  app_code: "",
  user_code: "",
  model: "",
  key_type: "",
  status_type: "",
};

export function RequestLogTable({ lockedUserCode }: { lockedUserCode?: string }) {
  const [data, setData] = useState<RequestLogRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [filters, setFilters] = useState<Filters>({
    ...DEFAULT_FILTERS,
    ...(lockedUserCode ? { user_code: lockedUserCode } : {}),
  });

  // 拉取数据
  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set("page", String(page));
      params.set("page_size", String(PAGE_SIZE));
      if (filters.app_code) params.set("app_code", filters.app_code);
      if (filters.user_code) params.set("user_code", filters.user_code);
      if (filters.model) params.set("model", filters.model);
      if (filters.key_type) params.set("key_type", filters.key_type);
      if (filters.status_type) params.set("status_type", filters.status_type);

      const res = await fetch(`/api/admin/stats/requests?${params.toString()}`);
      if (res.ok) {
        const json = await res.json();
        setData(json.data ?? []);
        setTotal(json.total ?? 0);
      }
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // 更新过滤条件并重置到第一页
  function updateFilter(key: keyof Filters, value: string) {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(1);
  }

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  return (
    <div className="space-y-3">
      {/* 过滤栏 */}
      <div className="flex flex-wrap gap-2">
        <Input
          placeholder="App Code"
          className="w-36 h-8 text-sm"
          value={filters.app_code}
          onChange={(e) => updateFilter("app_code", e.target.value)}
        />
        {!lockedUserCode && (
          <Input
            placeholder="User Code"
            className="w-36 h-8 text-sm"
            value={filters.user_code}
            onChange={(e) => updateFilter("user_code", e.target.value)}
          />
        )}
        <Input
          placeholder="Model"
          className="w-40 h-8 text-sm"
          value={filters.model}
          onChange={(e) => updateFilter("model", e.target.value)}
        />
        <Select
          value={filters.key_type || "all"}
          onValueChange={(v) => updateFilter("key_type", v === "all" ? "" : v)}
        >
          <SelectTrigger className="w-32 h-8 text-sm">
            <SelectValue placeholder="Key 类型" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部 Key</SelectItem>
            <SelectItem value="personal">Personal</SelectItem>
            <SelectItem value="service">Service</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={filters.status_type || "all"}
          onValueChange={(v) => updateFilter("status_type", v === "all" ? "" : v)}
        >
          <SelectTrigger className="w-28 h-8 text-sm">
            <SelectValue placeholder="状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="success">成功</SelectItem>
            <SelectItem value="error">失败</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* 数据表格 */}
      <div className="border rounded-md overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-8" />
              <TableHead>时间</TableHead>
              <TableHead>App</TableHead>
              <TableHead>User</TableHead>
              <TableHead>Model</TableHead>
              <TableHead>状态</TableHead>
              <TableHead className="text-right">延迟</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground text-sm">
                  加载中…
                </TableCell>
              </TableRow>
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground text-sm">
                  暂无数据
                </TableCell>
              </TableRow>
            ) : (
              data.map((row) => (
                <Fragment key={row.id}>
                  {/* 主行 */}
                  <TableRow
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => setExpandedId(expandedId === row.id ? null : row.id)}
                  >
                    <TableCell className="w-8 px-2">
                      {expandedId === row.id ? (
                        <ChevronDown className="h-4 w-4 text-muted-foreground" />
                      ) : (
                        <ChevronRight className="h-4 w-4 text-muted-foreground" />
                      )}
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground whitespace-nowrap">
                      {new Date(row.createdAt).toLocaleString("zh-CN", {
                        month: "2-digit",
                        day: "2-digit",
                        hour: "2-digit",
                        minute: "2-digit",
                        second: "2-digit",
                      })}
                    </TableCell>
                    <TableCell className="font-mono text-sm">{row.appCode}</TableCell>
                    <TableCell className="font-mono text-sm">{row.userCode ?? "—"}</TableCell>
                    <TableCell className="text-sm">{row.model ?? "—"}</TableCell>
                    <TableCell>
                      <span
                        className={
                          row.statusCode && row.statusCode < 400
                            ? "text-green-600 text-sm"
                            : "text-red-500 text-sm"
                        }
                      >
                        {row.statusCode ?? "—"}
                      </span>
                    </TableCell>
                    <TableCell className="text-right text-sm">
                      {row.latencyMs != null ? `${row.latencyMs}ms` : "—"}
                    </TableCell>
                  </TableRow>

                  {/* 展开详情行 */}
                  {expandedId === row.id && (
                    <TableRow key={`${row.id}-detail`} className="bg-muted/30">
                      <TableCell colSpan={7} className="px-6 py-3">
                        <div className="space-y-2 text-sm">
                          {row.meta?.prompt_summary && (
                            <div>
                              <span className="font-medium text-muted-foreground">Prompt: </span>
                              <span className="text-foreground">{row.meta.prompt_summary}</span>
                            </div>
                          )}
                          {row.meta?.completion_summary && (
                            <div>
                              <span className="font-medium text-muted-foreground">Completion: </span>
                              <span className="text-foreground">{row.meta.completion_summary}</span>
                            </div>
                          )}
                          {row.meta?.error_message && (
                            <div>
                              <span className="font-medium text-red-500">Error: </span>
                              <span className="text-red-600">{row.meta.error_message}</span>
                            </div>
                          )}
                          <div className="flex gap-4 text-muted-foreground">
                            {row.meta?.prompt_tokens != null && (
                              <span>Prompt tokens: {row.meta.prompt_tokens}</span>
                            )}
                            {row.meta?.completion_tokens != null && (
                              <span>Completion tokens: {row.meta.completion_tokens}</span>
                            )}
                            {row.meta?.total_tokens != null && (
                              <span>Total: {row.meta.total_tokens}</span>
                            )}
                          </div>
                          {row.meta?.request_params && (
                            <div>
                              <span className="font-medium text-muted-foreground">Request params: </span>
                              <code className="text-xs bg-muted rounded px-1 py-0.5 break-all">
                                {JSON.stringify(row.meta.request_params)}
                              </code>
                            </div>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </Fragment>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 分页 */}
      <div className="flex items-center justify-between pt-1">
        <p className="text-sm text-muted-foreground">共 {total} 条</p>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            className="h-8 w-8 p-0"
            disabled={page <= 1}
            onClick={() => setPage((p) => Math.max(1, p - 1))}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm text-muted-foreground min-w-16 text-center">
            {page} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            className="h-8 w-8 p-0"
            disabled={page >= totalPages}
            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
