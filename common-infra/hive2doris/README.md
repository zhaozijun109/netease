# Hive2Doris 数据同步工具

基于 Spark SQL + Doris Spark Connector 的 Hive → Doris 数据同步工具。  
支持 **4 种同步模式**、**自动 Schema 演进**（含 KEY 列变更重建）、**分布式 Worklog 锁**、**分区变更自动发现**、**分类别异常重试** 以及 **Hive Bitmap → Doris BITMAP 同步**。

## 特性概览

| 特性 | 说明                                                                           |
|------|------------------------------------------------------------------------------|
| **4 种同步模式** | `full`（全量重刷）、`partition`（单分区）、`partition_range`（分区范围）、`auto`（自动发现变更分区）       |
| **Schema 演进** | 自动检测 Hive ↔ Doris 列差异：增列 / 删列 / 改类型（ALTER TABLE）；KEY 列变更则全表重建                |
| **Worklog 分布式锁** | 基于 Doris UNIQUE KEY 表的 worklog，防止多实例重复同步，支持续租 + 过期恢复                         |
| **分区变更检测** | Auto 模式下通过 MammutMetaService 的 `updateTime` 自动发现当天变更分区                       |
| **重建兜底方案** | KEY 列变更或表删除时：收集 min(Doris 分区数, Hive 分区数)，DROP + CREATE 后重新导入最新 N 个分区         |
| **分类别重试** | 区分 Schema / Network / Data / Lease / Timeout 异常，推荐 Skip / Retry / Rebuild 策略 |
| **动态分区清理** | 单分区 ≥ 10GB 时自动添加 `dynamic_partition.start = -30`，仅保留最近 30 天分区                |
| **Stream Load 写入** | 通过 Doris Spark Connector，使用 Json 格式 Stream Load 高效写入（Arrow格式依赖spark和doris环境） |
| **Bitmap 同步** | Hive RoaringBitmap → Doris BITMAP，支持多 bitmap 列、两种模式（StringFunc 默认 + Expand）、分批写入 + 幂等填充对齐、融合迭代器低内存 |

## 架构设计

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        入口 (三个 main class)                                │
│                                                                              │
│  Hive2DorisManualSync  — 手动一次性同步（支持 full/partition/partition_range/auto）│
│  Hive2DorisAutoSync    — 自动批量同步（自动发现 Hive 库中的增量表，Auto 模式）    │
│  Hive2DorisBitmapSynchronizer — Bitmap 同步（Hive RoaringBitmap → Doris BITMAP）    │
│  Hive2DorisTest        — 功能验证测试（DDL 生成 + Doris 实际建表）               │
└───────────────────┬──────────────────────┬───────────────────────────────────┘
                    │                      │
                    ▼                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      Hive2DorisSynchronizer                                   │
