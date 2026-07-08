-- 权益中心商品兑换来源分布报表
-- 数据来源:
--   lofter.dwd_ad_reward_score_product_exchange_di (商品兑换明细，含 productId/product_name/category/subCategory/product_count/score)
--   lofter.dwd_rewardcenter_visit_di               (访问明细，提供最近来源 source_type/source)
--   lofter.dwd_rewardcenter_user_di                (用户类别: 权益中心新用户/回流用户/存量用户)
-- 维度: source_type × source × user_type × productId
-- 归因: 按单条兑换记录，取该用户在兑换时间(exchange_time)之前最近一次有效访问的 (source_type, source) 归因；兑换前无访问则走 'unknown'

CREATE TABLE IF NOT EXISTS lofter_dm.ads_reward_center_source_product_exchange_report_di (
    source_type STRING COMMENT '来源类型 fixed=常驻 attract=引流 recall=召回，未归因为 unknown',
    source STRING COMMENT '资源位英文键 mine_entrance/home_upperleft/...，未归因为 unknown',

    productId BIGINT COMMENT '商品ID',
    product_name STRING COMMENT '商品名称',
    category STRING COMMENT '商品大类',
    subCategory STRING COMMENT '商品子类',

    exchange_uv BIGINT COMMENT '兑换去重UV (count distinct userId)',
    exchange_cnt BIGINT COMMENT '兑换记录数 (count distinct exchange_recordid)',
    exchange_product_qty BIGINT COMMENT '兑换商品总件数 (sum product_count)',
    exchange_score BIGINT COMMENT '消耗积分总和 (sum score)',

    user_type STRING COMMENT '用户类别 权益中心新用户/权益中心回流用户/权益中心存量用户'
)
COMMENT '权益中心商品兑换来源分布 - 按天分区，维度: source_type × source × user_type × productId'
PARTITIONED BY (dt STRING COMMENT '分区日期 yyyy-MM-dd')
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'creators'='lofter-etl',
    'description'='权益中心商品兑换来源归因分布，统计不同 source_type/source/user_type 下各商品的兑换量、件数、积分消耗',
    'data_source'='lofter.dwd_ad_reward_score_product_exchange_di,lofter.dwd_rewardcenter_visit_di,lofter.dwd_rewardcenter_user_di',
    'update_frequency'='daily',
    'retention_days'='730'
);
