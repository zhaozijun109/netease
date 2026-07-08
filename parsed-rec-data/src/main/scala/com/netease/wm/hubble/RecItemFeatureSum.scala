package com.netease.wm.hubble

import com.netease.wm.hubble.avro.{RecItemEvent, RecItemFeature}
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
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.table.connector.ChangelogMode
import org.apache.flink.types.{Row, RowKind}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

import java.time.Duration

private class RecItemFeatureSum{}

object RecItemFeatureSum {
  val INTERACTION_REC_SCENES: Set[String] = Set("feed_rec", "related_item", "tag_rec", "tag_new")

  case class Event(itemId:String, itemType: String, scene: String, userId: Long, time: Long, dt: Int, action: Int, action_value: Int, interaction_type: Int, pay_amount: Double)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("RecItemFeatureSum")
      .setStartingOffsets(OffsetsInitializer.timestamp(startTimeStamp))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val aggregateSink = KafkaSink.builder[RecItemFeature]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(recConfig.itemFeatureTopic)
          .setValueSerializationSchema(new AvroJsonSerSchema[RecItemFeature])
          .build()
      ).build()

    val eventDetail: DataStream[Event] = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "rec-item-event-detail")
      .uid("rec-item-feature-input-new")
      .filter { e =>
        e.itemId.nonEmpty && ( e.action match {
          case 5 => e.repeat.getOrElse(0) == 0 && INTERACTION_REC_SCENES(e.scene)
          case 4 => INTERACTION_REC_SCENES(e.attribute_rec_scene)
          case 1 | 2 => INTERACTION_REC_SCENES(e.scene) && e.repeat.getOrElse(0) == 0
          case _ => e.repeat.getOrElse(0) == 0
        })
      }.map { e =>
      Event(e.itemId, e.itemType, e.scene, e.userId, e.time, e.dt, e.action, e.action_value, e.interaction_type.getOrElse(0), e.pay_amount.getOrElse(.0))
    }

    tableEnv.createTemporaryView("rec_item_event", eventDetail)

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "300 s")
    configuration.setString("table.exec.mini-batch.size", "1000000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofHours(30))

    val featureSql =
      """
        |select dt, itemId, itemType,
        |       count(1) filter (where action = 1 and scene <> 'tag_new') as exposure,
        |       count(1) filter (where action = 2 and scene <> 'tag_new') as click,
        |       count(1) filter (where action = 6) as videoPlay,
        |       sum(action_value) filter (where action = 5) as active,
        |       sum(pay_amount) filter (where action = 4) as gmv,
        |       count(1) filter (where action = 4) as orders,
        |       count(1) filter (where action = 1 and scene = 'feed_rec') as feed_rec_pv,
        |       count(1) filter (where action = 2 and scene = 'feed_rec') as feed_rec_click,
        |       count(1) filter (where action = 1 and scene = 'related_item') as similar_article_pv,
        |       count(1) filter (where action = 2 and scene = 'related_item') as similar_article_click,
        |       count(1) filter (where action = 1 and scene = 'tag_rec') as tag_rec_pv,
        |       count(1) filter (where action = 2 and scene = 'tag_rec') as tag_rec_click,
        |       count(1) filter (where action = 1 and scene = 'tag_new') as tag_new_exposure,
        |       count(1) filter (where action = 2 and scene = 'tag_new') as tag_new_click
        |from rec_item_event
        |group by dt, itemId, itemType
        |""".stripMargin

    tableEnv.sqlQuery(featureSql).toChangelogStream
      .filter(new FilterFunction[Row] {
        override def filter(value: Row): Boolean = {
          value.getKind == RowKind.INSERT || value.getKind == RowKind.UPDATE_AFTER
        }
      })
      .map{ x =>
            RecItemFeature("lofter", x.getField("dt").asInstanceOf[Int], x.getField("itemId").asInstanceOf[String], x.getField("itemType").asInstanceOf[String],
              x.getField("exposure").asInstanceOf[Long], x.getField("click").asInstanceOf[Long], x.getField("videoPlay").asInstanceOf[Long],
              x.getField("active").asInstanceOf[Int], x.getField("gmv").asInstanceOf[Double], x.getField("orders").asInstanceOf[Long],
              x.getField("feed_rec_pv").asInstanceOf[Long], x.getField("feed_rec_click").asInstanceOf[Long], x.getField("similar_article_pv").asInstanceOf[Long],
              x.getField("similar_article_click").asInstanceOf[Long], x.getField("tag_rec_pv").asInstanceOf[Long], x.getField("tag_rec_click").asInstanceOf[Long],
              x.getField("tag_new_exposure").asInstanceOf[Long], x.getField("tag_new_click").asInstanceOf[Long])
      }.sinkTo(aggregateSink).uid("rec-item-feature-sink")

    env.execute("rec item feature v2")
  }
}

