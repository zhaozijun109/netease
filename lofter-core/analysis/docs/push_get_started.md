# LOFTER Push任务新增指导手册

## 概述

本文档提供了在LOFTER项目中新增push任务的完整指导流程。通过遵循本指南，您可以快速、准确地创建新的push任务，确保与现有系统的兼容性和一致性。

## 前置条件

在开始之前，请确保您已经：
1. 熟悉LOFTER push系统的整体架构（参考 `analysis/docs/push.md`）
2. 了解Azkaban工作流调度系统
3. 具备Spark SQL和Hive的基础知识
4. 有相应的开发环境和权限

## 一、Push任务架构解析

### 1.1 双作业模式（推荐）

LOFTER的push任务通常采用双作业模式：

```
数据准备作业 → 推送作业
     ↓           ↓
  *_di.job   lofter_push_*.job
```

**数据准备作业** (`ads_push_*_di.job`)
- 负责数据清洗、过滤、计算
- 输出到中间表 `lofter_dm.ads_push_*_di`
- 为推送作业提供清洁的数据源

**推送作业** (`lofter_push_*.job`)
- 从中间表读取数据
- 格式化为标准推送格式
- 写入任务池表进行推送

### 1.2 单作业模式（简单场景）

对于简单的推送场景，可以使用单作业模式直接进行推送。

## 二、新增Push任务完整流程

### 步骤1：规划设计

#### 1.1 确定基本信息
- **功能描述**：明确推送的业务目标
- **目标用户群体**：确定推送对象（活跃用户、新用户等）
- **调度时间**：选择合适的时间段（参考现有调度安排）
- **messageType**：分配新的messageType编号（当前已使用：3-27）
- **优先级**：确定priority值（1-20，根据重要程度）

#### 1.2 选择调度组
根据业务需求选择合适的调度组：
- `push-1000/` (10:00) - 上午推送，适合关注通知类
- `push-1200-alg/` (12:00) - 午间算法推送
- `push-1800-alg/` (18:00) - 晚间算法推送  
- `push-1830/` (18:30) - 黄金时段推送
- `push-1930/` (19:30) - 运营推送
- `push-hourly/` - 实时推送

### 步骤2：创建数据准备作业

#### 2.1 文件创建
```bash
# 创建数据准备作业文件
touch analysis/jobs/push-{调度组}/ads_push_{功能名}_di.job
```

#### 2.2 作业模板

典型的push任务由**人群筛选**和**内容筛选**两个核心部分构成：

