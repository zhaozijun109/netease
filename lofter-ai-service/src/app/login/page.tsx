"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

function LoginContent() {
  const searchParams = useSearchParams();
  const openidError = searchParams.get("error");

  return (
    <CardContent className="space-y-4">
      {openidError && (
        <div className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive">
          登录失败：{decodeURIComponent(openidError)}
        </div>
      )}
      <Button
        className="w-full"
        onClick={() => {
          const callbackUrl = searchParams.get("callbackUrl");
          const loginUrl = new URL("/api/auth/openid/login", window.location.origin);
          if (callbackUrl) {
            loginUrl.searchParams.set("callbackUrl", callbackUrl);
          }
          window.location.href = loginUrl.toString();
        }}
      >
        网易 OpenID 登录
      </Button>
    </CardContent>
  );
}

export default function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/40">
      <Card className="w-full max-w-sm">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">AI 基础服务</CardTitle>
          <CardDescription>管理后台登录</CardDescription>
        </CardHeader>
        <Suspense fallback={<CardContent><div className="h-10" /></CardContent>}>
          <LoginContent />
        </Suspense>
      </Card>
    </div>
  );
}
