package com.netease.source;

import com.netease.dts.common.subscribe.SubscribeEvent;
import com.netease.util.BinlogSubscribeEventSchemaUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

import java.util.Properties;

/** Kafka consumer. */
public class KafkaConsumerSource {
    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final Properties properties;

    public KafkaConsumerSource(
            String bootstrapServers, String topic, String groupId, Properties properties) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.properties = properties;
    }

    public KafkaSource<SubscribeEvent> createBinlogNdcSource() {
        return KafkaSource.<SubscribeEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new BinlogSubscribeEventSchemaUtils())
                .setProperties(properties)
                .build();
    }

    public KafkaSource<String> createLatestKafkaSource() {
        return KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperties(properties)
                .build();
    }

    public KafkaSource<String> createEarliestKafkaSource() {
        return KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperties(properties)
                .build();
    }
}
