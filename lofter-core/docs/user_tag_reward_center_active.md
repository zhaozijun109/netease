# 权益中心活跃天数 - 用户标签实现说明

## 标签概述
**标签名称**: 权益中心活跃天数  
**标签类型**: user（用户标签）  
**标签用途**: 统计用户在权益中心的活跃行为，用于用户画像和精准营销  

## 实现架构

### 数据流向
```
ODS: ods_mda_app_di (权益中心访问事件)
  ↓
DWD: dwd_rewardcenter_visit_di (权益中心访问明细)  
  ↓
DWS: dws_par_user_reward_center_active_dd (用户活跃度汇总)
  ↓
USER_TAG: 权益中心活跃天数 (多维度用户标签)
```

### 标签维度结构

| 字段 | 说明 | 示例值 |
|------|------|--------|
| `userId` | 用户ID | 12345678 |
| `tag_name` | 标签名称 | "权益中心活跃天数" |
| `dim1` | 时间维度 | "DAY_1", "DAY_7", "DAY_15", "DAY_30", "DAY_90", "DAY_180" |
| `dim2-dim4` | 预留维度 | "" (暂未使用) |
| `grp` | 活跃天数值 | "1", "3", "7", "15"等 |
| `value` | 预留值字段 | "" (暂未使用) |

## 标签生成逻辑

### 时间维度说明
- **DAY_1**: 近1天内访问权益中心的天数（0或1）
- **DAY_7**: 近7天内访问权益中心的天数（0-7）  
- **DAY_15**: 近15天内访问权益中心的天数（0-15）
- **DAY_30**: 近30天内访问权益中心的天数（0-30）
- **DAY_90**: 近90天内访问权益中心的天数（0-90）
- **DAY_180**: 近180天内访问权益中心的天数（0-180）

### 核心SQL逻辑
```sql
-- 基于DWS表生成多维度标签
select userId as userid, 
       '权益中心活跃天数' as tag_name, 
       'DAY_30' as dim1, 
       cast(active_days_30d as string) as grp 
from lofter.dws_par_user_reward_center_active_dd 
where dt = '${target_date}' and active_days_30d > 0
```

### 过滤条件
- **有效性过滤**: 只包含活跃天数 > 0 的用户
- **时间过滤**: 基于计算日期的前一天数据
- **用户过滤**: 继承DWS层的用户过滤逻辑（userId > 0）

## 标签应用场景

### 1. 用户分群
```sql
-- 根据权益中心活跃度进行用户分群
SELECT 
    CASE 
        WHEN grp = '0' THEN '未活跃用户'
        WHEN CAST(grp AS INT) BETWEEN 1 AND 3 THEN '低活跃用户'
        WHEN CAST(grp AS INT) BETWEEN 4 AND 10 THEN '中活跃用户'  
        WHEN CAST(grp AS INT) BETWEEN 11 AND 20 THEN '高活跃用户'
        ELSE '超高活跃用户'
    END as user_group,
    COUNT(*) as user_count
FROM user_tag_table 
WHERE tag_name = '权益中心活跃天数' AND dim1 = 'DAY_30'
GROUP BY user_group;
```

### 2. 精准推送
```sql
-- 找到权益中心的潜在流失用户（30天内活跃但7天内未活跃）
SELECT DISTINCT a.userid 
FROM user_tag_table a
JOIN user_tag_table b ON a.userid = b.userid
WHERE a.tag_name = '权益中心活跃天数' AND a.dim1 = 'DAY_30' AND CAST(a.grp AS INT) > 0
  AND b.tag_name = '权益中心活跃天数' AND b.dim1 = 'DAY_7' AND b.grp = '0';
```

### 3. 活跃度趋势分析
```sql
-- 分析不同时间维度的活跃用户重合度
SELECT 
    d7.grp as active_7d,
    d30.grp as active_30d,
    COUNT(*) as overlap_users
FROM user_tag_table d7
JOIN user_tag_table d30 ON d7.userid = d30.userid
WHERE d7.tag_name = '权益中心活跃天数' AND d7.dim1 = 'DAY_7'
  AND d30.tag_name = '权益中心活跃天数' AND d30.dim1 = 'DAY_30'
GROUP BY d7.grp, d30.grp
ORDER BY CAST(d7.grp AS INT), CAST(d30.grp AS INT);
```

## 数据质量保证

### 1. 数据一致性检查
- **时间递增性**: DAY_1 ≤ DAY_7 ≤ DAY_15 ≤ DAY_30 ≤ DAY_90 ≤ DAY_180
- **有效性检查**: 所有grp值应为非负整数且不超过对应时间窗口
- **完整性检查**: 确保所有活跃用户都有相应的标签记录

### 2. 异常监控
```sql
-- 检查标签数据异常
SELECT 
    dim1,
    COUNT(*) as total_records,
    COUNT(DISTINCT userid) as unique_users,
    MIN(CAST(grp AS INT)) as min_days,
    MAX(CAST(grp AS INT)) as max_days,
    AVG(CAST(grp AS DOUBLE)) as avg_days
FROM user_tag_table 
WHERE tag_name = '权益中心活跃天数' AND dt = '${check_date}'
GROUP BY dim1;
```

## 调度与依赖

### 依赖关系
```
权益中心活跃天数.job
└── dws_par_user_reward_center_active_dd
    └── dwd_rewardcenter_visit_di  
        └── ods_mda_app_di
```

### 执行配置
- **调度频率**: 每日执行
- **执行时间**: DWS表完成后
- **数据分区**: T-1天数据
- **重试次数**: 0（避免重复标签）

## 性能优化

### 1. SQL优化
- **分区裁剪**: 基于dt字段过滤
- **条件下推**: 在子查询中过滤活跃天数 > 0
- **字段投影**: 只选择必要字段

### 2. 资源配置
- **执行引擎**: Spark SQL
- **并发度**: 基于数据量动态调整
- **内存配置**: 适当增加driver内存

## 版本历史
- **V1.0** (2026-04-29): 初始实现，支持6个时间维度的权益中心活跃天数标签
  - 修复原有标签配置错误（之前错误地计算注册天数）
  - 新增DAY_1和DAY_180两个时间维度
  - 基于新建的DWS表重新实现标签逻辑

## 相关文档
- [DWS表实现文档](./dws_par_user_reward_center_active_dd.md)
- [权益中心数据架构设计](./reward_center_data_architecture.md)
- [用户标签系统使用指南](./user_tag_system_guide.md)