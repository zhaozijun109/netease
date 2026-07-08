-- 权益中心曝光入口宽表
-- 粒度: dt × userid × source_type × source (当日曝光过的 用户 × 资源位)
-- 数据来源:
--   lofter.ods_mda_app_di            (曝光/点击埋点, 按 eventid 区分资源位; 提供 appchannel/city/occurtime)
--   lofter.dwd_user_new_di           (当日 APP 新用户)
--   lofter.dwd_user_return_di        (当日 APP 回流用户)
--   lofter.dws_par_user_active_dd    (近30天活跃天数)
--   lofter.dws_par_user_base_dd      (地域 country/province/city)
--   lofter.dwd_rewardcenter_visit_di (历史全量, 用于首次进入权益中心的归因资源位)
-- 曝光/点击 eventid 映射(仅 6 个有曝光埋点的资源位):
--   mine_account_loftgrain  fixed   曝光 e1-73  点击 e1-74
--   mine_account_banner     attract 曝光 e1-75  点击 e1-76
--   giftbag_grain_ticket    fixed   曝光 g3-103 点击 g3-104
--   home_upperleft          attract 曝光 a1-37  点击 a1-36
--   explorefeednative_11    attract 曝光 b1-45  点击 b1-46
--   home_float              attract 曝光 ad-65  点击 ad-37
-- 关键口径:
--   user_type       : APP新用户(dwd_user_new_di) > APP回流用户(dwd_user_return_di) > APP老用户
--   active_days_30d : dws_par_user_active_dd 当日近30天活跃天数原值
--   地域            : dws_par_user_base_dd 当日 country/province/city
--   app_channel     : 当日该资源位最近一次曝光(occurtime最大)的 appchannel
--   is_first_entry  : 当前资源位 = 该用户历史首次进入权益中心(dwd_rewardcenter_visit_di 最早有效访问)的归因资源位 → 1
--   is_click        : 同日该用户在该资源位对应点击 eventid 有记录 → 1

CREATE TABLE IF NOT EXISTS lofter_dm.ads_reward_center_expose_entry_wide_di (
    userid BIGINT COMMENT '用户ID',
    source_type STRING COMMENT '来源类型 fixed=常驻 attract=引流 (曝光eventid映射)',
    source STRING COMMENT '资源位英文键 mine_account_loftgrain/home_upperleft/... (曝光eventid映射)',

    user_type STRING COMMENT 'APP新老用户: APP新用户/APP回流用户/APP老用户',
    active_days_30d BIGINT COMMENT '近30天APP活跃天数 (dws_par_user_active_dd)',
    country STRING COMMENT '国家 (dws_par_user_base_dd 当日)',
    province STRING COMMENT '省份 (dws_par_user_base_dd 当日)',
    city STRING COMMENT '城市 (dws_par_user_base_dd 当日)',
    app_channel STRING COMMENT '应用商店渠道, 当日该资源位最近一次曝光的appchannel',
    is_first_entry INT COMMENT '当前资源位是否=该用户历史首次进入权益中心的归因资源位 1是 0否',
    is_click INT COMMENT '同日该用户对该资源位是否有点击 1是 0否',

    expose_cnt BIGINT COMMENT '当日该用户在该资源位的曝光次数'
)
COMMENT '权益中心曝光入口宽表 - 按天分区，粒度: userid × source_type × source'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心曝光入口宽表，含来源(资源位)、用户结构(APP新老/活跃度/地域/应用商店/历史首次入口标记)及是否点击',
    'data_source'='lofter.ods_mda_app_di,lofter.dwd_user_new_di,lofter.dwd_user_return_di,lofter.dws_par_user_active_dd,lofter.dws_par_user_base_dd,lofter.dwd_rewardcenter_visit_di',
    'update_frequency'='daily',
    'retention_days'='730'
);
