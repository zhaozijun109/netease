package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object LofterClientAdData {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Ad Value")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.sql.hive.convertMetastoreParquet", true)
      .config("spark.sql.parquet.filterPushdown", true)
      .config("spark.sql.parquet.mergeSchema", false)
      .config("parquet.filter.statistics.enabled", true)
      .config("parquet.filter.dictionary.enabled", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql =
      s"""
         |select deviceOs, appVersion, eventId, params['adId'] adId, count(1) pv, count(distinct deviceUdid) uv
         |from lofter.ods_mda_app_partition_di
         |where eventId like 'ad-%' and params['adId'] is not null and
         |      deviceOs is not null and appVersion is not null and eventId is not null and
         |      dt = '$day' and actionType = 'advertisement'
         |group by deviceOs, appVersion, eventId, params['adId']
      """.stripMargin

    spark.sql(sql).withColumn("dt", lit(day))
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ad_stats_by_events_di")

    spark.stop()
  }
}
