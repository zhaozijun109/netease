package com.netease.easyml.ml.sklearn.naive_bayes

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/12.
 */
class NaiveBayesSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val multinomialPicklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/multinomial_nb.pkl"
  val bernoulliPicklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/bernoulli_nb.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(3.0, 4.0, 0.0, 1.0, 3.0)),
      (1, Array(1.0, 2.0, 4.0, 2.0, 4.0)),
      (2, Array(4.0, 1.0, 1.0, 0.0, 1.0))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "features")
  }

  test("sklearn multinomial") {
    val df = makeDf()
    val model = SklearnUtils.read(multinomialPicklePath, classOf[Model[_]])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}