import { db } from "@/lib/db";
import { requirePermission } from "@/lib/admin-auth";
import { json } from "@/lib/utils";
import mysql from "mysql2/promise";

type Params = { params: Promise<{ code: string }> };

// POST /api/admin/services/{code}/test — 连通性测试
export async function POST(req: Request, { params }: Params) {
  const unauth = await requirePermission("services:manage", req);
  if (unauth) return unauth;
  const { code } = await params;
  const service = await db.service.findUnique({ where: { code } });
  if (!service || !service.credentialType) return json({ error: "Service or credential not found" }, { status: 404 });

  try {
    if (service.type === "http_proxy") {
      const cfg = service.config as { baseUrl: string };
      const res = await fetch(cfg.baseUrl, { method: "HEAD", signal: AbortSignal.timeout(5000) });
      return json({ ok: true, status: res.status });
    }
    if (service.type === "db_query") {
      const cfg = service.config as { host: string; port: number; database: string };
      const cred = service.credentialConfig as { username: string; password: string };
      const conn = await mysql.createConnection({
        host: cfg.host, port: cfg.port, database: cfg.database,
        user: cred.username, password: cred.password, connectTimeout: 5000,
      });
      await conn.query("SELECT 1");
      await conn.end();
      return json({ ok: true });
    }
    return json({ error: "Unknown service type" }, { status: 400 });
  } catch (err) {
    return json({ ok: false, error: String(err) }, { status: 200 });
  }
}
