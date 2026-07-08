package com.netease.lofter.etl.dwd

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object OfflinePackageIndexEtlJob {
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

    val sql_web_view_index =
      s"""
         |select occurTime,kafkaTime,userId,deviceUdid,deviceOs,deviceOsVersion,appVersion,ip,deviceNetwork,deviceCarrier,country, province, city,
         |       sessionId,webSessionId,isFirst,isMp,mpId,mpVersion,userAgent,
         |       cast(createEndTime as bigint) as createEndTime,cast(createStartTime as bigint) as createStartTime,
         |       cast(loadMpResEndTime as bigint) as loadMpResEndTime,cast(loadMpResStartTime as bigint) as loadMpResStartTime,
         |       regexp_extract(sessionId,'(\\\\w+)-(.*)',2) as pageUrl,cast((createEndTime - createStartTime) as bigint) as webviewCreateTime,
         |       cast((loadMpResEndTime-loadMpResStartTime) as bigint) as webviewLoadMpResTime,collectScene,
         |       noteScene,
         |       cast(presetDataReadyTime as bigint) as presetDataReadyTime,
         |       cast(dataReturnTime as bigint) as dataReturnTime,
         |       cast(completeTime as bigint) as completeTime
         |from
         |(select occurTime,kafkaTime,userId,deviceUdid,deviceOs,deviceOsVersion,appVersion,ip,deviceNetwork,deviceCarrier,
         |        inline(Array(resolve_ip(ip))) as (country, province, city),
         |        json_tuple(params['ext'],'sessionId','webSessionId','isFirst','isMp','mpId','mpVersion','webViewType','createEndTime','createStartTime','loadMpResEndTime','loadMpResStartTime','collectScene','noteScene','presetDataReadyTime','dataReturnTime','completeTime') as
         |        (sessionId,webSessionId,isFirst,isMp,mpId,mpVersion,userAgent,createEndTime,createStartTime,loadMpResEndTime,loadMpResStartTime,collectScene,noteScene,presetDataReadyTime,dataReturnTime,completeTime)
         |        from lofter.ods_mda_app_partition_di
         |        where dt='$date' and actionType = 'system' and eventId in('t1-1')
         |) a
         |""".stripMargin

    val outPath = s"$OUTPUT_PREFIX/WebViewIndex/dt=$date"
    spark.sql(sql_web_view_index).repartition(10).write
      .mode(SaveMode.Overwrite).parquet(outPath)

    spark.sql(s"alter table lofter.dwd_evt_webview_index_di add if not exists partition(dt='$date') location '$outPath'")

    spark.close()
  }

}
