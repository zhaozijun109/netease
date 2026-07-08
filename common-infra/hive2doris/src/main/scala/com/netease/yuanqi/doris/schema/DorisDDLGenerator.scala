package com.netease.yuanqi.doris.schema

import com.netease.yuanqi.metaservice.FieldInfo
import org.slf4j.LoggerFactory

import java.sql.Connection

/**
 * Generates Doris DDL statements and manages table/partition lifecycle.
 *
 * Design decisions:
 *   - DUPLICATE KEY model — best fit for bulk replication of non-unique data.
 *   - DISTRIBUTED BY HASH(...) BUCKETS AUTO — let Doris decide optimal bucket count.
 *   - Replication factor = 3 by default.
 *   - Partitioned tables use '''AUTO PARTITION BY RANGE(date_trunc(`col`, 'day')) ()'''
 *     so Doris automatically creates day-level partitions on INSERT.
   *   - When source table single-partition size >= 10G, additionally enable
   *     '''dynamic_partition''' for automatic historical partition cleanup (start = -30, end = 0).
 *   - Non-partition tables are created without PARTITION BY clause.
 *
 * Key methods:
 *   - `generateCreateTable`        : Generates CREATE TABLE DDL (supports auto-partition)
 *   - `isPartitionedTable`         : Checks if a table should be partitioned
 *   - `shouldEnableDynamicCleanup` : Checks if dynamic cleanup should be enabled (>= 10G)
 *   - `generateAddPartitionDDL`    : Generates ALTER TABLE ... ADD PARTITION DDL
 *   - `dropPartitionBothFormats`   : Drops partition in both AUTO (pYYYYMMDD000000) and DYNAMIC (pYYYYMMDD) formats
 *   - `withDynamicPartitionDisabled` : Temporarily disables dynamic partition for ALTER ops
 *
 * Usage:
 * {{{
 *   // 1. Generate and execute CREATE TABLE (dynamic partition, with cleanup for large tables)
 *   val ddl = DorisDDLGenerator.generateCreateTable(
 *     "ods", "user_event", fields,
 *     partitionCol = Some("dt"),
 *     enableDynamicCleanup = true   // source partition >= 10G → adds start=-30
 *   )
 *   connection.createStatement().execute(ddl)
 * }}}
 *
 * @note enableDynamicCleanup threshold: source single-partition size >= 10G
 */
object DorisDDLGenerator {

  private val LOG = LoggerFactory.getLogger(getClass)

  // ────────────────────────────────────────────
  //  Constants
  // ────────────────────────────────────────────

  /** Default number of leading columns used as DUPLICATE KEY. */
  private[doris] val DEFAULT_DUP_KEY_COUNT: Int = 3

  /** Default replication number for table properties. */
  private[doris] val DEFAULT_REPLICATION_NUM: Int = 3

  /** Default estimated partition size for auto-partition tables. */
  private[doris] val DEFAULT_ESTIMATE_PARTITION_SIZE: String = "10G"

  /** Threshold (bytes) for enabling dynamic partition cleanup: 10 GB. */
  private[doris] val DYNAMIC_CLEANUP_THRESHOLD_BYTES: Long = 10L * 1024 * 1024 * 1024

  /** Default number of days to retain history partitions when dynamic cleanup is enabled. */
  private[doris] val DEFAULT_DYNAMIC_PARTITION_START: Int = -30

  /** Default compression algorithm for table storage. Supported: lz4, zstd, zlib, snappy. */
  private[doris] val DEFAULT_COMPRESSION: String = "lz4"

  // ────────────────────────────────────────────
  //  Internal model
  // ────────────────────────────────────────────

  /**
   * Represents a single Doris column definition.
   */
  private case class DorisColumn(
    name: String,
    dorisType: String,
    comment: Option[String] = None,
    isPartitionKey: Boolean = false
  )

  // ════════════════════════════════════════════
  //  1. CREATE TABLE DDL Generation
  // ════════════════════════════════════════════

