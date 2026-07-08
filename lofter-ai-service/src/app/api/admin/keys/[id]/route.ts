import { db } from "@/lib/db";
import { invalidateKey } from "@/lib/cache";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

type Params = { params: Promise<{ id: string }> };

/**
 * PATCH /api/admin/keys/:id
 * 更新 Key 的 name / description 或 is_active（吊销）
 *
 * 吊销时主动清除 LRU Cache，使变更立即生效
 */
export async function PATCH(req: Request, { params }: Params) {
  const unauth = await requirePermission("keys:manage", req);
  if (unauth) return unauth;

  const { id } = await params;
  const keyId = BigInt(id);
  if (!keyId) {
    return json({ error: "Invalid key id" }, { status: 400 });
  }

  const body = (await req.json()) as {
    name?: string;
    description?: string;
    is_active?: boolean;
  };

  // 先取出原记录（吊销时需要知道 apiKey 值来清除缓存）
  const existing = await db.apiKey.findUnique({ where: { id: keyId } });
  if (!existing) {
    return json({ error: "Key not found" }, { status: 404 });
  }

  const updated = await db.apiKey.update({
    where: { id: keyId },
    data: {
      ...(body.name !== undefined && { name: body.name }),
      ...(body.description !== undefined && { description: body.description }),
      ...(body.is_active !== undefined && { status: body.is_active ? 1 : 0 }),
    },
    select: {
      id: true,
      apiKey: true,
      type: true,
      name: true,
      description: true,
      status: true,
      updatedAt: true,
      app: { select: { appCode: true } },
    },
  });

  // 吊销时主动从 LRU Cache 删除，使已有缓存立即失效
  if (body.is_active === false) {
    invalidateKey(existing.apiKey);
  }

  return json({
    ...updated,
    apiKey: `${updated.apiKey.slice(0, 8)}****`,
  });
}
