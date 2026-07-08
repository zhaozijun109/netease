package com.netease.lofter.etl.youdata

import java.sql.Connection
import com.github.nscala_time.time.Imports._
import com.netease.lofter.etl.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.lit

object BenefitMarketTradeStatJob {
  case class OrderStats(orderCount: Long, orderUserCount: Long, orderAmount: Option[Double], welfare: Option[Double], deliveryPrice: Option[Double], orderCoupon: Option[Double])
  case class PayStats(payUserCount: Long, payCount: Long, payAmount: Option[Double], payWelfare: Option[Double], payDeliveryPrice: Option[Double], payCoupon: Option[Double])
  case class FirstPayStats(firstPayUserCount: Long, firstPayDayAmount: Option[Double])
  case class TotalStats(orderUserCount: Long, orderCount: Long, orderAmount: Option[Double], payUserCount: Long, payCount: Long, payAmount: Option[Double])
  case class TotalProductCount(orderProductCount: Long, payProductCount: Long)

  case class ResultStats(orderCount: Long, orderUserCount: Long, orderAmount: Option[Double], welfare: Option[Double], deliveryPrice: Option[Double],
                         payCount: Long, payUserCount: Long, payAmount: Option[Double], payWelfare: Option[Double], payDeliveryPrice: Option[Double],
                         firstPayUserCount: Long, firstPayDayAmount: Option[Double], totalOrderUserCount: Long, totalOrderCount: Long, totalOrderAmount: Option[Double], totalPayUserCount: Long,
                         totalPayCount: Long, totalPayAmount: Option[Double], totalOrderProductCount: Long, totalPayProductCount: Long, orderCoupon: Option[Double], payCoupon: Option[Double])

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order/$yesterday").createOrReplaceTempView("benefit_order")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order_product/$yesterday").createOrReplaceTempView("benefit_order_product")

    val productTradeSql =
      s"""
         |select b.productId, b.buyerId, a.tradeId, b.productNum, b.storePrice, a.payTime
         |from benefit_order a join benefit_order_product b on a.id = b.orderId
         |where b.type = 3 and a.originType in (0, 4)
       """.stripMargin

    spark.sql(productTradeSql).createOrReplaceTempView("product_trade")

    val orderSql =
      s"""
         |select count(distinct tradeId) orderCount,
         |       count(distinct buyerId) orderUserCount,
         |       sum(amount) orderAmount,
         |       sum(welfare) welfare,
         |       sum(deliveryPrice) deliveryPrice,
         |       sum(newCouponPreferential) orderCoupon
         |from benefit_order
         |where productType = 3 and from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') = '$date' and originType in (0, 4)
        """.stripMargin

    val paySql =
      s"""
         |select count(distinct buyerId) payUserCount,
         |       count(distinct tradeId) payCount,
         |       sum(amount) payAmount,
         |       sum(welfare) payWelfare,
         |       sum(deliveryPrice) payDeliveryPrice,
         |       sum(newCouponPreferential) payCoupon
         |from benefit_order
         |where productType = 3 and from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') = '$date' and originType in (0, 4)
       """.stripMargin

    val firstPayUserSql =
      s"""
         |select distinct(buyerId) as userId
         |from (
         |  select *, row_number() over (partition by buyerId order by payTime) as ra
         |  from benefit_order
         |  where productType = 3 and payTime > 0 and originType in (0, 4)
         |) a
         |where ra = 1 and from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') = '$date'
       """.stripMargin


    spark.sql(firstPayUserSql).createOrReplaceTempView("first_pay_users")

    val firstPaySql =
      s"""
         |select count(distinct a.userId) firstPayUserCount,
         |       sum(amount) firstPayDayAmount
         |from first_pay_users a join benefit_order b on a.userId = b.buyerId
         |where from_unixtime(cast(b.payTime/1000 as bigint), 'yyyy-MM-dd') = '$date' and b.originType in (0, 4)
       """.stripMargin

    val totalSql =
      s"""
         |select count(distinct buyerId) orderUserCount,
         |       count(distinct tradeId) orderCount,
         |       sum(amount) orderAmount,
         |       count(distinct if(payTime > 0, buyerId, null)) payUserCount,
         |       count(distinct if(payTime > 0, tradeId, null)) payCount,
         |       sum(if(payTime > 0, amount, 0)) payAmount
         |from benefit_order
         |where productType = 3 and from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') <= '$date' and originType in (0, 4)
       """.stripMargin

    val totalProductNumSql =
      s"""
         |select count(distinct productId) orderProductCount,
         |       count(distinct if(payTime > 0, productId, null)) payProductCount
         |from product_trade
         |where from_unixtime(cast(payTime/1000 as bigint), 'yyyy-MM-dd') <= '$date'
       """.stripMargin

    val orderStats = spark.sql(orderSql).as[OrderStats].collect().head
    val payStats = spark.sql(paySql).as[PayStats].collect().head
    val firstPayStats = spark.sql(firstPaySql).as[FirstPayStats].collect().head
    val totalStats = spark.sql(totalSql).as[TotalStats].collect().head
    val totalProductCount = spark.sql(totalProductNumSql).as[TotalProductCount].collect().head

    val result = ResultStats(orderStats.orderCount, orderStats.orderUserCount, orderStats.orderAmount, orderStats.welfare,
      orderStats.deliveryPrice, payStats.payCount, payStats.payUserCount, payStats.payAmount, payStats.payWelfare,
      payStats.payDeliveryPrice, firstPayStats.firstPayUserCount, firstPayStats.firstPayDayAmount,
      totalStats.orderUserCount, totalStats.orderCount, totalStats.orderAmount, totalStats.payUserCount,
      totalStats.payCount, totalStats.payAmount, totalProductCount.orderProductCount, totalProductCount.payProductCount, orderStats.orderCoupon, payStats.payCoupon)

    spark.createDataFrame(Seq(result))
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ec_market_trade_di")

    spark.close()
  }
}
