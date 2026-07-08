package com.netease.lofter.etl.ods

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AntiSendLogEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Anti send and Cheat Header Log Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val etlType = pargs.optional("etlType").getOrElse("normal")

    var path = s"/user/da_lofter/datastream/AntiSend/$dt"
    if (etlType.equalsIgnoreCase("video")) {
      path = s"/user/da_lofter/datastream/AntiSendVideo/$dt"
      }


    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val antiRiskDatasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 && line.contains(""""logType":1""")) {
          Some(line.substring(pos))
        } else None
      }

    val antiSpamDatasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 && line.contains(""""logType":0""")) {
          Some(line.substring(pos))
        } else None
      }

    val yiDunDatasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 && line.contains(""""logType":"asDunCheck"""")) {
          Some(line.substring(pos))
        } else None
      }

    if (etlType.equalsIgnoreCase("normal")) {
      antiSpamDatasetWithoutApmHeader.write.mode("overwrite").option("compression","gzip").text(s"/user/da_lofter/warehouse/AntiSpamCopy/dt=$dt")
      antiRiskDatasetWithoutApmHeader.write.mode("overwrite").option("compression","gzip").text(s"/user/da_lofter/warehouse/cheatHeader/dt=$dt")
      yiDunDatasetWithoutApmHeader.write.mode("overwrite").option("compression","gzip").text(s"/user/da_lofter/warehouse/YiDunLog/dt=$dt")

      spark.sql(s"alter table lofter.ods_log_cheat_header_info_di add if not exists partition (dt='$dt')")
      spark.sql(s"alter table lofter.ods_log_anti_spam_copy_di add if not exists partition (dt='$dt')")
    } else {
      antiSpamDatasetWithoutApmHeader.write.mode("overwrite").option("compression","gzip").text(s"/user/da_lofter/warehouse/AntiSpamCopyForVideo/dt=$dt")
      spark.sql(s"alter table lofter.ods_log_anti_spam_copy_video_di add if not exists partition (dt='$dt')")
    }

    spark.close()
  }
}
