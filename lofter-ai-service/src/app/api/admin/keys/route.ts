import crypto from "crypto";
import { db } from "@/lib/db";
import { getAdminEmail, getAdminRole, requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";
import type { ApiKeyType } from "@/generated/prisma";

/**
 * GET /api/admin/keys?app_code=&type=&page=&page_size=
 * 列出 API Keys（key 只展示前 8 位 + ****，保护完整值）
 * 权限：admin / developer
 */
export async function GET(req: Request) {
  const unauth = await requirePermission("keys:view", req);
  if (unauth) return unauth;

  const { searchParams } = new URL(req.url);
  const appCode = searchParams.get("app_code") ?? undefined;
  const type = searchParams.get("type") as ApiKeyType | undefined ?? undefined;
  const page = Math.max(1, parseInt(searchParams.get("page") ?? "1", 10));
  const pageSize = Math.min(
    100,
    Math.max(1, parseInt(searchParams.get("page_size") ?? "20", 10))
  );

  const where = {
    ...(appCode && { app: { appCode } }),
    ...(type && { type }),
  };

  const [total, keys] = await Promise.all([
    db.apiKey.count({ where }),
    db.apiKey.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip: (page - 1) * pageSize,
      take: pageSize,
      select: {
        id: true,
        apiKey: true,
        type: true,
        name: true,
        description: true,
        status: true,
        createdBy: true,
        createdAt: true,
        app: { select: { appCode: true, name: true } },
      },
    }),
  ]);

  // 脱敏：只展示前 8 位
  const maskedKeys = keys.map((k) => ({
    ...k,
    apiKey: `${k.apiKey.slice(0, 8)}****`,
  }));

  return json({ total, page, page_size: pageSize, data: maskedKeys });
}

/**
 * POST /api/admin/keys
 * 生成新的 API Key（创建时一次性返回完整 key）
 * 权限：admin / developer
 *
 * Body:
 *   type           "personal" | "service"
 *   app_code       string (required)
 *   name?          string  -- service key 必填（业务系统名称）
 *   description?   string
 *   target_name?   string  -- admin only；为其他用户创建时的标识（邮箱/花名/工号等任意字符串）
 */
export async function POST(req: Request) {
  const unauth = await requirePermission("keys:manage", req);
  if (unauth) return unauth;

  const body = (await req.json()) as {
    type?: string;
    app_code?: string;
    name?: string;
    description?: string;
    target_name?: string;
  };

  if (!body.type || !body.app_code) {
    return json(
      { error: "type and app_code are required" },
      { status: 400 }
    );
  }

  if (body.type !== "personal" && body.type !== "service") {
    return json(
      { error: 'type must be "personal" or "service"' },
      { status: 400 }
    );
  }

  const adminEmail = await getAdminEmail(req);
  const adminRole = await getAdminRole(req);

  // target_name：只有 admin 可以为他人创建
  if (body.target_name !== undefined && adminRole !== "admin") {
    return json(
      { error: "只有管理员可以为其他用户创建 Key" },
      { status: 403 }
    );
  }

  // 确定 key 归属的用户标识（personal key）
  // - 未传 target_name → 为自己创建，使用当前用户邮箱
  // - 传了 target_name → 为他人创建，使用 target_name
  let name: string;
  if (body.type === "personal") {
    if (body.target_name !== undefined) {
      if (!body.target_name.trim()) {
        return json({ error: "target_name 不能为空" }, { status: 400 });
      }
      name = body.target_name.trim();
    } else {
      if (!adminEmail) {
        return json(
          { error: "无法获取当前用户邮箱，请重新登录" },
          { status: 400 }
        );
      }
      name = adminEmail;
    }
  } else {
    // service key：name 为业务系统名称，必填
    if (!body.name?.trim()) {
      return json(
        { error: "service key 需填写业务系统名称" },
        { status: 400 }
      );
    }
    name = body.name.trim();
  }

  // 检查 app 是否存在
  const app = await db.app.findUnique({ where: { appCode: body.app_code } });
  if (!app) {
    return json(
      { error: `App "${body.app_code}" not found` },
      { status: 404 }
    );
  }

  // personal key：尝试通过 name 作为邮箱查找 User（找不到也没关系，userId 可为 null）
  let userId: bigint | undefined;
  if (body.type === "personal") {
    const user = await db.user.findUnique({
      where: { email: name },
      select: { id: true },
    });
    if (user) userId = user.id;
  }

  // 生成 Key：sk-{appCode}-{hex32} 或 svc-{appCode}-{hex32}
  const prefix = body.type === "personal" ? "sk" : "svc";
  const hex = crypto.randomBytes(16).toString("hex");
  const rawKey = `${prefix}-${body.app_code}-${hex}`;

  const keySelect = {
    id: true,
    apiKey: true, // 完整 key，仅此一次
    type: true,
    name: true,
    description: true,
    status: true,
    createdAt: true,
    app: { select: { appCode: true, name: true } },
  } as const;

  // personal key：检查是否已有记录，防止重复创建
  if (body.type === "personal") {
    // userId 已知 → 用唯一索引精确查询；userId 为 null → 用 name + appId 查询
    const existing = userId
      ? await db.apiKey.findUnique({
          where: { userId_appId: { userId, appId: app.id } },
          select: { id: true, status: true },
        })
      : await db.apiKey.findFirst({
          where: { type: "personal", name, appId: app.id },
          select: { id: true, status: true },
        });

    if (existing) {
      if (existing.status === 1) {
        return json(
          { error: `用户 "${name}" 在 App "${body.app_code}" 下已有有效的 personal key，如需更换请先吊销旧 key` },
          { status: 409 }
        );
      }
      // 旧 key 已吊销，用新 key 值重新激活
      const keyRecord = await db.apiKey.update({
        where: { id: existing.id },
        data: {
          apiKey: rawKey,
          description: body.description ?? null,
          status: 1,
          createdBy: adminEmail ?? undefined,
          createdAt: new Date(),
        },
        select: keySelect,
      });
      return json(keyRecord, { status: 201 });
    }
  }

  const keyRecord = await db.apiKey.create({
    data: {
      apiKey: rawKey,
      type: body.type as ApiKeyType,
      appId: app.id,
      userId: userId ?? null,
      name,
      description: body.description ?? null,
      createdBy: adminEmail ?? undefined,
    },
    select: keySelect,
  });

  return json(keyRecord, { status: 201 });
}