```sql
type=sparksql
hive.query.01=\
-- ============================================
-- 第一部分：内容筛选 (Content Filtering)
-- ============================================
with qualified_content as (
    -- 内容质量筛选
    select postId,
           blogId,
           title,
           contentType,
           publishTime,
           hot_score,
           tag_list
    from {内容源表}  -- 如：lofter.dwd_post_publish_di
    where dt = '${azkaban.flow.current.date}'
      -- 内容状态筛选
      and isPublished = true           -- 已发布
      and isForbidden = false          -- 非违规
      and isMoved = 0                  -- 非搬运
      and allowView = 0                -- 公开可见
      -- 内容类型筛选  
      and contentType in ('文字', '图片', '视频')
      -- 内容质量筛选
      and hot_score >= {热度阈值}       -- 如：100
      -- 内容时效筛选
      and publishTime >= {时间范围}     -- 如：最近7天
),

-- ============================================
-- 第二部分：人群筛选 (Audience Targeting)  
-- ============================================
target_users as (
    -- 基础活跃用户筛选
    select userId,
           active_days,
           last_active_time
    from lofter.dws_par_user_active_dd 
    where dt = '${azkaban.flow.1.days.ago}'
      -- 活跃度筛选
      and active_days_30d > 0          -- 30天内活跃
      and active_days_7d > 0           -- 7天内活跃（可选）
),
interest_users as (
    -- 兴趣标签匹配筛选
    select userId,
           interest_tags,
           consume_score
    from lofter.dws_tag_user_consume_di
    where dt between date_sub('${azkaban.flow.current.date}', 7) 
                 and '${azkaban.flow.1.days.ago}'
      and consume_score >= {兴趣阈值}   -- 如：50
    group by userId
    having count(distinct tag) >= {标签数量}  -- 如：3个兴趣标签
),
filtered_users as (
    -- 用户行为筛选（根据具体业务需求）
    select t1.userId
    from target_users t1
    join interest_users t2 on t1.userId = t2.userId
    left join {历史推送表} h 
        on t1.userId = h.userId 
        and h.dt >= date_sub('${azkaban.flow.current.date}', {去重天数})
    where h.userId is null              -- 历史去重
      -- 其他业务筛选条件
      and {额外筛选条件}
),

-- ============================================
-- 第三部分：内容与人群匹配 (Content-User Matching)
-- ============================================
matched_results as (
    -- 内容与用户匹配逻辑
    select u.userId,
           c.postId,
           c.blogId,
           c.title as content,
           c.hot_score,
           row_number() over(
               partition by u.userId 
               order by c.hot_score desc, c.publishTime desc
           ) as content_rank
    from filtered_users u
    join qualified_content c 
        on {匹配条件}                   -- 如：标签匹配、兴趣匹配等
    left join lofter.dwd_post_browse_di b
        on u.userId = b.userId 
        and c.postId = b.postId
        and b.dt >= date_sub('${azkaban.flow.current.date}', 30)
    where b.postId is null              -- 排除已浏览内容
),

-- ============================================
-- 第四部分：最终结果生成 (Final Result Generation)
-- ============================================
final_push_data as (
    select userId,
           postId,
           blogId,
           content,
           -- 其他推送需要的字段
           unix_timestamp() as create_timestamp
    from matched_results
    where content_rank <= {每用户推送数量}  -- 如：3条内容
)

-- 输出到目标表
insert overwrite table lofter_dm.ads_push_{功能名}_di 
partition(dt = '${azkaban.flow.current.date}')
select *
from final_push_data
```

#### 2.3 模板结构说明

**四个核心部分的设计理念**：

1. **内容筛选 (qualified_content)**
   - 确保推送内容的质量和合规性
   - 包含状态、类型、质量、时效四个维度的筛选
   - 为后续匹配提供高质量内容池

2. **人群筛选 (target_users + interest_users + filtered_users)**
   - 基础活跃度筛选：确保用户仍然活跃
   - 兴趣标签筛选：确保推送内容符合用户兴趣  
   - 行为筛选和去重：避免重复推送，提升用户体验

3. **内容匹配 (matched_results)**
   - 根据业务逻辑将合适的内容推荐给合适的用户
   - 排除用户已浏览内容，避免重复
   - 按内容质量和时效性排序

4. **结果生成 (final_push_data)**
   - 控制每用户推送数量，避免过度打扰
   - 生成标准化的推送数据格式

#### 2.4 关键技术要点
- **用户去重**：通过历史推送表去重，避免重复推送
- **内容去重**：排除用户已浏览的内容
- **数据质量**：多维度内容筛选确保推送质量
- **性能优化**：合理使用分区和join策略
- **时间参数**：统一使用Azkaban时间参数
- **排序逻辑**：按内容热度和发布时间排序，确保推送优质内容

### 步骤3：创建推送作业

#### 3.1 文件创建
```bash
# 创建推送作业文件
touch analysis/jobs/push-{调度组}/lofter_push_{功能名}.job
```

#### 3.2 作业模板
```sql
type=sparksql
hive.query.01=insert overwrite table lofter_dm.ads_push_task_pool_di partition(dt = '${azkaban.flow.current.date}', executionId= '{execution_id}') \
select userId, \
       '{push_topic}' as push_topic, \
       to_json(struct(*)) as push_params, \
       '{policy}' as policy, \
       {priority} as priority, \
       unix_timestamp()*1000L as create_time \
from ( \
    select userId, {messageType} as messageType, {其他字段} \
    from lofter_dm.ads_push_{功能名}_di \
    where dt = '${azkaban.flow.current.date}' \
) t \

dependencies=ads_push_{功能名}_di
```

