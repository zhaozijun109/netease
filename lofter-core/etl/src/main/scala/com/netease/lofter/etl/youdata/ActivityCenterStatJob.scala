package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports._
import com.netease.lofter.etl.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object ActivityCenterStatJob {
  val batchSize = 20

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    // filter the post article on condition publishTime>threeMonthAgo; filter the post hot operation on condition opTime>threeMonthAgo
    val st = DateTime.parse(date).minusMonths(3).getMillis
    spark.read.parquet(s"/user/da_lofter/db_dump/Post/$yesterday").filter(s"length(tag)>0 and publishTime>$st").createOrReplaceTempView("post")
    spark.read.parquet(s"/user/da_lofter/db_dump/PostHot/$yesterday").filter(s"opTime>$st").createOrReplaceTempView("post_hot")
    spark.read.parquet(s"/user/da_lofter/db_dump/ActivityCenter_Activity/$yesterday").filter("length(tagNames)>0 and status=0").createOrReplaceTempView("activity")

    // only to stat the activity that startTime <= date <= endTime
    val sql_activity =
      s"""
         |select id,startTime,endTime,tagName
         |from activity lateral view explode(split(tagNames,",")) t1 as tagName
         |where from_unixtime(cast(startTime/1000 as bigint),'yyyy-MM-dd') <= '$date' and from_unixtime(cast(endTime/1000 as bigint),'yyyy-MM-dd') >= '$date'
       """.stripMargin

    spark.sql(sql_activity).createOrReplaceTempView("activity_opt")

    val sql_post =
      s"""
         |select tagName,ID as postId,PublisherUserId,PublishTime
         |from (select * from post where IsPublished != 0 and valid !=25 and (movefrom is null or movefrom not like 'blogmove2018%')) A lateral view explode(split(Tag,",")) t1 as tagName
       """.stripMargin

    spark.sql(sql_post).createOrReplaceTempView("post_opt")

    val activity_dim =
      s"""
         |select distinct A.id as activityId,startTime,endTime,B.postId,B.PublisherUserId,B.PublishTime,C.id as hotOpId,C.opTime
         |from activity_opt A
         |join post_opt B
         |on A.tagName=B.tagName
         |left join post_hot C
         |on B.postId = C.postId
       """.stripMargin

    spark.sql(activity_dim).createOrReplaceTempView("activity_dim")

    val sql_res =
      s"""
         |select A.activityId,
         |        nvl(userCount,0) as userCount,nvl(lastUserCount,0) as lastUserCount,nvl(postCount,0) as postCount,
         |        nvl(lastPostCount,0) as lastPostCount,nvl(hotValue,0) as hotValue,nvl(lastHotValue,0) as lastHotValue
         |from
         |(select activityId,count(distinct PublisherUserId) as userCount, count(distinct postId) as postCount,
         |        count(distinct case when from_unixtime(cast(PublishTime/1000 as bigint),'yyyy-MM-dd')='$date' then PublisherUserId end) as lastUserCount,
         |        count(distinct case when from_unixtime(cast(PublishTime/1000 as bigint),'yyyy-MM-dd')='$date' then postId end) as lastPostCount
         |from activity_dim where PublishTime>=startTime and PublishTime<= endTime group by activityId) A
         |left join
         |(select activityId,count(distinct hotOpId) as hotValue,
         |        count(distinct case when from_unixtime(cast(opTime/1000 as bigint),'yyyy-MM-dd')='$date' then hotOpId end) as lastHotValue
         |from activity_dim where opTime>=startTime and optime<= endTime
         |group by activityId) C
         |on A.activityId=C.activityId
        """.stripMargin

    //implicit val db: Connection = getStatisticsDB
    import com.netease.lofter.etl.common.spark.SparkSqlImplicits._
    import com.netease.wm.util.Sql._

    implicit val db2: Connection = databases.getDDBConn

    spark.sql(sql_res).collect().grouped(batchSize).foreach { row =>
      val params = row.map( x => rowParam(x))

      sql"""
            |update ActivityCenter_Activity  set userCount=${"userCount"},lastUserCount=${"lastUserCount"},postCount=${"postCount"},lastPostCount=${"lastPostCount"},hotValue=${"hotValue"},lastHotValue=${"lastHotValue"}
            | where id=${"activityId"}
         """.stripMargin.batchUpdate(params)
    }

    db2.close()
    spark.close()
  }

}
