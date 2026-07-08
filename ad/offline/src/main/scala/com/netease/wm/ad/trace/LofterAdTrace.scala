package com.netease.wm.ad.trace

import java.sql.{Connection, DriverManager}
import com.github.nscala_time.time.Imports._
import com.netease.wm.ad.common.dbConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.lit

import scala.util.control.NonFatal

object LofterAdTrace {
  case class AdLocation(category: String, location: String, adPage: String)

  def getAdLocations(): Seq[AdLocation] = {
    implicit val db: Connection = {
      Class.forName("com.mysql.jdbc.Driver")
      DriverManager.getConnection(dbConfig.lofterActivityJdbcUrl)
    }
    import com.netease.wm.util.Sql._

    try {
      sql"select category, location, adspace_name as adPage from adspace_manage".query[AdLocation]
    } finally {
      db.close()
    }
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Ad Trace")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order_product/$yesterday").createOrReplaceTempView("ddb_benefit_order_product")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_order/$yesterday").createOrReplaceTempView("ddb_benefit_order")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_adTrace_config/$yesterday").createOrReplaceTempView("ddb_benefit_ad_trace_config")
    spark.read.parquet(s"/user/da_lofter/db_dump/benefit_product_info/$yesterday").createOrReplaceTempView("ddb_benefit_product_info")
    spark.read.parquet(s"/user/da_lofter/db_dump/BenefitShelf/$yesterday").createOrReplaceTempView("ddb_benefit_shelf")
    spark.read.parquet(s"/user/da_lofter/warehouse/dim_blog/dt=$yesterday").createOrReplaceTempView("dim_blog")
    spark.read.parquet(s"/user/da_lofter/warehouse/dim_user/dt=$yesterday").createOrReplaceTempView("dim_user")

    spark.sql(s"select * from lofter.ods_mda_wap_di where dt = '$day'").createOrReplaceTempView("hubble_wap")

    val tracedTradeSql =
      s"""
        |select a.adTrace,
        |    case lower(b.orderFrom)
        |         when 'android' then 'Android'
        |         when 'iphone' then 'iOS' else b.orderFrom end as platform,
        |     count(distinct buyerId) orderUserCount,
        |     sum(storePrice * productNum) orderAmount,
        |     sum(storePrice * productNum - newCouponPreferential) payAmount,
        |     sum(newCouponPreferential) payCoupon
        |from ddb_benefit_order_product a left join (
        |   select n.id as orderId, m.orderFrom
        |   from (select * from lofter.ods_log_flpaysucess_di where dt = '$day') m join ddb_benefit_order n on m.orderId = n.tradeId
        |)b on a.orderId = b.orderId
        |where status in (1,2,3,4) and adTrace is not null and from_unixtime(cast(a.createTime/1000 as bigint), 'yyyy-MM-dd') = '$day'
        |group by adTrace,
        |         case lower(b.orderFrom)
        |            when 'android' then 'Android'
        |            when 'iphone' then 'iOS' else b.orderFrom end
      """.stripMargin

    val loadPageAccessSql =
      s"""
        |select deviceOs as platform, adTrace, sum(pv) loadPv, count(distinct deviceUdid) loadUv, count(distinct userId) loadUserCount
        |from (
        |  select deviceOs, parse_url(currentUrl, 'QUERY', 'adTrace') adTrace, deviceUdid, userId, count(1) pv
        |  from hubble_wap where currentUrl like '%adTrace%' and parse_url(currentUrl, 'QUERY', 'adTrace') is not null
        |  group by deviceOs, parse_url(currentUrl, 'QUERY', 'adTrace'), deviceUdid, userId
        | union all
        |  select deviceOs, parse_url(params['currentUrl'], 'QUERY', 'adTrace') adTrace, deviceUdid, userId, count(1) pv
        |  from lofter.ods_mda_app_partition_di
        |  where dt = '$day'
        |  group by deviceOs, parse_url(params['currentUrl'], 'QUERY', 'adTrace'), deviceUdid, userId
        |) t
        |where adTrace is not null
        |group by deviceOs, adTrace
      """.stripMargin

    val adSql =
     s"""
        |select platform, adId, parse_url(traceUrl, 'QUERY', 'adTrace') adTrace, traceUrl,
        |       sum(case eventId WHEN 'ad-1' THEN pv
        |             WHEN 'ad-3' THEN pv
        |             WHEN 'ad-5' THEN pv
        |             WHEN 'ad-7' THEN pv
        |             WHEN 'ad-9' THEN pv
        |             WHEN 'ad-11' THEN pv
        |             WHEN 'ad-18' THEN pv else 0 end) exposePv,
        |      sum(case eventId WHEN 'ad-1' THEN uv
        |             WHEN 'ad-3' THEN uv
        |             WHEN 'ad-5' THEN uv
        |             WHEN 'ad-7' THEN uv
        |             WHEN 'ad-9' THEN uv
        |             WHEN 'ad-11' THEN uv
        |             WHEN 'ad-18' THEN uv else 0 end) exposeUv,
        |      sum(case eventId WHEN 'ad-2' THEN pv
        |             WHEN 'ad-4' THEN pv
        |             WHEN 'ad-6' THEN pv
        |             WHEN 'ad-8' THEN pv
        |             WHEN 'ad-10' THEN pv
        |             WHEN 'ad-12' THEN pv
        |             WHEN 'ad-19' THEN pv else 0 end) clickPv,
        |      sum(case eventId WHEN 'ad-2' THEN uv
        |             WHEN 'ad-4' THEN uv
        |             WHEN 'ad-6' THEN uv
        |             WHEN 'ad-8' THEN uv
        |             WHEN 'ad-10' THEN uv
        |             WHEN 'ad-12' THEN uv
        |             WHEN 'ad-19' THEN uv else 0 end) clickUv
        |from
        |(
        |  select deviceOs as platform, eventId, params['adId'] adId, params['URL'] traceUrl, count(1) pv, count(distinct deviceUdid) uv
        |  from lofter.ods_mda_app_partition_di
        |  where dt = '$day' and actionType = 'advertisement' and eventId like 'ad-%' and params['URL'] like '%adTrace=%'
        |  group by deviceOs, eventId, params['adId'], params['URL']
        |) t
        |group by platform, adId, traceUrl
        |having (exposePv > 0 or clickPv > 0)
      """.stripMargin

    val traceMetaSql =
      """
        |select parse_url(traceUrl, 'QUERY', 'adTrace') adTrace, name, type
        |from ddb_benefit_ad_trace_config
        |where status = 0 and parse_url(traceUrl, 'QUERY', 'adTrace') is not null
      """.stripMargin

    spark.sql(tracedTradeSql).createOrReplaceTempView("trade")
    spark.sql(loadPageAccessSql).createOrReplaceTempView("load")
    spark.sql(adSql).createOrReplaceTempView("ad")
    spark.sql(traceMetaSql).createOrReplaceTempView("adTrace_meta")

    val adLocations = getAdLocations()

    val adMap = adLocations.map { a => (a.category, a.location) -> a.adPage}.toMap

    spark.udf.register("ad_location", (adTrace: String) => {
      if(adTrace == null || adTrace.isEmpty) {
        None
      } else {
        try {
          val Seq(category, location, _) = adTrace.replaceFirst("^lofter_", "").split("_").toSeq
          adMap.get(category -> location)
        } catch {
          case NonFatal(_) => None
        }

      }
    })

    val mergeSql =
      """
        |select t.platform, t.adTrace, t.adPlace, t.traceUrl, t.adId,
        |       t.productId, p.productName, t.shelfId, s.name as shelfName, t.shareUserId, concat(x.blogName, '.lofter.com') as shareUserHomePage,
        |       m.name as adTraceLinkName, if(t.shareUserId is not null, 2, nvl(m.type, 0)) as adTraceLinkType,
        |       sum(t.exposePv) exposePv, sum(t.exposeUv) exposeUv, sum(t.clickPv) clickPv, sum(t.clickUv) clickUv,
        |       sum(t.loadPv) loadPv, sum(t.loadUv) loadUv, sum(t.loadUserCount) loadUserCount,
        |       sum(t.orderAmount) orderAmount, sum(t.payAmount) payAmount, sum(t.payCoupon) payCoupon, sum(t.orderUserCount) orderUserCount
        |from (
        |  select nvl(nvl(ad.platform, load.platform), trade.platform) platform,
        |       nvl(nvl(ad.adTrace, load.adTrace), trade.adTrace) adTrace,
        |       ad_location(nvl(nvl(ad.adTrace, load.adTrace), trade.adTrace)) adPlace,
        |       ad.traceUrl,
        |       ad.adId,
        |       ad.exposePv, ad.exposeUv, ad.clickPv, ad.clickUv,
        |       load.loadPv, load.loadUv, load.loadUserCount,
        |       trade.orderAmount, trade.payAmount, trade.payCoupon, trade.orderUserCount,
        |       cast(parse_url(ad.traceUrl, 'QUERY', 'productId') as bigint) as productId,
        |       cast(parse_url(ad.traceUrl, 'QUERY', 'shelfId') as bigint) as shelfId,
        |       cast(regexp_extract(nvl(nvl(ad.adTrace, load.adTrace), trade.adTrace), 'lofter_(\\\\d+)_\\\\d+', 1) as bigint) shareUserId
        |  from ad full outer join load on ad.platform = load.platform and ad.adTrace = load.adTrace
        |        full outer join trade on load.adTrace = trade.adTrace and load.platform = trade.platform
        |) t left join adTrace_meta m on t.adTrace = m.adTrace
        |    left join ddb_benefit_product_info p on t.productId = p.id
        |    left join ddb_benefit_shelf s on t.shelfId = s.id
        |    left join (
        |      select u.id as userId, b.blogName
        |      from dim_user u join dim_blog b on u.mainBlogId = b.id
        |    ) x on t.shareUserId = x.userId
        |group by t.platform, t.adTrace, t.adPlace, t.traceUrl, t.adId,
        |         t.productId, p.productName, t.shelfId, s.name, t.shareUserId,
        |         m.name, if(t.shareUserId is not null, 2, nvl(m.type, 0)), x.blogName
      """.stripMargin

    spark.sql(mergeSql)
      .select("platform","adtrace","traceurl","adplace","adid","exposepv","exposeuv","clickpv","clickuv","loadpv","loaduv","orderamount","payamount","paycoupon","orderusercount","productid","productname","shelfid","shelfname","shareuserid","shareuserhomepage","adtracelinkname","adtracelinktype","loadusercount")
      .withColumn("dt", lit(day))
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ad_trace_source_di")

    spark.close()
  }
}
