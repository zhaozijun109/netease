package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.Vectorizer
import com.netease.easyml.ml.sklearn.preprocessing.Normalizer
import com.netease.easyml.ml.transform.FaissSearcher
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, udf}

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2020/11/9.
 * Embedding search based on Faiss
 * <p>
 * data schema:
 * [input] features: Array[Double], Array[Float], Vector
 * [output] labels: Array[Long] if no vocabulary is provided, else Array[String]
 * distances: Array[Float]
 * <p>
 * params:
 * input: input table
 * output: output table
 * featuresCol: col name of features
 * path: path of faiss index
 * vocab: path of vocab file
 * batchSize: batch size
 * k: return at most k vectors
 * nprobe: number of probes at query time, only used by IndexIVF
 * handleUnknown: handleUnknown labels
 * normalize: The norm to use to normalize each non zero sample. l2, l1 or none
 * threshold: threshold of distance
 * kv: whether user kv format
 * keepFeaturesCol: whether keep origin features col
 */
case class FaissSearcherArgs(input: String, output: String, featuresCol: String, path: String, vocab: String,
                             batchSize: Int = 128, k: Int = 100, nprobe: Int = -1, handleUnknown: String = "error", normalize: String = "l2",
                             threshold: Float = 0, kv: Boolean = true, keepFeaturesCol: Boolean = false)

object FaissSearcherUDS extends UDS[FaissSearcherArgs] {

  def toKv(labels: Seq[Any], scores: Seq[Float]): Seq[String] = {
    labels.zip(scores).map { case (k, v) => k + ":" + v }
  }

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")
    val conf = spark.sparkContext.getConf
    val numPartitions = 10 * SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val vocabs = if (IOUtil.exists(args.vocab)) {
      IOUtil.readLines(args.vocab, false).asScala.toArray
    } else {
      Array.empty[String]
    }

    val oriCols = df.columns

    val inputCol = if (!args.normalize.toLowerCase.equals("none")) {
      val normCol = args.featuresCol + "_norm"
      val inputCol = if (!SchemaUtils.isVectorType(df.schema, args.featuresCol)) {
        val vecCol = args.featuresCol + "_vec"
        df = new Vectorizer()
          .setInputCol(args.featuresCol)
          .setOutputCol(vecCol)
          .fit(df)
          .transform(df)
        vecCol
      } else {
        args.featuresCol
      }

      df = new Normalizer()
        .setNorm(args.normalize.toLowerCase)
        .setInputCol(inputCol)
        .setOutputCol(normCol)
        .transform(df)
      if (!inputCol.equals(args.featuresCol)) {
        df = df.drop(inputCol)
      }
      normCol
    } else {
      args.featuresCol
    }

    val model = new FaissSearcher()
      .setFeaturesCol(inputCol)
      .setPath(args.path)
      .setVocabs(vocabs)
      .setBatchSize(args.batchSize)
      .setK(args.k)
      .setHandleUnknown(args.handleUnknown)
      .setNumPartitions(numPartitions)

    if (args.nprobe >= 0) {
      model.setNprobe(args.nprobe)
    }

    if (args.threshold >= 0) {
      model.setThreshold(args.threshold)
    }

    df = model.transform(df)

    val labels = model.getLabelsCol
    val distances = model.getDistancesCol
    val keepCols = oriCols ++ Array(labels, distances)

    df.columns.filterNot(keepCols.contains).foreach(col => df = df.drop(col))
    if (!args.keepFeaturesCol) {
      df = df.drop(args.featuresCol)
    }

    df = df.filter(col(labels).isNotNull)

    if (args.kv) {
      val kvUdf = udf(toKv _)
      df = df.withColumn(labels, kvUdf(df(labels), df(distances)))
        .drop(distances)
    }

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
