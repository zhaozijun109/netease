package com.netease.easyml.ml

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.feature.{NSWord2Vec, PSNSWord2Vec}
import com.netease.easyml.ml.util.MLUtils
import com.tencent.angel.spark.context.PSContext
import org.apache.spark.ml.feature_.Word2Vec
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/9/28.
 */
class Word2VecSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder()
    .config("spark.ps.instances", "1")
    .getOrCreate()

  val dataPath = "/Users/linjiuning/workspace/git/netease/py_scripts/theseus/data/glue/data/tnews/wv/seg.txt"
  val outDir = "target/tmp/w2v"
  val inputCol = "features"
  val iter = 5
  val alpha = 0.025
  val minCount = 1
  val numPartitions = 4

  def dataset(): DataFrame = {
    import spark.implicits._
    val rawInput = spark.sparkContext.textFile(dataPath)
      .map(_.split(" ")).toDF(inputCol)

    rawInput
  }

  test("wordvec hs") {
    val path = IOUtil.join(outDir, "hs")
    if (IOUtil.exists(path)) {
      IOUtil.delete(path)
    }
    val training = dataset()

    val wordvec = new Word2Vec()
      .setMaxIter(iter)
      .setStepSize(alpha)
      .setMinCount(minCount)
      .setNumPartitions(numPartitions)
      .setInputCol(inputCol)

    wordvec.fit(training).save(path)
  }

  test("wordvec ns") {
    val path = IOUtil.join(outDir, "ns")
    if (IOUtil.exists(path)) {
      IOUtil.delete(path)
    }
    val training = dataset()

    val wordvec = new NSWord2Vec()
      .setMaxIter(iter)
      .setAlpha(alpha)
      .setMinCount(minCount)
      .setNumPartitions(numPartitions)
      .setInputCol(inputCol)

    wordvec.fit(training).save(path)
  }

  test("wordvec ps") {
    PSContext.getOrCreate(spark.sparkContext)
    val path = IOUtil.join(outDir, "ps")
    if (IOUtil.exists(path)) {
      IOUtil.delete(path)
    }

    val training = dataset()

    val wordvec = new PSNSWord2Vec()
      .setMaxIter(iter)
      .setNumPsPart(1)
      .setNumPartitions(numPartitions)
      .setInputCol(inputCol)
//      .setBatchWords(2500)
//      .setAlpha(0.1)
      .setMinCount(minCount)
      .setModelPath(path)

    wordvec.fit(training)

    PSContext.stop()
  }

  test("load ps") {
    MLUtils.loadAngelWordVec(spark.sparkContext, IOUtil.join(outDir, "ps"))
  }

  test("convert ps") {
    MLUtils.angelWordVecToText(spark, IOUtil.join(outDir, "ps"), IOUtil.join(outDir, "ps_text"), numPartitions = Some(1))
  }

  test("convert wv") {
    MLUtils.parquetWordVecToText(spark, IOUtil.join(outDir, "ns"), IOUtil.join(outDir, "ns_text"), numPartitions = Some(1))
  }
}
