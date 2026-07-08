package com.netease.yuanqi.common.sink.kafka;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import java.io.Serializable;

public class KafkaYcyBinlogSink extends KafkaBaseSink implements Serializable {
    private static final long serialVersionUID = 1L;
    private final KafkaConfig kafkaConfig;

    public KafkaYcyBinlogSink() {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setProperties(
                                ClusterConfigOptions.getKafkaSecurityProperty(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setTopic("a13_avg_data_binlog_gy_dump")
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setProperties(
                                ClusterConfigOptions.getKafkaSecurityProperty(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setTopic("a13_avg_data_binlog_gy_dump")
                        .build();
    }

    public KafkaYcyBinlogSink(String topic) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setProperties(
                                ClusterConfigOptions.getKafkaSecurityProperty(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setTopic(topic)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setProperties(
                                ClusterConfigOptions.getKafkaSecurityProperty(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .YCY_GUIANSERVER))
                        .setTopic(topic)
                        .build();
    }
}
