package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.sink.es.Es6BaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.es.EsUtils;
import com.netease.yuanqi.lofter.operator.dim.es.DimIpPostRecommendRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.dim.es.DimIpPostRichAsyncFunction;
import com.netease.yuanqi.lofter.operator.dim.es.DimIpPostRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimIpPost;
import com.netease.yuanqi.lofter.pojo.DimIpPostRecommend;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.AsyncRetryStrategy;
import org.apache.flink.streaming.util.retryable.AsyncRetryStrategies;
import org.apache.flink.streaming.util.retryable.RetryPredicates;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

public class DimIpPostJob {
    private static void dimIpPostJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        DataStream<String> binlogRowDataStream =
                env.fromSource(
                                new KafkaCommonSource("lofter.binlog.online", "DimIpPostJob")
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "BinlogSource")
                        .setParallelism(1)
                        .uid("BinlogKafkaSource")
                        .name("BinlogKafkaSource");

        DataStream<DimIpPost> dimIpPostDataStream =
                binlogRowDataStream
                        .flatMap(new DimIpPostRichFlatMapFunction())
                        .setParallelism(1)
                        .uid("DimIpPostRichFlatMapFunction")
                        .name("DimIpPostRichFlatMapFunction");

        DataStream<DimIpPostRecommend> dimIpPostRecommendDataStream =
                binlogRowDataStream
                        .flatMap(new DimIpPostRecommendRichFlatMapFunction())
                        .setParallelism(1)
                        .uid("DimIpPostRecommendRichFlatMapFunction")
                        .name("DimIpPostRecommendRichFlatMapFunction");

        AsyncRetryStrategy<DimIpPost> asyncRetryStrategy =
                new AsyncRetryStrategies.FixedDelayRetryStrategyBuilder<DimIpPost>(3, 1000L)
                        .ifResult(RetryPredicates.EMPTY_RESULT_PREDICATE)
                        .ifException(RetryPredicates.HAS_EXCEPTION_PREDICATE)
                        .build();

        AsyncDataStream.unorderedWaitWithRetry(
                        dimIpPostDataStream,
                        new DimIpPostRichAsyncFunction(),
                        30,
                        TimeUnit.SECONDS,
                        100,
                        asyncRetryStrategy)
                .setParallelism(1)
                .uid("DimIpPostRichAsyncFunction")
                .name("DimIpPostRichAsyncFunction")
                .sinkTo(
                        new Es6BaseSink<>(
                                        new ElasticsearchEmitter<DimIpPost>() {
                                            private ObjectMapper objectMapper;

                                            @Override
                                            public void open() throws Exception {
                                                objectMapper = new ObjectMapper();
                                            }

                                            @Override
                                            public void emit(
                                                    DimIpPost dimIpPost,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index(
                                                                            "lofter_dim_ip_new_post_v1")
                                                                    .type("_doc")
                                                                    .id(
                                                                            dimIpPost
                                                                                    .getPostId()
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimIpPost),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        },
                                        EsUtils.parseEsHosts(
                                                ClusterConfigOptions.getEsHosts(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)),
                                        ClusterConfigOptions.getEsAuthUserAndPass(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)
                                                .f0,
                                        ClusterConfigOptions.getEsAuthUserAndPass(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)
                                                .f1,
                                        FlushBackoffType.EXPONENTIAL,
                                        5,
                                        1000,
                                        1,
                                        1024,
                                        60000)
                                .createEsSink())
                .setParallelism(1)
                .uid("DimIpPostEsSink")
                .name("DimIpPostEsSink");

        dimIpPostRecommendDataStream
                .sinkTo(
                        new Es6BaseSink<>(
                                        new ElasticsearchEmitter<DimIpPostRecommend>() {
                                            @Override
                                            public void emit(
                                                    DimIpPostRecommend dimIpPostRecommend,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                try {
                                                    UpdateRequest updateRequest =
                                                            new UpdateRequest()
                                                                    .index(
                                                                            "lofter_dim_ip_new_post_v1")
                                                                    .type("_doc")
                                                                    .id(
                                                                            dimIpPostRecommend
                                                                                    .getPostId()
                                                                                    .toString())
                                                                    .doc(
                                                                            XContentFactory
                                                                                    .jsonBuilder()
                                                                                    .startObject()
                                                                                    .field(
                                                                                            "isInRecommendPool",
                                                                                            dimIpPostRecommend
                                                                                                    .getIsInRecommendPool())
                                                                                    .endObject())
                                                                    .docAsUpsert(false)
                                                                    .detectNoop(false);

                                                    requestIndexer.add(updateRequest);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        },
                                        EsUtils.parseEsHosts(
                                                ClusterConfigOptions.getEsHosts(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)),
                                        ClusterConfigOptions.getEsAuthUserAndPass(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)
                                                .f0,
                                        ClusterConfigOptions.getEsAuthUserAndPass(
                                                        ClusterConfigOptions.EsHostsEnum.COMMON)
                                                .f1,
                                        FlushBackoffType.EXPONENTIAL,
                                        5,
                                        1000,
                                        1,
                                        1024,
                                        60000)
                                .createEsFailureHandlerSink())
                .setParallelism(1)
                .uid("DimIpPostEsUpdateSink")
                .name("DimIpPostEsUpdateSink");

        env.execute("DimIpPostJob");
    }

    public static void main(String[] args) throws Exception {
        dimIpPostJob(ParameterTool.fromArgs(args));
    }
}
