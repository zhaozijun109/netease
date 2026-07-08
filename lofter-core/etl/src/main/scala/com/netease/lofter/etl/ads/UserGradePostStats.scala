package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserGradePostStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter User Grade Post stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)

    val sql_result =
      s"""
        |insert overwrite table lofter_dm.ads_user_grade_post_stat_di partition(dt='$date')
        |select a.grade,count(distinct a.blogId) as gradeUv,
        |       count(distinct b.accountId) as activeUv,
        |       count(distinct c.blogId) as postUv,
        |       count(distinct case when passPostNum>0 then c.blogId else null end) as passPostUv,
        |       sum(c.postNum) as postNum,
        |       sum(c.passPostNum) as passPostNum
        |from
        |-- 获取四类用户的等级
        |(select dt, a.blogId,case when b.blogId is not null then 3 else a.grade end as grade
        |from
        |    (select dt,blogId,grade from lofter.dws_user_security_level_di where dt='$date') a
        |left join
        |    (select blogId from lofter_dm.ads_high_security_user_di where dt='$date') b
        |on a.blogId=b.blogId
        |) a
        |
        |left join
        |-- 活跃用户数
        |(select accountid  from lofter.dwd_evt_user_login_di where dt='$date' group by accountid ) b
        |on a.blogid=b.accountid
        |
        |left join
        |-- 发文UV，发文数，发文通过UV，发文通过数
        |(select blogid,
        |    count(distinct id) as postnum,
        |    count(distinct case when valid=0 then id else null end) as passPostNum
        |from  lofter.dim_post
        |where publishdate='$date' and allowview in (0,50)
        |group by blogid ) c
        |on a.blogid=c.blogid
        |group by a.grade
        |""".stripMargin

    spark.sql(sql_result)

    spark.close()
  }
}
