package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.{Row, SparkSession}

import java.sql.Connection

object UserGroupDeviceExportJob {
  val BATCH_SIZE = 100

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    import com.netease.wm.util.Sql._

    val deviceUserMapping =
      s"""
         |select deviceId, deviceType, max(userId) as userId
         |from (
         |  select oaid as deviceId, 'oaid' as deviceType, userId,
         |         row_number() over (partition by oaid order by lastTime desc) as rnk
         |  from lofter.dwd_device_mapping_detail_di where dt = '$date' and length(oaid) > 0
         |
         |  union all
         |
         |  select androidId as deviceId, 'android_id' as deviceType, userId,
         |         row_number() over (partition by androidId order by lastTime desc) as rnk
         |  from lofter.dwd_device_mapping_detail_di where dt = '$date' and length(androidId) > 0
         |
         |  union all
         |
         |  select imei as deviceId, 'imei' as deviceType, userId,
         |         row_number() over (partition by imei order by lastTime desc) as rnk
         |  from lofter.dwd_device_mapping_detail_di where dt = '$date' and length(imei) > 0
         |
         |  union all
         |
         |  select idfa as deviceId, 'idfa' as deviceType, userId,
         |         row_number() over (partition by idfa order by lastTime desc) as rnk
         |  from lofter.dwd_device_mapping_detail_di where dt = '$date' and length(idfa) > 0
         |
         |  union all
         |
         |  select idfv as deviceId, 'idfv' as deviceType, userId,
         |         row_number() over (partition by idfv order by lastTime desc) as rnk
         |  from lofter.dwd_device_mapping_detail_di where dt = '$date' and length(idfv) > 0
         |) t
         |where userId > 0 and rnk = 1
         |group by deviceId, deviceType
         |""".stripMargin

    spark.sql(deviceUserMapping)
      .repartition(10)
      .foreachPartition { xs: Iterator[Row] =>
        import com.netease.wm.group.platform.common.SparkSqlImplicits._
        import com.netease.wm.util.Sql._
        import databases.getDDBConn
        implicit val conn: Connection = getDDBConn

        try {
          xs.toSeq.grouped(BATCH_SIZE).foreach { rows =>
            val batch = rows.map(rowParam _)
            sql"insert into Ad_DeviceUserId(id, deviceId, deviceType, userId, createTime) values(seq, ${"deviceId"}, ${"deviceType"}, ${"userId"}, now()) on duplicate key update deviceType = ${"deviceType"}, userId = ${"userId"} ".batchUpdate(batch)
          }
        } finally {
          conn.close()
        }
      }
  }
}
