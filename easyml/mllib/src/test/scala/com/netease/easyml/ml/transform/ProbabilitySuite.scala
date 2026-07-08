package com.netease.easyml.ml.transform

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/4.
 */
class ProbabilitySuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): Array[DataFrame] = {
    import spark.implicits._

    val arrDf = spark.sparkContext.parallelize(
      Seq(
        (0, Array(0.3, 0.7)),
        (1, Array(0.5, 0.5))
      )
    ).toDF("prediction", "probability")
    val vecDf = spark.sparkContext.parallelize(
      Seq(
        (0, Vectors.dense(Array(0.3, 0.7))),
        (1, Vectors.dense(Array(0.5, 0.5)).toSparse)
      )
    ).toDF("prediction", "probability")

    Array(arrDf, vecDf)
  }

  test("transform") {
    val dfs = makeDf()
    for (df <- dfs) {
      new Probability()
        .setOutputCol("output")
        .transform(df)
        .show(false)
    }
  }

}
