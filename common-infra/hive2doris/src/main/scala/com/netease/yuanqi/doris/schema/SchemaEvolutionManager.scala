package com.netease.yuanqi.doris.schema

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.synchronizer.Hive2DorisSynchronizer
import com.netease.yuanqi.doris.util.SchemaException
import com.netease.yuanqi.metaservice.MammutMetaService
import org.slf4j.{Logger, LoggerFactory}

import java.sql.{Connection, DriverManager}
import scala.collection.mutable

// ─── Schema Change Types ──────────────────────────────────────────────────────

/**
 * Describes a detected schema difference between Hive (source of truth) and Doris.
 */
sealed trait SchemaChange {
  def columnName: String
}

object SchemaChange {
  /** A column exists in Hive but not in Doris — needs `ALTER TABLE ADD COLUMN`. */
  case class AddColumn(columnName: String, dorisType: String, comment: Option[String]) extends SchemaChange

  /** A column exists in Doris but not in Hive — needs `ALTER TABLE DROP COLUMN`. */
  case class DropColumn(columnName: String) extends SchemaChange

  /** A column's type changed between Hive and Doris — may need type modification or rebuild. */
  case class ModifyColumn(columnName: String, oldDorisType: String, newDorisType: String, comment: Option[String]) extends SchemaChange

  /** A key column (DUPLICATE KEY) changed — requires full table rebuild. */
  case class KeyColumnChanged(columnName: String, detail: String) extends SchemaChange
}

// ─── Schema Evolution Result ──────────────────────────────────────────────────

/**
 * Result of a schema comparison.
 *
 * @param changes         list of detected schema changes
 * @param requiresRebuild true if any change requires dropping and recreating the table
 *                        (e.g. key column addition/removal/reorder, incompatible type change).
 *                        When true, the caller is responsible for orchestrating the rebuild
 *                        (DDL via [[SchemaEvolutionManager.rebuildTable]] + data re-import).
 */
case class SchemaEvolutionResult(
  changes: List[SchemaChange],
  requiresRebuild: Boolean
) {
  def hasChanges: Boolean = changes.nonEmpty
}

// ─── Doris Column Info (from INFORMATION_SCHEMA) ──────────────────────────────

/**
 * Represents a column as seen in Doris INFORMATION_SCHEMA.COLUMNS.
 */
private[doris] case class DorisColumnInfo(
  columnName: String,
  dataType: String,        // e.g. "VARCHAR", "INT", "DECIMAL"
  fullType: String,        // e.g. "VARCHAR(65533)", "DECIMAL(18, 2)"
  ordinalPosition: Int,
  columnKey: String,       // "DUP" for duplicate key columns, "" otherwise
  columnComment: String
)

// ─── Schema Evolution Manager ─────────────────────────────────────────────────

/**
 * Manages automatic schema evolution between Hive (source) and Doris (target).
 *
 * == Workflow ==
 *
 * 1. Fetch the latest Hive schema via [[MammutMetaService.getFieldInfo]].
 * 2. Query Doris `INFORMATION_SCHEMA.COLUMNS` for the current target table schema.
 * 3. Compare the two schemas to detect:
 *    - Added columns → generate `ALTER TABLE ADD COLUMN`
 *    - Dropped columns → generate `ALTER TABLE DROP COLUMN`
 *    - Type changes → generate `ALTER TABLE MODIFY COLUMN` (if compatible) or flag for rebuild
 *    - Key column changes → flag for full rebuild (drop + recreate)
 * 4. Execute the generated DDL statements.
 *
 * @param dorisDatabase target Doris database
 * @param metaService   MammutMetaService for Hive schema introspection
 */
