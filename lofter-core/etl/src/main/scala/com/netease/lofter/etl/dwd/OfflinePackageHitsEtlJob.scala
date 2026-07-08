package com.netease.lofter.etl.dwd

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object OfflinePackageHitsEtlJob {
  val OUTPUT_PREFIX = "/user/da_lofter/warehouse/OfflinePackage"

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")

    spark.sql("create temporary function parse_array as 'com.netease.wm.udf.ParseArrayJson'")
    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_web_view_hits =
      s"""
         |select occurTime,kafkaTime,userId,deviceUdid,deviceOs,deviceOsVersion,appVersion,ip,deviceNetwork,deviceCarrier,country, province, city,
         |      sessionId,mpId,mpVersion,resUrl,hit,cast(matchTs as bigint) as matchTs,
         |      regexp_extract(sessionId,'(\\\\w+)-(.*)',2) as pageUrl,collectScene
         |from
         |(select occurTime,kafkaTime,userId,deviceUdid,deviceOs,deviceOsVersion,appVersion,ip,deviceNetwork,deviceCarrier,
         |        inline(Array(resolve_ip(ip))) as (country, province, city),
         |        json_tuple(col,'sessionId','mpId','mpVersion','url','hit','matchTs','collectScene') as (sessionId,mpId,mpVersion,resUrl,hit,matchTs,collectScene)
         | from
         |    (select occurTime,kafkaTime,userId,deviceUdid,deviceOs,deviceOsVersion,appVersion,ip,
         |            deviceNetwork,deviceCarrier,inline(Array(resolve_ip(ip))) as (country, province, city),
         |            parse_array(params['ext']) as str from lofter.ods_mda_app_partition_di
         |    where dt='$date' and actionType = 'system' and eventId in('t1-2') ) a
         |lateral view explode(a.str) as col) b
         |""".stripMargin

    val outPath = s"$OUTPUT_PREFIX/WebViewHits/dt=$date"
    spark.sql(sql_web_view_hits).repartition(10).write
      .mode(SaveMode.Overwrite).parquet(outPath)

    spark.sql(s"alter table lofter.dwd_evt_webview_hits_di add if not exists partition(dt='$date') location '$outPath' ")

    spark.close()
  }

}
