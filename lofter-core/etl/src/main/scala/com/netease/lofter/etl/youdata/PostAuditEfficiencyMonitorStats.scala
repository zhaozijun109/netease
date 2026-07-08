package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostAuditEfficiencyMonitorStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Post Audit Efficiency Monitor Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneMonthAgo = DateTime.parse(date).minusMonths(1).toString("yyyy-MM-dd")

    val sql_post_audit_based_on_enter =
      s"""
         |select '$date' as dt,'入库' as statType,
         |       from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') as date/**入库日期**/,
         |case when datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))=0 then "当天审核"
         |     when datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))=1 then "次日审核"
         |     when datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))>=2
         |      and  datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))<=3 then "2-3日内审核"
         |     when datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))>=4
         |      and  datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))<=7 then "4-7日内审核"
         |     when datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd'))>7
         |        then "7天后内审核" end auditDays/**审核距离入库的天数**/,
         |case when type=1 then "text"
         |     when type=2 then "photo"
         |     when type=3 then "music"
         |     when type=4 then "video"
         |     when type=5 then "ask"
         |     when type=6 then "long"
         |     else 'else' end contentType/**帖子类型**/,
         |case when recomstatus=0 then "待审核"
         |     when recomstatus=1 then "推荐"
         |     when recomstatus=-1 then "不推荐"
         |     when recomstatus=-2 then '待push'
         |     else 'else' end recomStatus/**帖子类型**/,
         |count(distinct postid) as postNum
         | from lofter_db_dump.ods_db_recommend_review_post_nd
         | where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd HH') between '$oneMonthAgo 18' and '$date 18'
         | group by 1,2,3,4,5,6
         |""".stripMargin

    val sql_post_audit_based_on_audit =
      s"""
         |select '$date' as dt,'审核' as statType,reviewday as date /**审核日期**/,
         |case when difdays=0 then "当天发布"
         |     when difdays=1 then "前一天发布"
         |     when difdays>=2 and difdays<=3 then "2-3天发布"
         |     when difdays>=4 and difdays<=7 then "4-7天发布"
         |     when difdays>=7 then "7天前发布" end auditDays/**审核距离入库的天数**/,
         |case when type=1 then "text"
         |     when type=2 then "photo"
         |     when type=3 then "music"
         |     when type=4 then "video"
         |     when type=5 then "ask"
         |     when type=6 then "long"
         |     else 'else' end contentType/**帖子类型**/,
         |recomStatus/**审核结果**/,
         |count(distinct postId ) as postNum
         |from
         |
         |(select from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') reviewday,type,
         |        datediff(from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd') ,from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')) difdays,
         |        case when recomstatus=0 then "待审核"
         |             when recomstatus=1 then "推荐"
         |             when recomstatus=-1 then "不推荐"
         |             when recomstatus=-2 then '待push'
         |             else 'else' end recomStatus,
         |        postId
         | from
         | lofter_db_dump.ods_db_recommend_review_post_nd
         | where from_unixtime(cast(reviewtime/1000 as bigint),'yyyy-MM-dd')='$date'
         |)t
         |group by 1,2,3,4,5,6
         |""".stripMargin

    val df_result = spark.sql(sql_post_audit_based_on_enter).union(spark.sql(sql_post_audit_based_on_audit))
    df_result
      .repartition(5)
      .select("statType","date","auditDays","contentType","recomStatus","postNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_audit_efficiency_monitor_stats_di")

    spark.close()

  }

}
