package com.netease.lofter.realtime.ad

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{AdDeepLinkAccessEvent, Mda}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object AdDeepLinkAccessEtl {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("ad_deeplink_access_etl")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val adDeepLinkEventSink = KafkaSink.builder[AdDeepLinkAccessEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter-web-ddos")
          .setKeySerializationSchema(new AdDeepLinkEventKeySchema)
          .setValueSerializationSchema(new AvroJsonSerSchema[AdDeepLinkAccessEvent])
          .build()
      ).build()

    val adDeepLinkEvents = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .filter{ e =>
        val url  = e.url.getOrElse("")
        val isActivityUrl = url.startsWith("lofter://www.lofter.com/cms/150906/lofter.html") ||
          url.startsWith("lofter://www.lofter.com/cms/150907/lofter.html") ||
          url.startsWith("lofter://www.lofter.com/cms/150908/lofter.html") ||
          url.startsWith("lofter://www.lofter.com/cms/150909/lofter.html") ||
          url.startsWith("lofter://www.lofter.com/cms/150904/lofter.html")

        e.customUdid.isDefined && e.eventId == "rd-4" && isActivityUrl
      }
      .map{ e =>
        val deviceId = e.customUdid.getOrElse("")
        val ip = e.ip.getOrElse("")
        val ua = ""
        val oaid = e.oaid.getOrElse("")
        val androidId = e.androidid.getOrElse("")
        val platform = e.deviceOs match {
          case Some("Android") => "android"
          case Some("iOS") => "iphone"
          case _ => e.deviceOs.getOrElse("")
        }
        val url = e.url.getOrElse("")

        AdDeepLinkAccessEvent(deviceId, ip, ua, oaid, androidId, platform, url)
      }

    adDeepLinkEvents.sinkTo(adDeepLinkEventSink)

    env.execute("ad deeplink access event etl")
  }

  class AdDeepLinkEventKeySchema extends SerializationSchema[AdDeepLinkAccessEvent] {
    override def serialize(v: AdDeepLinkAccessEvent): Array[Byte] = {
      v.deviceId.getBytes("UTF-8")
    }
  }
}
