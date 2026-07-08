package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FlowControlEffectByPostBackFill {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneDayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val twoWeeksAgo = DateTime.parse(date).minusWeeks(2).toString("yyyy-MM-dd")

    // stat the postId detail index for the postId that first enter the rec
    val sql_project_post_detail_acc_first =
      s"""
         |select a.flowTaskId, a.flowTaskType,
         |      a.postId, b.dt as backFillDate,
         |      a.userId, a.contentType, a.customTags,a.firstTag, a.secondTag, a.thirdTag, a.publishDate,a.enterRecDate,
         |      b.exposurePv, b.exposureUv, b.clickPv, b.clickUv,
         |      b.posHotPv, b.hdUv, b.browseUv,
         |      if(a.contentType = '视频', b.realBrowsePv, 0) as realPlayPv, b.finishPlayPv, b.playPv, b.realFinishPlayPv,
         |      if(a.contentType = '视频', b.realBrowseUv, 0) as realPlayUv, b.realBrowseUv, b.realBrowseNewUv, a.level
         |from (
         |  select * from lofter_dm.ads_rec_dis_post_acc_di
         |  where dt = '$date' and isNewRecPost = 1
         |) a
         |  join (
         |    select dt,postId,exposurePv,exposureUv,clickPv,clickUv,posHotPv,hdUv,browseUv,
         |        playPv,realPlayPv,finishPlayPv,realFinishPlayPv,realPlayUv,realBrowsePv,realBrowseUv,realBrowseNewUv
         |     from lofter.dws_post_base_stats_dd
         |     where dt between '$twoWeeksAgo' and '$oneDayAgo'
         |  ) b on a.postId = b.postId
         |""".stripMargin

    spark.sql(sql_project_post_detail_acc_first)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_post_acc_first_di")

    spark.close()
  }
}
