package com.netease.wm.hubble

import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Properties

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.{BackendEvent, FileSinkHelper, kafkaConfig}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema}
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

object BackendLogToHdfsLoader {
  private val objectMapper = new ObjectMapper()
  objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"
  @transient private val formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 2) {
      println("usage: ./BackendLogToHdfsLoader topic output, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val topics = args(0).split(",").toSeq.map(_.trim).filter(_.nonEmpty)
    val outputPath = args(1)

    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "vc_backend_log_to_hdfs")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val logSource = env.addSource(
      new FlinkKafkaConsumer[BackendEvent](topics.asJava, new BackendEventSchema(), properties).setStartFromGroupOffsets()
    )

    logSource.sinkTo(FileSinkHelper.createBackendLogFileSink(outputPath))

    env.execute(s"BackendLogToHdfs from kafka: $topics")
  }

  class BackendEventSchema extends KafkaDeserializationSchema[BackendEvent] {
    override def isEndOfStream(nextElement: BackendEvent): Boolean = false

    override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]]): BackendEvent = {
      val content = new String(record.value(), StandardCharsets.UTF_8)
      try {
        val (tableName, logTime) = {
          val jsonNode = objectMapper.readTree(content)
          val timeNode = jsonNode.get("logTime")
          val logType = jsonNode.get("logType")
          val time = if(timeNode != null) formatter.parse(timeNode.asText("0")).getLong(ChronoField.INSTANT_SECONDS)*1000L else System.currentTimeMillis()
          val tableName = if(logType != null) logType.asText() else "logTypeUndefine"
          (tableName, time)
        }
        BackendEvent(tableName, logTime, content)
      } catch {
        case NonFatal(e) =>
          println("Exception: parse log error for input: {}", content , e.printStackTrace())
          BackendEvent("logTypeUndefine", System.currentTimeMillis(), content)
        case _ : Throwable =>
          println("parse log error for input: {}", content)
          BackendEvent("logTypeUndefine", System.currentTimeMillis(), content)
      }
    }

    override def getProducedType: TypeInformation[BackendEvent] = createTypeInformation[BackendEvent]
  }
}
