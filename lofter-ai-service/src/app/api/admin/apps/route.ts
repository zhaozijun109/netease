import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { verifyAigwCredentials } from "@/lib/aigw-client";
import { json } from "@/lib/utils";

/**
 * GET /api/admin/apps
 * 列出所有 App（app_key 不返回）
 * 权限：admin / developer
 */
export async function GET(req: Request) {
  const unauth = await requirePermission("apps:view", req);
  if (unauth) return unauth;

  const apps = await db.app.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true,
      appCode: true,
      appId: true,
      name: true,
      description: true,
      requireUserCode: true,
      status: true,
      createdAt: true,
      updatedAt: true,
      _count: { select: { apiKeys: true } },
    },
  });

  return json(apps);
}

/**
 * POST /api/admin/apps
 * 录入新的 AIGW App 凭证
 * 权限：admin only
 *
 * Body: { app_code, app_id, app_key, name, description?, require_user_code? }
 */
export async function POST(req: Request) {
  const unauth = await requirePermission("apps:manage", req);
  if (unauth) return unauth;

  const body = (await req.json()) as {
    app_code?: string;
    app_id?: string;
    app_key?: string;
    name?: string;
    description?: string;
    require_user_code?: boolean;
  };

  if (!body.app_code || !body.app_id || !body.app_key || !body.name) {
    return json(
      { error: "app_code, app_id, app_key, name are required" },
      { status: 400 }
    );
  }

  // HTTP 头只允许 ASCII 可见字符，app_id / app_key 会拼入 Authorization header
  const isAsciiPrintable = (s: string) => /^[\x20-\x7E]+$/.test(s);
  if (!isAsciiPrintable(body.app_id)) {
    return json(
      { error: "app_id 只能包含 ASCII 字符，不支持中文或其他特殊字符。请填写 AIGW 分配的英文标识符（如 lofter_tech），中文名称请填写在「应用名称」字段中。" },
      { status: 400 }
    );
  }
  if (!isAsciiPrintable(body.app_key)) {
    return json(
      { error: "app_key 只能包含 ASCII 字符，请检查填写的密钥是否正确。" },
      { status: 400 }
    );
  }

  // 创建前验证 AIGW 凭证是否有效
  const verify = await verifyAigwCredentials(body.app_id, body.app_key);
  if (!verify.ok) {
    return json(
      { error: verify.message },
      { status: verify.status === 502 ? 502 : 422 }
    );
  }

  try {
    const app = await db.app.create({
      data: {
        appCode: body.app_code,
        appId: body.app_id,
        appKey: body.app_key,
        name: body.name,
        description: body.description ?? null,
        requireUserCode: body.require_user_code ?? true,
      },
      select: {
        id: true,
        appCode: true,
        appId: true,
        name: true,
        description: true,
        requireUserCode: true,
        status: true,
        createdAt: true,
      },
    });
    return json(app, { status: 201 });
  } catch (err) {
    const e = err as { code?: string };
    if (e.code === "P2002") {
      return json(
        { error: `app_code "${body.app_code}" already exists` },
        { status: 409 }
      );
    }
    throw err;
  }
}
