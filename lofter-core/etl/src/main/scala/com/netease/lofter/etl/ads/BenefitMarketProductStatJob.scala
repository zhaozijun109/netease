package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports._
import com.netease.lofter.etl.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.sql.Connection

object BenefitMarketProductStatJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order/$yesterday").createOrReplaceTempView("benefit_order")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order_product/$yesterday").createOrReplaceTempView("benefit_order_product")

    val productTradeSql =
      s"""
         |select b.productId, b.buyerId, a.tradeId, b.productNum, b.storePrice, b.newCouponPreferential, a.payTime, a.createTime
         |from benefit_order a join benefit_order_product b on a.id = b.orderId
         |where b.type = 3 and a.originType = 0
       """.stripMargin

    spark.sql(productTradeSql).createOrReplaceTempView("product_trade")


    val totalOrderSql =
      s"""
         |select productId, count(distinct buyerId) orderUserCount,
         |       count(distinct tradeId) orderCount,
         |       sum(productNum) orderProductNum,
         |       sum(storePrice * productNum) orderProductAmount,
         |       sum(newCouponPreferential) totalOrderCoupon
         |from product_trade
         |group by productId
        """.stripMargin

    val totalPaySql =
      """
        |select productId,
        |       count(distinct buyerId) payUserCount,
        |       count(distinct tradeId) payCount,
        |       sum(productNum) payProductNum,
        |       sum(storePrice * productNum) payProductAmount,
        |       sum(newCouponPreferential) totalPayCoupon
        |from product_trade
        |where payTime > 0
        |group by productId
      """.stripMargin

    spark.sql(totalOrderSql).cache().createOrReplaceTempView("total_order")
    spark.sql(totalPaySql).cache().createOrReplaceTempView("total_pay")

    val periods = Seq("day", "week", "14Days", "month")

    for {
      period <- periods
    } {
      val startDate: String = period match {
        case "day" => date
        case "week" => DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
        case "14Days" => DateTime.parse(date).minusDays(14).toString("yyyy-MM-dd")
        case "month" => DateTime.parse(date).minusDays(30).toString("yyyy-MM-dd")
      }
      val endDate = date

      val pageViewSql =
        s"""
           |select
           |    productId,
           |    count(distinct(if(eventId in ('w3-2', 'WE1-23'), userId, null))) as viewUserCount,
           |    sum(if(eventId in ('w3-2', 'WE1-23'), 1, 0)) as viewCount,
           |    count(distinct(if(eventId = 'w3-5' or (eventId = 'WE1-25' and params['type'] = 1), userId, null))) as shoppingCartUserCount,
           |    sum(if(eventId = 'w3-5' or (eventId = 'WE1-25' and params['type'] = 1), 1, 0)) as shoppingCartCount
           |from (
           |    select
           |        a.productId,
           |        a.userId,
           |        a.eventId,
           |        a.params
           |    from lofter.dwd_evt_benefit_page_view_di a
           |    join lofter.dim_user b
           |      on a.userid = b.id
           |    where a.dt >= '$startDate'
           |      and a.dt <= '$endDate'
           |      and b.isAnonymous = 0
           |      and a.eventId in ('w3-2', 'w3-5')
           |    union all
           |    select
           |        productId,
           |        userId,
           |        eventId,
           |        params
           |    from lofter.dwd_evt_benefit_page_view_di
           |    where dt >= '$startDate'
           |      and dt <= '$endDate'
           |      and eventId in ('WE1-23', 'WE1-25')
           |      and userId is not null
           |) t
           |group by productId;
         """.stripMargin

      val orderSql =
        s"""
           |select productId, count(distinct buyerId) orderUserCount,
           |       count(distinct tradeId) orderCount,
           |       sum(productNum) orderProductNum,
           |       sum(storePrice * productNum) orderProductAmount,
           |       sum(newCouponPreferential) orderCoupon
           |from product_trade
           |where from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') >= '$startDate' and
           |      from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') <= '$endDate'
           |group by productId
          """.stripMargin

      val paySql =
        s"""
           |select productId,
           |       count(distinct buyerId) payUserCount,
           |       count(distinct tradeId) payCount,
           |       sum(productNum) payProductNum,
           |       sum(storePrice * productNum) payProductAmount,
           |       sum(newCouponPreferential) payCoupon
           |from product_trade
           |where from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') >= '$startDate' and
           |      from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') <= '$endDate'
           |group by productId
          """.stripMargin

      val exposeSql =
       s"""
          |select productId,
          |       count(1) as exposePv,
          |       count(distinct userId) as exposeUv
          |from (
          |    select a.productId,
          |           a.userId
          |    from lofter.dwd_evt_benefit_page_view_di a
          |    join lofter.dim_user b
          |      on a.userid = b.id
          |    where a.dt >= '$startDate'
          |      and a.dt <= '$endDate'
          |      and b.isAnonymous = 0
          |      and a.productId > 0
          |      and a.eventId in ('w1-18','w10-10','w3-15','w10-11','w3-21','w1-28','w2-13','w1-24','w1-77','w1-79','w1-76')
          |    union all
          |    select productId,
          |           userId
          |    from lofter.dwd_evt_benefit_page_view_di
          |    where dt >= '$startDate'
          |      and dt <= '$endDate'
          |      and eventId in ('WE1-6','WE1-8','WE1-18','WE1-21','WE1-29')
          |      and userId is not null
          |) t
          |group by productId;
          |""".stripMargin

      spark.sql(exposeSql).createOrReplaceTempView("expose")
      spark.sql(pageViewSql).createOrReplaceTempView("page_view")
      spark.sql(orderSql).createOrReplaceTempView("order")
      spark.sql(paySql).createOrReplaceTempView("pay")

      val mergeSql =
        s"""
           |select a.productId, a.productName,a.supplyName,
           |       0 as categoryType, a.category3_name as threeLevel, a.category2_name as twoLevel, a.category1_name as oneLevel,
           |       b.viewUserCount, b.viewCount, b.shoppingCartUserCount, b.shoppingCartCount,
           |       c.orderUserCount, c.orderCount, c.orderProductNum, c.orderProductAmount,
           |       d.payUserCount, d.payCount, d.payProductNum, d.payProductAmount,
           |       e.orderUserCount as totalOrderUserCount, e.orderCount as totalOrderCount,
           |       e.orderProductNum as totalOrderProductNum, e.orderProductAmount as totalOrderProductAmount,
           |       f.payUserCount as totalPayUserCount, f.payCount as totalPayCount, f.payProductNum as totalPayProductNum,
           |       f.payProductAmount as totalPayProductAmount,
           |       c.orderCoupon, d.payCoupon, e.totalOrderCoupon, f.totalPayCoupon,
           |       a.productType, concat_ws(',', a.ips) as ips,
           |       g.exposePv, g.exposeUv
           |from lofter.dim_benefit_product a
           |   left join page_view b on a.productId = b.productId
           |   left join order c on a.productId = c.productId
           |   left join pay d on a.productId = d.productId
           |   left join total_order e on a.productId = e.productId
           |   left join total_pay f on a.productId = f.productId
           |   left join expose g on a.productId = g.productId
           |""".stripMargin

      val result = spark.sql(mergeSql)
        .repartition(1)
        .withColumn("dt", lit(endDate))
        .withColumn("period", lit(period))

      result.write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_ec_market_product_di")

      // TODO disable output in gy env
      // writeResultToDDB(result, endDate, period)
    }

    def writeResultToDDB(df: DataFrame, date: String, period: String): Unit = {
      import com.netease.lofter.etl.common.spark.SparkSqlImplicits._
      import com.netease.wm.util.Sql._
      implicit val db2: Connection = databases.getMallDDBConn

      sql"delete from Benefit_MarketProductStats where dt = ${0} and period = ${1}".update(param(date,period))

      df.collect().foreach { row =>
        val productId = row.getAs[Long]("productId")
        if(productId != 7661010 && productId != 7675233 && productId != 7675232) {
          sql"""insert into Benefit_MarketProductStats (
               |         dt, period, productId, productName,
               |         viewUserCount, viewCount, shoppingCartUserCount, shoppingCartCount,
               |         orderUserCount, orderCount, orderProductNum, orderProductAmount,
               |         payUserCount, payCount, payProductNum, payProductAmount,
               |         totalOrderUserCount, totalOrdercount, totalOrderProductNum, totalOrderProductAmount,
               |         totalPayUserCount, totalPayCount, totalPayProductNum, totalPayProductAmount,
               |         orderCoupon, payCoupon, totalOrderCoupon, totalPayCoupon, createTime
               | ) values (${"dt"}, ${"period"}, ${"productId"}, ${"productName"}, ${"viewUserCount"}, ${"viewCount"},
               |           ${"shoppingCartUserCount"}, ${"shoppingCartCount"}, ${"orderUserCount"}, ${"orderCount"},
               |           ${"orderProductNum"}, ${"orderProductAmount"}, ${"payUserCount"}, ${"payCount"}, ${"payProductNum"},
               |           ${"payProductAmount"}, ${"totalOrderUserCount"}, ${"totalOrderCount"}, ${"totalOrderProductNum"},
               |           ${"totalOrderProductAmount"}, ${"totalPayUserCount"}, ${"totalPayCount"}, ${"totalPayProductNum"},
               |           ${"totalPayProductAmount"},${"orderCoupon"},${"payCoupon"},${"totalOrderCoupon"},${"totalPayCoupon"},unix_timestamp()*1000)
               |""".stripMargin.update(rowParam(row))
        }
      }

      db2.close()
    }

    spark.close()
  }
}
