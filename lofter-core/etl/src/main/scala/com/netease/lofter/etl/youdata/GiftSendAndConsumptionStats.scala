package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object GiftSendAndConsumptionStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Gift Send And Consumption Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val statTime = DateTime.parse(date).plusDays(1).getMillis

    val sql_gift_free =
      s"""
         |select a.*,b.type,b.rule,b.grantNum,b.grantUv
         |from
         |(select id as giftId,name, is_pay as isPay from lofter.dim_gift)  a
         |join
         |(select giftId,type,rule,sum(count) as grantNum ,count(distinct userid) as grantUv
         |from
         |lofter_db_dump.ods_db_trade_user_free_gift_nd
         |where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by giftId,type,rule ) b
         |on a.giftId=b.giftId
         |""".stripMargin

    val sql_gift_detail =
      s"""
         |select a.giftId,a.name,a.isPay,a.coin as perCoin,
         |b.grantNum,b.grantUv,b.grantCoin,
         |c.presentNum,c.presentUv,c.presentCoin,c.acquireNum,c.acquireUv,
         |d.pastNum ,d.pastUv
         |from
         |(select id as giftId,name,is_pay as isPay, coin from lofter.dim_gift) a
         |
         |join
         |(
         |select giftId,sum(count) as grantNum ,count(distinct userid) as grantUv,0 as grantCoin
         |from
         |lofter_db_dump.ods_db_trade_user_free_gift_nd
         |where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by giftId
         |
         |union
         |select giftId ,sum(count) as grantNum,count(distinct userid) as grantUv, sum(coin) as grantCoin
         |from
         |lofter_db_dump.ods_db_trade_gift_order_nd
         |where status=1 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by giftId
         |) b
         |on a.giftId=b.giftId
         |
         |join
         |(
         |select giftId,giftType,count(distinct sender) as presentUv,sum(count) as presentNum ,sum(coin) as presentCoin,
         |       count(distinct postId) as acquireNum,count(distinct blogId) as acquireUv
         |from
         |lofter_db_dump.ods_db_trade_gift_present_record_nd
         |where  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by giftId,giftType
         |) c
         |on a.giftId=c.giftId
         |
         |left join
         |(select giftId,sum(balance) as pastNum ,count(distinct userid) as pastUv
         |from
         |lofter_db_dump.ods_db_trade_user_free_gift_nd
         |where $statTime>endtime
         |group by giftId) d
         |on a.giftId=d.giftId
         |""".stripMargin

    val sql_user_gift =
      s"""
         |select a.giftId,a.name,a.isPay,a.coin as perCoin,
         |b.blogId,b.blogNickname,b.blogName,b.presentNum,b.presentUv,b.coin,b.acquireNum
         |from 
         |(select id as giftId,name, is_pay as isPay,coin from lofter.dim_gift) a
         |
         |join 
         |
         |(select a.*,b.blogNickname,b.blogName
         |from 
         |(select giftId,giftType,blogId,count(distinct sender) as presentUv,
         |        sum(count) as presentNum ,sum(coin) as coin,count(distinct postId) as acquireNum
         |from 
         |lofter_db_dump.ods_db_trade_gift_present_record_nd  
         |where  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |group by giftId,giftType,blogId) a
         |
         |join 
         |(select blogId,blogNickname,blogName from  lofter_db_dump.ods_db_blog_info_nd) b
         |on a.blogId=b.blogId
         |) b 
         |on a.giftId=b.giftId
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_gift_free)
      .withColumn("dt", lit(date))
      .select("giftId","name","isPay","type","rule","grantNum","grantUv","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_free_gift_rule_stats_di")

    spark.sql(sql_gift_detail)
      .withColumn("dt", lit(date))
      .select("giftId","name","isPay","perCoin","grantNum","grantUv","grantCoin","presentNum","presentUv",
              "presentCoin","acquireNum","acquireUv","pastNum","pastUv","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_gift_detail_stats_di")

    spark.sql(sql_user_gift)
      .withColumn("dt", lit(date))
      .select("giftId","name","isPay","perCoin","blogId","blogNickname","blogName",
              "presentNum","presentUv","coin","acquireNum","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_gift_send_stats_di")

    spark.close()

  }

}
