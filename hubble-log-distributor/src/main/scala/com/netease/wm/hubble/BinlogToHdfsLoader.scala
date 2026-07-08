package com.netease.wm.hubble

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.wm.hubble.CdcToHdfsAndKafkaLoader.{NewBinlogRow, toJsonObject}
import com.netease.wm.hubble.common.{BinlogRow, BinlogSubscribeEventSchema, FileSinkHelper, kafkaConfig}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConverters._

object BinlogToHdfsLoader {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if (args.length < 2) {
      println("usage: ./BinlogToHdfsLoader topic output, but got " + args.mkString(" "))
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
    properties.setProperty("group.id", "ddb_binlog_to_hdfs")
    properties.setProperty("auto.offset.reset", "latest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val logSource = env.addSource(
      new FlinkKafkaConsumer[SubscribeEvent](topic.split(",").toSeq.asJava, new BinlogSubscribeEventSchema(), properties).setStartFromGroupOffsets()
    )

    val sink = FileSinkHelper.createPartitionBinlogFileSink(outputPath)

    val datas = logSource
      .flatMap { (event: SubscribeEvent, collector: Collector[BinlogRow]) =>
        val seqno = event.getSeqno
        val opTime = event.getTimestamp
        val partitionId = event.getPartitonId

        event.getRowChanges.asScala
          .foreach { row =>
            val isDeleteOp = row.getType == RowChangeType.DELETE
            val isUpdateOp = row.getType == RowChangeType.UPDATE
            val columns: Map[String, Any] = row.getColumnChanges.asScala.map { r =>
              r.getColumnName -> (if (isDeleteOp) r.getOldValue else r.getNewValue)
            }.toMap

            val old: Map[String, Any] = if (isUpdateOp) {
              row.getColumnChanges.asScala
                .filter { r => r.getNewValue != r.getOldValue }
                .map(r => r.getColumnName -> r.getOldValue)
                .toMap
            } else Map.empty

            collector.collect(BinlogRow(row.getTableName, row.getType.getCode, opTime, seqno, partitionId, columns, old))
          }
      }

    datas.sinkTo(sink).name("hdfs-sink")

    val kafkaSink = KafkaSink.builder[String]()
      .setBootstrapServers("lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092,lofter-kafka-bi-risk4.gy.ntes:9092,lofter-kafka-bi-risk5.gy.ntes:9092")
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("vc.binlog.online")
          .setValueSerializationSchema(new SimpleStringSchema())
          .build()
      ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    datas.map(row => {
        val newbinlogrow = NewBinlogRow(row.table, row.op, row.opTime, row.seqno, row.partitionId, row.data, row.old, row.table, row.op, row.opTime, row.seqno, row.old)
        toJsonObject(newbinlogrow).toJSONString
      })
      .sinkTo(kafkaSink).name("kafka-sink")

    env.execute(s"BinlogToHdfs from kafka: $topic")
  }
}
