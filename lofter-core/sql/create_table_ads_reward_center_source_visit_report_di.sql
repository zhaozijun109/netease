-- 权益中心资源位入口监控报表
-- 数据来源:
--   lofter.dwd_rewardcenter_visit_di          (落地页访问明细，含 source_type / source)
--   lofter.dwd_rewardcenter_user_di            (用户类别拆分)
--   lofter.dwd_ad_reward_task_complete_di      (广告完整观看, task_pay_type=1)
--   lofter.dwd_ad_reward_score_product_exchange_di (商品兑换提交)
--   lofter.dwd_user_ad_revenue_di              (广告收益, position_name like '%权益中心%')
--   lofter.dws_par_user_active_di              (APP活跃，用于APP留存 + 全站日活)
--   lofter.ods_mda_app_di                      (入口曝光/点击埋点，按 eventid 区分资源位)
--   lofter_db_dump.ods_db_reward_particular_score_task_nd (签到，type=2)
-- 维度: source_type × source × user_type
-- 归因方式:
--   首次归因(旧列): 用户当日最早一次有归因访问的 source (occurtime asc)，保留兼容
--   最近归因(新列): 按单条行为记录，取该用户在"行为操作时间"之前最近一次有效访问的 source (occurtime <= 操作时间，取最大)
--                    广告观看=reward_time / 商品兑换=exchange_time / 广告收益=request_time / 签到=finishtime
--                    行为发生前无访问的记录归入 'unknown'；行为时间与 occurtime 均为毫秒 epoch
--   落地页访问(新列): 访问事件自归类，每条访问按其自身资源位(source)计，uv=该source去重访问用户/pv=访问事件数
--   曝光/点击(新列): 事件自归因，eventid 唯一对应资源位；曝光/点击用户当日未访问权益中心时 user_type 归 '未访问用户'
-- 留存基准:
--   *_retain_1d  : T-1 日导量用户在 T 日是否复访 (基准日 = dt)
--   *_retain_7d  : T-7 日导量用户在 T 日是否复访 (基准日 = dt - 6)
--   *_retain_30d : T-30 日导量用户在 T 日是否复访 (基准日 = dt - 29)
--   广告观看率 / 商品兑换率 / 留存率 / 点击率 / 到达率均在使用侧按 sum/sum 汇总后计算

