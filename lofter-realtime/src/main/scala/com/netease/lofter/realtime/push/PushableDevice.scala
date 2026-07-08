package com.netease.lofter.realtime.push

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{DevicePushStatus, Mda, TagEvent}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object PushableDevice {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(20000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("push_device_notification")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val devicePushStatusSink = KafkaSink.builder[DevicePushStatus]()
      .setBootstrapServers(kafkaConfig.GY_PUSH_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("yaolu.push.device.notification")
        .setValueSerializationSchema(new AvroJsonSerSchema[DevicePushStatus])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()


    val mda = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")

    val devicePushStatus = mda
      .filter(e => e.eventId == "rd-3" && e.customUdid.isDefined)
      .flatMap { e: Mda =>
        e.actionType match {
          case Some("on") => Some(DevicePushStatus(e.customUdid.get, isPushEnabled = 1))
          case Some("off") => Some(DevicePushStatus(e.customUdid.get, isPushEnabled = 0))
          case _ => None
        }
      }.uid("device-push-status")

    devicePushStatus.sinkTo(devicePushStatusSink)

    env.execute("device push status")
  }
}
