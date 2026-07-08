package com.netease.yuanqi.common.pojo.config;

import com.netease.yuanqi.common.utils.Preconditions;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class KafkaConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bootstrapServers;
    private final String topic;
    private final List<String> topics;
    private final String groupId;
    private final OffsetsInitializer startingOffsetsInitializer; // source
    private final DeliveryGuarantee deliveryGuarantee; // sink
    private final Properties properties;

    public KafkaConfig(
            String bootstrapServers,
            String topic,
            List<String> topics,
            String groupId,
            OffsetsInitializer startingOffsetsInitializer,
            DeliveryGuarantee deliveryGuarantee,
            Properties properties) {
        this.bootstrapServers = Preconditions.checkNotNull(bootstrapServers);
        this.topic = topic;
        this.topics = topics;
        this.groupId = groupId;
        this.startingOffsetsInitializer =
                startingOffsetsInitializer == null
                        ? OffsetsInitializer.latest()
                        : startingOffsetsInitializer;
        this.deliveryGuarantee =
                deliveryGuarantee == null ? DeliveryGuarantee.AT_LEAST_ONCE : deliveryGuarantee;
        this.properties = properties == null ? new Properties() : properties;
        if (!this.properties.containsKey("partition.discovery.interval.ms")) {
            // default 5min period partition discovery
            this.properties.setProperty("partition.discovery.interval.ms", "300000");
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getGroupId() {
        return groupId;
    }

    public OffsetsInitializer getStartingOffsetsInitializer() {
        return startingOffsetsInitializer;
    }

    public DeliveryGuarantee getDeliveryGuarantee() {
        return deliveryGuarantee;
    }

    public Properties getProperties() {
        return properties;
    }

    public static KafkaConfigBuilder builder() {
        return new KafkaConfigBuilder();
    }

    public static class KafkaConfigBuilder {
        private String bootstrapServers;
        private String topic;
        private List<String> topics;
        private String groupId;
        private OffsetsInitializer startingOffsetsInitializer;
        private DeliveryGuarantee deliveryGuarantee;
        private Properties properties;

        public KafkaConfigBuilder() {}

        public KafkaConfigBuilder setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
            return this;
        }

        public KafkaConfigBuilder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public KafkaConfigBuilder setTopics(List<String> topics) {
            this.topics = topics;
            return this;
        }

        public KafkaConfigBuilder setTopics(String topics) {
            return this.setTopics(Arrays.asList(topics.replaceAll(" ", "").split(",")));
        }

        public KafkaConfigBuilder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public KafkaConfigBuilder setStartingOffsetsInitializer(
                OffsetsInitializer startingOffsetsInitializer) {
            this.startingOffsetsInitializer = startingOffsetsInitializer;
            return this;
        }

        public KafkaConfigBuilder setDeliveryGuarantee(DeliveryGuarantee deliveryGuarantee) {
            this.deliveryGuarantee = deliveryGuarantee;
            return this;
        }

        public KafkaConfigBuilder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public KafkaConfig build() {
            return new KafkaConfig(
                    bootstrapServers,
                    topic,
                    topics,
                    groupId,
                    startingOffsetsInitializer,
                    deliveryGuarantee,
                    properties);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"bootstrapServers\":\""
                + bootstrapServers
                + '\"'
                + ",\"topic\":\""
                + topic
                + '\"'
                + ",\"topics\":"
                + topics
                + ",\"groupId\":\""
                + groupId
                + '\"'
                + ",\"startingOffsetsInitializer\":"
                + startingOffsetsInitializer
                + ",\"deliveryGuarantee\":"
                + deliveryGuarantee
                + ",\"properties\":"
                + properties
                + "}";
    }
}
