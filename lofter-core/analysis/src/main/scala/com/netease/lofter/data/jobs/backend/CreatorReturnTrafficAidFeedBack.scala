package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object CreatorReturnTrafficAidFeedBack {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val returnStart = DateTime.parse(date).minusDays(8).toString("yyyy-MM-dd")
    val returnStartPrevDay = DateTime.parse(returnStart).minusDays(1).toString("yyyy-MM-dd")


    val sql =
      s"""
         |select m.userId, m.postId, m.bgUv
         |from (
         |    select b.userId, a.postId, b.publishDate,
         |           sum(a.bgUv) as bgUv,
         |           sum(if(datediff(a.dt, b.publishDate) <= 2, a.bgUv, 0)) as twoDayBgUv
         |    from (
         |        select dt, postId, count(1) as bgUv
         |        from lofter.dwd_post_expose_di
         |        where dt >='$returnStart' and dt <= '$date' and reaction is null
         |        group by 1, 2
         |    ) a join lofter.dim_post b on a.postId = b.id
         |    where datediff(a.dt, b.publishDate) >= 0
         |    group by b.userId, a.postId, b.publishDate
         |    having twoDayBgUv > 500
         |) m join (
         |    select userId, min(dt) as dt
         |    from (
         |            select dt, accountId as userId, time,
         |                  lag(time, 1) over (partition by accountId order by dt) as  last_day_time
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt>='$returnStartPrevDay' and accountId > 0
         |    ) t
         |    where userId > 0 and
         |          datediff(from_unixtime(cast(`time`/1000 as bigint), 'yyyy-MM-dd'),
         |                   from_unixtime(cast(last_day_time/1000 as bigint), 'yyyy-MM-dd')) >= 30
         |    group by userId
         |) n on m.userId = n.userId
         |  join (
         |    select postid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomStatus=1
         |  ) x on m.postId = x.postId
         |where m.publishDate >= n.dt and m.publishDate >= '2022-01-01'
        |""".stripMargin

    spark.sql(sql)
      .repartition(1)
      .withColumn("time", lit(System.currentTimeMillis()))
      .selectExpr("CAST(userId as STRING) as key", """concat('{"userId":', userId, ',"time":', time, ',"stimulateType": "traffic_aid_feedback"', ',"data": {"blogId":', userId, ',"postId": ', postId,  ', "surpass":', bgUv, '}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.close()
  }
}
