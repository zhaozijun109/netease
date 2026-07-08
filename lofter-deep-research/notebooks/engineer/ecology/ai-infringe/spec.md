
# AI 侵权投诉数据 Spec

> **业务域**：生态治理 (Ecology) / AI 侵权投诉 (AI Infringe)  
> **责任表**：`lofter.dwd_ecology_ai_infringe_post_dd` · `lofter.dws_ecology_ai_infringe_post_dd`  
> **调度**：Azkaban，日级 T-1 调度，使用 `${azkaban.flow.1.days.ago}` 参数

---

## 1. 业务背景

LOFTER 平台为应对 AI 生成内容引发的侵权问题，建立了"用户举报 + 合伙人巡查"的双通道投诉机制。本数据链路用于沉淀文章维度的 AI 侵权投诉数据，支撑：

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
│                            │                                │
│                            ▼                                │
│  DWS 汇总层                                                  │
│    lofter.dws_ecology_ai_infringe_post_dd                   │
│    （文章粒度聚合 → UV + 多窗口指标）                        │
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
| 上游表 | `lofter_db_dump.ods_db_recommend_user_mark_nd`<br>`lofter_db_dump.ods_db_infringe_post_nd` |

### 2.2 字段定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `postId` | BIGINT | 文章ID |
| `blogId` | BIGINT | 被投诉文章所属博客ID/创作者ID |
| `userId` | BIGINT | 投诉发起人用户ID |
| `source` | INT | 投诉来源：`1` = 推荐标记 (aiContent)；`2` = 侵权举报 (reportType in 2,3) |
| `createTime` | BIGINT | 投诉创建时间戳（毫秒） |
| `createDate` | STRING | 投诉创建日期（yyyy-MM-dd），由 `createTime` 转换得到 |
| `dt` | STRING | 分区日期（yyyy-MM-dd） |

### 2.3 计算逻辑

通过 `UNION ALL` 合并两个来源的投诉数据：

| 来源 | 上游表 | 过滤条件 | source 值 |
|------|--------|---------|-----------|
| 推荐标记 | `ods_db_recommend_user_mark_nd` | `markType = 'aiContent'` | 1 |
| 侵权举报 | `ods_db_infringe_post_nd` | `reportType in (2, 3)` | 2 |

