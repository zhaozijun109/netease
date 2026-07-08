package com.netease.operator.ads.ecology.question;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.ecology.question.AskQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava30.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava30.com.google.common.cache.CacheBuilder;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AdsDynamicQuestionShowRichFlatMapFunction
        extends RichFlatMapFunction<String, AskQuestion> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsDynamicQuestionShowRichFlatMapFunction.class);
    private String jdbcUrl;
    private String driverClassName;
    private ObjectMapper objectMapper;
    private static final Cache<String, Integer> cache =
            CacheBuilder.newBuilder()
                    .maximumSize(1000000)
                    .initialCapacity(1)
                    .expireAfterWrite(7, TimeUnit.HOURS)
                    .build();

    private static final Cache<Long, Long> blogIdCache =
            CacheBuilder.newBuilder()
                    .maximumSize(10000000)
                    .initialCapacity(1)
                    .expireAfterWrite(7, TimeUnit.HOURS)
                    .build();

    private static final Cache<Long, Long> createTimeCache =
            CacheBuilder.newBuilder()
                    .maximumSize(10000000)
                    .initialCapacity(1)
                    .expireAfterWrite(7, TimeUnit.HOURS)
                    .build();

    private static final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1);

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
                            updateCacheFromDb();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                0,
                6,
                TimeUnit.HOURS);
    }

    @Override
    public void flatMap(String s, Collector<AskQuestion> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName") != null
                && "Ask_Question".equals(jsonNode.get("tableName").asText())
                && jsonNode.get("tags") != null
                && !"".equals(jsonNode.get("tags").asText())
                && jsonNode.get("status").asInt() == 1
                && jsonNode.get("cosplay") != null) {
            // Parent question id and blogId
            if (jsonNode.get("cosplay").asInt() == 2) {
                if (jsonNode.get("id") != null
                        && jsonNode.get("userId") != null
                        && jsonNode.get("createTime") != null) {
                    blogIdCache.put(jsonNode.get("id").asLong(), jsonNode.get("userId").asLong());
                    createTimeCache.put(
                            jsonNode.get("id").asLong(), jsonNode.get("createTime").asLong());
                }
            }

            if (jsonNode.get("cosplay").asInt() == 0
                    || jsonNode.get("cosplay").asInt() == 1
                    || jsonNode.get("cosplay").asInt() == 3) {
                String[] tags = jsonNode.get("tags").asText().split(",");
                if (!cache.getAllPresent(Arrays.asList(tags)).isEmpty()) {
                    for (String tag : tags) {
                        if (cache.getIfPresent(tag) != null) {
                            AskQuestion askQuestion = objectMapper.readValue(s, AskQuestion.class);
                            if (askQuestion.getCosplay() == 3) {
                                askQuestion.setId(askQuestion.getParentQuestionId());
                                askQuestion.setUserId(
                                        blogIdCache.getIfPresent(
                                                askQuestion.getParentQuestionId()));
                                askQuestion.setCreateTime(
                                        createTimeCache.getIfPresent(
                                                askQuestion.getParentQuestionId()));
                            }
                            // tag数组转存为要被统计的tag
                            askQuestion.setTags(tag);
                            collector.collect(askQuestion);
                        }
                    }
                }
            }
        }
    }

    private void updateCacheFromDb() throws SQLException {
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
                        "SELECT `id`,`tag` FROM Ask_TagChatItem WHERE `status` = 1 and `id` > ? order by id asc limit 50000");
        long preId = Long.MIN_VALUE;
        int count = 0;
        do {
            int res = 0;
            preparedStatement.setLong(1, preId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                preId = Math.max(rs.getLong("id"), preId);
                cache.put(rs.getString("tag"), 1);
                res += 1;
            }
            count = res;
            LOG.info("===============UpdateCacheFromDb=============: {}", count);
        } while (count == 50000);
        preparedStatement.close();

        PreparedStatement preparedStatement2 =
                connection.prepareStatement(
                        "SELECT `id`,`userId`, `createTime` FROM Ask_Question WHERE cosplay = 2 and status = 1 and `id` > ? order by id asc limit 50000");
        long preId2 = Long.MIN_VALUE;
        int count2 = 0;
        do {
            int res2 = 0;
            preparedStatement2.setLong(1, preId2);
            ResultSet rs = preparedStatement2.executeQuery();
            while (rs.next()) {
                preId2 = Math.max(rs.getLong("id"), preId2);
                blogIdCache.put(rs.getLong("id"), rs.getLong("userId"));
                createTimeCache.put(rs.getLong("id"), rs.getLong("createTime"));
                res2 += 1;
            }
            count2 = res2;
            LOG.info("===============UpdateBlogIdCacheFromDb=============: {}", count2);
        } while (count2 == 50000);
        preparedStatement2.close();

        connection.close();
        dataSource.close();
    }
}
