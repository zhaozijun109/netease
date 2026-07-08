package com.netease.easyml.ml.feature

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/28.
 */
class SkipGramSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val inputCol = "tokens"

  def makeDf(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      Seq(1, 3, 4, 5, 10, 11).map(_.toDouble),
      Seq(4, 11, 44, 32, 88, 1).map(_.toDouble)
    )).toDF(inputCol)
  }

  test("skipGram") {
    val df = makeDf()
    val newDf = new SkipGram()
      .setInputCol(inputCol)
      .setSample(0)
      .transform(df)
    newDf.show(false)
  }
}
