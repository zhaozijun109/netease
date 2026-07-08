-- ============================================================
-- 创作者圈层分群打标底表
-- 目标：输出 creator_id × ip 维度的分群标签
--   1. 圈层稳定发文（stability_tag）：稳定 / 衰退 / 断更
--   2. 发文质量（quality_tag）：【已废弃·不可用】字段保留兼容 Schema，写入 NULL
--      （质量过程指标 quality_post_ratio / low_quality_post_ratio 等仍正常输出）
--   3. 圈层活人感（vitality_tag）：活跃 / 有温度 / 沉默
--
-- 作者范围：近 180 天在该圈层有图文/视频发文的创作者
--          （来源：lofter.dwd_post_publish_di）
-- 数据范围：发文节律取近 180 天，质量/活人感取近 30 天
--           衰退判断：对比近 30 天 vs 前 30 天（31~60 天前）发文量
--
-- 底表依赖：
--   lofter.dwd_post_publish_di       -- 每日发文明细（含 post_ips 字段）
--   lofter_db_dump.ods_db_cmb_ip_nd  -- IP 基础信息（derivedflag=0 为实体圈层）
--   lofter.dws_post_traffic_dd       -- 文章流量聚合（含 browse_pv_30d、dislike_pv_30d、评论数等）
--   lofter.dwd_post_hot_di           -- 文章热度行为（点赞/推荐/转载）
--   lofter_db_dump.ods_db_post_response_nd -- 评论明细（replyl1commentid 区分回复）
--   lofter.dwd_post_browse_di        -- 文章浏览行为
-- ============================================================


-- ============================================================
-- 建表语句
-- ============================================================
CREATE TABLE IF NOT EXISTS lofter_dm.ads_creator_ip_grouping_dd (
                                                                    creator_id                   BIGINT   COMMENT '创作者 userId',
                                                                    ip                           STRING   COMMENT '圈层名称（实体圈层）',

    -- -------------------------------------------------------
    -- 发文节律过程指标
    -- -------------------------------------------------------
                                                                    post_count_180d              INT      COMMENT '近 180 天在该圈层发文数',
                                                                    post_count_30d               INT      COMMENT '近 30 天在该圈层发文数',
                                                                    post_count_30d_prev          INT      COMMENT '前 30 天（31~60 天前）在该圈层发文数（用于衰退判断）',
                                                                    active_weeks_180d            INT      COMMENT '近 180 天有发文的自然周数',
                                                                    personal_cadence_days        DOUBLE   COMMENT '自然发文周期（相邻发文间隔中位数，天）',
                                                                    last_post_date               STRING   COMMENT '最近一次发文日期',
                                                                    days_since_last_post         INT      COMMENT '距今最后一次发文的天数',
                                                                    is_active_in_3cycles         INT      COMMENT '近 3 个自然周期内是否有发文（1=是，0=否）',

    -- -------------------------------------------------------
    -- 发文质量过程指标
    --   文章范围：近180天发布的最近20篇，行为统计窗口：近30天
    --   CES = comment_cnt * 5 + hot_cnt * 1（在该圈层维度下计算）
    -- -------------------------------------------------------
                                                                    quality_post_cnt_180d        INT      COMMENT '近180天最近20篇文章中，近30天行为下的优质文章数',
                                                                    low_quality_post_cnt_180d    INT      COMMENT '近180天最近20篇文章中，近30天行为下的低质文章数',
                                                                    sample_post_cnt              INT      COMMENT '参与质量统计的文章数（近180天最多取20篇）',
                                                                    quality_post_ratio           DOUBLE   COMMENT '优质文章占比（优质文章数 / 样本文章数）',
                                                                    low_quality_post_ratio       DOUBLE   COMMENT '低质文章占比（低质文章数 / 样本文章数）',

    -- -------------------------------------------------------
    -- 活人感过程指标（近 30 天，只计他人文章上的行为）
    -- -------------------------------------------------------
                                                                    reply_to_comment_cnt_30d     INT      COMMENT '近 30 天回复他人/自己文章评论次数（weight=5）',
                                                                    comment_others_cnt_30d       INT      COMMENT '近 30 天主动评论他人文章次数（weight=5）',
                                                                    hot_others_post_cnt_30d      INT      COMMENT '近 30 天互动（点赞/推荐/转载）他人文章次数（weight=2）',
                                                                    browse_others_post_cnt_30d   INT      COMMENT '近 30 天浏览他人文章篇数（weight=1）',
                                                                    has_comment_or_reply_30d     INT      COMMENT '近 30 天是否有评论或回复行为（1=是，0=否）',
                                                                    vitality_score               DOUBLE   COMMENT '活人感综合得分（加权求和）',

    -- -------------------------------------------------------
    -- 分群标签
    -- -------------------------------------------------------
                                                                    stability_tag                STRING   COMMENT '发文稳定性标签：A_稳定/B_衰退/D_断更/C_发文不足3篇',
                                                                    quality_tag                  STRING   COMMENT '【已废弃·不可用】发文质量标签字段保留以兼容历史Schema，写入逻辑已下线，统一写 NULL，请勿使用',
                                                                    vitality_tag                 STRING   COMMENT '活人感标签：活跃/有温度/沉默'
)
    COMMENT '创作者圈层分群打标底表（每日快照）'
    PARTITIONED BY (dt STRING COMMENT '数据日期，格式 yyyy-MM-dd')
    STORED AS PARQUET
    TBLPROPERTIES ('parquet.compression' = 'SNAPPY');


