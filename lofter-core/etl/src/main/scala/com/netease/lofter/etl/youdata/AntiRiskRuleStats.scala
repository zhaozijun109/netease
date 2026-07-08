package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object AntiRiskRuleStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Anti Risk Rule Stats")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_rule =
      s"""
         |select 'post' as opType,deviceType,rgName,ruleName,ruleKey,count(distinct postId) as pv, count(distinct userId) as uv
         |from lofter.ods_log_anti_risk_post_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    deviceType,rgName,ruleName,ruleKey
         |union
         |select 'message' as opType,"" as deviceType,rgName,ruleName,ruleKey,count(distinct msgId) as pv, count(distinct blogId) as uv
         |from lofter.ods_log_anti_risk_message_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    rgName,ruleName,ruleKey
         |union
         |select 'comment' as opType,deviceType,rgName,ruleName,ruleKey,count(distinct commentId) as pv, count(distinct userId) as uv
         |from lofter.ods_log_anti_risk_comment_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    deviceType,rgName,ruleName,ruleKey
         |""".stripMargin

    val sql_rule_group =
      s"""
         |select 'post' as opType,deviceType,rgName,count(distinct postId) as pv, count(distinct userId) as uv
         |from lofter.ods_log_anti_risk_post_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    deviceType,rgName
         |union
         |select 'message' as opType,"" as deviceType,rgName,count(distinct msgId) as pv, count(distinct blogId) as uv
         |from lofter.ods_log_anti_risk_message_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    rgName
         |union
         |select 'comment' as opType,deviceType,rgName,count(distinct commentId) as pv, count(distinct userId) as uv
         |from lofter.ods_log_anti_risk_comment_di
         |where dt='$date' and rgRisk='reject'
         |group by
         |    deviceType,rgName
         |""".stripMargin

    val sql_detail =
      s"""
         |select 'post' as opType,deviceType,rgName,ruleName,ruleKey,postId as opId,blogId,postId as passiveId,
         |       userIp as ip,
         |       from_unixtime(cast(eventTime/1000 as bigint),'yyyy-MM-dd HH:mm:ss') opTime,
         |       postLink as content
         |from lofter.ods_log_anti_risk_post_di
         |where dt='$date' and rgRisk='reject'
         |union
         |( select 'message' as opType,"" as deviceType,rgName,ruleName,ruleKey,msgId as opId,a.blogId as blogId,a.otherblogid as passiveId,
         |    a.ip as ip,
         |    from_unixtime(cast(a.publishTime/1000 as bigint),'yyyy-MM-dd HH:mm:ss') opTime,
         |    b.content
         |  from
         |    (select *
         |    from lofter.ods_log_anti_risk_message_di
         |    where dt='$date' and rgRisk='reject') a
         |    left join lofter_db_dump.ods_db_message_nd b on a.msgid=b.id and b.issender=0
         |)
         |union
         |( select 'comment' as opType,deviceType,rgName,ruleName,ruleKey,commentId as opId,userId as blogId,c.postId as passiveId,
         |      c.ip,
         |      from_unixtime(cast(eventTime/1000 as bigint),'yyyy-MM-dd HH:mm:ss') opTime,
         |      d.content
         |  from
         |    (select *
         |      from lofter.ods_log_anti_risk_comment_di
         |      where dt='$date' and rgRisk='reject') c
         |      left join lofter_db_dump.ods_db_post_response_nd d on c.commentId=d.id
         |)
         |""".stripMargin


    val sql_post_daily =
      s"""
         |select count(distinct postid) as postCount, count(distinct blogid) as userCount
         |from lofter.ods_log_anti_risk_similarity_post_di
         |where dt = '$date'
         |""".stripMargin
    val sql_message_daily =
    s"""
      |select count(distinct messageId) as messageCount,
      |       count(1) pv, count(distinct blogid) as userCount
      |from lofter.ods_log_anti_risk_similarity_message_di
      |where dt = "$date"
      |group by dt
      |""".stripMargin

    spark.sql(sql_post_daily)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_risk_similarity_post_di")

    spark.sql(sql_message_daily)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_risk_similarity_message_di")

    spark.sql(sql_rule)
      .withColumn("dt", lit(date))
      .select("opType","deviceType","rgName","ruleName","ruleKey","pv","uv","dt")
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_risk_rule_stats_di")

    spark.sql(sql_rule_group)
      .withColumn("dt", lit(date))
      .select("opType","deviceType","rgName","pv","uv","dt")
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_risk_rule_group_stats_di")

    spark.sql(sql_detail)
      .withColumn("dt", lit(date))
      .select("opType","deviceType","rgName","ruleName","ruleKey","opId","blogId","passiveId","ip","opTime","content","dt")
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_risk_rule_detail_stats_di")

  }

}