  /**
   * Generate a CREATE TABLE IF NOT EXISTS DDL string for Doris.
   *
   * For partitioned tables (when `partitionCol` is specified):
   *   - Uses '''AUTO PARTITION BY RANGE (date_trunc(`col`, 'day')) ()''' so Doris
   *     automatically creates day-level partitions on INSERT.
   *   - Partition column is placed at the '''FIRST''' position of the column list, forced to DATE type
   *     with NOT NULL constraint, and occupies the first slot in DUPLICATE KEY.
   *   - When `enableDynamicCleanup = true`, additionally adds `dynamic_partition` properties
   *     (start=-30, end=0) to auto-clean history partitions older than 30 days.
   *
   * For non-partitioned tables:
   *   - No PARTITION BY clause.
   *
   * @param database              Doris database name
   * @param tableName             Doris table name
   * @param fields                field metadata from MammutMetaService.getFieldInfo
   * @param partitionCol          optional Hive partition column name (e.g. "dt"); if provided,
   *                              generates PARTITION BY RANGE on this column with dynamic partition
   * @param enableDynamicCleanup  whether to add dynamic_partition.start for automatic history cleanup;
   *                              should be true when source table single-partition size >= 10G
   * @param dupKeyCount           number of leading columns for the DUPLICATE KEY clause (default 3)
   * @param replicationNum        table replication factor (default 3)
   * @param estimatePartitionSize estimated partition data size (default "10G")
   * @param dynamicPartitionStart dynamic_partition.start value (default -30); only used when
   *                              `enableDynamicCleanup = true`; added as dynamic_partition.start
   * @param compression           compression algorithm (default "lz4"); supported: lz4, zstd, zlib, snappy
   * @param tableComment          optional table-level comment
   * @param properties            additional Doris table properties (appended to PROPERTIES)
   * @param excludeKeyColumns     column names to exclude from DUPLICATE KEY selection (case-insensitive);
   *                              these columns will be treated as non-key even if they would normally qualify.
   *                              Useful when a STRING column was auto-promoted to KEY with VARCHAR(4096)
   *                              but the actual data exceeds 4096 bytes.
   * @param columnTypeOverrides   override the Doris type for specific columns (case-insensitive column name → Doris type);
   *                              e.g. Map("queryname" -> "VARCHAR(8192)") to widen a column after a length error.
   * @return the complete CREATE TABLE DDL string
   */
  def generateCreateTable(
    database: String,
    tableName: String,
    fields: List[FieldInfo],
    partitionCol: Option[String] = None,
    enableDynamicCleanup: Boolean = false,
    dupKeyCount: Int = DEFAULT_DUP_KEY_COUNT,
    replicationNum: Int = DEFAULT_REPLICATION_NUM,
    estimatePartitionSize: String = DEFAULT_ESTIMATE_PARTITION_SIZE,
    dynamicPartitionStart: Int = DEFAULT_DYNAMIC_PARTITION_START,
    compression: String = DEFAULT_COMPRESSION,
    tableComment: Option[String] = None,
    properties: Map[String, String] = Map.empty,
    excludeKeyColumns: Set[String] = Set.empty,
    columnTypeOverrides: Map[String, String] = Map.empty
  ): String = {

    require(fields.nonEmpty, s"Cannot generate DDL for $database.$tableName: no fields provided")

    // 1. Convert FieldInfo → DorisColumn, classifying partition vs regular columns
    val partColName = partitionCol.map(_.toLowerCase)

    // Normalize columnTypeOverrides keys to lowercase for case-insensitive lookup
    val typeOverridesLower = columnTypeOverrides.map { case (k, v) => k.toLowerCase -> v }

    val allColumns: List[DorisColumn] = fields.flatMap { fi =>
      fi.name match {
        case None =>
          LOG.warn(s"Skipping field with no name in table $database.$tableName")
          None
        case Some(colName) =>
          val hiveType = fi.fieldType.getOrElse("string")
          val isPart = fi.partitionKey.getOrElse(false) || partColName.contains(colName.toLowerCase)
          // Force partition column to DATE type (Hive string yyyy-MM-dd → Doris DATE)
          val baseDorisType = if (isPart) "DATE" else DorisTypeMapper.mapType(hiveType)
          // Apply columnTypeOverrides if specified for this column
          val dorisType = typeOverridesLower.getOrElse(colName.toLowerCase, baseDorisType)
          if (dorisType != baseDorisType) {
            LOG.info(s"Column '$colName' type overridden: $baseDorisType → $dorisType")
          }
          Some(DorisColumn(
            name = colName,
            dorisType = dorisType,
            comment = fi.comment,
            isPartitionKey = isPart
          ))
      }
    }

    val partCols = allColumns.filter(_.isPartitionKey)
    val regularCols = allColumns.filterNot(_.isPartitionKey)

    require(regularCols.nonEmpty,
      s"Cannot generate DDL for $database.$tableName: all fields are partition columns or empty")

    // 2. Build DUPLICATE KEY
    //    - 分区字段（如有）占 KEY 第一位（DATE 类型，兼容 KEY）
    //    - 再从 regular 列中选取 N-1 个 KEY 兼容列（跳过不兼容类型；STRING → VARCHAR(4096））
    //    Doris KEY columns do not support:
    //      - Complex types: ARRAY, MAP, STRUCT, JSON, VARIANT
    //      - Floating-point types: FLOAT, DOUBLE
    //      - Special types: HLL, HLL_UNION, BITMAP, QUANTILE_STATE, AGG_STATE
    val keyIncompatibleTypes = Set(
      "ARRAY", "MAP", "STRUCT", "JSON", "VARIANT",
      "FLOAT", "DOUBLE",
      "HLL", "HLL_UNION", "BITMAP", "QUANTILE_STATE", "AGG_STATE"
    )

    // Normalize excludeKeyColumns to lowercase for case-insensitive comparison
    val excludeKeyColsLower = excludeKeyColumns.map(_.toLowerCase)

    def isKeyCompatible(col: DorisColumn): Boolean = {
      // Explicitly excluded columns are never key-compatible
      if (excludeKeyColsLower.contains(col.name.toLowerCase)) return false
      val upper = col.dorisType.toUpperCase
      !keyIncompatibleTypes.exists(t => upper == t || upper.startsWith(t + "<") || upper.startsWith(t + "("))
    }

    // 分区列占 KEY 第一位，剩余名额从 regular 列中取
    val remainingKeyCount = if (partCols.nonEmpty) dupKeyCount - partCols.size else dupKeyCount
    val regularKeyColumns = regularCols.filter(isKeyCompatible).take(math.max(0, remainingKeyCount))
    val keyColumns = partCols ++ regularKeyColumns
    val keyColumnNames = keyColumns.map(_.name).toSet
    val dupKeyClause = keyColumns.map(c => s"`${c.name}`").mkString(", ")

    // 3. Build column definitions — 分区字段第一位，然后 KEY 兼容列，最后不兼容列
    //    Doris 要求 KEY 列必须是列定义的有序前缀，因此不兼容类型的列必须排在 KEY 兼容列之后。
    //    For KEY columns whose type is STRING, replace with VARCHAR(4096) in the column definition
    val (compatibleRegularCols, incompatibleRegularCols) = regularCols.partition(isKeyCompatible)
    val orderedCols = partCols ++ compatibleRegularCols ++ incompatibleRegularCols
    val columnDefs = orderedCols.map { col =>
      val finalType = if (keyColumnNames.contains(col.name) && col.dorisType.toUpperCase == "STRING") {
        LOG.info(s"Column '${col.name}' is STRING but selected as KEY → replacing with VARCHAR(4096)")
        "VARCHAR(4096)"
      } else {
        col.dorisType
      }
      val notNullStr = if (col.isPartitionKey) " NOT NULL" else ""
      val commentStr = col.comment.filter(_.nonEmpty).map(c => s""" COMMENT '${escapeQuote(c)}'""").getOrElse("")
      s"  `${col.name}` $finalType$notNullStr$commentStr"
    }.mkString(",\n")

    // 4. Build PARTITION clause — AUTO PARTITION BY RANGE(date_trunc(col, 'day'))
    //    Doris will automatically create day-level partitions on INSERT.
    val partitionClause = if (partCols.nonEmpty) {
      val partColExpr = partCols.map(pc => s"date_trunc(`${pc.name}`, 'day')").mkString(", ")
      s"\nAUTO PARTITION BY RANGE ($partColExpr) ()"
    } else {
      ""
    }

    // 5. Build DISTRIBUTED BY — use DUPLICATE KEY columns for hash distribution, BUCKETS AUTO
    val bucketCols = keyColumns.map(c => s"`${c.name}`").mkString(", ")
    val distributedClause = s"DISTRIBUTED BY HASH($bucketCols) BUCKETS AUTO"

    // 6. Build PROPERTIES
    val defaultProps = scala.collection.mutable.LinkedHashMap[String, String](
      "replication_num" -> replicationNum.toString,
      "compression" -> compression
    )

    // For partitioned tables, add estimate_partition_size + dynamic_partition lifecycle management
    if (partCols.nonEmpty) {
      defaultProps += ("estimate_partition_size" -> estimatePartitionSize)

      // All partitioned tables get the base dynamic_partition parameters for lifecycle management
      defaultProps += ("dynamic_partition.enable" -> "true")
      defaultProps += ("dynamic_partition.time_unit" -> "day")
      defaultProps += ("dynamic_partition.end" -> "0")
      defaultProps += ("dynamic_partition.prefix" -> "p")

      // Only add history cleanup (start) when source table single-partition size >= 10G
      if (enableDynamicCleanup) {
        defaultProps += ("dynamic_partition.start" -> dynamicPartitionStart.toString)
      }
    }

    // Merge user-provided properties (user properties override defaults)
    val mergedProps = defaultProps ++ properties
    val propsStr = mergedProps.map { case (k, v) =>
      s"""  "$k" = "$v""""
    }.mkString(",\n")

    // 7. Assemble final DDL
    val commentClause = tableComment.map(c => s"\nCOMMENT '${escapeQuote(c)}'").getOrElse("")

    val ddl =
      s"""CREATE TABLE IF NOT EXISTS `$database`.`$tableName`
         |(
         |$columnDefs
         |)
         |DUPLICATE KEY($dupKeyClause)$commentClause$partitionClause
         |$distributedClause
         |PROPERTIES (
         |$propsStr
         |)""".stripMargin

    LOG.info(s"Generated DDL for $database.$tableName:\n$ddl")
    ddl
  }

