package com.netease.lofter.realtime.hubble

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{Mda, UserHotOps, Wda}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy

object AppUserAntiFraudHotOps {

  case class UserAction(app: String, userId: Long, action: String)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("hubble_user_antifraud_hot_ops_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val wdaSource = KafkaSource.builder[Wda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.web.online", "lofter.wap.online", "lofter.miniprogram.online")
      .setGroupId("hubble_user_antifraud_hot_ops_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[Wda](ignoreErrors = true))
      .build()

    val kafkaSink = KafkaSink.builder[UserHotOps]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("service.antifraud.user-hot-ops")
        .setValueSerializationSchema(new AvroJsonSerSchema[UserHotOps])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val mda = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .filter(e => e.userId.exists(_ > 0))
      .map { e =>
        val app = if(e.appKey == "MA-A4FE-A88932E7A98F") "Android" else "iOS"
        UserAction(app, e.userId.get, e.action.getOrElse(""))
      }

    val wda = env.fromSource(wdaSource, WatermarkStrategy.noWatermarks(), "lofter-wda-avro")
      .filter(s => s != null && s.userId.exists(_ > 0))
      .map { e =>
        val app = "wap"
        val userId = e.userId.get
        val action = e.attributes.flatMap(_.get("action")).getOrElse("")
        UserAction(app, userId, action)
      }

    mda.union(wda)
      .keyBy(e => (e.app, e.userId))
      .process(new UserActionMinuteSummary)
      .uid("user-action-sum")
      .sinkTo(kafkaSink)

    env.execute("app user antifraud hot ops")
  }

  class UserActionMinuteSummary extends KeyedProcessFunction[(String,Long), UserAction, UserHotOps] {
    lazy val lastTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("last-active-time", createTypeInformation[Long]))
    lazy val praiseState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("praise", createTypeInformation[Long]))
    lazy val recommendState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("recommend", createTypeInformation[Long]))
    lazy val otherState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("other", createTypeInformation[Long]))

    override def processElement(event: UserAction, ctx: KeyedProcessFunction[(String, Long), UserAction, UserHotOps]#Context, out: Collector[UserHotOps]): Unit = {
      val time = ctx.timerService().currentProcessingTime()
      val (app, userId) = ctx.getCurrentKey

      val currentMinute = (time / 60000) * 60000
      val lastTime = lastTimeState.value()
      val lastMinute = (lastTime / 60000) * 60000
      val isPraise = if(event.action == "107") 1 else 0
      val isRecommend = if(event.action == "108") 1 else 0

      if(currentMinute != lastMinute) {
        val id = s"${app}_${userId}_$lastMinute"
        out.collect(UserHotOps(id, app, userId, lastMinute, praiseState.value(), recommendState.value(), otherState.value()))

        lastTimeState.update(time)
        praiseState.update(isPraise)
        recommendState.update(isRecommend)
        otherState.update(1 - isPraise * isRecommend)
      } else if(isRecommend * isPraise > 0) {
        if(isPraise > 0) praiseState.update(praiseState.value() + isPraise)
        if(isRecommend > 0) recommendState.update(recommendState.value() + isRecommend)
        if(isPraise * isRecommend == 0) otherState.update(otherState.value() + 1)
        lastTimeState.update(time)
        // setup time clock
        ctx.timerService().registerProcessingTimeTimer(time + 60000)
      } else {
        // too much other ops updates affect performance
        val otherPv = otherState.value()
        if(otherPv > 0) {
          val slot = (time % 60000) / 10000
          if(slot == 3) {
            otherState.update(otherPv + 6) // approximate value
            lastTimeState.update(time)
            // setup time clock
            ctx.timerService().registerProcessingTimeTimer(time + 60000)
          }
        }
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[(String, Long), UserAction, UserHotOps]#OnTimerContext, out: Collector[UserHotOps]): Unit = {
      val lastTime = lastTimeState.value()
      val triggerTime = timestamp - 60000
      if(triggerTime >= lastTime) {
        val (app, userId) = ctx.getCurrentKey
        val triggerMinute = (triggerTime / 60000) * 60000
        val id = s"${app}_${userId}_$triggerMinute"
        out.collect(UserHotOps(id, app, userId, triggerMinute, praiseState.value(), recommendState.value(), otherState.value()))
      }
    }
  }
}
