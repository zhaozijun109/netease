# ADS_REWARD_CENTER_SOURCE_VISIT_REPORT_DI 实现说明

## 表概述
`lofter_dm.ads_reward_center_source_visit_report_di` 是权益中心资源位入口导量效果监控报表，按天分区，维度组合 `source_type × source × user_type`，覆盖入口曝光/点击、落地页访问、广告观看、商品兑换、广告收益、签到、次/7/30 日留存及全站日活渗透率。

> 需求来源：`docs/LOFTER权益中心资源位导量效果监控报表.md`（POPO：`80f48b26c5324df098204ade96a85c14`）。
>
> **归因方式**：
> - **首次归因（旧列）**：用户当日最早一次有归因访问的 `source`（`occurtime asc`），保留兼容，列名 `rc_visit_uv` / `ad_watch_uv` / `product_exchange_uv` / `ad_revenue` 及全部 `*_retain_*`。
> - **最近归因（新列）**：按单条行为记录，取该用户在行为操作时间之前最近一次有效访问的 `source`（`occurtime <= 操作时间` 取最近）；行为操作时间分别为广告观看 `reward_time` / 兑换 `exchange_time` / 收益 `request_time` / 签到 `finishtime`，操作前无访问归 `unknown`。用于 `ad_watch_*_recent` / `ad_watch_pv` / `ad_revenue_recent` / `product_exchange_*_recent` / `product_exchange_pv` / `sign_*`。
> - **访问事件自归类**：`rc_landing_uv` / `rc_landing_pv` 按每条访问事件自身 `source` 归类，不做前效归因。
> - **事件自归因（曝光/点击）**：`eventid` 唯一对应资源位，直接映射 `source`，不解析 URL。

## 数据源
| 表 | 用途 |
|---|---|
| `lofter.dwd_rewardcenter_visit_di` | 权益中心落地页访问明细，提供 `source_type`、`source`、`occurtime` |
| `lofter.dwd_rewardcenter_user_di` | 用户类别拆分（权益中心新用户 / 权益中心回流用户 / 普通访问用户） |
| `lofter.ods_mda_app_di` | 入口曝光/点击埋点，按 `eventid` 区分资源位 |
| `lofter.dwd_ad_reward_task_complete_di` | 广告完整观看（`task_pay_type=1`），UV + PV |
| `lofter.dwd_ad_reward_score_product_exchange_di` | 商品兑换提交明细，UV + PV |
| `lofter.dwd_user_ad_revenue_di` | 广告收益（筛选 `position_name like '%权益中心%'`） |
| `lofter_db_dump.ods_db_reward_particular_score_task_nd` | 签到（`type=2`，`date` 为 `yyyyMMdd`） |
| `lofter.dws_par_user_active_di` | APP 活跃用户，用于 APP 留存 + 全站日活 |

## 入口曝光/点击 eventid 映射（事件自归因）
| source_type | source | 曝光 eventid | 点击 eventid |
|---|---|---|---|
| fixed | mine_entrance（每日福利） | 无（曝光置 0） | e1-45 |
| fixed | mine_account_loftgrain（乐乎米） | e1-73 | e1-74 |
| attract | mine_account_banner（滚动 banner） | e1-75 | e1-76 |
| fixed | giftbag_grain_ticket（粮票页） | g3-103 | g3-104 |
| attract | home_upperleft（首页吸顶） | a1-37 | a1-36 |
| attract | explorefeednative_11（信息流定坑） | b1-45 | b1-46 |
| fixed | rc_subscribe（订阅提醒） | 无（曝光置 0） | e2-17 |
| attract | privatemessage（私信） | 无（曝光置 0） | im2-52 |
| recall | push | 无（曝光置 0） | rd-1 |
| attract | home_float（吸边） | ad-65 | ad-37 |

> 曝光/点击用户当日未访问权益中心时（无 `dwd_rewardcenter_user_di` 记录），`user_type` 归入 `未访问用户`。

## 维度
| 字段 | 说明 |
|---|---|
| `dt` | 分区日期（观察日 / 留存复访日），格式 `yyyy-MM-dd` |
| `source_type` | 来源类型：`fixed=常驻`、`attract=引流`、`recall=召回`，取自 `dwd_rewardcenter_visit_di.source_type`；用户当日所有访问均无归因时落入 `unknown` |
| `source` | 资源位英文键（`mine_entrance`、`mine_account_loftgrain`、`home_upperleft`、`explorefeednative_11` 等）；用户当日所有访问均无归因时落入 `unknown` |
| `user_type` | 用户类别（互斥三类，优先级：权益中心新用户 > 权益中心回流用户 > 普通访问用户）：<br>`权益中心新用户` = `is_new_user > 0`<br>`权益中心回流用户` = `is_new_user = 0 AND is_return_user > 0`<br>`普通访问用户` = `is_new_user = 0 AND is_return_user = 0` |

## 首次来源归因
同一用户当日可能从多个资源位进入权益中心。为避免在 source 维度上重复计数，采用 **优先归因 + unknown 兜底** 策略：

```sql
row_number() over (
    partition by userId
    order by CASE WHEN source IS NULL OR trim(source) = ''
                    OR source_type IS NULL OR trim(source_type) = ''
                  THEN 1 ELSE 0 END,
             occurtime asc
) = 1
```

排序规则：
1. **优先非空**：`source` 与 `source_type` 同时非空的访问优先（CASE = 0）。
2. **同优先级取最早**：在优先级相同的记录中按 `occurtime asc` 取首条。

因此对每个 userId：
- 当日存在任一有归因的访问 → 取最早的那条有归因访问的 `(source_type, source)`。
- 当日所有访问均缺失 `source` 或 `source_type` → 取最早一条空记录，外层 `nvl(nullif(trim(x), ''), 'unknown')` 兜底为 `(unknown, unknown)`，用于观测未归因访问规模与转化/留存表现。