#### 3.3 参数说明
- **push_topic**: 
  - `LOFTER.PUSH.NEW.TASK` - 算法推送
  - `lofter.push.json` - 通用推送
  - `lofter.push.follow.notice` - 关注通知
  - `lofter.push.collection.update` - 收藏更新
  - `lofter.push.similar.blog` - 相似推荐

- **executionId**: 唯一标识符，建议使用功能名
- **policy**: 推送策略名，用于优先级计算
- **messageType**: 新分配的消息类型编号
- **priority**: 静态优先级（1-20）

### 步骤4：目录结构组织

```
analysis/jobs/push-{调度组}/
├── ads_push_{功能名}_di.job          # 数据准备作业
├── lofter_push_{功能名}.job           # 推送作业
└── (其他相关作业)
```

### 步骤5：测试和验证

#### 5.1 数据验证
```sql
-- 检查数据准备作业输出
select count(*) as total_users,
       count(distinct userId) as unique_users
from lofter_dm.ads_push_{功能名}_di 
where dt = '{test_date}';

-- 检查推送作业输出
select count(*) as push_tasks,
       messageType,
       policy,
       priority
from lofter_dm.ads_push_task_pool_di 
where dt = '{test_date}' 
  and executionId = '{execution_id}'
group by messageType, policy, priority;
```

#### 5.2 流程测试
1. **单独测试数据准备作业**
2. **测试推送作业的依赖关系**
3. **验证数据格式和完整性**
4. **检查任务池优先级分配**

## 三、配置参数详解

### 3.1 MessageType分配

当前已使用的messageType：
- 3: 新内容互动
- 4: 创作者周报  
- 5: 评论互动
- 6: 关注通知
- 8: 活跃用户热门内容
- 9: 活跃用户热门内容（细分）
- 12: 关注通知聚合
- 13: 搜索标签更新
- 15: 标签热门更新
- 18: 问题评分更新
- 19: 问题讨论更新
- 21: 热门评论推送（活跃）
- 22: 算法推送（非活跃）
- 23-26: 用户标签推送（TC/TX/TQ/ZX）
- 27: 稍后阅读提醒

**新messageType建议**：从28开始分配

### 3.2 优先级设置指南

#### 静态优先级（priority字段）
- **1**: 标准优先级，大部分推送使用
- **2-3**: 中等优先级，重要但非紧急
- **10**: 高优先级，重要推送
- **20**: 最高优先级，关键推送

#### 动态优先级（任务池计算）
系统会根据用户活跃度和policy自动计算最终优先级：
- 110-140: 高活跃用户推送
- 210-230: 中等优先级推送
- 310-520: 特定功能推送
- 710: 活跃用户专项推送
- 1000: 默认优先级

### 3.3 调度时间选择

| 时间段 | 适用场景 | 用户特点 |
|--------|----------|----------|
| 09:50 | 早晨推送，关注类通知 | 通勤时间，关注社交动态 |
| 12:00 | 午间推送，算法推荐 | 午休时间，内容消费 |
| 18:00 | 晚间推送，非活跃用户 | 下班时间，回流激活 |
| 18:30 | 黄金时段，活跃用户 | 最佳推送时间 |
| 19:30 | 运营推送，内容更新 | 晚间娱乐时间 |

## 四、最佳实践

### 4.1 数据质量保障
1. **用户去重**：确保每个用户只收到一次推送
2. **数据验证**：检查必要字段的完整性
3. **过滤规则**：排除黑名单用户和无效用户
4. **历史去重**：避免重复推送相同内容

### 4.2 性能优化
1. **分区使用**：合理利用dt分区
2. **索引优化**：在大表join时使用适当的join策略
3. **资源控制**：合理设置Spark资源参数
4. **执行时间**：避免在高峰期执行大数据量任务

### 4.3 监控和告警
1. **数据量监控**：设置合理的数据量阈值
2. **执行时间监控**：关注作业执行时间
3. **错误处理**：设置合适的重试机制
4. **依赖管理**：确保依赖关系正确

## 五、常见问题和解决方案

### 5.1 常见错误

