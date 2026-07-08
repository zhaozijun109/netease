package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupMetricLevel2 {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val sparkSession = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .appName("User Login Info Value Extract")
      .getOrCreate()

    val dt = pargs.required("date")
    val day90Ago = DateTime.parse(dt).minusDays(90).toString("yyyy-MM-dd")
    val day30Ago = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")
    val day15Ago = DateTime.parse(dt).minusDays(15).toString("yyyy-MM-dd")
    val day7Ago = DateTime.parse(dt).minusDays(7).toString("yyyy-MM-dd")

    val userTagGroupMetricLevel2 =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level2_dd PARTITION(dt='$dt')
         |
         |select id as userId, 'ALL' as tag, 'DAY_ALL' as cat, 1 as value
         |from lofter.dim_user
         |where isAnonymous = 0
         |
         |union all
         |
         |SELECT userId,
         |       'pve_user_top_up_cnt' AS tag,
         |       'DAY_7' AS cat,
         |       count(1) as value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day7Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_top_up_cnt' AS tag,
         |       'DAY_15' AS cat,
         |       count(1) as value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day15Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_top_up_cnt' AS tag,
         |       'DAY_30' AS cat,
         |       count(1) as value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day30Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_top_up_cnt' AS tag,
         |       'DAY_90' AS cat,
         |       count(1) as value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day90Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_chats_cnt' AS tag,
         |       'DAY_7' AS cat,
         |       count(distinct dt) as value
         |FROM lofter.dws_pve_user_role_chats_di
         |WHERE dt >= '$day7Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_chats_cnt' AS tag,
         |       'DAY_15' AS cat,
         |       count(distinct dt) as value
         |FROM lofter.dws_pve_user_role_chats_di
         |WHERE dt >= '$day15Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_chats_cnt' AS tag,
         |       'DAY_30' AS cat,
         |       count(distinct dt) as value
         |FROM lofter.dws_pve_user_role_chats_di
         |WHERE dt >= '$day30Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_chats_cnt' AS tag,
         |       'DAY_90' AS cat,
         |       count(distinct dt) as value
         |FROM lofter.dws_pve_user_role_chats_di
         |WHERE dt >= '$day90Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_interview_cnt' AS tag,
         |       'DAY_7' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_interview_di WHERE dt >= '$day7Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_interview_cnt' AS tag,
         |       'DAY_15' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_interview_di WHERE dt >= '$day15Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_interview_cnt' AS tag,
         |       'DAY_30' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_interview_di WHERE dt >= '$day30Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_interview_cnt' AS tag,
         |       'DAY_90' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_interview_di WHERE dt >= '$day90Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_access_free_cp_cnt' AS tag,
         |       'DAY_7' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_access_free_cp_di WHERE dt >= '$day7Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_access_free_cp_cnt' AS tag,
         |       'DAY_15' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_access_free_cp_di WHERE dt >= '$day15Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_access_free_cp_cnt' AS tag,
         |       'DAY_30' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_access_free_cp_di WHERE dt >= '$day30Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'pve_user_access_free_cp_cnt' AS tag,
         |       'DAY_90' AS cat,
         |       cnt AS value
         |FROM (
         |      SELECT userId,
         |             COUNT(1) AS cnt
         |      FROM (
         |          SELECT userId FROM lofter_dm.ads_pve_user_access_free_cp_di WHERE dt >= '$day90Ago' AND dt <= '$dt'
         |      ) t1
         |      GROUP BY userId
         |) t3
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_7' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_7d, 0) + nvl(fans_consume_7d,0) + nvl(coupon_consume_7d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_1' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_1d, 0) + nvl(fans_consume_1d,0) + nvl(coupon_consume_1d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_15' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_15d, 0) + nvl(fans_consume_15d,0) + nvl(coupon_consume_15d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_30' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_30d, 0) + nvl(fans_consume_30d,0) + nvl(coupon_consume_30d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_90' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_90d, 0) + nvl(fans_consume_90d,0) + nvl(coupon_consume_90d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_180' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_180d, 0) + nvl(fans_consume_180d,0) + nvl(coupon_consume_180d,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |UNION ALL
         |select userId,
         |       '付费金额' as tag,
         |       'DAY_ALL' as cat,
         |       money as value
         |from (
         |    select userId, (nvl(coin_consume_std, 0) + nvl(fans_consume_std,0) + nvl(coupon_consume_std,0)) as money
         |    from lofter.dws_par_user_pay_dd
         |    where dt='$dt'
         |) t
         |where money > 0
         |
         |union all
         |
         |select userid,tag,cat,value from lofter_dm.ads_vc_user_tag_metric_level2_dd where dt = '$dt'
         |
         |union all
         |
         |select userid,tag,cat,value from lofter_dm.ads_pve_chat_new_users_metric_dd where dt = '$dt'
         |
         |union all
         |
         |select user_id as userid, 'avg_user_charge' as tag, time_tag as cat, cast(coin as double) as value
         |from avg.ads_user_charge_log_nd
         |
         |union all
         |
         |select user_id as userid, 'avg_user_charge' as tag, 'DAY_ALL' as cat, charge_amount as value
         |from avg.ads_user_pay_charge_amount_nd
         |
         |union all
         |
         |select user_id as userid, 'avg_user_pay' as tag, time_tag as cat, cast(coin as double) as value
         |from avg.ads_user_pay_log_nd
         |
         |union all
         |select user_id as userid, 'avg_user_pay' as tag, 'DAY_ALL' as cat, pay_amount as value
         |from avg.ads_user_pay_charge_amount_nd
         |
         |union all
         |
         |select user_id as userid, 'avg_user_sign' as tag, 'DAY_ALL' as cat, cast(total_count as double) as value
         |from avg.ads_user_sign_in_days_nd
         |
         |union all
         |select user_id as userid, 'avg_user_cycle_sign' as tag, 'DAY_ALL' as cat, cast(round_count as double) as value
         |from avg.ads_user_sign_in_days_nd
         |""".stripMargin

    sparkSession.sql(userTagGroupMetricLevel2)
    sparkSession.close()
  }
}
