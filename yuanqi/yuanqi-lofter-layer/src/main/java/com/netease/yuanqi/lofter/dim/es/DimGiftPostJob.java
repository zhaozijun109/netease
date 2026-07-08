package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.sink.es.Es6BaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.es.EsUtils;
import com.netease.yuanqi.lofter.operator.dim.es.DimGiftPostBroadcastProcessFunction;
import com.netease.yuanqi.lofter.operator.dim.es.DimGiftPostPgcRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.dim.es.DimGiftPostRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimGiftPost;
import com.netease.yuanqi.lofter.pojo.DimGiftPostPgc;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimGiftPostJob {
    private static final Logger LOG = LoggerFactory.getLogger(DimGiftPostJob.class);
    private static final MapStateDescriptor<Long, Long> DIM_GIFT_POST_PGC_BROADCAST_DESC =
            new MapStateDescriptor<>(
                    "DimGiftPostPgc", BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO);

    private static void dimGiftPostJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        DataStream<String> binlogStream =
                env.fromSource(
                                new KafkaCommonSource("lofter.binlog.online", "DimGiftPostJob")
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "BinlogSource")
                        .setParallelism(1)
                        .uid("BinlogKafkaSource")
                        .name("BinlogKafkaSource");

        DataStream<DimGiftPost> dimGiftPostDataStream =
                binlogStream
                        .flatMap(new DimGiftPostRichFlatMapFunction())
                        .setParallelism(1)
                        .uid("DimGiftPostRichFlatMapFunction")
                        .name("DimGiftPostRichFlatMapFunction");

        DataStream<DimGiftPostPgc> dimGiftPostPgcDataStream =
                binlogStream
                        .flatMap(new DimGiftPostPgcRichFlatMapFunction())
                        .setParallelism(1)
                        .uid("DimGiftPostPgcRichFlatMapFunction")
                        .name("DimGiftPostPgcRichFlatMapFunction");

        BroadcastStream<DimGiftPostPgc> dimGiftPostPgcBroadcastStream =
                dimGiftPostPgcDataStream.broadcast(DIM_GIFT_POST_PGC_BROADCAST_DESC);

        dimGiftPostDataStream
                .connect(dimGiftPostPgcBroadcastStream)
                .process(new DimGiftPostBroadcastProcessFunction())
                .setParallelism(1)
                .uid("DimGiftPostBroadcastProcessFunction")
                .name("DimGiftPostBroadcastProcessFunction")
                .sinkTo(
                        new Es6BaseSink<>(
                                        new ElasticsearchEmitter<DimGiftPost>() {
                                            private ObjectMapper objectMapper;

                                            @Override
                                            public void open() throws Exception {
                                                objectMapper = new ObjectMapper();
                                            }

                                            @Override
                                            public void emit(
                                                    DimGiftPost dimGiftPost,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index(
                                                                            "lofter_dim_gift_post_v1")
                                                                    .type("_doc")
                                                                    .id(
                                                                            dimGiftPost
                                                                                    .getPostId()
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimGiftPost),
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
                .uid("DimGiftPostEsSink")
                .name("DimGiftPostEsSink");

        env.execute("DimGiftPostJob");
    }

    public static void main(String[] args) throws Exception {
        dimGiftPostJob(ParameterTool.fromArgs(args));
    }
}
