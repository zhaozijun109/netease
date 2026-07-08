-- =====================================================
-- 视频域数仓建表 DDL · V4 (上线规范修复版)
-- 设计文档：https://docs.popo.netease.com/team/pc/3n4l0cii/pageDetail/5bdd9aebbc6e48159e8164d9d26c68e0
-- 字段权威来源：
--   播放器指标 v2 (doc 6c9acd00fa2b48399051c246735e53ff)
--   发布和草稿埋点方案 (doc 099fda4cb2a84023b9861154657d4d73)
-- 数据库分布：
--   lofter      — DIM / DWD / DWS 公共与业务域
--   lofter_dm   — ADS 应用层
-- 表后缀：_dd 日全量快照, _di 日增量
-- 存储格式：Parquet (Hive ParquetSerDe)
-- 规范要求：
--   1. 不指定 LOCATION（默认 Hive 路径映射，与现网 100% 一致）
--   2. 表 / 字段 / 分区字段三层 COMMENT 全覆盖
--   3. 仅输出分子分母因子，不直接输出 _rate / _ratio / _ctr / _penetration
--      率类指标由下游 BI / API 自行组合（见 etl/docs/video/spec.md 第 7 章）
-- =====================================================

-- =================================
-- DIM 层 · lofter.dim_video_dd
-- =================================
DROP TABLE IF EXISTS lofter.dim_video_dd;
CREATE EXTERNAL TABLE lofter.dim_video_dd (
    postId               BIGINT       COMMENT '文章 ID',
    userId               BIGINT       COMMENT '发布者 userId',
    blogId               BIGINT       COMMENT '创作者 blogId',
    blog_name            STRING       COMMENT '博客标识',
    blog_nickname        STRING       COMMENT '博客昵称',
    post_title           STRING       COMMENT '标题',
    publish_date         STRING       COMMENT '发布日期 yyyy-MM-dd',
    publish_time         BIGINT       COMMENT '发布时间戳(ms)',
    post_tags            ARRAY<STRING>     COMMENT '标签数组',
    post_ips             ARRAY<STRING>     COMMENT 'IP 圈层数组',
    post_domains         ARRAY<BIGINT>     COMMENT '一级领域数组',
    is_imported          INT          COMMENT '是否导入: 0=否 / 1=是',
    import_platform_type STRING       COMMENT '导入平台 (抖音/快手/B站等)',
    allow_view           INT          COMMENT '可见范围: 0=公开 / 50=审核中 / 100=仅自己可见',
    valid                INT          COMMENT '审核状态: 0=正常 / 25=屏蔽',
    is_published         BOOLEAN      COMMENT '是否已发布: true=已发布 / false=未发布',
    is_forbidden         BOOLEAN      COMMENT '是否被屏蔽: true=被屏蔽 / false=未屏蔽',
    post_url             STRING       COMMENT '文章 URL',
    movefrom             STRING       COMMENT '规整后的客户端: ios / android / web',
    caption              STRING       COMMENT '富文本内容 (HTML)',
    video_type           INT          COMMENT '视频类型: 3=站内原生 / 非3=站外/导入',
    origin_url           STRING       COMMENT '原始视频 URL',
    hls_url              STRING       COMMENT 'HLS 流 URL (m3u8)',
    h265_url             STRING       COMMENT 'H265 编码 URL',
    flash_url            STRING       COMMENT 'Flash URL',
    video_down_url       STRING       COMMENT '下载 URL',
    video_first_img      STRING       COMMENT '首帧封面图',
    video_img_url        STRING       COMMENT '用户自定义封面 URL',
    vid                  BIGINT       COMMENT '视频中台 vid (VOD 资源 ID)',
    duration_sec         BIGINT       COMMENT '视频时长 (秒) - 注意单位与 dwd_video_play_di 的毫秒不同',
    size_bytes           BIGINT       COMMENT '文件大小 (字节)',
    img_width            INT          COMMENT '画幅宽 (像素)',
    img_height           INT          COMMENT '画幅高 (像素)',
    aspect_ratio         STRING       COMMENT '宽高比: landscape / portrait / square / unknown',
    embed_type           STRING       COMMENT 'embed 类型, 如 uservideo'
)
COMMENT '视频域唯一新建 DIM 表 · dim_post(视频帖) LEFT JOIN ods_db_video_post_nd + embed JSON 解析'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';

