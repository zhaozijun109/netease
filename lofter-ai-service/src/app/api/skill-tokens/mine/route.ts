import { db } from "@/lib/db";
import { getSessionFromRequest } from "@/lib/auth-session";
import { getMicroFrontendSession } from "@/auth";
import { generateSkillToken, invalidateSkillToken } from "@/lib/skill-token";
import { json } from "@/lib/utils";

async function getSession(req: Request) {
  const session = await getSessionFromRequest(req);
  if (session) return session;
  return getMicroFrontendSession(req);
}

// GET /api/skill-tokens/mine — 查看自己的 Token 状态（不返回明文）
export async function GET(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  const token = await db.skillToken.findFirst({
    where: { userId: user.id, status: 1 },
    select: { id: true, name: true, permissions: true, status: true, expiresAt: true, createdAt: true },
    orderBy: { createdAt: "desc" },
  });
  return json(token ?? null);
}

// POST /api/skill-tokens/mine — 申请新 Token（吊销旧的，仅此一次返回明文）
export async function POST(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  // 吊销该用户所有有效 Token
  const oldTokens = await db.skillToken.findMany({ where: { userId: user.id, status: 1 } });
  for (const t of oldTokens) {
    await db.skillToken.update({ where: { id: t.id }, data: { status: 0 } });
    invalidateSkillToken(t.token);
  }

  // 生成新 Token
  const rawToken = generateSkillToken();
  const record = await db.skillToken.create({
    data: {
      token: rawToken,
      name: `${session.user.email} 的 Skill Token`,
      userId: user.id,
      permissions: ["*"],
    },
    select: { id: true, token: true, name: true, permissions: true, createdAt: true },
  });

  return json(record, { status: 201 });
}

// DELETE /api/skill-tokens/mine — 自助吊销
export async function DELETE(req: Request) {
  const session = await getSession(req);
  if (!session?.user?.email) return json({ error: "Unauthorized" }, { status: 401 });

  const user = await db.user.findUnique({ where: { email: session.user.email } });
  if (!user) return json({ error: "User not found" }, { status: 404 });

  const tokens = await db.skillToken.findMany({ where: { userId: user.id, status: 1 } });
  for (const t of tokens) {
    await db.skillToken.update({ where: { id: t.id }, data: { status: 0 } });
    invalidateSkillToken(t.token);
  }
  return json({ ok: true });
}
