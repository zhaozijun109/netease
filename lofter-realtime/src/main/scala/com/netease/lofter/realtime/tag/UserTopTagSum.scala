package com.netease.lofter.realtime.tag

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{TagEvent, UserTopTag}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.RowKind
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

import java.time.Duration

/**
 * OMLOFTER-56671: user recent 7 days topN accessed tag
 *
 * 1. 7 days sliding window is too large for realtime topN for state size
 * 2. workaround: 6 day offline data + 1 day tumble window topN
 */
object UserTopTagSum {

  case class Event(userId: Long, tag: String, dt: Int)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[TagEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("tag.event.dwd")
      .setGroupId("user_recent_top_tag")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[TagEvent])
      .build()

    val aggregateSink = KafkaSink.builder[UserTopTag]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("tag.user.top")
          .setValueSerializationSchema(new AvroBinarySerSchema[UserTopTag])
          .build()
      ).build()

    val eventDetail: DataStream[Event] = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "tag-event-detail")
      .uid("user-top-tag-input")
      .map { e =>
        val dt = new DateTime(e.time).toString("yyyyMMdd").toInt
        Event(e.userId, e.tag, dt)
      }

    tableEnv.createTemporaryView("tag_events", eventDetail)

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "30 s")
    configuration.setString("table.exec.mini-batch.size", "10000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofDays(1))

    val featureSql =
      """
        |select userId, dt, tag, pv
        |from (
        |  select userId, dt, tag, pv,
        |         row_number() over (partition by userId, dt order by pv desc) as row_num
        |  from (
        |     select userId, tag, dt, count(1) as pv
        |     from tag_events
        |     group by userId, dt, tag
        |  )
        |)
        |where row_num <= 10
        |""".stripMargin

    tableEnv.sqlQuery(featureSql)
      .toChangelogStream
      .filter(e => e.getKind == RowKind.INSERT || e.getKind == RowKind.UPDATE_AFTER)
      .map{ x =>
        UserTopTag(
          x.getFieldAs[Long]("userId"), x.getFieldAs[Int]("dt"),
          x.getFieldAs[String]("tag"), x.getFieldAs[Long]("pv")
        )
      }.sinkTo(aggregateSink).uid("user-daily-top-tag-sink")

    env.execute("user daily top tag")
  }


}
