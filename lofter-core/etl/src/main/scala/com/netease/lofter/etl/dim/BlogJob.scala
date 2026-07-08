package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object BlogJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("DimBlog")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.read.parquet(s"/user/da_lofter/db_dump/BlogInfo/$date").createOrReplaceTempView("t1")
    spark.read.parquet(s"/user/da_lofter/db_dump/authenticate_blog/$date").createOrReplaceTempView("t2")
    spark.read.parquet(s"/user/da_lofter/db_dump/BlogSettings/$date").createOrReplaceTempView("t3")
    spark.read.parquet(s"/user/da_lofter/db_dump/Blog_OfficialBlog/$date").createOrReplaceTempView("t4")

    //extract info from AdminPubData
    val sql_1 =
      s"""
         |select blogid,collect_set(domainid) as domainIdSet,collect_set(domainname) as domainNameSet, min(createTime) authTime
         |from t2 where blogid is not null
         |group by blogid
         |""".stripMargin

    spark.sql(sql_1).createOrReplaceTempView("blogAuth")

    // after online ,remove the NoSearch judge condition
    val sql_2 =
      s"""
         |select distinct blogid,
         |      case when (SecurityRank is null or SecurityRank=0) and (NoSearch is null or NoSearch=0) then true  else false end as blogValid
         |from t3 where blogid is not null
         |""".stripMargin

    spark.sql(sql_2).createOrReplaceTempView("blogSetting")

    // if C not match A based on blogId, then set the isValid to false(the blog belongs to anonymity user)
    val sql_3 =
      s"""
         |select A.blogid as id,A.blogName,case when B.blogid is null then false else true end as isAuthenticated,
         |       A.blogNickName,nvl(C.blogValid,false) as isValid,
         |       if(D.blogId is null, 0, 1) as isTest,
         |       A.BlogCreateTime as createTime,
         |       B.domainIdSet as authDomainIds, B.domainNameSet as authDomainNames,
         |       if(E.blogId is null, 0, 1) as isOfficial,
         |       B.authTime
         |from
         |    t1 A
         |left join
         |    blogAuth B
         |on A.blogid=B.blogid
         |left join
         |    blogSetting C
         |on A.blogid = C.blogid
         |left join lofter.ods_test_user_nd D
         |on A.blogId = D.blogId
         |left join (select blogId from t4 group by blogId) E on A.blogId = E.blogId
         |""".stripMargin

    val outPath = s"$output/dt=$date"
    spark.sql(sql_3).repartition(5).write.mode(SaveMode.Overwrite).parquet(outPath)
    spark.sql(s"alter table lofter.dim_blog set location '$outPath'")
    spark.close()
  }

}
