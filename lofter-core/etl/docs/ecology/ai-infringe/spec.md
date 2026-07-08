
# AI 侵权投诉数据 Spec

> **业务域**：生态治理 (Ecology) / AI 侵权投诉 (AI Infringe)  
> **责任表**：`lofter.dwd_ecology_ai_infringe_post_dd` · `lofter.dws_ecology_ai_infringe_post_dd` · `lofter.dws_ecology_ai_infringe_blog_dd` · `lofter_dm.ads_cp_data_ai_infringe_detail_dd` · `lofter_dm.ads_cp_data_ai_infringe_post_dd` · `lofter_dm.ads_cp_data_ai_infringe_blog_dd`  
> **调度**：Azkaban，日级 T-1 调度，使用 `${azkaban.flow.1.days.ago}` 参数

---

## 1. 业务背景

LOFTER 平台为应对 AI 生成内容引发的侵权问题，建立了"用户举报 + 合伙人巡查"的双通道投诉机制。本数据链路用于沉淀**文章维度**与**博客（创作者）维度**的 AI 侵权投诉数据，支撑：

- **风控审核**：识别高投诉文章/创作者
- **合伙人运营**：区分普通用户投诉与官方合伙人投诉
- **治理监控**：按时间窗口跟踪投诉趋势

### 数据流向

```
┌─────────────────────────────────────────────────────────────┐
│  ODS 原始层                                                  │
│    lofter_db_dump.ods_db_recommend_user_mark_nd  (推荐标记)  │
│    lofter_db_dump.ods_db_infringe_post_nd        (侵权举报)  │
│                            │                                │
│                            ▼                                │
│  DWD 明细层                                                  │
│    lofter.dwd_ecology_ai_infringe_post_dd                   │
│    （UNION ALL 合并双源 → 文章×用户×来源 投诉明细）          │
│                  │                       │                  │
│                  ▼                       ▼                  │
│  DWS 汇总层                                                  │
│    lofter.dws_ecology_ai_infringe_post_dd                   │
│    （文章粒度聚合 → UV + 多窗口指标）                        │
│    lofter.dws_ecology_ai_infringe_blog_dd                   │
│    （博客粒度聚合 → UV + 多窗口指标 + 首次/最新投诉日期）    │
│                            │                                │
│                            ▼                                │
│  ADS 应用层（创作者运营平台）                                │
│    lofter_dm.ads_cp_data_ai_infringe_detail_dd              │
│    （DWD 明细透传 → 供 CP 平台直查投诉明细）                 │
│    lofter_dm.ads_cp_data_ai_infringe_post_dd                │
│    （文章粒度透传 → 供 CP 平台直接消费）                     │
│    lofter_dm.ads_cp_data_ai_infringe_blog_dd                │
│    （博客粒度透传 → 供 CP 平台直接消费）                     │
│                            │                                │
│                            ▼                                │
│             [风控 / 合伙人运营 / 治理监控]                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. DWD 明细表 — `dwd_ecology_ai_infringe_post_dd`

### 2.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter.dwd_ecology_ai_infringe_post_dd` |
| 层级 | DWD（明细层） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 文章 × 投诉用户 × 来源 |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1，每日 1 次 |
| 上游表 | `lofter_db_dump.ods_db_recommend_user_mark_nd`（source=1）<br>`lofter_db_dump.ods_db_infringe_post_nd`（source=2）<br>`lofter.dwd_rec_content_understand_dd` + `lofter.dim_post_article`（source=3）<br>`lofter_db_dump.ods_db_partner_judge_task_nd`（source=4） |

### 2.2 字段定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `postId` | BIGINT | 文章ID |
| `blogId` | BIGINT | 被投诉文章所属博客ID/创作者ID |
| `userId` | BIGINT | 投诉发起人用户ID（source=3/4 时可能为空，表示系统/算法触发） |
| `source` | INT | 投诉来源：`1`=负反馈卡片；`2`=侵权后台；`3`=算法AI文识别；`4`=合伙人众裁 |
| `createTime` | BIGINT | 投诉创建时间戳（毫秒） |
| `createDate` | STRING | 投诉创建日期（yyyy-MM-dd），由 `createTime` 转换得到 |
| `dt` | STRING | 分区日期（yyyy-MM-dd） |

### 2.3 计算逻辑

通过 `UNION ALL` 合并四个来源的投诉/判定数据：

| source | 含义 | 上游表 | 关键过滤条件 | userId | createTime |
|:------:|------|--------|------------|:------:|:----------:|
| `1` | 负反馈卡片 | `ods_db_recommend_user_mark_nd` | `markType = 'aiContent' AND showType = 1` | 用户 ID | 用户标记时间 |
| `2` | 侵权后台举报 | `ods_db_infringe_post_nd` | `reportType IN (2, 3)` | 举报人 ID | 举报时间 |
| `3` | 算法 AI 文识别 | `dwd_rec_content_understand_dd` LEFT JOIN `dim_post_article` | `type = 'AI文识别V2'`（左表） + `contentType = '文字'`（右表，补 blogId） | `NULL` | `NULL` |
| `4` | 合伙人众裁 | `ods_db_partner_judge_task_nd` | `reportType = 9999 AND result = 1` | `NULL` | `NULL` |

> **关键设计点**：
> - source=`1`/`2`：来自**用户行为**，`userId` / `createTime` 均有值，参与 DWS 层 UV/时间窗口指标计算（§3 / §4 第一段 UNION）。
> - source=`3`/`4`：来自**系统/合伙人判定**，无具体投诉用户，`userId` 与 `createTime` 设为 `NULL`，DWS 层只用于 `is_rec_ai_recognized` / `is_partner_judged` / `rec_ai_recognition_cnt` / `partner_judge_cnt` 等判定字段计算（§3 / §4 第二段 UNION）。
> - `ods_db_recommend_user_mark_nd` 的 `showId` 字段需 `cast(showId AS bigint)` 后作为 `postId` 使用。
> - source=`3` 段的 `dwd_rec_content_understand_dd` 是按 `dt` 分区的 DWD 表（取 T-1 分区），`dim_post_article` 是不分区的维表，左 join 用于补全 `blogId`。

### 2.4 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter.dwd_ecology_ai_infringe_post_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT postId,
       blogId,
       userId,
       source,
       createTime,
       from_unixtime(cast(createTime/1000 AS bigint), 'yyyy-MM-dd') AS createDate
FROM (
    /* source=1 — 负反馈卡片（用户在推荐流标记 AI 内容） */
    SELECT cast(showId AS bigint) AS postId,
           blogId, userId, 1 AS source, createTime
    FROM lofter_db_dump.ods_db_recommend_user_mark_nd
    WHERE markType = 'aiContent' AND showType = 1

    UNION ALL

    /* source=2 — 侵权后台举报 */
    SELECT postId, blogId, userId, 2 AS source, createTime
    FROM lofter_db_dump.ods_db_infringe_post_nd
    WHERE reportType IN (2, 3)

    UNION ALL

    /* source=3 — 算法 AI 文识别（系统判定，无投诉人） */
    SELECT t1.postId,
           t2.blogId,
           NULL AS userId,
           3    AS source,
           NULL AS createTime
    FROM (
        SELECT itemId AS postId
        FROM lofter.dwd_rec_content_understand_dd
        WHERE dt = '${azkaban.flow.1.days.ago}' AND type = 'AI文识别V2'
    ) t1
    LEFT JOIN (
        SELECT id AS postId, blogId
        FROM lofter.dim_post_article
        WHERE contentType = '文字'
    ) t2
    ON t1.postId = t2.postId

    UNION ALL

    /* source=4 — 合伙人众裁（合伙人对内容做出违规判定） */
    SELECT postId, blogId, NULL AS userId, 4 AS source, NULL AS createTime
    FROM lofter_db_dump.ods_db_partner_judge_task_nd
    WHERE reportType = 9999 AND result = 1
) t
```

> ⚠️ **createDate 的特殊性**：source=3/4 段 `createTime` 为 NULL，经 `from_unixtime(NULL, ...)` 得到 NULL，因此这两类记录的 `createDate` 也是 NULL —— 不参与 DWS 层基于时间窗口（datediff）的指标计算，符合设计。

### 2.5 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter.dwd_ecology_ai_infringe_post_dd (
    postId      BIGINT  COMMENT '文章ID',
    blogId      BIGINT  COMMENT '博客ID/创作者ID',
    userId      BIGINT  COMMENT '投诉发起人用户ID',
    source      INT     COMMENT '1: 负反馈卡片，2: 侵权后台，3: 算法AI文识别，4: 合伙人众裁',
    createTime  BIGINT  COMMENT '投诉创建时间戳(毫秒)',
    createDate  STRING  COMMENT '投诉创建日期(yyyy-MM-dd)'
)
COMMENT 'AI侵权投诉文章明细表 — 推荐标记 + 侵权举报合并'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

## 3. DWS 汇总表 — `dws_ecology_ai_infringe_post_dd`

### 3.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter.dws_ecology_ai_infringe_post_dd` |
| 层级 | DWS（汇总层） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 文章 (postId × blogId) |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1，每日 1 次 |
| 上游表 | `lofter.dwd_ecology_ai_infringe_post_dd`<br>`lofter_db_dump.ods_db_partner_apply_job_nd` |
| 业务用途 | 评估**单篇文章**的被投诉情况，识别 AI 内容 / 高投诉文章用于风控审核 |