-- 兼容视图: 仅用于 Ad-hoc / SQL 客户端临时查询
-- 下游 ETL / 报表 / API 必须直接使用 dim_video_dd 并限制 dt 分区, 禁止依赖此视图
DROP VIEW IF EXISTS lofter.dim_video;
CREATE VIEW lofter.dim_video
COMMENT '视频维度兼容视图 · 自动指向 dim_video_dd 最新分区 · 仅供 Ad-hoc 查询使用, 下游消费请直接使用 dim_video_dd 并限制 dt 分区'
AS SELECT * FROM lofter.dim_video_dd WHERE dt = (SELECT MAX(dt) FROM lofter.dim_video_dd);


-- =================================
-- DWD 层 · 视频域 (lofter)
-- =================================

-- dwd_video_play_di — 播放会话明细 (按播放会话关联键聚合播放心跳/用户操作/播放器状态/错误/清晰度切换 5 类事件)
DROP TABLE IF EXISTS lofter.dwd_video_play_di;
CREATE EXTERNAL TABLE lofter.dwd_video_play_di (
    reqId                       STRING  COMMENT '播放会话关联键',
    postId                      BIGINT  COMMENT '视频 ID',
    userId                      BIGINT  COMMENT '消费用户 ID',
    deviceUdid                  STRING  COMMENT '设备唯一标识',
    deviceOs                    STRING  COMMENT 'iOS / Android',
    deviceModel                 STRING  COMMENT '设备型号',
    appVersion                  STRING  COMMENT 'App 版本',
    appChannel                  STRING  COMMENT 'App 渠道',
    dominant_quality            STRING  COMMENT '主档位, 格式 "宽x高" 如 720x1280',
    end_currentrate             STRING  COMMENT '本段视频结束的码率',
    player_type                 STRING  COMMENT 'exoplayer / neliveplayer / AVPlayer',
    scene                       STRING  COMMENT '播放场景',
    alg_info                    STRING  COMMENT '算法信息',
    rec_id                      STRING  COMMENT '推荐 ID',
    is_advertise                STRING  COMMENT '是否广告: 0=否 / 1=是',
    item_position               INT     COMMENT '列表位置',
    video_url                   STRING  COMMENT '视频链接',
    played_time_ms              BIGINT  COMMENT '实际播放时长 (毫秒)',
    buffer_time_ms              BIGINT  COMMENT '累计缓冲时长 (毫秒)',
    duration_ms                 BIGINT  COMMENT '视频总时长 (毫秒)',
    max_progress                DOUBLE  COMMENT '最大播放进度 0~1',
    play_rate                   DOUBLE  COMMENT '本次播放倍速档位 (1.0/1.25/1.5/1.75/2.0/0.5/0.75)',
    is_real                     INT     COMMENT '是否有效播放 (5 秒口径, played_time_ms > 5000): 0=否 / 1=是 · 与 dwd_post_browse_di.is_real 视频帖一致, 是视频域历史口径; 若需 3 秒口径 (effective_play_rate) 下游可基于 played_time_ms > 3000 自算',
    is_finished                 INT     COMMENT '是否完播 (max_progress>=0.9 OR played_time_ms/duration_ms>=0.9): 0=否 / 1=是',
    buffer_session_count        INT     COMMENT '本会话发生卡顿次数',
    userseek_session_count      INT     COMMENT '本会话用户拖动进度条次数',
    system_interrupt_count      INT     COMMENT '本会话系统打断次数 (失去音频焦点等)',
    inflate_error_count         INT     COMMENT '本会话渲染错误次数',
    timer_interrupt_count       INT     COMMENT '本会话定时器打断次数',
    start_event_time            BIGINT  COMMENT '起播事件时间戳 (ms)',
    prepare_event_time          BIGINT  COMMENT '播放器准备完成时间戳 (ms)',
    first_frame_event_time      BIGINT  COMMENT '首帧渲染完成时间戳 (ms)',
    ttfr_ms                     BIGINT  COMMENT 'TTFR 播放器准备时间 (ms) = prepare_event_time - start_event_time',
    ttfp_ms                     BIGINT  COMMENT 'TTFP 首帧渲染时间 (ms) = first_frame_event_time - start_event_time',
    is_play_started             INT     COMMENT '是否起播成功 (有首帧渲染事件): 0=否 / 1=是',
    is_play_failed              INT     COMMENT '是否播放失败 (有错误事件): 0=否 / 1=是',
    error_type                  STRING  COMMENT '错误分类: network / decode / source / unknown',
    error_code                  INT     COMMENT '播放器错误码',
    error_retry_count           INT     COMMENT '已重试次数',
    play_heartbeat_count        BIGINT  COMMENT '播放心跳次数 (播放停止/切换时上报)',
    user_action_count           BIGINT  COMMENT '用户播放操作次数 (start/resume/pause/stop/seek 总和)',
    player_state_change_count   BIGINT  COMMENT '播放器状态变更次数 (prepare/首帧/首次 buffer)',
    player_error_count          BIGINT  COMMENT '播放器错误次数',
    quality_event_count         BIGINT  COMMENT '清晰度事件总次数 (含首次确定档位)',
    quality_switch_count        BIGINT  COMMENT '清晰度真正切换次数 (不含首次确定)',
    pause_count                 BIGINT  COMMENT '用户暂停次数',
    resume_count                BIGINT  COMMENT '用户恢复播放次数',
    seek_count                  BIGINT  COMMENT '用户拖动进度条次数',
    is_speedrate_used           INT     COMMENT '本次会话是否使用过倍速 (play_rate > 1.0): 0=否 / 1=是'
)
COMMENT '视频播放会话明细 · 按 reqId 聚合一次完整播放的全量事件'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- dwd_video_quality_event_di — 清晰度切换事件明细
DROP TABLE IF EXISTS lofter.dwd_video_quality_event_di;
CREATE EXTERNAL TABLE lofter.dwd_video_quality_event_di (
    reqId                       STRING  COMMENT '会话关联键',
    postId                      BIGINT  COMMENT '视频 ID',
    userId                      BIGINT  COMMENT '消费用户',
    deviceUdid                  STRING  COMMENT '设备 ID',
    deviceOs                    STRING  COMMENT 'iOS / Android',
    deviceModel                 STRING  COMMENT '设备型号',
    appVersion                  STRING  COMMENT 'App 版本',
    event_time                  BIGINT  COMMENT '切换发生时间戳 (ms)',
    scene                       STRING  COMMENT '播放场景',
    switch_type                 STRING  COMMENT '切换类型: init / manual / auto',
    from_quality                STRING  COMMENT '切换前档位 "宽x高", 首次确定时为空',
    to_quality                  STRING  COMMENT '切换后档位 "宽x高"',
    direction                   STRING  COMMENT '方向 (按高度比较): up / down / same / none',
    current_position_ms         BIGINT  COMMENT '切换时播放进度 (ms)',
    duration_in_from_quality_ms BIGINT  COMMENT '在前一档位停留时长 (ms)',
    estimated_bandwidth_kbps    BIGINT  COMMENT '估计下行带宽 (Kbps)',
    buffer_duration_ms          BIGINT  COMMENT '切换时 buffer 余量 (ms)'
)
COMMENT '视频清晰度切换事件明细 · 一行 = 一次切换'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- dwd_video_publish_di — 视频发布明细
DROP TABLE IF EXISTS lofter.dwd_video_publish_di;
CREATE EXTERNAL TABLE lofter.dwd_video_publish_di (
    postId               BIGINT             COMMENT '视频 ID',
    userId               BIGINT             COMMENT '发布者 ID',
    blogId               BIGINT             COMMENT '创作者 ID',
    publish_date         STRING             COMMENT '发布日期 yyyy-MM-dd',
    publish_time         BIGINT             COMMENT '发布时间戳 (ms)',
    video_type           INT                COMMENT '3=站内原生 / 非3=站外',
    duration_sec         BIGINT             COMMENT '视频时长 (秒)',
    size_bytes           BIGINT             COMMENT '文件大小 (字节)',
    vid                  BIGINT             COMMENT '视频中台 vid',
    img_width            INT                COMMENT '画幅宽',
    img_height           INT                COMMENT '画幅高',
    aspect_ratio         STRING             COMMENT 'landscape / portrait / square',
    origin_url           STRING             COMMENT '原始视频 URL',
    hls_url              STRING             COMMENT 'HLS 流 URL',
    h265_url             STRING             COMMENT 'H265 编码 URL',
    video_first_img      STRING             COMMENT '首帧封面图',
    video_img_url        STRING             COMMENT '自定义封面 URL',
    is_imported          INT                COMMENT '是否导入 (从抖音/快手等外部平台搬运): 0=否 / 1=是',
    import_platform_type STRING             COMMENT '导入平台',
    tags                 ARRAY<STRING>      COMMENT '标签数组',
    ips                  ARRAY<STRING>      COMMENT 'IP 圈层数组',
    domains              ARRAY<BIGINT>      COMMENT '一级领域数组',
    title                STRING             COMMENT '标题',
    caption              STRING             COMMENT '富文本内容',
    client_type          STRING             COMMENT '发布端类型 (iOS/Android/Web)',
    is_user_first_post   INT                COMMENT '是否当日用户首发: 0=否 / 1=是',
    is_pay_gift          INT                COMMENT '是否礼物文章 (付费内容): 0=否 / 1=是',
    movefrom             STRING             COMMENT '规整后客户端: ios/android/web'
)
COMMENT '视频发布明细 · dwd_post_publish_di (视频帖) JOIN dim_video_dd'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- dwd_video_publish_funnel_di — 视频上传发布漏斗
DROP TABLE IF EXISTS lofter.dwd_video_publish_funnel_di;
CREATE EXTERNAL TABLE lofter.dwd_video_publish_funnel_di (
    req_id                STRING  COMMENT '上传任务关联键',
    deviceUdid            STRING  COMMENT '设备 ID',
    userId                BIGINT  COMMENT '用户 ID',
    deviceOs              STRING  COMMENT 'iOS / Android',
    deviceModel           STRING  COMMENT '设备型号',
    appVersion            STRING  COMMENT 'App 版本',
    itemId                BIGINT  COMMENT '文章 ID (新建为空/0, 草稿/编辑有值)',
    post_type             STRING  COMMENT '发布类型: postNew / postEdit / postReblog',
    scene                 STRING  COMMENT '场景 (固定 release_page)',
    start_time            BIGINT  COMMENT '点击发布按钮时间戳 (ms)',
    upload_start_time     BIGINT  COMMENT '开始上传素材时间戳 (ms)',
    upload_progress_time  BIGINT  COMMENT '素材上传成功时间戳 (ms)',
    success_time          BIGINT  COMMENT '发布成功时间戳 (ms)',
    is_success            INT     COMMENT '是否最终发布成功: 0=否 / 1=是',
    has_start             INT     COMMENT '是否触发点击发布: 0=否 / 1=是',
    has_upload_start      INT     COMMENT '是否触发开始上传: 0=否 / 1=是',
    has_progress          INT     COMMENT '是否触发素材上传成功: 0=否 / 1=是',
    has_intercepted       INT     COMMENT '是否被参数缺失拦截: 0=否 / 1=是',
    has_material_fail     INT     COMMENT '是否素材上传失败: 0=否 / 1=是',
    has_api_fail          INT     COMMENT '是否接口请求失败: 0=否 / 1=是',
    fail_stage            STRING  COMMENT '失败阶段: Success/ApiFail/MaterialFail/ParamIntercepted/StuckAtFinalCommit/StuckAtUploading/StuckAtInit/Unknown',
    fail_msg              STRING  COMMENT '失败提示文本',
    fail_ext_code         STRING  COMMENT '失败错误码 (来自 ext.errorCode)',
    fail_top_code         STRING  COMMENT '顶层 code 字段 (仅 c10-30 有, 与 ext.errorCode 通常一致)',
    fail_category         STRING  COMMENT '失败分类: UserInteraction_or_Network / ServerError / ClientError',
    total_duration_ms     BIGINT  COMMENT '总耗时 (ms) = success_time - start_time'
)
COMMENT '视频上传发布漏斗明细 · 按 req_id 聚合从点击发布到接口成功的全流程'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- dwd_video_cover_edit_di — 视频封面编辑事件明细
-- 注意: 当前埋点 c1-31/32/33 在 mda 中 itemId 全为 NULL, 无法关联到具体 postId, 因此本表仅承载设备/用户维度的事件流, 不含视频维度
DROP TABLE IF EXISTS lofter.dwd_video_cover_edit_di;
CREATE EXTERNAL TABLE lofter.dwd_video_cover_edit_di (
    userId         BIGINT              COMMENT '用户 ID',
    deviceUdid     STRING              COMMENT '设备 ID',
    deviceOs       STRING              COMMENT 'iOS / Android',
    appVersion     STRING              COMMENT 'App 版本',
    eventId        STRING              COMMENT '埋点事件 ID (c1-31 选帧界面曝光 / c1-32 选帧操作 / c1-33 保存)',
    occurTime      BIGINT              COMMENT '事件发生时间戳 (ms)',
    cover_width    INT                 COMMENT '封面宽度 (来自 ext.width, 仅 c1-31 有)',
    cover_height   INT                 COMMENT '封面高度 (来自 ext.height, 仅 c1-31 有)',
    opt_type       STRING              COMMENT 'c1-32 操作类型 (顶层 optType, 取值 "0"/"2", 含义待客户端确认)'
)
COMMENT '视频封面编辑事件明细 · 无 postId 关联 (mda c1-* 埋点 itemId 为空), 仅设备/用户粒度'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- dwd_video_icloud_download_di — iCloud 下载事件
DROP TABLE IF EXISTS lofter.dwd_video_icloud_download_di;
CREATE EXTERNAL TABLE lofter.dwd_video_icloud_download_di (
    userId             BIGINT   COMMENT '用户 ID',
    deviceUdid         STRING   COMMENT '设备 ID',
    appVersion         STRING   COMMENT 'App 版本',
    event_time         BIGINT   COMMENT '事件时间戳 (ms)',
    ext_type           INT      COMMENT '失败类型: 1=主动取消失败 / 0=下载失败',
    result_status      STRING   COMMENT 'cancel / fail / null (成功或其他)'
)
COMMENT '视频源 iCloud 下载事件明细 · 仅 iOS · 当前埋点仅上报失败/取消, 成功事件标识待客户端补充'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- =================================
-- DWS 层 · 视频域 (lofter)
-- 注意：本层仅输出因子，不输出 _rate / _ratio / _ctr 等比率字段
--      率指标由下游 BI / API 自行求商
-- =================================

