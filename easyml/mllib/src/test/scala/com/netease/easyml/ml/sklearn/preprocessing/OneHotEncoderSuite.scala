package com.netease.easyml.ml.sklearn.preprocessing

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/11.
 */
class OneHotEncoderSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val modelPath = "target/tmp/onehot_encoder"
  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/onehot_encoder.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      (0, Array("Male", "1")),
      (1, Array("Female", "3")),
      (2, Array("Female", "2"))
    )).toDF("id", "inputCol")
  }

  test("fit") {
    if (IOUtil.exists(modelPath))
      IOUtil.delete(modelPath)
    val df = makeDf()
    val estimator = new OneHotEncoder()
      .setInputCol("inputCol")
      .setOutputCol("outputCol")
      .setDrop("if_binary")

    val model = estimator.fit(df)
    model.save(modelPath)
  }

  test("transform") {
    val df = makeDf()
    val model = OneHotEncoderModel.load(modelPath)
      .setSparse(false)
    val newDf = model.transform(df)
    newDf.show(false)
  }

  test("sklearn") {
    val df = makeDf()
    val model = SklearnUtils.read(picklePath, classOf[OneHotEncoderModel])
    val newDf = model.transform(df)
    newDf.show(false)
  }
}
