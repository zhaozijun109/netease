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
class KernalCentererSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val modelPath = "target/tmp/kernal_centerer"
  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/kernal_centerer.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array(9.0, 2.0, -2.0)),
      (1, Array(2.0, 14.0, -13.0)),
      (2, Array(-2.0, -13.0, 21.0))
    )).map(it => (it._1, Vectors.dense(it._2))).toDF("id", "inputCol")
  }

  test("fit") {
    if (IOUtil.exists(modelPath))
      IOUtil.delete(modelPath)
    val df = makeDf()
    val estimator = new KernalCenterer()
      .setInputCol("inputCol")
      .setOutputCol("outputCol")

    val model = estimator.fit(df)
    model.save(modelPath)
  }

  test("transform") {
    val df = makeDf()
    val model = KernalCentererModel.load(modelPath)
    val newDf = model.transform(df)
    newDf.show(false)
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath, classOf[KernalCentererModel])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}
