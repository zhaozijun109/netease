package com.netease.lofter.realtime.live

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{LiveEvent, Mda}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object LiveEventEtl {
  private val eventSet = Set("n2-12","n2-21","n2-48","z4-1","z4-2","z4-3","z4-4","z4-5","z4-6","z4-7","z4-8","z4-9")
  private val focusEventSet = Set("n2-12", "n2-21")
  private val watchEventSet = Set("n2-48")
  private val shareEventSet = Set("z4-1","z4-2","z4-3","z4-4","z4-5","z4-6","z4-7","z4-8","z4-9")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    //val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("lofter_live_etl")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val liveSink = KafkaSink.builder[LiveEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("lofter.live.online")
        .setValueSerializationSchema(new AvroJsonSerSchema[LiveEvent])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val mda = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")

    mda.filter( e => eventSet(e.eventId) && e.scene.contains( "live") && e.userId.exists(_ > 0) && e.itemId.exists(_ > 0))
      .flatMap { e =>
        val followBlogId = e.blogId.getOrElse(0L)
        val followType = if(e.eventId == "n2-12") "on" else e.actionType.getOrElse("else")
        val behavior = e.eventId match {
          case eventId if focusEventSet(eventId) => Some("focus")
          case eventId if shareEventSet(eventId) => Some("share")
          case eventId if watchEventSet(eventId) && e.costTime.exists(_ > 5000) => Some("watch")
          case _ => None
        }
        behavior.map { b => LiveEvent(b, e.userId.get, e.itemId.get, e.kafkaTime, followBlogId, followType, e.costTime.getOrElse(0L), e.ip.getOrElse(""))}
      }.sinkTo(liveSink)

    env.execute("lofter live event etl")
  }
}
