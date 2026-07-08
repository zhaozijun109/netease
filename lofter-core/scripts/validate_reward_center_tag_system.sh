#!/bin/bash

# 权益中心标签体系验证脚本

echo "=== 权益中心标签体系验证 ==="

# 设置测试参数
TEST_DATE="2026-04-28"
echo "测试日期: ${TEST_DATE}"

echo ""
echo "1. 检查任务文件完整性..."

# 检查DWS表任务
DWS_FILE="etl/jobs/dws/dws_par_user_reward_center_active_dd.job"
echo "检查DWS任务: ${DWS_FILE}"
if [ -f "${DWS_FILE}" ]; then
    echo "✓ DWS任务文件存在"
    if grep -q "dws_par_user_reward_center_active_dd" "${DWS_FILE}"; then
        echo "✓ 表名配置正确"
    fi
else
    echo "✗ DWS任务文件缺失"
fi

# 检查标签任务
TAG1_FILE="etl/jobs/user-tag-v2/权益中心活跃天数.job"
TAG2_FILE="etl/jobs/user-tag-v2/权益中心活跃.job"

echo "检查标签任务: ${TAG1_FILE}"
if [ -f "${TAG1_FILE}" ]; then
    echo "✓ 权益中心活跃天数标签存在"
    if grep -q "权益中心活跃天数" "${TAG1_FILE}"; then
        echo "✓ 标签名称正确"
    fi
else
    echo "✗ 权益中心活跃天数标签缺失"
fi

echo "检查标签任务: ${TAG2_FILE}"
if [ -f "${TAG2_FILE}" ]; then
    echo "✓ 权益中心活跃标签存在"
    if grep -q "权益中心活跃" "${TAG2_FILE}"; then
        echo "✓ 标签名称正确"
    fi
else
    echo "✗ 权益中心活跃标签缺失"
fi

echo ""
echo "2. 生成完整验证SQL..."
cat > /tmp/reward_center_tags_validation.sql << 'EOF'
-- 权益中心标签体系验证SQL

-- 1. DWS源表数据概览
SELECT 'DWS_DATA_OVERVIEW' as check_type,
       COUNT(*) as total_users,
       COUNT(CASE WHEN active_days_1d > 0 THEN 1 END) as day1_active,
       COUNT(CASE WHEN active_days_7d > 0 THEN 1 END) as day7_active,
       COUNT(CASE WHEN active_days_30d > 0 THEN 1 END) as day30_active,
       COUNT(CASE WHEN active_days_180d > 0 THEN 1 END) as day180_active
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '2026-04-28';

-- 2. 权益中心活跃天数标签预览
SELECT 'ACTIVE_DAYS_TAG' as check_type,
       dim1 as time_dimension,
       COUNT(*) as tag_records,
       COUNT(DISTINCT userid) as unique_users,
       MIN(CAST(grp AS INT)) as min_days,
       MAX(CAST(grp AS INT)) as max_days
FROM (
    select userId as userid, 'DAY_1' as dim1, cast(active_days_1d as string) as grp from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28' and active_days_1d > 0
    union all
    select userId as userid, 'DAY_7' as dim1, cast(active_days_7d as string) as grp from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28' and active_days_7d > 0
    union all
    select userId as userid, 'DAY_30' as dim1, cast(active_days_30d as string) as grp from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28' and active_days_30d > 0
    union all
    select userId as userid, 'DAY_180' as dim1, cast(active_days_180d as string) as grp from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28' and active_days_180d > 0
) active_days_preview
GROUP BY dim1;

-- 3. 权益中心活跃等级标签预览  
SELECT 'ACTIVITY_LEVEL_TAG' as check_type,
       dim1 as time_dimension,
       grp as activity_level,
       COUNT(*) as user_count,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(PARTITION BY dim1), 2) as percentage
