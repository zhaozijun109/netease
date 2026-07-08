package com.netease.lofter.etl.dws

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostBaseDataHistoryStatsJob {
  val START_DATE = "2021-01-01"
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Post Base Data History Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.autoBroadcastJoinThreshold","10485760")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val oneDayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_post_his =
      s"""
         |select postId,
         |    sum(clickPv) as clickPv, sum(clickUv) as clickUv, sum(browsePv) as browsePv,sum(browseUv) as browseUv,
         |    sum(exposurePv) as exposurePv, sum(exposureUv) as exposureUv, sum(realBrowsePv) as realBrowsePv,
         |    sum(realBrowseUv) as realBrowseUv,sum(browseTime) as browseTime, sum(realBrowseTime) as realBrowseTime,
         |    sum(realBrowseNewUv) as realBrowseNewUv,
         |    sum(playPv) as playPv, sum(playUv) as playUv, sum(playTime) as playTime,sum(playProgress) as playProgress,
         |    sum(realPlayPv) as realPlayPv, sum(realPlayUv) as realPlayUv,sum(realPlayTime) as realPlayTime,sum(realPlayProgress) as realPlayProgress,
         |    sum(finishPlayPv) as finishPlayPv,sum(finishPlayUv) as finishPlayUv,sum(realFinishPlayPv) as realFinishPlayPv, sum(realFinishPlayUv) as realFinishPlayUv,
         |    sum(negFeedbackPv) as negFeedbackPv,sum(negFeedbackUv) as negFeedbackUv,
         |    sum(sharePv) as sharePv, sum(shareUv) as shareUv,
         |    sum(posPraisePv) as posPraisePv, sum(posPraiseUv) as posPraiseUv, sum(negPraisePv) as negPraisePv, sum(negPraiseUv) as negPraiseUv,
         |    sum(posRecPv) as posRecPv,sum(posRecUv) as posRecUv,sum(negRecPv) as negRecPv,sum(negRecUv) as negRecUv,
         |    sum(posReproducePv) as posReproducePv,sum(posReproduceUv) as posReproduceUv, sum(negReproducePv) as negReproducePv, sum(negReproduceUv) as negReproduceUv,
         |    sum(posSubscribePv) as posSubscribePv, sum(posSubscribeUv) as posSubscribeUv,sum(negSubscribePv) as negSubscribePv,sum(negSubscribeUv) as negSubscribeUv,
         |    sum(posHotPv) as posHotPv, sum(posHotUv) as posHotUv, sum(negHotPv) as negHotPv, sum(negHotUv) as negHotUv,
         |    sum(posCommentPv) as posCommentPv, sum(posCommentUv) as posCommentUv, sum(negCommentPv) as negCommentPv,sum(negCommentUv) as negCommentUv,
         |    sum(hdPv) as hdPv, sum(hdUv) as hdUv,
         |    sum(rewardUv) as rewardUv, sum(rewardAmount) as rewardAmount,
         |    sum(freeGiftUv) as freeGiftUv, sum(freeGiftAmount) as freeGiftAmount,
         |    sum(chargeGiftUv) as chargeGiftUv, sum(chargeGiftAmount) as chargeGiftAmount,
         |    sum(centralizedExposurePv) as centralizedExposurePv, sum(centralizedExposureUv) as centralizedExposureUv,
         |    sum(nonCentralizedExposurePv) as nonCentralizedExposurePv, sum(nonCentralizedExposureUv) as nonCentralizedExposureUv,
         |    sum(valid_response) as valid_response, sum(support_exposure_pv) as support_exposure_pv, sum(support_induced_pv) as support_induced_pv
         |from
         |(select * from lofter.dws_post_base_stats_dd where dt='$oneDayAgo'
         |union all
         |select postId,
         |      clickPv,clickUv,browsePv,browseUv,exposurePv,exposureUv,realBrowsePv,realBrowseUv,browseTime,realBrowseTime,
         |      realBrowseNewUv,playPv,playUv,playTime,playProgress,realPlayPv,realPlayUv,realPlayTime,realPlayProgress,
         |      finishPlayPv,finishPlayUv,realFinishPlayPv,realFinishPlayUv,
         |      negFeedbackPv,negFeedbackUv,sharePv,shareUv,
         |      posPraisePv,posPraiseUv,negPraisePv,negPraiseUv,posRecUv,posRecPv,negRecPv,negRecUv,
         |      posReproducePv,posReproduceUv,negReproducePv,negReproduceUv,posSubscribePv,posSubscribeUv,
         |      negSubscribePv,negSubscribeUv,posHotPv,posHotUv,negHotPv,negHotUv,
         |      posCommentPv,posCommentUv,negCommentPv,negCommentUv,hdPv,hdUv,
         |      rewardUv,rewardAmount,freeGiftUv,freeGiftAmount,chargeGiftUv,chargeGiftAmount,
         |      centralizedExposurePv,centralizedExposureUv,nonCentralizedExposurePv,nonCentralizedExposureUv,
         |      valid_response, support_exposure_pv, support_induced_pv,
         |      dt
         |from lofter.dws_post_base_stats_di where dt = '$date'
         |) a
         |group by postId
         |""".stripMargin

    spark.sql(sql_post_his).write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/hive_db/lofter.db/dws_post_base_stats_dd/dt=$date")
    spark.sql(s"alter table lofter.dws_post_base_stats_dd add if not exists partition(dt='$date')")

    spark.close()
  }

}
