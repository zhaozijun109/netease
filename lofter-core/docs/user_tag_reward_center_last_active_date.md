# 权益中心最后活跃日期 - 用户标签实现说明

## 标签概述
**标签名称**: 权益中心最后活跃日期  
**标签类型**: user（用户标签）  
**标签用途**: 记录用户在权益中心的最后一次访问日期，用于用户活跃度分析和流失预警

## 标签设计理念

### 业务价值
- **精确记录**: 提供用户在权益中心最后一次活跃的精确日期
- **流失分析**: 结合当前日期计算用户未活跃天数，进行流失风险评估
- **分群基础**: 作为用户生命周期分析和精准营销的重要维度

### 数据来源链路
```
ODS: ods_mda_app_di (权益中心访问事件)
  ↓
DWD: dwd_rewardcenter_visit_di (权益中心访问明细)
  ↓
DWS: dws_par_user_reward_center_active_dd (添加last_active_date字段)
  ↓
TAG: 权益中心最后活跃日期 (用户标签)
```

## 实现架构

### DWS层字段扩展
在 `lofter.dws_par_user_reward_center_active_dd` 表中新增字段：
```sql
last_active_date STRING COMMENT '最后活跃日期(yyyy-MM-dd格式)'
```

### DWS层计算逻辑（增量合并）
```sql
-- 增量计算：基于昨天结果 + 今天新增数据
-- 1. 今天的活跃天数统计（基于180天滑动窗口）
SELECT a.userId, 
       active_days_*, 
       max(if(dt = '${target_date}', 1, 0)) as has_visit_today
FROM dwd_rewardcenter_visit_di
WHERE dt > date_sub('${target_date}', 180)

-- 2. 昨天的最后活跃日期（增量继承）
SELECT userId, last_active_date
FROM dws_par_user_reward_center_active_dd
WHERE dt = '${target_date - 1}'

-- 3. 增量更新最后活跃日期
CASE WHEN today.has_visit_today = 1 THEN '${target_date}'
     ELSE yesterday.last_active_date
END as last_active_date
```

### 标签层实现
```sql
select userId as userId,
       '权益中心最后活跃日期' as tag_name,
       '' as dim1,
       '' as dim2, 
       '' as dim3,
       '' as dim4,
       last_active_date as grp,
       '' as value
from lofter.dws_par_user_reward_center_active_dd
where dt = '${target_date}' and last_active_date is not null
```

### 标签维度结构

| 字段 | 说明 | 示例值 |
|------|------|--------|
| `userId` | 用户ID | 12345678 |
| `tag_name` | 标签名称 | "权益中心最后活跃日期" |
| `dim1-dim4` | 预留维度 | "" |
| `grp` | 最后活跃日期 | "2026-04-25", "2026-04-28" |
| `value` | 预留值字段 | "" |

## 业务应用场景

### 1. 流失风险分级
```sql
-- 根据最后活跃日期进行流失风险分级
SELECT 
    CASE 
        WHEN datediff('${current_date}', grp) = 0 THEN '当日活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 1 AND 3 THEN '近期活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 4 AND 7 THEN '一周内活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 8 AND 30 THEN '潜在流失'
        WHEN datediff('${current_date}', grp) > 30 THEN '高风险流失'
    END as risk_level,
    COUNT(*) as user_count
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期'
GROUP BY 
    CASE 
        WHEN datediff('${current_date}', grp) = 0 THEN '当日活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 1 AND 3 THEN '近期活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 4 AND 7 THEN '一周内活跃'
        WHEN datediff('${current_date}', grp) BETWEEN 8 AND 30 THEN '潜在流失'
        WHEN datediff('${current_date}', grp) > 30 THEN '高风险流失'
    END;
```

### 2. 召回用户筛选
```sql
-- 筛选特定时间段的流失用户进行召回
SELECT userid
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期'
  AND datediff('${current_date}', grp) BETWEEN 7 AND 30  -- 7-30天未活跃
  AND grp >= '2026-04-01';  -- 最后活跃时间不能太久远
```

### 3. 用户生命周期分析
```sql
-- 分析用户最后活跃日期分布
SELECT 
    grp as last_active_date,
    COUNT(*) as user_count,
    datediff('${current_date}', grp) as days_since_last_active,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期'
GROUP BY grp
ORDER BY grp DESC;
```

### 4. 精准营销时机把握
```sql
-- 识别即将流失的用户（3-7天未活跃）
SELECT userid, grp as last_active_date
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期'
  AND datediff('${current_date}', grp) BETWEEN 3 AND 7
ORDER BY grp DESC;
```

