package com.netease.wm.hubble.common

import java.util.Properties

import org.apache.kafka.clients.producer.ProducerConfig

object recConfig {
  val JDBC_URL = "jdbc:mysql://lofter-rds-common-recomment-online-jd-27164.rds.cn-east-p1.internal.:3331/recomment?useUnicode=true&characterEncoding=UTF-8&user=lofter_bi&password=mBESHdBRr"
  val DRIVER = "com.mysql.jdbc.Driver"
  val AB_CONFIG_JDBC_URL = "jdbc:mysql://lofter-rds-yaolu-abtest-online-jd-27148.rds.cn-east-p1.internal.:3331/yaolu_abtest?useUnicode=true&characterEncoding=utf-8&user=lofter_bi&password=hvuVkwUbn"

  val BOOTSTRAP_SERVERS_TEST = "lofter-redis9.jd.163.org:9092,lofter-redis9.jd.163.org:9093,lofter-redis9.jd.163.org:9094"
  val BOOTSTRAP_SERVERS_ONLINE = "lofter-kafka0.service.163.org:9093,lofter-kafka1.service.163.org:9093,lofter-kafka2.service.163.org:9093"
  val sourceTopic = "rec_upload_action"
  val recLogTopic = "rec_ds_rec_log"
  val destTopic = "rec_upload_action_parse"
  val blogOutputPath = "hdfs://hz-cluster10/user/rec/lofter_hive/ods_lofter_blog_action_di"
  val noBlogOutputPath = "hdfs://hz-cluster10/user/rec/lofter_hive/ods_lofter_noblog_action_di"
  val blogOutputPathTest = "hdfs://hz-cluster10/user/rec/lofter_hive_test/ods_lofter_blog_action_di"
  val noBlogOutputPathTest = "hdfs://hz-cluster10/user/rec/lofter_hive_test/ods_lofter_noblog_action_di"

  val blogRecOutputPath = "hdfs://hz-cluster10/user/rec/lofter_hive/ods_lofter_blog_rec_di"
  val noBlogRecOutputPath = "hdfs://hz-cluster10/user/rec/lofter_hive/ods_lofter_noblog_rec_di"
  val blogRecOutputPathTest = "hdfs://hz-cluster10/user/rec/lofter_hive_test/ods_lofter_blog_rec_di"
  val noBlogRecOutputPathTest = "hdfs://hz-cluster10/user/rec/lofter_hive_test/ods_lofter_noblog_rec_di"

  val recSourceKafkaProperties = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS_ONLINE)
    props.setProperty("group.id", "hubble_shuffle_rec")
    props.setProperty("auto.offset.reset", "earliest")
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty("compression.type", "snappy")
    props
  }

  val recDestKafkaProperties = {
    val brokers = BOOTSTRAP_SERVERS_ONLINE
    val props = new Properties()
    props.setProperty("bootstrap.servers", brokers)
    props.setProperty("flink.partition-discovery.interval-millis", "60000")
    props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")
    props
  }

}