  // ════════════════════════════════════════════
  //  2. Partition Table Detection
  // ════════════════════════════════════════════

  /**
   * Determine if a table is partitioned based on field metadata.
   *
   * A table is considered partitioned if:
   *   - Any field has `partitionKey = Some(true)`, OR
   *   - A `partitionCol` name is explicitly provided
   *
   * @param fields       field metadata from MammutMetaService
   * @param partitionCol optional explicit partition column name
   * @return true if the table is partitioned
   */
  def isPartitionedTable(fields: List[FieldInfo], partitionCol: Option[String] = None): Boolean = {
    partitionCol.isDefined || fields.exists(_.partitionKey.getOrElse(false))
  }

  /**
   * Determine whether dynamic partition cleanup should be enabled based on
   * the source table's single-partition data size.
   *
   * Rule: enable cleanup when single partition size >= 10 GB.
   *
   * @param singlePartitionSizeBytes estimated size in bytes of one partition in the source table
   * @return true if dynamic cleanup should be enabled
   */
  def shouldEnableDynamicCleanup(singlePartitionSizeBytes: Long): Boolean = {
    singlePartitionSizeBytes >= DYNAMIC_CLEANUP_THRESHOLD_BYTES
  }

  // ════════════════════════════════════════════
  //  3. Partition DDL Generation
  // ════════════════════════════════════════════

