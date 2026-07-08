package com.netease.lofter.realtime.gift

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.GroupPostHot
import com.netease.wm.hubble.common.avro.binary.AvroBinarySerSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.table.api.{DataTypes, Schema}
import org.apache.flink.types.RowKind
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import java.time.Duration
import scala.collection.JavaConverters._

object GroupPostHotTask {

  case class PostHotEvent(postId: Long, userId: Long, hot: Long)
  case class GroupMemberEvent(teamId: Long, userId: Long, valid: Int)
  case class GroupPostEvent(teamId: Long, postId: Long, blogId: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val latenessInSeconds = 30

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("group_post_hot")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val groupMemberHistorySource = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.im.group.member")
      .setGroupId("group_post_hot")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val resultSink = KafkaSink.builder[GroupPostHot]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.IM.post.like.count.update")
          .setValueSerializationSchema(new AvroJsonSerSchema[GroupPostHot])
          .build()
      ).build()

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val groupMemberHistoryWatermarkStrategy =  WatermarkStrategy
      .forBoundedOutOfOrderness[String](Duration.ofSeconds(latenessInSeconds))
      .withIdleness(Duration.ofMinutes(1))

    val postHotEvents = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[PostHotEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.DELETE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "PostHot" =>
                val postId = if(changeType == RowChangeType.INSERT ) row.getColumn("PostID").getNewValue.asInstanceOf[Long] else row.getColumn("PostID").getOldValue.asInstanceOf[Long]
                val userId = if(changeType == RowChangeType.INSERT ) row.getColumn("PublisherUserID").getNewValue.asInstanceOf[Long] else row.getColumn("PublisherUserID").getOldValue.asInstanceOf[Long]
                val opType = if(changeType == RowChangeType.INSERT ) row.getColumn("Type").getNewValue.asInstanceOf[Int] else row.getColumn("Type").getOldValue.asInstanceOf[Int]
                val hot = if(changeType == RowChangeType.INSERT ) 1 else -1
                if(opType == 1) { // only count likes
                  collector.collect(PostHotEvent(postId, userId, hot))
                }
              case _ =>
            }
          }
      }

    val groupMemberEvents = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[GroupMemberEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Message_Group_Member" =>
                val teamId = row.getColumn("teamId").getNewValue.asInstanceOf[Long]
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
                val valid = changeType match {
                  case RowChangeType.INSERT if status == 1 => 1
                  case RowChangeType.UPDATE if status != oldStatus => -1
                  case _ => 0
                }

                if(valid != 0) {
                  collector.collect(GroupMemberEvent(teamId, userId, valid))
                }

              case _ =>
            }
          }
      }

    val groupMemberHistoryEvents = env.fromSource(groupMemberHistorySource, groupMemberHistoryWatermarkStrategy, "im-group-member-history")
      .flatMap { (s: String, collector: Collector[GroupMemberEvent]) =>
        implicit val formats: org.json4s.Formats = DefaultFormats
        parse(s).extractOpt[GroupMemberEvent].foreach{ e => collector.collect(e)}
      }

    val groupPostEvents = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[GroupPostEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Message_Group_Update_Post_Like_Count" =>
                val teamId = row.getColumn("teamId").getNewValue.asInstanceOf[Long]
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                val blogId = row.getColumn("blogId").getNewValue.asInstanceOf[Long]

                collector.collect(GroupPostEvent(teamId, postId, blogId))
              case _ =>
            }
          }
      }

    val postHotTable = tableEnv.fromDataStream(
      postHotEvents,
      Schema.newBuilder()
        .column("postId", DataTypes.BIGINT())
        .column("userId", DataTypes.BIGINT())
        .column("hot", DataTypes.BIGINT())
        .columnByMetadata("rowtime", DataTypes.TIMESTAMP(3))
        .watermark("rowtime", "SOURCE_WATERMARK()")
        .build())

    val groupMemberTable = tableEnv.fromDataStream(
      groupMemberHistoryEvents.union(groupMemberEvents),
      Schema.newBuilder().
        column("teamId", DataTypes.BIGINT())
        .column("userId", DataTypes.BIGINT())
        .column("valid", DataTypes.INT())
        .columnByMetadata("rowtime", DataTypes.TIMESTAMP(3))
        .watermark("rowtime", "SOURCE_WATERMARK()")
        .build())

    val groupPostTable = tableEnv.fromDataStream(
      groupPostEvents,
      Schema.newBuilder()
        .column("teamId", DataTypes.BIGINT())
        .column("postId", DataTypes.BIGINT())
        .column("blogId", DataTypes.BIGINT())
        .columnByMetadata("rowtime", DataTypes.TIMESTAMP(3))
        .watermark("rowtime", "SOURCE_WATERMARK()")
        .build())

    tableEnv.createTemporaryView("post_hot", postHotTable)
    tableEnv.createTemporaryView("group_member", groupMemberTable)
    tableEnv.createTemporaryView("group_post", groupPostTable)

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "300 s")
    configuration.setString("table.exec.mini-batch.size", "10000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofDays(7))

    val featureSql =
      """
        |select x.postId, x.blogId, x.teamId, sum(y.hot) as hot
        |from
        |  (
        |     select a.teamId, a.userId, b.blogId, b.postId, b.rowtime as publish_time
        |     from (
        |         select teamId, userId, sum(valid) as valid
        |         from group_member
        |         group by teamId, userId
        |     ) a
        |     inner join group_post b on a.teamId = b.teamId
        |     where a.valid > 0
        |  ) x,
        |  (
        |     select postId, userId, sum(hot) as hot, max(rowtime) as op_time
        |     from post_hot
        |     group by postId, userId
        |  ) y
        |  where x.postId = y.postId and x.userId = y.userId and
        |        x.publish_time between y.op_time - INTERVAL '7' DAYS and y.op_time
        |group by x.postId, x.blogId, x.teamId
        |""".stripMargin

    tableEnv.sqlQuery(featureSql)
      .toChangelogStream
      .filter(row => row.getKind == RowKind.INSERT || row.getKind == RowKind.UPDATE_AFTER)
      .map{ x =>
        GroupPostHot(x.getFieldAs("postId").asInstanceOf[Long], x.getFieldAs("blogId").asInstanceOf[Long], x.getFieldAs("teamId").asInstanceOf[Long], x.getFieldAs("hot").asInstanceOf[Long])
      }.sinkTo(resultSink).uid("group-post-hot-output")


    env.execute("group post hot")
  }
}
