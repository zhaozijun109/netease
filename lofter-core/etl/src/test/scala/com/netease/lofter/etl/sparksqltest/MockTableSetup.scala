package com.netease.lofter.etl.sparksqltest

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

import scala.collection.mutable

case class MockColumn(name: String, dataType: DataType, nullable: Boolean = true)
case class MockTableDef(database: String, table: String, columns: Seq[MockColumn], partitionColumns: Seq[String] = Seq.empty)

object MockTableSetup {

  private val columnTypeCache = mutable.Map[String, Seq[MockColumn]]()

  def inferColumnsFromSql(sql: String, tableRef: TableRef): Seq[MockColumn] = {
    val upperSql = sql.toUpperCase
    val aliasedColumns = extractAliasedColumnsForTable(sql, tableRef)

    if (aliasedColumns.nonEmpty) {
      aliasedColumns.map(col => MockColumn(col, StringType))
    } else {
      Seq(MockColumn("_placeholder", StringType))
    }
  }

  private def extractAliasedColumnsForTable(sql: String, tableRef: TableRef): Seq[String] = {
    val fullName = s"${tableRef.database}\\.${tableRef.table}"
    val cols = mutable.LinkedHashSet[String]()

    val aliasPattern = s"""(?i)(?:from|join)\\s+$fullName\\s+(?:as\\s+)?(\\w+)""".r
    val aliases = aliasPattern.findAllMatchIn(sql).map(_.group(1)).toSeq

    val selectPattern = """(?i)\b(\w+)\.(\w+)\b""".r
    for (m <- selectPattern.findAllMatchIn(sql)) {
      val prefix = m.group(1)
      val column = m.group(2)
      if (aliases.contains(prefix) || prefix.equalsIgnoreCase(tableRef.table)) {
        cols += column.toLowerCase
      }
    }

    val directColumnPattern = s"""(?i)$fullName\\s+(?:where|on|and|or)\\s+(\\w+)""".r
    for (m <- directColumnPattern.findAllMatchIn(sql)) {
      cols += m.group(1).toLowerCase
    }

    cols.toSeq.filterNot(c => isKeyword(c))
  }

  private val keywords = Set(
    "select", "from", "where", "join", "left", "right", "inner", "outer",
    "on", "and", "or", "not", "in", "is", "null", "as", "case", "when",
    "then", "else", "end", "group", "by", "order", "having", "union",
    "all", "insert", "overwrite", "table", "partition", "into", "values",
    "between", "like", "distinct", "asc", "desc", "limit", "with",
    "lateral", "view", "explode", "true", "false", "if", "cast"
  )

  private def isKeyword(word: String): Boolean = keywords.contains(word.toLowerCase)

  def createMockTable(
      spark: SparkSession,
      tableDef: MockTableDef
  ): Unit = {
    spark.sql(s"CREATE DATABASE IF NOT EXISTS ${tableDef.database}")

    val allColumns = tableDef.columns
    val partCols = tableDef.partitionColumns.toSet

    val dataCols = allColumns.filterNot(c => partCols.contains(c.name))
    val partColDefs = allColumns.filter(c => partCols.contains(c.name))

    val dataColSql = dataCols.map(c => s"${c.name} ${sparkTypeToHiveType(c.dataType)}").mkString(", ")

    val partitionSql = if (partColDefs.nonEmpty) {
      val pCols = partColDefs.map(c => s"${c.name} ${sparkTypeToHiveType(c.dataType)}").mkString(", ")
      s" PARTITIONED BY ($pCols)"
    } else ""

    val createSql = s"CREATE TABLE IF NOT EXISTS ${tableDef.database}.${tableDef.table} ($dataColSql)$partitionSql STORED AS PARQUET"

    spark.sql(createSql)
  }

  def createMockTableFromSchema(
      spark: SparkSession,
      database: String,
      table: String,
      schema: StructType,
      partitionColumns: Seq[String] = Seq.empty
  ): Unit = {
    val columns = schema.fields.map(f => MockColumn(f.name, f.dataType, f.nullable))
    createMockTable(spark, MockTableDef(database, table, columns, partitionColumns))
  }

