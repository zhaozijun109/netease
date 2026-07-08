import pino from "pino";

const level = process.env.LOG_LEVEL ?? (process.env.NODE_ENV === "development" ? "debug" : "info");

/**
 * Pino 日志单例
 * - 开发环境：使用 pino-pretty 格式化输出
 * - 生产环境：输出 JSON，便于日志采集
 */
export const logger = pino({
  level,
  ...(process.env.NODE_ENV === "development"
    ? {
        transport: {
          target: "pino-pretty",
          options: {
            colorize: true,
            translateTime: "SYS:yyyy-mm-dd HH:MM:ss.l",
            ignore: "pid,hostname",
          },
        },
      }
    : {}),
});
