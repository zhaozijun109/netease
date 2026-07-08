import crypto from "crypto";

const OPENID_SERVER = "https://login.netease.com/openid/";
const OPENID_IDENTITY_PREFIX = "https://login.netease.com/openid/";

// ── Cookie helpers ────────────────────────────────────────────────────────────

/**
 * 将 assoc 数据签名后序列化为 cookie 值
 * 格式：base64url(json).hmac-sha256-base64url
 */
export function signAssocForCookie(assoc: AssocData, secret: string): string {
  const payload = Buffer.from(
    JSON.stringify({ h: assoc.assocHandle, k: assoc.macKey, e: assoc.expiresAt })
  ).toString("base64url");

  const sig = crypto
    .createHmac("sha256", secret)
    .update(payload)
    .digest("base64url");

  return `${payload}.${sig}`;
}

/**
 * 解析并校验签名的 assoc cookie，返回 AssocData 或 null（签名无效/过期）
 */
export function parseAssocFromCookie(
  cookieValue: string,
  secret: string
): AssocData | null {
  const dotIdx = cookieValue.lastIndexOf(".");
  if (dotIdx === -1) return null;

  const payload = cookieValue.substring(0, dotIdx);
  const sig = cookieValue.substring(dotIdx + 1);

  const expectedSig = crypto
    .createHmac("sha256", secret)
    .update(payload)
    .digest("base64url");

  if (sig !== expectedSig) return null;

  try {
    const data = JSON.parse(Buffer.from(payload, "base64url").toString()) as {
      h: string;
      k: string;
      e: number;
    };
    const assoc: AssocData = {
      assocHandle: data.h,
      macKey: data.k,
      expiresAt: data.e,
    };
    // 已过期
    if (Date.now() >= assoc.expiresAt) return null;
    return assoc;
  } catch {
    return null;
  }
}

export interface AssocData {
  assocHandle: string;
  macKey: string;
  expiresAt: number;
}

export interface OpenIDUserInfo {
  identity: string;
  email: string;
  nickname: string;
  fullname: string;
}

/** 解析 OpenID 服务器返回的键值对格式文本（兼容 \r\n 行尾） */
function parseKeyValuePairs(text: string): Record<string, string> {
  const result: Record<string, string> = {};
  for (const line of text.split("\n")) {
    const idx = line.indexOf(":");
    if (idx > 0) {
      const key = line.substring(0, idx).trim();
      // 去掉行尾 \r，避免 Windows 换行符导致 assoc_handle / mac_key 带多余字符
      const value = line.substring(idx + 1).trimEnd();
      result[key] = value;
    }
  }
  return result;
}

/**
 * Step 1: 向 OpenID Server 发起关联请求，获取 assoc_handle 和 mac_key
 */
export async function fetchAssociation(): Promise<AssocData> {
  const body = new URLSearchParams({
    "openid.mode": "associate",
    "openid.assoc_type": "HMAC-SHA256",
    "openid.session_type": "no-encryption",
  });

  const res = await fetch(OPENID_SERVER, {
    method: "POST",
    body: body.toString(),
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });

  if (!res.ok) {
    throw new Error(`Association request failed with status: ${res.status}`);
  }

  const data = parseKeyValuePairs(await res.text());

  if (!data.assoc_handle || !data.mac_key || !data.expires_in) {
    throw new Error("Invalid association response from OpenID server");
  }

  return {
    assocHandle: data.assoc_handle,
    macKey: data.mac_key,
    expiresAt: Date.now() + parseInt(data.expires_in, 10) * 1000,
  };
}

/**
 * Step 2: 构造跳转至 OpenID Server 的认证 URL
 */
