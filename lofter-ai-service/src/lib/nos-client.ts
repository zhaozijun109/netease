import { nosConfig } from "@/lib/env";
import { logger } from "@/lib/logger";

// eslint-disable-next-line @typescript-eslint/no-require-imports
const { NosClient } = require("@nos-sdk/nos-node-sdk") as {
  NosClient: new (opts: Record<string, unknown>) => {
    putObject(opts: { objectKey: string; body: Buffer; length: number }): Promise<void>;
  };
};

let _client: InstanceType<typeof NosClient> | null = null;

function getClient() {
  if (!_client) {
    _client = new NosClient({
      accessKey: nosConfig.accessKey,
      accessSecret: nosConfig.accessSecret,
      endpoint: nosConfig.endpoint,
      host: nosConfig.host,
      defaultBucket: nosConfig.bucket,
      protocol: "https",
    });
  }
  return _client;
}

/**
 * 上传 Buffer 到 NOS，返回公开 CDN URL
 */
export async function uploadToNos(buffer: Buffer, objectKey: string): Promise<string> {
  const client = getClient();
  logger.debug({ objectKey, size: buffer.length }, "NOS 开始上传");
  try {
    await client.putObject({ objectKey, body: buffer, length: buffer.length });
    const url = `${nosConfig.objectOrigin}/${objectKey}`;
    logger.info({ objectKey, url }, "NOS 上传成功");
    return url;
  } catch (err) {
    logger.error({ err, objectKey }, "NOS 上传失败");
    throw err;
  }
}