CREATE TABLE IF NOT EXISTS lofter_dm.ads_reward_center_source_visit_report_di (
    source_type STRING COMMENT '来源类型 fixed=常驻 attract=引流 recall=召回',
    source STRING COMMENT '资源位英文键 mine_entrance/home_upperleft/...',
    user_type STRING COMMENT '用户类别 权益中心新用户/权益中心回流用户/普通访问用户/未访问用户(仅曝光点击用户当日未访问权益中心时)',

    rc_visit_uv BIGINT COMMENT '当日通过该资源位首次访问权益中心的去重UV',
    ad_watch_uv BIGINT COMMENT '导量用户中当日完整观看广告的去重UV (task_pay_type=1)',
    product_exchange_uv BIGINT COMMENT '导量用户中当日提交商品兑换的去重UV',
    ad_revenue DECIMAL(20,6) COMMENT '导量用户在权益中心产生的广告收益 (position_name like %权益中心%)',
    ad_watch_rate DECIMAL(10,6) COMMENT '广告观看率 = ad_watch_uv / rc_visit_uv',
    product_exchange_rate DECIMAL(10,6) COMMENT '商品兑换率 = product_exchange_uv / rc_visit_uv',

    rc_retain_base_1d BIGINT COMMENT '次留分母: 基准日 dt 当日的导量UV',
    rc_retain_uv_1d BIGINT COMMENT '次留分子: 基准日导量用户在 dt+1 日复访权益中心UV (反算: 基准日=dt-1)',
    rc_retain_rate_1d DECIMAL(10,6) COMMENT '权益中心次留率',
    rc_retain_base_7d BIGINT COMMENT '7留分母: 基准日 dt-6 日的导量UV',
    rc_retain_uv_7d BIGINT COMMENT '7留分子: 基准日导量用户在 dt 日复访UV',
    rc_retain_rate_7d DECIMAL(10,6) COMMENT '权益中心7留率',
    rc_retain_base_30d BIGINT COMMENT '30留分母: 基准日 dt-29 日的导量UV',
    rc_retain_uv_30d BIGINT COMMENT '30留分子: 基准日导量用户在 dt 日复访UV',
    rc_retain_rate_30d DECIMAL(10,6) COMMENT '权益中心30日留存率',

    app_retain_base_1d BIGINT COMMENT 'APP次留分母',
    app_retain_uv_1d BIGINT COMMENT 'APP次留分子: 基准日导量用户在 dt 日活跃APP UV',
    app_retain_rate_1d DECIMAL(10,6) COMMENT 'APP次留率',
    app_retain_base_7d BIGINT COMMENT 'APP 7留分母',
    app_retain_uv_7d BIGINT COMMENT 'APP 7留分子',
    app_retain_rate_7d DECIMAL(10,6) COMMENT 'APP 7留率',
    app_retain_base_30d BIGINT COMMENT 'APP 30留分母',
    app_retain_uv_30d BIGINT COMMENT 'APP 30留分子',
    app_retain_rate_30d DECIMAL(10,6) COMMENT 'APP 30日留存率',

    expose_uv BIGINT COMMENT '入口曝光去重UV (事件自归因, eventid对应资源位)',
    expose_pv BIGINT COMMENT '入口曝光PV',
    click_uv BIGINT COMMENT '入口点击去重UV (事件自归因, eventid对应资源位)',
    click_pv BIGINT COMMENT '入口点击PV',

    rc_landing_uv BIGINT COMMENT '落地页(权益中心)访问去重UV, 访问事件按自身资源位自归类',
    rc_landing_pv BIGINT COMMENT '落地页(权益中心)访问PV, 访问事件数',
    ad_watch_uv_recent BIGINT COMMENT '广告观看去重UV, 观看(reward_time)前最近一次访问归因 (task_pay_type=1)',
    ad_watch_pv BIGINT COMMENT '广告观看PV, 完整观看记录数(记录级归因)',
    ad_revenue_recent DECIMAL(20,6) COMMENT '广告收益, 请求(request_time)前最近一次访问归因 (position_name like %权益中心%)',
    product_exchange_uv_recent BIGINT COMMENT '商品兑换去重UV, 兑换(exchange_time)前最近一次访问归因',
    product_exchange_pv BIGINT COMMENT '商品兑换PV, 兑换记录数(记录级归因)',
    sign_uv BIGINT COMMENT '签到去重UV, 签到完成(finishtime)前最近一次访问归因 (ods_db_reward_particular_score_task_nd type=2)',
    sign_rate DECIMAL(10,6) COMMENT '签到率 = sign_uv / rc_landing_uv',

    lofter_dau BIGINT COMMENT 'LOFTER全站日活(每行冗余相同值), 用于计算渗透率',
    rc_penetration_rate DECIMAL(10,6) COMMENT '渗透率 = rc_landing_uv / lofter_dau'
)
COMMENT '权益中心资源位入口监控报表 - 按天分区，维度: source_type × source × user_type'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心资源位入口导量效果监控，含曝光/点击(事件自归因)、落地页访问(访问事件自归类)、广告观看/商品兑换/广告收益/签到(按行为操作时间前最近一次访问归因)、次/7/30日留存及全站日活渗透率',
    'data_source'='lofter.dwd_rewardcenter_visit_di,lofter.ods_mda_app_di,lofter_db_dump.ods_db_reward_particular_score_task_nd',
    'update_frequency'='daily',
    'retention_days'='730'
);
