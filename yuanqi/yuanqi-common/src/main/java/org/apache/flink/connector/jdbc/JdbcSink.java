package org.apache.flink.connector.jdbc;

import javax.sql.XADataSource;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.connector.jdbc.datasource.connections.SimpleJdbcConnectionProvider;
import org.apache.flink.connector.jdbc.internal.GenericJdbcSinkFunction;
import org.apache.flink.connector.jdbc.internal.JdbcOutputFormat;
import org.apache.flink.connector.jdbc.internal.executor.JdbcBatchStatementExecutor;
import org.apache.flink.connector.jdbc.sink.JdbcSinkBuilder;
import org.apache.flink.connector.jdbc.xa.JdbcXaSinkFunction;
import org.apache.flink.connector.jdbc.xa.XaFacade;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.function.SerializableSupplier;

@PublicEvolving
public class JdbcSink {
    public static <T> SinkFunction<T> sink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcConnectionOptions connectionOptions) {
        return sink(sql, statementBuilder, JdbcExecutionOptions.defaults(), connectionOptions);
    }

    public static <T> SinkFunction<T> sink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions,
            JdbcConnectionOptions connectionOptions) {
        return new GenericJdbcSinkFunction<>(
                new JdbcOutputFormat<>(
                        new SimpleJdbcConnectionProvider(connectionOptions),
                        executionOptions,
                        () -> JdbcBatchStatementExecutor.simple(sql, statementBuilder)));
    }

    public static <T> SinkFunction<T> exactlyOnceSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions,
            JdbcExactlyOnceOptions exactlyOnceOptions,
            SerializableSupplier<XADataSource> dataSourceSupplier) {
        return new JdbcXaSinkFunction<>(
                sql,
                statementBuilder,
                XaFacade.fromXaDataSourceSupplier(
                        dataSourceSupplier,
                        exactlyOnceOptions.getTimeoutSec(),
                        exactlyOnceOptions.isTransactionPerConnection()),
                executionOptions,
                exactlyOnceOptions);
    }

    public static <T> SinkFunction<T> multipleHostSink(
            String sql,
            JdbcStatementBuilder<T> statementBuilder,
            JdbcExecutionOptions executionOptions,
            JdbcConnectionOptions connectionOptions) {
        return new GenericJdbcSinkFunction<>(
                new JdbcOutputFormat<>(
                        new MultipleHostJdbcConnectionProvider(connectionOptions),
                        executionOptions,
                        () -> JdbcBatchStatementExecutor.simple(sql, statementBuilder)));
    }

    public static <IN> JdbcSinkBuilder<IN> builder() {
        return org.apache.flink.connector.jdbc.sink.JdbcSink.builder();
    }

    private JdbcSink() {}
}