#### 依赖关系错误
```bash
# 错误：依赖作业名称错误
dependencies=wrong_job_name

# 正确：使用正确的作业名称
dependencies=ads_push_功能名_di
```

#### 字段缺失错误
```sql
-- 错误：缺少必要字段
select userId, messageType
from source_table

-- 正确：包含所有必要字段
select userId, messageType, itemId, blogId, content
from source_table
```

#### 时间参数错误
```sql
-- 错误：硬编码时间
where dt = '2026-04-01'

-- 正确：使用参数化时间
where dt = '${azkaban.flow.current.date}'
```

### 5.2 调试技巧

1. **逐步调试**：先测试数据准备作业，再测试推送作业
2. **小数据集测试**：使用限制条件测试少量数据
3. **日志查看**：通过Azkaban界面查看详细日志
4. **数据抽样**：验证关键步骤的数据正确性

### 5.3 故障恢复

1. **重跑机制**：Azkaban支持单个作业重跑
2. **数据修复**：通过手动SQL修复错误数据
3. **依赖重置**：重新执行依赖的上游作业
4. **监控报警**：及时发现和处理异常

## 六、代码示例

### 6.1 完整示例：新用户欢迎推送

#### 数据准备作业 (ads_push_new_user_welcome_di.job)
```sql
type=sparksql
hive.query.01=\
-- ============================================
-- 第一部分：内容筛选 - 欢迎内容准备
-- ============================================
with welcome_content as (
    -- 为新用户准备欢迎内容（可以是热门内容、精选内容等）
    select 'welcome' as contentType,
           '欢迎来到LOFTER！探索你感兴趣的内容' as welcomeMessage,
           'system' as contentSource
),

-- ============================================
-- 第二部分：人群筛选 - 新注册用户
-- ============================================
new_registered_users as (
    -- 筛选新注册用户
    select userId, 
           registerTime
    from lofter.dws_par_user_profile_dd
    where dt = '${azkaban.flow.1.days.ago}'
      and registerTime >= unix_timestamp(date_sub('${azkaban.flow.current.date}', 1)) * 1000 
      and registerTime < unix_timestamp('${azkaban.flow.current.date}') * 1000 
),
qualified_new_users as (
    -- 过滤已经推送过的新用户
    select n.userId
    from new_registered_users n
    left join lofter_dm.ads_push_new_user_welcome_di h
        on n.userId = h.userId 
        and h.dt >= date_sub('${azkaban.flow.current.date}', 7)
    where h.userId is null  -- 7天内未推送过
),

-- ============================================
-- 第三部分：内容与人群匹配
-- ============================================
matched_welcome_push as (
    -- 为新用户匹配欢迎内容
    select u.userId,
           c.contentType,
           c.welcomeMessage,
           c.contentSource
    from qualified_new_users u
    cross join welcome_content c
)

-- 输出最终推送数据
insert overwrite table lofter_dm.ads_push_new_user_welcome_di 
partition(dt = '${azkaban.flow.current.date}')
select userId,
       contentType as welcomeType,
       welcomeMessage,
       contentSource
from matched_welcome_push
```

#### 推送作业 (lofter_push_new_user_welcome.job)
```sql
type=sparksql
hive.query.01=insert overwrite table lofter_dm.ads_push_task_pool_di partition(dt = '${azkaban.flow.current.date}', executionId= 'new_user_welcome') \
select userId, \
       'LOFTER.PUSH.NEW.TASK' as push_topic, \
       to_json(struct(*)) as push_params, \
       'new_user_welcome' as policy, \
       5 as priority, \
       unix_timestamp()*1000L as create_time \
from ( \
    select userId, 28 as messageType, welcomeType, welcomeMessage \
    from lofter_dm.ads_push_new_user_welcome_di \
    where dt = '${azkaban.flow.current.date}' \
) t \

dependencies=ads_push_new_user_welcome_di
```

### 6.2 高级示例：个性化内容推荐

