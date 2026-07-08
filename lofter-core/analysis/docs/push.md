# LOFTER项目Push任务完整分析报告

## 概述

本报告对LOFTER项目中所有push相关任务进行了全面分析，涵盖了作业目录结构、业务逻辑、调度配置、数据流向、优先级体系和具体调度时间等方面。

## 一、Push任务完整清单

### 1.1 按调度时间分组的Push任务详情

#### 上午时段（10:00-12:00）

##### push-1000/ (10:00执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_follow_notice_950 | 6 | 1 | follow_notice_950 | 关注通知推送 | lofter_dm.ads_push_task_pool_di |
| lofter_push_comment_interaction | 5 | 1 | comment_interaction | 评论互动推送 | lofter_dm.ads_push_task_pool_di |
| lofter_push_read_later_remind | 27 | 3 | follow_notice_950 | 稍后阅读提醒 | lofter_dm.ads_push_task_pool_di |

##### push-1100/ (11:00执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_follow_notice_1100 | 6 | 1 | follow_notice_1100 | 关注通知推送 | lofter_dm.ads_push_task_pool_di |

##### push-1200/ (12:00执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| 零兴趣用户推荐内容推送 | - | - | zero_interest_rec | 零兴趣用户推荐 | lofter_dm.ads_push_task_pool_di |

##### push-1200-alg/ (12:00执行 - 算法推送)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_user_tag_tc | 23 | 1 | user_tag_tc | 用户标签推送（TC算法）| LOFTER.PUSH.NEW.TASK |
| lofter_push_hot_comment_active | 21 | 20 | alg_hot_comment_active | 热门评论推送（活跃用户）| LOFTER.PUSH.NEW.TASK |
| lofter_push_tag_golden_active | 21 | 10 | alg_tag_golden_active | 黄金标签推送（活跃用户）| LOFTER.PUSH.NEW.TASK |

#### 下午/晚间时段（18:00-19:30）

##### push-1800-alg/ (18:00执行 - 算法推送)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_user_tag_tx | 24 | 1 | user_tag_tx | 用户标签推送（TX算法）| LOFTER.PUSH.NEW.TASK |
| lofter_push_user_tag_tq | 25 | 2 | user_tag_tq | 用户标签推送（TQ算法）| LOFTER.PUSH.NEW.TASK |
| lofter_push_tag_golden_inactive | 22 | 1 | alg_tag_golden_inactive | 黄金标签推送（非活跃用户）| LOFTER.PUSH.NEW.TASK |
| lofter_push_hot_comment_inactive | 22 | 1 | alg_hot_comment_inactive | 热门评论推送（非活跃用户）| LOFTER.PUSH.NEW.TASK |

##### push-1830/ (18:30执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_active_top1 | 8 | 1 | active_top1 | 活跃用户热门内容推送 | lofter_dm.ads_push_task_pool_di |
| lofter_push_interest_tag_content | 28 | 1-10 | interest_tag_content | 兴趣标签个性化内容推送（回流/新用户）| LOFTER.PUSH.NEW.TASK |
| lofter_push_active_hot30_grain | 9 | 3/2/1 | active_hot30_grain | 活跃用户热门内容推送（细分）| lofter_dm.ads_push_task_pool_di |
| lofter_push_follow_notice_aggregate | 12 | 1 | follow_notice_aggregate | 关注通知聚合推送 | lofter_dm.ads_push_task_pool_di |
| lofter_push_follow_notice_1700 | 6 | 1 | follow_notice_1700 | 关注通知推送（1700）| lofter_dm.ads_push_task_pool_di |
| lofter_push_best_collection | - | 1 | best_collection.job | 精选收藏推荐 | lofter_dm.ads_push_task_pool_di |
| lofter_return_user_tag_rec_push | - | 1 | return_user_tag_rec | 回流用户标签推荐 | lofter_dm.ads_push_task_pool_di |

##### push-1930/ (19:30执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_user_tag_zx | 26 | 1 | user_tag_zx | 用户标签推送（ZX算法）| lofter_dm.ads_push_task_pool_di |
| lofter_push_search_tag_update | 13 | 1 | search_tag_update | 搜索标签更新推送 | LOFTER.PUSH.NEW.TASK |
| lofter_push_tag_hot_update | 15 | 1 | tag_hot_update | 标签热门更新推送 | LOFTER.PUSH.NEW.TASK |
| lofter_push_discuss_question_update | 19 | 1 | discuss_question_update | 问题讨论更新推送 | LOFTER.PUSH.NEW.TASK |
| lofter_push_score_question_update | 18 | 1 | score_question_update | 问题评分更新推送 | LOFTER.PUSH.NEW.TASK |
| lofter_push_collection_update | - | 1 | collection_update | 收藏更新推送 | lofter.push.collection.update |
| lofter_similar_blog_push | - | 1 | similar_blog | 相似博客推送 | lofter.push.similar.blog |

