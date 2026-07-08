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
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";

interface ApplyTokenDialogProps {
  hasToken: boolean; // 当前用户是否已有有效 Token
}

interface AppliedToken {
  id: number;
  token: string;
  name: string;
  permissions: string[];
  createdAt: string;
}

export function ApplyTokenDialog({ hasToken }: ApplyTokenDialogProps) {
  const router = useRouter();

  // 控制重新申请前的确认 AlertDialog
  const [confirmOpen, setConfirmOpen] = useState(false);
  // 控制申请 Dialog
  const [dialogOpen, setDialogOpen] = useState(false);
  // 申请成功后展示的 token
  const [appliedToken, setAppliedToken] = useState<AppliedToken | null>(null);
  // 是否已复制
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);

  async function applyToken() {
    setLoading(true);
    try {
      const res = await fetch("/api/skill-tokens/mine", { method: "POST" });
      if (!res.ok) {
        const data = (await res.json()) as { error?: string };
        throw new Error(data.error ?? "申请失败");
      }
      const data = (await res.json()) as AppliedToken;
      setAppliedToken(data);
      router.refresh();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "申请失败");
      setDialogOpen(false);
    } finally {
      setLoading(false);
    }
  }

  function handleOpenDialog() {
    setAppliedToken(null);
    setCopied(false);
    setDialogOpen(true);
  }

  function handleTriggerClick() {
    if (hasToken) {
      // 已有 Token，先弹确认
      setConfirmOpen(true);
    } else {
      handleOpenDialog();
    }
  }

  function handleClose() {
    setDialogOpen(false);
    setAppliedToken(null);
    setCopied(false);
  }

  async function copyToken() {
    if (!appliedToken) return;
    try {
      await navigator.clipboard.writeText(appliedToken.token);
      setCopied(true);
      toast.success("Token 已复制到剪贴板");
    } catch {
      toast.error("复制失败，请手动复制");
    }
  }

  return (
    <>
      {/* 重新申请前确认 AlertDialog */}
      <AlertDialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认重新申请？</AlertDialogTitle>
            <AlertDialogDescription>
              重新申请将自动吊销当前有效的 Token，已部署的服务需要更新新 Token 后才能继续使用。此操作无法撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => {
                setConfirmOpen(false);
                handleOpenDialog();
              }}
            >
              确认，吊销旧 Token 并重新申请
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* 申请 Dialog */}
      <Dialog open={dialogOpen} onOpenChange={(open) => { if (!open) handleClose(); else setDialogOpen(true); }}>
        <DialogTrigger render={<Button variant={hasToken ? "outline" : "default"} onClick={handleTriggerClick} />}>
          {hasToken ? "重新申请" : "申请 Skill Token"}
        </DialogTrigger>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>申请 Skill Token</DialogTitle>
          </DialogHeader>
          {appliedToken ? (
            <TokenResult token={appliedToken.token} copied={copied} onCopy={copyToken} onClose={handleClose} />
          ) : (
            <ApplyForm loading={loading} onApply={applyToken} onCancel={handleClose} />
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}

// 权限选择表单（权限控制暂未开放，保持 disabled 展示）
function ApplyForm({
  loading,
  onApply,
  onCancel,
}: {
  loading: boolean;
  onApply: () => void;
  onCancel: () => void;
}) {
  return (
    <div className="space-y-4 mt-2">
      <div className="space-y-3">
        <p className="text-sm font-medium">权限范围</p>
        <div className="rounded-md border bg-muted/40 p-3 space-y-2 opacity-60">
          <label className="flex items-center gap-2 cursor-not-allowed">
            <input type="radio" checked readOnly disabled className="cursor-not-allowed" />
            <span className="text-sm">全部服务（*）</span>
          </label>
          <label className="flex items-center gap-2 cursor-not-allowed">
            <input type="radio" disabled className="cursor-not-allowed" />
            <span className="text-sm">指定服务</span>
          </label>
        </div>
        <p className="text-xs text-muted-foreground">
          🚧 权限控制即将支持，目前申请的 Token 默认拥有全部服务访问权限。
        </p>
      </div>
      <div className="flex gap-2 pt-2 justify-end">
        <Button type="button" variant="outline" onClick={onCancel}>
          取消
        </Button>
        <Button type="button" onClick={onApply} disabled={loading}>
          {loading ? "申请中..." : "申请"}
        </Button>
      </div>
    </div>
  );
}

// 申请成功后展示 Token 明文
function TokenResult({
  token,
  copied,
  onCopy,
  onClose,
}: {
  token: string;
  copied: boolean;
  onCopy: () => void;
  onClose: () => void;
}) {
  return (
    <div className="space-y-4 mt-2">
      <div className="rounded-md border border-amber-300 bg-amber-50 dark:bg-amber-950/20 dark:border-amber-800 p-3 space-y-1">
        <p className="text-sm font-medium text-amber-800 dark:text-amber-300">
          ⚠️ Token 仅展示一次，关闭后无法找回
        </p>
        <p className="text-xs text-amber-700 dark:text-amber-400">
          请立即复制并妥善保存，配置到你的服务后不再需要查看原始 Token。
        </p>
      </div>
      <div className="space-y-2">
        <p className="text-sm font-medium">你的 Skill Token</p>
        <div className="rounded-md border bg-muted p-3 flex items-center gap-2">
          <code className="font-mono text-xs flex-1 break-all">{token}</code>
          <Button type="button" variant="outline" size="sm" onClick={onCopy} className="shrink-0">
            {copied ? "已复制" : "复制"}
          </Button>
        </div>
        <p className="text-xs text-muted-foreground">
          配置到 Skill CLI：
          <code className="font-mono ml-1">python lofter_data.py config api_key &quot;{token.slice(0, 8)}...&quot;</code>
        </p>
      </div>
      <div className="flex justify-end pt-2">
        <Button type="button" onClick={onClose} disabled={!copied} title={copied ? "" : "请先复制 Token"}>
          {copied ? "已复制，关闭" : "关闭（请先复制）"}
        </Button>
      </div>
    </div>
  );
}
