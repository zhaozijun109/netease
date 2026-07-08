package com.netease.lofter.realtime.risk

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{AcwEvent, YiyouUgcProtectDevice}
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}

private class YiyouUgcDeviceProtection{}

object YiyouUgcDeviceProtection {
  val LOG: Logger = LoggerFactory.getLogger(classOf[YiyouUgcDeviceProtection])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val acwSource = KafkaSource.builder[AcwEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.acw.front.log")
      .setGroupId("bookstore_pgc_user_group")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[AcwEvent](ignoreErrors = true))
      .build()

    val yiyouSink = KafkaSink.builder[YiyouUgcProtectDevice]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("lofter.common.yiyou.user")
        .setValueSerializationSchema(new AvroJsonSerSchema[YiyouUgcProtectDevice])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val acwSearchDevices = env.fromSource(acwSource, WatermarkStrategy.noWatermarks(), "user-actpwd-search")
      .uid("ugc-actpwd-growth-device")
      .filter(s => s.actPwd.nonEmpty && s.customid.isDefined && s.customid.nonEmpty && s.channel.isDefined && s.channel.get.contains("米良") && s.settlementType.exists(_ == "UGC"))
      .map { e =>
        val messageType: Int = 2 // UGC拉新设备
        YiyouUgcProtectDevice(messageType, e.customid.get)
      }

    acwSearchDevices.sinkTo(yiyouSink)

    env.execute("yiyou protection ugc devices")
  }

}
