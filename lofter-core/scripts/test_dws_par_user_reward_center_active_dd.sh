#!/bin/bash

# dws_par_user_reward_center_active_dd 任务测试脚本

echo "=== DWS PAR USER REWARD CENTER ACTIVE DD 任务测试 ==="

# 设置测试参数
TEST_DATE="2026-04-28"
echo "测试日期: ${TEST_DATE}"

echo ""
echo "1. 检查依赖表是否存在数据..."
echo "-- 检查 dwd_rewardcenter_visit_di 表"
hive -e "
SELECT 
    dt, 
    COUNT(*) as visit_count,
    COUNT(DISTINCT userid) as user_count
FROM lofter.dwd_rewardcenter_visit_di 
WHERE dt >= '${TEST_DATE}' 
GROUP BY dt 
ORDER BY dt DESC 
LIMIT 5;
"

echo ""
echo "2. 执行数据质量检查..."
echo "-- 检查生成的数据一致性"
hive -e "
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN active_days_1d > active_days_7d THEN 1 ELSE 0 END) as inconsistent_1d_7d,
    SUM(CASE WHEN active_days_7d > active_days_15d THEN 1 ELSE 0 END) as inconsistent_7d_15d,
    SUM(CASE WHEN active_days_15d > active_days_30d THEN 1 ELSE 0 END) as inconsistent_15d_30d,
    SUM(CASE WHEN active_days_30d > active_days_90d THEN 1 ELSE 0 END) as inconsistent_30d_90d,
    SUM(CASE WHEN active_days_90d > active_days_180d THEN 1 ELSE 0 END) as inconsistent_90d_180d
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}';
"

echo ""
echo "3. 数据分布统计..."
hive -e "
SELECT 
    CASE 
        WHEN active_days_30d = 0 THEN '未活跃'
        WHEN active_days_30d BETWEEN 1 AND 3 THEN '低活跃(1-3天)' 
        WHEN active_days_30d BETWEEN 4 AND 10 THEN '中活跃(4-10天)'
        WHEN active_days_30d BETWEEN 11 AND 20 THEN '高活跃(11-20天)'
        ELSE '超高活跃(>20天)'
    END AS activity_level,
    COUNT(*) AS user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS percentage
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}'
GROUP BY 
    CASE 
        WHEN active_days_30d = 0 THEN '未活跃'
        WHEN active_days_30d BETWEEN 1 AND 3 THEN '低活跃(1-3天)' 
        WHEN active_days_30d BETWEEN 4 AND 10 THEN '中活跃(4-10天)'
        WHEN active_days_30d BETWEEN 11 AND 20 THEN '高活跃(11-20天)'
        ELSE '超高活跃(>20天)'
    END
ORDER BY user_count DESC;
"

echo ""
echo "4. 样例数据展示..."
hive -e "
SELECT 
    userId,
    is_anonymous,
    active_days_1d,
    active_days_7d,
    active_days_15d,
    active_days_30d,
    active_days_90d,
    active_days_180d
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}'
ORDER BY active_days_30d DESC 
LIMIT 10;
"

echo ""
echo "=== 测试完成 ==="