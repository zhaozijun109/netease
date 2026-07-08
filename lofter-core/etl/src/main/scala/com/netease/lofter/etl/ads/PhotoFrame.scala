package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object PhotoFrame {
  val START_DATE = "2021-10-29"

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Photo Frame Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_pv_uv =
      s"""
         |select '$date' as day,
         |       count(distinct a.userid) as hisUv,sum(pv) as hisPv,
         |       count(distinct case when a.dt='$date' then a.userid else 0 end) as dailyUv,
         |       sum(case when a.dt='$date' then pv else 0 end) as dailyPv
         |from (
         |  select a.*
         |  from (
         |     select userid,dt,sum(1) as pv
         |     from lofter.dwd_evt_avatar_box_access_di
         |     where dt between '$START_DATE' and '$date'
         |     group by userid,dt
         |  ) a
         |  join (
         |    select id as userid  from lofter.dim_user
         |  ) b on a.userid=b.userid
         |) a
         |group by day
         |""".stripMargin

    val sql_trade =
      s"""
         |select '$date' as dt,nvl(type,'all') as type,
         |       count(distinct a.userid) as hisUv,
         |       sum(a.sales) as hisSales,sum(a.orderNum) as hisOrderNum,sum(a.coins) as hisCoins,
         |       count(distinct case when day='$date' then userId else null end) as dailyUv,
         |       sum(case when day='$date' then sales else 0 end) as dailySales,
         |       sum(case when day='$date' then orderNum else 0 end) as dailyOrderNum,
         |       sum(case when day='$date' then coins else 0 end) as dailyCoins
         |from
         |(select a.day, 'free' as type,a.userid,count(a.userid)  as sales,count(a.userid) as orderNum, 0 as coins
         |from
         |    (select avatarboxid,userid,from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as day,createtime
         |    from
         |    lofter_db_dump.ods_db_avatar_box_user_nd
         |    where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$START_DATE' and '$date' and status in(1,-1)
         |    ) a
         |    join
         |   (select id  from lofter_db_dump.ods_db_avatar_box_item_nd
         |    where coin=0
         |   ) b
         |     on a.avatarboxid=b.id
         |group by a.day, a.userid
         | union
         | select  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as day, 'charge' as type,
         |        userid,sum(count) as sales, count(distinct id) as orderNum,sum(coin*count) as coins
         | from lofter_db_dump.ods_db_avatar_box_order_nd
         | where status=1 and
         |from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$START_DATE' and '$date'
         |group by from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') ,userid) a
         |group by dt,type
         |grouping sets((dt,type),(dt))
         |""".stripMargin

    spark.sql(sql_pv_uv).cache().createOrReplaceTempView("photoPvUv")
    spark.sql(sql_trade).cache().createOrReplaceTempView("tradeInfo")

    val sql_summary_result =
      s"""
         |select a.*,type,orderUv,sales,orderNum,coins
         |from
         |(select day,'daily' as statType,dailyUv as pageUv,dailyPv as pagePv from photoPvUv) a
         |left join
         |(select dt,'daily' as statType,type,
         |dailyUv as orderUv,dailySales as sales,dailyOrderNum as orderNum,dailyCoins as coins
         |from tradeInfo) b
         |on a.day=b.dt
         |
         |union
         |
         |select a.*,type,orderUv,sales,orderNum,coins
         |from
         |(select day,'history' as statType,hisUv as pageUv,hisPv as pagePv from photoPvUv) a
         |left join
         |(select dt,'history' as statType,type,
         |hisUv as orderUv,hisSales as sales,hisOrderNum as orderNum,hisCoins as coins
         |from tradeInfo) b
         |on a.day=b.dt
         |""".stripMargin

    spark.sql(sql_summary_result)
      .select("statType", "pageUv", "pagePv", "type", "orderUv", "sales", "orderNum", "coins", "day")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_photo_frame_summary_stat_di")

    spark.close()
  }

}
