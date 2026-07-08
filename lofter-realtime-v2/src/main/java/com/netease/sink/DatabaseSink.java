package com.netease.sink;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DatabaseSink<T> {
    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseSink(String jdbcDriver, String jdbcUrl, String username, String password) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public SinkFunction<T> createClickhouseSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions) {
        return JdbcSink.sink(
                sql,
                statementBuilder,
                executionOptions,
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcUrl)
                        .withDriverName(jdbcDriver)
                        .withUsername(username)
                        .withPassword(password)
                        .build());
    }
}
