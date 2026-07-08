package com.netease.yuanqi.common.sink.jdbc;

import com.netease.yuanqi.common.pojo.config.JdbcConfig;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class JdbcBaseSink<T> {
    private final JdbcConfig jdbcConfig;

    public JdbcBaseSink(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public SinkFunction<T> createSingleHostSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions) {
        return JdbcSink.sink(
                sql,
                statementBuilder,
                executionOptions,
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcConfig.getUrl())
                        .withDriverName(jdbcConfig.getDriverClassName())
                        .withUsername(jdbcConfig.getUsername())
                        .withPassword(jdbcConfig.getPassword())
                        .build());
    }

    public SinkFunction<T> createMultipleHostSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions) {
        return JdbcSink.multipleHostSink(
                sql,
                statementBuilder,
                executionOptions,
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcConfig.getUrl())
                        .withDriverName(jdbcConfig.getDriverClassName())
                        .withUsername(jdbcConfig.getUsername())
                        .withPassword(jdbcConfig.getPassword())
                        .build());
    }
}
