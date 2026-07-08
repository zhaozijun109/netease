package com.netease.easyml.ml.sklearn

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.decomposition.PCAModel
import com.netease.easyml.ml.sklearn.feature_extraction.{CountVectorizerModel, HashingVectorizer}
import com.netease.easyml.ml.sklearn.preprocessing.Normalizer
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/8/5.
 */
class SklearnUtilsSuite extends FunSuite with Matchers with SharedSparkContext {
  lazy val spark = SparkSession.builder().getOrCreate()

  def getPicklePath(name: String): String = {
    IOUtil.join("/Users/linjiuning/workspace/git/netease/py_scripts/sklearn", name)
  }

  test("count_vec") {
    val path = getPicklePath("count_vec.pkl")
    val model = SklearnUtils.read(path, classOf[CountVectorizerModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      "This is the first document.",
      "This document is the second document.",
      "And this is the third one.",
      "Is this the first document?"
    )
    val df = sc.makeRDD(features).toDF(model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("hash_vec") {
    val path = getPicklePath("hash_vec.pkl")
    val model = SklearnUtils.read(path, classOf[HashingVectorizer])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      "This is the first document.",
      "This document is the second document.",
      "And this is the third one.",
      "Is this the first document?"
    )
    val df = sc.makeRDD(features).toDF(model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("tfidf") {
    val path = getPicklePath("tfidf.pkl")
    val model = SklearnUtils.read(path, classOf[PipelineModel])
    import spark.sqlContext.implicits._
    val features = Seq(
      "This is the first document.",
      "This document is the second document.",
      "And this is the third one.",
      "Is this the first document?"
    )
    val df = sc.makeRDD(features).toDF("text")
    val result = model.transform(df)

    result.show(false)
  }

  test("lr") {
    val path = getPicklePath("lr.pkl")
    val model = SklearnUtils.read(path, classOf[LogisticRegressionModel])

    import spark.sqlContext.implicits._
    val features = Seq(
      (Array(5.1, 3.5, 1.4, 0.2), 0),
      (Array(4.9, 3.0, 1.4, 0.2), 0)
    ).map(it => (Vectors.dense(it._1), it._2))
    val df = sc.makeRDD(features).toDF(model.getFeaturesCol, model.getLabelCol)
    val result = model.transform(df)
    result.show(false)
  }

  test("pipeline") {
    val path = getPicklePath("pipeline.pkl")
    val model = SklearnUtils.read(path, classOf[PipelineModel])
    import spark.sqlContext.implicits._
    val features = Seq(
      "This is the first document.",
      "This document is the second document.",
      "And this is the third one.",
      "Is this the first document?"
    )
    val df = sc.makeRDD(features).toDF("text")
    val result = model.transform(df)

    result.show(false)
  }

  test("label encoder") {
    val path = getPicklePath("le.pkl")
    val model = SklearnUtils.read(path, classOf[StringIndexerModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq("tokyo", "tokyo", "paris")
    val df = sc.makeRDD(features).toDF(model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }


  test("max abs scaler") {
    val path = getPicklePath("max_abs_scaler.pkl")
    val model = SklearnUtils.read(path, classOf[MaxAbsScalerModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(1.0, -1.0, 2.0)),
      (1, Array(2.0, 0.0, 0.0)),
      (2, Array(0.0, 1.0, -1.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("min max scaler") {
    val path = getPicklePath("min_max_scaler.pkl")
    val model = SklearnUtils.read(path, classOf[MinMaxScalerModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(-1.0, 2.0)),
      (1, Array(-0.5, 6.0)),
      (2, Array(0.0, 10.0)),
      (3, Array(1.0, 18.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("normalizer") {
    val path = getPicklePath("normalizer.pkl")
    val model = SklearnUtils.read(path, classOf[Normalizer])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(4.0, 1.0, 2.0, 2.0)),
      (1, Array(1.0, 3.0, 9.0, 3.0)),
      (2, Array(5.0, 7.0, 5.0, 1.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("standard scaler") {
    val path = getPicklePath("standard_scaler.pkl")
    val model = SklearnUtils.read(path, classOf[StandardScalerModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(0.0, 0.0)),
      (1, Array(0.0, 0.0)),
      (2, Array(1.0, 1.0)),
      (3, Array(1.0, 1.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("binarizer") {
    val path = getPicklePath("binarizer.pkl")
    val model = SklearnUtils.read(path, classOf[Binarizer])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(1.0, -1.0, 2.0)),
      (1, Array(2.0, 0.0, 0.0)),
      (2, Array(0.0, 1.0, -1.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }

  test("pca") {
    val path = getPicklePath("pca.pkl")
    val model = SklearnUtils.read(path, classOf[PCAModel])
    model.setInputCol("inputCol")
    model.setOutputCol("outputCol")
    import spark.sqlContext.implicits._
    val features = Seq(
      (0, Array(-1.0, -1.0)),
      (1, Array(-2.0, -1.0)),
      (2, Array(-3.0, -2.0)),
      (3, Array(1.0, 1.0)),
      (4, Array(2.0, 1.0)),
      (5, Array(3.0, 2.0))
    ).map(it => (it._1, Vectors.dense(it._2)))
    val df = sc.makeRDD(features).toDF("id", model.getInputCol)
    val result = model.transform(df)

    result.show(false)
  }
}
