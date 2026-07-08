package com.netease.easyml.ml.sklearn.cluster

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.clustering.{GaussianMixtureModel => SparkGaussianMixtureModel}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/11.
 */
class GaussianMixtureModelSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/gaussian_mixture.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(21.76405235, 20.40015721)),
      (1, Array(20.97873798, 22.2408932)),
      (2, Array(2.06396262, 1.82110875))
    )).map(it => (it._1, Vectors.dense(it._2)))
      .toDF("id", "features")
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath, classOf[SparkGaussianMixtureModel])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}