class SchemaEvolutionManager(
  dorisDatabase: String,
  metaService: MammutMetaService
) {

  private val LOG: Logger = LoggerFactory.getLogger(classOf[SchemaEvolutionManager])

  // ─── JDBC Helpers ────────────────────────────────────────────────────────────

  private def getConnection: Connection = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    DriverManager.getConnection(DorisConfig.jdbcUrl, DorisConfig.user, DorisConfig.password)
  }

  private def executeSql(sql: String): Unit = {
    val conn = getConnection
    try {
      LOG.info(s"Executing DDL:\n$sql")
      conn.createStatement().execute(sql)
    } finally {
      conn.close()
    }
  }

  // ─── Doris Schema Introspection ──────────────────────────────────────────────

  /**
   * Query Doris INFORMATION_SCHEMA.COLUMNS for the current schema of a table.
   *
   * @param table the Doris table name
   * @return list of column info, empty if table does not exist
   */
  private def getDorisSchema(table: String): List[DorisColumnInfo] = {
    val conn = getConnection
    try {
      val sql =
        s"""SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, ORDINAL_POSITION, COLUMN_KEY, COLUMN_COMMENT
           |FROM INFORMATION_SCHEMA.COLUMNS
           |WHERE TABLE_SCHEMA = '$dorisDatabase'
           |  AND TABLE_NAME = '$table'
           |ORDER BY ORDINAL_POSITION""".stripMargin

      val rs = conn.createStatement().executeQuery(sql)
      val columns = mutable.ListBuffer.empty[DorisColumnInfo]

      while (rs.next()) {
        columns += DorisColumnInfo(
          columnName = rs.getString("COLUMN_NAME"),
          dataType = rs.getString("DATA_TYPE").toUpperCase,
          fullType = rs.getString("COLUMN_TYPE").toUpperCase,
          ordinalPosition = rs.getInt("ORDINAL_POSITION"),
          columnKey = Option(rs.getString("COLUMN_KEY")).getOrElse(""),
          columnComment = Option(rs.getString("COLUMN_COMMENT")).getOrElse("")
        )
      }

      columns.toList
    } finally {
      conn.close()
    }
  }

  // ─── Schema Comparison ───────────────────────────────────────────────────────

  /**
   * Compare Hive schema (from MammutMetaService) with Doris schema (from INFORMATION_SCHEMA)
   * and produce a list of [[SchemaChange]] items.
   *
   * @param hiveDb    Hive database name
   * @param table     table name (same in Hive and Doris)
   * @param partitionCol partition column name to exclude from regular schema comparison
   * @return [[SchemaEvolutionResult]] describing changes and whether a rebuild is needed
   */
  private def detectChanges(
    hiveDb: String,
    table: String,
    partitionCol: Option[String] = Some("dt")
  ): SchemaEvolutionResult = {

    LOG.info(s"Detecting schema changes for $hiveDb.$table → $dorisDatabase.$table")

    // 1. Get Hive schema (enriched with partition column if missing)
    val rawHiveFields = metaService.getFieldInfo(hiveDb, table)
    if (rawHiveFields.isEmpty) {
      LOG.warn(s"No fields found for $hiveDb.$table via MammutMetaService, skipping schema evolution")
      return SchemaEvolutionResult(List.empty, requiresRebuild = false)
    }
    // 获取所有列（含分区列及类型）并通过差集方式合并
    val allColumnInfos = try {
      val tableInfo = metaService.getTableDetailV3(hiveDb, table, "hive")
      tableInfo.tableMetaInfo.map(_.columnInfos).getOrElse(Nil)
    } catch {
      case e: Exception =>
        LOG.warn(s"Failed to get columnInfos from getTableDetailV3 for $hiveDb.$table: ${e.getMessage}")
        Nil
    }
    val hiveFields = Hive2DorisSynchronizer.buildFieldsWithPartitionCols(rawHiveFields, allColumnInfos, partitionCol)

    // 2. Get Doris schema
    val dorisColumns = getDorisSchema(table)
    if (dorisColumns.isEmpty) {
      LOG.info(s"Table $dorisDatabase.$table does not exist in Doris yet, no schema evolution needed (will be created)")
      return SchemaEvolutionResult(List.empty, requiresRebuild = false)
    }

    // 3. Build lookup maps (case-insensitive)
    val dorisColMap: Map[String, DorisColumnInfo] = dorisColumns.map(c => c.columnName.toLowerCase -> c).toMap
    val keyColumnNames: Set[String] = dorisColumns
      .filter(c => c.columnKey.toUpperCase.contains("DUP") || c.columnKey.toUpperCase.contains("TRUE"))
      .map(_.columnName.toLowerCase)
      .toSet

    val partColLower = partitionCol.map(_.toLowerCase).getOrElse("")

    // Build Hive column map (excluding partition columns)
    val hiveColMap: Map[String, (String, Option[String])] = hiveFields.flatMap { fi =>
      fi.name.flatMap { name =>
        val colNameLower = name.toLowerCase
        val isPart = fi.partitionKey.getOrElse(false) || colNameLower == partColLower
        if (isPart) None
        else {
          val hiveType = fi.fieldType.getOrElse("string")
          val dorisType = DorisTypeMapper.mapType(hiveType)
          Some(colNameLower -> (dorisType, fi.comment))
        }
      }
    }.toMap

    val changes = mutable.ListBuffer.empty[SchemaChange]
    var needsRebuild = false

    // 4. Detect added columns (in Hive but not in Doris)
    for ((colName, (dorisType, comment)) <- hiveColMap) {
      if (!dorisColMap.contains(colName)) {
        LOG.info(s"Column '$colName' found in Hive but not in Doris → ADD COLUMN")
        changes += SchemaChange.AddColumn(colName, dorisType, comment)
      }
    }

    // 5. Detect dropped columns (in Doris but not in Hive, excluding partition columns)
    for ((colName, _) <- dorisColMap) {
      if (colName != partColLower && !hiveColMap.contains(colName)) {
        if (keyColumnNames.contains(colName)) {
          LOG.warn(s"Key column '$colName' exists in Doris but not in Hive → requires REBUILD")
          changes += SchemaChange.KeyColumnChanged(colName, s"Key column '$colName' removed from Hive schema")
          needsRebuild = true
        } else {
          LOG.info(s"Column '$colName' exists in Doris but not in Hive → DROP COLUMN")
          changes += SchemaChange.DropColumn(colName)
        }
      }
    }

    // 6. Detect type changes (column exists in both but type differs)
    for ((colName, (expectedDorisType, comment)) <- hiveColMap) {
      dorisColMap.get(colName).foreach { existingCol =>
        if (!isTypeCompatible(existingCol.fullType, expectedDorisType)) {
          if (keyColumnNames.contains(colName)) {
            LOG.warn(s"Key column '$colName' type changed: ${existingCol.fullType} → $expectedDorisType → requires REBUILD")
            changes += SchemaChange.KeyColumnChanged(colName,
              s"Key column type changed from ${existingCol.fullType} to $expectedDorisType")
            needsRebuild = true
          } else {
            LOG.info(s"Column '$colName' type changed: ${existingCol.fullType} → $expectedDorisType, will attempt MODIFY")
            changes += SchemaChange.ModifyColumn(colName, existingCol.fullType, expectedDorisType, comment)
          }
        }
      }
    }

    val result = SchemaEvolutionResult(changes.toList, needsRebuild)
    LOG.info(s"Schema evolution result for $table: ${result.changes.size} changes, requiresRebuild=${result.requiresRebuild}")
    result
  }

  // ─── DDL Execution ───────────────────────────────────────────────────────────

  /**
   * Apply detected schema changes to Doris.
   *
   * For light schema changes (ADD/DROP/MODIFY non-key columns), executes ALTER TABLE DDLs.
   * If `requiresRebuild` is true, throws [[SchemaException]] so the caller can handle
   * the full table rebuild flow.
   *
   * @param table  the Doris table name
   * @param result the schema evolution result from [[detectChanges]]
   * @throws SchemaException if a rebuild is required (caller must drop + recreate)
   */
  private def applyChanges(table: String, result: SchemaEvolutionResult): Unit = {
    if (!result.hasChanges) {
      LOG.info(s"No schema changes to apply for table $table")
      return
    }

    if (result.requiresRebuild) {
      LOG.warn(s"Table $table requires a full rebuild due to key column changes")
      val keyChanges = result.changes.collect { case kc: SchemaChange.KeyColumnChanged => kc }
      throw SchemaException(
        table = table,
        detail = s"Key column changes detected: ${keyChanges.map(_.detail).mkString("; ")}",
        requiresRebuild = true
      )
    }

    // Apply light schema changes
    result.changes.foreach {
      case SchemaChange.AddColumn(colName, dorisType, comment) =>
        val commentClause = comment.filter(_.nonEmpty).map(c => s" COMMENT '${escapeQuote(c)}'").getOrElse("")
        val ddl = s"ALTER TABLE `$dorisDatabase`.`$table` ADD COLUMN `$colName` $dorisType$commentClause"
        executeSql(ddl)
        LOG.info(s"Added column '$colName' ($dorisType) to $dorisDatabase.$table")

      case SchemaChange.DropColumn(colName) =>
        val ddl = s"ALTER TABLE `$dorisDatabase`.`$table` DROP COLUMN `$colName`"
        executeSql(ddl)
        LOG.info(s"Dropped column '$colName' from $dorisDatabase.$table")

      case SchemaChange.ModifyColumn(colName, oldType, newType, comment) =>
        val commentClause = comment.filter(_.nonEmpty).map(c => s" COMMENT '${escapeQuote(c)}'").getOrElse("")
        val ddl = s"ALTER TABLE `$dorisDatabase`.`$table` MODIFY COLUMN `$colName` $newType$commentClause"
        try {
          executeSql(ddl)
          LOG.info(s"Modified column '$colName' to type $newType in $dorisDatabase.$table")
        } catch {
          case e: java.sql.SQLException =>
            LOG.warn(s"ALTER TABLE MODIFY COLUMN '$colName' failed ($oldType → $newType): ${e.getMessage}. " +
              s"This is likely an incompatible type change — falling back to full table rebuild.")
            throw SchemaException(
              table = table,
              detail = s"Incompatible MODIFY COLUMN '$colName' ($oldType → $newType): ${e.getMessage}",
              requiresRebuild = true
            )
        }

      case SchemaChange.KeyColumnChanged(_, _) =>
        // Should not reach here (handled above), but just in case
        LOG.warn("Unexpected KeyColumnChanged in light schema change path")
    }

    LOG.info(s"Schema evolution completed for table $table: ${result.changes.size} changes applied")
  }

  // ─── Full Rebuild Support ───────────────────────────────────────────────────

  /**
   * Drop and recreate a Doris table using the latest Hive schema.
   *
   * This is the nuclear option for when schema changes affect key columns
   * and cannot be handled by ALTER TABLE.
   *
   * @param hiveDb               Hive database name
   * @param table                table name
   * @param partitionCol         partition column name
   * @param enableDynamicCleanup whether to enable dynamic_partition for automatic history cleanup;
   *                             should be true when source table single-partition avg size >= 10G
   * @param excludeKeyColumns    columns to exclude from DUPLICATE KEY (case-insensitive)
   * @param columnTypeOverrides  override Doris type for specific columns (case-insensitive name → type)
   */
  def rebuildTable(
    hiveDb: String,
    table: String,
    partitionCol: Option[String] = Some("dt"),
    enableDynamicCleanup: Boolean = false,
    excludeKeyColumns: Set[String] = Set.empty,
    columnTypeOverrides: Map[String, String] = Map.empty
  ): Unit = {
    LOG.warn(s"REBUILDING table $dorisDatabase.$table — dropping and recreating with latest schema")

    // 1. Drop existing table
    executeSql(s"DROP TABLE IF EXISTS `$dorisDatabase`.`$table`")

    // 2. Get fresh schema from Hive and enrich with all partition columns (via diff)
    val rawFields = metaService.getFieldInfo(hiveDb, table)
    require(rawFields.nonEmpty, s"Cannot rebuild $dorisDatabase.$table: no fields from MammutMetaService")
    val allColumnInfos = try {
      val tableInfo = metaService.getTableDetailV3(hiveDb, table, "hive")
      tableInfo.tableMetaInfo.map(_.columnInfos).getOrElse(Nil)
    } catch {
      case e: Exception =>
        LOG.warn(s"Failed to get columnInfos from getTableDetailV3 for $hiveDb.$table: ${e.getMessage}")
        Nil
    }
    val fields = Hive2DorisSynchronizer.buildFieldsWithPartitionCols(rawFields, allColumnInfos, partitionCol)

    // 3. Generate and execute CREATE TABLE DDL
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

    LOG.info(s"Table $dorisDatabase.$table rebuilt successfully with ${fields.size} columns")
  }

  // ─── Convenience: Detect + Apply ────────────────────────────────────────────

  /**
   * Detect and apply schema changes.
   *
   * - '''Light changes''' (ADD / DROP / MODIFY non-key columns): executed immediately via ALTER TABLE.
   * - '''Heavy changes''' (key column changes): returns `requiresRebuild = true` without executing
   *   any DDL. The '''caller''' is responsible for:
   *   1. Collecting Doris partition count & Hive partition list.
   *   2. Calling [[rebuildTable]] to execute DROP + CREATE.
   *   3. Re-importing the latest N partitions.
   *
   * @param hiveDb       Hive database name
   * @param table        table name
   * @param partitionCol partition column name
   * @return the schema evolution result; check `requiresRebuild` to decide next steps
   */
  def evolve(
    hiveDb: String,
    table: String,
    partitionCol: Option[String] = Some("dt")
  ): SchemaEvolutionResult = {

    val result = detectChanges(hiveDb, table, partitionCol)

    if (!result.hasChanges) {
      return result
    }

    if (result.requiresRebuild) {
      // 不在这里执行 rebuild DDL — 交给调用方编排 rebuild + resync 流程
      val keyChanges = result.changes.collect { case kc: SchemaChange.KeyColumnChanged => kc }
      LOG.warn(s"Table $table requires REBUILD due to key column changes: " +
        s"${keyChanges.map(_.detail).mkString("; ")}. Returning to caller for orchestration.")
      result
    } else {
      try {
        applyChanges(table, result)
        result
      } catch {
        case se: SchemaException if se.requiresRebuild =>
          // MODIFY COLUMN 在执行时失败（如 Can not change STRING to INT），降级为 rebuild
          LOG.warn(s"MODIFY COLUMN failed at runtime for table $table: ${se.detail}. " +
            s"Upgrading to requiresRebuild=true so caller can orchestrate a full rebuild.")
          SchemaEvolutionResult(result.changes, requiresRebuild = true)
      }
    }
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  /**
   * Check if two Doris type strings are compatible (case-insensitive).
   *
   * Compatibility rules:
   *   1. After normalization, identical strings are compatible.
   *   2. Doris integer types with display width are compatible with bare types:
   *      BIGINT(20) ≡ BIGINT, INT(11) ≡ INT, TINYINT(4) ≡ TINYINT, etc.
   *   3. STRING ≡ VARCHAR(65533) (Doris alias).
   *   4. VARCHAR(4096) is compatible with STRING (KEY columns are auto-replaced
   *      from STRING to VARCHAR(4096) by [[DorisDDLGenerator]]).
   */
  private def isTypeCompatible(existingType: String, expectedType: String): Boolean = {
    val normExisting = normalizeType(existingType)
    val normExpected = normalizeType(expectedType)

    if (normExisting == normExpected) return true

    // STRING ≡ VARCHAR(65533) — Doris treats STRING as an alias for VARCHAR(65533)
    val stringAliases = Set("STRING", "VARCHAR(65533)")
    if (stringAliases.contains(normExisting) && stringAliases.contains(normExpected)) return true

    // VARCHAR(4096) ↔ STRING — KEY columns are auto-replaced STRING → VARCHAR(4096)
    // so existing VARCHAR(4096) is compatible with expected STRING, and vice versa
    val keyStringAliases = Set("STRING", "VARCHAR(65533)", "VARCHAR(4096)")
    if (keyStringAliases.contains(normExisting) && keyStringAliases.contains(normExpected)) return true

    false
  }

  /** Regex to strip integer display width: BIGINT(20) → BIGINT, INT(11) → INT, etc. */
  private val IntDisplayWidthPattern = """^(TINYINT|SMALLINT|INT|INTEGER|BIGINT|MEDIUMINT|BOOLEAN)\(\d+\)$""".r

  /**
   * Normalize a Doris type string for comparison.
   *
   *   - Converts to uppercase, removes whitespace.
   *   - Strips integer display width: BIGINT(20) → BIGINT, INT(11) → INT, etc.
   *     (Doris INFORMATION_SCHEMA.COLUMN_TYPE returns display widths for integer types,
   *      but the actual storage and semantics are identical.)
   */
  private def normalizeType(typeStr: String): String = {
    val upper = typeStr.toUpperCase.replaceAll("\\s+", "")
    upper match {
      case IntDisplayWidthPattern(baseType) => baseType
      case _ => upper
    }
  }

  /** Escape single quotes in SQL strings. */
  private def escapeQuote(s: String): String = s.replace("'", "\\'")
}