### 3.2 字段定义

按 **文章粒度（postId + blogId）** 聚合：三类投诉 UV × 8 个时间窗口 = 27 个 UV 指标，外加 2 个判定标记位（AI 识别 / 合伙人众裁）。

#### 3.2.1 维度键

| 字段 | 类型 | 说明 |
|------|------|------|
| `postId` | BIGINT | 文章ID |
| `blogId` | BIGINT | 博客ID/创作者ID |

#### 3.2.2 全量 UV 指标（不限时间）

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_uv` | BIGINT | 总投诉 UV |
| `complaint_user_uv` | BIGINT | 普通用户投诉 UV（非合伙人） |
| `complaint_partner_uv` | BIGINT | 合伙人投诉 UV |

#### 3.2.3 总投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_uv_1d` | BIGINT | 近 1 天总投诉 UV |
| `complaint_uv_3d` | BIGINT | 近 3 天总投诉 UV |
| `complaint_uv_5d` | BIGINT | 近 5 天总投诉 UV |
| `complaint_uv_7d` | BIGINT | 近 7 天总投诉 UV |
| `complaint_uv_15d` | BIGINT | 近 15 天总投诉 UV |
| `complaint_uv_30d` | BIGINT | 近 30 天总投诉 UV |
| `complaint_uv_60d` | BIGINT | 近 60 天总投诉 UV |
| `complaint_uv_90d` | BIGINT | 近 90 天总投诉 UV |

#### 3.2.4 普通用户投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_user_uv_1d` | BIGINT | 近 1 天普通用户投诉 UV |
| `complaint_user_uv_3d` | BIGINT | 近 3 天普通用户投诉 UV |
| `complaint_user_uv_5d` | BIGINT | 近 5 天普通用户投诉 UV |
| `complaint_user_uv_7d` | BIGINT | 近 7 天普通用户投诉 UV |
| `complaint_user_uv_15d` | BIGINT | 近 15 天普通用户投诉 UV |
| `complaint_user_uv_30d` | BIGINT | 近 30 天普通用户投诉 UV |
| `complaint_user_uv_60d` | BIGINT | 近 60 天普通用户投诉 UV |
| `complaint_user_uv_90d` | BIGINT | 近 90 天普通用户投诉 UV |

#### 3.2.5 合伙人投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_partner_uv_1d` | BIGINT | 近 1 天合伙人投诉 UV |
| `complaint_partner_uv_3d` | BIGINT | 近 3 天合伙人投诉 UV |
| `complaint_partner_uv_5d` | BIGINT | 近 5 天合伙人投诉 UV |
| `complaint_partner_uv_7d` | BIGINT | 近 7 天合伙人投诉 UV |
| `complaint_partner_uv_15d` | BIGINT | 近 15 天合伙人投诉 UV |
| `complaint_partner_uv_30d` | BIGINT | 近 30 天合伙人投诉 UV |
| `complaint_partner_uv_60d` | BIGINT | 近 60 天合伙人投诉 UV |
| `complaint_partner_uv_90d` | BIGINT | 近 90 天合伙人投诉 UV |

#### 3.2.6 判定标记位

| 字段 | 类型 | 说明 |
|------|------|------|
| `is_rec_ai_recognized` | INT | 是否被算法 AI 识别（0=否, 1=是），来自 `source = 3` |
| `is_partner_judged` | INT | 是否被合伙人众裁（0=否, 1=是），来自 `source = 4` |

> 💡 标记位仅参与 `MAX` 聚合，不进入合伙人 join 逻辑。

### 3.3 计算逻辑

#### 3.3.1 双段 UNION ALL 结构

> ⚠️ **本任务采用 UNION ALL 两段写法**，避免在单段 SQL 中混合 `source IN (1,2)` 与 `source IN (3,4)` 两种语义导致的可读性下降与 NULL key 数据倾斜。

| 段 | source 范围 | 是否 join 合伙人表 | 输出列 |
|---|------------|------------------|-------|
| 第一段 | `source IN (1, 2)` | ✅ LEFT JOIN | 27 个 UV 指标，标记位补 0 |
| 第二段 | `source IN (3, 4)` | ❌ 不 join | UV 全部补 0，标记位 `MAX(CASE WHEN source=3 THEN 1 ELSE 0 END)` 等 |

外层再按 `postId, blogId` 聚合：UV 列 `SUM`、标记位 `MAX`，得到一行结果。

#### 3.3.2 投诉用户分类规则（仅第一段）

通过 LEFT JOIN `lofter_db_dump.ods_db_partner_apply_job_nd`（合伙人申请表）判断投诉发起人是否为**有效合伙人**：

| 条件 | 说明 |
|------|------|
| `partnerType = 5` | 合伙人类型 = 5 |
| `status = 0` | 状态正常 |
| `endTime >= ${azkaban.flow.1.days.ago}` | 合伙人资格未过期 |

- `t2.userId IS NULL`  → 普通用户 → 计入 `complaint_user_uv_*`
- `t2.userId IS NOT NULL` → 合伙人 → 计入 `complaint_partner_uv_*`

> ⚠️ **合伙人申请表必须 `GROUP BY blogId` 去重**：同一合伙人历史可能有多条申请记录，未去重会导致 join 后行数放大、shuffle 数据量倍增。

#### 3.3.3 时间窗口判定规则

基于 `t1.createDate`（投诉创建日期）与运行日期 T-1 的差值：

```sql
datediff('${azkaban.flow.1.days.ago}', t1.createDate) < N
```

| N 值 | 含义 |
|------|------|
| `< 1` | 仅当天（近 1 天） |
| `< 3` | 近 3 天 |
| `< 5` | 近 5 天 |
| `< 7` | 近 7 天 |
| `< 15` | 近 15 天 |
| `< 30` | 近 30 天 |
| `< 60` | 近 60 天 |
| `< 90` | 近 90 天 |

#### 3.3.4 性能设计

- **第一段**只扫 `source IN (1, 2)` 的明细，把 `source` 谓词下推至 t1 子查询的 WHERE，源端过滤不进入 shuffle
- **合伙人维表**先 `GROUP BY blogId` 去重再参与 join，避免行数放大
- 一次 `GROUP BY postId, blogId` 同段内完成 27 个 UV 指标计算（`COUNT(DISTINCT CASE WHEN ...)`）
- 外层 `SUM` / `MAX` 拆平 UNION 结果，每个 (postId, blogId) 仍保留单行

