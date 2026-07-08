//package com.netease.easyml.ml
//
//import com.intel.analytics.bigdl.utils.Engine
//import org.apache.spark.SparkConf
//import org.apache.spark.sql.SparkSession
//import org.scalatest.FunSuite
//
///**
// * Created by linjiuning on 2020/9/2.
// */
//class AnalyticsSuite extends FunSuite {
//  val conf: SparkConf = Engine.createSparkConf().setMaster("local[1]")
//  val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
//
//  test("NNEstimator") {
//    import com.intel.analytics.bigdl.nn._
//    import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric.NumericFloat
//    import com.intel.analytics.zoo.pipeline.nnframes.NNEstimator
//    Engine.init
//
//    val model = Sequential().add(Linear(2, 2))
//    val criterion = MSECriterion()
//    val estimator = NNEstimator(model, criterion)
//      .setLearningRate(0.2)
//      .setMaxEpoch(40)
//    val data = spark.sparkContext.parallelize(Seq(
//      (Array(2.0, 1.0), Array(1.0, 2.0)),
//      (Array(1.0, 2.0), Array(2.0, 1.0)),
//      (Array(2.0, 1.0), Array(1.0, 2.0)),
//      (Array(1.0, 2.0), Array(2.0, 1.0))))
//    val df = spark.createDataFrame(data).toDF("features", "label")
//    val nnModel = estimator.fit(df)
//    nnModel.transform(df).show(false)
//  }
//
//}
