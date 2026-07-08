"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
import { toast } from "sonner";
import { ALL_ROLES, ROLE_LABELS, type UserRole } from "@/lib/permissions";

export function UserRoleSelect({
  userId,
  currentRole,
  userEmail,
  selfEmail,
}: {
  userId: number;
  currentRole: UserRole;
  userEmail: string;
  selfEmail: string | null | undefined;
}) {
  const router = useRouter();
  const isSelf = userEmail === selfEmail;
  const [loading, setLoading] = useState(false);
  const [pendingRole, setPendingRole] = useState<string | null>(null);

  async function confirmRoleChange() {
    if (!pendingRole) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/admin/users/${userId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: pendingRole }),
      });
      const data = await res.json() as { error?: string };
      if (res.ok) {
        toast.success(`角色已更新为「${ROLE_LABELS[pendingRole as UserRole]}」`);
        router.refresh();
      } else {
        toast.error(data.error ?? "操作失败");
      }
    } finally {
      setLoading(false);
      setPendingRole(null);
    }
  }

  return (
    <>
      <Select
        value={currentRole}
        onValueChange={(newRole) => {
          if (newRole === currentRole) return;
          setPendingRole(newRole);
        }}
        disabled={isSelf || loading}
      >
        <SelectTrigger
          className="w-28 h-8 text-xs"
          title={isSelf ? "不能修改自己的角色" : undefined}
        >
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {ALL_ROLES.map((role) => (
            <SelectItem key={role} value={role} className="text-xs">
              {ROLE_LABELS[role]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <AlertDialog open={!!pendingRole} onOpenChange={(open) => { if (!open) setPendingRole(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认修改角色？</AlertDialogTitle>
            <AlertDialogDescription>
              将 {userEmail} 的角色改为「{pendingRole ? ROLE_LABELS[pendingRole as UserRole] : ""}」。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={confirmRoleChange}>确认</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

export function UserStatusToggle({
  userId,
  currentStatus,
  userEmail,
  selfEmail,
}: {
  userId: number;
  currentStatus: number;
  userEmail: string;
  selfEmail: string | null | undefined;
}) {
  const router = useRouter();
  const isSelf = userEmail === selfEmail;
  const newStatus = currentStatus === 1 ? 0 : 1;
  const action = newStatus === 1 ? "启用" : "禁用";

  async function toggleStatus() {
    const res = await fetch(`/api/admin/users/${userId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: newStatus }),
    });
    const data = await res.json() as { error?: string };
    if (res.ok) {
      toast.success(`用户已${action}`);
      router.refresh();
    } else {
      toast.error(data.error ?? "操作失败");
    }
  }

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button
          variant={currentStatus === 1 ? "destructive" : "outline"}
          size="sm"
          className="h-8 text-xs"
          disabled={isSelf}
          title={isSelf ? "不能禁用自己的账号" : undefined}
        >
          {currentStatus === 1 ? "禁用" : "启用"}
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认{action}用户？</AlertDialogTitle>
          <AlertDialogDescription>
            即将{action}用户 {userEmail}。
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction
            className={newStatus === 0 ? "bg-destructive text-destructive-foreground hover:bg-destructive/90" : undefined}
            onClick={toggleStatus}
          >
            确认{action}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
