# 视频域数据 Spec

> **业务域**：视频功能 (Video) · 用户体验指标搭建
> **责任表**：`lofter.dim_video_dd` · `lofter.dwd_video_*` (6) · `lofter.dws_video_*` (5) · `lofter_dm.ads_growth_video_*` (4)
> **调度**：Azkaban，日级 T-1 调度，使用 `${azkaban.flow.1.days.ago}` 参数
> **建表**：`etl/docs/video/ddl.sql`
> **字段权威源**：
> - 播放器指标 v2 (doc `6c9acd00fa2b48399051c246735e53ff`)
> - 发布和草稿埋点方案 (doc `099fda4cb2a84023b9861154657d4d73`)
> - 2026 版本埋点记录 (excel `05a13a69d7354c52b82348768f3d69e4`，封面编辑 c1-3* 字段待此 excel 同步)

---

## 1. 业务背景

LOFTER 视频功能用户体验改造（含动态分辨率、上传保活、倍速、封面编辑、iCloud 下载），引入大量 m7-* 和 c10-* 客户端埋点。视频域数仓建设目的：

- **质量监控**：卡顿率 / 首帧耗时 (TTFP) / 各分辨率分布 / 播放失败率
- **业务观测**：人均观看时长 / 完播率 / 倍速渗透率 / 互动率
- **发布漏斗**：上传成功率 / 失败原因分类 / 后台/断点续传成功率
- **创作者画像**：视频粒度 + 创作者粒度的曝光/播放/完播/互动

### 数据流向

```
┌────────────────────────────────────────────────────────────────────────┐
│  ODS 原始层                                                             │
│    lofter.ods_mda_app_di                                                │
│      ├─ m7-5/6/7/8/9             (视频播放质量埋点，俞忻峰@Android)      │
│      ├─ c10-1/5/10/15/30/31/32   (发布漏斗埋点)                         │
│      ├─ c1-31/32/33              (封面编辑埋点，杨正灿)                  │
│      └─ m7-18                    (iCloud 下载埋点，iOS only)            │
│    lofter_db_dump.ods_db_video_post_nd                                  │
│      └─ videoType + embed JSON (视频物理属性的权威源)                    │
│    lofter.dwd_post_browse_di                                            │
│      └─ page_view / page_duration / is_real / is_video_finish           │
│    lofter.dwd_post_publish_di / dwd_post_hot_di / *_response_di / *_share_di │
│                              │                                         │
│                              ▼                                         │
│  DIM 层 (1 张)                                                          │
│    lofter.dim_video_dd                                                     │
│      = dim_post(视频帖) LEFT JOIN ods_db_video_post_nd + embed JSON 解析│
│                              │                                         │
│                              ▼                                         │
│  DWD 层 (6 张)                                                          │
│    dwd_video_play_di              (m7-5/6/7/8/9 按 reqId 聚合)          │
│    dwd_video_quality_event_di     (m7-9 切换事件明细)                   │
│    dwd_video_publish_di           (dwd_post_publish_di + dim_video_dd)     │
│    dwd_video_publish_funnel_di    (c10-* 按 reqId 聚合漏斗)             │
│    dwd_video_cover_edit_di        (c1-31/32/33 设备粒度, 无 postId 关联)│
│    dwd_video_icloud_download_di   (m7-18, iOS)                          │
│                              │                                         │
│                              ▼                                         │
│  DWS 层 (5 张) — 只输出因子, 不直接出"率"字段                            │
│    dws_video_post_dd              (视频粒度全量汇总)                    │
│    dws_video_quality_dd           (按 deviceOs/tier/version/quality)    │
│    dws_video_user_consume_di      (用户视频消费)                        │
│    dws_video_creator_dd           (创作者视频画像)                      │
│    dws_video_speedrate_dd         (按 1.0/1.25/1.5/2.0x 真实档位)       │
│                              │                                         │
│                              ▼                                         │
│  ADS 层 (4 张, 新增) — 同 DWS 规则, 只透传因子                          │
│    ads_growth_video_quality_monitor_di   (质量监控大盘 P0)                     │
│    ads_growth_video_publish_funnel_di    (发布漏斗大盘)                        │
│    ads_growth_video_cover_edit_stat_di   (封面编辑设备粒度统计因子)│
│    ads_growth_video_icloud_download_funnel_di (iCloud 漏斗)                    │
│                              │                                         │
│                              ▼                                         │
│      [BI 报表 / 算法降级决策 / 灰度发布监控 / 创作者引导]               │
└────────────────────────────────────────────────────────────────────────┘
```

### 设计原则（与现网规范对齐）

1. **DWS/ADS 只输出因子（pv/uv/cnt/time_ms）**，所有"率"在下游 BI/API 自行组合。详见第 7 章。
2. **建表 DDL 不指定 LOCATION**（使用 Hive 默认路径映射）。
3. **三层 COMMENT 全覆盖**：表级 / 字段级 / 分区字段级。
4. **存储 Parquet（Hive ParquetSerDe）**，与现网视频域邻近表一致。
5. **日期参数统一**：`${azkaban.flow.1.days.ago}`，禁止硬编码。
6. **SQL 编码规范**（强制）：
   - **谓词下推**：dt 分区过滤、业务过滤（如 `contentType='视频'` / `is_real=1`）必须放在子查询/CTE 的 WHERE 中，让 Spark 在 scan 阶段就过滤
   - **ON 仅等值关联**：`ON a.id = b.id` 或复合等值键 `ON a.id=b.id AND a.dt=b.dt`，**禁止**在 ON 中带分区/业务过滤条件
   - **列裁剪**：子查询/CTE 内只 select 真正用到的列，减少 shuffle
   - **JOIN 类型选择**：维度补充用 `LEFT JOIN`（保左表全量）；强一致关系用 `INNER JOIN`（写作 `JOIN`）；**禁止 `FULL OUTER JOIN`**（SparkSQL 不友好且可读性差）；范围关联用 `JOIN + WHERE BETWEEN`

---

## 2. ODS 输入源

