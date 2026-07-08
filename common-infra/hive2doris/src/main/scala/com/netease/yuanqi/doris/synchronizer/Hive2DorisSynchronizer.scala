package com.netease.yuanqi.doris.synchronizer

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.schema.{DorisDDLGenerator, SchemaEvolutionManager}
import com.netease.yuanqi.metaservice.{ColumnInfo, FieldInfo, MammutMetaService}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

import java.sql.{Connection, DriverManager, ResultSet}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

// ─── Sync Mode ──────────────────────────────────────────────────────────────────

/**
 * Sync mode controls how data is written to Doris.
 *
 * - Full:           全量同步 — 删除当前 Doris 表，重新获取 Hive 表结构建表，导入全部分区数据。
 * - Partition:      单分区同步 — 表不存在则新建，将 Hive 指定的单分区数据导入 Doris。
 * - PartitionRange: 分区范围同步 — 表不存在则新建，导入 Hive 指定分区范围内的数据。
 * - Auto:           自动同步 — 自动发现 Hive 分区 updateTime 为当天的分区，重新获取表结构，
 *                   表不存在则新建，导入所有变更分区数据。
 */
sealed trait SyncMode

object SyncMode {
  case object Full           extends SyncMode { override def toString: String = "full" }
  case object Partition      extends SyncMode { override def toString: String = "partition" }
  case object PartitionRange extends SyncMode { override def toString: String = "partition_range" }
  case object Auto           extends SyncMode { override def toString: String = "auto" }

  def fromString(s: String): SyncMode = s.toLowerCase match {
    case "full"            => Full
    case "partition"       => Partition
    case "partition_range" | "partitionrange" => PartitionRange
    case "auto"            => Auto
    case other             => throw new IllegalArgumentException(
      s"Unknown sync mode: '$other'. Use 'full', 'partition', 'partition_range', or 'auto'."
    )
  }
}

// ─── Hive → Doris Synchronizer ──────────────────────────────────────────────────

/**
 * Hive → Doris 同步器。
 *
 * 核心职责：
 *   1. 根据 SyncMode 决定同步行为（全量 / 单分区 / 分区范围 / 自动发现变更分区）。
 *   2. 通过 MammutMetaService 获取 Hive 表结构和分区元信息。
 *   3. 通过 DorisDDLGenerator 生成建表 DDL。
 *   4. 通过 Spark doris-spark-connector 将数据从 Hive 写入 Doris。
 *
 * @param hiveDatabase  source Hive database
 * @param dorisDatabase target Doris database
 * @param partitionCol  partition column name (e.g. "dt")
 * @param metaService   MammutMetaService for schema & partition introspection
 */
