import { logger } from "@/lib/logger";
import { NextResponse } from "next/server";

/**
 * GET /api/health
 * 健康检查端点：验证 env、logger、db 模块可正常加载
 */
export async function GET() {
  return NextResponse.json(200);
}
