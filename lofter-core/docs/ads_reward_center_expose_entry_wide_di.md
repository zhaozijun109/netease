# 权益中心曝光入口宽表

`lofter_dm.ads_reward_center_expose_entry_wide_di`

## 概述

权益中心曝光入口宽表，按天分区，粒度为 `userid × source_type × source`（当日曝光过的 用户 × 资源位）。用于分析各曝光入口的用户结构与点击表现。每行代表某用户当日在某资源位的曝光聚合，附带该用户的 APP 新老、活跃度、地域、应用商店、历史首次入口标记及是否点击。

> **粒度**：`dt × userid × source_type × source`，一个用户在一个资源位当日一行。
> **是否点击**：同日该用户对该资源位对应点击 eventid 是否有记录。

## 数据源

| 表 | 用途 |
|---|---|
| `lofter.ods_mda_app_di` | 曝光/点击埋点，按 `eventid` 区分资源位；提供 `appchannel`、`occurtime` |
| `lofter.dwd_user_new_di` | 当日 APP 新用户 |
| `lofter.dwd_user_return_di` | 当日 APP 回流用户 |
| `lofter.dws_par_user_active_dd` | 近30天活跃天数 `active_days_30d` |
| `lofter.dws_par_user_base_dd` | 地域 `country`/`province`/`city` |
| `lofter.dwd_rewardcenter_visit_di` | 历史全量，首次进入权益中心的归因资源位 |

## 曝光/点击 eventid 映射

仅 6 个资源位有曝光埋点，曝光与点击 eventid 成对：

| source | source_type | 曝光 eventid | 点击 eventid |
|---|---|---|---|
| `mine_account_loftgrain` | fixed | `e1-73` | `e1-74` |
| `mine_account_banner` | attract | `e1-75` | `e1-76` |
| `giftbag_grain_ticket` | fixed | `g3-103` | `g3-104` |
| `home_upperleft` | attract | `a1-37` | `a1-36` |
| `explorefeednative_11` | attract | `b1-45` | `b1-46` |
| `home_float` | attract | `ad-65` | `ad-37` |

## 字段口径

| 字段 | 计算逻辑 |
|---|---|
| `userid` | 用户 ID（`ods_mda_app_di.userId > 0`） |
| `source_type` / `source` | 由曝光 eventid 直接映射，事件自归因，不解析 URL |
| `user_type` | 优先级：`dwd_user_new_di` → `APP新用户`；否则 `dwd_user_return_di` → `APP回流用户`；否则 `APP老用户` |
| `active_days_30d` | `dws_par_user_active_dd` 当日近30天活跃天数原值，无记录取 0 |
| `country` / `province` / `city` | `dws_par_user_base_dd` 当日地域 |
| `app_channel` | 当日该用户在该资源位**最近一次曝光**（`occurtime` 最大）的 `appchannel`，用 `max_by` 取值 |
| `is_first_entry` | 该用户历史首次进入权益中心（`dwd_rewardcenter_visit_di` 最早一条有效访问）的归因资源位 = 当前行 `(source_type, source)` → 1，否则 0 |
| `is_click` | 同日该用户在该资源位对应点击 eventid 有记录 → 1，否则 0 |
| `expose_cnt` | 当日该用户在该资源位的曝光事件次数 |

## 归因说明

- **曝光/点击**：`eventid` 唯一对应资源位，事件自归因；仅 6 个有曝光埋点的资源位会出现在本表。
- **历史首次入口**：基于 `dwd_rewardcenter_visit_di`，扫描 `dt <= 当日` 全量分区，按 `userId` 取 `occurtime asc` 最早一条有效访问（`source`/`source_type` 非空优先）的归因资源位；与当前曝光行资源位一致时 `is_first_entry=1`。每用户全局至多一个资源位为首次入口。

## 依赖与性能

- 依赖 T-1 分区：`ods_mda_app_di`、`dwd_user_new_di`、`dwd_user_return_di`、`dws_par_user_active_dd`、`dws_par_user_base_dd`。
- `first_entry` **全量扫描** `dwd_rewardcenter_visit_di` 历史分区（`dt <= T-1`），随历史累积扫描量增大；如需降本可后续限定权益中心上线起始日期或改为维护增量首次入口维表。
- `ods_mda_app_di` 为超大分区表，查询严格限定 `dt = '${azkaban.flow.1.days.ago}'`。

## 调度

- 每日跑 T-1 分区，`INSERT OVERWRITE PARTITION(dt='${azkaban.flow.1.days.ago}')`。