### 5. 活跃度趋势分析
```sql
-- 结合活跃天数标签进行综合分析
SELECT 
    t1.grp as last_active_date,
    datediff('${current_date}', t1.grp) as days_inactive,
    COUNT(*) as user_count,
    AVG(CAST(t2.grp AS INT)) as avg_active_days_30d
FROM user_tag_table t1
LEFT JOIN user_tag_table t2 
  ON t1.userid = t2.userid 
  AND t2.tag_name = '权益中心活跃天数' 
  AND t2.dim1 = 'DAY_30'
WHERE t1.tag_name = '权益中心最后活跃日期'
GROUP BY t1.grp
ORDER BY t1.grp DESC;
```

## 数据质量保证

### 1. 数据一致性检查
```sql
-- 检查最后活跃日期与活跃天数的一致性
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN datediff('${current_date}', grp) > 180 AND t2.grp IS NOT NULL THEN 1 ELSE 0 END) as inconsistent_users
FROM user_tag_table t1
LEFT JOIN user_tag_table t2 
  ON t1.userid = t2.userid 
  AND t2.tag_name = '权益中心活跃天数' 
  AND t2.dim1 = 'DAY_180'
WHERE t1.tag_name = '权益中心最后活跃日期';
```

### 2. 日期有效性检查
```sql
-- 检查日期格式和范围有效性
SELECT 
    COUNT(*) as total_tags,
    SUM(CASE WHEN grp > '${current_date}' THEN 1 ELSE 0 END) as future_dates,
    SUM(CASE WHEN grp < '2020-01-01' THEN 1 ELSE 0 END) as too_old_dates,
    SUM(CASE WHEN grp NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' THEN 1 ELSE 0 END) as invalid_format
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期';
```

### 3. 数据完整性监控
```sql
-- 监控标签生成完整性
SELECT 
    dt,
    COUNT(DISTINCT userid) as tagged_users,
    -- 对比DWS源表中有最后活跃日期的用户数
    (SELECT COUNT(DISTINCT userId) 
     FROM lofter.dws_par_user_reward_center_active_dd 
     WHERE dt = '${check_date}' AND last_active_date IS NOT NULL) as source_users
FROM user_tag_table 
WHERE tag_name = '权益中心最后活跃日期' AND dt = '${check_date}'
GROUP BY dt;
```

## 性能优化策略

### 1. DWS层优化
- **索引优化**: 在 `last_active_date` 字段上建立索引
- **分区裁剪**: 利用dt分区进行数据过滤
- **计算优化**: `MAX(dt)` 操作相对高效

### 2. 标签层优化
- **条件下推**: 在DWS层就过滤 `last_active_date IS NOT NULL`
- **内存优化**: 标签数据量相对较小，内存占用可控

### 3. 查询优化
- **日期比较**: 使用 `datediff` 函数进行日期差值计算
- **索引利用**: ClickHouse中可对 `grp` 字段建立索引

## 运营应用建议

### 1. 召回策略
| 未活跃天数 | 风险等级 | 召回策略 |
|-----------|----------|----------|
| 1-3天 | 低风险 | 轻度提醒：每日签到奖励 |
| 4-7天 | 中风险 | 主动关怀：专属优惠推送 |
| 8-15天 | 高风险 | 强力召回：限时高价值奖励 |
| 16-30天 | 严重流失 | 重新激活：个性化内容推荐 |
| >30天 | 深度流失 | 长期培养：节假日问候 |

### 2. 个性化推荐
- **基于最后活跃日期**: 推送用户错过的重要更新
- **结合用户偏好**: 根据历史访问行为定制内容
- **时机选择**: 在用户历史活跃时间段推送

### 3. 效果评估
- **召回成功率**: 被召回用户的重新活跃比例
- **留存改善**: 召回后的用户留存情况
- **价值提升**: 召回用户的后续价值贡献

## 版本历史
- **V1.0** (2026-04-29): 初始实现
  - 在DWS表中新增 `last_active_date` 字段
  - 实现权益中心最后活跃日期用户标签
  - 建立完整的数据质量监控体系

## 相关标签协同

| 标签名称 | 关系类型 | 协同价值 |
|---------|----------|----------|
| 权益中心活跃天数 | 互补 | 结合分析用户活跃强度 |
| 权益中心活跃 | 互补 | 验证活跃状态一致性 |
| 最后活跃日期 | 对比 | 分析权益中心与整体活跃差异 |

通过多标签联合分析，可以更精准地描绘用户画像，制定个性化的运营策略。