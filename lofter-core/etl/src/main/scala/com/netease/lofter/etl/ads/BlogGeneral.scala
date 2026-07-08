package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object BlogGeneral {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val weekAgo = DateTime.parse(date).minusDays(7).toString("yyyy-MM-dd")

    val sql_hot_ops =
      s"""
         |select blogId,
         |       max(case when rk=1 then ip else null end ) hotIp1,
         |       max(case when rk=1 then hotCount else 0 end ) hotIp1Count,
         |       max(case when rk=2 then ip else null end ) hotIp2,
         |       max(case when rk=2 then hotCount else 0 end ) hotIp2Count,
         |       max(case when rk=3 then ip else null end ) hotIp3,
         |       max(case when rk=3 then hotCount else 0 end ) hotIp3Count,
         |       max(case when rk=4 then ip else null end ) hotIp4,
         |       max(case when rk=4 then hotCount else 0 end ) hotIp4Count,
         |       max(case when rk=5 then ip else null end ) hotIp5,
         |       max(case when rk=5 then hotCount else 0 end ) hotIp5Count
         |from (
         |    select blogId,ip,hotCount,
         |           row_number() over (partition by blogId order by hotCount desc) as rk
         |    from (
         |        select blogId, ip, count(1) as hotCount
         |        from lofter.dwd_post_hot_di
         |        where dt = '$date'
         |        group by blogId, ip
         |    ) b
         |) c where rk <= 5
         |group by blogId
         |""".stripMargin

    // pay attention to use union all not union
    val sql_new_follow_hotOp =
      s"""
         |select a.blogId,
         |       count(distinct b.userId) as newFollowHotOpUserCount,
         |       sum(hot_pv) as newFollowHotOpHot
         |from (
         |    select userId, blogId
         |    from lofter.dwd_blog_follow_di
         |    where dt = '$date'
         |) a
         |join (
         |    select userId, blogId, count(1) hot_pv
         |    from lofter.dwd_post_hot_di
         |    where dt = '$date'
         |    group by 1, 2
         |) b on a.blogId = b.blogId and a.userId = b.userId
         |join lofter.dim_user c on a.userId = c.id
         |where c.createDate > '$weekAgo' and c.createDate <= '$date'
         |group by a.blogId
         |""".stripMargin

    spark.sql(sql_hot_ops).createOrReplaceTempView("hotOps")
    spark.sql(sql_new_follow_hotOp).createOrReplaceTempView("followHotOps")

    //success
    val sql_behavior =
      s"""
         |select a.id as blogId,followCount,newFollowCount,commendCount,commendUserCount,
         |        praiseCount, praiseCount as praiseUserCount,
         |        reproduceCount, reproduceCount as reproduceUserCount,
         |        recommendCount, recommendCount as recommendUserCount,
         |        publishCount, subscribeCount, subscribeCount as subscribeUserCount,
         |        newHot, hot,
         |        newFollowHotOpUserCount, newFollowHotOpHot,
         |        hotIp1, hotIp1Count, hotIp2, hotIp2Count, hotIp3, hotIp3Count, hotIp4, hotIp4Count, hotIp5, hotIp5Count
         |from lofter.dim_blog a
         |left join(
         |    select m.blogId, count(1) as followCount,
         |        count(if(u.createDate > '$weekAgo' and u.createDate <= '$date', userId, null)) as newFollowCount,
         |        count(distinct if(u.createDate > '$weekAgo' and u.createDate <= '$date', userId, null)) as newFollowUsers
         |    from lofter.dwd_blog_follow_di m
         |         left join lofter.dim_user u on m.userId = u.id
         |    where dt = '$date'
         |    group by m.blogId
         |) b on a.id = b.blogId
         |left join (
         |    select m.blogId, count(1) as hot,
         |         count(if(u.createDate > '$weekAgo' and u.createDate <= '$date', userId, null)) as newHot,
         |         sum(if(opType = 'praise', 1, 0)) as praiseCount,
         |         sum(if(opType = 'reproduce', 1, 0)) as reproduceCount,
         |         sum(if(opType = 'recommend', 1, 0)) as recommendCount,
         |         sum(if(opType = 'subscribe', 1, 0)) as subscribeCount
         |    from lofter.dwd_post_hot_di m
         |         left join lofter.dim_user u on m.userId = u.id
         |    where dt = '$date'
         |    group by m.blogId
         |) c on a.id = c.blogId
         |left join (
         |    select blogId, count(1) as commendCount,
         |           count(distinct userId) as commendUserCount
         |    from lofter.dwd_post_response_di
         |    where dt = '$date'
         |    group by blogId
         |) d on a.id = d.blogId
         |left join (
         |    select userId as blogId, count(1) as publishCount
         |    from lofter.dwd_post_publish_di
         |    where dt = '$date'
         |    group by 1
         |) g on a.id = g.blogId
         |left join followHotOps i on a.id = i.blogId
         |left join hotOps j on a.id = j.blogId
         |where b.followCount > 0 or c.hot > 0 or d.commendCount > 0 or g.publishCount > 0 or i.newFollowHotOpHot > 0 or j.blogId is not null
         |""".stripMargin

    spark.sql(sql_behavior).createOrReplaceTempView("tableBehavior")

    val sql_res =
      s"""
         |select a.id as blogId, a.blogName, a.blogNickName, a.isAuthenticated,
         |      nvl(followCount,0) as followCount,nvl(commendCount,0) as commendCount,nvl(commendUserCount,0) as commendUserCount,nvl(praiseCount,0) as praiseCount,nvl(praiseUserCount,0) as praiseUserCount,
         |      nvl(reproduceCount,0) as reproduceCount,nvl(reproduceUserCount,0) as reproduceUserCount,nvl(recommendCount,0) as recommendCount,nvl(recommendUserCount,0) as recommendUserCount,
         |      nvl(subscribeCount,0) as subscribeCount,nvl(subscribeUserCount,0) as subscribeUserCount,nvl(publishCount,0) as publishCount,
         |      nvl(newFollowCount,0) as newFollowCount,nvl(newHot,0) as newHot,nvl(hot,0) as hot,nvl(newFollowHotOpUserCount,0) as newFollowHotOpUserCount,nvl(newFollowHotOpHot,0) as newFollowHotOpHot,
         |      a.authDomainNames,
         |      hotIp1, nvl(hotIp1Count,0) as hotIp1Count, hotIp2, nvl(hotIp2Count,0) as hotIp2Count, hotIp3, nvl(hotIp3Count,0) as hotIp3Count,
         |      hotIp4, nvl(hotIp4Count,0) as hotIp4Count, hotIp5, nvl(hotIp5Count,0) as hotIp5Count
         |from lofter.dim_blog a
         |join tableBehavior B on a.id = b.blogId
         |""".stripMargin

    spark.sql(sql_res).repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_blog_general_di")

    spark.close()
  }


}
