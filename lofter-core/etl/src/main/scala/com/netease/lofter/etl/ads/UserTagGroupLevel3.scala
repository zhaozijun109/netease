package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupLevel3 {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .appName("User Login Info Extract")
      .getOrCreate()

    val dt = pargs.required("date")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")
    val halfMonthAgo = DateTime.parse(dt).minusDays(15).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(dt).minusDays(7).toString("yyyy-MM-dd")

    val creatorTagIncomeGroup =
      s"""
         |INSERT OVERWRITE TABLE `lofter_dm`.`ads_par_user_tag_group_level3_dd` PARTITION(`dt`='$dt', `tag`='creator_tag_income')
         |SELECT
         |	   `blogid` AS `userId`,
         |     `count_period` AS `cat`,
         |     CASE WHEN `total_money` > 0 AND `total_money` < 10 THEN '1-10'
         |	        WHEN `total_money` >= 10 AND `total_money` < 50 THEN '10-50'
         |	        WHEN `total_money` >= 50 AND `total_money` < 100 THEN '50-100'
         |	        WHEN `total_money` >= 100 THEN '100及以上' END AS `subcat`,
         |	   `tag` AS `grp`
         |FROM `lofter_dm`.`ads_creator_tag_income_dd`
         |WHERE `dt` = '$dt' and `total_money` > 0 and length(tag) > 0
         |""".stripMargin

    val userTagPvGroup =
      s"""
         |INSERT OVERWRITE TABLE `lofter_dm`.`ads_par_user_tag_group_level3_dd` PARTITION(`dt`='$dt', `tag`='user_tag_pv')
         |SELECT
         |	    t.`userid` AS `userId`,
         |      t.`count_period` AS `cat`,
         |      CASE WHEN t.`pv` >= 1 AND t.`pv` < 10 THEN '1-10'
         |           WHEN t.`pv` >= 10 AND t.`pv` < 50 THEN '10-50'
         |           WHEN t.`pv` >= 50 AND t.`pv` < 100 THEN '50-100'
         |	         WHEN t.`pv` >= 100 AND t.`pv` < 500 THEN '100-500'
         |	         WHEN t.`pv` >= 500 AND t.`pv` < 1000 THEN '500-1000'
         |	         WHEN t.`pv` >= 1000 THEN '1000及以上' END AS `subcat`,
         |	    t.`tag` AS `grp`
         |FROM (
         |	    SELECT
         |	    	   `userid`,
         |	    	   `tag`,
         |	    	   'DAY_7' AS `count_period`,
         |	    	   COUNT(`userid`) AS `pv`
         |	    FROM
         |	    	  `lofter`.`dwd_tag_browse_di`
         |	    WHERE
         |	    	  `dt` <= '$dt' AND `dt` >= '$weekAgo'
         |	    GROUP BY `userid`,`tag`
         |
         |	    UNION ALL
         |
         |	    SELECT
         |	    	   `userid`,
         |	    	   `tag`,
         |	    	   'DAY_15' AS `count_period`,
         |	    	   COUNT(`userid`) AS `pv`
         |	    FROM
         |	    	  `lofter`.`dwd_tag_browse_di`
         |	    WHERE
         |	    	  `dt` <= '$dt' AND `dt` >= '$halfMonthAgo'
         |	    GROUP BY `userid`,`tag`
         |
         |	    UNION ALL
         |
         |	    SELECT
         |	    	   `userid`,
         |	    	   `tag`,
         |	    	   'DAY_30' AS `count_period`,
         |	    	   COUNT(`userid`) AS `pv`
         |	    FROM
         |	    	  `lofter`.`dwd_tag_browse_di`
         |	    WHERE
         |	    	  `dt` <= '$dt' AND `dt` >= '$monthAgo'
         |	    GROUP BY `userid`,`tag`
         |) t
         |WHERE t.`pv` > 0 and length(t.`tag`) > 0
         |""".stripMargin

    val userTagPayGroup =
      s"""
         |INSERT OVERWRITE TABLE `lofter_dm`.`ads_par_user_tag_group_level3_dd` PARTITION(`dt`='$dt', `tag`='user_tag_pay')
         |SELECT
         |	   `userid` AS `userId`,
         |     `count_period` AS `cat`,
         |	   CASE WHEN `total_money` > 0 AND `total_money` < 10 THEN '1-10'
         |	        WHEN `total_money` >= 10 AND `total_money` < 50 THEN '10-50'
         |	        WHEN `total_money` >= 50 AND `total_money` < 100 THEN '50-100'
         |	        WHEN `total_money` >= 100 THEN '100及以上' END AS `subcat`,
         |	   `tag` AS `grp`
         |FROM `lofter_dm`.`ads_user_tag_pay_dd`
         |WHERE `dt` = '$dt' and `total_money` > 0 and length(tag) > 0
         |""".stripMargin

    val allGroup =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='ALL')
        |select id as userId, 'ALL' as cat, 'ALL' as subcat, '1' as grp
        |from lofter.dim_user
        |""".stripMargin

    val categoryGroup =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='category_consume')
        |select userId, cat, subcat, grp
        |from (
        |    select userId,
        |           'DAY_7' as cat,
        |           case when post_7d < 10 then 'COUNT_1_10'
        |                when post_7d < 100 then 'COUNT_10_100'
        |                when post_7d < 500 then 'COUNT_100_500'
        |                when post_7d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           category as grp, post_7d as pc
        |    from lofter.dws_category_user_consume_dd
        |    where dt = '$dt' and post_7d > 0
        |
        |    union all
        |
        |    select userId,
        |           'DAY_15' as cat,
        |           case when post_15d < 10 then 'COUNT_1_10'
        |                when post_15d < 100 then 'COUNT_10_100'
        |                when post_15d < 500 then 'COUNT_100_500'
        |                when post_15d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           category as grp, post_15d as pc
        |    from lofter.dws_category_user_consume_dd
        |    where dt = '$dt' and post_15d > 0
        |
        |    union all
        |
        |    select userId,
        |           'DAY_30' as cat,
        |           case when post_30d < 10 then 'COUNT_1_10'
        |                when post_30d < 100 then 'COUNT_10_100'
        |                when post_30d < 500 then 'COUNT_100_500'
        |                when post_30d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           category as grp, post_30d as pc
        |    from lofter.dws_category_user_consume_dd
        |    where dt = '$dt' and post_30d > 0
        |) t
        |where userId > 0L
        |""".stripMargin

    val ipGroup =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='ip_consume')
        |select userId, cat, subcat, grp
        |from (
        |    select userId,
        |           'DAY_7' as cat,
        |           case when post_7d < 10 then 'COUNT_1_10'
        |                when post_7d < 100 then 'COUNT_10_100'
        |                when post_7d < 500 then 'COUNT_100_500'
        |                when post_7d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           ip as grp, post_7d as pc
        |    from lofter.dws_ip_user_consume_dd
        |    where dt = '$dt' and post_7d > 0
        |
        |    union all
        |
        |    select userId,
        |           'DAY_15' as cat,
        |           case when post_15d < 10 then 'COUNT_1_10'
        |                when post_15d < 100 then 'COUNT_10_100'
        |                when post_15d < 500 then 'COUNT_100_500'
        |                when post_15d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           ip as grp, post_15d as pc
        |    from lofter.dws_ip_user_consume_dd
        |    where dt = '$dt' and post_15d > 0
        |
        |    union all
        |
        |    select userId,
        |           'DAY_30' as cat,
        |           case when post_30d < 10 then 'COUNT_1_10'
        |                when post_30d < 100 then 'COUNT_10_100'
        |                when post_30d < 500 then 'COUNT_100_500'
        |                when post_30d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           ip as grp, post_30d as pc
        |    from lofter.dws_ip_user_consume_dd
        |    where dt = '$dt' and post_30d > 0
        |) t
        |""".stripMargin

    val tagGroup =
      s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='tag_consume')
        |select userId, cat, subcat, grp
        |from (
        |    select userId,
        |           'DAY_7' as cat,
        |           case when post_7d < 10 then 'COUNT_1_10'
        |                when post_7d < 100 then 'COUNT_10_100'
        |                when post_7d < 500 then 'COUNT_100_500'
        |                when post_7d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           tag as grp, post_7d as pc
        |    from lofter.dws_tag_user_consume_dd
        |    where dt = '$dt' and post_7d > 3
        |
        |    union all
        |
        |    select userId,
        |           'DAY_15' as cat,
        |           case when post_15d < 10 then 'COUNT_1_10'
        |                when post_15d < 100 then 'COUNT_10_100'
        |                when post_15d < 500 then 'COUNT_100_500'
        |                when post_15d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           tag as grp, post_15d as pc
        |    from lofter.dws_tag_user_consume_dd
        |    where dt = '$dt' and post_15d > 3
        |
        |    union all
        |
        |    select userId,
        |           'DAY_30' as cat,
        |           case when post_30d < 10 then 'COUNT_1_10'
        |                when post_30d < 100 then 'COUNT_10_100'
        |                when post_30d < 500 then 'COUNT_100_500'
        |                when post_30d < 1000 then 'COUNT_500_1k'
        |                else 'COUNT_1k' end as subcat,
        |           tag as grp, post_30d as pc
        |    from lofter.dws_tag_user_consume_dd
        |    where dt = '$dt' and post_30d > 3
        |) t
        |""".stripMargin

    val contentGroup =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='content_post')
        |select userId, cat, subcat, grp
        |from (
        |  select userId, 'DAY_7' as cat,
        |         case when col2 < 3 then 'COUNT_1_3'
        |              when col2 < 6 then 'COUNT_3_6'
        |              when col2 < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', '文字', 'col2', core_post_text_7d), named_struct('col1', '图片', 'col2', core_post_photo_7d), named_struct('col1', '视频', 'col2', core_post_video_7d), named_struct('col1', '长文章', 'col2', core_post_longtext_7d), named_struct('col1', '问答', 'col2', core_post_answer_7d), named_struct('col1', '聊聊', 'col2', core_post_chat_7d)))
        |      from lofter.dws_par_user_post_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_15' as cat,
        |         case when col2 < 3 then 'COUNT_1_3'
        |              when col2 < 6 then 'COUNT_3_6'
        |              when col2 < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', '文字', 'col2', core_post_text_15d), named_struct('col1', '图片', 'col2', core_post_photo_15d), named_struct('col1', '视频', 'col2', core_post_video_15d), named_struct('col1', '长文章', 'col2', core_post_longtext_15d), named_struct('col1', '问答', 'col2', core_post_answer_15d), named_struct('col1', '聊聊', 'col2', core_post_chat_15d)))
        |      from lofter.dws_par_user_post_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_30' as cat,
        |         case when col2 < 3 then 'COUNT_1_3'
        |              when col2 < 6 then 'COUNT_3_6'
        |              when col2 < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', '文字', 'col2', core_post_text_30d), named_struct('col1', '图片', 'col2', core_post_photo_30d), named_struct('col1', '视频', 'col2', core_post_video_30d), named_struct('col1', '长文章', 'col2', core_post_longtext_30d), named_struct('col1', '问答', 'col2', core_post_answer_30d), named_struct('col1', '聊聊', 'col2', core_post_chat_30d)))
        |      from lofter.dws_par_user_post_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_90' as cat,
        |         case when col2 < 3 then 'COUNT_1_3'
        |              when col2 < 6 then 'COUNT_3_6'
        |              when col2 < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', '文字', 'col2', core_post_text_90d), named_struct('col1', '图片', 'col2', core_post_photo_90d), named_struct('col1', '视频', 'col2', core_post_video_90d), named_struct('col1', '长文章', 'col2', core_post_longtext_90d), named_struct('col1', '问答', 'col2', core_post_answer_90d), named_struct('col1', '聊聊', 'col2', core_post_chat_90d)))
        |      from lofter.dws_par_user_post_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_180' as cat,
        |         case when col2 < 3 then 'COUNT_1_3'
        |              when col2 < 6 then 'COUNT_3_6'
        |              when col2 < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', '文字', 'col2', core_post_text_180d), named_struct('col1', '图片', 'col2', core_post_photo_180d), named_struct('col1', '视频', 'col2', core_post_video_180d), named_struct('col1', '长文章', 'col2', core_post_longtext_180d), named_struct('col1', '问答', 'col2', core_post_answer_180d), named_struct('col1', '聊聊', 'col2', core_post_chat_180d)))
        |      from lofter.dws_par_user_post_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |) t
        |where userId > 0L
        |""".stripMargin

    val tagPostGroup =
      s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='tag_post')
        |select userId, cat, subcat, grp
        |from (
        |  select userId, 'DAY_7' as cat,
        |         case when post_count_7d < 3 then 'COUNT_1_3'
        |              when post_count_7d < 6 then 'COUNT_3_6'
        |              when post_count_7d < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        tag as grp
        |  from lofter.dws_par_user_tag_create_dd
        |  where post_count_7d > 0 and
        |        dt = '$dt'
        |
        |  union all
        |
        |  select userId, 'DAY_15' as cat,
        |         case when post_count_15d < 3 then 'COUNT_1_3'
        |              when post_count_15d < 6 then 'COUNT_3_6'
        |              when post_count_15d < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        tag as grp
        |  from lofter.dws_par_user_tag_create_dd
        |  where post_count_15d > 0 and
        |        dt = '$dt'
        |
        |  union all
        |
        |  select userId, 'DAY_30' as cat,
        |         case when post_count_30d < 3 then 'COUNT_1_3'
        |              when post_count_30d < 6 then 'COUNT_3_6'
        |              when post_count_30d < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        tag as grp
        |  from lofter.dws_par_user_tag_create_dd
        |  where post_count_30d > 0 and
        |        dt = '$dt'
        |
        |  union all
        |
        |  select userId, 'DAY_90' as cat,
        |         case when post_count_90d < 3 then 'COUNT_1_3'
        |              when post_count_90d < 6 then 'COUNT_3_6'
        |              when post_count_90d < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        tag as grp
        |  from lofter.dws_par_user_tag_create_dd
        |  where post_count_90d > 0 and
        |        dt = '$dt'
        |
        |  union all
        |
        |  select userId, 'DAY_180' as cat,
        |         case when post_count_180d < 3 then 'COUNT_1_3'
        |              when post_count_180d < 6 then 'COUNT_3_6'
        |              when post_count_180d < 30 then 'COUNT_6_30'
        |              else 'COUNT_30' end as subcat,
        |        tag as grp
        |  from lofter.dws_par_user_tag_create_dd
        |  where post_count_180d > 0 and
        |        dt = '$dt'
        |
        |) t
        |where userId > 0L
        |""".stripMargin

    val productTypePay =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='product_type_pay')
        |select userId, cat, subcat, grp
        |from (
        |
        |  select userId, 'DAY_7' as cat,
        |         case when col2 < 10 then 'COUNT_1_10'
        |              when col2 < 50 then 'COUNT_10_50'
        |              when col2 < 100 then 'COUNT_50_100'
        |              when col2 < 200 then 'COUNT_100_200'
        |              when col2 < 300 then 'COUNT_200_300'
        |              else 'COUNT_300' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', 'market', 'col2', market_sale_amount_7d), named_struct('col1', 'card', 'col2', card_sale_amount_7d)))
        |      from lofter.dws_par_user_ec_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_15' as cat,
        |         case when col2 < 10 then 'COUNT_1_10'
        |              when col2 < 50 then 'COUNT_10_50'
        |              when col2 < 100 then 'COUNT_50_100'
        |              when col2 < 200 then 'COUNT_100_200'
        |              when col2 < 300 then 'COUNT_200_300'
        |              else 'COUNT_300' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', 'market', 'col2', market_sale_amount_15d), named_struct('col1', 'card', 'col2', card_sale_amount_15d)))
        |      from lofter.dws_par_user_ec_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |    select userId, 'DAY_30' as cat,
        |         case when col2 < 10 then 'COUNT_1_10'
        |              when col2 < 50 then 'COUNT_10_50'
        |              when col2 < 100 then 'COUNT_50_100'
        |              when col2 < 200 then 'COUNT_100_200'
        |              when col2 < 300 then 'COUNT_200_300'
        |              else 'COUNT_300' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', 'market', 'col2', market_sale_amount_30d), named_struct('col1', 'card', 'col2', card_sale_amount_30d)))
        |      from lofter.dws_par_user_ec_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_90' as cat,
        |         case when col2 < 10 then 'COUNT_1_10'
        |              when col2 < 50 then 'COUNT_10_50'
        |              when col2 < 100 then 'COUNT_50_100'
        |              when col2 < 200 then 'COUNT_100_200'
        |              when col2 < 300 then 'COUNT_200_300'
        |              else 'COUNT_300' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', 'market', 'col2', market_sale_amount_90d), named_struct('col1', 'card', 'col2', card_sale_amount_90d)))
        |      from lofter.dws_par_user_ec_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |
        |  union all
        |
        |  select userId, 'DAY_180' as cat,
        |         case when col2 < 10 then 'COUNT_1_10'
        |              when col2 < 50 then 'COUNT_10_50'
        |              when col2 < 100 then 'COUNT_50_100'
        |              when col2 < 200 then 'COUNT_100_200'
        |              when col2 < 300 then 'COUNT_200_300'
        |              else 'COUNT_300' end as subcat,
        |        col1 as grp
        |  from (
        |      select userId,
        |             inline(array(named_struct('col1', 'market', 'col2', market_sale_amount_180d), named_struct('col1', 'card', 'col2', card_sale_amount_180d)))
        |      from lofter.dws_par_user_ec_dd
        |      where dt = '$dt'
        |  ) a
        |  where col2 > 0
        |)
        |where userId > 0L
        |""".stripMargin

    val specialtyConsumeGroup =
      s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='specialty_consume')
        |select *
        |from (
        |    select userId, 'DAY_7' as cat,
        |           case when post_count_7d < 10 then 'COUNT_1_10'
        |              when post_count_7d < 100 then 'COUNT_10_100'
        |              when post_count_7d < 500 then 'COUNT_100_500'
        |              when post_count_7d < 1000 then 'COUNT_500_1k'
        |              else 'COUNT_1k' end as subcat,
        |           label as grp
        |    from lofter_dm.ads_specialty_user_consume_di
        |    where dt = '$dt' and post_count_7d > 0
        |
        |    union all
        |
        |    select userId, 'DAY_15' as cat,
        |           case when post_count_15d < 10 then 'COUNT_1_10'
        |              when post_count_15d < 100 then 'COUNT_10_100'
        |              when post_count_15d < 500 then 'COUNT_100_500'
        |              when post_count_15d < 1000 then 'COUNT_500_1k'
        |              else 'COUNT_1k' end as subcat,
        |              label as grp
        |    from lofter_dm.ads_specialty_user_consume_di
        |    where dt = '$dt' and post_count_15d > 0
        |
        |    union all
        |
        |    select userId, 'DAY_30' as cat,
        |           case when post_count_30d < 10 then 'COUNT_1_10'
        |              when post_count_30d < 100 then 'COUNT_10_100'
        |              when post_count_30d < 500 then 'COUNT_100_500'
        |              when post_count_30d < 1000 then 'COUNT_500_1k'
        |              else 'COUNT_1k' end as subcat,
        |              label as grp
        |     from lofter_dm.ads_specialty_user_consume_di
        |     where dt = '$dt' and post_count_30d > 0
        |) t
        |""".stripMargin

    val specialtyCreatorGroup =
      s"""
         |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_dd partition(dt='$dt', tag='specialty_publish')
         |select *
         |from (
         |    select blogId as userId, 'DAY_7' as cat,
         |           case when post_count_7d < 10 then 'COUNT_1_10'
         |              when post_count_7d < 100 then 'COUNT_10_100'
         |              when post_count_7d < 500 then 'COUNT_100_500'
         |              when post_count_7d < 1000 then 'COUNT_500_1k'
         |              else 'COUNT_1k' end as subcat,
         |           label as grp
         |    from lofter_dm.ads_specialty_creator_publish_di
         |    where dt = '$dt' and post_count_7d > 0
         |
         |    union all
         |
         |    select blogId as userId, 'DAY_15' as cat,
         |           case when post_count_15d < 10 then 'COUNT_1_10'
         |              when post_count_15d < 100 then 'COUNT_10_100'
         |              when post_count_15d < 500 then 'COUNT_100_500'
         |              when post_count_15d < 1000 then 'COUNT_500_1k'
         |              else 'COUNT_1k' end as subcat,
         |              label as grp
         |    from lofter_dm.ads_specialty_creator_publish_di
         |    where dt = '$dt' and post_count_15d > 0
         |
         |    union all
         |
         |    select blogId as userId, 'DAY_30' as cat,
         |           case when post_count_30d < 10 then 'COUNT_1_10'
         |              when post_count_30d < 100 then 'COUNT_10_100'
         |              when post_count_30d < 500 then 'COUNT_100_500'
         |              when post_count_30d < 1000 then 'COUNT_500_1k'
         |              else 'COUNT_1k' end as subcat,
         |              label as grp
         |     from lofter_dm.ads_specialty_creator_publish_di
         |     where dt = '$dt' and post_count_30d > 0
         |) t
         |""".stripMargin

    spark.sql(creatorTagIncomeGroup)
    spark.sql(userTagPvGroup)
    spark.sql(userTagPayGroup)
    spark.sql(allGroup)
    spark.sql(productTypePay)
    spark.sql(tagPostGroup)
    spark.sql(contentGroup)
    spark.sql(tagGroup)
    spark.sql(categoryGroup)
    spark.sql(ipGroup)
    spark.sql(specialtyConsumeGroup)
    spark.sql(specialtyCreatorGroup)
    spark.close()
  }
}
