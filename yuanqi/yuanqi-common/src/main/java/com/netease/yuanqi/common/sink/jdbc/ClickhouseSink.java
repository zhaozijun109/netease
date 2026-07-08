package com.netease.yuanqi.common.sink.jdbc;

import com.netease.yuanqi.common.pojo.config.JdbcConfig;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class ClickhouseSink<T> {
    private static final String JDBC_URL =
            "jdbc:clickhouse:http://lofter-data-common6.gy.ntes:8123/lofter?socket_timeout=1000000";
    private static final String DRIVER_CLASS_NAME = "com.clickhouse.jdbc.ClickHouseDriver";
    private static final String USERNAME = "lofter_rw";
    private static final String PASSWORD = "O4nWNA9slAn8";
    private final JdbcConfig jdbcConfig;

    public ClickhouseSink() {
        this.jdbcConfig =
                JdbcConfig.builder()
                        .setDriverClassName(DRIVER_CLASS_NAME)
                        .setUrl(JDBC_URL)
                        .setUsername(USERNAME)
                        .setPassword(PASSWORD)
                        .build();
    }

    public ClickhouseSink(String jdbcUrl) {
        this.jdbcConfig =
                JdbcConfig.builder()
                        .setDriverClassName(DRIVER_CLASS_NAME)
                        .setUrl(jdbcUrl)
                        .setUsername(USERNAME)
                        .setPassword(PASSWORD)
                        .build();
    }

    public SinkFunction<T> createSingleHostClickhouseSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions) {
        return new JdbcBaseSink<T>(jdbcConfig)
                .createSingleHostSink(sql, statementBuilder, executionOptions);
    }

    public SinkFunction<T> createMultiHostsClickhouseSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions) {
        return new JdbcBaseSink<T>(jdbcConfig)
                .createMultipleHostSink(sql, statementBuilder, executionOptions);
    }
}
