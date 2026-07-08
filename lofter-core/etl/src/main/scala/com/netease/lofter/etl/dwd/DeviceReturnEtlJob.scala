package com.netease.lofter.etl.dwd

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
 * compute return devices for period 7, 15, 30
 */
object DeviceReturnEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val endDate = DateTime.parse(date)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    def createWideParquetTable(prefix: String, tableName: String, period: Int = 1): DataFrame = {
      val dates = (0 until period) map (n => endDate.minusDays(n).toString("yyyy-MM-dd"))
      val paths = dates map { d => s"$prefix/dt=$d" }
      val df = spark.read.option("basePath", prefix).parquet(paths: _*)
      df.createOrReplaceTempView(tableName)
      df
    }

    createWideParquetTable("/user/da_lofter/warehouse/device_active", "device_active", 31)
    createWideParquetTable("/user/da_lofter/warehouse/device_new", "device_new", 1)

    def returnDeviceForPeriod(period: Int): Unit = {
      val periodDate = endDate.minusDays(period).toString("yyyy-MM-dd")
      val returnDeviceSql =
       s"""
          |select a.deviceUdid, deviceModel, deviceOs, firstAccessTime,
          |       if(size(userIds) = 0, null, userIds) as userIds,
          |       if(size(appChannels) = 0, null, appChannels) as appChannels,
          |       if(size(appVersions) = 0, null, appVersions) as appVersions,
          |       a.returnOccurTime
          |from (select * from device_active where dt='$date') a
          |    left join (select deviceUdid from device_new where dt='$date') b on a.deviceUdid = b.deviceUdid
          |    left join (select deviceUdid from device_active where dt >= '$periodDate' and dt < '$date' group by deviceUdid) m on a.deviceUdid = m.deviceUdid
          |where b.deviceUdid is null and m.deviceUdid is null
          |""".stripMargin

      spark.sql(returnDeviceSql)
        .repartition(1)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(period))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter.device_return")
    }

    for(period <- Seq(7, 15, 30)) {
      returnDeviceForPeriod(period)
    }

    spark.close()
  }
}
