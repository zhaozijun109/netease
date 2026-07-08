# LOFTER 业务知识库 — 表结构与 SQL 指南

> 本文档是 LOFTER 数据仓库的业务知识沉淀，涵盖数据分层架构、核心表结构、业务规则与高频 SQL 模式，  
> 旨在帮助 AI 理解 LOFTER 业务域，生成可在 Doris / Hive 上执行的 SQL 查询。

---

## 目录

1. [数据分层架构](#1-数据分层架构)
2. [核心表与字段速查](#2-核心表与字段速查)
3. [业务规则与数据规范](#3-业务规则与数据规范)
4. [高频 SQL 模式与模板](#4-高频-sql-模式与模板)
5. [典型 ETL 场景示例](#5-典型-etl-场景示例)
6. [附录](#6-附录)

---

## 1. 数据分层架构

### 1.1 分层模型

```
┌─────────────────────────────────────────────────┐
│  ADS 应用层 (lofter_dm)                          │
│  ads_* — 面向业务的聚合宽表、排行榜、推荐池       │
├─────────────────────────────────────────────────┤
│  DWS 汇总层 (lofter)                             │
│  dws_* — 主题域汇总：流量、互动、创作者、用户画像   │
├─────────────────────────────────────────────────┤
│  DWD 明细层 (lofter)                             │
│  dwd_* — 事件明细：浏览、点赞、发布、设备归因       │
├─────────────────────────────────────────────────┤
│  DIM 维度层 (lofter)                             │
│  dim_* — 核心实体：用户、博客/创作者、文章、IP、标签   │
├─────────────────────────────────────────────────┤
│  ODS 原始层 (lofter / lofter_db_dump)            │
│  ods_* — 日志原始数据、数据库快照、binlog/log 实时数据 │
└─────────────────────────────────────────────────┘
```

### 1.2 数据库命名

| 数据库 | 用途 | 引用频次 |
|--------|------|---------|
| `lofter` | 核心数仓（DIM/DWD/DWS/ODS） | 4248 |
| `lofter_db_dump` | 业务数据库快照（ods_db_*_nd） | 1240 |
| `lofter_dm` | 应用层数据集市（ADS） | 1249 |

### 1.3 表命名规范

| 前缀 | 含义 | 示例 |
|------|------|------|
| `ods_mda_*` | 埋点日志原始数据（离线实表: `ods_mda_app_di`【查历史推荐】，实时归档: `ods_mda_app_raw_di`【查当日推荐，每10min落盘】，视图: `ods_mda_app_partition_di`→指向离线实表，历史存档: `ods_mda_app_partition_old_di`） | `ods_mda_app_di` |
| `ods_binlog_*` | binlog 实时增量数据 | `ods_binlog_post_hot_di` |
| `ods_log_*` | 服务端实时日志 | `ods_log_praise_di` |
| `ods_db_*_nd` | 数据库全量快照 | `ods_db_user_following_nd` |
| `dim_*` | 维度表 | `dim_post`, `dim_user`, `dim_blog` |
| `dwd_*_di` | 日增量明细 | `dwd_post_browse_di` |
| `dwd_*_dd` | 日全量明细 | `dwd_post_status_dd` |
| `dws_*_di` | 日增量汇总 | `dws_post_traffic_di` |
| `dws_*_dd` | 日全量汇总 | `dws_par_user_base_dd` |
| `dws_par_*` | 带分区的汇总表 | `dws_par_user_interaction_di` |
| `ads_*_di` | 日增量应用表 | `ads_post_category_di` |
| `ads_*_dd` | 日全量应用表 | `ads_ip_life_cycle_dd` |
| `bridge_*` | 桥接/映射表 | `bridge_collection_post` |
| `dwb_*` | 中间宽表 | `dwb_par_user_info_nd` |

### 1.4 表后缀规范

表名后缀决定了表的**数据更新模式与分区行为**，是判断查询方式的核心依据：

| 后缀 | 全称 | 分区 | 更新模式 | 查询方式 |
|------|------|------|---------|---------|
| `_dd` | Day Delta（日全量） | 有 `dt` 分区 | 每天全量覆盖写入，每个分区是当日完整快照 | **`dt` 必须固定为最新一天**（`dt = '${dt}'`），跨分区会产生重复数据；如需按业务时间范围过滤，`dt` 仍取最新一天，再通过表内业务时间字段（如 `createDate`、`registerDate` 等）做范围限制 |
| `_di` | Day Increment（日增量） | 有 `dt` 分区 | 每天只写入当天新增/变更数据 | 根据业务需求限制分区范围，单日取 `dt = '${dt}'`，跨天取 `dt BETWEEN ... AND ...` |
| `_nd` | No-partition Delta（无分区全量快照） | 无 `dt` 分区（底层有但表自动指向最新） | 全量 dump，始终指向最新快照 | **不加 `dt` 条件**，通过表内业务时间字段（`createTime` / `payTime` / `followTime` 等）限制范围 |

> **总结记忆**：`_dd` 查最新一天 → `_di` 按需限范围 → `_nd` 用业务时间过滤
>
> **`_dd` 补充**：遇到时间范围需求时，`dt` 仍只取最新一天，时间范围改用表内业务字段过滤：
>
> ```sql
> -- ✅ 正确：dt 锁定最新分区，业务时间字段做范围过滤
> SELECT userId, createDate, total_browse_cnt
> FROM lofter.dws_user_life_circle_index_dd
> WHERE dt = '${dt}'                               -- 必须只取最新分区
>   AND createDate BETWEEN '${dt_7}' AND '${dt}'   -- 再用业务时间字段缩小范围
>
> -- ❌ 错误：用多个 dt 分区模拟时间范围（全量表跨分区数据翻倍）
> SELECT userId, createDate, total_browse_cnt
> FROM lofter.dws_user_life_circle_index_dd
> WHERE dt BETWEEN '${dt_7}' AND '${dt}'           -- 扫描 7 个分区，数据严重重复
> ```

---

## 2. 核心表与字段速查

### 2.1 高频表 TOP 30（按引用次数排序）

| 排名 | 表名 | 引用次数 | 层级 | 说明 |
|------|------|---------|------|------|
| 1 | `lofter.dim_post` | 284 | DIM | 文章维度主表 |
| 2 | `lofter.ods_mda_app_di` | 145 | ODS | 客户端埋点日志（实表，`ods_mda_app_partition_di` 为其视图，优先查此表） |
| 3 | `lofter.dim_user` | 127 | DIM | 用户维度主表 |
| 4 | `lofter.dws_post_interaction_dd` | 94 | DWS | 文章互动汇总 |
| 5 | `lofter.dwd_post_browse_di` | 87 | DWD | 文章浏览明细 |
| 6 | `lofter.dwd_gift_post_unlock_dd` | 82 | DWD | 礼物文章解锁明细 |
| 7 | `lofter.dws_par_creator_dd` | 73 | DWS | 创作者画像汇总 |
| 8 | `lofter.device_active` | 72 | DWD | 设备活跃明细 |
| 9 | `lofter_db_dump.ods_db_user_following_nd` | 70 | ODS | 关注关系快照 |
| 10 | `lofter.dim_post_article` | 63 | DIM | 非问答文章维度 |
| 11 | `lofter.dwd_user_order_dd` | 61 | DWD | 用户订单明细 |
| 12 | `lofter.dwd_post_hot_di` | 69 | DWD | 文章热度操作明细 |
| 13 | `lofter.dwd_evt_post_paid_detail_dd` | 55 | DWD | 付费文章详情 |
| 14 | `lofter.dim_blog` | 41 | DIM | 博客/创作者维度表 |
| 15 | `lofter.dwd_device_growth_attribution_di` | 41 | DWD | 设备增长归因 |
| 16 | `lofter.dim_gift_post_return_dd` | 38 | DIM | 礼物文章回馈维度 |
| 17 | `lofter.dwd_post_publish_di` | 37 | DWD | 文章发布明细 |
| 18 | `lofter.dws_par_creator_level_scoring_dd` | 35 | DWS | 创作者等级评分 |
| 19 | `lofter.dws_post_premium_di` | 34 | DWS | 优质文章 |
| 20 | `lofter.dws_collection_dd` | 34 | DWS | 合集汇总 |
| 21 | `lofter.dws_par_user_interaction_di` | 31 | DWS | 用户互动汇总 |
| 22 | `lofter.dwd_post_response_di` | 31 | DWD | 文章评论明细 |
| 23 | `lofter.dws_par_user_active_di` | 28 | DWS | 用户活跃汇总 |
| 24 | `lofter.dwd_growth_actpwd_access_di` | 28 | DWD | 活动密码访问 |
| 25 | `lofter.dws_post_traffic_di` | 20 | DWS | 文章流量汇总 |
| 26 | `lofter.dws_post_base_stats_di` | 26 | DWS | 文章基础统计 |
| 27 | `lofter.dws_par_user_base_dd` | 18 | DWS | 用户基础画像 |
| 28 | `lofter.dwd_post_expose_di` | 22 | DWD | 文章曝光明细 |
| 29 | `lofter.dwd_post_share_di` | 21 | DWD | 文章分享明细 |
| 30 | `lofter.bridge_collection_post` | 27 | Bridge | 合集-文章桥接 |

### 2.2 高频字段 TOP 30

| 排名 | 字段名 | 频次 | 数据类型 | 说明 |
|------|--------|------|---------|------|
| 1 | `dt` | 7574 | STRING | 分区日期字段 (yyyy-MM-dd) |
| 2 | `userId` | 6913 | BIGINT | 用户ID |
| 3 | `postId` | 4973 | BIGINT | 文章ID |
| 4 | `blogId` | 3410 | BIGINT | 博客ID/创作者ID（通常等于 userId） |
| 5 | `deviceUdid` | 1625 | STRING | 设备唯一标识 |
| 6 | `ip` | 894 | STRING | IP 地址 |
| 7 | `scene` | 558 | STRING | 场景 |
| 8 | `createTime` | 1001 | BIGINT | 创建时间戳(毫秒) |
| 9 | `contentType` | 641 | STRING | 内容类型 |
| 10 | `tags` | 390 | ARRAY&lt;STRING&gt; | 标签数组 |
| 11 | `eventId` | 645 | STRING | 埋点事件ID |
| 12 | `source` | 330 | STRING(JSON) | 来源链路JSON |
| 13 | `publishDate` | 579 | STRING | 发布日期 |
| 14 | `opType` | 389 | STRING | 操作类型 |
| 15 | `ips` | 270 | ARRAY&lt;STRING&gt; | IP标签数组 |
| 16 | `module` | 200 | STRING | 功能模块 |
| 17 | `deviceOs` | 357 | STRING | 设备系统 |
| 18 | `occurTime` | 350 | BIGINT | 事件发生时间戳(毫秒) |
| 19 | `appVersion` | 169 | STRING | App 版本号 |
| 20 | `is_real` | 148 | INT | 是否有效浏览 |
| 21 | `valid` | 140 | INT | 帖子审核状态 |
| 22 | `actionType` | 132 | STRING | 行为类型 |
| 23 | `deviceId` | 123 | BIGINT | 设备自增ID |
| 24 | `publishTime` | 119 | BIGINT | 发布时间戳(毫秒) |
| 25 | `allowView` | 203 | INT | 可见范围 |
| 26 | `isForbidden` | 101 | BOOLEAN | 是否被禁止 |
| 27 | `isPublished` | 93 | BOOLEAN | 是否已发布 |
| 28 | `expose_pv` | 105 | BIGINT | 曝光PV |
| 29 | `click_pv` | 91 | BIGINT | 点击PV |
| 30 | `is_new_user` | 71 | INT | 是否新用户 |

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

### 3.1 有效文章的标准过滤条件

这是全项目最常用的过滤逻辑，几乎所有涉及文章的查询都会用到：

```sql
-- 标准有效文章过滤
WHERE isPublished = true
  AND isForbidden = false
  AND isCitedPost = false
  AND allowView = 0
  AND isMoved = 0
  AND isActivityAutoPost = 0
  AND isImported = 0
  AND contentType IN ('图片','文字','视频')
```

### 3.2 有效浏览判定规则

```sql
-- 视频类: 停留>5秒为有效浏览
-- 图片/文字类: 停留>3秒为有效浏览
CASE WHEN contentType = '视频' THEN IF(costTime > 5000, 1, 0)
     WHEN contentType IN ('图片','文字') THEN IF(costTime > 3000, 1, 0)
     ELSE 0
END AS is_real
```

### 3.3 文章状态映射

```sql
CASE WHEN allowView = 50 AND valid = 25 AND isPublished = true THEN '审核不通过'
     WHEN allowView = 50 AND valid = 0  AND isPublished = true THEN '审核中'
     WHEN allowView = 0  AND valid = 0  AND isPublished = true THEN '公开'
     WHEN allowView = 100 AND valid = 0 AND isPublished = true THEN '仅自己可见'
     WHEN allowView = 0  AND valid = 25 AND isPublished = true THEN '屏蔽'
     WHEN allowView = 0  AND valid = 0  AND isPublished = false THEN '草稿'
     ELSE '其他'
END AS post_status
```

### 3.4 时间戳处理规范

```sql
-- 毫秒时间戳转日期（Hive / Doris 兼容）
from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd')

-- 毫秒时间戳转日期+小时
from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd HH')

-- 日期差计算（参数 ${dt} 代表运行日期，通常为 T-1 天）
datediff('${dt}', publishDate)
```

### 3.5 数据质量过滤规则

以下过滤条件在编写业务 SQL 时应严格遵守，以保证数据质量。

#### 3.5.1 排除测试 / 机器人用户

```sql
JOIN lofter.dim_user u ON ... WHERE u.isTest = 0 AND u.isRobot = 0 AND u.isAnonymous = 0
```

#### 3.5.2 排除无效设备

```sql
WHERE length(deviceUdid) > 0 AND eventId != 'rd-2'
```

#### 3.5.3 排除黑名单用户

```sql
-- 方式一: 硬编码已知ID
WHERE userId NOT IN (2178121696, 2178132867, ...)

-- 方式二: LEFT JOIN 机器人表排除
LEFT JOIN lofter_db_dump.ods_db_robot_blog_info_nd f ON userId = f.blogId
WHERE f.blogId IS NULL
```

#### 3.5.4 排除搬迁 / 导入 / 自动文章

见 [3.1 标准有效文章过滤条件](#31-有效文章的标准过滤条件)

#### 3.5.5 关注关系时间过滤

```sql
FROM lofter_db_dump.ods_db_user_following_nd
WHERE from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') <= '${dt}'
```

### 3.6 分区查询规范

LOFTER 数据表按数据更新方式分为**全量表**和**增量表**两类，查询时对分区范围的约束不同：

#### 3.6.1 全量表（大宽表）查询

全量表每天全量覆盖写入，每个分区都是当日的完整快照（类似大宽表），包含所有历史+当天的聚合指标。查询时**分区必须只取最新一天（通常为 T-1 天）**，不允许跨多个分区查询，否则会产生大量重复数据。

```sql
-- ✅ 正确：只查最新分区
SELECT userId, total_post_cnt, total_browse_cnt
FROM lofter.dws_user_life_circle_index_dd
WHERE dt = '${dt}'    -- ${dt} 为 T-1 天

-- ❌ 错误：跨分区查全量表会得到重复数据
SELECT userId, total_post_cnt
FROM lofter.dws_user_life_circle_index_dd
WHERE dt BETWEEN '2026-03-01' AND '2026-03-07'
```

**常见全量表示例**（后缀 `_dd` 或 `dim_` 前缀表）：
- `dws_user_life_circle_index_dd` — 用户生命周期宽表
- `dim_post` — 文章维度表
- `dim_user` — 用户维度表
- `dim_blog` — 博客维度表

#### 3.6.2 全量 dump 表（`_nd` 后缀）查询

后缀为 `_nd` 的表**只存在于 `lofter_db_dump` 库**中，是从业务数据库全量 dump 出来的原始表。这类表的特点是：
- 底层文件按日期分区存储，但**表会自动指向最新分区**，因此查询时**无需指定 `dt` 条件**
- 每次查询拿到的都是最新全量快照数据
- 需要通过表中的业务时间字段（如 `createTime`、`finishTime`、`payTime`、`followTime` 等）来限制数据范围

```sql
-- ✅ 正确：通过业务时间字段限制范围（无需指定 dt）
SELECT userId, blogId, followTime
FROM lofter_db_dump.ods_db_user_following_nd
WHERE from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') = '${dt}'

-- ✅ 正确：通过 createTime 取某天新注册用户
SELECT userId, createDate
FROM lofter_db_dump.ods_db_user_info_nd
WHERE createDate = '${dt}'

-- ❌ 错误：不需要也不应该指定 dt 分区条件
SELECT userId, blogId
FROM lofter_db_dump.ods_db_user_following_nd
WHERE dt = '${dt}'    -- 该表无需且不支持 dt 过滤
```

**常见 `_nd` 表表示例**（均在 `lofter_db_dump` 库下）：
- `ods_db_user_following_nd` — 用户关注关系全量表
- `ods_db_user_info_nd` — 用户基础信息全量表
- `ods_db_user_statistic_nd` — 用户统计信息全量表
- `ods_db_robot_blog_info_nd` — 机器人博客信息表
- `ods_db_post_info_nd` — 文章原始信息全量表

#### 3.6.3 增量表查询

增量表每天只写入当天的增量数据，每个分区包含该日新增/变更的记录。查询时应**根据单表分区的数据量大小，合理限制分区范围**，避免一次扫描过多分区导致查询超时或资源不足。

```sql
-- ✅ 正确：根据需求限制合理的分区范围
SELECT postId, count(1) AS browse_pv
FROM lofter.dwd_post_browse_di
WHERE dt = '${dt}'    -- 单日增量
GROUP BY postId

-- ✅ 正确：需要跨天统计时，根据分区大小控制范围
SELECT postId, count(1) AS browse_pv_7d
FROM lofter.dwd_post_browse_di
WHERE dt BETWEEN '${dt_7}' AND '${dt}'   -- 7天范围
GROUP BY postId

-- ❌ 错误：无限制扫描大增量表（如行为日志表每天数亿条）
SELECT postId, count(1)
FROM lofter.dwd_post_browse_di
GROUP BY postId
```

**常见增量表示例**（后缀 `_di` 表示日增量）：
- `dwd_post_browse_di` — 文章浏览明细（单分区数据量大）
- `dwd_post_expose_di` — 文章曝光明细（单分区数据量大）
- `dwd_post_hot_di` — 文章热度操作明细
- `ods_mda_app_di` — 客户端埋点日志（单分区数据量极大，注意：`ods_mda_app_partition_di` 是其视图，推荐直接查此实表）

### 3.7 SQL 查询优化规范

生成 SQL 时必须注意查询优化，以下规则在 Doris / Hive 上均适用。

#### 3.7.1 分区裁剪（Partition Pruning）

所有分区表查询**必须在 WHERE 中直接指定 `dt` 条件**，使引擎在扫描前即可裁剪无关分区。

```sql
-- ✅ 正确：WHERE 中直接过滤分区列
SELECT postId, count(1) AS pv
FROM lofter.dwd_post_browse_di
WHERE dt = '${dt}'
GROUP BY postId

-- ❌ 错误：将分区条件放在 HAVING 或嵌套子查询中，无法裁剪分区
SELECT postId, count(1) AS pv
FROM lofter.dwd_post_browse_di
GROUP BY postId
HAVING dt = '${dt}'
```

#### 3.7.2 谓词下推（Predicate Pushdown）

将过滤条件尽可能**下推到最内层子查询或 JOIN 之前**，减少参与 JOIN 和聚合的数据量。

```sql
-- ✅ 正确：过滤条件下推到子查询内部
SELECT a.postId, a.expose_pv, b.hot_pv
FROM (
    SELECT postId, count(1) AS expose_pv
    FROM lofter.dwd_post_expose_di
    WHERE dt = '${dt}'                -- 在子查询内过滤
      AND length(deviceUdid) > 0      -- 在子查询内过滤
    GROUP BY postId
) a
LEFT JOIN (
    SELECT postId, count(1) AS hot_pv
    FROM lofter.dwd_post_hot_di
    WHERE dt = '${dt}'                -- 在子查询内过滤
    GROUP BY postId
) b ON a.postId = b.postId

-- ❌ 错误：过滤条件放在外层，导致子查询扫描全量数据后再过滤
SELECT a.postId, a.expose_pv, b.hot_pv
FROM (
    SELECT postId, dt, count(1) AS expose_pv
    FROM lofter.dwd_post_expose_di
    GROUP BY postId, dt
) a
LEFT JOIN (
    SELECT postId, dt, count(1) AS hot_pv
    FROM lofter.dwd_post_hot_di
    GROUP BY postId, dt
) b ON a.postId = b.postId
WHERE a.dt = '${dt}' AND b.dt = '${dt}'   -- 过滤太晚，子查询已扫全表
```

#### 3.7.3 JOIN 优化

1. **小表放右侧**：LEFT JOIN 时将小表（维度表）放在 JOIN 的右侧
2. **JOIN 前先聚合/过滤**：先在子查询中完成 `WHERE` + `GROUP BY`，再用聚合结果 JOIN
3. **避免笛卡尔积**：JOIN 必须有明确的 ON 条件
4. **关联字段类型一致**：避免 JOIN 两端字段类型不一致导致的隐式转换

```sql
-- ✅ 正确：先聚合过滤，再 JOIN 维度表
SELECT p.id, p.userId, p.contentType, a.browse_pv
FROM lofter.dim_post p
INNER JOIN (
    SELECT postId, count(1) AS browse_pv
    FROM lofter.dwd_post_browse_di
    WHERE dt = '${dt}'
    GROUP BY postId
) a ON p.id = a.postId

-- ❌ 错误：先做大表 JOIN，再聚合过滤
SELECT p.id, p.userId, p.contentType, count(1) AS browse_pv
FROM lofter.dim_post p
JOIN lofter.dwd_post_browse_di b ON p.id = b.postId
WHERE b.dt = '${dt}'
GROUP BY p.id, p.userId, p.contentType
```

#### 3.7.4 其他优化要点

| 规则 | 说明 |
|------|------|
| **避免 `SELECT *`** | 只查询需要的字段，减少 I/O 和内存消耗 |
| **用 `COUNT(DISTINCT ...)` 代替子查询去重** | 避免不必要的嵌套 |
| **NULL 安全处理** | 使用 `COALESCE()` 或 `NVL()` 处理 NULL 值，避免 NULL 参与计算导致结果异常 |
| **UNION ALL 优于 UNION** | 无需去重时使用 `UNION ALL`，避免额外排序开销 |
| **合理使用 `LIMIT`** | 探查数据时加 `LIMIT`，避免全表返回 |
| **字符串匹配优先 `=`** | 能用 `=` 精确匹配时不要用 `LIKE`；需要前缀匹配时用 `LIKE 'abc%'`，避免 `LIKE '%abc%'` 全表扫描 |

---

## 4. 高频 SQL 模式与模板

### 4.1 SQL 操作频次统计

| 操作 | 频次 | 说明 |
|------|------|------|
| `SUM()` | 3196 | 聚合求和，最常用 |
| `GROUP BY` | 2839 | 分组聚合 |
| `IF()` | 2825 | 条件判断 |
| `COUNT()` | 2821 | 计数 |
| `COUNT(DISTINCT ...)` | 2422 | 去重计数 |
| `LEFT JOIN` | 2042 | 左连接（主要连接方式） |
| `NVL()` / `COALESCE()` | 1766 | NULL值处理 |
| `CASE WHEN` | 1628 | 条件分支 |
| `FROM_UNIXTIME()` | 1284 | 时间戳转换 |
| `INSERT OVERWRITE TABLE` | 1047 | 覆盖写入 |
| `UNION ALL` | 989 | 纵向合并（不去重） |
| `ROW_NUMBER()` | 492 | 行号窗口函数 |
| `GET_JSON_OBJECT()` | 533 | JSON解析 |
| `LATERAL VIEW EXPLODE` | 340 | 数组展开 |

### 4.2 文章流量统计模板

```sql
-- 模式: 文章维度 LEFT JOIN 行为明细，聚合 PV/UV
INSERT OVERWRITE TABLE lofter.dws_post_traffic_di
PARTITION(dt = '${dt}')
SELECT p.id AS postId,
       p.userId AS post_userId,
       p.publishDate AS post_publish_date,
       p.contentType AS post_content_type,
       p.tags AS post_tags,
       -- 曝光指标
       nvl(a.expose_pv, 0) AS expose_pv,
       nvl(a.expose_uv, 0) AS expose_uv,
       -- 浏览指标
       nvl(b.real_browse_pv, 0) AS real_browse_pv,
       nvl(b.real_browse_uv, 0) AS real_browse_uv,
       nvl(b.real_browse_duration, 0) AS real_browse_duration
FROM lofter.dim_post_article p
LEFT JOIN (
    SELECT postId,
           count(1) AS expose_pv,
           count(distinct deviceId) AS expose_uv
    FROM lofter.dwd_post_expose_di
    WHERE dt = '${dt}'
    GROUP BY postId
) a ON p.id = a.postId
LEFT JOIN (
    SELECT postId,
           sum(if(is_real > 0, 1, 0)) AS real_browse_pv,
           count(distinct if(is_real > 0, deviceId, null)) AS real_browse_uv,
           sum(if(is_real > 0, duration, 0)) AS real_browse_duration
    FROM lofter.dwd_post_browse_di
    WHERE dt = '${dt}'
    GROUP BY postId
) b ON p.id = b.postId
WHERE a.expose_pv > 0 OR b.real_browse_pv > 0
```

### 4.3 文章互动统计模板

```sql
-- 模式: 文章维度 LEFT JOIN 多张行为表（热度/评论/分享）
SELECT p.id AS postId,
       -- 热度操作（点赞/转载/推荐/收藏）
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
    WHERE dt = '${dt}'
    GROUP BY postId
) a ON p.id = a.postId
LEFT JOIN (
    SELECT postId, count(1) AS response_pv,
           count(distinct deviceId) AS response_device_uv
    FROM lofter.dwd_post_response_di
    WHERE dt = '${dt}'
    GROUP BY postId
) b ON p.id = b.postId
LEFT JOIN (
    SELECT postId, count(1) AS share_pv,
           count(distinct deviceId) AS share_device_uv
    FROM lofter.dwd_post_share_di
    WHERE dt = '${dt}'
    GROUP BY postId
) c ON p.id = c.postId
```

### 4.4 设备活跃统计模板

```sql
-- 模式: 从埋点日志聚合设备级指标
SELECT appKey, deviceOs, appChannel, appVersion,
       deviceUdid, userId, deviceModel, ip,
       min(occurTime) AS occurTime,
       min(if(from_unixtime(cast(occurTime/1000 as bigint), 'yyyy-MM-dd') = '${dt}',
              occurTime, null)) AS returnOccurTime,
       max(occurTime) AS maxOccurTime
FROM lofter.ods_mda_app_di
WHERE dt = '${dt}'
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
-- 模式: 从 binlog 取增量，用窗口函数取最新状态
SELECT *
FROM (
    SELECT *,
           ROW_NUMBER() OVER (
               PARTITION BY id, blogid
               ORDER BY _bin_op_time DESC, _bin_op_seqno DESC
           ) AS rk
    FROM lofter.ods_binlog_post_hot_di
    WHERE dt = '${dt}'
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
-- 模式: JOIN dim_user 判断注册日期
IF(u.createDate = '${dt}', 1, 0) AS is_new_user

-- 近7天新用户（${dt_7} 代表 T-7 天）
WHERE u.createDate > '${dt_7}' AND u.createDate <= '${dt}'
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

**输入表**: `dim_post` + `ods_log_publishpost_di` + `dim_miniprogram_post_dd` + `dim_gift_post_dd`  
**输出表**: `lofter.dwd_post_publish_di`  
**说明**: 计算当日有效文章发布明细，含用户首发标记、发布平台、是否付费礼物帖

```sql
INSERT OVERWRITE TABLE lofter.dwd_post_publish_di
PARTITION(dt = '${dt}')
SELECT a.postId, userId, blogName AS blog_name, title AS post_title,
       tags AS post_tags, ips AS post_ips, domains AS post_domains,
       contentType AS post_content_type,
       publishDate AS post_publish_date, publishTime AS post_publish_time,
       blogNickName AS blog_nickname,
       if(rk = 1, 1, 0) AS is_user_first_post,
       b.clientType AS platform,
       case when d.postId is not null then 1 else 0 end AS is_pay_gift
FROM (
    -- 有效文章 + 发文序号
    SELECT id AS postId, userId, blogName, blogNickName, title, contentType,
           publishDate, publishTime, tags, ips, domains,
           row_number() OVER (PARTITION BY userId ORDER BY publishTime) rk
    FROM lofter.dim_post
    WHERE publishDate = '${dt}'
      AND isPublished = true AND isCitedPost = false AND isForbidden = false
      AND allowView = 0 AND contentType IN ('图片','文字','视频')
      AND isActivityAutoPost = 0 AND isImported = 0 AND isMoved = 0 AND is_book_store = 0
) a
LEFT JOIN (
    -- 发布端信息（取最近一条）
    SELECT postId, clientType
    FROM (
        SELECT postId, clientType,
               row_number() OVER (PARTITION BY postId ORDER BY time DESC) AS rk
        FROM lofter.ods_log_publishpost_di
        WHERE dt = '${dt}'
    ) t WHERE rk = 1
) b ON a.postId = b.postId
LEFT JOIN (
    SELECT postId FROM lofter.dim_miniprogram_post_dd
    WHERE dt = '${dt}' GROUP BY postId
) c ON a.postId = c.postId
LEFT JOIN (
    SELECT postId FROM lofter.dim_gift_post_dd
    WHERE dt = '${dt}'
      AND publishDate = '${dt}'
      AND is_pay_return_gift IN ('2','3','4','5','6','7')
    GROUP BY postId
) d ON a.postId = d.postId
WHERE c.postId IS NULL  -- 排除小程序帖
```

**要点总结**：
- 标准有效文章过滤条件（见 3.1）
- `ROW_NUMBER()` 计算用户当日首发标记
- 多 LEFT JOIN 关联发布端信息、小程序帖过滤、付费礼物状态

### 5.2 场景二：设备活跃与新增（DWD 层）

**输入表**: `lofter.ods_mda_app_di`（客户端埋点日志）
**输出表**: `lofter.device_active`  
**说明**: 聚合每个设备当日首次/末次活跃时间，用于设备活跃与新增统计

```sql
SELECT appKey, deviceOs, appChannel, appVersion,
       deviceUdid, userId, deviceModel, ip,
       min(occurTime) AS occurTime,
       min(if(from_unixtime(cast(occurTime/1000 as bigint), 'yyyy-MM-dd') = '${dt}',
              occurTime, null)) AS returnOccurTime,
       max(occurTime) AS maxOccurTime
FROM lofter.ods_mda_app_di
WHERE dt = '${dt}'
  AND length(deviceUdid) > 0
  AND eventId != 'rd-2'
GROUP BY appKey, deviceOs, appChannel, appVersion,
         deviceUdid, userId, deviceModel, ip
```

**要点总结**：
- 使用 `min(if(...))` 取当日首次活跃时间（回访时间）
- `length(deviceUdid) > 0` 过滤无效设备
- `eventId != 'rd-2'` 过滤系统级事件

### 5.3 场景三：用户生命周期宽表（DWS 层）

**输出表**: `lofter.dws_user_life_circle_index_dd`  
**模式**: 用户维度表为基准，LEFT JOIN 多主题域数据，构建用户生命周期全量宽表

关键数据来源：
- `lofter.dim_user` → 基础信息（注册日期）
- `lofter.dws_evt_login_user_last_dd` → 最后登录
- `lofter.dws_par_user_content_di` → 浏览统计
- `lofter.dim_post` → 发文统计
- `lofter.dws_par_user_interaction_dd` → 互动统计
- `lofter.dwd_user_order_dd` → 交易统计
- `lofter.dwd_post_browse_di` + `dim_gift_post_dd` → 付费文章浏览

### 5.4 场景四：维度表构建（DIM 层）

**输出表**: `lofter.dim_post`  
**模式**: 多张 ODS 原表通过 JOIN 构建宽维度表（文章维度主表）

关键步骤：
1. 从 DB dump 读取文章原始数据（`ods_db_*_nd`），清洗字段
2. JOIN `dim_blog` 获取博客/创作者信息
3. `LATERAL VIEW EXPLODE(tags)` 展开标签 → JOIN `dim_domain` 获取领域 ID 列表
4. LEFT JOIN 问答/导入/活动等辅助表补充标记字段
5. 覆盖写入 `lofter.dim_post`（全量快照，无分区）

---

## 6. 附录

### 6.1 领域概念术语表

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

### 6.2 内容类型映射

| Type 值 | contentType | 说明 |
|---------|-------------|------|
| 1 | 文字 | 纯文字帖 |
| 2 | 图片 | 图文帖 |
| 3 | 音乐 | 音乐帖 |
| 4 | 视频 | 视频帖 |
| 5 | 问答 | 讨论/问答帖 |
| 6 | 长文章 | 长文帖 |

### 6.3 热度操作类型映射

| Type 值 | opType | 说明 |
|---------|--------|------|
| 1 | praise | 点赞 |
| 2 | reproduce | 转载 |
| 3 | recommend | 推荐 |
| 4 | subscribe | 收藏 |

### 6.4 用户注册来源映射

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

### 6.5 帖子导入平台类型

| platformType 值 | 含义 |
|-----------------|------|
| 0 | 站内 |
| 1 | 知识公路 |
| 2 | 云音乐 |
| 3 | 抖音 |
| 4 | 快手 |
| 5 | YouTube |
| 6 | 微博 |
| 7 | MCN机构 |

### 6.6 用户权限等级

| privilegeLevel | 含义 | 判定逻辑 |
|----------------|------|---------|
| 0 | 反作弊白名单用户 | `ods_db_risk_antispam_white_user_nd.status=1` |
| 1 | 官号 | `ods_db_verify_blog_nd` |
| 2 | 达人 | `ods_db_authenticate_blog_nd` |
| 3 | 普通用户 | 默认 |

### 6.7 关键数据流向图

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
