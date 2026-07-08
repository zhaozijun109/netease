package com.netease.wm.hubble.common

import org.apache.kafka.clients.producer.ProducerConfig

import java.util.Properties

object shuffleConfig {
  val srcTopic = "hubble.log.online"

  val kafkaProperties = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    props.setProperty("group.id", "hubble_shuffle")
    props.setProperty("auto.offset.reset", "earliest")
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty("compression.type", "snappy")
    props
  }

  val appKeyDispatchRules = Seq(
    "vc.mda.online" -> "MA-BA51-F8C059C0C618,MA-AA46-CB3D4835ED29",
    "vc.wap.online" -> "MA-BD70-EE13FDC8DB51"
  )

  val defaultDestTopic = "hubble.useless.online"

  val recSourceTopic = "lofter.mda.online"

  val recDestTopic = "rec_upload_action"

  val recSourceKafkaProperties = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    props.setProperty("group.id", "hubble_shuffle_rec")
    props.setProperty("auto.offset.reset", "latest")
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty("compression.type", "snappy")
    props
  }

  val recDestKafkaProperties = {
    val brokers = "lofter-kafka0.service.163.org:9093,lofter-kafka1.service.163.org:9093,lofter-kafka2.service.163.org:9093"
    val props = new Properties()
    props.setProperty("bootstrap.servers", brokers)
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")
    props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")
    props
  }
}
