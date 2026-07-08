package com.netease.easyml.ml.sklearn.preprocessing

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.feature.{StandardScalerModel => SparkStandardScalerModel}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/19.
 */
class RobustScalerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val modelPath = "target/tmp/robust_scaler"
  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/robust_scaler.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(1.0, -2.0, 2.0)),
      (1, Array(-2.0, 1.0, 3.0)),
      (2, Array(4.0, 1.0, -2.0))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "inputCol")
  }

  test("fit") {
    if (IOUtil.exists(modelPath))
      IOUtil.delete(modelPath)
    val df = makeDf()
    val estimator = new RobustScaler()
      .setInputCol("inputCol")
      .setOutputCol("outputCol")

    val model = estimator.fit(df)
    model.save(modelPath)
  }

  test("transform") {
    val df = makeDf()
    val model = SparkStandardScalerModel.load(modelPath)
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
