package com.netease.lofter.realtime.activity

import java.sql.PreparedStatement

import com.netease.lofter.realtime.common.{clickhouseConfig, kafkaConfig}
import com.netease.wm.hubble.avro.ActivityEvent
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object ActivityEventToClickHouse {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val activitySource = KafkaSource.builder[ActivityEvent]()
      .setTopics("activity.event.dwd")
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setGroupId("activity_to_clickhouse_gy_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[ActivityEvent])
      .build()

    val chSink = JdbcSink.sink(
      "insert into activity_events_local(platform,dataType,appKey,deviceUdid,devicePlatform,deviceOs,eventId,currentUrl,ip,userId,occurTime,costTime,kafkaTime,activityId,channel,value,attributes,deviceId,profileCreateTime,ProfileCreateFrom,clientHubbleDeviceId) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      new JdbcStatementBuilder[ActivityEvent] {
        override def accept(ps: PreparedStatement, e: ActivityEvent): Unit = {
          //          println("activity event is: " + e)
          import e._
          ps.setString(1, platform)
          ps.setString(2, dataType)
          ps.setString(3, appKey)
          ps.setString(4, deviceUdid)
          ps.setString(5, devicePlatform)
          ps.setString(6, deviceOs)
          ps.setString(7, eventId)
          ps.setString(8, currentUrl)
          ps.setString(9, ip)
          ps.setLong(10, userId)
          ps.setLong(11, occurTime)
          ps.setLong(12, costTime)
          ps.setLong(13, kafkaTime)
          ps.setString(14, activityId)
          ps.setString(15, channel)
          ps.setLong(16, value)
          ps.setString(17, attributes)
          ps.setLong(18, deviceId)
          ps.setLong(19, profileCreateTime)
          ps.setString(20, profileCreateFrom)
          ps.setString(21, clientHubbleDeviceId)
        }
      },
      JdbcExecutionOptions.builder().withBatchSize(10000).withBatchIntervalMs(5000).withMaxRetries(5).build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withDriverName(clickhouseConfig.clickHouseDriver)
        .withUrl(clickhouseConfig.phyClickHouseJdbcUrl)
        .withUsername(clickhouseConfig.phyClickHouseUser)
        .withPassword(clickhouseConfig.phyClickHousePassword)
        .build()
    )

    env.fromSource(activitySource, WatermarkStrategy.noWatermarks(), "activity").addSink(chSink).uid("clickhouse")

    env.execute("lofter activity record sink to clickhouse")

  }

}
