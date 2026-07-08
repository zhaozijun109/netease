package com.netease.lofter.etl.ods

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdOuterLinkupEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("ad outer linkup log etl")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val path = s"/user/da_lofter/datastream/outer-linkup/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val logWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 ) {
          Some(line.substring(pos))
        } else None
      }

    logWithoutApmHeader.repartition().write.option("compression","gzip").mode("overwrite").text(s"/user/da_lofter/warehouse/adx_outer_linkup/dt=$dt")
    spark.sql(s"alter table lofter.ods_log_ad_outer_linkup_di add if not exists partition(dt='$dt')")

    spark.close()
  }
}
