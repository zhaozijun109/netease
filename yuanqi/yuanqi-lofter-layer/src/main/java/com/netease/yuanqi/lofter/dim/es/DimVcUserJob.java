package com.netease.yuanqi.lofter.dim.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.dim.es.DimVcUserRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimVcUser;
import java.util.Objects;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class DimVcUserJob {
    private static void dimVcUserJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //        StreamExecutionEnvironment env =
        //                StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new
        // Configuration());

        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("vc.binlog.online", "DimVcUserJob").createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimVcUserJobSource")
                .setParallelism(1)
                .uid("DimVcUserJobSource")
                .name("DimVcUserJobSource")
                .flatMap(new DimVcUserRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimVcUserRichFlatMapFunction")
                .name("DimVcUserRichFlatMapFunction")
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
                                                DimVcUser dimVcUser =
                                                        DimVcUser.builder()
                                                                .setUserId(
                                                                        Long.valueOf(
                                                                                binlogRow
                                                                                        .getData()
                                                                                        .get("id")
                                                                                        .toString()))
                                                                .setCreateTime(
                                                                        Math.min(
                                                                                binlogRow
                                                                                        .getOpTime(),
                                                                                Long.parseLong(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "create_time")
                                                                                                .toString())))
                                                                .setIsAnonymous(
                                                                        Objects.equals(
                                                                                        binlogRow
                                                                                                .getData()
                                                                                                .get(
                                                                                                        "account_type")
                                                                                                .toString(),
                                                                                        "2")
                                                                                ? 1
                                                                                : 0)
                                                                .build();
                                                try {
                                                    IndexRequest indexRequest =
                                                            Requests.indexRequest()
                                                                    .index("vc_dim_user")
                                                                    .type("_doc")
                                                                    .id(
                                                                            binlogRow
                                                                                    .getData()
                                                                                    .get("id")
                                                                                    .toString())
                                                                    .source(
                                                                            objectMapper
                                                                                    .writeValueAsString(
                                                                                            dimVcUser),
                                                                            XContentType.JSON);
                                                    requestIndexer.add(indexRequest);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimVcUserEsSink")
                .name("DimVcUserEsSink");

        env.execute("DimVcUserJob");
    }

    public static void main(String[] args) throws Exception {
        dimVcUserJob(ParameterTool.fromArgs(args));
    }
}
