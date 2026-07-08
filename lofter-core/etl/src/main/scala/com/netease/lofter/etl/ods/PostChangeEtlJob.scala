package com.netease.lofter.etl.ods

import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object PostChangeEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("PostChange Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.required("date")

    val path = s"/user/da_lofter/datastream/post_change/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val datasetWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0) {
          Some(line.substring(pos))
        } else None
      }

    val outPath = s"/user/da_lofter/warehouse/post_change/dt=$dt"
    datasetWithoutApmHeader.write.mode("overwrite").text(outPath)

    spark.sql(s"alter table lofter.ods_log_post_change_di add if not exists partition (dt='$dt') location '$outPath' ")

    spark.close()
  }
}