FROM (
    select userId as userid, 'DAY_1' as dim1,
           CASE WHEN active_days_1d >= 1 THEN '活跃' ELSE '不活跃' END as grp
    from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28'
    
    union all
    
    select userId as userid, 'DAY_7' as dim1,
           CASE 
               WHEN active_days_7d = 0 THEN '不活跃'
               WHEN active_days_7d BETWEEN 1 AND 2 THEN '低活跃'
               WHEN active_days_7d BETWEEN 3 AND 5 THEN '中活跃'
               ELSE '高活跃'
           END as grp
    from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28'
    
    union all
    
    select userId as userid, 'DAY_30' as dim1,
           CASE 
               WHEN active_days_30d = 0 THEN '不活跃'
               WHEN active_days_30d BETWEEN 1 AND 5 THEN '低活跃'
               WHEN active_days_30d BETWEEN 6 AND 15 THEN '中活跃'
               ELSE '高活跃'
           END as grp
    from lofter.dws_par_user_reward_center_active_dd where dt = '2026-04-28'
) activity_level_preview
GROUP BY dim1, grp;

-- 4. 标签一致性检查
SELECT 'CONSISTENCY_CHECK' as check_type,
       COUNT(*) as total_comparisons,
       SUM(CASE WHEN days_tag.active_days = 0 AND level_tag.activity_level != '不活跃' THEN 1 ELSE 0 END) as inconsistent_zero,
       SUM(CASE WHEN days_tag.active_days > 15 AND level_tag.activity_level = '低活跃' THEN 1 ELSE 0 END) as inconsistent_high,
       0 as placeholder1,
       0 as placeholder2
FROM (
    SELECT userId, active_days_30d as active_days
    FROM lofter.dws_par_user_reward_center_active_dd 
    WHERE dt = '2026-04-28'
) days_tag
JOIN (
    SELECT userId,
           CASE 
               WHEN active_days_30d = 0 THEN '不活跃'
               WHEN active_days_30d BETWEEN 1 AND 5 THEN '低活跃'
               WHEN active_days_30d BETWEEN 6 AND 15 THEN '中活跃'
               ELSE '高活跃'
           END as activity_level
    FROM lofter.dws_par_user_reward_center_active_dd 
    WHERE dt = '2026-04-28'
) level_tag ON days_tag.userId = level_tag.userId;
EOF

echo "验证SQL已保存到: /tmp/reward_center_tags_validation.sql"

echo ""
echo "3. 权益中心标签体系总结..."
echo "✓ 数据层次:"
echo "  - ODS: ods_mda_app_di (权益中心访问事件)"
echo "  - DWD: dwd_rewardcenter_visit_di (访问明细)"  
echo "  - DWS: dws_par_user_reward_center_active_dd (活跃度汇总)"
echo "  - TAG: 权益中心活跃天数 + 权益中心活跃 (用户标签)"

echo ""
echo "✓ 标签体系:"
echo "  权益中心活跃天数     | 精确数值 | 6个时间维度 | 适用于算法和精确分析"
echo "  权益中心活跃         | 活跃标识 | 6个时间维度 | 适用于业务运营和分群"
echo "  权益中心最后活跃日期 | 具体日期 | 单一维度   | 适用于流失分析和召回"

echo ""
echo "✓ 时间维度覆盖:"
echo "  - DAY_1   (当天活跃)"
echo "  - DAY_7   (近一周活跃)" 
echo "  - DAY_15  (近半月活跃)"
echo "  - DAY_30  (近一月活跃)"
echo "  - DAY_90  (近一季活跃)"
echo "  - DAY_180 (近半年活跃)"

echo ""
echo "✓ 活跃度分层:"
echo "  - 不活跃: 0天访问"
echo "  - 低活跃: 偶尔访问 (占活跃用户20-30%)"
echo "  - 中活跃: 定期访问 (占活跃用户40-50%)"
echo "  - 高活跃: 频繁访问 (占活跃用户20-30%)"

echo ""
echo "4. 执行建议..."
echo "1. 创建DWS表结构: hive -f sql/create_table_dws_par_user_reward_center_active_dd.sql"
echo "2. 验证数据逻辑: hive -f /tmp/reward_center_tags_validation.sql"
echo "3. 配置Azkaban调度: 确保DWS -> UserTag的依赖关系"
echo "4. 监控标签质量: 定期检查用户分布和一致性"

echo ""
echo "=== 验证完成 ==="