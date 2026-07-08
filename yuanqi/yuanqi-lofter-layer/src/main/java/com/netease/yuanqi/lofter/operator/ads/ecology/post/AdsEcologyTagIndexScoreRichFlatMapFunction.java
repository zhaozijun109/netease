package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.config.AccompanyTagConfig;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.TagPostUserHotEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 离线 SQL ads_ecology_tag_index_score_di 实时化的第一步：解析 binlog → 过滤 + 组装单事件.
 *
 * <ol>
 *   <li>消费 binlog（仅 PostHot / PostResponse 两张表）。每条 binlog 仅产生一条事件（一个用户一次仅 1 种操作）。
 *   <li>open() 中初始化 LOFTER_BACKEND JDBC 数据源 + 两份 Caffeine 缓存：
 *       <ul>
 *         <li>{@code postCache}: postId → (blogId, publisherUserId, tags)
 *         <li>{@code shipNumCache}: "tag_userId" → shipNum
 *       </ul>
 *       白名单由 {@link AccompanyTagConfig#ACCOMPANY_TAG_SET} 提供。
 *   <li>每条 binlog → 设置 6 个 cnt 中"对应那一个"为 1，其余为 0 → 通过 postCache 补 tags/blogId → 对 tags explode →
 *       命中白名单才下发 → 通过 shipNumCache 查 shipNum → 输出单事件 {@link TagPostUserHotEvent}.
 * </ol>
 *
 * <p>本算子只负责"过滤 + 单事件组装"，不计算 newPostHot/oldPostHot/newUserScore，全部聚合 + 公式计算交给下游
 * ProcessWindowFunction.
 */
public class AdsEcologyTagIndexScoreRichFlatMapFunction
        extends RichFlatMapFunction<String, TagPostUserHotEvent> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsEcologyTagIndexScoreRichFlatMapFunction.class);

    /** Post 维表查询 SQL：按 postId 单点查 (blogId, tag). */
    private static final String POST_LOOKUP_SQL = "SELECT `BlogID`, `Tag` FROM Post WHERE `ID` = ?";

    /** Tag_UserShipInfo 维表查询 SQL：按 (tag, userId) 单点查 shipNum. */
    private static final String SHIP_NUM_LOOKUP_SQL =
            "SELECT `shipNum` FROM Tag_UserShipInfo WHERE `tag` = ? AND `userId` = ?";

    /** Post 缓存 sentinel: DB 中找不到的 postId. */
    private static final PostMeta POST_NOT_FOUND = new PostMeta(null, Collections.emptyList());

    /** shipNum 缓存 sentinel: DB 中找不到的 (tag, userId). */
    private static final Long SHIP_NUM_NOT_FOUND = -1L;

    private transient ObjectMapper objectMapper;
    private transient DruidDataSource dataSource;
    private transient Cache<Long, PostMeta> postCache;
    private transient Cache<String, Long> shipNumCache;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(
                ClusterConfigOptions.getMysqlDriverClassName(
                        ClusterConfigOptions.MysqlConnectionEnum.LOFTER_BACKEND));
        dataSource.setUrl(
                ClusterConfigOptions.getMysqlConnection(
                        ClusterConfigOptions.MysqlConnectionEnum.LOFTER_BACKEND));
        dataSource.setLoginTimeout(5000);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setFailFast(true);

        postCache =
                Caffeine.newBuilder()
                        .initialCapacity(10240)
                        .maximumSize(2000000)
                        .expireAfterWrite(Duration.ofHours(12))
                        .build();

        shipNumCache =
                Caffeine.newBuilder()
                        .initialCapacity(10240)
                        .maximumSize(2000000)
                        .expireAfterWrite(Duration.ofHours(12))
                        .build();
    }

    @Override
    public void close() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void flatMap(String s, Collector<TagPostUserHotEvent> collector) throws Exception {
        // 上游 binlog topic 含几十张表的变更; 99% 的事件不是 PostHot / PostResponse, 用字符串 contains 预筛, 命中才反序列化
        if (s == null || (!s.contains("\"PostHot\"") && !s.contains("\"PostResponse\""))) {
            return;
        }

        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        // 对齐离线 SQL: WHERE `_bin_op` < 2 AND rk=1 AND `_bin_op`=0 —— 实时侧只取最新的 op=0 新增事件
        Integer op = binlogRow.getOp();
        if (op == null || op != 0) {
            return;
        }
        Map<String, Object> data = binlogRow.getData();
        if (data == null) {
            return;
        }

        // 上游一条 binlog 只对应一种操作：要么 PostHot 中的某 type，要么 PostResponse 单条评论；6 个 cnt 中只有 1 个为 1.
        long likeCnt = 0L;
        long reproduceCnt = 0L;
        long recommendCnt = 0L;
        long collectCnt = 0L;
        long commentCnt = 0L;
        long underscoreCircleCommentCnt = 0L;

        Long postId;
        Long userId;
        String tbl = binlogRow.get_tbl();

        if ("PostHot".equals(tbl)) {
            Integer type = asInt(data.get("Type"));
            if (type == null || type < 1 || type > 4) {
                return;
            }
            postId = asLong(data.get("PostID"));
            userId = asLong(data.get("PublisherUserID"));
            switch (type) {
                case 1:
                    likeCnt = 1L;
                    break;
                case 2:
                    reproduceCnt = 1L;
                    break;
                case 3:
                    recommendCnt = 1L;
                    break;
                case 4:
                    collectCnt = 1L;
                    break;
                default:
                    break;
            }
        } else if ("PostResponse".equals(tbl)) {
            postId = asLong(data.get("PostID"));
            userId = asLong(data.get("PublisherUserID"));
            String pid = asString(data.get("pid"));
            String imgId = asString(data.get("imgId"));
            boolean isUnderscore = pid != null && !pid.isEmpty() && imgId == null;
            boolean isCircleComment = imgId != null && !imgId.isEmpty();
            if (!isUnderscore && !isCircleComment) {
                commentCnt = 1L;
            } else {
                underscoreCircleCommentCnt = 1L;
            }
        } else {
            return;
        }

        if (postId == null || postId <= 0 || userId == null || userId <= 0) {
            return;
        }

        // 通过缓存补 (blogId, tags); 离线 SQL 中 userId 来自 dwd 表的 userId(=publisherUserid), 这里直接用 binlog 的
        // publisherUserId
        PostMeta meta = lookupPostMeta(postId);
        if (meta == null
                || meta.getBlogId() == null
                || meta.getTags() == null
                || meta.getTags().isEmpty()) {
            return;
        }

        // explode(tags) + 查 shipNum，输出单事件；meta.getTags() 已在缓存时按 ACCOMPANY_TAG_SET 预过滤，无需再 contains
        for (String tag : meta.getTags()) {
            Long shipNum = lookupShipNum(tag, userId);

            TagPostUserHotEvent event = new TagPostUserHotEvent();
            event.setTag(tag);
            event.setPostId(postId);
            event.setBlogId(meta.getBlogId());
            event.setUserId(userId);
            event.setShipNum(shipNum);
            event.setLikeCnt(likeCnt);
            event.setReproduceCnt(reproduceCnt);
            event.setRecommendCnt(recommendCnt);
            event.setCollectCnt(collectCnt);
            event.setCommentCnt(commentCnt);
            event.setUnderscoreCircleCommentCnt(underscoreCircleCommentCnt);
            collector.collect(event);
        }
    }

    /** 通过 Caffeine + JDBC 查 Post 表，命中缓存即返回，未命中走 DB，sentinel 防穿透. */
    private PostMeta lookupPostMeta(Long postId) {
        PostMeta cached =
                postCache.get(
                        postId,
                        id -> {
                            try {
                                PostMeta fromDb = queryPostMetaFromDb(id);
                                return fromDb == null ? POST_NOT_FOUND : fromDb;
                            } catch (Exception e) {
                                LOG.warn("Failed to query Post for postId={}", id, e);
                                // 异常也写 sentinel, 避免每条 binlog 都重复查 DB; 等 6h 过期后再重试
                                return POST_NOT_FOUND;
                            }
                        });
        if (cached == POST_NOT_FOUND) {
            return null;
        }
        return cached;
    }

    private PostMeta queryPostMetaFromDb(Long postId) throws Exception {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(POST_LOOKUP_SQL)) {
            ps.setLong(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long blogId = rs.getLong("BlogID");
                    String tag = rs.getString("Tag");
                    if (tag == null || tag.trim().isEmpty()) {
                        return new PostMeta(blogId, Collections.emptyList());
                    }
                    // 按","切分; 原样精确匹配 AccompanyTagConfig.ACCOMPANY_TAG_SET (不做 toLowerCase / trim
                    // 转换).
                    // 缓存时即预过滤白名单, 后续主流程循环可省 contains 调用.
                    String[] tagArr = tag.split(",");
                    List<String> tags = new ArrayList<>(tagArr.length);
                    for (String t : tagArr) {
                        if (t != null
                                && !t.isEmpty()
                                && AccompanyTagConfig.ACCOMPANY_TAG_SET.contains(t)) {
                            tags.add(t);
                        }
                    }
                    return new PostMeta(blogId, tags);
                }
            }
        }
        return null;
    }

    /**
     * 通过 Caffeine + JDBC 查 Tag_UserShipInfo, sentinel 防穿透. 返回 null 等同于 left join 时 shipNum 为 NULL →
     * coalesce 为 0.
     */
    private Long lookupShipNum(String tag, Long userId) {
        String key = tag + "_" + userId;
        Long cached =
                shipNumCache.get(
                        key,
                        k -> {
                            try {
                                Long fromDb = queryShipNumFromDb(tag, userId);
                                return fromDb == null ? SHIP_NUM_NOT_FOUND : fromDb;
                            } catch (Exception e) {
                                LOG.warn(
                                        "Failed to query Tag_UserShipInfo for tag={}, userId={}",
                                        tag,
                                        userId,
                                        e);
                                // 异常也写 sentinel, 避免穿透; 等 6h 过期后再重试
                                return SHIP_NUM_NOT_FOUND;
                            }
                        });
        if (SHIP_NUM_NOT_FOUND.equals(cached)) {
            return null;
        }
        return cached;
    }

    private Long queryShipNumFromDb(String tag, Long userId) throws Exception {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(SHIP_NUM_LOOKUP_SQL)) {
            ps.setString(1, tag);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long v = rs.getLong("shipNum");
                    return rs.wasNull() ? null : v;
                }
            }
        }
        return null;
    }

    private static Long asLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer asInt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    /** 仅本算子内部用的轻量 Post 维度 (blogId + tags), 不放入 pojo 包. */
    private static final class PostMeta {
        private final Long blogId;
        private final List<String> tags;

        PostMeta(Long blogId, List<String> tags) {
            this.blogId = blogId;
            this.tags = tags;
        }

        Long getBlogId() {
            return blogId;
        }

        List<String> getTags() {
            return tags;
        }
    }
}
