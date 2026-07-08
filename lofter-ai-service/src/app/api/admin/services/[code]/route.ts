import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";
import { invalidateTokenCache } from "@/lib/token-provider";

type Params = { params: Promise<{ code: string }> };

// GET /api/admin/services/{code}
export async function GET(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({ where: { code } });
  if (!service) return json({ error: "Not found" }, { status: 404 });

  // credentialConfig 脱敏：key/password/token 显示 ****
  let safeCredentialConfig: Record<string, unknown> | null = null;
  if (service.credentialConfig) {
    const cfg = service.credentialConfig as Record<string, unknown>;
    safeCredentialConfig = Object.fromEntries(
      Object.entries(cfg).map(([k, v]) =>
        ["key", "password", "token"].includes(k) ? [k, "****"] : [k, v]
      )
    );
  }

  return json({ ...service, credentialConfig: safeCredentialConfig });
}

// PUT /api/admin/services/{code}
export async function PUT(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const body = await req.json() as {
    name?: string; config?: unknown; status?: number;
    credentialType?: string; credentialConfig?: unknown;
  };

  const service = await db.service.findUnique({ where: { code } });
  if (!service) return json({ error: "Not found" }, { status: 404 });

  // 凭据 config 含脱敏占位符时拒绝保存，防止把 **** 写入 DB
  if (body.credentialConfig) {
    const hasRedacted = Object.values(body.credentialConfig as Record<string, unknown>)
      .some((v) => v === "****");
    if (hasRedacted) {
      return json(
        { error: "凭据包含脱敏占位符（****），请重新填写完整的凭据信息" },
        { status: 400 }
      );
    }
  }

  const updated = await db.service.update({
    where: { code },
    data: {
      name: body.name,
      config: body.config !== undefined ? (body.config as object) : undefined,
      status: body.status,
      credentialType: body.credentialType,
      credentialConfig: body.credentialConfig ? body.credentialConfig as object : undefined,
      // 凭据变更时清除 token 缓存
      ...(body.credentialConfig ? { cachedToken: null, cachedTokenExpiresAt: null } : {}),
    },
  });

  // 凭据有变更时清除内存缓存
  if (body.credentialConfig) {
    invalidateTokenCache(service.id);
  }

  return json(updated);
}

// DELETE /api/admin/services/{code}
export async function DELETE(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  await db.service.delete({ where: { code } });
  return json({ ok: true });
}
