package com.netease.lofter.etl.dws

import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * 设备会话聚合指标数据
 */
object DeviceSessionJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Device Session Job")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.required("date")

    val sessionSql =
      s"""
         |select deviceUdid, appVersion,appChannel,deviceOs,
         |       count(1) as sessionCount,
         |       sum(nextOccurTime - occurTime) as sessionTime
         |from (select deviceUdid, eventId, occurTime, appVersion, appChannel, deviceOs,
         |            lead(eventId,1) over(partition by deviceUdid,appVersion,appChannel,deviceOs order by occurTime) as nextEvent,
         |            lead(occurTime,1) over(partition by deviceUdid,appVersion,appChannel,deviceOs order by occurTime) nextOccurTime
         |            from lofter.ods_mda_app_partition_di where dt = '$dt' and actionType = 'system' and eventId in('da_session_start', 'da_session_close')
         |) t
         |where (nextOccurTime - occurTime) < 24 * 3600 * 1000 and
         |      eventId = 'da_session_start' and
         |      nextEvent= 'da_session_close'
         |group by deviceUdid, appVersion, appChannel, deviceOs
       """.stripMargin

    spark.sql(sessionSql)
      .withColumn("dt", lit(dt))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dws_par_device_session_di")

    spark.close()
  }
}
