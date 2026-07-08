package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object CreatorTrafficAidSensing {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val startDay = DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")


    val sql =
      s"""
         |select a.postId, a.blogId, least(b.pv, d.viewCount) as pv
         |from (
         |  select id as postId, blogId
         |  from lofter.dim_post
         |  where publishDate = '$startDay'
         |  group by id, blogId
         |) a join (
         |  select postId as itemId, count(userid) pv
         |  from lofter.dwd_post_expose_di
         |  where dt between '$startDay' and '$date' and reaction is null
         |  group by itemId
         |) b on a.postId = b.itemId
         |join (
         |  select blogId, postId, count(1) hot
         |  from lofter_db_dump.ods_db_post_hot_nd
         |  where from_unixtime(cast(optime/1000 as bigint),'yyyy-MM-dd') between '$startDay' and '$date'
         |  group by blogId, postId
         |) c on a.postId = c.postId and a.blogId = c.blogId
         |join (
         |  select postId, viewCount
         |  from lofter_db_dump.ods_db_post_view_count_nd
         |) d on a.postId = d.postId
         |where case when least(b.pv, d.viewCount) >= 9999 then c.hot >= 300
         |           when least(b.pv, d.viewCount) >= 7000 and least(b.pv, d.viewCount) < 9999 then c.hot >= 200
         |           when least(b.pv, d.viewCount) >= 5000 and least(b.pv, d.viewCount) < 7000 then c.hot >= 150
         |           when least(b.pv, d.viewCount) >= 3000 and least(b.pv, d.viewCount) < 5000 then c.hot >= 100
         |           else false end
         |""".stripMargin

    spark.sql(sql)
      .withColumn("time", lit(System.currentTimeMillis()))
      .selectExpr("CAST(blogId as STRING) as key", """concat('{"userId":', blogId, ',"time":', time, ',"stimulateType": "traffic_aid_sense"', ',"data": {"blogId":', blogId, ',"postId": ', postId,  ', "userCount":', pv, '}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.close()
  }
}
