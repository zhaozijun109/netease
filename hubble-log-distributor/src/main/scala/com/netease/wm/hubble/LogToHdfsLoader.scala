package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.{FileSinkHelper, kafkaConfig}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.joda.time.DateTime

import java.util.Properties
import scala.collection.JavaConverters._

object LogToHdfsLoader {
  private val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 2) {
      println("usage: ./LogToHdfsLoader topic output, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val topic = args(0)
    val outputPath = args(1)

    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "hubble_log_to_jd_hdfs")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](Seq(topic).asJava, new SimpleStringSchema(), properties).setStartFromGroupOffsets()
    )

    logSource.map { line =>
      // if line contains new line, it should be escaped before sink into hdfs
      line.replaceAll("\\r\\n|\\n", "\\\\n")
    }.sinkTo(FileSinkHelper.createTextFileSink(outputPath))

    env.execute(s"LogToHdfs from kafka: $topic")
  }
}
