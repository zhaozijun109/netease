package com.netease.lofter.data.common

object kafkaConfig {
  val ENABLE_OUTPUT: Boolean = true

  val BOOTSTRAP_SERVERS = "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092"

  val BOOTSTRAP_SERVERS_BACKEND = "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092"
}
