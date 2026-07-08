package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{PSWordVecArgs, PSWordVecUDS, WordVecArgs, WordVecUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/10/22.
 */
class WordVecSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder()
    .config("spark.ps.instances", "1")
    .getOrCreate()

  val dataPath = "/Users/linjiuning/Downloads/session.txt"
  val outDir = "target/tmp/w2v"
  val inputTable = "w2v_tmp"
  val inputCol = "features"
  val iter = 1
  val alpha = 0.025
  val vectorSize = 100
  val minCount = 10
  val negative = 5
  val shuffle = false

  val psAlpha = 0.1
  val psBatchSize = 100

  def dataset(): DataFrame = {
    import spark.implicits._
    val rawInput = spark.sparkContext.textFile(dataPath)
      .sample(false, 0.0001)
      .map(_.split(String.valueOf('\u0002'))).toDF(inputCol)
    //    rawInput.show(false)
    rawInput
  }

  test("wordvec") {
    // ns, hs, ps
    val mode = "ns"
    val ckpt = IOUtil.join(outDir, mode)
    if (IOUtil.exists(ckpt)) {
      IOUtil.delete(ckpt)
    }

    val output = IOUtil.join(outDir, mode + "_text")

    val training = dataset()
    training.createOrReplaceTempView(inputTable)

    if (mode.equals("ps")) {
      val args = PSWordVecArgs(input = inputTable, checkpoint = ckpt, output = output, inputCol = inputCol, iter = iter, lr = psAlpha,
        vectorSize = vectorSize, minCount = minCount, negative = negative, batchSize = psBatchSize, shuffle = shuffle)
      PSWordVecUDS.run(spark, args)
    } else {
      val args = WordVecArgs(input = inputTable, checkpoint = ckpt, output = output, inputCol = inputCol, iter = iter, lr = alpha,
        vectorSize = vectorSize, minCount = minCount, negative = if (mode.equals("hs")) 0 else negative, shuffle = shuffle)
      WordVecUDS.run(spark, args)
    }
  }
}