| 表 / 事件 | 用途 | 关键字段（仅列本域使用） |
|----------|------|-------------------------|
| `lofter.ods_mda_app_di` `eventId='m7-5'` | 播放停止时上报（累计播放数据） | `params[reqId]` (String 会话ID) · `params[postId]` · `params[playedTime]` (Long ms) · `params[bufferTime]` (Long ms) · `params[duration]` (Long ms 视频总长) · `params[maxProgress]` (Float 0~1) · `params[playRate]` (Float, 1.0/1.25/1.5/2.0…) · `params[interruptReason]` (Map JSON, 5 个 key: userSeek/buffer/system/inflateError/timer) · `params[quality]` (String "宽x高", 如 "720x1280") · `params[currentrate]` · `params[playerType]` · `params[trackerScene]` · `params[algInfo]` · `params[recId]` · `params[itemPosition]` · `params[isAdvertise]` |
| `lofter.ods_mda_app_di` `eventId='m7-6'` | 用户播放行为 | `params[reqId]` · `params[postId]` · `params[action]` (start/resume/pause/stop/seek) · `params[createtime]` · `params[quality]` |
| `lofter.ods_mda_app_di` `eventId='m7-7'` | 播放器状态回调 | `params[reqId]` · `params[postId]` · `params[createtime]` · `params[state]` (1=prepare完成 / 2=首帧渲染 / 3=首次buffer) |
| `lofter.ods_mda_app_di` `eventId='m7-8'` | 播放器错误 | `params[reqId]` · `params[postId]` · `params[errCode]` (Int) · `params[errMsg]` · `params[errType]` (network/decode/source/unknown) · `params[retryCount]` |
| `lofter.ods_mda_app_di` `eventId='m7-9'` | 清晰度切换 | `params[reqId]` · `params[postId]` · `params[createtime]` · `params[type]` (init/manual/auto) · `params[fromQuality]` · `params[toQuality]` · `params[currentPositionMs]` · `params[durationInFromQualityMs]` · `params[estimatedBandwidthKbps]` · `params[bufferDurationMs]` |
| `lofter.ods_mda_app_di` `eventId in ('c10-1','c10-5','c10-10','c10-15')` | 发布主漏斗 | `params[reqId]` · `params[itemId]` · `itemType` (顶层列, 'VIDEO' 或 '4') · `params[scene]` ('release_page') · `params[type]` (postNew/postEdit/postReblog, c10-1 中) |
| `lofter.ods_mda_app_di` `eventId in ('c10-30','c10-31','c10-32')` | 发布异常 | `params[reqId]` · `params[code]` 顶层 · `get_json_object(params[ext], '$.msg')` · `get_json_object(params[ext], '$.code')` 或 `$.errorCode` |
| `lofter.ods_mda_app_di` `eventId in ('c1-31','c1-32','c1-33')` | 封面编辑 | `userId` 顶层 · `ext.width / ext.height`（仅 c1-31）· `optType`（仅 c1-32, 顶层）· **`itemId` 全部为 NULL**, 无法关联到 postId |
| `lofter.ods_mda_app_di` `eventId='m7-18'` | iCloud 下载（仅 iOS） | `get_json_object(params[ext], '$.type')` (1=主动取消失败 / 0=下载失败) |
| `lofter_db_dump.ods_db_video_post_nd` | 视频帖业务库快照 | `id` (=postId) · `videoType` (3=站内原生/非3=站外) · `caption` · `embed` (JSON, 含 originUrl/hlsUrl/h265Url/duration/size/img_width/img_height/vid/video_first_img/video_img_url 等) |
| `lofter.dim_post` | 文章公共维度 | `id` · `userId` · `blogId` · `blogName` · `title` · `contentType` (='视频') · `publishDate` · `publishTime` · `tags` · `ips` · `domains` · `isPublished` · `isForbidden` · `allowView` · `valid` · `isImported` · `ImportPlatformType` · `moveFrom` · `url` |
| `lofter.dwd_post_browse_di` | 浏览/播放明细 | `postId` · `action_type` (page_view/page_duration) · `is_real` (视频 >5000ms) · `is_video_finish` (progress>=0.9) · `duration` (停留时长 ms) · `post_content_type` (='视频') · `deviceUdid` · `userId` |
| `lofter.dwd_post_publish_di` | 发布明细 | `postId` · `userId` · `post_content_type` · `post_publish_date` · `post_publish_time` · `platform` · `is_user_first_post` · `is_pay_gift` |
| `lofter.dwd_post_hot_di` | 互动（点赞/转载/推荐/订阅） | `postId` · `userId` · `opType` (praise/reproduce/recommend/subscribe) |
| `lofter.dwd_post_response_di` · `lofter.dwd_post_share_di` | 评论 / 分享 | `postId` · `userId` |

---

## 3. DIM 层 — `lofter.dim_video_dd`

### 3.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter.dim_video_dd` |
| 后缀 | `_dd` 日全量快照, 每天一份分区 |
| 粒度 | postId (与 dim_post 1:1, contentType='视频') |
| 上游 | `lofter.dim_post` + `lofter_db_dump.ods_db_video_post_nd` |
| 任务 | `etl/jobs/dimensions/dim_video_dd.job` |
| 依赖 | `dim_post` |

### 3.2 字段与说明

| 字段 | 类型 | 说明 | 来源 |
|------|------|------|------|
| postId | BIGINT | 主键, = dim_post.id | dim_post.id |
| userId / blogId | BIGINT | 发布者 / 创作者 | dim_post |
| blog_name / blog_nickname / post_title | STRING | 博客名 / 昵称 / 标题 | dim_post |
| publish_date | STRING | yyyy-MM-dd | dim_post.publishDate |
| publish_time | BIGINT | ms | dim_post.publishTime |
| post_tags / post_ips / post_domains | ARRAY | | dim_post |
| is_imported / import_platform_type | INT / STRING | | dim_post |
| allow_view / valid / is_published / is_forbidden | | 状态四要素 | dim_post |
| post_url | STRING | | dim_post |
| movefrom | STRING | ios/android/web 规整 | dim_post.moveFrom + CASE WHEN 规整 |
| caption | STRING | 富文本 | ods_db_video_post_nd.caption |
| video_type | INT | 3=站内原生 / 非3=站外 | ods_db_video_post_nd.videoType |
| origin_url / hls_url / h265_url / flash_url / video_down_url | STRING | 各类视频 URL | embed JSON 解析 |
| video_first_img / video_img_url | STRING | 封面 | embed JSON |
| vid | BIGINT | 视频中台 vid | embed JSON |
| **duration_sec** | BIGINT | **单位秒**, 与 dwd_video_play_di.duration_ms（毫秒）单位不同 | embed JSON.duration |
| **size_bytes** | BIGINT | **单位字节**, 不是 MB | embed JSON.size |
| img_width / img_height | INT | 像素 | embed JSON |
| aspect_ratio | STRING | landscape / portrait / square / unknown | 派生 |
| embed_type | STRING | 如 uservideo | embed JSON.type |

### 3.3 核心 SQL

