-- 创建权益中心用户活跃度统计表
-- 基于 dws_par_user_active_dd 表结构，针对权益中心用户访问数据

CREATE TABLE IF NOT EXISTS lofter.dws_par_user_reward_center_active_dd (
    userId BIGINT COMMENT '用户ID',
    is_anonymous TINYINT COMMENT '是否匿名用户 0-否 1-是',
    active_days_1d INT COMMENT '近1天活跃天数',
    active_days_7d INT COMMENT '近7天活跃天数', 
    active_days_15d INT COMMENT '近15天活跃天数',
    active_days_30d INT COMMENT '近30天活跃天数',
    active_days_90d INT COMMENT '近90天活跃天数',
    active_days_180d INT COMMENT '近180天活跃天数',
    last_active_date STRING COMMENT '最后活跃日期(yyyy-MM-dd格式)'
)
COMMENT '权益中心用户活跃度汇总表 - 按天聚合'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心用户活跃度统计，基于用户访问权益中心的行为计算不同时间窗口的活跃天数',
    'data_source'='lofter.dwd_rewardcenter_visit_di',
    'update_frequency'='daily',
    'retention_days'='365'
);