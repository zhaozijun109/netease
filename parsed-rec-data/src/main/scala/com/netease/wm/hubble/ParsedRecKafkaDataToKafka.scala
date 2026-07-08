package com.netease.wm.hubble

import com.google.gson.Gson
import com.lofter.rs.basic.bean.dto.upload.ActionDto
import com.netease.wm.hubble.common.recConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import rs.basic.upload.parse.handler.ActionMessageHandler

import java.util.Properties
import scala.util.control.NonFatal

object ParsedRecKafkaDataToKafka {
  val LOG: Logger = LoggerFactory.getLogger(ParsedRecKafkaDataToKafka.getClass)
  val gson = new Gson()

  def main(args: Array[String]): Unit = {
    var bootStrapServers = recConfig.BOOTSTRAP_SERVERS_ONLINE
    val sourceTopic = recConfig.sourceTopic
    val descTopic = recConfig.destTopic

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 1) {
      println("usage: ./ParsedRecKafkaDataToKafka run_mode, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val RUN_MODE = args(0)
    if (RUN_MODE=="test") {
      bootStrapServers = recConfig.BOOTSTRAP_SERVERS_TEST
    }

    env.enableCheckpointing(1 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(5 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(10 * 1000L)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", bootStrapServers)
    properties.setProperty("group.id", "upload-action-consumer-online")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val producerProps = new Properties()
    producerProps.setProperty("bootstrap.servers", bootStrapServers)
    producerProps.setProperty("transaction.timeout.ms","900000")
    producerProps.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema(), properties).setStartFromGroupOffsets()
    )

    val parsedSource = logSource
      .flatMap { line =>
        try {
          val parsedRecord: ActionDto = ActionMessageHandler.parseActionDto(line)
          Option(parsedRecord)
        } catch {
          case NonFatal(_) =>
            LOG.info(s"error parsing mda event to rec event actionDto: $line")
            None
        }
      }.uid("parsed_source_kafka")

    val myProducer = new FlinkKafkaProducer[ActionDto](descTopic, new ParsedEventSerializationSchema, producerProps, Semantic.AT_LEAST_ONCE)
    myProducer.setWriteTimestampToKafka(true)

    parsedSource.addSink(myProducer).uid("rec_kafka_parsed_sink")

    env.execute(s"ParsedRecKafkaDataToKafka parsed rec kafka data to kafka directly: $sourceTopic $RUN_MODE")
  }

  class ParsedEventSerializationSchema extends KafkaSerializationSchema[ActionDto] {
    override def serialize(element: ActionDto, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      val topic = recConfig.destTopic
      val source = gson.toJson(element)
      LOG.debug("log shuffle rec: {}", source)
      new ProducerRecord[Array[Byte], Array[Byte]](topic, source.getBytes("UTF-8"))
    }
  }

}
