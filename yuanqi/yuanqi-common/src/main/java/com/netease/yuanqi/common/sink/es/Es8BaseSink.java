package com.netease.yuanqi.common.sink.es;

import java.io.Serializable;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch8AsyncSink;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch8AsyncSinkBuilder;
import org.apache.http.HttpHost;

/**
 * Elasticsearch 8.x sink 构建基类.
 *
 * <p>仅引用 {@code flink-connector-elasticsearch8} 的类型。需要 ES6 写入能力的 Job 请改用 {@link Es6BaseSink}.
 *
 * <p>ES8 connector 使用 {@link Elasticsearch8AsyncSinkBuilder}（继承 AsyncSinkBase），其可调参数集合与 ES6 完全不同（无
 * flush/backoff 等参数），均使用 connector 提供的默认值。
 */
public class Es8BaseSink<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final HttpHost[] httpHosts;
    private final String username;
    private final String password;

    public Es8BaseSink(HttpHost[] httpHosts, String username, String password) {
        this.httpHosts = httpHosts;
        this.username = username;
        this.password = password;
    }

    public Elasticsearch8AsyncSink<T> createEsSink(Es8Emitter<T> es8Emitter) {
        return Elasticsearch8AsyncSinkBuilder.<T>builder()
                .setHosts(httpHosts)
                .setUsername(username)
                .setPassword(password)
                .setElementConverter(es8Emitter)
                .build();
    }
}
