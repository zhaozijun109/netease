package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object HubbleResultStats {

  val batchSize = 100
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hubble Result Stats")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_active =
      s"""
         |select appVersion, appChannels[0] as appChannel, deviceOs,
         |       count(distinct(deviceUdid)) as active_device
         |from lofter.device_active
         |     lateral view explode(appVersions) as appVersion
         |where dt = '$date'
         |group by 1, 2, 3
       """.stripMargin

    val sql_new =
      s"""
         |select appVersion, appChannel, deviceOs,
         |       count(distinct(deviceUdid)) as new_device
         |from lofter.device_new
         |where dt = '$date'
         |group by appVersion, appChannel, deviceOs
       """.stripMargin

    val sql_time_stats =
      s"""
         |select appversion,appchannel,deviceOs,sum(sessiontime)/count(distinct deviceudid) as per_time, sum(session_fre) as session_times
         |  from (select deviceudid,appversion,appchannel,deviceOs,count(1) as session_fre,sum(occur2-occurtime) as sessiontime
         |    from (select deviceudid,eventid,occurtime,appversion,appchannel,deviceOs,
         |            lead(eventid,1) over(partition by deviceudid,appversion,appchannel,deviceOs order by occurtime) as event2,
         |            lead(occurtime,1) over(partition by deviceudid,appversion,appchannel,deviceOs order by occurtime) occur2
         |            from (select * from lofter.ods_mda_app_partition_di where dt= '$date' and actionType in ('system', 'other')) a  where eventid in('da_session_start', 'da_session_close') ) t1
         |       where (occur2-occurtime) <24*3600*1000 and eventid='da_session_start' and event2='da_session_close'
         |group by  deviceudid,appversion,appchannel,deviceOs ) t2 group by appversion,appchannel,deviceOs
       """.stripMargin

    spark.sql(sql_active).createOrReplaceTempView("tb_active")
    spark.sql(sql_new).createOrReplaceTempView("tb_new")
    spark.sql(sql_time_stats).createOrReplaceTempView("tb_time_stats")

    val sql_result =
      s"""
         |select a.*,b.new_device,c.per_time,c.session_times
         |from tb_active a
         |left join tb_new b on a.appVersion = b.appVersion and a.appchannel = b.appchannel and a.deviceOs = b.deviceOs
         |left join tb_time_stats c on a.appVersion = c.appVersion and a.appchannel = c.appchannel and a.deviceOs = c.deviceOs
       """.stripMargin

    spark.sql(sql_result)
      .repartition(1)
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_hubble_appchannel_session_di")
  }

}
