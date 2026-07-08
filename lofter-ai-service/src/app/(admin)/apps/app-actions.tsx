"use client";

import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";

export function AppActions({
  appCode,
  isActive,
  requireUserCode,
}: {
  appCode: string;
  isActive: boolean;
  requireUserCode: boolean;
}) {
  const router = useRouter();

  async function toggleActive() {
    const res = await fetch(`/api/admin/apps/${appCode}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ is_active: !isActive }),
    });
    if (res.ok) {
      toast.success(isActive ? "App 已禁用" : "App 已启用");
      router.refresh();
    } else {
      toast.error("操作失败");
    }
  }

  async function toggleRequireUserCode() {
    const res = await fetch(`/api/admin/apps/${appCode}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ require_user_code: !requireUserCode }),
    });
    if (res.ok) {
      toast.success(requireUserCode ? "已关闭强制 User Code" : "已开启强制 User Code");
      router.refresh();
    } else {
      toast.error("操作失败");
    }
  }

  return (
    <div className="flex gap-2 justify-end">
      <Button
        variant="outline"
        size="sm"
        onClick={toggleRequireUserCode}
      >
        {requireUserCode ? "取消强制" : "强制 UC"}
      </Button>
      <Button
        variant={isActive ? "destructive" : "outline"}
        size="sm"
        onClick={toggleActive}
      >
        {isActive ? "禁用" : "启用"}
      </Button>
    </div>
  );
}
