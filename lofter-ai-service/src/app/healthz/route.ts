import { NextResponse } from "next/server";

/**
 * GET /healthz
 * Liveness probe endpoint - 仅验证进程存活，不检查外部依赖
 */
export async function GET() {
  return NextResponse.json(200);
}
