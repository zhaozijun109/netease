"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { toast } from "sonner";

const SERVICE_CONFIG_PLACEHOLDERS: Record<string, string> = {
  http_proxy: `{"baseUrl":"https://...","timeout":30000,"maxTimeout":120000}`,
  db_query: `{"driver":"mysql","host":"...","port":9030,"database":"lofter","queryTimeout":60000,"maxTimeout":300000,"largeResultThreshold":1000}`,
  static_content: `{"content":"你的规则或文本内容...","contentType":"text/markdown"}`,
};

const CREDENTIAL_CONFIG_PLACEHOLDERS: Record<string, string> = {
  auth_key: `{"account":"_xxx","key":"xxx","authUrl":"http://int-auth.nie.netease.com","project":"space_xxx"}`,
  db_password: `{"username":"xxx","password":"xxx"}`,
  static_token: `{"token":"xxx","headerName":"X-Access-Token"}`,
};

export interface ServiceActionsProps {
  code: string;
  name: string;
  type: string;
  config: unknown;
  status: number;
  credentialType: string | null;
  credentialConfig: unknown;
  canManage: boolean;
}

// ---- 配置 Dialog（单一表单，无 Tabs）----
function ConfigDialog({ code, name, type, config, status, credentialType, credentialConfig, onDone }: {
  code: string;
  name: string;
  type: string;
  config: unknown;
  status: number;
  credentialType: string | null;
  credentialConfig: unknown;
  onDone: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    name,
    config: type === "static_content"
      ? ((config as Record<string, unknown>)?.content as string ?? "")
      : config ? JSON.stringify(config, null, 2) : "",
    enabled: status === 1,
    credType: credentialType ?? "auth_key",
    credConfig: credentialConfig ? JSON.stringify(credentialConfig, null, 2) : "",
  });
  const [loading, setLoading] = useState(false);

  function onOpenChange(val: boolean) {
    setOpen(val);
    if (val) {
      // 打开时重置为当前 props 值（脱敏后的 credentialConfig）
      setForm({
        name,
        config: type === "static_content"
          ? ((config as Record<string, unknown>)?.content as string ?? "")
          : config ? JSON.stringify(config, null, 2) : "",
        enabled: status === 1,
        credType: credentialType ?? "auth_key",
        credConfig: credentialConfig ? JSON.stringify(credentialConfig, null, 2) : "",
      });
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (type !== "static_content" && form.config.trim()) {
      try { JSON.parse(form.config); }
      catch { toast.error("服务 Config 格式不合法，请输入有效的 JSON"); return; }
    }
    if (form.credConfig.trim()) {
      try { JSON.parse(form.credConfig); }
      catch { toast.error("凭据 Config 格式不合法，请输入有效的 JSON"); return; }
    }

    setLoading(true);
    try {
      const body: Record<string, unknown> = {
        name: form.name,
        status: form.enabled ? 1 : 0,
      };
      if (form.config.trim()) {
        body.config = type === "static_content"
          ? { content: form.config, contentType: "text/markdown" }
          : JSON.parse(form.config);
      }
      if (form.credConfig.trim()) {
        body.credentialType = form.credType;
        body.credentialConfig = JSON.parse(form.credConfig);
      } else if (form.credType !== (credentialType ?? "auth_key")) {
        // 仅类型变了但没填 config，不更新凭据
      }

      const res = await fetch(`/api/admin/services/${code}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        const data = (await res.json()) as { error?: string };
        throw new Error(data.error ?? "更新失败");
      }
      toast.success("服务配置已保存");
      setOpen(false);
      onDone();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "更新失败");
    } finally { setLoading(false); }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogTrigger render={<Button variant="outline" size="sm" />}>配置</DialogTrigger>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>配置 · {code}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          {/* 名称 */}
          <div className="space-y-2">
            <Label htmlFor={`edit-name-${code}`}>名称</Label>
            <Input
              id={`edit-name-${code}`}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
          {/* 类型（只读） */}
          <div className="space-y-2">
            <Label>类型</Label>
            <p className="text-sm font-mono text-muted-foreground">{type}</p>
          </div>
          {/* 服务 Config */}
          {type === "static_content" ? (
          <div className="space-y-2">
            <Label htmlFor={`edit-config-${code}`}>内容（纯文本，直接粘贴 Prompt）</Label>
            <textarea
              id={`edit-config-${code}`}
              className="flex min-h-[200px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              placeholder="直接粘贴 Prompt 内容，无需 JSON 格式..."
              value={form.config}
              onChange={(e) => setForm({ ...form, config: e.target.value })}
              spellCheck={false}
            />
          </div>
          ) : (
          <div className="space-y-2">
            <Label htmlFor={`edit-config-${code}`}>服务 Config（JSON）</Label>
            <textarea
              id={`edit-config-${code}`}
              className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              placeholder={SERVICE_CONFIG_PLACEHOLDERS[type] ?? "{}"}
              value={form.config}
              onChange={(e) => setForm({ ...form, config: e.target.value })}
              spellCheck={false}
            />
          </div>
          )}
          {type !== "static_content" && (<>
          {/* 分隔线 */}
          <hr className="border-border" />
          {/* 凭据类型 */}
          <div className="space-y-2">
            <Label>凭据类型</Label>
            {credentialType && (
              <p className="text-xs text-amber-600 dark:text-amber-400">
                已有凭据（类型：{credentialType}），密钥字段已脱敏。修改时请重新填写完整 Config。
              </p>
            )}
            <Select
              value={form.credType}
              onValueChange={(v) => setForm({ ...form, credType: v, credConfig: "" })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="auth_key">auth_key（网易 Auth 动态 Token）</SelectItem>
                <SelectItem value="db_password">db_password（数据库账号密码）</SelectItem>
                <SelectItem value="static_token">static_token（固定 Token）</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {/* 凭据 Config */}
          <div className="space-y-2">
            <Label htmlFor={`cred-config-${code}`}>凭据 Config（JSON，留空则不修改）</Label>
            <textarea
              id={`cred-config-${code}`}
              className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              placeholder={CREDENTIAL_CONFIG_PLACEHOLDERS[form.credType] ?? "{}"}
              value={form.credConfig}
              onChange={(e) => setForm({ ...form, credConfig: e.target.value })}
              spellCheck={false}
            />
          </div>
          </>)}
          {/* 启用状态 */}
          <div className="flex items-center gap-3">
            <Switch
              id={`edit-status-${code}`}
              checked={form.enabled}
              onCheckedChange={(checked) => setForm({ ...form, enabled: checked })}
            />
            <Label htmlFor={`edit-status-${code}`}>{form.enabled ? "启用" : "停用"}</Label>
          </div>
          <div className="flex gap-2 pt-2 justify-end">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>取消</Button>
            <Button type="submit" disabled={loading}>{loading ? "保存中..." : "保存"}</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ---- 删除 AlertDialog ----
function DeleteServiceDialog({ code, name, onDone }: {
  code: string;
  name: string;
  onDone: () => void;
}) {
  const [loading, setLoading] = useState(false);

  async function handleDelete() {
    setLoading(true);
    try {
      const res = await fetch(`/api/admin/services/${code}`, { method: "DELETE" });
      if (!res.ok) {
        const data = (await res.json()) as { error?: string };
        throw new Error(data.error ?? "删除失败");
      }
      toast.success(`服务「${name}」已删除`);
      onDone();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "删除失败");
    } finally { setLoading(false); }
  }

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="destructive" size="sm" disabled={loading}>删除</Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除服务「{name}」？</AlertDialogTitle>
          <AlertDialogDescription>
            此操作不可恢复，删除后引用此服务的 Skill 将无法运行。
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            onClick={handleDelete}
          >
            确认删除
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

// ---- 主组件 ----
export function ServiceActions({ code, name, type, config, status, credentialType, credentialConfig, canManage }: ServiceActionsProps) {
  const router = useRouter();
  const [testing, setTesting] = useState(false);

  function refresh() { router.refresh(); }

  async function handleTest() {
    setTesting(true);
    try {
      const res = await fetch(`/api/admin/services/${code}/test`, { method: "POST" });
      const data = (await res.json()) as { ok: boolean; error?: string };
      if (data.ok) toast.success("✅ 连通正常");
      else toast.error(`❌ 连接失败: ${data.error ?? "未知错误"}`);
    } catch (err) {
      toast.error(`❌ 连接失败: ${err instanceof Error ? err.message : "网络错误"}`);
    } finally { setTesting(false); }
  }

  if (!canManage) return null;

  return (
    <div className="flex items-center gap-2">
      <ConfigDialog
        code={code} name={name} type={type} config={config} status={status}
        credentialType={credentialType} credentialConfig={credentialConfig}
        onDone={refresh}
      />
      <Button variant="outline" size="sm" onClick={handleTest} disabled={testing}>
        {testing ? "测试中..." : "测试"}
      </Button>
      <DeleteServiceDialog code={code} name={name} onDone={refresh} />
    </div>
  );
}
