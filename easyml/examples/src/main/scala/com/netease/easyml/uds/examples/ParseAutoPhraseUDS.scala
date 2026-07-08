package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/6/22.
 * Parse autophrase result and write to hive
 * <p>
 * data schema:
 * [output] word: String, ngram: Integer score: Float
 * <p>
 * params:
 * input: input hdfs path
 * output: output table
 * threshold
 */
case class ParseAutoPhraseArgs(input: String, output: String, threshold: Double)

object ParseAutoPhraseUDS extends UDS[ParseAutoPhraseArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val rdd = spark.sparkContext.textFile(args.input)
      .map(line => {
        val i = line.indexOf("\t")
        if (i > 0) {
          val score = line.slice(0, i).toDouble

          val word = line.slice(i + 1, line.length).trim
          val ngram = word.split(" ").filter(_.nonEmpty)

          (ngram.mkString(""), ngram.length, score)
        } else {
          null
        }
      }).filter(it => it != null && it._1.nonEmpty && it._3 >= args.threshold)

    import spark.implicits._
    val df = rdd.toDF("word", "ngram", "score")
    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output, "textfile")
    }
  }
}