### 3.4 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter.dws_ecology_ai_infringe_post_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT postId,
       blogId,
       SUM(complaint_uv)              AS complaint_uv,
       SUM(complaint_user_uv)         AS complaint_user_uv,
       SUM(complaint_partner_uv)      AS complaint_partner_uv,
       SUM(complaint_uv_1d)           AS complaint_uv_1d,
       SUM(complaint_uv_3d)           AS complaint_uv_3d,
       SUM(complaint_uv_5d)           AS complaint_uv_5d,
       SUM(complaint_uv_7d)           AS complaint_uv_7d,
       SUM(complaint_uv_15d)          AS complaint_uv_15d,
       SUM(complaint_uv_30d)          AS complaint_uv_30d,
       SUM(complaint_uv_60d)          AS complaint_uv_60d,
       SUM(complaint_uv_90d)          AS complaint_uv_90d,
       SUM(complaint_user_uv_1d)      AS complaint_user_uv_1d,
       SUM(complaint_user_uv_3d)      AS complaint_user_uv_3d,
       SUM(complaint_user_uv_5d)      AS complaint_user_uv_5d,
       SUM(complaint_user_uv_7d)      AS complaint_user_uv_7d,
       SUM(complaint_user_uv_15d)     AS complaint_user_uv_15d,
       SUM(complaint_user_uv_30d)     AS complaint_user_uv_30d,
       SUM(complaint_user_uv_60d)     AS complaint_user_uv_60d,
       SUM(complaint_user_uv_90d)     AS complaint_user_uv_90d,
       SUM(complaint_partner_uv_1d)   AS complaint_partner_uv_1d,
       SUM(complaint_partner_uv_3d)   AS complaint_partner_uv_3d,
       SUM(complaint_partner_uv_5d)   AS complaint_partner_uv_5d,
       SUM(complaint_partner_uv_7d)   AS complaint_partner_uv_7d,
       SUM(complaint_partner_uv_15d)  AS complaint_partner_uv_15d,
       SUM(complaint_partner_uv_30d)  AS complaint_partner_uv_30d,
       SUM(complaint_partner_uv_60d)  AS complaint_partner_uv_60d,
       SUM(complaint_partner_uv_90d)  AS complaint_partner_uv_90d,
       MAX(is_rec_ai_recognized)      AS is_rec_ai_recognized,
       MAX(is_partner_judged)         AS is_partner_judged
FROM (
    /* 第一段：source IN (1, 2) — 用户/侵权举报投诉 UV */
    SELECT postId,
           blogId,
           COUNT(DISTINCT t1.userId) AS complaint_uv,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL THEN t1.userId END)     AS complaint_user_uv,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL THEN t1.userId END) AS complaint_partner_uv,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_uv_1d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_uv_3d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_uv_5d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_uv_7d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_uv_15d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_uv_30d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_uv_60d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_uv_90d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_user_uv_1d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_user_uv_3d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_user_uv_5d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_user_uv_7d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_user_uv_15d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_user_uv_30d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_user_uv_60d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_user_uv_90d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_partner_uv_1d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_partner_uv_3d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_partner_uv_5d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_partner_uv_7d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_partner_uv_15d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_partner_uv_30d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_partner_uv_60d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_partner_uv_90d,
           0 AS is_rec_ai_recognized,
           0 AS is_partner_judged
    FROM (
        SELECT postId, blogId, userId, createDate
        FROM lofter.dwd_ecology_ai_infringe_post_dd
        WHERE dt = '${azkaban.flow.1.days.ago}' AND source IN (1, 2)
    ) t1
    LEFT JOIN (
        SELECT blogId AS userId
        FROM lofter_db_dump.ods_db_partner_apply_job_nd
        WHERE partnerType = 5
          AND status = 0
          AND from_unixtime(cast(endTime/1000 AS bigint), 'yyyy-MM-dd') >= '${azkaban.flow.1.days.ago}'
        GROUP BY blogId
    ) t2
    ON t1.userId = t2.userId
    GROUP BY postId, blogId

    UNION ALL

    /* 第二段：source IN (3, 4) — AI 识别 / 合伙人众裁 标记位 */
    SELECT postId,
           blogId,
           0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           MAX(CASE WHEN source = 3 THEN 1 ELSE 0 END) AS is_rec_ai_recognized,
           MAX(CASE WHEN source = 4 THEN 1 ELSE 0 END) AS is_partner_judged
    FROM lofter.dwd_ecology_ai_infringe_post_dd
    WHERE dt = '${azkaban.flow.1.days.ago}' AND source IN (3, 4)
    GROUP BY postId, blogId
) merged
GROUP BY postId, blogId
```

### 3.5 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter.dws_ecology_ai_infringe_post_dd (
    postId                    BIGINT  COMMENT '文章ID',
    blogId                    BIGINT  COMMENT '博客ID/创作者ID',
    complaint_uv              BIGINT  COMMENT '总投诉UV',
    complaint_user_uv         BIGINT  COMMENT '普通用户投诉UV（非合伙人）',
    complaint_partner_uv      BIGINT  COMMENT '合伙人投诉UV',
    complaint_uv_1d           BIGINT  COMMENT '近1天总投诉UV',
    complaint_uv_3d           BIGINT  COMMENT '近3天总投诉UV',
    complaint_uv_5d           BIGINT  COMMENT '近5天总投诉UV',
    complaint_uv_7d           BIGINT  COMMENT '近7天总投诉UV',
    complaint_uv_15d          BIGINT  COMMENT '近15天总投诉UV',
    complaint_uv_30d          BIGINT  COMMENT '近30天总投诉UV',
    complaint_uv_60d          BIGINT  COMMENT '近60天总投诉UV',
    complaint_uv_90d          BIGINT  COMMENT '近90天总投诉UV',
    complaint_user_uv_1d      BIGINT  COMMENT '近1天普通用户投诉UV',
    complaint_user_uv_3d      BIGINT  COMMENT '近3天普通用户投诉UV',
    complaint_user_uv_5d      BIGINT  COMMENT '近5天普通用户投诉UV',
    complaint_user_uv_7d      BIGINT  COMMENT '近7天普通用户投诉UV',
    complaint_user_uv_15d     BIGINT  COMMENT '近15天普通用户投诉UV',
    complaint_user_uv_30d     BIGINT  COMMENT '近30天普通用户投诉UV',
    complaint_user_uv_60d     BIGINT  COMMENT '近60天普通用户投诉UV',
    complaint_user_uv_90d     BIGINT  COMMENT '近90天普通用户投诉UV',
    complaint_partner_uv_1d   BIGINT  COMMENT '近1天合伙人投诉UV',
    complaint_partner_uv_3d   BIGINT  COMMENT '近3天合伙人投诉UV',
    complaint_partner_uv_5d   BIGINT  COMMENT '近5天合伙人投诉UV',
    complaint_partner_uv_7d   BIGINT  COMMENT '近7天合伙人投诉UV',
    complaint_partner_uv_15d  BIGINT  COMMENT '近15天合伙人投诉UV',
    complaint_partner_uv_30d  BIGINT  COMMENT '近30天合伙人投诉UV',
    complaint_partner_uv_60d  BIGINT  COMMENT '近60天合伙人投诉UV',
    complaint_partner_uv_90d  BIGINT  COMMENT '近90天合伙人投诉UV',
    is_rec_ai_recognized      INT     COMMENT '是否被算法AI识别(0=否, 1=是)',
    is_partner_judged         INT     COMMENT '是否被合伙人众裁(0=否, 1=是)'
)
COMMENT 'AI侵权投诉文章日全量汇总表'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

## 4. DWS 汇总表 — `dws_ecology_ai_infringe_blog_dd`

### 4.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter.dws_ecology_ai_infringe_blog_dd` |
| 层级 | DWS（汇总层） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 博客 / 创作者 (blogId) |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1，每日 1 次 |
| 上游表 | `lofter.dwd_ecology_ai_infringe_post_dd`<br>`lofter_db_dump.ods_db_partner_apply_job_nd` |
| 业务用途 | 评估**单个创作者**的整体被投诉情况，识别高投诉创作者用于风控决策 |

### 4.2 字段定义

按 **博客粒度（blogId）** 聚合：三类投诉 UV × 8 个时间窗口 = 27 个 UV 指标，外加 2 个生命周期日期字段、2 个 AI/合伙人文章数指标。

#### 4.2.1 维度键

