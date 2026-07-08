package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

import java.time.LocalDate

object BookstoreSettlementStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Bookstore Settlement Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val dayOfMonth = LocalDate.parse(date).getDayOfMonth
    val lastDayOfMonth = LocalDate.parse(date).lengthOfMonth()
    if (dayOfMonth == lastDayOfMonth) {
      monthSettlementStats(spark,date)
    } else {
      println(s"there is no need to cal on date $date ")
    }

    def monthSettlementStats(spark: SparkSession, date: String): Unit = {
      val localDt = LocalDate.parse(date)
      val startDay = localDt.withDayOfMonth(1).toString
      val endDay = localDt.withDayOfMonth(localDt.lengthOfMonth()).toString

      val sql_blog_vip =
        s"""
           |select cast(userId as bigint) as userId
           |from lofter_db_dump.ods_db_admin_pub_data_nd LATERAL VIEW explode(split(content, ',')) t2 AS userId
           |where name='book_store_blogIds'
           |group by cast(userId as bigint)
           |""".stripMargin

      spark.sql(sql_blog_vip).createOrReplaceTempView("blogVip")

      val sql_bookstore_post_uv =
        s"""
           |select a.postId,count(distinct c.userid) as uv,'read' as type  
           |from
           |(
           |  select userid, postId as itemid, dt
           |  from lofter.dwd_post_browse_di
           |  where dt between '$startDay'  and '$endDay' and is_real > 0
           |  group by userid, postId, dt
           |) c
           |join
           |(select postId,blogid from lofter.dim_bookstore_post_dd where dt='$date' group by postId,blogid) a
           |on a.postId=c.itemid
           |join
           |(select amount,tradeid,userid,from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') as startday,finishtime,vipdays,
           |        deviceid,date_add(from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') ,cast((vipdays-1) as int)) as endday
           |from lofter_db_dump.ods_db_trade_store_vip_order_nd
           |where date_add(from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') ,cast((vipdays-1) as int)) >= '$startDay'
           |    and from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') <= '$endDay'
           |    and status=1) b
           |on c.dt between b.startday and b.endday and b.userid=c.userid
           |group by a.postId,'read'
           |
           |union all
           |select postid,count(distinct userid)*3 as uv,'trade' as type
           |from lofter_db_dump.ods_db_trade_store_vip_order_nd 
           |where date_add(from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd'),cast((vipdays-1) as int)) >= '$startDay'
           |    and from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') <='$endDay'
           |    and status=1
           |group by postid,'trade'
           |
           |union all
           |select postid,count(distinct userid)*3 as uv,'trade' as type
           |from lofter_db_dump.ods_db_trade_fans_vip_order_nd
           |where date_add(from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd'),cast((vipdays-1) as int)) >= '$startDay'
           |    and from_unixtime(cast(finishtime / 1000 AS BIGINT), 'yyyy-MM-dd') <='$endDay'
           |    and status=1 and vipblogId in (select userId from blogVip)
           |group by postid,'trade'
           |""".stripMargin

      spark.sql(sql_bookstore_post_uv).createOrReplaceTempView("bookstore_post")
      val totalUv = spark.sql("select sum(uv) as uv from bookstore_post").head().getLong(0)

      val sql_result =
        s"""
           |select postId, sum(uv)/$totalUv as ratio
           |from bookstore_post
           |where postId > 0
           |group by postId
           |""".stripMargin

      spark.sql(sql_result)
        .withColumn("dt", lit(date))
        .repartition(1)
        .write
        .mode("overwrite")
        .insertInto("lofter_dm.ads_bookstore_settlement_md")

    }

    spark.close()
  }

}
