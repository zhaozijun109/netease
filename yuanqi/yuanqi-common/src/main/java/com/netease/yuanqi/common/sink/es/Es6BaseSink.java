package com.netease.yuanqi.common.sink.es;

import java.io.Serializable;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch6SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.elasticsearch.sink.FailureHandler;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elasticsearch 6.x sink 构建基类.
 *
 * <p>仅引用 {@code flink-connector-elasticsearch6} 的类型。需要 ES8 写入能力的 Job 请改用 {@link Es8BaseSink}.
 */
public class Es6BaseSink<T> implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Es6BaseSink.class);
    private static final long serialVersionUID = 1L;
    private final ElasticsearchEmitter<T> emitter;
    private final HttpHost[] httpHosts;
    private final String username;
    private final String password;
    private final FlushBackoffType flushBackoffType;
    private final int maxRetryTimes;
    private final int delayMillis;
    private final int numMaxActions;
    private final int maxSizeMb;
    private final long intervalMillis;

    public Es6BaseSink(
            ElasticsearchEmitter<T> emitter,
            HttpHost[] httpHosts,
            String username,
            String password,
            FlushBackoffType flushBackoffType,
            int maxRetryTimes,
            int delayMillis,
            int numMaxActions,
            int maxSizeMb,
            long intervalMillis) {
        this.emitter = emitter;
        this.httpHosts = httpHosts;
        this.username = username;
        this.password = password;
        this.flushBackoffType = flushBackoffType;
        this.maxRetryTimes = maxRetryTimes;
        this.delayMillis = delayMillis;
        this.numMaxActions = numMaxActions;
        this.maxSizeMb = maxSizeMb;
        this.intervalMillis = intervalMillis;
    }

    public ElasticsearchSink<T> createEsSink() {
        return new Elasticsearch6SinkBuilder<T>()
                .setHosts(httpHosts)
                .setConnectionUsername(username)
                .setConnectionPassword(password)
                .setEmitter(emitter)
                .setBulkFlushBackoffStrategy(flushBackoffType, maxRetryTimes, delayMillis)
                .setBulkFlushMaxActions(numMaxActions)
                .setBulkFlushMaxSizeMb(maxSizeMb)
                .setBulkFlushInterval(intervalMillis)
                .build();
    }

    public ElasticsearchSink<T> createEsFailureHandlerSink() {
        return new Elasticsearch6SinkBuilder<T>()
                .setHosts(httpHosts)
                .setConnectionUsername(username)
                .setConnectionPassword(password)
                .setEmitter(emitter)
                .setBulkFlushBackoffStrategy(flushBackoffType, maxRetryTimes, delayMillis)
                .setBulkFlushMaxActions(numMaxActions)
                .setBulkFlushMaxSizeMb(maxSizeMb)
                .setBulkFlushInterval(intervalMillis)
                .setFailureHandler(
                        new FailureHandler() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                if (throwable
                                        .getCause()
                                        .getMessage()
                                        .contains("document_missing_exception")) {
                                    LOG.error(
                                            "Es document missing exception, {}",
                                            throwable.getMessage());
                                } else {
                                    throw new FlinkRuntimeException(throwable);
                                }
                            }
                        })
                .build();
    }
}
