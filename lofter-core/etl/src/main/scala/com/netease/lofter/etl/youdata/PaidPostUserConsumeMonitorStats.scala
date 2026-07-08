package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PaidPostUserConsumeMonitorStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Paid Post High Consumer Monitor Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    // regist a udf function to spark sql
    spark.sqlContext.udf.register("payLevelClassification",
      (period: String, pay_type: String, money: Double) => payLevelClassification(period, pay_type, money))

    val sql_all =
      s"""
         |select nvl(pay_type,'all') as pay_type, '1' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, '7' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneWeekAgo' and '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, '30' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, 'history' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') <= '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, 'month' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between trunc('$date','MM') and '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, 'quarter' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between trunc('$date','quarter') and '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |
         |union all
         |select nvl(pay_type,'all') as pay_type, 'year' as period,userId,
         |	sum(money) as trade_money
         |from lofter.dwd_evt_post_paid_detail_dd
         |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between trunc('$date','YY') and '$date'
         |group by userId,pay_type
         |grouping sets((userId,pay_type),(userId))
         |""".stripMargin

    spark.sql(sql_all).createOrReplaceTempView("t1")

    val sql_result =
      s"""
         |select pay_type,period,payLevelClassification(period,pay_type,trade_money) as pay_level,
         |    count(distinct userId) as trade_uv,
         |    sum(trade_money) as trade_money
         |from t1
         |group by 1,2,3
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_paid_user_consume_di")

    spark.close()

  }

  def payLevelClassification(period: String, pay_type: String, money: Double): String = {
    period match {
      case "1" => {
        pay_type match {
          case "付费礼物" =>
            if (money > 0 && money < 1) "level_1"
            else if (money >=1 && money < 3) "level_2"
            else if (money >=3 && money < 7) "level_3"
            else if (money >= 7 && money < 15) "level_4"
            else if (money >= 15) "level_5"
            else "level_0"
          case  "粉丝会员" | "官方账号" | "博客订阅" | "书城会员" =>
            if (money > 0 && money < 10) "level_1"
            else if (money >=10 && money < 15) "level_2"
            else if (money >=15 && money < 25) "level_3"
            else if (money >= 25) "level_4"
            else "level_0"
          case "all" =>
            if (money > 0 && money < 1) "level_1"
            else if (money >=1 && money < 3) "level_2"
            else if (money >=3 && money < 7) "level_3"
            else if (money >= 7 && money < 15) "level_4"
            else if (money >= 15 && money < 25) "level_5"
            else if (money >= 25) "level_6"
            else "level_0"
        }
      }
      case "7" | "30" | "month" | "quarter" | "year" | "history" =>{
        pay_type match {
          case "付费礼物" =>
            if (money > 0 && money < 1) "level_1"
            else if (money >=1 && money < 3) "level_2"
            else if (money >=3 && money < 7) "level_3"
            else if (money >= 7 && money < 25) "level_4"
            else if (money >= 25 && money < 50) "level_5"
            else if (money >= 50) "level_6"
            else "level_0"
          case  "粉丝会员" | "官方账号" | "博客订阅" | "书城会员"  =>
            if (money > 0 && money < 10) "level_1"
            else if (money >=10 && money < 15) "level_2"
            else if (money >=15 && money < 25) "level_3"
            else if (money >= 25 && money <50) "level_4"
            else if (money >= 50) "level_5"
            else "level_0"
          case "all" =>
            if (money > 0 && money < 1) "level_1"
            else if (money >=1 && money < 3) "level_2"
            else if (money >=3 && money < 7) "level_3"
            else if (money >= 7 && money < 15) "level_4"
            else if (money >= 15 && money < 25) "level_5"
            else if (money >= 25 && money < 50) "level_6"
            else if (money >= 50) "level_7"
            else "level_0"
        }
      }
    }
  }

}
