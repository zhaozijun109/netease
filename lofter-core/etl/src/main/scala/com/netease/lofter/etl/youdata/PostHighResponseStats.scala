package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostHighResponseStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter High Response Post Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    if (DateTime.parse(date).dayOfWeek().get() == 5) highResponseStats(spark, date) else spark.close()

    def highResponseStats(spark: SparkSession, date: String): Unit = {

      val sql_high_response_post =
        s"""
           |select tag,blogname,blogid,a.postid,a.comments,a.topcommenthot
           |from
           |(select postid,count(id) as comments,max(commenthot) as topcommenthot
           |from lofter_db_dump.ods_db_post_response_nd
           |where  from_unixtime(cast(publishtime/1000 as bigint),'yyyy-MM-dd')>='2020-01-01'
           |group by postid
           |having count(id)>100 or max(commenthot)>1000
           |) a
           |inner join
           |(select id postid,userid,blogid,blogname,tag
           |from lofter.dim_post
           |lateral view explode(tags) t1 as tag
           |where ispublished=true and isforbidden=false and iscitedpost=false
           |and publishdate>='2020-01-01'
           |and allowview=0
           |and valid in (0,12)
           |and isactivityautopost=0 and isimported=0 and ismoved=0
           |) b
           |on a.postid=b.postid
           |inner join
           |(
           |select postid,recomstatus
           |from lofter_db_dump.ods_db_recommend_review_post_nd
           |where recomstatus=1
           |group by postid,recomstatus
           |) c
           |on a.postid=c.postid
           |inner join
           |(
           |select name from lofter_db_dump.ods_db_user_guide_ip_nd where status = 0 group by name
           |) d
           |on b.tag=d.name
           |""".stripMargin

      spark.sql(sql_high_response_post).createOrReplaceTempView("t1")

      val sql_result =
        s"""
           |select postId,ip,post_url,type as stat_type,comments as comment_num,topcommenthot as top_comment_hot
           |from
           |(select postid,tag ip,concat(blogname,'.lofter.com/post/',conv(blogid, 10, 16),'_',conv(postid, 10, 16)) as post_url,
           |        comments,topcommenthot,type,row_number()over(partition by postid order by type ) rkk
           |from
           |(
           |    select tag,blogname,blogid,postid,comments,topcommenthot,1 as type
           |    from
           |    (
           |    select tag,blogname,blogid,postid,comments,topcommenthot,row_number()over(partition by tag order by comments desc) rk
           |    from t1
           |    )
           |    where rk<=100
           |    union all
           |    select tag,blogname,blogid,postid,comments,topcommenthot,2 as type
           |    from
           |    (
           |    select tag,blogname,blogid,postid,comments,topcommenthot,row_number()over(partition by tag order by topcommenthot desc) rk
           |    from t1
           |    )
           |    where rk<=100
           |)
           |)
           |where rkk=1
       """.stripMargin

      spark.sql(sql_result)
        .repartition(1)
        .withColumn("dt", lit(date))
        .write
        .mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_post_high_comment_dd")

    }
  }
}
