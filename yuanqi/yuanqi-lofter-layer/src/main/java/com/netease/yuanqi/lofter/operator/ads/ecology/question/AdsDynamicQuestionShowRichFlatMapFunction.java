package com.netease.yuanqi.lofter.operator.ads.ecology.question;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.ads.ecology.question.AskQuestion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsDynamicQuestionShowRichFlatMapFunction
        extends RichFlatMapFunction<String, AskQuestion> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsDynamicQuestionShowRichFlatMapFunction.class);
    private ObjectMapper objectMapper;
    private static final Cache<String, Integer> cache =
            Caffeine.newBuilder()
                    .maximumSize(1000000)
                    .initialCapacity(1)
                    .expireAfterWrite(7, TimeUnit.HOURS)
                    .build();

    private static final Cache<Long, Long> blogIdCache =
            Caffeine.newBuilder()
                    .maximumSize(10000000)
                    .initialCapacity(1)
                    .expireAfterWrite(7, TimeUnit.HOURS)
                    .build();

    private static final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1);

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
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Ask_Question".equals(binlogRow.get_tbl())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            AskQuestion askQuestion =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(binlogRow.getData()),
                            AskQuestion.class);
            askQuestion.setTableName(binlogRow.get_tbl());

            if (binlogRow.getOp() == 0) {
                askQuestion.setDeltaDiscussCount(askQuestion.getDiscussCount());
                askQuestion.setDeltaScoreCount(askQuestion.getScoreCount());
            }

            if (binlogRow.getOp() == 2) {
                AskQuestion oldAskQuestion =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(binlogRow.getOld()),
                                AskQuestion.class);
                if (oldAskQuestion.getDiscussCount() != null) {
                    askQuestion.setDeltaDiscussCount(
                            askQuestion.getDiscussCount() - oldAskQuestion.getDiscussCount());
                } else {
                    askQuestion.setDeltaDiscussCount(0L);
                }

                if (oldAskQuestion.getScoreCount() != null) {
                    askQuestion.setDeltaScoreCount(
                            askQuestion.getScoreCount() - oldAskQuestion.getScoreCount());
                } else {
                    askQuestion.setDeltaScoreCount(0L);
                }
            }

            if (askQuestion.getTags() != null
                    && !"".equals(askQuestion.getTags())
                    && askQuestion.getStatus() != null
                    && askQuestion.getStatus() == 1
                    && askQuestion.getCosplay() != null) {
                // Parent question id and blogId
                if (askQuestion.getCosplay() == 2) {
                    if (askQuestion.getId() != null && askQuestion.getUserId() != null) {
                        blogIdCache.put(askQuestion.getId(), askQuestion.getUserId());
                    }
                }

                if (askQuestion.getCosplay() == 0
                        || askQuestion.getCosplay() == 1
                        || askQuestion.getCosplay() == 3) {
                    String[] tags = askQuestion.getTags().split(",");
                    if (!cache.getAllPresent(Arrays.asList(tags)).isEmpty()) {
                        for (String tag : tags) {
                            if (cache.getIfPresent(tag) != null) {
                                if (askQuestion.getCosplay() == 3) {
                                    askQuestion.setId(askQuestion.getParentQuestionId());
                                    askQuestion.setUserId(
                                            blogIdCache.getIfPresent(
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
    }

    private void updateCacheFromDb() throws SQLException {
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
                        "SELECT `id`,`userId` FROM Ask_Question WHERE cosplay = 2 and `id` > ? order by id asc limit 50000");
        long preId2 = Long.MIN_VALUE;
        int count2 = 0;
        do {
            int res2 = 0;
            preparedStatement2.setLong(1, preId2);
            ResultSet rs = preparedStatement2.executeQuery();
            while (rs.next()) {
                preId2 = Math.max(rs.getLong("id"), preId2);
                blogIdCache.put(rs.getLong("id"), rs.getLong("userId"));
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
