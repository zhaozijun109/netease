import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { serverConfig } from "@/lib/env";

interface ApiItem {
  name: string;
  method: "GET" | "POST" | "PATCH" | "DELETE";
  path: string;
  description: string;
}

interface ApiGroup {
  title: string;
  description?: string;
  apis: ApiItem[];
}

const API_GROUPS: ApiGroup[] = [
  {
    title: "核心模型接口",
    description: "OpenAI 兼容格式，适用于所有 LLM/多模态/生图/语音场景",
    apis: [
      { name: "对话补全", method: "POST", path: "/v1/chat/completions", description: "所有 LLM 对话，支持流式 / 非流式，兼容全部模型供应商" },
      { name: "嵌入向量", method: "POST", path: "/v1/embeddings", description: "文本 Embedding，支持字符串、字符串数组、整数数组" },
      { name: "嵌入向量（旧路径）", method: "POST", path: "/v1/engines//embeddings", description: "兼容旧版 OpenAI 格式" },
      { name: "文生图", method: "POST", path: "/v1/images/generations", description: "支持 dall-e-3、gpt-image-1" },
      { name: "图片编辑", method: "POST", path: "/v1/images/edits", description: "仅支持 gpt-image-1，支持单图/多图/带 Mask" },
      { name: "文本转语音", method: "POST", path: "/v1/audio/speech", description: "支持 tts-1-hd，可输出 mp3 等格式" },
      { name: "模型列表", method: "GET", path: "/v1/models", description: "获取全部可用模型列表" },
      { name: "模型详情", method: "GET", path: "/v1/models/{model}", description: "获取指定模型的详情信息" },
    ],
  },
  {
    title: "视频接口",
    description: "基于 Sora 2（OpenAI）的视频生成，当前为 Preview 阶段",
    apis: [
      { name: "创建视频", method: "POST", path: "/v1/videos", description: "文生视频（sora-2），支持 4 / 8 / 12 秒，720p 横/竖屏" },
      { name: "查询视频状态", method: "GET", path: "/v1/videos/{video_id}", description: "轮询任务状态（pending / completed / failed）" },
      { name: "下载视频内容", method: "GET", path: "/v1/videos/{video_id}/content", description: "下载生成的 MP4 文件，视频 24 小时后过期" },
    ],
  },
  {
    title: "文件接口",
    description: "OpenAI 格式兼容，支持上传/查询/下载/删除，文件七天后自动过期",
    apis: [
      { name: "上传文件", method: "POST", path: "/v1/files", description: "支持 user_data（512MB）、batch、voice_clone（20MB）三种用途" },
      { name: "列出文件", method: "GET", path: "/v1/files?after={file_id}&limit=1&order=asc", description: "分页获取文件列表" },
      { name: "获取文件信息", method: "GET", path: "/v1/files/{file_id}", description: "获取指定文件的元信息" },
      { name: "下载文件内容", method: "GET", path: "/v1/files/{file_id}/content", description: "下载文件原始内容" },
      { name: "删除文件", method: "DELETE", path: "/v1/files/{file_id}", description: "删除指定文件" },
    ],
  },
  {
    title: "批量任务接口",
    description: "OpenAI Batch 格式兼容，成本为普通请求的一半，24 小时内完成",
    apis: [
      { name: "创建批量任务", method: "POST", path: "/v1/batches", description: "基于已上传的 jsonl 文件创建 Batch 任务" },
      { name: "获取任务信息", method: "GET", path: "/v1/batches/{batch_id}", description: "获取单个 Batch 任务的状态和进度" },
      { name: "列出任务", method: "GET", path: "/v1/batches?after={id}&limit=10", description: "分页获取 Batch 任务列表" },
      { name: "取消任务", method: "POST", path: "/v1/batches/{batch_id}/cancel", description: "取消指定的 Batch 任务" },
    ],
  },
];

const METHOD_COLOR: Record<string, string> = {
  GET: "text-blue-600 bg-blue-50 border-blue-200",
  POST: "text-green-600 bg-green-50 border-green-200",
  PATCH: "text-amber-600 bg-amber-50 border-amber-200",
  DELETE: "text-red-600 bg-red-50 border-red-200",
};

export default function AigwApisPage() {
  const totalApis = API_GROUPS.reduce((sum, g) => sum + g.apis.length, 0);

  return (
    <div className="space-y-8">
      {/* 页头 */}
      <div>
        <h1 className="text-2xl font-bold">AIGW API 接口文档</h1>
        <p className="text-sm text-muted-foreground mt-1">
          大模型统一接入服务接口汇总 · 共{" "}
          <span className="font-medium text-foreground">{API_GROUPS.length}</span> 个分组 ·{" "}
          <span className="font-medium text-foreground">{totalApis}</span> 个接口
        </p>
      </div>

      {/* 端点说明 */}
      <div className="rounded-lg border bg-muted/40 px-4 py-3 text-sm space-y-1">
        <p className="font-medium">服务端点</p>
        <div className="flex flex-wrap gap-x-6 gap-y-1 text-muted-foreground">
          <span>
            外网：
            <code className="ml-1 text-foreground font-mono text-xs">{serverConfig.serverIntUrl}</code>
          </span>
          <span>
            办公网：
            <code className="ml-1 text-foreground font-mono text-xs">{serverConfig.serverUrl}</code>
          </span>
        </div>
      </div>

      {/* 各分组 */}
      {API_GROUPS.map((group) => (
        <section key={group.title} className="space-y-2">
          <div className="flex items-baseline gap-3">
            <h2 className="text-base font-semibold">{group.title}</h2>
            <Badge variant="outline" className="text-xs font-normal">
              {group.apis.length} 个接口
            </Badge>
          </div>
          {group.description && (
            <p className="text-sm text-muted-foreground">{group.description}</p>
          )}
          <div className="rounded-md border overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-muted/50">
                  <TableHead className="w-[140px]">接口名称</TableHead>
                  <TableHead className="w-[72px]">方法</TableHead>
                  <TableHead className="w-[380px]">路径</TableHead>
                  <TableHead>说明</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {group.apis.map((api) => (
                  <TableRow key={api.path + api.method}>
                    <TableCell className="text-sm font-medium">{api.name}</TableCell>
                    <TableCell>
                      <span
                        className={`inline-flex items-center rounded border px-1.5 py-0.5 text-xs font-mono font-semibold ${METHOD_COLOR[api.method]}`}
                      >
                        {api.method}
                      </span>
                    </TableCell>
                    <TableCell className="font-mono text-xs text-muted-foreground break-all">
                      {api.path}
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {api.description}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </section>
      ))}
    </div>
  );
}
