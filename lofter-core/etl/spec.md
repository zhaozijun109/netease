# LOFTER 数据仓库知识库

> **定位**：本文档是 LOFTER 数据仓库的**唯一权威业务知识库**，面向 AI 生成可在 **Doris / Hive（SparkSQL）** 上直接执行的 SQL。  
> **范围**：数据分层架构 · 核心表与字段 · 业务规则 · SQL 模式模板 · ETL 场景示例 · 业务术语。  
> **调度**：所有 ETL 作业由 **Azkaban** 调度，日期参数统一使用 `${azkaban.flow.1.days.ago}`（T-1）和 `${azkaban.flow.7.days.ago}`（T-7）。

---

## 目录

1. [数据分层架构](#1-数据分层架构)
   - 1.1 分层模型
   - 1.2 数据库命名
   - 1.3 表命名规范
   - 1.4 表后缀与查询规范
2. [核心表与字段速查](#2-核心表与字段速查)
   - 2.1 高频表 TOP 1000
   - 2.2 高频字段 TOP 145
   - 2.3 核心维度表 Schema
3. [业务规则与数据规范](#3-业务规则与数据规范)
   - 3.1 有效文章过滤
   - 3.2 有效浏览判定
   - 3.3 文章状态映射
   - 3.4 时间戳处理
   - 3.5 数据质量过滤
   - 3.6 分区查询规范
   - 3.7 SQL 优化规范
4. [高频 SQL 模式与模板](#4-高频-sql-模式与模板)
5. [典型 ETL 场景示例](#5-典型-etl-场景示例)
6. [附录](#6-附录)
   - 6.1 业务术语表
   - 6.2 枚举值映射
   - 6.3 数据流向图

---

## 1. 数据分层架构

### 1.1 分层模型

```
┌──────────────────────────────────────────────────────────┐
│  ADS 应用层  (lofter_dm)                                  │
│  ads_* — 面向业务：聚合宽表、排行榜、推荐池、报表         │
├──────────────────────────────────────────────────────────┤
│  DWS 汇总层  (lofter)                                     │
│  dws_* — 主题域汇总：流量、互动、创作者、用户画像         │
├──────────────────────────────────────────────────────────┤
│  DWD 明细层  (lofter)                                     │
│  dwd_* — 事件明细：浏览、点赞、发布、设备归因             │
├──────────────────────────────────────────────────────────┤
│  DIM 维度层  (lofter)                                     │
│  dim_* — 核心实体：用户、博客/创作者、文章、IP、标签       │
├──────────────────────────────────────────────────────────┤
│  ODS 原始层  (lofter / lofter_db_dump)                    │
│  ods_* — 日志、数据库快照、binlog/log 实时数据             │
└──────────────────────────────────────────────────────────┘
```

### 1.2 数据库命名

| 数据库 | 用途 | 代码引用频次 |
|--------|------|-------------|
| `lofter` | 核心数仓 — DIM / DWD / DWS / ODS 各层 | 4248 |
| `lofter_dm` | 应用层数据集市 — ADS 层 | 1249 |
| `lofter_db_dump` | 业务数据库全量快照 — `ods_db_*_nd` 表 | 1240 |

### 1.3 表命名规范

| 前缀 | 含义 | 数据来源与时效 | 典型示例 |
|------|------|--------------|---------|
| `ods_mda_*` | 客户端埋点日志 | **离线** `ods_mda_app_di`（查 T-1 及更早）; **实时** `ods_mda_app_raw_di`（每 10min 落盘，查当日 T）; **视图** `ods_mda_app_partition_di`（→离线实表） | `ods_mda_app_di` |
| `ods_binlog_*` | binlog 增量数据 | **实时**（Kafka → Hive） | `ods_binlog_post_hot_di` |
| `ods_log_*` | 服务端日志 | **实时**（Kafka → Hive） | `ods_log_praise_di` |
| `ods_db_*_nd` | 数据库全量快照 | 离线全量 dump（自动指向最新分区） | `ods_db_user_following_nd` |
| `dim_*` | 维度表 | DIM 层，全量快照或按 `dt` 分区 | `dim_post` / `dim_user` / `dim_blog` |
| `dwd_*_di` | 日增量明细 | 每天只写当日新增/变更 | `dwd_post_browse_di` |
| `dwd_*_dd` | 日全量明细 | 每天全量覆盖，每分区为完整快照 | `dwd_post_status_dd` |
| `dws_*_di` | 日增量汇总 | 增量汇总 | `dws_post_traffic_di` |
| `dws_*_dd` | 日全量汇总 | 全量覆盖汇总 | `dws_par_user_base_dd` |
| `ads_*_di/dd` | 应用层 | 面向业务的聚合表 | `ads_post_category_di` |
| `bridge_*` | 桥接/映射表 | 多对多关系 | `bridge_collection_post` |
| `dwb_*` | 中间宽表 | 过渡性宽表 | `dwb_par_user_info_nd` |
| `stg_*` | 暂存/过渡表 | ETL 中间结果 | `stg_post_content_feature_dd` |

### 1.4 表后缀与查询规范

> ⚠️ **表名后缀决定查询方式**，这是编写 SQL 前必须确认的第一件事。

| 后缀 | 全称 | 分区 | 更新模式 | 查询规则 |
|------|------|------|---------|---------|
| `_dd` | Day Delta（日全量快照） | 有 `dt` | 每天全量覆盖，每个 `dt` 分区是当日完整快照 | **`dt` 必须且只取最新一天**；需要时间范围时，用表内业务时间字段二次过滤 |
| `_di` | Day Increment（日增量） | 有 `dt` | 每天只写当日新增/变更记录 | 单日 `dt = '${azkaban.flow.1.days.ago}'`；跨天 `dt BETWEEN ... AND ...` |
| `_nd` | No-partition Delta（无分区全量快照） | 无（自动指向最新） | 全量 dump，始终为最新快照 | **不加 `dt` 条件**；用业务时间字段限制范围 |

**速记**：`_dd` → 锁最新分区 · `_di` → 按需选分区范围 · `_nd` → 不加 dt，用业务时间过滤

**`_dd` 时间范围查询示范**：

```sql
-- ✅ 正确：dt 锁定最新分区，业务时间字段做范围过滤
SELECT userId, createDate, total_browse_cnt
FROM lofter.dws_user_life_circle_index_dd
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND createDate BETWEEN '${azkaban.flow.7.days.ago}' AND '${azkaban.flow.1.days.ago}'

-- ❌ 错误：跨分区查全量表 → 数据严重重复
SELECT userId, createDate, total_browse_cnt
FROM lofter.dws_user_life_circle_index_dd
WHERE dt BETWEEN '${azkaban.flow.7.days.ago}' AND '${azkaban.flow.1.days.ago}'
```

---

## 2. 核心表与字段速查

> 以下数据基于 LOFTER ETL 代码库 1333 个 SQL 作业文件的静态分析。引用次数反映表/字段在生产 SQL 中的出现频率。

### 2.1 高频表 TOP 1000（按代码引用次数降序）

| # | 表名 | 引用次数 | 层级 |
|---|------|---------|------|
| 1 | `lofter.dim_post` | 292 | DIM |
| 2 | `lofter.ods_mda_app_partition_di` | 155 | ODS |
| 3 | `lofter.dim_user` | 139 | DIM |
| 4 | `lofter.dwd_post_browse_di` | 105 | DWD |
| 5 | `lofter.dws_post_interaction_dd` | 95 | DWS |
| 6 | `lofter.dws_par_creator_dd` | 95 | DWS |
| 7 | `lofter.dws_pve_user_role_chats_di` | 91 | DWS |
| 8 | `lofter.device_active` | 87 | DWD |
| 9 | `lofter.dim_gift_post_return_dd` | 86 | DIM |
| 10 | `lofter.dwd_gift_post_unlock_dd` | 85 | DWD |
| 11 | `lofter_dm.ads_gift_post_title_classify_blog_dd` | 80 | ADS |
| 12 | `lofter.dws_par_user_interaction_dd` | 75 | DWS |
| 13 | `lofter.dwd_post_hot_di` | 71 | DWD |
| 14 | `lofter_db_dump.ods_db_user_following_nd` | 70 | ODS |
| 15 | `lofter.dwd_evt_post_paid_detail_dd` | 67 | DWD |
| 16 | `lofter.dim_post_article` | 66 | DIM |
| 17 | `lofter.dwd_user_order_dd` | 65 | DWD |
| 18 | `lofter.dws_par_user_pay_dd` | 58 | DWS |
| 19 | `lofter.dws_par_user_post_dd` | 51 | DWS |
| 20 | `lofter.dws_par_user_base_dd` | 50 | DWS |
| 21 | `lofter.dwd_post_publish_di` | 50 | DWD |
| 22 | `lofter.dim_blog` | 48 | DIM |
| 23 | `lofter.dws_par_user_revenue_dd` | 47 | DWS |
| 24 | `lofter_db_dump.ods_db_post_response_nd` | 46 | ODS |
| 25 | `lofter.dws_post_base_stats_di` | 46 | DWS |
| 26 | `lofter_db_dump.ods_db_post_hot_nd` | 45 | ODS |
| 27 | `lofter_db_dump.ods_db_benefit_order_product_nd` | 44 | ODS |
| 28 | `lofter.dwd_device_growth_attribution_di` | 43 | DWD |
| 29 | `lofter.dws_par_creator_level_scoring_dd` | 41 | DWS |
| 30 | `lofter.dwd_post_response_di` | 40 | DWD |
| 31 | `lofter.dim_gift_post_return` | 39 | DIM |
| 32 | `lofter_db_dump.ods_db_trade_gift_present_record_nd` | 37 | ODS |
| 33 | `lofter.dws_par_user_fans_dd` | 37 | DWS |
| 34 | `lofter.dws_post_premium_di` | 34 | DWS |
| 35 | `lofter.dws_collection_dd` | 34 | DWS |
| 36 | `lofter.dwd_pve_user_amount_info_di` | 33 | DWD |
| 37 | `lofter.dwd_growth_actpwd_access_di` | 33 | DWD |
| 38 | `lofter_db_dump.ods_db_recommend_review_post_nd` | 32 | ODS |
| 39 | `lofter.dws_pve_user_role_amount_di` | 31 | DWS |
| 40 | `lofter.dws_par_creator_di` | 31 | DWS |
| 41 | `lofter_db_dump.ods_db_trade_gift_account_nd` | 30 | ODS |
| 42 | `lofter_db_dump.ods_db_risk_antispam_callback_record_nd` | 30 | ODS |
| 43 | `lofter.dws_par_user_interaction_di` | 30 | DWS |
| 44 | `lofter.dws_par_user_active_di` | 30 | DWS |
| 45 | `lofter.dws_par_user_ec_dd` | 29 | DWS |
| 46 | `lofter.dws_par_user_active_dd` | 29 | DWS |
| 47 | `lofter.dws_post_interaction_di` | 28 | DWS |
| 48 | `lofter_dm.ads_post_paid_user_consume_detail_di` | 27 | ADS |
| 49 | `lofter_db_dump.ods_db_ask_question_nd` | 27 | ODS |
| 50 | `lofter.dws_post_base_stats_dd` | 27 | DWS |
| 51 | `lofter.bridge_collection_post` | 27 | Bridge |
| 52 | `lofter_db_dump.ods_db_benefit_order_nd` | 26 | ODS |
| 53 | `lofter.device_new` | 26 | DWD |
| 54 | `lofter_db_dump.ods_db_trade_gift_pay_account_nd` | 25 | ODS |
| 55 | `lofter_db_dump.ods_db_cmb_tag_nd` | 25 | ODS |
| 56 | `lofter.dwd_post_share_di` | 25 | DWD |
| 57 | `lofter.dwd_post_expose_di` | 25 | DWD |
| 58 | `lofter.dwd_paid_subscribe_order_di` | 25 | DWD |
| 59 | `lofter.dim_post_category_dd` | 25 | DIM |
| 60 | `lofter_db_dump.ods_db_pfb_content_related_post_nd` | 24 | ODS |
| 61 | `lofter.dws_post_traffic_di` | 24 | DWS |
| 62 | `lofter_db_dump.ods_db_pve_role_info_nd` | 23 | ODS |
| 63 | `lofter_db_dump.ods_db_ask_answer_post_nd` | 23 | ODS |
| 64 | `lofter.dwd_post_length_dd` | 23 | DWD |
| 65 | `lofter.dwd_device_mapping` | 23 | DWD |
| 66 | `lofter.dim_ip_dd` | 23 | DIM |
| 67 | `lofter.dwd_ab_platform_exp_user_di` | 22 | DWD |
| 68 | `lofter_db_dump.ods_db_cmb_category_nd` | 21 | ODS |
| 69 | `lofter_db_dump.ods_db_benefit_trade_nd` | 21 | ODS |
| 70 | `lofter.dwd_search_action_di` | 21 | DWD |
| 71 | `lofter.dim_benefit_product` | 21 | DIM |
| 72 | `lofter.dim_actpwd_dd` | 21 | DIM |
| 73 | `lofter_db_dump.ods_db_trade_store_vip_order_nd` | 20 | ODS |
| 74 | `lofter.dwd_user_active_di` | 20 | DWD |
| 75 | `lofter.dwd_paid_post_detail_dd` | 20 | DWD |
| 76 | `lofter_db_dump.ods_db_tag_resource_nd` | 19 | ODS |
| 77 | `lofter_db_dump.ods_db_post_collection_nd` | 19 | ODS |
| 78 | `lofter.device_retain` | 19 | DWD |
| 79 | `lofter_db_dump.ods_db_blog_info_nd` | 18 | ODS |
| 80 | `lofter.dws_user_pay_type_info_dd` | 18 | DWS |
| 81 | `lofter.dws_par_creator_interaction_dd` | 18 | DWS |
| 82 | `lofter.dwd_user_return_di` | 18 | DWD |
| 83 | `lofter.dwd_push_action_di` | 18 | DWD |
| 84 | `lofter.dwd_evt_benefit_page_view_di` | 18 | DWD |
| 85 | `lofter.ods_mda_wap_di` | 17 | ODS |
| 86 | `lofter.dwd_ad_actions_di` | 17 | DWD |
| 87 | `lofter.device_return` | 17 | DWD |
| 88 | `lofter_db_dump.ods_db_trade_return_gift_plan_nd` | 16 | ODS |
| 89 | `lofter_db_dump.ods_db_trade_pay_grade_nd` | 16 | ODS |
| 90 | `lofter.dws_par_user_tag_create_dd` | 16 | DWS |
| 91 | `lofter.dwd_par_device_all_dd` | 16 | DWD |
| 92 | `lofter.dim_post_talk` | 16 | DIM |
| 93 | `lofter.bridge_calendar_date` | 16 | Bridge |
| 94 | `lofter_dm.ads_gift_post_title_classify_` | 15 | ADS |
| 95 | `lofter_db_dump.ods_db_trade_fans_vip_account_nd` | 15 | ODS |
| 96 | `lofter_db_dump.ods_db_benefit_category_nd` | 15 | ODS |
| 97 | `lofter_db_dump.ods_db_benefit_card_activity_nd` | 15 | ODS |
| 98 | `lofter.ods_log_anti_spam_copy_di` | 15 | ODS |
| 99 | `lofter.dws_video_post_general_di` | 15 | DWS |
| 100 | `lofter.dws_post_traffic_dd` | 15 | DWS |
| 101 | `lofter.dws_par_user_coupon_exchange_dd` | 15 | DWS |
| 102 | `lofter.dws_membership_post_score_dd` | 15 | DWS |
| 103 | `lofter.dws_evt_login_user_last_dd` | 15 | DWS |
| 104 | `lofter_dm.ads_tag_general_` | 14 | ADS |
| 105 | `lofter_dm.ads_par_user_tag_group_level` | 14 | ADS |
| 106 | `lofter_dm.ads_creator_double_perspective_post_di` | 14 | ADS |
| 107 | `lofter_db_dump.ods_db_post_nd` | 14 | ODS |
| 108 | `lofter_db_dump.ods_db_grain_info_nd` | 14 | ODS |
| 109 | `lofter_db_dump.ods_db_benefit_product_info_nd` | 14 | ODS |
| 110 | `lofter.dws_post_highlight_comment_dd` | 14 | DWS |
| 111 | `lofter.dws_par_creator_traffic_dd` | 14 | DWS |
| 112 | `lofter.dws_gift_post_premium_scoring_dd` | 14 | DWS |
| 113 | `lofter.dws_gift_post_di` | 14 | DWS |
| 114 | `lofter.dwd_user_new_di` | 14 | DWD |
| 115 | `lofter.dwd_tag_ip_mapping_nd` | 14 | DWD |
| 116 | `lofter.dwd_evt_user_login_di` | 14 | DWD |
| 117 | `lofter.dwd_blog_follow_di` | 14 | DWD |
| 118 | `lofter.dwd_ad_growth_device_di` | 14 | DWD |
| 119 | `lofter_dm.ads_paid_post_blog_level_detail_di` | 13 | ADS |
| 120 | `lofter_db_dump.ods_db_trade_return_gift_feed_back_nd` | 13 | ODS |
| 121 | `lofter_db_dump.ods_db_trade_fans_vip_order_nd` | 13 | ODS |
| 122 | `lofter_db_dump.ods_db_admin_pub_data_nd` | 13 | ODS |
| 123 | `lofter_db_dump.ods_db_ad_channel_config_nd` | 13 | ODS |
| 124 | `lofter.dws_tag_user_consume_di` | 13 | DWS |
| 125 | `lofter.dws_par_user_content_di` | 13 | DWS |
| 126 | `lofter.dws_ip_user_consume_dd` | 13 | DWS |
| 127 | `lofter.dws_gift_post_premium_scoring_detail_dd` | 13 | DWS |
| 128 | `lofter.dwd_ad_actions_v` | 13 | DWD |
| 129 | `lofter.dwd_act_pve_action_di` | 13 | DWD |
| 130 | `lofter.dim_miniprogram_post_dd` | 13 | DIM |
| 131 | `lofter_dm.ads_paid_post_premium_scoring_group_normalization_di` | 12 | ADS |
| 132 | `lofter_dm.ads_paid_post_blog_ai_low_quality_studio_target_detail_di` | 12 | ADS |
| 133 | `lofter_dm.ads_hot_search_list_di` | 12 | ADS |
| 134 | `lofter_db_dump.ods_db_trade_user_exchange_coupon_nd` | 12 | ODS |
| 135 | `lofter_db_dump.ods_db_trade_pve_stamina_order_nd` | 12 | ODS |
| 136 | `lofter_db_dump.ods_db_favorite_tag_nd` | 12 | ODS |
| 137 | `lofter_db_dump.ods_db_cmb_ip_nd` | 12 | ODS |
| 138 | `lofter.ods_log_ad_new_linkup_di` | 12 | ODS |
| 139 | `lofter.dws_tag_dd` | 12 | DWS |
| 140 | `lofter.dws_par_user_ad_di` | 12 | DWS |
| 141 | `lofter.dws_ip_user_consume_di` | 12 | DWS |
| 142 | `lofter.dwd_ad_resource_action_di` | 12 | DWD |
| 143 | `lofter.dim_tag` | 12 | DIM |
| 144 | `lofter.dim_pve_user_dd` | 12 | DIM |
| 145 | `lofter.dim_gift_post` | 12 | DIM |
| 146 | `lofter.dim_gift` | 12 | DIM |
| 147 | `lofter_dm.ads_paid_post_premium_scoring_all_round_di` | 11 | ADS |
| 148 | `lofter_dm.ads_growth_content_di` | 11 | ADS |
| 149 | `lofter_dm.ads_creator_guide_task_data_dd` | 11 | ADS |
| 150 | `lofter_db_dump.ods_db_trade_user_free_gift_nd` | 11 | ODS |
| 151 | `lofter_db_dump.ods_db_trade_member_ship_order_nd` | 11 | ODS |
| 152 | `lofter_db_dump.ods_db_trade_coupon_order_nd` | 11 | ODS |
| 153 | `lofter_db_dump.ods_db_post_count_nd` | 11 | ODS |
| 154 | `lofter.dws_pve_user_coststamina_role_di` | 11 | DWS |
| 155 | `lofter.dwd_pve_user_interview_active_di` | 11 | DWD |
| 156 | `lofter.dwd_pve_user_chats_info_di` | 11 | DWD |
| 157 | `lofter.dim_seven_group_ip_category_res_dd` | 11 | DIM |
| 158 | `lofter.dim_gift_post_dd` | 11 | DIM |
| 159 | `lofter.dim_domain` | 11 | DIM |
| 160 | `lofter_dm.ads_video_post_interaction_dd` | 10 | ADS |
| 161 | `lofter_dm.ads_membership_hot_list_di` | 10 | ADS |
| 162 | `lofter_dm.ads_creator_ecology_identity_dd` | 10 | ADS |
| 163 | `lofter_db_dump.ods_db_recommend_tag_new_nd` | 10 | ODS |
| 164 | `lofter_db_dump.ods_db_pfb_content_nd` | 10 | ODS |
| 165 | `lofter_db_dump.ods_db_audit_log_nd` | 10 | ODS |
| 166 | `lofter.ods_binlog_post_response_di` | 10 | ODS |
| 167 | `lofter.dws_user_security_level_di` | 10 | DWS |
| 168 | `lofter.dws_pve_user_action_di` | 10 | DWS |
| 169 | `lofter.dws_membership_ip_dd` | 10 | DWS |
| 170 | `lofter.dws_device_growth_dau_stratify_di` | 10 | DWS |
| 171 | `lofter.dws_ab_platform_exp` | 10 | DWS |
| 172 | `lofter.dwd_post_collection_di` | 10 | DWD |
| 173 | `lofter_dm.ads_tag_general_di` | 9 | ADS |
| 174 | `lofter_dm.ads_specialty_categories_level` | 9 | ADS |
| 175 | `lofter_dm.ads_pve_user_interview_di` | 9 | ADS |
| 176 | `lofter_dm.ads_pve_user_access_free_cp_di` | 9 | ADS |
| 177 | `lofter_db_dump.ods_db_trade_reward_order_nd` | 9 | ODS |
| 178 | `lofter_db_dump.ods_db_challenge_tag_nd` | 9 | ODS |
| 179 | `lofter_db_dump.ods_db_authenticate_blog_nd` | 9 | ODS |
| 180 | `lofter.ods_risk_limit` | 9 | ODS |
| 181 | `lofter.ods_mda_web_di` | 9 | ODS |
| 182 | `lofter.dws_user_life_circle_judge_dd` | 9 | DWS |
| 183 | `lofter.dws_tag_user_consume_dd` | 9 | DWS |
| 184 | `lofter.dws_post_support_di` | 9 | DWS |
| 185 | `lofter.dws_par_user_traffic_dd` | 9 | DWS |
| 186 | `lofter.dws_par_device_active_dd` | 9 | DWS |
| 187 | `lofter.dws_par_creator_premium_staging_dd` | 9 | DWS |
| 188 | `lofter.dws_ip_user_life_cycle_dd` | 9 | DWS |
| 189 | `lofter.dws_ip_growth_dd` | 9 | DWS |
| 190 | `lofter.dws_gift_post_premium_scoring_v` | 9 | DWS |
| 191 | `lofter.dws_gift_post_premium_scoring_di` | 9 | DWS |
| 192 | `lofter.dwd_pve_user_chats_active_di` | 9 | DWD |
| 193 | `lofter.dwd_membership_order_di` | 9 | DWD |
| 194 | `lofter.dim_membership_post_vip_dd` | 9 | DIM |
| 195 | `lofter.dim_membership_post_dd` | 9 | DIM |
| 196 | `lofter_dm.ads_risk_actpwd_comment_induction_di` | 8 | ADS |
| 197 | `lofter_dm.ads_post_tag_group_level` | 8 | ADS |
| 198 | `lofter_dm.ads_post_paid_creator_revenue_detail_v` | 8 | ADS |
| 199 | `lofter_dm.ads_post_general_di` | 8 | ADS |
| 200 | `lofter_dm.ads_par_user_tag_metric_level` | 8 | ADS |
| 201 | `lofter_db_dump.ods_db_trade_return_gift_exchange_record_nd` | 8 | ODS |
| 202 | `lofter_db_dump.ods_db_pve_user_dialogue_nd` | 8 | ODS |
| 203 | `lofter_db_dump.ods_db_message_nd` | 8 | ODS |
| 204 | `lofter_db_dump.ods_db_forbid_nd` | 8 | ODS |
| 205 | `lofter_db_dump.ods_db_cmb_business_introduction_nd` | 8 | ODS |
| 206 | `lofter_db_dump.ods_db_c` | 8 | ODS |
| 207 | `lofter_db_dump.ods_db_blog_settings_nd` | 8 | ODS |
| 208 | `lofter.dws_par_device_session_di` | 8 | DWS |
| 209 | `lofter.dws_gift_post_return_dd` | 8 | DWS |
| 210 | `lofter.dws_gift_post_premium_ip_scoring_dd` | 8 | DWS |
| 211 | `lofter.dwd_tag_browse_di` | 8 | DWD |
| 212 | `lofter.dwd_gift_return_post_dd` | 8 | DWD |
| 213 | `lofter.dwd_device_growth_content_di` | 8 | DWD |
| 214 | `lofter_dm.ads_specialty_user_consume_di` | 7 | ADS |
| 215 | `lofter_dm.ads_specialty_creator_publish_di` | 7 | ADS |
| 216 | `lofter_dm.ads_risk_shortlink_induction_posts_di` | 7 | ADS |
| 217 | `lofter_dm.ads_risk_actpwd_similar_posts_di` | 7 | ADS |
| 218 | `lofter_dm.ads_risk_actpwd_post_header_induction_di` | 7 | ADS |
| 219 | `lofter_dm.ads_risk_actpwd_post_footer_induction_di` | 7 | ADS |
| 220 | `lofter_dm.ads_domain_tag_rank_di` | 7 | ADS |
| 221 | `lofter_db_dump.ods_db_subscribe_collection_nd` | 7 | ODS |
| 222 | `lofter_db_dump.ods_db_robot_blog_info_nd` | 7 | ODS |
| 223 | `lofter_db_dump.ods_db_pve_user_stamina_log_nd` | 7 | ODS |
| 224 | `lofter_db_dump.ods_db_post_collection_record_nd` | 7 | ODS |
| 225 | `lofter_db_dump.ods_db_media_account_import_nd` | 7 | ODS |
| 226 | `lofter_db_dump.ods_db_dressing_suit_order_nd` | 7 | ODS |
| 227 | `lofter_db_dump.ods_db_derive_partner_nd` | 7 | ODS |
| 228 | `lofter_db_dump.ods_db_derive_partner_discount_nd` | 7 | ODS |
| 229 | `lofter_db_dump.ods_db_comment_hot_nd` | 7 | ODS |
| 230 | `lofter_db_dump.ods_db_ask_user_item_score_nd` | 7 | ODS |
| 231 | `lofter.zq_lofter_recommendwork_name_zeppelin` | 7 | ZQ |
| 232 | `lofter.dws_par_user_session_di` | 7 | DWS |
| 233 | `lofter.dws_par_user_ad_dd` | 7 | DWS |
| 234 | `lofter.dws_creator_valid_detail_di` | 7 | DWS |
| 235 | `lofter.dws_category_user_consume_dd` | 7 | DWS |
| 236 | `lofter.dwd_user_events_di` | 7 | DWD |
| 237 | `lofter.dwd_liaoliao_mda_base` | 7 | DWD |
| 238 | `lofter.dwd_content_browse_di` | 7 | DWD |
| 239 | `lofter.dwd_collection_user_subscribe_dd` | 7 | DWD |
| 240 | `lofter.dwd_cc_module_query_di` | 7 | DWD |
| 241 | `lofter.dim_pve_user` | 7 | DIM |
| 242 | `lofter.dim_bookstore_post_dd` | 7 | DIM |
| 243 | `lofter_dm.ads_tag_blog_index_di` | 6 | ADS |
| 244 | `lofter_dm.ads_post_trend_grade_di` | 6 | ADS |
| 245 | `lofter_dm.ads_gift_return_tag_di` | 6 | ADS |
| 246 | `lofter_dm.ads_ec_product_trade_retain_di` | 6 | ADS |
| 247 | `lofter_dm.ads_device_platform_di` | 6 | ADS |
| 248 | `lofter_db_dump.ods_db_verify_blog_nd` | 6 | ODS |
| 249 | `lofter_db_dump.ods_db_risk_antispam_post_tmp_nd` | 6 | ODS |
| 250 | `lofter_db_dump.ods_db_p_comment_user_mark_nd` | 6 | ODS |
| 251 | `lofter_db_dump.ods_db_image_market_product_order_nd` | 6 | ODS |
| 252 | `lofter_db_dump.ods_db_image_market_loot_box_order_nd` | 6 | ODS |
| 253 | `lofter_db_dump.ods_db_derive_purchase_order_item_nd` | 6 | ODS |
| 254 | `lofter_db_dump.ods_db_benefit_new_coupon_nd` | 6 | ODS |
| 255 | `lofter_db_dump.ods_db_ab_exp_nd` | 6 | ODS |
| 256 | `lofter.stg_post_content_feature_dd` | 6 | STG |
| 257 | `lofter.dws_par_user_push_dd` | 6 | DWS |
| 258 | `lofter.dws_par_user_ip_create_dd` | 6 | DWS |
| 259 | `lofter.dws_ip_supply_di` | 6 | DWS |
| 260 | `lofter.dws_category_user_consume_di` | 6 | DWS |
| 261 | `lofter.dws_ab_platform_push_user_device_metric_di` | 6 | DWS |
| 262 | `lofter.dwd_user_ad_revenue_di` | 6 | DWD |
| 263 | `lofter.dwd_par_creator_first_publish_di` | 6 | DWD |
| 264 | `lofter.dwd_ad_content_unlock_di` | 6 | DWD |
| 265 | `lofter.dwd_act_paper_man_action_di` | 6 | DWD |
| 266 | `lofter.dwd_act_card_action_di` | 6 | DWD |
| 267 | `lofter.dim_date_rolling` | 6 | DIM |
| 268 | `lofter.db` | 6 | Other |
| 269 | `lofter_dm.ads_tag_blog_rank_moderated_di` | 5 | ADS |
| 270 | `lofter_dm.ads_risk_tag_post_rank_brush_hot_suspect_users_di` | 5 | ADS |
| 271 | `lofter_dm.ads_risk_offsite_induction_zhihu_collection_di` | 5 | ADS |
| 272 | `lofter_dm.ads_evt_paid_post_reservoir_user_di` | 5 | ADS |
| 273 | `lofter_dm.ads_creator_rec_reason_detail_di` | 5 | ADS |
| 274 | `lofter_db_dump.ods_db_user_subscribe_folder_nd` | 5 | ODS |
| 275 | `lofter_db_dump.ods_db_trade_support_record_nd` | 5 | ODS |
| 276 | `lofter_db_dump.ods_db_trade_pay_creator_idea_nd` | 5 | ODS |
| 277 | `lofter_db_dump.ods_db_trade_paper_man_stamina_order_nd` | 5 | ODS |
| 278 | `lofter_db_dump.ods_db_trade_coupon_coin_order_nd` | 5 | ODS |
| 279 | `lofter_db_dump.ods_db_trade_buy_coin_order_nd` | 5 | ODS |
| 280 | `lofter_db_dump.ods_db_trade_ad_reward_complete_nd` | 5 | ODS |
| 281 | `lofter_db_dump.ods_db_text_post_nd` | 5 | ODS |
| 282 | `lofter_db_dump.ods_db_rta_creator_ad_switch_nd` | 5 | ODS |
| 283 | `lofter_db_dump.ods_db_photo_post_nd` | 5 | ODS |
| 284 | `lofter_db_dump.ods_db_kol_user_nd` | 5 | ODS |
| 285 | `lofter_db_dump.ods_db_dressing_suit_nd` | 5 | ODS |
| 286 | `lofter_db_dump.ods_db_blog_official_blog_nd` | 5 | ODS |
| 287 | `lofter_db_dump.ods_db_benefit_user_card_bag_nd` | 5 | ODS |
| 288 | `lofter_db_dump.ods_db_benefit_ip_series_relation_nd` | 5 | ODS |
| 289 | `lofter_db_dump.ods_db_avatar_box_order_nd` | 5 | ODS |
| 290 | `lofter_db_dump.ods_db_act_incentive_mission_user_nd` | 5 | ODS |
| 291 | `lofter_db_dump.ods_db_act_incentive_mission_nd` | 5 | ODS |
| 292 | `lofter.tag_fetch_tag_action_dd` | 5 | Other |
| 293 | `lofter.stg_post_hot_dynamic_in_` | 5 | STG |
| 294 | `lofter.ods_mda_app_di` | 5 | ODS |
| 295 | `lofter.ods_binlog_text_post_di` | 5 | ODS |
| 296 | `lofter.dws_par_user_stratify_di` | 5 | DWS |
| 297 | `lofter.dws_par_device_session_v` | 5 | DWS |
| 298 | `lofter.dws_par_device_interaction_di` | 5 | DWS |
| 299 | `lofter.dws_ab_platform_pve_metric_expand_di` | 5 | DWS |
| 300 | `lofter.dws_ab_platform_paycontent_metric_v` | 5 | DWS |
| 301 | `lofter.dws_ab_platform_paycontent_metric_di` | 5 | DWS |
| 302 | `lofter.dws_ab_platform_ecology_scene_metric_di` | 5 | DWS |
| 303 | `lofter.dws_ab_platform_ecology_fullsite_metric_di` | 5 | DWS |
| 304 | `lofter.dws_ab_platform_ecology_creator_metric_di` | 5 | DWS |
| 305 | `lofter.dws_ab_platform_ecology_collection_metric_di` | 5 | DWS |
| 306 | `lofter.dws_ab_platform_client_metric_di` | 5 | DWS |
| 307 | `lofter.dwd_user_retention_di` | 5 | DWD |
| 308 | `lofter.dwd_tag_subscribe_di` | 5 | DWD |
| 309 | `lofter.dwd_suspect_shuare_model_di` | 5 | DWD |
| 310 | `lofter.dwd_rec_content_understand_dd` | 5 | DWD |
| 311 | `lofter.dwd_device_all_dd` | 5 | DWD |
| 312 | `lofter.dim_tag_dd` | 5 | DIM |
| 313 | `lofter.dim_miniprogram_post` | 5 | DIM |
| 314 | `lofter.dim_kol_channel_dd` | 5 | DIM |
| 315 | `lofter.dim_black_tag` | 5 | DIM |
| 316 | `lofter.dim_act_accompany_tag` | 5 | DIM |
| 317 | `lofter.category_domain_mapping` | 5 | Other |
| 318 | `lofter_dm.ads_theater_active_user_report_di` | 4 | ADS |
| 319 | `lofter_dm.ads_taitai_open_report_dd` | 4 | ADS |
| 320 | `lofter_dm.ads_specialty_categories_post_` | 4 | ADS |
| 321 | `lofter_dm.ads_risk_tag_post_rank_brush_hot_review_di` | 4 | ADS |
| 322 | `lofter_dm.ads_risk_shuare_model_stat_di` | 4 | ADS |
| 323 | `lofter_dm.ads_risk_induction_block_posts_di` | 4 | ADS |
| 324 | `lofter_dm.ads_risk_induction_block_blogs_di` | 4 | ADS |
| 325 | `lofter_dm.ads_revenue_monitor_v` | 4 | ADS |
| 326 | `lofter_dm.ads_pve_chat_new_users_metric_dd` | 4 | ADS |
| 327 | `lofter_dm.ads_paid_post_quality_score_value_di` | 4 | ADS |
| 328 | `lofter_dm.ads_paid_post_active_user_lifecycle_income_channel_di` | 4 | ADS |
| 329 | `lofter_dm.ads_high_security_user_di` | 4 | ADS |
| 330 | `lofter_dm.ads_growth_channel_dau_stratify_di` | 4 | ADS |
| 331 | `lofter_dm.ads_grain_hot_list_di` | 4 | ADS |
| 332 | `lofter_dm.ads_ec_trace_trade_di` | 4 | ADS |
| 333 | `lofter_dm.ads_ec_product_trade_di` | 4 | ADS |
| 334 | `lofter_dm.ads_creator_lighthouse_potential_di` | 4 | ADS |
| 335 | `lofter_dm.ads_ad_video_extend_user_group_di` | 4 | ADS |
| 336 | `lofter_dm.ads_ad_video_extend_core_user_group_di` | 4 | ADS |
| 337 | `lofter_dm.ads_act_paper_man_user_group_di` | 4 | ADS |
| 338 | `lofter_dm.ads_act_music_interaction_present_di` | 4 | ADS |
| 339 | `lofter_dm.ads_act_coupon_daily_user_group_dd` | 4 | ADS |
| 340 | `lofter_db_dump.ods_db_trade_mini_program_order_nd` | 4 | ODS |
| 341 | `lofter_db_dump.ods_db_trade_exchange_coupon_sku_nd` | 4 | ODS |
| 342 | `lofter_db_dump.ods_db_trade_coupon_card_order_nd` | 4 | ODS |
| 343 | `lofter_db_dump.ods_db_trade_coin_balance_log_nd` | 4 | ODS |
| 344 | `lofter_db_dump.ods_db_trade_blog_vip_order_nd` | 4 | ODS |
| 345 | `lofter_db_dump.ods_db_tag_joined_nd` | 4 | ODS |
| 346 | `lofter_db_dump.ods_db_recommend_post_trace_log_nd` | 4 | ODS |
| 347 | `lofter_db_dump.ods_db_pve_user_info_nd` | 4 | ODS |
| 348 | `lofter_db_dump.ods_db_pve_role_group_dialogue_nd` | 4 | ODS |
| 349 | `lofter_db_dump.ods_db_paper_man_user_dialogue_new_nd` | 4 | ODS |
| 350 | `lofter_db_dump.ods_db_lucky_boy_config_nd` | 4 | ODS |
| 351 | `lofter_db_dump.ods_db_grain_follower_nd` | 4 | ODS |
| 352 | `lofter_db_dump.ods_db_corp_out_user_nd` | 4 | ODS |
| 353 | `lofter_db_dump.ods_db_blog_misc_setting_nd` | 4 | ODS |
| 354 | `lofter_db_dump.ods_db_benefit_refund_nd` | 4 | ODS |
| 355 | `lofter_db_dump.ods_db_benefit_product_cmb_ip_relation_nd` | 4 | ODS |
| 356 | `lofter_db_dump.ods_db_benefit_product_attribute_nd` | 4 | ODS |
| 357 | `lofter_db_dump.ods_db_benefit_category_product_relation_nd` | 4 | ODS |
| 358 | `lofter_db_dump.ods_db_avatar_box_item_nd` | 4 | ODS |
| 359 | `lofter_db_dump.ods_db_act_lighthouse_user_v` | 4 | ODS |
| 360 | `lofter_db_dump.ods_db_act_activity_effect_base_config_nd` | 4 | ODS |
| 361 | `lofter_db_dump.db` | 4 | ODS |
| 362 | `lofter.zq_lofter_liangdan_black_tag` | 4 | ZQ |
| 363 | `lofter.ods_log_monitor_crab_suspect_di` | 4 | ODS |
| 364 | `lofter.ods_log_anti_risk_post_di` | 4 | ODS |
| 365 | `lofter.ods_log_anti_risk_message_di` | 4 | ODS |
| 366 | `lofter.ods_log_anti_risk_comment_di` | 4 | ODS |
| 367 | `lofter.ods_log_ad_linkup_ks_stat_di` | 4 | ODS |
| 368 | `lofter.ods_binlog_post_hot_di` | 4 | ODS |
| 369 | `lofter.ods_binlog_post_di` | 4 | ODS |
| 370 | `lofter.dws_user_post_talk_interaction_di` | 4 | DWS |
| 371 | `lofter.dws_user_post_other_interaction_di` | 4 | DWS |
| 372 | `lofter.dws_user_post_interaction_di` | 4 | DWS |
| 373 | `lofter.dws_par_user_appversion_dd` | 4 | DWS |
| 374 | `lofter.dws_par_creator_user_support_score_dd` | 4 | DWS |
| 375 | `lofter.dws_page_note_source_scene_di` | 4 | DWS |
| 376 | `lofter.dws_ip_consume_di` | 4 | DWS |
| 377 | `lofter.dws_gift_unlock_tag_interests_di` | 4 | DWS |
| 378 | `lofter.dws_gift_post_premium_post_score_di` | 4 | DWS |
| 379 | `lofter.dws_device_tag_interest_di` | 4 | DWS |
| 380 | `lofter.dws_device_ip_interest_di` | 4 | DWS |
| 381 | `lofter.dwd_risk_shuare_model_di` | 4 | DWD |
| 382 | `lofter.dwd_risk_brush_hot_suspect_post_rank_posts_di` | 4 | DWD |
| 383 | `lofter.dwd_risk_brush_hot_suspect_post_rank_ip_posts_di` | 4 | DWD |
| 384 | `lofter.dwd_rewardcenter_user_di` | 4 | DWD |
| 385 | `lofter.dwd_rec_post_review_di` | 4 | DWD |
| 386 | `lofter.dwd_pve_user_stamina_log_di` | 4 | DWD |
| 387 | `lofter.dwd_post_talk_publish_di` | 4 | DWD |
| 388 | `lofter.dwd_evt_webview_index_di` | 4 | DWD |
| 389 | `lofter.dwd_device_apk_install_dd` | 4 | DWD |
| 390 | `lofter.dwd_collection_detail_di` | 4 | DWD |
| 391 | `lofter.dwd_benefit_trade_order_product_dd` | 4 | DWD |
| 392 | `lofter.dwd_ad_content_unlock_sdk_di` | 4 | DWD |
| 393 | `lofter.dwb_par_lofter_music_user_label_di` | 4 | DWB |
| 394 | `lofter.dim_post_category_set_dd` | 4 | DIM |
| 395 | `lofter.ads_iad_lofter_info_flow_di` | 4 | ADS |
| 396 | `lofter_dm.ads_wmyzy_post_publish_di` | 3 | ADS |
| 397 | `lofter_dm.ads_wmyzy_post_di` | 3 | ADS |
| 398 | `lofter_dm.ads_video_post_index_dd` | 3 | ADS |
| 399 | `lofter_dm.ads_video_play_top_di` | 3 | ADS |
| 400 | `lofter_dm.ads_video_pay` | 3 | ADS |
| 401 | `lofter_dm.ads_video_native_hot_top_di` | 3 | ADS |
| 402 | `lofter_dm.ads_video_hot_top_di` | 3 | ADS |
| 403 | `lofter_dm.ads_video_external_link_hot_top_di` | 3 | ADS |
| 404 | `lofter_dm.ads_user_interaction_di` | 3 | ADS |
| 405 | `lofter_dm.ads_tag_potential_top_` | 3 | ADS |
| 406 | `lofter_dm.ads_tag_hot_top_` | 3 | ADS |
| 407 | `lofter_dm.ads_search_keyword_rec_reason_detail_di` | 3 | ADS |
| 408 | `lofter_dm.ads_risk_shuare_model_backend_di` | 3 | ADS |
| 409 | `lofter_dm.ads_risk_rec_audit_publish_monitor_di` | 3 | ADS |
| 410 | `lofter_dm.ads_rec_tag_wanre_post_di` | 3 | ADS |
| 411 | `lofter_dm.ads_rec_tag_rec_reason_detail_di` | 3 | ADS |
| 412 | `lofter_dm.ads_rec_content_rec_reason_detail_di` | 3 | ADS |
| 413 | `lofter_dm.ads_rec_collection_rec_reason_detail_di` | 3 | ADS |
| 414 | `lofter_dm.ads_rec_auto_column_premium_tag_di` | 3 | ADS |
| 415 | `lofter_dm.ads_question_invite_message_dd` | 3 | ADS |
| 416 | `lofter_dm.ads_pve_user_interview_import_di` | 3 | ADS |
| 417 | `lofter_dm.ads_pve_inactive_user_invite_di` | 3 | ADS |
| 418 | `lofter_dm.ads_pve_homepage_user_group_di` | 3 | ADS |
| 419 | `lofter_dm.ads_post_publish_di` | 3 | ADS |
| 420 | `lofter_dm.ads_post_paid_post_revenue_detail_di` | 3 | ADS |
| 421 | `lofter_dm.ads_post_paid_creator_revenue_detail_di` | 3 | ADS |
| 422 | `lofter_dm.ads_post_high_expose_di` | 3 | ADS |
| 423 | `lofter_dm.ads_paid_post_revenue_module_user_first_last_date_dd` | 3 | ADS |
| 424 | `lofter_dm.ads_paid_post_premium_scoring_metrics_detail_di` | 3 | ADS |
| 425 | `lofter_dm.ads_mbo_revenue_dd` | 3 | ADS |
| 426 | `lofter_dm.ads_lofter_tags_data_wd` | 3 | ADS |
| 427 | `lofter_dm.ads_lofter_tags_data_dd` | 3 | ADS |
| 428 | `lofter_dm.ads_ip_user_top_di` | 3 | ADS |
| 429 | `lofter_dm.ads_growth_content_monitor_di` | 3 | ADS |
| 430 | `lofter_dm.ads_growth_ad_period_cvr_di` | 3 | ADS |
| 431 | `lofter_dm.ads_grain_detail_dd` | 3 | ADS |
| 432 | `lofter_dm.ads_grain_batch_hot_list_di` | 3 | ADS |
| 433 | `lofter_dm.ads_gift_return_neg_protect_user_group_di` | 3 | ADS |
| 434 | `lofter_dm.ads_gift_post_high_positive_vote_blogs_di` | 3 | ADS |
| 435 | `lofter_dm.ads_ec_store_vip_user_effective_dd` | 3 | ADS |
| 436 | `lofter_dm.ads_creator_ecology_identity_rank_dd` | 3 | ADS |
| 437 | `lofter_dm.ads_creator_ecology_grow_advice_dd` | 3 | ADS |
| 438 | `lofter_dm.ads_creator_double_perspective_user_di` | 3 | ADS |
| 439 | `lofter_dm.ads_bookstore_hot_list_di` | 3 | ADS |
| 440 | `lofter_dm.ads_ad_waterfull_user_group_di` | 3 | ADS |
| 441 | `lofter_dm.ads_ad_user_group_di` | 3 | ADS |
| 442 | `lofter_dm.ads_ad_resource_group_di` | 3 | ADS |
| 443 | `lofter_dm.ads_ad_paid_post_warning_low_post_di` | 3 | ADS |
| 444 | `lofter_dm.ads_ad_content_revenue_post_di` | 3 | ADS |
| 445 | `lofter_dm.ads_ad_content_post_pool_di` | 3 | ADS |
| 446 | `lofter_dm.ads_act_showcase_user_group_di` | 3 | ADS |
| 447 | `lofter_dm.ads_act_pve_user_group_di` | 3 | ADS |
| 448 | `lofter_dm.ads_act_coupon_summer_holiday_user_group_dd` | 3 | ADS |
| 449 | `lofter_dm.ads_act_coupon_spring_user_group_dd` | 3 | ADS |
| 450 | `lofter_dm.ads_act_coupon_new_year_user_group_dd` | 3 | ADS |
| 451 | `lofter_db_dump.ods_db_video_post_nd` | 3 | ODS |
| 452 | `lofter_db_dump.ods_db_user_blog_account_nd` | 3 | ODS |
| 453 | `lofter_db_dump.ods_db_trade_user_black_home_nd` | 3 | ODS |
| 454 | `lofter_db_dump.ods_db_trade_reward_author_nd` | 3 | ODS |
| 455 | `lofter_db_dump.ods_db_trade_return_gift_type_nd` | 3 | ODS |
| 456 | `lofter_db_dump.ods_db_trade_grain_gift_order_nd` | 3 | ODS |
| 457 | `lofter_db_dump.ods_db_trade_commentator_reward_nd` | 3 | ODS |
| 458 | `lofter_db_dump.ods_db_trade_collection_gift_order_nd` | 3 | ODS |
| 459 | `lofter_db_dump.ods_db_trade_ad_unlock_user_setting_log_nd` | 3 | ODS |
| 460 | `lofter_db_dump.ods_db_store_rank_list_nd` | 3 | ODS |
| 461 | `lofter_db_dump.ods_db_rta_ad_post_pool_nd` | 3 | ODS |
| 462 | `lofter_db_dump.ods_db_risk_antispam_hist_task_nd` | 3 | ODS |
| 463 | `lofter_db_dump.ods_db_reward_user_score_log_nd` | 3 | ODS |
| 464 | `lofter_db_dump.ods_db_recommend_review_tag_nd` | 3 | ODS |
| 465 | `lofter_db_dump.ods_db_pve_user_grass_log_nd` | 3 | ODS |
| 466 | `lofter_db_dump.ods_db_pve_user_duplicate_dialogue_nd` | 3 | ODS |
| 467 | `lofter_db_dump.ods_db_profile_nd` | 3 | ODS |
| 468 | `lofter_db_dump.ods_db_post_pool_nd` | 3 | ODS |
| 469 | `lofter_db_dump.ods_db_post_fans_post_nd` | 3 | ODS |
| 470 | `lofter_db_dump.ods_db_pfb_contract_nd` | 3 | ODS |
| 471 | `lofter_db_dump.ods_db_paper_man_user_stamina_log_nd` | 3 | ODS |
| 472 | `lofter_db_dump.ods_db_paper_man_backend_reward_nd` | 3 | ODS |
| 473 | `lofter_db_dump.ods_db_paid_content_activity_nd` | 3 | ODS |
| 474 | `lofter_db_dump.ods_db_money_store_vip_split_sum_nd` | 3 | ODS |
| 475 | `lofter_db_dump.ods_db_media_post_import_nd` | 3 | ODS |
| 476 | `lofter_db_dump.ods_db_media_org_bind_user_nd` | 3 | ODS |
| 477 | `lofter_db_dump.ods_db_kol_promote_word_nd` | 3 | ODS |
| 478 | `lofter_db_dump.ods_db_kol_attribution_order_cost_nd` | 3 | ODS |
| 479 | `lofter_db_dump.ods_db_image_market_product_nd` | 3 | ODS |
| 480 | `lofter_db_dump.ods_db_exchange_task_nd` | 3 | ODS |
| 481 | `lofter_db_dump.ods_db_emote_dun_nd` | 3 | ODS |
| 482 | `lofter_db_dump.ods_db_derive_purchase_after_sale_order_nd` | 3 | ODS |
| 483 | `lofter_db_dump.ods_db_derive_purchase_after_sale_detail_nd` | 3 | ODS |
| 484 | `lofter_db_dump.ods_db_derive_deliver_order_nd` | 3 | ODS |
| 485 | `lofter_db_dump.ods_db_derive_deliver_order_detail_nd` | 3 | ODS |
| 486 | `lofter_db_dump.ods_db_connect_phone_account_nd` | 3 | ODS |
| 487 | `lofter_db_dump.ods_db_blacklist_user_nd` | 3 | ODS |
| 488 | `lofter_db_dump.ods_db_benefit_order_product_delivery_nd` | 3 | ODS |
| 489 | `lofter_db_dump.ods_db_benefit_coupon_user_nd` | 3 | ODS |
| 490 | `lofter_db_dump.ods_db_benefit_card_pool_nd` | 3 | ODS |
| 491 | `lofter_db_dump.ods_db_act_lighthouse_user_nd` | 3 | ODS |
| 492 | `lofter.zq_lofter_searchbangdan_black_post_tag` | 3 | ZQ |
| 493 | `lofter.zq_lofter_fanlaji_black_keyword` | 3 | ZQ |
| 494 | `lofter.stg_post_hot_static_out_` | 3 | STG |
| 495 | `lofter.post_portrait_level` | 3 | DWD |
| 496 | `lofter.ods_risk_mark_return_gift_nd` | 3 | ODS |
| 497 | `lofter.ods_risk_limit_dd` | 3 | ODS |
| 498 | `lofter.ods_mda_miniprogram_di` | 3 | ODS |
| 499 | `lofter.ods_log_anti_risk_shuare_di` | 3 | ODS |
| 500 | `lofter.ods_log_ad_dsp_di` | 3 | ODS |
| 501 | `lofter.ods_log_ab_platform_sdk_log_di` | 3 | ODS |
| 502 | `lofter.ods_binlog_user_following_di` | 3 | ODS |
| 503 | `lofter.lofter_user_portrait_to_game_staging_primary_dd` | 3 | Other |
| 504 | `lofter.lofter_user_portrait_to_game_staging_incr_di` | 3 | Other |
| 505 | `lofter.lofter_user_portrait_to_game_staging_attr_dd` | 3 | Other |
| 506 | `lofter.dws_user_life_circle_index_dd` | 3 | DWS |
| 507 | `lofter.dws_tag_di` | 3 | DWS |
| 508 | `lofter.dws_pve_roles_amount_info_dd` | 3 | DWS |
| 509 | `lofter.dws_post_talk_question_interaction_dd` | 3 | DWS |
| 510 | `lofter.dws_post_pay_di` | 3 | DWS |
| 511 | `lofter.dws_post_misc_dd` | 3 | DWS |
| 512 | `lofter.dws_par_user_discuss_di` | 3 | DWS |
| 513 | `lofter.dws_par_user_core_staging_dd` | 3 | DWS |
| 514 | `lofter.dws_par_user_` | 3 | DWS |
| 515 | `lofter.dws_par_creator_gift_level_scoring_detail_dd` | 3 | DWS |
| 516 | `lofter.dws_ip_life_cycle_dd` | 3 | DWS |
| 517 | `lofter.dws_growth_device_ip_di` | 3 | DWS |
| 518 | `lofter.dws_creator_browse_users_di` | 3 | DWS |
| 519 | `lofter.dws_collection_scene_agg_bm_di` | 3 | DWS |
| 520 | `lofter.dwd_user_low_active_di` | 3 | DWD |
| 521 | `lofter.dwd_subject_bubble_action_di` | 3 | DWD |
| 522 | `lofter.dwd_risk_shuare_post_model_di` | 3 | DWD |
| 523 | `lofter.dwd_risk_offsite_induction_return_gift_di` | 3 | DWD |
| 524 | `lofter.dwd_risk_brush_hot_suspect_users_di` | 3 | DWD |
| 525 | `lofter.dwd_risk_brush_hot_suspect_post_rank_users_di` | 3 | DWD |
| 526 | `lofter.dwd_risk_brush_hot_suspect_post_rank_ip_users_di` | 3 | DWD |
| 527 | `lofter.dwd_rewardcenter_visit_di` | 3 | DWD |
| 528 | `lofter.dwd_pve_user_timed_stamina_log_di` | 3 | DWD |
| 529 | `lofter.dwd_pve_user_interview_return_di` | 3 | DWD |
| 530 | `lofter.dwd_pve_user_interview_new_di` | 3 | DWD |
| 531 | `lofter.dwd_pve_user_chats_return_di` | 3 | DWD |
| 532 | `lofter.dwd_pve_user_chats_new_di` | 3 | DWD |
| 533 | `lofter.dwd_post_talk_expose_di` | 3 | DWD |
| 534 | `lofter.dwd_post_group_post_list_di` | 3 | DWD |
| 535 | `lofter.dwd_post_audit_di` | 3 | DWD |
| 536 | `lofter.dwd_post_audio_di` | 3 | DWD |
| 537 | `lofter.dwd_paid_subscribe_silent_user_activate_di` | 3 | DWD |
| 538 | `lofter.dwd_paid_subscribe_device_cpa_deduplicate_di` | 3 | DWD |
| 539 | `lofter.dwd_membership_vip_post_browse_di` | 3 | DWD |
| 540 | `lofter.dwd_gift_post_order_dd` | 3 | DWD |
| 541 | `lofter.dwd_emote_dun_ti_record_di` | 3 | DWD |
| 542 | `lofter.dwd_ec_add_cart_di` | 3 | DWD |
| 543 | `lofter.dwd_device_collect_apk_di` | 3 | DWD |
| 544 | `lofter.dwd_coupon_order_di` | 3 | DWD |
| 545 | `lofter.dwd_blog_nickname_sensitive_word_di` | 3 | DWD |
| 546 | `lofter.dwd_blog_intro_sensitive_word_di` | 3 | DWD |
| 547 | `lofter.dwd_benefit_product_trade_di` | 3 | DWD |
| 548 | `lofter.dwd_ad_growth_return_di` | 3 | DWD |
| 549 | `lofter.dwd_ad_growth_order_activate_di` | 3 | DWD |
| 550 | `lofter.dwd_ad_growth_new_di` | 3 | DWD |
| 551 | `lofter.dwd_activity_action_di` | 3 | DWD |
| 552 | `lofter.dwb_par_lofter_tag_wd` | 3 | DWB |
| 553 | `lofter.dim_membership_collection_dd` | 3 | DIM |
| 554 | `lofter.dim_ip_extend_dd` | 3 | DIM |
| 555 | `lofter.bridge_collection_post_dd` | 3 | Bridge |
| 556 | `lofter.bridge_calendar_date_dd` | 3 | Bridge |
| 557 | `lofter.bridge_ad_content_collection_dd` | 3 | Bridge |
| 558 | `lofter_dm.ads_vc_user_tag_metric_level` | 2 | ADS |
| 559 | `lofter_dm.ads_user_touch_push_cnt_di` | 2 | ADS |
| 560 | `lofter_dm.ads_user_device_type_` | 2 | ADS |
| 561 | `lofter_dm.ads_tag_protection_exempt_di` | 2 | ADS |
| 562 | `lofter_dm.ads_tag_fetch_post_di` | 2 | ADS |
| 563 | `lofter_dm.ads_tag_fetch_post_dd` | 2 | ADS |
| 564 | `lofter_dm.ads_tag_fetch_blog_di` | 2 | ADS |
| 565 | `lofter_dm.ads_tag_fetch_blog_dd` | 2 | ADS |
| 566 | `lofter_dm.ads_tag_blog_rank_di` | 2 | ADS |
| 567 | `lofter_dm.ads_risk_tag_post_rank_brush_hot_suspect_users_review_di` | 2 | ADS |
| 568 | `lofter_dm.ads_risk_tag_post_rank_brush_hot_deduction_di` | 2 | ADS |
| 569 | `lofter_dm.ads_risk_shuare_warn_blogs_di` | 2 | ADS |
| 570 | `lofter_dm.ads_risk_post_ai_suspect_di` | 2 | ADS |
| 571 | `lofter_dm.ads_risk_induction_warn_posts_di` | 2 | ADS |
| 572 | `lofter_dm.ads_risk_induction_warn_blogs_di` | 2 | ADS |
| 573 | `lofter_dm.ads_risk_induction_shuare_warn_di` | 2 | ADS |
| 574 | `lofter_dm.ads_risk_induction_block_blogs_unlock_di` | 2 | ADS |
| 575 | `lofter_dm.ads_rec_question_rec_reason_di` | 2 | ADS |
| 576 | `lofter_dm.ads_rec_gift_user_stratify_di` | 2 | ADS |
| 577 | `lofter_dm.ads_rec_content_rec_reason_di` | 2 | ADS |
| 578 | `lofter_dm.ads_rec_collection_rec_reason_di` | 2 | ADS |
| 579 | `lofter_dm.ads_push_rec_content_candidates_di` | 2 | ADS |
| 580 | `lofter_dm.ads_post_tag_metric_level` | 2 | ADS |
| 581 | `lofter_dm.ads_post_premium_monitor_di` | 2 | ADS |
| 582 | `lofter_dm.ads_post_paid_monitor_di` | 2 | ADS |
| 583 | `lofter_dm.ads_post_highlight_comment_top_di` | 2 | ADS |
| 584 | `lofter_dm.ads_post_highlight_comment_dd` | 2 | ADS |
| 585 | `lofter_dm.ads_post_general_` | 2 | ADS |
| 586 | `lofter_dm.ads_post_comment_type_dd` | 2 | ADS |
| 587 | `lofter_dm.ads_post_category_di` | 2 | ADS |
| 588 | `lofter_dm.ads_par_user_favorite_tag_top_dd` | 2 | ADS |
| 589 | `lofter_dm.ads_paid_post_return_gift_feed_back_dd` | 2 | ADS |
| 590 | `lofter_dm.ads_paid_post_low_quality_post_di` | 2 | ADS |
| 591 | `lofter_dm.ads_paid_post_level_scoring_dd` | 2 | ADS |
| 592 | `lofter_dm.ads_paid_post_blog_circle_protected_di` | 2 | ADS |
| 593 | `lofter_dm.ads_paid_post_ad_unlock_users_di` | 2 | ADS |
| 594 | `lofter_dm.ads_membership_post_revenue_di` | 2 | ADS |
| 595 | `lofter_dm.ads_membership_finance_revenue_detail_di` | 2 | ADS |
| 596 | `lofter_dm.ads_maisui_advertisers_detail_report_di` | 2 | ADS |
| 597 | `lofter_dm.ads_lofter_tags_data_bitmap_wd` | 2 | ADS |
| 598 | `lofter_dm.ads_lofter_tags_data_bitmap_dd` | 2 | ADS |
| 599 | `lofter_dm.ads_ip_yiyou_traffic_limit_creator_di` | 2 | ADS |
| 600 | `lofter_dm.ads_ip_yiyou_high_net_value_user_di` | 2 | ADS |
| 601 | `lofter_dm.ads_im_user_match_di` | 2 | ADS |
| 602 | `lofter_dm.ads_growth_act_password_di` | 2 | ADS |
| 603 | `lofter_dm.ads_gift_ecology_rectify_limit_user_di` | 2 | ADS |
| 604 | `lofter_dm.ads_gift_ecology_rectify_limit_post_di` | 2 | ADS |
| 605 | `lofter_dm.ads_gift_commentator_task_post_pool_di` | 2 | ADS |
| 606 | `lofter_dm.ads_gift_commentator_post_pool_di` | 2 | ADS |
| 607 | `lofter_dm.ads_evt_paid_post_quality_dd` | 2 | ADS |
| 608 | `lofter_dm.ads_evt_content_supply_dd` | 2 | ADS |
| 609 | `lofter_dm.ads_ec_paid_subscribe_actpwd_funnel_di` | 2 | ADS |
| 610 | `lofter_dm.ads_ec_book_store_growth_user_di` | 2 | ADS |
| 611 | `lofter_dm.ads_device_platform_v` | 2 | ADS |
| 612 | `lofter_dm.ads_device_platform_retain_di` | 2 | ADS |
| 613 | `lofter_dm.ads_device_appchannel_di` | 2 | ADS |
| 614 | `lofter_dm.ads_device_active_layer_di` | 2 | ADS |
| 615 | `lofter_dm.ads_creator_vip_notice_di` | 2 | ADS |
| 616 | `lofter_dm.ads_creator_vip_fans_open_entry_dd` | 2 | ADS |
| 617 | `lofter_dm.ads_creator_private_zone_induction_di` | 2 | ADS |
| 618 | `lofter_dm.ads_creator_private_zone_conversion_di` | 2 | ADS |
| 619 | `lofter_dm.ads_creator_premium_detail_di` | 2 | ADS |
| 620 | `lofter_dm.ads_creator_gift_tag_good_examples_di` | 2 | ADS |
| 621 | `lofter_dm.ads_creator_gift_open_entry_dd` | 2 | ADS |
| 622 | `lofter_dm.ads_creator_ecology_lucky_boy_dd` | 2 | ADS |
| 623 | `lofter_dm.ads_creator_ecology_fans_favorite_wd` | 2 | ADS |
| 624 | `lofter_dm.ads_creator_ecology_achievement_dd` | 2 | ADS |
| 625 | `lofter_dm.ads_creator_benchmark_detail_di` | 2 | ADS |
| 626 | `lofter_dm.ads_collection_search_hot_list_di` | 2 | ADS |
| 627 | `lofter_dm.ads_blog_general_di` | 2 | ADS |
| 628 | `lofter_dm.ads_anti_spam_idle_send_forbidden_blog_nd` | 2 | ADS |
| 629 | `lofter_dm.ads_ad_revenue` | 2 | ADS |
| 630 | `lofter_dm.ads_ad_resource_sdk_post_di` | 2 | ADS |
| 631 | `lofter_dm.ads_ad_resource_group_blog_revenue_di` | 2 | ADS |
| 632 | `lofter_dm.ads_ad_resource_cvr_di` | 2 | ADS |
| 633 | `lofter_dm.ads_ad_resource_blog_revenue_di` | 2 | ADS |
| 634 | `lofter_dm.ads_act_signup_users_dd` | 2 | ADS |
| 635 | `lofter_dm.ads_act_lottery_path_di` | 2 | ADS |
| 636 | `lofter_dm.ads_abnormal_behavior_user_overall_stats_di` | 2 | ADS |
| 637 | `lofter_dm.ads_abnormal_behavior_user_detail_stats_di` | 2 | ADS |
| 638 | `lofter_db_dump.ods_db_vote_info_nd` | 2 | ODS |
| 639 | `lofter_db_dump.ods_db_verify_phone_account_nd` | 2 | ODS |
| 640 | `lofter_db_dump.ods_db_user_subscribe_post_nd` | 2 | ODS |
| 641 | `lofter_db_dump.ods_db_user_statistic_nd` | 2 | ODS |
| 642 | `lofter_db_dump.ods_db_user_showcase_config_nd` | 2 | ODS |
| 643 | `lofter_db_dump.ods_db_user_guide_ip_nd` | 2 | ODS |
| 644 | `lofter_db_dump.ods_db_trade_user_bind_nd` | 2 | ODS |
| 645 | `lofter_db_dump.ods_db_trade_post_revenue_nd` | 2 | ODS |
| 646 | `lofter_db_dump.ods_db_trade_order_nd` | 2 | ODS |
| 647 | `lofter_db_dump.ods_db_trade_gift_user_setting_nd` | 2 | ODS |
| 648 | `lofter_db_dump.ods_db_trade_gift_order_nd` | 2 | ODS |
| 649 | `lofter_db_dump.ods_db_trade_gift_money_balance_log_nd` | 2 | ODS |
| 650 | `lofter_db_dump.ods_db_trade_fans_private_account_nd` | 2 | ODS |
| 651 | `lofter_db_dump.ods_db_trade_ad_unlock_user_setting_nd` | 2 | ODS |
| 652 | `lofter_db_dump.ods_db_statis_point_nd` | 2 | ODS |
| 653 | `lofter_db_dump.ods_db_sign_authenticate_nd` | 2 | ODS |
| 654 | `lofter_db_dump.ods_db_recommend_user_mark_nd` | 2 | ODS |
| 655 | `lofter_db_dump.ods_db_pve_user_timed_stamina_log_nd` | 2 | ODS |
| 656 | `lofter_db_dump.ods_db_pve_user_daily_gift_nd` | 2 | ODS |
| 657 | `lofter_db_dump.ods_db_pve_user_bind_nd` | 2 | ODS |
| 658 | `lofter_db_dump.ods_db_pve_ugc_role_record_nd` | 2 | ODS |
| 659 | `lofter_db_dump.ods_db_push_push_task_nd` | 2 | ODS |
| 660 | `lofter_db_dump.ods_db_post_view_count_nd` | 2 | ODS |
| 661 | `lofter_db_dump.ods_db_post_collection_view_count_nd` | 2 | ODS |
| 662 | `lofter_db_dump.ods_db_media_video_fetch_nd` | 2 | ODS |
| 663 | `lofter_db_dump.ods_db_live_gift_income_nd` | 2 | ODS |
| 664 | `lofter_db_dump.ods_db_invalid_nos_image_nd` | 2 | ODS |
| 665 | `lofter_db_dump.ods_db_infringe_post_nd` | 2 | ODS |
| 666 | `lofter_db_dump.ods_db_image_market_loot_box_nd` | 2 | ODS |
| 667 | `lofter_db_dump.ods_db_grain_post_nd` | 2 | ODS |
| 668 | `lofter_db_dump.ods_db_grain_officer_declare_nd` | 2 | ODS |
| 669 | `lofter_db_dump.ods_db_emote_tag_resource_nd` | 2 | ODS |
| 670 | `lofter_db_dump.ods_db_dressing_tag_resource_nd` | 2 | ODS |
| 671 | `lofter_db_dump.ods_db_dressing_suit_reserve_nd` | 2 | ODS |
| 672 | `lofter_db_dump.ods_db_derive_sku_nd` | 2 | ODS |
| 673 | `lofter_db_dump.ods_db_deity_recommend_nd` | 2 | ODS |
| 674 | `lofter_db_dump.ods_db_brush_hot_user_nd` | 2 | ODS |
| 675 | `lofter_db_dump.ods_db_blog_store_vip_nd` | 2 | ODS |
| 676 | `lofter_db_dump.ods_db_blog_pay_creator_warn_nd` | 2 | ODS |
| 677 | `lofter_db_dump.ods_db_blog_fans_private_nd` | 2 | ODS |
| 678 | `lofter_db_dump.ods_db_birthday_party_nd` | 2 | ODS |
| 679 | `lofter_db_dump.ods_db_benefit_coupon_product_nd` | 2 | ODS |
| 680 | `lofter_db_dump.ods_db_benefit_card_nd` | 2 | ODS |
| 681 | `lofter_db_dump.ods_db_benefit_ad_trace_config_nd` | 2 | ODS |
| 682 | `lofter_db_dump.ods_db_avatar_box_user_nd` | 2 | ODS |
| 683 | `lofter_db_dump.ods_db_ad_position_nd` | 2 | ODS |
| 684 | `lofter_db_dump.ods_db_act_tag_ship_user_reward_nd` | 2 | ODS |
| 685 | `lofter_db_dump.ods_db_act_reward_user_pay_info_nd` | 2 | ODS |
| 686 | `lofter_db_dump.ods_db_act_backend_message_push_record_nd` | 2 | ODS |
| 687 | `lofter_db_dump.ods_db_act_adspace_snapshot_nd` | 2 | ODS |
| 688 | `lofter_db_dump.ods_db_account_nd` | 2 | ODS |
| 689 | `lofter_db_dump.ods_db_ab_metric_dim_nd` | 2 | ODS |
| 690 | `lofter_db_dump.ods_db_ab_metric_detail_nd` | 2 | ODS |
| 691 | `lofter.zq_lofter_yichanguser_blacktext_zeppelin` | 2 | ZQ |
| 692 | `lofter.zq_lofter_shuaretag_white` | 2 | ZQ |
| 693 | `lofter.zq_lofter_recommend_white_user` | 2 | ZQ |
| 694 | `lofter.user_portrait_status_v` | 2 | DWD |
| 695 | `lofter.table_b` | 2 | Other |
| 696 | `lofter.table_a` | 2 | Other |
| 697 | `lofter.stg_post_interaction_out_` | 2 | STG |
| 698 | `lofter.stg_post_interaction_in_` | 2 | STG |
| 699 | `lofter.stg_par_creator_interaction_out_` | 2 | STG |
| 700 | `lofter.stg_par_creator_interaction_in_` | 2 | STG |
| 701 | `lofter.source_table` | 2 | Other |
| 702 | `lofter.push` | 2 | Other |
| 703 | `lofter.output` | 2 | Other |
| 704 | `lofter.ods_test_user_nd` | 2 | ODS |
| 705 | `lofter.ods_rec_content_understand_log_di` | 2 | ODS |
| 706 | `lofter.ods_overmind_issue_field_value` | 2 | ODS |
| 707 | `lofter.ods_overmind_issue_field_option` | 2 | ODS |
| 708 | `lofter.ods_overmind_field_config` | 2 | ODS |
| 709 | `lofter.ods_mda_bookstore_miniprogram_di` | 2 | ODS |
| 710 | `lofter.ods_mda_app_raw_di` | 2 | ODS |
| 711 | `lofter.ods_log_subscribecollection_di` | 2 | ODS |
| 712 | `lofter.ods_log_risk_slider_di` | 2 | ODS |
| 713 | `lofter.ods_log_publishpost_di` | 2 | ODS |
| 714 | `lofter.ods_log_praise_di` | 2 | ODS |
| 715 | `lofter.ods_log_post_change_di` | 2 | ODS |
| 716 | `lofter.ods_log_content_incantation_di` | 2 | ODS |
| 717 | `lofter.ods_log_commend_di` | 2 | ODS |
| 718 | `lofter.ods_log_artificial_import_tag_di` | 2 | ODS |
| 719 | `lofter.ods_log_antispam_brush_dispose_di` | 2 | ODS |
| 720 | `lofter.ods_log_anti_spam_copy_video_di` | 2 | ODS |
| 721 | `lofter.ods_log_anti_risk_similarity_post_di` | 2 | ODS |
| 722 | `lofter.ods_log_anti_risk_similarity_message_di` | 2 | ODS |
| 723 | `lofter.ods_jira_sprint` | 2 | ODS |
| 724 | `lofter.ods_binlog_photo_post_di` | 2 | ODS |
| 725 | `lofter.mda_common_deviceid_nd` | 2 | Other |
| 726 | `lofter.lofter_user_portrait_to_game_di` | 2 | Other |
| 727 | `lofter.dws_user_tag_interaction_di` | 2 | DWS |
| 728 | `lofter.dws_user_first_interaction_dd` | 2 | DWS |
| 729 | `lofter.dws_tag_supply_di` | 2 | DWS |
| 730 | `lofter.dws_tag_interaction_di` | 2 | DWS |
| 731 | `lofter.dws_tag_fetch_di` | 2 | DWS |
| 732 | `lofter.dws_tag_consume_di` | 2 | DWS |
| 733 | `lofter.dws_post_talk_user_crowd_dd` | 2 | DWS |
| 734 | `lofter.dws_post_talk_answer_interaction_dd` | 2 | DWS |
| 735 | `lofter.dws_par_user_misc_di` | 2 | DWS |
| 736 | `lofter.dws_par_user_home_visit_di` | 2 | DWS |
| 737 | `lofter.dws_par_creator_interaction_di` | 2 | DWS |
| 738 | `lofter.dws_page_source_scene_di` | 2 | DWS |
| 739 | `lofter.dws_page_scene_di` | 2 | DWS |
| 740 | `lofter.dws_page_note_di` | 2 | DWS |
| 741 | `lofter.dws_miniprogram_post_order_dd` | 2 | DWS |
| 742 | `lofter.dws_ip_interaction_di` | 2 | DWS |
| 743 | `lofter.dws_ip_di` | 2 | DWS |
| 744 | `lofter.dws_index_user_status_di` | 2 | DWS |
| 745 | `lofter.dws_index_user_status_dd` | 2 | DWS |
| 746 | `lofter.dws_index_tag_status_dd` | 2 | DWS |
| 747 | `lofter.dws_index_tag_action_di` | 2 | DWS |
| 748 | `lofter.dws_index_tag_action_dd` | 2 | DWS |
| 749 | `lofter.dws_index_post_status_dd` | 2 | DWS |
| 750 | `lofter.dws_creator_gift_users_di` | 2 | DWS |
| 751 | `lofter.dws_collection_scene_bm_di` | 2 | DWS |
| 752 | `lofter.dws_collection_revisit_di` | 2 | DWS |
| 753 | `lofter.dws_act_tag_big_event_di` | 2 | DWS |
| 754 | `lofter.dws_act_card_cvr_di` | 2 | DWS |
| 755 | `lofter.dws_ab_platform_user_metric_di` | 2 | DWS |
| 756 | `lofter.dws_ab_platform_rewardcenter_metric_di` | 2 | DWS |
| 757 | `lofter.dws_ab_platform_return_user_metric_di` | 2 | DWS |
| 758 | `lofter.dws_ab_platform_pve_metric_di` | 2 | DWS |
| 759 | `lofter.dws_ab_platform_paycontent_scene_metric_di` | 2 | DWS |
| 760 | `lofter.dws_ab_platform_paycontent_membership_metric_di` | 2 | DWS |
| 761 | `lofter.dws_ab_platform_new_user_metric_di` | 2 | DWS |
| 762 | `lofter.dws_ab_platform_experiment_metric_di` | 2 | DWS |
| 763 | `lofter.dws_ab_platform_device_metric_di` | 2 | DWS |
| 764 | `lofter.dws_ab_platform_ad_metric_di` | 2 | DWS |
| 765 | `lofter.dws_ab_platform_active_user_metric_di` | 2 | DWS |
| 766 | `lofter.dwd_vote_record_di` | 2 | DWD |
| 767 | `lofter.dwd_user_white_list_dd` | 2 | DWD |
| 768 | `lofter.dwd_user_black_hit_rule_di` | 2 | DWD |
| 769 | `lofter.dwd_suspect_shuare_model_post_rank_di` | 2 | DWD |
| 770 | `lofter.dwd_risk_user_level_dd` | 2 | DWD |
| 771 | `lofter.dwd_pve_user_sweet_stamina_log_di` | 2 | DWD |
| 772 | `lofter.dwd_pve_user_props_stamina_log_di` | 2 | DWD |
| 773 | `lofter.dwd_pve_user_grass_stamina_log_di` | 2 | DWD |
| 774 | `lofter.dwd_pve_user_chats_group_info_di` | 2 | DWD |
| 775 | `lofter.dwd_post_talk_share_di` | 2 | DWD |
| 776 | `lofter.dwd_post_talk_response_di` | 2 | DWD |
| 777 | `lofter.dwd_post_talk_hot_di` | 2 | DWD |
| 778 | `lofter.dwd_post_talk_discuss_score_di` | 2 | DWD |
| 779 | `lofter.dwd_paper_man_reserve_di` | 2 | DWD |
| 780 | `lofter.dwd_operate_log` | 2 | DWD |
| 781 | `lofter.dwd_miniprogram_order_di` | 2 | DWD |
| 782 | `lofter.dwd_lucky_boy_record_di` | 2 | DWD |
| 783 | `lofter.dwd_issue_with_status_change_for_lofter` | 2 | DWD |
| 784 | `lofter.dwd_issue_data` | 2 | DWD |
| 785 | `lofter.dwd_evt_avatar_box_access_di` | 2 | DWD |
| 786 | `lofter.dwd_ec_trace_product_view_di` | 2 | DWD |
| 787 | `lofter.dwd_ec_trace_product_expose_di` | 2 | DWD |
| 788 | `lofter.dwd_ec_product_order_di` | 2 | DWD |
| 789 | `lofter.dwd_ec_product_expose_di` | 2 | DWD |
| 790 | `lofter.dwd_ec_derivate_revenue_di` | 2 | DWD |
| 791 | `lofter.dwd_ec_derivate_refund_di` | 2 | DWD |
| 792 | `lofter.dwd_ec_derivate_ipseries_category_revenue_di` | 2 | DWD |
| 793 | `lofter.dwd_ec_derivate_ipseries_category_refund_di` | 2 | DWD |
| 794 | `lofter.dwd_ec_derivate_gmv_di` | 2 | DWD |
| 795 | `lofter.dwd_device_mapping_detail_di` | 2 | DWD |
| 796 | `lofter.dwd_beginner_guide_page_events_di` | 2 | DWD |
| 797 | `lofter.dwd_antispam_copy_and_callback_di` | 2 | DWD |
| 798 | `lofter.dwd_ad_resource_monitor_close_di` | 2 | DWD |
| 799 | `lofter.dwd_ad_dsp_win_fill_di` | 2 | DWD |
| 800 | `lofter.dwd_ad_amount_per_user_di` | 2 | DWD |
| 801 | `lofter.dwd_act_tag_big_event_detail_di` | 2 | DWD |
| 802 | `lofter.dim_test` | 2 | DIM |
| 803 | `lofter.dim_gift_dd` | 2 | DIM |
| 804 | `lofter.dim_c` | 2 | DIM |
| 805 | `lofter.dim_benefit_sku` | 2 | DIM |
| 806 | `lofter.dim_benefit_product_category` | 2 | DIM |
| 807 | `lofter.bridge_exp_metric_dd` | 2 | Bridge |
| 808 | `lofter_dm.table` | 1 | ADS |
| 809 | `lofter_dm.output_table` | 1 | ADS |
| 810 | `lofter_dm.dwb_user_nega_feed_stat_di` | 1 | ADS |
| 811 | `lofter_dm.dwb_rec_pool_blog_list_di` | 1 | ADS |
| 812 | `lofter_dm.dwb_post_nega_feed_stat_di` | 1 | ADS |
| 813 | `lofter_dm.dwb_par_user_info_nd` | 1 | ADS |
| 814 | `lofter_dm.dwb_hubble_app_general_di` | 1 | ADS |
| 815 | `lofter_dm.dwb_device_new_trace_source` | 1 | ADS |
| 816 | `lofter_dm.dwb_device_callback_trace_source` | 1 | ADS |
| 817 | `lofter_dm.ads_x_search_result_report_di` | 1 | ADS |
| 818 | `lofter_dm.ads_wmyzy_post_monitor_di` | 1 | ADS |
| 819 | `lofter_dm.ads_wmyzy_post_level` | 1 | ADS |
| 820 | `lofter_dm.ads_wgt_tag_post_dd` | 1 | ADS |
| 821 | `lofter_dm.ads_week_report_revenue_total_report_di` | 1 | ADS |
| 822 | `lofter_dm.ads_week_report_revenue_detail_report_di` | 1 | ADS |
| 823 | `lofter_dm.ads_week_report_dau_total_report_di` | 1 | ADS |
| 824 | `lofter_dm.ads_week_report_dau_detail_report_di` | 1 | ADS |
| 825 | `lofter_dm.ads_webview_pageurl_mpid_di` | 1 | ADS |
| 826 | `lofter_dm.ads_webview_pageurl_di` | 1 | ADS |
| 827 | `lofter_dm.ads_video_tag_hot_top_di` | 1 | ADS |
| 828 | `lofter_dm.ads_video_source_stat_di` | 1 | ADS |
| 829 | `lofter_dm.ads_video_source_post_stat_di` | 1 | ADS |
| 830 | `lofter_dm.ads_video_scene_next_day_play_di` | 1 | ADS |
| 831 | `lofter_dm.ads_video_scene_di` | 1 | ADS |
| 832 | `lofter_dm.ads_video_retain_realplay_di` | 1 | ADS |
| 833 | `lofter_dm.ads_video_realplay_di` | 1 | ADS |
| 834 | `lofter_dm.ads_video_post_dd` | 1 | ADS |
| 835 | `lofter_dm.ads_video_post_daily_stat_report_di` | 1 | ADS |
| 836 | `lofter_dm.ads_video_ip_stats_consume_report_di` | 1 | ADS |
| 837 | `lofter_dm.ads_video_ip_publish_stats_report_di` | 1 | ADS |
| 838 | `lofter_dm.ads_video_grab_strategy_dd` | 1 | ADS |
| 839 | `lofter_dm.ads_video_di` | 1 | ADS |
| 840 | `lofter_dm.ads_video_blog_bind_mi` | 1 | ADS |
| 841 | `lofter_dm.ads_video_blog_bind_di` | 1 | ADS |
| 842 | `lofter_dm.ads_video_blog_bind_dd` | 1 | ADS |
| 843 | `lofter_dm.ads_video_audit_di` | 1 | ADS |
| 844 | `lofter_dm.ads_vc_weekly_data_report_di` | 1 | ADS |
| 845 | `lofter_dm.ads_vc_user_tag_group_level` | 1 | ADS |
| 846 | `lofter_dm.ads_userfolder_type_di` | 1 | ADS |
| 847 | `lofter_dm.ads_userfolder_subscribe_top_di` | 1 | ADS |
| 848 | `lofter_dm.ads_user_vertical_category_summary_report_di` | 1 | ADS |
| 849 | `lofter_dm.ads_user_vertical_category_ip_retain_report_di` | 1 | ADS |
| 850 | `lofter_dm.ads_user_tag_rec_post_dd` | 1 | ADS |
| 851 | `lofter_dm.ads_user_new_category_ip_report_di` | 1 | ADS |
| 852 | `lofter_dm.ads_user_new_category_consume_report_di` | 1 | ADS |
| 853 | `lofter_dm.ads_user_login_by_one_key_di` | 1 | ADS |
| 854 | `lofter_dm.ads_user_life_cycle_uv_dd` | 1 | ADS |
| 855 | `lofter_dm.ads_user_life_cycle_change_pay_uv_dd` | 1 | ADS |
| 856 | `lofter_dm.ads_user_life_cycle_change_monitor_dd` | 1 | ADS |
| 857 | `lofter_dm.ads_user_interaction_v` | 1 | ADS |
| 858 | `lofter_dm.ads_user_interaction_level_monitor_di` | 1 | ADS |
| 859 | `lofter_dm.ads_user_hot_behavior_dd` | 1 | ADS |
| 860 | `lofter_dm.ads_user_grade_post_stat_di` | 1 | ADS |
| 861 | `lofter_dm.ads_user_experience_report_di` | 1 | ADS |
| 862 | `lofter_dm.ads_user_data_center_wd` | 1 | ADS |
| 863 | `lofter_dm.ads_user_data_center_dd` | 1 | ADS |
| 864 | `lofter_dm.ads_user_collection_browse_di` | 1 | ADS |
| 865 | `lofter_dm.ads_user_black_low_quality_dd` | 1 | ADS |
| 866 | `lofter_dm.ads_unlock_data_retain_report_di` | 1 | ADS |
| 867 | `lofter_dm.ads_unlock_data_report_di` | 1 | ADS |
| 868 | `lofter_dm.ads_unlock_data_cpmpare_report_di` | 1 | ADS |
| 869 | `lofter_dm.ads_ug_new_return_device_report_di` | 1 | ADS |
| 870 | `lofter_dm.ads_ug_new_return_device_active_report_di` | 1 | ADS |
| 871 | `lofter_dm.ads_ue_user_order_di` | 1 | ADS |
| 872 | `lofter_dm.ads_ue_user_base_dd` | 1 | ADS |
| 873 | `lofter_dm.ads_trade_product_type_daily_stats_report_di` | 1 | ADS |
| 874 | `lofter_dm.ads_tp_user_detail_report_di` | 1 | ADS |
| 875 | `lofter_dm.ads_tp_total_indicator_report_di` | 1 | ADS |
| 876 | `lofter_dm.ads_tp_creater_report_di` | 1 | ADS |
| 877 | `lofter_dm.ads_theater_user_retain_report_di` | 1 | ADS |
| 878 | `lofter_dm.ads_theater_main_data_report_di` | 1 | ADS |
| 879 | `lofter_dm.ads_tag_supply_consume_interaction_di` | 1 | ADS |
| 880 | `lofter_dm.ads_tag_protected_report_di` | 1 | ADS |
| 881 | `lofter_dm.ads_tag_potential_top_di` | 1 | ADS |
| 882 | `lofter_dm.ads_tag_noncp_hot_top_di` | 1 | ADS |
| 883 | `lofter_dm.ads_tag_interest_dau_retain_di` | 1 | ADS |
| 884 | `lofter_dm.ads_tag_interest_dau_di` | 1 | ADS |
| 885 | `lofter_dm.ads_tag_hot_top_di` | 1 | ADS |
| 886 | `lofter_dm.ads_tag_flat_di` | 1 | ADS |
| 887 | `lofter_dm.ads_tag_fetch_tag_di` | 1 | ADS |
| 888 | `lofter_dm.ads_tag_fetch_ip_di` | 1 | ADS |
| 889 | `lofter_dm.ads_tag_dynamic_activation_di` | 1 | ADS |
| 890 | `lofter_dm.ads_tag_discuss_hot_list_di` | 1 | ADS |
| 891 | `lofter_dm.ads_tag_concurrency_wd` | 1 | ADS |
| 892 | `lofter_dm.ads_system_architecture_` | 1 | ADS |
| 893 | `lofter_dm.ads_suspect_hot_based_on_user_di` | 1 | ADS |
| 894 | `lofter_dm.ads_suspect_hot_based_on_slider_di` | 1 | ADS |
| 895 | `lofter_dm.ads_suspect_hot_based_on_client_di` | 1 | ADS |
| 896 | `lofter_dm.ads_summer_act_role_challenge_list_di` | 1 | ADS |
| 897 | `lofter_dm.ads_summer_act_challenge_hot_post_list_di` | 1 | ADS |
| 898 | `lofter_dm.ads_summer_act_challenge_hot_list_di` | 1 | ADS |
| 899 | `lofter_dm.ads_suit_reserve_pay_dd` | 1 | ADS |
| 900 | `lofter_dm.ads_suit_purchase_dd` | 1 | ADS |
| 901 | `lofter_dm.ads_source_exposure_click_report_di` | 1 | ADS |
| 902 | `lofter_dm.ads_seven_group_mau_cp_report_di` | 1 | ADS |
| 903 | `lofter_dm.ads_search_word_top_di` | 1 | ADS |
| 904 | `lofter_dm.ads_search_trend_hot_tag_di` | 1 | ADS |
| 905 | `lofter_dm.ads_search_tag_score_top_di` | 1 | ADS |
| 906 | `lofter_dm.ads_search_sug_word_cvr_di` | 1 | ADS |
| 907 | `lofter_dm.ads_search_sug_cvr_di` | 1 | ADS |
| 908 | `lofter_dm.ads_search_query_tag_report_di` | 1 | ADS |
| 909 | `lofter_dm.ads_search_premium_post_di` | 1 | ADS |
| 910 | `lofter_dm.ads_search_password_device_retain_di` | 1 | ADS |
| 911 | `lofter_dm.ads_search_password_device_di` | 1 | ADS |
| 912 | `lofter_dm.ads_search_no_result_stats_di` | 1 | ADS |
| 913 | `lofter_dm.ads_search_keyword_rec_reason_di` | 1 | ADS |
| 914 | `lofter_dm.ads_search_hopper_di` | 1 | ADS |
| 915 | `lofter_dm.ads_search_collection_pool_di` | 1 | ADS |
| 916 | `lofter_dm.ads_search_click_result_stats_di` | 1 | ADS |
| 917 | `lofter_dm.ads_sdk_hybrid_white_screen_di` | 1 | ADS |
| 918 | `lofter_dm.ads_sdk_hybrid_loadtime_di` | 1 | ADS |
| 919 | `lofter_dm.ads_risk_user_tag_score_dd` | 1 | ADS |
| 920 | `lofter_dm.ads_risk_user_level_di` | 1 | ADS |
| 921 | `lofter_dm.ads_risk_tag_post_rank_brush_hot_report_di` | 1 | ADS |
| 922 | `lofter_dm.ads_risk_rec_review_retrospect_di` | 1 | ADS |
| 923 | `lofter_dm.ads_risk_rec_audit_review_monitor_di` | 1 | ADS |
| 924 | `lofter_dm.ads_risk_offsite_induction_self_intro_di` | 1 | ADS |
| 925 | `lofter_dm.ads_risk_offsite_induction_return_gift_di` | 1 | ADS |
| 926 | `lofter_dm.ads_risk_offsite_induction_content_di` | 1 | ADS |
| 927 | `lofter_dm.ads_risk_offsite_induction_comment_di` | 1 | ADS |
| 928 | `lofter_dm.ads_risk_message_stats_di` | 1 | ADS |
| 929 | `lofter_dm.ads_risk_inspection_record_di` | 1 | ADS |
| 930 | `lofter_dm.ads_risk_gr_blog_ia_di` | 1 | ADS |
| 931 | `lofter_dm.ads_risk_abnormal_blog_copy_link_di` | 1 | ADS |
| 932 | `lofter_dm.ads_reward_video_creator_real_income_report_di` | 1 | ADS |
| 933 | `lofter_dm.ads_rev_gift_consume_dd` | 1 | ADS |
| 934 | `lofter_dm.ads_return` | 1 | ADS |
| 935 | `lofter_dm.ads_resource_traffic_value_di` | 1 | ADS |
| 936 | `lofter_dm.ads_rec_wall_papers_di` | 1 | ADS |
| 937 | `lofter_dm.ads_rec_user_pay_item_detail_dd` | 1 | ADS |
| 938 | `lofter_dm.ads_rec_tag_rec_reason_di` | 1 | ADS |
| 939 | `lofter_dm.ads_rec_silent_user_di` | 1 | ADS |
| 940 | `lofter_dm.ads_rec_retain_next_day_pt_di` | 1 | ADS |
| 941 | `lofter_dm.ads_rec_pushable_user_di` | 1 | ADS |
| 942 | `lofter_dm.ads_rec_post_review_monitor_di` | 1 | ADS |
| 943 | `lofter_dm.ads_rec_pending_humanreview_monitor_di` | 1 | ADS |
| 944 | `lofter_dm.ads_rec_hot_comment_content_di` | 1 | ADS |
| 945 | `lofter_dm.ads_rec_high_revenue_blogs_di` | 1 | ADS |
| 946 | `lofter_dm.ads_rec_dis_category_percentile_di` | 1 | ADS |
| 947 | `lofter_dm.ads_rec_cvr_pt_di` | 1 | ADS |
| 948 | `lofter_dm.ads_rec_collection_user_follow_di` | 1 | ADS |
| 949 | `lofter_dm.ads_rec_collection_user_browse_di` | 1 | ADS |
| 950 | `lofter_dm.ads_rec_auto_column_content_di` | 1 | ADS |
| 951 | `lofter_dm.ads_rec_auto_column_collection_di` | 1 | ADS |
| 952 | `lofter_dm.ads_rec_auto_column_blog_di` | 1 | ADS |
| 953 | `lofter_dm.ads_rec_all_rec_reason_di` | 1 | ADS |
| 954 | `lofter_dm.ads_question_square_tag_host_list_di` | 1 | ADS |
| 955 | `lofter_dm.ads_question_square_host_list_di` | 1 | ADS |
| 956 | `lofter_dm.ads_pve_weekly_data_report_di` | 1 | ADS |
| 957 | `lofter_dm.ads_pve_user_ugc_role_sort_list_dd` | 1 | ADS |
| 958 | `lofter_dm.ads_pve_user_role_sort_list_dd` | 1 | ADS |
| 959 | `lofter_dm.ads_pve_user_paid_retain_di` | 1 | ADS |
| 960 | `lofter_dm.ads_pve_user_interview_retain_di` | 1 | ADS |
| 961 | `lofter_dm.ads_pve_user_interview_chat_days_di` | 1 | ADS |
| 962 | `lofter_dm.ads_pve_user_dialogue_trade_dd` | 1 | ADS |
| 963 | `lofter_dm.ads_pve_user_cp_content_pool_dd` | 1 | ADS |
| 964 | `lofter_dm.ads_pve_user_chats_stats_di` | 1 | ADS |
| 965 | `lofter_dm.ads_pve_user_chat_retain_di` | 1 | ADS |
| 966 | `lofter_dm.ads_pve_user_active_trade_di` | 1 | ADS |
| 967 | `lofter_dm.ads_pve_user_active_` | 1 | ADS |
| 968 | `lofter_dm.ads_pve_role_summary_report_di` | 1 | ADS |
| 969 | `lofter_dm.ads_pve_role_data_report_di` | 1 | ADS |
| 970 | `lofter_dm.ads_pve_aisource_strategy_report_di` | 1 | ADS |
| 971 | `lofter_dm.ads_pve_aisource_chat_trade_report_di` | 1 | ADS |
| 972 | `lofter_dm.ads_push_oppo_invalid_tokens_di` | 1 | ADS |
| 973 | `lofter_dm.ads_push_off_report_di` | 1 | ADS |
| 974 | `lofter_dm.ads_push_group_report_di` | 1 | ADS |
| 975 | `lofter_dm.ads_push_group_di` | 1 | ADS |
| 976 | `lofter_dm.ads_push_group_channel_di` | 1 | ADS |
| 977 | `lofter_dm.ads_product_daily_sales_report_di` | 1 | ADS |
| 978 | `lofter_dm.ads_post_usermark_stats_di` | 1 | ADS |
| 979 | `lofter_dm.ads_post_top_list_week_di` | 1 | ADS |
| 980 | `lofter_dm.ads_post_top_list_month_di` | 1 | ADS |
| 981 | `lofter_dm.ads_post_top_list_day_di` | 1 | ADS |
| 982 | `lofter_dm.ads_post_to_dynamic_dd` | 1 | ADS |
| 983 | `lofter_dm.ads_post_talk_search_highlight_dd` | 1 | ADS |
| 984 | `lofter_dm.ads_post_tag_cover_monitor_di` | 1 | ADS |
| 985 | `lofter_dm.ads_post_tag_cover_daily_di` | 1 | ADS |
| 986 | `lofter_dm.ads_post_share_dd` | 1 | ADS |
| 987 | `lofter_dm.ads_post_score_di` | 1 | ADS |
| 988 | `lofter_dm.ads_post_publish_failure_report_di` | 1 | ADS |
| 989 | `lofter_dm.ads_post_publish_draft_report_di` | 1 | ADS |
| 990 | `lofter_dm.ads_post_publish_by_content_type_di` | 1 | ADS |
| 991 | `lofter_dm.ads_post_potential_top_di` | 1 | ADS |
| 992 | `lofter_dm.ads_post_performance_video_report_di` | 1 | ADS |
| 993 | `lofter_dm.ads_post_paid_user_consume_v` | 1 | ADS |
| 994 | `lofter_dm.ads_post_paid_user_consume_migrate_report_di` | 1 | ADS |
| 995 | `lofter_dm.ads_post_paid_user_consume_di` | 1 | ADS |
| 996 | `lofter_dm.ads_post_paid_scene_ip_action_detail_v` | 1 | ADS |
| 997 | `lofter_dm.ads_post_paid_scene_ip_action_detail_di` | 1 | ADS |
| 998 | `lofter_dm.ads_post_paid_retain_di` | 1 | ADS |
| 999 | `lofter_dm.ads_post_paid_monitor_v` | 1 | ADS |
| 1000 | `lofter_dm.ads_post_paid_low_quality_creator_detail_di` | 1 | ADS |

### 2.2 高频字段 TOP 145（按代码引用频次降序）

| # | 字段名 | 频次 | 数据类型 | 业务说明 |
|---|--------|------|---------|---------|
| 1 | `dt` | 10054 | STRING | 分区日期字段(yyyy-MM-dd) |
| 2 | `userId` | 8686 | BIGINT | 用户ID |
| 3 | `postId` | 5873 | BIGINT | 文章ID |
| 4 | `blogId` | 4508 | BIGINT | 博客ID/创作者ID（通常等于userId） |
| 5 | `deviceUdid` | 2078 | STRING | 设备唯一标识 |
| 6 | `createTime` | 1375 | BIGINT | 创建时间戳(毫秒) |
| 7 | `ip` | 1108 | STRING | IP地址 |
| 8 | `status` | 921 | INT/STRING | 状态 |
| 9 | `contentType` | 908 | STRING | 内容类型 |
| 10 | `eventId` | 891 | STRING | 埋点事件ID |
| 11 | `publishDate` | 775 | STRING | 发布日期 |
| 12 | `params` | 621 | MAP<STRING,STRING> | 扩展参数 |
| 13 | `scene` | 617 | STRING | 场景 |
| 14 | `deviceOs` | 494 | STRING | 设备系统(android/iphone) |
| 15 | `source` | 487 | STRING(JSON) | 来源链路JSON |
| 16 | `tags` | 474 | ARRAY<STRING> | 标签数组 |
| 17 | `collectionId` | 456 | BIGINT | 合集ID |
| 18 | `occurTime` | 452 | BIGINT | 事件发生时间戳(毫秒) |
| 19 | `opType` | 445 | STRING | 操作类型(praise/reproduce/recommend/subscribe) |
| 20 | `level` | 438 | INT | 等级 |
| 21 | `post_content_type` | 416 | STRING | 文章内容类型(衍生字段) |
| 22 | `itemId` | 388 | STRING | 操作对象ID |
| 23 | `amount` | 369 | BIGINT/DECIMAL | 金额 |
| 24 | `exp_date` | 363 | STRING | 实验日期 |
| 25 | `trade_date` | 360 | STRING | 交易日期 |
| 26 | `blogName` | 342 | STRING | 博客名 |
| 27 | `bucket_id` | 307 | INT | 实验分桶ID |
| 28 | `pv` | 305 | BIGINT | 页面浏览量 |
| 29 | `score` | 297 | DOUBLE/INT | 评分 |
| 30 | `appVersion` | 294 | STRING | App版本号 |
| 31 | `url` | 286 | STRING | 文章URL |
| 32 | `ips` | 279 | ARRAY<STRING> | IP标签数组 |
| 33 | `roleId` | 278 | BIGINT | PVE角色ID |
| 34 | `publishTime` | 275 | BIGINT | 发布时间戳(毫秒) |
| 35 | `exp_id` | 258 | STRING | 实验ID |
| 36 | `allowView` | 249 | INT | 可见范围(0=公开,50=审核中,100=仅自己) |
| 37 | `uv` | 247 | BIGINT | 独立访客数 |
| 38 | `itemType` | 233 | STRING | 操作对象类型(ARTICLE/TEXT/PHOTO/VIDEO) |
| 39 | `category` | 230 | STRING | 分类 |
| 40 | `actionType` | 209 | STRING | 行为类型(page_view/page_duration/like) |
| 41 | `orderId` | 205 | BIGINT/STRING | 订单ID |
| 42 | `valid` | 197 | INT | 审核状态(0=正常,25=屏蔽) |
| 43 | `isPublished` | 197 | BOOLEAN | 是否已发布 |
| 44 | `module` | 196 | STRING | 功能模块 |
| 45 | `rank` | 195 | INT | 排名 |
| 46 | `title` | 193 | STRING | 标题 |
| 47 | `post_count` | 192 | INT | 发文数 |
| 48 | `channel` | 188 | STRING | 渠道 |
| 49 | `tagName` | 187 | STRING | 标签名称 |
| 50 | `createDate` | 172 | STRING | 注册/创建日期 |
| 51 | `is_real` | 167 | INT | 是否有效浏览(0/1) |
| 52 | `productType` | 165 | STRING | 商品类型 |
| 53 | `deviceId` | 165 | BIGINT | 设备自增ID |
| 54 | `isCitedPost` | 164 | BOOLEAN | 是否转载 |
| 55 | `blogNickName` | 164 | STRING | 博客昵称 |
| 56 | `pay_date` | 163 | STRING | 支付日期 |
| 57 | `recomStatus` | 149 | INT | 推荐审核状态 |
| 58 | `unlock_time` | 140 | BIGINT | 解锁时间戳 |
| 59 | `isForbidden` | 137 | BOOLEAN | 是否被屏蔽 |
| 60 | `fans_total` | 128 | BIGINT | 粉丝总数 |
| 61 | `real_browse_pv` | 112 | BIGINT | 有效浏览PV |
| 62 | `expose_pv` | 107 | BIGINT | 曝光PV |
| 63 | `cnt` | 107 | BIGINT | 计数 |
| 64 | `click_pv` | 101 | BIGINT | 点击PV |
| 65 | `isImported` | 99 | INT | 是否导入内容 |
| 66 | `isMoved` | 94 | INT | 是否搬迁内容 |
| 67 | `appChannel` | 94 | STRING | 渠道 |
| 68 | `review_status` | 89 | INT | 审核状态 |
| 69 | `platform` | 82 | STRING | 发布平台 |
| 70 | `hot_pv` | 75 | BIGINT | 热度PV |
| 71 | `post_publish_date` | 71 | STRING | 文章发布日期(衍生字段) |
| 72 | `is_new_user` | 71 | INT | 是否新用户(0/1) |
| 73 | `share_pv` | 61 | BIGINT | 分享PV |
| 74 | `post_tags` | 59 | ARRAY<STRING> | 文章标签(衍生字段) |
| 75 | `isActivityAutoPost` | 57 | INT | 是否活动自动发文 |
| 76 | `duration` | 57 | BIGINT | 停留时长(毫秒) |
| 77 | `browse_pv` | 54 | BIGINT | 浏览PV |
| 78 | `total_amount` | 53 | DECIMAL | 总金额 |
| 79 | `isAnonymous` | 53 | INT | 是否匿名(0=非匿名) |
| 80 | `post_ips` | 52 | ARRAY<STRING> | 文章IP标签(衍生字段) |
| 81 | `is_book_store` | 52 | INT | 是否书店内容 |
| 82 | `sourceType` | 49 | STRING | 来源类型(官方账号/PGC/UGC) |
| 83 | `_bin_op` | 49 | INT | binlog操作类型(0=INSERT,1=UPDATE) |
| 84 | `costTime` | 48 | BIGINT | 停留时长(毫秒) |
| 85 | `clientType` | 47 | STRING | 客户端类型 |
| 86 | `expose_uv` | 46 | BIGINT | 曝光UV |
| 87 | `blog_nickname` | 43 | STRING | 博客昵称(衍生字段) |
| 88 | `recommend_pv` | 42 | BIGINT | 推荐PV |
| 89 | `kafkaTime` | 38 | BIGINT | Kafka入队时间 |
| 90 | `is_fans` | 37 | INT | 是否粉丝(0/1) |
| 91 | `browse_uv` | 35 | BIGINT | 浏览UV |
| 92 | `reproduce_pv` | 33 | BIGINT | 转载PV |
| 93 | `real_browse_uv` | 31 | BIGINT | 有效浏览UV |
| 94 | `city` | 31 | STRING | 城市 |
| 95 | `hot_uv` | 29 | BIGINT | 热度UV |
| 96 | `_bin_op_time` | 29 | BIGINT | binlog操作时间 |
| 97 | `domains` | 26 | ARRAY<BIGINT> | 所属领域ID列表 |
| 98 | `subscribe_pv` | 25 | BIGINT | 收藏PV |
| 99 | `praise_pv` | 25 | BIGINT | 点赞PV |
| 100 | `response_pv` | 24 | BIGINT | 评论PV |
| 101 | `model` | 24 | STRING | 设备型号 |
| 102 | `province` | 23 | STRING | 省份 |
| 103 | `gender` | 23 | STRING | 性别 |
| 104 | `deviceModel` | 23 | STRING | 设备型号 |
| 105 | `country` | 23 | STRING | 国家 |
| 106 | `algInfo` | 19 | STRING | 推荐算法信息(JSON) |
| 107 | `_bin_op_seqno` | 18 | BIGINT | binlog操作序号 |
| 108 | `isBlogAuthenticated` | 15 | BOOLEAN | 博客是否认证 |
| 109 | `real_browse_duration` | 12 | BIGINT | 有效浏览时长(毫秒) |
| 110 | `modify_time` | 12 | BIGINT | 修改时间戳 |
| 111 | `is_collection` | 12 | INT | 是否合集 |
| 112 | `is_daren` | 11 | INT | 是否达人(0/1) |
| 113 | `commentNum` | 11 | INT | 评论数 |
| 114 | `active_days` | 10 | INT | 活跃天数 |
| 115 | `likeNum` | 9 | INT | 点赞数 |
| 116 | `isValid` | 9 | BOOLEAN | 是否有效 |
| 117 | `isTest` | 9 | INT | 是否测试用户 |
| 118 | `is_pay_gift` | 9 | INT | 是否付费礼物 |
| 119 | `blog_name` | 9 | STRING | 博客名(衍生字段) |
| 120 | `authDomainNames` | 9 | ARRAY<STRING> | 认证领域名称 |
| 121 | `resource_type` | 8 | STRING | 资源类型 |
| 122 | `post_domains` | 8 | ARRAY<BIGINT> | 文章领域(衍生字段) |
| 123 | `isOfficial` | 8 | INT | 是否官方 |
| 124 | `is_paid` | 8 | INT | 是否付费 |
| 125 | `importPlatformType` | 8 | STRING | 导入平台类型 |
| 126 | `campaign_id` | 8 | STRING | 推广活动ID |
| 127 | `authTime` | 8 | BIGINT | 认证时间 |
| 128 | `userPostIndex` | 7 | BIGINT | 用户发文序号 |
| 129 | `userCreateDate` | 7 | STRING | 用户注册日期 |
| 130 | `isAuthenticated` | 7 | BOOLEAN | 是否认证 |
| 131 | `total_count` | 6 | BIGINT | 总计数 |
| 132 | `question_id` | 6 | BIGINT | 问答ID |
| 133 | `product_id` | 6 | BIGINT | 商品ID |
| 134 | `progress` | 5 | DOUBLE | 视频播放进度 |
| 135 | `mainBlogId` | 5 | BIGINT | 主博客ID/创作者ID |
| 136 | `fansCount` | 5 | BIGINT | 粉丝数 |
| 137 | `authDomainIds` | 5 | ARRAY<BIGINT> | 认证领域ID |
| 138 | `product_name` | 4 | STRING | 商品名称 |
| 139 | `isRobot` | 4 | INT | 是否机器人 |
| 140 | `ip_name` | 4 | STRING | IP名称 |
| 141 | `shareNum` | 2 | INT | 分享数 |
| 142 | `is_user_first_post` | 2 | INT | 是否用户首发 |
| 143 | `is_miniprogram` | 2 | INT | 是否小程序用户 |
| 144 | `domain_name` | 2 | STRING | 领域名称 |
| 145 | `createFrom` | 2 | STRING | 注册来源平台 |

### 2.3 核心维度表 Schema

#### `lofter.dim_post` — 文章维度主表

```sql
-- 核心字段
id            BIGINT    -- 文章ID (主键)
userId        BIGINT    -- 发布者用户ID
blogId        BIGINT    -- 所属博客ID/创作者ID
blogName      STRING    -- 博客名
blogNickName  STRING    -- 博客昵称
title         STRING    -- 标题
contentType   STRING    -- 内容类型: '图片','文字','视频','长文章','问答','音乐'
publishTime   BIGINT    -- 发布时间戳(ms)
publishDate   STRING    -- 发布日期
tags          ARRAY<STRING>  -- 标签列表
ips           ARRAY<STRING>  -- IP标签列表
domains       ARRAY<BIGINT>  -- 所属领域ID列表
isPublished   BOOLEAN   -- 是否已发布
isForbidden   BOOLEAN   -- 是否被屏蔽
isCitedPost   BOOLEAN   -- 是否转载
allowView     INT       -- 可见范围: 0=公开, 50=审核中, 100=仅自己可见
valid         INT       -- 审核状态: 0=正常, 25=屏蔽
isMoved       INT       -- 是否搬迁内容
isImported    INT       -- 是否导入内容
isActivityAutoPost INT  -- 是否活动自动发帖
isBlogAuthenticated BOOLEAN -- 博客是否认证
userPostIndex BIGINT    -- 用户发文序号
userCreateDate STRING   -- 用户注册日期
url           STRING    -- 文章URL
is_book_store INT       -- 是否书店内容
recomStatus   INT       -- 推荐审核状态
ImportPlatformType STRING -- 导入平台类型
```

#### `lofter.dim_user` — 用户维度表

```sql
id            BIGINT    -- 用户ID (主键)
createTime    BIGINT    -- 注册时间戳(ms)
createDate    STRING    -- 注册日期
email         STRING    -- 邮箱
mainBlogId    BIGINT    -- 主博客ID/创作者ID
createFrom    STRING    -- 注册来源平台
isAnonymous   INT       -- 是否匿名: 0=非匿名
isTest        INT       -- 是否测试用户
isRobot       INT       -- 是否机器人
sourceType    STRING    -- '官方账号','PGC','UGC'
is_miniprogram INT      -- 是否小程序用户
```

#### `lofter.dim_blog` — 博客维度表

```sql
id              BIGINT    -- 博客ID/创作者ID (主键，通常=userId)
blogName        STRING    -- 博客名
blogNickName    STRING    -- 昵称
isAuthenticated BOOLEAN   -- 是否认证
isValid         BOOLEAN   -- 是否有效
isTest          INT       -- 是否测试
isOfficial      INT       -- 是否官方
createTime      BIGINT    -- 创建时间
authDomainIds   ARRAY<BIGINT>   -- 认证领域ID
authDomainNames ARRAY<STRING>   -- 认证领域名称
authTime        BIGINT    -- 认证时间
```

#### `lofter.ods_mda_app_di` — 客户端埋点日志（ODS 最高频表）

> **说明**：LOFTER 埋点日志涉及以下几张表，查询时应注意区分：
>
> | 表名 | 类型 | 说明 |
> |------|------|------|
> | `lofter.ods_mda_app_di` | **实表（推荐）** | 离线采集的埋点原始数据，**查询历史（T-1 及更早）MDA 数据应优先使用此表** |
> | `lofter.ods_mda_app_raw_di` | **实表（当日推荐）** | 实时归档的客户端 MDA 日志表，每 10 分钟落盘一次；**查询当日（T 日）MDA 数据应优先从此表查询**，`dt` 限制为当日 |
> | `lofter.ods_mda_app_partition_di` | 视图 | 指向 `ods_mda_app_di` 的视图表，查询效果等同于直接查 `ods_mda_app_di`，推荐直接查实表 |
> | `lofter.ods_mda_app_partition_old_di` | 实表 | 历史存档的 MDA 埋点数据，用于查询较早期的离线数据 |

```sql
-- 核心字段
userId        BIGINT    -- 用户ID
deviceUdid    STRING    -- 设备唯一标识
deviceOs      STRING    -- 设备系统 (android/iphone)
eventId       STRING    -- 埋点事件ID
actionType    STRING    -- 行为类型 (page_view/page_duration/like/reproduce等)
itemId        STRING    -- 操作对象ID
itemType      STRING    -- 操作对象类型 (ARTICLE/TEXT/PHOTO/VIDEO等)
scene         STRING    -- 场景
source        STRING    -- 来源链路(JSON数组)
occurTime     BIGINT    -- 发生时间戳(ms)
kafkaTime     BIGINT    -- Kafka入队时间
params        MAP<STRING,STRING> -- 扩展参数
algInfo       STRING    -- 推荐算法信息(JSON)
appVersion    STRING    -- App版本
appChannel    STRING    -- 渠道
deviceModel   STRING    -- 设备型号
costTime      BIGINT    -- 停留时长(ms)
progress      DOUBLE    -- 视频播放进度
dt            STRING    -- 分区日期
```

---

## 3. 业务规则与数据规范

> 本章定义了编写 LOFTER SQL 时必须遵守的业务规则，所有模板中的日期参数均使用 Azkaban 标准参数。

### 3.1 有效文章过滤（全局最高频规则）

几乎所有涉及文章的查询都**必须**加上以下过滤条件：

```sql
WHERE isPublished = true
  AND isForbidden = false
  AND isCitedPost = false          -- 排除转载
  AND allowView = 0                -- 仅公开
  AND isMoved = 0                  -- 排除搬迁
  AND isActivityAutoPost = 0       -- 排除活动自动发文
  AND isImported = 0               -- 排除导入
  AND contentType IN ('图片','文字','视频')
```

### 3.2 有效浏览判定

```sql
-- 视频类: 停留 > 5 秒为有效浏览；图片/文字类: 停留 > 3 秒
CASE WHEN contentType = '视频' THEN IF(costTime > 5000, 1, 0)
     WHEN contentType IN ('图片','文字') THEN IF(costTime > 3000, 1, 0)
     ELSE 0
END AS is_real
```

### 3.3 文章状态映射

```sql
CASE WHEN allowView = 50  AND valid = 25 AND isPublished = true  THEN '审核不通过'
     WHEN allowView = 50  AND valid = 0  AND isPublished = true  THEN '审核中'
     WHEN allowView = 0   AND valid = 0  AND isPublished = true  THEN '公开'
     WHEN allowView = 100 AND valid = 0  AND isPublished = true  THEN '仅自己可见'
     WHEN allowView = 0   AND valid = 25 AND isPublished = true  THEN '屏蔽'
     WHEN allowView = 0   AND valid = 0  AND isPublished = false THEN '草稿'
     ELSE '其他'
END AS post_status
```

### 3.4 时间戳处理

```sql
-- 毫秒时间戳 → 日期（Hive / Doris 通用）
from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd')

-- 毫秒时间戳 → 日期 + 小时
from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd HH')

-- 日期差（T-1 天距发布日的天数）
datediff('${azkaban.flow.1.days.ago}', publishDate)
```

### 3.5 数据质量过滤

> 以下规则在生产 SQL 中应**严格遵守**。

#### 3.5.1 排除测试/机器人/匿名用户

```sql
JOIN lofter.dim_user u ON ... WHERE u.isTest = 0 AND u.isRobot = 0 AND u.isAnonymous = 0
```

#### 3.5.2 排除无效设备

```sql
WHERE length(deviceUdid) > 0 AND eventId != 'rd-2'
```

#### 3.5.3 排除黑名单/机器人博客

```sql
-- 方式一：硬编码
WHERE userId NOT IN (2178121696, 2178132867, ...)

-- 方式二：LEFT JOIN 排除
LEFT JOIN lofter_db_dump.ods_db_robot_blog_info_nd f ON userId = f.blogId
WHERE f.blogId IS NULL
```

#### 3.5.4 排除搬迁/导入/自动文章

见 [3.1 有效文章过滤](#31-有效文章过滤全局最高频规则)。

#### 3.5.5 关注关系时间过滤

```sql
-- _nd 表不加 dt，用业务时间字段限制
FROM lofter_db_dump.ods_db_user_following_nd
WHERE from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') <= '${azkaban.flow.1.days.ago}'
```

### 3.6 分区查询规范

> 详见 [1.4 表后缀与查询规范](#14-表后缀与查询规范)，此处补充具体示例。

#### 3.6.1 全量表（`_dd` / `dim_*`）— 只取最新分区

```sql
-- ✅ 正确
SELECT userId, total_post_cnt, total_browse_cnt
FROM lofter.dws_user_life_circle_index_dd
WHERE dt = '${azkaban.flow.1.days.ago}'

-- ❌ 错误：跨分区 → 数据翻倍
WHERE dt BETWEEN '2026-03-01' AND '2026-03-07'
```

常见全量表：`dws_user_life_circle_index_dd` / `dim_post` / `dim_user` / `dim_blog`

#### 3.6.2 全量 dump 表（`_nd`）— 不加 dt，用业务时间

`_nd` 表仅存在于 `lofter_db_dump` 库，自动指向最新快照，**查询时不加 `dt` 条件**。

```sql
-- ✅ 正确：用业务时间过滤
SELECT userId, blogId, followTime
FROM lofter_db_dump.ods_db_user_following_nd
WHERE from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') = '${azkaban.flow.1.days.ago}'

-- ❌ 错误：_nd 表不支持 dt 过滤
WHERE dt = '${azkaban.flow.1.days.ago}'
```

常见 `_nd` 表：`ods_db_user_following_nd` / `ods_db_post_hot_nd` / `ods_db_robot_blog_info_nd` / `ods_db_benefit_order_product_nd`

#### 3.6.3 增量表（`_di`）— 按需限制分区范围

```sql
-- ✅ 单日增量
SELECT postId, count(1) AS browse_pv
FROM lofter.dwd_post_browse_di
WHERE dt = '${azkaban.flow.1.days.ago}'
GROUP BY postId

-- ✅ 7 天范围
SELECT postId, count(1) AS browse_pv_7d
FROM lofter.dwd_post_browse_di
WHERE dt BETWEEN '${azkaban.flow.7.days.ago}' AND '${azkaban.flow.1.days.ago}'
GROUP BY postId

-- ❌ 错误：无分区限制 → 全表扫描
SELECT postId, count(1) FROM lofter.dwd_post_browse_di GROUP BY postId
```

常见大增量表：`dwd_post_browse_di` / `dwd_post_expose_di` / `ods_mda_app_di`（单分区数据量极大）

### 3.7 SQL 优化规范

> 以下规则在 Doris / Hive（SparkSQL）上均适用。

#### 3.7.1 分区裁剪（Partition Pruning）

所有分区表查询**必须在 WHERE 中直接指定 `dt`**。

```sql
-- ✅ WHERE 中直接过滤
SELECT postId, count(1) AS pv
FROM lofter.dwd_post_browse_di
WHERE dt = '${azkaban.flow.1.days.ago}'
GROUP BY postId

-- ❌ 分区条件放在 HAVING → 无法裁剪
HAVING dt = '${azkaban.flow.1.days.ago}'
```

#### 3.7.2 谓词下推（Predicate Pushdown）

过滤条件**下推到最内层子查询/JOIN 之前**，减少数据量。

```sql
-- ✅ 过滤在子查询内部
SELECT a.postId, a.expose_pv, b.hot_pv
FROM (
    SELECT postId, count(1) AS expose_pv
    FROM lofter.dwd_post_expose_di
    WHERE dt = '${azkaban.flow.1.days.ago}' AND length(deviceUdid) > 0
    GROUP BY postId
) a
LEFT JOIN (
    SELECT postId, count(1) AS hot_pv
    FROM lofter.dwd_post_hot_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) b ON a.postId = b.postId

-- ❌ 外层过滤 → 子查询已扫全表
WHERE a.dt = '${azkaban.flow.1.days.ago}' AND b.dt = '${azkaban.flow.1.days.ago}'
```

#### 3.7.3 JOIN 优化

| 规则 | 说明 |
|------|------|
| 小表放右侧 | LEFT JOIN 时维度表（小表）在右 |
| JOIN 前先聚合 | 子查询先 WHERE + GROUP BY，再用聚合结果 JOIN |
| 避免笛卡尔积 | JOIN 必须有明确 ON 条件 |
| 类型一致 | JOIN 两端字段类型一致，避免隐式转换 |

```sql
-- ✅ 先聚合，再 JOIN 维度表
SELECT p.id, p.userId, p.contentType, a.browse_pv
FROM lofter.dim_post p
INNER JOIN (
    SELECT postId, count(1) AS browse_pv
    FROM lofter.dwd_post_browse_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) a ON p.id = a.postId

-- ❌ 先大表 JOIN 再聚合
SELECT p.id, p.userId, p.contentType, count(1) AS browse_pv
FROM lofter.dim_post p
JOIN lofter.dwd_post_browse_di b ON p.id = b.postId
WHERE b.dt = '${azkaban.flow.1.days.ago}'
GROUP BY p.id, p.userId, p.contentType
```

#### 3.7.4 其他要点

| 规则 | 说明 |
|------|------|
| 避免 `SELECT *` | 只查需要的字段 |
| `COUNT(DISTINCT)` | 优先用聚合函数，避免子查询去重 |
| NULL 安全 | `COALESCE()` / `NVL()` 处理 NULL |
| `UNION ALL` > `UNION` | 无需去重时用 UNION ALL |
| 合理 `LIMIT` | 探查数据时加 LIMIT |
| `=` > `LIKE` | 能精确匹配不用 LIKE；前缀用 `LIKE 'x%'`，禁止 `LIKE '%x%'` |

---

## 4. 高频 SQL 模式与模板

### 4.1 SQL 函数/操作频次

| 操作 | 出现次数 | 用途 |
|------|---------|------|
| `SUM()` | 3196 | 聚合求和 |
| `GROUP BY` | 2839 | 分组聚合 |
| `IF()` | 2825 | 条件判断 |
| `COUNT()` | 2821 | 计数 |
| `COUNT(DISTINCT)` | 2422 | 去重计数 |
| `LEFT JOIN` | 2042 | 主要连接方式 |
| `NVL()` / `COALESCE()` | 1766 | NULL 处理 |
| `CASE WHEN` | 1628 | 条件分支 |
| `FROM_UNIXTIME()` | 1284 | 毫秒时间戳转日期 |
| `INSERT OVERWRITE TABLE` | 1047 | 全量覆盖写入 |
| `UNION ALL` | 989 | 纵向合并（不去重） |
| `GET_JSON_OBJECT()` | 533 | JSON 字段解析 |
| `ROW_NUMBER()` | 492 | 行号/去重取最新 |
| `LATERAL VIEW EXPLODE` | 340 | 数组展开为多行 |

### 4.2 文章流量统计模板

```sql
-- 模式：文章维度 LEFT JOIN 行为明细 → 聚合 PV/UV
INSERT OVERWRITE TABLE lofter.dws_post_traffic_di
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT p.id AS postId,
       p.userId AS post_userId,
       p.publishDate AS post_publish_date,
       p.contentType AS post_content_type,
       p.tags AS post_tags,
       nvl(a.expose_pv, 0) AS expose_pv,
       nvl(a.expose_uv, 0) AS expose_uv,
       nvl(b.real_browse_pv, 0) AS real_browse_pv,
       nvl(b.real_browse_uv, 0) AS real_browse_uv,
       nvl(b.real_browse_duration, 0) AS real_browse_duration
FROM lofter.dim_post_article p
LEFT JOIN (
    SELECT postId,
           count(1) AS expose_pv,
           count(distinct deviceId) AS expose_uv
    FROM lofter.dwd_post_expose_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) a ON p.id = a.postId
LEFT JOIN (
    SELECT postId,
           sum(if(is_real > 0, 1, 0)) AS real_browse_pv,
           count(distinct if(is_real > 0, deviceId, null)) AS real_browse_uv,
           sum(if(is_real > 0, duration, 0)) AS real_browse_duration
    FROM lofter.dwd_post_browse_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) b ON p.id = b.postId
WHERE a.expose_pv > 0 OR b.real_browse_pv > 0
```

### 4.3 文章互动统计模板

```sql
-- 模式：文章维度 LEFT JOIN 多张行为表（热度/评论/分享）
SELECT p.id AS postId,
       nvl(a.hot_pv, 0) AS hot_pv,
       nvl(a.praise_pv, 0) AS praise_pv,
       nvl(a.reproduce_pv, 0) AS reproduce_pv,
       -- 评论
       nvl(b.response_pv, 0) AS response_pv,
       -- 分享
       nvl(c.share_pv, 0) AS share_pv
FROM lofter.dim_post_article p
LEFT JOIN (
    SELECT postId,
           count(1) AS hot_pv,
           sum(if(opType = 'praise', 1, 0)) AS praise_pv,
           sum(if(opType = 'reproduce', 1, 0)) AS reproduce_pv,
           sum(if(opType = 'recommend', 1, 0)) AS recommend_pv,
           sum(if(opType = 'subscribe', 1, 0)) AS subscribe_pv,
           count(distinct deviceId) AS hot_device_uv
    FROM lofter.dwd_post_hot_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) a ON p.id = a.postId
LEFT JOIN (
    SELECT postId, count(1) AS response_pv,
           count(distinct deviceId) AS response_device_uv
    FROM lofter.dwd_post_response_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) b ON p.id = b.postId
LEFT JOIN (
    SELECT postId, count(1) AS share_pv,
           count(distinct deviceId) AS share_device_uv
    FROM lofter.dwd_post_share_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
    GROUP BY postId
) c ON p.id = c.postId
```

### 4.4 设备活跃统计模板

```sql
-- 模式：从埋点日志聚合设备级指标
SELECT appKey, deviceOs, appChannel, appVersion,
       deviceUdid, userId, deviceModel, ip,
       min(occurTime) AS occurTime,
       min(if(from_unixtime(cast(occurTime/1000 as bigint), 'yyyy-MM-dd')
              = '${azkaban.flow.1.days.ago}', occurTime, null)) AS returnOccurTime,
       max(occurTime) AS maxOccurTime
FROM lofter.ods_mda_app_di
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND length(deviceUdid) > 0
  AND eventId != 'rd-2'
GROUP BY appKey, deviceOs, appChannel, appVersion,
         deviceUdid, userId, deviceModel, ip
```

### 4.5 用户宽表构建模板

```sql
-- 模式: 以用户维度表为基准，大量 LEFT JOIN 各主题汇总
SELECT u.id AS userId,
       u.mainBlogId AS blogId,
       b.blogName, b.blogNickName,
       u.createTime,
       -- 各维度画像字段
       nvl(c.gender, null) AS gender,
       nvl(e.isPushOn, 0) AS is_push_on,
       nvl(z5.post_count, 0) AS post_count,
       x.time AS last_login_time,
       x.clientType AS last_login_platform
FROM (
    SELECT userId AS id, mainBlogID, profileCreateTime AS createTime, ...
    FROM lofter_db_dump.ods_db_profile_nd
) u
JOIN (SELECT id FROM lofter.dim_user WHERE isanonymous = 0) du ON u.id = du.id
LEFT JOIN lofter.dim_blog b ON u.id = b.id
LEFT JOIN (...用户标签...) c ON u.id = c.userid
LEFT JOIN (...推送状态...) e ON u.id = e.userId
LEFT JOIN (...发帖统计...) z5 ON u.id = z5.userId
LEFT JOIN (...登录信息...) x ON u.id = x.accountId
```

### 4.6 binlog 增量处理模板

```sql
-- 模式：从 binlog 取增量，ROW_NUMBER 取最新状态
SELECT *
FROM (
    SELECT *,
           ROW_NUMBER() OVER (
               PARTITION BY id, blogid
               ORDER BY _bin_op_time DESC, _bin_op_seqno DESC
           ) AS rk
    FROM lofter.ods_binlog_post_hot_di
    WHERE dt = '${azkaban.flow.1.days.ago}'
      AND _bin_op < 2       -- 0=INSERT, 1=UPDATE
      AND type IN (1, 2, 3, 4)
) a
WHERE rk = 1 AND _bin_op = 0
```

### 4.7 标签展开与聚合模板

```sql
-- 模式: LATERAL VIEW EXPLODE 展开数组字段
SELECT A.id, collect_set(B.id) AS domainIds
FROM (
    SELECT id, tagName
    FROM lofter.dim_post
    LATERAL VIEW explode(tags) ss AS tagName
) A
LEFT JOIN (
    SELECT id, tagName
    FROM lofter.dim_domain
    LATERAL VIEW explode(tags) ss AS tagName
) B ON A.tagName = B.tagName
GROUP BY A.id
```

### 4.8 新用户判断模板

```sql
-- 模式：JOIN dim_user 判断注册日期
IF(u.createDate = '${azkaban.flow.1.days.ago}', 1, 0) AS is_new_user

-- 近 7 天新用户
WHERE u.createDate > '${azkaban.flow.7.days.ago}'
  AND u.createDate <= '${azkaban.flow.1.days.ago}'
```

### 4.9 来源链路解析模板

```sql
-- 模式: 从 source JSON 数组中提取多级来源
GET_JSON_OBJECT(source, '$[1].scene')  AS source1_scene
GET_JSON_OBJECT(source, '$[2].scene')  AS source2_scene
GET_JSON_OBJECT(source, '$[1].module') AS source1_module
GET_JSON_OBJECT(source, '$[2].module') AS source2_module
params['module']                       AS module

-- 场景归类逻辑
CASE WHEN source1_module = 'related_item' THEN 'related_article'
     WHEN source1_scene IN ('floatcollection','immerse_collection') THEN 'collection'
     WHEN source1_scene IS NULL AND source0_scene = 'discovery' THEN 'discovery'
     WHEN source1_scene = 'tag' THEN 'tag'
     ELSE source1_scene
END AS detail_source1_scene_level_1
```

### 4.10 URL 构造模板

```sql
-- 文章URL拼接（16进制转换）
concat(blogName, '.lofter.com/post/',
       conv(blogId, 10, 16), '_', conv(postId, 10, 16)) AS url
```

---

## 5. 典型 ETL 场景示例

### 5.1 场景一：文章发布事实表（DWD 层）

**输入** · `dim_post` + `ods_log_publishpost_di` + `dim_miniprogram_post_dd` + `dim_gift_post_dd`  
**输出** · `lofter.dwd_post_publish_di`  
**说明** · 当日有效文章发布明细 → 用户首发标记 + 发布平台 + 是否付费礼物帖

```sql
INSERT OVERWRITE TABLE lofter.dwd_post_publish_di
PARTITION(dt = '${azkaban.flow.1.days.ago}')
SELECT a.postId, userId, blogName AS blog_name, title AS post_title,
       tags AS post_tags, ips AS post_ips, domains AS post_domains,
       contentType AS post_content_type,
       publishDate AS post_publish_date, publishTime AS post_publish_time,
       blogNickName AS blog_nickname,
       if(rk = 1, 1, 0) AS is_user_first_post,
       b.clientType AS platform,
       case when d.postId is not null then 1 else 0 end AS is_pay_gift
FROM (
    SELECT id AS postId, userId, blogName, blogNickName, title, contentType,
           publishDate, publishTime, tags, ips, domains,
           row_number() OVER (PARTITION BY userId ORDER BY publishTime) rk
    FROM lofter.dim_post
    WHERE publishDate = '${azkaban.flow.1.days.ago}'
      AND isPublished = true AND isCitedPost = false AND isForbidden = false
      AND allowView = 0 AND contentType IN ('图片','文字','视频')
      AND isActivityAutoPost = 0 AND isImported = 0 AND isMoved = 0 AND is_book_store = 0
) a
LEFT JOIN (
    SELECT postId, clientType
    FROM (
        SELECT postId, clientType,
               row_number() OVER (PARTITION BY postId ORDER BY time DESC) AS rk
        FROM lofter.ods_log_publishpost_di
        WHERE dt = '${azkaban.flow.1.days.ago}'
    ) t WHERE rk = 1
) b ON a.postId = b.postId
LEFT JOIN (
    SELECT postId FROM lofter.dim_miniprogram_post_dd
    WHERE dt = '${azkaban.flow.1.days.ago}' GROUP BY postId
) c ON a.postId = c.postId
LEFT JOIN (
    SELECT postId FROM lofter.dim_gift_post_dd
    WHERE dt = '${azkaban.flow.1.days.ago}'
      AND publishDate = '${azkaban.flow.1.days.ago}'
      AND is_pay_return_gift IN ('2','3','4','5','6','7')
    GROUP BY postId
) d ON a.postId = d.postId
WHERE c.postId IS NULL  -- 排除小程序帖
```

**要点**：标准有效文章过滤（3.1）· `ROW_NUMBER()` 计算首发标记 · 多 LEFT JOIN 关联

### 5.2 场景二：设备活跃与新增（DWD 层）

**输入** · `lofter.ods_mda_app_di`  
**输出** · `lofter.device_active`  
**说明** · 聚合设备当日首次/末次活跃时间

```sql
SELECT appKey, deviceOs, appChannel, appVersion,
       deviceUdid, userId, deviceModel, ip,
       min(occurTime) AS occurTime,
       min(if(from_unixtime(cast(occurTime/1000 as bigint), 'yyyy-MM-dd')
              = '${azkaban.flow.1.days.ago}', occurTime, null)) AS returnOccurTime,
       max(occurTime) AS maxOccurTime
FROM lofter.ods_mda_app_di
WHERE dt = '${azkaban.flow.1.days.ago}'
  AND length(deviceUdid) > 0
  AND eventId != 'rd-2'
GROUP BY appKey, deviceOs, appChannel, appVersion,
         deviceUdid, userId, deviceModel, ip
```

**要点**：`min(if(...))` 取当日首次活跃 · 过滤无效设备和系统事件

### 5.3 场景三：用户生命周期宽表（DWS 层）

**输出** · `lofter.dws_user_life_circle_index_dd`  
**模式** · 用户维度表为基准，LEFT JOIN 多主题域数据 → 全量宽表

关键来源：`dim_user`（注册）· `dws_evt_login_user_last_dd`（最后登录）· `dws_par_user_content_di`（浏览）· `dim_post`（发文）· `dws_par_user_interaction_dd`（互动）· `dwd_user_order_dd`（交易）· `dwd_post_browse_di` + `dim_gift_post_dd`（付费浏览）

### 5.4 场景四：维度表构建（DIM 层）

**输出** · `lofter.dim_post`  
**模式** · 多张 ODS 原表 JOIN → 宽维度表

步骤：
1. `ods_db_*_nd` 读取文章原始数据并清洗
2. JOIN `dim_blog` 获取博客/创作者信息
3. `LATERAL VIEW EXPLODE(tags)` → JOIN `dim_domain` 获取领域 ID
4. LEFT JOIN 问答/导入/活动辅助表补充标记
5. `INSERT OVERWRITE` 写入 `dim_post`（全量快照，无分区）

---

## 6. 附录

### 6.1 业务术语表

| 术语 | 英文 / 标识符 | 说明 |
|------|-------------|------|
| 文章(Post) | `post` | LOFTER 的核心内容单元，`postId` 即文章 ID |
| 博客/创作者(Blog) | `blog` | 用户的个人空间，`blogId` 即创作者 ID，通常 `userId ≈ blogId` |
| 标签(Tag) | `tag`（字段 `tags`） | 内容圈层标签，代表文章所属的兴趣圈层，如"第五人格""原神"等，以作者自由打标为主；存储在 `tags ARRAY<STRING>` 字段，通过 `dim_tag` 表维护 |
| IP标签 | `ip`（字段 `ips`） | 平台认定的内容圈层标签，与网络 IP 无关；同样代表圈层归属（如"第五人格""原神"），区别在于 IP 由平台官方维护和映射，通过 `dim_ip_dd`、`dim_ip_extend_dd` 等 `dim_ip*` 系列表维护，精度高于自由 Tag；文章关联的 IP 圈层存储在 `ips ARRAY<STRING>` 字段 |
| 领域(Domain) | `domain` | 内容分类领域（如二次元、绘画），通过 `dim_domain` 表维护 |
| 达人(Daren) | `daren` | 经平台认证的创作者，对应 `dim_blog.isAuthenticated = true` |
| 合集(Collection) | `collection` | 创作者将多篇文章归纳在一起的文章集合，通过 `bridge_collection_post` 关联文章 |
| 热度(Hot) | `hot` | 点赞/转载/推荐/收藏操作的统称，明细存于 `dwd_post_hot_di` |
| 粮单(Grain) | `grain` | 类圈子社区功能，用户可加入粮单获取创作者独家内容 |
| 口令(ActPwd) | `actpwd` | 活动口令功能，包含**米粮口令**（粮单专属）、**剪贴板口令**（复制触发）、**链接口令**（URL 跳转）三种类型，明细存于 `dwd_growth_actpwd_access_di` |
| 礼物文章(Gift Post) | `gift_post` | 创作者设置的付费解锁文章，读者需消费礼物才能解锁，相关表为 `dwd_gift_post_unlock_dd` |
| 文章赠礼/文章回礼(Return Gift) | `return_gift` | 粉丝赠送礼物后创作者回赠的专属文章，相关表为 `dim_gift_post_return_dd` |
| 书城(Book Store) | `book_store` | LOFTER 内的电子书购买功能，文章字段 `is_book_store = 1` 表示书城内容 |
| 私域(Private Zone) | `private_zone` | 创作者私域空间，仅订阅用户可见 |
| 虚拟恋人/PVE | `pve` | LOFTER 站内的虚拟人/虚拟恋人功能，是"破次元恋人"产品内嵌在 LOFTER App 内的部分 |
| 破次元恋人 | - | 网易旗下虚拟恋人产品线，分为两部分：**VC**（独立 App）和 **PVE**（内嵌 LOFTER 的模块） |
| VC | `vc` | 破次元恋人独立 App 业务线，包含 VC 端**模拟器**（AI 陪伴互动）和 VC 端**小剧场**（剧情内容）两类核心功能 |
| 里世界模拟器 | - | LOFTER（PVE）内的模拟器功能，与 VC 端模拟器相互独立 |
| 易次元 | `avg` | 易次元业务线，对应数据中 `appKey` 或业务标识为 `avg` |
| 业务线标识 | `lofter` / `vc` / `avg` | `lofter` = LOFTER 主站业务，`vc` = 破次元恋人业务，`avg` = 易次元业务 |
| 算法表(Rec) | `rec`（表前缀） | 以 `rec_` 开头的表均为 LOFTER 算法相关表，涵盖推荐算法、内容召回、个性化排序等，如 `rec_post_recall_dd`、`rec_user_interest_dd` 等 |

### 6.2 枚举值映射

#### 内容类型 (`contentType`)

| Type | contentType | 说明 |
|------|-------------|------|
| 1 | 文字 | 纯文字帖 |
| 2 | 图片 | 图文帖 |
| 3 | 音乐 | 音乐帖 |
| 4 | 视频 | 视频帖 |
| 5 | 问答 | 讨论/问答帖 |
| 6 | 长文章 | 长文帖 |

#### 热度操作类型 (`opType`)

| Type | opType | 说明 |
|------|--------|------|
| 1 | praise | 点赞 |
| 2 | reproduce | 转载 |
| 3 | recommend | 推荐 |
| 4 | subscribe | 收藏 |

#### 用户注册来源 (`createFrom`)

| 编号 | 来源 |
|------|------|
| 1 | 新浪 |
| 3 | QQ |
| 12 | 微信 |
| 13 | URS（网易通行证） |
| 14 | 手机号 |
| 15 | APPLE |
| 100 | 匿名 |
| 其他 | 邮箱 |

#### 导入平台类型 (`importPlatformType`)

| 值 | 含义 |
|----|------|
| 0 | 站内 |
| 1 | 知识公路 |
| 2 | 云音乐 |
| 3 | 抖音 |
| 4 | 快手 |
| 5 | YouTube |
| 6 | 微博 |
| 7 | MCN 机构 |

#### 用户权限等级 (`privilegeLevel`)

| 值 | 含义 | 判定来源 |
|----|------|---------|
| 0 | 反作弊白名单 | `ods_db_risk_antispam_white_user_nd.status=1` |
| 1 | 官号 | `ods_db_verify_blog_nd` |
| 2 | 达人 | `ods_db_authenticate_blog_nd` |
| 3 | 普通用户 | 默认 |

### 6.3 数据流向图

```
[ODS 原始层]
  ods_mda_app_di (离线埋点) ────────────────────────────┐
  ods_mda_app_raw_di (实时埋点) ────────────────────────┤
  ods_mda_app_partition_di (离线埋点视图) ──────────────┤
  ods_binlog_*_di (binlog实时)  ───────────────────────────┤
  ods_log_*_di (服务端实时日志)  ──────────────────────────┤
  ods_db_*_nd (数据库快照)   ────────────────────────────┤
                                                          │
[DIM 维度层]                                              ▼
  dim_user ◄──── ods_db_profile_nd                   [DWD 明细层]
  dim_blog ◄──── ods_db_blog_info_nd                   dwd_post_browse_di
  dim_post ◄──── ods_db_post (Post表)                  dwd_post_hot_di
  dim_domain ◄── ods_db_tag_resource_nd                dwd_post_expose_di
                                                        dwd_post_response_di
           │                                            dwd_post_publish_di
           ▼                                            dwd_post_share_di
[DWS 汇总层]                                            device_active / device_new
  dws_post_traffic_di ◄── dwd_post_expose + browse       │
  dws_post_interaction_di ◄── dwd_post_hot + response     │
  dws_par_user_base_dd ◄── dim_user + 多张画像表          ▼
  dws_par_creator_dd ◄── 创作者各维度汇总         [ADS 应用层]
                                                    ads_post_category_di
           │                                        ads_blog_general_di
           ▼                                        ads_hot_search_list_di
  [报表 & 推荐 & 风控 & 增长]                        ads_tag_general_di
                                                    ads_user_data_center_dd
                                                    ...
```
