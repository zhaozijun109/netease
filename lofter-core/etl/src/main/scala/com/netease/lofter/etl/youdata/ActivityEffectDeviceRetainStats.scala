package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object ActivityEffectDeviceRetainStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Activity Effect Device Retain Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val retainDays = Seq(2,7,30)

    // stat the retain ratio for 1DaysAgo, 7DaysAgo, 30DaysAgo
    for (daysAgo <- retainDays){
      newAndCallbackDeviceRetain(spark, date, daysAgo)
    }

    spark.close()
  }

  def newAndCallbackDeviceRetain(spark: SparkSession, date: String, daysAgo: Int): Unit = {
    val newDay = DateTime.parse(date).minusDays(daysAgo-1).toString("yyyy-MM-dd")

    val sql_device_new_and_callback_detail =
      s"""
         |select aa.activityId,activityName,
         |       bb.deviceUdid as deviceNew,
         |       cc.deviceUdid as deviceCallBack
         |from (
         |    select b.dt,a.activityId,a.activityName,userId,mdaDeviceUdid,occurtime
         |    from (
         |        select activityId,activityName from lofter_db_dump.ods_db_act_activity_effect_base_config_nd
         |        where status=0 and from_unixtime(cast(startTime/1000 as bigint),'yyyy-MM-dd')<='$newDay' and
         |              from_unixtime(cast(endTime/1000 as bigint),'yyyy-MM-dd')>='$newDay'
         |    ) a
         |    join (
         |        select dt, activityId, userId, deviceUdid as mdaDeviceUdid, occurTime
         |        from lofter.dwd_activity_action_di
         |        where dt = '$newDay'
         |    ) b on a.activityId=b.activityId
         |) aa
         |
         |left join (
         |    select finalUserId userId,deviceUdid,dt,firstaccesstime
         |    from lofter.device_new
         |    where dt = '$newDay'
         |) bb on aa.mdaDeviceUdid=bb.deviceUdid and aa.dt=bb.dt and (aa.occurtime - bb.firstaccesstime)<=180000
         |left join (
         |    select dt,deviceUdid,firstaccesstime
         |    from lofter.device_return
         |    where dt='$newDay' and period=30
         |) cc on aa.mdaDeviceUdid=cc.deviceUdid and aa.dt=cc.dt and (aa.occurtime - cc.firstaccesstime)<=180000
         |""".stripMargin

    spark.sql(sql_device_new_and_callback_detail).createOrReplaceTempView("newCallbackDetail")

    val sql_new_device_retain =
      s"""
         |select '$newDay' as baseDate,a.activityId,activityName,
         |       count(distinct b.deviceUdid) as newDeviceRetain
         |from
         |(select distinct activityId,activityName,deviceNew from newCallbackDetail where length(deviceNew)>0) a
         |left join
         |(select dt,deviceUdid from lofter.device_active where dt='$date') b
         |on a.deviceNew=b.deviceUdid
         |group by 1,2,3
       """.stripMargin

    val sql_callback_device_retain =
      s"""
         |select '$newDay' as baseDate,a.activityId,activityName,
         |       count(distinct b.deviceUdid) as callbackDeviceRetain
         |from
         |(select distinct activityId,activityName,deviceCallBack from newCallbackDetail where length(deviceCallBack)>0) a
         |left join
         |(select dt,deviceUdid from lofter.device_active where dt='$date') b
         |on a.deviceCallBack=b.deviceUdid
         |group by 1,2,3
       """.stripMargin

    spark.sql(sql_new_device_retain)
      .withColumn("dt", lit(date))
      .withColumn("period", lit(daysAgo))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_activity_new_device_retain_di")

    spark.sql(sql_callback_device_retain)
      .withColumn("dt", lit(date))
      .withColumn("period", lit(daysAgo))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_activity_callback_device_retain_di")
  }

}