-- ============================================================
-- 写数据 SQL（SparkSQL，WITH-AS 写法）
-- 运行时将 ${azkaban.flow.1.days.ago} 替换为目标日期，例如 '2025-04-20'
-- ============================================================
INSERT OVERWRITE TABLE lofter_dm.ads_creator_ip_grouping_dd
PARTITION (dt = '${azkaban.flow.1.days.ago}')

WITH

-- ----------------------------------------------------------
-- Step 1：取所有圈层列表（含实体圈层和衍生圈层）
-- ----------------------------------------------------------
entity_ip AS (
    SELECT name AS ip
    FROM lofter_db_dump.ods_db_cmb_ip_nd
),

-- ----------------------------------------------------------
-- Step 2：从 dwd_post_publish_di 取近 180 天图文/视频发文明细
--   - post_ips 是数组字段，用 LATERAL VIEW EXPLODE 展开
--   - 只取实体圈层（inner join entity_ip）
--   - 过滤掉非图片/文字/视频内容
-- ----------------------------------------------------------
post_ip_detail AS (
    SELECT
        t.creator_id,
        t.ip_raw                                            AS ip,
        t.postId,
        t.publish_date,
        DATEDIFF('${azkaban.flow.1.days.ago}', t.publish_date)            AS days_ago
    FROM (
        SELECT
            p.userId                                        AS creator_id,
            ip_raw,
            p.postId,
            p.post_publish_date                             AS publish_date
        FROM lofter.dwd_post_publish_di p
        LATERAL VIEW EXPLODE(p.post_ips) ip_tbl AS ip_raw
        WHERE p.dt BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 179) AND '${azkaban.flow.1.days.ago}'
          AND p.post_publish_date BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 179) AND '${azkaban.flow.1.days.ago}'
          AND p.post_content_type IN ('图片', '文字', '视频')
    ) t
    INNER JOIN entity_ip ip_t ON t.ip_raw = ip_t.ip
),

-- ----------------------------------------------------------
-- Step 3：计算每个 creator×ip 的发文统计指标
-- ----------------------------------------------------------
post_stats AS (
    SELECT
        creator_id,
        ip,
        COUNT(DISTINCT postId)                                                              AS post_count_180d,
        COUNT(DISTINCT CASE WHEN days_ago < 30 THEN postId END)                             AS post_count_30d,
        -- 前 30 天（即 31~60 天前）发文数，用于衰退判断
        COUNT(DISTINCT CASE WHEN days_ago >= 30 AND days_ago < 60 THEN postId END)          AS post_count_30d_prev,
        -- 有发文的自然周数（近 180 天）
        COUNT(DISTINCT WEEKOFYEAR(CAST(publish_date AS DATE)))                              AS active_weeks_180d,
        MAX(publish_date)                                                                    AS last_post_date,
        DATEDIFF('${azkaban.flow.1.days.ago}', MAX(publish_date))                                         AS days_since_last_post
    FROM post_ip_detail
    GROUP BY creator_id, ip
),