DROP TABLE IF EXISTS lofter.dws_video_post_dd;
CREATE EXTERNAL TABLE lofter.dws_video_post_dd (
    postId                      BIGINT             COMMENT '视频 ID',
    userId                      BIGINT             COMMENT '发布者',
    blogId                      BIGINT             COMMENT '创作者',
    publish_date                STRING             COMMENT '发布日期',
    video_type                  INT                COMMENT '3=站内原生 / 非3=站外',
    duration_sec                BIGINT             COMMENT '视频时长(秒)',
    size_bytes                  BIGINT             COMMENT '文件大小(字节)',
    aspect_ratio                STRING             COMMENT '宽高比',
    is_imported                 INT                COMMENT '是否导入 (从抖音/快手等外部平台搬运): 0=否 / 1=是',
    import_platform_type        STRING             COMMENT '导入平台',
    movefrom                    STRING             COMMENT 'ios/android/web',
    post_tags                   ARRAY<STRING>      COMMENT '标签数组',
    post_ips                    ARRAY<STRING>      COMMENT 'IP 数组',
    post_domains                ARRAY<BIGINT>      COMMENT '领域数组',
    expose_pv                   BIGINT             COMMENT '曝光 PV (dwd_post_browse_di action_type=page_view)',
    expose_uv                   BIGINT             COMMENT '曝光 UV',
    play_pv                     BIGINT             COMMENT '播放 PV (action_type=page_duration)',
    play_uv                     BIGINT             COMMENT '播放 UV',
    real_play_pv                BIGINT             COMMENT '有效播放 PV (is_real=1)',
    real_play_uv                BIGINT             COMMENT '有效播放 UV',
    finish_play_pv              BIGINT             COMMENT '完播 PV (is_video_finish=1)',
    finish_play_uv              BIGINT             COMMENT '完播 UV',
    real_play_time_ms           BIGINT             COMMENT '有效播放总时长 (ms)',
    play_time_ms                BIGINT             COMMENT '播放总时长 (ms)',
    play_session_count          BIGINT             COMMENT '播放会话数 (dwd_video_play_di)',
    play_session_uv             BIGINT             COMMENT '播放会话 UV',
    play_session_real_count     BIGINT             COMMENT '5s 有效会话数',
    play_session_finish_count   BIGINT             COMMENT '完播会话数',
    play_session_started_count  BIGINT             COMMENT '起播成功会话数 (有首帧渲染事件)',
    buffer_session_count        BIGINT             COMMENT '卡顿会话数总和',
    buffer_time_ms              BIGINT             COMMENT '卡顿时长总和 (ms)',
    session_played_time_ms      BIGINT             COMMENT '会话级累计播放时长 (来自播放心跳, 单位 ms)',
    ttfp_ms_sum                 BIGINT             COMMENT 'TTFP 时长求和 (ms, 用于求均值)',
    ttfp_ms_count               BIGINT             COMMENT 'TTFP 有效记录数',
    ttfr_ms_sum                 BIGINT             COMMENT 'TTFR 时长求和 (ms)',
    ttfr_ms_count               BIGINT             COMMENT 'TTFR 有效记录数',
    play_fail_count             BIGINT             COMMENT '播放失败会话数',
    speedrate_session_count     BIGINT             COMMENT '使用倍速的会话数',
    speedrate_uv                BIGINT             COMMENT '使用倍速的 UV',
    praise_pv                   BIGINT             COMMENT '点赞次数 (dwd_post_hot_di praise)',
    reproduce_pv                BIGINT             COMMENT '转载次数',
    recommend_pv                BIGINT             COMMENT '推荐次数',
    subscribe_pv                BIGINT             COMMENT '订阅次数',
    hot_pv                      BIGINT             COMMENT 'hot 总次数 (praise+reproduce+recommend+subscribe)',
    response_pv                 BIGINT             COMMENT '评论次数 (dwd_post_response_di)',
    share_pv                    BIGINT             COMMENT '分享次数 (dwd_post_share_di)'
)
COMMENT '视频粒度日汇总 · 仅输出因子 (下游求商得率)'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter.dws_video_quality_dd;
CREATE EXTERNAL TABLE lofter.dws_video_quality_dd (
    deviceOs                    STRING   COMMENT 'iOS / Android',
    device_tier                 STRING   COMMENT 'high / mid / low / unknown',
    appVersion                  STRING   COMMENT 'App 版本',
    dominant_quality            STRING   COMMENT '主档位 "宽x高"',
    play_session_count          BIGINT   COMMENT '播放会话数',
    play_session_uv             BIGINT   COMMENT '播放会话 UV',
    buffer_session_count        BIGINT   COMMENT '卡顿会话数 (buffer>0 的会话数)',
    buffer_session_uv           BIGINT   COMMENT '卡顿会话 UV',
    buffer_time_ms              BIGINT   COMMENT '卡顿时长求和 (ms)',
    played_time_ms              BIGINT   COMMENT '播放时长求和 (ms)',
    ttfp_ms_sum                 BIGINT   COMMENT 'TTFP 求和 (用于均值)',
    ttfp_ms_count               BIGINT   COMMENT 'TTFP 有效记录数',
    ttfp_ms_p50_input_count     BIGINT   COMMENT '用于分位数计算的样本数',
    ttfr_ms_sum                 BIGINT   COMMENT 'TTFR 求和',
    ttfr_ms_count               BIGINT   COMMENT 'TTFR 有效记录数',
    play_started_count          BIGINT   COMMENT '起播成功会话数 (有首帧渲染事件)',
    play_failed_count           BIGINT   COMMENT '播放失败会话数 (有错误事件)',
    play_heartbeat_count_sum    BIGINT   COMMENT '播放心跳次数求和',
    play_attempt_count          BIGINT   COMMENT '起播尝试次数 (用户点击播放)',
    quality_switch_count_sum    BIGINT   COMMENT '清晰度切换次数求和 (不含首次确定)',
    speedrate_uv                BIGINT   COMMENT '使用倍速的 UV (play_rate>1.0)'
)
COMMENT '视频质量大盘因子 · 按 deviceOs × device_tier × appVersion × dominant_quality 切片'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter.dws_video_user_consume_di;
CREATE EXTERNAL TABLE lofter.dws_video_user_consume_di (
    userId                BIGINT   COMMENT '用户 ID',
    deviceUdid            STRING   COMMENT '设备 ID',
    deviceOs              STRING   COMMENT 'iOS / Android',
    appVersion            STRING   COMMENT 'App 版本',
    expose_pv             BIGINT   COMMENT '视频曝光 PV',
    play_pv               BIGINT   COMMENT '视频播放 PV',
    play_post_uv          BIGINT   COMMENT '播放过的不同视频数',
    real_play_pv          BIGINT   COMMENT '有效播放 PV',
    finish_play_pv        BIGINT   COMMENT '完播 PV',
    real_play_time_ms     BIGINT   COMMENT '有效播放总时长 (ms)',
    play_session_count    BIGINT   COMMENT '播放会话数',
    play_post_distinct    BIGINT   COMMENT '会话粒度不同视频数',
    buffer_session_count  BIGINT   COMMENT '卡顿会话数',
    buffer_time_ms        BIGINT   COMMENT '卡顿时长 (ms)',
    played_time_ms        BIGINT   COMMENT '播放时长 (ms)',
    is_speedrate_used     INT      COMMENT '当日是否用过倍速: 0=否 / 1=是',
    hot_pv                BIGINT   COMMENT '互动次数 (全内容类型)'
)
COMMENT '用户视频消费日汇总 · 仅因子'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter.dws_video_creator_dd;
CREATE EXTERNAL TABLE lofter.dws_video_creator_dd (
    blogId                       BIGINT   COMMENT '创作者 ID',
    userId                       BIGINT   COMMENT '用户 ID',
    total_video_count            BIGINT   COMMENT '历史累计视频数',
    publish_count_1d             BIGINT   COMMENT '当日发布数',
    expose_pv_1d                 BIGINT   COMMENT '当日曝光 PV',
    play_pv_1d                   BIGINT   COMMENT '当日播放 PV',
    real_play_pv_1d              BIGINT   COMMENT '当日有效播放 PV',
    finish_play_pv_1d            BIGINT   COMMENT '当日完播 PV',
    hot_pv_1d                    BIGINT   COMMENT '当日互动 PV',
    share_pv_1d                  BIGINT   COMMENT '当日分享 PV',
    response_pv_1d               BIGINT   COMMENT '当日评论 PV',
    play_session_count_1d        BIGINT   COMMENT '当日播放会话数',
    buffer_session_count_1d      BIGINT   COMMENT '当日卡顿会话数'
)
COMMENT '创作者视频画像 · 仅因子 · 当日发布数 + 视频聚合指标 (因封面编辑事件无 postId 关联, 不含封面编辑相关因子)'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter.dws_video_speedrate_dd;
CREATE EXTERNAL TABLE lofter.dws_video_speedrate_dd (
    deviceOs                    STRING   COMMENT 'iOS / Android',
    speedrate_tier              STRING   COMMENT '1.0x / 1.25x / 1.5x / 1.75x / 2.0x / 0.5x / 0.75x ...',
    overall_play_uv             BIGINT   COMMENT '该 deviceOs 大盘有效播放 UV (各 tier 相同, 透传)',
    overall_speedrate_uv        BIGINT   COMMENT '该 deviceOs 大盘倍速 UV (各 tier 相同, 透传)',
    tier_session_count          BIGINT   COMMENT '本 tier 会话数',
    tier_session_uv             BIGINT   COMMENT '本 tier 会话 UV',
    tier_finish_count           BIGINT   COMMENT '本 tier 完播会话数',
    tier_buffer_session_count   BIGINT   COMMENT '本 tier 卡顿会话数',
    tier_buffer_time_ms         BIGINT   COMMENT '本 tier 卡顿时长 (ms)',
    tier_played_time_ms         BIGINT   COMMENT '本 tier 播放时长 (ms)'
)
COMMENT '倍速功能专项因子 · 按 deviceOs × speedrate_tier 切片'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


