package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object BlogGiftStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Photo Frame Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_blog_gift_detail =
      s"""
         |select a.day,a.blogId,b.blogNickName,b.blogName,
         |      a.giftIdName,a.isPay,a.type,
         |      case when c.userId is null then 0 else 1 end  as giftFlag,
         |      round(a.coin,3) as coin,a.presentNum
         |from
         |(select
         |     authorBlogId as blogId,
         |     from_unixtime(cast(createTime AS BIGINT), 'yyyy-MM-dd') as day,
         |     '打赏' as type,
         |     '打赏' as giftIdName,
         |     1 as isPay,
         |     sum(rewardAmount) as coin ,
         |     count(1) as presentNum
         |from lofter_db_dump.ods_db_trade_reward_order_nd
         |where status=10 and
         |   from_unixtime(cast(createTime AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |group by 1,2,3,4,5
         |
         |union all
         |
         |select b.blogId,b.day,
         |      '礼物' as type,
         |      a.name as giftIdName,
         |      a.isPay,
         |      case when  a.isPay=0 then b.presentNum*a.price else b.coin*0.1 end as coin ,
         |      b.presentNum
         |from
         |    (select id as giftId,name, is_pay as isPay, coin, price from lofter.dim_gift) a
         |    join
         |    (select blogId,from_unixtime(cast(createTime / 1000 AS BIGINT), 'yyyy-MM-dd') as day,giftId,giftType,
         |          sum(count) as presentNum ,sum(coin) as coin
         |    from
         |    lofter_db_dump.ods_db_trade_gift_present_record_nd
         |    where  from_unixtime(cast(createTime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |    group by 1,2,3,4
         |    ) b
         |    on a.giftId=b.giftId
         |) a
         |
         |join
         |
         |(select blogId,blogNickName,blogName from  lofter_db_dump.ods_db_blog_info_nd) b
         |on a.blogId=b.blogId
         |left join
         |
         |(select userId from lofter_db_dump.ods_db_trade_gift_account_nd
         |where status=2
         |group by  userId )c
         |on b.blogId=c.userId
         |""".stripMargin

    spark.sql(sql_blog_gift_detail).cache().createOrReplaceTempView("blogGiftDetail")

    val sql_summary_result =
      s"""
         |select '$date' as dt,b.giftFlag,a.*
         |from
         |(select 1 as period,
         |      blogId,blogNickName,blogName,
         |      giftIdName,isPay,type,
         |      sum(coin) as coin,sum(presentNum) as presentNum
         |from blogGiftDetail
         |where day='$date'
         |group by 1,2,3,4,5,6,7
         |
         |union
         |select 7 as period,
         |      blogId,blogNickName,blogName,
         |      giftIdName,isPay,type,
         |      sum(coin) as coin,sum(presentNum) as presentNum
         |from blogGiftDetail
         |where day between '$oneWeekAgo' and '$date'
         |group by 1,2,3,4,5,6,7
         |
         |union
         |select 30 as period,
         |      blogId,blogNickName,blogName,
         |      giftIdName,isPay,type,
         |      sum(coin) as coin,sum(presentNum) as presentNum
         |from blogGiftDetail
         |group by 1,2,3,4,5,6,7
         |) a
         |join
         |(select distinct blogId,first_value(giftFlag) over(partition by blogId order by day desc) as giftFlag from blogGiftDetail) b
         |on a.blogId=b.blogId
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_summary_result)
      .select("blogId","blogNickName", "blogName","giftFlag","period","giftIdName", "isPay", "type", "coin", "presentNum", "dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_blog_gift_detail_stat_di")

    spark.close()
  }

}
