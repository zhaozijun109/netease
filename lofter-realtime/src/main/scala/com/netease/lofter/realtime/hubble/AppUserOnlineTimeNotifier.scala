package com.netease.lofter.realtime.hubble

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{UserActiveNotifyMessage, UserSessionTick}
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
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

object AppUserOnlineTimeNotifier {
  val ONLINE_TIME_TOPIC: String = "lofter.session.time"
  val NOTIFY_TOPIC: String = "LOFTER.TRADE.GIFT.userlogin"

  val NOTIFY_THRESHOLD: Long = 15 * 60 * 1000L

  case class UserDaily(userId: Long, day: Int, total: Int, delta: Int)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(60000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(20000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTime = DateTime.now().withTimeAtStartOfDay().getMillis
    val onlineTimeTickSource =
      KafkaSource.builder[UserSessionTick]()
        .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
        .setTopics(ONLINE_TIME_TOPIC)
        .setGroupId("hubble_user_online_time_notifier_gy")
        //.setStartingOffsets(OffsetsInitializer.timestamp(startTime))
        .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
        .setValueOnlyDeserializer(new AvroJsonDeserSchema[UserSessionTick])
        .build()

    val notifySink = KafkaSink.builder[UserActiveNotifyMessage]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(NOTIFY_TOPIC)
        .setValueSerializationSchema(new AvroJsonSerSchema[UserActiveNotifyMessage])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val onlineTimeTick = env.fromSource(onlineTimeTickSource, WatermarkStrategy.noWatermarks(), "user-session-tick-source")

    onlineTimeTick.keyBy(_.userId)
      .process(new UserDailyTotalProcess)
      .flatMap { (e: UserDaily, collector: Collector[UserActiveNotifyMessage]) =>
        val current = e.total
        val old = e.total - e.delta

        if(current >= 60 * 60000L && old < 60 * 60000L) {
          collector.collect(UserActiveNotifyMessage(e.userId, 60))
        }

        if(current >= 30 * 60000L && old < 30 * 60000L) {
          collector.collect(UserActiveNotifyMessage(e.userId, 30))
        }

        if (current >= 15 * 60000L && old < 15 * 60000L) {
          collector.collect(UserActiveNotifyMessage(e.userId, 15))
        }

      }.sinkTo(notifySink)

    env.execute("lofter user online time notifier")
  }

  class UserDailyTotalProcess extends KeyedProcessFunction[Long, UserSessionTick, UserDaily] {
    lazy val dailySessionTime: MapState[Int, Int] =  {
      val stateDescriptor = new MapStateDescriptor[Int, Int]("daily-session-time", createTypeInformation[Int], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }
    override def processElement(event: UserSessionTick, ctx: KeyedProcessFunction[Long, UserSessionTick, UserDaily]#Context, out: Collector[UserDaily]): Unit = {
      import event._
      val day = new DateTime(time).toString("yyyyMMdd").toInt

      val dailyTotal = dailySessionTime.get(day) + durationDelta
      dailySessionTime.put(day, dailyTotal)

      out.collect(UserDaily(userId, day, dailyTotal, durationDelta))
    }
  }
}