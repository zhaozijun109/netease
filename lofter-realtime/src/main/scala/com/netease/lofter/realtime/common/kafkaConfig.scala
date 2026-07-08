package com.netease.lofter.realtime.common

object kafkaConfig {
  val NDC_TOPIC = "lofter.binlog.ndc"
  val BOOTSTRAP_SERVERS = "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092"
  val RISK_BOOTSTRAP_SERVERS = "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092,lofter-kafka-bi-risk4.gy.ntes:9092,lofter-kafka-bi-risk5.gy.ntes:9092,lofter-kafka-bi-risk6.gy.ntes:9092,lofter-kafka-bi-risk7.gy.ntes:9092,lofter-kafka-bi-risk8.gy.ntes:9092"
  val BACKEND_BOOTSTRAP_SERVERS = "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092"

  val GY_PUSH_BOOTSTRAP_SERVERS = "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092"
  val GY_BOOTSTRAP_SERVERS = "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092"

  val MUSIC_BOOTSTRAP_SERVERS= "10.48.2.16:9092,10.48.2.17:9092,10.48.2.18:9092,10.48.2.19:9092,10.48.2.20:9092,10.48.2.21:9092,10.48.2.22:9092,10.48.2.23:9092,10.48.2.24:9092,10.48.2.25:9092,10.48.2.26:9092,10.48.2.27:9092,10.48.2.28:9092,10.48.2.29:9092,10.48.2.30:9092"
}