  /**
   * Generate ALTER TABLE ... ADD PARTITION DDL for a specific date value.
   *
   * Creates a partition covering exactly one day:
   *   PARTITION pYYYYMMDD000000 VALUES [("YYYY-MM-DD"), ("YYYY-MM-DD+1"))
   *
   * Note: For AUTO PARTITION tables this is generally not needed (Doris auto-creates
   * partitions on INSERT), but is kept for manual/pre-creation scenarios.
   *
   * @param database     Doris database name
   * @param tableName    Doris table name
   * @param partitionCol partition column name
   * @param partitionVal partition date value in yyyy-MM-dd format
   * @return the ALTER TABLE DDL string
   */
  def generateAddPartitionDDL(
    database: String,
    tableName: String,
    partitionCol: String,
    partitionVal: String
  ): String = {
    val partName = s"p${formatPartitionName(partitionVal)}"
    val nextDay = computeNextDay(partitionVal)

    s"""ALTER TABLE `$database`.`$tableName` ADD PARTITION IF NOT EXISTS `$partName` VALUES [("$partitionVal"), ("$nextDay"))"""
  }

  /**
   * Drop a partition by date value, handling both partition naming formats.
   *
   * '''Background:''' Doris AUTO PARTITION and DYNAMIC PARTITION use different naming conventions:
   *   - AUTO PARTITION (created on INSERT): `pYYYYMMDD000000` (e.g. `p20260417000000`)
   *   - DYNAMIC PARTITION daemon (pre-created): `pYYYYMMDD` (e.g. `p20260417`)
   *
   * When both features are enabled, partitions for the same date may exist under either format.
   * This method drops '''both''' formats to ensure no stale data remains.
   *
   * Executes within [[withDynamicPartitionDisabled]] to bypass the Doris restriction on
   * ALTER PARTITION while `dynamic_partition.enable = true`.
   *
   * @param conn         JDBC connection to Doris
   * @param database     Doris database name
   * @param tableName    Doris table name
   * @param partitionVal partition date value in yyyy-MM-dd format (e.g. "2026-04-17")
   */
  def dropPartitionBothFormats(
    conn: Connection,
    database: String,
    tableName: String,
    partitionVal: String
  ): Unit = {
    val dateStr = partitionVal.replace("-", "")
    // AUTO PARTITION format: pYYYYMMDD000000
    val autoPartName = s"p${dateStr}000000"
    // DYNAMIC PARTITION format: pYYYYMMDD
    val dynPartName = s"p$dateStr"

    withDynamicPartitionDisabled(conn, database, tableName) {
      // Drop AUTO PARTITION format
      val dropAutoSql = s"""ALTER TABLE `$database`.`$tableName` DROP PARTITION IF EXISTS `$autoPartName`"""
      LOG.info(s"Dropping partition (auto format): $dropAutoSql")
      conn.createStatement().execute(dropAutoSql)

      // Drop DYNAMIC PARTITION format
      val dropDynSql = s"""ALTER TABLE `$database`.`$tableName` DROP PARTITION IF EXISTS `$dynPartName`"""
      LOG.info(s"Dropping partition (dynamic format): $dropDynSql")
      conn.createStatement().execute(dropDynSql)
    }
  }


