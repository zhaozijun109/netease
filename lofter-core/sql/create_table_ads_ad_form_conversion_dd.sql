-- 广告单投放转化汇总（广告位 147001）
-- 维度：日 x 业务；指标：曝光pv、点击pv、曝光uv、点击uv、落地页访问uv
-- 曝光/点击：lofter.dwd_ad_actions_v2_di（is_bg/is_click），uv 按 adid+userid 去重
-- 落地页：lofter.ods_mda_app_di 埋点 eventid='b1-45'，按 target_url 关键词归类业务
create table if not exists lofter_dm.ads_ad_form_conversion_di(
    business string comment '业务名称：权益中心/破次元/里世界/星匣/拼图/内容付费/其他',
    expose_pv bigint comment '曝光pv',
    click_pv bigint comment '点击pv',
    expose_uv bigint comment '曝光uv，按 adid+userid 去重',
    click_uv bigint comment '点击uv，按 adid+userid 去重',
    landing_uv bigint comment '落地页访问uv，按 userid 去重'
) partitioned by (dt string) STORED AS PARQUET
;
