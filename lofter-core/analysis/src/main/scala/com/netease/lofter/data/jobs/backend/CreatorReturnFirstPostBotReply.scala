package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object CreatorReturnFirstPostBotReply {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(date).minusDays(30).toString("yyyy-MM-dd")

    val sql =
      s"""
         |select a.userId as blogId, d.postId
         |from (
         |    select userId from lofter.dws_par_creator_dd where dt='$date' and level in ('C', 'B', 'A', 'S')
         |) a
         |  join (
         |    select userId from lofter.dws_par_user_post_dd where dt='$dayAgo' and from_unixtime(cast(last_valid_publish_time/1000 as bigint), 'yyyy-MM-dd') <= '$monthAgo'
         |  ) b on a.userId = b.userId
         |  join (
         |    select userId from lofter.dws_par_user_post_dd where  dt='$date' and  from_unixtime(cast(last_valid_publish_time/1000 as bigint), 'yyyy-MM-dd') = '$date'
         |  ) c on a.userId = c.userId
         |  join (
         |    select userId, id as postId,
         |           row_number() over (partition by userId order by publishTime) as rnk
         |    from lofter.dim_post
         |    where from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd') = '$date' and
         |          isPublished = true and isForbidden = false and allowView = 0
         |  ) d on a.userId = d.userId
         |where d.rnk = 1
         |""".stripMargin

    spark.sql(sql)
      .withColumn("time", lit(System.currentTimeMillis()))
      .selectExpr("CAST(blogId as STRING) as key", """concat('{"userId":', blogId, ',"time":', time, ',"stimulateType": "callback_post"', ',"data": {"blogId":', blogId, ',"postId": ', postId,'}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.close()
  }
}
