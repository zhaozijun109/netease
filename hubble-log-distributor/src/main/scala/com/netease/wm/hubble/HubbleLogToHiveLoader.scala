package com.netease.wm.hubble

import com.netease.wm.hubble.avro.MdaHive
import com.netease.wm.hubble.common.MdaHelper.MdaHiveBucketAssigner
import com.netease.wm.hubble.common.{AppendOnlyStringMap, FileSinkHelper, MdaHelper, kafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.Partitioner
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object HubbleLogToHiveLoader {
  def main(args: Array[String]): Unit = {
    if(args.length < 1) {
      println("usage: HubbleLogToHiveLoader output, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val output = args(0)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(900000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    // env.getConfig.enableObjectReuse()

    //val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val mdaSource = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.online")
      .setGroupId("hubble_log_to_hive")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val hiveParquetSink = FileSinkHelper.createParquetFileSink[MdaHive](output, MdaHiveBucketAssigner("yyyy-MM-dd"))

    env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "mda-source")
      .rebalance
      .process(new ParseMdaFunction)
      .partitionCustom(new Partitioner[String]{
        override def partition(k: String, partitions: Int): Int = Math.abs(k.hashCode % partitions)
      }, e => e.deviceUdid)
      .sinkTo(hiveParquetSink).uid("mda-parquet-sink")

    env.execute("hubble log to hive")
  }

  class ParseMdaFunction extends ProcessFunction[String, MdaHive] {
    val params = new AppendOnlyStringMap(32)

    override def processElement(input: String, context: ProcessFunction[String, MdaHive]#Context, collector: Collector[MdaHive]): Unit = {
      params.clear()

      MdaHelper.parseMdaEvent(input, new MdaHive(), params).foreach { event =>
        collector.collect(event)
      }
    }
  }
}
