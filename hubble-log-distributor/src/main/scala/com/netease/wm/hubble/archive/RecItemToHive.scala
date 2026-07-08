package com.netease.wm.hubble.archive

import com.netease.wm.hubble.avro.RecItemEvent
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.{FileSinkHelper, kafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

object RecItemToHive {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val output = args(0)

    env.enableCheckpointing(300000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("rec_item_dwd_test")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val hdfsSink = FileSinkHelper.createParquetFileSink[RecItemEvent](output)

    env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "rec-item-event-detail")
      .sinkTo(hdfsSink)

    env.execute("rec item event dwd")
  }
}
