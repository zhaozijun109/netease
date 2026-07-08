package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupLevel2 {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .appName("User Login Info Extract")
      .getOrCreate()

    val dt = pargs.required("date")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")
    val halfMonthAgo = DateTime.parse(dt).minusDays(15).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(dt).minusDays(7).toString("yyyy-MM-dd")
    val threeMonthAgo = DateTime.parse(dt).minusDays(90).toString("yyyy-MM-dd")
    val halfYearAgo = DateTime.parse(dt).minusDays(180).toString("yyyy-MM-dd")

    spark.sql("create temporary function bitmap_to_array as 'com.netease.wm.udf.bitmap.BitmapToArrayUDF'")

    val tagGroupLevel2 =
      s"""
         |insert overwrite table lofter_dm.ads_par_user_tag_group_level2_dd partition(dt='$dt')
         |select userId, tag, cat, grp
         |from (
         |    select userId, 'coupon_exchange' as tag,
         |           'DAY_1' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_1d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_1d > 0
         |    ) t
         |
         |    union all
         |
         |    select userId, 'coupon_exchange' as tag,
         |           'DAY_7' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_7d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_7d > 0
         |    ) t
         |
         |    union all
         |
         |    select userId, 'coupon_exchange' as tag,
         |           'DAY_15' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_15d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_15d > 0
         |    ) t
         |
         |    union all
         |
         |    select userId, 'coupon_exchange' as tag,
         |           'DAY_30' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_30d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_30d > 0
         |    ) t
         |
         |    union all
         |
         |    select userId, 'coupon_exchange' as tag,
         |           'DAY_90' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_90d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_90d > 0
         |    ) t
         |
         |    union all
         |
         |     select userId, 'coupon_exchange' as tag,
         |           'DAY_180' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_180d as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_180d > 0
         |    ) t
         |
         |    union all
         |
         |     select userId, 'coupon_exchange' as tag,
         |           'DAY_ALL' as cat,
         |           case when coupon_count <= 20 then '0-20'
         |                when coupon_count <= 100 then '20-100'
         |                else '100及以上' end as grp
         |    from (
         |        select userId, coupon_count_std as coupon_count
         |        from lofter.dws_par_user_coupon_exchange_dd
         |        where dt = '$dt' and coupon_count_std > 0
         |    ) t
         |
         |    union all
         |
         |    select userId,
         |           'pve用户消耗体力' as tag,
         |           'DAY_7' as cat,
         |           case when total_costStamina = 0 then '0'
         |               when total_costStamina > 0 and total_costStamina <= 50 then '0-50'
         |               when total_costStamina > 50 and total_costStamina <= 100 then '50-100'
         |               when total_costStamina > 100 and total_costStamina <= 300 then '100-300'
         |               when total_costStamina > 300 and total_costStamina <= 500 then '300-500'
         |               else '500及以上'
         |           end as grp
         |    from (
         |           select userId,
         |                  sum(total_costStamina) as total_costStamina
         |           from lofter.dws_pve_user_coststamina_role_di
         |           where dt > '$weekAgo' and dt <= '$dt'
         |           group by userId
         |    ) t1
         |
         |    union all
         |
         |    select userId,
         |           'pve用户消耗体力' as tag,
         |           'DAY_15' as cat,
         |           case when total_costStamina = 0 then '0'
         |               when total_costStamina > 0 and total_costStamina <= 50 then '0-50'
         |               when total_costStamina > 50 and total_costStamina <= 100 then '50-100'
         |               when total_costStamina > 100 and total_costStamina <= 300 then '100-300'
         |               when total_costStamina > 300 and total_costStamina <= 500 then '300-500'
         |               else '500及以上'
         |           end as grp
         |    from (
         |           select userId,
         |                  sum(total_costStamina) as total_costStamina
         |           from lofter.dws_pve_user_coststamina_role_di
         |           where dt > '$halfMonthAgo' and dt <= '$dt'
         |           group by userId
         |    ) t1
         |
         |    union all
         |
         |    select userId,
         |           'pve用户消耗体力' as tag,
         |           'DAY_30' as cat,
         |           case when total_costStamina = 0 then '0'
         |               when total_costStamina > 0 and total_costStamina <= 50 then '0-50'
         |               when total_costStamina > 50 and total_costStamina <= 100 then '50-100'
         |               when total_costStamina > 100 and total_costStamina <= 300 then '100-300'
         |               when total_costStamina > 300 and total_costStamina <= 500 then '300-500'
         |               else '500及以上'
         |           end as grp
         |    from (
         |           select userId,
         |                  sum(total_costStamina) as total_costStamina
         |           from lofter.dws_pve_user_coststamina_role_di
         |           where dt > '$monthAgo' and dt <= '$dt'
         |           group by userId
         |    ) t1
         |
         |    union all
         |
         |    select userId,
         |           'pve用户消耗体力' as tag,
         |           'DAY_90' as cat,
         |           case when total_costStamina = 0 then '0'
         |               when total_costStamina > 0 and total_costStamina <= 50 then '0-50'
         |               when total_costStamina > 50 and total_costStamina <= 100 then '50-100'
         |               when total_costStamina > 100 and total_costStamina <= 300 then '100-300'
         |               when total_costStamina > 300 and total_costStamina <= 500 then '300-500'
         |               else '500及以上'
         |           end as grp
         |    from (
         |           select userId,
         |                  sum(total_costStamina) as total_costStamina
         |           from lofter.dws_pve_user_coststamina_role_di
         |           where dt > '$threeMonthAgo' and dt <= '$dt'
         |           group by userId
         |    ) t1
         |
         |    union all
         |
         |    select userId, '激励视频获得粮票' as tag,
         |          'DAY_7' as cat,
         |          case when video_grain_ticket_7d < 20 then '1-20'
         |               else '20及以上' end as grp
         |    from lofter.dws_par_user_ad_dd
         |    where dt = '$dt' and video_grain_ticket_7d > 0
         |
         |    union all
         |
         |    select userId, '激励视频获得粮票' as tag,
         |          'DAY_15' as cat,
         |          case when video_grain_ticket_15d < 20 then '1-20'
         |               else '20及以上' end as grp
         |    from lofter.dws_par_user_ad_dd
         |    where dt = '$dt' and video_grain_ticket_15d > 0
         |
         |    union all
         |
         |    select userId, '激励视频获得粮票' as tag,
         |          'DAY_30' as cat,
         |          case when video_grain_ticket_30d < 20 then '1-20'
         |               else '20及以上' end as grp
         |    from lofter.dws_par_user_ad_dd
         |    where dt = '$dt' and video_grain_ticket_30d > 0
         |
         |    union all
         |
         |    SELECT
         |	        `userid` AS `userId`,
         |	        '搜索口令的用户' AS `tag`,
         |	        'DAY_7' AS `cat`,
         |          IF(`is_paid_subscribe` = 1,'付费口令', '非付费口令') AS `grp`
         |    FROM
         |	        `lofter`.`dwd_growth_actpwd_access_di`
         |    WHERE
         |	        `dt` <= '$dt' AND `dt` > '$weekAgo' AND (`actpwd_type` = 0 OR `actpwd_type` = 2)
         |    GROUP BY `userId`, `tag`, `cat`, `grp`
         |
         |    UNION ALL
         |
         |    SELECT
         |	        `userid` AS `userId`,
         |	        '搜索口令的用户' AS `tag`,
         |	        'DAY_15' AS `cat`,
         |	        IF(`is_paid_subscribe` = 1,'付费口令', '非付费口令') AS `grp`
         |    FROM
         |	        `lofter`.`dwd_growth_actpwd_access_di`
         |    WHERE
         |	        `dt` <= '$dt' AND `dt` > '$halfMonthAgo' AND (`actpwd_type` = 0 OR `actpwd_type` = 2)
         |    GROUP BY `userId`, `tag`, `cat`, `grp`
         |
         |    UNION ALL
         |
         |    SELECT
         |	        `userid` AS `userId`,
         |	        '搜索口令的用户' AS `tag`,
         |	        'DAY_30' AS `cat`,
         |	        IF(`is_paid_subscribe` = 1,'付费口令', '非付费口令') AS `grp`
         |    FROM
         |	        `lofter`.`dwd_growth_actpwd_access_di`
         |    WHERE
         |	        `dt` <= '$dt' AND `dt` > '$monthAgo' AND (`actpwd_type` = 0 OR `actpwd_type` = 2)
         |    GROUP BY `userId`, `tag`, `cat`, `grp`
         |
         |    UNION ALL
         |
         |    select id as userId, 'ALL' as tag, 'ALL' as cat, '1' as grp
         |    from lofter.dim_user
         |
         |    union all
         |
         |    select userId, '活跃天数' as tag, 'DAY_7' as cat,
         |           cast(active_days_7d as string) as grp
         |    from lofter.dws_par_user_active_dd
         |    where dt = '$dt' and active_days_7d > 0
         |
         |    union all
         |
         |    select userId, '活跃天数' as tag, 'DAY_15' as cat,
         |           cast(active_days_15d as string) as grp
         |    from lofter.dws_par_user_active_dd
         |    where dt = '$dt' and active_days_15d > 0
         |
         |    union all
         |
         |    select userId, '活跃天数' as tag, 'DAY_30' as cat,
         |           cast(active_days_30d as string) as grp
         |    from lofter.dws_par_user_active_dd
         |    where dt = '$dt' and active_days_30d > 0
         |
         |    union all
         |
         |    select userId, '活跃天数' as tag, 'DAY_90' as cat,
         |           cast(active_days_90d as string) as grp
         |    from lofter.dws_par_user_active_dd
         |    where dt = '$dt' and active_days_90d > 0
         |
         |    union all
         |
         |    select userId, '潜力有效创作者' as tag, 'DAY_7' as cat,
         |           case when creator_post_count_7d < 2 then '1-2'
         |                when creator_post_count_7d < 3 then '2-3'
         |                when creator_post_count_7d < 6 then '3-6'
         |                when creator_post_count_7d < 30 then '6-30'
         |                else '30及以上' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and creator_post_count_7d > 0
         |
         |    union all
         |
         |    select userId, '潜力有效创作者' as tag, 'DAY_15' as cat,
         |           case when creator_post_count_15d < 2 then '1-2'
         |                when creator_post_count_15d < 3 then '2-3'
         |                when creator_post_count_15d < 6 then '3-6'
         |                when creator_post_count_15d < 30 then '6-30'
         |                else '30及以上' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and creator_post_count_15d > 0
         |
         |    union all
         |
         |    select userId, '潜力有效创作者' as tag, 'DAY_30' as cat,
         |           case when creator_post_count_30d < 2 then '1-2'
         |                when creator_post_count_30d < 3 then '2-3'
         |                when creator_post_count_30d < 6 then '3-6'
         |                when creator_post_count_30d < 30 then '6-30'
         |                else '30及以上' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and creator_post_count_30d > 0
         |
         |    union all
         |
         |    select userId, '发文总有效pv' as tag, 'DAY_7' as cat,
         |           case when browse_pv_7d < 200 then '1-200'
         |                when browse_pv_7d < 400 then '200-400'
         |                when browse_pv_7d < 600 then '400-600'
         |                when browse_pv_7d < 800 then '600-800'
         |                when browse_pv_7d < 1000 then '800-1000'
         |                else '1000及以上' end as grp
         |    from lofter.dws_par_creator_traffic_dd
         |    where dt = '$dt' and browse_pv_7d > 0
         |
         |    union all
         |
         |    select userId, '发文总有效pv' as tag, 'DAY_15' as cat,
         |           case when browse_pv_15d < 200 then '1-200'
         |                when browse_pv_15d < 400 then '200-400'
         |                when browse_pv_15d < 600 then '400-600'
         |                when browse_pv_15d < 800 then '600-800'
         |                when browse_pv_15d < 1000 then '800-1000'
         |                else '1000及以上' end as grp
         |    from lofter.dws_par_creator_traffic_dd
         |    where dt = '$dt' and browse_pv_15d > 0
         |
         |    union all
         |
         |    select userId, '发文总有效pv' as tag, 'DAY_30' as cat,
         |           case when browse_pv_30d < 200 then '1-200'
         |                when browse_pv_30d < 400 then '200-400'
         |                when browse_pv_30d < 600 then '400-600'
         |                when browse_pv_30d < 800 then '600-800'
         |                when browse_pv_30d < 1000 then '800-1000'
         |                else '1000及以上' end as grp
         |    from lofter.dws_par_creator_traffic_dd
         |    where dt = '$dt' and browse_pv_30d > 0
         |
         |    union all
         |
         |    select userId, '篇均有效pv' as tag, 'DAY_7' as cat,
         |           case when avg_browse_pv < 50 then '1-50'
         |                when avg_browse_pv < 100 then '50-100'
         |                when avg_browse_pv < 150 then '100-150'
         |                when avg_browse_pv < 200 then '150-200'
         |                else '200及以上' end as grp
         |    from (
         |      select *, new_post_browse_pv_7d/post_count_7d as avg_browse_pv
         |      from lofter.dws_par_creator_traffic_dd
         |      where dt = '$dt' and post_count_7d > 0
         |    ) t
         |    where avg_browse_pv > 0
         |
         |    union all
         |
         |    select userId, '篇均有效pv' as tag, 'DAY_15' as cat,
         |           case when avg_browse_pv < 50 then '1-50'
         |                when avg_browse_pv < 100 then '50-100'
         |                when avg_browse_pv < 150 then '100-150'
         |                when avg_browse_pv < 200 then '150-200'
         |                else '200及以上' end as grp
         |    from (
         |      select *, new_post_browse_pv_15d/post_count_15d as avg_browse_pv
         |      from lofter.dws_par_creator_traffic_dd
         |      where dt = '$dt' and post_count_15d > 0
         |    ) t
         |    where avg_browse_pv > 0
         |
         |    union all
         |
         |    select userId, '篇均有效pv' as tag, 'DAY_30' as cat,
         |           case when avg_browse_pv < 50 then '1-50'
         |                when avg_browse_pv < 100 then '50-100'
         |                when avg_browse_pv < 150 then '100-150'
         |                when avg_browse_pv < 200 then '150-200'
         |                else '200及以上' end as grp
         |    from (
         |      select *, new_post_browse_pv_30d/post_count_30d as avg_browse_pv
         |      from lofter.dws_par_creator_traffic_dd
         |      where dt = '$dt' and post_count_30d > 0
         |    ) t
         |    where avg_browse_pv > 0
         |
         |    union all
         |
         |    select userId, '付费文章曝光篇数' as tag, 'DAY_30' as cat,
         |           case when paid_post_expose_count_30d < 50 then '1-50'
         |                else '50+' end as grp
         |    from lofter.dws_par_user_traffic_dd
         |    where dt = '$dt' and paid_post_expose_count_30d > 0
         |
         |    union all
         |
         |    select userId, '付费文章有效浏览篇数' as tag, 'DAY_30' as cat,
         |           case when paid_post_browse_count_30d < 20 then '1-20'
         |                else '20+' end as grp
         |    from lofter.dws_par_user_traffic_dd
         |    where dt = '$dt' and paid_post_browse_count_30d > 0
         |
         |    union all
         |
         |    select userId, '付费文章有效浏览篇数' as tag, 'DAY_7' as cat,
         |           case when paid_post_browse_count_7d < 20 then '1-20'
         |                else '20+' end as grp
         |    from lofter.dws_par_user_traffic_dd
         |    where dt = '$dt' and paid_post_browse_count_7d > 0
         |
         |    union all
         |
         |    select userId, '页面访问' as tag, 'DAY_7' as cat,
         |           case when eventId = 'z1-4' then '买周边'
         |                when eventId = 'z1-3' then '发布器'
         |                end as grp
         |    from lofter.dwd_user_events_di
         |    where dt <= '$dt' and dt > '$weekAgo' and eventId in ('z1-4', 'z1-3')
         |    group by userId, eventId
         |
         |    union all
         |
         |    select userId, '页面访问' as tag, 'DAY_15' as cat,
         |           case when eventId = 'z1-4' then '买周边'
         |                when eventId = 'z1-3' then '发布器'
         |                end as grp
         |    from lofter.dwd_user_events_di
         |    where dt <= '$dt' and dt > '$halfMonthAgo' and eventId in ('z1-4', 'z1-3')
         |    group by userId, eventId
         |
         |    union all
         |
         |        select userId, '页面访问' as tag, 'DAY_30' as cat,
         |           case when eventId = 'z1-4' then '买周边'
         |                when eventId = 'z1-3' then '发布器'
         |                end as grp
         |    from lofter.dwd_user_events_di
         |    where dt <= '$dt' and dt > '$monthAgo' and eventId in ('z1-4', 'z1-3')
         |    group by userId, eventId
         |
         |    union all
         |
         |    select userId, '热度' as tag, 'DAY_7' as cat,
         |           case when hot < 50 then '1-50'
         |                 when hot < 100 then '50-100'
         |                 when hot < 200 then '100-200'
         |                 when hot < 500 then '200-500'
         |                 when hot < 1000 then '500-1k'
         |                 when hot < 2000 then '1k-2k'
         |                 when hot < 5000 then '2k-5k'
         |                 when hot < 10000 then '5k-1w'
         |                 when hot < 30000 then '1w-3w'
         |                 when hot < 50000 then '3w-5w'
         |                 when hot < 100000 then '5w-10w'
         |                 else '10w及以上' end as grp
         |    from (
         |        select userId,
         |          nvl(receive_like_cnt_7d,0)+nvl(receive_reproduce_cnt_7d,0)+nvl(receive_recommend_cnt_7d,0)+nvl(receive_collect_cnt_7d,0) as hot
         |        from lofter.dws_par_creator_interaction_dd
         |        where dt = '$dt'
         |    ) t
         |    where hot > 0
         |
         |    union all
         |
         |    select userId, '热度' as tag, 'DAY_15' as cat,
         |            case when hot < 50 then '1-50'
         |                 when hot < 100 then '50-100'
         |                 when hot < 200 then '100-200'
         |                 when hot < 500 then '200-500'
         |                 when hot < 1000 then '500-1k'
         |                 when hot < 2000 then '1k-2k'
         |                 when hot < 5000 then '2k-5k'
         |                 when hot < 10000 then '5k-1w'
         |                 when hot < 30000 then '1w-3w'
         |                 when hot < 50000 then '3w-5w'
         |                 when hot < 100000 then '5w-10w'
         |                 else '10w及以上' end as grp
         |    from (
         |        select userId,
         |          nvl(receive_like_cnt_15d,0)+nvl(receive_reproduce_cnt_15d,0)+nvl(receive_recommend_cnt_15d,0)+nvl(receive_collect_cnt_15d,0) as hot
         |        from lofter.dws_par_creator_interaction_dd
         |        where dt = '$dt'
         |    ) t
         |    where hot > 0
         |
         |    union all
         |
         |    select userId, '热度' as tag, 'DAY_30' as cat,
         |            case when hot < 50 then '1-50'
         |                 when hot < 100 then '50-100'
         |                 when hot < 200 then '100-200'
         |                 when hot < 500 then '200-500'
         |                 when hot < 1000 then '500-1k'
         |                 when hot < 2000 then '1k-2k'
         |                 when hot < 5000 then '2k-5k'
         |                 when hot < 10000 then '5k-1w'
         |                 when hot < 30000 then '1w-3w'
         |                 when hot < 50000 then '3w-5w'
         |                 when hot < 100000 then '5w-10w'
         |                 else '10w及以上' end as grp
         |    from (
         |        select userId,
         |          nvl(receive_like_cnt_30d,0)+nvl(receive_reproduce_cnt_30d,0)+nvl(receive_recommend_cnt_30d,0)+nvl(receive_collect_cnt_30d,0) as hot
         |        from lofter.dws_par_creator_interaction_dd
         |        where dt = '$dt'
         |    ) t
         |    where hot > 0
         |
         |    union all
         |    select userId, '热度' as tag, 'DAY_ALL' as cat,
         |            case when hot < 50 then '1-50'
         |                 when hot < 100 then '50-100'
         |                 when hot < 200 then '100-200'
         |                 when hot < 500 then '200-500'
         |                 when hot < 1000 then '500-1k'
         |                 when hot < 2000 then '1k-2k'
         |                 when hot < 5000 then '2k-5k'
         |                 when hot < 10000 then '5k-1w'
         |                 when hot < 30000 then '1w-3w'
         |                 when hot < 50000 then '3w-5w'
         |                 when hot < 100000 then '5w-10w'
         |                 else '10w及以上' end as grp
         |    from (
         |        select userId, receive_hot_std as hot
         |        from lofter.dws_par_creator_interaction_dd
         |        where dt = '$dt'
         |    ) t
         |    where hot > 0
         |
         |    union all
         |
         |    select userId, '发文频次' as tag, 'DAY_7' as cat,
         |           case when post_count_7d < 3 then 'COUNT_1_3'
         |                when post_count_7d < 6 then 'COUNT_3_6'
         |                when post_count_7d < 30 then 'COUNT_6_30'
         |                else 'COUNT_30' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and post_count_7d > 0
         |
         |    union all
         |
         |    select userId, '发文频次' as tag, 'DAY_15' as cat,
         |           case when post_count_15d < 3 then 'COUNT_1_3'
         |                when post_count_15d < 6 then 'COUNT_3_6'
         |                when post_count_15d < 30 then 'COUNT_6_30'
         |                else 'COUNT_30' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and post_count_15d > 0
         |
         |    union all
         |
         |    select userId, '发文频次' as tag, 'DAY_30' as cat,
         |           case when post_count_30d < 3 then 'COUNT_1_3'
         |                when post_count_30d < 6 then 'COUNT_3_6'
         |                when post_count_30d < 30 then 'COUNT_6_30'
         |                else 'COUNT_30' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and post_count_30d > 0
         |
         |    union all
         |
         |    select userId, '发文频次' as tag, 'DAY_90' as cat,
         |           case when post_count_90d < 3 then 'COUNT_1_3'
         |                when post_count_90d < 6 then 'COUNT_3_6'
         |                when post_count_90d < 30 then 'COUNT_6_30'
         |                else 'COUNT_30' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and post_count_90d > 0
         |
         |    union all
         |
         |    select userId, '发文频次' as tag, 'DAY_180' as cat,
         |           case when post_count_180d < 3 then 'COUNT_1_3'
         |                when post_count_180d < 6 then 'COUNT_3_6'
         |                when post_count_180d < 30 then 'COUNT_6_30'
         |                else 'COUNT_30' end as grp
         |    from lofter.dws_par_user_post_dd
         |    where dt = '$dt' and post_count_180d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_7' as cat,
         |           case when send_hot_7d < 100 then 'COUNT_1_100'
         |                when send_hot_7d < 500 then 'COUNT_100_500'
         |                when send_hot_7d < 1000 then 'COUNT_500_1K'
         |                when send_hot_7d < 5000 then 'COUNT_1K_5K'
         |                when send_hot_7d < 10000 then 'COUNT_5K_1W'
         |                when send_hot_7d < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_7d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_15' as cat,
         |           case when send_hot_15d < 100 then 'COUNT_1_100'
         |                when send_hot_15d < 500 then 'COUNT_100_500'
         |                when send_hot_15d < 1000 then 'COUNT_500_1K'
         |                when send_hot_15d < 5000 then 'COUNT_1K_5K'
         |                when send_hot_15d < 10000 then 'COUNT_5K_1W'
         |                when send_hot_15d < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_15d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_30' as cat,
         |           case when send_hot_30d < 100 then 'COUNT_1_100'
         |                when send_hot_30d < 500 then 'COUNT_100_500'
         |                when send_hot_30d < 1000 then 'COUNT_500_1K'
         |                when send_hot_30d < 5000 then 'COUNT_1K_5K'
         |                when send_hot_30d < 10000 then 'COUNT_5K_1W'
         |                when send_hot_30d < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_30d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_90' as cat,
         |           case when send_hot_90d < 100 then 'COUNT_1_100'
         |                when send_hot_90d < 500 then 'COUNT_100_500'
         |                when send_hot_90d < 1000 then 'COUNT_500_1K'
         |                when send_hot_90d < 5000 then 'COUNT_1K_5K'
         |                when send_hot_90d < 10000 then 'COUNT_5K_1W'
         |                when send_hot_90d < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_90d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_180' as cat,
         |           case when send_hot_180d < 100 then 'COUNT_1_100'
         |                when send_hot_180d < 500 then 'COUNT_100_500'
         |                when send_hot_180d < 1000 then 'COUNT_500_1K'
         |                when send_hot_180d < 5000 then 'COUNT_1K_5K'
         |                when send_hot_180d < 10000 then 'COUNT_5K_1W'
         |                when send_hot_180d < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_180d > 0
         |
         |    union all
         |
         |    select userId, '热度贡献量' as tag, 'DAY_ALL' as cat,
         |           case when send_hot_std < 100 then 'COUNT_1_100'
         |                when send_hot_std < 500 then 'COUNT_100_500'
         |                when send_hot_std < 1000 then 'COUNT_500_1K'
         |                when send_hot_std < 5000 then 'COUNT_1K_5K'
         |                when send_hot_std < 10000 then 'COUNT_5K_1W'
         |                when send_hot_std < 50000 then 'COUNT_1W_5W'
         |                else 'COUNT_5W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_hot_std > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_7' as cat,
         |            case when send_like_cnt_7d < 10 then 'COUNT_1_10'
         |                 when send_like_cnt_7d < 50 then 'COUNT_10_50'
         |                 when send_like_cnt_7d < 100 then 'COUNT_50_100'
         |                 when send_like_cnt_7d < 500 then 'COUNT_100_500'
         |                 when send_like_cnt_7d < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt_7d < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt_7d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt_7d > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_15' as cat,
         |            case when send_like_cnt_15d < 10 then 'COUNT_1_10'
         |                 when send_like_cnt_15d < 50 then 'COUNT_10_50'
         |                 when send_like_cnt_15d < 100 then 'COUNT_50_100'
         |                 when send_like_cnt_15d < 500 then 'COUNT_100_500'
         |                 when send_like_cnt_15d < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt_15d < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt_15d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt_15d > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_30' as cat,
         |            case when send_like_cnt_30d < 10 then 'COUNT_1_10'
         |                 when send_like_cnt_30d < 50 then 'COUNT_10_50'
         |                 when send_like_cnt_30d < 100 then 'COUNT_50_100'
         |                 when send_like_cnt_30d < 500 then 'COUNT_100_500'
         |                 when send_like_cnt_30d < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt_30d < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt_30d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt_30d > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_90' as cat,
         |            case when send_like_cnt_90d < 10 then 'COUNT_1_10'
         |                 when send_like_cnt_90d < 50 then 'COUNT_10_50'
         |                 when send_like_cnt_90d < 100 then 'COUNT_50_100'
         |                 when send_like_cnt_90d < 500 then 'COUNT_100_500'
         |                 when send_like_cnt_90d < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt_90d < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt_90d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt_90d > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_180' as cat,
         |            case when send_like_cnt_180d < 10 then 'COUNT_1_10'
         |                 when send_like_cnt_180d < 50 then 'COUNT_10_50'
         |                 when send_like_cnt_180d < 100 then 'COUNT_50_100'
         |                 when send_like_cnt_180d < 500 then 'COUNT_100_500'
         |                 when send_like_cnt_180d < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt_180d < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt_180d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt_180d > 0
         |
         |    union all
         |
         |    select userId, '点赞量' as tag, 'DAY_ALL' as cat,
         |            case when send_like_cnt < 10 then 'COUNT_1_10'
         |                 when send_like_cnt < 50 then 'COUNT_10_50'
         |                 when send_like_cnt < 100 then 'COUNT_50_100'
         |                 when send_like_cnt < 500 then 'COUNT_100_500'
         |                 when send_like_cnt < 1000 then 'COUNT_500_1K'
         |                 when send_like_cnt < 5000 then 'COUNT_1K_5K'
         |                 when send_like_cnt < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_like_cnt > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_7' as cat,
         |            case when send_recommend_cnt_7d < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt_7d < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt_7d < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt_7d < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt_7d < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt_7d < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt_7d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt_7d > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_15' as cat,
         |            case when send_recommend_cnt_15d < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt_15d < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt_15d < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt_15d < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt_15d < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt_15d < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt_15d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt_15d > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_30' as cat,
         |            case when send_recommend_cnt_30d < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt_30d < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt_30d < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt_30d < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt_30d < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt_30d < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt_30d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt_30d > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_90' as cat,
         |            case when send_recommend_cnt_90d < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt_90d < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt_90d < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt_90d < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt_90d < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt_90d < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt_90d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt_90d > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_180' as cat,
         |            case when send_recommend_cnt_180d < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt_180d < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt_180d < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt_180d < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt_180d < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt_180d < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt_180d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt_180d > 0
         |
         |    union all
         |
         |    select userId, '蓝手量' as tag, 'DAY_ALL' as cat,
         |            case when send_recommend_cnt < 10 then 'COUNT_1_10'
         |                 when send_recommend_cnt < 50 then 'COUNT_10_50'
         |                 when send_recommend_cnt < 100 then 'COUNT_50_100'
         |                 when send_recommend_cnt < 500 then 'COUNT_100_500'
         |                 when send_recommend_cnt < 1000 then 'COUNT_500_1K'
         |                 when send_recommend_cnt < 5000 then 'COUNT_1K_5K'
         |                 when send_recommend_cnt < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_recommend_cnt > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_7' as cat,
         |            case when send_collect_cnt_7d < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt_7d < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt_7d < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt_7d < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt_7d < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt_7d < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt_7d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt_7d > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_15' as cat,
         |            case when send_collect_cnt_15d < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt_15d < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt_15d < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt_15d < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt_15d < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt_15d < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt_15d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt_15d > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_30' as cat,
         |            case when send_collect_cnt_30d < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt_30d < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt_30d < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt_30d < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt_30d < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt_30d < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt_30d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt_30d > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_90' as cat,
         |            case when send_collect_cnt_90d < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt_90d < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt_90d < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt_90d < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt_90d < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt_90d < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt_90d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt_90d > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_180' as cat,
         |            case when send_collect_cnt_180d < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt_180d < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt_180d < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt_180d < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt_180d < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt_180d < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt_180d < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt_180d > 0
         |
         |    union all
         |
         |    select userId, '收藏量' as tag, 'DAY_ALL' as cat,
         |            case when send_collect_cnt < 10 then 'COUNT_1_10'
         |                 when send_collect_cnt < 50 then 'COUNT_10_50'
         |                 when send_collect_cnt < 100 then 'COUNT_50_100'
         |                 when send_collect_cnt < 500 then 'COUNT_100_500'
         |                 when send_collect_cnt < 1000 then 'COUNT_500_1K'
         |                 when send_collect_cnt < 5000 then 'COUNT_1K_5K'
         |                 when send_collect_cnt < 10000 then 'COUNT_5K_1W'
         |                 else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt='$dt' and send_collect_cnt > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_7' as cat,
         |           case when send_comment_cnt_7d < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_7d < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_7d < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_7d < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_7d < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_7d < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_7d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_7d > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_15' as cat,
         |           case when send_comment_cnt_15d < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_15d < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_15d < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_15d < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_15d < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_15d < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_15d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_15d > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_30' as cat,
         |           case when send_comment_cnt_30d < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_30d < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_30d < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_30d < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_30d < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_30d < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_30d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_30d > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_90' as cat,
         |           case when send_comment_cnt_15d < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_15d < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_15d < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_15d < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_15d < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_15d < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_15d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_15d > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_180' as cat,
         |           case when send_comment_cnt_30d < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_30d < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_30d < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_30d < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_30d < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_30d < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_30d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_30d > 0
         |
         |    union all
         |
         |    select userId, '评论量' as tag, 'DAY_ALL' as cat,
         |           case when send_comment_cnt_std < 10 then 'COUNT_1_10'
         |                when send_comment_cnt_std < 50 then 'COUNT_10_50'
         |                when send_comment_cnt_std < 100 then 'COUNT_50_100'
         |                when send_comment_cnt_std < 500 then 'COUNT_100_500'
         |                when send_comment_cnt_std < 1000 then 'COUNT_500_1K'
         |                when send_comment_cnt_std < 5000 then 'COUNT_1K_5K'
         |                when send_comment_cnt_std < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_interaction_dd
         |    where dt = '$dt' and send_comment_cnt_std > 0
         |
         |    union all
         |
         |    select userId, '打赏收礼金额' as tag, 'DAY_7' as cat,
         |           case when (receive_gift_amount_7d + receive_reward_amount_7d) < 10 then 'COUNT_1_10'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 50 then 'COUNT_10_50'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 100 then 'COUNT_50_100'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 500 then 'COUNT_100_500'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 1000 then 'COUNT_500_1K'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 5000 then 'COUNT_1K_5K'
         |                when (receive_gift_amount_7d + receive_reward_amount_7d) < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and (receive_gift_amount_7d + receive_reward_amount_7d) > 0
         |
         |    union all
         |
         |    select userId, '打赏收礼金额' as tag, 'DAY_15' as cat,
         |           case when (receive_gift_amount_15d + receive_reward_amount_15d) < 10 then 'COUNT_1_10'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 50 then 'COUNT_10_50'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 100 then 'COUNT_50_100'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 500 then 'COUNT_100_500'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 1000 then 'COUNT_500_1K'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 5000 then 'COUNT_1K_5K'
         |                when (receive_gift_amount_15d + receive_reward_amount_15d) < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and (receive_gift_amount_15d + receive_reward_amount_15d) > 0
         |
         |    union all
         |
         |    select userId, '打赏收礼金额' as tag, 'DAY_30' as cat,
         |           case when (receive_gift_amount_30d + receive_reward_amount_30d) < 10 then 'COUNT_1_10'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 50 then 'COUNT_10_50'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 100 then 'COUNT_50_100'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 500 then 'COUNT_100_500'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 1000 then 'COUNT_500_1K'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 5000 then 'COUNT_1K_5K'
         |                when (receive_gift_amount_30d + receive_reward_amount_30d) < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and (receive_gift_amount_30d + receive_reward_amount_30d) > 0
         |
         |    union all
         |
         |    select userId, '打赏收礼金额' as tag, 'DAY_90' as cat,
         |           case when (receive_gift_amount_90d + receive_reward_amount_90d) < 10 then 'COUNT_1_10'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 50 then 'COUNT_10_50'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 100 then 'COUNT_50_100'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 500 then 'COUNT_100_500'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 1000 then 'COUNT_500_1K'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 5000 then 'COUNT_1K_5K'
         |                when (receive_gift_amount_90d + receive_reward_amount_90d) < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and (receive_gift_amount_90d + receive_reward_amount_90d) > 0
         |
         |    union all
         |
         |    select userId, '打赏收礼金额' as tag, 'DAY_180' as cat,
         |           case when (receive_gift_amount_180d + receive_reward_amount_180d) < 10 then 'COUNT_1_10'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 50 then 'COUNT_10_50'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 100 then 'COUNT_50_100'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 500 then 'COUNT_100_500'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 1000 then 'COUNT_500_1K'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 5000 then 'COUNT_1K_5K'
         |                when (receive_gift_amount_180d + receive_reward_amount_180d) < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and (receive_gift_amount_180d + receive_reward_amount_180d) > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_7' as cat,
         |           case when send_reward_amount_7d < 10 then 'COUNT_1_10'
         |                when send_reward_amount_7d < 50 then 'COUNT_10_50'
         |                when send_reward_amount_7d < 100 then 'COUNT_50_100'
         |                when send_reward_amount_7d < 500 then 'COUNT_100_500'
         |                when send_reward_amount_7d < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_7d < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_7d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_7d > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_15' as cat,
         |           case when send_reward_amount_15d < 10 then 'COUNT_1_10'
         |                when send_reward_amount_15d < 50 then 'COUNT_10_50'
         |                when send_reward_amount_15d < 100 then 'COUNT_50_100'
         |                when send_reward_amount_15d < 500 then 'COUNT_100_500'
         |                when send_reward_amount_15d < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_15d < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_15d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_15d > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_30' as cat,
         |           case when send_reward_amount_30d < 10 then 'COUNT_1_10'
         |                when send_reward_amount_30d < 50 then 'COUNT_10_50'
         |                when send_reward_amount_30d < 100 then 'COUNT_50_100'
         |                when send_reward_amount_30d < 500 then 'COUNT_100_500'
         |                when send_reward_amount_30d < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_30d < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_30d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_30d > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_90' as cat,
         |           case when send_reward_amount_90d < 10 then 'COUNT_1_10'
         |                when send_reward_amount_90d < 50 then 'COUNT_10_50'
         |                when send_reward_amount_90d < 100 then 'COUNT_50_100'
         |                when send_reward_amount_90d < 500 then 'COUNT_100_500'
         |                when send_reward_amount_90d < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_90d < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_90d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_90d > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_180' as cat,
         |           case when send_reward_amount_180d < 10 then 'COUNT_1_10'
         |                when send_reward_amount_180d < 50 then 'COUNT_10_50'
         |                when send_reward_amount_180d < 100 then 'COUNT_50_100'
         |                when send_reward_amount_180d < 500 then 'COUNT_100_500'
         |                when send_reward_amount_180d < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_180d < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_180d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_180d > 0
         |
         |    union all
         |
         |    select userId, '打赏金额' as tag, 'DAY_ALL' as cat,
         |           case when send_reward_amount_std < 10 then 'COUNT_1_10'
         |                when send_reward_amount_std < 50 then 'COUNT_10_50'
         |                when send_reward_amount_std < 100 then 'COUNT_50_100'
         |                when send_reward_amount_std < 500 then 'COUNT_100_500'
         |                when send_reward_amount_std < 1000 then 'COUNT_500_1K'
         |                when send_reward_amount_std < 5000 then 'COUNT_1K_5K'
         |                when send_reward_amount_std < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_reward_amount_std > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_7' as cat,
         |           case when send_gift_amount_7d < 10 then 'COUNT_1_10'
         |                when send_gift_amount_7d < 50 then 'COUNT_10_50'
         |                when send_gift_amount_7d < 100 then 'COUNT_50_100'
         |                when send_gift_amount_7d < 500 then 'COUNT_100_500'
         |                when send_gift_amount_7d < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_7d < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_7d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_7d > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_15' as cat,
         |           case when send_gift_amount_15d < 10 then 'COUNT_1_10'
         |                when send_gift_amount_15d < 50 then 'COUNT_10_50'
         |                when send_gift_amount_15d < 100 then 'COUNT_50_100'
         |                when send_gift_amount_15d < 500 then 'COUNT_100_500'
         |                when send_gift_amount_15d < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_15d < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_15d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_15d > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_30' as cat,
         |           case when send_gift_amount_30d < 10 then 'COUNT_1_10'
         |                when send_gift_amount_30d < 50 then 'COUNT_10_50'
         |                when send_gift_amount_30d < 100 then 'COUNT_50_100'
         |                when send_gift_amount_30d < 500 then 'COUNT_100_500'
         |                when send_gift_amount_30d < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_30d < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_30d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_30d > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_90' as cat,
         |           case when send_gift_amount_90d < 10 then 'COUNT_1_10'
         |                when send_gift_amount_90d < 50 then 'COUNT_10_50'
         |                when send_gift_amount_90d < 100 then 'COUNT_50_100'
         |                when send_gift_amount_90d < 500 then 'COUNT_100_500'
         |                when send_gift_amount_90d < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_90d < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_90d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_90d > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_180' as cat,
         |           case when send_gift_amount_180d < 10 then 'COUNT_1_10'
         |                when send_gift_amount_180d < 50 then 'COUNT_10_50'
         |                when send_gift_amount_180d < 100 then 'COUNT_50_100'
         |                when send_gift_amount_180d < 500 then 'COUNT_100_500'
         |                when send_gift_amount_180d < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_180d < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_180d < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_180d > 0
         |
         |    union all
         |
         |    select userId, '送礼金额' as tag, 'DAY_ALL' as cat,
         |           case when send_gift_amount_std < 10 then 'COUNT_1_10'
         |                when send_gift_amount_std < 50 then 'COUNT_10_50'
         |                when send_gift_amount_std < 100 then 'COUNT_50_100'
         |                when send_gift_amount_std < 500 then 'COUNT_100_500'
         |                when send_gift_amount_std < 1000 then 'COUNT_500_1K'
         |                when send_gift_amount_std < 5000 then 'COUNT_1K_5K'
         |                when send_gift_amount_std < 10000 then 'COUNT_5K_1W'
         |                else 'COUNT_1W' end as grp
         |    from lofter.dws_par_user_revenue_dd
         |    where dt = '$dt' and send_gift_amount_std > 0
         |
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_7' as cat,
         |           case when coin_recharge_7d < 5 then 'COUNT_1_5'
         |                when coin_recharge_7d < 8 then 'COUNT_5_8'
         |                when coin_recharge_7d < 10 then 'COUNT_8_10'
         |                when coin_recharge_7d < 15 then 'COUNT_10_15'
         |                when coin_recharge_7d < 20 then 'COUNT_15_20'
         |                when coin_recharge_7d < 25 then 'COUNT_20_25'
         |                when coin_recharge_7d < 50 then 'COUNT_25_50'
         |                when coin_recharge_7d < 80 then 'COUNT_50_80'
         |                when coin_recharge_7d < 100 then 'COUNT_80_100'
         |                when coin_recharge_7d < 150 then 'COUNT_100_150'
         |                when coin_recharge_7d < 200 then 'COUNT_150_200'
         |                when coin_recharge_7d < 250 then 'COUNT_200_250'
         |                when coin_recharge_7d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_7d > 0
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_15' as cat,
         |           case when coin_recharge_15d < 5 then 'COUNT_1_5'
         |                when coin_recharge_15d < 8 then 'COUNT_5_8'
         |                when coin_recharge_15d < 10 then 'COUNT_8_10'
         |                when coin_recharge_15d < 15 then 'COUNT_10_15'
         |                when coin_recharge_15d < 20 then 'COUNT_15_20'
         |                when coin_recharge_15d < 25 then 'COUNT_20_25'
         |                when coin_recharge_15d < 50 then 'COUNT_25_50'
         |                when coin_recharge_15d < 80 then 'COUNT_50_80'
         |                when coin_recharge_15d < 100 then 'COUNT_80_100'
         |                when coin_recharge_15d < 150 then 'COUNT_100_150'
         |                when coin_recharge_15d < 200 then 'COUNT_150_200'
         |                when coin_recharge_15d < 250 then 'COUNT_200_250'
         |                when coin_recharge_15d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_15d > 0
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_30' as cat,
         |           case when coin_recharge_30d < 5 then 'COUNT_1_5'
         |                when coin_recharge_30d < 8 then 'COUNT_5_8'
         |                when coin_recharge_30d < 10 then 'COUNT_8_10'
         |                when coin_recharge_30d < 15 then 'COUNT_10_15'
         |                when coin_recharge_30d < 20 then 'COUNT_15_20'
         |                when coin_recharge_30d < 25 then 'COUNT_20_25'
         |                when coin_recharge_30d < 50 then 'COUNT_25_50'
         |                when coin_recharge_30d < 80 then 'COUNT_50_80'
         |                when coin_recharge_30d < 100 then 'COUNT_80_100'
         |                when coin_recharge_30d < 150 then 'COUNT_100_150'
         |                when coin_recharge_30d < 200 then 'COUNT_150_200'
         |                when coin_recharge_30d < 250 then 'COUNT_200_250'
         |                when coin_recharge_30d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_30d > 0
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_90' as cat,
         |           case when coin_recharge_90d < 5 then 'COUNT_1_5'
         |                when coin_recharge_90d < 8 then 'COUNT_5_8'
         |                when coin_recharge_90d < 10 then 'COUNT_8_10'
         |                when coin_recharge_90d < 15 then 'COUNT_10_15'
         |                when coin_recharge_90d < 20 then 'COUNT_15_20'
         |                when coin_recharge_90d < 25 then 'COUNT_20_25'
         |                when coin_recharge_90d < 50 then 'COUNT_25_50'
         |                when coin_recharge_90d < 80 then 'COUNT_50_80'
         |                when coin_recharge_90d < 100 then 'COUNT_80_100'
         |                when coin_recharge_90d < 150 then 'COUNT_100_150'
         |                when coin_recharge_90d < 200 then 'COUNT_150_200'
         |                when coin_recharge_90d < 250 then 'COUNT_200_250'
         |                when coin_recharge_90d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_90d > 0
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_180' as cat,
         |           case when coin_recharge_180d < 5 then 'COUNT_1_5'
         |                when coin_recharge_180d < 8 then 'COUNT_5_8'
         |                when coin_recharge_180d < 10 then 'COUNT_8_10'
         |                when coin_recharge_180d < 15 then 'COUNT_10_15'
         |                when coin_recharge_180d < 20 then 'COUNT_15_20'
         |                when coin_recharge_180d < 25 then 'COUNT_20_25'
         |                when coin_recharge_180d < 50 then 'COUNT_25_50'
         |                when coin_recharge_180d < 80 then 'COUNT_50_80'
         |                when coin_recharge_180d < 100 then 'COUNT_80_100'
         |                when coin_recharge_180d < 150 then 'COUNT_100_150'
         |                when coin_recharge_180d < 200 then 'COUNT_150_200'
         |                when coin_recharge_180d < 250 then 'COUNT_200_250'
         |                when coin_recharge_180d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_180d > 0
         |    union all
         |
         |    select userId, '乐乎币充值' as tag, 'DAY_ALL' as cat,
         |           case when coin_recharge_std < 5 then 'COUNT_1_5'
         |                when coin_recharge_std < 8 then 'COUNT_5_8'
         |                when coin_recharge_std < 10 then 'COUNT_8_10'
         |                when coin_recharge_std < 15 then 'COUNT_10_15'
         |                when coin_recharge_std < 20 then 'COUNT_15_20'
         |                when coin_recharge_std < 25 then 'COUNT_20_25'
         |                when coin_recharge_std < 50 then 'COUNT_25_50'
         |                when coin_recharge_std < 80 then 'COUNT_50_80'
         |                when coin_recharge_std < 100 then 'COUNT_80_100'
         |                when coin_recharge_std < 150 then 'COUNT_100_150'
         |                when coin_recharge_std < 200 then 'COUNT_150_200'
         |                when coin_recharge_std < 250 then 'COUNT_200_250'
         |                when coin_recharge_std < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_recharge_std > 0
         |
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_7' as cat,
         |           case when coin_consume_7d < 5 then 'COUNT_1_5'
         |                when coin_consume_7d < 8 then 'COUNT_5_8'
         |                when coin_consume_7d < 10 then 'COUNT_8_10'
         |                when coin_consume_7d < 15 then 'COUNT_10_15'
         |                when coin_consume_7d < 20 then 'COUNT_15_20'
         |                when coin_consume_7d < 25 then 'COUNT_20_25'
         |                when coin_consume_7d < 50 then 'COUNT_25_50'
         |                when coin_consume_7d < 80 then 'COUNT_50_80'
         |                when coin_consume_7d < 100 then 'COUNT_80_100'
         |                when coin_consume_7d < 150 then 'COUNT_100_150'
         |                when coin_consume_7d < 200 then 'COUNT_150_200'
         |                when coin_consume_7d < 250 then 'COUNT_200_250'
         |                when coin_consume_7d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_7d > 0
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_15' as cat,
         |           case when coin_consume_15d < 5 then 'COUNT_1_5'
         |                when coin_consume_15d < 8 then 'COUNT_5_8'
         |                when coin_consume_15d < 10 then 'COUNT_8_10'
         |                when coin_consume_15d < 15 then 'COUNT_10_15'
         |                when coin_consume_15d < 20 then 'COUNT_15_20'
         |                when coin_consume_15d < 25 then 'COUNT_20_25'
         |                when coin_consume_15d < 50 then 'COUNT_25_50'
         |                when coin_consume_15d < 80 then 'COUNT_50_80'
         |                when coin_consume_15d < 100 then 'COUNT_80_100'
         |                when coin_consume_15d < 150 then 'COUNT_100_150'
         |                when coin_consume_15d < 200 then 'COUNT_150_200'
         |                when coin_consume_15d < 250 then 'COUNT_200_250'
         |                when coin_consume_15d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_15d > 0
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_30' as cat,
         |           case when coin_consume_30d < 5 then 'COUNT_1_5'
         |                when coin_consume_30d < 8 then 'COUNT_5_8'
         |                when coin_consume_30d < 10 then 'COUNT_8_10'
         |                when coin_consume_30d < 15 then 'COUNT_10_15'
         |                when coin_consume_30d < 20 then 'COUNT_15_20'
         |                when coin_consume_30d < 25 then 'COUNT_20_25'
         |                when coin_consume_30d < 50 then 'COUNT_25_50'
         |                when coin_consume_30d < 80 then 'COUNT_50_80'
         |                when coin_consume_30d < 100 then 'COUNT_80_100'
         |                when coin_consume_30d < 150 then 'COUNT_100_150'
         |                when coin_consume_30d < 200 then 'COUNT_150_200'
         |                when coin_consume_30d < 250 then 'COUNT_200_250'
         |                when coin_consume_30d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_30d > 0
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_90' as cat,
         |           case when coin_consume_90d < 5 then 'COUNT_1_5'
         |                when coin_consume_90d < 8 then 'COUNT_5_8'
         |                when coin_consume_90d < 10 then 'COUNT_8_10'
         |                when coin_consume_90d < 15 then 'COUNT_10_15'
         |                when coin_consume_90d < 20 then 'COUNT_15_20'
         |                when coin_consume_90d < 25 then 'COUNT_20_25'
         |                when coin_consume_90d < 50 then 'COUNT_25_50'
         |                when coin_consume_90d < 80 then 'COUNT_50_80'
         |                when coin_consume_90d < 100 then 'COUNT_80_100'
         |                when coin_consume_90d < 150 then 'COUNT_100_150'
         |                when coin_consume_90d < 200 then 'COUNT_150_200'
         |                when coin_consume_90d < 250 then 'COUNT_200_250'
         |                when coin_consume_90d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_90d > 0
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_180' as cat,
         |           case when coin_consume_180d < 5 then 'COUNT_1_5'
         |                when coin_consume_180d < 8 then 'COUNT_5_8'
         |                when coin_consume_180d < 10 then 'COUNT_8_10'
         |                when coin_consume_180d < 15 then 'COUNT_10_15'
         |                when coin_consume_180d < 20 then 'COUNT_15_20'
         |                when coin_consume_180d < 25 then 'COUNT_20_25'
         |                when coin_consume_180d < 50 then 'COUNT_25_50'
         |                when coin_consume_180d < 80 then 'COUNT_50_80'
         |                when coin_consume_180d < 100 then 'COUNT_80_100'
         |                when coin_consume_180d < 150 then 'COUNT_100_150'
         |                when coin_consume_180d < 200 then 'COUNT_150_200'
         |                when coin_consume_180d < 250 then 'COUNT_200_250'
         |                when coin_consume_180d < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_180d > 0
         |    union all
         |
         |    select userId, '乐乎币消费' as tag, 'DAY_ALL' as cat,
         |           case when coin_consume_std < 5 then 'COUNT_1_5'
         |                when coin_consume_std < 8 then 'COUNT_5_8'
         |                when coin_consume_std < 10 then 'COUNT_8_10'
         |                when coin_consume_std < 15 then 'COUNT_10_15'
         |                when coin_consume_std < 20 then 'COUNT_15_20'
         |                when coin_consume_std < 25 then 'COUNT_20_25'
         |                when coin_consume_std < 50 then 'COUNT_25_50'
         |                when coin_consume_std < 80 then 'COUNT_50_80'
         |                when coin_consume_std < 100 then 'COUNT_80_100'
         |                when coin_consume_std < 150 then 'COUNT_100_150'
         |                when coin_consume_std < 200 then 'COUNT_150_200'
         |                when coin_consume_std < 250 then 'COUNT_200_250'
         |                when coin_consume_std < 300 then 'COUNT_250_300'
         |                else 'COUNT_300' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and coin_consume_std > 0
         |
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_7' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 15 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_7d > 0
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_15' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 15 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_15d > 0
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_30' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 15 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_30d > 0
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_90' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 15 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_90d > 0
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_180' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_180d > 0
         |    union all
         |
         |    select userId, '粮票赠送' as tag, 'DAY_ALL' as cat,
         |           case when grain_ticket_consume_7d < 5 then 'COUNT_1_5'
         |                when grain_ticket_consume_7d < 10 then 'COUNT_5_10'
         |                when grain_ticket_consume_7d < 15 then 'COUNT_10_15'
         |                when grain_ticket_consume_7d < 20 then 'COUNT_15_20'
         |                when grain_ticket_consume_7d < 25 then 'COUNT_20_25'
         |                when grain_ticket_consume_7d < 50 then 'COUNT_25_50'
         |                when grain_ticket_consume_7d < 100 then 'COUNT_50_100'
         |                else 'COUNT_100' end as grp
         |    from lofter.dws_par_user_pay_dd
         |    where dt = '$dt' and grain_ticket_consume_std > 0
         |
         |    union all
         |
         |    select b.userId, '消费广告' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_ad_di
         |            where click_pv > 0 and dt <= '$dt' and dt > '$weekAgo'
         |            group by userId
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费广告' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_ad_di
         |            where click_pv > 0 and dt <= '$dt' and dt > '$halfMonthAgo'
         |            group by userId
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费广告' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_ad_di
         |            where click_pv > 0 and dt <= '$dt' and dt > '$monthAgo'
         |            group by userId
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费广告' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_ad_di
         |            where click_pv > 0 and dt <= '$dt' and dt > '$threeMonthAgo'
         |            group by userId
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费广告' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_ad_di
         |            where click_pv > 0 and dt <= '$dt' and dt > '$halfYearAgo'
         |            group by userId
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '是否发文' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_valid_publish_time > 0 and
         |                  from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                  datediff('$dt', from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd')) < 7
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '是否发文' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_valid_publish_time > 0 and
         |                  from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                  datediff('$dt', from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd')) < 15
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '是否发文' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_valid_publish_time > 0 and
         |                  from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                  datediff('$dt', from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd')) < 30
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '是否发文' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_valid_publish_time > 0 and
         |                  from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                  datediff('$dt', from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd')) < 90
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '是否发文' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_valid_publish_time > 0 and
         |                  from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                  datediff('$dt', from_unixtime(cast(last_valid_publish_time/ 1000 as bigint), 'yyyy-MM-dd')) < 180
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_1' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd') = '$dt'
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and datediff('$dt', from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd')) < 7
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and datediff('$dt', from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd')) < 15
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and datediff('$dt', from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd')) < 30
         |    ) b
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and datediff('$dt', from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd')) < 90
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '消费者活跃情况' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |            select accountId as userId
         |            from lofter.dws_evt_login_user_last_dd
         |            where dt = '$dt' and datediff('$dt', from_unixtime(cast(time/1000 as bigint), 'yyyy-MM-dd')) < 180
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '活跃情况' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_publish_time > 0 and datediff('$dt', from_unixtime(cast(last_publish_time/1000 as bigint), 'yyyy-MM-dd')) < 7
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '活跃情况' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_publish_time > 0 and datediff('$dt', from_unixtime(cast(last_publish_time/1000 as bigint), 'yyyy-MM-dd')) < 15
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '活跃情况' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_publish_time > 0 and datediff('$dt', from_unixtime(cast(last_publish_time/1000 as bigint), 'yyyy-MM-dd')) < 30
         |    ) b
         |
         |
         |    union all
         |
         |    select b.userId, '活跃情况' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_publish_time > 0 and datediff('$dt', from_unixtime(cast(last_publish_time/1000 as bigint), 'yyyy-MM-dd')) < 90
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '活跃情况' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_post_dd
         |            where dt='$dt' and last_publish_time > 0 and datediff('$dt', from_unixtime(cast(last_publish_time/1000 as bigint), 'yyyy-MM-dd')) < 180
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '购买行为' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_revenue_dd
         |            where dt='$dt' and (send_gift_amount_7d + send_reward_amount_7d) > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '购买行为' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_revenue_dd
         |            where dt='$dt' and (send_gift_amount_15d + send_reward_amount_15d) > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '购买行为' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_revenue_dd
         |            where dt='$dt' and (send_gift_amount_30d + send_reward_amount_30d) > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '购买行为' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_revenue_dd
         |            where dt='$dt' and (send_gift_amount_90d + send_reward_amount_90d) > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '购买行为' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |            select userId
         |            from lofter.dws_par_user_revenue_dd
         |            where dt='$dt' and (send_gift_amount_180d + send_reward_amount_180d) > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '涨粉情况' as tag, 'DAY_7' as cat,
         |           case when b.fans < 50 then 'COUNT_1_50'
         |                when b.fans < 100 then 'COUNT_50_100'
         |                when b.fans < 200 then 'COUNT_100_200'
         |                when b.fans < 300 then 'COUNT_200_300'
         |                when b.fans < 400 then 'COUNT_300_400'
         |                when b.fans < 500 then 'COUNT_400_500'
         |                when b.fans < 1000 then 'COUNT_500_1K'
         |                when b.fans < 2000 then 'COUNT_1K_2K'
         |                when b.fans < 5000 then 'COUNT_2K_5K'
         |                when b.fans < 10000 then 'COUNT_5K_1W'
         |                when b.fans < 30000 then 'COUNT_1W_3W'
         |                when b.fans < 50000 then 'COUNT_3W_5W'
         |                when b.fans < 100000 then 'COUNT_5W_10W'
         |                else 'COUNT_10W' end as grp
         |    from (
         |      select userId, fans_7d as fans
         |      from lofter.dws_par_user_fans_dd b
         |      where dt = '$dt' and fans_7d > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '涨粉情况' as tag, 'DAY_15' as cat,
         |           case when b.fans < 50 then 'COUNT_1_50'
         |                when b.fans < 100 then 'COUNT_50_100'
         |                when b.fans < 200 then 'COUNT_100_200'
         |                when b.fans < 300 then 'COUNT_200_300'
         |                when b.fans < 400 then 'COUNT_300_400'
         |                when b.fans < 500 then 'COUNT_400_500'
         |                when b.fans < 1000 then 'COUNT_500_1K'
         |                when b.fans < 2000 then 'COUNT_1K_2K'
         |                when b.fans < 5000 then 'COUNT_2K_5K'
         |                when b.fans < 10000 then 'COUNT_5K_1W'
         |                when b.fans < 30000 then 'COUNT_1W_3W'
         |                when b.fans < 50000 then 'COUNT_3W_5W'
         |                when b.fans < 100000 then 'COUNT_5W_10W'
         |                else 'COUNT_10W' end as grp
         |    from (
         |      select userId, fans_15d as fans
         |      from lofter.dws_par_user_fans_dd b
         |      where dt = '$dt' and fans_15d > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '涨粉情况' as tag, 'DAY_30' as cat,
         |           case when b.fans < 50 then 'COUNT_1_50'
         |                when b.fans < 100 then 'COUNT_50_100'
         |                when b.fans < 200 then 'COUNT_100_200'
         |                when b.fans < 300 then 'COUNT_200_300'
         |                when b.fans < 400 then 'COUNT_300_400'
         |                when b.fans < 500 then 'COUNT_400_500'
         |                when b.fans < 1000 then 'COUNT_500_1K'
         |                when b.fans < 2000 then 'COUNT_1K_2K'
         |                when b.fans < 5000 then 'COUNT_2K_5K'
         |                when b.fans < 10000 then 'COUNT_5K_1W'
         |                when b.fans < 30000 then 'COUNT_1W_3W'
         |                when b.fans < 50000 then 'COUNT_3W_5W'
         |                when b.fans < 100000 then 'COUNT_5W_10W'
         |                else 'COUNT_10W' end as grp
         |    from (
         |      select userId, fans_30d as fans
         |      from lofter.dws_par_user_fans_dd b
         |      where dt = '$dt' and fans_30d > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '涨粉情况' as tag, 'DAY_90' as cat,
         |           case when b.fans < 50 then 'COUNT_1_50'
         |                when b.fans < 100 then 'COUNT_50_100'
         |                when b.fans < 200 then 'COUNT_100_200'
         |                when b.fans < 300 then 'COUNT_200_300'
         |                when b.fans < 400 then 'COUNT_300_400'
         |                when b.fans < 500 then 'COUNT_400_500'
         |                when b.fans < 1000 then 'COUNT_500_1K'
         |                when b.fans < 2000 then 'COUNT_1K_2K'
         |                when b.fans < 5000 then 'COUNT_2K_5K'
         |                when b.fans < 10000 then 'COUNT_5K_1W'
         |                when b.fans < 30000 then 'COUNT_1W_3W'
         |                when b.fans < 50000 then 'COUNT_3W_5W'
         |                when b.fans < 100000 then 'COUNT_5W_10W'
         |                else 'COUNT_10W' end as grp
         |    from (
         |      select userId, fans_90d as fans
         |      from lofter.dws_par_user_fans_dd b
         |      where dt = '$dt' and fans_90d > 0
         |    ) b
         |
         |    union all
         |
         |    select b.userId, '涨粉情况' as tag, 'DAY_180' as cat,
         |           case when b.fans < 50 then 'COUNT_1_50'
         |                when b.fans < 100 then 'COUNT_50_100'
         |                when b.fans < 200 then 'COUNT_100_200'
         |                when b.fans < 300 then 'COUNT_200_300'
         |                when b.fans < 400 then 'COUNT_300_400'
         |                when b.fans < 500 then 'COUNT_400_500'
         |                when b.fans < 1000 then 'COUNT_500_1K'
         |                when b.fans < 2000 then 'COUNT_1K_2K'
         |                when b.fans < 5000 then 'COUNT_2K_5K'
         |                when b.fans < 10000 then 'COUNT_5K_1W'
         |                when b.fans < 30000 then 'COUNT_1W_3W'
         |                when b.fans < 50000 then 'COUNT_3W_5W'
         |                when b.fans < 100000 then 'COUNT_5W_10W'
         |                else 'COUNT_10W' end as grp
         |    from (
         |      select userId, fans_180d as fans
         |      from lofter.dws_par_user_fans_dd b
         |      where dt = '$dt' and fans_180d > 0
         |    ) b
         |
         |    union all
         |
         |    select userId, '涨粉情况-突破' as tag, 'DAY_7' as cat, cast(break_through as string) as grp
         |    from lofter.dws_par_user_fans_dd
         |      lateral view explode(array(
         |         if(fans_total - fans_7d < 50 and fans_total >= 50, 50, 0),
         |         if(fans_total - fans_7d < 100 and fans_total >= 100, 100, 0),
         |         if(fans_total - fans_7d < 500 and fans_total >= 500, 500, 0),
         |         if(fans_total - fans_7d < 1000 and fans_total >= 1000, 1000, 0),
         |         if(fans_total - fans_7d < 5000 and fans_total >= 5000, 5000, 0),
         |         if(fans_total - fans_7d < 10000 and fans_total >= 10000, 10000, 0),
         |         if(fans_total - fans_7d < 100000 and fans_total >= 100000, 100000, 0)
         |      )) as break_through
         |    where dt = '$dt' and fans_total > 0 and fans_7d > 0 and break_through > 0
         |
         |    union all
         |
         |    select userId, '涨粉情况-突破' as tag, 'DAY_15' as cat, cast(break_through as string) as grp
         |    from lofter.dws_par_user_fans_dd
         |      lateral view explode(array(
         |         if(fans_total - fans_15d < 50 and fans_total >= 50, 50, 0),
         |         if(fans_total - fans_15d < 100 and fans_total >= 100, 100, 0),
         |         if(fans_total - fans_15d < 500 and fans_total >= 500, 500, 0),
         |         if(fans_total - fans_15d < 1000 and fans_total >= 1000, 1000, 0),
         |         if(fans_total - fans_15d < 5000 and fans_total >= 5000, 5000, 0),
         |         if(fans_total - fans_15d < 10000 and fans_total >= 10000, 10000, 0),
         |         if(fans_total - fans_15d < 100000 and fans_total >= 100000, 100000, 0)
         |      )) as break_through
         |    where dt = '$dt' and fans_total > 0 and fans_15d > 0 and break_through > 0
         |
         |    union all
         |
         |    select userId, '涨粉情况-突破' as tag, 'DAY_30' as cat, cast(break_through as string) as grp
         |    from lofter.dws_par_user_fans_dd
         |      lateral view explode(array(
         |         if(fans_total - fans_30d < 50 and fans_total >= 50, 50, 0),
         |         if(fans_total - fans_30d < 100 and fans_total >= 100, 100, 0),
         |         if(fans_total - fans_30d < 500 and fans_total >= 500, 500, 0),
         |         if(fans_total - fans_30d < 1000 and fans_total >= 1000, 1000, 0),
         |         if(fans_total - fans_30d < 5000 and fans_total >= 5000, 5000, 0),
         |         if(fans_total - fans_30d < 10000 and fans_total >= 10000, 10000, 0),
         |         if(fans_total - fans_30d < 100000 and fans_total >= 100000, 100000, 0)
         |      )) as break_through
         |    where dt = '$dt' and fans_total > 0 and fans_30d > 0 and break_through > 0
         |
         |    union all
         |
         |    select userId, '涨粉情况-突破' as tag, 'DAY_90' as cat, cast(break_through as string) as grp
         |    from lofter.dws_par_user_fans_dd
         |      lateral view explode(array(
         |         if(fans_total - fans_90d < 50 and fans_total >= 50, 50, 0),
         |         if(fans_total - fans_90d < 100 and fans_total >= 100, 100, 0),
         |         if(fans_total - fans_90d < 500 and fans_total >= 500, 500, 0),
         |         if(fans_total - fans_90d < 1000 and fans_total >= 1000, 1000, 0),
         |         if(fans_total - fans_90d < 5000 and fans_total >= 5000, 5000, 0),
         |         if(fans_total - fans_90d < 10000 and fans_total >= 10000, 10000, 0),
         |         if(fans_total - fans_90d < 100000 and fans_total >= 100000, 100000, 0)
         |      )) as break_through
         |    where dt = '$dt' and fans_total > 0 and fans_90d > 0 and break_through > 0
         |
         |    union all
         |
         |    select userId, '涨粉情况-突破' as tag, 'DAY_180' as cat, cast(break_through as string) as grp
         |    from lofter.dws_par_user_fans_dd
         |      lateral view explode(array(
         |         if(fans_total - fans_180d < 50 and fans_total >= 50, 50, 0),
         |         if(fans_total - fans_180d < 100 and fans_total >= 100, 100, 0),
         |         if(fans_total - fans_180d < 500 and fans_total >= 500, 500, 0),
         |         if(fans_total - fans_180d < 1000 and fans_total >= 1000, 1000, 0),
         |         if(fans_total - fans_180d < 5000 and fans_total >= 5000, 5000, 0),
         |         if(fans_total - fans_180d < 10000 and fans_total >= 10000, 10000, 0),
         |         if(fans_total - fans_180d < 100000 and fans_total >= 100000, 100000, 0)
         |      )) as break_through
         |    where dt = '$dt' and fans_total > 0 and fans_180d > 0 and break_through > 0
         |
         |    union all
         |
         |    select a.userId, '加购行为' as tag, 'DAY_7' as cat, '1' as grp
         |    from (
         |        select userId
         |        from lofter.dws_par_user_ec_dd
         |        where dt = '$dt' and add_cart_pv_7d > 0
         |    ) a
         |
         |    union all
         |
         |    select a.userId, '加购行为' as tag, 'DAY_15' as cat, '1' as grp
         |    from (
         |        select userId
         |        from lofter.dws_par_user_ec_dd
         |        where dt = '$dt' and add_cart_pv_15d > 0
         |    ) a
         |
         |    union all
         |
         |    select a.userId, '加购行为' as tag, 'DAY_30' as cat, '1' as grp
         |    from (
         |        select userId
         |        from lofter.dws_par_user_ec_dd
         |        where dt = '$dt' and add_cart_pv_30d > 0
         |    ) a
         |
         |    union all
         |
         |    select a.userId, '加购行为' as tag, 'DAY_90' as cat, '1' as grp
         |    from (
         |        select userId
         |        from lofter.dws_par_user_ec_dd
         |        where dt = '$dt' and add_cart_pv_90d > 0
         |    ) a
         |
         |    union all
         |
         |    select a.userId, '加购行为' as tag, 'DAY_180' as cat, '1' as grp
         |    from (
         |        select userId
         |        from lofter.dws_par_user_ec_dd
         |        where dt = '$dt' and add_cart_pv_180d > 0
         |   ) a
         |
         |    union all
         |
         |    select userId, '圈层拉新用户' as tag, 'DAY_1' as cat, ip as grp
         |    from lofter.dws_ip_growth_dd
         |        lateral view explode(bitmap_to_array(new_bitmap_1d)) as userId
         |    where dt = '$dt'
         |
         |    union all
         |
         |    select userId, '圈层拉新用户' as tag, 'DAY_7' as cat, ip as grp
         |    from lofter.dws_ip_growth_dd
         |        lateral view explode(bitmap_to_array(new_bitmap_7d)) as userId
         |    where dt = '$dt'
         |
         |    union all
         |
         |    select userId, '圈层拉新用户' as tag, 'DAY_15' as cat, ip as grp
         |    from lofter.dws_ip_growth_dd
         |        lateral view explode(bitmap_to_array(new_bitmap_15d)) as userId
         |    where dt = '$dt'
         |
         |    union all
         |
         |    select userId, '圈层拉新用户' as tag, 'DAY_30' as cat, ip as grp
         |    from lofter.dws_ip_growth_dd
         |        lateral view explode(bitmap_to_array(new_bitmap_30d)) as userId
         |    where dt = '$dt'
         |
         |    union all
         |
         |    select userId, 'IP下30日自分享次数' as tag, ip as cat,
         |      case when pv < 3 then '1-3'
         |           when pv < 5 then '3-5'
         |           when pv < 8 then '5-8'
         |           when pv < 10 then '8-10'
         |           when pv < 15 then '10-15'
         |           else '15及以上' end as grp
         |    from (
         |        select post_ip as ip, userId, count(1) as pv
         |        from lofter.dwd_post_share_di
         |            lateral view explode(post_ips) as post_ip
         |        where dt <= '$dt' and dt > '$monthAgo'
         |        group by 1, 2
         |    ) t
         |
         |    union all
         |
         |    select userId, 'Tag下30日自分享次数' as tag, tag as cat,
         |      case when pv < 3 then '1-3'
         |           when pv < 5 then '3-5'
         |           when pv < 8 then '5-8'
         |           when pv < 10 then '8-10'
         |           when pv < 15 then '10-15'
         |           else '15及以上' end as grp
         |    from (
         |        select tag, userId, count(1) as pv
         |        from lofter.dwd_post_share_di
         |            lateral view explode(post_tags) as tag
         |        where dt <= '$dt' and dt > '$monthAgo'
         |        group by 1, 2
         |    ) t
         |
         |    union all
         |
         |    select userId, 'Tag下单条评论字数' as tag, tag as cat, grp
         |    from (
         |        select userId, tag,
         |            case when comment_size < 15 then '1-15'
         |                when comment_size < 30 then '15-30'
         |                when comment_size < 60 then '30-60'
         |                else '60及以上' end as grp
         |        from (
         |            select id, postId, publisherUserid as userId, length(content) as comment_size
         |            from lofter_db_dump.ods_db_post_response_nd
         |            where from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd') > date_sub('$dt', 365)
         |        ) a
         |        join (
         |            select id as postId, tag
         |            from lofter.dim_post
         |                lateral view explode(tags) as tag
         |        ) b on a.postId = b.postId
         |        left join (
         |            select comment_id
         |            from rec.rec_data_ecology_neg_comment_minning
         |            where day <= '$dt' and is_neg_comment = 1
         |        ) c on a.id = c.comment_id
         |        where a.comment_size > 0 and c.comment_id is null
         |        group by 1, 2, 3
         |    ) t
         |
         |    union all
         |
         |    select userId, 'IP下单条评论字数' as tag, ip as cat, grp
         |    from (
         |        select a.userId, b.ip,
         |            case when a.comment_size < 15 then '1-15'
         |                when a.comment_size < 30 then '15-30'
         |                when a.comment_size < 60 then '30-60'
         |                else '60及以上' end as grp
         |        from (
         |            select id, postId, publisherUserid as userId, length(content) as comment_size
         |            from lofter_db_dump.ods_db_post_response_nd
         |            where from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt' and
         |                from_unixtime(cast(publishTime/1000 as bigint), 'yyyy-MM-dd') > date_sub('$dt', 365)
         |        ) a
         |        join (
         |            select id as postId, ip
         |            from lofter.dim_post
         |                lateral view explode(ips) as ip
         |        ) b on a.postId = b.postId
         |        left join (
         |            select comment_id
         |            from rec.rec_data_ecology_neg_comment_minning
         |            where day <= '$dt' and is_neg_comment = 1
         |        ) c on a.id = c.comment_id
         |        where a.comment_size > 0 and c.comment_id is null
         |        group by 1, 2, 3
         |    ) t
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_1' as cat, grp
         |    from (
         |            select user_id as userId, if(last_login_time = '$dt', '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_7' as cat, grp
         |    from (
         |            select user_id as userId, if(datediff('$dt', last_login_time) < 7, '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_15' as cat, grp
         |    from (
         |            select user_id as userId, if(datediff('$dt', last_login_time) < 15, '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_30' as cat, grp
         |    from (
         |            select user_id as userId, if(datediff('$dt', last_login_time) < 30, '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_90' as cat, grp
         |    from (
         |            select user_id as userId, if(datediff('$dt', last_login_time) < 90, '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_180' as cat, grp
         |    from (
         |            select user_id as userId, if(datediff('$dt', last_login_time) < 180, '1', '0') as grp
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |
         |    union all
         |
         |    select b.userId, 'avg_login_active' as tag, 'DAY_ALL' as cat, '1' as grp
         |    from (
         |            select user_id as userId
         |            from avg.ads_user_last_login_time_nd
         |    ) b
         |) tt
         |where userId > 0L
         |""".stripMargin

    spark.sql(tagGroupLevel2)
  }
}
