package com.netease.wm.hubble.common

import java.util.Properties

object vcKafkaConfig {
  val VC_MDA_KAFKA_SERVICES = "10.46.246.11:9092,10.46.246.13:9092,10.46.246.12:9092"

  val VCKafkaProperties = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", VC_MDA_KAFKA_SERVICES)
    props.setProperty("group.id", "vc_mda_online_prod01")
    props.setProperty("auto.offset.reset", "latest")
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty("compression.type", "snappy")
    props
  }
}
