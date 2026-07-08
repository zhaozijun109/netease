package com.netease.wm.hubble

import com.fasterxml.jackson.core.{JsonParser, JsonToken}
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.{FileSinkHelper, HubbleEvent, kafkaConfig}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.joda.time.DateTime

import java.nio.charset.StandardCharsets
import java.util.Properties
import scala.collection.JavaConverters._

object HubbleLogToHdfsLoader {

  private val objectMapper = new ObjectMapper()
  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 2) {
      println("usage: ./HubbleLogToHdfsLoader topics output, but got " + args.mkString(" "))
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
    properties.setProperty("group.id", "hubble_log_to_jd_hdfs")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val logSource = env.addSource(
      new FlinkKafkaConsumer[HubbleEvent](topics.asJava, new HubbleEventSchema(), properties).setStartFromGroupOffsets()
    )

    logSource.disableChaining().sinkTo(FileSinkHelper.createHubbleLogFileSink(outputPath))

    env.execute(s"HubbleLogToHdfs from kafka: $topics")
  }

  class HubbleEventSchema extends KafkaDeserializationSchema[HubbleEvent] {
    override def isEndOfStream(nextElement: HubbleEvent): Boolean = false

    def parseEventTime(content: String): Long = {
      val parser = objectMapper.createParser(content)
      if(parser.nextToken() != JsonToken.START_OBJECT) {
        return 0
      }
      var token = parser.nextToken()
      var time: Long = 0
      while(token != JsonToken.END_OBJECT) {
        if (token == JsonToken.FIELD_NAME) {
          val fieldName = parser.getCurrentName()
          val valueToken = parser.nextToken()

          if(fieldName == "kafkaTime") {
            time = parser.getValueAsLong()
          }

          if(valueToken == JsonToken.START_OBJECT || valueToken == JsonToken.START_ARRAY) {
            parser.skipChildren()
          }
        }

        token = parser.nextToken()
      }

      if(time <= 0) 0 else time
    }

    override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]]): HubbleEvent = {
      val content = new String(record.value(), StandardCharsets.UTF_8)
      val topic = record.topic()
      val eventTime: Long = parseEventTime(content)
      val time = if(eventTime > 0) eventTime else System.currentTimeMillis()
      HubbleEvent(topic, time, content)
    }

    override def getProducedType: TypeInformation[HubbleEvent] = createTypeInformation[HubbleEvent]
  }

}
