package com.netease.lofter.etl.common.spark

import com.netease.wm.util.Sql._
import org.apache.spark.sql.Row

object SparkSqlImplicits {

  case class SparkRowSqlParam(row: Row) extends SqlParam {
    override def get(i: Int): Any = {
      require(i < row.length, s"Not enough args for index $i with param size ${row.length}")
      row.get(i)
    }

    override def get(name: String): Any = {
      row.get(row.fieldIndex(name))
    }
  }

  implicit def rowParam(row: Row): SqlParam = SparkRowSqlParam(row)
}
