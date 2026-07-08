package com.netease.wm.hubble.common

import org.apache.kafka.clients.producer.ProducerConfig

import java.util.Properties

object shuffleConfig {
  val recSourceTopic = "lofter.mda.online.json"
  val recDestTopic = "rec_upload_action_parse"
  val vc2recActionTopic = "vc_action_parse"

  val recSourceKafkaProperties = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    // props.setProperty("group.id", "hubble_shuffle_rec")
    // props.setProperty("auto.offset.reset", "latest")
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty("compression.type", "snappy")
    props
  }

  val recDestKafkaProperties = {
    val brokers = "lofter-kafka-recommend1.gy.ntes:9092,lofter-kafka-recommend2.gy.ntes:9092,lofter-kafka-recommend3.gy.ntes:9092"
    val props = new Properties()
    props.setProperty("bootstrap.servers", brokers)
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")
    props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")
    props
  }
}
