# 项目背景
在 内容社区 lofter 中，我想要计算每个创作者对社区的“贡献度”，并以此来设计流量扶持方案，高贡献度的创作者能获得更多流量扶持，流量扶持目标是让创作者发文粘性更高。

# 大致思路
关于如何计算创作者的社区“贡献度“，目前我的大致思路是：创作者发文关联到对用户粘性贡献上，输入一批每个用户浏览和互动文章的行为、用户本身活跃度，以及用户浏览完文章后的次留或长留。输出每个文章对用户留存的作用，再映射到创作者对用户留存的贡献度上。其中可能会用到因果推断模型，也可以是其他更有效的统计或算法模型。

# 已有数据
目前已有的数据是一张宽表，表格中包含了用户和文章的交互特征、文章特征、用户特征，以及用户留存标记。字段具体如下：
```SQL
CREATE TABLE `rec`.`tmp_0324_creator_contribution_degree_input_wide_table`(
  -- 用户和文章的交互特征
  `dt` string COMMENT '日期', 
  `userid` bigint COMMENT '用户id', 
  `postid` bigint COMMENT '文章 id', 
  `duration` int COMMENT '用户浏览该文章的时长，单位秒', 
  `like_recommend_pv` bigint COMMENT '用户点赞该文章的次数', 
  `collect_pv` bigint COMMENT '用户收藏该文章的次数', 
  `comment_pv` bigint COMMENT '用户评论该文章的次数', 
  `share_pv` bigint COMMENT '用户分享该文章的次数', 
  `is_follow` int COMMENT '用户是否因为浏览该文章而关注了作者', 
  -- 文章特征
  `blog_id` string COMMENT '作者 id', 
  `tags` array<string> COMMENT '文章的标签，array', 
  `article_type` int COMMENT '文章类型：1-文本2-图片3-音乐4-视频5-问答6-长文章', 
  `create_time_days` int COMMENT '文章创建天数', 
  `words_count` bigint COMMENT '文章字数，图片文章可能是0', 
  `photo_num` int COMMENT '文章图片数量，文字文章可能是0', 
  `hot` bigint COMMENT '文章热度', 
  `valid_comment_cnt` bigint COMMENT '文章的有效评论数', 
  -- 用户特征
  `active_days_1d` bigint COMMENT '用户前一天是否活跃', 
  `active_days_3d` bigint COMMENT '用户前3天活跃天数', 
  `active_days_7d` bigint COMMENT '用户前第7天活跃天数', 
  `active_days_15d` bigint COMMENT '用户前15天活跃天数', 
  `active_days_30d` bigint COMMENT '用户前30天活跃天数', 
  `send_hot_30d` bigint COMMENT '用户前30天点赞次数', 
  `send_comment_cnt_30d` bigint COMMENT '用户前30天评论次数', 
  `send_collect_cnt_30d` bigint COMMENT '用户前30天收藏次数', 
  -- 用户留存标记
  `retain_1d` int COMMENT 'label：用户是否次留', 
  `retain_3d` int COMMENT 'label：用户是否三留', 
  `retain_active_days_7d` bigint  COMMENT 'label，用户是否7留'
  );
```

# 目标和约束
请设计一个因果推断模型，计算每个创作者对社区的贡献度。有几个重要注意点：
1. 需要考虑不同用户的权重，社区核心用户（喜欢互动/评论/发文）的用户，权重更高
2. 宽表的数据量很大，接近1亿条数据。请确定是否本地处理？
3. 输出每篇文章的贡献度、每个创作者的贡献度