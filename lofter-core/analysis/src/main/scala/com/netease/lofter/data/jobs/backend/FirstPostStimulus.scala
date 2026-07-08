package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object FirstPostStimulus {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val date = pargs.optional("date").getOrElse(DateTime.now().toString("yyyy-MM-dd"))
    val twoWeeksAgo = DateTime.parse(date).minusDays(14).toString("yyyy-MM-dd")

    val firstPostStimulus =
      s"""
        |select a.userId, nvl(concat_ws(',', c.tags), '') as tags, unix_timestamp() * 1000L as stimulusTime
        |from (
        |   select userId
        |   from (
        |    select userId, tag, post_15d as postCount
        |    from lofter.dws_tag_user_consume_dd
        |    where dt = '$date' and post_15d > 5
        |   ) t1
        |   group by userId
        |) a join lofter.dim_user u on a.userId = u.id
        |left join (
        |    select userId
        |    from lofter.dwd_post_publish_di
        |    where dt = '$date'
        |    group by userId
        |) b on a.userId = b.userId
        |left join (
        |    select userId, collect_list(tag) as tags
        |    from (
        |        select userId, tag, hot,
        |               row_number() over (partition by userId order by hot desc) hotRank
        |        from (
        |            select a.userId, a.tag, b.hot
        |            from (
        |               select userId, tag,
        |                      row_number() over (partition by userId order by postCount desc) as tagRank
        |               from (
        |                select userId, tag, post_15d as postCount
        |                from lofter.dws_tag_user_consume_dd
        |                where dt = '$date' and post_15d > 5
        |               ) t1
        |            ) a
        |            left join (
        |                select tag, count(1) hot
        |                from lofter.dwd_post_hot_di lateral view explode(post_tags) tgs as tag
        |                where dt >= '$twoWeeksAgo'
        |                group by tag
        |            ) b on a.tag = b.tag
        |            where a.tagRank <= 5
        |        ) t2
        |    ) t3
        |    where hotRank <= 3
        |    group by userId
        |) c on a.userId = c.userId
        |where b.userId is null and
        | u.isAnonymous = 0 and
        | u.createTime < to_unix_timestamp('$date','yyyy-MM-dd') * 1000 - 3 * 24 * 3600 * 1000L and
        | u.createTime > to_unix_timestamp('$date','yyyy-MM-dd') * 1000 - 45 * 24 * 3600 * 1000L
        | """.stripMargin

    spark.sql(firstPostStimulus)
      .selectExpr("CAST(userId as STRING) as key", """concat('{"userId":', userId, ',"time":', stimulusTime, ',"stimulateType": "first_post"', ',"data": {"tag": "', tags, '"}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.stop()
  }
}
