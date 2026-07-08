package com.netease.operator.ads.ecology.post;

import com.alibaba.druid.pool.DruidDataSource;
import com.netease.pojo.ecology.post.ActJoinTagPostHot;

import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava30.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava30.com.google.common.cache.CacheBuilder;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AdsActJoinTagPostProcessWindowFunction
        extends ProcessWindowFunction<ActJoinTagPostHot, ActJoinTagPostHot, String, TimeWindow> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActJoinTagPostProcessWindowFunction.class);
    private String jdbcUrl;
    private String driverClassName;
    private static final Cache<Long, Tuple2<String, Long>> cache =
            CacheBuilder.newBuilder()
                    .maximumSize(1000000)
                    .initialCapacity(1)
                    .expireAfterWrite(3, TimeUnit.HOURS)
                    .build();
    private static final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1);
    private ValueState<Long> postHotStatisticsState;

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool p =
                (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        jdbcUrl = p.get("mysql.lofter.db.jdbc.url");
        driverClassName = p.get("mysql.jdbc.driver");
        scheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getActTaskTagFromDb();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                0,
                1,
                TimeUnit.HOURS);

        ValueStateDescriptor<Long> postHotStatisticsStateDescriptor =
                new ValueStateDescriptor<>(
                        "ActJoinTagPostHotStatisticsState", BasicTypeInfo.LONG_TYPE_INFO);
        StateTtlConfig stateTtlConfig =
                StateTtlConfig.newBuilder(Time.days(35))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                        .cleanupInRocksdbCompactFilter(100000)
                        .build();
        postHotStatisticsStateDescriptor.enableTimeToLive(stateTtlConfig);
        postHotStatisticsState = getRuntimeContext().getState(postHotStatisticsStateDescriptor);
    }

    @Override
    public void process(
            String key,
            ProcessWindowFunction<ActJoinTagPostHot, ActJoinTagPostHot, String, TimeWindow>.Context
                    context,
            Iterable<ActJoinTagPostHot> iterable,
            Collector<ActJoinTagPostHot> collector)
            throws Exception {
        ActJoinTagPostHot actJoinTagPostHot = iterable.iterator().next();
        Tuple2<String, Long> tuple2 = cache.getIfPresent(actJoinTagPostHot.getActTaskId());
        if (tuple2 != null && tuple2.f1 >= System.currentTimeMillis()) {
            actJoinTagPostHot.setTag(tuple2.f0);
            Long deltaHot = actJoinTagPostHot.getHot();
            Long currentHot = postHotStatisticsState.value();
            if (currentHot == null) {
                postHotStatisticsState.update(deltaHot);
            } else {
                postHotStatisticsState.update(currentHot + deltaHot);
                actJoinTagPostHot.setHot(currentHot + deltaHot);
            }
            collector.collect(actJoinTagPostHot);
        }
    }

    private void getActTaskTagFromDb() throws SQLException {
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
                        "SELECT `id`,`tag`,`taskEndTime`, `progress`, `target` FROM Tag_GroupTask WHERE `status` = 0 and `id` > ? order by id asc limit 50000");
        long preId = Long.MIN_VALUE;
        int count = 0;
        do {
            int res = 0;
            preparedStatement.setLong(1, preId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                if (rs.getLong("taskEndTime") >= System.currentTimeMillis()) {
                    preId = Math.max(rs.getLong("id"), preId);
                    cache.put(
                            rs.getLong("id"),
                            Tuple2.of(rs.getString("tag"), rs.getLong("taskEndTime")));
                }
                res += 1;
            }
            count = res;
            LOG.info("===============Get Task Tag From DB============= count: {}", count);
        } while (count == 50000);
        preparedStatement.close();
        connection.close();
        dataSource.close();
    }
}