> **设计原则**：dim_post 视频帖为左表 LEFT JOIN ods_db_video_post_nd，确保所有视频帖都能进入 dim_video_dd（缺 embed 数据时物理属性字段为 NULL，由 DQC 监控）。谓词下推到子查询，ON 仅等值。

```sql
INSERT OVERWRITE TABLE lofter.dim_video_dd PARTITION(dt='${azkaban.flow.1.days.ago}')
SELECT
    p.postId, p.userId, p.blogId, p.blog_name, p.blog_nickname, p.post_title,
    p.publish_date, p.publish_time, p.post_tags, p.post_ips, p.post_domains,
    p.is_imported, p.import_platform_type, p.allow_view, p.valid,
    p.is_published, p.is_forbidden, p.post_url, p.movefrom,
    q.caption, q.video_type,
    q.origin_url, q.hls_url, q.h265_url, q.flash_url, q.video_down_url,
    q.video_first_img, q.video_img_url,
    q.vid, q.duration_sec, q.size_bytes, q.img_width, q.img_height,
    q.aspect_ratio, q.embed_type
FROM (
    -- 谓词下推：扫 dim_post 时即过滤 contentType='视频'
    SELECT
        id AS postId, userId, blogId, blogName AS blog_name, blogNickName AS blog_nickname,
        title AS post_title, publishDate AS publish_date, publishTime AS publish_time,
        tags AS post_tags, ips AS post_ips, domains AS post_domains,
        isImported AS is_imported, ImportPlatformType AS import_platform_type,
        allowView AS allow_view, valid, isPublished AS is_published,
        isForbidden AS is_forbidden, url AS post_url,
        CASE
            WHEN movefrom LIKE '%ios%' OR movefrom LIKE '%iphone%' THEN 'ios'
            WHEN movefrom LIKE '%android%' THEN 'android'
            WHEN movefrom IS NULL THEN 'web'
            ELSE split(movefrom, '\\\\&')[0]
        END AS movefrom
    FROM lofter.dim_post
    WHERE contentType = '视频'
) p
LEFT JOIN (
    -- 列裁剪 + embed JSON 解析下推到子查询
    SELECT
        id AS postId, caption, videoType AS video_type,
        GET_JSON_OBJECT(embed, '$.originUrl')       AS origin_url,
        GET_JSON_OBJECT(embed, '$.hlsUrl')          AS hls_url,
        GET_JSON_OBJECT(embed, '$.h265Url')         AS h265_url,
        GET_JSON_OBJECT(embed, '$.flashurl')        AS flash_url,
        GET_JSON_OBJECT(embed, '$.video_down_url')  AS video_down_url,
        GET_JSON_OBJECT(embed, '$.video_first_img') AS video_first_img,
        GET_JSON_OBJECT(embed, '$.video_img_url')   AS video_img_url,
        CAST(GET_JSON_OBJECT(embed, '$.vid')       AS BIGINT) AS vid,
        CAST(GET_JSON_OBJECT(embed, '$.duration')  AS BIGINT) AS duration_sec,
        CAST(GET_JSON_OBJECT(embed, '$.size')      AS BIGINT) AS size_bytes,
        CAST(GET_JSON_OBJECT(embed, '$.img_width') AS INT)    AS img_width,
        CAST(GET_JSON_OBJECT(embed, '$.img_height')AS INT)    AS img_height,
        CASE
            WHEN CAST(GET_JSON_OBJECT(embed, '$.img_width')  AS DOUBLE) > 0
             AND CAST(GET_JSON_OBJECT(embed, '$.img_height') AS DOUBLE) > 0
            THEN CASE
                WHEN CAST(GET_JSON_OBJECT(embed, '$.img_width')  AS DOUBLE)
                   / CAST(GET_JSON_OBJECT(embed, '$.img_height') AS DOUBLE) > 1.05 THEN 'landscape'
                WHEN CAST(GET_JSON_OBJECT(embed, '$.img_width')  AS DOUBLE)
                   / CAST(GET_JSON_OBJECT(embed, '$.img_height') AS DOUBLE) < 0.95 THEN 'portrait'
                ELSE 'square' END
            ELSE 'unknown'
        END AS aspect_ratio,
        GET_JSON_OBJECT(embed, '$.type') AS embed_type
    FROM lofter_db_dump.ods_db_video_post_nd
) q
ON p.postId = q.postId  -- 仅等值关联, 过滤已下推到子查询
DISTRIBUTE BY p.postId % 10;
```

`dim_video_dd.job` 同时创建一个兼容视图 `lofter.dim_video`（自动指向最新分区），仅用于**临时探查 / Ad-hoc 查询**。

> ⚠️ **下游 ETL / 报表 / API 消费规范**：必须显式使用 `lofter.dim_video_dd` 并限制 dt 分区，**禁止使用 `dim_video` 视图**。原因：
> - **分区裁剪**：直接查 `dim_video_dd WHERE dt = ?` 只扫一个分区；走视图会有额外解析开销，部分场景分区裁剪失效会全表扫描
> - **可回溯性**：固定 dt 才能保证回刷 / 回溯时数据一致；视图永远指向最新，下游历史查询会"飘"
> - **依赖明确**：Azkaban / DQC 必须能识别上游表名，视图屏蔽了真实分区依赖

**规范用法**（强制）：
```sql
SELECT * FROM lofter.dim_video_dd WHERE dt = '${azkaban.flow.1.days.ago}' AND postId = ?;
```

**临时查询用法**（仅 Ad-hoc，不可入 ETL）：
```sql
SELECT * FROM lofter.dim_video WHERE postId = ?;  -- 仅用于 SQL 客户端临时查询
```

---

## 4. DWD 层

> **共 6 张表**：play_di（核心）/ quality_event_di / publish_di / publish_funnel_di / cover_edit_di / icloud_download_di
> **目录**：`etl/jobs/dwd-video/`

### 4.1 `dwd_video_play_di` — 播放会话明细（核心）

**粒度**：reqId（= mda 会话 ID）。一次完整播放 = 一行。

**上游事件**：m7-5（心跳/停止）+ m7-6（用户行为）+ m7-7（播放器状态）+ m7-8（错误）+ m7-9（清晰度切换），全部按 `params['reqId']` group by。

