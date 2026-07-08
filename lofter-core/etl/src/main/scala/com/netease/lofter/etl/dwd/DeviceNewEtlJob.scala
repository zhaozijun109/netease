package com.netease.lofter.etl.dwd

import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime

object DeviceNewEtlJob {
  val DEVICE_ALL_PATH_PREFIX = "/user/da_lofter/warehouse/device_all"
  val OUTPUT_PATH_PREFIX = "/user/da_lofter/warehouse/device_new"

  def main(args: Array[String]): Unit = {
    val day = args(0)
    val yesterday = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")
    val spark = SparkSession.builder()
      .appName("device new etl job")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    spark.read.parquet(s"$DEVICE_ALL_PATH_PREFIX/dt=$yesterday").createOrReplaceTempView("spark_device_all")

    etlJob(day, spark)
  }

  def etlJob(day: String, spark: SparkSession): Unit = {
    val maxDeviceId = spark.sql("select max(deviceId) from spark_device_all").head().getLong(0)

    val sql_device =
   s"""
      |select appKey, deviceOs, appChannel, appVersion, deviceUdid, userId, deviceModel, customUdid, deviceAdid, deviceIMEI,
      |       min(occurTime) occurTime, max(occurTime) lastOccurTime
      |from lofter.ods_mda_app_partition_di
      |where dt = '$day' and length(deviceUdid) > 0 and eventId != 'rd-2'
      |group by appKey, deviceOs, appChannel, appVersion, deviceUdid, userId, deviceModel, customUdid, deviceAdid, deviceIMEI
      |""".stripMargin

    spark.sql(sql_device).cache().createOrReplaceTempView("spark_device")

    val sql_possible_new =
      s"""
         |select b.deviceUdid,appKey,deviceModel,deviceOs,firstAccessTime,
         |nvl(d.userId, b.userId) as userId,
         |nvl(d.customUdid, b.customUdid) as customUdid,
         |nvl(d.deviceAdid, b.deviceAdid) as idfa,
         |nvl(d.deviceIMEI, b.deviceIMEI) as imei,
         |appChannel,appVersion,
         |e.userId as finalUserId
         |from
         |(select deviceUdid,appKey,deviceModel,deviceOs,occurTime as firstAccessTime,userId,appChannel,appVersion,customUdid, deviceAdid, deviceIMEI from
         |  (select deviceUdid,appKey,deviceModel,deviceOs,occurTime,userId,appChannel,appVersion, customUdid, deviceAdid, deviceIMEI, row_number() over (partition by deviceUdid order by occurTime) as rk
         |   from spark_device ) a
         |   where rk=1)b
         | left join
         |(select deviceUdid, userId, customUdid, deviceAdid, deviceIMEI from
         |   (select deviceUdid, userId, customUdid, deviceAdid, deviceIMEI, row_number() over (partition by deviceUdid order by occurTime) as rk2 from spark_device where userId is not null) c
         |    where rk2 =1) d on b.deviceUdid=d.deviceUdid
         | left join
         | (select deviceUdid, userId from
         |   (select deviceUdid, userId,row_number() over (partition by deviceUdid order by lastOccurTime desc) as rk3 from spark_device where userId is not null) c
         |    where rk3 =1) e on b.deviceUdid=e.deviceUdid
       """.stripMargin

    spark.sql(sql_possible_new).createOrReplaceTempView("new_possible")

    val sql_new =
      s"""
         |select a.* from
         | (select * from new_possible) a
         | left join spark_device_all b on a.deviceUdid=b.deviceUdid
         | where b.deviceUdid is NULL
       """.stripMargin

    spark.sql(sql_new).cache().createOrReplaceTempView("spark_device_new")

    val sql_all =
      s"""
         |select deviceUdid,deviceModel,deviceOs,firstAccessTime,userId,appChannel,appVersion,deviceId from spark_device_all
         |union
         |select deviceUdid,deviceModel,deviceOs,firstAccessTime,userId,appChannel,appVersion,
         |    ($maxDeviceId + row_number() over(order by firstAccessTime,deviceUdid)) as deviceId
         |from spark_device_new
       """.stripMargin

    val newOutPath = s"$OUTPUT_PATH_PREFIX/dt=$day"
    val allOutPath = s"$DEVICE_ALL_PATH_PREFIX/dt=$day"

    spark.sql(sql_new).repartition(1)
      .write.mode("overwrite")
      .parquet(newOutPath)

    spark.sql(sql_all).repartition(5)
      .write.mode("overwrite")
      .parquet(allOutPath)

    spark.sql(s"alter table lofter.device_new add if not exists partition (dt='$day') location '$newOutPath'")
    spark.sql(s"alter table lofter.dwd_par_device_all_dd add if not exists partition (dt='$day') location '$allOutPath'")
    spark.close()
  }

}
