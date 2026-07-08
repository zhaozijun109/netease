package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.FileSinkHelper
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.joda.time.DateTime

import java.util.Properties
import scala.collection.JavaConverters._
import scala.util.matching.Regex

object DsNoHeadJsonLogToHdfsLoader {
  private val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  val LOG_PATTERN: Regex = """^[^\{]+(\{.*\})\s*$""".r

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 3) {
      println("usage: ./DsJsonLogToHdfsLoader kafkaServers topic output, but got " + args.mkString(" "))
      System.exit(-1)
    }


    val bootStrapServers = args(0)
    val topic = args(1)
    val outputPath = args(2)

    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", bootStrapServers)
    properties.setProperty("group.id", "ds_no_header_log_to_hdfs")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](Seq(topic).asJava, new SimpleStringSchema(), properties).setStartFromTimestamp(startTimeStamp)
    )

    logSource
      .flatMap { line =>
        line match {
          case LOG_PATTERN(json) => Some(json)
          case _ => None
        }
      }
      .sinkTo(FileSinkHelper.createTextFileSink(outputPath))

    env.execute(s"DsNoHeaderJsonLogToHdfs from kafka: $topic")
  }
}
