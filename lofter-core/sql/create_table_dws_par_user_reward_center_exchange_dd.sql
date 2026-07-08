-- 创建权益中心用户商品兑换统计表

CREATE TABLE IF NOT EXISTS lofter.dws_par_user_reward_center_exchange_dd (
    userId BIGINT COMMENT '用户ID',
    grain_ticket_count_total INT COMMENT '粮票兑换总次数',
    coupon_count_total INT COMMENT '糖果券兑换总次数',
    pve_stamina_count_total INT COMMENT '虚拟人体力兑换总次数',
    ip_coupon_count_total INT COMMENT 'IP糖果券兑换总次数',
    emote_count_total INT COMMENT '表情包兑换总次数',
    avatarbox_count_total INT COMMENT '头像框兑换总次数',
    comment_count_total INT COMMENT '评论气泡兑换总次数',
    skin_count_total INT COMMENT '主题装扮兑换总次数',
    red_packet_count_total INT COMMENT '谷票（红包）兑换总次数',
    regret_card_count_total INT COMMENT '后悔卡兑换总次数',
    boot_screen_count_total INT COMMENT '定制开屏兑换总次数',
    yinge_count_total INT COMMENT '印鸽兑换总次数',
    cash_count_total INT COMMENT '现金兑换总次数',
    media_vip_count_total INT COMMENT '影视音会员兑换总次数',
    music_vip_count_total INT COMMENT '云音乐会员兑换总次数'
)
COMMENT '权益中心用户商品兑换汇总表 - 累计数据'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心用户商品兑换行为统计，按商品二级分类统计累计兑换次数',
    'data_source'='lofter.dwd_ad_reward_score_product_exchange_di',
    'update_frequency'='daily',
    'retention_days'='365'
);