#### 特殊频率任务

##### push-hourly/ (每小时执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_new_post_interact | 3 | - | new_post_interact | 新内容互动推送 | lofter.push.follow.notice |

##### push-1000-weekly/ (每周执行)
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| lofter_push_creator_weekly_report | 4 | 1 | creator_weekly_report | 创作者周报推送 | lofter_dm.ads_push_task_pool_di |

### 1.2 特殊功能Push任务

#### push-user-tag/
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| ads_push_user_tag_source_di | - | - | user_tag_source | 用户标签源数据生成 | - |

#### push-return-test/
| 任务名称 | messageType | priority | executionId | 功能描述 | Kafka Topic |
|---------|-------------|----------|-------------|----------|-------------|
| ads_return_user_push_exp_di | - | - | return_user_exp | 回流用户实验推送 | - |
| ads_push_return_achieved_task_di | - | - | return_achieved_task | 回流任务达成推送 | - |

### 1.3 Push任务池管理

| 任务池 | 调度表 | 目标Topic | 服务器集群 | 功能描述 |
|--------|--------|----------|------------|----------|
| push-task-pool1 | ads_push_task_pool1_schedule_di | lofter.push.json | lofter-kafka-dc1/dc2/dc3 | 任务池1 |
| push-task-pool2 | ads_push_task_pool2_schedule_di | lofter.push.json | lofter-kafka-dc1/dc2/dc3 | 任务池2 |
| push-task-pool3 | ads_push_task_pool3_schedule_di | lofter.push.json | lofter-kafka-dc1/dc2/dc3 | 任务池3 |
| push-task-pool4 | ads_push_task_pool4_schedule_di | lofter.push.json | lofter-kafka-bi-risk1/2/3 | 任务池4 |

## 二、优先级体系详解

### 2.1 动态优先级计算机制

基于用户活跃度的动态优先级分配：

#### 高活跃用户 (1天、3天、7天活跃)
| 推送类型 | policy | 计算优先级 |
|----------|--------|------------|
| 新内容互动 | new_post_interact | 110 |
| 评论互动 | comment_interaction | 120 |
| 关注通知 | follow_notice | 130 |
| 创作者周报 | creator_weekly_report | 140 |
| 收藏更新 | collection_update | 210 |
| 标签热门更新 | tag_hot_update | 220 |
| 问题讨论更新 | discuss_question_update | 230 |
| 关注通知聚合 | follow_notice_aggregate | 310 |
| 搜索标签更新 | search_tag_update | 410 |
| 问题评分更新 | score_question_update | 510 |
| 问题讨论更新 | discuss_question_update | 520 |
| 活跃用户热门 | active_top1 | 710 |

#### 中等活跃用户 (15天、30天活跃)
| 推送类型 | policy | 计算优先级 |
|----------|--------|------------|
| 评论互动 | comment_interaction | 120 |
| 关注通知 | follow_notice | 130 |
| 创作者周报 | creator_weekly_report | 140 |
| 收藏更新 | collection_update | 210 |
| 关注通知聚合 | follow_notice_aggregate | 310 |
| 15天活跃用户内容 | active_hot15_grain | 410 |
| 30天活跃用户内容 | active_hot30_grain | 410 |

#### 默认优先级
- 未匹配规则的推送：1000

### 2.2 静态优先级设置

各个push作业中设置的静态优先级：

| priority值 | 应用任务 | 说明 |
|-----------|----------|------|
| 1 | 大部分推送任务 | 标准优先级 |
| 2 | lofter_push_user_tag_tq | 中等优先级 |
| 3 | lofter_push_read_later_remind, lofter_push_active_hot30_grain（部分）| 较高优先级 |
| 10 | lofter_push_tag_golden_active | 高优先级 |
| 20 | lofter_push_hot_comment_active | 最高优先级 |

## 三、调度时间体系详解

### 3.1 精确调度时间安排

