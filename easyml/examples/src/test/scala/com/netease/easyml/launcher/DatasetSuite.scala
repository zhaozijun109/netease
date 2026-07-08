package com.netease.easyml.launcher

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.ml.dataset._
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/7/14.
 */
class DatasetSuite extends FunSuite with SharedSparkContext {

  test("render") {
    var sql =
      """
        |easyml.5.days.ago = ${easyml.5.days.ago};
        |easyml.5.month.ago = ${easyml.5.month.ago};
        |easyml.current.date = ${easyml.current.date};
        |easyml.current.month = ${easyml.current.month};
        |easyml.start.year = ${easyml.start.year};
        |path = ${path}
        |""".stripMargin
    sql = sqlRender(None, sql, Map("path" -> "test_table"))
    println(sql)
  }

  test("sql"){
    val spark = SparkSession.builder().enableHiveSupport().getOrCreate()

    spark.sql("select 1")
  }
}
