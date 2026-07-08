package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

import java.io.File

/**
 * Created by linjiuning on 2020/12/25.
 * Read or write tf record.
 * <p>
 * params:
 * input: input table if read is false else path
 * output: output path if read is false else table
 * inputCols: col name of feature
 * recordType: Example or SequenceExample
 */

case class TFRecordArgs(input: String, output: String, inputCols: String = NULL, recordType: String = EXAMPLE, gzip: Boolean = true, partitionBy: String = "")

object TFRecordUDS extends UDS[TFRecordArgs] {

  def run(spark: SparkSession, args: Args): Unit = {

    val inputCols_ = if (!args.inputCols.equals(NULL)) {
      args.inputCols.split(";").filter(_.nonEmpty).mkString(",")
    } else {
      "*"
    }

    var df = if (IOUtil.isDirectory(args.input)) {
      SparkUtil.loadFromTfRecord(spark, args.input, recordType = args.recordType)
    } else {
      spark.sql(s"select * from ${args.input}")
    }

    if (!inputCols_.equals("*")) {
      df = df.select(args.inputCols.split(",").map(col): _*)
    }

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf) || args.output.equals(NULL)) {
      df.show(false)
    } else {
      if (args.output.contains(File.separator)) {
        SparkUtil.saveAsTfRecord(df, args.output, recordType = args.recordType, gzip = args.gzip, partitionBy = Some(args.partitionBy.split(",")))
      } else {
        SparkUtil.saveAsTable(df, args.output)
      }
    }
  }
}
