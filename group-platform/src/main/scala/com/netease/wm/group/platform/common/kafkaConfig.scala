package com.netease.wm.group.platform.common

object kafkaConfig {
  val BOOTSTRAP_SERVERS = "lofter-kafka3.service.163.org:9092,lofter-kafka4.service.163.org:9092,lofter-kafka5.service.163.org:9092"
  val BOOTSTRAP_SERVERS_BACKEND = "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092"
  val REC_BOOTSTRAP_SERVERS_ONLINE = "lofter-kafka0.service.163.org:9093,lofter-kafka1.service.163.org:9093,lofter-kafka2.service.163.org:9093"
  val BOOTSTRAP_SERVERS_TEST = "lofter-redis9.jd.163.org:9092,lofter-redis9.jd.163.org:9093,lofter-redis9.jd.163.org:9094"
}
