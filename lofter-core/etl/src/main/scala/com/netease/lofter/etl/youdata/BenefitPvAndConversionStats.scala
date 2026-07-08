package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object BenefitPvAndConversionStats {

  val batchSize = 100

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Benefit Pv And Conversion Stats")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.read.parquet(s"/user/da_lofter/hive_db/lofter.db/dwd_evt_benefit_page_view_di/dt=$date").createOrReplaceTempView("fact_benefit_page_view")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order_product/$yesterday").createOrReplaceTempView("ddb_benefit_order_product")
    spark.read.parquet(s"/user/da_lofter/db_dump/BenefitShelfProductRecord/$yesterday").createOrReplaceTempView("ddb_benefit_shelf_product_record")

    benefitPvAndConversionStats(spark, date)
    spark.close()

  }

  def benefitPvAndConversionStats(spark: SparkSession, date: String): Unit = {

    val sql_benefit_uv =
      s"""
         |select "$date" as dt,count(distinct(allUserId)) as totalUv,count(distinct(adUserId)) as adUv,count(distinct(nonAdUserId)) as nonAdUv from
         |    ((select userId as allUserId,
         |            case when params['currentUrl'] like "%adTrace%" then userId else null end adUserId,
         |            case when params['currentUrl'] not like "%adTrace%" then userId else null end nonAdUserId
         |    from fact_benefit_page_view)  union all
         |    (select case when currenturl like "%benefit%" or currenturl like "%market%" then userId else null end as allUserId,
         |            case when currenturl like "%adTrace%" then userId else null end adUserId,
         |            case when (currenturl like "%benefit%" or currenturl like "%market%") and currenturl not like "%adTrace%" then userId else null end as  nonAdUserId
         |    from lofter.ods_mda_wap_di where dt='$date')) c  where allUserId is not null
       """.stripMargin

    val sql_benefit_payMoney =
      s"""
         |select "$date" as dt, sum(storeprice*productnum- newcouponpreferential) as totalMoney,
         |       sum(case when adtrace is not null then storeprice*productnum- newcouponpreferential end) adMoney,
         |       sum(case when adtrace is  null then storeprice*productnum- newcouponpreferential end) nonAdMoney
         |from ddb_benefit_order_product
         |      where status in(1,4,6,7) and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date'
       """.stripMargin

    spark.sql(sql_benefit_uv).createOrReplaceTempView("benefit_uv")
    spark.sql(sql_benefit_payMoney).createOrReplaceTempView("benefit_money")

    val sql_benefit_all =
      s"""
         |select a.dt,totalUv,adUv,nonAdUv,totalMoney,adMoney,nonAdMoney from
         |benefit_uv a left join benefit_money b on a.dt=b.dt
       """.stripMargin

    val sql_benefit_shelf =
      s"""
         |select a.userid,flag,b.productid  from
         |  (select userid,shelfid,sum(flag) flag
         |    from (select distinct userid,cast(params['shelfId'] as bigint)  shelfid,
         |              case when params['currentUrl'] like "%adTrace%" then 1 else 0 end flag
         |      from fact_benefit_page_view where params['currentUrl'] like "%shelf%" and eventid like "w2%" )t
         |   group by userid,shelfid )a
         |join  ddb_benefit_shelf_product_record b on a.shelfid=b.shelfid
       """.stripMargin

    val sql_benefit_product_detail =
      s"""
         |select distinct userid, productid from fact_benefit_page_view where  eventid like "w3%"
       """.stripMargin

    val sql_benefit_pay =
      s"""
         |select buyerid as userid,productid,sum(storeprice*productnum-newcouponpreferential) money
         |from ddb_benefit_order_product
         |where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date' and status in(1,3,4)
         |group by buyerid,productid
       """.stripMargin

    spark.sql(sql_benefit_shelf).createOrReplaceTempView("benefit_shelf")
    spark.sql(sql_benefit_product_detail).createOrReplaceTempView("benefit_product_detail")
    spark.sql(sql_benefit_pay).createOrReplaceTempView("benefit_pay")

    val sql_shelf_conv_type =
      s"""
         | select case when flag>=1 then "ad" else "nonAd" end as type,
         |        count(distinct a.userid) as shelfUv,count(distinct b.userid) as productUv, count(distinct c.userid) as payUv, sum(c.money) as payMoney
         | from benefit_shelf a
         | left join benefit_product_detail b on a.userid=b.userid and a.productid=b.productid
         | left join benefit_pay c on b.userid=c.userid and b.productid=c.productid
         | group by case when flag>=1 then "ad" else "nonAd" end
       """.stripMargin

    val sql_shelf_conv_all =
      s"""
         | select "all" as type,
         |        count(distinct a.userid) as shelfUv,count(distinct b.userid) as productUv, count(distinct c.userid) as payUv, sum(c.money) as payMoney
         | from benefit_shelf a
         | left join benefit_product_detail b on a.userid=b.userid and a.productid=b.productid
         | left join benefit_pay c on b.userid=c.userid and b.productid=c.productid
       """.stripMargin

    val sql_shelf_conv_wap =
      s"""
         |select "wap" as type,count(distinct c.userid) as shelfUv,count(distinct d.userid) as productUv, count(distinct e.userid) as payUv, sum(e.money) as payMoney
         |from
         |   ( select a.userid,b.productid from
         |       (select userid,case when currenturl like "%benefit%" then regexp_extract(currenturl, 'shelf/([0-9]+)', 1)
         |                 when currenturl like "%market%" then parse_url(currenturl,"QUERY","shelfId")  end shelfid
         |        from lofter.ods_mda_wap_di where dt='$date' and currenturl like "%shelf%") a
         |      join ddb_benefit_shelf_product_record b on a.shelfid=b.shelfid
         |   ) c left join
         |   (select userid,parse_url(currenturl,"QUERY","productId") as productid
         |    from lofter.ods_mda_wap_di
         |    where dt='$date' and currenturl like "%productId%") d
         |   on c.userid=d.userid and c.productid=d.productid
         |   left join benefit_pay e
         |   on d.userid=e.userid and d.productid=e.productid
       """.stripMargin

    spark.sql(sql_shelf_conv_type).createOrReplaceTempView("shelf_conv_type")
    spark.sql(sql_shelf_conv_all).createOrReplaceTempView("shelf_conv_all")
    spark.sql(sql_shelf_conv_wap).createOrReplaceTempView("shelf_conv_wap")

    val sql_shelf_conv =
      s"""
         |select * from shelf_conv_type
         |union (select * from shelf_conv_all)
         |union (select * from shelf_conv_wap)
       """.stripMargin

    spark.sql(sql_benefit_all)
      .drop("dt")
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ec_market_trace_sale_cvr_di")

    spark.sql(sql_shelf_conv)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ec_market_shelf_cvr_di")
  }
}
