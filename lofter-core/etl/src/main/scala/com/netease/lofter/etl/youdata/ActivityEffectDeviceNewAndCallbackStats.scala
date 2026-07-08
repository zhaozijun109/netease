package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object ActivityEffectDeviceNewAndCallbackStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Activity Effect Device New And Callback Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    deviceNewAndCallbackStat(spark,date)

    spark.close()
  }

  def deviceNewAndCallbackStat(spark: SparkSession, date: String): Unit = {
    val sql_device_new_callback =
      s"""
         |select aa.activityId,activityName,
         |       count(distinct bb.deviceUdid) as deviceNew,
         |       count(distinct cc.deviceUdid) as deviceCallBack
         |from (
         |    select b.dt,a.activityId,a.activityName,userId,mdaDeviceUdid,occurtime
         |    from (
         |        select activityId,activityName from lofter_db_dump.ods_db_act_activity_effect_base_config_nd
         |        where status=0 and from_unixtime(cast(startTime/1000 as bigint),'yyyy-MM-dd')<='$date' and
         |              from_unixtime(cast(endTime/1000 as bigint),'yyyy-MM-dd')>='$date'
         |    ) a
         |    join (
         |        select dt, activityId, userId, deviceUdid as mdaDeviceUdid, occurTime
         |        from lofter.dwd_activity_action_di
         |        where dt = '$date'
         |    ) b on a.activityId=b.activityId
         |) aa
         |left join (
         |    select finalUserId userId,deviceUdid,dt,firstaccesstime
         |    from lofter.device_new
         |    where dt='$date'
         |) bb on aa.mdaDeviceUdid=bb.deviceUdid and aa.dt=bb.dt and (aa.occurtime - bb.firstaccesstime)<=180000
         |left join (
         |    select dt,deviceUdid,firstaccesstime
         |    from lofter.device_return
         |    where dt='$date' and period=30
         |)cc on aa.mdaDeviceUdid=cc.deviceUdid and aa.dt=cc.dt and (aa.occurtime - cc.firstaccesstime)<=180000
         |group by aa.activityId,activityName
       """.stripMargin

    spark.sql(sql_device_new_callback)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_activity_device_di")
  }

}
