package com.netease.lofter.realtime.hubble

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{AntiAddiction, UserSessionTick}
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state._
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

import java.util.Properties

object AppAntiAddiction {
  val ONLINE_TIME_TOPIC: String = "lofter.session.time"
  val NOTIFY_TOPIC: String = "lofter.push.anti.addiction"

  val REST_THRESHOLD: Long = 10 * 60 * 1000L
  val ADDICTION_THRESHOLD: Long = 2 * 60 * 60 * 1000L

  case class UserDaily(userId: Long, day: Int, total: Int, delta: Int)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(60000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(20000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)

    val onlineTimeTickSource =
      KafkaSource.builder[UserSessionTick]()
        .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
        .setTopics(ONLINE_TIME_TOPIC)
        .setGroupId("lofter_midnight_anti_addiction")
        .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
        .setValueOnlyDeserializer(new AvroJsonDeserSchema[UserSessionTick])
        .build()

    val notifySink = KafkaSink.builder[AntiAddiction]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(NOTIFY_TOPIC)
        .setValueSerializationSchema(new AvroJsonSerSchema[AntiAddiction])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val onlineTimeTick = env.fromSource(onlineTimeTickSource, WatermarkStrategy.noWatermarks(), "user-session-tick-source")

    onlineTimeTick.keyBy(_.userId)
      .process(new UserDailyTotalProcess)
      .flatMap { (e: UserDaily, collector: Collector[AntiAddiction]) =>
        val current = e.total
        val old = e.total - e.delta

        if(current >= ADDICTION_THRESHOLD && old < ADDICTION_THRESHOLD) {
          collector.collect(AntiAddiction(e.userId))
        }

      }.sinkTo(notifySink)

    env.execute("lofter midnight anti addiction")
  }

  class UserDailyTotalProcess extends KeyedProcessFunction[Long, UserSessionTick, UserDaily] {
    lazy val lastActiveTime: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("last-active-time", createTypeInformation[Long]))
    lazy val midnightNoRestTime: MapState[Int, Int] =  {
      val stateDescriptor = new MapStateDescriptor[Int, Int]("midnight-no-rest-time", createTypeInformation[Int], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }
    override def processElement(event: UserSessionTick, ctx: KeyedProcessFunction[Long, UserSessionTick, UserDaily]#Context, out: Collector[UserDaily]): Unit = {
      import event._
      val day = new DateTime(time).toString("yyyyMMdd").toInt
      val today = DateTime.now().toString("yyyyMMdd").toInt
      val hour = new DateTime(time).getHourOfDay

      if(day == today) {
        val restTime = event.time - lastActiveTime.value()
        if(restTime > 0) {
          lastActiveTime.update(time)
          val totalNoRestTime = if(restTime > REST_THRESHOLD) {
            midnightNoRestTime.put(day, durationDelta)
            durationDelta
          } else {
            val total = midnightNoRestTime.get(day) + durationDelta
            midnightNoRestTime.put(day, total)
            total
          }

          out.collect(UserDaily(userId, day, totalNoRestTime, durationDelta))
        }
      }
    }
  }
}
