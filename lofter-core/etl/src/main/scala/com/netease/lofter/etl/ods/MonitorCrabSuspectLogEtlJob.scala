package com.netease.lofter.etl.ods

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object MonitorCrabSuspectLogEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Monitor Crab Suspect Log Etl")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val path = s"/user/da_lofter/datastream/MonitorCrabSuspectLog/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val suspectLogWithoutApmHeader = spark.read.textFile(path)
      .flatMap { line =>
        val pos = line.indexOf("{\"")
        if(pos >= 0 ) {
          Some(line.substring(pos))
        } else None
      }

    suspectLogWithoutApmHeader.write.mode("overwrite").text(s"/user/da_lofter/warehouse/MonitorCrabSuspectLog/dt=$dt")
    spark.sql(s"alter table lofter.ods_log_monitor_crab_suspect_di add if not exists partition(dt='$dt')")

    spark.close()
  }
}
