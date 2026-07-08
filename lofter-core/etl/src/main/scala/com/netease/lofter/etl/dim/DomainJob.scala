package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object DomainJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.read.parquet(s"/user/da_lofter/db_dump/recommend_domain/$date").createOrReplaceTempView("t1")
    spark.read.parquet(s"/user/da_lofter/db_dump/recommend_domain_tag/$date").createOrReplaceTempView("t2")

    //pay attention to the record which tagName is "" but domainId is not null
    val sql_1 =
      s"""
         |select domainId,collect_set(tagName) as tags,collect_set(topTag) as topTags, collect_set(topTag) as topicTags
         |from
         |(select domainId,tagName,case when toptag=1 then tagName else null end as topTag
         | from t2 where domainId is not null and length(trim(tagName))>0 ) A
         |group by domainId
         |""".stripMargin

    spark.sql(sql_1).createOrReplaceTempView("domainTags")

    val sql_2 =
      s"""
         |select distinct id,domainName,tags,topTags, topicTags from
         |      t1 join domainTags A on t1.id=A.domainId and t1.id>0
         |""".stripMargin

    val outPath = s"$output/dt=$date"
    spark.sql(sql_2).repartition(1).write.mode(SaveMode.Overwrite).parquet(outPath)

    spark.sql(s"alter table lofter.dim_domain set location '$outPath'")
    spark.close()
  }

}
