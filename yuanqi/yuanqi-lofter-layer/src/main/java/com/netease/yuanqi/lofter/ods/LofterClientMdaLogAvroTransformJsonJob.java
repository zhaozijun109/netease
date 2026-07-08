package com.netease.yuanqi.lofter.ods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.AvroJsonUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.avro.Schema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class LofterClientMdaLogAvroTransformJsonJob {
    private static final Logger LOG =
            LoggerFactory.getLogger(LofterClientMdaLogAvroTransformJsonJob.class);

    private static void lofterClientMdaLogAvroTransformJsonJob(ParameterTool params)
            throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);
        env.getConfig().enableObjectReuse();

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "120000"); // default 5min -> 2min
        properties.setProperty("compression.type", "lz4");

        env.fromSource(
                        new KafkaCommonSource(
                                        "lofter.mda.online",
                                        "LofterClientMdaLogAvroTransformJsonJob")
                                .createClientMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "LofterClientMdaLogAvroSource")
                .setParallelism(32)
                .uid("LofterClientMdaLogAvroKafkaSource")
                .name("LofterClientMdaLogAvroKafkaSource")
                .map(
                        new RichMapFunction<ClientMdaLogAvro, String>() {
                            private ObjectMapper objectMapper;
                            private Set<String> schemaSet;
                            private List<Schema.Field> fields;
                            private ObjectNode eventNode;
                            private ObjectNode attributesNode;

                            @Override
                            public void open(Configuration parameters) throws Exception {
                                objectMapper = new ObjectMapper();
                                schemaSet =
                                        new HashSet<>(
                                                Arrays.asList(
                                                        "source",
                                                        "deviceAndroidId",
                                                        "itemId",
                                                        "itemType",
                                                        "recId",
                                                        "scene",
                                                        "action",
                                                        "tagName",
                                                        "layout",
                                                        "algInfo",
                                                        "params"));
                                fields = ClientMdaLogAvro.getClassSchema().getFields();
                                eventNode = objectMapper.createObjectNode();
                                attributesNode = objectMapper.createObjectNode();
                            }

                            @Override
                            public String map(ClientMdaLogAvro clientMdaLogAvro) throws Exception {
                                eventNode.removeAll();
                                attributesNode.removeAll();
                                for (Schema.Field field : fields) {
                                    if (schemaSet.contains(field.name())) {
                                        if ("params".equals(field.name())) {
                                            for (Map.Entry<CharSequence, CharSequence> keys :
                                                    clientMdaLogAvro.getParams().entrySet()) {
                                                attributesNode.set(
                                                        keys.getKey().toString(),
                                                        AvroJsonUtils.toJsonNode(keys.getValue()));
                                            }
                                        } else {
                                            attributesNode.set(
                                                    field.name(),
                                                    AvroJsonUtils.toJsonNode(
                                                            clientMdaLogAvro.get(field.pos())));
                                        }
                                    } else {
                                        eventNode.set(
                                                field.name(),
                                                AvroJsonUtils.toJsonNode(
                                                        clientMdaLogAvro.get(field.pos())));
                                    }
                                }
                                eventNode.set("attributes", attributesNode);
                                return objectMapper.writeValueAsString(eventNode);
                            }
                        })
                .setParallelism(32)
                .uid("LofterClientMdaLogAvroTransformJsonMapFunction")
                .name("LofterClientMdaLogAvroTransformJsonMapFunction")
                .disableChaining()
                .sinkTo(new KafkaCommonSink("lofter.mda.online.json", properties).createLogSink())
                .setParallelism(32)
                .uid("LofterClientMdaLogAvroTransformJsonKafkaSink")
                .name("LofterClientMdaLogAvroTransformJsonKafkaSink");

        env.execute("Lofter Client Mda Log Avro Transform Json Job");
    }

    public static void main(String[] args) throws Exception {
        lofterClientMdaLogAvroTransformJsonJob(ParameterTool.fromArgs(args));
    }
}
