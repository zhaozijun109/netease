package com.netease.yuanqi.common.sink.kafka;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import java.io.Serializable;
import java.util.Properties;
import org.apache.flink.connector.base.DeliveryGuarantee;

public class KafkaCommonSink extends KafkaBaseSink implements Serializable {
    private static final long serialVersionUID = 1L;

    public KafkaCommonSink() {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .build());
    }

    public KafkaCommonSink(Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .setProperties(properties)
                        .build());
    }

    public KafkaCommonSink(DeliveryGuarantee deliveryGuarantee, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .setDeliveryGuarantee(deliveryGuarantee)
                        .setProperties(properties)
                        .build());
    }

    public KafkaCommonSink(String topic) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .setTopic(topic)
                        .build());
    }

    public KafkaCommonSink(String topic, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .setTopic(topic)
                        .setProperties(properties)
                        .build());
    }

    public KafkaCommonSink(
            String topic, DeliveryGuarantee deliveryGuarantee, Properties properties) {
        super(
                KafkaConfig.builder()
                        .setBootstrapServers(
                                ClusterConfigOptions.getKafkaBootStrapServers(
                                        ClusterConfigOptions.KafkaBootstrapServersEnum.COMMON))
                        .setTopic(topic)
                        .setDeliveryGuarantee(deliveryGuarantee)
                        .setProperties(properties)
                        .build());
    }
}