**核心字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| reqId | STRING | 会话主键 |
| postId / userId / deviceUdid / deviceOs / deviceModel / appVersion / appChannel | | 维度 |
| dominant_quality | STRING | 主档位 "宽x高"（m7-5.quality 直接上报，不需我们计算） |
| end_currentrate | STRING | 本段视频结束的码率 |
| player_type | STRING | AVPlayer / exoplayer / neliveplayer |
| scene / alg_info / rec_id / is_advertise / item_position / video_url | | m7-5 业务字段 |
| **played_time_ms** | BIGINT | 实际播放时长(ms) — m7-5.playedTime 求和 |
| **buffer_time_ms** | BIGINT | 累计缓冲时长(ms) — m7-5.bufferTime 求和 |
| duration_ms | BIGINT | 视频总时长(ms) — m7-5.duration |
| max_progress | DOUBLE | m7-5.maxProgress |
| play_rate | DOUBLE | 倍速档位（m7-5.playRate, 真实档位 1.0/1.25/1.5/2.0…） |
| **is_real** | INT | 是否有效播放（5 秒口径，与 dwd_post_browse_di.is_real 视频帖一致）；若需 3 秒口径（权威 effective_play_rate）下游可基于 `played_time_ms > 3000` 自算 |
| **is_finished** | INT | 完播：maxProgress>=0.9 OR played/duration>=0.9 |
| buffer_session_count / userseek_*  / system_* / inflate_* / timer_* | INT | m7-5.interruptReason 5 个 key 的计数 |
| start_event_time / prepare_event_time / first_frame_event_time | BIGINT | 关键时间戳 |
| **ttfr_ms** | BIGINT | TTFR: m7-7(state=1).ts - m7-6(start).ts（播放器准备时间） |
| **ttfp_ms** | BIGINT | TTFP: m7-7(state=2).ts - m7-6(start).ts（**首帧时间**） |
| is_play_started | INT | 有 m7-7(state=2) |
| is_play_failed / error_type / error_code / error_retry_count | | m7-8 |
| play_heartbeat_count / user_action_count / player_state_change_count / player_error_count / quality_event_count / quality_switch_count | BIGINT | 各类事件计数（用于下游算"切换频率 = quality_switch_count / play_heartbeat_count"等组合指标） |
| pause_count / resume_count / seek_count | BIGINT | m7-6 各 action 计数 |
| is_speedrate_used | INT | play_rate > 1.0 |

**关键 SQL 片段**（完整版见 `etl/jobs/dwd-video/dwd_video_play_di.job`）：

```sql
INSERT OVERWRITE TABLE lofter.dwd_video_play_di PARTITION(dt='${azkaban.flow.1.days.ago}')
SELECT reqId, MAX(postId) AS postId, ...,
       SUM(CASE WHEN eventId='m7-5' THEN NVL(playedTime, 0) ELSE 0 END) AS played_time_ms,
       SUM(CASE WHEN eventId='m7-5' THEN NVL(bufferTime, 0)  ELSE 0 END) AS buffer_time_ms,
       MAX(CASE WHEN eventId='m7-5' THEN duration_param END) AS duration_ms,
       IF(SUM(CASE WHEN eventId='m7-5' THEN NVL(playedTime,0) ELSE 0 END) > 5000, 1, 0) AS is_real,
       CASE
           WHEN MAX(CASE WHEN eventId='m7-5' THEN maxProgress END) >= 0.9
             OR (MAX(...duration...) > 0 AND SUM(...played...) / MAX(...duration...) >= 0.9)
           THEN 1 ELSE 0
       END AS is_finished,
       SUM(CASE WHEN eventId='m7-5'
                 AND CAST(GET_JSON_OBJECT(interruptReason, '$.buffer') AS INT) > 0
                THEN 1 ELSE 0 END) AS buffer_session_count,
       MIN(CASE WHEN eventId='m7-7' AND state=1 THEN createtime END)
         - MIN(CASE WHEN eventId='m7-6' AND action='start' THEN createtime END) AS ttfr_ms,
       MIN(CASE WHEN eventId='m7-7' AND state=2 THEN createtime END)
         - MIN(CASE WHEN eventId='m7-6' AND action='start' THEN createtime END) AS ttfp_ms,
       ...
FROM ( SELECT params['reqId'] AS reqId, ... FROM lofter.ods_mda_app_di
       WHERE dt='${azkaban.flow.1.days.ago}'
         AND eventId IN ('m7-5','m7-6','m7-7','m7-8','m7-9') ) a
GROUP BY reqId
DISTRIBUTE BY hash(reqId) % 50;
```

### 4.2 `dwd_video_quality_event_di` — 分辨率切换事件明细

**粒度**：m7-9 每一次切换 = 一行。

**核心字段**：reqId · postId · userId · switch_type (init/manual/auto) · from_quality · to_quality · direction (up/down/same/none, 按"宽x高"的高度对比派生) · current_position_ms · duration_in_from_quality_ms · estimated_bandwidth_kbps · buffer_duration_ms

**用途**：下游计算"乒乓切换占比"（30s 内 down→up 或 up→down 反转）和"升降级占比"。

### 4.3 `dwd_video_publish_di` — 视频发布明细

**粒度**：postId（一条视频一行）。

**逻辑**：以 dwd_post_publish_di 视频帖（post_content_type='视频'）为左表 INNER JOIN dim_video_dd（强一致：发布事件必有视频维度）。**不与 funnel 表 join**（funnel 是 reqId 维度、发布是 postId 维度，盲目 join 会膨胀）。谓词下推：dt + post_content_type 过滤在子查询内完成，ON 仅 postId 等值。

### 4.4 `dwd_video_publish_funnel_di` — 上传漏斗

**粒度**：req_id（c10-* 一个上传任务一行）。

**核心字段**：req_id · userId · deviceOs · **post_type** (postNew/postEdit/postReblog，来自 c10-1.params.type) · start_time/upload_start_time/upload_progress_time/success_time · is_success · has_start/has_upload_start/has_progress/has_intercepted/has_material_fail/has_api_fail · **fail_stage** (Success/ApiFail/MaterialFail/ParamIntercepted/Stuck*) · fail_msg · fail_ext_code · **fail_category** (UserInteraction_or_Network / ServerError / ClientError) · total_duration_ms

**关键过滤**（参考现网 ads_post_publish_draft_report_di.job）：c10-30 的 `ext.msg` 必须命中预定义白名单（'正在自动保存' '请输入日志内容' 等），c10-32 必须 errorCode > 99 或 = 11004，避免把客户端运营级提示算成失败。

### 4.5 `dwd_video_cover_edit_di` — 封面编辑事件（c1-31/32/33）

**粒度**：单事件一行。

**字段**：userId / deviceUdid / deviceOs / appVersion / eventId / occurTime / cover_width / cover_height（仅 c1-31, 来自 ext）/ opt_type（仅 c1-32, 顶层 optType, 取值 "0"/"2"）。

