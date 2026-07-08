package com.netease.easyml.ml.sklearn.linear_model

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/12.
 */
class LinearSVCSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/linear_svc.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(-0.01776, 0.21514, 0.23358, 0.11607)),
      (1, Array(-0.27616, 1.54202, 1.58732, 0.66371)),
      (2, Array(-0.84277, 0.02854, -0.45921, -0.93393)),
      (3, Array(0.18081, -0.53482, -0.50094, -0.13415)),
      (4, Array(-0.18553, -0.49176, -0.66579, -0.52071))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "features")
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath, classOf[Model[_]])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}