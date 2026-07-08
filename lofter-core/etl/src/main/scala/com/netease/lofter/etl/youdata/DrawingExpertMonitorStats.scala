package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object DrawingExpertMonitorStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter Drawing Expert Monitor stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_result =
      s"""
        |insert overwrite table lofter_dm.ads_drawing_expert_monitor_di partition(dt='$date')
        |select a.userid,a.blogname,a.blognickname,a.url,a.createday,a.last_login_date,a.auth_flag,a.all_blog_fans,c.fans_30day,
        |       d.postnum,d.post_num_from_2022,d.lastpostday,e.level,f.exposurepv,f.poshotpv, x.exposurepv_30day,x.poshotpv_30day,
        |       e.lingyu_r1,f.real_browse_pv,g.post_num_1k
        |from
        |(select userid,blogname,blognickname,concat('https://',blogname,'.lofter.com') as url,
        |       from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as createday,
        |       all_blog_fans, from_unixtime(cast(last_login_time / 1000 AS BIGINT), 'yyyy-MM-dd') as last_login_date,
        |       case when auth_domain_names[0] is not null then '达人' else '非达人' end  as auth_flag
        |from  lofter.dws_par_user_base_dd
        |where dt='$date') a    -----用户基础信息
        |join
        |(select userid as blogid
        |from lofter_db_dump.ods_db_cmb_business_introduction_nd
        |where status=0 and level is not null
        |group by userid) b
        |on a.userid=b.blogid
        |left join
        |
        |(select blogid,count(distinct userid) as fans_30day
        |from lofter_db_dump.ods_db_user_following_nd
        |where from_unixtime(cast(followtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
        |group by blogid) c
        |on a.userid=c.blogid
        |left join
        |
        |(select blogid,count(id) as postnum ,
        |       count(case when publishdate >='2022-01-01' then id else null end  ) as post_num_from_2022,
        |       max(case when publishdate>='$date' then '$date' else publishdate end) as lastpostday  ---发文相关
        |from lofter.dim_post
        |where
        |contenttype!='问答'  and ispublished=true and valid=0 and allowview=0 and iscitedpost=false group by blogid ) d
        |on a.userid=d.blogid
        |
        |left  join
        |(select userId as blogid, level, element_at(post_top_domains,1) as lingyu_r1
        |from lofter.dws_par_creator_dd
        |where dt='$date'
        |) e
        |on a.userid=e.blogid
        |
        |left join
        |(select m.userId,
        |        sum(exposurepv) as exposurepv,
        |        sum(poshotpv) as poshotpv,
        |        sum(real_browse_pv) as real_browse_pv
        | from lofter.dim_post m
        | join (
        |    select postId, exposurepv, poshotpv, realBrowsePv as real_browse_pv
        |    from lofter.dws_post_base_stats_dd
        |    where dt = '$date'
        | ) n on m.id = n.postId
        | group by m.userId
        |) f
        |on a.userid=f.userid
        |left join (
        |  select userId, sum(exposurepv) as exposurepv_30day, sum(poshotpv) as poshotpv_30day
        |  from lofter.dws_post_base_stats_di
        |  where dt <= '$date' and dt >= '$oneMonthAgo'
        |  group by userId
        |) x on a.userid=x.userid
        |left join
        |(select blogId,count(case when favoritecount+reblogcount+sharecount+subscribecount>=1000 then postId else null end) as post_num_1k
        | from lofter_db_dump.ods_db_post_count_nd group by blogId) g
        |on a.userid=g.blogId
        |""".stripMargin

    spark.sql(sql_result)

    spark.close()
  }
}
