package com.netease.easyml.ml.sklearn.preprocessing

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/18.
 */
class MultiLabelBinarizerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val modelPath = "target/tmp/multi_label_binarizer"
  val picklePath = "/Users/linjiuning/workspace/git/netease/py_scripts/sklearn/multi_label_binarizer.pkl"

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(Seq("a", "b"), Seq("c"))).toDF("inputCol")
  }

  test("fit") {
    if (IOUtil.exists(modelPath))
      IOUtil.delete(modelPath)
    val df = makeDf()
    val estimator = new MultiLabelBinarizer()
      .setInputCol("inputCol")
      .setOutputCol("outputCol")

    val model = estimator.fit(df)
    model.save(modelPath)
  }

  test("transform") {
    val df = makeDf()
    val model = MultiLabelBinarizerModel.load(modelPath)
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
