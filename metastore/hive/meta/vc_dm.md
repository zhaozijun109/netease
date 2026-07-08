# 数据库: vc_dm

> 本文档包含数据库 `vc_dm` 中**最近 30 天内有更新**的 67 张表的元数据信息，用于 AI 训练学习。
> （库内共 69 张表，2 张表因超过 30 天未更新已被过滤）

## 表目录

1. [ads_appversion_device_dd](#ads_appversion_device_dd) - 无描述
2. [ads_deviceos_appversion_device_report_di](#ads_deviceos_appversion_device_report_di) - 无描述
3. [ads_deviceosversion_device_dd](#ads_deviceosversion_device_dd) - 无描述
4. [ads_growth_dau_stratify_report_di](#ads_growth_dau_stratify_report_di) - 无描述
5. [ads_member_renew_report_di](#ads_member_renew_report_di) - 无描述
6. [ads_member_report_di](#ads_member_report_di) - 无描述
7. [ads_new_register_action_convert_report_di](#ads_new_register_action_convert_report_di) - 无描述
8. [ads_new_register_chat_convert_report_di](#ads_new_register_chat_convert_report_di) - 无描述
9. [ads_pve_app_chat_base_data_di](#ads_pve_app_chat_base_data_di) - 无描述
10. [ads_pve_app_chat_retain_data_di](#ads_pve_app_chat_retain_data_di) - 无描述
11. [ads_theater_active_user_vc_report_di](#ads_theater_active_user_vc_report_di) - 无描述
12. [ads_theater_main_data_vc_report_di](#ads_theater_main_data_vc_report_di) - 无描述
13. [ads_theater_user_chat_retain_vc_report_di](#ads_theater_user_chat_retain_vc_report_di) - 无描述
14. [ads_theater_user_retain_vc_report_di](#ads_theater_user_retain_vc_report_di) - 无描述
15. [ads_vc_3mincrush_report_di](#ads_vc_3mincrush_report_di) - 无描述
16. [ads_vc_3mincrush_server_report_di](#ads_vc_3mincrush_server_report_di) - 无描述
17. [ads_vc_active_chat_consum_report_di](#ads_vc_active_chat_consum_report_di) - 无描述
18. [ads_vc_active_user_retain_report_di](#ads_vc_active_user_retain_report_di) - 无描述
19. [ads_vc_ad_cvr_di](#ads_vc_ad_cvr_di) - 无描述
20. [ads_vc_ad_cvr_new_di](#ads_vc_ad_cvr_new_di) - 广告转化数据日报表
21. [ads_vc_ai_model_errormsg_report_di](#ads_vc_ai_model_errormsg_report_di) - 无描述
22. [ads_vc_ai_scene_report_di](#ads_vc_ai_scene_report_di) - 无描述
23. [ads_vc_base_data_report_di](#ads_vc_base_data_report_di) - 无描述
24. [ads_vc_character_dailyfree_token_cnt_report_di](#ads_vc_character_dailyfree_token_cnt_report_di) - 无描述
25. [ads_vc_chat_project_report_di](#ads_vc_chat_project_report_di) - 无描述
26. [ads_vc_consum_amount_retain_report_di](#ads_vc_consum_amount_retain_report_di) - 无描述
27. [ads_vc_consum_uv_retain_report_di](#ads_vc_consum_uv_retain_report_di) - 无描述
28. [ads_vc_core_table_active_energy_report_di](#ads_vc_core_table_active_energy_report_di) - 无描述
29. [ads_vc_custom_rank_dd](#ads_vc_custom_rank_dd) - 无描述
30. [ads_vc_daily_user_simulator_dd](#ads_vc_daily_user_simulator_dd) - 无描述
31. [ads_vc_data_user_plotsimulator_model_dd](#ads_vc_data_user_plotsimulator_model_dd) - 无描述
32. [ads_vc_device_retain_data_report_di](#ads_vc_device_retain_data_report_di) - 无描述
33. [ads_vc_device_retain_new_report_di](#ads_vc_device_retain_new_report_di) - 无描述
34. [ads_vc_energy_data_report_di](#ads_vc_energy_data_report_di) - 无描述
35. [ads_vc_energy_group_report_di](#ads_vc_energy_group_report_di) - 无描述
36. [ads_vc_energy_kpi_report_di](#ads_vc_energy_kpi_report_di) - 无描述
37. [ads_vc_event_info_report_di](#ads_vc_event_info_report_di) - 无描述
38. [ads_vc_growth_ad_period_cvr_di](#ads_vc_growth_ad_period_cvr_di) - 无描述
39. [ads_vc_growth_arpu_report_di](#ads_vc_growth_arpu_report_di) - 无描述
40. [ads_vc_growth_ltv_report_di](#ads_vc_growth_ltv_report_di) - 无描述
41. [ads_vc_new_device_login_active_retain_report_di](#ads_vc_new_device_login_active_retain_report_di) - 无描述
42. [ads_vc_new_register_retain_rate_report_di](#ads_vc_new_register_retain_rate_report_di) - 无描述
43. [ads_vc_new_register_role_chat_report_di](#ads_vc_new_register_role_chat_report_di) - 无描述
44. [ads_vc_new_register_role_recommend_report_di](#ads_vc_new_register_role_recommend_report_di) - 无描述
45. [ads_vc_new_register_user_chat_kpi_report_di](#ads_vc_new_register_user_chat_kpi_report_di) - 无描述
46. [ads_vc_ogc_ugc_summary_report_di](#ads_vc_ogc_ugc_summary_report_di) - 无描述
47. [ads_vc_report_active_data_di](#ads_vc_report_active_data_di) - 无描述
48. [ads_vc_report_app_cumsum_data_di](#ads_vc_report_app_cumsum_data_di) - 无描述
49. [ads_vc_report_app_device_appversion_dd](#ads_vc_report_app_device_appversion_dd) - 无描述
50. [ads_vc_report_app_device_deviceosversion_dd](#ads_vc_report_app_device_deviceosversion_dd) - 无描述
51. [ads_vc_role_data_report_di](#ads_vc_role_data_report_di) - 无描述
52. [ads_vc_send_message_user_report_di](#ads_vc_send_message_user_report_di) - 无描述
53. [ads_vc_simulator_base_data_report_di](#ads_vc_simulator_base_data_report_di) - 无描述
54. [ads_vc_simulator_detail_data_report_di](#ads_vc_simulator_detail_data_report_di) - 无描述
55. [ads_vc_user_active_data_v2_report_di](#ads_vc_user_active_data_v2_report_di) - 无描述
56. [ads_vc_user_chat_base_data_report_di](#ads_vc_user_chat_base_data_report_di) - 无描述
57. [ads_vc_user_chat_data_v2_report_di](#ads_vc_user_chat_data_v2_report_di) - 无描述
58. [ads_vc_user_chat_preference_report_di](#ads_vc_user_chat_preference_report_di) - 用户聊天OGC UGC偏好及关键指标分析日报表
59. [ads_vc_user_chat_retain_report_di](#ads_vc_user_chat_retain_report_di) - 无描述
60. [ads_vc_user_chats_churn_di](#ads_vc_user_chats_churn_di) - 无描述
61. [ads_vc_user_consum_data_v2_report_di](#ads_vc_user_consum_data_v2_report_di) - 无描述
62. [ads_vc_user_funnel_data_v2_report_di](#ads_vc_user_funnel_data_v2_report_di) - 无描述
63. [ads_vc_user_group_core_metrics_report_di](#ads_vc_user_group_core_metrics_report_di) - 个人群核心指标探查日报
64. [ads_vc_user_interview_retain_report_di](#ads_vc_user_interview_retain_report_di) - 无描述
65. [ads_vc_user_paid_retain_report_di](#ads_vc_user_paid_retain_report_di) - 无描述
66. [ads_vc_user_paid_retain_v2_report_di](#ads_vc_user_paid_retain_v2_report_di) - 无描述
67. [ads_vc_user_transfer_report_di](#ads_vc_user_transfer_report_di) - 无描述

---

## ads_appversion_device_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_appversion_device_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 26.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 26.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | userid |
| 3 | `occurtime` | `bigint` |  | 事件时刻 |
| 4 | `appversion` | `string` |  | 版本号 |
| 5 | `dt` | `string` |  |  |

---

## ads_deviceos_appversion_device_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_deviceos_appversion_device_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.8M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appversion` | `string` |  | app版本号 |
| 2 | `deviceos` | `string` |  | 系统类型 |
| 3 | `deviceosversion` | `string` |  | 系统版本号 |
| 4 | `device_uv` | `bigint` |  | 设备数 |
| 5 | `user_uv` | `bigint` |  | 用户数 |
| 6 | `appchannel` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_deviceosversion_device_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_deviceosversion_device_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 18.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 18.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | userid |
| 3 | `occurtime` | `bigint` |  | 事件时刻 |
| 4 | `deviceosversion` | `string` |  | 系统版本号 |
| 5 | `deviceos` | `string` |  | 系统类型 |
| 6 | `dt` | `string` |  |  |

---

## ads_growth_dau_stratify_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_growth_dau_stratify_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.3M |
| **是否分区表** | 是 |

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dau` | `bigint` |  |  |
| 2 | `new_active` | `bigint` |  |  |
| 3 | `return_active` | `bigint` |  |  |
| 4 | `new_channel_active` | `bigint` |  |  |
| 5 | `new_natural_active` | `bigint` |  |  |
| 6 | `return_channel_active` | `bigint` |  |  |
| 7 | `return_natural_active` | `bigint` |  |  |
| 8 | `old_active` | `bigint` |  |  |
| 9 | `old_1d_device` | `bigint` |  |  |
| 10 | `old_2d_device` | `bigint` |  |  |
| 11 | `old_3d_device` | `bigint` |  |  |
| 12 | `old_4d_device` | `bigint` |  |  |
| 13 | `old_5d_device` | `bigint` |  |  |
| 14 | `old_6d_device` | `bigint` |  |  |
| 15 | `old_7d_device` | `bigint` |  |  |
| 16 | `old_8d_device` | `bigint` |  |  |
| 17 | `old_9d_device` | `bigint` |  |  |
| 18 | `old_10d_device` | `bigint` |  |  |
| 19 | `old_11d_device` | `bigint` |  |  |
| 20 | `old_12d_device` | `bigint` |  |  |
| 21 | `old_13d_device` | `bigint` |  |  |
| 22 | `old_14d_device` | `bigint` |  |  |
| 23 | `old_15d_device` | `bigint` |  |  |
| 24 | `old_16d_device` | `bigint` |  |  |
| 25 | `old_17d_device` | `bigint` |  |  |
| 26 | `old_18d_device` | `bigint` |  |  |
| 27 | `old_19d_device` | `bigint` |  |  |
| 28 | `old_20d_device` | `bigint` |  |  |
| 29 | `old_21d_device` | `bigint` |  |  |
| 30 | `old_22d_device` | `bigint` |  |  |
| 31 | `old_23d_device` | `bigint` |  |  |
| 32 | `old_24d_device` | `bigint` |  |  |
| 33 | `old_25d_device` | `bigint` |  |  |
| 34 | `old_26d_device` | `bigint` |  |  |
| 35 | `old_27d_device` | `bigint` |  |  |
| 36 | `old_28d_device` | `bigint` |  |  |
| 37 | `old_29d_device` | `bigint` |  |  |
| 38 | `old_30d_device` | `bigint` |  |  |
| 39 | `dt` | `string` |  |  |

---

## ads_member_renew_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_member_renew_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 1.2M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `valid_end_dt` | `string` |  | 过期日期 |
| 2 | `diff` | `bigint` |  | 过期天数 |
| 3 | `origin_type` | `string` |  |  |
| 4 | `device_os` | `string` |  |  |
| 5 | `origin` | `string` |  |  |
| 6 | `new_register_level` | `string` |  | 注册天数 |
| 7 | `user_cnt` | `bigint` |  | 过期用户数 |
| 8 | `day0_user_cnt` | `bigint` |  | 当天续费人用户 |
| 9 | `day3_user_cnt` | `bigint` |  | 3天内续费人用户 |
| 10 | `day7_user_cnt` | `bigint` |  | 7天内续费人用户 |
| 11 | `day15_user_cnt` | `bigint` |  | 15天内续费人用户 |
| 12 | `dt` | `string` |  |  |

---

## ads_member_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_member_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 5.6M |
| **是否分区表** | 是 |

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `origin_type` | `string` |  | origin_type |
| 2 | `device_os` | `string` |  | device_os |
| 3 | `origin` | `string` |  | origin |
| 4 | `day_active_member_user_cnt` | `bigint` |  | 日活跃会员数 |
| 5 | `day_active_user_cnt` | `bigint` |  | 日活跃用户数 |
| 6 | `week_active_member_user_cnt` | `bigint` |  | 周活跃会员数 |
| 7 | `week_active_user_cnt` | `bigint` |  | 周活跃用户数 |
| 8 | `week_active_member_days_cnt` | `bigint` |  | 周活跃会员活跃天数 |
| 9 | `week_active_days_cnt` | `bigint` |  | 周活跃用户活跃天数 |
| 10 | `month_active_member_user_cnt` | `bigint` |  | 月活跃会员数 |
| 11 | `month_active_user_cnt` | `bigint` |  | 月活跃用户数 |
| 12 | `month_active_member_days_cnt` | `bigint` |  | 月活跃会员活跃天数 |
| 13 | `month_active_days_cnt` | `bigint` |  | 月活跃用户活跃天数 |
| 14 | `day_cnt_order` | `bigint` |  | 日总订单数 |
| 15 | `day_order_price` | `bigint` |  | 日总订单金额 |
| 16 | `day_user_order_cnt` | `bigint` |  | 日总订单用户数 |
| 17 | `day_member_cnt_order` | `bigint` |  | 日会员订单数 |
| 18 | `day_member_order_price` | `bigint` |  | 日会员订单金额 |
| 19 | `day_member_user_order_cnt` | `bigint` |  | 日会员订单用户数 |
| 20 | `day_member_no_member_cnt_order` | `bigint` |  | 日会员非会员卡订单数 |
| 21 | `day_member_no_member_order_price` | `bigint` |  | 日会员非会员卡订单金额 |
| 22 | `day_member_no_member_user_order_cnt` | `bigint` |  | 日会员非会员卡订单用户数 |
| 23 | `new_register_level` | `string` |  | 注册天数 |
| 24 | `day_member_talk_cnt` | `bigint` |  | 日会员聊天轮数 |
| 25 | `day_member_talk_user_cnt` | `bigint` |  | 日会员聊天用户数 |
| 26 | `day_active_talk_cnt` | `bigint` |  | 日用户聊天轮数 |
| 27 | `day_active_talk_user_cnt` | `bigint` |  | 日聊天用户数 |
| 28 | `week_active_member_activesdays_cnt` | `bigint` |  | 周活跃会员活跃天数 |
| 29 | `week_active_activesdays_cnt` | `bigint` |  | 周活跃用户活跃天数 |
| 30 | `month_active_member_activesdays_cnt` | `bigint` |  | 月活跃会员活跃天数 |
| 31 | `month_active_activesdays_cnt` | `bigint` |  | 月活跃用户活跃天数 |
| 32 | `dt` | `string` |  |  |

---

## ads_new_register_action_convert_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_new_register_action_convert_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 2.0M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `register_date` | `string` |  | 注册日期 |
| 2 | `origin_type` | `string` |  | origin_type |
| 3 | `device_os` | `string` |  | device_os |
| 4 | `origin` | `string` |  | origin |
| 5 | `register_user_cnt` | `bigint` |  | 注册用户数 |
| 6 | `chat_detail_expose_uv` | `bigint` |  | 聊天详情页曝光uv |
| 7 | `chat_detail_expose_pv` | `bigint` |  | 聊天详情页曝光pv |
| 8 | `talk_cnt` | `bigint` |  | 聊天轮数 |
| 9 | `talk_user_cnt` | `bigint` |  | 聊天人数 |
| 10 | `automatic_inspiration_click_pv` | `bigint` |  | 自动灵感点击pv |
| 11 | `automatic_inspiration_click_uv` | `bigint` |  | 自动灵感点击uv |
| 12 | `manual_inspiration_click_pv` | `bigint` |  | 手动灵感点击pv |
| 13 | `manual_inspiration_click_uv` | `bigint` |  | 手动灵感点击uv |
| 14 | `interaction_click_pv` | `bigint` |  | 互动点击pv |
| 15 | `interaction_click_uv` | `bigint` |  | 互动点击uv |
| 16 | `task_page_expose_pv` | `bigint` |  | 任务列表页曝光pv |
| 17 | `task_page_expose_uv` | `bigint` |  | 任务列表页曝光uv |
| 18 | `task_page_click_pv` | `bigint` |  | 任务列表页点击pv |
| 19 | `task_page_click_uv` | `bigint` |  | 任务列表页点击uv |
| 20 | `event_click_pv` | `bigint` |  | 事件点击pv |
| 21 | `event_click_uv` | `bigint` |  | 事件点击uv |
| 22 | `dt` | `string` |  |  |

---

## ads_new_register_chat_convert_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_new_register_chat_convert_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 8.2M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `diff` | `string` |  | 注册天数 |
| 2 | `origin_type` | `string` |  | origin_type |
| 3 | `device_os` | `string` |  | device_os |
| 4 | `origin` | `string` |  | origin |
| 5 | `ttl_user_cnt` | `bigint` |  | 注册用户数 |
| 6 | `amounts` | `bigint` |  | 总订单金额 |
| 7 | `talk_cnt` | `bigint` |  | 聊天轮数 |
| 8 | `talk_user_cnt` | `bigint` |  | 聊天用户数 |
| 9 | `talk_days_cnt` | `bigint` |  | 聊天天数 |
| 10 | `cnt_talk_character` | `bigint` |  | 聊天角色数 |
| 11 | `cnt_talk_ugc_character` | `bigint` |  | ugc聊天角色数 |
| 12 | `ugc_talk_user_cnt` | `bigint` |  | ugc聊天人数 |
| 13 | `cnt_talk_ogc_character` | `bigint` |  | ogc聊天角色数 |
| 14 | `ogc_talk_user_cnt` | `bigint` |  | ogc聊天人数 |
| 15 | `cnt_talk_ugc` | `bigint` |  | ugc聊天轮数 |
| 16 | `cnt_talk_ogc` | `bigint` |  | ogc聊天轮数 |
| 17 | `ogc_order_price` | `bigint` |  | ogc订单金额 |
| 18 | `ogc_order_user_cnt` | `bigint` |  | ogc订单用户数 |
| 19 | `ugc_order_price` | `bigint` |  | ugc订单金额 |
| 20 | `ugc_order_user_cnt` | `bigint` |  | ugc订单用户数 |
| 21 | `dt` | `string` |  |  |

---

## ads_pve_app_chat_base_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_pve_app_chat_base_data_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 661.6K |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `new_consum_flag` | `int` |  | 聊天新老用户标识,1新用户,0老用户,2汇总用户 |
| 2 | `chat_uv` | `bigint` |  | 聊天人数 |
| 3 | `chat_characters_drop_duplicate` | `bigint` |  | 按天去重聊天角色数 |
| 4 | `total_chat_rounds` | `bigint` |  | 聊天轮数 |
| 5 | `chat_characters` | `bigint` |  | 按用户统计聊天角色数 |
| 6 | `dt` | `string` |  |  |

---

## ads_pve_app_chat_retain_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_pve_app_chat_retain_data_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.9M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基础日期 |
| 2 | `period` | `bigint` |  | 留存周期 |
| 3 | `new_chat_flag` | `int` |  | 聊天新老用户标识,1新用户,0老用户,2汇总用户 |
| 4 | `chat_uv` | `bigint` |  | 基础日聊天人数 |
| 5 | `chat_characters` | `bigint` |  | 聊天角色数 |
| 6 | `total_chat_rounds` | `bigint` |  | 聊天轮数 |
| 7 | `chat_uv_1d` | `bigint` |  | 当日聊天留存人数 |
| 8 | `chat_uv_2d` | `bigint` |  | 次日聊天留存人数 |
| 9 | `chat_uv_3d` | `bigint` |  | 第3日聊天留存人数 |
| 10 | `chat_uv_7d` | `bigint` |  | 第7日聊天留存人数 |
| 11 | `chat_uv_15d` | `bigint` |  | 第15日聊天留存人数 |
| 12 | `chat_uv_30d` | `bigint` |  | 第30日聊天留存人数 |
| 13 | `chat_characters_drop_duplicate` | `bigint` |  | 去重后的聊天角色数 |
| 14 | `dt` | `string` |  |  |

---

## ads_theater_active_user_vc_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_theater_active_user_vc_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 29.3M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  |  |
| 2 | `dt` | `string` |  |  |

---

## ads_theater_main_data_vc_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_theater_main_data_vc_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 466.4K |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `period` | `string` |  |  |
| 2 | `dau` | `bigint` |  |  |
| 3 | `new_user_cnt` | `bigint` |  |  |
| 4 | `chat_user` | `bigint` |  | 互动人数 |
| 5 | `cnt_user_10` | `bigint` |  | 互动超过10轮人数 |
| 6 | `cnt_user_30` | `bigint` |  | 互动超过30轮人数 |
| 7 | `cnt_user_100` | `bigint` |  | 互动超过100轮人数 |
| 8 | `avg_cnt_huihe` | `decimal(23,15)` |  | 人均互动数 |
| 9 | `cnt_user_order` | `bigint` |  | 下单人数 |
| 10 | `amount` | `bigint` |  | 订单金额 |
| 11 | `avg_cnt_juchang` | `decimal(23,15)` |  | 人均剧场数 |
| 12 | `dt` | `string` |  |  |

---

## ads_theater_user_chat_retain_vc_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_theater_user_chat_retain_vc_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 348.3K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `date1` | `string` |  |  |
| 2 | `diff` | `bigint` |  |  |
| 3 | `active_uv` | `bigint` |  | 活跃人数 |
| 4 | `active_uv_1d` | `bigint` |  | 次留活跃人数 |
| 5 | `active_uv_3d` | `bigint` |  | 3留活跃人数 |
| 6 | `active_uv_7d` | `bigint` |  | 7留活跃人数 |
| 7 | `active_uv_15d` | `bigint` |  | 15留活跃人数 |
| 8 | `active_uv_30d` | `bigint` |  | 30留活跃人数 |
| 9 | `dt` | `string` |  |  |

---

## ads_theater_user_retain_vc_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_theater_user_retain_vc_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 347.9K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `date1` | `string` |  |  |
| 2 | `diff` | `bigint` |  |  |
| 3 | `active_uv` | `bigint` |  | 活跃人数 |
| 4 | `active_uv_1d` | `bigint` |  | 次留活跃人数 |
| 5 | `active_uv_3d` | `bigint` |  | 3留活跃人数 |
| 6 | `active_uv_7d` | `bigint` |  | 7留活跃人数 |
| 7 | `active_uv_15d` | `bigint` |  | 15留活跃人数 |
| 8 | `active_uv_30d` | `bigint` |  | 30留活跃人数 |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_3mincrush_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_3mincrush_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xuexingyu |
| **表类型** | internal |
| **表大小** | 511.2K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `clicktype` | `string` |  |  |
| 3 | `clickitem` | `string` |  |  |
| 4 | `event_type` | `string` |  |  |
| 5 | `pv_cnt` | `bigint` |  |  |
| 6 | `uv_cnt` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_vc_3mincrush_server_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_3mincrush_server_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xuexingyu |
| **表类型** | internal |
| **表大小** | 765.0K |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `crush_user_user_cnt` | `bigint` |  |  |
| 2 | `crush_user_record_cnt` | `bigint` |  |  |
| 3 | `crush_role_user_cnt` | `bigint` |  |  |
| 4 | `crush_role_record_cnt` | `bigint` |  |  |
| 5 | `order_cnt` | `bigint` |  |  |
| 6 | `price_cnt` | `double` |  |  |
| 7 | `free_amount_cnt` | `bigint` |  |  |
| 8 | `pay_amount_cnt` | `bigint` |  |  |
| 9 | `invite_amount_cnt` | `bigint` |  |  |
| 10 | `invite_record_cnt` | `bigint` |  |  |
| 11 | `crush_times_cost` | `bigint` |  |  |
| 12 | `crush_energy_cost` | `bigint` |  |  |
| 13 | `dt` | `date` |  |  |

---

## ads_vc_active_chat_consum_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_active_chat_consum_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 513.6K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `new_flag` | `bigint` |  | 用户新老标识（例如：new、old） |
| 2 | `active_uv` | `bigint` |  | 活跃用户数（Unique Visitors） |
| 3 | `chat_uv` | `bigint` |  | 聊天用户数（至少发送过 1 条消息的用户） |
| 4 | `chat_uv_10up` | `bigint` |  | 发送 10 条及以上消息的用户数 |
| 5 | `chat_uv_30up` | `bigint` |  | 发送 30 条及以上消息的用户数 |
| 6 | `chat_uv_100up` | `bigint` |  | 发送 100 条及以上消息的用户数 |
| 7 | `total_chats` | `bigint` |  | 总聊天消息数 |
| 8 | `consum_uv` | `bigint` |  | 消费用户数（至少有消费行为的用户数） |
| 9 | `total_amount` | `double` |  | 总消费金额 |
| 10 | `dt` | `string` |  | 数据日期(格式:yyyy-MM-dd) |

---

## ads_vc_active_user_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_active_user_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.6M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基础日期 |
| 2 | `period` | `bigint` |  | 留存周期 |
| 3 | `new_flag` | `bigint` |  | 留存周期 |
| 4 | `active_uv` | `double` |  | 活跃用户数 |
| 5 | `active_uv_1d` | `double` |  | 基础日活跃用户数 |
| 6 | `active_uv_2d` | `double` |  | 次日活跃用户数留存数 |
| 7 | `active_uv_3d` | `double` |  | 第3日活跃用户数留存数 |
| 8 | `active_uv_7d` | `double` |  | 第7日活跃用户数留存数 |
| 9 | `active_uv_15d` | `double` |  | 第15日活跃用户数留存数 |
| 10 | `active_uv_30d` | `double` |  | 第30日活跃用户数留存数 |
| 11 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 12 | `dt` | `string` |  |  |

---

## ads_vc_ad_cvr_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_ad_cvr_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 9.5M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `advertiserid` | `string` |  | 广告账号id |
| 2 | `campaignid` | `string` |  | 活动id |
| 3 | `new_date` | `string` |  | 新增日期 |
| 4 | `new_device_count` | `bigint` |  | 拉新设备数 |
| 5 | `pve_access_device_count` | `bigint` |  | pve访问设备数 |
| 6 | `trade_user_count` | `bigint` |  | 交易用户数 |
| 7 | `trade_amount` | `double` |  | 交易金额 |
| 8 | `device_type` | `string` |  | 设备类型: new return_30 active |
| 9 | `pve_access_device_count_v2` | `bigint` |  | 虚拟人访问量： 含匿名用户 |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_ad_cvr_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_ad_cvr_new_di` |
| **描述** | 广告转化数据日报表 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 50.9M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `advertiserid` | `string` |  | 广告主ID |
| 2 | `campaignid` | `string` |  | 推广计划ID |
| 3 | `new_date` | `string` |  | 新增日期 |
| 4 | `new_device_count` | `bigint` |  | 新增设备数 |
| 5 | `pve_access_device_count` | `bigint` |  | PVE访问设备数 |
| 6 | `trade_user_count` | `bigint` |  | 交易用户数 |
| 7 | `trade_amount` | `double` |  | 交易金额 |
| 8 | `device_type` | `string` |  | 设备类型 |
| 9 | `pve_access_device_count_v2` | `bigint` |  | PVE访问设备数V2 |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_ai_model_errormsg_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_ai_model_errormsg_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 8.2G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `requesttime` | `string` |  |  |
| 2 | `model` | `int` |  |  |
| 3 | `modelname` | `string` |  |  |
| 4 | `aiscenecode` | `string` |  |  |
| 5 | `aiscenecodedesc` | `string` |  |  |
| 6 | `code` | `int` |  |  |
| 7 | `errormsg` | `string` |  |  |
| 8 | `codedesc` | `string` |  |  |
| 9 | `callcount` | `bigint` |  |  |
| 10 | `inputtokencount` | `bigint` |  |  |
| 11 | `outputtokencount` | `bigint` |  |  |
| 12 | `totaltokencount` | `bigint` |  |  |
| 13 | `dt` | `string` |  |  |

---

## ads_vc_ai_scene_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_ai_scene_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 130.2M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `requesttime` | `string` |  |  |
| 2 | `model` | `int` |  | from deserializer |
| 3 | `modelname` | `string` |  | from deserializer |
| 4 | `aiscenecode` | `string` |  | from deserializer |
| 5 | `aiscenecodedesc` | `string` |  | from deserializer |
| 6 | `code` | `int` |  | from deserializer |
| 7 | `errormsg` | `string` |  | from deserializer |
| 8 | `codedesc` | `string` |  |  |
| 9 | `callcount` | `bigint` |  |  |
| 10 | `inputtokencount` | `bigint` |  |  |
| 11 | `outputtokencount` | `bigint` |  |  |
| 12 | `totaltokencount` | `bigint` |  |  |
| 13 | `inputcost` | `double` |  |  |
| 14 | `outputcost` | `double` |  |  |
| 15 | `cachecost` | `double` |  |  |
| 16 | `cachetokencount` | `double` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ads_vc_base_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_base_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 2.8M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `period` | `string` |  |  |
| 2 | `register_uv` | `bigint` |  |  |
| 3 | `active_device` | `bigint` |  |  |
| 4 | `active_user` | `bigint` |  |  |
| 5 | `new_device` | `bigint` |  |  |
| 6 | `chat_uv` | `bigint` |  |  |
| 7 | `chat_uv_30` | `bigint` |  |  |
| 8 | `chat_uv_100` | `bigint` |  |  |
| 9 | `total_chat_rounds` | `bigint` |  |  |
| 10 | `chat_characters` | `bigint` |  |  |
| 11 | `chat_character_ids` | `bigint` |  |  |
| 12 | `consum_uv` | `bigint` |  |  |
| 13 | `total_amount` | `double` |  |  |
| 14 | `is_anonymity` | `string` |  | 总消耗能量 |
| 15 | `total_talk_cnt` | `bigint` |  | 总聊天轮数（包含摸摸头） |
| 16 | `total_mmt_cnt` | `bigint` |  | 摸摸头轮数 |
| 17 | `total_energy_consume` | `bigint` |  | 总消耗能量能量 |
| 18 | `total_free_energy` | `bigint` |  | 总消耗免费能量 |
| 19 | `total_buy_energy` | `bigint` |  | 总消耗购买能量 |
| 20 | `total_reward_energy` | `bigint` |  | 总消耗奖励能量 |
| 21 | `dt` | `string` |  |  |

---

## ads_vc_character_dailyfree_token_cnt_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_character_dailyfree_token_cnt_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 548.0K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `characterfreetype` | `string` |  |  |
| 2 | `is_vip` | `int` |  |  |
| 3 | `has_unlimited_chat_card` | `int` |  |  |
| 4 | `has_new_critical_hit` | `int` |  |  |
| 5 | `has_free_chat_card_pack` | `int` |  |  |
| 6 | `total_message_count` | `bigint` |  |  |
| 7 | `user_count` | `bigint` |  |  |
| 8 | `sum_total_tokens` | `double` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_chat_project_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_chat_project_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 73.8M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `n` | `int` |  |  |
| 2 | `active_days` | `bigint` |  |  |
| 3 | `chat_level` | `string` |  |  |
| 4 | `new_register_flag` | `int` |  |  |
| 5 | `origin_type` | `string` |  | 来源类型 |
| 6 | `last_consecutive_days` | `int` |  |  |
| 7 | `device_os` | `string` |  | 最后记录的设备os |
| 8 | `is_pve` | `string` |  |  |
| 9 | `uv` | `bigint` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_consum_amount_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_consum_amount_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.5M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基础日期 |
| 2 | `period` | `bigint` |  | 留存周期 |
| 3 | `new_consum_flag` | `int` |  | 付费新老用户标识,1新用户,0老用户，2汇总用户 |
| 4 | `total_amount` | `double` |  | 付费金额 |
| 5 | `total_amount_1d` | `double` |  | 基础日付费金额 |
| 6 | `total_amount_2d` | `double` |  | 次日付费金额 |
| 7 | `total_amount_3d` | `double` |  | 第3日付费留存金额 |
| 8 | `total_amount_7d` | `double` |  | 第7日付费留存金额 |
| 9 | `total_amount_15d` | `double` |  | 第15日付费留存金额 |
| 10 | `total_amount_30d` | `double` |  | 第30日付费留存金额 |
| 11 | `dt` | `string` |  |  |

---

## ads_vc_consum_uv_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_consum_uv_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.2M |
| **是否分区表** | 是 |

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基础日期 |
| 2 | `period` | `bigint` |  | 留存周期 |
| 3 | `new_consum_flag` | `int` |  | 付费新老用户标识,1新用户,0老用户,2汇总用户 |
| 4 | `consum_uv` | `bigint` |  | 基础日付费人数 |
| 5 | `consum_uv_1d` | `bigint` |  | 当日付费留存人数 |
| 6 | `consum_uv_2d` | `bigint` |  | 次日付费留存人数 |
| 7 | `consum_uv_3d` | `bigint` |  | 3日内付费留存人数 |
| 8 | `consum_uv_7d` | `bigint` |  | 7日内付费留存人数 |
| 9 | `consum_uv_15d` | `bigint` |  | 15日内付费留存人数 |
| 10 | `consum_uv_30d` | `bigint` |  | 30日内付费留存人数 |
| 11 | `consum_chat_uv_1d` | `bigint` |  | 当日付费聊天留存人数 |
| 12 | `consum_chat_uv_2d` | `bigint` |  | 次日付费聊天留存人数 |
| 13 | `consum_chat_uv_3d` | `bigint` |  | 3日内付费聊天留存人数 |
| 14 | `consum_chat_uv_7d` | `bigint` |  | 7日内付费聊天留存人数 |
| 15 | `consum_chat_uv_15d` | `bigint` |  | 15日内付费聊天留存人数 |
| 16 | `consum_chat_uv_30d` | `bigint` |  | 30日内付费聊天留存人数 |
| 17 | `consum_active_uv_1d` | `bigint` |  | 当日付费活跃留存人数 |
| 18 | `consum_active_uv_2d` | `bigint` |  | 次日付费活跃留存人数 |
| 19 | `consum_active_uv_3d` | `bigint` |  | 3日内付费活跃留存人数 |
| 20 | `consum_active_uv_7d` | `bigint` |  | 7日内付费活跃留存人数 |
| 21 | `consum_active_uv_15d` | `bigint` |  | 15日内付费活跃留存人数 |
| 22 | `consum_active_uv_30d` | `bigint` |  | 30日内付费活跃留存人数 |
| 23 | `dt` | `string` |  |  |

---

## ads_vc_core_table_active_energy_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_core_table_active_energy_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 843.3K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | tag |
| 2 | `tag_active_days_30d` | `string` |  | 近30天活跃天数分层 |
| 3 | `cnt_user_active_days_30d` | `bigint` |  | 近30天活跃人数 |
| 4 | `tag_active_days_7d` | `string` |  | 近7天活跃天数分层 |
| 5 | `cnt_user_active_days_7d` | `bigint` |  | 近7天活跃人数 |
| 6 | `ttl_energy_consume` | `bigint` |  | 消耗能量 |
| 7 | `ttl_energy_get` | `bigint` |  | 获得能量 |
| 8 | `ttl_energy_rest` | `bigint` |  | 剩余能量 |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_custom_rank_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_custom_rank_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 190.5M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `rank_id` | `bigint` |  | 榜单Id |
| 2 | `target_id` | `string` |  |  |
| 3 | `targettype` | `int` |  |  |
| 4 | `rank_value` | `bigint` |  |  |
| 5 | `rank_order` | `bigint` |  |  |
| 6 | `dt` | `string` |  |  |
| 7 | `hour` | `int` |  |  |

---

## ads_vc_daily_user_simulator_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_daily_user_simulator_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 608.0M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `simulator_id` | `string` |  | 模拟器ID |
| 3 | `source` | `int` |  | 1:vc, 2:lofter, 3:theater |
| 4 | `simulator_chats_1d` | `bigint` |  | 近1d聊天轮次 |
| 5 | `simulator_chats_3d` | `bigint` |  | 近3d聊天轮次 |
| 6 | `simulator_chats_5d` | `bigint` |  | 近5d聊天轮次 |
| 7 | `simulator_chats_7d` | `bigint` |  | 近7d聊天轮次 |
| 8 | `simulator_chats_15d` | `bigint` |  | 近15d聊天轮次 |
| 9 | `simulator_chats_30d` | `bigint` |  | 近30d聊天轮次 |
| 10 | `type` | `int` |  | 模拟器类型：1-单角色聊天数值报告玩法 |
| 11 | `dt` | `string` |  |  |

---

## ads_vc_data_user_plotsimulator_model_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_data_user_plotsimulator_model_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 27.2M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `user_code` | `string` |  | Lofter用户ID |
| 3 | `simulator_id` | `string` |  | 模拟器ID |
| 4 | `dt` | `string` |  |  |

---

## ads_vc_device_retain_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_device_retain_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 2.8M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基础日期 |
| 2 | `deviceos` | `string` |  | 设备系统 |
| 3 | `period` | `bigint` |  | 留存周期 |
| 4 | `active_device` | `double` |  | 活跃设备 |
| 5 | `active_device_1d` | `double` |  | 基础日活跃设备 |
| 6 | `active_device_2d` | `double` |  | 次日活跃设备留存数 |
| 7 | `active_device_7d` | `double` |  | 第7日活跃设备留存数 |
| 8 | `active_device_15d` | `double` |  | 第15日活跃设备留存数 |
| 9 | `active_device_30d` | `double` |  | 第30日活跃设备留存数 |
| 10 | `new_device` | `double` |  | 新增设备 |
| 11 | `new_device_1d` | `double` |  | 基础日新增设备 |
| 12 | `new_device_2d` | `double` |  | 次日新增设备留存数 |
| 13 | `new_device_7d` | `double` |  | 第7日新增设备留存数 |
| 14 | `new_device_15d` | `double` |  | 第15日新增设备留存数 |
| 15 | `new_device_30d` | `double` |  | 第30日新增设备留存数 |
| 16 | `return_device` | `int` |  |  |
| 17 | `return_device_1d` | `int` |  |  |
| 18 | `return_device_2d` | `int` |  |  |
| 19 | `return_device_7d` | `int` |  |  |
| 20 | `return_device_15d` | `int` |  |  |
| 21 | `return_device_30d` | `int` |  |  |
| 22 | `dt` | `string` |  |  |

---

## ads_vc_device_retain_new_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_device_retain_new_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 9.9M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `int` |  |  |
| 3 | `device_type` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `appchannel` | `string` |  | 当天最后渠道 |
| 6 | `appversion` | `string` |  | 当天最后版本 |
| 7 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 8 | `active_device` | `bigint` |  |  |
| 9 | `active_device_1d` | `bigint` |  |  |
| 10 | `active_device_2d` | `bigint` |  |  |
| 11 | `active_device_7d` | `bigint` |  |  |
| 12 | `active_device_15d` | `bigint` |  |  |
| 13 | `active_device_30d` | `bigint` |  |  |
| 14 | `dt` | `string` |  |  |

---

## ads_vc_energy_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_energy_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 2.0M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `get_free_energy` | `bigint` |  |  |
| 2 | `get_buy_energy` | `bigint` |  |  |
| 3 | `get_reward_energy` | `bigint` |  |  |
| 4 | `total_amount` | `double` |  |  |
| 5 | `chat_uv` | `bigint` |  |  |
| 6 | `total_chats` | `bigint` |  |  |
| 7 | `total_cost_energy` | `bigint` |  |  |
| 8 | `cost_free_energy` | `bigint` |  |  |
| 9 | `cost_buy_energy` | `bigint` |  |  |
| 10 | `cost_reward_energy` | `bigint` |  |  |
| 11 | `total_uv` | `bigint` |  |  |
| 12 | `rest_free_energy` | `bigint` |  |  |
| 13 | `rest_buy_energy` | `bigint` |  |  |
| 14 | `rest_reward_energy` | `bigint` |  |  |
| 15 | `new_register_flg` | `int` |  |  |
| 16 | `new_order_flg` | `int` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ads_vc_energy_group_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_energy_group_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 369.0K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `label` | `string` |  | 分类1 |
| 2 | `scene_desc` | `string` |  | 分类2 |
| 3 | `energy` | `bigint` |  | 能量值 |
| 4 | `ttl_cnt_user` | `bigint` |  | 用户数 |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_energy_kpi_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_energy_kpi_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 1.5M |
| **是否分区表** | 是 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `expired_energy` | `bigint` |  | 过期能量 |
| 2 | `ttl_user_cnt` | `bigint` |  | 总用户数 |
| 3 | `rest_energy` | `bigint` |  | 剩余能量总 |
| 4 | `free_rest_energy` | `bigint` |  | 免费剩余能量 |
| 5 | `buy_rest_energy` | `bigint` |  | 购买剩余能量 |
| 6 | `reward_rest_energy` | `bigint` |  | 奖励剩余能量 |
| 7 | `get_free_energy` | `bigint` |  | 获得免费能量 |
| 8 | `get_buy_energy` | `bigint` |  | 获得购买能量 |
| 9 | `get_reward_energy` | `bigint` |  | 获得奖励能量 |
| 10 | `get_total_energy` | `bigint` |  | 获得能量总 |
| 11 | `real_ttl_consume_expire` | `bigint` |  | 广义真实消耗能量总排除lofter 消耗加过期 |
| 12 | `total_consume_contain_lofter` | `bigint` |  | 消耗能量总包含lofter |
| 13 | `total_consume_except_lofter` | `bigint` |  | 消耗能量总排除lofter |
| 14 | `real_buy_consume_expire` | `bigint` |  | 广义真实消耗购买能量 消耗加过期 |
| 15 | `real_reward_consume_expire` | `bigint` |  | 广义真实消耗奖励能量 消耗加过期 |
| 16 | `real_free_consume_expire` | `bigint` |  | 广义真实消耗免费能量 消耗加过期 |
| 17 | `buy_nums_consume` | `bigint` |  | 购买能量消耗 |
| 18 | `reward_nums_consume` | `bigint` |  | 奖励能量消耗 |
| 19 | `free_nums_consume` | `bigint` |  | 免费能量消耗 |
| 20 | `lofter_nums_consume` | `bigint` |  | lofter能量消耗 |
| 21 | `free_energy_consume_talk` | `bigint` |  | 免费能量消耗-聊天 |
| 22 | `reward_energy_consume_talk` | `bigint` |  | 奖励能量消耗-聊天 |
| 23 | `buy_energy_consume_talk` | `bigint` |  | 购买能量消耗-聊天 |
| 24 | `ttl_energy_consume_talk` | `bigint` |  | 全部能量消耗-聊天含互动 |
| 25 | `dt` | `string` |  |  |

---

## ads_vc_event_info_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_event_info_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 487.2K |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `type` | `string` |  | 事件类型，邀约事件、思念事件、破冰事件...... |
| 2 | `event_cnt` | `bigint` |  |  |
| 3 | `dt` | `string` |  |  |

---

## ads_vc_growth_ad_period_cvr_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_growth_ad_period_cvr_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 39.0M |
| **是否分区表** | 是 |

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_type` | `string` |  | 设备类型: new return_30 |
| 2 | `deviceos` | `string` |  |  |
| 3 | `advertiserid` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `campaignid` | `string` |  |  |
| 6 | `aid` | `string` |  |  |
| 7 | `cid` | `string` |  |  |
| 8 | `media` | `string` |  |  |
| 9 | `proxy` | `string` |  |  |
| 10 | `custom_ouid` | `string` |  |  |
| 11 | `photoid` | `string` |  |  |
| 12 | `newuv` | `bigint` |  |  |
| 13 | `loguv` | `bigint` |  |  |
| 14 | `reguv` | `bigint` |  |  |
| 15 | `per_activedays` | `double` |  |  |
| 16 | `active_2days_uv` | `bigint` |  |  |
| 17 | `n_day_uv` | `bigint` |  |  |
| 18 | `per_duration_minutes` | `double` |  |  |
| 19 | `interactionuv` | `bigint` |  |  |
| 20 | `hotuv` | `bigint` |  |  |
| 21 | `hotpv` | `bigint` |  |  |
| 22 | `commenduv` | `bigint` |  |  |
| 23 | `commendpv` | `bigint` |  |  |
| 24 | `postuv` | `bigint` |  |  |
| 25 | `postpv` | `bigint` |  |  |
| 26 | `duration_uv` | `bigint` |  |  |
| 27 | `excellent_uv` | `bigint` |  |  |
| 28 | `impounding_uv` | `bigint` |  |  |
| 29 | `whiteboard_uv` | `bigint` |  | 白板用户数 |
| 30 | `photo_url` | `string` |  |  |
| 31 | `photo_caption` | `string` |  |  |
| 32 | `star_user_id` | `bigint` |  |  |
| 33 | `star_name` | `string` |  |  |
| 34 | `invest_amount` | `double` |  |  |
| 35 | `period` | `int` |  |  |
| 36 | `dt` | `string` |  |  |

---

## ads_vc_growth_arpu_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_growth_arpu_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 82.6M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 4 | `origin_channel` | `string` |  |  |
| 5 | `proxy` | `string` |  |  |
| 6 | `period` | `int` |  |  |
| 7 | `device_count` | `bigint` |  |  |
| 8 | `trade_device_count` | `bigint` |  |  |
| 9 | `trade_money` | `double` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_growth_ltv_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_growth_ltv_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 4.8M |
| **是否分区表** | 是 |

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 4 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 5 | `device_count` | `bigint` |  |  |
| 6 | `user_count` | `bigint` |  |  |
| 7 | `user_count_7d` | `bigint` |  |  |
| 8 | `user_count_15d` | `bigint` |  |  |
| 9 | `user_count_30d` | `bigint` |  |  |
| 10 | `user_count_60d` | `bigint` |  |  |
| 11 | `user_count_90d` | `bigint` |  |  |
| 12 | `user_count_120d` | `bigint` |  |  |
| 13 | `user_count_180d` | `bigint` |  |  |
| 14 | `trade_device_count` | `bigint` |  |  |
| 15 | `trade_device_count_7d` | `bigint` |  |  |
| 16 | `trade_device_count_15d` | `bigint` |  |  |
| 17 | `trade_device_count_30d` | `bigint` |  |  |
| 18 | `trade_device_count_60d` | `bigint` |  |  |
| 19 | `trade_device_count_90d` | `bigint` |  |  |
| 20 | `trade_device_count_120d` | `bigint` |  |  |
| 21 | `trade_device_count_180d` | `bigint` |  |  |
| 22 | `trade_money` | `double` |  |  |
| 23 | `trade_money_7d` | `double` |  |  |
| 24 | `trade_money_15d` | `double` |  |  |
| 25 | `trade_money_30d` | `double` |  |  |
| 26 | `trade_money_60d` | `double` |  |  |
| 27 | `trade_money_90d` | `double` |  |  |
| 28 | `trade_money_120d` | `double` |  |  |
| 29 | `trade_money_180d` | `double` |  |  |
| 30 | `proxy` | `string` |  | 渠道代理 |
| 31 | `dt` | `string` |  |  |

---

## ads_vc_new_device_login_active_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_new_device_login_active_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 4.4M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `deviceos` | `string` |  | 操作系统 |
| 3 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 4 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 5 | `appchannel` | `string` |  | 当天最后渠道 |
| 6 | `appversion` | `string` |  | 当天最后版本 |
| 7 | `period` | `int` |  |  |
| 8 | `active_device` | `bigint` |  |  |
| 9 | `active_uv` | `bigint` |  |  |
| 10 | `active_device_1d` | `bigint` |  |  |
| 11 | `active_device_2d` | `bigint` |  |  |
| 12 | `active_device_7d` | `bigint` |  |  |
| 13 | `active_device_15d` | `bigint` |  |  |
| 14 | `active_device_30d` | `bigint` |  |  |
| 15 | `dt` | `string` |  |  |

---

## ads_vc_new_register_retain_rate_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_new_register_retain_rate_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 4.3M |
| **是否分区表** | 是 |

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `register_date` | `string` |  | 注册日期 |
| 2 | `origin_type` | `string` |  | 来源类型1 |
| 3 | `device_os` | `string` |  | os |
| 4 | `origin` | `string` |  | 来源类型2 |
| 5 | `period` | `bigint` |  | period |
| 6 | `register_user_cnt` | `bigint` |  | 注册用户数 |
| 7 | `active_uv_2d` | `bigint` |  | 次日留存用户数 |
| 8 | `active_uv_3d` | `bigint` |  | 3日留存用户数 |
| 9 | `active_uv_4d` | `bigint` |  | 4日留存用户数 |
| 10 | `active_uv_5d` | `bigint` |  | 5日留存用户数 |
| 11 | `active_uv_6d` | `bigint` |  | 6日留存用户数 |
| 12 | `active_uv_7d` | `bigint` |  | 7日留存用户数 |
| 13 | `active_uv_8d` | `bigint` |  | 8日留存用户数 |
| 14 | `active_uv_9d` | `bigint` |  | 9日留存用户数 |
| 15 | `active_uv_10d` | `bigint` |  | 10日留存用户数 |
| 16 | `active_uv_11d` | `bigint` |  | 11日留存用户数 |
| 17 | `active_uv_12d` | `bigint` |  | 12日留存用户数 |
| 18 | `active_uv_13d` | `bigint` |  | 13日留存用户数 |
| 19 | `active_uv_14d` | `bigint` |  | 14日留存用户数 |
| 20 | `active_uv_15d` | `bigint` |  | 15日留存用户数 |
| 21 | `active_uv_16d` | `bigint` |  | 16日留存用户数 |
| 22 | `active_uv_17d` | `bigint` |  | 17日留存用户数 |
| 23 | `active_uv_18d` | `bigint` |  | 18日留存用户数 |
| 24 | `active_uv_19d` | `bigint` |  | 19日留存用户数 |
| 25 | `active_uv_20d` | `bigint` |  | 20日留存用户数 |
| 26 | `active_uv_21d` | `bigint` |  | 21日留存用户数 |
| 27 | `active_uv_22d` | `bigint` |  | 22日留存用户数 |
| 28 | `active_uv_23d` | `bigint` |  | 23日留存用户数 |
| 29 | `active_uv_24d` | `bigint` |  | 24日留存用户数 |
| 30 | `active_uv_25d` | `bigint` |  | 25日留存用户数 |
| 31 | `active_uv_26d` | `bigint` |  | 26日留存用户数 |
| 32 | `active_uv_27d` | `bigint` |  | 27日留存用户数 |
| 33 | `active_uv_28d` | `bigint` |  | 28日留存用户数 |
| 34 | `active_uv_29d` | `bigint` |  | 29日留存用户数 |
| 35 | `active_uv_30d` | `bigint` |  | 30日留存用户数 |
| 36 | `dt` | `string` |  |  |

---

## ads_vc_new_register_role_chat_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_new_register_role_chat_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 15.1M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  | user_id |
| 2 | `origin_type` | `string` |  | 来源类型1 |
| 3 | `device_os` | `string` |  | os |
| 4 | `origin` | `string` |  | 来源类型2 |
| 5 | `character_id` | `string` |  | 角色id |
| 6 | `character_name` | `string` |  | 角色名称 |
| 7 | `talk_cnt` | `string` |  | 聊天轮数 |
| 8 | `character_type` | `string` |  | ogc |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_new_register_role_recommend_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_new_register_role_recommend_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 108.9M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `origin_type` | `string` |  | 来源类型1 |
| 2 | `device_os` | `string` |  | os |
| 3 | `origin` | `string` |  | 来源类型2 |
| 4 | `expose_role_id` | `string` |  | 曝光角色id |
| 5 | `character_name` | `string` |  | 曝光角色名名称 |
| 6 | `expose_pos` | `string` |  | 曝光角色位置 |
| 7 | `expose_user_id` | `string` |  | 曝光用户id |
| 8 | `click_user_id` | `string` |  | 点击用户id |
| 9 | `talk_user_id` | `string` |  | 聊天用户id |
| 10 | `talk_cnt` | `string` |  | 聊天轮数 |
| 11 | `dt` | `string` |  |  |

---

## ads_vc_new_register_user_chat_kpi_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_new_register_user_chat_kpi_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.qibaoguang |
| **表类型** | external |
| **表大小** | 1.7M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `register_date` | `string` |  | 注册日期 |
| 2 | `origin_type` | `string` |  | 来源类型1 |
| 3 | `origin` | `string` |  | 来源类型2 |
| 4 | `device_os` | `string` |  | os |
| 5 | `register_user_cnt` | `bigint` |  | 注册用户数 |
| 6 | `first_role_chat_cnt` | `bigint` |  | 首次聊天轮数 |
| 7 | `first_role_chat_user_cnt` | `bigint` |  | 首次聊天人数 |
| 8 | `chat_user_cnt` | `bigint` |  | 聊天人数 |
| 9 | `talk_cnt` | `bigint` |  | 聊天轮数 |
| 10 | `talk_role_cnt` | `bigint` |  | 聊天角色数 |
| 11 | `findpage_role_expose_pv` | `bigint` |  | 发现页角色曝光pv |
| 12 | `findpage_role_expose_uv` | `bigint` |  | 发现页角色曝光uv |
| 13 | `findpage_role_click_pv` | `bigint` |  | 发现页角色点击pv |
| 14 | `findpage_role_click_uv` | `bigint` |  | 发现页角色点击uv |
| 15 | `automatic_inspiration_click_pv` | `bigint` |  | 自动灵感点击pv |
| 16 | `automatic_inspiration_expose_pv` | `bigint` |  | 自动灵感曝光pv |
| 17 | `automatic_inspiration_click_uv` | `bigint` |  | 自动灵感点击uv |
| 18 | `manual_inspiration_click_pv` | `bigint` |  | 手动灵感点击pv |
| 19 | `manual_inspiration_expose_pv` | `bigint` |  | 手动灵感曝光pv |
| 20 | `manual_inspiration_click_uv` | `bigint` |  | 手动灵感点击uv |
| 21 | `dt` | `string` |  |  |

---

## ads_vc_ogc_ugc_summary_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_ogc_ugc_summary_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 1.6M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 2 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 3 | `public_scope` | `int` |  | 公开状态 0 初始值 1:公开 2:私密 |
| 4 | `chat_character_cnt` | `bigint` |  |  |
| 5 | `message_cnt` | `bigint` |  |  |
| 6 | `mmt_cnt` | `bigint` |  |  |
| 7 | `heart_cnt` | `bigint` |  |  |
| 8 | `energy_consume` | `bigint` |  |  |
| 9 | `free_energy` | `bigint` |  |  |
| 10 | `buy_energy` | `bigint` |  |  |
| 11 | `reward_energy` | `bigint` |  |  |
| 12 | `free_message_cnt` | `bigint` |  |  |
| 13 | `message_consume` | `bigint` |  |  |
| 14 | `chat_uv` | `bigint` |  |  |
| 15 | `chat_uv_10up` | `bigint` |  |  |
| 16 | `chat_uv_30up` | `bigint` |  |  |
| 17 | `chat_uv_100up` | `bigint` |  |  |
| 18 | `chat_uv_150up` | `bigint` |  |  |
| 19 | `pay_uv` | `bigint` |  |  |
| 20 | `pay_character_cnt` | `bigint` |  |  |
| 21 | `total_amount` | `double` |  |  |
| 22 | `dt` | `string` |  |  |

---

## ads_vc_report_active_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_report_active_data_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 390.0K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `active_device` | `bigint` |  | 活跃设备数 |
| 2 | `active_user` | `bigint` |  | 活跃用户数 |
| 3 | `new_device` | `bigint` |  | 新增设备数 |
| 4 | `dt` | `string` |  |  |

---

## ads_vc_report_app_cumsum_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_report_app_cumsum_data_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 918.0K |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `period` | `string` |  | 统计周期 |
| 2 | `register_uv` | `bigint` |  | 注册人数 |
| 3 | `chat_uv` | `bigint` |  | 聊天人数 |
| 4 | `total_chat_rounds` | `bigint` |  | 聊天轮数 |
| 5 | `chat_characters` | `bigint` |  | 聊天角色数 |
| 6 | `consum_uv` | `bigint` |  | 付费人数 |
| 7 | `total_amount` | `double` |  | 付费金额 |
| 8 | `dt` | `string` |  |  |

---

## ads_vc_report_app_device_appversion_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_report_app_device_appversion_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 730.6K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appversion` | `string` |  | 版本号 |
| 2 | `device_uv` | `bigint` |  | 设备数 |
| 3 | `user_uv` | `bigint` |  | 用户数 |
| 4 | `dt` | `string` |  |  |

---

## ads_vc_report_app_device_deviceosversion_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_report_app_device_deviceosversion_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.5M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceosversion` | `string` |  | 系统版本号 |
| 2 | `deviceos` | `string` |  | 系统类型 |
| 3 | `device_uv` | `bigint` |  | 设备数 |
| 4 | `user_uv` | `bigint` |  | 用户数 |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_role_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_role_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 329.6M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `period` | `string` |  | 累计指标类型 |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名称 |
| 4 | `expose_uv` | `bigint` |  | 角色访问人数(取聊天角色页面曝光的userid) |
| 5 | `chat_uv` | `bigint` |  | 聊天人数 |
| 6 | `total_chats` | `bigint` |  | 聊天轮数 |
| 7 | `energy_consume` | `bigint` |  | 角色能量消耗 |
| 8 | `total_chats_10up` | `bigint` |  | 10轮以上聊天人数 |
| 9 | `total_chats_30up` | `bigint` |  | 30轮以上聊天人数 |
| 10 | `consum_uv` | `bigint` |  | 付费人数 |
| 11 | `total_amount` | `double` |  | 付费金额 |
| 12 | `type` | `bigint` |  | 0官方角色,1奇遇角色,2cp梦境,3光速匹配,高阶定义 |
| 13 | `status` | `bigint` |  | 角色状态,0:未生效, 1: 生效, -1: 删除 |
| 14 | `audience_type` | `bigint` |  | 受众类型:1所有人(默认),2成年人,3未成年人,4审核人员 |
| 15 | `total_chats_100up` | `bigint` |  | 100轮以上聊天人数 |
| 16 | `dt` | `string` |  |  |

---

## ads_vc_send_message_user_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_send_message_user_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 992.0K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `send_dt` | `string` |  |  |
| 2 | `source` | `string` |  |  |
| 3 | `send_users` | `bigint` |  |  |
| 4 | `active_uv_24h` | `bigint` |  |  |
| 5 | `chat_uv_7d` | `bigint` |  |  |
| 6 | `total_chats_7d` | `bigint` |  |  |
| 7 | `paid_uv_7d` | `bigint` |  |  |
| 8 | `total_amount_7d` | `double` |  |  |
| 9 | `scene` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_simulator_base_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_simulator_base_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 389.8K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `source` | `bigint` |  |  |
| 2 | `simulator_chat_uv` | `bigint` |  |  |
| 3 | `simulator_cnt` | `bigint` |  |  |
| 4 | `simulator_message_cnt` | `bigint` |  |  |
| 5 | `simulator_total_consume` | `bigint` |  |  |
| 6 | `simulator_buy_nums` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_vc_simulator_detail_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_simulator_detail_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.zhouyiqing01 |
| **表类型** | internal |
| **表大小** | 1.2M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `source` | `bigint` |  |  |
| 2 | `name` | `string` |  | 名称 |
| 3 | `simulator_chat_uv` | `bigint` |  |  |
| 4 | `simulator_message_cnt` | `bigint` |  |  |
| 5 | `simulator_total_consume` | `bigint` |  |  |
| 6 | `simulator_buy_nums` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_vc_user_active_data_v2_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_active_data_v2_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.8M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `new_register_flag` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `period` | `int` |  |  |
| 5 | `active_uv` | `bigint` |  |  |
| 6 | `active_uv_1d` | `bigint` |  |  |
| 7 | `active_uv_2d` | `bigint` |  |  |
| 8 | `active_uv_3d` | `bigint` |  |  |
| 9 | `active_uv_7d` | `bigint` |  |  |
| 10 | `active_uv_15d` | `bigint` |  |  |
| 11 | `active_uv_30d` | `bigint` |  |  |
| 12 | `dt` | `string` |  |  |

---

## ads_vc_user_chat_base_data_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_chat_base_data_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.3M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `new_chat_flag` | `int` |  |  |
| 2 | `chat_uv` | `bigint` |  |  |
| 3 | `chat_characters_drop_duplicate` | `bigint` |  |  |
| 4 | `total_chat_rounds` | `bigint` |  |  |
| 5 | `chat_characters` | `bigint` |  |  |
| 6 | `total_consume_nums` | `bigint` |  | 总消耗能量 |
| 7 | `total_free_energy` | `bigint` |  | 总消耗免费能量 |
| 8 | `total_buy_energy` | `bigint` |  | 总消耗购买能量 |
| 9 | `total_reward_energy` | `bigint` |  | 总消耗奖励能量 |
| 10 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 11 | `dt` | `string` |  |  |

---

## ads_vc_user_chat_data_v2_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_chat_data_v2_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.9M |
| **是否分区表** | 是 |

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `new_register_flag` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `period` | `int` |  |  |
| 5 | `chat_uv` | `bigint` |  |  |
| 6 | `chat_uv_1d` | `bigint` |  |  |
| 7 | `chat_uv_2d` | `bigint` |  |  |
| 8 | `chat_uv_3d` | `bigint` |  |  |
| 9 | `chat_uv_7d` | `bigint` |  |  |
| 10 | `chat_uv_15d` | `bigint` |  |  |
| 11 | `chat_uv_30d` | `bigint` |  |  |
| 12 | `chat_uv_30up` | `bigint` |  |  |
| 13 | `chat_uv_100up` | `bigint` |  |  |
| 14 | `chat_characters` | `bigint` |  |  |
| 15 | `total_chat_rounds` | `bigint` |  |  |
| 16 | `mmt_cnt` | `bigint` |  |  |
| 17 | `heart_cnt` | `bigint` |  |  |
| 18 | `total_consume_nums` | `bigint` |  |  |
| 19 | `total_free_energy` | `bigint` |  |  |
| 20 | `total_buy_energy` | `bigint` |  |  |
| 21 | `total_reward_energy` | `bigint` |  |  |
| 22 | `doctor_chat_uv` | `bigint` |  |  |
| 23 | `doctor_total_rounds` | `bigint` |  |  |
| 24 | `doctor_cost_energy` | `bigint` |  |  |
| 25 | `date_chat_uv` | `bigint` |  |  |
| 26 | `date_total_rounds` | `bigint` |  |  |
| 27 | `date_cost_energy` | `bigint` |  |  |
| 28 | `dt` | `string` |  |  |

---

## ads_vc_user_chat_preference_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_chat_preference_report_di` |
| **描述** | 用户聊天OGC UGC偏好及关键指标分析日报表 |
| **Owner** | bdms_huanglvdian |
| **表类型** | internal |
| **表大小** | 1.3M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `stat_date` | `string` |  | 统计日期 |
| 2 | `chat_preference_group` | `string` |  | 用户聊天类别 |
| 3 | `registration_days_tier` | `string` |  | 注册天数分层 |
| 4 | `lofter_origin_type` | `string` |  | Lofter来源类型 |
| 5 | `active_uv` | `bigint` |  | 活跃用户数 |
| 6 | `next_day_active_uv` | `bigint` |  | 次日活跃用户数 |
| 7 | `t6_active_uv` | `bigint` |  | T6活跃用户数 |
| 8 | `active_user_chat_ratio` | `decimal(18,4)` |  | 活跃用户聊天参与率 |
| 9 | `chat_uv` | `bigint` |  | 聊天用户数 |
| 10 | `total_talk_rounds` | `bigint` |  | 总聊天轮数 |
| 11 | `avg_talk_rounds_per_chat_user` | `decimal(18,4)` |  | 人均聊天轮数(按聊天用户) |
| 12 | `total_payment_amount` | `decimal(18,2)` |  | 付费总金额 |
| 13 | `paid_uv` | `bigint` |  | 付费用户数 |
| 14 | `active_user_payment_ratio` | `decimal(18,4)` |  | 活跃用户付费率 |
| 15 | `arpu` | `decimal(18,4)` |  | ARPU(活跃用户平均收入) |
| 16 | `arppu` | `decimal(18,4)` |  | ARPPU(付费用户平均收入) |
| 17 | `dt` | `string` |  | 分区日期, yyyy-MM-dd |

---

## ads_vc_user_chat_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_chat_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 4.8M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `device_os` | `string` |  | 最后记录的设备os |
| 5 | `new_chat_flag` | `int` |  |  |
| 6 | `chat_uv` | `bigint` |  |  |
| 7 | `chat_uv_1d` | `bigint` |  |  |
| 8 | `chat_uv_2d` | `bigint` |  |  |
| 9 | `chat_uv_3d` | `bigint` |  |  |
| 10 | `chat_uv_7d` | `bigint` |  |  |
| 11 | `chat_uv_15d` | `bigint` |  |  |
| 12 | `chat_uv_30d` | `bigint` |  |  |
| 13 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 14 | `new_register_level` | `string` |  | 注册时间分段 |
| 15 | `dt` | `string` |  |  |

---

## ads_vc_user_chats_churn_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_chats_churn_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 121.9M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `simulator_def` | `string` |  | 模拟器定义：vc,lofter,theater |
| 3 | `chats_churn` | `int` |  | N日流失 |
| 4 | `dt` | `string` |  |  |

---

## ads_vc_user_consum_data_v2_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_consum_data_v2_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 4.7M |
| **是否分区表** | 是 |

### 字段详情

共 30 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `new_register_flag` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `period` | `int` |  |  |
| 5 | `consum_uv` | `bigint` |  |  |
| 6 | `consum_uv_1d` | `bigint` |  |  |
| 7 | `consum_uv_2d` | `bigint` |  |  |
| 8 | `consum_uv_3d` | `bigint` |  |  |
| 9 | `consum_uv_7d` | `bigint` |  |  |
| 10 | `consum_uv_15d` | `bigint` |  |  |
| 11 | `consum_uv_30d` | `bigint` |  |  |
| 12 | `consum_chat_uv_1d` | `bigint` |  |  |
| 13 | `consum_chat_uv_2d` | `bigint` |  |  |
| 14 | `consum_chat_uv_3d` | `bigint` |  |  |
| 15 | `consum_chat_uv_7d` | `bigint` |  |  |
| 16 | `consum_chat_uv_15d` | `bigint` |  |  |
| 17 | `consum_chat_uv_30d` | `bigint` |  |  |
| 18 | `consum_active_uv_1d` | `bigint` |  |  |
| 19 | `consum_active_uv_2d` | `bigint` |  |  |
| 20 | `consum_active_uv_3d` | `bigint` |  |  |
| 21 | `consum_active_uv_7d` | `bigint` |  |  |
| 22 | `consum_active_uv_15d` | `bigint` |  |  |
| 23 | `consum_active_uv_30d` | `bigint` |  |  |
| 24 | `total_amount_1d` | `double` |  |  |
| 25 | `total_amount_2d` | `double` |  |  |
| 26 | `total_amount_3d` | `double` |  |  |
| 27 | `total_amount_7d` | `double` |  |  |
| 28 | `total_amount_15d` | `double` |  |  |
| 29 | `total_amount_30d` | `double` |  |  |
| 30 | `dt` | `string` |  |  |

---

## ads_vc_user_funnel_data_v2_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_funnel_data_v2_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.6M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `string` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `new_register_flag` | `int` |  |  |
| 5 | `active_uv` | `bigint` |  |  |
| 6 | `chat_uv` | `bigint` |  |  |
| 7 | `total_chats` | `bigint` |  |  |
| 8 | `consum_uv` | `bigint` |  |  |
| 9 | `total_amount` | `double` |  |  |
| 10 | `chat_uv_10up` | `bigint` |  |  |
| 11 | `chat_uv_30up` | `bigint` |  |  |
| 12 | `chat_uv_100up` | `bigint` |  |  |
| 13 | `dt` | `string` |  |  |

---

## ads_vc_user_group_core_metrics_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_group_core_metrics_report_di` |
| **描述** | 个人群核心指标探查日报 |
| **Owner** | bdms_huanglvdian |
| **表类型** | internal |
| **表大小** | 5.0M |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `stat_date` | `string` |  | 统计日期 |
| 2 | `content_segment` | `string` |  | 内容分层-人群 |
| 3 | `user_type` | `string` |  | 用户新老 |
| 4 | `lofter_origin_type` | `string` |  | LOFTER来源 |
| 5 | `active_users` | `bigint` |  | 活跃用户数 |
| 6 | `next_day_active_users` | `bigint` |  | 次日活跃用户数(T+1) |
| 7 | `t6_day_active_users` | `bigint` |  | T6日活跃用户数(T+6) |
| 8 | `chat_users` | `bigint` |  | 聊天用户数 |
| 9 | `chat_participation_rate` | `decimal(20,4)` |  | 用户聊天参与率 |
| 10 | `total_chats` | `bigint` |  | 总聊天数 |
| 11 | `avg_chats_per_chat_user` | `decimal(20,4)` |  | 人均聊天数(按聊天用户) |
| 12 | `ogc_chats` | `bigint` |  | OGC聊天数 |
| 13 | `ugc_chats` | `bigint` |  | UGC聊天数 |
| 14 | `ugc_chat_ratio` | `decimal(20,4)` |  | UGC聊天占比 |
| 15 | `total_payment_amount` | `decimal(20,4)` |  | 付费总金额 |
| 16 | `paying_users` | `bigint` |  | 付费用户数 |
| 17 | `payment_rate` | `decimal(20,4)` |  | 付费率 |
| 18 | `arpu` | `decimal(20,4)` |  | ARPU |
| 19 | `arppu` | `decimal(20,4)` |  | ARPPU |
| 20 | `dt` | `string` |  | 分区日期 |

---

## ads_vc_user_interview_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_interview_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.7M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `device_os` | `string` |  | 最后记录的设备os |
| 5 | `new_flg` | `int` |  |  |
| 6 | `uv` | `bigint` |  |  |
| 7 | `uv_1d` | `bigint` |  |  |
| 8 | `uv_2d` | `bigint` |  |  |
| 9 | `uv_3d` | `bigint` |  |  |
| 10 | `uv_5d` | `bigint` |  |  |
| 11 | `uv_7d` | `bigint` |  |  |
| 12 | `uv_15d` | `bigint` |  |  |
| 13 | `uv_30d` | `bigint` |  |  |
| 14 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 15 | `new_register_level` | `string` |  | 注册时间分段 |
| 16 | `dt` | `string` |  |  |

---

## ads_vc_user_paid_retain_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_paid_retain_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 9.7M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `device_os` | `string` |  | 最后记录的设备os |
| 5 | `new_flg` | `int` |  |  |
| 6 | `detail_type` | `string` |  |  |
| 7 | `sub_type` | `string` |  |  |
| 8 | `uv` | `bigint` |  |  |
| 9 | `money` | `double` |  |  |
| 10 | `uv_1d` | `bigint` |  |  |
| 11 | `uv_2d` | `bigint` |  |  |
| 12 | `uv_7d` | `bigint` |  |  |
| 13 | `uv_15d` | `bigint` |  |  |
| 14 | `uv_30d` | `bigint` |  |  |
| 15 | `uv_60d` | `bigint` |  |  |
| 16 | `uv_90d` | `bigint` |  |  |
| 17 | `new_register_flg` | `int` |  |  |
| 18 | `dt` | `string` |  |  |

---

## ads_vc_user_paid_retain_v2_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_paid_retain_v2_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 18.1M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `period` | `int` |  |  |
| 3 | `origin_type` | `string` |  | 来源类型 |
| 4 | `device_os` | `string` |  | 最后记录的设备os |
| 5 | `new_flg` | `int` |  |  |
| 6 | `new_register_flg` | `int` |  |  |
| 7 | `new_register_level` | `string` |  |  |
| 8 | `detail_type` | `string` |  |  |
| 9 | `sub_type` | `string` |  |  |
| 10 | `uv` | `bigint` |  |  |
| 11 | `money` | `double` |  |  |
| 12 | `uv_1d` | `bigint` |  |  |
| 13 | `uv_2d` | `bigint` |  |  |
| 14 | `uv_3d` | `bigint` |  |  |
| 15 | `uv_5d` | `bigint` |  |  |
| 16 | `uv_7d` | `bigint` |  |  |
| 17 | `uv_15d` | `bigint` |  |  |
| 18 | `uv_30d` | `bigint` |  |  |
| 19 | `uv_60d` | `bigint` |  |  |
| 20 | `uv_90d` | `bigint` |  |  |
| 21 | `dt` | `string` |  |  |

---

## ads_vc_user_transfer_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc_dm` |
| **表名** | `ads_vc_user_transfer_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.0M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `new_flag` | `int` |  |  |
| 2 | `active_uv` | `bigint` |  |  |
| 3 | `chat_uv` | `bigint` |  |  |
| 4 | `total_chats` | `bigint` |  |  |
| 5 | `consum_uv` | `bigint` |  |  |
| 6 | `total_amount` | `double` |  |  |
| 7 | `chat_uv_10up` | `bigint` |  | 发送 10 条及以上消息的用户数 |
| 8 | `chat_uv_30up` | `bigint` |  | 发送 30 条及以上消息的用户数 |
| 9 | `chat_uv_100up` | `bigint` |  | 发送 100 条及以上消息的用户数 |
| 10 | `dt` | `string` |  |  |

---

