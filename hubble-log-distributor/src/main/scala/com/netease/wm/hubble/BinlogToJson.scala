package com.netease.wm.hubble

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.wm.hubble.common.{BinlogRow, BinlogSubscribeEventSchema, kafkaConfig}
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import java.lang
import java.util.Properties
import scala.collection.JavaConverters._

/**
 * parse and output binlog to kafka with json format
 */
object BinlogToJson {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 2) {
      println("usage: ./BinlogToHdfsLoader inTopic outTopic, but got " + args.mkString(" "))
      System.exit(-1)
    }
    val inTopic = args(0)
    val outTopic = args(1)

    env.enableCheckpointing(1 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "binlog_to_json")
    properties.setProperty("auto.offset.reset", "latest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")
    properties.setProperty("max.request.size", "20000000")

    val logSource = env.addSource(
      new FlinkKafkaConsumer[SubscribeEvent](inTopic.split(",").toSeq.asJava, new BinlogSubscribeEventSchema(), properties).setStartFromGroupOffsets()
    )

    val sink = new FlinkKafkaProducer[BinlogRow](outTopic, new BinLogRowSerializationSchema(outTopic), properties, FlinkKafkaProducer.Semantic.EXACTLY_ONCE)

    logSource
      .flatMap { (event: SubscribeEvent,  collector: Collector[BinlogRow]) =>
        val seqno = event.getSeqno
        val opTime = event.getTimestamp
        val partitionId = event.getPartitonId

        implicit val format = DefaultFormats
        event.getRowChanges.asScala
          .foreach { row =>
            val isDeleteOp = row.getType == RowChangeType.DELETE
            val isUpdateOp = row.getType == RowChangeType.UPDATE
            val columns: Map[String, String] = row.getColumnChanges.asScala.map { r =>
              r.getColumnName -> (if (isDeleteOp) r.getOldValue else r.getNewValue)
            }.filterNot(_._2 == null)
              .map {
              case (key, value: String) => key -> value
              case (key, value) => key -> write(value)
            }.toMap

            val old: Map[String, String] = if( isUpdateOp ) {
              row.getColumnChanges.asScala
                .filter{ r => r.getNewValue != r.getOldValue}
                .map{ r =>
                  r.getOldValue match {
                    case value: String => r.getColumnName -> value
                    case value if value == null => r.getColumnName -> ""
                    case value => r.getColumnName -> write(value)
                  }
                }
                .toMap
            } else Map.empty

            collector.collect(BinlogRow(row.getTableName, row.getType.getCode, opTime, seqno, partitionId, columns, old))
          }
      }.addSink(sink)

    env.execute(s"BinlogToJson from kafka: $inTopic")
  }
}

class BinLogRowSerializationSchema(val outTopic: String) extends KafkaSerializationSchema[BinlogRow] {
  override def serialize(element: BinlogRow, timestamp: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
    implicit val format = DefaultFormats
    val content = write(element)
    new ProducerRecord[Array[Byte], Array[Byte]](outTopic, content.getBytes("UTF-8"))
  }
}
