package com.netease.easyml.ml.metric.ranking

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class GroupAUCSuite extends FunSuite with Matchers with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new GroupAUC()

  test("group auc") {
    import spark.sqlContext.implicits._
    val label0 = Array(1, 0, 0, 0, 1, 0, 1, 0)
    val label1 = Array(1, 0, 1, 0, 1, 0, 0, 0)
    val label2 = Array(0, 0, 0, 0, 0, 0, 0, 1)
    val label3 = Array(0, 0, 0, 0, 0, 0, 0, 0)
    val scores = Array(0.9, 0.8, 0.3, 0.1, 0.4, 0.9, 0.66, 0.7)

    val allIds = Array.fill(label0.length)("0") ++ Array.fill(label1.length)("1") ++
      Array.fill(label2.length)("2") ++ Array.fill(label3.length)("3")
    val allLabels = label0 ++ label1 ++ label2 ++ label3
    val allScores = scores ++ scores ++ scores ++ scores

    val tuples = allIds.zip(allScores).zip(allLabels.map(_.toDouble))
      .map(it => (it._1._1, it._1._2, it._2))
    val df = sc.makeRDD(tuples).toDF(metric.getUserIdCol, metric.getPredictionCol, metric.getLabelCol)

    val auc = metric.evaluate(df)
    println(auc)
    auc shouldBe (0.52381 +- 1e-5)
  }
}