> ⚠️ **重要限制**：通过 2026-06-22 真实 mda 数据校验，c1-31/32/33 的 `itemId / postId` 在 mda 中**全部为 NULL**，**无法关联到具体视频**。因此本表仅承载设备/用户维度的事件流，不含视频维度。下游若需"单视频编辑次数 / 修改前后 CTR 对比"等指标，需推动客户端补 postId 字段后才能实现。

### 4.6 `dwd_video_icloud_download_di` — iCloud 下载（m7-18, iOS）

**粒度**：m7-18 事件一行。

**字段**：userId / deviceUdid / appVersion / event_time / ext_type (1=主动取消失败 / 0=下载失败) / result_status (cancel / fail / null)

**注意**：当前 m7-18 仅在失败/取消时上报，**成功事件如何标识待 SDK 同步**（README 中已标注）。

---

## 5. DWS 层

> **共 5 张表**：post_dd / quality_dd / user_consume_di / creator_dd / speedrate_dd
> **规则**：**只输出因子**（pv/uv/cnt/time_ms），不直接输出 _rate/_ratio。
> **目录**：`etl/jobs/dws-video/`

### 5.1 `dws_video_post_dd` — 视频粒度全量汇总

**粒度**：postId × dt。

**因子分组**（共 38 个因子字段）：
- 维度（11 个，dim_video_dd 透传）：postId/userId/blogId/publish_date/video_type/duration_sec/size_bytes/aspect_ratio/is_imported/import_platform_type/movefrom + post_tags/post_ips/post_domains
- 浏览（11 个，从 dwd_post_browse_di 聚合）：expose_pv/expose_uv/play_pv/play_uv/real_play_pv/real_play_uv/finish_play_pv/finish_play_uv/real_play_time_ms/play_time_ms
- 播放会话（11 个，从 dwd_video_play_di 聚合）：play_session_count/play_session_uv/play_session_real_count/play_session_finish_count/play_session_started_count/buffer_session_count/buffer_time_ms/session_played_time_ms/ttfp_ms_sum/ttfp_ms_count/ttfr_ms_sum/ttfr_ms_count/play_fail_count/speedrate_session_count/speedrate_uv
- 互动（7 个）：praise_pv/reproduce_pv/recommend_pv/subscribe_pv/hot_pv/response_pv/share_pv

**TTFP/TTFR 用"求和+计数"双因子存储**，下游用 `ttfp_ms_sum / ttfp_ms_count` 算均值，或基于 dwd 层用 `percentile_approx` 算分位数。

### 5.2 `dws_video_quality_dd` — 质量大盘（按 deviceOs × device_tier × appVersion × dominant_quality）

只输出因子：play_session_count/uv · buffer_session_count/uv · buffer_time_ms · played_time_ms · ttfp_ms_sum/count · ttfr_ms_sum/count · play_started_count · play_failed_count · play_heartbeat_count_sum · play_attempt_count · quality_switch_count_sum · speedrate_uv

下游算 8 大率指标（见第 7 章），全部基于上述因子组合。

### 5.3 `dws_video_user_consume_di` — 用户视频消费

粒度：userId × deviceUdid。本身就是 pv/uv/cnt 因子，无率字段。

### 5.4 `dws_video_creator_dd` — 创作者视频画像

粒度：blogId。只输出因子（expose_pv_1d / play_pv_1d / real_play_pv_1d / finish_play_pv_1d / hot_pv_1d / share_pv_1d / response_pv_1d / play_session_count_1d / buffer_session_count_1d / publish_count_1d / total_video_count）。下游求商得"完播率 / 互动率"等。**因封面编辑事件无 postId 关联，本表不含封面编辑相关因子**。

### 5.5 `dws_video_speedrate_dd` — 倍速专项

粒度：deviceOs × speedrate_tier（1.0x / 1.25x / 1.5x / 2.0x 等真实档位）。

字段：overall_play_uv（透传，每个 tier 相同，便于下游算渗透率分母）/ overall_speedrate_uv / tier_session_count/uv / tier_finish_count / tier_buffer_session_count / tier_buffer_time_ms / tier_played_time_ms

---

## 6. ADS 层

> **共 4 张表**：quality_monitor_di / publish_funnel_di / cover_edit_ctr_di / icloud_download_funnel_di
> **规则**：因子透传，BI 自行求商。
> **目录**：`etl/jobs/ads-growth-video/`

### 6.1 `ads_growth_video_quality_monitor_di`

DWS quality_dd 因子的直接透传（P0 监控大盘）。

### 6.2 `ads_growth_video_publish_funnel_di`

按 deviceOs × appVersion × **post_type** 切片，输出 task_count / task_uv / step1~4 / fail_*_count / stuck_at_* / failed_at_* / fail_*_error。

### 6.3 `ads_growth_video_cover_edit_stat_di`

deviceOs × appVersion × eventId 粒度，输出 event_count / user_uv / event_with_size_count / avg_cover_width / avg_cover_height / c1_32_opt_0_count / c1_32_opt_2_count。**因 c1-* 埋点无 postId 关联，无法做单视频粒度 CTR 对比**；仅支持大盘维度的事件量 + 封面尺寸均值 + optType 分布观测。

### 6.4 `ads_growth_video_icloud_download_funnel_di`

按 appVersion 切片，输出 total_event_count / user_uv / cancel_count / fail_count / success_or_other_count。**取消率 / 失败率由下游求商**。

---

## 7. 指标计算示例（重点：因子如何组合得"率"）

> 下游 BI、API、Doris 物化视图都基于以下公式。**所有率指标的分子分母都明确可追溯**。

### 7.1 总观测指标

| 指标 | 公式 | 上游因子表 |
|------|------|----------|
| 人均观看时长（按 deviceOs） | `SUM(real_play_time_ms) / COUNT(DISTINCT deviceUdid)` | dws_video_user_consume_di |
| 整体发布成功率（按 deviceOs/post_type） | `SUM(step4_success_count) / SUM(step1_init_count)` | ads_growth_video_publish_funnel_di |
| 每日人均发布成功数 | `SUM(step4_success_count) / COUNT(DISTINCT userId where step1>0)` | ads_growth_video_publish_funnel_di + dwd_video_publish_funnel_di |
| **视频完播率**（按 deviceOs） | `SUM(finish_play_uv) / SUM(play_uv)` | dws_video_post_dd × dim_video_dd.movefrom |
| **倍速下完播率** | `SUM(tier_finish_count) / SUM(tier_session_count) WHERE speedrate_tier <> '1.0x'` | dws_video_speedrate_dd |
| 视频互动率 | `SUM(hot_pv + response_pv + share_pv) / SUM(real_play_pv)` | dws_video_post_dd |
| 人均视频播放量 | `SUM(play_session_count) / COUNT(DISTINCT deviceUdid)` | dws_video_user_consume_di |

