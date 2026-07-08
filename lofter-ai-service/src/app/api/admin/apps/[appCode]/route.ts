import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

type Params = { params: Promise<{ appCode: string }> };

/**
 * PATCH /api/admin/apps/:appCode
 * 更新 App 配置（name、description、require_user_code、is_active）
 */
export async function PATCH(req: Request, { params }: Params) {
  const unauth = await requirePermission("apps:manage", req);
  if (unauth) return unauth;

  const { appCode } = await params;
  const body = (await req.json()) as {
    name?: string;
    description?: string;
    require_user_code?: boolean;
    is_active?: boolean;
  };

  const app = await db.app.update({
    where: { appCode },
    data: {
      ...(body.name !== undefined && { name: body.name }),
      ...(body.description !== undefined && { description: body.description }),
      ...(body.require_user_code !== undefined && {
        requireUserCode: body.require_user_code,
      }),
      ...(body.is_active !== undefined && { status: body.is_active ? 1 : 0 }),
    },
    select: {
      appCode: true,
      name: true,
      description: true,
      requireUserCode: true,
      status: true,
      updatedAt: true,
    },
  });

  return json(app);
}

/**
 * DELETE /api/admin/apps/:appCode
 * 软删除 App（设置 status=0）
 */
export async function DELETE(req: Request, { params }: Params) {
  const unauth = await requirePermission("apps:manage", req);
  if (unauth) return unauth;

  const { appCode } = await params;

  await db.app.update({
    where: { appCode },
    data: { status: 0 },
  });

  return new Response(null, { status: 204 });
}
