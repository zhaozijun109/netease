package com.netease.lofter.realtime.pve

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.PveManUserReplyAggregate
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

private class PveManReplyCount{}

object PveManReplyCount {
  val logger: Logger = LoggerFactory.getLogger(classOf[PveManReplyCount])

  case class PveManDialogue(userId: Long, roleId: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val RISK_SINK_TOPIC: String = "LOFTER.PVEMAN.invite"
    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("pve_man_reply_count")
      .setStartingOffsets(OffsetsInitializer.timestamp(startTimeStamp))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val riskSink = KafkaSink.builder[PveManUserReplyAggregate]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder[PveManUserReplyAggregate]()
          .setTopic(RISK_SINK_TOPIC)
          .setValueSerializationSchema(new AvroJsonSerSchema[PveManUserReplyAggregate])
          .build()
      ).build()

    val replies = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[PveManDialogue]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT)
          .foreach { row =>
            row.getTableName match {
              case "PVE_UserDialogue" =>
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val roleId = row.getColumn("pveUserId").getNewValue.asInstanceOf[Long]
                val sender = row.getColumn("sender").getNewValue.asInstanceOf[Int]

                // only accounting user-initiated dialogue
                if(sender == 1) {
                  collector.collect(PveManDialogue(userId, roleId))
                }
              case _ =>
            }
          }
      }

    tableEnv.createTemporaryView("replies", replies)

    val sql =
      """
        |select userId, roleId,
        |       count(1) as dialogueCount
        |from replies
        |group by userId, roleId
        |""".stripMargin

    tableEnv.sqlQuery(sql).toRetractStream[(Long,Long,Long)]
      .filter(_._1)
      .map { x =>
        val (_, (userId, roleId, dialogueCount)) = x
        PveManUserReplyAggregate(userId, roleId, `type` = 1, dialogueCount = dialogueCount)
      }.sinkTo(riskSink).uid("pve-man-reply-count-sink")


    env.execute("pve man reply count")
  }
}