| 时间段 | 调度组 | 主要功能类型 |
|--------|--------|-------------|
| 09:50 | push-1000 | 关注通知、评论互动、稍后阅读 |
| 11:00 | push-1100 | 关注通知补充 |
| 12:00 | push-1200 | 零兴趣用户推荐 |
| 12:00 | push-1200-alg | 算法推荐（活跃用户） |
| 18:00 | push-1800-alg | 算法推荐（非活跃用户） |
| 18:30 | push-1830 | 活跃用户专项推送 |
| 19:30 | push-1930 | 内容更新和运营推送 |
| 每小时 | push-hourly | 实时互动推送 |
| 每周一 | push-1000-weekly | 创作者周报 |

### 3.2 任务池调度机制

任务池调度基于时间窗口控制：
- 任务池1-4：每日10:00前的任务进入调度队列
- 时间窗口：`from_unixtime(cast(create_time/1000 as bigint),'yyyy-MM-dd HH:mm') < '${azkaban.flow.current.date} 10:00'`

## 四、业务逻辑架构

### 4.1 核心推送策略

#### 用户分层推送策略
基于用户活跃度进行分层推送：
- **1天活跃用户**: 最高优先级推送（110-140）
- **3天活跃用户**: 高优先级推送（同1天活跃）
- **7天活跃用户**: 高优先级推送（同1天活跃）
- **15天活跃用户**: 中等优先级推送，特定内容（active_hot15_grain）
- **30天活跃用户**: 中等优先级推送，特定内容（active_hot30_grain）

#### 算法推荐机制
- **TC算法** (messageType 23): 12:00执行，priority 1
- **TX算法** (messageType 24): 18:00执行，priority 1  
- **TQ算法** (messageType 25): 18:00执行，priority 2
- **ZX算法** (messageType 26): 19:30执行，priority 1

### 4.2 推送内容类型矩阵

#### 社交互动推送
- **关注通知** (messageType 6): 多时段覆盖（9:50, 11:00, 17:00）
- **评论互动** (messageType 5): 10:00执行
- **新内容互动** (messageType 3): 每小时执行

#### 内容推荐推送
- **热门内容** (messageType 8, 9): 18:30执行，基于活跃度分层
- **个性化推荐** (messageType 21, 22): 12:00和18:00分别针对活跃/非活跃用户
- **标签相关** (messageType 13, 15): 19:30执行标签更新推送

#### 运营推送
- **创作者周报** (messageType 4): 每周执行
- **问题更新** (messageType 18, 19): 19:30执行
- **收藏推荐**: 18:30执行精选收藏

### 4.3 推送优化特性

#### 防重推机制
- 跨时间段去重：通过policy_rk排序避免重复
- 用户级别去重：`row_number() over (partition by userId order by priority, create_time)`
- 历史推送记录维护

#### 个性化推送时机
- 基于用户活跃度调整推送时间窗口
- 动态优先级计算确保重要推送及时送达
- 多任务池负载均衡

## 五、数据流向和依赖关系

### 5.1 数据流架构

```
源数据表 → 算法计算作业 → 推送候选表 → 任务池调度表 → Kafka推送
```

### 5.2 核心数据表

#### 主要推送表
- **lofter_dm.ads_push_task_pool_di**: 主要推送任务汇总表
- **lofter_dm.ads_push_task_poolN_schedule_di**: 任务池调度表（N=1,2,3,4）
- **lofter_dm.ads_push_user_tag_source_di**: 用户标签推送源数据

#### 依赖数据表
- **lofter.dws_par_user_active_dd**: 用户活跃度数据
- **lofter.dws_tag_user_consume_di**: 用户标签消费数据
- **lofter.dwd_post_browse_di**: 用户浏览行为数据

### 5.3 Kafka消息队列体系

#### 主要Topics分类
- **通用推送**: `lofter.push.json`
- **算法推送**: `LOFTER.PUSH.NEW.TASK`
- **专项推送**: `lofter.push.follow.notice`, `lofter.push.collection.update`, `lofter.push.similar.blog`

#### Kafka集群配置
- **主集群**: lofter-kafka-dc1/dc2/dc3.gy.ntes:9092
- **风控集群**: lofter-kafka-bi-risk1/risk2/risk3.gy.ntes:9092

## 六、技术架构特点

### 6.1 分层架构设计
- **数据层**: 基于Hive的数据仓库，存储用户行为和内容数据
- **计算层**: 基于Spark的分布式计算，进行特征计算和推荐算法
- **调度层**: 基于Azkaban的工作流调度，管理任务依赖和执行时间
- **消息层**: 基于Kafka的消息队列，实现高并发推送分发
- **应用层**: 客户端推送服务，负责最终的消息推送

