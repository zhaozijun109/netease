package com.netease.easyml.ml.nlp

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/1.
 */
class PMIStatSuite extends FunSuite with SharedSparkContext {
  lazy val spark = SparkSession.builder().getOrCreate()

  test("pmi") {
    val df = spark.read.json("/Users/linjiuning/workspace/git/netease/py_scripts/utils/dl/sideinfo.txt")
      .drop("item")
      .sample(0.01)

    df.show(false)

    val pmi = new PMIStat()
      .setLowercase(true)
      .setInputCol("tags")
      .setNormalize(true)

    val newDf = pmi.transform(df)

    newDf.show(false)
  }
}
