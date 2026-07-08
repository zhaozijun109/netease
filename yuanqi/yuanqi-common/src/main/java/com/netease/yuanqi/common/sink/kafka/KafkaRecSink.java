package com.netease.yuanqi.common.sink.kafka;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import java.io.Serializable;
import java.util.Properties;
import org.apache.flink.connector.base.DeliveryGuarantee;

public class KafkaRecSink extends KafkaBaseSink implements Serializable {
    private static final long serialVersionUID = 1L;
    private final KafkaConfig kafkaConfig;

    public KafkaRecSink(String topic) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .build();
    }

    public KafkaRecSink(String topic, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .setProperties(properties)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .setProperties(properties)
                        .build();
    }

    public KafkaRecSink(String topic, DeliveryGuarantee deliveryGuarantee, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .setDeliveryGuarantee(deliveryGuarantee)
                        .setProperties(properties)
                        .build());
        this.kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum
                                                .LOFTER_RECOMMEND))
                        .setTopic(topic)
                        .setDeliveryGuarantee(deliveryGuarantee)
                        .setProperties(properties)
                        .build();
    }
}
