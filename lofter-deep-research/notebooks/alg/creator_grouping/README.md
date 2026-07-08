# 创作者圈层分群项目

## 概述

本项目在 **创作者 × 圈层** 维度上，对 Lofter 社区创作者进行多维分群打标，输出到日更快照表 `lofter_dm.ads_creator_ip_grouping_dd`。

分群覆盖以下维度：

| 维度 | 标签字段 | 标签值 |
|---|---|---|
| 圈层发文稳定性 | `stability_tag` | A_稳定 / B_衰退 / C_发文不足3篇 / D_断更 |
| 发文质量 | `quality_tag` | **【已废弃·不可用】** 字段保留以兼容历史 Schema，写入 `NULL`，请勿使用 |
| 圈层活人感 | `vitality_tag` | A_活跃 / B_有温度 / C_沉默 |

> **注**：`quality_tag` 已下线，但相关的**质量过程指标**（`quality_post_cnt_180d`、`low_quality_post_cnt_180d`、`sample_post_cnt`、`quality_post_ratio`、`low_quality_post_ratio`）仍按原逻辑正常计算并输出，下游可直接基于过程指标自行加工。

**作者范围**：近 180 天在该圈层有图文/文字/视频发文的创作者（来源：`dwd_post_publish_di`）。

---

## 一、圈层发文稳定性（stability_tag）

### 定义规则

判断优先级从高到低依次执行：

| 优先级 | 标签 | 判断条件 |
|---|---|---|
| 1 | **C_发文不足3篇** | 近 180 天在该圈层发文总数 ≤ 3 篇（新人或低频偶发，不适合用趋势判断） |
| 2 | **D_断更** | 近 3 个自然发文周期内无发文（`days_since_last_post > personal_cadence_days × 3`） |
| 3 | **A_稳定** | 近 3 个周期内有发文，且近 30 天发文量 ≥ 前 30 天（31~60 天前）发文量 |
| 4 | **B_衰退** | 其余（近期发文在减少） |

### 关键指标

- **`personal_cadence_days`**：个人自然发文周期，用相邻两篇文章发布时间间隔的**中位数**（`PERCENTILE_APPROX`）计算；若仅有 1 篇文章无法计算，默认取 30 天兜底。
- **`post_count_30d_prev`**：前 30 天（即距今 31~60 天）在该圈层的发文数，用于衰退对比。
- 衰退判断对比窗口：近 30 天 vs 前 30 天，而非与全量历史对比，更敏感地捕捉近期下滑趋势。

---

## 二、发文质量过程指标（`quality_tag` 已废弃）

> ⚠️ **重要变更**：`quality_tag` 标签字段已下线，写入 `NULL`，请勿使用。
> 本节描述的是**仍在正常输出**的质量过程指标，下游可直接基于这些字段自行加工分群规则。
> 表中保留的过程指标字段：`quality_post_cnt_180d`、`low_quality_post_cnt_180d`、`sample_post_cnt`、`quality_post_ratio`、`low_quality_post_ratio`。

### 文章质量定义

**优质文章**：近 30 天行为下，文章在**该圈层**的 CES 得分 ≥ 该圈层 p80 阈值。

> CES（Content Engagement Score）= 评论数 × 5 + 热度行为数 × 1
> - 评论数：近 30 天他人对该文章的评论（含回复），来源 `ods_db_post_response_nd`
> - 热度行为：近 30 天点赞/推荐/转载次数，来源 `dwd_post_hot_di`
> - **CES 在圈层维度单独计算，不跨圈层取最大值**
> - p80 阈值基于该圈层内所有样本文章的 CES 分布计算

**低质文章**（满足其一）：

| 条件 | 维度 | 阈值 | 说明 |
|---|---|---|---|
| 条件1 | 文章级 | 推荐场景负反馈率 `dislike_pv_30d / discovery_expose_pv_30d ≥ 0.1%` | 来源：`dws_post_traffic_dd` |
| 条件2 | 作者级 | UV 负反馈率 `blocked_cnt_30d / post_click_uv_30d ≥ 10%`（需 `post_click_uv_30d ≥ 50`） | 命中则该作者在**所有圈层**均视为低质 |

### 作者 × 圈层质量过程指标

**样本范围**：作者在该圈层近 180 天发布的**最近 20 篇**文章（`ROW_NUMBER() ORDER BY publish_date DESC`，取 `rn ≤ 20`），行为统计窗口取近 30 天。

