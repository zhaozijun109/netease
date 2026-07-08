package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.Vectorizer
import com.netease.easyml.ml.sklearn.preprocessing.Normalizer
import com.netease.easyml.ml.util.{SchemaUtils, VectorUtils}
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.linalg_.BLAS
import org.apache.spark.sql.types.{ArrayType, StringType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * Created by linjiuning on 2020/11/11.
 * Kmeans
 * <p>
 * data schema:
 * [input] features: NumericType, MapType, StringType, ArrayType or VectorType
 * [output] prediction: Int
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * modelPath: output model path
 * inputCol: feature col name
 * maxIter: max iteration
 * k: cluster number
 * mode: train, predict
 * top: whether return array(k:v)
 * dropInput: whether drop input feature cols
 * startDay, endDay: day in [startDay, endDay]
 */
case class KMeansArgs(input: String, output: String, inputCol: String, k: Int, mode: String = "all", top: Int = 0,
                      maxIter: Int = 20, modelPath: String = NULL, dropInput: Boolean = true, startDay: String = "", endDay: String = "")

object KMeansUDS extends UDS[KMeansArgs] {

  def computeSimilarity(centers: Array[Vector],
                        point: Vector): Array[(Int, Double)] = {
    centers.zipWithIndex.map { case (center, i) =>
      val cosine = BLAS.dot(center, point)
      (i, cosine)
    }
  }

  def run(spark: SparkSession, args: Args): Unit = {
    var df = SparkUtil.loadFromTable(spark, args.input, startDay = args.startDay, endDay = args.endDay)

    val oriCols = df.columns

    val tmpCol = "__tmp"
    val vectorCol = "__vector"

    df = new Vectorizer()
      .setInputCol(args.inputCol)
      .setOutputCol(tmpCol)
      .fit(df)
      .transform(df)

    df = new Normalizer()
      .setInputCol(tmpCol)
      .setOutputCol(vectorCol)
      .setNorm("l2")
      .transform(df)
      .drop(tmpCol)

    val model = if (args.mode.equals("train") || args.modelPath.equals(NULL)) {
      val kmeans = new KMeans()
        .setFeaturesCol(vectorCol)
        .setK(args.k)
        .setMaxIter(args.maxIter)

      val model = kmeans.fit(df)
      if (IOUtil.exists(args.modelPath)) {
        IOUtil.delete(args.modelPath)
      }
      if (!args.modelPath.equals(NULL)) {
        model.save(args.modelPath)
      }
      model
    } else {
      KMeansModel.load(args.modelPath)
    }

    if (args.top <= 0) {
      df = model.transform(df)
    } else {
      val centers = model.clusterCenters.map(VectorUtils.normalize(_, 2.0))
      val centersBc = spark.sparkContext.broadcast(centers)
      val newRdd = df.toDF.rdd.map(row => {
        val vector = row.getAs[Vector](vectorCol)
        var scores = computeSimilarity(centersBc.value, vector)
        scores = scores
          .sortBy(-_._2)
          .slice(0, Math.min(scores.length, args.top))
        val predict = scores.map(it => "%d:%.5f" format(it._1, it._2))
        Row.fromSeq(row.toSeq ++ Seq(predict))
      })
      df = spark.createDataFrame(newRdd, SchemaUtils.appendColumn(df.schema, model.getPredictionCol, ArrayType(StringType)))
    }

    val keepCols = oriCols ++ Array(model.getPredictionCol)

    df.columns.filterNot(keepCols.contains).foreach(col => df = df.drop(col))

    if (args.dropInput) {
      args.inputCol.split(";").foreach(col => df = df.drop(col))
    }

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
