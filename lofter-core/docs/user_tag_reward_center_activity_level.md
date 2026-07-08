# 权益中心活跃 - 用户标签实现说明

## 标签概述
**标签名称**: 权益中心活跃  
**标签类型**: user（用户标签）  
**标签用途**: 基于活跃天数进行分层分类，提供直观的活跃度等级，便于业务理解和应用

## 标签设计理念

### 与"权益中心活跃天数"标签的区别
- **权益中心活跃天数**: 提供精确的活跃天数值（如"3天"、"15天"）
- **权益中心活跃**: 提供活跃度分层分类（如"低活跃"、"高活跃"）

### 输出策略
基于简洁高效的设计理念，采用**活跃用户标记模式**：
- **只输出活跃用户**: 仅对在指定时间窗口内有访问行为的用户打标签
- **标签值固定为"1"**: 表示该用户在对应时间维度内为活跃状态
- **不输出不活跃用户**: 无访问行为的用户不生成标签记录

## 输出规则详情

### 时间维度输出标准

| 时间维度 | 输出条件 | 标签值 | 说明 |
|---------|----------|--------|------|
| **DAY_1** | active_days_1d > 0 | "1" | 当天有访问 |
| **DAY_7** | active_days_7d > 0 | "1" | 近7天有访问 |
| **DAY_15** | active_days_15d > 0 | "1" | 近15天有访问 |
| **DAY_30** | active_days_30d > 0 | "1" | 近30天有访问 |
| **DAY_90** | active_days_90d > 0 | "1" | 近90天有访问 |
| **DAY_180** | active_days_180d > 0 | "1" | 近180天有访问 |

### 设计原则

1. **精准标记**: 只标记有意义的活跃用户，避免数据冗余
2. **性能优化**: 减少数据量，提高查询和存储效率
3. **语义明确**: 标签的存在即表示活跃，标签值"1"强化这一语义

## 标签实现架构

### SQL核心逻辑
```sql
-- 以DAY_30为例，只输出活跃用户
select userId, '权益中心活跃' as tag, 'DAY_30' as cat, '1' as grp
from lofter.dws_par_user_reward_center_active_dd
where dt = '${target_date}' and active_days_30d > 0
```

### 标签维度结构

| 字段 | 说明 | 示例值 |
|------|------|--------|
| `userId` | 用户ID | 12345678 |
| `tag_name` | 标签名称 | "权益中心活跃" |
| `dim1` | 时间维度 | "DAY_1", "DAY_7", "DAY_15", "DAY_30", "DAY_90", "DAY_180" |
| `grp` | 活跃标识 | "1" |

## 业务应用场景

### 1. 活跃用户统计
```sql
-- 权益中心各时间维度活跃用户数量
SELECT 
    dim1 as time_period,
    COUNT(*) as active_user_count
FROM user_tag_table 
WHERE tag_name = '权益中心活跃' AND grp = '1'
GROUP BY dim1
ORDER BY dim1;
```

### 2. 流失风险识别
```sql
-- 识别潜在流失用户（30天内活跃但7天内不活跃）
SELECT DISTINCT t30.userid
FROM user_tag_table t30
LEFT JOIN user_tag_table t7 
  ON t30.userid = t7.userid 
  AND t7.tag_name = '权益中心活跃' 
  AND t7.dim1 = 'DAY_7' 
  AND t7.grp = '1'
WHERE t30.tag_name = '权益中心活跃' 
  AND t30.dim1 = 'DAY_30' 
  AND t30.grp = '1'
  AND t7.userid IS NULL;  -- 30天活跃但7天内无标签记录
```

