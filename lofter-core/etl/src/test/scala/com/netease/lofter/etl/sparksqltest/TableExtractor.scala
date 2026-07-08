package com.netease.lofter.etl.sparksqltest

import scala.util.matching.Regex

case class TableRef(database: String, table: String) {
  def fullName: String = s"$database.$table"
}

case class SqlAnalysis(
    inputTables: Set[TableRef],
    outputTable: Option[TableRef],
    outputPartitionColumns: Seq[String],
    selfReferences: Set[SelfReference] = Set.empty
)

case class SelfReference(
    table: TableRef,
    hasHistoricalRead: Boolean,
    hasCurrentWrite: Boolean
)

object TableExtractor {

  private val QualifiedTablePattern: Regex =
    """(?i)`?(lofter|lofter_db_dump|lofter_dm)`?\s*\.\s*`?(\w+)`?""".r

  private val InsertOverwritePattern: Regex =
    """(?i)insert\s+overwrite\s+table\s+`?(\w+)`?\s*\.\s*`?(\w+)`?""".r

  private val PartitionPattern: Regex =
    """(?i)partition\s*\(\s*`?(\w+)`?\s*=""".r

  private val HistoricalPartitionPattern: Regex =
    """(?i)where\s+[^()]*dt\s*[<>!]+\s*['"]?\$\{azkaban\.flow\.\d+\.days\.ago\}['"]?""".r

  def analyze(sql: String): SqlAnalysis = {
    val allTables = QualifiedTablePattern.findAllMatchIn(sql).map { m =>
      TableRef(m.group(1).toLowerCase, m.group(2))
    }.toSet

    val outputTable = InsertOverwritePattern.findFirstMatchIn(sql).map { m =>
      TableRef(m.group(1).toLowerCase, m.group(2))
    }

    val partitionCols = PartitionPattern.findAllMatchIn(sql).map(_.group(1)).toSeq

    // 检测自引用场景
    val selfReferences = detectSelfReferences(sql, allTables, outputTable)

    val inputTables = outputTable match {
      case Some(out) => allTables.filterNot(t => t.database == out.database && t.table == out.table)
      case None => allTables
    }

    SqlAnalysis(inputTables, outputTable, partitionCols, selfReferences)
  }

  // 检测自引用场景
  private def detectSelfReferences(sql: String, inputTables: Set[TableRef], outputTable: Option[TableRef]): Set[SelfReference] = {
    outputTable match {
      case Some(output) =>
        inputTables.filter(input => input.database == output.database && input.table == output.table)
          .map { selfRefTable =>
            // 检查是否有历史分区读取模式
            val hasHistoricalRead = hasHistoricalPartitionRead(sql, selfRefTable)
            val hasCurrentWrite = true // 既然有输出表，就认为有当前写入

            SelfReference(selfRefTable, hasHistoricalRead, hasCurrentWrite)
          }
      case None => Set.empty
    }
  }

  /**
   * 检测是否有历史分区读取
   */
  private def hasHistoricalPartitionRead(sql: String, table: TableRef): Boolean = {
    val tablePattern = s"(?i)${table.database}\\.${table.table}"
    val tableRegex = tablePattern.r

    // 查找表引用的位置
    tableRegex.findAllMatchIn(sql).exists { tableMatch =>
      val beforeTable = sql.substring(0, tableMatch.start)
      val afterTable = sql.substring(tableMatch.end)

      // 排除INSERT OVERWRITE语句中的表引用
      // 更精确的检查：只检查是否直接在 "insert overwrite table" 后面
      val lowerBeforeTable = beforeTable.toLowerCase.replaceAll("\\s+", " ")
      val isInsertOverwrite = lowerBeforeTable.endsWith("insert overwrite table ")

      if (!isInsertOverwrite) {
        // 检查是否有复合的历史分区条件，比如 dt < X and dt >= Y
        // 忽略换行符和空格
        val cleanedAfterTable = afterTable.replaceAll("\\s+", " ").toLowerCase
        // 方法1：检查是否包含azkaban变量（在变量替换前）
        val hasAzkabanVars = cleanedAfterTable.contains("azkaban.flow") || cleanedAfterTable.contains("{azkaban.flow")

        // 方法2：检查是否有历史分区读取模式（在变量替换后）
        // 查找形如 "dt < 'YYYY-MM-DD'" 的模式，表示读取当前日期之前的数据
        val hasHistoricalCondition = cleanedAfterTable.contains("dt <") || cleanedAfterTable.contains("dt<=")

        hasAzkabanVars || hasHistoricalCondition
      } else {
        false
      }
    }
  }

  // 获取需要创建历史表的表列表
  def getTablesNeedingHistory(analysis: SqlAnalysis): Set[TableRef] = {
    analysis.selfReferences.filter(_.hasHistoricalRead).map(_.table)
  }
}