  def autoSetupTablesForJob(
      spark: SparkSession,
      job: SparkSqlJob,
      schemaOverrides: Map[String, MockTableDef] = Map.empty
  ): SqlAnalysis = {
    val analysis = job.queries.map(TableExtractor.analyze).reduce { (a, b) =>
      SqlAnalysis(
        a.inputTables ++ b.inputTables,
        b.outputTable.orElse(a.outputTable),
        (a.outputPartitionColumns ++ b.outputPartitionColumns).distinct,
        a.selfReferences ++ b.selfReferences
      )
    }

    // 首先创建输出表
    analysis.outputTable.foreach { out =>
      val tableDef = schemaOverrides.getOrElse(out.fullName,
        inferTableDefFromSql(job.queries.mkString(" "), out, analysis.outputPartitionColumns)
      )
      createMockTable(spark, tableDef)
    }

    // 创建输入表
    analysis.inputTables.foreach { ref =>
      val tableDef = schemaOverrides.getOrElse(ref.fullName,
        inferTableDefFromSql(job.queries.mkString(" "), ref, Seq.empty)
      )
      createMockTable(spark, tableDef)
    }

    // 为自引用场景创建历史表
    createHistoryTablesForSelfReferences(spark, analysis, schemaOverrides)

    analysis
  }

  /**
   * 为自引用场景创建历史表
   */
  private def createHistoryTablesForSelfReferences(
      spark: SparkSession,
      analysis: SqlAnalysis,
      schemaOverrides: Map[String, MockTableDef]
  ): Unit = {
    val tablesNeedingHistory = TableExtractor.getTablesNeedingHistory(analysis)

    if (tablesNeedingHistory.nonEmpty) {
      println(s"  [INFO] 检测到自引用场景，将为以下表创建历史表: ${tablesNeedingHistory.map(_.fullName).mkString(", ")}")
    }

    tablesNeedingHistory.foreach { table =>
      // 尝试从schemaOverrides获取原表定义
      val originalTableDefOpt = schemaOverrides.get(table.fullName)

      originalTableDefOpt match {
        case Some(originalTableDef) =>
          // 基于原表定义创建历史表
          val historyTableDef = SqlRewriter.createHistoryTableDef(originalTableDef)
          createMockTable(spark, historyTableDef)
          println(s"  [INFO] 创建历史表: ${historyTableDef.database}.${historyTableDef.table}")

        case None =>
          // 如果没有明确的schema定义，尝试推断
          val inferredTableDef = inferTableDefFromSql("", table, Seq("dt"))
          val historyTableDef = SqlRewriter.createHistoryTableDef(inferredTableDef)
          createMockTable(spark, historyTableDef)
          println(s"  [INFO] 创建推断的历史表: ${historyTableDef.database}.${historyTableDef.table}")
      }
    }
  }

  private def inferTableDefFromSql(sql: String, ref: TableRef, partitionCols: Seq[String]): MockTableDef = {
    val inferred = inferColumnsFromSql(sql, ref)
    val partSet = partitionCols.toSet

    val needsDt = sql.toLowerCase.contains(s"${ref.database}.${ref.table}") &&
      sql.toLowerCase.contains("dt") &&
      !inferred.exists(_.name == "dt")

    val columns = if (needsDt) inferred :+ MockColumn("dt", StringType) else inferred

    MockTableDef(ref.database, ref.table, columns, partitionCols)
  }

  private def sparkTypeToHiveType(dt: DataType): String = dt match {
    case StringType => "STRING"
    case IntegerType => "INT"
    case LongType => "BIGINT"
    case DoubleType => "DOUBLE"
    case FloatType => "FLOAT"
    case BooleanType => "BOOLEAN"
    case TimestampType => "TIMESTAMP"
    case DateType => "DATE"
    case _: DecimalType => "DECIMAL(38,18)"
    case _: ArrayType => "ARRAY<STRING>"
    case _: MapType => "MAP<STRING,STRING>"
    case BinaryType => "BINARY"
    case _ => "STRING"
  }
}
