package com.netease.lofter.realtime.growth

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{RecItemEvent, SurpriseBoxPost, TraceProductAggregate, TraceProductEvent}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state._
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

/**
 * 惊喜盒子-高频分享用户多次访问掉落
 *
 * 圈定用户：近7天分享2次非自己发文内容的用户
 * 触发行为：7日内第二次进入单日志页触发（第一次需要是有效浏览）
 * 限制频次：30天仅触发一次
 * 奖励：分享成功后获得头像框奖励（ID待同步）
 *
 */
object SurpriseBox {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("surprise_box_sum")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val aggregateSink = KafkaSink.builder[SurpriseBoxPost]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.PUPUP.newHighShare")
          .setValueSerializationSchema(new AvroJsonSerSchema[SurpriseBoxPost])
          .build()
      ).build()

    val eventDetail = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "trace-product-detail")
      .uid("surprise-box-sum-input")

    eventDetail.filter(_.userId > 0).keyBy(_.userId)
      .process(new UserShareablePostFunction).uid("surprise-box-shareable-identify")
      .sinkTo(aggregateSink).uid("surprise-box-shareable-output")

    env.execute("surprise box shareable posts")
  }

  class UserShareablePostFunction  extends KeyedProcessFunction[Long, RecItemEvent, SurpriseBoxPost] {
    lazy val sharedState: ValueState[(Long, Long)] = {
      val stateDescriptor = new ValueStateDescriptor[(Long, Long)]("recent-shares", createTypeInformation[(Long, Long)])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(7)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getState(stateDescriptor)
    }

    override def processElement(e: RecItemEvent, ctx: KeyedProcessFunction[Long, RecItemEvent, SurpriseBoxPost]#Context, out: Collector[SurpriseBoxPost]): Unit = {
      e.action match {
        case 5 if e.interaction_type.exists(_ == 6) && e.itemId.contains("_") => //share
          val blogIdHex = e.itemId.split("_").head
          val blogId = java.lang.Long.parseLong(blogIdHex, 16)
          if(blogId != e.userId) { // not self post share
            val recentShares = sharedState.value()
            val lastShareTime = if(recentShares == null) 0L else recentShares._1
            val newRecentShares = (e.time, lastShareTime)
            sharedState.update(newRecentShares)
          }
        case 3 if e.itemId.nonEmpty => // browse
          val recentShares = sharedState.value()
          if(recentShares != null) {
            val (a, b) = recentShares
            val sevenDaysAgo = DateTime.now().minusDays(7).getMillis
            if(a > sevenDaysAgo && b > sevenDaysAgo) {
              out.collect(SurpriseBoxPost(e.userId, e.itemId))
            }
          }
        case _ => // just ignore
      }
    }
  }
}
