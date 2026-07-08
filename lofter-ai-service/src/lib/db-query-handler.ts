import { randomUUID } from "crypto";
import mysql from "mysql2/promise";
import { logger } from "@/lib/logger";
import { nosConfig } from "@/lib/env";
import type { Service } from "@/generated/prisma";

type DbQueryConfig = {
  host: string;
  port: number;
  database: string;
  queryTimeout?: number;
  maxTimeout?: number;
  /** inline 模式硬上限：行数超过则 413,要求改用 mode=file（默认 200000）*/
  maxQueryRows?: number;
};
type DbPasswordConfig = { username: string; password: string };

// 连接池缓存，按 serviceId
const pools = new Map<bigint, mysql.Pool>();

function getPool(serviceId: bigint, cfg: DbQueryConfig, cred: DbPasswordConfig): mysql.Pool {
  if (!pools.has(serviceId)) {
    pools.set(serviceId, mysql.createPool({
      host: cfg.host,
      port: cfg.port,
      database: cfg.database,
      user: cred.username,
      password: cred.password,
      connectionLimit: 5,
      connectTimeout: 10000,
    }));
  }
  return pools.get(serviceId)!;
}

// 匹配只读查询：SELECT 开头，或 WITH...SELECT 的 CTE，忽略前置注释和大小写
const SELECT_RE = /^\s*(\/\*[\s\S]*?\*\/\s*)*\s*(WITH\s|SELECT\s)/i;

export type DbQueryResult =
  | { type: "inline"; columns: string[]; rows: unknown[][]; rowCount: number }
  | { type: "file"; url: string; rowCount: number; fileSize: number; format: "csv" };

/**
 * 流式执行 SELECT,边收边数,行数超过 maxRows 时主动中止连接以防 OOM
 * 通过 PoolConnection 的底层 callback Connection 拿 .stream() —— promise 接口不暴露 stream
 */
function streamQuery(
  poolConn: mysql.PoolConnection,
  sql: string,
  maxRows: number,
): Promise<{ rows: unknown[][]; fields: mysql.FieldPacket[]; aborted: boolean }> {
  return new Promise((resolve, reject) => {
    // promise.d.ts 上 PoolConnection.connection 声明为 Connection,但运行时是 callback 版 Connection
    // mysql2 的 callback Connection.query() 返回带 .stream() 的 Query 对象,promise 版不暴露
    const rawConn = (poolConn as unknown as { connection: { query: (opts: object) => unknown; destroy: () => void } }).connection;
    const queryObj = rawConn.query({ sql, rowsAsArray: true }) as unknown as {
      stream: () => NodeJS.ReadableStream;
      on: (event: string, listener: (...args: unknown[]) => void) => unknown;
    };

    const rows: unknown[][] = [];
    let fields: mysql.FieldPacket[] = [];
    let aborted = false;
    let settled = false;

    // 字段事件从 query 对象本身发出,不是从 stream
    queryObj.on("fields", (...args: unknown[]) => {
      fields = args[0] as mysql.FieldPacket[];
    });

    const stream = queryObj.stream();

    stream.on("data", (row: unknown) => {
      if (aborted) return;
      rows.push(row as unknown[]);
      if (rows.length > maxRows) {
        aborted = true;
        // 销毁底层连接强制中断查询。连接池会丢弃这个连接,不会被复用
        try { rawConn.destroy(); } catch { /* ignore */ }
        if (!settled) {
          settled = true;
          resolve({ rows, fields, aborted: true });
        }
      }
    });

    stream.on("end", () => {
      if (settled) return;
      settled = true;
      resolve({ rows, fields, aborted });
    });

    stream.on("error", (err: Error) => {
      if (settled) return;
      // 如果是因为我们主动 destroy 触发的 error,当作正常 abort 处理
      if (aborted) {
        settled = true;
        resolve({ rows, fields, aborted: true });
        return;
      }
      settled = true;
      reject(err);
    });
  });
}

/**
 * 通过 SELECT INTO OUTFILE 让 Doris BE 直接把结果写到 NOS。
 * 强制单文件输出（max_file_size=10240MB，远超任何合理结果集）。
 * 返回可公开访问的 NOS URL + 行数 + 文件字节数。
 */
