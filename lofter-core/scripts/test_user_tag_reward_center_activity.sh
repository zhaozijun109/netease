#!/bin/bash

# 权益中心活跃标签测试脚本

echo "=== 权益中心活跃标签测试 ==="

# 设置测试参数
TEST_DATE="2026-04-28"
echo "测试日期: ${TEST_DATE}"

echo ""
echo "1. 检查活跃度分层逻辑..."
hive -e "
-- 验证活跃度分层逻辑
SELECT 
    time_dimension,
    activity_level,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(PARTITION BY time_dimension), 2) as percentage,
    MIN(active_days) as min_days,
    MAX(active_days) as max_days
FROM (
    -- DAY_7 分层验证
    SELECT 'DAY_7' as time_dimension,
           active_days_7d as active_days,
           CASE 
               WHEN active_days_7d > 0 THEN '活跃'
               ELSE '不活跃'
           END as activity_level
    FROM lofter.dws_par_user_reward_center_active_dd
    WHERE dt = '${TEST_DATE}'

    UNION ALL

    -- DAY_30 分层验证
    SELECT 'DAY_30' as time_dimension,
           active_days_30d as active_days,
           CASE
               WHEN active_days_30d > 0 THEN '活跃'
               ELSE '不活跃'
           END as activity_level
    FROM lofter.dws_par_user_reward_center_active_dd
    WHERE dt = '${TEST_DATE}'

    UNION ALL

    -- DAY_90 分层验证
    SELECT 'DAY_90' as time_dimension,
           active_days_90d as active_days,
           CASE
               WHEN active_days_90d > 0 THEN '活跃'
               ELSE '不活跃'
           END as activity_level
    FROM lofter.dws_par_user_reward_center_active_dd 
    WHERE dt = '${TEST_DATE}'
) activity_analysis
GROUP BY time_dimension, activity_level
ORDER BY time_dimension, 
         CASE activity_level 
             WHEN '不活跃' THEN 1 
             WHEN '低活跃' THEN 2 
             WHEN '中活跃' THEN 3 
             WHEN '高活跃' THEN 4 
         END;
"

echo ""
echo "2. 测试标签生成SQL..."
echo "-- 预览权益中心活跃标签数据（只包含活跃用户）"
hive -e "
SELECT
    tag_name,
    dim1 as time_dimension,
    grp as activity_value,
    COUNT(*) as active_user_count
FROM (
    -- DAY_1 活跃用户
    select userId as userid, '权益中心活跃' as tag_name, 'DAY_1' as dim1, '1' as grp
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and active_days_1d > 0

    UNION ALL

    -- DAY_7 活跃用户
    select userId as userid, '权益中心活跃' as tag_name, 'DAY_7' as dim1, '1' as grp
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and active_days_7d > 0

    UNION ALL

    -- DAY_30 活跃用户
    select userId as userid, '权益中心活跃' as tag_name, 'DAY_30' as dim1, '1' as grp
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and active_days_30d > 0

    UNION ALL

    -- DAY_180 活跃用户
    select userId as userid, '权益中心活跃' as tag_name, 'DAY_180' as dim1, '1' as grp
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}' and active_days_180d > 0
) tag_preview
GROUP BY tag_name, dim1, grp
ORDER BY dim1;
"

echo ""
echo "3. 用户活跃度转移分析..."
echo "-- 分析用户从30天到7天的活跃度变化"
hive -e "
SELECT
    activity_30d,
    activity_7d,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM (
    SELECT userId,
           CASE
               WHEN active_days_30d > 0 THEN '活跃'
               ELSE '不活跃'
           END as activity_30d,
           CASE
               WHEN active_days_7d > 0 THEN '活跃'
               ELSE '不活跃'
           END as activity_7d
    FROM lofter.dws_par_user_reward_center_active_dd
    WHERE dt = '${TEST_DATE}'
) activity_transition
GROUP BY activity_30d, activity_7d
ORDER BY activity_30d DESC, activity_7d DESC;
"

echo ""
echo "4. 活跃标签样例数据..."
hive -e "
SELECT
    userid,
    tag_name,
    dim1,
    grp,
    CASE dim1
        WHEN 'DAY_1' THEN active_days_1d
        WHEN 'DAY_7' THEN active_days_7d
        WHEN 'DAY_30' THEN active_days_30d
        WHEN 'DAY_180' THEN active_days_180d
    END as actual_days
FROM (
    select userId as userid, '权益中心活跃' as tag_name, 'DAY_30' as dim1,
           CASE
               WHEN active_days_30d > 0 THEN '活跃'
               ELSE '不活跃'
           END as grp,
           active_days_1d, active_days_7d, active_days_30d, active_days_180d
    from lofter.dws_par_user_reward_center_active_dd
    where dt = '${TEST_DATE}'
    order by active_days_30d desc
    limit 20
) sample_data;
"

echo ""
echo "=== 测试完成 ==="