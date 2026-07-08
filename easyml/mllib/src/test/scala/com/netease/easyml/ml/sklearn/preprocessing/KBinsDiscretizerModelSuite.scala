package com.netease.easyml.ml.sklearn.preprocessing

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/18.
 */
class KBinsDiscretizerModelSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val modelPath = "target/tmp/k_bins_discretizer"
  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/k_bins_discretizer.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(-2.0, 1.0, -4.0, -1.0)),
      (1, Array(-3.3, 2.0, -3.0, -0.5)),
      (2, Array(0, 3, -2, 0.5)),
      (3, Array(1, 4, -1, 2.0))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "inputCol")
  }

  test("fit") {
    if (IOUtil.exists(modelPath))
      IOUtil.delete(modelPath)
    val df = makeDf()
    val estimator = new KBinsDiscretizer()
      .setNBins(3)
      .setEncode("ordinal")
      .setStrategy("quantile")
      .setInputCol("inputCol")
      .setOutputCol("outputCol")

    val model = estimator.fit(df)
    model.save(modelPath)
  }

  test("transform") {
    val df = makeDf()
    val model = KBinsDiscretizerModel.load(modelPath)
    val newDf = model.transform(df)
    newDf.show(false)
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath)
    val newDf = model.transform(df)
    newDf.show(false)
  }
}
