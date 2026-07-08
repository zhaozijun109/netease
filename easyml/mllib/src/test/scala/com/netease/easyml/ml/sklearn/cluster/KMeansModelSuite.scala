package com.netease.easyml.ml.sklearn.cluster

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.clustering.{KMeansModel => SparkKMeansModel}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/11.
 */
class KMeansModelSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/kmeans.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(0.0, 0.0)),
      (1, Array(12.0, 3.0)),
      (2, Array(-19.0, 3.0))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "features")
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath, classOf[SparkKMeansModel])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}