package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupLevel1 {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .appName("User Login Info Extract")
      .getOrCreate()

    val dt = pargs.required("date")
    val dayAgo =  DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")
    val halfMonthAgo = DateTime.parse(dt).minusDays(15).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(dt).minusDays(7).toString("yyyy-MM-dd")
    val threeMonthAgo = DateTime.parse(dt).minusDays(90).toString("yyyy-MM-dd")
    val halfYearAgo = DateTime.parse(dt).minusDays(180).toString("yyyy-MM-dd")

    spark.sql("create temporary function to_bitmap as 'com.netease.wm.udf.bitmap.ToBitmapUDAF'")
    spark.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF'")
    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp' ")

    val userAttributeTagSql =
      """
        |
        |select userId,
        |       nvl(case gender when '男' then 1 when '女' then 2 else null end, gender) as gender,
        |       birth_year
        |from (
        |       select cast(a.user_id as bigint) as userId,
        |              first_value(if(pt_tag='edu_degree', tag_value, null), true) edu_degree,
        |              first_value(if(pt_tag='profession', tag_value, null), true) profession,
        |              first_value(if(pt_tag='city_reside', tag_value, null), true) city_reside,
        |              first_value(if(pt_tag='phone_price_180d', tag_value, null), true) phone_price_180d,
        |              first_value(if(pt_tag='birth_y', tag_value, null), true) birth_year,
        |              first_value(if(pt_tag='gender', tag_value, null), true) gender
        |       from lofter.dwb_par_lofter_tag_wd a
        |       where pt_d>='$twoWeeksAgo' and cast(a.user_id as bigint) > 0 and
        |             pt_tag in ('edu_degree','profession', 'city_reside', 'phone_price_180d','birth_y','gender')
        |       group by cast(a.user_id as bigint)
        |) t
        |where userId > 0
        |""".stripMargin

    spark.sql(userAttributeTagSql).createOrReplaceTempView("lofter_user_attribute_tag")

    val userLoginRegionTagSql =
      """
        |select userId, lastLoginIp,
        |       inline(Array(resolve_ip(lastLoginIp))) as (country, province, city)
        |from lofter_db_dump.ods_db_user_statistic_nd
        |where lastLoginTime > 0 and lastLoginIp is not null
        |""".stripMargin

    spark.sql(userLoginRegionTagSql).createOrReplaceTempView("lofter_user_login_region_tag")

    val tagGroupLevel1 =
     s"""
        |insert overwrite table lofter_dm.ads_par_user_tag_group_level1_dd partition(dt='$dt')
        |select userId, tag, grp
        |from (
        |
        |    select userId,
        |           'last_appversion' as tag,
        |           last_appversion as grp
        |    from lofter.dws_par_user_appversion_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId,
        |           'pve用户分群' as tag,
        |           '主页入口用户' as grp
        |    from lofter_dm.ads_pve_homepage_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId,
        |           'pve用户剩余体力' as tag,
        |           case when stamina = 0 then '0'
        |                when stamina > 0 and stamina <= 50 then '0-50'
        |                when stamina > 50 and stamina <= 100 then '50-100'
        |                when stamina > 100 and stamina <= 300 then '100-300'
        |                when stamina > 300 and stamina <= 500 then '300-500'
        |                else '500及以上'
        |            end as grp
        |    from lofter_db_dump.ods_db_pve_user_info_nd
        |    where sendmsgcount > 0
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, '回礼低意愿保护用户' as grp
        |    from lofter_dm.ads_gift_return_neg_protect_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, '低活用户' as grp
        |    from lofter.dwd_user_low_active_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, user_type as grp
        |    from lofter.dwb_par_lofter_music_user_label_di
        |    where pt_d='$dt' and user_type in ('音乐新客', '音乐召回')
        |
        |    union all
        |
        |    select blogId as userId, '定制化分群' as tag, '回礼好评创作者' as grp
        |    from lofter_dm.ads_gift_post_high_positive_vote_blogs_di
        |    where dt = '$dt'
        |    group by 1
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, '广告激励视频回礼文章扩容' as grp
        |    from lofter_dm.ads_ad_video_extend_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, '广告激励视频核心汇总' as grp
        |    from lofter_dm.ads_ad_video_extend_core_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '定制化分群' as tag, '云音乐会员积极互动鼓励' as grp
        |    from lofter_dm.ads_act_music_interaction_present_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select b.userId as userId, '安装应用' as tag, apk_name as grp
        |    from (
        |        select deviceUdid, apk_name
        |        from lofter.dwd_device_apk_install_dd
        |        where dt='$dt' and is_installed = 1
        |    ) a
        |    join (
        |        select sid as deviceUdid, cast(tid as bigint) as userId
        |        from lofter.dwd_device_mapping
        |        where sid_tp='deviceudid' and tid_tp = 'userid' and lastNo = 1
        |    ) b on a.deviceUdid = b.deviceUdid
        |    group by 1, 2, 3
        |
        |    union all
        |
        |    select userId, '个人橱窗用户' as tag, '1' as grp
        |    from lofter_dm.ads_act_showcase_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    SELECT
        |	         `userid` AS `userId`,
        |	         '付费会员状态' AS `tag`,
        |	         IF(`dt` >= '$dt','书城-生效中', '书城-失效') AS `grp`
        |    FROM (
        |    	    SELECT
        |    	    	    `userid`,
        |    	    	    FROM_UNIXTIME(CAST(`expiretime` / 1000 AS BIGINT), 'yyyy-MM-dd') AS `dt`
        |    	    FROM
        |    	    	   `lofter_db_dump`.`ods_db_blog_store_vip_nd`
        |    )
        |    GROUP BY `userId`, `tag`, `grp`
        |
        |    UNION ALL
        |
        |    SELECT
        |    	     `userid` AS `userId`,
        |    	     '付费会员状态' AS `tag`,
        |    	     IF(`dt` >= '$dt','粉丝-生效中', '粉丝-失效') AS `grp`
        |    FROM (
        |    	    SELECT
        |    	    	    `userid`,
        |    	    	    FROM_UNIXTIME(CAST(`expiretime` / 1000 AS BIGINT), 'yyyy-MM-dd') AS `dt`
        |    	    FROM
        |    	    	  `lofter_db_dump`.`ods_db_blog_fans_vip_nd`
        |    )
        |    GROUP BY `userId`, `tag`, `grp`
        |
        |    UNION ALL
        |
        |    SELECT
        |	         `userid` AS `userId`,
        |	         '付费会员sku' AS `tag`,
        |	         CASE WHEN `productid` IN (54,56,57,58,42,1001,47) THEN '书城-连续包月'
        |	              WHEN `productid` = 55 OR `productid` = 44 THEN '书城-连续包季'
        |               WHEN `vipdays` = 3 THEN '书城-3天'
        |               WHEN `vipdays` = 7 THEN '书城-7天'
        |               WHEN `vipdays` = 31 THEN '书城-1个月'
        |               WHEN `vipdays` = 93 THEN '书城-3个月'
        |               WHEN `vipdays` = 365 THEN '书城-12个月'
        |               ELSE '书城-其他' END AS `grp`
        |    FROM
        |	        `lofter_db_dump`.`ods_db_trade_store_vip_order_nd`
        |    WHERE FROM_UNIXTIME(CAST(`finishtime` / 1000 AS BIGINT), 'yyyy-MM-dd') = '$dt' AND `status` = 1
        |    GROUP BY `userId`, `tag`, `grp`
        |
        |    UNION ALL
        |
        |    SELECT
        |          `userid` AS `userId`,
        |          '付费会员sku' AS `tag`,
        |          CASE WHEN `vipdays` = 31 THEN '粉丝-1个月'
        |               WHEN `vipdays` = 93 THEN '粉丝-3个月'
        |               WHEN `vipdays` = 186 THEN '粉丝-6个月'
        |               WHEN `vipdays` = 365 THEN '粉丝-12个月'
        |               ELSE '粉丝-其他' END AS grp
        |    FROM
        |	        `lofter_db_dump`.`ods_db_trade_fans_vip_order_nd`
        |    WHERE FROM_UNIXTIME(CAST(`finishtime` / 1000 AS BIGINT), 'yyyy-MM-dd') = '$dt' AND `status` = 1
        |    GROUP BY `userId`, `tag`, `grp`
        |
        |    UNION ALL
        |
        |    select id as userId, 'ALL' as tag, '1' as grp
        |    from lofter.dim_user
        |
        |    UNION ALL
        |
        |    select id as userId, '是否匿名' as tag, cast(isAnonymous as string) as grp
        |    from lofter.dim_user
        |
        |    union all
        |
        |    select userId, '广告wf分群' as tag,
        |           concat_ws('-', os, positionId, groupId) as grp
        |    from lofter_dm.ads_ad_waterfull_user_group_di
        |    where dt = '$dt' and groupId >= 0 and positionId = 1000
        |
        |    union all
        |
        |    select accountId as userId, '最后活跃日期' as tag,
        |           from_unixtime(cast(app_time / 1000 as bigint), 'yyyy-MM-dd') as grp
        |    from lofter.dws_evt_login_user_last_dd
        |    where dt = '$dt' and app_time > 0
        |
        |    union all
        |
        |    select userId, '是否开过书城会员' as tag, '1' as grp
        |    from lofter_db_dump.ods_db_blog_store_vip_nd
        |
        |    union all
        |
        |    select userId, '测试账号' as tag, cast(userId as string) as grp
        |    from (
        |        SELECT cast(userId as bigint) as userId
        |              LATERAL VIEW explode(array(1288334362,2011534200,1995067397,1288334362,2011534200,1995067397,1944296620,2047691598,1287157289,1944296620,2047691598,1287157289)) users AS userId
        |    ) t
        |
        |    union all
        |
        |    select userId, '性别' as tag,
        |           case when gender = 1 then '男'
        |                when gender = 2 then '女'
        |                end as grp
        |    from lofter_user_attribute_tag
        |
        |    union all
        |
        |    select userId, '创作内容类型' as tag, content_type as grp
        |    from lofter.dws_par_user_post_dd
        |        lateral view explode(content_types) tb as content_type
        |    where dt='$dt'
        |
        |    union all
        |
        |    select id as userId, '达人' as tag, '1' as grp
        |    from lofter.dim_blog
        |    where size(authDomainIds) > 0
        |
        |    union all
        |
        |    select userId, '常态化糖果券包购买档位' as tag,
        |          concat(platform, ':', cast(price_band as string), ':', if(is_test > 0, 'A', 'B')) as grp
        |    from lofter_dm.ads_act_coupon_daily_user_group_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '元旦糖果券包购买档位' as tag, cast(price_band as string) as grp
        |    from lofter_dm.ads_act_coupon_new_year_user_group_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '春节糖果券包购买档位' as tag, cast(price_band as string) as grp
        |    from lofter_dm.ads_act_coupon_spring_user_group_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '暑期券包档位' as tag, concat(platform, '-', user_type) as grp
        |    from lofter_dm.ads_act_coupon_summer_holiday_user_group_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '虚拟人用户' as tag, activate_type as grp
        |    from lofter_dm.ads_act_pve_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '纸片人用户' as tag, activate_type as grp
        |    from lofter_dm.ads_act_paper_man_user_group_di
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select blogId as userId, '灯塔计划潜力创作者' as tag, level as grp
        |    from lofter_dm.ads_creator_lighthouse_potential_di
        |    where dt = '$dt'
        |    group by blogId, level
        |
        |    union all
        |
        |    select userId, '同人创作者' as tag, '1' as grp
        |    from lofter.dws_par_creator_dd
        |    where is_cp > 0 and dt='$dt'
        |
        |    union all
        |
        |    select blogId as userId, '签约创作者' as tag, '1' as grp
        |    from lofter_db_dump.ods_db_sign_authenticate_nd
        |    where status = 3
        |    group by blogId
        |
        |    union all
        |
        |    select userId, '创作领域' as tag, domain as grp
        |    from lofter.dws_par_creator_dd
        |         lateral view explode(post_top_domains) as domain
        |    where dt='$dt'
        |
        |    union all
        |
        |    select userId, '擅长IP' as tag, ip as grp
        |    from lofter.dws_par_creator_dd
        |         lateral view explode(post_top_ips) as ip
        |    where dt='$dt'
        |
        |    union all
        |
        |    select userId, '创作者等级' as tag, concat("LEVEL_", level) as grp
        |    from lofter.dws_par_creator_dd
        |    where dt='$dt' and level is not null
        |
        |    union all
        |
        |    select blogId as userId, '新创作者等级' as tag,
        |           case creatorStatus when '10' then '发文吧鸽'
        |                               when '20' then '回来吧鸽'
        |                               when '30' then '求你了鸽'
        |                               when '40' then '出山鸽'
        |                               when '50' then '成长鸽'
        |                               when '60' then '守护鸽'
        |                               when '70' then '起飞鸽'
        |                               when '80' then '火箭鸽'
        |                               when '90' then '太太鸽'
        |                               when '100' then '宗师鸽'
        |                               when '110' then '传奇鸽'
        |                               else '未知' end as grp
        |    from lofter_dm.ads_creator_ecology_identity_dd
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '外部引入' as tag, '1' as grp
        |    from lofter_db_dump.ods_db_cmb_business_introduction_nd
        |    where status = 0 and level is not null
        |
        |    union all
        |
        |    select userId, '订阅标签' as tag, tagName as grp
        |    from lofter_db_dump.ods_db_favorite_tag_nd
        |
        |    union all
        |
        |    select userId, '消费偏好' as tag, post_content_type as grp
        |    from lofter.dwd_post_browse_di
        |    where dt <= '$dt' and dt > '$halfYearAgo' and is_real > 0 and userId > 0
        |    group by userId, post_content_type
        |
        |    union all
        |
        |    select a.userId, '安全等级' as tag,
        |           case when a.grade = 0 then 'LEVEL_GREEN'
        |                when a.grade = 1 then 'LEVEL_YELLOW'
        |                when a.grade = 2 then 'LEVEL_RED'
        |                when a.grade = 3 then 'LEVEL_HIGHT_GREEN'
        |                end as grp
        |    from (
        |        select blogId as userId, grade
        |        from lofter.dws_user_security_level_di
        |        where dt = '$dt' and grade in (0,1,2,3)
        |    ) a
        |
        |    union all
        |
        |    select id as userId, '注册天数' as tag,
        |           datediff('$dt', createDate) + 1 as grp
        |    from lofter.dim_user
        |    where datediff('$dt', createDate) >= 0
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_1' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) < 1
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_3' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) < 3
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_7' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) < 7
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_15' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) < 15
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_30' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) < 30
        |
        |    union all
        |
        |    select id as userId, '注册时间' as tag, 'DAY_30+' as grp
        |    from lofter.dim_user where datediff('$dt', createDate) >= 30
        |
        |    union all
        |
        |    select userId, '生命周期' as tag, life_cycle_type as grp
        |    from lofter.dws_user_life_circle_judge_dd
        |    where dt='$dt' and life_cycle_type in ('导入期', '低价值', '成长期', '成熟期', '休眠期', '沉默期', '流失期')
        |
        |    union all
        |
        |    select userId, '入池文章' as tag,
        |            case when post_count_recommend_pool_180d < 3 then 'COUNT_1_3'
        |                 when post_count_recommend_pool_180d < 7 then 'COUNT_3_7'
        |                 else 'COUNT_7' end as grp
        |    from lofter.dws_par_creator_level_scoring_dd
        |    where dt='$dt' and post_count_recommend_pool_180d > 0
        |
        |    union all
        |
        |    select userId, '点赞量' as tag,
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
        |    select userId, '蓝手量' as tag,
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
        |    select userId, '收藏量' as tag,
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
        |    select userId, '地域' as tag, city as grp
        |    from lofter_user_login_region_tag
        |    where city is not null
        |
        |    union all
        |
        |    select userId, '地域' as tag, province as grp
        |    from lofter_user_login_region_tag
        |    where province is not null
        |
        |    union all
        |
        |    select userId, '地域' as tag, country as grp
        |    from lofter_user_login_region_tag
        |    where country is not null
        |
        |    union all
        |
        |    select userId, '年龄' as tag,
        |           case when string(year(now()) - birth_year) < 15 then 'AGE_0_15'
        |                when string(year(now()) - birth_year) < 18 then 'AGE_15_18'
        |                when string(year(now()) - birth_year) < 22 then 'AGE_18_22'
        |                when string(year(now()) - birth_year) < 30 then 'AGE_22_30'
        |                when string(year(now()) - birth_year) < 40 then 'AGE_30_40'
        |                else 'AGE_40' end as grp
        |    from lofter_user_attribute_tag
        |    where birth_year is not null and birth_year <= year(now())
        |
        |    union all
        |
        |    select userId, '擅长类目' as tag, categoryCombo as grp
        |    from (
        |         select userId, category
        |         from lofter.dws_par_creator_dd
        |              lateral view explode(post_top_categories) as category
        |         where dt='$dt'
        |    ) t lateral view explode(array(category, split(category, '-')[0], regexp_extract(category, '[^\\-]+-[^\\-]+',0))) t as categoryCombo
        |
        |    union all
        |
        |    select b.userId, '累计粉丝' as tag,
        |           case when b.fans_total < 100 then 'COUNT_1_100'
        |                when b.fans_total < 200 then 'COUNT_100_200'
        |                when b.fans_total < 300 then 'COUNT_200_300'
        |                when b.fans_total < 400 then 'COUNT_300_400'
        |                when b.fans_total < 500 then 'COUNT_400_500'
        |                when b.fans_total < 1000 then 'COUNT_500_1K'
        |                when b.fans_total < 2000 then 'COUNT_1K_2K'
        |                when b.fans_total < 5000 then 'COUNT_2K_5K'
        |                when b.fans_total < 10000 then 'COUNT_5K_1W'
        |                when b.fans_total < 30000 then 'COUNT_1W_3W'
        |                when b.fans_total < 50000 then 'COUNT_3W_5W'
        |                when b.fans_total < 100000 then 'COUNT_5W_10W'
        |                else 'COUNT_10W' end as grp
        |    from (
        |      select userId, fans_total
        |      from lofter.dws_par_user_fans_dd b
        |      where dt = '$dt' and fans_total > 0
        |    ) b
        |
        |    union all
        |
        |    select b.userId, '互动粉丝' as tag,
        |           case when b.fans_total < 100 then 'COUNT_1_100'
        |                when b.fans_total < 200 then 'COUNT_100_200'
        |                when b.fans_total < 300 then 'COUNT_200_300'
        |                when b.fans_total < 400 then 'COUNT_300_400'
        |                when b.fans_total < 500 then 'COUNT_400_500'
        |                when b.fans_total < 1000 then 'COUNT_500_1K'
        |                when b.fans_total < 2000 then 'COUNT_1K_2K'
        |                when b.fans_total < 5000 then 'COUNT_2K_5K'
        |                when b.fans_total < 10000 then 'COUNT_5K_1W'
        |                when b.fans_total < 30000 then 'COUNT_1W_3W'
        |                when b.fans_total < 50000 then 'COUNT_3W_5W'
        |                when b.fans_total < 100000 then 'COUNT_5W_10W'
        |                else 'COUNT_10W' end as grp
        |    from (
        |      select userId, hd_fans_1y as fans_total
        |      from lofter.dws_par_creator_level_scoring_dd
        |      where dt='$dt' and hd_fans_1y > 0
        |    ) b
        |
        |    union all
        |
        |    select userId, '市集消费类目' as tag, level as grp
        |    from lofter.dws_par_user_ec_dd
        |         lateral view explode(pay_product_category_level1) as level
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '市集消费类目' as tag, level as grp
        |    from lofter.dws_par_user_ec_dd
        |         lateral view explode(pay_product_category_level2) as level
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select userId, '市集消费类目' as tag, level as grp
        |    from lofter.dws_par_user_ec_dd
        |         lateral view explode(pay_product_category_level3) as level
        |    where dt = '$dt'
        |
        |    union all
        |
        |    select cast(userId as bigint) as userId, 'recomAlgoTags' as tag, tag as grp
        |    from rec.rec_alg_active_user_under_tag
        |         lateral view explode(split(interest_user_group, ',')) as userId
        |    where length(tag) > 0 and cast(userId as bigint) > 0 and
        |          day >= '$dayAgo'
        |
        |    union all
        |    select accountId as userId, 'deviceType' as tag,
        |           case app_client_type when 'IPHONE' then 'IOS'
        |                                when 'IPAD' then 'IOS'
        |                                else app_client_type end as grp
        |    from lofter.dws_evt_login_user_last_dd
        |    where dt = '$dt' and length(app_client_type) > 0
        |
        |    union all
        |
        |    select userId, 'ad_user_group' as tag,
        |           ad_group_type as grp
        |    from lofter_dm.ads_ad_user_group_di
        |    where dt = '$dt' and length(ad_group_type) > 0
        |
        |    union all
        |
        |    select blogId as userId, '屏蔽标签' as tag, tagName as grp
        |    from (
        |        select blogId, tags
        |        from lofter_db_dump.ods_db_blog_misc_setting_nd
        |            lateral view explode(split(multiFields, '\\\\|')) as tags
        |        where multiFields like '%56::%'
        |    ) t
        |    lateral view explode(split(substr(tags, 5), ',')) as tagName
        |    group by blogId, tagName
        |
        |    union all
        |
        |    select blogId as userId, '嗑CP品类' as tag, special_category as grp
        |    from lofter_dm.ads_creator_double_perspective_user_di
        |         lateral view explode(split(double_perspective_type, '\\\\|')) as special_category
        |    where dt = '$dt' and length(special_category) > 0
        |
        |    union all
        |
        |    select cast(comment_user_id as bigint) as userId, 'IP下正向长评用户' as tag, ip as grp
        |    from rec.rec_data_ecology_comment_detail
        |         lateral view explode(ips_v2) as ip
        |    where day <= '$dt' and day > '$monthAgo' and concat_ws(',', tags) not rlike '反|怼'  and is_quality_comment > 0
        |    group by cast(comment_user_id as bigint), ip
        |
        |    union all
        |
        |    select cast(comment_user_id as bigint) as userId, 'Tag下正向长评用户' as tag, tag as grp
        |    from rec.rec_data_ecology_comment_detail
        |         lateral view explode(tags) as tag
        |    where day <= '$dt' and day > '$monthAgo' and concat_ws(',', tags) not rlike '反|怼'  and is_quality_comment > 0
        |    group by cast(comment_user_id as bigint), tag
        |
        |    union all
        |
        |    select userid,tag,grp from lofter_dm.ads_vc_user_tag_group_level1_dd where dt='$dt'
        |
        |    union all
        |
        |    select userid,'pve_interflow_users' as tag,'ALL' as grp from lofter_db_dump.ods_db_pve_user_bind_nd
        |    where FROM_UNIXTIME(createtime / 1000,'yyyy-MM-dd') <= '$dt'
        |
        |    union all
        |
        |    select user_id as userId, 'avg_register_days' as tag,
        |            datediff('$dt', create_time) + 1 as grp
        |    from avg.ads_user_page_nd
        |    where datediff('$dt', create_time) >= 0
        |
        |    union all
        |
        |    select user_id as userId,
        |           'avg_last_login_date' as tag,
        |           last_login_time as grp
        |    from avg.ads_user_last_login_time_nd
        |
        |) t
        |where userId > 0L
        |""".stripMargin

    spark.sql(tagGroupLevel1)
  }
}
