package com.netease.yuanqi.common.sink.kafka;

import static com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils.getMdaLogTopicWithAppKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;
import com.netease.yuanqi.common.serialization.protobuf.schema.ClientMdaLogProtoSerializationSchema;
import com.netease.yuanqi.common.serialization.protobuf.schema.WapMdaLogProtoSerializationSchema;
import com.netease.yuanqi.common.serialization.protobuf.schema.WebMdaLogProtoSerializationSchema;
import com.netease.yuanqi.common.utils.kafka.KafkaBalanceSinkPartitioner;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.formats.avro.AvroSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaBaseSink implements Serializable {
    private static final long serialVersionUID = 1L;
    private final KafkaConfig kafkaConfig;

    public KafkaBaseSink(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    // ------------------------------------------------------------------------
    //  Mda protobuf sink
    // ------------------------------------------------------------------------
    @Experimental
    public KafkaSink<ClientMdaLogProtoBuilder.ClientMdaLogProto> createClientMdaLogProtobufSink() {
        return KafkaSink.<ClientMdaLogProtoBuilder.ClientMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        new ClientMdaLogProtoSerializationSchema())
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    @Experimental
    public KafkaSink<WapMdaLogProtoBuilder.WapMdaLogProto> createWapMdaLogProtobufSink() {
        return KafkaSink.<WapMdaLogProtoBuilder.WapMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        new WapMdaLogProtoSerializationSchema())
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    @Experimental
    public KafkaSink<WebMdaLogProtoBuilder.WebMdaLogProto> createWebMdaLogProtobufSink() {
        return KafkaSink.<WebMdaLogProtoBuilder.WebMdaLogProto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        new WebMdaLogProtoSerializationSchema())
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Mda avro sink
    // ------------------------------------------------------------------------
    public KafkaSink<ClientMdaLogAvro> createClientMdaLogAvroSink() {
        return KafkaSink.<ClientMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopicSelector(
                                        new TopicSelector<ClientMdaLogAvro>() {
                                            @Override
                                            public String apply(ClientMdaLogAvro clientMdaLogAvro) {
                                                return getMdaLogTopicWithAppKey(
                                                        clientMdaLogAvro.getAppKey().toString());
                                            }
                                        })
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        AvroSerializationSchema.forSpecific(ClientMdaLogAvro.class))
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    public KafkaSink<WapMdaLogAvro> createWapMdaLogAvroSink() {
        return KafkaSink.<WapMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopicSelector(
                                        new TopicSelector<WapMdaLogAvro>() {
                                            @Override
                                            public String apply(WapMdaLogAvro wapMdaLogAvro) {
                                                return getMdaLogTopicWithAppKey(
                                                        wapMdaLogAvro.getAppKey().toString());
                                            }
                                        })
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        AvroSerializationSchema.forSpecific(WapMdaLogAvro.class))
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    public KafkaSink<WebMdaLogAvro> createWebMdaLogAvroSink() {
        return KafkaSink.<WebMdaLogAvro>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopicSelector(
                                        new TopicSelector<WebMdaLogAvro>() {
                                            @Override
                                            public String apply(WebMdaLogAvro webMdaLogAvro) {
                                                return getMdaLogTopicWithAppKey(
                                                        webMdaLogAvro.getAppKey().toString());
                                            }
                                        })
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        AvroSerializationSchema.forSpecific(WebMdaLogAvro.class))
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    public <AVRO extends SpecificRecord> KafkaSink<AVRO> createCommonSpecificAvroSink(
            Class<AVRO> clazz) {
        return KafkaSink.<AVRO>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        AvroSerializationSchema.forSpecific(clazz))
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Mda multi topic sink
    // ------------------------------------------------------------------------
    public KafkaSink<Tuple2<String, String>> createMdaLogWithTopicSink() {
        return KafkaSink.<Tuple2<String, String>>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setTopicSelector(
                                        new TopicSelector<Tuple2<String, String>>() {
                                            @Override
                                            public String apply(Tuple2<String, String> tuple2) {
                                                return tuple2.f0;
                                            }
                                        })
                                .setValueSerializationSchema(
                                        new SerializationSchema<Tuple2<String, String>>() {
                                            @Override
                                            public byte[] serialize(Tuple2<String, String> tuple2) {
                                                return tuple2.f1.getBytes(StandardCharsets.UTF_8);
                                            }
                                        })
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Simple log sink
    // ------------------------------------------------------------------------
    public KafkaSink<String> createSimpleStringSinkWithTimestamp() {
        return KafkaSink.<String>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        new KafkaRecordSerializationSchema<String>() {
                            @Override
                            public ProducerRecord<byte[], byte[]> serialize(
                                    String element, KafkaSinkContext context, Long timestamp) {
                                String targetTopic = kafkaConfig.getTopic();
                                byte[] value = element.getBytes(StandardCharsets.UTF_8);
                                byte[] key = null;

                                OptionalInt partition =
                                        OptionalInt.of(
                                                new KafkaBalanceSinkPartitioner<String>()
                                                        .partition(
                                                                element,
                                                                key,
                                                                value,
                                                                targetTopic,
                                                                context.getPartitionsForTopic(
                                                                        targetTopic)));
                                return new ProducerRecord<>(
                                        targetTopic,
                                        partition.getAsInt(),
                                        timestamp != null && timestamp >= 0L
                                                ? timestamp
                                                : System.currentTimeMillis(),
                                        key,
                                        value);
                            }
                        })
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    public KafkaSink<String> createLogSink() {
        return KafkaSink.<String>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }

    // ------------------------------------------------------------------------
    //  Rec action log sink
    // ------------------------------------------------------------------------
    public KafkaSink<ActionDto> createRecActionDtoSink() {
        return KafkaSink.<ActionDto>builder()
                .setBootstrapServers(kafkaConfig.getBootstrapServers())
                .setKafkaProducerConfig(kafkaConfig.getProperties())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(kafkaConfig.getTopic())
                                .setPartitioner(new KafkaBalanceSinkPartitioner<>())
                                .setValueSerializationSchema(
                                        new SerializationSchema<ActionDto>() {
                                            private ObjectMapper objectMapper;

                                            @Override
                                            public void open(InitializationContext context)
                                                    throws Exception {
                                                objectMapper = new ObjectMapper();
                                            }

                                            @Override
                                            public byte[] serialize(ActionDto actionDto) {
                                                try {
                                                    return objectMapper
                                                            .writeValueAsString(actionDto)
                                                            .getBytes(StandardCharsets.UTF_8);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        })
                                .build())
                .setDeliveryGuarantee(kafkaConfig.getDeliveryGuarantee())
                .build();
    }
}