输出的过程指标（均为 `creator_id × ip` 维度）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `sample_post_cnt` | INT | 参与质量统计的样本文章数（最多 20 篇） |
| `quality_post_cnt_180d` | INT | 样本内被判为优质的文章数 |
| `low_quality_post_cnt_180d` | INT | 样本内被判为低质的文章数 |
| `quality_post_ratio` | DOUBLE | 优质文章占比 = `quality_post_cnt_180d / sample_post_cnt` |
| `low_quality_post_ratio` | DOUBLE | 低质文章占比 = `low_quality_post_cnt_180d / sample_post_cnt` |

> 历史的 `quality_tag` 打标规则（A_优质 / B_一般 / C_低质）已下线，代码中不再产生标签值，统一写 `NULL`。

---

## 三、圈层活人感（vitality_tag）

### 行为权重（统计窗口：近 30 天）

| 行为 | 权重 | 说明 |
|---|---|---|
| 回复评论 | ×5 | 回复他人或自己文章下的评论（`replyl1commentid IS NOT NULL`）|
| 评论他人文章 | ×5 | 主动评论（非回复），且被评论文章不属于自己 |
| 互动他人文章 | ×2 | 点赞/推荐/转载他人文章 |
| 浏览他人文章 | ×1 | 浏览他人文章（`is_real=1`，过滤自己文章） |

> **关键区分**：回复行为（`replyl1commentid != NULL`）允许回复自己文章下的评论（体现创作者与粉丝的互动）；但对自己文章的主动评论/浏览/互动全部排除（避免自嗨刷分）。

### 分群规则

| 标签 | 判断条件 |
|---|---|
| **A_活跃** | `vitality_score ≥ 20` **且** 近 30 天有评论或回复行为（`has_comment_or_reply_30d = 1`） |
| **C_沉默** | 无评论、无回复、无互动，且浏览他人文章 ≤ 3 次 |
| **B_有温度** | 其余 |

---

## 四、底表依赖

| 表名 | 用途 |
|---|---|
| `lofter.dwd_post_publish_di` | 发文明细，含 `post_ips`（文章所属圈层数组）|
| `lofter_db_dump.ods_db_cmb_ip_nd` | 圈层基础信息（含实体圈层和衍生圈层）|
| `lofter_db_dump.ods_db_post_response_nd` | 评论明细，`replyl1commentid` 区分主动评论和回复 |
| `lofter.dwd_post_hot_di` | 热度行为明细（点赞/推荐/转载）|
| `lofter.dws_post_traffic_dd` | 文章流量聚合，含 `discovery_expose_pv_30d`、`dislike_pv_30d` |
| `lofter.dwd_post_browse_di` | 浏览行为明细，`is_real=1` 过滤有效浏览 |
| `lofter.dws_par_creator_dd` | 作者粒度汇总，含 `post_click_uv_30d` |
| `lofter_db_dump.ods_db_blacklist_user_nd` | 用户拉黑记录，用于计算 UV 负反馈率 |

---

## 五、代码开发要点

### 1. LATERAL VIEW EXPLODE 与 JOIN 不能混用

`LATERAL VIEW EXPLODE` 展开数组字段后不能在同一层 `FROM` 中直接跟 `INNER JOIN`，需要将展开操作包裹为子查询，再在外层做 JOIN：

```sql
-- 正确写法
FROM (
    SELECT p.userId, ip_raw, p.postId, p.post_publish_date
    FROM lofter.dwd_post_publish_di p
    LATERAL VIEW EXPLODE(p.post_ips) ip_tbl AS ip_raw
    WHERE ...
) t
INNER JOIN entity_ip ON t.ip_raw = entity_ip.ip
```

> `LATERAL VIEW EXPLODE` 对空数组/NULL 不产生输出行（等价于 INNER JOIN 语义）；若需保留无圈层的文章，改用 `LATERAL VIEW OUTER EXPLODE`。

### 2. 以全量文章集为质量计算的驱动表

文章评论、热度、流量均为稀疏数据（有评论/热度的文章只占少数），若以 `post_comment_cnt` 为左表做 LEFT JOIN，无评论的文章会被丢弃，导致质量统计漏数据。

正确做法：以**全量样本文章集**（`post_quality_sample`）为主驱动表，评论/热度/流量均作为可选属性 LEFT JOIN，缺失时用 `COALESCE(..., 0)` 兜底。

### 3. 个人发文周期（cadence）的计算

使用 `LAG` 窗口函数获取上一篇文章的发布时间，再用 `DATEDIFF` 计算间隔，最后用 `PERCENTILE_APPROX(interval_days, 0.5)` 取中位数作为个人节奏：

```sql
LAG(publish_date) OVER (PARTITION BY creator_id, ip ORDER BY publish_date) AS prev_publish_date
```