-- ----------------------------------------------------------
-- Step 4：计算相邻发文间隔 → 得出个人自然发文周期（中位数）
-- ----------------------------------------------------------
post_ordered AS (
    SELECT
        creator_id,
        ip,
        postId,
        publish_date,
        LAG(publish_date) OVER (
            PARTITION BY creator_id, ip
            ORDER BY publish_date
        ) AS prev_publish_date
    FROM post_ip_detail
),

post_intervals AS (
    SELECT
        creator_id,
        ip,
        DATEDIFF(CAST(publish_date AS DATE), CAST(prev_publish_date AS DATE)) AS interval_days
    FROM post_ordered
    WHERE prev_publish_date IS NOT NULL
      AND DATEDIFF(CAST(publish_date AS DATE), CAST(prev_publish_date AS DATE)) > 0
),

-- 用 PERCENTILE_APPROX 取中位数作为个人 cadence
-- 若只有 1 篇文章（无法算间隔），则 cadence 为 NULL，后续用 30 天兜底
cadence AS (
    SELECT
        creator_id,
        ip,
        PERCENTILE_APPROX(interval_days, 0.5) AS personal_cadence_days
    FROM post_intervals
    GROUP BY creator_id, ip
),

-- ----------------------------------------------------------
-- Step 5：结合节律，判断近 3 个自然周期内是否有发文
-- ----------------------------------------------------------
cadence_check AS (
    SELECT
        ps.creator_id,
        ps.ip,
        ps.post_count_180d,
        ps.post_count_30d,
        ps.post_count_30d_prev,
        ps.active_weeks_180d,
        ps.last_post_date,
        ps.days_since_last_post,
        COALESCE(c.personal_cadence_days, 30.0)                              AS personal_cadence_days,
        CASE
            WHEN ps.days_since_last_post <= COALESCE(c.personal_cadence_days, 30.0) * 3
            THEN 1
            ELSE 0
        END                                                                   AS is_active_in_3cycles
    FROM post_stats ps
    LEFT JOIN cadence c
           ON ps.creator_id = c.creator_id AND ps.ip = c.ip
),

-- ----------------------------------------------------------
-- Step 6：发文质量
--   文章范围：作者在该圈层近180天发布的最近20篇文章（按发布时间倒序取前20）
--   行为统计窗口：近30天
--   CES = 评论×5 + 热度×1，跨圈层取文章最大CES分（去圈层偏差）
    --   优质文章：CES >= 该圈层全部文章CES的p80
    --   低质文章（满足其一）：
    --     条件1（文章级）推荐场景负反馈率 dislike_pv_30d/discovery_expose_pv_30d >= 0.1%
    --     条件2（作者级）UV负反馈率 blocked_cnt_30d/post_click_uv_30d >= 10%（全圈层低质）
-- ----------------------------------------------------------

-- 6a：取每个 creator×ip 近180天发布的最近20篇文章（样本文章集）
post_quality_sample AS (
    SELECT
        creator_id,
        ip,
        postId,
        publish_date
    FROM (
        SELECT
            creator_id,
            ip,
            postId,
            publish_date,
            ROW_NUMBER() OVER (
                PARTITION BY creator_id, ip
                ORDER BY publish_date DESC
            ) AS rn
        FROM post_ip_detail
        -- 近180天已在 post_ip_detail 中过滤
    ) t
    WHERE rn <= 20
),

-- 6b：计算每篇样本文章在该圈层下近30天收到的评论数（来自他人）
--     注意：CES 在圈层维度计算，不跨圈层取最大值
post_comment_cnt AS (
    SELECT
        s.creator_id,
        s.ip,
        s.postId,
        COUNT(r.id) AS comment_cnt_30d
    FROM post_quality_sample s
    LEFT JOIN lofter_db_dump.ods_db_post_response_nd r
           ON s.postId = r.postId
          AND FROM_UNIXTIME(CAST(r.publishTime / 1000 AS BIGINT), 'yyyy-MM-dd')
              BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
    GROUP BY s.creator_id, s.ip, s.postId
),

