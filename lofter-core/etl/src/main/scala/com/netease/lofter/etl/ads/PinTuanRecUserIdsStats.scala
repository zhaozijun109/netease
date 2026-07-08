package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}

object PinTuanRecUserIdsStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Pin Tuan Invitation Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    val sql_exclude_userId =
      s"""
         |select blogId as userId from lofter_db_dump.ods_db_blog_official_blog_nd
         |union
         |select userId from lofter.zq_lofter_pindan_black_user
         |""".stripMargin

    spark.sql(sql_exclude_userId).cache().createOrReplaceTempView("excludeUser")

   val sql_follow =
     s"""
        |select * from
        |(select a.*, 1 as grade,row_number() over(partition by a.blogId order by c.post_count_std desc) as rk from
        |(select userId,blogId from lofter_db_dump.ods_db_user_following_nd where userId not in (select userId from excludeUser)) a
        |join
        |(select blogId,userId from lofter_db_dump.ods_db_user_following_nd where blogId not in (select userId from excludeUser)) b
        |on a.userId = b.blogId and a.blogId=b.userId
        |join
        |(select userId,post_count_std from lofter.dws_par_user_base_dd
        |where dt='$date' and from_unixtime(cast(last_login_time/1000 as bigint),'yyyy-MM-dd') >= '$oneWeekAgo' ) c
        |on a.userId =c.userId)
        |where rk<=10
        |""".stripMargin

    val sql_message =
      s"""
         |select a.* from
         |(select a.*,2 as grade, row_number() over(partition by a.receive_userId order by publishTime desc) as rk from
         |(select blogId as sender_userId, otherblogid as receive_userId,max(publishTime) as publishTime from lofter_db_dump.ods_db_message_nd
         | where issender=1 and  from_unixtime(cast(publishTime/1000 as bigint),'yyyy-MM-dd')  between '$oneWeekAgo' and '$date'
         |    and blogId not in (select userId from excludeUser)
         | group by 1,2) a
         |join
         |(select distinct blogId as sender_userId, otherblogid as receive_userId from lofter_db_dump.ods_db_message_nd
         |where issender=1 and  from_unixtime(cast(publishtime/1000 as bigint),'yyyy-MM-dd')  between '$oneWeekAgo' and '$date'
         |  and otherblogid not in (select userId from excludeUser) ) b
         |on a.sender_userId = b.receive_userId and a.receive_userId = b.sender_userId
         |) a
         |where rk<=10
         |""".stripMargin

    val sql_visit =
      s"""
         |select a.* from
         |(select userId,blogId,3 as grade, row_number() over(partition by blogId order by visit_count desc) as rk
         |from
         |(select userId,blogId,sum(visitcount) as visit_count
         |from lofter.dws_par_user_home_visit_di
         |where dt between '$oneWeekAgo' and '$date' and blogId not in (select userId from excludeUser) and userId not in (select userId from excludeUser)
         |group by 1,2) a
         |) a
         |where rk<=10
         |""".stripMargin

    spark.sql(sql_follow).createOrReplaceTempView("followUsers")
    spark.sql(sql_message).createOrReplaceTempView("messageUsers")
    spark.sql(sql_visit).createOrReplaceTempView("visitUsers")

    val sql_result =
      s"""
         |select blogId,concat_ws(',',collect_set(userId)) as recUserIds from
         |(select *,row_number() over(partition by blogId order by grade,rk) as rk2
         |from
         |(select userId,blogId,grade,rk from followUsers
         |union all
         |select sender_userId as userId,receive_userId as blogId,grade,rk from messageUsers
         |union all
         |select userId,blogId,grade,rk from visitUsers) a
         |)
         |where rk2<=10
         |group by blogId
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_pintuan_user_invitation_di")

    spark.stop()

  }

}
