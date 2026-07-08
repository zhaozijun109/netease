-- 为 dwd_device_collect_apk_di 增加 customudid 字段（mda 后端设备ID）
-- 用于广告 Applist 上报：ES 索引 lofter_user_applist_v1 的 deviceId 取该字段
-- 上线步骤：
--   1) 在 Hive 执行本脚本（CASCADE 同步分区元数据）
--   2) 重跑 dwd_device_collect_apk_di 当日及历史所需分区
--   3) 下游 hive2es 作业 lofter_user_applist_v1 才可投产

ALTER TABLE lofter.dwd_device_collect_apk_di
ADD COLUMNS (
    customudid STRING COMMENT 'mda 后端设备ID'
) CASCADE;