### 6.2 扩展性设计
- **水平扩展**: 通过4个任务池支持推送量的水平扩展
- **垂直扩展**: 每个组件都支持独立的性能优化和扩展
- **插件化**: 新的推送类型可以通过添加新的作业轻松接入

### 6.3 可靠性保障
- **容错机制**: 通过依赖管理确保上游数据准备完成后才执行推送
- **监控告警**: 完整的任务监控和异常告警机制
- **数据校验**: 推送前的数据质量校验和异常数据过滤

## 七、MessageType完整映射表

| messageType | 推送类型 | 调度时间 | Priority | 主要作业文件 | Topic |
|-------------|----------|----------|----------|-------------|-------|
| 3 | 新内容互动 | 每小时 | - | lofter_new_post_interact | lofter.push.follow.notice |
| 4 | 创作者周报 | 每周 | 1 | lofter_push_creator_weekly_report | lofter_dm.ads_push_task_pool_di |
| 5 | 评论互动 | 10:00 | 1 | lofter_push_comment_interaction | lofter_dm.ads_push_task_pool_di |
| 6 | 关注通知 | 9:50/11:00/17:00 | 1 | lofter_push_follow_notice_* | lofter_dm.ads_push_task_pool_di |
| 8 | 活跃用户热门内容 | 18:30 | 1 | lofter_push_active_top1 | lofter_dm.ads_push_task_pool_di |
| 9 | 活跃用户热门内容（细分）| 18:30 | 1-3 | lofter_push_active_hot30_grain | lofter_dm.ads_push_task_pool_di |
| 12 | 关注通知聚合 | 18:30 | 1 | lofter_push_follow_notice_aggregate | lofter_dm.ads_push_task_pool_di |
| 13 | 搜索标签更新 | 19:30 | 1 | lofter_push_search_tag_update | LOFTER.PUSH.NEW.TASK |
| 15 | 标签热门更新 | 19:30 | 1 | lofter_push_tag_hot_update | LOFTER.PUSH.NEW.TASK |
| 18 | 问题评分更新 | 19:30 | 1 | lofter_push_score_question_update | LOFTER.PUSH.NEW.TASK |
| 19 | 问题讨论更新 | 19:30 | 1 | lofter_push_discuss_question_update | LOFTER.PUSH.NEW.TASK |
| 21 | 热门评论推送（活跃）| 12:00 | 20/10 | lofter_push_hot_comment_active, lofter_push_tag_golden_active | LOFTER.PUSH.NEW.TASK |
| 22 | 算法推送（非活跃）| 18:00 | 1 | lofter_push_*_inactive | LOFTER.PUSH.NEW.TASK |
| 23 | 用户标签推送（TC）| 12:00 | 1 | lofter_push_user_tag_tc | LOFTER.PUSH.NEW.TASK |
| 24 | 用户标签推送（TX）| 18:00 | 1 | lofter_push_user_tag_tx | LOFTER.PUSH.NEW.TASK |
| 25 | 用户标签推送（TQ）| 18:00 | 2 | lofter_push_user_tag_tq | LOFTER.PUSH.NEW.TASK |
| 26 | 用户标签推送（ZX）| 19:30 | 1 | lofter_push_user_tag_zx | lofter_dm.ads_push_task_pool_di |
| 27 | 稍后阅读提醒 | 10:00 | 3 | lofter_push_read_later_remind | lofter_dm.ads_push_task_pool_di |

## 八、总结

LOFTER的push系统是一个高度复杂和精细化的用户推送平台，具有以下核心特点：

1. **精确的时间调度**: 从上午9:50到晚上19:30，覆盖用户活跃时段的多时段精准推送
2. **动态优先级体系**: 基于用户活跃度的智能优先级计算，确保重要推送及时到达
3. **智能算法驱动**: 结合TC/TX/TQ/ZX等多种算法的个性化推荐
4. **分层运营策略**: 针对不同活跃度用户的差异化推送策略
5. **负载均衡架构**: 通过4个任务池和多kafka集群实现高并发处理
6. **完整的消息类型体系**: 从messageType 3到27的完整推送类型覆盖

该系统有效提升了LOFTER作为内容社区平台的用户活跃度、内容消费效率和创作者生态健康度，体现了对用户体验和商业价值的精细化平衡。

---

*文档更新时间: 2026年4月1日*  
*分析范围: LOFTER项目analysis模块所有push相关任务*  
*文档版本: v2.0 - 包含优先级和调度时间详情*