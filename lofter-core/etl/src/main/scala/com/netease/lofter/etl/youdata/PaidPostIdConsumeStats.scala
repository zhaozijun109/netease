package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PaidPostIdConsumeStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val weekAgo = DateTime.parse(date).minusDays(6).withTimeAtStartOfDay().toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(date).minusDays(29).withTimeAtStartOfDay().toString("yyyy-MM-dd")
    val dayOfYear = DateTime.parse(date).getYear.toString
    val quarterStart = DateTime.parse(date).getMonthOfYear match {
      case x if x>=1 && x<=3 => dayOfYear + "-01-01"
      case x if x>=4 && x<=6 => dayOfYear + "-04-01"
      case x if x>=7 && x<=9 => dayOfYear + "-07-01"
      case x if x>=10 && x<=12 => dayOfYear + "-10-01"
    }

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    val sql_result =
     s"""
        |select 'day' period, platform_type,return_gift_type,
        |     sum(coin) as coin_num ,count(distinct postid) as postid_num,count(distinct sender) as sender_uv,
        |     sum(gift_num) as gift_num,count(distinct postid,sender) as post_sender_num,
        |     count(1) as num,
        |     count(distinct case when is_first_pay='首次付费' then sender else null end ) as new_sender_uv
        |from lofter.dwd_paid_post_detail_dd
        |where dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
        |group by platform_type,return_gift_type
        |with cube
        |
        |union
        |select 'week' period,platform_type,return_gift_type,
        |     sum(coin) as coin_num ,count(distinct postid) as postid_num,count(distinct sender) as sender_uv,
        |     sum(gift_num) as gift_num,count(distinct postid,sender) as post_sender_num,
        |     count(1) as num,
        |     count(distinct case when is_first_pay='首次付费' then sender else null end ) as new_sender_uv
        |from  lofter.dwd_paid_post_detail_dd
        |where dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$weekAgo' and '$date'
        |group by platform_type,return_gift_type
        |with cube
        |
        |union
        |select 'month' period,platform_type,return_gift_type,
        |     sum(coin) as coin_num ,count(distinct postid) as postid_num,count(distinct sender) as sender_uv,
        |     sum(gift_num) as gift_num,count(distinct postid,sender) as post_sender_num,
        |     count(1) as num,
        |     count(distinct case when is_first_pay='首次付费' then sender else null end ) as new_sender_uv
        |from  lofter.dwd_paid_post_detail_dd
        |where dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$monthAgo' and '$date'
        |group by platform_type,return_gift_type
        |with cube
        |
        |union
        |select 'quarter' period, platform_type,return_gift_type,
        |     sum(coin) as coin_num ,count(distinct postid) as postid_num,count(distinct sender) as sender_uv,
        |     sum(gift_num) as gift_num,count(distinct postid,sender) as post_sender_num,
        |     count(1) as num,
        |     count(distinct case when is_first_pay='首次付费' then sender else null end ) as new_sender_uv
        |from  lofter.dwd_paid_post_detail_dd
        |where dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$quarterStart' and '$date'
        |group by platform_type,return_gift_type
        |with cube
        |
        |union
        |select 'history' period,platform_type,return_gift_type,
        |     sum(coin) as coin_num ,count(distinct postid) as postid_num,count(distinct sender) as sender_uv,
        |     sum(gift_num) as gift_num,count(distinct postid,sender) as post_sender_num,
        |     count(1) as num,
        |     count(distinct case when is_first_pay='首次付费' then sender else null end ) as new_sender_uv
        |from  lofter.dwd_paid_post_detail_dd
        |where  dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') <= '$date'
        |group by platform_type,return_gift_type
        |with cube
        |""".stripMargin

    spark.sql(sql_result).repartition(5)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_paid_post_consumption_di")

    spark.close()
  }
}