| 字段 | 类型 | 说明 |
|------|------|------|
| `blogId` | BIGINT | 博客ID/创作者ID |

#### 4.2.2 全量 UV 指标（不限时间）

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_uv` | BIGINT | 总投诉 UV（去重投诉人数） |
| `complaint_user_uv` | BIGINT | 普通用户投诉 UV（非合伙人） |
| `complaint_partner_uv` | BIGINT | 合伙人投诉 UV |

#### 4.2.3 总投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_uv_1d` | BIGINT | 近 1 天总投诉 UV |
| `complaint_uv_3d` | BIGINT | 近 3 天总投诉 UV |
| `complaint_uv_5d` | BIGINT | 近 5 天总投诉 UV |
| `complaint_uv_7d` | BIGINT | 近 7 天总投诉 UV |
| `complaint_uv_15d` | BIGINT | 近 15 天总投诉 UV |
| `complaint_uv_30d` | BIGINT | 近 30 天总投诉 UV |
| `complaint_uv_60d` | BIGINT | 近 60 天总投诉 UV |
| `complaint_uv_90d` | BIGINT | 近 90 天总投诉 UV |

#### 4.2.4 普通用户投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_user_uv_1d` | BIGINT | 近 1 天普通用户投诉 UV |
| `complaint_user_uv_3d` | BIGINT | 近 3 天普通用户投诉 UV |
| `complaint_user_uv_5d` | BIGINT | 近 5 天普通用户投诉 UV |
| `complaint_user_uv_7d` | BIGINT | 近 7 天普通用户投诉 UV |
| `complaint_user_uv_15d` | BIGINT | 近 15 天普通用户投诉 UV |
| `complaint_user_uv_30d` | BIGINT | 近 30 天普通用户投诉 UV |
| `complaint_user_uv_60d` | BIGINT | 近 60 天普通用户投诉 UV |
| `complaint_user_uv_90d` | BIGINT | 近 90 天普通用户投诉 UV |

#### 4.2.5 合伙人投诉 UV — 多窗口

| 字段 | 类型 | 说明 |
|------|------|------|
| `complaint_partner_uv_1d` | BIGINT | 近 1 天合伙人投诉 UV |
| `complaint_partner_uv_3d` | BIGINT | 近 3 天合伙人投诉 UV |
| `complaint_partner_uv_5d` | BIGINT | 近 5 天合伙人投诉 UV |
| `complaint_partner_uv_7d` | BIGINT | 近 7 天合伙人投诉 UV |
| `complaint_partner_uv_15d` | BIGINT | 近 15 天合伙人投诉 UV |
| `complaint_partner_uv_30d` | BIGINT | 近 30 天合伙人投诉 UV |
| `complaint_partner_uv_60d` | BIGINT | 近 60 天合伙人投诉 UV |
| `complaint_partner_uv_90d` | BIGINT | 近 90 天合伙人投诉 UV |

#### 4.2.6 投诉生命周期日期

| 字段 | 类型 | 说明 |
|------|------|------|
| `first_complaint_date` | STRING | 创作者首次被投诉日期（yyyy-MM-dd），仅基于 `source IN (1, 2)` 计算 |
| `last_complaint_date` | STRING | 创作者最新被投诉日期（yyyy-MM-dd），仅基于 `source IN (1, 2)` 计算 |

> 💡 由于上游 `dwd_ecology_ai_infringe_post_dd` 的当前 `dt` 分区即**全量明细**（非当日增量），`min/max` 直接得到该创作者历史首次与最新被投诉日期，无需扫多分区。

#### 4.2.7 AI / 合伙人判定文章数

| 字段 | 类型 | 说明 |
|------|------|------|
| `rec_ai_recognition_cnt` | BIGINT | 该创作者名下被算法 AI 识别（`source = 3`）的文章数（去重 postId） |
| `partner_judge_cnt` | BIGINT | 该创作者名下被合伙人众裁（`source = 4`）的文章数（去重 postId） |

> 💡 与文章粒度（§3.2.6）的 `is_rec_ai_recognized` / `is_partner_judged`（0/1 标记位）不同，blog 粒度采用**计数语义**，统计该创作者下被 AI/合伙人判定过的不同文章数。

### 4.3 计算逻辑

#### 4.3.1 双段 UNION ALL 结构

> ⚠️ **本任务采用 UNION ALL 两段写法**，避免在单段 SQL 中混合 `source IN (1,2)` 与 `source IN (3,4)` 两种语义导致的可读性下降与 NULL key 数据倾斜。

| 段 | source 范围 | 是否 join 合伙人表 | 输出列 |
|---|------------|------------------|-------|
| 第一段 | `source IN (1, 2)` | ✅ LEFT JOIN | 27 个 UV 指标 + `first/last_complaint_date`，AI/合伙人计数补 0 |
| 第二段 | `source IN (3, 4)` | ❌ 不 join | UV 与日期补 NULL/0，`rec_ai_recognition_cnt` / `partner_judge_cnt` 用 `COUNT(DISTINCT CASE source=3/4 THEN postId END)` |

外层再按 `blogId` 聚合：UV / 文章数列 `SUM`、首末日期 `MIN`/`MAX`，得到每个创作者一行结果。

#### 4.3.2 与 post 粒度的差异

与 `dws_ecology_ai_infringe_post_dd` 的计算规则完全一致（投诉用户分类、时间窗口、合伙人维表去重、SQL 双段结构），**唯一区别在于聚合粒度**：

| 项 | 文章维度（post） | 博客维度（blog） |
|----|---------------|---------------|
| **`GROUP BY` 字段** | `postId, blogId` | `blogId` |
| **维度键列数** | 2 | 1 |
| **UV 含义** | 单篇文章的去重投诉人数 | 单个创作者所有文章的合并去重投诉人数 |
| **AI/合伙人字段** | 0/1 标记位（`is_rec_ai_recognized`、`is_partner_judged`） | 文章数（`rec_ai_recognition_cnt`、`partner_judge_cnt`） |
| **生命周期日期** | — | `first_complaint_date` / `last_complaint_date` |

> 💡 同一用户对同一创作者的多篇文章投诉，在 blog 维度只算 1 个 UV；在 post 维度每篇都算 1 个 UV。

