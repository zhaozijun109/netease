#!/bin/bash

# 权益中心活跃天数用户标签测试脚本

echo "=== 权益中心活跃天数用户标签测试 ==="

# 设置测试参数
TEST_DATE="2026-04-28"
echo "测试日期: ${TEST_DATE}"

echo ""
echo "1. 检查DWS源表数据状态..."
hive -e "
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN active_days_1d > 0 THEN 1 END) as active_1d_users,
    COUNT(CASE WHEN active_days_7d > 0 THEN 1 END) as active_7d_users,
    COUNT(CASE WHEN active_days_15d > 0 THEN 1 END) as active_15d_users,
    COUNT(CASE WHEN active_days_30d > 0 THEN 1 END) as active_30d_users,
    COUNT(CASE WHEN active_days_90d > 0 THEN 1 END) as active_90d_users,
    COUNT(CASE WHEN active_days_180d > 0 THEN 1 END) as active_180d_users
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '${TEST_DATE}';
"

echo ""
echo "2. 测试标签SQL逻辑..."
echo "-- 模拟用户标签生成的SQL逻辑"
hive -e "
SELECT 
    tag_name,
    dim1 as time_dimension,
    COUNT(*) as user_count,
    MIN(CAST(grp AS INT)) as min_active_days,
    MAX(CAST(grp AS INT)) as max_active_days,
    AVG(CAST(grp AS DOUBLE)) as avg_active_days
FROM (
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_1' as dim1, 
           cast(active_days_1d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_1d > 0 
    
    union all 
    
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_7' as dim1, 
           cast(active_days_7d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_7d > 0 
    
    union all 
    
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_15' as dim1, 
           cast(active_days_15d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_15d > 0 
    
    union all 
    
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_30' as dim1, 
           cast(active_days_30d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_30d > 0 
    
    union all 
    
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_90' as dim1, 
           cast(active_days_90d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_90d > 0 
    
    union all 
    
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_180' as dim1, 
           cast(active_days_180d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_180d > 0
) tag_data 
GROUP BY tag_name, dim1
ORDER BY 
    CASE dim1 
        WHEN 'DAY_1' THEN 1 
        WHEN 'DAY_7' THEN 2 
        WHEN 'DAY_15' THEN 3 
        WHEN 'DAY_30' THEN 4 
        WHEN 'DAY_90' THEN 5 
        WHEN 'DAY_180' THEN 6 
    END;
"

echo ""
echo "3. 各时间维度活跃天数分布..."
for period in "DAY_1" "DAY_7" "DAY_15" "DAY_30" "DAY_90" "DAY_180"; do
    echo "-- ${period} 活跃天数分布"
    case ${period} in
        "DAY_1")   field="active_days_1d" ;;
        "DAY_7")   field="active_days_7d" ;;
        "DAY_15")  field="active_days_15d" ;;
        "DAY_30")  field="active_days_30d" ;;
        "DAY_90")  field="active_days_90d" ;;
        "DAY_180") field="active_days_180d" ;;
    esac
    
    hive -e "
    SELECT 
        '${period}' as time_period,
        ${field} as active_days,
        COUNT(*) as user_count,
        ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
    FROM lofter.dws_par_user_reward_center_active_dd 
    WHERE dt = '${TEST_DATE}' AND ${field} > 0
    GROUP BY ${field}
    ORDER BY ${field}
    LIMIT 10;
    "
done

echo ""
echo "4. 样例标签数据..."
hive -e "
SELECT 
    userid,
    tag_name, 
    dim1, 
    grp
FROM (
    select userId as userid, 
           '权益中心活跃天数' as tag_name, 
           'DAY_30' as dim1, 
           cast(active_days_30d as string) as grp 
    from lofter.dws_par_user_reward_center_active_dd 
    where dt = '${TEST_DATE}' and active_days_30d > 0 
    order by active_days_30d desc
    limit 10
) sample_data;
"

echo ""
echo "=== 测试完成 ==="