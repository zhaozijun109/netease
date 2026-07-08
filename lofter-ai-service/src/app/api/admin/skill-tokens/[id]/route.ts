import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { invalidateSkillToken } from "@/lib/skill-token";
import { json } from "@/lib/utils";

type Params = { params: Promise<{ id: string }> };

// DELETE /api/admin/skill-tokens/{id} — 强制吊销
export async function DELETE(req: Request, { params }: Params) {
  const unauth = await requirePermission("skill-tokens:manage", req);
  if (unauth) return unauth;
  const { id } = await params;

  const record = await db.skillToken.findUnique({ where: { id: BigInt(id) } });
  if (!record) return json({ error: "Not found" }, { status: 404 });

  await db.skillToken.update({ where: { id: BigInt(id) }, data: { status: 0 } });
  invalidateSkillToken(record.token);
  return json({ ok: true });
}
