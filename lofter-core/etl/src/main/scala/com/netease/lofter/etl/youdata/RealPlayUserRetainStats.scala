package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.lit

object RealPlayUserRetainStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName(name = "Lofter Video Real Play User Retain Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val realPlayBase =
      s"""
         |select count(distinct deviceudid) as realplay_uv
         |from lofter.ods_mda_app_partition_di
         |where dt = '$date' and actionType = 'page_duration'
         |  and eventId in ('l3-1')
         |  and params['costtime']>=5000
         |""".stripMargin

    spark.sql(realPlayBase)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_video_realplay_di")

    for(daysAgo <- Seq(2, 3, 7)) {
      val baseDate = DateTime.parse(date).minusDays(daysAgo - 1).toString("yyyy-MM-dd")

      val realPlayRetain =
        s"""
           |select t1.dt as baseDate,
           |   count(distinct t1.deviceudid) realplay_uv,
           |   count(distinct t2.deviceudid) remain_uv
           |from
           |(
           |  select dt,deviceudid
           |  from lofter.ods_mda_app_partition_di
           |  where dt = '$baseDate' and actionType = 'page_duration'
           |    and eventId in ('l3-1')
           |    and params['costtime']>=5000
           |  group by dt,deviceudid
           |) t1
           |left join
           |(
           |  select dt,deviceudid
           |  from lofter.ods_mda_app_partition_di
           |  where dt = '$date' and actionType = 'page_duration'
           |  and eventId in ('l3-1')
           |  and params['costtime']>=5000
           |  group by dt,deviceudid
           |) t2 on t1.deviceudid=t2.deviceudid
           |group by t1.dt
           |""".stripMargin

      spark.sql(realPlayRetain)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(daysAgo))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_video_retain_realplay_di")
    }

    spark.close()
  }

}
