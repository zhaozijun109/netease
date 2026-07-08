package com.netease.easyml.ml.dataset

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.ml.param.{HasColumns, HasSQLs}
import org.apache.spark.internal.Logging
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/7/6.
 */
@Register(name = "sql")
class SQLDatasetReader(override val uid: String) extends DatasetReader
  with HasSQLs with HasColumns with Logging with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("sql_reader"))

  def setSqls(value: Array[String]): this.type = set(sqls, value)

  def setColumns(value: Array[String]): this.type = set(columns, value)

  override def read(spark: SparkSession, params: JParams): DataFrame = {
    val sqls = params.get("sqls", getSqls)

    val map = params.getStringMap.toMap
    for (elem <- sqls.slice(0, sqls.length - 1)) {
      val sql = sqlRender(Some(spark), elem, map)
      logInfo(s"Execute SQL: $sql")
      spark.sql(sql)
    }
    var sql = sqls.last
    sql = sqlRender(Some(spark), sql, map)
    logInfo(s"Execute SQL: $sql")
    val df = spark.sql(sql)

    if ($(columns).nonEmpty) {
      df.select($(columns).map(col): _*)
    } else
      df
  }
}

object SQLDatasetReader extends DefaultParamsReadable[SQLDatasetReader] {

  override def load(path: String): SQLDatasetReader = super.load(path)
}