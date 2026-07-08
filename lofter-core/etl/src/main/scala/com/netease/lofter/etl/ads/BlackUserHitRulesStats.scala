package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object BlackUserHitRulesStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter Black User Which Hit the rules stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)
    val day1Ago = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val day30Ago = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")
    val day90Ago = DateTime.parse(date).minusDays(89).toString("yyyy-MM-dd")

    val sql_release_user =
      s"""
         |select blogId as userId ,'MCN知识公路' as type
         |from lofter_db_dump.ods_db_media_post_import_nd
         |where platformtype in (1,7)
         |group by blogid
         |union all
         |select id as userId ,'PGC文学博客引入' as type
         |from lofter.dim_user  where sourceType='PGC'
         |union all
         |select userid ,'商务引入签约' as type
         |from lofter_db_dump.ods_db_cmb_business_introduction_nd
         |where status=0 and level is not null
         |group by userid
         |union all
         |select userId , '流失60日以上' as type
         |from lofter.dws_par_creator_dd
         |where dt='$day1Ago' and post_first_date is not null and datediff('$date', post_last_date)>=60
         |group by userId
         |union all
         |select  userid , '白名单及达人' as type
         |from lofter.dws_par_user_base_dd
         |where dt='$day1Ago' and privilegelevel in (0,1,2)
         |group by userid
         |union all
         |select userId ,'创作者等级SAB' as type
         |from lofter.dws_par_creator_dd
         |where dt='$day1Ago' and level in ('S','A','B')
         |group by userId
         |union all
         |select userid,'人工白名单导入'  as type
         |from lofter.zq_lofter_recommend_white_user
         |group by userid
         |""".stripMargin

    spark.sql(sql_release_user).cache().createOrReplaceTempView("releaseUser")

    val sql_model_rule =
      s"""
         |select a.blogId from
         |(select  a.blogid
         |from
         |(select  a.blogid, count(distinct a.postid) as allpost,
         |     count(distinct  case when a.recomstatus=-1 then postid else null end) as unpasspost
         |from
         |     (select postid,recomstatus,reviewaccount,blogid
         |     from lofter_db_dump.ods_db_recommend_review_post_nd
         |     where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day90Ago' and '$date'
         |     group by blogid,postid,recomstatus,reviewaccount) a
         |    join
         |     (select reviewaccount from  lofter.zq_lofter_recommendwork_name_zeppelin group by reviewaccount) b
         |    on a.reviewaccount=b.reviewaccount
         |group by a.blogid) a
         |left join
         | (select userid from releaseUser group by userid) b
         |on a.blogid=b.userid
         | where ((a.allpost>=5 and unpasspost/allpost=1)
         | or (a.allpost>=10 and unpasspost/allpost >=0.95)
         | or (a.allpost>=50 and unpasspost/allpost >=0.85)
         | or (a.allpost>=100 and unpasspost/allpost >=0.8)
         | or (a.allpost>=300 and unpasspost/allpost >=0.75)
         | or (a.allpost>=500 and unpasspost/allpost >=0.70)) and b.userid is null
         | group by  a.blogid
         |
         | union
         |select  a.blogid
         |from
         |(select  a.blogid, count(distinct a.postid) as allpost,
         |     count(distinct case when a.recomstatus=-1 then postid else null end) as unpasspost
         |from
         |    (select postid,recomstatus,reviewaccount,blogid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day30Ago' and '$date'
         |    group by blogid,postid,recomstatus,reviewaccount) a
         |    join
         |    (select reviewaccount from  lofter.zq_lofter_recommendwork_name_zeppelin group by reviewaccount) b
         |    on a.reviewaccount=b.reviewaccount
         |group by a.blogid) a
         |
         |join (
         |    select userid from releaseUser where type in ('首发30日内','流失60日以上') group by userid
         |) b on a.blogid=b.userid
         |left join (
         |    select userid from releaseUser where type in ('人工白名单导入') group by userid
         |) c on a.blogId = c.userid
         |where
         |  (a.allpost>=50 and unpasspost/allpost >=0.9) and c.userid is null
         |  group by  a.blogid
         |
         |union
         |select a.blogid from
         |    (select a.blogid from
         |        (select a.blogid,days,count(distinct case when a.allpost>=2 and a.rate=1.0 then a.day else null end ) as unpassdays
         |         from
         |         (select  a.blogid,a.day,a.allpost,(a.unpasspost/a.allpost) as rate,datediff('$date', zhuceday) as days
         |         from
         |            (select a.day, count(distinct a.postid) as allpost,a.blogid ,count(distinct  case when a.recomstatus=-1 then postid else null end ) as unpasspost
         |              from
         |              (select postid,recomstatus,reviewaccount,blogid, from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as day
         |               from lofter_db_dump.ods_db_recommend_review_post_nd
         |               where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day30Ago' and '$date'
         |               group by blogid,postid,recomstatus,reviewaccount,blogid, from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') ) a
         |              join
         |               (select reviewaccount from  lofter.zq_lofter_recommendwork_name_zeppelin group by reviewaccount) b
         |               on a.reviewaccount=b.reviewaccount
         |               group by a.blogid,a.day
         |             ) a
         |            join
         |            (select id as userid, createdate as zhuceday
         |            from lofter.dim_user
         |            where createdate>='$day30Ago'
         |            ) c
         |            on a.blogid=c.userid) a
         |        group by  a.blogid,days) a
         |      where a.unpassdays/a.days>=0.8 and a.days>=2
         |    group by a.blogid ) a
         |    left join
         |    (select userid userid from releaseUser group by userid) b
         |    on a.blogid=b.userid
         |    where  b.userid is null
         |    group by  a.blogid
         |) a group by  blogid
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql(sql_release_user)
      .repartition(5)
      .withColumn("dt",lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter.dwd_user_white_list_dd")

    spark.sql(sql_model_rule)
      .repartition(5)
      .withColumn("dt",lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter.dwd_user_black_hit_rule_di")

    spark.close()
  }
}
