package com.netease.easyml.uds

import com.netease.easyml.uds.examples.{BertEmbeddingArgs, BertEmbeddingUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/7/26.
 */
class BertEmbeddingSuite extends FunSuite {
  val spark: SparkSession = SparkSession.builder().master("local[4]").getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  val input = "input"
  val output = "output"

  val vocabPath = "null"
  //  val exportDir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/glue/STS-B/tmp/saved_model"
  val exportDir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/torch/clip/tmp/models/clip.pt"
  val textACol = "text_a"
  val textBCol = "null"
  val batchSize = 32
  val maxLength = 64
  val lowercase = true

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      "一个女孩在给她的头发做发型。", "一个女孩在梳头。",
      "一群男人在海滩上踢足球。", "一群男孩在海滩上踢足球。"
    )
    ).toDF(textACol)
  }

  test("bert") {
    val ds = dataset()
    ds.createOrReplaceTempView(input)

    val args = BertEmbeddingArgs(input = input, output = output, vocabPath = vocabPath, exportDir = exportDir,
      textACol = textACol, textBCol = textBCol,
      maxLength = maxLength, lowercase = lowercase, batchSize = batchSize)

    BertEmbeddingUDS.run(spark, args)
  }
}