### 7.2 分辨率指标（均出自 dws_video_quality_dd）

| 指标 | 公式 |
|------|------|
| **卡顿率（会话级）** | `SUM(buffer_session_count) / SUM(play_session_count)` |
| **卡顿时长占比** | `SUM(buffer_time_ms) / SUM(played_time_ms)` |
| **卡顿用户 UV 占比** | `SUM(buffer_session_uv) / SUM(play_session_uv)` |
| **百秒卡顿次数** | `SUM(buffer_session_count) * 100 * 1000 / SUM(played_time_ms)` |
| **各分辨率播放时长占比** | `SUM(played_time_ms) GROUP BY dominant_quality / SUM(played_time_ms over all)` |
| **TTFP P50 / P90 / Avg** | 均值：`SUM(ttfp_ms_sum) / SUM(ttfp_ms_count)`<br>分位数：基于 dwd_video_play_di 用 `percentile_approx(ttfp_ms, 0.5/0.9)` |
| **TTFR P50 / P90 / Avg** | 同上，字段换 ttfr |
| **起播成功率** | `SUM(play_started_count) / SUM(play_attempt_count)` |
| **播放失败率** | `SUM(play_failed_count) / SUM(play_attempt_count)` |
| **清晰度自动切换频率** | `SUM(quality_switch_count_sum) / SUM(play_heartbeat_count_sum)`（每次心跳的平均切换数） |
| **倍速渗透率** | `SUM(speedrate_uv) / SUM(play_session_uv)` |

### 7.3 上传成功率（ads_growth_video_publish_funnel_di 按 deviceOs 切片）

| 指标 | 公式 |
|------|------|
| 整体发布成功率 | `step4_success_count / step1_init_count` |
| 阶段命中率（init → upload_start） | `step2_upload_start_count / step1_init_count` |
| 阶段命中率（upload_start → progress） | `step3_progress_count / step2_upload_start_count` |
| 阶段命中率（progress → success） | `step4_success_count / step3_progress_count` |
| 服务端失败占比 | `fail_server_error / (failed_at_intercept + failed_at_material + failed_at_api)` |
| 客户端失败占比 | `fail_client_error / total_fails` |
| 网络/用户交互失败占比 | `fail_user_or_network / total_fails` |
| 按 post_type 比较新发/编辑/转发各自成功率 | 同上公式 group by `post_type` |

> ⚠️ "后台上传成功率" "断点续传成功率" "主动重连率" 在当前 c10-* 埋点中**无对应 ext 字段**，需推动客户端补 ext.bg / ext.resume / ext.action 后才能落地。当前 fail_msg 中文消息可作为间接信号，但不准确。

### 7.4 倍速（dws_video_speedrate_dd）

| 指标 | 公式 |
|------|------|
| **倍速功能渗透率**（按 deviceOs） | `overall_speedrate_uv / overall_play_uv` (因 tier 内透传所以任取一行) |
| **1.0x vs 2.0x 百秒卡顿次数对比** | `tier_buffer_session_count * 100000 / tier_played_time_ms` group by `speedrate_tier` |
| **2.0x 完播率** | `tier_finish_count / tier_session_count WHERE speedrate_tier = '2.0x'` |

### 7.5 封面编辑（ads_growth_video_cover_edit_stat_di）

> ⚠️ **当前限制**：通过 mda 真实数据校验（dt=2026-06-22），c1-31/32/33 埋点中 `itemId` 全部为 NULL，无法关联到具体 postId。因此**单视频粒度的"修改次数 / CTR 前后对比 / 二次编辑渗透率"指标暂时无法计算**，需推动客户端在 c1-* 埋点中补 postId 字段。

| 指标 | 公式 | 是否可算 |
|------|------|------|
| **封面编辑事件量（按 eventId）** | `SUM(event_count) group by eventId` | ✅ 可算 |
| **封面编辑触达用户数** | `SUM(user_uv) group by deviceOs / appVersion` | ✅ 可算 |
| **平均封面尺寸** | `AVG(avg_cover_width)`、`AVG(avg_cover_height)` | ✅ 可算（仅 c1-31）|
| **c1-32 操作类型分布** | `SUM(c1_32_opt_0_count) / SUM(c1_32_opt_2_count)` | ✅ 可算 |
| ~~二次编辑渗透率（按创作者）~~ | ~~需 postId 关联~~ | ❌ 缺埋点 |
| ~~单视频平均编辑次数~~ | ~~需 postId 关联~~ | ❌ 缺埋点 |
| ~~修改前后 24h CTR 对比~~ | ~~需 postId 关联 dwd_post_browse_di~~ | ❌ 缺埋点 |

### 7.6 iCloud 下载（ads_growth_video_icloud_download_funnel_di）

| 指标 | 公式 |
|------|------|
| **iCloud 下载成功率** | `success_or_other_count / total_event_count`（前提：m7-18 成功也上报，需 SDK 确认） |
| **主动取消率** | `cancel_count / total_event_count` |
| **失败率** | `fail_count / total_event_count` |

---

## 8. 注意事项（高频坑）

### 8.1 单位字典

| 字段 | 单位 | 来源 |
|------|------|------|
| `dim_video_dd.duration_sec` | **秒** | embed.duration |
| `dim_video_dd.size_bytes` | **字节** | embed.size（不是 MB！） |
| `dwd_video_play_di.duration_ms / played_time_ms / buffer_time_ms / ttfp_ms / ttfr_ms` | **毫秒** | m7-5/6/7 params |
| `dwd_video_play_di.play_rate` | **倍速 Float**（1.0 标准） | m7-5.playRate |
| `dwd_video_play_di.dominant_quality` | **字符串 "宽x高"**（如 "720x1280"） | m7-5.quality |
| 所有 `_pv / _uv / _count` | **整数次数** | count / count distinct |

### 8.2 c10-* itemType 兼容

现网 ods_mda 中 `itemType` 字段存 `'VIDEO'` 也存 `'4'`（int 字符化）。所有 c10-* 过滤必须 `itemType in ('VIDEO','4')`。

### 8.3 c10 错误码字段路径双向兼容

权威文档说 `ext.code`，现网 `ads_post_publish_draft_report_di` 用 `ext.errorCode`。SQL 中用 `COALESCE(get_json_object(ext,'$.code'), get_json_object(ext,'$.errorCode'))` 两者皆取。

