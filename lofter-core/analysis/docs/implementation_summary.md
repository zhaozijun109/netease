# 新回用户行为策略实现总结

## 已完成的文件

### 策略一：回流用户历史兴趣内容推荐（调度12:00）

1. **数据准备作业**: `analysis/jobs/push-1200/ads_push_return_user_history_interest_di.job`
   - 筛选有历史兴趣但回流当天无行为的用户
   - 基于历史兴趣标签匹配推荐内容
   - MessageType: 30, Priority: 2

2. **推送作业**: `analysis/jobs/push-1200/lofter_push_return_user_history_interest.job`
   - 将匹配结果格式化并推送到任务池
   - ExecutionId: `return_user_history_interest`

### 策略二：无明确兴趣与行为的新回用户热搜内容推荐（调度19:30）

1. **数据准备作业**: `analysis/jobs/push-1930/ads_push_new_return_user_hot_content_di.job`
   - 筛选无历史兴趣且无行为的新回用户
   - 基于热搜榜单推荐热门内容
   - MessageType: 31, Priority: 1

2. **推送作业**: `analysis/jobs/push-1930/lofter_push_new_return_user_hot_content.job`
   - 将匹配结果格式化并推送到任务池
   - ExecutionId: `new_return_user_hot_content`

## 策略特点对比

| 策略 | 调度时间 | 目标用户 | 内容来源 | 个性化程度 | 覆盖范围 |
|------|----------|----------|----------|------------|----------|
| 回流历史兴趣 | 12:00 | 有历史兴趣的回流用户 | 基于兴趣标签的内容 | 高（个性化） | 中等 |
| 新回热搜内容 | 19:30 | 无兴趣无行为的新回用户 | 热搜榜单内容 | 低（通用） | 高（兜底） |

## 核心实现逻辑

### 用户筛选条件（AND关系）

**策略一（回流用户历史兴趣）**:
- ✅ 有历史兴趣的回流用户
- ✅ 在兴趣弹窗中无行为 或 跳过兴趣弹窗
- ✅ 回流当天没有浏览内容
- ✅ 回流次日截止12:00前不活跃

**策略二（新回用户热搜内容）**:
- ✅ 新回用户（新用户+回流用户）
- ✅ 无历史兴趣
- ✅ 新回当天没有任何有效行为
- ✅ 新回次日截止19:30前不活跃

## 技术实现要点

1. **数据表依赖**:
   - `lofter.dwd_user_return_di` - 回流用户识别
   - `lofter.dwd_user_new_di` - 新用户识别
   - `lofter.dwd_beginner_guide_page_events_di` - 兴趣弹窗行为
   - `lofter_db_dump.ods_db_blog_settings_nd` - 历史兴趣设置
   - `lofter.ods_mda_app_raw_di` - 用户行为数据
   - `lofter_dm.ads_hot_search_list_di` - 热搜榜单

2. **防重复推送**:
   - 7天历史推送去重
   - 排除已曝光内容
   - 排除用户禁用标签

3. **内容质量控制**:
   - 策略一：基于hot_30d筛选优质内容
   - 策略二：基于热搜排名和发布时效性

## 预期效果

- **策略一**: 通过精准的个性化推荐，提升有兴趣基础的回流用户召回效果
- **策略二**: 作为兜底策略，确保无明确兴趣偏好的新回用户也能被有效触达

## 下一步工作

1. **测试验证**: 在测试环境验证SQL逻辑和数据质量
2. **Azkaban配置**: 配置调度任务和依赖关系
3. **监控设置**: 建立推送效果监控指标
4. **灰度上线**: 小流量测试后逐步放量