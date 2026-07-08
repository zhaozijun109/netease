package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.net.URI

object PostJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.sql("create temporary function resolve_tag_ip as 'com.netease.wm.udf.ResolveTagIp' ")

    spark.read.parquet(s"/user/da_lofter/db_dump/Post/$date").createOrReplaceTempView("t1")
    spark.read.parquet(s"/user/da_lofter/warehouse/dim_blog/dt=$date").createOrReplaceTempView("t2")
    spark.read.parquet(s"/user/da_lofter/warehouse/dim_domain/dt=$date").createOrReplaceTempView("t3")
    spark.read.parquet(s"/user/da_lofter/warehouse/dim_user/dt=$date").createOrReplaceTempView("t4")
    spark.read.parquet(s"/user/da_lofter/db_dump/Ask_AnswerPost/$date").createOrReplaceTempView("ods_db_ask_answer_post_nd")
    spark.read.parquet(s"/user/da_lofter/db_dump/Ask_Question/$date").createOrReplaceTempView("ods_db_ask_question_nd")
    spark.read.parquet(s"/user/da_lofter/db_dump/Media_PostImport/$date").createOrReplaceTempView("ods_db_media_post_import")

    //extract info from AdminPubData
    val sql_1 =
      s"""
         |select id, userId, blogId, title, publishTime,
         |       publishDate, tags, isPublished, isForbidden, contentType,
         |       isCitedPost, citedParentPostId, moveFrom, allowView, valid,
         |       userPostIndex
         |from (
         |    select  id, publisherUserId as userId,blogId,title,publishTime,
         |        from_unixtime(cast(publishTime/1000 as bigint),'yyyy-MM-dd') as publishDate,
         |        case when length(trim(Tag))>0 then split(lower(trim(Tag)),",") else null end as tags,
         |        case when IsPublished is not null and IsPublished!=0 then true else false end as isPublished,
         |        case when valid is not null and valid=25 then true  else false end as isForbidden,
         |        case when Type =1 then "文字"
         |             when Type =2 then "图片"
         |             when Type =3 then "音乐"
         |             when Type =4 then "视频"
         |             when Type =5 then "问答"
         |             when Type =6 then "长文章"
         |             else "未知" end  as contentType,
         |         case when CiteParentPostId is not null and CiteParentPostId>0 then true  else false end as isCitedPost,
         |         CiteParentPostId as citedParentPostId,
         |         moveFrom,allowView,valid,
         |         cast(row_number() over (partition by publisherUserId order by publishTime) as bigint) - 1 as userPostIndex,
         |         row_number() over (partition by id order by publishTime desc) as rk
         |    from t1
         |) t
         |where rk = 1
         |""".stripMargin

    spark.sql(sql_1).createOrReplaceTempView("post_org")

    val sql_user_create =
      s"""
         |select A.*,B.createFrom as userCreateFrom,B.createDate as userCreateDate from
         |      post_org A
         |join t4 B on A.userId=B.id
         |""".stripMargin

    // pay attention to the lateral view explode when tags is null, this record will be ignored
    val sql_tags2_domainIds =
      s"""
         |select A.id,collect_set(B.id) as domainIds
         |from (
         |    select id,tagName from post_org lateral view explode(tags) ss as tagName
         |) A
         |left join (
         |    select id,tagName from t3 lateral view explode(tags) ss as tagName
         |) B on A.tagName = B.tagName
         |group by A.id
         |""".stripMargin

    val sql_blog =
      s"""
         |select A.id,A.blogId,blogName,blogNickName,isAuthenticated
         |from post_org A
         |join t2 B on A.blogId=B.id
         |""".stripMargin

    val sql_bookstore =
      s"""
         |select b.postId
         |from lofter_db_dump.ods_db_pfb_content_nd a
         |     join lofter_db_dump.ods_db_pfb_content_related_post_nd b on a.id = b.contentId
         |     join (
         |             select  cast(trim(blogId) as bigint) as blogId
         |             from lofter_db_dump.ods_db_admin_pub_data_nd
         |                  lateral view explode(split(content, ',')) as blogId
         |             where name = 'book_store_blogIds' and cast(trim(blogId) as bigint) > 0
         |             group by 1
         |     ) c on b.blogId = c.blogId
         |group by b.postId
         |""".stripMargin

    spark.sql(sql_tags2_domainIds).createOrReplaceTempView("tags_domain")
    spark.sql(sql_blog).createOrReplaceTempView("blog")
    spark.sql(sql_user_create).createOrReplaceTempView("post_user")
    spark.sql(sql_bookstore).createOrReplaceTempView("bookstore")

    val sql_res =
      s"""
         |select A.*,B.domainIds as domains ,C.blogName,C.blogNickName,C.isAuthenticated isBlogAuthenticated,
         |       if(D.postId is null, 0, 1) as isChat,
         |       if(A.moveFrom in ("blog","lofternetease","blog163like","loftmove","BLOGPOST","bbs","photo-pp","163_news","instagram_mirror","weibo_sync","news","pp","netease_photo" )
         |           or A.moveFrom like "%blog%" or  A.moveFrom like "%move%", 1, 0) as isMoved,
         |       if(E.postId is null, 0, 1) as isImported,
         |       if(F.moveFrom is null, 0, 1) as isActivityAutoPost,
         |       resolve_tag_ip(tags) as ips,
         |       concat(C.blogName,'.lofter.com/post/',conv(A.blogId, 10, 16),'_',conv(A.id, 10, 16)) as url,
         |       if(G.postId is null, 0, 1) as is_book_store,
         |       H.recomstatus as recomStatus,
         |   case when E.platformtype=0 then '站内'
         |        when E.platformtype=1 then '知识公路'
         |        when E.platformtype=2 then '云音乐'
         |        when E.platformtype=3 then '抖音'
         |        when E.platformtype=4 then '快手'
         |        when E.platformtype=5 then 'youtube'
         |        when E.platformtype=6 then '微博'
         |        when E.platformtype=7 then 'MCN机构'
         |        when E.platformtype is not null then '未定义'
         |        else '站内' end as ImportPlatformType
         |from post_user A
         |  join blog C on A.id=C.id
         |  left join tags_domain B on A.id= B.id
         |  left join (
         |        select x.id as postId
         |        from ods_db_ask_answer_post_nd x
         |             join ods_db_ask_question_nd y on x.questionId = y.id
         |        where y.type = 3
         |        group by x.id
         |  ) D on A.id = D.postId
         |  left join (
         |    select *
         |    from ods_db_media_post_import
         |    where source = 0
         |  ) E on A.id = E.postId
         |  left join (
         |    select moveFrom
         |    from lofter_db_dump.ods_db_risk_gcc_priority_nd
         |    where status = 0 and length(moveFrom) > 0
         |    group by moveFrom
         |  ) F on A.moveFrom = F.moveFrom
         |  left join bookstore g on A.id = G.postId
         |  left join lofter_db_dump.ods_db_recommend_review_post_nd H on A.id = H.postId
         |""".stripMargin

    val outPath = s"$output/dt=$date"
    spark.sql(sql_res).write.mode(SaveMode.Overwrite).parquet(outPath)

    spark.sql(s"alter table lofter.dim_post set location '$outPath' ")
    spark.close()
  }

}