详见 [3.3 计算逻辑](#33-计算逻辑) 章节中的"投诉用户分类规则、时间窗口判定规则、性能设计"等通用规则。

### 4.4 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter.dws_ecology_ai_infringe_blog_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT blogId,
       SUM(complaint_uv)              AS complaint_uv,
       SUM(complaint_user_uv)         AS complaint_user_uv,
       SUM(complaint_partner_uv)      AS complaint_partner_uv,
       SUM(complaint_uv_1d)           AS complaint_uv_1d,
       SUM(complaint_uv_3d)           AS complaint_uv_3d,
       SUM(complaint_uv_5d)           AS complaint_uv_5d,
       SUM(complaint_uv_7d)           AS complaint_uv_7d,
       SUM(complaint_uv_15d)          AS complaint_uv_15d,
       SUM(complaint_uv_30d)          AS complaint_uv_30d,
       SUM(complaint_uv_60d)          AS complaint_uv_60d,
       SUM(complaint_uv_90d)          AS complaint_uv_90d,
       SUM(complaint_user_uv_1d)      AS complaint_user_uv_1d,
       SUM(complaint_user_uv_3d)      AS complaint_user_uv_3d,
       SUM(complaint_user_uv_5d)      AS complaint_user_uv_5d,
       SUM(complaint_user_uv_7d)      AS complaint_user_uv_7d,
       SUM(complaint_user_uv_15d)     AS complaint_user_uv_15d,
       SUM(complaint_user_uv_30d)     AS complaint_user_uv_30d,
       SUM(complaint_user_uv_60d)     AS complaint_user_uv_60d,
       SUM(complaint_user_uv_90d)     AS complaint_user_uv_90d,
       SUM(complaint_partner_uv_1d)   AS complaint_partner_uv_1d,
       SUM(complaint_partner_uv_3d)   AS complaint_partner_uv_3d,
       SUM(complaint_partner_uv_5d)   AS complaint_partner_uv_5d,
       SUM(complaint_partner_uv_7d)   AS complaint_partner_uv_7d,
       SUM(complaint_partner_uv_15d)  AS complaint_partner_uv_15d,
       SUM(complaint_partner_uv_30d)  AS complaint_partner_uv_30d,
       SUM(complaint_partner_uv_60d)  AS complaint_partner_uv_60d,
       SUM(complaint_partner_uv_90d)  AS complaint_partner_uv_90d,
       MIN(first_complaint_date)      AS first_complaint_date,
       MAX(last_complaint_date)       AS last_complaint_date,
       SUM(rec_ai_recognition_cnt)    AS rec_ai_recognition_cnt,
       SUM(partner_judge_cnt)         AS partner_judge_cnt
FROM (
    /* 第一段：source IN (1, 2) — 用户/侵权举报投诉 UV + 首末日期 */
    SELECT blogId,
           COUNT(DISTINCT t1.userId) AS complaint_uv,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL THEN t1.userId END)     AS complaint_user_uv,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL THEN t1.userId END) AS complaint_partner_uv,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_uv_1d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_uv_3d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_uv_5d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_uv_7d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_uv_15d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_uv_30d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_uv_60d,
           COUNT(DISTINCT CASE WHEN datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_uv_90d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_user_uv_1d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_user_uv_3d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_user_uv_5d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_user_uv_7d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_user_uv_15d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_user_uv_30d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_user_uv_60d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NULL     AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_user_uv_90d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1  THEN t1.userId END) AS complaint_partner_uv_1d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3  THEN t1.userId END) AS complaint_partner_uv_3d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5  THEN t1.userId END) AS complaint_partner_uv_5d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7  THEN t1.userId END) AS complaint_partner_uv_7d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 THEN t1.userId END) AS complaint_partner_uv_15d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 THEN t1.userId END) AS complaint_partner_uv_30d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 THEN t1.userId END) AS complaint_partner_uv_60d,
           COUNT(DISTINCT CASE WHEN t2.userId IS NOT NULL AND datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 THEN t1.userId END) AS complaint_partner_uv_90d,
           MIN(t1.createDate) AS first_complaint_date,
           MAX(t1.createDate) AS last_complaint_date,
           0 AS rec_ai_recognition_cnt,
           0 AS partner_judge_cnt
    FROM (
        SELECT postId, blogId, userId, createDate
        FROM lofter.dwd_ecology_ai_infringe_post_dd
        WHERE dt = '${azkaban.flow.1.days.ago}' AND source IN (1, 2)
    ) t1
    LEFT JOIN (
        SELECT blogId AS userId
        FROM lofter_db_dump.ods_db_partner_apply_job_nd
        WHERE partnerType = 5
          AND status = 0
          AND from_unixtime(cast(endTime/1000 AS bigint), 'yyyy-MM-dd') >= '${azkaban.flow.1.days.ago}'
        GROUP BY blogId
    ) t2
    ON t1.userId = t2.userId
    GROUP BY blogId

    UNION ALL

    /* 第二段：source IN (3, 4) — AI 识别 / 合伙人众裁 文章数 */
    SELECT blogId,
           0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0,
           CAST(NULL AS STRING) AS first_complaint_date,
           CAST(NULL AS STRING) AS last_complaint_date,
           COUNT(DISTINCT CASE WHEN source = 3 THEN postId END) AS rec_ai_recognition_cnt,
           COUNT(DISTINCT CASE WHEN source = 4 THEN postId END) AS partner_judge_cnt
    FROM lofter.dwd_ecology_ai_infringe_post_dd
    WHERE dt = '${azkaban.flow.1.days.ago}' AND source IN (3, 4)
    GROUP BY blogId
) merged
GROUP BY blogId
```

### 4.5 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter.dws_ecology_ai_infringe_blog_dd (
    blogId                    BIGINT  COMMENT '博客ID/创作者ID',
    complaint_uv              BIGINT  COMMENT '总投诉UV',
    complaint_user_uv         BIGINT  COMMENT '普通用户投诉UV（非合伙人）',
    complaint_partner_uv      BIGINT  COMMENT '合伙人投诉UV',
    complaint_uv_1d           BIGINT  COMMENT '近1天总投诉UV',
    complaint_uv_3d           BIGINT  COMMENT '近3天总投诉UV',
    complaint_uv_5d           BIGINT  COMMENT '近5天总投诉UV',
    complaint_uv_7d           BIGINT  COMMENT '近7天总投诉UV',
    complaint_uv_15d          BIGINT  COMMENT '近15天总投诉UV',
    complaint_uv_30d          BIGINT  COMMENT '近30天总投诉UV',
    complaint_uv_60d          BIGINT  COMMENT '近60天总投诉UV',
    complaint_uv_90d          BIGINT  COMMENT '近90天总投诉UV',
    complaint_user_uv_1d      BIGINT  COMMENT '近1天普通用户投诉UV',
    complaint_user_uv_3d      BIGINT  COMMENT '近3天普通用户投诉UV',
    complaint_user_uv_5d      BIGINT  COMMENT '近5天普通用户投诉UV',
    complaint_user_uv_7d      BIGINT  COMMENT '近7天普通用户投诉UV',
    complaint_user_uv_15d     BIGINT  COMMENT '近15天普通用户投诉UV',
    complaint_user_uv_30d     BIGINT  COMMENT '近30天普通用户投诉UV',
    complaint_user_uv_60d     BIGINT  COMMENT '近60天普通用户投诉UV',
    complaint_user_uv_90d     BIGINT  COMMENT '近90天普通用户投诉UV',
    complaint_partner_uv_1d   BIGINT  COMMENT '近1天合伙人投诉UV',
    complaint_partner_uv_3d   BIGINT  COMMENT '近3天合伙人投诉UV',
    complaint_partner_uv_5d   BIGINT  COMMENT '近5天合伙人投诉UV',
    complaint_partner_uv_7d   BIGINT  COMMENT '近7天合伙人投诉UV',
    complaint_partner_uv_15d  BIGINT  COMMENT '近15天合伙人投诉UV',
    complaint_partner_uv_30d  BIGINT  COMMENT '近30天合伙人投诉UV',
    complaint_partner_uv_60d  BIGINT  COMMENT '近60天合伙人投诉UV',
    complaint_partner_uv_90d  BIGINT  COMMENT '近90天合伙人投诉UV',
    first_complaint_date      STRING  COMMENT '创作者首次被投诉日期',
    last_complaint_date       STRING  COMMENT '创作者最新被投诉日期',
    rec_ai_recognition_cnt    BIGINT  COMMENT '创作者AI识别文章的数量',
    partner_judge_cnt         BIGINT  COMMENT '创作者被合伙人众裁的文章数量'
)
COMMENT 'AI侵权投诉博客（创作者）日全量汇总表'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

## 5. ADS 应用层 — 创作者运营平台透传

ADS 层提供三张透传表，分别对接明细、文章粒度与博客粒度的下游消费场景。三表均**完全透传**对应上游层的 schema（按列名 SELECT，不做二次加工），用于解耦数仓内层与运营平台直查口径。

| 表 | 上游表 | 粒度 | 下游消费场景 |
|----|-------|------|-------------|
| `lofter_dm.ads_cp_data_ai_infringe_detail_dd` | `lofter.dwd_ecology_ai_infringe_post_dd` | postId × userId × source（明细，未聚合） | 投诉明细查询 / 单条投诉溯源 / 自定义聚合 |
| `lofter_dm.ads_cp_data_ai_infringe_post_dd` | `lofter.dws_ecology_ai_infringe_post_dd` | postId × blogId | 高投诉文章列表 / 内容审核派发 |
| `lofter_dm.ads_cp_data_ai_infringe_blog_dd` | `lofter.dws_ecology_ai_infringe_blog_dd` | blogId | 创作者风险画像 / 高投诉创作者列表 / 巡查任务派发 |

> ⚠️ 三表通用约定：**字段顺序必须与对应上游表完全一致**（按列名 SELECT 而非 `SELECT *`），上游 schema 变更时必须同步更新本任务及 ADS 表 DDL。

---

### 5.1 明细粒度 — `ads_cp_data_ai_infringe_detail_dd`

#### 5.1.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter_dm.ads_cp_data_ai_infringe_detail_dd` |
| 层级 | ADS（应用层 / 创作者运营平台 cp_data 主题） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 文章 × 投诉用户 × 来源（明细，未聚合） |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1，每日 1 次（依赖 `dwd_ecology_ai_infringe_post_dd` 产出后触发） |
| 上游表 | `lofter.dwd_ecology_ai_infringe_post_dd` |
| Job 路径 | `analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_detail_dd.job` |