class Hive2DorisSynchronizer(
  hiveDatabase: String,
  dorisDatabase: String,
  partitionCol: Option[String],
  metaService: MammutMetaService
) {

  private val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2DorisSynchronizer])

  private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  // ─── Worklog Constants ───────────────────────────────────────────────────

  /** worklog 表所在的 Doris 数据库 */
  private val WORKLOG_DB = "common"
  /** worklog 表名 */
  private val WORKLOG_TABLE = "hive_doris_sync_worklog"
  /** Full 模式下的分区占位符 */
  private val FULL_PARTITION_MARKER = "__FULL__"
  /** 租约刷新间隔（秒） */
  private val LEASE_REFRESH_INTERVAL_SEC = 120

  /** 后台续租线程 */
  @volatile private var leaseRefresher: Option[java.util.concurrent.ScheduledExecutorService] = None

  // ─── JDBC Helpers ─────────────────────────────────────────────────────────

  /** Get a JDBC connection to Doris FE (MySQL protocol). */
  private def getConnection: Connection = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    DriverManager.getConnection(DorisConfig.jdbcUrl, DorisConfig.user, DorisConfig.password)
  }

  /** Execute a single DDL/DML statement. */
  private def executeSql(sql: String): Unit = {
    val conn = getConnection
    try {
      LOG.info(s"Executing SQL:\n$sql")
      conn.createStatement().execute(sql)
    } finally {
      conn.close()
    }
  }

  /** Execute a query and return a single Long value (first column, first row). */
  private def queryLong(sql: String): Long = {
    val conn = getConnection
    try {
      val rs = conn.createStatement().executeQuery(sql)
      if (rs.next()) rs.getLong(1) else 0L
    } finally {
      conn.close()
    }
  }

  /** Execute a query and process the ResultSet via a callback. */
  private def queryWith[T](sql: String)(handler: ResultSet => T): T = {
    val conn = getConnection
    try {
      val rs = conn.createStatement().executeQuery(sql)
      handler(rs)
    } finally {
      conn.close()
    }
  }

  /** Check whether a table already exists in the target Doris database. */
  private def dorisTableExists(table: String): Boolean = {
    val sql =
      s"""SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES
         |WHERE TABLE_SCHEMA = '$dorisDatabase' AND TABLE_NAME = '$table'""".stripMargin
    queryLong(sql) > 0
  }

  // ─── Schema Evolution ─────────────────────────────────────────────────────

  /** SchemaEvolutionManager 实例（延迟初始化）。 */
  private lazy val schemaEvolutionManager = new SchemaEvolutionManager(dorisDatabase, metaService)

  // ─── Schema Introspection ─────────────────────────────────────────────────

  /**
   * 通过 MammutMetaService 获取 Hive 表的完整字段元信息（含分区列）。
   *
   * '''工作原理'''：
   *   - `getFieldInfo()` 返回非分区列
   *   - `getTableDetailV3().tableMetaInfo.columnInfos` 返回所有列（含分区列及其真实类型）
   *   - 两者求差集自动识别分区列，合并为完整字段列表
   *
   * '''分区列标记规则'''：
   *   - 主分区列（dt）→ partitionKey=true，参与 Doris AUTO PARTITION
   *   - 额外分区列（period）→ partitionKey=false，作为普通列参与 DUPLICATE KEY
   */
  private def fetchHiveFields(table: String): List[FieldInfo] = {
    // 1. 获取非分区列
    val nonPartFields = metaService.getFieldInfo(hiveDatabase, table)
    require(nonPartFields.nonEmpty, s"No fields found for $hiveDatabase.$table via MammutMetaService")
    LOG.info(s"Fetched ${nonPartFields.size} non-partition fields for $hiveDatabase.$table")

    // 2. 获取所有列（含分区列及类型）
    val allColumnInfos = try {
      val tableInfo = metaService.getTableDetailV3(hiveDatabase, table, "hive")
      tableInfo.tableMetaInfo.map(_.columnInfos).getOrElse(Nil)
    } catch {
      case e: Exception =>
        LOG.warn(s"Failed to get columnInfos from getTableDetailV3 for $hiveDatabase.$table: " +
          s"${e.getMessage}. Partition columns may be missing.")
        Nil
    }
    LOG.info(s"Fetched ${allColumnInfos.size} total columns from getTableDetailV3 for $hiveDatabase.$table")

    // 3. 差集方式合并，自动补充分区列（含真实类型）
    val enrichedFields = Hive2DorisSynchronizer.buildFieldsWithPartitionCols(
      nonPartFields, allColumnInfos, partitionCol
    )

    LOG.info(s"Final field count for $hiveDatabase.$table: ${enrichedFields.size} " +
      s"(nonPart=${nonPartFields.size}, allCols=${allColumnInfos.size}, " +
      s"partitionCol=${partitionCol.getOrElse("none")})")
    enrichedFields
  }

  /**
   * 获取 Doris 表当前所有分区对应的日期值列表。
   *
   * Doris 中同一张表可能存在两种分区命名格式：
   *   - AUTO PARTITION 格式: `pYYYYMMDD000000`（如 `p20260311000000`）
   *   - DYNAMIC PARTITION 格式: `pYYYYMMDD`（如 `p20260311`）
   * 本方法从 INFORMATION_SCHEMA.PARTITIONS 查询分区名，使用 [[parsePartitionNameToDate]] 兼容解析两种格式，
   * 返回 `yyyy-MM-dd` 格式的日期字符串列表。
   *
   * @param table Doris 表名
   * @return 分区对应的日期值列表（例如 ["2026-03-11", "2026-03-13"]）；表不存在时返回空列表
   */
  private def getDorisPartitionValues(table: String): List[String] = {
    val sql =
      s"""SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS
         |WHERE TABLE_SCHEMA = '$dorisDatabase'
         |  AND TABLE_NAME = '$table'
         |  AND PARTITION_NAME IS NOT NULL
         |  AND PARTITION_NAME != ''""".stripMargin

    val conn = getConnection
    try {
      val rs = conn.createStatement().executeQuery(sql)
      val partNames = scala.collection.mutable.ListBuffer.empty[String]
      while (rs.next()) {
        partNames += rs.getString("PARTITION_NAME")
      }

      // 解析 pYYYYMMDD000000 → yyyy-MM-dd
      val dateValues = partNames.flatMap(parsePartitionNameToDate).toList.sorted
      LOG.info(s"Doris table $dorisDatabase.$table has ${dateValues.size} partitions: ${dateValues.mkString(", ")}")
      dateValues
    } finally {
      conn.close()
    }
  }

  /**
   * 解析 Doris 分区名为日期字符串，兼容两种分区命名格式。
   *
   * Doris 中同一张表可能存在两种分区命名格式：
   *   - AUTO PARTITION 格式:    `pYYYYMMDD000000` → `YYYY-MM-DD`（如 `p20260311000000` → `2026-03-11`）
   *   - DYNAMIC PARTITION 格式: `pYYYYMMDD` → `YYYY-MM-DD`（如 `p20260311` → `2026-03-11`）
   *
   * 本方法使用 `\d*` 通配尾部数字，可同时匹配两种格式。
   *
   * @param partName Doris 分区名
   * @return Some(日期字符串) 或 None（格式不匹配时）
   */
  private def parsePartitionNameToDate(partName: String): Option[String] = {
    // 匹配 pYYYYMMDD 后面可选跟 0~6 个数字（兼容 AUTO PARTITION 的 000000 后缀和 DYNAMIC PARTITION 的无后缀）
    val pattern = """^p(\d{4})(\d{2})(\d{2})\d*$""".r
    partName match {
      case pattern(year, month, day) => Some(s"$year-$month-$day")
      case _ =>
        LOG.warn(s"Unable to parse partition name '$partName' to date value, skipping")
        None
    }
  }

  /**
   * 获取 Hive 表的所有分区值，按降序排列（最新的在前面）。
   *
   * 分区值提取方式与 [[discoverChangedPartitions]] 保持一致：
   * partition 格式可能是 "dt=2026-03-11" 或直接 "2026-03-11"。
   *
   * @param table Hive 表名
   * @return 分区值列表，降序排列
   */
  private def getHivePartitionsSorted(table: String): List[String] = {
    val allPartitions = metaService.getPartitionList(hiveDatabase, table)

    val partValues = allPartitions.flatMap { pi =>
      pi.partition.map { p =>
        if (p.contains("=")) p.split("=", 2).last else p
      }
    }.sorted.reverse // 降序 → 最新的在前面

    LOG.info(s"Hive table $hiveDatabase.$table has ${partValues.size} partitions (sorted desc)")
    partValues
  }

  // ─── Partition Size Estimation ────────────────────────────────────────────

  /** Number of recent partitions sampled for size estimation. */
  private val PARTITION_SIZE_SAMPLE_COUNT = 7

  /**
   * 估算 Hive 表单分区的平均数据大小（字节）。
   *
   * 策略：获取 Hive 分区列表，按分区名降序排列，取最近 N 个分区的 totalSize 求平均值。
   *   - 若分区数不足 N，则取实际分区数的平均值。
   *   - 若表无分区（非分区表或 `partitionCol` 为 None），返回 0。
   *   - 若分区的 `totalSize` 为 None，该分区不参与计算。
   *
   * @param table Hive table name
   * @return estimated single partition size in bytes; 0 if non-partitioned or no data
   */
  private[doris] def estimateSinglePartitionSize(table: String): Long = {
    if (partitionCol.isEmpty) {
      LOG.info(s"Non-partitioned table $hiveDatabase.$table — skip partition size estimation")
      return 0L
    }

    val allPartitions = metaService.getPartitionList(hiveDatabase, table)
    if (allPartitions.isEmpty) {
      LOG.info(s"No partitions found for $hiveDatabase.$table — returning 0")
      return 0L
    }

    // 按分区名降序排列，取最近 N 个
    val sorted = allPartitions
      .filter(_.partition.isDefined)
      .sortBy(_.partition.get)(Ordering[String].reverse)
      .take(PARTITION_SIZE_SAMPLE_COUNT)

    // 提取有 totalSize 的分区
    val sizes = sorted.flatMap(_.totalSize)
    if (sizes.isEmpty) {
      LOG.warn(s"All sampled partitions for $hiveDatabase.$table have no totalSize — returning 0")
      return 0L
    }

    val avgSize = sizes.sum / sizes.size
    LOG.info(s"Estimated single partition size for $hiveDatabase.$table: " +
      s"${avgSize} bytes (${avgSize / 1024 / 1024} MB) — " +
      s"sampled ${sizes.size}/${sorted.size} partitions with totalSize data")
    avgSize
  }

  // ─── Table DDL Management ─────────────────────────────────────────────────

  /**
   * 在 Doris 中创建表（IF NOT EXISTS，幂等）。
   *
   * 建表前自动评估 Hive 分区大小，若单分区平均值 >= 10G 则启用 dynamic_partition 动态清理。
   */
  private def ensureDorisTable(
    table: String,
    fields: List[FieldInfo],
    excludeKeyColumns: Set[String] = Set.empty,
    columnTypeOverrides: Map[String, String] = Map.empty
  ): Unit = {
    val enableDynamicCleanup = if (partitionCol.isDefined) {
      val partSizeBytes = estimateSinglePartitionSize(table)
      val enable = DorisDDLGenerator.shouldEnableDynamicCleanup(partSizeBytes)
      LOG.info(s"Dynamic cleanup decision for $table: partitionSize=${partSizeBytes} bytes, " +
        s"threshold=${DorisDDLGenerator.DYNAMIC_CLEANUP_THRESHOLD_BYTES} bytes, enable=$enable")
      enable
    } else {
      false
    }

    val ddl = DorisDDLGenerator.generateCreateTable(
      database = dorisDatabase,
      tableName = table,
      fields = fields,
      partitionCol = partitionCol,
      enableDynamicCleanup = enableDynamicCleanup,
      excludeKeyColumns = excludeKeyColumns,
      columnTypeOverrides = columnTypeOverrides
    )
    executeSql(ddl)
  }

  /**
   * 删除 Doris 表。
   */
  private def dropDorisTable(table: String): Unit = {
    val sql = s"DROP TABLE IF EXISTS `$dorisDatabase`.`$table`"
    executeSql(sql)
    LOG.info(s"Dropped Doris table $dorisDatabase.$table")
  }

  // ─── Partition Discovery (Auto Mode) ──────────────────────────────────────

  /**
   * 自动发现 Hive 表中 updateTime 为当天（程序执行日期）的变更分区。
   *
   * 逻辑：通过 MammutMetaService.getPartitionList 获取所有分区，
   * 筛选 updateTime 的日期部分等于今天的分区。
   *
   * @param table Hive table name
   * @return 变更分区值列表（e.g. List("dt=2026-03-11")中提取的 "2026-03-11"）
   */
  /**
   * 从 worklog 批量读取该表今天已 sync_done 的分区值 → sync_done 时间映射（一次 JDBC 查询）。
   * 用于在 discoverChangedPartitions 阶段判断：
   *   - Hive partition.updateTime > worklog.update_time → 已 sync_done 但又被更新，需要 force 重导
   *   - Hive partition.updateTime <= worklog.update_time → 已 sync_done 且未变更，跳过
   */
  private def fetchTodayDonePartitionTimes(table: String): Map[String, LocalDateTime] = {
    val fullTableName = s"$hiveDatabase.$table"
    queryWith(
      s"""SELECT `partition_val`, `update_time`
         |FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |WHERE `dt` = CURDATE()
         |  AND `table_name` = '$fullTableName'
         |  AND `action` = 'sync_done'
         |""".stripMargin
    ) { rs =>
      val buf = Map.newBuilder[String, LocalDateTime]
      while (rs.next()) {
        val pv = rs.getString("partition_val")
        val ts = rs.getTimestamp("update_time")
        if (pv != null && ts != null) buf += (pv -> ts.toLocalDateTime)
      }
      buf.result()
    }
  }

  /**
   * 发现需要同步的分区。
   *
   * 返回 `List[(partitionVal, needForceResync)]`：
   *   - `needForceResync = false` → worklog 无记录、或非 sync_done 状态，正常注册 pending
   *   - `needForceResync = true`  → worklog 已 sync_done，但 Hive partition.updateTime
   *                                 比 worklog.update_time 更晚（说明 sync_done 之后 Hive 又更新了），
   *                                 需要强制覆写 worklog 并重新同步
   *
   * 判定逻辑：
   *   1. 从 MammutMetaService 拉取全量分区
   *   2. 筛选 updateTime 落在今天的分区
   *   3. 与 worklog 中今天的 sync_done 记录对比：
   *      - 无 sync_done 记录    → 纳入，needForce=false
   *      - 有 sync_done 记录    →
   *        - Hive updateTime  > worklog.update_time → 纳入，needForce=true
   *        - Hive updateTime <= worklog.update_time → 跳过
   */
  private def discoverChangedPartitions(table: String): List[(String, Boolean)] = {
    val today = LocalDate.now()
    LOG.info(s"Auto mode: discovering partitions with updateTime = $today for $hiveDatabase.$table")

    // Step A: 批量预读 worklog，获取今天已 sync_done 的分区 → sync_done 时间映射
    val alreadyDoneTimes: Map[String, LocalDateTime] = try {
      val done = fetchTodayDonePartitionTimes(table)
      if (done.nonEmpty) {
        LOG.info(s"Auto mode: pre-fetched ${done.size} already sync_done partition(s) from worklog: " +
          s"${done.map { case (pv, t) => s"$pv@$t" }.mkString(", ")}")
      }
      done
    } catch {
      case NonFatal(e) =>
        // worklog 查询失败（如表不存在）时不阻断主流程，降级为不过滤
        LOG.warn(s"Auto mode: failed to pre-fetch sync_done partitions from worklog, skipping pre-filter: ${e.getMessage}")
        Map.empty[String, LocalDateTime]
    }

    // Step B: 拉取全量分区，筛选 updateTime == today 的分区
    val allPartitions = metaService.getPartitionList(hiveDatabase, table)
    LOG.info(s"Auto mode: fetched ${allPartitions.size} partitions from MammutMetaService")

    // 提取 (partitionVal, hiveUpdateTime) 元组
    val changedTodayWithTime: List[(String, LocalDateTime)] = allPartitions.flatMap { pi =>
      pi.updateTime.flatMap { ut =>
        try {
          // updateTime 可能是 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-dd" 格式
          val hiveUpdateDateTime: LocalDateTime = if (ut.contains(" ")) {
            LocalDateTime.parse(ut, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
          } else {
            LocalDate.parse(ut, DATE_FORMAT).atStartOfDay()
          }
          if (hiveUpdateDateTime.toLocalDate.isEqual(today)) {
            // 提取分区值（"dt=2026-03-11" → "2026-03-11"）
            pi.partition.map { p =>
              val pv = if (p.contains("=")) p.split("=", 2).last else p
              (pv, hiveUpdateDateTime)
            }
          } else None
        } catch {
          case NonFatal(e) =>
            LOG.warn(s"Failed to parse updateTime '$ut' for partition ${pi.partition.getOrElse("?")}: ${e.getMessage}")
            None
        }
      }
    }

    // Step C: 与 worklog sync_done 时间对比，标记是否需要 force 重导
    val (skipped, toSync) = changedTodayWithTime.foldLeft(
      (List.empty[String], List.empty[(String, Boolean)])
    ) { case ((skippedAcc, toSyncAcc), (pv, hiveTime)) =>
      alreadyDoneTimes.get(pv) match {
        case Some(workLogTime) if !hiveTime.isAfter(workLogTime) =>
          // 已 sync_done 且 Hive 未在之后更新 → 跳过
          (pv :: skippedAcc, toSyncAcc)
        case Some(workLogTime) =>
          // 已 sync_done 但 Hive 之后又更新了 → 需要 force 重导
          LOG.info(s"Auto mode: partition $pv was sync_done@$workLogTime but Hive updated@$hiveTime, marking for force re-sync")
          (skippedAcc, (pv, true) :: toSyncAcc)
        case None =>
          // worklog 无 sync_done 记录 → 正常注册
          (skippedAcc, (pv, false) :: toSyncAcc)
      }
    }

    if (skipped.nonEmpty) {
      LOG.info(s"Auto mode: skipped ${skipped.size} already sync_done partition(s) (Hive not updated since): " +
        s"${skipped.mkString(", ")}")
    }

    val result = toSync.reverse
    val forceCount = result.count(_._2)
    LOG.info(s"Auto mode: found ${result.size} changed partitions (${forceCount} need force re-sync): " +
      s"${result.map { case (pv, force) => if (force) s"$pv[force]" else pv }.mkString(", ")}")
    result
  }

  // ─── Data Sync: Spark → Doris ─────────────────────────────────────────────

  /**
   * 使用 Spark 读取 Hive 数据，通过 doris-spark-connector 写入 Doris。
   *
   * @param table      Hive/Doris table name
   * @param partitions 分区值列表，为空则全量读取
   * @param spark      SparkSession
   */
  private def syncData(table: String, partitions: List[String], spark: SparkSession): Unit = {
    val partColName = partitionCol.getOrElse("dt")
    val dorisTarget = s"$dorisDatabase.$table"
    val isFullMode = partitions.isEmpty

    // 构建 Hive 读取 SQL
    val readSql = if (isFullMode) {
      s"SELECT * FROM $hiveDatabase.$table"
    } else {
      val partFilter = partitions.map(p => s"'$p'").mkString(", ")
      s"SELECT * FROM $hiveDatabase.$table WHERE `$partColName` IN ($partFilter)"
    }

    LOG.info(s"Reading Hive data: $readSql")
    val rawDf = spark.sql(readSql)

    // 将分区列从 STRING 显式 cast 为 DateType，避免 ANSI storeAssignmentPolicy 下
    // 写入 Doris DATE 类型列时报 "Cannot safely cast 'dt': string to date"
    import org.apache.spark.sql.functions.{col, to_date, to_json}
    import org.apache.spark.sql.types.{ArrayType => SparkArrayType, MapType => SparkMapType, StructType => SparkStructType}
    val partCastedDf = partitionCol match {
      case Some(pc) if rawDf.schema.fieldNames.contains(pc) =>
        LOG.info(s"Casting partition column '$pc' from StringType to DateType")
        rawDf.withColumn(pc, to_date(col(pc), "yyyy-MM-dd"))
      case _ => rawDf
    }

    // ── 复杂类型列转 JSON 字符串 ────────────────────────────────────────
    //
    // spark-doris-connector 在读取 Doris 表 schema 时，会将 Doris 的 ARRAY/MAP/STRUCT 类型
    // 映射为 Spark 的 StringType。而 Hive 读出的 DataFrame 中这些列是 ArrayType/MapType/StructType，
    // 导致 Spark DataSource V2 写入前的 schema 校验报 "incompatible" 错误。
    //
    // 解决方案：写入前将所有复杂类型列用 to_json() 转为 StringType（JSON 字符串）。
    // Doris stream_load 的 JSON 格式支持自动将 JSON 数组/对象解析写入 ARRAY/MAP/STRUCT 列。
    //   例如: WrappedArray("崩坏星穹铁道", "波枝") → '["崩坏星穹铁道","波枝"]' → Doris ARRAY<STRING>
    //
    val complexFields = partCastedDf.schema.fields.filter { f =>
      f.dataType.isInstanceOf[SparkArrayType] ||
      f.dataType.isInstanceOf[SparkMapType] ||
      f.dataType.isInstanceOf[SparkStructType]
    }
    val df = if (complexFields.nonEmpty) {
      LOG.info(s"Converting ${complexFields.length} complex-type columns to JSON strings: " +
        s"${complexFields.map(f => s"${f.name}(${f.dataType.simpleString})").mkString(", ")}")
      complexFields.foldLeft(partCastedDf) { (accDf, field) =>
        accDf.withColumn(field.name, to_json(col(field.name)))
      }
    } else {
      partCastedDf
    }

    val rowCount = df.count()
    LOG.info(s"Read $rowCount rows from Hive for table $table")

    if (rowCount == 0) {
      LOG.warn(s"No data read from Hive for table $table, skipping write")
      return
    }

    // ── 写入模式选择 ─────────────────────────────────────────────────────
    //
    // Doris Spark Connector 的 SaveMode.Overwrite = TRUNCATE TABLE + INSERT（整表覆写）。
    // 分区模式下如果使用 Overwrite，会导致写入一个分区时清空整个表的所有分区数据！
    //
    // 因此：
    //   - Full 模式：使用 SaveMode.Overwrite（语义正确：全量替换）
    //   - 分区模式：先 DROP PARTITION 删除目标分区，再用 SaveMode.Append 追加写入
    //     （AUTO PARTITION 会在写入时自动重建分区，无需手动 ADD PARTITION）
    //
    val saveMode = if (isFullMode) {
      LOG.info(s"Full mode: using SaveMode.Overwrite (TRUNCATE + INSERT)")
      SaveMode.Overwrite
    } else {
      // 分区模式：逐个 DROP PARTITION 删除目标分区（比 DELETE 性能好，直接删除物理文件）
      // 注意：AUTO PARTITION 和 DYNAMIC PARTITION 的分区名格式不同：
      //   - AUTO PARTITION (数据写入时创建): pYYYYMMDD000000（如 p20260311000000）
      //   - DYNAMIC PARTITION 守护进程 (预创建): pYYYYMMDD（如 p20260311）
      // dropPartitionBothFormats 会同时尝试删除两种格式，确保数据完全清除
      partitions.foreach { pv =>
        LOG.info(s"Partition mode: dropping partition for date $pv (both auto & dynamic formats)")
        val conn = getConnection
        try {
          DorisDDLGenerator.dropPartitionBothFormats(conn, dorisDatabase, table, pv)
        } finally {
          conn.close()
        }
      }
      LOG.info(s"Partition mode: using SaveMode.Append (partition-level overwrite)")
      SaveMode.Append
    }

    // 写入 Doris（含 VARCHAR 长度不足自动重建 + retry 逻辑）
    def doWrite(label: String, mode: SaveMode): Unit = {
      df.write
        .format("doris")
        .option("doris.fenodes", DorisConfig.fenodes)
        .option("doris.table.identifier", dorisTarget)
        .option("user", DorisConfig.user)
        .option("password", DorisConfig.password)
        .option("doris.query.port", DorisConfig.feQueryPort)
        .option("doris.fe.auto.fetch", "true")
        .option("doris.sink.auto-redirect", "true")
        .option("doris.sink.batch.size", "1000000")
        .option("doris.sink.batch.interval.ms", "30000")
        .option("doris.sink.max-retries", "3")
        .option("doris.sink.label.prefix", label)
        .option("doris.sink.properties.format", "json")
        .option("doris.sink.properties.read_json_by_line", "true")
        .mode(mode)
        .save()
    }

    val labelPrefix = s"hive2doris_${table}_${System.currentTimeMillis()}"
    LOG.info(s"Writing to Doris: $dorisTarget (stream_load/json, label=$labelPrefix, mode=$saveMode)")

    // Regex to parse Doris "length of input is too long" error from cause chain:
    //   column_name[source_scene], the length of input is too long than schema. ... schema length: 4096; actual length: 4271
    val VarcharTooLongPattern =
      """column_name\[(\w+)\].*?schema length:\s*(\d+);\s*actual length:\s*(\d+)""".r.unanchored

    /** Collect all messages from the exception cause chain. */
    def collectCauseMessages(ex: Throwable): String = {
      val sb = new StringBuilder
      var current: Throwable = ex
      while (current != null) {
        if (current.getMessage != null) sb.append(current.getMessage).append("\n")
        current = current.getCause
      }
      sb.toString()
    }

    try {
      doWrite(labelPrefix, saveMode)
    } catch {
      case e: Exception =>
        val allMessages = collectCauseMessages(e)
        VarcharTooLongPattern.findFirstMatchIn(allMessages) match {
          case Some(m) =>
            val colName = m.group(1)
            val schemaLen = m.group(2).toInt
            val actualLen = m.group(3).toInt
            // 新长度 = 当前 schema 长度 * 2；如果超过 Doris VARCHAR 上限 65533，改用 STRING 类型
            val doubled = schemaLen * 2
            val newType = if (doubled > 65533) "STRING" else s"VARCHAR($doubled)"

            LOG.warn(s"VARCHAR column '$colName' too short (schema=$schemaLen, actual=$actualLen). " +
              s"Rebuilding table $table with '$colName' changed to $newType and excluded from KEY.")

            // 重建表：该列改为新类型 + 排除 KEY
            val fields = fetchHiveFields(table)
            dropDorisTable(table)
            ensureDorisTable(
              table,
              fields,
              excludeKeyColumns = Set(colName),
              columnTypeOverrides = Map(colName -> newType)
            )

            // 重建后需要重新 DROP 分区（分区模式下），然后 retry 写入
            // 同时删除 AUTO PARTITION (pYYYYMMDD000000) 和 DYNAMIC PARTITION (pYYYYMMDD) 两种格式
            if (!isFullMode) {
              partitions.foreach { pv =>
                val conn = getConnection
                try {
                  DorisDDLGenerator.dropPartitionBothFormats(conn, dorisDatabase, table, pv)
                } finally {
                  conn.close()
                }
              }
            }

            val retryLabel = s"hive2doris_${table}_retry_${System.currentTimeMillis()}"
            LOG.info(s"Retrying write to Doris after rebuild: $dorisTarget (label=$retryLabel)")
            doWrite(retryLabel, saveMode)

          case None =>
            // 不是 VARCHAR 长度问题，重新抛出原始异常
            throw e
        }
    }

    LOG.info(s"Successfully wrote $rowCount rows to Doris table $dorisTarget")
  }

  // ─── Row Count Verification ───────────────────────────────────────────────

  /** 同步后验证目标表行数。 */
  private def verifyRowCount(table: String): Unit = {
    try {
      val count = queryLong(s"SELECT COUNT(1) FROM `$dorisDatabase`.`$table`")
      LOG.info(s"Verification: $dorisDatabase.$table has $count rows after sync")
    } catch {
      case NonFatal(e) =>
        LOG.warn(s"Row count verification failed for $dorisDatabase.$table: ${e.getMessage}")
    }
  }

  // ─── Worklog: DDL ─────────────────────────────────────────────────────────

  /**
   * 确保 worklog 表存在（IF NOT EXISTS，幂等）。
   */
  def ensureWorklogTable(): Unit = {
    val ddl =
      s"""CREATE TABLE IF NOT EXISTS `$WORKLOG_DB`.`$WORKLOG_TABLE` (
         |  `dt`             DATE          NOT NULL COMMENT '同步日期',
         |  `table_name`     VARCHAR(256)  NOT NULL COMMENT '完整表名 database.table',
         |  `partition_val`  VARCHAR(256)  NOT NULL COMMENT '分区值, Full模式为__FULL__',
         |  `action`         VARCHAR(32)   COMMENT 'sync_pending/sync_running/sync_done/sync_failed',
         |  `uuid`           VARCHAR(64)   COMMENT '执行者标识',
         |  `message`        STRING        DEFAULT '' COMMENT '附加信息(失败原因等)',
         |  `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '记录更新时间'
         |) UNIQUE KEY (`dt`, `table_name`, `partition_val`)
         |PARTITION BY RANGE (`dt`) ()
         |DISTRIBUTED BY HASH(`table_name`) BUCKETS AUTO
         |PROPERTIES (
         |  "replication_num" = "3",
         |  "enable_unique_key_merge_on_write" = "true",
         |  "dynamic_partition.enable" = "true",
         |  "dynamic_partition.time_unit" = "DAY",
         |  "dynamic_partition.start" = "-7",
         |  "dynamic_partition.end" = "3",
         |  "dynamic_partition.prefix" = "p"
         |)""".stripMargin
    executeSql(ddl)
    LOG.info(s"Ensured worklog table $WORKLOG_DB.$WORKLOG_TABLE exists (DYNAMIC PARTITION + 7d retention)")
  }

  // ─── Worklog: CRUD ────────────────────────────────────────────────────────

  private val worklogUuid: String = java.util.UUID.randomUUID().toString

  /**
   * 注册待同步的分区（状态=sync_pending）。
   * 如果该 (dt, table, partition_val) 上已有 sync_done 记录，则不会覆盖（利用 UNIQUE KEY 特性，
   * 只有在记录不存在时才 INSERT）。
   *
   * 注意：Doris UNIQUE KEY 模型的 INSERT 会直接替换同 key 的旧记录。
   * 因此这里先查询再决定是否 INSERT。
   */
  private def registerPendingPartitions(table: String, partitionVals: List[String], force: Boolean = false): Unit = {
    val fullTableName = s"$hiveDatabase.$table"

    partitionVals.foreach { pv =>
      // 查询当前 worklog 状态（action + uuid）
      val currentStatus = queryWith(
        s"""SELECT `action`, `uuid`
           |FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
           |WHERE `dt` = CURDATE() AND `table_name` = '$fullTableName' AND `partition_val` = '$pv'
           |""".stripMargin
      ) { rs =>
        if (rs.next()) Some((rs.getString("action"), rs.getString("uuid")))
        else None
      }

      currentStatus match {
        // 其他进程正在 running → 始终跳过（不管 force），避免覆盖正在执行的任务
        case Some(("sync_running", existingUuid)) if existingUuid != worklogUuid =>
          LOG.warn(s"Worklog: $fullTableName partition=$pv is currently sync_running by $existingUuid, " +
            s"skipping registration (even with force=$force)")

        // 已完成 + force=false → 跳过
        case Some(("sync_done", _)) if !force =>
          LOG.info(s"Worklog: $fullTableName partition=$pv already sync_done today, skipping registration")

        // 已完成 + force=true → 强制覆写为 pending
        case Some(("sync_done", _)) if force =>
          LOG.info(s"Worklog: $fullTableName partition=$pv was sync_done today, but force=true — re-registering as pending")
          val sql =
            s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
               |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
               |VALUES
               |  (CURDATE(), '$fullTableName', '$pv', 'sync_pending', '$worklogUuid', 'force re-sync', NOW())""".stripMargin
          executeSql(sql)
          LOG.info(s"Worklog: registered pending (force) $fullTableName partition=$pv")

        // 其他状态（无记录 / sync_pending / sync_failed / 自己的 sync_running）→ 注册 pending
        case _ =>
          val sql =
            s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
               |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
               |VALUES
               |  (CURDATE(), '$fullTableName', '$pv', 'sync_pending', '$worklogUuid', '', NOW())""".stripMargin
          executeSql(sql)
          LOG.info(s"Worklog: registered pending $fullTableName partition=$pv")
      }
    }
  }

  /**
   * 标记分区为 sync_running（获取锁）。
   * 返回 true 表示成功获取锁；false 表示被其他进程占用。
   */
  private def markSyncRunning(table: String, partitionVal: String, force: Boolean = false): Boolean = {
    val fullTableName = s"$hiveDatabase.$table"

    // 查询当前状态
    val currentAction = queryWith(
      s"""SELECT `action`, `uuid`
         |FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |WHERE `dt` = CURDATE() AND `table_name` = '$fullTableName' AND `partition_val` = '$partitionVal'
         |""".stripMargin
    ) { rs =>
      if (rs.next()) Some((rs.getString("action"), rs.getString("uuid")))
      else None
    }

    currentAction match {
      case Some(("sync_done", _)) if !force =>
        LOG.info(s"Worklog: $fullTableName partition=$partitionVal already sync_done, skipping")
        false

      case Some(("sync_done", _)) if force =>
        LOG.info(s"Worklog: $fullTableName partition=$partitionVal was sync_done, but force=true — acquiring lock")
        val sql =
          s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
             |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
             |VALUES
             |  (CURDATE(), '$fullTableName', '$partitionVal', 'sync_running', '$worklogUuid', 'force re-sync', NOW())""".stripMargin
        executeSql(sql)
        LOG.info(s"Worklog: acquired lock (force) for $fullTableName partition=$partitionVal (uuid=$worklogUuid)")
        true

      case Some(("sync_running", existingUuid)) if existingUuid != worklogUuid =>
        // 被其他进程锁定 — 在串行调度下理论上不会发生，但做防御
        LOG.warn(s"Worklog: $fullTableName partition=$partitionVal locked by $existingUuid, skipping")
        false

      case _ =>
        // sync_pending / sync_failed / 自己之前的 sync_running → 可以获取锁
        val sql =
          s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
             |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
             |VALUES
             |  (CURDATE(), '$fullTableName', '$partitionVal', 'sync_running', '$worklogUuid', '', NOW())""".stripMargin
        executeSql(sql)
        LOG.info(s"Worklog: acquired lock for $fullTableName partition=$partitionVal (uuid=$worklogUuid)")
        true
    }
  }

  /**
   * 标记分区同步完成。
   */
  private def markSyncDone(table: String, partitionVal: String): Unit = {
    val fullTableName = s"$hiveDatabase.$table"
    val sql =
      s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
         |VALUES
         |  (CURDATE(), '$fullTableName', '$partitionVal', 'sync_done', '$worklogUuid', '', NOW())""".stripMargin
    executeSql(sql)
    LOG.info(s"Worklog: marked sync_done for $fullTableName partition=$partitionVal")
  }

  /**
   * 标记分区同步失败（释放锁）。
   */
  private def markSyncFailed(table: String, partitionVal: String, reason: String): Unit = {
    val fullTableName = s"$hiveDatabase.$table"
    // 截断 reason 防止超出 VARCHAR(1024)
    val safeReason = if (reason.length > 512) reason.substring(0, 512) else reason
    val escapedReason = safeReason.replace("'", "\\'")
    val sql =
      s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
         |VALUES
         |  (CURDATE(), '$fullTableName', '$partitionVal', 'sync_failed', '$worklogUuid', '$escapedReason', NOW())""".stripMargin
    executeSql(sql)
    LOG.info(s"Worklog: marked sync_failed for $fullTableName partition=$partitionVal reason=$safeReason")
  }

  /**
   * 查询某张表今天需要（重新）同步的分区列表。
   * 即 action IN ('sync_pending', 'sync_failed') 的记录。
   */
  private def getPendingPartitions(table: String): List[String] = {
    val fullTableName = s"$hiveDatabase.$table"
    val sql =
      s"""SELECT `partition_val` FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |WHERE `dt` = CURDATE() AND `table_name` = '$fullTableName'
         |  AND `action` IN ('sync_pending', 'sync_failed')""".stripMargin
    queryWith(sql) { rs =>
      val buf = ListBuffer.empty[String]
      while (rs.next()) buf += rs.getString("partition_val")
      buf.toList
    }
  }

  // ─── Worklog: Expired Running Recovery ────────────────────────────────────

  /**
   * 恢复过期的 sync_running 记录为 sync_failed。
   *
   * 过期判断默认基于 [[LEASE_REFRESH_INTERVAL_SEC]] — 如果 update_time 超过该秒数未刷新，
   * 说明续租线程已停止（持有者已死），应恢复为 sync_failed。
   *
   * 策略：
   *   - 当前 uuid 的 sync_running → 直接标记为 sync_failed（自己超时了）
   *   - 其他 uuid 的 sync_running → 仅当 update_time 超过 expireSeconds 秒
   *     未刷新时标记为 sync_failed
   *
   * 典型场景：AutoSync awaitTermination 超时后调用，或调度启动时做一次全局清理。
   *
   * @param expireSeconds 其他 uuid 的 running 任务过期时间（秒），默认 LEASE_REFRESH_INTERVAL_SEC
   * @return 被恢复的记录数
   */
  def recoverExpiredRunningTasks(expireSeconds: Int = LEASE_REFRESH_INTERVAL_SEC): Int = {
    var recovered = 0

    // 1. 当前 uuid 的 sync_running → 直接改为 sync_failed
    val ownSql =
      s"""SELECT `table_name`, `partition_val` FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |WHERE `dt` = CURDATE() AND `action` = 'sync_running' AND `uuid` = '$worklogUuid'""".stripMargin

    val ownRunning = queryWith(ownSql) { rs =>
      val buf = ListBuffer.empty[(String, String)]
      while (rs.next()) buf += ((rs.getString("table_name"), rs.getString("partition_val")))
      buf.toList
    }

    ownRunning.foreach { case (tableName, partVal) =>
      val updateSql =
        s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
           |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
           |VALUES
           |  (CURDATE(), '$tableName', '$partVal', 'sync_failed', '$worklogUuid', 'timeout_recovery', NOW())""".stripMargin
      executeSql(updateSql)
      recovered += 1
      LOG.warn(s"Worklog: recovered own expired running task → sync_failed: $tableName partition=$partVal")
    }

    // 2. 其他 uuid 的 sync_running → 检查 update_time 是否超过 expireSeconds 秒
    val otherSql =
      s"""SELECT `table_name`, `partition_val`, `uuid` FROM `$WORKLOG_DB`.`$WORKLOG_TABLE`
         |WHERE `dt` = CURDATE() AND `action` = 'sync_running' AND `uuid` != '$worklogUuid'
         |  AND `update_time` < DATE_SUB(NOW(), INTERVAL $expireSeconds SECOND)""".stripMargin

    val otherExpired = queryWith(otherSql) { rs =>
      val buf = ListBuffer.empty[(String, String, String)]
      while (rs.next()) buf += ((rs.getString("table_name"), rs.getString("partition_val"), rs.getString("uuid")))
      buf.toList
    }

    otherExpired.foreach { case (tableName, partVal, oldUuid) =>
      val updateSql =
        s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
           |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
           |VALUES
           |  (CURDATE(), '$tableName', '$partVal', 'sync_failed', '$worklogUuid',
           |   'expired_recovery(original_uuid=$oldUuid)', NOW())""".stripMargin
      executeSql(updateSql)
      recovered += 1
      LOG.warn(s"Worklog: recovered other expired running task → sync_failed: $tableName partition=$partVal (was uuid=$oldUuid)")
    }

    if (recovered > 0) {
      LOG.info(s"Worklog: recovered $recovered expired running tasks to sync_failed " +
        s"(own=${ownRunning.size}, other_expired=${otherExpired.size})")
    } else {
      LOG.info(s"Worklog: no expired running tasks found")
    }

    recovered
  }

  // ─── Lease Refresh (Background Thread) ────────────────────────────────────

  /**
   * 启动后台续租线程，每 LEASE_REFRESH_INTERVAL_SEC 刷新一次指定分区的 lease。
   */
  private def startLeaseRefresher(table: String, partitionVal: String): Unit = {
    stopLeaseRefresher() // 确保之前的续租线程已停止
    val executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
    val fullTableName = s"$hiveDatabase.$table"

    val task: Runnable = () => {
      try {
        val sql =
          s"""INSERT INTO `$WORKLOG_DB`.`$WORKLOG_TABLE`
             |  (`dt`, `table_name`, `partition_val`, `action`, `uuid`, `message`, `update_time`)
             |VALUES
             |  (CURDATE(), '$fullTableName', '$partitionVal', 'sync_running', '$worklogUuid', '', NOW())""".stripMargin
        executeSql(sql)
        LOG.info(s"Worklog: refreshed lease for $fullTableName partition=$partitionVal")
      } catch {
        case NonFatal(e) =>
          LOG.warn(s"Worklog: failed to refresh lease for $fullTableName partition=$partitionVal: ${e.getMessage}")
      }
    }

    executor.scheduleAtFixedRate(
      task,
      LEASE_REFRESH_INTERVAL_SEC,
      LEASE_REFRESH_INTERVAL_SEC,
      java.util.concurrent.TimeUnit.SECONDS
    )
    leaseRefresher = Some(executor)
    LOG.info(s"Worklog: started lease refresher (interval=${LEASE_REFRESH_INTERVAL_SEC}s)")
  }

  /**
   * 停止后台续租线程。
   */
  private def stopLeaseRefresher(): Unit = {
    leaseRefresher.foreach { executor =>
      executor.shutdownNow()
      LOG.info("Worklog: stopped lease refresher")
    }
    leaseRefresher = None
  }

  // ─── Orchestrator ─────────────────────────────────────────────────────────

  /**
   * 同步入口。根据 syncMode 执行不同的同步策略，集成 worklog 状态管理。
   *
   * 流程：
   *   1. 确保 worklog 表存在
   *   2. 根据 syncMode 确定待同步的分区列表
   *   3. 注册 pending 分区到 worklog
   *   4. 逐个分区：获取锁 → 同步 → 标记 done/failed → 释放锁
   *
   * @param table      Hive / Doris table name
   * @param partitions 分区值（Partition 模式传单个值，PartitionRange 模式传起止日期 [start, end] 2 个值，
   *                   Full/Auto 模式忽略此参数）
   * @param syncMode   同步模式
   * @param spark      SparkSession
   */
  def syncTable(table: String,
                partitions: List[String],
                syncMode: SyncMode,
                spark: SparkSession,
                force: Boolean = false): Unit = {

    LOG.info("╔══════════════════════════════════════════════════════════════╗")
    LOG.info(s"║  Starting sync: $hiveDatabase.$table → $dorisDatabase.$table [$syncMode]")
    LOG.info("╚══════════════════════════════════════════════════════════════╝")

    // Step 0: 确保 worklog 表存在
    ensureWorklogTable()

    // Step 1: 根据 syncMode 确定分区列表
    val partitionVals: List[String] = syncMode match {
      case SyncMode.Full           => List(FULL_PARTITION_MARKER)
      case SyncMode.Partition      =>
        require(partitions.nonEmpty, s"Partition mode requires at least one partition value, got empty list")
        LOG.info(s"Partition mode: syncing ${partitions.size} partition(s) — ${partitions.mkString(", ")}")
        partitions
      case SyncMode.PartitionRange =>
        // partitions 传入的是 2 个日期值：[startDate, endDate]，需要展开为闭区间内所有 Hive 分区
        require(partitions.size == 2,
          s"PartitionRange mode requires exactly 2 partition values (start, end), got: ${partitions.mkString(",")}")
        val startDate = partitions(0)
        val endDate   = partitions(1)
        val allHivePartitions = getHivePartitionsSorted(table) // 降序排列
        val rangePartitions = allHivePartitions.filter(p => p >= startDate && p <= endDate).sorted
        require(rangePartitions.nonEmpty,
          s"No Hive partitions found in range [$startDate, $endDate] for table $table " +
            s"(Hive has ${allHivePartitions.size} total partitions)")
        LOG.info(s"PartitionRange [$startDate, $endDate]: found ${rangePartitions.size} partitions " +
          s"in Hive — ${rangePartitions.mkString(", ")}")
        rangePartitions
      case SyncMode.Auto           =>
        val changedWithForce = discoverChangedPartitions(table)
        if (changedWithForce.isEmpty) {
          LOG.info(s"[Auto] No changed partitions found for table $table today, skipping sync")
          return
        }
        // 对"已 sync_done 但 Hive 之后又更新"的分区单独 force 注册一次：
        // 将 worklog 中的 sync_done 状态覆写为 sync_pending，
        // 后续 Step 2 的 register(force=false) 对这些分区已是 sync_pending，会正常处理；
        // Step 6 的 markSyncRunning(force=false) 对 sync_pending 也能正常获取锁。
        val forceResyncPvs = changedWithForce.collect { case (pv, true) => pv }
        if (forceResyncPvs.nonEmpty) {
          LOG.info(s"Auto mode: re-registering ${forceResyncPvs.size} force-resync partition(s) " +
            s"to overwrite sync_done in worklog: ${forceResyncPvs.mkString(", ")}")
          registerPendingPartitions(table, forceResyncPvs, force = true)
        }
        changedWithForce.map(_._1)
    }

    // Step 2: 注册 pending 分区
    registerPendingPartitions(table, partitionVals, force)

    // Step 3: 获取待同步分区（pending + failed）
    val pendingParts = getPendingPartitions(table)
    if (pendingParts.isEmpty) {
      LOG.info(s"No pending partitions for $table, all partitions already synced today")
      return
    }
    LOG.info(s"Pending partitions for $table: ${pendingParts.mkString(", ")}")

    // Step 4: 表结构管理 + Schema Evolution
    val fields = fetchHiveFields(table)
    // 用于承载 rebuild 后需要 resync 的分区列表（仅在 rebuild 场景下非空）
    var rebuildResyncPartitions: List[String] = List.empty

    syncMode match {
      case SyncMode.Full =>
        // Full 模式：直接 DROP + CREATE，不需要 schema evolution
        dropDorisTable(table)
        ensureDorisTable(table, fields)

      case _ =>
        if (!dorisTableExists(table)) {
          // 表不存在：直接创建，无需 schema evolution
          ensureDorisTable(table, fields)
        } else {
          // 表已存在：执行 schema evolution 检测
          LOG.info(s"Running schema evolution check for $table")
          val evolutionResult = schemaEvolutionManager.evolve(hiveDatabase, table, partitionCol)

          if (evolutionResult.requiresRebuild) {
            // ── 重建兜底方案 ──────────────────────────────────────────
            // 1. 在 DROP 前收集 Doris 已有分区值 & Hive 全部分区列表
            val dorisPartitionValues = getDorisPartitionValues(table)
            val hivePartitions = getHivePartitionsSorted(table)
            val hivePartitionSet = hivePartitions.toSet

            // 2. 计算 resync 分区 = (Doris 已有分区 ∪ 本次待同步分区) ∩ Hive 全部分区
            //    - 并集：确保 Doris 已有数据 + 本次新增分区都被 resync
            //    - 与 Hive 取交集：避免 Hive 中已删除的分区导致查询报错
            val desiredPartitions = (dorisPartitionValues ++ pendingParts).distinct
            val resyncPartitions = desiredPartitions.filter(hivePartitionSet.contains).sorted

            LOG.info(s"Rebuild strategy: Doris partitions=${dorisPartitionValues.mkString(",")}, " +
              s"pendingParts=${pendingParts.mkString(",")}, " +
              s"union=${desiredPartitions.mkString(",")}, " +
              s"resync (filtered by Hive)=${resyncPartitions.mkString(",")}")

            // 3. 执行 DROP + CREATE（纯 DDL），自动判断是否启用动态清理
            val rebuildEnableDynCleanup = if (partitionCol.isDefined) {
              val partSizeBytes = estimateSinglePartitionSize(table)
              DorisDDLGenerator.shouldEnableDynamicCleanup(partSizeBytes)
            } else false
            schemaEvolutionManager.rebuildTable(hiveDatabase, table, partitionCol, rebuildEnableDynCleanup)

            // 4. 记录需要 resync 的分区列表
            rebuildResyncPartitions = resyncPartitions
            LOG.info(s"Rebuild completed for $table. Partitions to resync: " +
              s"${rebuildResyncPartitions.mkString(", ")}")
          }
          // 轻量变更（ADD/DROP/MODIFY）已在 evolve() 内部通过 ALTER TABLE 完成
        }
    }

    // Step 5: 确定最终待同步分区列表
    // 如果发生了 rebuild，需要用 rebuild 的 resync 分区列表替代原有的 pending 分区
    val finalPendingParts = if (rebuildResyncPartitions.nonEmpty) {
      LOG.info(s"Overriding pending partitions with rebuild resync list (${rebuildResyncPartitions.size} partitions)")
      // 将 resync 分区注册到 worklog
      registerPendingPartitions(table, rebuildResyncPartitions)
      rebuildResyncPartitions
    } else {
      pendingParts
    }

    // Step 6: 逐个分区同步（带 worklog 状态管理）
    finalPendingParts.foreach { pv =>
      val isFullMode = pv == FULL_PARTITION_MARKER
      val displayPv = if (isFullMode) "FULL" else pv

      // 获取锁
      if (!markSyncRunning(table, pv, force)) {
        LOG.info(s"Skipping partition $displayPv (already done or locked)")
        return
      }

      // 启动续租线程
      startLeaseRefresher(table, pv)

      try {
        // 执行同步
        val syncPartitions = if (isFullMode) List.empty[String] else List(pv)
        syncData(table, syncPartitions, spark)

        // 标记完成
        markSyncDone(table, pv)
        LOG.info(s"✓ Partition $displayPv sync completed for table $table")
      } catch {
        case NonFatal(e) =>
          LOG.error(s"✗ Partition $displayPv sync failed for table $table: ${e.getMessage}", e)
          markSyncFailed(table, pv, e.getMessage)
          throw e // 重新抛出异常，让调用方感知同步失败
      } finally {
        stopLeaseRefresher()
      }
    }

    verifyRowCount(table)
    LOG.info(s"✓ Sync completed for table $table [$syncMode]")
  }

}