- 只有 1 篇文章的作者无间隔数据，cadence 为 NULL，统一用 **30 天**兜底。
- 过滤 `interval_days > 0`，防止同一天多次发文导致间隔为 0 影响中位数。

### 4. 活人感中回复与评论的区分

通过 `ods_db_post_response_nd` 表的 `replyl1commentid` 字段区分：

| 行为类型 | 条件 |
|---|---|
| 回复评论 | `replyl1commentid IS NOT NULL AND replyl1commentid != 0` |
| 主动评论 | `replyl1commentid IS NULL OR replyl1commentid = 0` |

回复行为允许发生在自己的文章下（视为与粉丝互动），但主动评论必须过滤自己的文章（`publisherUserid != pid.creator_id`）。

### 5. 作者级低质的全圈层覆盖

UV 负反馈率（被拉黑 UV / 点击 UV）是**作者维度**的指标，一旦命中（≥ 10%），该作者在所有圈层的所有文章都视为低质。实现上，在 `post_quality_label` 中通过 `LEFT JOIN creator_uv_negr ON creator_id` 引入，与文章级条件用 `OR` 组合：

```sql
CASE
    WHEN (discovery_expose_pv_30d > 0 AND dislike_pv_30d / discovery_expose_pv_30d >= 0.001)
    OR COALESCE(uv.is_low_quality_creator, 0) = 1
    THEN 1 ELSE 0
END AS is_low_quality
```

### 6. SparkSQL WITH-AS 全链路约束

全部中间计算均采用 `WITH-AS` CTE 写法，禁止创建物理临时表，确保在单次 SQL 提交中完成所有计算。CTE 之间的依赖关系：

```
entity_ip
    └─ post_ip_detail
           ├─ post_stats → cadence → cadence_check（稳定性）
           ├─ post_quality_sample
           │      ├─ post_comment_cnt ┐
           │      ├─ post_hot_cnt     ├─ post_ces → ip_ces_p80 ┐
           │      ├─ post_traffic     │                         ├─ post_quality_label → quality_stats（质量）
           │      └─ creator_uv_negr ─┘─────────────────────────┘
           └─ (活人感行为 CTEs) → vitality_stats（活人感）
```

---

## 六、输出表结构

**表名**：`lofter_dm.ads_creator_ip_grouping_dd`  
**分区**：`dt`（yyyy-MM-dd）  
**存储**：Parquet + Snappy  
**调度参数**：`${azkaban.flow.1.days.ago}`（昨日日期）

| 字段 | 类型 | 说明 |
|---|---|---|
| `creator_id` | BIGINT | 创作者 userId |
| `ip` | STRING | 圈层名称 |
| `post_count_180d` | INT | 近 180 天发文数 |
| `post_count_30d` | INT | 近 30 天发文数 |
| `post_count_30d_prev` | INT | 前 30 天（31~60 天前）发文数 |
| `active_weeks_180d` | INT | 近 180 天有发文的自然周数 |
| `personal_cadence_days` | DOUBLE | 个人自然发文周期（天，中位数） |
| `last_post_date` | STRING | 最近发文日期 |
| `days_since_last_post` | INT | 距今最后发文天数 |
| `is_active_in_3cycles` | INT | 近 3 个自然周期内是否有发文 |
| `quality_post_cnt_180d` | INT | 优质文章数（样本内） |
| `low_quality_post_cnt_180d` | INT | 低质文章数（样本内） |
| `sample_post_cnt` | INT | 质量统计样本文章数（最多 20 篇） |
| `quality_post_ratio` | DOUBLE | 优质文章占比 |
| `low_quality_post_ratio` | DOUBLE | 低质文章占比 |
| `reply_to_comment_cnt_30d` | INT | 近 30 天回复评论次数 |
| `comment_others_cnt_30d` | INT | 近 30 天主动评论他人文章次数 |
| `hot_others_post_cnt_30d` | INT | 近 30 天互动他人文章次数 |
| `browse_others_post_cnt_30d` | INT | 近 30 天浏览他人文章篇数 |
| `has_comment_or_reply_30d` | INT | 近 30 天是否有评论或回复（1/0） |
| `vitality_score` | DOUBLE | 活人感综合得分 |
| `stability_tag` | STRING | 稳定性标签：A_稳定 / B_衰退 / C_发文不足3篇 / D_断更 |
| `quality_tag` | STRING | **【已废弃·不可用】** 字段保留以兼容历史 Schema，写入逻辑已下线，统一写 `NULL`，请勿使用 |
| `vitality_tag` | STRING | 活人感标签：A_活跃 / B_有温度 / C_沉默 |
