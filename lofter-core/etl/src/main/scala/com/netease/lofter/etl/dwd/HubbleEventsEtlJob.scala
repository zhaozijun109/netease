package com.netease.lofter.etl.dwd

import com.github.nscala_time.time.Imports.DateTime
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{LongType, StringType, StructType}
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.{expr, lit}

object HubbleEventsEtlJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    spark.sqlContext.udf.register("to_appid", (s: String) => {
      s match {
        case "MA-A4FE-A88932E7A98F" => "android"
        case "MA-9A4C-437494F370B3" => "iphone"
        case "MA-88DF-03AA6989372E" => "ipad"
        case _ => ""
      }
    })

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val insertSql =
      s"""
         |     insert overwrite table lofter.hubble_events partition(dt='$dt')
         |       select userId, deviceUdid, null as deviceImei, null as deviceAndroidid, null as deviceAdid, ip, country, region, city, deviceModel, deviceOs, null as deviceNetwork, null as deviceCarrier, 'web' as pt_appid, occurTime, null as oaid
         |       from lofter.ods_mda_web_di
         |       where dt = '$dt'
         |     union all
         |       select userId, deviceUdid, null as deviceImei, null as deviceAndroidid, null as deviceAdid, ip, country, region, city, deviceModel, deviceOs, null as deviceNetwork, null as deviceCarrier, 'wap' as pt_appid, occurTime, null as oaid
         |       from lofter.ods_mda_wap_di
         |       where dt = '$dt'
         |     union all
         |       select userId, deviceUdid, deviceImei, deviceAndroidid, deviceAdid, ip, null as country, null as region, city, deviceModel, deviceOs, deviceNetwork, deviceCarrier, to_appid(appKey) as pt_appid, occurTime, oaid
         |       from lofter.ods_mda_app_raw_di
         |       where dt = '$dt'
         |""".stripMargin

    spark.sql(insertSql)
    spark.close()
  }
}
