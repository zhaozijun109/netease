package com.netease.vc.data.dwd

import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime

object DeviceAll {

  def main(args: Array[String]): Unit = {
    val day = args(0)
    val dayAgo = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")

    val spark = SparkSession.builder()
      .appName("device all etl job")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    spark.sql(s"select * from vc.dwd_device_all_dd where dt = '$dayAgo' ").createOrReplaceTempView("spark_device_all")
    val maxDeviceId = spark.sql(s"select nvl(max(deviceId),0) as max_device_id from spark_device_all")
      .collect()
      .headOption
      .map(_.getLong(0))
      .getOrElse(0L)

    val sql_device =
      s"""
         |select appKey, deviceOs, appChannel, appVersion, deviceUdid, user_id as userId,
         |       deviceModel, customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid,
         |       min(occurTime) occurTime, max(occurTime) lastOccurTime
         |from vc.ods_mda_app_di
         |where dt = '$day' and length(deviceUdid) > 0 and eventId != 'rd-2'
         |group by appKey, deviceOs, appChannel, appVersion, deviceUdid, user_id,
         |         deviceModel, customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid
         |""".stripMargin

    spark.sql(sql_device).cache().createOrReplaceTempView("spark_device")

    val sql_possible_new =
      s"""
         |select b.deviceUdid,appKey,deviceModel,deviceOs,firstAccessTime,
         |       nvl(d.userId, b.userId) as userId,
         |       nvl(d.customUdid, b.customUdid) as customUdid,
         |       nvl(d.deviceAdid, b.deviceAdid) as idfa,
         |       nvl(d.deviceIMEI, b.deviceIMEI) as imei,
         |       nvl(d.deviceandroidid, b.deviceandroidid) as androidid,
         |       nvl(d.oaid, b.oaid) as oaid,
         |       appChannel,appVersion,
         |       e.userId as finalUserId
         |from (
         |    select deviceUdid,appKey,deviceModel,deviceOs,occurTime as firstAccessTime,userId,appChannel,appVersion,
         |           customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid
         |    from (
         |        select deviceUdid,appKey,deviceModel,deviceOs,occurTime,userId,appChannel,appVersion,
         |               customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid,
         |               row_number() over (partition by deviceUdid order by occurTime) as rk
         |        from spark_device
         |    ) a
         |    where rk=1
         |) b
         |left join (
         |    select deviceUdid, userId, customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid
         |    from (
         |        select deviceUdid, userId, customUdid, deviceAdid, deviceIMEI, deviceAndroidid, oaid,
         |               row_number() over (partition by deviceUdid order by occurTime) as rk2
         |        from spark_device
         |        where userId is not null
         |    ) c
         |    where rk2 =1
         |) d on b.deviceUdid=d.deviceUdid
         |left join (
         |    select deviceUdid, userId
         |    from (
         |        select deviceUdid, userId,
         |               row_number() over (partition by deviceUdid order by lastOccurTime desc) as rk3
         |        from spark_device where userId is not null
         |    ) c
         |    where rk3 =1
         |) e on b.deviceUdid=e.deviceUdid
         |
       """.stripMargin

    spark.sql(sql_possible_new).createOrReplaceTempView("new_possible")

    val sql_new =
      s"""
         |select a.*
         |from (
         |    select * from new_possible
         |) a
         |left join spark_device_all b on a.deviceUdid=b.deviceUdid
         |where b.deviceUdid is NULL
       """.stripMargin

    spark.sql(sql_new).cache().createOrReplaceTempView("spark_device_new")

    val sql_all =
      s"""
         |insert overwrite table vc.dwd_device_all_dd partition(dt = '$day')
         |select deviceudid,devicemodel,deviceos,firstaccesstime,userid,appchannel,appversion,customudid,imei,idfa,oaid,androidid,finaluserid,new_date,deviceid
         |from (
         |    select deviceUdid,deviceModel,deviceOs,firstAccessTime,userId,appChannel,appVersion,deviceId,
         |           customudid,imei,idfa,oaid,androidid, finalUserid, new_date
         |    from spark_device_all
         |  union all
         |    select deviceUdid,deviceModel,deviceOs,firstAccessTime,userId,appChannel,appVersion,
         |           ($maxDeviceId + row_number() over(order by firstAccessTime,deviceUdid)) as deviceId,
         |           customudid,imei,idfa,oaid,androidid,finalUserid,
         |           '$day' as new_date
         |    from spark_device_new
         |) t
       """.stripMargin

    spark.sql(sql_all)
    spark.close()
  }


}
