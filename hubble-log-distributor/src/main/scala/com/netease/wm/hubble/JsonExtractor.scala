package com.netease.wm.hubble

import java.nio.charset.StandardCharsets
import java.util.Properties

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.producer.ProducerRecord
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.util.matching.Regex

object JsonExtractor {
  private val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  val LOG_PATTERN: Regex = """^[^\{]+(\{.*\})\s*$""".r

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 3) {
      println("usage: ./JsonExtractor kafkaServers inTopic outTopic, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val bootStrapServers = args(0)
    val inTopic = args(1)
    val outTopic = args(2)

    val kafkaProperties = {
      val props = new Properties()
      props.setProperty("bootstrap.servers", bootStrapServers)
      props.setProperty("group.id", "json_extractor")
      props.setProperty("auto.offset.reset", "latest")
      props.setProperty("flink.partition-discovery.interval-millis", "60000")
      props.setProperty("compression.type", "snappy")
      props
    }

    env.enableCheckpointing(2 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(5 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(1 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](Seq(inTopic).asJava, new SimpleStringSchema(), kafkaProperties).setStartFromTimestamp(startTimeStamp)
    )

    val sink = new FlinkKafkaProducer[String](outTopic, new StringSerializationSchema(outTopic), kafkaProperties, FlinkKafkaProducer.Semantic.EXACTLY_ONCE)

    logSource
      .flatMap { line =>
        line match {
          case LOG_PATTERN(json) => Some(json)
          case _ => None
        }
      }
      .addSink(sink).uid("json-extractor-sink")

    env.execute(s"JsonExtractor from kafka topic[$inTopic] to topic[$outTopic]")
  }

  class StringSerializationSchema(val outTopic: String) extends KafkaSerializationSchema[String] {
    override def serialize(element: String, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      new ProducerRecord[Array[Byte], Array[Byte]](outTopic,  element.getBytes(StandardCharsets.UTF_8))
    }
  }
}
