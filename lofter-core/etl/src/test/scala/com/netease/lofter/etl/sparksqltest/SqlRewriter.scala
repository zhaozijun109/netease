package com.netease.lofter.etl.sparksqltest

import scala.util.matching.Regex

/**
 * SQL重写器，用于处理读写相同表不同分区的情况
 * 
 * 核心功能：
 * 1. 检测SQL中的自引用场景（读写同一表）
 * 2. 将对历史分区的读取重定向到Mock历史表
 * 3. 确保当前分区的写入与历史分区的读取隔离
 */
object SqlRewriter {

  /**
   * 重写SQL，处理自引用场景
   * 
   * @param sql 原始SQL
   * @param selfReferences 自引用信息集合
   * @return 重写后的SQL
   */
  def rewriteForSelfReference(sql: String, selfReferences: Set[SelfReference]): String = {
    if (selfReferences.isEmpty) {
      return sql
    }

    var rewrittenSql = sql

    // 对每个需要处理历史分区的表进行重写
    selfReferences.filter(_.hasHistoricalRead).foreach { selfRef =>
      rewrittenSql = rewriteHistoricalPartitionReads(rewrittenSql, selfRef.table)
    }

    rewrittenSql
  }

  /**
   * 重写历史分区读取，将其重定向到历史表
   * 
   * @param sql 原始SQL
   * @param table 需要重写的表
   * @return 重写后的SQL
   */
  private def rewriteHistoricalPartitionReads(sql: String, table: TableRef): String = {
    val historyTableName = getHistoryTableName(table)
    
    // 构建表引用的模式，支持各种格式：database.table、`database`.`table`等
    val tableReferencePattern = buildTableReferencePattern(table)
    
    // 查找所有该表的引用
    val tablePattern = tableReferencePattern.r
    
    var result = sql
    var offset = 0
    
    tablePattern.findAllMatchIn(sql).foreach { tableMatch =>
      val matchStart = tableMatch.start + offset
      val matchEnd = tableMatch.end + offset
      
      val beforeMatch = result.substring(0, matchStart)
      val afterMatch = result.substring(matchEnd)

      // 排除INSERT OVERWRITE语句中的表引用
      // 更精确的检查：只检查是否直接在 "insert overwrite table" 后面
      val lowerBeforeTable = beforeMatch.toLowerCase.replaceAll("\\s+", " ")
      val isInsertOverwrite = lowerBeforeTable.endsWith("insert overwrite table ")

      if (!isInsertOverwrite && isHistoricalPartitionRead(afterMatch)) {
        // 这是历史分区读取，需要重写为历史表
        val originalReference = result.substring(matchStart, matchEnd)
        val newReference = historyTableName
        
        result = result.substring(0, matchStart) + newReference + result.substring(matchEnd)
        offset += newReference.length - originalReference.length
        
        println(s"  [INFO] 重写历史分区读取: $originalReference -> $newReference")
      }
    }
    
    result
  }

  /**
   * 构建表引用的正则模式
   */
  private def buildTableReferencePattern(table: TableRef): String = {
    // 支持多种格式：database.table、`database`.table、database.`table`、`database`.`table`
    s"""(?i)`?${table.database}`?\\s*\\.\\s*`?${table.table}`?"""
  }

  /**
   * 检查SQL片段是否包含历史分区读取条件
   */
  private def isHistoricalPartitionRead(sqlFragment: String): Boolean = {
    // 检查是否有复合的历史分区条件，比如 dt < X and dt >= Y
    // 忽略换行符和空格
    val cleanedFragment = sqlFragment.replaceAll("\\s+", " ").toLowerCase

    // 方法1：检查是否包含azkaban变量（在变量替换前）
    val hasAzkabanVars = cleanedFragment.contains("azkaban.flow") || cleanedFragment.contains("{azkaban.flow")

    // 方法2：检查是否有历史分区读取模式（在变量替换后）
    // 查找形如 "dt < 'YYYY-MM-DD'" 的模式，表示读取当前日期之前的数据
    val hasHistoricalCondition = cleanedFragment.contains("dt <") || cleanedFragment.contains("dt<=")

    hasAzkabanVars || hasHistoricalCondition
  }

  /**
   * 获取历史表名称（添加_history后缀）
   */
  def getHistoryTableName(table: TableRef): String = {
    s"${table.database}.${table.table}_history"
  }

  /**
   * 从历史表名恢复原表名
   */
  def getOriginalTableName(historyTableName: String): Option[TableRef] = {
    val pattern = """(\w+)\.(\w+)_history""".r
    pattern.findFirstMatchIn(historyTableName).map { m =>
      TableRef(m.group(1), m.group(2))
    }
  }

  /**
   * 检查表名是否为历史表
   */
  def isHistoryTable(tableName: String): Boolean = {
    tableName.endsWith("_history")
  }

  /**
   * 为历史表生成Mock表定义
   * 历史表不应该是分区表，因为它包含多个历史分区的合并数据
   */
  def createHistoryTableDef(originalTableDef: MockTableDef): MockTableDef = {
    originalTableDef.copy(
      table = s"${originalTableDef.table}_history",
      partitionColumns = Seq.empty // 历史表作为普通表，不分区
    )
  }
}