### 3. 精准营销策略
```sql
-- 根据活跃度制定营销策略
SELECT 
    '权益中心活跃用户' as user_segment,
    dim1 as time_period,
    COUNT(*) as target_users,
    CASE dim1
        WHEN 'DAY_1' THEN '实时推送: 热门权益推荐'
        WHEN 'DAY_7' THEN '周度关怀: 专属权益包'
        WHEN 'DAY_30' THEN '月度回馈: VIP特权体验'
        WHEN 'DAY_180' THEN '长期用户: 专属客服服务'
    END as marketing_strategy
FROM user_tag_table 
WHERE tag_name = '权益中心活跃' AND grp = '1'
GROUP BY dim1;
```

### 4. 活跃用户重叠分析
```sql
-- 分析不同时间维度的用户重叠情况
SELECT 
    '30天活跃且7天活跃' as overlap_type,
    COUNT(*) as user_count
FROM user_tag_table t30
INNER JOIN user_tag_table t7 
  ON t30.userid = t7.userid
WHERE t30.tag_name = '权益中心活跃' AND t30.dim1 = 'DAY_30' AND t30.grp = '1'
  AND t7.tag_name = '权益中心活跃' AND t7.dim1 = 'DAY_7' AND t7.grp = '1'

UNION ALL

SELECT 
    '180天活跃但30天不活跃' as overlap_type,
    COUNT(*) as user_count
FROM user_tag_table t180
LEFT JOIN user_tag_table t30 
  ON t180.userid = t30.userid 
  AND t30.tag_name = '权益中心活跃' 
  AND t30.dim1 = 'DAY_30' 
  AND t30.grp = '1'
WHERE t180.tag_name = '权益中心活跃' 
  AND t180.dim1 = 'DAY_180' 
  AND t180.grp = '1'
  AND t30.userid IS NULL;
```

## 数据质量与监控

### 1. 分布合理性检查
```sql
-- 检查各等级用户分布是否合理
SELECT 
    dim1,
    grp,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(PARTITION BY dim1), 2) as percentage
FROM user_tag_table 
WHERE tag_name = '权益中心活跃' AND dt = '${check_date}'
GROUP BY dim1, grp
HAVING percentage > 80 OR percentage < 5;  -- 异常分布预警
```

### 2. 一致性检查
```sql
-- 检查不同时间维度的逻辑一致性
-- 长期高活跃用户在短期内不应该完全不活跃
SELECT COUNT(*) as inconsistent_users
FROM user_tag_table a
JOIN user_tag_table b ON a.userid = b.userid
WHERE a.tag_name = '权益中心活跃' AND a.dim1 = 'DAY_180' AND a.grp = '高活跃'
  AND b.tag_name = '权益中心活跃' AND b.dim1 = 'DAY_7' AND b.grp = '不活跃';
```

## 性能优化

### 1. SQL优化策略
- **条件下推**: 在各个UNION分支中直接应用过滤条件
- **分区利用**: 基于dt字段进行分区裁剪
- **避免重复扫描**: 通过合理的CASE WHEN结构减少表扫描次数

### 2. 执行效率
- **并行化**: 各时间维度可并行计算
- **内存优化**: 合理设置Spark执行参数
- **缓存策略**: 对DWS源表进行适当缓存

## 业务价值

### 1. 直观性
- 将数字化的活跃天数转化为业务易理解的等级标签
- 支持快速的用户分群和画像分析

### 2. 可操作性  
- 每个等级对应明确的运营策略
- 便于制定差异化的营销方案

### 3. 可扩展性
- 分层阈值可根据业务反馈调整
- 支持添加新的时间维度或活跃度等级

## 版本历史
- **V1.0** (2026-04-29): 初始实现
  - 支持6个时间维度的活跃度分层
  - 建立4级活跃度分类体系
  - 基于dws_par_user_reward_center_active_dd表实现

## 相关标签对比

| 标签名称 | 输出格式 | 应用场景 | 优势 |
|---------|----------|----------|------|
| 权益中心活跃天数 | 具体数值 | 精确分析、算法建模 | 数据精确、粒度细 |
| 权益中心活跃 | 分层等级 | 业务运营、用户分群 | 直观易懂、便于应用 |

两个标签相互补充，满足不同场景的需求。