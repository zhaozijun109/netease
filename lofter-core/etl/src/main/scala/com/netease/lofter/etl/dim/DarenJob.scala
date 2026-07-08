package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object DarenJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.read.parquet(s"/user/da_lofter/db_dump/UserBlogAccount/$date")
      .filter("blogid is not null and userid is not null").createOrReplaceTempView("t1")
    spark.read.parquet(s"/user/da_lofter/db_dump/authenticate_blog/$date").createOrReplaceTempView("t2")

    // authInfo contains(authTime,authDomainId,authDomainName), user can use authInfo.authTime to get the value
    val sql_3 =
      s"""
         |select userId, blogId, collect_set(authInfo) as authInfo from
         |(select  A.userId,A.blogId,B.authInfo from
         |    t1 A
         |join
         |    (select blogId,named_struct('authTime',createTime,'authDomainId',domainId,'authDomainName',domainName) as authInfo from t2) B
         |on A.blogid=B.blogid) C
         |group by userId,blogId
         |""".stripMargin
    val outPath = s"$output/dt=$date"
    spark.sql(sql_3).repartition(1).write.mode(SaveMode.Overwrite).parquet(outPath)
    spark.sql(s"alter table lofter.dim_daren set location '$outPath'")
    spark.close()
  }

}