### 8.4 m7-6 action 字段在 ext JSON 里

`m7-6.action` 嵌套在 `params['ext']` JSON 字符串中，**不在 params 顶层**。取值 ∈ {start, resume, pause, stop, seek}。提取写法：`get_json_object(params['ext'], '$.action')`。详见 §8.12。

### 8.5 m7-10 不存在

倍速通过 `m7-5.params['playRate']`(Float) 获取。

### 8.6 quality 格式 "宽x高"

派生升降级方向时用 `split(quality, 'x')[1]` 拿高度比较：

```sql
CASE
    WHEN CAST(split(to_quality, 'x')[1] AS INT) > CAST(split(from_quality, 'x')[1] AS INT) THEN 'up'
    WHEN CAST(split(to_quality, 'x')[1] AS INT) < CAST(split(from_quality, 'x')[1] AS INT) THEN 'down'
    ELSE 'same'
END
```

### 8.7 reqId 唯一性

m7-* 同一次播放共用一个 reqId（UUID）；c10-* 同一次上传任务共用一个 reqId。**dwd 表都按 reqId group by 聚合**，DQC 必须校验 reqId 唯一。

### 8.8 dim_video_dd 与 dim_post 一致性

`dim_video_dd.postId = dim_post.id WHERE contentType='视频'`，关系是 1:1 内连接。若 ods_db_video_post_nd 缺数据则 dim_video_dd 行数会小于 dim_post 视频帖数 → DQC 必须监控。

### 8.9 ext_type 与 result_status（m7-18）

`ext_type` 是 INT；`result_status` 是派生的 STRING：
- ext_type=1 → result_status='cancel'
- ext_type=0 → result_status='fail'
- 其他 → result_status=NULL（暂视为成功或其他类型，待 SDK 确认）

### 8.10 现网 dws_video_post_general_di 的 size_mb 字段名陷阱

现网 `dws_video_post_general_di` 字段叫 `size_mb` 但实际是字节数（来自 embed.size）。我们 dim_video_dd 用 `size_bytes` 正名。后续若下游报表迁移到 dim_video_dd，**务必注意单位换算（不需要换算，都是字节）**。

### 8.11 is_real 字段口径与同名陷阱

`dwd_video_play_di.is_real` 与 `dwd_post_browse_di.is_real`（视频帖）**口径相同**（5 秒口径，`played_time_ms > 5000`），同名 + 同义，下游可放心理解。但是：
- 两表 JOIN 同一查询时必须**用表别名引用**：`v.is_real`（dwd_video_play_di）vs `b.is_real`（dwd_post_browse_di）
- 数据量不同：dwd_video_play_di 是会话粒度（按 reqId 聚合 m7-* 心跳），dwd_post_browse_di 是浏览事件粒度（page_view + page_duration）；两者 is_real 计数不可直接相加
- 若需 3 秒口径（权威 effective_play_rate），下游基于 `played_time_ms > 3000` 自算，不要再单独建字段

### 8.12 mda ext 字段嵌套（重大坑点）

通过 2026-06-22 真实数据校验确认，以下事件的业务字段**嵌套在 params['ext'] JSON 字符串中**，不是 params 顶层字段：

| 事件 | 顶层 params 字段 | ext JSON 子字段 |
|---|---|---|
| m7-5 | reqId / postId / playedTime / bufferTime / duration / maxProgress / playRate / quality / currentrate / playerType / trackerScene / interruptReason / url / itemPosition / isAdvertise | （无）|
| m7-6 | reqId / postId / createtime / playerType / trackerScene / itemPosition / duration | **action / quality / url** |
| m7-7 | reqId / postId / createtime / playerType | **state** |
| m7-8 | reqId / postId / playerType / retryCount / trackerScene / itemPosition | **errCode / errMsg / errType** |
| m7-9 | reqId / postId / createtime / playerType / trackerScene | **type / fromQuality / toQuality / currentPositionMs / durationInFromQualityMs / estimatedBandwidthKbps / bufferDurationMs** |
| m7-18 | trackerScene / postId(空) / time | **type**（仅 0=失败 / 1=主动取消）|
| c10-1/5/10/15 | reqId / type / itemType（顶层独立列）/ scene（顶层独立列）/ itemId（顶层独立列） | blogId |
| c10-30/31/32 | reqId / type / code（仅 c10-30）| msg / errorCode / blogId |
| c1-31 | userId（顶层独立列）| width / height |
| c1-32 | optType / userId | （无业务字段）|
| c1-33 | userId | （无业务字段）|

**SQL 写法**：嵌套字段必须用 `get_json_object(params['ext'], '$.xxx')` 提取；m7-5 的 `interruptReason` 也是 JSON 字符串但本身在 params 顶层，写法为 `get_json_object(params['interruptReason'], '$.buffer')`。

### 8.13 c1-* 封面编辑无 postId 关联

封面编辑事件 c1-31/32/33 在 mda 中 `itemId` 顶层独立列**全部为 NULL**，无法关联到具体视频。影响：
- ❌ `dws_video_creator_dd` 不能计算创作者维度的封面编辑因子（已移除）
- ❌ `ads_growth_video_cover_edit_*` 不能计算单视频 CTR 对比（已改造为 `_stat_di`）
- ⏳ 待客户端补 postId 字段后，本表可扩展

### 8.14 m7-9 toQuality 偶尔为码率字符串

通过真实样本观察到 `toQuality` 偶尔为 `"1209k"` 等码率字符串（非"宽x高"格式）。dwd_video_quality_event_di 中 `direction` 派生用 `rlike '^[0-9]+x[0-9]+$'` 做格式校验，非匹配的值方向标记为 `'unknown'`。



| 规则 | ❌ 违规写法 | ✅ 合规写法 |
|------|----------|----------|
| 谓词下推 | `JOIN tbl t ON a.id=t.id WHERE t.dt='${...}' AND t.flag=1` | `FROM (SELECT ... FROM tbl WHERE dt='${...}' AND flag=1) t JOIN ... ON a.id=t.id` |
| ON 仅等值 | `ON a.id=t.id AND t.dt='${...}'` | dt 过滤下推到子查询，`ON a.id=t.id` |
| 复合等值键 | `ON a.id=t.id AND t.flag=1` | `ON a.id=t.id AND a.dt=t.dt`（多个等值合规）|
| 列裁剪 | `FROM (SELECT * FROM tbl) t` | `FROM (SELECT id, name FROM tbl) t` |
| JOIN 类型 | `FULL OUTER JOIN`（禁用）| 维度补充用 `LEFT JOIN` / 强一致用 `JOIN` |
| 范围关联 | `JOIN tbl t ON a.id=t.id AND t.time BETWEEN ...` | `JOIN tbl t ON a.id=t.id WHERE t.time BETWEEN ...` |

