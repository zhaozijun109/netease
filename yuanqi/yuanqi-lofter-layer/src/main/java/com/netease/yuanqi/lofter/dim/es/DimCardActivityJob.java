package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.dim.es.DimCardActivityRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimCardActivity;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimCardActivityJob {
    private static void dimCardActivityJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "DimCardActivityJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimCardActivitySource")
                .setParallelism(1)
                .uid("DimCardActivityKafkaSource")
                .name("DimCardActivityKafkaSource")
                .flatMap(new DimCardActivityRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimCardActivityRichFlatMapFunction")
                .name("DimCardActivityRichFlatMapFunction")
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
                                                DimCardActivity dimCardActivity =
                                                        DimCardActivity.builder()
                                                                .setActivityId(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get("id")
                                                                                        .toString()))
                                                                .setActivityCode(
                                                                        binlogRow
                                                                                .getData()
                                                                                .get("code")
                                                                                .toString())
                                                                .build();
                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index(
                                                                            "lofter_dim_card_activity")
                                                                    .type("_doc")
                                                                    .id(
                                                                            binlogRow
                                                                                    .getData()
                                                                                    .get("code")
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimCardActivity),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimCardActivityEsSink")
                .name("DimCardActivityEsSink");

        env.execute("DimCardActivityJob");
    }

    public static void main(String[] args) throws Exception {
        dimCardActivityJob(ParameterTool.fromArgs(args));
    }
}
