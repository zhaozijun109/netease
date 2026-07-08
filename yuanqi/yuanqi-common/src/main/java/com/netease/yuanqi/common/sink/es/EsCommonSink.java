package com.netease.yuanqi.common.sink.es;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.EsConfig;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.utils.es.EsUtils;
import java.io.Serializable;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch8AsyncSink;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.http.HttpHost;

public class EsCommonSink implements Serializable {
    private static final long serialVersionUID = 1L;
    private final EsConfig esConfig;

    public EsCommonSink() {
        HttpHost[] esHttpHosts =
                EsUtils.parseEsHosts(
                        ClusterConfigOptions.getEsHosts(ClusterConfigOptions.EsHostsEnum.COMMON));
        Tuple2<String, String> authUserAndPass =
                ClusterConfigOptions.getEsAuthUserAndPass(ClusterConfigOptions.EsHostsEnum.COMMON);
        this.esConfig =
                EsConfig.builder()
                        .setHttpHosts(esHttpHosts)
                        .setUsername(authUserAndPass.f0)
                        .setPassword(authUserAndPass.f1)
                        .build();
    }

    public EsCommonSink(ClusterConfigOptions.EsHostsEnum esHostsEnum) {
        HttpHost[] esHttpHosts = EsUtils.parseEsHosts(ClusterConfigOptions.getEsHosts(esHostsEnum));
        Tuple2<String, String> authUserAndPass =
                ClusterConfigOptions.getEsAuthUserAndPass(esHostsEnum);
        this.esConfig =
                EsConfig.builder()
                        .setHttpHosts(esHttpHosts)
                        .setUsername(authUserAndPass.f0)
                        .setPassword(authUserAndPass.f1)
                        .build();
    }

    // ------------------------------------------------------------------------
    //  Elasticsearch 6.x sinks
    // ------------------------------------------------------------------------

    public ElasticsearchSink<BinlogRow> createCommonBinlogEsSink(
            ElasticsearchEmitter<BinlogRow> emitter) {
        return new Es6BaseSink<>(
                        emitter,
                        esConfig.getHttpHosts(),
                        esConfig.getUsername(),
                        esConfig.getPassword(),
                        FlushBackoffType.EXPONENTIAL,
                        5,
                        1000,
                        1,
                        1024,
                        60000)
                .createEsSink();
    }

    public ElasticsearchSink<String> createCommonStringEsSink(
            ElasticsearchEmitter<String> emitter) {
        return new Es6BaseSink<>(
                        emitter,
                        esConfig.getHttpHosts(),
                        esConfig.getUsername(),
                        esConfig.getPassword(),
                        FlushBackoffType.EXPONENTIAL,
                        5,
                        1000,
                        1,
                        1024,
                        60000)
                .createEsSink();
    }

    public <T> ElasticsearchSink<T> createCommonEsSink(ElasticsearchEmitter<T> emitter) {
        return new Es6BaseSink<>(
                        emitter,
                        esConfig.getHttpHosts(),
                        esConfig.getUsername(),
                        esConfig.getPassword(),
                        FlushBackoffType.EXPONENTIAL,
                        5,
                        1000,
                        1,
                        1024,
                        60000)
                .createEsSink();
    }

    // ------------------------------------------------------------------------
    //  Elasticsearch 8.x sinks
    // ------------------------------------------------------------------------

    public Elasticsearch8AsyncSink<BinlogRow> createCommonBinlogEs8Sink(
            Es8Emitter<BinlogRow> emitter) {
        return new Es8BaseSink<BinlogRow>(
                        esConfig.getHttpHosts(), esConfig.getUsername(), esConfig.getPassword())
                .createEsSink(emitter);
    }

    public Elasticsearch8AsyncSink<String> createCommonStringEs8Sink(Es8Emitter<String> emitter) {
        return new Es8BaseSink<String>(
                        esConfig.getHttpHosts(), esConfig.getUsername(), esConfig.getPassword())
                .createEsSink(emitter);
    }

    public <T> Elasticsearch8AsyncSink<T> createCommonEs8Sink(Es8Emitter<T> emitter) {
        return new Es8BaseSink<T>(
                        esConfig.getHttpHosts(), esConfig.getUsername(), esConfig.getPassword())
                .createEsSink(emitter);
    }
}
