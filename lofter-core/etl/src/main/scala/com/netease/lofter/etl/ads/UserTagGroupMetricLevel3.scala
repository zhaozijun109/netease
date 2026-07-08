package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupMetricLevel3 {
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
    val day3Ago = DateTime.parse(dt).minusDays(3).toString("yyyy-MM-dd")

    val pveUserTradeProductGroup =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt='$dt', tag='pve_user_trade_product')
         |SELECT  userId,
         |        'DAY_7'       AS cat,
         |        productType   AS subCat,
         |        count(1)      AS value
         |FROM (
         |        SELECT  userId,
         |                case when productType = 0 and productId = 1 then '3元充值'
         |                     when productType = 0 and productId = 2 then '6元充值'
         |                     when productType = 0 and productId = 3 then '12元充值'
         |                     when productType = 0 and productId = 4 then '18元充值'
         |                     when productType = 0 and productId = 5 then '36元充值'
         |                     when productType = 0 and productId = 6 then '88元充值'
         |                     when productType = 0 and productId = 7 then '6元首充'
         |                     when productType = 0 and productId = 8 then '12元首充'
         |                     when productType = 0 and productId = 9 then '18元首充'
         |                     when productType = 0 and productId = 10 then '36元首充'
         |                     when productType = 0 and productId = 11 then '88元首充'
         |                     when productType = 0 and productId = 12 then '3.8周卡'
         |                     when productType = 0 and productId = 13 then '9.9月卡'
         |                     when productType = 1 and (
         |                             productId = 100 or
         |                             productId = 101 or
         |                             productId = 102 or
         |                             productId = 103 or
         |                             productId = 104 or
         |                             productId = 105 or
         |                             productId = 106 or
         |                             productId = 107 or
         |                             productId = 108
         |                         ) then '忘忧草'
         |                     when productType = 2 and productId = 100 then '新人礼包'
         |                     else '体力值'
         |                end as productType
         |        FROM lofter.dwd_pve_user_amount_info_di
         |        WHERE dt >= '$day7Ago' and dt <= '$dt'
         |) t1
         |GROUP BY userId, productType
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        'DAY_15'      AS cat,
         |        productType   AS subCat,
         |        count(1)      AS value
         |FROM (
         |        SELECT  userId,
         |                case when productType = 0 and productId = 1 then '3元充值'
         |                     when productType = 0 and productId = 2 then '6元充值'
         |                     when productType = 0 and productId = 3 then '12元充值'
         |                     when productType = 0 and productId = 4 then '18元充值'
         |                     when productType = 0 and productId = 5 then '36元充值'
         |                     when productType = 0 and productId = 6 then '88元充值'
         |                     when productType = 0 and productId = 7 then '6元首充'
         |                     when productType = 0 and productId = 8 then '12元首充'
         |                     when productType = 0 and productId = 9 then '18元首充'
         |                     when productType = 0 and productId = 10 then '36元首充'
         |                     when productType = 0 and productId = 11 then '88元首充'
         |                     when productType = 0 and productId = 12 then '3.8周卡'
         |                     when productType = 0 and productId = 13 then '9.9月卡'
         |                     when productType = 1 and (
         |                             productId = 100 or
         |                             productId = 101 or
         |                             productId = 102 or
         |                             productId = 103 or
         |                             productId = 104 or
         |                             productId = 105 or
         |                             productId = 106 or
         |                             productId = 107 or
         |                             productId = 108
         |                         ) then '忘忧草'
         |                     when productType = 2 and productId = 100 then '新人礼包'
         |                     else '体力值'
         |                end as productType
         |        FROM lofter.dwd_pve_user_amount_info_di
         |        WHERE dt >= '$day15Ago' and dt <= '$dt'
         |) t1
         |GROUP BY userId, productType
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        'DAY_30'      AS cat,
         |        productType   AS subCat,
         |        count(1)      AS value
         |FROM (
         |        SELECT  userId,
         |                case when productType = 0 and productId = 1 then '3元充值'
         |                     when productType = 0 and productId = 2 then '6元充值'
         |                     when productType = 0 and productId = 3 then '12元充值'
         |                     when productType = 0 and productId = 4 then '18元充值'
         |                     when productType = 0 and productId = 5 then '36元充值'
         |                     when productType = 0 and productId = 6 then '88元充值'
         |                     when productType = 0 and productId = 7 then '6元首充'
         |                     when productType = 0 and productId = 8 then '12元首充'
         |                     when productType = 0 and productId = 9 then '18元首充'
         |                     when productType = 0 and productId = 10 then '36元首充'
         |                     when productType = 0 and productId = 11 then '88元首充'
         |                     when productType = 0 and productId = 12 then '3.8周卡'
         |                     when productType = 0 and productId = 13 then '9.9月卡'
         |                     when productType = 1 and (
         |                             productId = 100 or
         |                             productId = 101 or
         |                             productId = 102 or
         |                             productId = 103 or
         |                             productId = 104 or
         |                             productId = 105 or
         |                             productId = 106 or
         |                             productId = 107 or
         |                             productId = 108
         |                         ) then '忘忧草'
         |                     when productType = 2 and productId = 100 then '新人礼包'
         |                     else '体力值'
         |                end as productType
         |        FROM lofter.dwd_pve_user_amount_info_di
         |        WHERE dt >= '$day30Ago' and dt <= '$dt'
         |) t1
         |GROUP BY userId, productType
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        'DAY_90'      AS cat,
         |        productType   AS subCat,
         |        count(1)      AS value
         |FROM (
         |        SELECT  userId,
         |                case when productType = 0 and productId = 1 then '3元充值'
         |                     when productType = 0 and productId = 2 then '6元充值'
         |                     when productType = 0 and productId = 3 then '12元充值'
         |                     when productType = 0 and productId = 4 then '18元充值'
         |                     when productType = 0 and productId = 5 then '36元充值'
         |                     when productType = 0 and productId = 6 then '88元充值'
         |                     when productType = 0 and productId = 7 then '6元首充'
         |                     when productType = 0 and productId = 8 then '12元首充'
         |                     when productType = 0 and productId = 9 then '18元首充'
         |                     when productType = 0 and productId = 10 then '36元首充'
         |                     when productType = 0 and productId = 11 then '88元首充'
         |                     when productType = 0 and productId = 12 then '3.8周卡'
         |                     when productType = 0 and productId = 13 then '9.9月卡'
         |                     when productType = 1 and (
         |                             productId = 100 or
         |                             productId = 101 or
         |                             productId = 102 or
         |                             productId = 103 or
         |                             productId = 104 or
         |                             productId = 105 or
         |                             productId = 106 or
         |                             productId = 107 or
         |                             productId = 108
         |                         ) then '忘忧草'
         |                     when productType = 2 and productId = 100 then '新人礼包'
         |                     else '体力值'
         |                end as productType
         |        FROM lofter.dwd_pve_user_amount_info_di
         |        WHERE dt >= '$day90Ago' and dt <= '$dt'
         |) t1
         |GROUP BY userId, productType
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'DAY_7'       AS cat,
         |       'all'         AS subCat,
         |       count(1)      AS value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day7Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'DAY_15'       AS cat,
         |       'all'         AS subCat,
         |       count(1)      AS value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day15Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |
         |SELECT userId,
         |       'DAY_30'       AS cat,
         |       'all'         AS subCat,
         |       count(1)      AS value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day30Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |UNION ALL
         |SELECT userId,
         |       'DAY_90'       AS cat,
         |       'all'         AS subCat,
         |       count(1)      AS value
         |FROM lofter.dwd_pve_user_amount_info_di
         |WHERE dt >= '$day90Ago' and dt <= '$dt'
         |GROUP BY userId
         |
         |""".stripMargin

    val pveUserRoleLastChatsGroup =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt='$dt', tag='pve_user_last_chats')
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_3' AS count_period,
         |                roleId,
         |                roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day3Ago' and dt <= '$dt' and roleDef = 'ogc'
         |        GROUP BY userId, roleId, roleName
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_7' AS count_period,
         |                roleId,
         |                roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day7Ago' and dt <= '$dt' and roleDef = 'ogc'
         |        GROUP BY userId, roleId, roleName
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_15' AS count_period,
         |                roleId,
         |                roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day15Ago' and dt <= '$dt' and roleDef = 'ogc'
         |        GROUP BY userId, roleId, roleName
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_30' AS count_period,
         |                roleId,
         |                roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day30Ago' and dt <= '$dt' and roleDef = 'ogc'
         |        GROUP BY userId, roleId, roleName
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_90' AS count_period,
         |                roleId,
         |                roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day90Ago' and dt <= '$dt' and roleDef = 'ogc'
         |        GROUP BY userId, roleId, roleName
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_3' AS count_period,
         |                CASE WHEN roleDef = 'ogc' THEN 0
         |                     WHEN roleDef = 'hc' THEN 1
         |                     WHEN roleDef = 'pc' THEN 2
         |                     ELSE 3
         |                END AS roleId,
         |                roleDef AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day3Ago' and dt <= '$dt'
         |        GROUP BY userId, roleDef
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_7' AS count_period,
         |                CASE WHEN roleDef = 'ogc' THEN 0
         |                     WHEN roleDef = 'hc' THEN 1
         |                     WHEN roleDef = 'pc' THEN 2
         |                     ELSE 3
         |                END AS roleId,
         |                roleDef AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day7Ago' and dt <= '$dt'
         |        GROUP BY userId, roleDef
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_15' AS count_period,
         |                CASE WHEN roleDef = 'ogc' THEN 0
         |                     WHEN roleDef = 'hc' THEN 1
         |                     WHEN roleDef = 'pc' THEN 2
         |                     ELSE 3
         |                END AS roleId,
         |                roleDef AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day15Ago' and dt <= '$dt'
         |        GROUP BY userId, roleDef
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_30' AS count_period,
         |                CASE WHEN roleDef = 'ogc' THEN 0
         |                     WHEN roleDef = 'hc' THEN 1
         |                     WHEN roleDef = 'pc' THEN 2
         |                     ELSE 3
         |                END AS roleId,
         |                roleDef AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day30Ago' and dt <= '$dt'
         |        GROUP BY userId, roleDef
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_90' AS count_period,
         |                CASE WHEN roleDef = 'ogc' THEN 0
         |                     WHEN roleDef = 'hc' THEN 1
         |                     WHEN roleDef = 'pc' THEN 2
         |                     ELSE 3
         |                END AS roleId,
         |                roleDef AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day90Ago' and dt <= '$dt'
         |        GROUP BY userId, roleDef
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_3' AS count_period,
         |                3 AS roleId,
         |                'all' AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day3Ago' and dt <= '$dt'
         |        GROUP BY userId
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_7' AS count_period,
         |                3 AS roleId,
         |                'all' AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day7Ago' and dt <= '$dt'
         |        GROUP BY userId
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_15' AS count_period,
         |                3 AS roleId,
         |                'all' AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day15Ago' and dt <= '$dt'
         |        GROUP BY userId
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_30' AS count_period,
         |                3 AS roleId,
         |                'all' AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day30Ago' and dt <= '$dt'
         |        GROUP BY userId
         |) t1
         |
         |UNION ALL
         |
         |SELECT  userId,
         |        count_period              AS cat,
         |        roleName                  AS subCat,
         |        DATEDIFF('$dt', lastDay)  AS value
         |FROM (
         |        SELECT  userId,
         |                'DAY_90' AS count_period,
         |                3 AS roleId,
         |                'all' AS roleName,
         |                max(dt) AS lastDay
         |        FROM lofter.dws_pve_user_role_chats_di
         |        WHERE dt >= '$day90Ago' and dt <= '$dt'
         |        GROUP BY userId
         |) t1
         |
         |""".stripMargin

    val pveUserRoleChatsGroup =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt='$dt', tag='pve_user_role_chats')
         |SELECT  `userId`,
         |        `count_period`    AS `cat`,
         |        `roleName`        AS `subCat`,
         |        `total_chats`     AS `value`
         |FROM (
         |    SELECT userId,
         |           'DAY_1' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt = '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_3' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day3Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_1' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt = '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_3' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day3Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_1' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt = '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_3' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day3Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_chats)  AS total_chats
         |    FROM lofter.dws_pve_user_role_chats_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |) tmp1
         |
         |""".stripMargin

    val pveUserRoleAmountGroup =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt='$dt', tag='pve_user_role_amount')
         |SELECT  `userId`,
         |        `count_period`    AS `cat`,
         |        `roleName`        AS `subCat`,
         |        `total_amount`    AS `value`
         |FROM (
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           roleId,
         |           roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt' and roleDef = 'ogc'
         |    GROUP BY userId, roleId, roleName
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           CASE WHEN roleDef = 'ogc' THEN 0
         |                WHEN roleDef = 'hc' THEN 1
         |                WHEN roleDef = 'pc' THEN 2
         |                ELSE 3
         |           END AS roleId,
         |           roleDef AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt'
         |    GROUP BY userId, roleDef
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_7' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day7Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_15' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day15Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_30' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day30Ago' and dt <= '$dt'
         |    GROUP BY userId
         |
         |    UNION ALL
         |
         |    SELECT userId,
         |           'DAY_90' AS count_period,
         |           3 AS roleId,
         |           'all' AS roleName,
         |           SUM(total_amount)  AS total_amount
         |    FROM lofter.dws_pve_user_role_amount_di
         |    WHERE dt >= '$day90Ago' and dt <= '$dt'
         |    GROUP BY userId
         |) tmp1
         |
         |""".stripMargin

    val vcUserTagDataMetricLevel3 =
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt,tag)
         |select userid,cat,subcat,value,dt,tag from lofter_dm.ads_vc_user_tag_metric_level3_dd where dt = '$dt'
         |""".stripMargin


    val avgTags =
      s"""
        |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt = '$dt', tag = 'avg_pay_type_money')
        |    select cast(user_id as bigint) as userId,
        |           time_tag as cat,
        |           pay_type as subcat,
        |           cast(coin as double) as value
        |    from avg.ads_user_pay_type_log_nd
        |""".stripMargin

    val allTags =
      s"""
        |INSERT OVERWRITE TABLE lofter_dm.ads_par_user_tag_metric_level3_dd PARTITION(dt = '$dt', tag = 'ALL')
        |select id as userId, 'DAY_ALL' as cat,  'ALL' as subcat, 1 as value
        |from lofter.dim_user
        |where isAnonymous = 0
        |""".stripMargin

    sparkSession.sql(pveUserTradeProductGroup)
    sparkSession.sql(pveUserRoleLastChatsGroup)
    sparkSession.sql(pveUserRoleChatsGroup)
    sparkSession.sql(pveUserRoleAmountGroup)
    sparkSession.sql(vcUserTagDataMetricLevel3)
    sparkSession.sql(avgTags)
    sparkSession.close()
  }
}
