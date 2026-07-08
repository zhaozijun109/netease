-- 激励广告每博客解锁后用户行为净效果统计表

CREATE TABLE IF NOT EXISTS lofter_dm.ads_ad_content_unlock_effect_di (
    module STRING COMMENT '广告类型 激励广告/广告文/视频合集',
    blogId BIGINT COMMENT '博客ID（被解锁文章的发布博客）',
    unlock_uv BIGINT COMMENT '当日通过广告解锁该博客文章的用户数（去重）',
    follow_net_uv BIGINT COMMENT '解锁该博客后关注该博客且当日最终态有效的净关注用户数（去重）',
    praise_net_uv BIGINT COMMENT '解锁该博客文章后点赞该文章且当日最终态有效的净点赞用户数（去重）',
    praise_net_count BIGINT COMMENT '解锁该博客文章后点赞该文章且当日最终态有效的净点赞 user-post 数'
)
COMMENT '激励广告每博客解锁效果统计 - 解锁后关注/点赞净人数（剔除当日取消）'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='基于 dwd_ad_content_unlock_di（含 unlock_time）+ ods_binlog_user_following_di + ods_binlog_post_hot_di(type=1) 计算每个博客激励广告解锁后用户净关注/净点赞行为',
    'data_source'='lofter.dwd_ad_content_unlock_di, lofter.ods_binlog_user_following_di, lofter.ods_binlog_post_hot_di',
    'update_frequency'='daily',
    'retention_days'='365'
);