## 当日指标口径
### 首次归因（旧列，保留兼容）
| 指标 | 计算逻辑 |
|---|---|
| `rc_visit_uv` | 当日通过该资源位首次访问权益中心的去重 userId |
| `ad_watch_uv` | 首次归因用户当日完整观看广告（`task_pay_type=1`）的去重 userId |
| `product_exchange_uv` | 首次归因用户当日提交商品兑换的去重 userId |
| `ad_revenue` | 首次归因用户在 `dwd_user_ad_revenue_di` 中 `position_name like '%权益中心%'` 的 `money` 求和 |
| `ad_watch_rate` | `ad_watch_uv / rc_visit_uv` |
| `product_exchange_rate` | `product_exchange_uv / rc_visit_uv` |

### 事件自归因（曝光/点击）
| 指标 | 计算逻辑 |
|---|---|
| `expose_uv` / `expose_pv` | 按 eventid 映射资源位的曝光去重 UV / 曝光 PV |
| `click_uv` / `click_pv` | 按 eventid 映射资源位的点击去重 UV / 点击 PV |

### 最近归因（新列，按行为记录）
按**单条行为记录**，取该用户在"行为操作时间"之前最近一次有效访问的 `(source_type, source)`（`occurtime <= 操作时间`，`occurtime desc` 取最近一条）；操作前无有效访问的记录归入 `unknown`。行为操作时间与 `occurtime` 均为毫秒 epoch。落地页访问不做归因，直接按访问事件自身 `source` 归类。

| 指标 | 计算逻辑 |
|---|---|
| `rc_landing_uv` / `rc_landing_pv` | 落地页访问事件自归类：每条访问按自身 `source` 计，UV = 该 source 去重访问用户，PV = 访问事件数 |
| `ad_watch_uv_recent` / `ad_watch_pv` | 广告完整观看（`task_pay_type=1`），按观看 `reward_time` 前最近一次访问归因；UV = 去重 userId，PV = 观看记录数 |
| `ad_revenue_recent` | 广告收益按请求 `request_time` 前最近一次访问归因后 `money` 求和 |
| `product_exchange_uv_recent` / `product_exchange_pv` | 商品兑换按 `exchange_time` 前最近一次访问归因；UV = 去重 userId，PV = 兑换记录数 |
| `sign_uv` | 签到（`ods_db_reward_particular_score_task_nd` `type=2` 且 `date=当日yyyyMMdd`），按签到完成 `finishtime` 前最近一次访问归因的去重 UV |
| `sign_rate` | `sign_uv / rc_landing_uv`（分母为 0 输出 0） |

### 日活 / 渗透率
| 指标 | 计算逻辑 |
|---|---|
| `lofter_dau` | LOFTER 全站日活 = `dws_par_user_active_di` 当日去重 userId，**每行冗余相同值** |
| `rc_penetration_rate` | `rc_landing_uv / lofter_dau`（分母为 0 输出 0） |

> 具体商品兑换（按 `productId` 拆分）仍落在 `lofter_dm.ads_reward_center_source_product_exchange_report_di`，已同步改为最近归因。

## 留存指标口径
分区 `dt` 写入的留存为 **基准日导量用户在 dt 日的复访情况**：

| 指标 | 基准日 | 复访观察日 |
|---|---|---|
| `*_retain_*_1d` | `dt - 1`（`flow.2.days.ago`） | `dt`（`flow.1.days.ago`） |
| `*_retain_*_7d` | `dt - 6`（`flow.7.days.ago`） | `dt` |
| `*_retain_*_30d` | `dt - 29`（`flow.30.days.ago`） | `dt` |

- 基准日同样使用 **首次来源归因** + `dwd_rewardcenter_user_di` 当日用户类别。
- 权益中心留存：基准日导量用户在 `dt` 日 `dwd_rewardcenter_visit_di` 出现即视为留存。
- APP 留存：基准日导量用户在 `dt` 日 `dws_par_user_active_di` 出现即视为留存。
- 分母 `*_retain_base_*` = 基准日的导量 UV；分子 `*_retain_uv_*` = 留存 UV；率 = 分子 / 分母（分母为 0 时输出 0）。

> 因留存基准日不同，同一分区内 `rc_visit_uv` 与 `*_retain_base_*` 不会相等，使用方在 BI 层求率时应分别 sum 后再相除。

## 任务依赖
```
ads_reward_center_source_visit_report_di
├── dwd_rewardcenter_visit_di
├── dwd_rewardcenter_user_di
├── ods_mda_app_di
├── dwd_ad_reward_task_complete_di
├── dwd_ad_reward_score_product_exchange_di
├── dwd_user_ad_revenue_di
├── ods_db_reward_particular_score_task_nd
└── dws_par_user_active_di
```

## 调度配置
- 任务类型：`sparksql`
- 执行频率：每日
- 分区写入：`dt = ${azkaban.flow.1.days.ago}`
- 历史回看窗口：30 天（覆盖 30 日留存）

## 后续待办
1. 曝光埋点缺失的资源位（mine_entrance / rc_subscribe / privatemessage / push）待客户端补齐曝光 eventid 后，`expose_uv/pv` 由 0 替换为真实值。
2. `explorefeednative_11`（b1-45/b1-46）为信息流通用埋点，若后续发现同 eventid 复用于非权益中心场景，需追加 `target_url like '%reward-center%'` 过滤。
3. `a1-37/a1-36`（首页吸顶）同为通用埋点，如需与 `pve-boyfriends` 等其它落地页拆分，可复用 `dwd_home_top_resource_visit_di` 的 URL 过滤逻辑。
