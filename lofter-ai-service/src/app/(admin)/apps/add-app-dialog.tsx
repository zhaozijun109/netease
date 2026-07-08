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

export function AddAppDialog() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    app_code: "",
    app_id: "",
    app_key: "",
    name: "",
    description: "",
    require_user_code: true,
  });

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch("/api/admin/apps", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (!res.ok) {
        const data = (await res.json()) as { error?: string };
        throw new Error(data.error ?? "创建失败");
      }
      toast.success("App 创建成功");
      setOpen(false);
      setForm({ app_code: "", app_id: "", app_key: "", name: "", description: "", require_user_code: true });
      router.refresh();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "创建失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button />}>新增 App</DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>录入 AIGW App 凭证</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label htmlFor="app_code">App Code</Label>
            <Input
              id="app_code"
              placeholder="如：lofter_ai"
              value={form.app_code}
              onChange={(e) => setForm({ ...form, app_code: e.target.value })}
              required
            />
            <p className="text-xs text-muted-foreground">
              在{" "}
              <a
                href="https://modelspace.netease.com/model_access/app_manage"
                target="_blank"
                rel="noreferrer"
                className="underline underline-offset-2"
              >
                ModelSpace
              </a>{" "}
              申请账号后获得，格式：小写字母、数字、<code className="text-xs">._-</code>，最长 64 位。不建议用 <code className="text-xs">dm</code> 开头命名。
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="name">名称</Label>
            <Input
              id="name"
              placeholder="如：Lofter AI 服务"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="description">描述（可选）</Label>
            <Input
              id="description"
              placeholder="应用用途说明"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="app_id">AIGW App ID</Label>
            <Input
              id="app_id"
              placeholder="如：m2x3d00k-dei9-q7"
              value={form.app_id}
              onChange={(e) => setForm({ ...form, app_id: e.target.value })}
              required
            />
            <p className="text-xs text-muted-foreground">
              非敏感信息，可在 ModelSpace 的 App 管理页查看。格式示例：<code className="text-xs">m2x3d00k-dei9-q7</code>
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="app_key">AIGW App Key</Label>
            <Input
              id="app_key"
              type="password"
              placeholder="从 ModelSpace 获取"
              value={form.app_key}
              onChange={(e) => setForm({ ...form, app_key: e.target.value })}
              required
            />
            <p className="text-xs text-muted-foreground">
              敏感密钥，AIGW 不存储原始 Key，<span className="text-amber-600 dark:text-amber-400">重置后正在使用的服务将立即中断</span>。请妥善保管，填写后无法再次查看。格式示例：<code className="text-xs">4emjaubh3ja4ob2jlai4uvls82tk317d</code>
            </p>
          </div>
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
