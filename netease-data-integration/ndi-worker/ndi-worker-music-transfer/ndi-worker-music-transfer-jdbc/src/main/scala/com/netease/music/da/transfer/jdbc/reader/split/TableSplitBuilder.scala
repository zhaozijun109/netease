package com.netease.music.da.transfer.jdbc.reader.split

import java.math.BigDecimal
import java.sql.{ResultSet, Types}

import com.netease.music.da.transfer.common.log.LogTrait
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.util.JDBCUtils
import com.netease.music.da.transfer.jdbc.vo.TableMeta

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


case class Predicate(
                      lowBound: String,
                      upperBound: String,
                      rowLength: Long,
                      dataSize: Long,
                      keyName: String,
                      keyType: Int,
                      splitNum: Option[Int],
                      maxSplitNumber: Option[Int],
                      splitSize: Option[Long]
                    )

object TableSplitBuilder extends LogTrait {
  def buildTableSplits(dbConnection: DBConnection,
                       tableMeta: TableMeta): List[TableSplit] = {
    val properties = tableMeta.properties
    var tableRangeList: List[TableRange] = List[TableRange]()
    val partitionSize = properties.getProperty(PARTITION_SIZE).get
    val condition = properties.getProperty(CONDITION).get
    if (properties.getProperty(MUST_SPLIT).get) {
      tableRangeList = getSplitRangeList(dbConnection, tableMeta, condition)
    } else {
      if (tableMeta.tableDataLength > partitionSize) {
        tableRangeList = getSplitRangeList(dbConnection, tableMeta, condition)
      }
    }

    combineToTableSplitList(tableMeta, tableRangeList)
  }

  def getSplitRangeList(dbConnection: DBConnection,
                        tableMeta: TableMeta,
                        condition: String): List[TableRange] = {
    val predicate: Predicate = getPredicate(dbConnection, tableMeta, condition)
    LOG.info(s"using predicate: $predicate")
    var result = List[TableRange]()
    if (predicate != null) {
      result = getSplitRangeList(predicate)
    }
    result
  }

  def getPredicate(dbConnection: DBConnection, tableMeta: TableMeta, condition: String): Predicate = {
    val sql = dbConnection.getMinMaxSql(
      JDBCUtils.getQueryTableName(tableMeta.tableIdentifier, tableMeta.properties),
      tableMeta.primaryKey.name, condition)
    dbConnection.executeQuery[Predicate](sql, (resultSet: ResultSet) => {
      if (resultSet.next()) {
        val keyType = resultSet.getMetaData.getColumnType(1)
        val minValue = resultSet.getObject("minVal")
        val maxValue = resultSet.getObject("maxVal")
        val cnt = tableMeta.tableRows
        if (minValue != null) {
          Predicate(
            minValue.toString,
            maxValue.toString,
            cnt,
            cnt * tableMeta.tableAvgRowLength,
            tableMeta.primaryKey.name,
            keyType,
            tableMeta.properties.getProperty(SPLIT_NUM),
            tableMeta.properties.getProperty(MAX_SPLIT_NUM),
            tableMeta.properties.getProperty(SPLIT_SIZE)
          )
        } else {
          null
        }
      } else {
        null
      }
    }).orNull
  }

  def combineToTableSplitList(tableMeta: TableMeta,
                              predicateList: Seq[TableRange]): List[TableSplit] = {
    val partitionNum = tableMeta.properties.getProperty(PARTITION_NUM).get
    if (predicateList.size < partitionNum) {
      LOG.warn(s"Reset 'partitionNum' to ${predicateList.size}.")
    }
    var resultList = new Array[ListBuffer[TableRange]](Math.min(partitionNum, predicateList.size))
    if (predicateList.nonEmpty) {
      for (item <- predicateList.indices) {
        val index = item % partitionNum
        if (resultList(index) == null) {
          resultList(index) = new ListBuffer[TableRange]
        }
        resultList(index) += predicateList.get(item)
      }
    } else {
      resultList = List(new ListBuffer[TableRange]).toArray
    }
    resultList.map(entry => {
      TableSplit(tableMeta.tableIdentifier, tableMeta.primaryKey.name, entry.toList, tableMeta.properties)
    }).toList
  }

  def getSplitRangeList(predicate: Predicate): List[TableRange] = {
    var splitPositions: List[String] = null
    var canSplit = false
    predicate.keyType match {
      case Types.BIGINT | Types.INTEGER | Types.NUMERIC |
           Types.DOUBLE | Types.FLOAT | Types.TIMESTAMP =>
        canSplit = true
        splitPositions = SplitUtil.split(
          BigDecimal.valueOf(predicate.lowBound.toString.toDouble),
          BigDecimal.valueOf(predicate.upperBound.toString.toDouble),
          splitSize = predicate.splitSize.get,
          maxSplitNum = predicate.maxSplitNumber.get,
          splitNumber = predicate.splitNum
        ).toList
      case Types.VARCHAR | Types.LONGNVARCHAR | Types.NVARCHAR | Types.NCHAR =>
        if (predicate.splitNum.isEmpty) {
          throw new IllegalArgumentException(
            "Property 'splitNum' must be specified when split key's datatype is string.")
        }
        splitPositions = SplitUtil.splitString(
          predicate.lowBound.toString,
          predicate.upperBound.toString,
          splitNumber = predicate.splitNum,
          maxSplitNum = predicate.maxSplitNumber.get
        ).toList
      case _ =>
        LOG.info("unsupported split type: " + predicate.keyType)
        return List[TableRange]()
    }
    buildRangeList(splitPositions, canSplit)
  }

  def buildRangeList(splitPositions: List[String], canSplit: Boolean): List[TableRange] = {
    val rangeList = new ListBuffer[TableRange]
    if (splitPositions.size >= 2) {
      for (index <- 0 until splitPositions.size() - 1) {
        rangeList += TableRange(splitPositions(index), splitPositions(index + 1), canSplit)
      }
      val index = splitPositions.size - 1
      rangeList += TableRange(splitPositions(index), splitPositions(index), canSplit)
    } else if (splitPositions.size == 1) {
      rangeList += TableRange(splitPositions.head, splitPositions.head, canSplit)
    }
    rangeList.toList
  }

  def buildConditionList(keyName: String, splitPositions: List[String]): List[String] = {
    val conditionList = new ListBuffer[String]
    for (index <- 0 until splitPositions.size() - 1) {
      val partG = s"$keyName >= ${splitPositions(index)}"
      var partL = s"$keyName < ${splitPositions(index + 1).toString}"
      if (index == splitPositions.size() - 2) {
        partL = s"$keyName <= ${splitPositions(index + 1).toString}"
      }
      conditionList += s"($partG and $partL)"
    }
    conditionList += s"$keyName > ${splitPositions.last.toString}"
    conditionList.toList
  }
}
