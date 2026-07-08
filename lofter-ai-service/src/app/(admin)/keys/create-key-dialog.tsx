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
import { toast } from "sonner";

type App = { appCode: string; name: string };

export function CreateKeyDialog({
  apps,
  isAdmin = false,
}: {
  apps: App[];
  isAdmin?: boolean;
}) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [createdKey, setCreatedKey] = useState<string | null>(null);
  const [form, setForm] = useState({
    type: "personal" as "personal" | "service",
    app_code: apps[0]?.appCode ?? "",
    name: "",
    description: "",
    // delegation
    forOther: false,
    target_name: "",
  });

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      type Payload = {
        type: string;
        app_code: string;
        description?: string;
        name?: string;
        target_name?: string;
      };

      const payload: Payload = {
        type: form.type,
        app_code: form.app_code,
        description: form.description || undefined,
      };

      if (form.type === "service") {
        payload.name = form.name;
      }

      if (form.type === "personal" && form.forOther && form.target_name.trim()) {
        payload.target_name = form.target_name.trim();
      }

      const res = await fetch("/api/admin/keys", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = (await res.json()) as { apiKey?: string; error?: string };
      if (!res.ok) throw new Error(data.error ?? "创建失败");
      setCreatedKey(data.apiKey ?? null);
      router.refresh();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "创建失败");
    } finally {
      setLoading(false);
    }
  }

  function handleClose() {
    setOpen(false);
    setCreatedKey(null);
    setForm({
      type: "personal",
      app_code: apps[0]?.appCode ?? "",
      name: "",
      description: "",
      forOther: false,
      target_name: "",
    });
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) handleClose(); else setOpen(true); }}>
      <DialogTrigger render={<Button />}>生成 Key</DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>生成 API Key</DialogTitle>
        </DialogHeader>

        {createdKey ? (
          <div className="space-y-4 mt-2">
            <p className="text-sm text-muted-foreground">
              Key 已创建。请立即复制并妥善保存，<strong>此后不再展示完整值</strong>。
            </p>
            <div className="rounded-md bg-muted p-3 font-mono text-xs break-all select-all">
              {createdKey}
            </div>
            <div className="flex justify-between gap-2">
              <Button
                variant="outline"
                onClick={async () => {
                  try {
                    await navigator.clipboard.writeText(createdKey);
                    toast.success("已复制到剪贴板");
                  } catch {
                    toast.error("复制失败，请手动复制");
                  }
                }}
              >
                复制
              </Button>
              <Button onClick={handleClose}>完成</Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4 mt-2">
            {/* Key 类型 */}
            <div className="space-y-2">
              <Label>类型</Label>
              <div className="flex gap-2">
                {(["service", "personal"] as const).map((t) => (
                  <Button
                    key={t}
                    type="button"
                    variant={form.type === t ? "default" : "outline"}
                    size="sm"
                    onClick={() =>
                      setForm({ ...form, type: t, forOther: false, target_name: "" })
                    }
                  >
                    {t === "service" ? "服务 svc-" : "个人 sk-"}
                  </Button>
                ))}
              </div>
            </div>

            {/* App 选择 */}
            <div className="space-y-2">
              <Label>App</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
                value={form.app_code}
                onChange={(e) => setForm({ ...form, app_code: e.target.value })}
                required
              >
                {apps.map((a) => (
                  <option key={a.appCode} value={a.appCode}>
                    {a.name} ({a.appCode})
                  </option>
                ))}
              </select>
            </div>

            {/* Service key：业务系统名称 */}
            {form.type === "service" && (
              <div className="space-y-2">
                <Label htmlFor="name">业务系统名称</Label>
                <Input
                  id="name"
                  placeholder="如：lofter-search"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  required
                />
              </div>
            )}

            {/* Personal key：admin 可选择为他人创建 */}
            {form.type === "personal" && isAdmin && (
              <div className="space-y-2">
                <Label>创建对象</Label>
                <div className="flex gap-3">
                  {[false, true].map((isOther) => (
                    <label
                      key={String(isOther)}
                      className="flex items-center gap-1.5 cursor-pointer text-sm"
                    >
                      <input
                        type="radio"
                        name="forOther"
                        checked={form.forOther === isOther}
                        onChange={() =>
                          setForm({ ...form, forOther: isOther, target_name: "" })
                        }
                        className="accent-primary"
                      />
                      {isOther ? "为其他用户创建" : "为自己创建"}
                    </label>
                  ))}
                </div>
              </div>
            )}

            {/* 为他人创建：用户标识 */}
            {form.type === "personal" && form.forOther && (
              <div className="space-y-2">
                <Label htmlFor="target_name">用户标识</Label>
                <Input
                  id="target_name"
                  placeholder="邮箱 / 花名 / 工号"
                  value={form.target_name}
                  onChange={(e) => setForm({ ...form, target_name: e.target.value })}
                  required
                />
              </div>
            )}

            {/* 描述 */}
            <div className="space-y-2">
              <Label htmlFor="description">描述（可选）</Label>
              <Input
                id="description"
                placeholder="用途说明"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </div>

            <div className="flex gap-2 pt-2 justify-end">
              <Button type="button" variant="outline" onClick={handleClose}>
                取消
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? "生成中..." : "生成"}
              </Button>
            </div>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}
