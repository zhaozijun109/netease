package com.netease.yuanqi.common.source.kafka;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import java.io.Serializable;
import java.util.Properties;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaRecSource extends KafkaBaseSource implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaRecSource.class);
    private static final long serialVersionUID = 1L;
    private final KafkaConfig kafkaConfig;

    public KafkaRecSource(String topics, String groupId) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .build();
    }

    public KafkaRecSource(String topics, String groupId, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .setProperties(properties)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .setProperties(properties)
                        .build();
    }

    public KafkaRecSource(
            String topics,
            String groupId,
            OffsetsInitializer offsetsInitializer,
            Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .setStartingOffsetsInitializer(offsetsInitializer)
                        .setProperties(properties)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopics(topics)
                        .setGroupId(groupId)
                        .setStartingOffsetsInitializer(offsetsInitializer)
                        .setProperties(properties)
                        .build();
    }
}