#### 5.1.2 字段定义

完全透传 DWD 明细表，6 列：

| 字段 | 类型 | 说明 |
|------|------|------|
| `postId` | BIGINT | 文章ID |
| `blogId` | BIGINT | 被投诉文章所属博客ID/创作者ID |
| `userId` | BIGINT | 投诉发起人用户ID |
| `source` | INT | 投诉来源：`1` = 推荐标记 (aiContent)；`2` = 侵权举报 (reportType in 2,3) |
| `createTime` | BIGINT | 投诉创建时间戳（毫秒） |
| `createDate` | STRING | 投诉创建日期（yyyy-MM-dd），由 `createTime` 转换得到 |

字段语义与 DWD 层完全一致，详见 [2.2 字段定义](#22-字段定义)。

> 💡 与 `ads_cp_data_ai_infringe_post_dd` / `blog_dd` 不同，本表保留**未聚合的原始投诉记录**，供 CP 平台做二次自定义聚合或单条投诉溯源使用。

#### 5.1.3 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter_dm.ads_cp_data_ai_infringe_detail_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT postId,
       blogId,
       userId,
       source,
       createTime,
       createDate
FROM lofter.dwd_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
```

#### 5.1.4 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter_dm.ads_cp_data_ai_infringe_detail_dd (
    postId      BIGINT  COMMENT '文章ID',
    blogId      BIGINT  COMMENT '博客ID/创作者ID',
    userId      BIGINT  COMMENT '投诉发起人用户ID',
    source      INT     COMMENT '1: 负反馈卡片，2: 侵权后台，3: 算法AI文识别，4: 合伙人众裁',
    createTime  BIGINT  COMMENT '投诉创建时间戳(毫秒)',
    createDate  STRING  COMMENT '投诉创建日期(yyyy-MM-dd)'
)
COMMENT 'AI侵权投诉文章明细表 — 创作者运营平台ADS透传'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

### 5.2 文章粒度 — `ads_cp_data_ai_infringe_post_dd`

#### 5.2.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter_dm.ads_cp_data_ai_infringe_post_dd` |
| 层级 | ADS（应用层 / 创作者运营平台 cp_data 主题） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 文章 (postId × blogId) |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1,每日 1 次（依赖 `dws_ecology_ai_infringe_post_dd` 产出后触发） |
| 上游表 | `lofter.dws_ecology_ai_infringe_post_dd` |
| Job 路径 | `analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_post_dd.job` |

#### 5.2.2 字段定义

完全透传 DWS 文章粒度表，27 个 UV 指标 + 2 个维度键 + 2 个判定标记位：

- 维度键：`postId` · `blogId`
- 全量 UV：`complaint_uv` · `complaint_user_uv` · `complaint_partner_uv`
- 总投诉 UV 多窗口：`complaint_uv_{1,3,5,7,15,30,60,90}d`
- 普通用户 UV 多窗口：`complaint_user_uv_{1,3,5,7,15,30,60,90}d`
- 合伙人 UV 多窗口：`complaint_partner_uv_{1,3,5,7,15,30,60,90}d`
- 判定标记位：`is_rec_ai_recognized` · `is_partner_judged`

字段类型与 DWS 层完全一致，详见 [3.2 字段定义](#32-字段定义)。

> 💡 文章粒度**不包含** `first_complaint_date` / `last_complaint_date`，生命周期日期仅在博客粒度（§5.3）提供。

#### 5.2.3 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter_dm.ads_cp_data_ai_infringe_post_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT postId,
       blogId,
       complaint_uv,
       complaint_user_uv,
       complaint_partner_uv,
       complaint_uv_1d,  complaint_uv_3d,  complaint_uv_5d,  complaint_uv_7d,
       complaint_uv_15d, complaint_uv_30d, complaint_uv_60d, complaint_uv_90d,
       complaint_user_uv_1d,  complaint_user_uv_3d,  complaint_user_uv_5d,  complaint_user_uv_7d,
       complaint_user_uv_15d, complaint_user_uv_30d, complaint_user_uv_60d, complaint_user_uv_90d,
       complaint_partner_uv_1d,  complaint_partner_uv_3d,  complaint_partner_uv_5d,  complaint_partner_uv_7d,
       complaint_partner_uv_15d, complaint_partner_uv_30d, complaint_partner_uv_60d, complaint_partner_uv_90d,
       is_rec_ai_recognized,
       is_partner_judged
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
```

#### 5.2.4 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter_dm.ads_cp_data_ai_infringe_post_dd (
    postId                    BIGINT  COMMENT '文章ID',
    blogId                    BIGINT  COMMENT '博客ID/创作者ID',
    complaint_uv              BIGINT  COMMENT '总投诉UV',
    complaint_user_uv         BIGINT  COMMENT '普通用户投诉UV（非合伙人）',
    complaint_partner_uv      BIGINT  COMMENT '合伙人投诉UV',
    complaint_uv_1d           BIGINT  COMMENT '近1天总投诉UV',
    complaint_uv_3d           BIGINT  COMMENT '近3天总投诉UV',
    complaint_uv_5d           BIGINT  COMMENT '近5天总投诉UV',
    complaint_uv_7d           BIGINT  COMMENT '近7天总投诉UV',
    complaint_uv_15d          BIGINT  COMMENT '近15天总投诉UV',
    complaint_uv_30d          BIGINT  COMMENT '近30天总投诉UV',
    complaint_uv_60d          BIGINT  COMMENT '近60天总投诉UV',
    complaint_uv_90d          BIGINT  COMMENT '近90天总投诉UV',
    complaint_user_uv_1d      BIGINT  COMMENT '近1天普通用户投诉UV',
    complaint_user_uv_3d      BIGINT  COMMENT '近3天普通用户投诉UV',
    complaint_user_uv_5d      BIGINT  COMMENT '近5天普通用户投诉UV',
    complaint_user_uv_7d      BIGINT  COMMENT '近7天普通用户投诉UV',
    complaint_user_uv_15d     BIGINT  COMMENT '近15天普通用户投诉UV',
    complaint_user_uv_30d     BIGINT  COMMENT '近30天普通用户投诉UV',
    complaint_user_uv_60d     BIGINT  COMMENT '近60天普通用户投诉UV',
    complaint_user_uv_90d     BIGINT  COMMENT '近90天普通用户投诉UV',
    complaint_partner_uv_1d   BIGINT  COMMENT '近1天合伙人投诉UV',
    complaint_partner_uv_3d   BIGINT  COMMENT '近3天合伙人投诉UV',
    complaint_partner_uv_5d   BIGINT  COMMENT '近5天合伙人投诉UV',
    complaint_partner_uv_7d   BIGINT  COMMENT '近7天合伙人投诉UV',
    complaint_partner_uv_15d  BIGINT  COMMENT '近15天合伙人投诉UV',
    complaint_partner_uv_30d  BIGINT  COMMENT '近30天合伙人投诉UV',
    complaint_partner_uv_60d  BIGINT  COMMENT '近60天合伙人投诉UV',
    complaint_partner_uv_90d  BIGINT  COMMENT '近90天合伙人投诉UV',
    is_rec_ai_recognized      INT     COMMENT '是否被算法AI识别(0=否, 1=是)',
    is_partner_judged         INT     COMMENT '是否被合伙人众裁(0=否, 1=是)'
)
COMMENT 'AI侵权投诉文章日全量汇总表 — 创作者运营平台ADS透传'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

### 5.3 博客粒度 — `ads_cp_data_ai_infringe_blog_dd`

#### 5.3.1 表说明

| 项 | 内容 |
|----|------|
| 表名 | `lofter_dm.ads_cp_data_ai_infringe_blog_dd` |
| 层级 | ADS（应用层 / 创作者运营平台 cp_data 主题） |
| 后缀 | `_dd`（Day Delta，日全量快照） |
| 粒度 | 博客 / 创作者 (blogId) |
| 分区 | `dt`（yyyy-MM-dd），全量快照 |
| 调度 | T-1,每日 1 次（依赖 `dws_ecology_ai_infringe_blog_dd` 产出后触发） |
| 上游表 | `lofter.dws_ecology_ai_infringe_blog_dd` |
| Job 路径 | `analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_blog_dd.job` |

#### 5.3.2 字段定义

完全透传 DWS 博客粒度表，27 个 UV 指标 + 1 个维度键 + 2 个生命周期日期字段 + 2 个 AI/合伙人文章数指标：

- 维度键：`blogId`
- 全量 UV：`complaint_uv` · `complaint_user_uv` · `complaint_partner_uv`
- 总投诉 UV 多窗口：`complaint_uv_{1,3,5,7,15,30,60,90}d`
- 普通用户 UV 多窗口：`complaint_user_uv_{1,3,5,7,15,30,60,90}d`
- 合伙人 UV 多窗口：`complaint_partner_uv_{1,3,5,7,15,30,60,90}d`
- 投诉生命周期日期：`first_complaint_date` · `last_complaint_date`
- AI / 合伙人文章数：`rec_ai_recognition_cnt` · `partner_judge_cnt`

字段类型与 DWS 层完全一致，详见 [4.2 字段定义](#42-字段定义)。

#### 5.3.3 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter_dm.ads_cp_data_ai_infringe_blog_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT blogId,
       complaint_uv,
       complaint_user_uv,
       complaint_partner_uv,
       complaint_uv_1d,  complaint_uv_3d,  complaint_uv_5d,  complaint_uv_7d,
       complaint_uv_15d, complaint_uv_30d, complaint_uv_60d, complaint_uv_90d,
       complaint_user_uv_1d,  complaint_user_uv_3d,  complaint_user_uv_5d,  complaint_user_uv_7d,
       complaint_user_uv_15d, complaint_user_uv_30d, complaint_user_uv_60d, complaint_user_uv_90d,
       complaint_partner_uv_1d,  complaint_partner_uv_3d,  complaint_partner_uv_5d,  complaint_partner_uv_7d,
       complaint_partner_uv_15d, complaint_partner_uv_30d, complaint_partner_uv_60d, complaint_partner_uv_90d,
       first_complaint_date,
       last_complaint_date,
       rec_ai_recognition_cnt,
       partner_judge_cnt
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
```

#### 5.3.4 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter_dm.ads_cp_data_ai_infringe_blog_dd (
    blogId                    BIGINT  COMMENT '博客ID/创作者ID',
    complaint_uv              BIGINT  COMMENT '总投诉UV',
    complaint_user_uv         BIGINT  COMMENT '普通用户投诉UV（非合伙人）',
    complaint_partner_uv      BIGINT  COMMENT '合伙人投诉UV',
    complaint_uv_1d           BIGINT  COMMENT '近1天总投诉UV',
    complaint_uv_3d           BIGINT  COMMENT '近3天总投诉UV',
    complaint_uv_5d           BIGINT  COMMENT '近5天总投诉UV',
    complaint_uv_7d           BIGINT  COMMENT '近7天总投诉UV',
    complaint_uv_15d          BIGINT  COMMENT '近15天总投诉UV',
    complaint_uv_30d          BIGINT  COMMENT '近30天总投诉UV',
    complaint_uv_60d          BIGINT  COMMENT '近60天总投诉UV',
    complaint_uv_90d          BIGINT  COMMENT '近90天总投诉UV',
    complaint_user_uv_1d      BIGINT  COMMENT '近1天普通用户投诉UV',
    complaint_user_uv_3d      BIGINT  COMMENT '近3天普通用户投诉UV',
    complaint_user_uv_5d      BIGINT  COMMENT '近5天普通用户投诉UV',
    complaint_user_uv_7d      BIGINT  COMMENT '近7天普通用户投诉UV',
    complaint_user_uv_15d     BIGINT  COMMENT '近15天普通用户投诉UV',
    complaint_user_uv_30d     BIGINT  COMMENT '近30天普通用户投诉UV',
    complaint_user_uv_60d     BIGINT  COMMENT '近60天普通用户投诉UV',
    complaint_user_uv_90d     BIGINT  COMMENT '近90天普通用户投诉UV',
    complaint_partner_uv_1d   BIGINT  COMMENT '近1天合伙人投诉UV',
    complaint_partner_uv_3d   BIGINT  COMMENT '近3天合伙人投诉UV',
    complaint_partner_uv_5d   BIGINT  COMMENT '近5天合伙人投诉UV',
    complaint_partner_uv_7d   BIGINT  COMMENT '近7天合伙人投诉UV',
    complaint_partner_uv_15d  BIGINT  COMMENT '近15天合伙人投诉UV',
    complaint_partner_uv_30d  BIGINT  COMMENT '近30天合伙人投诉UV',
    complaint_partner_uv_60d  BIGINT  COMMENT '近60天合伙人投诉UV',
    complaint_partner_uv_90d  BIGINT  COMMENT '近90天合伙人投诉UV',
    first_complaint_date      STRING  COMMENT '创作者首次被投诉日期',
    last_complaint_date       STRING  COMMENT '创作者最新被投诉日期',
    rec_ai_recognition_cnt    BIGINT  COMMENT '创作者AI识别文章的数量',
    partner_judge_cnt         BIGINT  COMMENT '创作者被合伙人众裁的文章数量'
)
COMMENT 'AI侵权投诉博客（创作者）日全量汇总表 — 创作者运营平台ADS透传'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

### 5.4 增量字段 ALTER 语句

#### 5.4.1 博客粒度首末投诉日期（DWS + ADS blog）

```sql
-- DWS 层
ALTER TABLE lofter.dws_ecology_ai_infringe_blog_dd ADD COLUMNS (
    first_complaint_date STRING COMMENT '创作者首次被投诉日期',
    last_complaint_date  STRING COMMENT '创作者最新被投诉日期'
) CASCADE;

-- ADS 层（同步透传）
ALTER TABLE lofter_dm.ads_cp_data_ai_infringe_blog_dd ADD COLUMNS (
    first_complaint_date STRING COMMENT '创作者首次被投诉日期',
    last_complaint_date  STRING COMMENT '创作者最新被投诉日期'
) CASCADE;
```

#### 5.4.2 文章粒度 AI/合伙人判定标记位（DWS + ADS post）

```sql
-- DWS 层
ALTER TABLE lofter.dws_ecology_ai_infringe_post_dd ADD COLUMNS (
    is_rec_ai_recognized INT COMMENT '是否被算法AI识别(0=否, 1=是)',
    is_partner_judged    INT COMMENT '是否被合伙人众裁(0=否, 1=是)'
) CASCADE;

-- ADS 层（同步透传）
ALTER TABLE lofter_dm.ads_cp_data_ai_infringe_post_dd ADD COLUMNS (
    is_rec_ai_recognized INT COMMENT '是否被算法AI识别(0=否, 1=是)',
    is_partner_judged    INT COMMENT '是否被合伙人众裁(0=否, 1=是)'
) CASCADE;
```

#### 5.4.3 博客粒度 AI/合伙人文章数（DWS + ADS blog）

```sql
-- DWS 层
ALTER TABLE lofter.dws_ecology_ai_infringe_blog_dd ADD COLUMNS (
    rec_ai_recognition_cnt BIGINT COMMENT '创作者AI识别文章的数量',
    partner_judge_cnt      BIGINT COMMENT '创作者被合伙人众裁的文章数量'
) CASCADE;

-- ADS 层（同步透传）
ALTER TABLE lofter_dm.ads_cp_data_ai_infringe_blog_dd ADD COLUMNS (
    rec_ai_recognition_cnt BIGINT COMMENT '创作者AI识别文章的数量',
    partner_judge_cnt      BIGINT COMMENT '创作者被合伙人众裁的文章数量'
) CASCADE;
```

> ⚠️ **执行顺序**：必须先 `ALTER` 上下游两张表（DWS → ADS），再上线对应任务变更，否则 `INSERT OVERWRITE` 会因列数不匹配失败。`CASCADE` 用于让历史分区元数据同步包含新列定义。

---

## 6. 查询使用示例

### 6.1 文章维度

#### 6.1.1 查询某文章近 7 天的投诉情况

```sql
SELECT postId, blogId,
       complaint_uv_7d,
       complaint_user_uv_7d,
       complaint_partner_uv_7d
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND postId = <目标文章ID>;
```

#### 6.1.2 TOP 100 高投诉文章（近 30 天）

```sql
SELECT postId, blogId, complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
ORDER BY complaint_uv_30d DESC
LIMIT 100;
```

#### 6.1.3 合伙人投诉占比高的可疑文章

```sql
SELECT postId, blogId,
       complaint_uv_30d,
       complaint_partner_uv_30d,
       round(complaint_partner_uv_30d / complaint_uv_30d, 4) AS partner_ratio
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND complaint_uv_30d >= 5
  AND complaint_partner_uv_30d > 0
ORDER BY partner_ratio DESC, complaint_uv_30d DESC
LIMIT 100;
```

### 6.2 博客（创作者）维度

#### 6.2.1 查询某创作者近 7 天的投诉情况

```sql
SELECT blogId,
       complaint_uv_7d,
       complaint_user_uv_7d,
       complaint_partner_uv_7d
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND blogId = <目标博客ID>;
```

#### 6.2.2 TOP 100 高投诉创作者（近 30 天）

```sql
SELECT blogId, complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
ORDER BY complaint_uv_30d DESC
LIMIT 100;
```

#### 6.2.3 合伙人持续关注的高风险创作者（近 90 天）

```sql
SELECT blogId,
       complaint_uv_90d,
       complaint_partner_uv_90d,
       round(complaint_partner_uv_90d / complaint_uv_90d, 4) AS partner_ratio
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND complaint_partner_uv_90d >= 3
ORDER BY complaint_partner_uv_90d DESC, partner_ratio DESC
LIMIT 100;
```

#### 6.2.4 长期高风险创作者（首次被投诉久 + 至今仍在被投诉）

```sql
SELECT blogId,
       first_complaint_date,
       last_complaint_date,
       datediff(last_complaint_date, first_complaint_date) AS complaint_span_days,
       complaint_uv,
       complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND complaint_uv >= 10
  AND last_complaint_date >= date_sub('${azkaban.flow.1.days.ago}', 30)
ORDER BY complaint_span_days DESC, complaint_uv DESC
LIMIT 100;
```

#### 6.2.5 AI 内容批量发布者识别（按 AI 识别数排序）

```sql
SELECT blogId,
       rec_ai_recognition_cnt,
       partner_judge_cnt,
       complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_blog_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND rec_ai_recognition_cnt >= 3
ORDER BY rec_ai_recognition_cnt DESC, partner_judge_cnt DESC
LIMIT 100;
```

#### 6.2.6 同时被 AI 识别 + 合伙人众裁的双重判定文章（post 粒度）

```sql
SELECT postId, blogId,
       complaint_uv_30d,
       complaint_partner_uv_30d
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND is_rec_ai_recognized = 1
  AND is_partner_judged = 1
ORDER BY complaint_uv_30d DESC
LIMIT 100;
```

### 6.3 跨维度联合分析

#### 6.3.1 创作者整体 vs 单文章对比（识别 AI 内容批量发布者）

```sql
SELECT b.blogId,
       b.complaint_uv_30d AS blog_complaint_uv_30d,
       count(distinct p.postId) AS complained_post_cnt,
       max(p.complaint_uv_30d) AS max_post_complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_blog_dd b
LEFT JOIN lofter.dws_ecology_ai_infringe_post_dd p
  ON b.blogId = p.blogId AND p.dt = '${azkaban.flow.1.days.ago}'
WHERE b.dt = '${azkaban.flow.1.days.ago}'
  AND b.complaint_uv_30d >= 10
GROUP BY b.blogId, b.complaint_uv_30d
ORDER BY complained_post_cnt DESC, blog_complaint_uv_30d DESC
LIMIT 100;
```

---

## 7. 注意事项

| 项 | 说明 |
|----|------|
| **分区查询** | 五张表均为 `_dd` 后缀（日全量快照），查询时**必须只取最新一天**（`dt = '${azkaban.flow.1.days.ago}'`），跨分区会产生重复数据 |
| **source 语义** | DWD 的 `source` 取值：`1`=负反馈卡片、`2`=侵权后台、`3`=算法AI文识别、`4`=合伙人众裁。**前两类是用户行为**（`userId` / `createTime` 有值），**后两类是系统/合伙人判定**（`userId` / `createTime` 写入 NULL），参与的指标范围严格区分（详见下条） |
| **DWS join 范围（仅 source IN (1,2)）** | DWS post/blog 任务采用 **UNION ALL 双段写法**：第一段仅 `source IN (1,2)` 与合伙人维表 join 计算 27 个 UV 指标；第二段 `source IN (3,4)` 不 join，输出 AI/合伙人判定字段。避免 source=3/4 的 NULL userId 引入 join 倾斜 |
| **合伙人维表去重** | `ods_db_partner_apply_job_nd` 同一 blogId 可能有多条历史申请，**子查询必须 `GROUP BY blogId` 去重**后再 join，否则会导致行数放大、shuffle 数据量倍增 |
| **合伙人定义** | 当前以 `partnerType = 5 AND status = 0 AND endTime >= T-1` 识别有效合伙人，未来如新增合伙人类型需同步更新 DWS 层 SQL |
| **时间窗口语义** | `< N` 表示最近 N 天（含 T-1 当天），`< 1` 即只统计 T-1 当天 |
| **post vs blog 维度** | post 维度统计单文章去重投诉人数；blog 维度统计该创作者所有文章合并后的去重投诉人数（同人多帖只算 1 UV）。**两表 UV 不可直接相加**。 |
| **AI / 合伙人指标差异** | post 维度用 0/1 标记位（`is_rec_ai_recognized` / `is_partner_judged`）表达"该文章是否被判定过"；blog 维度用计数（`rec_ai_recognition_cnt` / `partner_judge_cnt`）表达"创作者下被判定过的文章数" |
| **首次/最新投诉日期** | 仅 blog 维度提供。基于 DWD 当前 `dt` 分区即全量明细的设定，`min/max(createDate)` 即为真正的历史首次与最新被投诉日期；若上游改为增量分区则需重新设计逻辑。仅基于 `source IN (1,2)` 计算，不包含 AI/合伙人判定时间 |
| **DWS → ADS 列对齐** | ADS 表通过显式列名 `SELECT` 自 DWS 透传，DWS schema 变更必须同步：①ALTER 两张表 → ②更新 ADS Job → ③上线发布，三步不可乱序 |
| **数据来源** | DWD 层的 `_nd` 上游表自动指向最新快照，无需指定 `dt` |
| **建表存储** | 使用 Parquet + SNAPPY 压缩，与 LOFTER 数仓默认存储格式一致，列式存储利于聚合查询 |

---

## 8. 维护信息

| 项 | 内容 |
|----|------|
| 业务域 | Ecology / AI Infringe |
| Job 路径 | `etl/jobs/dwd-ecology-ai-infringe/`<br>`etl/jobs/dws-ecology-ai-infringe/`<br>`analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_detail_dd.job`<br>`analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_post_dd.job`<br>`analysis/jobs/creator-operations-platform/ads_cp_data_ai_infringe_blog_dd.job` |
| 文档路径 | `etl/docs/ecology/ai-infringe/spec.md` |
| 调度参数 | `${azkaban.flow.1.days.ago}` (T-1) |
