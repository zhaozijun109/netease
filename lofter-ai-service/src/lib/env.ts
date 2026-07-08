/**
 * 环境变量封装 - 提供类型安全的配置读取
 *
 * 通过 Next.js 原生 .env 加载机制，配置文件优先级：
 *   .env.local > .env.{NODE_ENV} > .env
 */

function getEnvVar(key: string, defaultValue?: string): string {
  const value = process.env[key] ?? defaultValue;
  if (value === undefined) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}

function getEnvVarOptional(key: string, defaultValue?: string): string | undefined {
  return process.env[key] ?? defaultValue;
}

/** 数据库配置 */
export const dbConfig = {
  get url() {
    return getEnvVar("DATABASE_URL");
  },
} as const;

/** AIGW 网关配置 */
export const aigwConfig = {
  get baseUrl() {
    return getEnvVar("AIGW_BASE_URL");
  },
  /** 上游请求超时时间（毫秒），LLM 调用默认 120 秒 */
  get timeoutMs() {
    return parseInt(getEnvVarOptional("AIGW_TIMEOUT_MS", "120000") ?? "120000", 10);
  },
  /** 解析逗号分隔的 app_codes 和对应的 keys */
  get apps(): Array<{ appCode: string; appKey: string }> {
    const codes = getEnvVarOptional("AIGW_APP_CODES", "");
    const keys = getEnvVarOptional("AIGW_APP_KEYS", "");
    if (!codes || !keys) return [];

    const codeList = codes.split(",").map((s) => s.trim()).filter(Boolean);
    const keyList = keys.split(",").map((s) => s.trim()).filter(Boolean);

    if (codeList.length !== keyList.length) {
      throw new Error(
        `AIGW_APP_CODES (${codeList.length}) and AIGW_APP_KEYS (${keyList.length}) must have the same number of entries`
      );
    }

    return codeList.map((appCode, i) => ({
      appCode,
      appKey: keyList[i],
    }));
  },
} as const;

/** CORS 配置 */
export const corsConfig = {
  /**
   * 允许的跨域来源，逗号分隔。
   * 设置为 * 允许所有来源；不设置则仅允许同源。
   */
  get allowedOrigins(): string[] {
    const raw = getEnvVarOptional("CORS_ALLOWED_ORIGINS", "*") ?? "*";
    return raw.split(",").map((s) => s.trim()).filter(Boolean);
  },
} as const;

/** Auth 配置 */
export const authConfig = {
  get secret() {
    return getEnvVar("NEXTAUTH_SECRET");
  },
  get url() {
    return getEnvVar("NEXTAUTH_URL", "http://localhost:3000");
  },
  /**
   * 初始管理员邮箱白名单（逗号分隔）。
   * 列在此处的邮箱首次登录时会被自动提升为 admin；
   * 后续通过管理后台授权的 admin 不受此变量影响。
   */
  get adminEmails(): string[] {
    const raw = getEnvVarOptional("ADMIN_EMAILS", "") ?? "";
    return raw.split(",").map((s) => s.trim()).filter(Boolean);
  },
} as const;

/** 网易 OpenID 配置 */
export const openidConfig = {
  /**
   * 允许登录的邮件后缀白名单，逗号分隔。
   * 例如：@corp.netease.com,@163.com
   * 留空则允许所有通过 OpenID 认证的用户
   */
  get allowedEmailSuffix() {
    return getEnvVarOptional("OPENID_ALLOWED_EMAIL_SUFFIX", "");
  },
  /**
   * 微前端主应用注入 Header 时使用的 RSA 公钥（PEM 格式）。
   * 用于校验 LOFTER-ADMIN-OPEN-ID-SIGN 签名。
   * 在 CI/CD 环境中可将换行符以 \n 字面量存储，读取时自动还原。
   * 设置后将优先走微前端 Header 认证流程。
   */
  get microFrontendPublicKey(): string | undefined {
    const raw = getEnvVarOptional("OPENID_MF_PUBLIC_KEY");

    if (!raw) return undefined;
    // 支持 CI/CD 环境中用 \n 字面量表示换行的公钥格式
    return raw.replace(/\\n/g, "\n");
  },
  /**
   * 微前端签名有效期（毫秒），超过此时长的请求将被拒绝。
   * 默认 5 分钟（300000ms）。
   */
  get microFrontendSignTimeoutMs(): number {
    return parseInt(
      getEnvVarOptional("OPENID_MF_SIGN_TIMEOUT_MS", "300000") ?? "300000",
      10
    );
  },
} as const;

/** 服务器配置 */
export const serverConfig = {
  get port() {
    return parseInt(getEnvVar("PORT", "3000"), 10);
  },
  get nodeEnv() {
    return getEnvVar("NODE_ENV", "development");
  },
  get isDev() {
    return this.nodeEnv === "development";
  },
  get isProd() {
    return this.nodeEnv === "production";
  },
  get serverUrl() {
    return getEnvVar("SERVER_URL");
  },
  get serverIntUrl() {
    return getEnvVar("SERVER_INT_URL");
  },
} as const;

/** 日志配置 */
export const logConfig = {
  get level() {
    return getEnvVarOptional("LOG_LEVEL", "info") as
      | "debug"
      | "info"
      | "warn"
      | "error";
  },
} as const;

/** 定时任务配置 */
export const cronConfig = {
  /**
   * Cron 鉴权密钥，调用 /api/cron/* 接口时需在 Authorization: Bearer 头中携带。
   * 生产环境必须设置，建议使用随机生成的长字符串。
   */
  get secret() {
    return getEnvVar("CRON_SECRET");
  },
  /**
   * 请求日志保留天数，超过此期限的记录将被定期清理。
   * 默认 90 天。
   */
  get logRetentionDays() {
    return parseInt(getEnvVarOptional("LOG_RETENTION_DAYS", "90") ?? "90", 10);
  },
} as const;

/** NOS 对象存储配置（大结果集 Excel 导出用） */
export const nosConfig = {
  get accessKey() { return getEnvVar("NOS_ACCESS_KEY"); },
  get accessSecret() { return getEnvVar("NOS_ACCESS_SECRET"); },
  get endpoint() { return getEnvVar("NOS_ENDPOINT"); },
  get bucket() { return getEnvVar("NOS_BUCKET", "lofter"); },
  get host() { return getEnvVar("NOS_HOST"); },
  get objectOrigin() { return getEnvVar("NOS_OBJECT_ORIGIN"); },
  /** Doris OUTFILE 用的 S3 兼容内网 endpoint(必须 Doris BE DNS 能解析) */
  get s3Endpoint() { return getEnvVarOptional("NOS_S3_ENDPOINT", "http://nos2-i.service.163.org") ?? "http://nos2-i.service.163.org"; },
} as const;

/** 所有配置的统一入口 */
export const env = {
  db: dbConfig,
  aigw: aigwConfig,
  auth: authConfig,
  openid: openidConfig,
  server: serverConfig,
  log: logConfig,
  cors: corsConfig,
  cron: cronConfig,
  nos: nosConfig,
} as const;