#### 数据准备作业 (ads_push_personalized_content_di.job)
```sql
type=sparksql
hive.query.01=\
with user_interests as (
    select userId,
           collect_list(tagName) as interest_tags,
           avg(consume_score) as avg_score
    from lofter.dws_tag_user_consume_di
    where dt between date_sub('${azkaban.flow.current.date}', 7) 
                 and '${azkaban.flow.1.days.ago}'
    group by userId
    having count(*) >= 3  -- 至少3个兴趣标签
),
recommended_content as (
    select u.userId,
           p.postId,
           p.blogId,
           p.title as content,
           p.hot_score,
           row_number() over(partition by u.userId order by p.hot_score desc) as rn
    from user_interests u
    lateral view explode(interest_tags) t as tag
    join lofter.dwd_post_publish_di p 
        on array_contains(split(lower(p.tag), ','), tag)
    left join lofter.dwd_post_browse_di b
        on u.userId = b.userId and p.postId = b.postId
        and b.dt >= date_sub('${azkaban.flow.current.date}', 30)
    where p.post_publish_date = '${azkaban.flow.1.days.ago}'
      and p.post_content_type in ('文字', '图片')
      and b.postId is null  -- 用户未浏览过
      and p.hot_score >= 100  -- 热度阈值
),
final_recommendations as (
    select userId, postId, blogId, content
    from recommended_content
    where rn <= 3  -- 每用户推荐3个内容
)
insert overwrite table lofter_dm.ads_push_personalized_content_di partition(dt = '${azkaban.flow.current.date}')
select *
from final_recommendations
```

#### 推送作业 (lofter_push_personalized_content.job)
```sql
type=sparksql
hive.query.01=insert overwrite table lofter_dm.ads_push_task_pool_di partition(dt = '${azkaban.flow.current.date}', executionId= 'personalized_content') \
select userId, \
       'LOFTER.PUSH.NEW.TASK' as push_topic, \
       to_json(struct(*)) as push_params, \
       'personalized_content' as policy, \
       3 as priority, \
       unix_timestamp()*1000L as create_time \
from ( \
    select userId, 29 as messageType, postId as itemId, blogId, content \
    from lofter_dm.ads_push_personalized_content_di \
    where dt = '${azkaban.flow.current.date}' \
) t \

dependencies=ads_push_personalized_content_di
```

## 七、上线检查清单

### 7.1 开发完成检查
- [ ] 数据准备作业SQL语法正确
- [ ] 推送作业格式符合标准
- [ ] 依赖关系配置正确
- [ ] messageType未与现有冲突
- [ ] 优先级设置合理

### 7.2 测试验证检查
- [ ] 数据准备作业单独执行成功
- [ ] 推送作业依赖执行成功
- [ ] 输出数据格式正确
- [ ] 用户数量在合理范围内
- [ ] 无重复推送问题

### 7.3 上线前检查
- [ ] 调度时间确认
- [ ] 任务池分配确认
- [ ] 监控告警配置
- [ ] 回滚方案准备
- [ ] 相关团队通知

## 八、维护和优化

### 8.1 性能监控
- 定期检查作业执行时间
- 监控数据量变化趋势
- 关注系统资源使用情况
- 跟踪推送效果指标

### 8.2 持续优化
- 根据业务需求调整推送策略
- 优化SQL查询性能
- 完善用户画像和推荐算法
- 改进推送时机和频率

### 8.3 问题处理
- 建立问题处理流程
- 维护常见问题解决方案
- 定期review和改进
- 保持文档更新

## 九、相关资源

### 9.1 文档链接
- [Push任务完整分析报告](./push.md)
- [LOFTER数据字典](../data_dictionary.md)
- [Azkaban调度文档](../azkaban_guide.md)

### 9.2 联系人
- 数据开发团队：负责作业开发和维护
- 产品团队：负责推送策略和效果评估  
- 运维团队：负责系统监控和故障处理

### 9.3 工具和环境
- Azkaban Web界面：作业调度和监控
- Hive客户端：数据查询和调试
- Spark Web UI：作业性能监控

---

*文档创建时间: 2026年4月1日*  
*适用版本: LOFTER Push系统 v2.0*  
*维护团队: 数据开发团队*