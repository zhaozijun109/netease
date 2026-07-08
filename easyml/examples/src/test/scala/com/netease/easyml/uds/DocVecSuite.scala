package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/10/22.
 */
class DocVecSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder()
    .getOrCreate()

  val dataPath = "/Users/linjiuning/workspace/git/netease/py_scripts/theseus/data/glue/data/tnews/wv/seg.txt"
  val vecDir = "target/tmp/w2v"
  val outDir = "target/tmp/d2v"
  val inputTable = "d2v_tmp"
  val idCol = "word"
  val inputCol = "tokens"

  val minDf = 5

  val a = 1e-3
  val docLevel = false
  val useSVD = false
  val filterEmpty = false

  if (!IOUtil.exists(outDir)) {
    IOUtil.mkdirs(outDir)
  }

  def dataset(concat: Boolean = false): DataFrame = {
    import spark.implicits._
    if (concat) {
      spark.sparkContext.textFile(dataPath)
        .map(it => {
          val tokens = it.split(" ")
          val text = tokens.mkString("")
          (text, it)
        }).toDF(idCol, inputCol)
    } else {
      spark.sparkContext.textFile(dataPath)
        .map(it => {
          val tokens = it.split(" ")
          val text = tokens.mkString("")
          (text, tokens)
        }).toDF(idCol, inputCol)
    }
  }

  test("idf weight") {
    val path = IOUtil.join(outDir, "idf_wt")

    val training = dataset(true)
    training.createOrReplaceTempView(inputTable)

    val args = IDFWeightArgs(inputTable, path, inputCol, minDf)
    IDFWeightUDS.run(spark, args)
  }

  test("sif weight") {
    val path = IOUtil.join(outDir, "sif_wt")

    val training = dataset()
    training.createOrReplaceTempView(inputTable)

    val args = SIFWeightArgs(inputTable, path, inputCol, a, docLevel)
    SIFWeightUDS.run(spark, args)
  }

  test("d2v") {
    // mean, idf, sif
    val mode = "mean"
    val path = IOUtil.join(outDir, "d2v_" + mode)
    if (IOUtil.exists(path)) {
      IOUtil.delete(path)
    }

    val vector = IOUtil.join(vecDir, "ns_text/part-00000")
    val weight = IOUtil.join(outDir, mode + "_wt")

    val training = dataset()
    training.createOrReplaceTempView(inputTable)

    val args = DocVecArgs(inputTable, path, idCol, inputCol, mode, vector, weight, useSVD, filterEmpty)
    DocVecUDS.run(spark, args)
  }

}
