package com.netease.yuanqi.unified.dim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.dim.DimRecBuriedPointRichFlatMapFunction;
import com.netease.yuanqi.unified.pojo.DimRecBuriedPoint;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimRecBuriedPointJob {
    private static void dimRecBuriedPointJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "DimRecBuriedPointJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimRecBuriedPointSource")
                .setParallelism(1)
                .uid("DimRecBuriedPointKafkaSource")
                .name("DimRecBuriedPointKafkaSource")
                .flatMap(new DimRecBuriedPointRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimRecBuriedPointRichFlatMapFunction")
                .name("DimRecBuriedPointRichFlatMapFunction")
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
                                                DimRecBuriedPoint dimRecBuriedPoint =
                                                        DimRecBuriedPoint.builder()
                                                                .setId(
                                                                        Long.valueOf(
                                                                                String.valueOf(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "id"))))
                                                                .setBusinessName(
                                                                        String.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "businessName")))
                                                                .setDataSource(
                                                                        String.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "dataSource")))
                                                                .setEventId(
                                                                        String.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "eventId")))
                                                                .setAppVersion(
                                                                        String.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "appVersion")))
                                                                .setActionCode(
                                                                        Integer.valueOf(
                                                                                String.valueOf(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "actionCode"))))
                                                                .setEnable(
                                                                        Integer.valueOf(
                                                                                String.valueOf(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "enable"))))
                                                                .build();

                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index("dim_rec_buried_point")
                                                                    .id(
                                                                            String.valueOf(
                                                                                    binlogRow
                                                                                            .getData()
                                                                                            .get(
                                                                                                    "id")))
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimRecBuriedPoint),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimRecBuriedPointEsSink")
                .name("DimRecBuriedPointEsSink");

        env.execute("DimRecBuriedPointJob");
    }

    public static void main(String[] args) throws Exception {
        dimRecBuriedPointJob(ParameterTool.fromArgs(args));
    }
}
