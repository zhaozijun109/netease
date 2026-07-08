package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object HotContentAuditAndRecPoolStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot Content and Enter Rec Pool Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    val sql_post_info =
      s"""
         |select a.*, b.postId as recRevPostId,b.createTime,reviewTime,reasons,recomStatus,
         |      c.hotPv,d.fans
         |from
         |(
         |    select id postId,blogId,blogName,title,publishDate,publishTime
         |    from lofter.dim_post
         |    where publishDate between '$oneWeekAgo' and '$date'
         |)   a
         |left join
         |(
         |    select postId,createTime,reviewTime,reasons,recomStatus
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |)   b
         |on a.postId=b.postId
         |left join
         |(
         |    select postId,count(1) as hotPv
         |    from lofter_db_dump.ods_db_post_hot_nd
         |    where from_unixtime(cast(opTime/1000 AS BIGINT), 'yyyy-MM-dd') between '$oneWeekAgo' and '$date'
         |    group by postId
         |)   c
         |on a.postId = c.postId
         |
         |left join
         |(
         |    select blogId,count(distinct userId) as fans
         |    from lofter_db_dump.ods_db_user_following_nd
         |    where from_unixtime(cast(followtime/1000 as bigint),'yyyy-MM-dd')<='$date'
         |    group by blogId
         |)   d
         |on a.blogId=d.blogId
         |""".stripMargin

    spark.sql(sql_post_info).createOrReplaceTempView("postInfo")

    val sql_summary_index =
      s"""
         |insert overwrite table lofter_dm.ads_post_audit_and_rec_all_di partition(dt='$date')
         |select publishDate,
         |    count(postId) as postNum,
         |    count(case when recRevPostId is null then a.postId else null end) as notAuditPostNum,
         |    count(case when recRevPostId is not null and recomStatus!=1 then a.postId else null end) as notRecPostNum,
         |    count(case when recRevPostId is not null then a.postId else null end) as enterAuditPostNum,
         |    count(case when recRevPostId is not null and recomStatus=1 then a.postId else null end) as enterRecPostNum,
         |    count(case when hotPv>=1000 and  recRevPostId is null then a.postId else null end) as notAuditPostNumHot,
         |    count(case when hotPv>=1000 and  recRevPostId is not null and recomStatus!=1 then a.postId else null end) as notRecPostNumHot,
         |    count(case when fans>=3000 and  recRevPostId is null then a.postId else null end) as notAuditPostNumFans,
         |    count(case when fans>=3000 and  recRevPostId is not null and recomStatus!=1 then a.postId else null end) as notRecPostNumFans
         |from postInfo a
         |group by publishDate
         |""".stripMargin

    val sql_post_detail =
      s"""
         |insert overwrite table lofter_dm.ads_post_audit_and_rec_detail_di partition(dt='$date')
         |select postId,publishDate,title, blogName,
         |    from_unixtime(cast(createTime/1000 AS BIGINT), 'yyyy-MM-dd') as auditDate,
         |    from_unixtime(cast(reviewTime/1000 AS BIGINT), 'yyyy-MM-dd') as poolDate,
         |    concat(blogName,'.lofter.com/post/',conv(blogId, 10, 16),'_',conv(postId, 10, 16)) as url,
         |    reasons,
         |    case when hotPv>=1000 and  recRevPostId is null then 'hot1K未入库'
         |        when hotPv>=1000 and  recRevPostId is not null and recomStatus!=1 then 'hot1K未入池'
         |        when fans>=3000 and  recRevPostId is null then 'fans3K未入库'
         |        when fans>=3000 and  recRevPostId is not null and recomStatus!=1 then 'fans3K未入池'
         |        else '其他' end as judgeType,
         |     hotPv,
         |     fans,
         |     recomStatus as recStatus
         |from postInfo a
         |where (hotPv>=1000 and  recRevPostId is null) or (hotPv>=1000 and  recRevPostId is not null and recomStatus!=1)
         |    or (fans>=3000 and  recRevPostId is null) or (fans>=3000 and  recRevPostId is not null and recomStatus!=1)
         |""".stripMargin

    spark.sql(sql_summary_index)
    spark.sql(sql_post_detail)

    spark.close()
  }

}