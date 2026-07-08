import { db } from "@/lib/db";
import { requireAdminSession } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

/**
 * GET /api/admin/users?q=&role=&page=&page_size=
 * 列出用户（分页 + 搜索 + 角色过滤）
 */
export async function GET(req: Request) {
  const unauth = await requireAdminSession(req);
  if (unauth) return unauth;

  const { searchParams } = new URL(req.url);
  const q = searchParams.get("q") ?? "";
  const role = searchParams.get("role") ?? "";
  const page = Math.max(1, parseInt(searchParams.get("page") ?? "1", 10));
  const pageSize = Math.min(100, Math.max(1, parseInt(searchParams.get("page_size") ?? "20", 10)));

  const where = {
    ...(q && {
      OR: [
        { email: { contains: q } },
        { name: { contains: q } },
      ],
    }),
    ...(role && { role: role as never }),
  };

  const [total, users] = await Promise.all([
    db.user.count({ where }),
    db.user.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * pageSize,
      take: pageSize,
      select: {
        id: true,
        email: true,
        name: true,
        role: true,
        status: true,
        createdAt: true,
        _count: { select: { apiKeys: true } },
      },
    }),
  ]);

  return json({ total, page, page_size: pageSize, data: users });
}

/**
 * POST /api/admin/users
 * 管理员预创建用户（用户首次登录时会自动关联）
 *
 * Body: { email, name?, role }
 */
export async function POST(req: Request) {
  const unauth = await requireAdminSession(req);
  if (unauth) return unauth;

  const body = (await req.json()) as {
    email?: string;
    name?: string;
    role?: string;
  };

  if (!body.email || !body.role) {
    return json({ error: "email and role are required" }, { status: 400 });
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(body.email)) {
    return json({ error: "邮箱格式不正确" }, { status: 400 });
  }

  const validRoles = ["admin", "developer", "guest"];
  if (!validRoles.includes(body.role)) {
    return json({ error: `角色必须是：${validRoles.join("、")}` }, { status: 400 });
  }

  try {
    const user = await db.user.create({
      data: {
        email: body.email.toLowerCase().trim(),
        name: body.name?.trim() || null,
        role: body.role as never,
      },
      select: {
        id: true,
        email: true,
        name: true,
        role: true,
        status: true,
        createdAt: true,
      },
    });
    return json(user, { status: 201 });
  } catch (err) {
    const e = err as { code?: string };
    if (e.code === "P2002") {
      return json({ error: `邮箱 ${body.email} 已存在` }, { status: 409 });
    }
    throw err;
  }
}
