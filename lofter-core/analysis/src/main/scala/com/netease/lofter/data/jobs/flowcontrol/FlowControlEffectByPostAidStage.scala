package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FlowControlEffectByPostAidStage {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_project_stage =
      s"""
         |select level, nvl(contentType,'all') as contentType,
         |       count(distinct a.postId) as postNum,
         |       sum(bgPv) as exposurePv,
         |       sum(hot) as hotPv
         |from
         |(select post_id as postId,level,exposurepv as bgpv,hot from rec.rec_lofter_boost_article_adapt_v2 where day='$date') a
         |left join
         |(select id,contentType from lofter.dim_post) b
         |on a.postId = b.id
         |group by level,contentType
         |grouping sets((level,contentType),(level))
         |""".stripMargin

    spark.sql(sql_project_stage)
      .repartition(3)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_aid_stage_di")

    spark.close()
  }
}
