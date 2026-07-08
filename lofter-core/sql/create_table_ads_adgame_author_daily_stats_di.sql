-- 拼图小游戏运营后台 作者每日结算数据表（透传 ES 索引 adgame_author_daily_stats_v1）

CREATE TABLE IF NOT EXISTS lofter_dm.ads_adgame_author_daily_stats_di (
    userId BIGINT COMMENT '作者Id（游戏归属博客 blogId）',
    ip STRING COMMENT '圈层名称',
    gameLink STRING COMMENT '游戏链接',
    gameId BIGINT COMMENT '游戏id',
    exposeUv BIGINT COMMENT '曝光人数（聚合页卡片曝光去重设备）',
    exposePv BIGINT COMMENT '曝光次数（聚合页卡片曝光）',
    playUv BIGINT COMMENT '游玩人数（去重用户）',
    playPv BIGINT COMMENT '游玩次数',
    completeUv BIGINT COMMENT '通关人数（去重用户）',
    completePv BIGINT COMMENT '通关次数',
    adExposePv BIGINT COMMENT '广告曝光次数（点击观看广告）',
    adUnlockPv BIGINT COMMENT '广告解锁次数（看广告完播解锁）',
    adRawRevenue DOUBLE COMMENT '广告收入（原始）',
    adAuthorIncome DOUBLE COMMENT '作者结算收入'
)
COMMENT '拼图小游戏作者每日运营数据 - 曝光/游玩/通关/广告解锁与结算收入'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='拼图小游戏运营后台结算数据：曝光取埋点 game_puzzle_home_card_expose(app端)，游玩/通关取 action_log PLAY/PASS，广告解锁取 reward_complete，收入取 dwd_ad_actions_v2_di(positionId=277630) 每请求max(bid)按 rta_sponsorId(=gameId) 聚合得原始收入；作者收入仅对 (gameId,userId) 命中 reward_complete(真实完成解锁) 的请求按0.5分成',
    'data_source'='lofter_db_dump.ods_db_trade_ad_game_nd, lofter_db_dump.ods_db_trade_ad_game_action_log_nd, lofter_db_dump.ods_db_trade_ad_game_reward_complete_nd, lofter.ods_mda_app_di, lofter.dwd_ad_actions_v2_di',
    'update_frequency'='daily',
    'retention_days'='365'
);
