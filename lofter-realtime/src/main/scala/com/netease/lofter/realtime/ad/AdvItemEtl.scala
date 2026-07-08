package com.netease.lofter.realtime.ad

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.lofter.realtime.common.kafkaConfig
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

import java.util.Properties


object AdvItemEtl {
  val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val source = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter_ad_log")
      .setGroupId("lofter_ad_log_etl")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val sinkKafkaProperties = {
      val props = new Properties()
      props.setProperty("compression.type", "snappy")
      props
    }

    val advSink = KafkaSink.builder[String]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setKafkaProducerConfig(sinkKafkaProperties)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("AD.DSP_MONITOR_APP")
          .setValueSerializationSchema(new SimpleStringSchema())
          .build()
      ).build()

    env.fromSource(source, WatermarkStrategy.noWatermarks(), "mda")
      .flatMap {
        v => parseAdvEvent(v)
      }.sinkTo(advSink)

    env.execute("lofter adv event etl job")
  }

  def parseAdvEvent(s: String): Option[String] = {
    val root = objectMapper.readTree(s)
    val eventId = Option(root.get("eventId")).map(_.asText("")).getOrElse("")
    val params = root.get("attributes")
    val itemTypeNode = params.get("itemType")
    val itemType = Option(itemTypeNode).map(_.asText())
    itemType match {
      case Some("ADV") => Some(s)
      case _ if eventId.startsWith("ad-") => Some(s)
      case _ => None
    }
  }
}
