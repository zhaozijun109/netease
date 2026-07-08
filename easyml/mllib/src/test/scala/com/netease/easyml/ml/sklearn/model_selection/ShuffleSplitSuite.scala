package com.netease.easyml.ml.sklearn.model_selection

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/19.
 */
class ShuffleSplitSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): DataFrame = {
    import spark.implicits._
    val X = Seq(
      Seq(1, 2),
      Seq(3, 4),
      Seq(5, 6),
      Seq(7, 8),
      Seq(3, 4),
      Seq(5, 6)
    )
    val y = Seq(1, 2, 1, 2, 1, 2)
    sc.makeRDD(X.zip(y)).toDF("X", "y")
  }

  test("split") {
    val df = makeDf()
    val split = new ShuffleSplit()
      .setNSplits(5)
      .setTestSize(0.25)
      .setRandomState(0)

    split.split(df).foreach {
      case (train, test) =>
        println("TRAIN:")
        train.show(false)
        println("TEST:")
        test.show(false)
    }
  }
}
