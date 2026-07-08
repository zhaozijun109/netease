package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FlowControlEffectByTaskUser {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Rec Distribute Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneYearAgo = DateTime.parse(date).minusYears(1).toString("yyyy-MM-dd")

    val sql_blog_index =
      s"""
         |select  aa.flowTaskId, aa.flowTaskType,
         |        aa.userId,blogNickName,
         |        nvl(category_r1,'无') as level1Category,nvl(level,'无')  as creatorLevel,
         |        ljfans as fansAcc,hdfans as hdFansAcc,
         |        if(authenticateNames is  not null, 1, 0) darenFlag,
         |        exposurePv,exposureUv,clickPv,clickUv,realPlayPv,realPlayUv,hotPv,hotUv,
         |        pvClickRate,uvClickRate,realPlayRate,
         |        hdUv, browseUv,
         |        d.supportPostCount,
         |        e.sensingUserCount,
         |        e.sensingItemCount
         |from (
         |  select projectId as flowTaskId, 0 as flowTaskType, userId
         |  from lofter_db_dump.ods_db_dispatch_project_user_post_nd
         |  where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |  group by projectId,userId
         |
         |  union all
         |
         |  select supportId as flowTaskId, 1 as flowTaskType, blogId as userId
         |  from lofter_db_dump.ods_db_dispatch_site_support_user_nd
         |  where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |  group by supportId, blogId
         |) aa
         |left join (
         |  select userId, element_at(post_top_categories,1) as category_r1, level
         |  from lofter.dws_par_creator_dd
         |  where dt = '$date'
         |) a on aa.userId = a.userId
         |left join (
         |  select userid,auth_domain_names as authenticatenames,blogNickName
         |  from lofter.dws_par_user_base_dd
         |  where dt = '$date'
         |) b on aa.userId=b.userid
         |left join (
         |    select userId as blogid, fans_std as ljfans, hd_fans_1y as hdfans
         |    from lofter.dws_par_creator_level_scoring_dd
         |    where dt = '$date'
         |  ) d on aa.userId=d.blogid
         |
         |left join (
         |     select flowTaskId, flowTaskType, userId, sum(exposurePv) exposurePv, sum(exposureUv) exposureUv,
         |       sum(clickPv) clickPv,sum(clickUv) clickUv,
         |       sum(realBrowsePv) realPlayPv, sum(realPlayUv) realPlayUv,
         |       sum(posHotPv) hotPv, sum(posHotUv) hotUv,
         |       sum(clickPv)/sum(exposurePv) as pvClickRate,
         |       sum(clickUv)/sum(exposureUv) as uvClickRate,
         |       sum(realBrowsePv)/sum(exposurePv) as realPlayRate,
         |       sum(browseUv) browseUv,
         |       sum(hdUv) hdUv
         |    from (
         |      select a1.flowTaskId, a1.flowTaskType, a1.userId, a1.postId,exposurePv,exposureUv,clickPv,clickUv,realPlayPv,realPlayUv,posHotPv,posHotUv,browseUv,hdUv,
         |             realBrowsePv,realBrowseUv
         |      from (
         |           select flowTaskId, flowTaskType, userId, postId
         |           from lofter.dwd_dstr_flow_task_post_dd
         |           where dt = '$date' and
         |               from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |           group by flowTaskId, flowTaskType, userId, postId
         |      ) a1 join (
         |          select  dt,postid,exposurePv, exposureUv,clickPv,clickUv,realPlayPv,realPlayUv,posHotPv,posHotUv,browseUv,hdUv,
         |                  realBrowsePv,realBrowseUv
         |          from lofter.dws_post_base_stats_dd
         |          where dt='$date'
         |      ) a2 on a1.postId = a2.postId
         |  ) a
         |  group by flowTaskId, flowTaskType, userId
         |) c on aa.flowTaskId = c.flowTaskId and aa.flowTaskType = c.flowTaskType and aa.userId = c.userId
         |left join (
         |  select flowTaskId, flowTaskType, postBlogId as userId, count(distinct postId) as supportPostCount
         |  from lofter.dwd_dstr_flow_task_action_di
         |  where dt >='$oneYearAgo' and dt <= '$date'
         |  group by flowTaskId, flowTaskType, postBlogId
         |) d on aa.flowTaskId = d.flowTaskId and aa.flowTaskType = d.flowTaskType and aa.userId = d.userId
         |left join (
         |  select flowTaskId, flowTaskType, blogId as userId,
         |         count(distinct blogId) as sensingUserCount,
         |         count(distinct postId) as sensingItemCount
         |  from lofter_dm.ads_rec_dis_post_push_di
         |  where dt <= '$date'
         |  group by flowTaskId, flowTaskType, blogId
         |) e on aa.flowTaskId = e.flowTaskId and aa.flowTaskType = e.flowTaskType and aa.userId = e.userId
         |""".stripMargin

    spark.sql(sql_blog_index)
      .repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_task_user_acc_di")

    spark.stop()
  }
}
