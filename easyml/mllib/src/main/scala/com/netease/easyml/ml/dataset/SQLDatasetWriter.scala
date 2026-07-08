package com.netease.easyml.ml.dataset

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection
import com.netease.easyml.ml.param.{HasColumns, HasSQLs}
import org.apache.spark.internal.Logging
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/7/8.
 */
@Register(name = "sql")
class SQLDatasetWriter(override val uid: String) extends DatasetWriter
  with HasSQLs with HasColumns with Logging with DefaultParamsWritable {

  import SQLDatasetWriter._

  def this() = this(Identifiable.randomUID("sql_writer"))

  def setSqls(value: Array[String]): this.type = set(sqls, value)

  def setColumns(value: Array[String]): this.type = set(columns, value)

  override def write(dataFrame: DataFrame, params: collection.Params): Unit = {
    val sqls = params.get("sqls", getSqls)

    val df = if ($(columns).nonEmpty)
      dataFrame.select($(columns).map(col): _*)
    else
      dataFrame

    //    val tmpTable = Identifiable.randomUID(TMP_EASYML_TABLE)
    val tmpTable = TMP_EASYML_TABLE
    logInfo(s"Create temp view: $tmpTable")
    df.createOrReplaceTempView(tmpTable)

    val map = params.getStringMap.toMap

    for (elem <- sqls) {
      var sql = elem.replaceAll("__THIS__", tmpTable)

      sql = sqlRender(None, sql, map)
      logInfo(s"Execute SQL: $sql")
      df.sparkSession.sql(sql)
    }
  }
}

object SQLDatasetWriter extends DefaultParamsReadable[SQLDatasetWriter] {
  val TMP_EASYML_TABLE: String = "easyml_temp_table"

  override def load(path: String): SQLDatasetWriter = super.load(path)
}