-- 6c：计算每篇样本文章近30天收到的热度行为数（点赞/推荐/转载）
post_hot_cnt AS (
    SELECT
        s.creator_id,
        s.ip,
        s.postId,
        COUNT(h.postId) AS hot_cnt_30d
    FROM post_quality_sample s
    LEFT JOIN lofter.dwd_post_hot_di h
           ON s.postId = h.postId
          AND h.opType IN ('praise', 'recommend', 'reproduce')
          AND h.dt BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
    GROUP BY s.creator_id, s.ip, s.postId
),

-- 6d：获取每篇样本文章近30天的推荐曝光 pv 和 dislike pv（用于推荐场景负反馈率）
post_traffic AS (
    SELECT
        s.creator_id,
        s.ip,
        s.postId,
        COALESCE(tr.discovery_expose_pv_30d, 0) AS discovery_expose_pv_30d,
        COALESCE(tr.dislike_pv_30d,          0) AS dislike_pv_30d
    FROM post_quality_sample s
    LEFT JOIN lofter.dws_post_traffic_dd tr
           ON s.postId = tr.postId
          AND tr.dt = '${azkaban.flow.1.days.ago}'
),

-- 6d2：计算作者级 UV 负反馈率（blocked_cnt_30d / post_click_uv_30d）
--      条件：post_click_uv_30d >= 50（保证样本量）
--      命中则该作者在所有圈层均视为低质
creator_uv_negr AS (
    SELECT
        a.userid                                                  AS creator_id,
        a.post_click_uv_30d,
        COALESCE(b.blocked_cnt_30d, 0)                           AS blocked_cnt_30d,
        COALESCE(b.blocked_cnt_30d, 0) * 1.0 / a.post_click_uv_30d AS uv_neg_feedback_rate,
        -- UV负反馈率 >= 10% 则标记为低质作者
        CASE
            WHEN COALESCE(b.blocked_cnt_30d, 0) * 1.0 / a.post_click_uv_30d >= 0.1
            THEN 1 ELSE 0
        END                                                       AS is_low_quality_creator
    FROM (
        SELECT userid, post_click_uv_30d
        FROM lofter.dws_par_creator_dd
        WHERE dt = '${azkaban.flow.1.days.ago}'
          AND post_click_uv_30d >= 50
    ) a
    LEFT JOIN (
        SELECT blacklistblogid AS userid, COUNT(1) AS blocked_cnt_30d
        FROM lofter_db_dump.ods_db_blacklist_user_nd
        WHERE FROM_UNIXTIME(CAST(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')
              BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
        GROUP BY blacklistblogid
    ) b ON a.userid = b.userid
),

-- 6e：计算每篇文章在该圈层下的 CES 得分（评论×5 + 热度×1）
post_ces AS (
    SELECT
        s.creator_id,
        s.ip,
        s.postId,
        COALESCE(pc.comment_cnt_30d, 0) * 5 + COALESCE(ph.hot_cnt_30d, 0) AS ces_score
    FROM post_quality_sample s
    LEFT JOIN post_comment_cnt pc ON s.creator_id = pc.creator_id
                                  AND s.ip = pc.ip AND s.postId = pc.postId
    LEFT JOIN post_hot_cnt     ph ON s.creator_id = ph.creator_id
                                  AND s.ip = ph.ip AND s.postId = ph.postId
),

-- 6f：计算各圈层的 CES p80 阈值（基于该圈层内所有样本文章的CES）
ip_ces_p80 AS (
    SELECT
        ip,
        PERCENTILE_APPROX(ces_score, 0.80) AS p80_ces
    FROM post_ces
    GROUP BY ip
),

-- 6g：为每篇样本文章打优质/低质标记
--   低质条件（满足其一）：
--     条件1（文章级）：推荐场景负反馈率 dislike_pv_30d / discovery_expose_pv_30d >= 0.1%
--     条件2（作者级）：UV负反馈率 blocked_cnt_30d / post_click_uv_30d >= 10%（全圈层低质）
post_quality_label AS (
    SELECT
        s.creator_id,
        s.ip,
        s.postId,
        ces.ces_score,
        pt.discovery_expose_pv_30d,
        pt.dislike_pv_30d,
        -- 推荐场景负反馈率
        CASE
            WHEN COALESCE(pt.discovery_expose_pv_30d, 0) > 0
            THEN COALESCE(pt.dislike_pv_30d, 0) * 1.0 / pt.discovery_expose_pv_30d
            ELSE NULL
        END                                                                   AS rec_neg_feedback_rate,
        -- UV 负反馈率（作者维度）
        COALESCE(uv.uv_neg_feedback_rate, 0)                                 AS uv_neg_feedback_rate,
        -- 优质标记：该圈层 CES >= p80
        CASE
            WHEN ces.ces_score >= p80.p80_ces AND p80.p80_ces > 0
            THEN 1 ELSE 0
        END                                                                   AS is_quality,
        -- 低质标记：条件1（推荐负反馈率>=0.1%）OR 条件2（作者UV负反馈率>=10%）
        CASE
            WHEN (
                COALESCE(pt.discovery_expose_pv_30d, 0) > 0
                AND COALESCE(pt.dislike_pv_30d, 0) * 1.0 / pt.discovery_expose_pv_30d >= 0.001
            )
            OR COALESCE(uv.is_low_quality_creator, 0) = 1
            THEN 1 ELSE 0
        END                                                                   AS is_low_quality
    FROM post_quality_sample s
    LEFT JOIN post_ces        ces ON s.creator_id = ces.creator_id
                                 AND s.ip = ces.ip AND s.postId = ces.postId
    LEFT JOIN post_traffic    pt  ON s.creator_id = pt.creator_id
                                 AND s.ip = pt.ip AND s.postId = pt.postId
    LEFT JOIN ip_ces_p80      p80 ON s.ip = p80.ip
    LEFT JOIN creator_uv_negr uv  ON s.creator_id = uv.creator_id
),

-- 6h：汇总 creator×ip 质量指标
quality_stats AS (
    SELECT
        creator_id,
        ip,
        COUNT(DISTINCT postId)                                                    AS sample_post_cnt,
        SUM(CAST(is_quality    AS INT))                                           AS quality_post_cnt_180d,
        SUM(CAST(is_low_quality AS INT))                                          AS low_quality_post_cnt_180d,
        SUM(CAST(is_quality    AS INT)) * 1.0
            / NULLIF(COUNT(DISTINCT postId), 0)                                   AS quality_post_ratio,
        SUM(CAST(is_low_quality AS INT)) * 1.0
            / NULLIF(COUNT(DISTINCT postId), 0)                                   AS low_quality_post_ratio
    FROM post_quality_label
    GROUP BY creator_id, ip
),

-- ----------------------------------------------------------
-- Step 7：活人感指标（近 30 天）
--   规则（来自需求文档约束 5）：
--   a. 回复评论 = 5 分（r.replyl1commentid IS NOT NULL，且可以是自己文章下的回复）
--   b. 评论他人文章 = 5 分（主动评论，非自己文章，replyl1commentid IS NULL/0）
--   c. 互动他人文章（点赞/推荐/转载）= 2 分
--   d. 浏览他人文章 = 1 分
--   过滤：排除对自己文章的主动评论、浏览和互动（不排除回复）
-- ----------------------------------------------------------

-- 7a：回复评论（replyl1commentid 不为空 => 是一条"回复"）
--     可以回复自己文章下的评论；但需过滤 creator 回复自己发出的评论
--     底表：ods_db_post_response_nd
reply_activity AS (
    SELECT
        r.publisherUserid                                                    AS creator_id,
        -- 关联被评论文章所在的圈层
        pid.ip,
        COUNT(r.id)                                                          AS reply_to_comment_cnt_30d
    FROM lofter_db_dump.ods_db_post_response_nd r
    -- 关联圈层：文章必须属于某个实体圈层（被回复的帖子在圈层内）
    INNER JOIN (
        SELECT DISTINCT creator_id, ip, postId
        FROM post_ip_detail
        WHERE days_ago < 60
    ) pid ON r.postId = pid.postId
    WHERE -- 是回复行为（replyl1commentid 不为 NULL 且不为 0）
          r.replyl1commentid IS NOT NULL
      AND r.replyl1commentid != 0
      -- 时间范围：近 30 天
      AND FROM_UNIXTIME(CAST(r.publishTime / 1000 AS BIGINT), 'yyyy-MM-dd')
          BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
    GROUP BY r.publisherUserid, pid.ip
),

-- 7b：主动评论他人文章（非回复，且被评论的文章不属于自己）
comment_others_activity AS (
    SELECT
        r.publisherUserid                                                    AS creator_id,
        pid.ip,
        COUNT(r.id)                                                          AS comment_others_cnt_30d
    FROM lofter_db_dump.ods_db_post_response_nd r
    INNER JOIN (
        SELECT DISTINCT creator_id, ip, postId
        FROM post_ip_detail
        WHERE days_ago < 60
    ) pid ON r.postId = pid.postId
    WHERE -- 非回复（replyl1commentid 为 NULL 或 0）
          (r.replyl1commentid IS NULL OR r.replyl1commentid = 0)
      -- 只统计对他人文章的评论（过滤自己评论自己）
      AND r.publisherUserid != pid.creator_id
      AND FROM_UNIXTIME(CAST(r.publishTime / 1000 AS BIGINT), 'yyyy-MM-dd')
          BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
    GROUP BY r.publisherUserid, pid.ip
),

-- 7c：互动他人文章（点赞/推荐/转载，过滤自己文章）
hot_others_activity AS (
    SELECT
        h.userId                                                             AS creator_id,
        pid.ip,
        COUNT(DISTINCT h.postId)                                             AS hot_others_post_cnt_30d
    FROM lofter.dwd_post_hot_di h
    INNER JOIN (
        SELECT DISTINCT creator_id, ip, postId
        FROM post_ip_detail
        WHERE days_ago < 60
    ) pid ON h.postId = pid.postId
           AND h.userId != pid.creator_id   -- 过滤对自己文章的互动
    WHERE h.dt BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
      AND h.opType IN ('praise', 'recommend', 'reproduce')
    GROUP BY h.userId, pid.ip
),

-- 7d：浏览他人文章（is_real=1，过滤自己文章）
browse_others_activity AS (
    SELECT
        b.userId                                                             AS creator_id,
        pid.ip,
        COUNT(DISTINCT b.postId)                                             AS browse_others_post_cnt_30d
    FROM lofter.dwd_post_browse_di b
    INNER JOIN (
        SELECT DISTINCT creator_id, ip, postId
        FROM post_ip_detail
        WHERE days_ago < 60
    ) pid ON b.postId = pid.postId
           AND b.userId != pid.creator_id   -- 过滤浏览自己文章
    WHERE b.dt BETWEEN DATE_SUB('${azkaban.flow.1.days.ago}', 29) AND '${azkaban.flow.1.days.ago}'
      AND b.is_real = 1
    GROUP BY b.userId, pid.ip
),

-- ----------------------------------------------------------
-- Step 8：汇总活人感得分
-- vitality_score = reply×5 + comment_others×5 + hot_others×2 + browse_others×1
-- ----------------------------------------------------------
vitality_stats AS (
    SELECT
        cc.creator_id,
        cc.ip,
        COALESCE(ra.reply_to_comment_cnt_30d,    0)  AS reply_to_comment_cnt_30d,
        COALESCE(coa.comment_others_cnt_30d,      0)  AS comment_others_cnt_30d,
        COALESCE(hoa.hot_others_post_cnt_30d,     0)  AS hot_others_post_cnt_30d,
        COALESCE(boa.browse_others_post_cnt_30d,  0)  AS browse_others_post_cnt_30d,
        -- 是否有评论或回复行为（用于活跃判断）
        CASE
            WHEN COALESCE(ra.reply_to_comment_cnt_30d, 0) > 0
              OR COALESCE(coa.comment_others_cnt_30d,   0) > 0
            THEN 1 ELSE 0
        END                                           AS has_comment_or_reply_30d,
        -- 综合得分
        COALESCE(ra.reply_to_comment_cnt_30d,   0) * 5.0
            + COALESCE(coa.comment_others_cnt_30d, 0) * 5.0
            + COALESCE(hoa.hot_others_post_cnt_30d, 0) * 2.0
            + COALESCE(boa.browse_others_post_cnt_30d, 0) * 1.0             AS vitality_score
    FROM cadence_check cc
    LEFT JOIN reply_activity          ra  ON cc.creator_id = ra.creator_id  AND cc.ip = ra.ip
    LEFT JOIN comment_others_activity coa ON cc.creator_id = coa.creator_id AND cc.ip = coa.ip
    LEFT JOIN hot_others_activity     hoa ON cc.creator_id = hoa.creator_id AND cc.ip = hoa.ip
    LEFT JOIN browse_others_activity  boa ON cc.creator_id = boa.creator_id AND cc.ip = boa.ip
)

-- ----------------------------------------------------------
-- Step 9：最终汇总并打标
-- ----------------------------------------------------------
SELECT
    cc.creator_id,
    cc.ip,

    -- 发文节律过程指标
    cc.post_count_180d,
    cc.post_count_30d,
    cc.post_count_30d_prev,
    cc.active_weeks_180d,
    cc.personal_cadence_days,
    cc.last_post_date,
    cc.days_since_last_post,
    cc.is_active_in_3cycles,

    -- 发文质量过程指标
    COALESCE(qs.quality_post_cnt_180d,    0)    AS quality_post_cnt_180d,
    COALESCE(qs.low_quality_post_cnt_180d,0)    AS low_quality_post_cnt_180d,
    COALESCE(qs.sample_post_cnt,          0)    AS sample_post_cnt,
    COALESCE(qs.quality_post_ratio,       0.0)  AS quality_post_ratio,
    COALESCE(qs.low_quality_post_ratio,   0.0)  AS low_quality_post_ratio,

    -- 活人感过程指标
    vs.reply_to_comment_cnt_30d,
    vs.comment_others_cnt_30d,
    vs.hot_others_post_cnt_30d,
    vs.browse_others_post_cnt_30d,
    vs.has_comment_or_reply_30d,
    vs.vitality_score,

    -- -------------------------------------------------------
    -- 打标：稳定性 stability_tag
    --   C_发文不足3篇：近180天在该圈层发文数 <= 3（新人或低频偶发，优先判断）
    --   D_断更：近 3 个自然周期内无发文
    --   A_稳定：近 3 个周期有发文，且近 30d 发文量 >= 前 30d（31~60d前）发文量
    --   B_衰退：其余（近期发文在减少）
    -- -------------------------------------------------------
    CASE
        WHEN cc.post_count_180d <= 3
            THEN 'C_发文不足3篇'
        WHEN cc.is_active_in_3cycles = 0
            THEN 'D_断更'
        WHEN cc.is_active_in_3cycles = 1
            AND cc.post_count_30d >= cc.post_count_30d_prev
            THEN 'A_稳定'
        ELSE 'B_衰退'
        END                                            AS stability_tag,

    -- -------------------------------------------------------
    -- 打标：质量 quality_tag —— 【已废弃·不可用】
    --   字段保留以兼容历史 Schema，原打标逻辑已下线，统一写 NULL。
    --   下游若需要质量信息，请直接使用过程指标
    --   （quality_post_ratio / low_quality_post_ratio 等）。
    -- -------------------------------------------------------
    CAST(NULL AS STRING)                               AS quality_tag,

    -- -------------------------------------------------------
    -- 打标：活人感 vitality_tag
    --   A_活跃：30天互动分 >= 20 且至少有一条评论或评论回复
    --   C_沉默：只有浏览且浏览次数 <= 3
    --   B_有温度：其余
    -- -------------------------------------------------------
    CASE
        WHEN vs.vitality_score >= 20
            AND vs.has_comment_or_reply_30d = 1
            THEN 'A_活跃'
        WHEN vs.comment_others_cnt_30d = 0
            AND vs.reply_to_comment_cnt_30d = 0
            AND vs.hot_others_post_cnt_30d = 0
            AND vs.browse_others_post_cnt_30d <= 3
            THEN 'C_沉默'
        ELSE 'B_有温度'
        END                                            AS vitality_tag

FROM cadence_check cc
         LEFT JOIN quality_stats  qs ON cc.creator_id = qs.creator_id AND cc.ip = qs.ip
         LEFT JOIN vitality_stats vs ON cc.creator_id = vs.creator_id AND cc.ip = vs.ip
;