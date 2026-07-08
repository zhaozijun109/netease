package com.netease.lofter.etl.ads

import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserInfo {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .appName("User Login Info Extract")
      .getOrCreate()

    val outPath = "/user/da_lofter/hive/lofter_user_info"

    val sql =
      s"""
        |select userId as UserId, blogId as MainBlogID, createTime as ProfileCreateTime,
        |       from_platform as platfrom, accountType as accounttype, account,
        |       blogName as BlogName, blogNickName as BlogNickName, gender, birth_year as birthday, blogs, emails as Email,
        |       last_phone as phone, last_phone_create_time as phoneCreateTime, connect_sites as siteInfo, last_login_time as lastlogtime,
        |       last_login_platform as lastlogform, last_login_deviceid as lastdevice, last_login_app_version as lastappversion,
        |       auth_domain_ids as authenticatedomainids, auth_domain_names as authenticatenames, is_open_reward as openReward,
        |       all_blog_fans as allblogfans, cast(size(follow_blogs) as bigint) as followusers,
        |       post_count_std as postnum, cast(size(subscribe_tags) as bigint) as favoritetagnum, post_count_since_2020 as postNumFrom2020,
        |       concat_ws(',', follow_blogs) followBlogs,
        |       concat_ws(',', subscribe_tags) as favoriteTags
        |from lofter.dws_par_user_base_dd
        |where dt = '$dt'
        |""".stripMargin

    spark.sql(sql)
      .write.mode(SaveMode.Overwrite)
      .parquet(outPath)

    spark.sql(s"alter table lofter_dm.dwb_par_user_info_nd set location '$outPath' ")

    spark.close()
  }
}
