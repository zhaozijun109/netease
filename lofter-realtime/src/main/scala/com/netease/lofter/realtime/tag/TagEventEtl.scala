package com.netease.lofter.realtime.tag

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{Mda, TagEvent}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object TagEventEtl {
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
      .setGroupId("tag_event_etl")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val tagEventSink = KafkaSink.builder[TagEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("tag.event.dwd")
        .setValueSerializationSchema(new AvroBinarySerSchema[TagEvent])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()


    val mda = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")

    val tagEvents = mda.filter(e => e.eventId == "f1-4" && e.text.nonEmpty)
      .map { e: Mda =>
        TagEvent(e.text.get, e.userId.getOrElse(0L), e.kafkaTime)
      }.uid("tag-event-etl")

    tagEvents.sinkTo(tagEventSink)

    env.execute("tag event etl")
  }
}
