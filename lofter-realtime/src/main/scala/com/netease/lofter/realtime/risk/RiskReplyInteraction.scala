package com.netease.lofter.realtime.risk

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.RowKind
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

private class RiskReplyInteraction{}

/**
 * 48h reply interaction data
 */
object RiskReplyInteraction {
  val logger: Logger = LoggerFactory.getLogger(classOf[RiskReplyInteraction])

  case class CommentEvent(commentId: Long, userId: Long, postId: Long, blogId: Long,
                           publishTime: Long, parentCommentId: Long, commentHot: Long, replyCount: Long)

  case class NewReplyFeature(responseId: Long, postId: Long, blogId: Long, fid: Long,
                             publishTime: Long, likeNum: Long, replyNum: Long)

  case class RiskMessage(messageType: Int, payLoad: NewReplyFeature)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val RISK_SINK_TOPIC: String = "GR_COMMENT"
    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    
    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("risk_reply_interaction")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val riskSink = KafkaSink.builder[RiskMessage]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.RISK_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder[RiskMessage]()
          .setTopic(RISK_SINK_TOPIC)
          .setValueSerializationSchema(new RiskMessageSerSchema)
          .build()
      ).build()

    val replies = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[CommentEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "PostResponse" =>
                val commentId = row.getColumn("ID").getNewValue.asInstanceOf[Long]
                val userId = row.getColumn("PublisherUserID").getNewValue.asInstanceOf[Long]
                val postId = row.getColumn("PostID").getNewValue.asInstanceOf[Long]
                val blogId = row.getColumn("BlogID").getNewValue.asInstanceOf[Long]
                val publishTime = row.getColumn("PublishTime").getNewValue.asInstanceOf[Long]
                val commentHot = row.getColumn("commentHot").getNewValue.asInstanceOf[Long]
                val replyL2Count = row.getColumn("replyL2Count").getNewValue.asInstanceOf[Long]
                val replyL1CommentId = row.getColumn("replyL1CommentId").getNewValue.asInstanceOf[Long]
                val replyL2CommentId = row.getColumn("replyL2CommentId").getNewValue.asInstanceOf[Long]
                val commentHotOld = row.getColumn("commentHot").getOldValue.asInstanceOf[Long]
                val replyL2CountOld = row.getColumn("replyL2Count").getOldValue.asInstanceOf[Long]

                val parentCommentId = if(replyL2CommentId > 0) replyL2CommentId else replyL1CommentId

                val twoDaysAgo = DateTime.now().minusDays(2).getMillis

                if(publishTime > twoDaysAgo &&
                  Math.max(commentHot, replyL2Count) >= 10 &&
                  (commentHot != commentHotOld || replyL2Count != replyL2CountOld)) {
                  collector.collect(CommentEvent(commentId, userId, postId, blogId, publishTime, parentCommentId, commentHot, replyL2Count))
                }

              case _ =>
            }
          }
      }

    tableEnv.createTemporaryView("replies", replies)

    val sql =
      """
        |select commentId, postId, blogId, publishTime, parentCommentId,
        |       max(commentHot) as commentHot,
        |       max(replyCount) as replyCount
        |from replies
        |group by commentId, postId, blogId, publishTime, parentCommentId
        |""".stripMargin

    tableEnv.sqlQuery(sql).toChangelogStream
      .filter(row => row.getKind == RowKind.INSERT || row.getKind == RowKind.UPDATE_AFTER)
      .map { x =>
        RiskMessage(20000, NewReplyFeature(x.getFieldAs("commentId").asInstanceOf[Long], x.getFieldAs("postId").asInstanceOf[Long],
          x.getFieldAs("blogId").asInstanceOf[Long], x.getFieldAs("parentCommentId").asInstanceOf[Long], x.getFieldAs("publishTime").asInstanceOf[Long],
          x.getFieldAs("commentHot").asInstanceOf[Long], x.getFieldAs("replyCount").asInstanceOf[Long]))
      }.sinkTo(riskSink).uid("risk-new-post-interaction-sink")


    env.execute("risk reply interaction")
  }

  class RiskMessageSerSchema extends SerializationSchema[RiskMessage]{
    override def serialize(element: RiskMessage): Array[Byte] = {
      implicit val format = DefaultFormats
      val content = write(element)
      content.getBytes("UTF-8")
    }
  }
}