async function executeOutfile(
  conn: mysql.PoolConnection,
  sql: string,
  log: typeof logger,
): Promise<{ url: string; rowCount: number; fileSize: number }> {
  const date = new Date().toISOString().slice(0, 10);
  // 给每次查询带 uuid 前缀, 便于 NOS 上识别和清理。Doris 实际产物 <prefix><doris内部runId>_0.csv
  const objectKeyPrefix = `ai-service/query-results/${date}/${randomUUID()}_`;
  const outfilePath = `s3://${nosConfig.bucket}/${objectKeyPrefix}`;

  // 凭据明文进 SQL —— 会进 Doris audit log。和 mcp-doris 同等暴露面
  const propsSql = [
    `"s3.endpoint" = "${nosConfig.s3Endpoint}"`,
    `"s3.access_key" = "${nosConfig.accessKey}"`,
    `"s3.secret_key" = "${nosConfig.accessSecret}"`,
    `"s3.region" = "1"`,
    `"column_separator" = ","`,
    `"max_file_size" = "10240MB"`,
    `"use_path_style" = "true"`,
  ].join(",\n  ");

  const innerSql = sql.trim().replace(/;\s*$/, "");
  const outfileSql = `${innerSql}\nINTO OUTFILE "${outfilePath}"\nFORMAT AS CSV_WITH_NAMES\nPROPERTIES(\n  ${propsSql}\n)`;

  // 日志脱敏:secret 不进 log
  log.info({ outfilePath }, "OUTFILE 开始执行");

  const [rows] = await conn.query(outfileSql) as [
    Array<{ FileNumber: number; TotalRows: number; FileSize: number; URL: string }>,
    mysql.FieldPacket[],
  ];

  if (!rows || rows.length === 0) {
    throw new Error("OUTFILE 没有返回结果元数据");
  }
  const meta = rows[0];
  if (meta.FileNumber !== 1) {
    throw new Error(`OUTFILE 产出 ${meta.FileNumber} 个分片(预期 1)。结果集可能超过 max_file_size 10GB`);
  }

  // Doris 返回的 URL 形如 's3://lofter/ai-service/query-results/2026-05-15/<runId>_*'
  // 单文件场景下实际文件是 <prefix><runId>_0.csv
  const urlSuffix = meta.URL.replace(/\*$/, "0.csv").replace(/^s3:\/\/[^/]+\//, "");
  const publicUrl = `${nosConfig.objectOrigin}/${urlSuffix}`;

  log.info(
    { url: publicUrl, rowCount: meta.TotalRows, fileSize: meta.FileSize },
    "OUTFILE 完成,文件已可公开下载",
  );

  return { url: publicUrl, rowCount: meta.TotalRows, fileSize: meta.FileSize };
}

/**
 * 执行 SQL 查询。mode 控制结果形态:
 *  - "inline" / "auto" / 不传：内存读取,行数 ≤ maxQueryRows 走 inline,超过则 413
 *  - "file": 走 SELECT INTO OUTFILE → NOS, 返回可公开下载的 csv URL
 */
export async function handleDbQuery(
  req: Request,
  service: Service
): Promise<Response> {
  const body = await req.json() as { sql?: string; database?: string; mode?: string };

  if (!body.sql) {
    return Response.json({ error: "sql is required" }, { status: 400 });
  }
  if (!SELECT_RE.test(body.sql)) {
    return Response.json({ error: "只允许 SELECT 查询" }, { status: 400 });
  }

  const cfg = service.config as DbQueryConfig;
  const cred = service.credentialConfig as Record<string, unknown> as DbPasswordConfig;
  const log = logger.child({ service: service.code, host: cfg.host, port: cfg.port, database: cfg.database });

  const sqlPreview = body.sql.length > 500 ? body.sql.slice(0, 500) + "..." : body.sql;
  const mode = body.mode === "file" ? "file" : "inline";  // 默认 inline,只有显式 "file" 才走 OUTFILE
  log.info({ sql: sqlPreview, requestDatabase: body.database || undefined, mode }, "db query 收到请求");

  const maxQueryRows = cfg.maxQueryRows ?? 200000;
  const defaultTimeout = cfg.queryTimeout ?? 60000;
  const maxTimeout = cfg.maxTimeout ?? 300000;
  const requestedTimeout = parseInt(req.headers.get("X-Query-Timeout") ?? "0", 10);
  const timeoutMs =
    Number.isFinite(requestedTimeout) && requestedTimeout > 0
      ? Math.min(requestedTimeout, maxTimeout)
      : defaultTimeout;

  const pool = getPool(service.id, cfg, cred);
  const conn = await pool.getConnection();

  try {
    const targetDb = body.database ?? cfg.database;
    if (targetDb) await conn.query(`USE \`${targetDb}\``);
    await conn.query(`SET SESSION MAX_EXECUTION_TIME=${timeoutMs}`);

    // ── mode=file 路径:直接 OUTFILE → NOS ──
    if (mode === "file") {
      const queryStart = Date.now();
      try {
        const { url, rowCount, fileSize } = await executeOutfile(conn, body.sql, log);
        log.info({ rowCount, fileSize, latencyMs: Date.now() - queryStart }, "OUTFILE 路径完成");
        const result: DbQueryResult = { type: "file", url, rowCount, fileSize, format: "csv" };
        return Response.json(result);
      } catch (err) {
        const msg = err instanceof Error ? err.message : String(err);
        log.error({ err: msg, latencyMs: Date.now() - queryStart }, "OUTFILE 路径失败");
        return Response.json(
          { error: `OUTFILE 导出失败: ${msg}` },
          { status: 500 },
        );
      }
    }

    // ── mode=inline 路径:流式接收,边收边数,超过 maxQueryRows 主动 abort ──
    log.debug({ timeoutMs, targetDb, maxQueryRows }, "inline 路径开始执行");
    const queryStart = Date.now();
    const { rows: allRows, fields, aborted } = await streamQuery(conn, body.sql, maxQueryRows);
    const queryLatencyMs = Date.now() - queryStart;

    if (aborted) {
      log.warn({ rowsReceived: allRows.length, maxQueryRows }, "inline 路径超过硬上限,主动中止");
      return Response.json(
        {
          error: `查询结果超过 ${maxQueryRows} 行(已接收 ${allRows.length} 行后主动中止)。请改用 mode="file" 导出文件,或增加 WHERE/LIMIT 缩小范围`,
          rowsReceived: allRows.length,
          maxQueryRows,
          hint: "switch to mode=file",
        },
        { status: 413 },
      );
    }

    const columns = fields.map((f: mysql.FieldPacket) => f.name);
    log.info({ rowCount: allRows.length, queryLatencyMs }, "inline 路径完成");
    const result: DbQueryResult = {
      type: "inline",
      columns,
      rows: allRows,
      rowCount: allRows.length,
    };
    return Response.json(result);
  } finally {
    conn.release();
  }
}
