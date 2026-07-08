package com.netease.lofter.etl.ods

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AntiRiskSimilarityEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Anti Risk Similarity Log Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val path = s"/user/da_lofter/datastream/antiRiskSimilarity/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val postDatasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 && line.contains(""""modelName":"发文"""")) {
          Some(line.substring(pos))
        } else None
      }

    val messageDatasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 && line.contains(""""modelName":"私信"""")) {
          Some(line.substring(pos))
        } else None
      }

    postDatasetWithoutApmHeader.write.mode("overwrite").text(s"/user/da_lofter/warehouse/AntiRiskSimilarityPost/dt=$dt")
    messageDatasetWithoutApmHeader.write.mode("overwrite").text(s"/user/da_lofter/warehouse/AntiRiskSimilarityMessage/dt=$dt")

    spark.sql(s"alter table lofter.ods_log_anti_risk_similarity_post_di add if not exists partition (dt='$dt')")
    spark.sql(s"alter table lofter.ods_log_anti_risk_similarity_message_di add if not exists partition (dt='$dt')")

    spark.close()
  }
}
