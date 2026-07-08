import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Response.json 的替代，自动将 BigInt 序列化为 Number。
 * Prisma 对 BigInt 主键返回 bigint 类型，原生 JSON.stringify 会抛异常。
 */
export function json(data: unknown, init?: ResponseInit): Response {
  const body = JSON.stringify(data, (_, v) =>
    typeof v === "bigint" ? Number(v) : v
  );
  return new Response(body, {
    status: 200,
    ...init,
    headers: { "Content-Type": "application/json", ...(init?.headers) },
  });
}