export function buildLoginUrl(params: {
  assocHandle: string;
  returnTo: string;
  realm: string;
}): string {
  const qs = new URLSearchParams({
    "openid.ns": "http://specs.openid.net/auth/2.0",
    "openid.mode": "checkid_setup",
    "openid.assoc_handle": params.assocHandle,
    "openid.return_to": params.returnTo,
    "openid.claimed_id": "http://specs.openid.net/auth/2.0/identifier_select",
    "openid.identity": "http://specs.openid.net/auth/2.0/identifier_select",
    "openid.realm": params.realm,
    "openid.ns.sreg": "http://openid.net/extensions/sreg/1.1",
    "openid.sreg.required": "nickname,email,fullname",
  });

  return `${OPENID_SERVER}?${qs.toString()}`;
}

/** 校验 openid.identity / openid.claimed_id 是否来自网易 OpenID */
function assertValidIdentity(value: string, fieldName: string): void {
  if (!value.startsWith(OPENID_IDENTITY_PREFIX)) {
    throw new Error(`Invalid ${fieldName}: ${value}`);
  }
}

/**
 * Step 3: 本地校验 OpenID 回调签名（HMAC-SHA256）
 * 使用关联阶段获取的 mac_key 进行验证，性能最优
 */
export function verifyOpenIDResponse(
  callbackParams: URLSearchParams,
  macKey: string
): OpenIDUserInfo {
  const mode = callbackParams.get("openid.mode");
  if (mode !== "id_res") {
    throw new Error(`Invalid openid.mode: ${mode}`);
  }

  const identity = callbackParams.get("openid.identity") ?? "";
  const claimedId = callbackParams.get("openid.claimed_id") ?? "";

  assertValidIdentity(identity, "openid.identity");
  assertValidIdentity(claimedId, "openid.claimed_id");

  const signed = callbackParams.get("openid.signed") ?? "";
  const signedKeys = signed.split(",");

  // 按文档要求，将签名字段组织为 "key:value\n" 格式（UTF-8, 使用 \n 换行）
  const message =
    signedKeys
      .map((key) => `${key}:${callbackParams.get(`openid.${key}`) ?? ""}`)
      .join("\n") + "\n";

  const macKeyBytes = Buffer.from(macKey, "base64");
  const digest = crypto
    .createHmac("sha256", macKeyBytes)
    .update(message, "utf8")
    .digest("base64");

  const expectedSig = callbackParams.get("openid.sig") ?? "";
  if (digest !== expectedSig) {
    throw new Error("OpenID signature verification failed");
  }

  return extractUserInfo(callbackParams);
}

/**
 * Step 4 (降级方案): 当 assoc_handle 不匹配时，向 OpenID Server 发起服务端校验
 */
export async function verifyWithServer(
  callbackParams: URLSearchParams
): Promise<OpenIDUserInfo> {
  const identity = callbackParams.get("openid.identity") ?? "";
  const claimedId = callbackParams.get("openid.claimed_id") ?? "";

  assertValidIdentity(identity, "openid.identity");
  assertValidIdentity(claimedId, "openid.claimed_id");

  const postParams = new URLSearchParams(callbackParams);
  postParams.set("openid.mode", "check_authentication");

  const res = await fetch(OPENID_SERVER, {
    method: "POST",
    body: postParams.toString(),
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });

  if (!res.ok) {
    throw new Error(`OpenID server verification failed with status: ${res.status}`);
  }

  const text = await res.text();
  const isValid = text.split("\n").some((line) => line.trim() === "is_valid:true");

  if (!isValid) {
    throw new Error("OpenID server returned is_valid:false");
  }

  return extractUserInfo(callbackParams);
}

function extractUserInfo(params: URLSearchParams): OpenIDUserInfo {
  return {
    identity: params.get("openid.identity") ?? "",
    email: params.get("openid.sreg.email") ?? "",
    nickname: params.get("openid.sreg.nickname") ?? "",
    fullname: params.get("openid.sreg.fullname") ?? "",
  };
}

// ── 微前端 Header 认证 ─────────────────────────────────────────────────────────

