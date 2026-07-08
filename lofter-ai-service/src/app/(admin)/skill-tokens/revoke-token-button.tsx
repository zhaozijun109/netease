"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
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

interface RevokeTokenButtonProps {
  tokenId: number;
  isAdmin: boolean; // true 时调管理员接口，false 时调用户自己接口
}

export function RevokeTokenButton({ tokenId, isAdmin }: RevokeTokenButtonProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  async function revoke() {
    setLoading(true);
    try {
      const url = isAdmin
        ? `/api/admin/skill-tokens/${tokenId}`
        : "/api/skill-tokens/mine";
      const res = await fetch(url, { method: "DELETE" });
      if (res.ok) {
        toast.success("Token 已吊销");
        router.refresh();
      } else {
        const data = (await res.json()) as { error?: string };
        toast.error(data.error ?? "吊销失败");
      }
    } catch {
      toast.error("吊销失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="destructive" size="sm" disabled={loading}>
          吊销
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认吊销此 Token？</AlertDialogTitle>
          <AlertDialogDescription>
            吊销后立即生效，无法恢复。使用此 Token 的服务将立即无法访问。
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            onClick={revoke}
          >
            确认吊销
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
