package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.util.Constant
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2022/8/17.
 */
case class CountTfrecordArgs(input: String, recordType: String = Constant.EXAMPLE)

object CountTfrecordUDS extends UDS[CountTfrecordArgs] {
  def run(spark: SparkSession, args: Args): Unit = {
    val df = SparkUtil.loadFromTfRecord(spark, args.input, recordType = args.recordType)
    println(s"COUNT: ${df.count()}")
  }
}
