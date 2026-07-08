package com.netease.music.da.transfer.jdbc.reader

import java.sql.ResultSet

import com.netease.music.da.transfer.common.log.LogTrait
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.reader.split.TableSplit
import org.apache.spark.sql.Row
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils
import org.apache.spark.sql.types.StructType

import scala.collection.mutable.ArrayBuffer

class DBRowIterator(
                     jdbcReader: JDBCReader,
                     tableSplits: Iterator[TableSplit],
                     schema: StructType) extends Iterator[Row] with LogTrait {

  private var sqlIterator: Iterator[(String, TableSplit)] = _
  private var index = -1
  private var rs: ResultSet = _
  private var rsTableSplit: TableSplit = _
  private var iterator: Iterator[Row] = _

  private def getSqlIterator: Iterator[(String, TableSplit)] = {
    val columns = schema.map(_.name)
    val sqlIterator = ArrayBuffer[(String, TableSplit)]()
    tableSplits.foreach { tableSplit =>
      val connection = jdbcReader.getOrCreateDBConnection(tableSplit.properties)
      val condition = tableSplit.properties.getProperty(CONDITION).get
      val iteratorSize = tableSplit.properties.getProperty(ITERATOR_SIZE).get
      sqlIterator ++= TableSplit.genSQLIterator(connection, tableSplit, columns, condition, iteratorSize)
    }
    println(s"${sqlIterator.length} sql(s) would be executed.")
    sqlIterator.toIterator
  }

  def moveNextSQL(): Boolean = {
    var result = true
    if (sqlIterator == null) {
      sqlIterator = getSqlIterator
    }
    if (sqlIterator.hasNext) {
      index += 1
      val pair = sqlIterator.next()
      val sql = pair._1
      val tableSplit = pair._2
      println(s"Execute sql $index: $sql")
      rs = jdbcReader.getOrCreateDBConnection(tableSplit.properties).getResultSet(sql)
      rsTableSplit = tableSplit
      iterator = JdbcUtils.resultSetToRows(rs, schema)
      result = true
    } else {
      if (rs != null) {
        rs.close()
        jdbcReader.closeDBConnection(rsTableSplit.properties)
        rs = null
      }
      result = false
    }
    result
  }

  override def hasNext: Boolean = {
    while (true) {
      var next = true
      if (iterator == null) {
        next = moveNextSQL()
      }
      if (next) {
        next = iterator.hasNext
        if (!next) {
          if (rs != null) {
            rs.close()
            rs = null
          }
          iterator = null
        } else {
          return next
        }
      } else {
        return next
      }
    }
    false
  }

  override def next(): Row = {
    iterator.next()
  }
}