│                                                                               │
│  syncTable(table, partitions, syncMode, spark)                               │
│                                                                               │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌──────────────────────┐  │
│  │ Step 1: Worklog     │  │ Step 2: Schema       │  │ Step 3: Sync Data   │  │
│  │ - 确保 worklog 表   │  │ - 获取 Hive 字段     │  │ - Spark SQL 读 Hive │  │
│  │ - 注册 pending 分区 │  │ - Schema Evolution   │  │ - Doris Connector   │  │
│  │ - 获取 pending 列表 │  │ - 必要时 Rebuild     │  │   Stream Load 写入  │  │
│  │ - 续租 + 过期恢复   │  │   + Resync           │  │ - 逐分区锁控制      │  │
│  └─────────────────────┘  └─────────────────────┘  └──────────────────────┘  │
│                                                                               │
│  支撑组件:                                                                     │
│  ┌────────────────────┐ ┌──────────────────────┐ ┌────────────────────────┐   │
│  │ SchemaEvolution    │ │ DorisTypeMapper      │ │ DorisDDLGenerator      │   │
│  │ Manager            │ │ (Hive→Doris类型映射) │ │ (DDL生成/分区管理)     │   │
│  └────────────────────┘ └──────────────────────┘ └────────────────────────┘   │
│  ┌────────────────────┐ ┌──────────────────────┐ ┌────────────────────────┐   │
│  │ RetryPolicy        │ │ SyncException        │ │ Args                   │   │
│  │ (分类+指数退避重试) │ │ (异常分类模型)       │ │ (CLI参数解析)          │   │
│  └────────────────────┘ └──────────────────────┘ └────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────┘
```

## 项目结构

```
hive2doris/
├── src/main/scala/com/netease/yuanqi/
│   ├── doris/
│   │   ├── bitmap/
│   │   │   └── HiveBitmapConverter.scala       # RoaringBitmap 反序列化 + 格式转换
│   │   ├── config/
│   │   │   └── LoadConfig.scala                # 业务线 → 历史 load 表白名单 (lofter/vc)
│   │   ├── schema/
│   │   │   ├── DorisDDLGenerator.scala         # DDL 生成: CREATE TABLE / ADD PARTITION / DROP
│   │   │   ├── DorisTypeMapper.scala           # 类型映射: Hive → Spark → Doris
│   │   │   └── SchemaEvolutionManager.scala    # Schema 演进: 检测差异 + ALTER/REBUILD
│   │   ├── synchronizer/
│   │   │   ├── Hive2DorisBitmapSynchronizer.scala # 入口3: Bitmap 同步 (Expand/StringFunc 两种模式)
│   │   │   └── Hive2DorisSynchronizer.scala    # 核心同步器: 编排 worklog + schema + data sync
│   │   └── util/
│   │       ├── Args.scala                      # 轻量 CLI 参数解析器 (--key value / --flag)
│   │       └── SyncException.scala             # 异常层次: Schema/Network/Data/Lease/Timeout
│   └── hive2doris/
│       ├── Hive2DorisManualSync.scala          # 入口1: 手动一次性同步 (4种模式)
│       ├── Hive2DorisAutoSync.scala            # 入口2: 自动批量同步 (Auto模式, 支持并发)
│       └── Hive2DorisTest.scala                # 功能验证测试: 类型映射 + DDL生成 + Doris实际建表
└── jobs/                                        # Azkaban 调度脚本（按业务线分目录）
    ├── common.properties
    ├── hive2doris-lofter/                       # Lofter 业务线 (hive-db=lofter_dm, doris-db=lofter)
    │   ├── hive2doris_table_auto_sync_lofter.job
    │   ├── hive2doris_table_sync_lofter.job
    │   └── hive2doris_table_bitmap_sync_lofter.test
    └── hive2doris-vc/                           # VC 业务线 (hive-db=vc_dm, doris-db=vc)
        ├── hive2doris_table_auto_sync_vc.job
        ├── hive2doris_table_sync_vc.job
        └── hive2doris_table_bitmap_sync_vc.test
```

## 环境要求

| 依赖 | 版本                       |
|------|--------------------------|
| Scala | 2.12.x                   |
| Spark | 3.3.x                    |
| Java | 1.8+                     |
| Doris | 3.1+（需支持 AUTO PARTITION） |
| SBT | 1.x                      |

## 编译构建

```bash
cd common-infra

# 编译
sbt "project doris" compile

# 打 fat JAR
sbt "project doris" assembly

# 产出位于:
# hive2doris/target/scala-2.12/hive2doris-assembly-0.0.1.jar
```

## 配置说明

Doris 连接配置位于 `common/src/main/scala/com/netease/yuanqi/config/DorisConfig.scala`：

```scala
object DorisConfig {
  val fenodes  = "doris-fe-1:8030,doris-fe-2:8030,doris-fe-3:8030"
  val feQueryPort = "9030" 
  val user     = "lofter"
  val password = "123456"
  val jdbcUrl  = "jdbc:mysql://doris-fe-1:9030"
  val database = "lofter_test"
}
```

MammutMetaService 配置位于 `common/src/main/resources/` 下（API 密钥等）。

## 同步模式详解

### 1. Full 模式 — 全量重刷

```
DROP TABLE → CREATE TABLE → SELECT * FROM hive_table → Stream Load → Doris
```

```bash
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
  hive2doris-assembly.jar \
  --hive-db lofter_dm --doris-db lofter_doris \
  --table user_report_di \
  --mode full
```

### 2. Partition 模式 — 单分区补数

```
CREATE TABLE IF NOT EXISTS → DELETE partition data → INSERT partition data
```

```bash
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
  hive2doris-assembly.jar \
  --hive-db lofter_dm --doris-db lofter_doris \
  --table user_report_di \
  --mode partition --date 2026-03-11
```

### 3. PartitionRange 模式 — 多分区范围补数

```bash
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
  hive2doris-assembly.jar \
  --hive-db lofter_dm --doris-db lofter_doris \
  --table user_report_di,order_report_di \
  --mode partition_range --date 2026-03-10,2026-03-11,2026-03-12
```

### 4. Auto 模式 — 自动发现变更分区

通过 MammutMetaService 检查各分区的 `updateTime`，自动同步当天更新的分区。

```bash
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
  hive2doris-assembly.jar \
  --hive-db lofter_dm --doris-db lofter_doris \
  --table user_report_di \
  --mode auto