-- =================================
-- ADS 层 · 视频域 (lofter_dm)
-- 注意：同 DWS 层规则, 仅输出因子, 由 BI 自行求商
-- =================================

DROP TABLE IF EXISTS lofter_dm.ads_growth_video_quality_monitor_di;
CREATE EXTERNAL TABLE lofter_dm.ads_growth_video_quality_monitor_di (
    deviceOs                    STRING   COMMENT 'iOS / Android',
    device_tier                 STRING   COMMENT 'high / mid / low / unknown',
    appVersion                  STRING   COMMENT 'App 版本',
    dominant_quality            STRING   COMMENT '主档位',
    play_session_count          BIGINT   COMMENT '播放会话数',
    play_session_uv             BIGINT   COMMENT '播放会话 UV',
    buffer_session_count        BIGINT   COMMENT '卡顿会话数',
    buffer_session_uv           BIGINT   COMMENT '卡顿会话 UV',
    buffer_time_ms              BIGINT   COMMENT '卡顿时长 (ms)',
    played_time_ms              BIGINT   COMMENT '播放时长 (ms)',
    ttfp_ms_sum                 BIGINT   COMMENT 'TTFP 求和',
    ttfp_ms_count               BIGINT   COMMENT 'TTFP 有效数',
    ttfp_ms_p50_input_count     BIGINT   COMMENT '用于分位数计算的样本数',
    ttfr_ms_sum                 BIGINT   COMMENT 'TTFR 求和',
    ttfr_ms_count               BIGINT   COMMENT 'TTFR 有效数',
    play_started_count          BIGINT   COMMENT '起播成功数',
    play_failed_count           BIGINT   COMMENT '失败数',
    play_heartbeat_count_sum    BIGINT   COMMENT '播放心跳次数求和',
    play_attempt_count          BIGINT   COMMENT '起播尝试次数',
    quality_switch_count_sum    BIGINT   COMMENT '清晰度切换次数求和',
    speedrate_uv                BIGINT   COMMENT '倍速 UV'
)
COMMENT '视频质量监控大盘 (P0) · 因子透传'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter_dm.ads_growth_video_publish_funnel_di;
CREATE EXTERNAL TABLE lofter_dm.ads_growth_video_publish_funnel_di (
    deviceOs                STRING   COMMENT 'iOS / Android',
    appVersion              STRING   COMMENT 'App 版本',
    post_type               STRING   COMMENT 'postNew / postEdit / postReblog / unknown',
    task_count              BIGINT   COMMENT '任务数',
    task_uv                 BIGINT   COMMENT '任务 UV',
    step1_init_count        BIGINT   COMMENT '点击发布按钮事件数',
    step2_upload_start_count BIGINT  COMMENT '开始上传素材事件数',
    step3_progress_count    BIGINT   COMMENT '素材上传成功事件数',
    step4_success_count     BIGINT   COMMENT '发布成功事件数',
    fail_intercepted_count  BIGINT   COMMENT '参数缺失拦截命中数',
    fail_material_count     BIGINT   COMMENT '素材上传失败命中数',
    fail_api_count          BIGINT   COMMENT '接口请求失败命中数',
    stuck_at_init           BIGINT   COMMENT '停在 Init 阶段',
    stuck_at_uploading      BIGINT   COMMENT '停在 Uploading',
    stuck_at_final_commit   BIGINT   COMMENT '停在 FinalCommit',
    failed_at_intercept     BIGINT   COMMENT 'fail_stage=ParamIntercepted',
    failed_at_material      BIGINT   COMMENT 'fail_stage=MaterialFail',
    failed_at_api           BIGINT   COMMENT 'fail_stage=ApiFail',
    fail_server_error       BIGINT   COMMENT 'fail_category=ServerError',
    fail_client_error       BIGINT   COMMENT 'fail_category=ClientError',
    fail_user_or_network    BIGINT   COMMENT 'fail_category=UserInteraction_or_Network'
)
COMMENT '视频发布漏斗大盘 · 按 deviceOs × appVersion × post_type 切片'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter_dm.ads_growth_video_cover_edit_stat_di;
CREATE EXTERNAL TABLE lofter_dm.ads_growth_video_cover_edit_stat_di (
    deviceOs               STRING   COMMENT 'iOS / Android',
    appVersion             STRING   COMMENT 'App 版本',
    eventId                STRING   COMMENT 'c1-31 选帧界面曝光 / c1-32 选帧操作 / c1-33 保存',
    event_count            BIGINT   COMMENT '事件总数',
    user_uv                BIGINT   COMMENT '触发事件的用户 UV (按 deviceUdid 去重)',
    event_with_size_count  BIGINT   COMMENT '含封面尺寸信息的事件数 (主要是 c1-31)',
    avg_cover_width        DOUBLE   COMMENT '平均封面宽度 (像素)',
    avg_cover_height       DOUBLE   COMMENT '平均封面高度 (像素)',
    c1_32_opt_0_count      BIGINT   COMMENT 'c1-32 中 optType=0 的事件数',
    c1_32_opt_2_count      BIGINT   COMMENT 'c1-32 中 optType=2 的事件数'
)
COMMENT '视频封面编辑大盘统计 · 设备维度因子 · 因 mda c1-* 埋点无 postId, 不能做单视频粒度 CTR 对比'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';


DROP TABLE IF EXISTS lofter_dm.ads_growth_video_icloud_download_funnel_di;
CREATE EXTERNAL TABLE lofter_dm.ads_growth_video_icloud_download_funnel_di (
    appVersion                STRING   COMMENT 'App 版本',
    total_event_count         BIGINT   COMMENT 'iCloud 下载事件总数',
    user_uv                   BIGINT   COMMENT '上报 UV',
    cancel_count              BIGINT   COMMENT 'ext.type=1 主动取消数',
    fail_count                BIGINT   COMMENT 'ext.type=0 失败数',
    success_or_other_count    BIGINT   COMMENT 'result_status 为 null 的事件数 (待 SDK 确认成功标识)'
)
COMMENT 'iCloud 下载漏斗 (iOS) · 因子'
PARTITIONED BY (
  `dt` STRING COMMENT '分区日期 yyyy-MM-dd')
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';
