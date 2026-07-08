import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

// GET /api/admin/skill-tokens — 管理员查看所有 Token（脱敏）
export async function GET(req: Request) {
  const unauth = await requirePermission("skill-tokens:manage", req);
  if (unauth) return unauth;

  const tokens = await db.skillToken.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true, name: true, token: true, permissions: true,
      status: true, expiresAt: true, createdAt: true,
      user: { select: { email: true, name: true } },
    },
  });
  return json(tokens.map((t) => ({ ...t, token: `${t.token.slice(0, 8)}****` })));
}
