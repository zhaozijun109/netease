package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.common.databases
import com.netease.wm.group.platform.common.SparkSqlImplicits.rowParam
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object UserGroupEffectJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import com.netease.wm.util.Sql._

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val dt = pargs.optional("date").getOrElse(yesterday)
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")
    implicit val conn: Connection = databases.getDDBConn

    spark.sql("msck repair table lofter.dwd_user_group_user_list_di")
    spark.sql("set spark.sql.crossJoin.enabled=true")

    val creatorSql =
      s"""
         |select cast(job_id as bigint) as packId,
         |       cast(replace(dt,'-', '') as bigint) as `time`,
         |       level_s_creators,
         |       level_a_creators,
         |       level_b_creators,
         |       level_c_creators,
         |       daren_creators,
         |       active_creators,
         |       active_creators / user_count as activePeopleCount,
         |       post_creators,
         |       post_count_1d / user_count as postCountAvg,
         |       post_count_7d / user_count as postFreqWeekAvg,
         |       post_count_30d / user_count as postFreqMonthAvg,
         |       revenue / user_count as rewardMoneyAvg,
         |       new_fans / user_count as fansGrowAvg,
         |       receive_hot / user_count as receiveHotAvg,
         |       exposure_pv / user_count as exposureCountAvg,
         |       click_pv / user_count as clickCountAvg,
         |       browse_pv / user_count as browseCountAvg,
         |       play_pv / user_count as playVideoCountAvg,
         |       folder_exposure_pv / user_count as recommendCollectionExposureCountAvg,
         |       folder_click_pv / user_count as recommendCollectionClickCountAvg,
         |       folder_browse_pv / user_count as recommendCollectionBrowseCountAvg,
         |       click_pv / nvl(exposure_pv,1) as clickAvgRate,
         |       hd_pv / nvl(exposure_pv,1) as interactiveAvgRate,
         |       collection_exposure_pv / user_count as collectionExposureCountAvg,
         |       collection_click_pv / user_count as collectionClickCountAvg,
         |       collection_browse_pv / user_count as collectionBrowseCountAvg,
         |       level_d_creators, level_d_star_creators
         |from lofter_dm.ads_user_group_creator_effect_di
         |where dt = '$dt'
         |""".stripMargin

    val consumerSql =
      s"""
         |select cast(job_id as bigint) as packId,
         |       cast(replace(dt,'-', '') as bigint) as `time`,
         |       follow_count / user_count as followCountAvg,
         |       subscribe_tag_cnt / user_count as subscribeTagCountAvg,
         |       subscribe_collection_cnt / user_count as subscribeCollectionCountAvg,
         |       first_post_uv as firstPostPeopleCount,
         |       post_count / user_count as browsePostCountAvg,
         |       video_count / user_count as playVideoCountAvg,
         |       session_time / 1000.0 / user_count as stayTimeAvg,
         |       click_ad_count / user_count as clickAdvertisingCountAvg,
         |       comment_count / praise_count as commentLickRatio,
         |       live_uv as watchLivePeopleCount,
         |       interaction_uv as interactivePeopleCount,
         |       reward_uv as rewardPeopleCount,
         |       gift_uv as giftPeopleCount,
         |       buy_uv as buyPeopleCount,
         |       reward_amount / user_count as rewardMoneyAvg,
         |       gift_amount / user_count as giftMoneyAvg,
         |       buy_amount / user_count as buyMoneyAvg
         |from lofter_dm.ads_user_group_consumer_effect_di
         |where dt = '$dt'
         |""".stripMargin

    spark.sql(creatorSql).collect().foreach { row =>
      sql"""delete from Cmb_CreatorObserveData where packId = ${"packId"} and time = ${"time"}""".update(rowParam(row))

      sql"""
           |insert ignore into Cmb_CreatorObserveData(id,packId,time,sLevelPeopleCount,aLevelPeopleCount,bLevelPeopleCount,cLevelPeopleCount,expertPeopleCount,activePeopleCount,activePeopleCountRatio,postPeopleCount,postCountAvg,postFreqWeekAvg,postFreqMonthAvg,rewardMoneyAvg,fansGrowAvg,receiveHotAvg,exposureCountAvg,clickCountAvg,browseCountAvg,playVideoCountAvg,collectionExposureCountAvg,collectionClickCountAvg,collectionBrowseCountAvg,recommendCollectionExposureCountAvg,recommendCollectionClickCountAvg,recommendCollectionBrowseCountAvg,clickAvgRate,interactiveAvgRate,dLevelPeopleCount,dAsteriskLevelPeopleCount)
           |values(seq,${"packId"},${"time"},${"level_s_creators"},${"level_a_creators"},${"level_b_creators"},${"level_c_creators"},${"daren_creators"},${"active_creators"},${"activePeopleCount"},${"post_creators"},${"postCountAvg"},${"postFreqWeekAvg"},${"postFreqMonthAvg"},${"rewardMoneyAvg"},${"fansGrowAvg"},${"receiveHotAvg"},${"exposureCountAvg"},${"clickCountAvg"},${"browseCountAvg"},${"playVideoCountAvg"},${"collectionExposureCountAvg"},${"collectionClickCountAvg"},${"collectionBrowseCountAvg"},${"recommendCollectionExposureCountAvg"},${"recommendCollectionClickCountAvg"},${"recommendCollectionBrowseCountAvg"},${"clickAvgRate"},${"interactiveAvgRate"},${"level_d_creators"},${"level_d_star_creators"})
        """.stripMargin.update(rowParam(row))
    }

    spark.sql(consumerSql).collect().foreach { row =>
      sql"""delete from Cmb_ConsumerObserveData where packId = ${"packId"} and time = ${"time"}""".update(rowParam(row))

      sql"""
           |insert ignore into Cmb_ConsumerObserveData(id,packId,time,followCountAvg,subscribeTagCountAvg,subscribeCollectionCountAvg,firstPostPeopleCount,browsePostCountAvg,playVideoCountAvg,stayTimeAvg,clickAdvertisingCountAvg,commentLickRatio,watchLivePeopleCount,interactivePeopleCount,rewardPeopleCount,giftPeopleCount,buyPeopleCount,rewardMoneyAvg,giftMoneyAvg,buyMoneyAvg)
           |values(seq,${"packId"},${"time"},${"followCountAvg"},${"subscribeTagCountAvg"},${"subscribeCollectionCountAvg"},${"firstPostPeopleCount"},${"browsePostCountAvg"},${"playVideoCountAvg"},${"stayTimeAvg"},${"clickAdvertisingCountAvg"},${"commentLickRatio"},${"watchLivePeopleCount"},${"interactivePeopleCount"},${"rewardPeopleCount"},${"giftPeopleCount"},${"buyPeopleCount"},${"rewardMoneyAvg"},${"giftMoneyAvg"},${"buyMoneyAvg"})
           |""".stripMargin.update(rowParam(row))
    }

    val retention1dSql =
      s"""
         |select job_id, cast(replace(base_date,'-', '') as bigint) as `time`, nvl(retention_user_count / user_count, 0) as r1 from lofter_dm.ads_user_group_retention_di where dt='$dt' and period = 1
         |""".stripMargin

    spark.sql(retention1dSql).collect().foreach { row =>
      sql"""
           |update Cmb_ConsumerObserveData set retentionD1 = ${"r1"} where packId = ${"job_id"} and time = ${"time"}
           |""".stripMargin.update(rowParam(row))
    }

    val retention2dSql =
      s"""
         |select job_id, cast(replace(base_date,'-', '') as bigint) as `time`, nvl(retention_user_count / user_count,0) as r2 from lofter_dm.ads_user_group_retention_di where dt='$dt' and period = 3
         |""".stripMargin

    spark.sql(retention2dSql).collect().foreach { row =>
      sql"""
           |update Cmb_ConsumerObserveData set retentionD3 = ${"r2"} where packId = ${"job_id"} and time = ${"time"}
           |""".stripMargin.update(rowParam(row))
    }

    val retention6dSql =
      s"""
         |select job_id, cast(replace(base_date,'-', '') as bigint) as `time`, nvl(retention_user_count / user_count,0) as r6 from lofter_dm.ads_user_group_retention_di where dt='$dt' and period = 7
         |""".stripMargin

    spark.sql(retention6dSql).collect().foreach { row =>
      sql"""
           |update Cmb_ConsumerObserveData set retentionD7 = ${"r6"} where packId = ${"job_id"} and time = ${"time"}
           |""".stripMargin.update(rowParam(row))
    }

    conn.close()
    spark.close()
  }
}
