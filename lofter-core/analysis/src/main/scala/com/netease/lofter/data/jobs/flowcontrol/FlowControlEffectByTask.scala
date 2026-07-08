package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FlowControlEffectByTask {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Rec Distribute Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneYearAgo = DateTime.parse(date).minusYears(1).toString("yyyy-MM-dd")

    val sql_task_user =
      s"""
         |select a.flowTaskId, a.flowTaskType, a.userId, b.flowTaskName, b.startTime, b.endTime, b.status
         |from (
         |         select projectId as flowTaskId, 0 as flowTaskType, b.userId
         |         from lofter_db_dump.ods_db_dispatch_project_post_nd a
         |              join lofter.dim_post b on a.postId = b.id
         |
         |          union all
         |
         |          select projectId as flowTaskId, 0 as flowTaskType, userId
         |          from lofter_db_dump.ods_db_dispatch_project_user_post_nd
         |          where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |          group by projectId,userId
         |
         |          union all
         |
         |          select supportId as flowTaskId, 1 as flowTaskType, blogId as userId
         |          from lofter_db_dump.ods_db_dispatch_site_support_user_nd
         |          where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |          group by supportId, blogId
         |) a join (
         |    select id as flowTaskId, 0 as flowTaskType, name as flowTaskName, startTime, endTime, status
         |    from lofter_db_dump.ods_db_dispatch_project_nd
         |
         |    union all
         |
         |    select id as flowTaskId, 1 as flowTaskType, name as flowTaskName, startTime, endTime, status
         |    from lofter_db_dump.ods_db_dispatch_site_support_nd
         |) b on a.flowTaskId = b.flowTaskId and a.flowTaskType = b.flowTaskType
         |""".stripMargin

    spark.sql(sql_task_user).cache().createOrReplaceTempView("task_users")

    val sql_blog_index =
      s"""
         |select x.*, y.supportPostCount, y.supportUserCount, k.levelUser, z.darenUser, z.arpuAddFans,
         |       v.sensingUserCount, v.sensingItemCount
         |from (
         |   select a.flowTaskId, a.flowTaskType,
         |       sum(exposurePv) exposurePv, sum(exposureUv) exposureUv,
         |       sum(clickPv) clickPv,sum(clickUv) clickUv,
         |       sum(realBrowsePv) realPlayPv, sum(realPlayUv) realPlayUv,
         |       sum(posHotPv) hotPv, sum(posHotUv) hotUv,
         |       sum(browsePv) as browsePv,
         |       sum(browseUv) as browseUv,
         |       sum(clickPv)/sum(exposurePv) as pvClickRate,
         |       sum(clickUv)/sum(exposureUv) as uvClickRate,
         |       sum(realBrowsePv)/sum(exposurePv) as realPlayRate
         |     from (
         |         select a1.flowTaskId, a1.flowTaskType, a1.postId,exposurePv,exposureUv,clickPv,clickUv,browsePv,browseUv,realPlayPv,realPlayUv,posHotPv,posHotUv,
         |                realBrowsePv,realBrowseUv
         |         from (
         |           select flowTaskId, flowTaskType, postId
         |           from lofter.dwd_dstr_flow_task_post_dd
         |           where dt = '$date' and
         |               from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$oneYearAgo' and '$date'
         |         ) a1 join (
         |            select postId,exposurePv, exposureUv,clickPv,clickUv,browsePv,browseUv,realPlayPv,realPlayUv,posHotPv,posHotUv,
         |                   realBrowsePv,realBrowseUv
         |            from lofter.dws_post_base_stats_dd
         |            where dt='$date'
         |         ) a2 on a1.postId = a2.postId
         |     ) a
         |     group by flowTaskId, flowTaskType
         |) x
         |  left join (
         |    select flowTaskId, flowTaskType, count(distinct postId) as supportPostCount, count(distinct postBlogId) supportUserCount
         |    from lofter.dwd_dstr_flow_task_action_di
         |    where dt >='$oneYearAgo' and dt <= '$date'
         |    group by flowTaskId, flowTaskType
         |  ) y on x.flowTaskId = y.flowTaskId and x.flowTaskType = y.flowTaskType
         |
         |  left join (
         |      select a.flowTaskId, a.flowTaskType,
         |             count(distinct if(b.isAuthenticated, b.id, null)) as darenUser,
         |             sum(c.fans) / count(distinct a.userId) as arpuAddFans
         |      from task_users a left join lofter.dim_blog b on a.userId = b.id
         |      left join (
         |          select a.flowTaskId, a.flowTaskType, a.userId, count(distinct b.userId) as fans
         |          from task_users a
         |               join (
         |                   select blogId, userId, follow_time
         |                   from lofter.dwd_blog_follow_di
         |                   where dt >= '$oneYearAgo' and dt <= '$date' and blogId not in (493676441,502552131,1956197847)
         |               ) b on a.userId = b.blogId
         |          where b.follow_time > a.startTime
         |          group by a.flowTaskId, a.flowTaskType, a.userId
         |      ) c on a.flowTaskId = c.flowTaskId and a.flowTaskType = c.flowTaskType and a.userId = c.userId
         |      group by a.flowTaskId, a.flowTaskType
         |  ) z on x.flowTaskId = z.flowTaskId and x.flowTaskType = z.flowTaskType
         |
         |  left join (
         |    select flowTaskId, flowTaskType,
         |           to_json(named_struct(
         |              'S', sum(if(level = 'S', levelUv, 0)),
         |              'A', sum(if(level = 'A', levelUv, 0)),
         |              'B', sum(if(level = 'B', levelUv, 0)),
         |              'C', sum(if(level = 'C', levelUv, 0)),
         |              'D', sum(if(level = 'D', levelUv, 0)),
         |              'D*', sum(if(level = 'D*', levelUv, 0))
         |           )) as levelUser
         |    from (
         |      select a.flowTaskId, a.flowTaskType, level, count(distinct a.userId) levelUv
         |      from task_users a join (
         |         select userId, level from lofter.dws_par_creator_dd
         |         where dt = '$date'
         |      ) b on a.userId = b.userId
         |      group by a.flowTaskId, a.flowTaskType, level
         |    ) t
         |    group by flowTaskId, flowTaskType
         |  ) k on x.flowTaskId = k.flowTaskId and x.flowTaskType = k.flowTaskType
         |  left join (
         |    select flowTaskId, flowTaskType,
         |           count(distinct blogId) as sensingUserCount,
         |           count(distinct postId) as sensingItemCount
         |    from lofter_dm.ads_rec_dis_post_push_di
         |    where dt <= '$date'
         |    group by flowTaskId, flowTaskType
         |  ) v on x.flowTaskId = v.flowTaskId and x.flowTaskType = v.flowTaskType
         |""".stripMargin

    spark.sql(sql_blog_index)
      .repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_task_acc_di")

    spark.stop()
  }
}
