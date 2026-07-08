import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";

// GET /api/admin/services — 服务列表
export async function GET(req: Request) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;

  const services = await db.service.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true, code: true, name: true, type: true,
      credentialType: true, status: true, createdAt: true,
    },
  });
  return json(services);
}

// POST /api/admin/services — 创建服务
export async function POST(req: Request) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;

  const body = await req.json() as {
    code?: string; name?: string; type?: string; config?: unknown;
    credentialType?: string; credentialConfig?: unknown;
  };
  if (!body.code || !body.name || !body.type || !body.config) {
    return json({ error: "code, name, type, config are required" }, { status: 400 });
  }
  if (!["http_proxy", "db_query", "static_content"].includes(body.type)) {
    return json({ error: 'type must be "http_proxy", "db_query" or "static_content"' }, { status: 400 });
  }

  const exists = await db.service.findUnique({ where: { code: body.code } });
  if (exists) return json({ error: `Service code "${body.code}" already exists` }, { status: 409 });

  const service = await db.service.create({
    data: {
      code: body.code,
      name: body.name,
      type: body.type,
      config: body.config as object,
      credentialType: body.credentialType,
      credentialConfig: body.credentialConfig ? body.credentialConfig as object : undefined,
    },
  });
  return json(service, { status: 201 });
}
