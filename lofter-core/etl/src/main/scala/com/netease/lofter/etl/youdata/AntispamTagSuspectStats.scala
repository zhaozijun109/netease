package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object AntispamTagSuspectStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Antispam Tag Suspect Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val threeMonthAgo = DateTime.parse(date).minusMonths(3).toString("yyyy-MM-dd")
    val threeDaysAgo = DateTime.parse(date).minusDays(3).toString("yyyy-MM-dd")

    val sql_post_tag_info =
      s"""
         |select a.postid,b.userid,b.tag,b.publishtime,blog_status,phones
         |from 
         |(select postid from lofter.dwd_post_length_dd where dt='$date' and words_count<=50 and photo_count<=1) a
         |
         |join
         |(select id,userid,publishtime,tag from lofter.dim_post
         |  LATERAL VIEW explode(tags) t2 AS tag
         | where publishDate between '$threeDaysAgo' and '$date' and contenttype in('文字','图片')
         |) b
         |on a.postid=b.id
         |
         |left join
         |(select userid,phones,blog_status from lofter.dws_par_user_base_dd where dt='$date'and privilegeLevel=3) d
         |on b.userid=d.userid
         |
         |left join
         |(select userid from lofter.dws_par_user_base_dd where dt='$date' and (privilegelevel in(0,1,2)
         |    or from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')<= '$threeMonthAgo')
         |union
         |select a.userid from
         |    (select blogid as userid, count(distinct userid) as num from lofter_db_dump.ods_db_user_following_nd  group by blogid ) a
         |where a.num>=50
         |group by a.userid
         |) c
         |on b.userid=c.userid
         |where c.userid is null
         |group by a.postid,b.userid,b.tag,b.publishtime,blog_status,phones
         |""".stripMargin

    spark.sql(sql_post_tag_info).cache().createOrReplaceTempView("t1")

    val sql_result =
      s"""
         |select a.userId,a.tag,from_unixtime(cast(a.time1 / 1000 AS BIGINT), 'yyyy-MM-dd') as publishDate,
         |    blog_status,concat_ws(',',phones) as phones
         |from
         |(select a.postid,a.userid,a.tag,a.blog_status,a.phones,a.time1,count(distinct b.postid) as num
         |from
         |(select postid,userid,tag,blog_status,phones,publishtime as time1,(publishtime+3600000) as time2  from t1 ) a
         |join
         |(select postid,userid,tag,blog_status,phones,publishtime from t1 ) b
         |on a.userid=b.userid and a.tag=b.tag
         |where b.publishtime>a.time1 and b.publishtime<=a.time2
         |group by a.postid,a.userid,a.tag,a.blog_status,a.phones,a.time1 ) a
         |where a.num>=10
         |group  by  1,2,3,4,5
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_antispam_tag_suspect_di")

    spark.close()
  }

}
