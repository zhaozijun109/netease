package com.netease.easyml.ml.transform

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/27.
 */
class ONNXPredictorTest extends FunSuite with Matchers with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  test("nickname") {
    import spark.sqlContext.implicits._
    val path = "/Users/linjiuning/workspace/git/netease/py_scripts/gender/data/model.onnx"
    val onnx = new ONNXPredictor()
      .setPath(path)

    val df = sc.makeRDD(Seq("林久宁", "张长江"))
      .repartition(2)
      .toDF("input")

    val result = onnx.transform(df)

    result.show()
  }
}
