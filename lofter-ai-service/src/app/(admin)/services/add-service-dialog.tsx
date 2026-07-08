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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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

interface AddServiceDialogProps {
  canManage: boolean;
}

export function AddServiceDialog({ canManage }: AddServiceDialogProps) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    code: "",
    name: "",
    type: "http_proxy",
    config: "",
    credentialType: "auth_key",
    credentialConfig: "",
  });

  if (!canManage) return null;

  function handleTypeChange(value: string) {
    setForm((prev) => ({ ...prev, type: value, config: "" }));
  }

  function handleCredTypeChange(value: string) {
    setForm((prev) => ({ ...prev, credentialType: value, credentialConfig: "" }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (form.type !== "static_content" && form.config.trim()) {
      try { JSON.parse(form.config); }
      catch { toast.error("服务 Config 格式不合法，请输入有效的 JSON"); return; }
    }
    if (form.credentialConfig.trim()) {
      try { JSON.parse(form.credentialConfig); }
      catch { toast.error("凭据 Config 格式不合法，请输入有效的 JSON"); return; }
    }

    setLoading(true);
    try {
      const body: Record<string, unknown> = {
        code: form.code,
        name: form.name,
        type: form.type,
        config: form.type === "static_content"
          ? { content: form.config, contentType: "text/markdown" }
          : form.config.trim() ? JSON.parse(form.config) : {},
      };
      if (form.credentialConfig.trim()) {
        body.credentialType = form.credentialType;
        body.credentialConfig = JSON.parse(form.credentialConfig);
      }

      const res = await fetch("/api/admin/services", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        const data = (await res.json()) as { error?: string };
        throw new Error(data.error ?? "创建失败");
      }
      toast.success("服务创建成功");
      setOpen(false);
      setForm({ code: "", name: "", type: "http_proxy", config: "", credentialType: "auth_key", credentialConfig: "" });
      router.refresh();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "创建失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button />}>新增服务</DialogTrigger>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>新增服务</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label htmlFor="code">Code（唯一标识）</Label>
            <Input
              id="code"
              placeholder="如：doris"
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="name">名称</Label>
            <Input
              id="name"
              placeholder="如：Doris 数据库"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="type">类型</Label>
            <Select value={form.type} onValueChange={handleTypeChange}>
              <SelectTrigger id="type">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="http_proxy">http_proxy</SelectItem>
                <SelectItem value="db_query">db_query</SelectItem>
                <SelectItem value="static_content">static_content</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {form.type === "static_content" ? (
          <div className="space-y-2">
            <Label htmlFor="staticContent">内容（纯文本，直接粘贴 Prompt）</Label>
            <textarea
              id="staticContent"
              className="flex min-h-[200px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 font-mono"
              placeholder="直接粘贴 Prompt 内容，无需 JSON 格式..."
              value={form.config}
              onChange={(e) => setForm({ ...form, config: e.target.value })}
              spellCheck={false}
            />
          </div>
          ) : (
          <div className="space-y-2">
            <Label htmlFor="config">服务 Config（JSON，可选）</Label>
            <textarea
              id="config"
              className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 font-mono"
              placeholder={SERVICE_CONFIG_PLACEHOLDERS[form.type] ?? "{}"}
              value={form.config}
              onChange={(e) => setForm({ ...form, config: e.target.value })}
              spellCheck={false}
            />
          </div>
          )}
          {form.type !== "static_content" && (<>
          {/* 分隔线 */}
          <hr className="border-border" />
          <div className="space-y-2">
            <Label htmlFor="credentialType">凭据类型（可选）</Label>
            <Select value={form.credentialType} onValueChange={handleCredTypeChange}>
              <SelectTrigger id="credentialType">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="auth_key">auth_key（网易 Auth 动态 Token）</SelectItem>
                <SelectItem value="db_password">db_password（数据库账号密码）</SelectItem>
                <SelectItem value="static_token">static_token（固定 Token）</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="credentialConfig">凭据 Config（JSON，留空则暂不配置）</Label>
            <textarea
              id="credentialConfig"
              className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 font-mono"
              placeholder={CREDENTIAL_CONFIG_PLACEHOLDERS[form.credentialType] ?? "{}"}
              value={form.credentialConfig}
              onChange={(e) => setForm({ ...form, credentialConfig: e.target.value })}
              spellCheck={false}
            />
          </div>
          </>)}
          <div className="flex gap-2 pt-2 justify-end">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              取消
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "创建中..." : "创建"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
