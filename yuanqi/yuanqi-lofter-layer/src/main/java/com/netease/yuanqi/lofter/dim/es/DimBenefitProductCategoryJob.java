package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.sink.es.Es6BaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.es.EsUtils;
import com.netease.yuanqi.lofter.operator.dim.es.DimBenefitProductCategoryRichAsyncFunction;
import com.netease.yuanqi.lofter.operator.dim.es.DimBenefitProductCategoryRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimBenefitCategory;
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
import org.apache.flink.util.FlinkRuntimeException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimBenefitProductCategoryJob {
    private static void dimBenefitProductCategoryJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        DataStream<DimBenefitCategory> dimBenefitCategoryDataStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.binlog.online",
                                                "DimBenefitProductCategoryJob")
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "DimBenefitProductCategorySource")
                        .setParallelism(1)
                        .uid("DimBenefitProductCategoryKafkaSource")
                        .name("DimBenefitProductCategoryKafkaSource")
                        .flatMap(new DimBenefitProductCategoryRichFlatMapFunction())
                        .setParallelism(1)
                        .uid("DimBenefitProductCategoryRichFlatMapFunction")
                        .name("DimBenefitProductCategoryRichFlatMapFunction");

        AsyncRetryStrategy<DimBenefitCategory> asyncRetryStrategy =
                new AsyncRetryStrategies.FixedDelayRetryStrategyBuilder<DimBenefitCategory>(
                                5, 60000)
                        .ifResult(RetryPredicates.EMPTY_RESULT_PREDICATE)
                        .ifException(
                                RetryPredicates.createExceptionTypePredicate(
                                        FlinkRuntimeException.class))
                        .build();

        AsyncDataStream.unorderedWaitWithRetry(
                        dimBenefitCategoryDataStream,
                        new DimBenefitProductCategoryRichAsyncFunction(),
                        30,
                        TimeUnit.SECONDS,
                        100,
                        asyncRetryStrategy)
                .sinkTo(
                        new Es6BaseSink<>(
                                        new ElasticsearchEmitter<DimBenefitCategory>() {
                                            private ObjectMapper objectMapper;

                                            @Override
                                            public void open() throws Exception {
                                                objectMapper = new ObjectMapper();
                                            }

                                            @Override
                                            public void emit(
                                                    DimBenefitCategory dimBenefitCategory,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index(
                                                                            "lofter_dim_benefit_product_category")
                                                                    .type("_doc")
                                                                    .id(
                                                                            dimBenefitCategory
                                                                                    .getCategoryId()
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimBenefitCategory),
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
                .uid("DimBenefitProductCategoryEsSink")
                .name("DimBenefitProductCategoryEsSink");

        env.execute("DimBenefitProductCategoryJob");
    }

    public static void main(String[] args) throws Exception {
        dimBenefitProductCategoryJob(ParameterTool.fromArgs(args));
    }
}
