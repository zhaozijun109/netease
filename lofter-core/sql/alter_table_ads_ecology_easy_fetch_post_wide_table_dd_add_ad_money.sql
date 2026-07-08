-- 为 ads_ecology_easy_fetch_post_wide_table_dd 增加 t26 模块：近 1/7/15/30/90 日及累计的总广告收入
-- 数据来源：lofter.dwd_user_ad_revenue_di（按 postId 聚合，未按 revenue_module 过滤，包含激励广告/贴片广告/效果广告全部广告收入）
-- 上线步骤：
--   1) 在 Hive 执行本脚本（CASCADE 同步分区元数据）
--   2) 重跑 ads_ecology_easy_fetch_post_wide_table_dd 当日及历史所需分区
--   3) 下游基于 ad_money* 字段的作业才可投产

ALTER TABLE lofter_dm.ads_ecology_easy_fetch_post_wide_table_dd
ADD COLUMNS (
    ad_money     DOUBLE COMMENT '累计广告收入（来源 dwd_user_ad_revenue_di，按 postId 聚合）',
    ad_money_1d  DOUBLE COMMENT '近 1 日广告收入',
    ad_money_7d  DOUBLE COMMENT '近 7 日广告收入',
    ad_money_15d DOUBLE COMMENT '近 15 日广告收入',
    ad_money_30d DOUBLE COMMENT '近 30 日广告收入',
    ad_money_90d DOUBLE COMMENT '近 90 日广告收入'
) CASCADE;
