package com.netease.music.da.transfer.jdbc.reader.split

import java.math.BigDecimal

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.common.log.LogTrait
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.util.JDBCUtils
import com.netease.music.da.transfer.jdbc.vo.{ColumnMeta, TableMeta}
import org.apache.spark.sql.catalyst.TableIdentifier


case class TableRange(
                       var lowBound: String,
                       var upperBound: String,
                       var canSplit: Boolean = false
                     )

case class TableSplit(
                       tableIdentifier: TableIdentifier,
                       partitionKey: String,
                       var partitionList: Seq[TableRange],
                       properties: Properties) extends LogTrait {
  private def optimizeRange(
                             dbConnection: DBConnection,
                             condition: String): Unit = {
    val tableMeta =
      TableMeta(tableIdentifier,
        ColumnMeta(tableIdentifier, partitionKey, "", "", 0, pri = true), null, "", properties)

    def isNotEmpty(tableRange: TableRange): Boolean = {
      var result = true
      if (!tableRange.lowBound.equals(tableRange.upperBound) && tableRange.canSplit) {
        val rangeCondition =
          s"""
             |${TableSplit.buildCondition(tableRange.lowBound, tableRange.upperBound, partitionKey)}
             |AND $condition
           """.stripMargin
        val predicate = TableSplitBuilder.getPredicate(dbConnection, tableMeta, rangeCondition)
        if (predicate == null) {
          LOG.info("filter empty range : " + tableRange.toString)
          result = false
        } else {
          tableRange.lowBound = predicate.lowBound
          if (!tableRange.lowBound.equals(tableRange.upperBound)) {
            if (predicate.upperBound.equals(tableRange.lowBound)) {
              tableRange.upperBound = predicate.upperBound
            } else {
              val upperBound = new BigDecimal(predicate.upperBound).add(new BigDecimal(1))
              if (upperBound.compareTo(new BigDecimal(tableRange.upperBound)) < 0) {
                tableRange.upperBound = upperBound.toString
              }
            }
          }
        }
      }
      result
    }

    if (partitionList != null && partitionList.nonEmpty) {
      this.partitionList = partitionList.filter(entry => isNotEmpty(entry))
    }
  }
}


object TableSplit {

  def buildCondition(
                      lowBound: String,
                      upperBound: String,
                      partitionKey: String): String = {
    if (lowBound.equals(upperBound)) {
      s"$partitionKey = $lowBound"
    } else {
      s"$partitionKey >= $lowBound and $partitionKey < $upperBound"
    }
  }


  def genSQLIterator(
                      dbConnection: DBConnection,
                      tableSplit: TableSplit,
                      columns: Seq[String],
                      condition: String,
                      iteratorSize: Long = 500000): Iterator[(String, TableSplit)] = {
    if (tableSplit.partitionList == null || tableSplit.partitionList.isEmpty) {
      List(
        (
          s"""SELECT
             |  ${columns.mkString(", ")}
             |FROM
             |  ${JDBCUtils.getQueryTableName(tableSplit.tableIdentifier, tableSplit.properties)}
             |WHERE
             |  $condition
         """.stripMargin, tableSplit)).toIterator
    } else {
      tableSplit.optimizeRange(dbConnection, condition)
      tableSplit.partitionList.map { partition =>
        val extraCondition = buildCondition(partition.lowBound, partition.upperBound, tableSplit.partitionKey)
        (
          s"""SELECT
             |  ${columns.mkString(", ")}
             |FROM
             |  ${JDBCUtils.getQueryTableName(tableSplit.tableIdentifier, tableSplit.properties)}
             |WHERE
             |  $extraCondition AND $condition
         """.stripMargin, tableSplit)
      }.iterator
    }
  }
}

