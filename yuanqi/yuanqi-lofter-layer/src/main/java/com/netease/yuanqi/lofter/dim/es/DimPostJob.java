package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.dim.es.DimPostRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimPost;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimPostJob {
    private static void dimPostJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "DimPostJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimPostSource")
                .setParallelism(1)
                .uid("DimPostKafkaSource")
                .name("DimPostKafkaSource")
                .flatMap(new DimPostRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimPostRichFlatMapFunction")
                .name("DimPostRichFlatMapFunction")
                .sinkTo(
                        new EsCommonSink()
                                .createCommonBinlogEsSink(
                                        new ElasticsearchEmitter<BinlogRow>() {
                                            private ObjectMapper objectMapper;

                                            @Override
                                            public void open() throws Exception {
                                                objectMapper = new ObjectMapper();
                                            }

                                            @Override
                                            public void emit(
                                                    BinlogRow binlogRow,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                DimPost dimPost =
                                                        DimPost.builder()
                                                                .setPostId(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get("ID")
                                                                                        .toString()))
                                                                .setBlogId(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "BlogID")
                                                                                        .toString()))
                                                                .setPublishTime(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "PublishTime")
                                                                                        .toString()))
                                                                .build();

                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index("lofter_dim_post")
                                                                    .type("_doc")
                                                                    .id(
                                                                            binlogRow
                                                                                    .getData()
                                                                                    .get("ID")
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimPost),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimPostEsSink")
                .name("DimPostEsSink");

        env.execute("DimPostJob");
    }

    public static void main(String[] args) throws Exception {
        dimPostJob(ParameterTool.fromArgs(args));
    }
}