  // ════════════════════════════════════════════
  //  Internal helpers
  // ════════════════════════════════════════════

  /**
   * Format partition name from date value to match Doris AUTO PARTITION naming convention.
   *
   * Doris AUTO PARTITION generates partition names in the format `pYYYYMMDD000000`
   * (date + 6-digit zero time suffix). This method produces the same format
   * (without the 'p' prefix, which is added by callers).
   *
   * "2026-03-11" → "20260311000000"
   */
  private def formatPartitionName(dateVal: String): String = {
    dateVal.replace("-", "") + "000000"
  }

  /**
   * Compute the next day from a date string in yyyy-MM-dd format.
   * "2026-03-11" → "2026-03-12"
   */
  private def computeNextDay(dateStr: String): String = {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = java.time.LocalDate.parse(dateStr, formatter)
    date.plusDays(1).format(formatter)
  }

  /**
   * Execute a partition ALTER operation with dynamic partition temporarily disabled.
   *
   * Required because Doris does not allow ADD/DROP PARTITION on tables with
   * `dynamic_partition.enable = true`.
   *
   * Sequence:
   *   1. ALTER TABLE SET ("dynamic_partition.enable" = "false")
   *   2. Execute the provided operation (ADD / DROP PARTITION)
   *   3. ALTER TABLE SET ("dynamic_partition.enable" = "true")  [in finally block]
   *
   * If disabling fails (e.g. table has no dynamic partition), the operation is still attempted.
   *
   * @param conn      JDBC connection to Doris
   * @param database  Doris database name
   * @param tableName Doris table name
   * @param op        the partition ALTER operation to execute
   */
  private[doris] def withDynamicPartitionDisabled(
    conn: Connection,
    database: String,
    tableName: String
  )(op: => Unit): Unit = {
    // Step 1: Disable dynamic partition
    try {
      conn.createStatement().execute(
        s"""ALTER TABLE `$database`.`$tableName` SET ("dynamic_partition.enable" = "false")"""
      )
      LOG.info(s"Dynamic partition disabled for $database.$tableName")
    } catch {
      case e: Exception =>
        LOG.warn(s"Failed to disable dynamic partition for $database.$tableName: ${e.getMessage}. Proceeding anyway.")
    }

    // Step 2: Execute the partition operation; re-enable in finally to guarantee cleanup
    try {
      op
    } finally {
      // Step 3: Re-enable dynamic partition
      try {
        conn.createStatement().execute(
          s"""ALTER TABLE `$database`.`$tableName` SET ("dynamic_partition.enable" = "true")"""
        )
        LOG.info(s"Dynamic partition re-enabled for $database.$tableName")
      } catch {
        case e: Exception =>
          LOG.error(s"Failed to re-enable dynamic partition for $database.$tableName: ${e.getMessage}")
      }
    }
  }

  /** Escape single quotes in SQL strings. */
  private def escapeQuote(s: String): String = s.replace("'", "\\'")
}
