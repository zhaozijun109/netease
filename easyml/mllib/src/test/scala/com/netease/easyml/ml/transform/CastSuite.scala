package com.netease.easyml.ml.transform

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/9/16.
 */
class CastSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(-2.0, 1.0, -4.0, -1.0)).toDF("data")
  }

  test("transform") {
    val df = makeDf()

    val cast = new Cast()
      .setTo("int")
      .setInputCol("data")
      .setOutputCol("cast")
    val newDf = cast.transform(df)
    newDf.show(false)
  }
}
