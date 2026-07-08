package com.netease.lofter.etl.dwd

import org.apache.spark.sql.SparkSession

object DeviceActiveEtlJob {
  val OUTPUT_PATH_PREFIX = "/user/da_lofter/warehouse/device_active"

  def main(args: Array[String]): Unit = {
    val day = args(0)
    val spark = SparkSession.builder()
      .appName("device active etl job")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    etlJob(day, spark)
  }

  def etlJob(day: String, spark: SparkSession): Unit = {
    val sql_device =
      s"""
        |select appKey, deviceOs, appChannel, appVersion, deviceUdid, userId, deviceModel,ip,
        |       min(occurTime) occurTime,
        |       min(if(from_unixtime(cast(occurTime/1000 as bigint), 'yyyy-MM-dd') = '$day', occurTime, null)) as returnOccurTime,
        |       max(occurTime) as maxOccurTime
        |from lofter.ods_mda_app_partition_di
        |where dt = '$day' and length(deviceUdid) > 0 and eventId != 'rd-2'
        |group by appKey, deviceOs, appChannel, appVersion, deviceUdid, userId, deviceModel, ip
        |""".stripMargin

    spark.sql(sql_device).cache().createOrReplaceTempView("spark_device")

    val sql_device_active =
      s"""
         |select a.deviceUdid,appKey,deviceModel,deviceOs,firstAccessTime,firstAccessIp,userIds, appChannels,appVersions, returnOccurTime, appVersionChannel
         |from (
         |  select deviceUdid,
         |         filter(transform(sort_array(collect_set(struct(rank, userId)), true), x -> x.userId), x -> x > 0) as userIds,
         |         collect_set(appChannel) as appChannels,
         |         collect_set(appVersion) as appVersions
         |  from (
         |    select x.*, rank() over (partition by deviceUdid order by if(u.isanonymous = 0, u.createTime, 0) desc) rank
         |    from spark_device x
         |    left join lofter.dim_user u on x.userId = u.id
         |  ) t
         |  group by deviceUdid
         |) a
         |left join (
         | select deviceUdid,appKey,deviceModel,deviceOs,occurTime as firstAccessTime, ip as firstAccessIp, returnOccurTime
         | from (
         |    select deviceUdid,appKey,deviceModel,deviceOs,occurTime,ip,
         |           row_number() over (partition by deviceUdid order by occurTime) as rk,
         |           min(returnOccurTime) over (partition by deviceUdid) as returnOccurTime
         |    from spark_device
         | ) b
         | where rk = 1
         |) c on a.deviceUdid=c.deviceUdid
         |left join (
         |    select deviceUdid, map_from_entries(collect_list(struct(appVersion, appChannel))) as appVersionChannel
         |    from (
         |        select deviceUdid, appVersion, appChannel
         |        from spark_device
         |        where appVersion is not null
         |        group by deviceUdid, appVersion, appChannel
         |    ) t
         |    group by deviceUdid
         |) d on a.deviceUdid = d.deviceUdid
       """.stripMargin

    val outPath = s"$OUTPUT_PATH_PREFIX/dt=$day"
    spark.sql(sql_device_active).repartition(1)
      .write.mode("overwrite")
      .parquet(outPath)

    spark.sql(s"alter table lofter.device_active add if not exists partition (dt='$day') location '$outPath'")
    spark.close()
  }

}
