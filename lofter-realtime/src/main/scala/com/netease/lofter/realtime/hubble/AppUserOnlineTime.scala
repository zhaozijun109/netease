package com.netease.lofter.realtime.hubble

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{Mda, UserSessionTick}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
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
import org.slf4j.{Logger, LoggerFactory}

private class AppUserOnlineTime{}

object AppUserOnlineTime {
  val LOG: Logger = LoggerFactory.getLogger(classOf[AppUserOnlineTime])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(20000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val logSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("hubble_user_online_time_gy")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val activeUserSink = KafkaSink.builder[UserSessionTick]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("lofter.session.time")
        .setValueSerializationSchema(new AvroJsonSerSchema[UserSessionTick])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val mda = env.fromSource(logSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")

    val activeUsers = mda.filter { event: Mda =>
      import event._

      eventId != "z-8888" && eventId != "rd-2" && !eventId.startsWith("ad-") &&
        deviceOs.isDefined &&
        sessionUuid.exists(_.length > 0) &&
        userId.exists(_ > 0) &&
        occurTime > 0
    }
      .keyBy(_.userId.get)
      .process(new UserSessionTimeProcess).uid("user-session-time")

    activeUsers.sinkTo(activeUserSink)

    env.execute("lofter user online time")
  }

  class UserSessionTimeProcess extends KeyedProcessFunction[Long, Mda, UserSessionTick] {
    lazy val sessionLastActiveTime: MapState[Int, Long] =  {
      val stateDescriptor = new MapStateDescriptor[Int, Long]("sessions", createTypeInformation[Int], createTypeInformation[Long])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(2)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    override def processElement(event: Mda, context: KeyedProcessFunction[Long, Mda, UserSessionTick]#Context, collector: Collector[UserSessionTick]): Unit = {
      import event._
      val lastSessionTime = sessionLastActiveTime.get(sessionUuid.hashCode)
      val gap = occurTime - lastSessionTime

      gap match {
        case _ if eventId == "z-6666" =>
          sessionLastActiveTime.put(sessionUuid.hashCode, 0L)

        case _ if lastSessionTime == 0 => // setup new session
          sessionLastActiveTime.put(sessionUuid.hashCode, occurTime)

          val isNewSession = Option(1)
          collector.collect(UserSessionTick(userId.get, deviceOs.get, occurTime, sessionUuid.get, 0, isNewSession))

        case x if x >= 600*1000 => // skip gap >= 10 minutes
          sessionLastActiveTime.put(sessionUuid.hashCode, occurTime)

        case x if x >= 10000 => // throttle small gap, only >= 10 seconds
          val delta = gap.toInt
          sessionLastActiveTime.put(sessionUuid.hashCode, occurTime)
          collector.collect(UserSessionTick(userId.get, deviceOs.get, occurTime, sessionUuid.get, delta, None))

        case _ => // do nothing
      }
    }
  }
}