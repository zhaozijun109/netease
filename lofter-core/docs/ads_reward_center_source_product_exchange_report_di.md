# ADS_REWARD_CENTER_SOURCE_PRODUCT_EXCHANGE_REPORT_DI 实现说明

## 表概述
`lofter_dm.ads_reward_center_source_product_exchange_report_di` 是权益中心商品兑换来源分布报表，按天分区，维度组合 `source_type × source × user_type × productId`，统计不同来源、不同用户类别下各商品的兑换量、件数与积分消耗。

> 配套报表：`ads_reward_center_source_visit_report_di`（资源位入口监控），两表共用同一套最近来源归因口径，使用方可按 `source_type × source × user_type` 关联使用。

## 数据源
| 表 | 用途 |
|---|---|
| `lofter.dwd_ad_reward_score_product_exchange_di` | 商品兑换明细，提供 `productId`/`product_name`/`category`/`subCategory`/`product_count`/`score` |
| `lofter.dwd_rewardcenter_visit_di` | 提供当日最近访问来源 `(source_type, source)` |
| `lofter.dwd_rewardcenter_user_di` | 提供用户类别 `is_new_user`/`is_return_user` |

## 维度
| 字段 | 说明 |
|---|---|
| `dt` | 分区日期，格式 `yyyy-MM-dd` |
| `source_type` | 来源类型：`fixed=常驻`、`attract=引流`、`recall=召回`，未归因写 `unknown` |
| `source` | 资源位英文键（`mine_entrance`、`home_upperleft` 等），未归因写 `unknown` |
| `user_type` | 用户类别（优先级：新用户 > 回流用户 > 存量用户）：<br>`权益中心新用户` = `is_new_user > 0`<br>`权益中心回流用户` = `is_new_user = 0 AND is_return_user > 0`<br>`权益中心存量用户` = 其余（含未匹配到 `dwd_rewardcenter_user_di` 的兑换用户） |
| `productId` | 商品ID |
| `product_name` / `category` / `subCategory` | 商品冗余属性，便于直接切片 |

## 归因逻辑
按**单条兑换记录**归因：取该用户在兑换时间 `exchange_time` **之前最近一次**有效访问的资源位。

```sql
row_number() over (
    partition by exchange_recordid
    order by CASE WHEN v.occurtime IS NULL THEN 1 ELSE 0 END, v.occurtime desc
) = 1
-- 关联条件: v.userId = e.userId AND v.occurtime <= e.exchange_time
```

以每条兑换记录（`exchange_recordid`）为粒度，LEFT JOIN 当日 `dwd_rewardcenter_visit_di` 中该用户 `occurtime <= exchange_time` 的有效访问（`source_type`/`source` 非空），按 `occurtime desc` 取兑换前最近的一条；再 LEFT JOIN `dwd_rewardcenter_user_di` 取用户类别：

- 命中：兑换归因到兑换前最近一次访问的资源位。
- 兑换前无有效访问：`source_type` = `source` = `unknown`。
- 未命中用户类别：归入 `权益中心存量用户`。

> `occurtime` 与 `exchange_time` 均按毫秒时间戳比较；同一用户当日多次兑换会按各自兑换时刻分别归因到不同资源位。

## 指标
| 指标 | 计算逻辑 |
|---|---|
| `exchange_uv` | `count(distinct userId)` 兑换去重用户数 |
| `exchange_cnt` | `count(distinct exchange_recordid)` 兑换记录条数 |
| `exchange_product_qty` | `sum(product_count)` 兑换商品总件数 |
| `exchange_score` | `sum(score)` 消耗积分总和 |

> 商品维度下 `product_name/category/subCategory` 用 `max()` 聚合（同一 productId 当日属性应稳定，取任意一条即可）。

## 任务依赖
```
ads_reward_center_source_product_exchange_report_di
├── dwd_ad_reward_score_product_exchange_di
├── dwd_rewardcenter_visit_di
└── dwd_rewardcenter_user_di
```

## 调度配置
- 任务类型：`sparksql`
- 执行频率：每日
- 分区写入：`dt = ${azkaban.flow.1.days.ago}`

## 使用建议
- 按 `source_type/source` 看资源位贡献的兑换分布。
- 按 `productId` 看不同商品在哪些资源位更受欢迎。
- 与 `ads_reward_center_source_visit_report_di` 的 `rc_visit_uv` 关联，可算资源位级"访问→兑换"转化率（`exchange_uv / rc_visit_uv`）。

## 后续待办
1. 若上线 `position_id` 与 `source` 的精确映射，可补 `position_id` 维度替代 `unknown` 兜底。
2. 商品维度可与商品主数据表 JOIN 增加上架时间、价格等属性，做更精细分析。
