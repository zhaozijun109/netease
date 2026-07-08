package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PaidPostRemainMonitorStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Paid Post Retain Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    val userRetainDays = Seq(2,7,30,90,180)
    // paidPostRemainStats(spark,date)

    // stat the retain ratio for 1DaysAgo, 3DaysAgo, 7DaysAgo
    for (daysAgo <- userRetainDays){
      updatePaidPostRetainStats(spark, date, daysAgo)
    }

    def paidPostRemainStats(spark: SparkSession, date: String): Unit = {
      val sql_paid_post =
        s"""
           |select nvl(pay_type,'all') as pay_type,
           |  count(distinct userId) as trade_uv,
           |	count(distinct blogId) as blog_num,
           |	count(distinct concat_ws('-',userId,blogId)) as blog_user_num
           |from lofter.dwd_evt_post_paid_detail_dd
           |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
           |group by pay_type with rollup
           |""".stripMargin

      spark.sql(sql_paid_post)
        .withColumn("dt", lit(date))
        .repartition(1)
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_post_paid_monitor_di")
    }

    def updatePaidPostRetainStats(spark: SparkSession, date: String, daysAgo: Int): Unit = {
      val newDay = DateTime.parse(date).minusDays(daysAgo-1).toString("yyyy-MM-dd")
      val remainStartDay = DateTime.parse(newDay).plusDays(1).toString("yyyy-MM-dd")

      val sql_result =
        s"""
           |select  '$newDay' as baseDate,a.pay_type,
           |  count(distinct  case when a.blogid=b.blogid then   b.userid else null end) as trade_uv1,
           |  count(distinct  case when a.blogid=b.blogid then  b.blogid else null end ) as blog_num1,
           |  count(distinct case when a.blogid=b.blogid then  concat(b.userid,b.blogid)  else null  end) as blog_user_num1,
           |  count(distinct  case when a.blogid!=b.blogid and b.blogid is not null then   b.userid else null end) as trade_uv2,
           |  count(distinct  case when a.blogid!=b.blogid and b.blogid is not null then  b.blogid else null end ) as blog_num2,
           |  count(distinct case when a.blogid!=b.blogid and b.blogid is not null then  concat(b.userid,b.blogid)  else null  end) as blog_user_num2,
           |  count(distinct b.userid ) as trade_uv3,
           |  count(distinct b.blogid ) as blog_num3,
           |  count(distinct b.userid,b.blogid) as blog_user_num3
           |from
           |(select distinct pay_type,userId,blogId
           |from lofter.dwd_evt_post_paid_detail_dd
           |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$newDay'
           |) a
           |left join
           |(select distinct pay_type,userId,blogId
           |from lofter.dwd_evt_post_paid_detail_dd
           |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$remainStartDay' and '$date'
           |) b
           |on a.pay_type=b.pay_type and a.userId=b.userId
           |group by baseDate,a.pay_type
           |
           |union all
           |select  '$newDay' as baseDate,'all' as pay_type,
           |  count(distinct  case when a.blogid=b.blogid then   b.userid else null end) as trade_uv1,
           |  count(distinct  case when a.blogid=b.blogid then  b.blogid else null end ) as blog_num1,
           |  count(distinct case when a.blogid=b.blogid then  concat(b.userid,b.blogid)  else null  end) as blog_user_num1,
           |  count(distinct  case when a.blogid!=b.blogid and b.blogid is not null then   b.userid else null end) as trade_uv2,
           |  count(distinct  case when a.blogid!=b.blogid and b.blogid is not null then  b.blogid else null end ) as blog_num2,
           |  count(distinct case when a.blogid!=b.blogid and b.blogid is not null then  concat(b.userid,b.blogid)  else null  end) as blog_user_num2,
           |  count(distinct b.userid ) as trade_uv3,
           |  count(distinct b.blogid ) as blog_num3,
           |  count(distinct b.userid,b.blogid) as blog_user_num3
           |from
           |(select distinct userId,blogId
           |from lofter.dwd_evt_post_paid_detail_dd
           |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$newDay'
           |) a
           |left join
           |(select distinct userId,blogId
           |from lofter.dwd_evt_post_paid_detail_dd
           |where dt='$yesterday' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$remainStartDay' and '$date'
           |) b
           |on  a.userId=b.userId
           |group by baseDate,pay_type
           |""".stripMargin

      spark.sql(sql_result)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(daysAgo))
        .repartition(1)
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_post_paid_retain_di")
    }
  }
}
