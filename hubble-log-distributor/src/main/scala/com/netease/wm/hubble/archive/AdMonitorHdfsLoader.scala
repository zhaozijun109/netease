package com.netease.wm.hubble.archive

import com.netease.wm.hubble.avro.AdxMonitorEvent
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.{FileSinkHelper, kafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object AdMonitorHdfsLoader {
  def main(args: Array[String]): Unit = {
    if(args.length < 1) {
      println("usage: AdMonitorHdfsLoader output, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val output = args(0)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(900000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    //val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val adxMonitorSource = KafkaSource.builder[AdxMonitorEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("adx.server.avro")
      .setGroupId("adx_monitor_hdfs")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[AdxMonitorEvent])
      .build()

    val hdfsSink = FileSinkHelper.createParquetFileSink[AdxMonitorEvent](output)

    env.fromSource(adxMonitorSource, WatermarkStrategy.noWatermarks(), "adx-monitor-source")
      .sinkTo(hdfsSink)

    env.execute("adx monitor hdfs load")
  }
}
