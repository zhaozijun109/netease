import { db } from "@/lib/db";
import { requireAdminSession, getAdminEmail } from "@/lib/admin-auth";
import { getSessionFromRequest } from "@/lib/auth-session";
import { json } from "@/lib/utils";

/**
 * PATCH /api/admin/users/:id
 * 更新用户角色或状态
 *
 * Body: { role?: "admin" | "user"; status?: 0 | 1 }
 */
export async function PATCH(
  req: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const unauth = await requireAdminSession(req);
  if (unauth) return unauth;

  const { id } = await params;
  const userId = BigInt(id);

  const body = (await req.json()) as {
    role?: "admin" | "developer" | "guest";
    status?: number;
  };

  if (body.role === undefined && body.status === undefined) {
    return json({ error: "role or status is required" }, { status: 400 });
  }

  const validRoles = ["admin", "developer", "guest"];
  if (body.role && !validRoles.includes(body.role)) {
    return json(
      { error: `role must be one of: ${validRoles.join(", ")}` },
      { status: 400 }
    );
  }

  // 禁止管理员撤销自己的 admin 权限，防止锁死
  if (body.role && body.role !== "admin") {
    const session = await getSessionFromRequest(req);
    const selfEmail = session?.user?.email;
    const target = await db.user.findUnique({
      where: { id: userId },
      select: { email: true },
    });
    if (target?.email === selfEmail) {
      return json(
        { error: "不能撤销自己的管理员权限" },
        { status: 400 }
      );
    }
  }

  const user = await db.user.update({
    where: { id: userId },
    data: {
      ...(body.role !== undefined && { role: body.role }),
      ...(body.status !== undefined && { status: body.status }),
    },
    select: {
      id: true,
      email: true,
      name: true,
      role: true,
      status: true,
      updatedAt: true,
    },
  });

  const operatorEmail = await getAdminEmail(req);
  console.info(
    `[admin-users] ${operatorEmail} updated user ${user.email}: role=${user.role} status=${user.status}`
  );

  return json(user);
}

/**
 * DELETE /api/admin/users/:id
 * 禁用用户（软删除，设置 status=0）
 */
export async function DELETE(
  req: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const unauth = await requireAdminSession(req);
  if (unauth) return unauth;

  const { id } = await params;
  const userId = BigInt(id);

  const session = await getSessionFromRequest(req);
  const target = await db.user.findUnique({
    where: { id: userId },
    select: { email: true },
  });

  if (target?.email === session?.user?.email) {
    return json({ error: "不能禁用自己的账号" }, { status: 400 });
  }

  await db.user.update({
    where: { id: userId },
    data: { status: 0 },
  });

  return json({ success: true });
}