```

**变更判定逻辑**（避免遗漏 sync_done 之后再次更新的分区）：

| Worklog 状态 | Hive `updateTime` | 处理 |
|---|---|---|
| 无 sync_done 记录 | 当天更新 | 正常注册 pending → 同步 |
| 已 sync_done | `<= worklog.update_time` | 跳过（同步后未变更） |
| 已 sync_done | `>  worklog.update_time` | **force 重导**（sync_done 之后 Hive 又更新） |

> 关键修复：旧版本只比较 `partition_val` 是否在 sync_done 集合中，导致"sync_done 后再变更"的分区被永久跳过。新版本会拉取 worklog 中 sync_done 记录的 `update_time`，与 Hive `updateTime` 严格对比，若 Hive 更新时间更晚则覆写 sync_done → sync_pending 触发重导。

### 5. AutoSync 批量入口 — 自动发现整库增量表

自动发现 Hive 库中符合命名规范的增量表（如 `_di`/`_dd`/`_7d` 等后缀），并行以 Auto 模式同步：

```bash
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisAutoSync \
  hive2doris-assembly.jar \
  --hive-db lofter_dm \
  --doris-db lofter_doris \
  --includes "dim_user_base,_report_" \
  --excludes "_wide_table_,_tmp_" \
  --parallelism 4
```

AutoSync 过滤链（4 步）：
1. **Suffix Pattern** — 用正则匹配表名后缀（如 `_di`/`_dd`/`_7d`），筛选增量表
2. **Merge Includes** — 将 `--includes` 指定的表名或关键词合并到 Step 1 的结果中。每个条目同时按**精确表名**和**子串包含（contains）**两种方式匹配，如 `_report_` 会匹配所有表名中包含该关键词的表
3. **Merge HistoryLoadTables**（按业务线自动选择）— 根据 `--hive-db` 自动匹配业务线，并把对应的历史 load 表白名单合并进来（**不受 includes/suffix 过滤影响**）
4. **Excludes** — 用 `--excludes` 关键词子串匹配过滤掉不需要的表

### 业务线 → HistoryLoadTables 映射

`com.netease.yuanqi.doris.config.LoadConfig` 维护各业务线的"历史 load 表"白名单。每次调度时根据 `--hive-db` 名称自动选择：

| `hive-db` 包含子串 | 命中集合 | 当前规模 |
|---|---|---|
| `lofter` | `lofterHistoryLoadTables` | 全量历史 ADS 表 |
| `vc` | `vcHistoryLoadTables` | 预留位（当前为空） |
| 其他 | `Set.empty` | 空集合（仅依赖 includes 选表） |

```scala
// LoadConfig.scala
object LoadConfig {
  val lofterHistoryLoadTables: Set[String] = Set(/* ... */)
  val vcHistoryLoadTables: Set[String] = Set.empty[String]   // 预留

