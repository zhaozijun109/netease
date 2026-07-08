-- 为 dwd_ad_content_unlock_di 增加 unlock_time 字段
-- 上线步骤：
--   1) 在 Hive 执行本脚本（CASCADE 同步分区元数据）
--   2) 重跑 dwd_ad_content_unlock_di 当日及历史所需分区
--   3) 下游基于 unlock_time 的作业才可投产

ALTER TABLE lofter.dwd_ad_content_unlock_di
ADD COLUMNS (
    unlock_time BIGINT COMMENT '解锁时间 毫秒时间戳'
) CASCADE;
