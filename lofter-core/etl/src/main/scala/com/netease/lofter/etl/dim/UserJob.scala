package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.read.parquet(s"/user/da_lofter/db_dump/Profile/$date").createOrReplaceTempView("t1")
    spark.read.parquet(s"/user/da_lofter/db_dump/anonymity_login/$date").createOrReplaceTempView("t2")

    val sql_blog_vip =
      s"""
         |select cast(userId as bigint) as userId
         |from lofter_db_dump.ods_db_admin_pub_data_nd LATERAL VIEW explode(split(content, ',')) t2 AS userId
         |where name in('book_store_blogIds','blogvip_ids')
         |group by cast(userId as bigint)
         |""".stripMargin

    val sql_user_pgc =
      s"""
         |select blogId from lofter_db_dump.ods_db_user_following_nd  where userid=1943463653 group by blogid
         |union
         |select b.blogId
         |from
         |(select id from lofter_db_dump.ods_db_pfb_content_nd  where providertype=1) a
         |join
         |(select contentid,type,postid,blogid  from lofter_db_dump.ods_db_pfb_content_related_post_nd where status=0) b
         |on a.id=b.contentid
         |group by b.blogId
         |""".stripMargin

    spark.sql(sql_blog_vip).cache().createOrReplaceTempView("blogVips")
    spark.sql(sql_user_pgc).createOrReplaceTempView("blogPgc")

    val sql_1 =
      s"""
         |select A.userId as id,ProfileCreateTime as createTime,Email as email,mainBlogId,ProfileCreateFrom as createFrom,
         |      createDate, case when B.userId is null then 0 else 1 end as isAnonymous,
         |      if(C.blogId is null, 0, 1) as isTest,
         |      if(D.userId is null, 0, 1) as isRobot,
         |      case when E.userId is not null then '官方账号'
         |           when F.blogId is not null  then 'PGC'
         |           else 'UGC' end as sourceType,
         |      if(email like '101#%' or email like '102#%', 1, 0) as is_miniprogram
         |from (
         |    select userId,ProfileCreateTime,from_unixtime(cast(ProfileCreateTime/1000 as bigint),'yyyy-MM-dd') as createDate,
         |           Email, mainBlogId,ProfileCreateFrom
         |    from t1
         |    where userId is not null
         |) A
         |left join (
         |    select userId from t2
         |  union
         |    select blogId as userId from lofter_db_dump.ods_db_blog_info_nd where blogNickName='游客'
         |) B on A.userId=B.userId
         |left join lofter.ods_test_user_nd C on A.userId = C.blogid
         |left join (
         |    select userId from lofter_db_dump.ods_db_media_account_import_nd where platformType in (0,2) group by userId
         |  union
         |    select blogid as userId from lofter_db_dump.ods_db_robot_blog_info_nd group by blogid
         |) D on A.userId=D.userId
         |left join blogVips E on A.userId = E.userId
         |left join blogPgc F on A.userId = F.blogId
         |""".stripMargin

    val outPath = s"$output/dt=$date"
    spark.sql(sql_1).repartition(5).write.mode(SaveMode.Overwrite).parquet(outPath)
    spark.sql(s"alter table lofter.dim_user set location '$outPath'")
    spark.close()
  }

}
