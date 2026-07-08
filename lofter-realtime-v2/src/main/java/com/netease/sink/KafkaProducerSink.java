package com.netease.sink;

import com.netease.util.KafkaSinkPartitioner;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import java.util.Properties;

/** Kafka producer. */
public class KafkaProducerSink {
    private final String bootstrapServers;
    private final String topic;
    private final Properties properties;

    public KafkaProducerSink(String bootstrapServers, String topic, Properties properties) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.properties = properties;
    }

    public KafkaSink<String> createKafkaProducerSink() {
        return KafkaSink.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setKafkaProducerConfig(properties)
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(topic)
                                .setPartitioner(new KafkaSinkPartitioner<>())
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    public KafkaSink<String> createKafkaProducerSinkWithTimestamp() {
        return KafkaSink.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setKafkaProducerConfig(properties)
                .setRecordSerializer(
                        new KafkaRecordSerializationSchema<String>() {
                            @Override
                            public ProducerRecord<byte[], byte[]> serialize(
                                    String element, KafkaSinkContext context, Long timestamp) {
                                String targetTopic = topic;
                                byte[] value = element.getBytes(StandardCharsets.UTF_8);
                                byte[] key = null;

                                OptionalInt partition =
                                        OptionalInt.of(
                                                new KafkaSinkPartitioner<String>()
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
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }
}