**典型应用**：
- `dim_video_dd.job` —— dim_post 子查询内 `WHERE contentType='视频'` + ods_db_video_post_nd 子查询列裁剪 + LEFT JOIN ON 仅等值
- `dwd_video_publish_di.job` —— dwd_post_publish_di 子查询内 `WHERE dt='${...}' AND post_content_type='视频'` + dim_video_dd 子查询内 `WHERE dt='${...}'` + INNER JOIN ON 仅等值
- `dws_video_speedrate_dd.job` —— sess CTE 内 `WHERE dt='${...}' AND is_real=1` + LEFT JOIN ON 仅等值
- `dws_video_user_consume_di.job` —— browse_user 为左表 LEFT JOIN play_user，**避免 FULL OUTER JOIN**

---

## 9. 维护信息

### 9.1 Job 路径

| 任务 | 路径 | 类型 |
|------|------|------|
| dim_video_dd | `etl/jobs/dimensions/dim_video_dd.job` | sparksql |
| dwd_video_play_di | `etl/jobs/dwd-video/dwd_video_play_di.job` | sparksql |
| dwd_video_quality_event_di | `etl/jobs/dwd-video/dwd_video_quality_event_di.job` | sparksql |
| dwd_video_publish_di | `etl/jobs/dwd-video/dwd_video_publish_di.job` | sparksql |
| dwd_video_publish_funnel_di | `etl/jobs/dwd-video/dwd_video_publish_funnel_di.job` | sparksql |
| dwd_video_cover_edit_di | `etl/jobs/dwd-video/dwd_video_cover_edit_di.job` | sparksql |
| dwd_video_icloud_download_di | `etl/jobs/dwd-video/dwd_video_icloud_download_di.job` | sparksql |
| dws_video_post_dd | `etl/jobs/dws-video/dws_video_post_dd.job` | sparksql |
| dws_video_quality_dd | `etl/jobs/dws-video/dws_video_quality_dd.job` | sparksql |
| dws_video_user_consume_di | `etl/jobs/dws-video/dws_video_user_consume_di.job` | sparksql |
| dws_video_creator_dd | `etl/jobs/dws-video/dws_video_creator_dd.job` | sparksql |
| dws_video_speedrate_dd | `etl/jobs/dws-video/dws_video_speedrate_dd.job` | sparksql |
| ads_growth_video_quality_monitor_di | `etl/jobs/ads-growth-video/ads_growth_video_quality_monitor_di.job` | sparksql |
| ads_growth_video_publish_funnel_di | `etl/jobs/ads-growth-video/ads_growth_video_publish_funnel_di.job` | sparksql |
| ads_growth_video_cover_edit_stat_di | `etl/jobs/ads-growth-video/ads_growth_video_cover_edit_stat_di.job` | sparksql |
| ads_growth_video_icloud_download_funnel_di | `etl/jobs/ads-growth-video/ads_growth_video_icloud_download_funnel_di.job` | sparksql |
| **建表 DDL（全部 16 张）** | `etl/docs/video/ddl.sql` | sql |

### 9.2 调度参数

| 参数 | 值 |
|------|---|
| 调度引擎 | Azkaban |
| 调度周期 | 日级 T-1 |
| 日期变量 | `${azkaban.flow.1.days.ago}`（禁止硬编码） |
| 跨天回刷 | 必须逐天提交，不能 dt BETWEEN |

### 9.3 SLA / DQC（建议初值，上线后实际调阈值）

| 表 | SLA | DQC |
|----|-----|-----|
| dim_video_dd | 早 6 点 | postId 唯一 / 行数 ≈ dim_post(contentType='视频') ±1% / duration_sec 非 NULL ≥ 99% |
| dwd_video_play_di | 早 7 点 | reqId 唯一 / m7-5 事件对账 / is_real 比例在历史 [-30%,+30%] / TTFP 非 NULL ≥ 95% |
| dwd_video_publish_funnel_di | 早 7 点 | req_id 唯一 / has_start ≥ has_upload_start ≥ has_progress ≥ is_success |
| dws_video_quality_dd / ads_growth_video_quality_monitor_di | 早 8 点 | buffer_per_100s 计算后在历史 [-30%,+30%] / TTFP P50 ≤ 2000ms |
| 其他 | 早 9 点 | 行数波动 ±30% |

### 9.4 待 SDK 确认事项（不阻塞上线）

| 来源 | 待确认 | 影响表 |
|------|--------|--------|
| 2026 版本埋点 excel | c1-31/32/33 缺少 postId（itemId 已确认为 NULL），需推动客户端补 postId 字段 | dwd_video_cover_edit_di（当前仅设备维度，无法做单视频粒度指标） |
| iOS SDK | m7-18 是否在成功时也上报？标识是 eventId 还是 ext.type 其他值？ | dwd_video_icloud_download_di（当前成功率算不准） |
| 客户端 | c10-* 的 ext 是否补 bg/resume/action 字段以支持后台/续传/重连指标 | ads_growth_video_publish_funnel_di（缺这些指标） |

### 9.5 变更记录

| 版本 | 日期 | 变更 |
|------|------|------|
| V1 | 2026-06-02 | 初版 16 张表设计 |
| V2 | 2026-06-03 | 修正：视频物理属性来源改回 ods_db_video_post_nd.embed JSON（非 mda） |
| V3 | 2026-06-04 | 基于权威子文档全量对齐 mda 字段（m7-5/6/7/8/9 + c10-*） |
| V4 | 2026-06-04 | **上线规范修复**：DDL 去 LOCATION + 补分区 COMMENT；DWS/ADS 去率字段（只输出因子）；建 spec.md 取代 README.md |

---

## 10. 现网代码引用证据

- **m7-5 params 字段**：`etl/jobs/ads-client/ads_client_version_metrics_report_di.job` 第 31-50 行
- **c10-* params 字段** + **错误 msg 过滤白名单**：`etl/jobs/ads-reports/ads_post_publish_draft_report_di.job`
- **dim_video_dd LEFT JOIN 模式参考**：`etl/jobs/dws/dws_video_post_general_di.job` 第 5-7 行（ods_db_video_post_nd embed JSON 解析）
- **dim_post.moveFrom 字段**：`etl/src/main/scala/com/netease/lofter/etl/dim/PostJob.scala`
