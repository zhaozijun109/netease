-- 创建权益中心用户看广告次数统计表

CREATE TABLE IF NOT EXISTS lofter.dws_par_user_reward_center_ad_watch_dd (
    userId BIGINT COMMENT '用户ID',
    is_anonymous TINYINT COMMENT '是否匿名用户 0-否 1-是',
    ad_watch_count_1d INT COMMENT '近1天看广告次数',
    ad_watch_count_7d INT COMMENT '近7天看广告次数', 
    ad_watch_count_15d INT COMMENT '近15天看广告次数',
    ad_watch_count_30d INT COMMENT '近30天看广告次数',
    ad_watch_count_90d INT COMMENT '近90天看广告次数',
    ad_watch_count_180d INT COMMENT '近180天看广告次数',
    ad_watch_count_total INT COMMENT '累计看广告次数'
)
COMMENT '权益中心用户看广告次数汇总表 - 按天聚合'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心用户看广告次数统计，基于task_pay_type=1的广告任务完成记录',
    'data_source'='lofter.dwd_ad_reward_task_complete_di',
    'update_frequency'='daily',
    'retention_days'='365'
);