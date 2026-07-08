# DWS_PAR_USER_REWARD_CENTER_ACTIVE_DD 实现说明

## 表概述
`lofter.dws_par_user_reward_center_active_dd` 是权益中心用户活跃度统计表，用于计算用户在权益中心的活跃天数。

## 实现详情

### 数据源
- **主要来源**: `lofter.dwd_rewardcenter_visit_di` - 权益中心用户访问明细表
- **辅助来源**: `lofter.dim_user` - 用户维度表（获取匿名用户标识）

### 计算逻辑
按用户ID聚合，统计不同时间窗口内的活跃天数：

| 字段 | 计算逻辑 | 时间范围 |
|------|---------|----------|
| `active_days_1d` | 当天访问天数 | 仅计算当天 |
| `active_days_7d` | 近7天访问天数 | [T-6, T] |
| `active_days_15d` | 近15天访问天数 | [T-14, T] |
| `active_days_30d` | 近30天访问天数 | [T-29, T] |
| `active_days_90d` | 近90天访问天数 | [T-89, T] |
| `active_days_180d` | 近180天访问天数 | [T-179, T] |

### SQL 核心逻辑
```sql
-- 按用户去重访问日期，然后统计不同时间窗口的活跃天数
count(distinct if(dt = '${target_date}', dt, null)) as active_days_1d,
count(distinct if(dt >= '${target_date - 7}', dt, null)) as active_days_7d,
count(distinct if(datediff('${target_date}', dt) < 15, dt, null)) as active_days_15d,
count(distinct if(dt >= '${target_date - 30}', dt, null)) as active_days_30d,
count(distinct if(datediff('${target_date}', dt) < 90, dt, null)) as active_days_90d,
count(distinct if(datediff('${target_date}', dt) < 180, dt, null)) as active_days_180d
```

### 数据范围
- **历史数据窗口**: 180天（支持最长周期统计）
- **分区字段**: `dt` (yyyy-MM-dd 格式)
- **用户过滤**: 排除无效用户ID（userId > 0）

### 依赖关系
```
dws_par_user_reward_center_active_dd
└── dwd_rewardcenter_visit_di
    └── ods_mda_app_di (权益中心相关事件)
```

### 调度配置
- **任务类型**: sparksql
- **执行频率**: 每日
- **依赖任务**: `dwd_rewardcenter_visit_di`
- **分区**: T-1 天数据

### 数据质量检查
1. **完整性检查**: 确保分区数据不为空
2. **一致性检查**: active_days_1d ≤ active_days_7d ≤ active_days_15d ≤ ... ≤ active_days_180d
3. **有效性检查**: 所有活跃天数字段应 ≥ 0 且 ≤ 对应的时间窗口长度

### 典型查询示例
```sql
-- 查看用户权益中心活跃度分布
SELECT 
    CASE 
        WHEN active_days_30d = 0 THEN '未活跃'
        WHEN active_days_30d BETWEEN 1 AND 7 THEN '低活跃' 
        WHEN active_days_30d BETWEEN 8 AND 15 THEN '中活跃'
        ELSE '高活跃'
    END AS activity_level,
    COUNT(*) AS user_count
FROM lofter.dws_par_user_reward_center_active_dd 
WHERE dt = '2026-04-28'
GROUP BY 1;
```

### 性能优化
1. **分区裁剪**: 基于dt字段进行分区过滤
2. **预聚合**: 在子查询中先去重，减少JOIN开销
3. **分布策略**: 使用 `DISTRIBUTE BY 1` 确保数据均匀分布

### 版本历史
- **V1.0** (2026-04-29): 初始实现，支持1/7/15/30/90/180天活跃度统计