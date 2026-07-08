package com.netease.wm.hubble

import com.netease.wm.hubble.avro.{RecItemEvent, RecSceneCtr}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import com.netease.wm.hubble.common.{kafkaConfig, recConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Schema
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.{Row, RowKind}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

import java.sql.Timestamp
import java.time.{Duration, ZoneOffset}

object RecSceneCtrSum {

  case class Event(itemId:String, itemType: String, scene: String, userId: Long, action: Int, eventTime: java.sql.Timestamp)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(300000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("RecSceneCtrV2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val aggregateSink = KafkaSink.builder[RecSceneCtr]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("rec.scene.ctr")
          .setValueSerializationSchema(new AvroJsonSerSchema[RecSceneCtr])
          .build()
      ).build()

    val events = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "rec-item-event-detail")
      .uid("rec-item-feature-input-new")
      .filter(e => e.action <= 2 && (e.scene == "feed_rec" || e.scene == "tag_rec" || e.scene == "related_item"))
      .map { e =>
        Event(e.itemId, e.itemType, e.scene, e.userId, e.action, new java.sql.Timestamp(e.time))
      }

    tableEnv.createTemporaryView("rec_item_event", events,
      Schema.newBuilder()
        .columnByExpression("rowTime", "CAST(eventTime AS TIMESTAMP_LTZ(3))")
        .watermark("rowTime", "rowTime - INTERVAL '10' SECOND")
        .build()
    )

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "10 s")
    configuration.setString("table.exec.mini-batch.size", "100000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofHours(30))

    val featureSql =
      """
        |select window_start, window_end, scene,
        |       count(1) filter (where action = 1) as exposure,
        |       count(1) filter (where action = 2) as click
        |from (
        |    select window_start, window_end, scene, itemId, itemType, userId, action
        |    from table(
        |        tumble(table rec_item_event, descriptor(rowTime), interval '10' minutes)
        |    ) t
        |    group by window_start, window_end, scene, itemId, itemType, userId, action
        |) tt
        |group by window_start, window_end, scene
        |""".stripMargin

    tableEnv.sqlQuery(featureSql).toChangelogStream
      .filter(new FilterFunction[Row] {
        override def filter(value: Row): Boolean = {
          value.getKind == RowKind.INSERT || value.getKind == RowKind.UPDATE_AFTER
        }
      })
      .map{ x =>
        RecSceneCtr(x.getField("scene").asInstanceOf[String], Timestamp.valueOf(x.getField("window_start").asInstanceOf[java.time.LocalDateTime]).getTime,
          x.getField("exposure").asInstanceOf[Long], x.getField("click").asInstanceOf[Long])
      }.sinkTo(aggregateSink).uid("rec-scene-ctr-sink")

    env.execute("rec scene ctr")
  }
}
