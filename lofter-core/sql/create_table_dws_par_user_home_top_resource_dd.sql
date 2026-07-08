-- 首页吸顶资源人群包中间表
-- 来源: lofter.dwd_home_top_resource_visit_di
-- 用途: 支撑用户标签 - 破次元/权益中心 曝光天数 / 点击 / 点击天数 等指标

CREATE TABLE IF NOT EXISTS lofter.dws_par_user_home_top_resource_dd (
    userId BIGINT COMMENT '用户ID',
    is_anonymous TINYINT COMMENT '是否匿名用户 0-否 1-是',

    -- 权益中心 曝光天数
    rc_expose_days_1d INT COMMENT '近1天权益中心icon曝光天数',
    rc_expose_days_7d INT COMMENT '近7天权益中心icon曝光天数',
    rc_expose_days_15d INT COMMENT '近15天权益中心icon曝光天数',
    rc_expose_days_30d INT COMMENT '近30天权益中心icon曝光天数',
    rc_expose_days_90d INT COMMENT '近90天权益中心icon曝光天数',
    rc_expose_days_180d INT COMMENT '近180天权益中心icon曝光天数',

    -- 权益中心 点击次数
    rc_click_count_1d INT COMMENT '近1天权益中心icon点击次数',
    rc_click_count_7d INT COMMENT '近7天权益中心icon点击次数',
    rc_click_count_15d INT COMMENT '近15天权益中心icon点击次数',
    rc_click_count_30d INT COMMENT '近30天权益中心icon点击次数',
    rc_click_count_90d INT COMMENT '近90天权益中心icon点击次数',
    rc_click_count_180d INT COMMENT '近180天权益中心icon点击次数',

    -- 权益中心 点击天数
    rc_click_days_1d INT COMMENT '近1天权益中心icon点击天数',
    rc_click_days_7d INT COMMENT '近7天权益中心icon点击天数',
    rc_click_days_15d INT COMMENT '近15天权益中心icon点击天数',
    rc_click_days_30d INT COMMENT '近30天权益中心icon点击天数',
    rc_click_days_90d INT COMMENT '近90天权益中心icon点击天数',
    rc_click_days_180d INT COMMENT '近180天权益中心icon点击天数',

    -- 破次元 曝光天数
    pve_expose_days_1d INT COMMENT '近1天破次元icon曝光天数',
    pve_expose_days_7d INT COMMENT '近7天破次元icon曝光天数',
    pve_expose_days_15d INT COMMENT '近15天破次元icon曝光天数',
    pve_expose_days_30d INT COMMENT '近30天破次元icon曝光天数',
    pve_expose_days_90d INT COMMENT '近90天破次元icon曝光天数',
    pve_expose_days_180d INT COMMENT '近180天破次元icon曝光天数',

    -- 破次元 点击次数
    pve_click_count_1d INT COMMENT '近1天破次元icon点击次数',
    pve_click_count_7d INT COMMENT '近7天破次元icon点击次数',
    pve_click_count_15d INT COMMENT '近15天破次元icon点击次数',
    pve_click_count_30d INT COMMENT '近30天破次元icon点击次数',
    pve_click_count_90d INT COMMENT '近90天破次元icon点击次数',
    pve_click_count_180d INT COMMENT '近180天破次元icon点击次数',

    -- 破次元 点击天数
    pve_click_days_1d INT COMMENT '近1天破次元icon点击天数',
    pve_click_days_7d INT COMMENT '近7天破次元icon点击天数',
    pve_click_days_15d INT COMMENT '近15天破次元icon点击天数',
    pve_click_days_30d INT COMMENT '近30天破次元icon点击天数',
    pve_click_days_90d INT COMMENT '近90天破次元icon点击天数',
    pve_click_days_180d INT COMMENT '近180天破次元icon点击天数'
)
COMMENT '首页吸顶资源(权益中心/破次元)用户人群包指标 - 按天聚合'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='首页吸顶资源用户人群包指标，覆盖权益中心与破次元的曝光天数/点击次数/点击天数',
    'data_source'='lofter.dwd_home_top_resource_visit_di',
    'update_frequency'='daily',
    'retention_days'='365'
);
