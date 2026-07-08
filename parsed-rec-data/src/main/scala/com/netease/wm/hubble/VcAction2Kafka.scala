package com.netease.wm.hubble

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.lofter.rs.basic.bean.dto.upload.ActionDto
import com.netease.wm.hubble.common.{recConfig, shuffleConfig, vcKafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.{SerializationSchema, SimpleStringSchema}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}
import rs.basic.upload.parse.handler.{ActionMessageHandler, VcMessageHandler}

private class VcAction2Kafka{}
/**
 * vc行为数据转发
 * 1. 数据源kafka server：10.46.246.11:9092,10.46.246.13:9092,10.46.246.12:9092
 *          topic:
 * 2. 目标源：kafka server：lofter-kafka0.service.163.org:9093,lofter-kafka1.service.163.org:9093,lofter-kafka2.service.163.org:9093
 *          Topic：vc_action_parse
 */
object VcAction2Kafka {
  val MAX_LOG_SIZE = 700000
  val objectMapper = new ObjectMapper()
  val gson = new Gson()

  val LOG: Logger = LoggerFactory.getLogger(classOf[VcAction2Kafka])


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val kafkaSource = KafkaSource.builder[String]
      .setBootstrapServers(vcKafkaConfig.VC_MDA_KAFKA_SERVICES)
      .setProperties(vcKafkaConfig.VCKafkaProperties)
      .setTopics("vc.mda.online")
      .setGroupId("vc_mda_online_prod01")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema)
      .build

    val logSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "vc_mda_online")

    val mdto = logSource.map(m1 => VcMessageHandler.parseMessage(m1)).filter(s1 => s1 != null)
    val adto = mdto.map(m2 => ActionMessageHandler.parseActionDto(m2, null)).filter(s2 => s2 != null).flatMap(f => Option(f))

    adto.print()

    val recSink = KafkaSink.builder[ActionDto]()
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setKafkaProducerConfig(shuffleConfig.recDestKafkaProperties)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(shuffleConfig.vc2recActionTopic)
          .setValueSerializationSchema(new ActionDtoSerializationSchema)
          .build()
      ).build()

    adto.sinkTo(recSink).uid("vc-mda-online")

    env.execute("vc mda data to rec")
  }

  class ActionDtoSerializationSchema extends SerializationSchema[ActionDto] {
    override def serialize(element: ActionDto): Array[Byte] = {
      val source = gson.toJson(element)
      LOG.debug("log shuffle rec: {}", source)
      source.getBytes("UTF-8")
    }
  }

}

