package com.netease.yuanqi.common.source.kafka;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.dts.common.subscribe.SubscribeEvent;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;
import com.netease.yuanqi.common.serialization.protobuf.schema.ClientMdaLogProtoDeserializationSchema;
import com.netease.yuanqi.common.serialization.protobuf.schema.WapMdaLogProtoDeserializationSchema;
import com.netease.yuanqi.common.serialization.protobuf.schema.WebMdaLogProtoDeserializationSchema;
import com.netease.yuanqi.common.utils.kafka.DsLogArchiveFormatUtils;
import com.netease.yuanqi.common.utils.kafka.LofterNdcBinlogSchema;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.basic.music.feature.parse.handler.MusicDumpMessageHandler;

public class KafkaBaseSource implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaBaseSource.class);
    private static final long serialVersionUID = 1L;
    private final KafkaConfig kafkaConfig;

    public KafkaBaseSource(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    // ------------------------------------------------------------------------
    //  Mda protobuf source
    // ------------------------------------------------------------------------
    @Experimental
    public KafkaSource<ClientMdaLogProtoBuilder.ClientMdaLogProto>
            createClientMdaLogProtobufSource() {
        return KafkaSource.<ClientMdaLogProtoBuilder.ClientMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(new ClientMdaLogProtoDeserializationSchema())
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    @Experimental
    public KafkaSource<WapMdaLogProtoBuilder.WapMdaLogProto> createWapMdaLogProtobufSource() {
        return KafkaSource.<WapMdaLogProtoBuilder.WapMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(new WapMdaLogProtoDeserializationSchema())
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    @Experimental
    public KafkaSource<WebMdaLogProtoBuilder.WebMdaLogProto> createWebMdaLogProtobufSource() {
        return KafkaSource.<WebMdaLogProtoBuilder.WebMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(new WebMdaLogProtoDeserializationSchema())
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Mda avro source
    // ------------------------------------------------------------------------
    public KafkaSource<ClientMdaLogAvro> createClientMdaLogAvroSource() {
        return KafkaSource.<ClientMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(
                        AvroDeserializationSchema.forSpecific(ClientMdaLogAvro.class))
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public KafkaSource<WapMdaLogAvro> createWapMdaLogAvroSource() {
        return KafkaSource.<WapMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(
                        AvroDeserializationSchema.forSpecific(WapMdaLogAvro.class))
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public KafkaSource<WebMdaLogAvro> createWebMdaLogAvroSource() {
        return KafkaSource.<WebMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(
                        AvroDeserializationSchema.forSpecific(WebMdaLogAvro.class))
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public <AVRO extends SpecificRecord> KafkaSource<AVRO> createCommonSpecificAvroSource(
            Class<AVRO> clazz) {
        return KafkaSource.<AVRO>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(AvroDeserializationSchema.forSpecific(clazz))
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Ndc binlog source
    // ------------------------------------------------------------------------
    public KafkaSource<SubscribeEvent> createNdcBinlogSource() {
        return KafkaSource.<SubscribeEvent>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(new LofterNdcBinlogSchema())
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Simple log source
    // ------------------------------------------------------------------------
    public KafkaSource<String> createLogSource() {
        return KafkaSource.<String>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    /**
     * Creates a Kafka source that extracts metadata from ConsumerRecord. Returns Tuple3(topic,
     * kafkaTime, jsonData).
     */
    public KafkaSource<Tuple3<String, Long, String>> createLogSourceWithMetadata() {
        return KafkaSource.<Tuple3<String, Long, String>>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setDeserializer(
                        new KafkaRecordDeserializationSchema<Tuple3<String, Long, String>>() {
                            @Override
                            public void deserialize(
                                    ConsumerRecord<byte[], byte[]> record,
                                    Collector<Tuple3<String, Long, String>> out) {
                                String topic = record.topic();
                                long kafkaTime = record.timestamp();
                                String data = new String(record.value(), StandardCharsets.UTF_8);
                                out.collect(Tuple3.of(topic, kafkaTime, data));
                            }

                            @Override
                            public TypeInformation<Tuple3<String, Long, String>> getProducedType() {
                                return TypeInformation.of(
                                        new TypeHint<Tuple3<String, Long, String>>() {});
                            }
                        })
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public KafkaSource<ArchiveFormatRow> createMdaArchiveFormatRowSource() {
        return KafkaSource.<ArchiveFormatRow>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setDeserializer(
                        new KafkaRecordDeserializationSchema<ArchiveFormatRow>() {
                            ObjectMapper objectMapper;

                            @Override
                            public void open(DeserializationSchema.InitializationContext context)
                                    throws Exception {
                                objectMapper = new ObjectMapper();
                                objectMapper.configure(
                                        JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                                        true);
                            }

                            @Override
                            public void deserialize(
                                    ConsumerRecord<byte[], byte[]> consumerRecord,
                                    Collector<ArchiveFormatRow> collector)
                                    throws IOException {
                                try {
                                    String content =
                                            "adx.newlinkup.online".equals(consumerRecord.topic())
                                                            || "lofter.outerlinkup.online"
                                                                    .equals(consumerRecord.topic())
                                                    ? new String(
                                                                    consumerRecord.value(),
                                                                    StandardCharsets.UTF_8)
                                                            .split(" - ")[1]
                                                    : new String(
                                                            consumerRecord.value(),
                                                            StandardCharsets.UTF_8);
                                    JsonNode jsonNode = objectMapper.readTree(content);
                                    Long archiveTime =
                                            jsonNode.has("kafkaTime")
                                                    ? jsonNode.get("kafkaTime").asLong()
                                                    : consumerRecord.timestamp();
                                    ArchiveFormatRow archiveFormatRow =
                                            ArchiveFormatRow.builder()
                                                    .setArchiveDir(consumerRecord.topic())
                                                    .setArchiveTime(archiveTime)
                                                    .setData(content)
                                                    .build();
                                    collector.collect(archiveFormatRow);
                                } catch (JsonProcessingException
                                        | ArrayIndexOutOfBoundsException e) {
                                    LOG.warn(
                                            "Deserializer failed to parse record: {}",
                                            Arrays.toString(consumerRecord.value()),
                                            e);
                                }
                            }

                            @Override
                            public TypeInformation<ArchiveFormatRow> getProducedType() {
                                return TypeInformation.of(ArchiveFormatRow.class);
                            }
                        })
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public KafkaSource<ArchiveFormatRow> createDataStreamArchiveFormatRowSource() {
        return KafkaSource.<ArchiveFormatRow>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setDeserializer(
                        new KafkaRecordDeserializationSchema<ArchiveFormatRow>() {
                            ObjectMapper objectMapper;
                            Pattern logPattern;

                            @Override
                            public void open(DeserializationSchema.InitializationContext context)
                                    throws Exception {
                                objectMapper = new ObjectMapper();
                                objectMapper.configure(
                                        JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                                        true);
                                logPattern = Pattern.compile("^[^\\{]+(\\{.*\\})\\s*$");
                            }

                            @Override
                            public void deserialize(
                                    ConsumerRecord<byte[], byte[]> consumerRecord,
                                    Collector<ArchiveFormatRow> collector)
                                    throws IOException {
                                String content =
                                        new String(consumerRecord.value(), StandardCharsets.UTF_8);
                                String topic = consumerRecord.topic();
                                Long archiveTime = consumerRecord.timestamp();

                                // Ds json log
                                if (DsLogArchiveFormatUtils.DS_JSON_LOG_TOPIC_SET.contains(topic)) {
                                    JsonNode jsonNode = objectMapper.readTree(content);
                                    if (jsonNode.has("body")) {
                                        Matcher matcher =
                                                logPattern.matcher(jsonNode.get("body").asText());
                                        if (matcher.matches()) {
                                            ArchiveFormatRow archiveFormatRow =
                                                    ArchiveFormatRow.builder()
                                                            .setArchiveDir(topic)
                                                            .setArchiveTime(archiveTime)
                                                            .setData(jsonNode.get("body").asText())
                                                            .build();
                                            collector.collect(archiveFormatRow);
                                        }
                                    }
                                } else if (DsLogArchiveFormatUtils.DS_NO_HEAD_JSON_LOG_TOPIC_SET
                                        .contains(topic)) { // Ds no head json log
                                    Matcher matcher = logPattern.matcher(content);
                                    if (matcher.matches()) {
                                        ArchiveFormatRow archiveFormatRow =
                                                ArchiveFormatRow.builder()
                                                        .setArchiveDir(topic)
                                                        .setArchiveTime(archiveTime)
                                                        .setData(content)
                                                        .build();
                                        collector.collect(archiveFormatRow);
                                    }
                                } else if (DsLogArchiveFormatUtils.DS_LOG_TOPIC_SET.contains(
                                        topic)) { // Ds log
                                    ArchiveFormatRow archiveFormatRow =
                                            ArchiveFormatRow.builder()
                                                    .setArchiveDir(topic)
                                                    .setArchiveTime(archiveTime)
                                                    .setData(
                                                            content.replaceAll(
                                                                    "\\r\\n|\\n", "\\\\n"))
                                                    .build();
                                    collector.collect(archiveFormatRow);
                                } else {
                                    ArchiveFormatRow archiveFormatRow =
                                            ArchiveFormatRow.builder()
                                                    .setArchiveDir(topic)
                                                    .setArchiveTime(archiveTime)
                                                    .setData(content)
                                                    .build();
                                    collector.collect(archiveFormatRow);
                                }
                            }

                            @Override
                            public TypeInformation<ArchiveFormatRow> getProducedType() {
                                return TypeInformation.of(ArchiveFormatRow.class);
                            }
                        })
                .setProperties(kafkaConfig.getProperties())
                .build();
    }

    public KafkaSource<ArchiveFormatRow> createRecMusicFeatureArchiveFormatRowSource(
            String archiveDir) {
        return KafkaSource.<ArchiveFormatRow>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setTopics(kafkaConfig.getTopics())
                .setGroupId(kafkaConfig.getGroupId())
                .setStartingOffsets(kafkaConfig.getStartingOffsetsInitializer())
                .setDeserializer(
                        new KafkaRecordDeserializationSchema<ArchiveFormatRow>() {
                            @Override
                            public void deserialize(
                                    ConsumerRecord<byte[], byte[]> consumerRecord,
                                    Collector<ArchiveFormatRow> collector)
                                    throws IOException {
                                collector.collect(
                                        ArchiveFormatRow.builder()
                                                .setArchiveDir(archiveDir)
                                                .setArchiveTime(consumerRecord.timestamp())
                                                .setData(
                                                        MusicDumpMessageHandler.parseMsg(
                                                                consumerRecord.value()))
                                                .build());
                            }

                            @Override
                            public TypeInformation<ArchiveFormatRow> getProducedType() {
                                return TypeInformation.of(ArchiveFormatRow.class);
                            }
                        })
                .setProperties(kafkaConfig.getProperties())
                .build();
    }
}