// ─── Companion Object ─────────────────────────────────────────────────────────

object Hive2DorisSynchronizer {

  private val LOG = LoggerFactory.getLogger(classOf[Hive2DorisSynchronizer])

  /**
   * 通过 `getFieldInfo`（非分区列）与 `getTableDetailV3.columnInfos`（所有列）求差集，
   * 自动识别分区列及其真实类型，合并为完整的字段列表。
   *
   * == 工作原理 ==
   *
   *   - `MammutMetaService.getFieldInfo()` 返回的 fields 列表通常 '''不包含''' 分区字段。
   *   - `MammutMetaService.getTableDetailV3().tableMetaInfo.columnInfos` 包含 '''所有''' 列
   *     （分区列 + 非分区列）及其 `columnName` 和 `columnType`。
   *   - 两者求差集即可得到分区列的名称和类型。
   *
   * == 分区列标记规则 ==
   *
   *   - 与 `partitionCol` 匹配的列（如 dt）标记为 `partitionKey = true`，
   *     参与 Doris AUTO PARTITION 分区键。
   *   - 其余分区列（如 period）标记为 `partitionKey = false`，
   *     作为普通列参与 DUPLICATE KEY 和列定义，不参与 AUTO PARTITION。
   *
   * 该方法被 [[Hive2DorisSynchronizer.fetchHiveFields]] 和
   * [[SchemaEvolutionManager]] 共同使用。
   *
   * @param nonPartFields  `getFieldInfo` 返回的字段列表（通常不含分区列）
   * @param allColumnInfos `getTableDetailV3.tableMetaInfo.columnInfos`（所有列含类型）
   * @param partitionCol   主分区列名（e.g. Some("dt")），None 表示非分区表
   * @return 合并后的完整字段列表（非分区列 + 分区列），分区列按
   *         [主分区列, 额外分区列...] 顺序追加在末尾
   */
  private[doris] def buildFieldsWithPartitionCols(
    nonPartFields: List[FieldInfo],
    allColumnInfos: List[ColumnInfo],
    partitionCol: Option[String]
  ): List[FieldInfo] = {

    if (partitionCol.isEmpty || allColumnInfos.isEmpty) {
      LOG.info("No partition column or no columnInfos — returning nonPartFields as-is")
      return nonPartFields
    }

    // 1. nonPartFields 的列名集合（case-insensitive）
    val nonPartNameSet: Set[String] = nonPartFields
      .flatMap(_.name)
      .map(_.toLowerCase)
      .toSet

    // 2. allColumnInfos 中不在 nonPartFields 中的列 → 分区列
    val partitionCols = allColumnInfos.filter { ci =>
      ci.columnName.exists(name => !nonPartNameSet.contains(name.toLowerCase))
    }

    if (partitionCols.isEmpty) {
      LOG.info("No partition columns detected via diff — all columns already in nonPartFields")
      return nonPartFields
    }

    val partColNames = partitionCols.flatMap(_.columnName)
    LOG.info(s"Detected ${partitionCols.size} partition column(s) via diff: " +
      s"${partColNames.mkString(", ")}")

    // 3. 将分区列转为 FieldInfo
    //    - 主分区列 (dt) → partitionKey=true
    //    - 额外分区列 (period) → partitionKey=false
    val mainPartColLower = partitionCol.map(_.toLowerCase)

    // 分离主分区列和额外分区列，主分区列排在前面
    val (mainPartCIs, extraPartCIs) = partitionCols.partition { ci =>
      ci.columnName.exists(name => mainPartColLower.contains(name.toLowerCase))
    }

    val partFieldInfos = (mainPartCIs ++ extraPartCIs).map { ci =>
      val colName = ci.columnName.getOrElse("unknown")
      val colType = ci.columnType.getOrElse("string")
      val isMainPart = mainPartColLower.contains(colName.toLowerCase)

      LOG.info(s"Partition column '$colName' (type=$colType, isMainPartition=$isMainPart)")
      FieldInfo(
        name = Some(colName),
        fieldType = Some(colType),
        comment = ci.description.orElse(Some("分区字段(自动补充)")),
        partitionKey = Some(isMainPart)
      )
    }

    // 4. 合并：非分区列 + 分区列（主分区列在前）
    val result = nonPartFields ++ partFieldInfos
    LOG.info(s"Built complete field list: ${nonPartFields.size} non-partition + " +
      s"${partFieldInfos.size} partition = ${result.size} total columns")

    result
  }
}