  def getHistoryLoadTables(hiveDb: String): Set[String] = {
    val db = hiveDb.toLowerCase
    if (db.contains("lofter")) lofterHistoryLoadTables
    else if (db.contains("vc")) vcHistoryLoadTables
    else Set.empty[String]
  }
}
```

新增业务线（如 `xx_dm`）的步骤：
1. 在 `LoadConfig.scala` 中添加 `xxHistoryLoadTables: Set[String] = Set(...)`
2. 在 `getHistoryLoadTables` 中添加 `else if (db.contains("xx")) xxHistoryLoadTables` 分支
3. 在 `jobs/hive2doris-xx/` 目录下添加调度脚本，参数 `--hive-db xx_dm --doris-db xx`

AutoSync 参数：

| 参数 | 必需 | 说明 |
|------|:----:|------|
| `--hive-db` | ✓ | 源 Hive 数据库 |
| `--doris-db` | ✗ | 目标 Doris 库（默认取 DorisConfig） |
| `--partition-col` | ✗ | 分区列名（默认 `dt`） |
| `--includes` | ✗ | 逗号分隔的表名或关键词，每个条目同时按精确表名和 `contains` 子串两种方式匹配（如 `dim_user_base,_report_`） |
| `--excludes` | ✗ | 逗号分隔的关键词，表名包含则排除 |
| `--suffix-pattern` | ✗ | 自定义表名后缀正则（默认匹配 `_di/_dd/_7d` 等） |
| `--parallelism` | ✗ | 并发线程数（默认 1） |

## Worklog 分布式锁机制

### worklog 表结构

```sql
CREATE TABLE IF NOT EXISTS `common`.`hive_doris_sync_worklog` (
  `dt`             DATE          NOT NULL COMMENT '同步日期',
  `table_name`     VARCHAR(256)  NOT NULL COMMENT '完整表名 database.table',
  `partition_val`  VARCHAR(256)  NOT NULL COMMENT '分区值, Full模式为__FULL__',
  `action`         VARCHAR(32)   COMMENT 'sync_pending/sync_running/sync_done/sync_failed',
  `uuid`           VARCHAR(64)   COMMENT '执行者标识',
  `message`        STRING        DEFAULT '' COMMENT '附加信息(失败原因等)',
  `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '记录更新时间'
) UNIQUE KEY (`dt`, `table_name`, `partition_val`)
PARTITION BY RANGE (`dt`) ()
DISTRIBUTED BY HASH(`table_name`) BUCKETS AUTO
PROPERTIES (
  "replication_num" = "3",
  "enable_unique_key_merge_on_write" = "true",
  "dynamic_partition.enable" = "true",
  "dynamic_partition.time_unit" = "DAY",
  "dynamic_partition.start" = "-7",
  "dynamic_partition.end" = "3",
  "dynamic_partition.prefix" = "p"
)
```

> **分区策略**：使用动态分区（`dynamic_partition.enable = true`），按天自动创建和管理分区。`dynamic_partition.start = -7` 自动清理 7 天前的过期数据，`dynamic_partition.end = 3` 预创建未来 3 天的分区，无需手动维护。

### 状态机

```
sync_pending ──→ sync_running ──→ sync_done
                     │
                     └──→ sync_failed ──→ (下次调度重试)
```

### 核心机制

1. **注册 pending**：同步开始前注册待同步分区。Auto 模式下若已有 `sync_done`，会比较 worklog `update_time` 与 Hive `updateTime`：
   - Hive 更新时间不晚于 sync_done → 跳过
   - Hive 更新时间晚于 sync_done → **force 覆写为 `sync_pending`，触发重导**
2. **获取锁**：通过 INSERT 将状态改为 `sync_running` + 当前 `uuid`
3. **续租**：后台线程每 120 秒刷新 `update_time`，防止被误判为过期
4. **过期恢复**：启动时扫描 `update_time` 超过 120 秒未刷新的 `sync_running` 记录，恢复为 `sync_failed`

## Schema 演进

`SchemaEvolutionManager` 在同步前自动对比 Hive 和 Doris 的列定义：

### 轻量变更（ALTER TABLE）

| 变更类型 | 处理方式 |
|---------|---------|
| **新增列** | `ALTER TABLE ADD COLUMN` |
| **删除列** | `ALTER TABLE DROP COLUMN` |
| **类型变更**（非 KEY 列） | `ALTER TABLE MODIFY COLUMN` |

### 重量变更（全表重建）

当 **KEY 列** 发生新增、删除或类型变更时，无法通过 ALTER TABLE 处理：

1. 收集当前 Doris 分区数 `D` 和 Hive 分区数 `H`
2. 计算 `N = min(D, H)`
3. 执行 `DROP TABLE` + `CREATE TABLE`（最新 Hive Schema）
4. 重新导入 Hive 最新 `N` 个分区的数据

## 数据类型映射

映射链路：**Hive Type → Spark Type → Doris Type**

| Hive 类型 | Doris 类型 | 说明 |
|-----------|------------|------|
| `boolean` | `BOOLEAN` | |
| `tinyint` | `TINYINT` | |
| `smallint` | `SMALLINT` | |
| `int` / `integer` | `INT` | |
| `bigint` | `BIGINT` | |
| `float` | `FLOAT` | |
| `double` | `DOUBLE` | |
| `decimal` | `DECIMAL(27, 9)` | 无精度时使用默认值 |
| `decimal(p,s)` | `DECIMAL(p, s)` | p ≤ 38, s ≤ p |
| `char(n)` | `CHAR(n)` | n ≤ 255 |
| `varchar(n)` | `VARCHAR(n)` | n ≤ 65533 |
| `string` | `STRING` | 即 VARCHAR(65533) |
| `binary` | `STRING` | Doris 无原生 BINARY |
| `date` | `DATE` | |
| `timestamp` | `DATETIME` | |
| `array<T>` | `ARRAY<T>` | 递归映射元素类型 |
| `map<K,V>` | `MAP<K, V>` | 递归映射 Key/Value 类型 |
| `struct<...>` | `STRUCT<...>` | 递归映射字段类型 |
| `uniontype<...>` | `STRING` | Doris 不支持 UNION |
| 未知类型 | `STRING` | 兜底 fallback |

> 映射逻辑见 `DorisTypeMapper.scala`。Doris 3.x 约束：DECIMAL max_precision=38, CHAR max=255, VARCHAR max=65533。

## DDL 生成规则

`DorisDDLGenerator` 生成的 CREATE TABLE DDL 遵循以下规则：

| 规则 | 说明 |
|------|------|
| 数据模型 | `DUPLICATE KEY` — 适合批量数据导入场景 |
| 分区方式 | `PARTITION BY RANGE (\`dt\`) ()` + `dynamic_partition.enable=true` — Doris 动态管理分区 |
| 分桶方式 | `DISTRIBUTED BY HASH(...) BUCKETS AUTO` — Doris 自动决定桶数 |
| KEY 列选取 | 分区列占首位，再从普通列中选取前 N 个 KEY 兼容类型列（默认共 3 列） |
| KEY 列约束 | KEY 列必须是列定义的有序前缀；ARRAY/MAP/STRUCT/JSON/VARIANT 等不兼容 |
| STRING → KEY | STRING 类型列若选为 KEY 则自动替换为 `VARCHAR(4096)` |
| 分区列 | 强制 `DATE NOT NULL` 类型，放在列定义第一位且占 KEY 首位 |
| 动态分区 | 所有分区表启用 `dynamic_partition`（end=0）；单分区 ≥ 10GB 时额外启用 `start=-30` 清理 30 天前历史 |
| 副本数 | 默认 `replication_num = 3` |

## 异常分类与重试

`RetryPolicy` 对所有异常进行分类并推荐恢复策略：

| 异常类型 | 推荐动作 | 示例 |
|---------|---------|------|
| `SchemaException(rebuild=true)` | `RebuildTable` — DROP + CREATE | KEY 列变更 |
| `SchemaException(rebuild=false)` | `RetryFullTable` | 列类型不兼容 |
| `NetworkException` | `RetryFullTable` | JDBC 超时、FE 不可用 |
| `DataException(partitions)` | `RetryPartitions` | Stream Load 部分分区失败 |
| `DataException(无分区)` | `RetryFullTable` | 数据质量问题 |
| `LeaseException` | `Skip` | 另一个实例正在同步 |
| `TimeoutException` | `RetryFullTable` | 同步超时 |
| `UnknownSyncException` | `Fail` | 未分类异常 |

默认最多重试 3 次，退避基数 5 秒（指数递增：5s → 10s → 20s）。

## 功能测试

`Hive2DorisTest` 提供 6 组测试，无需任何测试框架：

```bash
# 方式1: IDE 直接运行 main 方法
# 方式2: spark-submit
spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisTest hive2doris-assembly.jar
```

| 测试组 | 内容 |
|--------|------|
| 1. DorisTypeMapper | 简单类型、参数化类型、复杂嵌套类型、未知类型 fallback |
| 2. splitTopLevelComma | 泛型嵌套下的逗号正确分割 |
| 3. DorisDDLGenerator | 分区表/非分区表/复杂类型/动态清理 DDL 验证 |
| 4. SyncMode | 4 种模式解析 + 非法值异常 |
| 5. Args | CLI 参数解析、必选/可选/布尔标志、缺失异常 |
| 6. DorisDDLExecute | **实际连接 Doris**：DROP + CREATE 5 种表类型，验证 DDL 可执行 |

> 测试 6 需要 Doris 实例在线；不可达时自动跳过，不影响其他测试。

## Hive Bitmap → Doris BITMAP 同步

`Hive2DorisBitmapSynchronizer` 是独立的 Bitmap 数据同步入口，将 Hive 中以 `Roaring64NavigableMap`（PORTABLE 序列化）存储的 bitmap 数据导入 Doris `BITMAP` 列。支持**多 bitmap 列**同步和**自动内存优化**。

### 背景

Hive bitmap 和 Doris bitmap 底层都基于 RoaringBitmap，但二者的序列化格式不兼容，**不能通过 JDBC 直接写入**。因此需要在 Spark 端反序列化 Hive bitmap，转换为 Doris 支持的输入格式后通过 Spark Doris Connector（Stream Load）写入。

### 两种写入模式

#### StringFunc 模式（默认，推荐，支持多 bitmap 列）

将 bitmap 分批转为逗号分隔的 ID 字符串（每批 50K 个 ID），通过 Doris `bitmap_from_string()` 函数解析。多批次由 Doris `BITMAP_UNION` 聚合自动合并为完整 bitmap。

```
Hive Row: (dt, tag, v1_bitmap[150K], v2_bitmap[30K])
  ↓ 每列独立反序列化 + 分批转为 ID 字符串
批次1: (dt, tag, "v1_ids_1-50K",   "v2_ids_1-30K")    ← 两列都有数据
批次2: (dt, tag, "v1_ids_50K-100K", "v2_lastId")      ← v2 耗尽，填充幂等 ID
批次3: (dt, tag, "v1_ids_100K-150K","v2_lastId")      ← 同上
  ↓ Spark Doris Connector
doris.write.fields = "dt,tag,v1,v2,v1=bitmap_from_string(v1),v2=bitmap_from_string(v2)"
  ↓ Doris AGGREGATE KEY + BITMAP_UNION 聚合
```

**优点**：行数膨胀可控（每批 50K 个 ID），支持多 bitmap 列，流式 Iterator 低内存开销  
**缺点**：相比 Expand 模式略复杂

#### Expand 模式（仅限单 bitmap 列）

将每个 bitmap 展开为 N 行（每个 userId 一行），通过 Doris `to_bitmap()` 函数重建 bitmap。

```
Hive Row: (dt, tag, dim1, bitmap_bytes[100个userId])
  ↓ 反序列化 + flatMap 展开为 100 行
(dt, tag, dim1, userId_1)
(dt, tag, dim1, userId_2)
...
(dt, tag, dim1, userId_100)
  ↓ Spark Doris Connector
doris.write.fields = "dt,tag,dim1,bitmap,bitmap=to_bitmap(bitmap)"
  ↓ Doris AGGREGATE KEY + BITMAP_UNION 聚合
```

**优点**：简单直接，无字符串长度限制  
**缺点**：bitmap 基数很大时行数膨胀（1 行 → 百万行）；**不支持多 bitmap 列**（会产生笛卡尔积）

### 多 bitmap 列对齐策略

当一行包含多个 bitmap 列时（如 `uv_7d_bitmap`, `uv_30d_bitmap`），各列基数不同会导致分批数量不一致。

**解决方案——幂等填充**：按最长批次对齐，已耗尽的列用该列**最后一批的第一个 ID** 填充。

```
v1: 150K IDs → 3 批    v2: 30K IDs → 1 批

批次1: v1="id_1,...,id_50K"     v2="id_1,...,id_30K"     ← 正常
批次2: v1="id_50K+1,...,id_100K" v2="id_1"               ← v2 用最后一批第一个 ID 填充
批次3: v1="id_100K+1,...,id_150K" v2="id_1"              ← 同上
```

**为什么安全？**
- `bitmap_from_string("id_1")` 产出 bitmap `{id_1}`，该 ID 已在第一批中存在
- `BITMAP_UNION` 的幂等性：`A ∪ {id_1} ∪ {id_1} = A ∪ {id_1}`
- 重复写入不影响最终聚合结果

**技术优势**：
- `doris.write.fields` 始终保持静态不变，无需按批次动态切换
- 完全保持 Iterator 流式处理，无 cache、无分组、低内存开销

### 自动建表

如果目标 Doris 表不存在，`Hive2DorisBitmapSynchronizer` 会根据 DataFrame schema **自动建表**：

| 规则 | 说明 |
|------|------|
| 数据模型 | `AGGREGATE KEY`（BITMAP 列必须使用聚合模型） |
| BITMAP 列 | 声明为 `BITMAP BITMAP_UNION`，**始终放在最后一列** |
| KEY 列 | 所有非 BITMAP 列均作为 AGGREGATE KEY |
| 分区方式 | 如有 `dt`(DATE) 列，使用 `AUTO PARTITION BY RANGE` |
| 列类型映射 | KEY 列全部加 `NOT NULL`，STRING → `VARCHAR(256)` |

示例自动生成的 DDL：

```sql
CREATE TABLE IF NOT EXISTS `lofter`.`user_portrait_datas_bitmap` (
  `dt` DATE NOT NULL,
  `tag` VARCHAR(256) NOT NULL DEFAULT '',
  `dim1` VARCHAR(256) NOT NULL DEFAULT '',
  `dim2` VARCHAR(256) NOT NULL DEFAULT '',
  `users_bitmap` BITMAP BITMAP_UNION COMMENT 'bitmap column'
)
AGGREGATE KEY(`dt`, `tag`, `dim1`, `dim2`)
AUTO PARTITION BY RANGE (date_trunc(`dt`, 'day')) ()
DISTRIBUTED BY HASH(`tag`) BUCKETS AUTO
PROPERTIES (
  "replication_num" = "3",
  "estimate_partition_size" = "10G"
)
```

### 使用方法

```bash
# StringFunc 模式（默认，单 bitmap 列）
spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
  hive2doris-assembly.jar \
  --source "SELECT dt, tag, dim1, dim2, dim3, dim4, grp, bitmap AS users_bitmap
            FROM lofter_dm.ads_lofter_tags_data_bitmap_dd
            WHERE dt = '2026-04-21' AND tag = 'xxx'" \
  --doris-db lofter --doris-table user_portrait_datas_bitmap \
  --bitmap-col users_bitmap \
  --partition 2026-04-21 \
  --parallel 50

# StringFunc 模式（多 bitmap 列）
spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
  hive2doris-assembly.jar \
  --source "SELECT dt, tag, uv_7d_bitmap, uv_30d_bitmap
            FROM lofter_dm.ads_lofter_multi_bitmap_dd
            WHERE dt = '2026-04-21'" \
  --doris-db lofter --doris-table user_portrait_multi_bitmap \
  --bitmap-col uv_7d_bitmap,uv_30d_bitmap \
  --partition 2026-04-21

# Expand 模式（仅限单 bitmap 列）
spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
  hive2doris-assembly.jar \
  --source "SELECT dt, tag, dim1, dim2, dim3, dim4, grp, bitmap AS users_bitmap
            FROM lofter_dm.ads_lofter_tags_data_bitmap_dd
            WHERE dt = '2026-04-21' AND tag = 'xxx'" \
  --doris-db lofter --doris-table user_portrait_datas_bitmap \
  --bitmap-col users_bitmap \
  --mode expand \
  --partition 2026-04-21 \
  --parallel 50
```

### 参数说明

| 参数 | 必需 | 说明 |
|------|:----:|------|
| `--source` | ✓ | Hive 查询 SQL，必须包含 bitmap 列（BinaryType） |
| `--doris-db` | ✗ | 目标 Doris 库（默认取 DorisConfig） |
| `--doris-table` | ✓ | 目标 Doris 表名 |
| `--bitmap-col` | ✗ | bitmap 列名，支持逗号分隔多列（如 `v1,v2`）。默认自动识别 `*_bitmap` 后缀的 BinaryType 列 |
| `--mode` | ✗ | 写入模式：`string`（默认）或 `expand`（仅限单列） |
| `--partition` | ✗ | 分区值（如 `2026-04-21`），指定后会在写入前清理该分区 |
| `--parallel` | ✗ | Spark 并行度（默认 10） |

> **提示**：`--source` 也可通过 Spark 配置 `spark.source.query` 传入，适合在调度系统中使用。
> 
> **多列限制**：`--mode expand` 仅支持单个 bitmap 列，多列场景请使用默认的 StringFunc 模式。

### 核心组件

| 组件 | 职责 |
|------|------|
| `HiveBitmapConverter` | RoaringBitmap 反序列化（PORTABLE 模式）+ 融合迭代器：`deserializeToStringBatches()`（StringFunc 用，按批产出 ID 字符串）/ `deserializeToLongIterator()`（Expand 用，流式逐个产出 userId） |
| `Hive2DorisBitmapSynchronizer` | 主编排：参数解析 → Hive 查询 → bitmap 列识别（支持多列）→ 自动建表（AGGREGATE KEY + BITMAP_UNION）→ 分区清理 → 多列对齐写入 |

### 模式选择建议

| 场景                   | 推荐模式 | 原因 |
|----------------------|---------|------|
| 多 bitmap 列           | StringFunc（默认） | Expand 不支持多列（笛卡尔积） |
| 单列，bitmap 基数 <100K   | 两种均可 | 性能差异不大 |
| 单列，bitmap 基数 100K~10M | 两种均可 | 性能差异不大 |
| 多列，bitmap 基数 >10M    | StringFunc（默认） | 分批写入（每批 50K），内存友好 |
| 数据量小，快速验证            | StringFunc（默认） | 行数膨胀可控，调试方便 |

## 故障排查

| 问题 | 排查方向 |
|------|---------|
| 元数据获取失败 | 检查 MammutMetaService 配置、AKSK、网络连通性 |
| Doris 连接失败 | 检查 DorisConfig 的 jdbcUrl / fenodes / 用户名密码 |
| Schema 转换错误 | 检查 Hive 表是否有不支持的类型，查看 `DorisTypeMapper` 警告日志 |
| Schema Evolution 触发重建 | 查看日志中的 "REBUILDING" 关键词，确认 KEY 列变更详情 |
| Worklog 锁冲突 | 查询 `common.hive_doris_sync_worklog` 表，检查 `sync_running` 记录 |
| Stream Load 失败 | 检查 Doris BE 日志、内存和磁盘资源 |
| 过期恢复未触发 | 确认 `recoverExpiredRunningTasks` 在启动时被调用 |

调试模式（详细日志）：

```bash
spark-submit \
  --conf "spark.driver.extraJavaOptions=-Dlog4j.logger.com.netease.yuanqi.hive2doris=DEBUG" \
  ...
```

## 版本历史

### v3.3.0 (2026-06)
- **新增** 多业务线 `historyLoadTables` 支持
  - `LoadConfig` 拆分为 `lofterHistoryLoadTables` + `vcHistoryLoadTables`（预留）
  - 新增 `getHistoryLoadTables(hiveDb)`：根据 `--hive-db` 子串自动匹配业务线
  - `Hive2DorisAutoSync` Step 3 改为按业务线动态选择，不再固定使用 lofter 白名单
- **改进** Azkaban 调度脚本目录按业务线分组
  - `jobs/hive2doris/` → `jobs/hive2doris-lofter/`（lofter_dm → lofter）
  - 新增 `jobs/hive2doris-vc/`（vc_dm → vc）
- **移除** `LoadConfig.historyLoadTables` 兼容别名（无外部引用）

### v3.2.1 (2026-06)
- **修复** Auto 模式下"sync_done 之后再次变更"的分区被永久跳过的 bug
  - 旧版本：`fetchTodayDonePartitions` 只返回 `partition_val` 集合，`discoverChangedPartitions` 通过 `filterNot` 直接丢弃所有已 sync_done 分区
  - 新版本：`fetchTodayDonePartitionTimes` 返回 `Map[partitionVal -> sync_done update_time]`，与 Hive `partition.updateTime` 严格对比；当 Hive 更新时间晚于 sync_done 时间时，标记 `needForceResync=true`
  - syncTable Auto 分支：对 `needForce` 分区单独 `registerPendingPartitions(force=true)`，覆写 worklog 中的 sync_done 状态触发重导

### v3.2.0 (2026-04)
- **新增** 多 bitmap 列支持：StringFunc 模式下支持同一张表包含多个 BITMAP 列（如 `uv_7d_bitmap`, `uv_30d_bitmap`）
- **新增** 多列对齐策略（幂等填充）：按最长批次对齐，已耗尽列用最后一批的第一个 ID 填充，利用 `BITMAP_UNION` 幂等性保证结果正确
- **新增** `--bitmap-col` 参数支持逗号分隔多列（如 `--bitmap-col v1,v2`）
- **改进** 默认模式改为 StringFunc（分批写入，内存友好），Expand 模式仅限单 bitmap 列
- **改进** 融合迭代器 `deserializeToStringBatches()` / `deserializeToLongIterator()`：bitmap 对象不逃逸，降低 GC 压力
- **改进** Expand 模式增加多列校验：检测到多列时直接拒绝并提示使用 StringFunc 模式

### v3.1.0 (2026-04)
- **新增** Hive Bitmap → Doris BITMAP 同步功能（`Hive2DorisBitmapSynchronizer`）
- **新增** 两种写入模式：Expand（`to_bitmap`）和 StringFunc（`bitmap_from_string`），均通过 Spark Doris Connector 的 `doris.write.fields` 实现
- **新增** Bitmap 表自动建表：AGGREGATE KEY 模型 + BITMAP_UNION 聚合列 + bitmap 列置末位
- **新增** `HiveBitmapConverter` 工具类：Hive RoaringBitmap（PORTABLE 模式）反序列化 + 格式转换
- **新增** 依赖 `org.roaringbitmap:RoaringBitmap:0.9.45`

### v3.0.1 (2026-04)
- **修复** DROP PARTITION 分区命名格式不一致导致数据残留的 bug（AUTO PARTITION 用 `pYYYYMMDD000000`，DYNAMIC PARTITION 用 `pYYYYMMDD`，现在同时删除两种格式）
- **改进** 优化Auto PARTITION、DYNAMIC PARTITION的生成逻辑

### v3.0.0 (2026-03)
- **架构重构** 全新实现，10 个源文件精简到位
- **新增** 4 种同步模式：full / partition / partition_range / auto
- **新增** Worklog 分布式锁 + 续租机制 + 过期恢复
- **新增** Schema Evolution + KEY 列重建兜底方案
- **新增** AutoSync 批量入口（表发现 + 4 步过滤链 + 并发支持）
- **新增** 动态分区清理（`dynamic_partition.start = -30`，分区 ≥ 10GB 时启用）
- **新增** 功能测试类（DDL 生成验证 + Doris 实际建表验证）
- **改进** 类型映射：ARRAY/MAP/STRUCT 保持原生类型（不再统一转 JSON）
- **改进** DDL 生成：AUTO PARTITION + KEY 有序前缀约束 + STRING→VARCHAR(4096) KEY 替换

### v2.0.0 (2026-03)
- Auto 同步模式、Schema 演进、上游依赖检查
- 异常分类与指数退避重试

### v1.0.0 (2026-03)
- 基础功能实现、猛犸元数据中心集成、分区导入支持

## 联系

- 维护者: zhaozijun03@corp.netease.com