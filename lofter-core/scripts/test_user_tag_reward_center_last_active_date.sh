#!/bin/bash

# 权益中心最后活跃日期标签测试脚本

echo "=== 权益中心最后活跃日期标签测试 ==="

# 设置测试参数
TEST_DATE="2026-04-28"
echo "测试日期: ${TEST_DATE}"

echo ""
echo "1. 检查DWS源表最后活跃日期字段..."
hive -e "
-- 检查最后活跃日期字段数据
SELECT 
    COUNT(*) as total_users,
    COUNT(last_active_date) as users_with_last_active_date,
    COUNT(CASE WHEN last_active_date IS NULL THEN 1 END) as users_without_last_active_date,
    MIN(last_active_date) as earliest_active_date,
    MAX(last_active_date) as latest_active_date
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}';
"

echo ""
echo "2. 最后活跃日期分布..."
hive -e "
-- 最后活跃日期分布统计
SELECT 
    last_active_date,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}' AND last_active_date IS NOT NULL
GROUP BY last_active_date
ORDER BY last_active_date DESC
LIMIT 20;
"

echo ""
echo "3. 测试标签生成SQL..."
echo "-- 预览权益中心最后活跃日期标签数据"
hive -e "
SELECT 
    tag_name,
    grp as last_active_date,
    COUNT(*) as user_count
FROM (
    select userId as userid, 
           '权益中心最后活跃日期' as tag_name, 
           last_active_date as grp
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and last_active_date is not null
) tag_preview
GROUP BY tag_name, grp
ORDER BY grp DESC
LIMIT 10;
"

echo ""
echo "4. 最后活跃日期与活跃天数关联分析..."
hive -e "
-- 分析最后活跃日期与活跃天数的关系
SELECT 
    last_active_date,
    COUNT(*) as user_count,
    AVG(active_days_7d) as avg_active_days_7d,
    AVG(active_days_30d) as avg_active_days_30d,
    CASE 
        WHEN last_active_date = '${TEST_DATE}' THEN '当天活跃'
        WHEN datediff('${TEST_DATE}', last_active_date) <= 7 THEN '近7天活跃'
        WHEN datediff('${TEST_DATE}', last_active_date) <= 30 THEN '近30天活跃'
        ELSE '30天前活跃'
    END as activity_segment
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}' AND last_active_date IS NOT NULL
GROUP BY last_active_date
ORDER BY last_active_date DESC
LIMIT 15;
"

echo ""
echo "5. 数据质量检查..."
hive -e "
-- 检查数据逻辑一致性
SELECT 
    'consistency_check' as check_type,
    COUNT(*) as total_users_with_last_date,
    SUM(CASE WHEN active_days_180d = 0 AND datediff('${TEST_DATE}', last_active_date) <= 180 THEN 1 ELSE 0 END) as inconsistent_recent_users,
    SUM(CASE WHEN last_active_date > '${TEST_DATE}' THEN 1 ELSE 0 END) as future_date_users,
    SUM(CASE WHEN datediff('${TEST_DATE}', last_active_date) > 365 THEN 1 ELSE 0 END) as very_old_users,
    MIN(last_active_date) as earliest_last_active,
    MAX(last_active_date) as latest_last_active
FROM lofter.dws_par_user_reward_center_active_dd
WHERE dt = '${TEST_DATE}' AND last_active_date IS NOT NULL;
"

echo ""
echo "6. 验证增量计算逻辑..."
YESTERDAY=$(date -d "${TEST_DATE} -1 day" +%Y-%m-%d)
echo "-- 检查增量计算是否正确（${YESTERDAY} -> ${TEST_DATE}）"
hive -e "
-- 验证增量计算逻辑
WITH today_visitors AS (
    SELECT DISTINCT userid
    FROM lofter.dwd_rewardcenter_visit_di
    WHERE dt = '${TEST_DATE}' AND userid > 0
),
yesterday_data AS (
    SELECT userId, last_active_date
    FROM lofter.dws_par_user_reward_center_active_dd
    WHERE dt = '${YESTERDAY}'
),
today_data AS (
    SELECT userId, last_active_date
    FROM lofter.dws_par_user_reward_center_active_dd
    WHERE dt = '${TEST_DATE}'
)
SELECT
    'incremental_check' as check_type,
    SUM(CASE WHEN tv.userid IS NOT NULL AND td.last_active_date = '${TEST_DATE}' THEN 1 ELSE 0 END) as correct_today_updates,
    SUM(CASE WHEN tv.userid IS NULL AND td.last_active_date = yd.last_active_date THEN 1 ELSE 0 END) as correct_inherited_dates,
    SUM(CASE WHEN tv.userid IS NOT NULL AND td.last_active_date != '${TEST_DATE}' THEN 1 ELSE 0 END) as incorrect_today_updates,
    SUM(CASE WHEN tv.userid IS NULL AND td.last_active_date != yd.last_active_date THEN 1 ELSE 0 END) as incorrect_inherited_dates
FROM today_data td
LEFT JOIN yesterday_data yd ON td.userId = yd.userId
LEFT JOIN today_visitors tv ON td.userId = tv.userid;
"

echo ""
echo "7. 验证数据连续性..."
hive -e "
-- 检查数据的时间连续性
SELECT
    CASE
        WHEN datediff('${TEST_DATE}', last_active_date) <= 180 THEN '180天内活跃'
        WHEN datediff('${TEST_DATE}', last_active_date) BETWEEN 181 AND 365 THEN '180天-1年前活跃'
        ELSE '1年前活跃'
    END as activity_period,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage,
    MIN(last_active_date) as earliest_date,
    MAX(last_active_date) as latest_date
FROM lofter.dws_par_user_reward_center_active_dd
WHERE dt = '${TEST_DATE}' AND last_active_date IS NOT NULL
GROUP BY
    CASE
        WHEN datediff('${TEST_DATE}', last_active_date) <= 180 THEN '180天内活跃'
        WHEN datediff('${TEST_DATE}', last_active_date) BETWEEN 181 AND 365 THEN '180天-1年前活跃'
        ELSE '1年前活跃'
    END;
"

echo ""
echo "6. 标签样例数据..."
hive -e "
SELECT 
    userid,
    tag_name,
    grp as last_active_date,
    active_days_7d,
    active_days_30d,
    active_days_180d
FROM (
    select userId as userid, 
           '权益中心最后活跃日期' as tag_name, 
           last_active_date as grp,
           active_days_7d, active_days_30d, active_days_180d
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and last_active_date is not null
    order by last_active_date desc, active_days_30d desc
    limit 10
) sample_data;
"

echo ""
echo "=== 测试完成 ==="