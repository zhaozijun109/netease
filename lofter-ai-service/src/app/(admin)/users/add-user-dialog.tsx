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
import { ALL_ROLES, ROLE_LABELS, type UserRole } from "@/lib/permissions";

export function AddUserDialog() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    email: "",
    role: "guest" as UserRole,
  });

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch("/api/admin/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      const data = (await res.json()) as { error?: string };
      if (!res.ok) throw new Error(data.error ?? "创建失败");
      toast.success(`用户 ${form.email} 创建成功`);
      setOpen(false);
      setForm({ email: "", role: "guest" });
      router.refresh();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "创建失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button />}>新增用户</DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>新增用户</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label htmlFor="email">邮箱 <span className="text-destructive">*</span></Label>
            <Input
              id="email"
              type="email"
              placeholder="user@corp.netease.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
            <p className="text-xs text-muted-foreground">
              用户首次登录时会自动关联此邮箱的权限
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="role-select">角色 <span className="text-destructive">*</span></Label>
            <select
              id="role-select"
              className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}
              required
            >
              {ALL_ROLES.map((role) => (
                <option key={role} value={role}>
                  {ROLE_LABELS[role]}
                </option>
              ))}
            </select>
            <p className="text-xs text-muted-foreground">
              管理员·完整权限 / 开发·管理 Keys / 运营·管理 Keys / 访客·仅统计
            </p>
          </div>
          <div className="flex gap-2 pt-2 justify-end">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              取消
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "创建中…" : "创建"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