/**
 * 校验微前端主应用注入的 OpenID Header，返回用户信息。
 *
 * Header 说明：
 *   LOFTER-ADMIN-OPEN-ID      base64({"email","fullName","nickName"})
 *   LOFTER-ADMIN-OPEN-ID-SIGN base64({"alg","timestamp"}).rsa-sha256-signature
 *
 * 验证步骤：
 *   1. 拆分 sign header → signHeaderBase64 + signature
 *   2. 解码 signHeaderBase64，校验 timestamp 时效
 *   3. 用公钥校验 rsa-sha256 签名：sign(signHeaderBase64 + "." + openIdHeader)
 *   4. 解码 openIdHeader，提取用户信息
 *
 * @param openIdHeader      LOFTER-ADMIN-OPEN-ID header 的值
 * @param signHeader        LOFTER-ADMIN-OPEN-ID-SIGN header 的值
 * @param publicKey         RSA 公钥（PEM 格式）
 * @param signatureTimeoutMs 签名有效期（毫秒），默认 5 分钟
 */
export function verifyMicroFrontendHeaders(
  openIdHeader: string,
  signHeader: string,
  publicKey: string,
  signatureTimeoutMs: number = 5 * 60 * 1000
): OpenIDUserInfo {
  // 1. 拆分 sign header：{signHeaderBase64}.{signature}
  const dotIdx = signHeader.indexOf(".");
  if (dotIdx === -1) {
    throw new Error("LOFTER-ADMIN-OPEN-ID-SIGN 格式无效，缺少分隔符");
  }
  const signHeaderBase64 = signHeader.substring(0, dotIdx);
  const signature = signHeader.substring(dotIdx + 1);

  // 2. 解码 signHeaderBase64，校验时间戳
  let signHeaderData: { alg: string; timestamp: number };
  try {
    signHeaderData = JSON.parse(
      Buffer.from(signHeaderBase64, "base64").toString("utf8")
    ) as { alg: string; timestamp: number };
  } catch {
    throw new Error("LOFTER-ADMIN-OPEN-ID-SIGN signHeader 解码失败");
  }

  if (typeof signHeaderData.timestamp !== "number") {
    throw new Error("LOFTER-ADMIN-OPEN-ID-SIGN 缺少 timestamp 字段");
  }

  const age = Date.now() - signHeaderData.timestamp;
  if (age > signatureTimeoutMs) {
    throw new Error(
      `LOFTER-ADMIN-OPEN-ID-SIGN 签名已过期（age=${age}ms, limit=${signatureTimeoutMs}ms）`
    );
  }

  // 3. 用 RSA 公钥校验签名：签名内容为 `${signHeaderBase64}.${openIdHeader}`
  const verify = crypto.createVerify("SHA256");
  verify.write(`${signHeaderBase64}.${openIdHeader}`);
  verify.end();

  let isValid: boolean;
  try {
    isValid = verify.verify(publicKey, signature, "base64");
  } catch (e) {
    throw new Error(`RSA 签名校验异常：${e instanceof Error ? e.message : String(e)}`);
  }

  if (!isValid) {
    throw new Error("LOFTER-ADMIN-OPEN-ID-SIGN 签名校验失败");
  }

  // 4. 解码 openIdHeader，提取用户信息
  let mfUser: { email?: string; fullName?: string; nickName?: string };
  try {
    mfUser = JSON.parse(
      Buffer.from(openIdHeader, "base64").toString("utf8")
    ) as { email?: string; fullName?: string; nickName?: string };
  } catch {
    throw new Error("LOFTER-ADMIN-OPEN-ID header 解码失败");
  }

  if (!mfUser.email) {
    throw new Error("LOFTER-ADMIN-OPEN-ID 缺少 email 字段");
  }

  return {
    identity: mfUser.email,
    email: mfUser.email,
    nickname: mfUser.nickName ?? "",
    fullname: mfUser.fullName ?? "",
  };
}
