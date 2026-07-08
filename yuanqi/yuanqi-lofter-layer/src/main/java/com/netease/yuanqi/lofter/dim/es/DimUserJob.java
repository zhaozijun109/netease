package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.dim.es.DimUserRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimUser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimUserJob {
    private static void dimUserJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //        StreamExecutionEnvironment env =
        //                StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new
        // Configuration());

        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "DimUserJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimUserJobSource")
                .setParallelism(1)
                .uid("DimUserJobSource")
                .name("DimUserJobSource")
                .flatMap(new DimUserRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimUserRichFlatMapFunction")
                .name("DimUserRichFlatMapFunction")
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
                                                DimUser dimUser =
                                                        DimUser.builder()
                                                                .setUserId(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get(
                                                                                                "UserID")
                                                                                        .toString()))
                                                                .setCreateTime(
                                                                        Math.min(
                                                                                binlogRow
                                                                                        .getOpTime(),
                                                                                Long.parseLong(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "ProfileCreateTime")
                                                                                                .toString())))
                                                                .setIsAnonymous(
                                                                        binlogRow
                                                                                                        .getData()
                                                                                                        .get(
                                                                                                                "Email")
                                                                                                        .toString()
                                                                                                != null
                                                                                        && binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "Email")
                                                                                                .toString()
                                                                                                .startsWith(
                                                                                                        "100#")
                                                                                ? 1
                                                                                : 0)
                                                                .build();

                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index("lofter_dim_user")
                                                                    .type("_doc")
                                                                    .id(
                                                                            binlogRow
                                                                                    .getData()
                                                                                    .get("UserID")
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimUser),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimUserEsSink")
                .name("DimUserEsSink");

        env.execute("DimUserJob");
    }

    public static void main(String[] args) throws Exception {
        dimUserJob(ParameterTool.fromArgs(args));
    }
}
