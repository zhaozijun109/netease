package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPost;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.Post;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsActTagJoinPostRichFlatMapFunction
        extends RichFlatMapFunction<String, ActJoinTagPost> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActTagJoinPostRichFlatMapFunction.class);
    private ObjectMapper objectMapper;
    private static final Cache<String, Tuple3<Long, Long, Long>> cache =
            Caffeine.newBuilder()
                    .maximumSize(1000000)
                    .initialCapacity(1)
                    .expireAfterWrite(3, TimeUnit.HOURS)
                    .build();
    private static final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1);
    private Set<String> moveFromSet = new HashSet<>(50);
    private final Set<Long> expireActTaskIdSet = new HashSet<>(50);

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        scheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getTagTaskFromDb();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                0,
                1,
                TimeUnit.HOURS);
        moveFromSet =
                new HashSet<>(
                        Arrays.asList(
                                "blog",
                                "lofternetease",
                                "blog163like",
                                "loftmove",
                                "BLOGPOST",
                                "bbs",
                                "photo-pp",
                                "163_news",
                                "instagram_mirror",
                                "weibo_sync",
                                "news",
                                "pp",
                                "netease_photo",
                                "move"));
    }

    @Override
    public void flatMap(String s, Collector<ActJoinTagPost> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Post".equals(binlogRow.get_tbl())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            Post post =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(binlogRow.getData()), Post.class);
            post.setTableName(binlogRow.get_tbl());

            if (post.getIsPublished() != null
                    && post.getIsPublished() != 0
                    && post.getType() != null
                    && post.getType() != 5
                    && post.getMoveFrom() != null
                    && !moveFromSet.contains(post.getMoveFrom())
                    && post.getTag() != null
                    && !post.getTag().trim().isEmpty()) {
                String[] tags = post.getTag().trim().toLowerCase().split(",");
                if (!cache.getAllPresent(Arrays.asList(tags)).isEmpty()) {
                    ActJoinTagPost actJoinTagPost = new ActJoinTagPost();
                    actJoinTagPost.setPostId(post.getId());
                    actJoinTagPost.setBlogId(post.getBlogId());
                    if (post.getAllowView() != null
                            && post.getAllowView() == 0
                            && post.getValid() != null
                            && post.getValid() == 0) {
                        actJoinTagPost.setStatus(1);
                    } else {
                        actJoinTagPost.setStatus(-1);
                    }

                    actJoinTagPost.setModifyTime(post.getModifyTime());
                    long publishTime = post.getPublishTime();
                    Set<Long> actTaskIds = new HashSet<>();
                    for (String tag : tags) {
                        Tuple3<Long, Long, Long> tuple3 = cache.getIfPresent(tag);
                        if (tuple3 != null
                                && tuple3.f2 >= System.currentTimeMillis()
                                && publishTime <= tuple3.f2
                                && publishTime >= tuple3.f1) {
                            actTaskIds.add(tuple3.f0);
                        }
                    }
                    if (!actTaskIds.isEmpty()) {
                        actJoinTagPost.setActTaskIds(actTaskIds);
                        collector.collect(actJoinTagPost);
                    }
                }
            }
            if (!expireActTaskIdSet.isEmpty()) {
                ActJoinTagPost actJoinTagPost = new ActJoinTagPost();
                actJoinTagPost.setActTaskIds(expireActTaskIdSet);

                actJoinTagPost.setStatus(-2);
                LOG.info(
                        "==============expireActTaskIdSet==============: {}",
                        objectMapper.writeValueAsString(actJoinTagPost));
                collector.collect(actJoinTagPost);
                expireActTaskIdSet.clear();
            }
        }
    }

    private void getTagTaskFromDb() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
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
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement(
                        "SELECT `id`,`tag`,`taskStartTime`,`taskEndTime`, `progress`, `target` FROM Tag_GroupTask WHERE `status` = 0 and `id` > ? order by id asc limit 50000");
        long preId = Long.MIN_VALUE;
        int count = 0;
        do {
            int res = 0;
            preparedStatement.setLong(1, preId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                preId = Math.max(rs.getLong("id"), preId);
                if (rs.getLong("taskEndTime") >= System.currentTimeMillis()) {
                    cache.put(
                            rs.getString("tag"),
                            Tuple3.of(
                                    rs.getLong("id"),
                                    rs.getLong("taskStartTime"),
                                    rs.getLong("taskEndTime")));
                } else if (rs.getLong("taskEndTime")
                        >= (System.currentTimeMillis() - 3 * 60 * 60 * 1000)) { // 时间控制
                    expireActTaskIdSet.add(rs.getLong("id"));
                }
                res += 1;
            }
            count = res;
            LOG.info("===============Get Tag_GroupTask From DB============= count: {}", count);
        } while (count == 50000);
        preparedStatement.close();
        connection.close();
        dataSource.close();
    }
}
