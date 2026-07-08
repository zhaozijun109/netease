-- 首页吸顶资源访问明细表
-- 数据来源: lofter.ods_mda_app_di
-- 资源识别:
--   eventid = 'a1-37' -> icon 模块曝光
--   eventid = 'a1-36' -> icon 模块点击
--   params['URL'] / params['url'] 包含 'reward-center' -> 权益中心 (reward_center)
--   params['URL'] / params['url'] 包含 'pve-boyfriends' -> 破次元 (pve_boyfriends)

CREATE TABLE IF NOT EXISTS lofter.dwd_home_top_resource_visit_di (
    userid BIGINT COMMENT '用户ID',
    deviceudid STRING COMMENT '设备ID',
    eventid STRING COMMENT '事件ID a1-37=曝光 a1-36=点击',
    action_type STRING COMMENT '行为类型 expose=曝光 click=点击',
    resource_type STRING COMMENT '资源类型 reward_center=权益中心 pve_boyfriends=破次元',
    url STRING COMMENT '埋点上报的URL',
    occurtime BIGINT COMMENT '事件发生时间(毫秒)'
)
COMMENT '首页吸顶资源访问明细 - 按天分区'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='首页吸顶资源(权益中心/破次元)曝光与点击明细',
    'data_source'='lofter.ods_mda_app_di',
    'update_frequency'='daily',
    'retention_days'='365'
);