> **注意**：`ods_db_recommend_user_mark_nd` 的 `showId` 字段需 `cast(showId as bigint)` 后作为 `postId` 使用。

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
    SELECT cast(showId AS bigint) AS postId,
           blogId, userId, 1 AS source, createTime
    FROM lofter_db_dump.ods_db_recommend_user_mark_nd
    WHERE markType = 'aiContent'

    UNION ALL

    SELECT postId, blogId, userId, 2 AS source, createTime
    FROM lofter_db_dump.ods_db_infringe_post_nd
    WHERE reportType IN (2, 3)
) t
```

### 2.5 建表语句

```sql
CREATE TABLE IF NOT EXISTS lofter.dwd_ecology_ai_infringe_post_dd (
    postId      BIGINT  COMMENT '文章ID',
    blogId      BIGINT  COMMENT '博客ID/创作者ID',
    userId      BIGINT  COMMENT '投诉发起人用户ID',
    source      INT     COMMENT '投诉来源：1=推荐标记aiContent, 2=侵权举报reportType in 2,3',
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

### 3.2 字段定义

按 **文章粒度（postId + blogId）** 聚合，三类投诉 UV × 8 个时间窗口 = 27 个指标：

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

### 3.3 计算逻辑

#### 3.3.1 投诉用户分类规则

通过 LEFT JOIN `lofter_db_dump.ods_db_partner_apply_job_nd`（合伙人申请表）判断投诉发起人是否为**有效合伙人**：

| 条件 | 说明 |
|------|------|
| `partnerType = 5` | 合伙人类型 = 5 |
| `status = 0` | 状态正常 |
| `endTime >= ${azkaban.flow.1.days.ago}` | 合伙人资格未过期 |

- `t2.userId IS NULL`  → 普通用户 → 计入 `complaint_user_uv_*`
- `t2.userId IS NOT NULL` → 合伙人 → 计入 `complaint_partner_uv_*`

#### 3.3.2 时间窗口判定规则

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

#### 3.3.3 性能设计

- 一次 `GROUP BY postId, blogId` 完成 **27 个指标计算**，避免重复扫描
- 通过 `CASE WHEN` 在同一查询内分窗口聚合
- 使用 `count(distinct ... case when ...)` 实现按条件去重

### 3.4 SQL 实现

```sql
INSERT OVERWRITE TABLE lofter.dws_ecology_ai_infringe_post_dd
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT postId,
       blogId,
       count(distinct t1.userId) AS complaint_uv,
       count(distinct case when t2.userId is null then t1.userId end) AS complaint_user_uv,
       count(distinct case when t2.userId is not null then t1.userId end) AS complaint_partner_uv,
       -- 总投诉 UV 多窗口
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1 then t1.userId end) AS complaint_uv_1d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3 then t1.userId end) AS complaint_uv_3d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5 then t1.userId end) AS complaint_uv_5d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7 then t1.userId end) AS complaint_uv_7d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 then t1.userId end) AS complaint_uv_15d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 then t1.userId end) AS complaint_uv_30d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 then t1.userId end) AS complaint_uv_60d,
       count(distinct case when datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 then t1.userId end) AS complaint_uv_90d,
       -- 普通用户投诉 UV 多窗口
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1 then t1.userId end) AS complaint_user_uv_1d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3 then t1.userId end) AS complaint_user_uv_3d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5 then t1.userId end) AS complaint_user_uv_5d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7 then t1.userId end) AS complaint_user_uv_7d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 then t1.userId end) AS complaint_user_uv_15d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 then t1.userId end) AS complaint_user_uv_30d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 then t1.userId end) AS complaint_user_uv_60d,
       count(distinct case when t2.userId is null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 then t1.userId end) AS complaint_user_uv_90d,
       -- 合伙人投诉 UV 多窗口
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 1 then t1.userId end) AS complaint_partner_uv_1d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 3 then t1.userId end) AS complaint_partner_uv_3d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 5 then t1.userId end) AS complaint_partner_uv_5d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 7 then t1.userId end) AS complaint_partner_uv_7d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 15 then t1.userId end) AS complaint_partner_uv_15d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 30 then t1.userId end) AS complaint_partner_uv_30d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 60 then t1.userId end) AS complaint_partner_uv_60d,
       count(distinct case when t2.userId is not null and datediff('${azkaban.flow.1.days.ago}', t1.createDate) < 90 then t1.userId end) AS complaint_partner_uv_90d
FROM (
    SELECT postId, blogId, userId, createDate
    FROM lofter.dwd_ecology_ai_infringe_post_dd
    WHERE dt = '${azkaban.flow.1.days.ago}'
) t1
LEFT JOIN (
    SELECT blogId AS userId
    FROM lofter_db_dump.ods_db_partner_apply_job_nd
    WHERE partnerType = 5
      AND status = 0
      AND from_unixtime(cast(endTime/1000 AS bigint), 'yyyy-MM-dd') >= '${azkaban.flow.1.days.ago}'
) t2
ON t1.userId = t2.userId
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
    complaint_partner_uv_90d  BIGINT  COMMENT '近90天合伙人投诉UV'
)
COMMENT 'AI侵权投诉文章日全量汇总表'
PARTITIONED BY (dt STRING COMMENT '分区日期yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES ('parquet.compression' = 'SNAPPY');
```

---

## 4. 查询使用示例

### 4.1 查询某文章近 7 天的投诉情况

```sql
SELECT postId, blogId,
       complaint_uv_7d,
       complaint_user_uv_7d,
       complaint_partner_uv_7d
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND postId = <目标文章ID>;
```

### 4.2 TOP 100 高投诉文章（近 30 天）

```sql
SELECT postId, blogId, complaint_uv_30d
FROM lofter.dws_ecology_ai_infringe_post_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
ORDER BY complaint_uv_30d DESC
LIMIT 100;
```

### 4.3 合伙人投诉占比高的可疑文章

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

---

## 5. 注意事项

| 项 | 说明 |
|----|------|
| **分区查询** | 两张表均为 `_dd` 后缀（日全量快照），查询时**必须只取最新一天**（`dt = '${azkaban.flow.1.days.ago}'`），跨分区会产生重复数据 |
| **合伙人定义** | 当前以 `partnerType = 5` 识别，未来如新增合伙人类型，需同步更新 DWS 层 SQL |
| **时间窗口语义** | `< N` 表示最近 N 天（含 T-1 当天），`< 1` 即只统计 T-1 当天 |
| **数据来源** | DWD 层的 `_nd` 上游表自动指向最新快照，无需指定 `dt` |
| **建表存储** | 建议使用 ORC + SNAPPY 压缩，节省存储且查询性能优 |

---

## 6. 维护信息

| 项 | 内容 |
|----|------|
| 业务域 | Ecology / AI Infringe |
| Job 路径 | `etl/jobs/dwd-ecology-ai-infringe/`<br>`etl/jobs/dws-ecology-ai-infringe/` |
| 文档路径 | `etl/docs/ecology/ai-infringe/spec.md` |
| 调度参数 | `${azkaban.flow.1.days.ago}` (T-1) |
