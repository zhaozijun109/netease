package com.netease.operator.ads.ecology.post;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.ecology.post.ActJoinTagPost;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava30.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava30.com.google.common.cache.CacheBuilder;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class AdsActTagJoinPostRichFlatMapFunction
        extends RichFlatMapFunction<String, ActJoinTagPost> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActTagJoinPostRichFlatMapFunction.class);
    private String jdbcUrl;
    private String driverClassName;
    private ObjectMapper objectMapper;
    private static final Cache<String, Tuple3<Long, Long, Long>> cache =
            CacheBuilder.newBuilder()
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
        ParameterTool p =
                (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        jdbcUrl = p.get("mysql.lofter.db.jdbc.url");
        driverClassName = p.get("mysql.jdbc.driver");
        objectMapper = new ObjectMapper();
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
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName") != null
                && "Post".equals(jsonNode.get("tableName").asText())
                && jsonNode.get("isPublished") != null
                && jsonNode.get("isPublished").asInt() != 0
                && jsonNode.get("type") != null
                && jsonNode.get("type").asInt() != 5
                && jsonNode.get("moveFrom") != null
                && !moveFromSet.contains(jsonNode.get("moveFrom").asText())
                && jsonNode.get("tag") != null
                && !jsonNode.get("tag").asText().trim().isEmpty()) {
            String[] tags = jsonNode.get("tag").asText().trim().toLowerCase().split(",");
            if (!cache.getAllPresent(Arrays.asList(tags)).isEmpty()) {
                ActJoinTagPost actJoinTagPost = new ActJoinTagPost();
                actJoinTagPost.setPostId(jsonNode.get("id").asLong());
                actJoinTagPost.setBlogId(jsonNode.get("blogId").asLong());
                if (jsonNode.get("allowView") != null
                        && jsonNode.get("allowView").asInt() == 0
                        && jsonNode.get("valid") != null
                        && jsonNode.get("valid").asInt() == 0) {
                    actJoinTagPost.setStatus(1);
                } else {
                    actJoinTagPost.setStatus(-1);
                }

                actJoinTagPost.setModifyTime(jsonNode.get("modifyTime").asLong());
                long publishTime = jsonNode.get("publishTime").asLong();
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

    private void getTagTaskFromDb() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
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
