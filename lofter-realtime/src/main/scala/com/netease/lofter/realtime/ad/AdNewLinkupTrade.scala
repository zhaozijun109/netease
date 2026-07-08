package com.netease.lofter.realtime.ad

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.{AdNewLinkup, NewDeviceTrade}
import com.netease.wm.hubble.common.avro.json.{AvroDsJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy

import scala.collection.JavaConverters._
import java.time.Duration

/**
 * match ad new linkup device with orders in 24h
 */
object AdNewLinkupTrade {
  case class Trade(deviceId: String, orderId: Long, orderType: Int, orderAmount: Double, payTime: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val latenessInSeconds = 60

    val adNewLinkupSource = KafkaSource.builder[AdNewLinkup]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("adx.newlinkup.online")
      .setGroupId("ad_new_linkup_trade")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroDsJsonDeserSchema[AdNewLinkup](ignoreErrors = true))
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("ad_new_linkup_trade")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val newDeviceTradeSink = KafkaSink.builder[NewDeviceTrade]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter.aso.pay")
          .setKeySerializationSchema(new DeviceActionIdSchema)
          .setValueSerializationSchema(new AvroJsonSerSchema[NewDeviceTrade])
          .build()
      ).build()

    val adNewLinkupWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[AdNewLinkup](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[AdNewLinkup] {
        override def extractTimestamp(element: AdNewLinkup, recordTimestamp: Long): Long = element.time
      })

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val adNewDevices = env.fromSource(adNewLinkupSource, adNewLinkupWaterMark, "ad-new-linkup")
      .uid("ad-new-linkup-input")
      .filter(_.newUserFlag)

    val trades = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[Trade]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Trade_StoreVipOrder" | "Trade_FansVipOrder" | "Trade_PVEStaminaOrder" =>
                val deviceId = row.getColumn("deviceId").getNewValue.asInstanceOf[String]
                val orderId = row.getColumn("id").getNewValue.asInstanceOf[Long]
                val orderType = row.getTableName match {
                  case "Trade_FansVipOrder" => 2
                  case "Trade_StoreVipOrder" => 3
                  case "Trade_PVEStaminaOrder" => 4
                  case _ => -1
                }

                val payTime = row.getColumn("finishTime").getNewValue.asInstanceOf[Long]
                val orderAmount = row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal].doubleValue()
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]

                val paySuccess = if(changeType == RowChangeType.INSERT) status == 1 else (oldStatus != 1 && status == 1)

                if(paySuccess && orderType > 0 && deviceId != null) {
                  collector.collect(Trade(deviceId, orderId, orderType, orderAmount, payTime))
                }
              case _ =>
            }
          }
      }

    adNewDevices.keyBy(_.deviceId)
      .connect(trades.keyBy(_.deviceId))
      .process(new NewTradeCombiner)
      .sinkTo(newDeviceTradeSink)

    env.execute("ad new linkup trade")
  }

  class NewTradeCombiner extends KeyedCoProcessFunction[String, AdNewLinkup, Trade, NewDeviceTrade] {
    lazy val activateState: ValueState[(Long, Long)] = getRuntimeContext.getState[(Long, Long)](new ValueStateDescriptor[(Long, Long)]("activate", createTypeInformation[(Long, Long)]))
    lazy val lateOrders: MapState[Long, Trade] = {
      val descriptor = new MapStateDescriptor[Long, Trade]("late-orders", createTypeInformation[Long], createTypeInformation[Trade])
      val ttl = StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build()
      descriptor.enableTimeToLive(ttl)
      getRuntimeContext.getMapState[Long, Trade](descriptor)
    }

    private val TRADE_PERIOD: Long = 24 * 3600L * 1000L

    override def processElement1(value: AdNewLinkup, ctx: KeyedCoProcessFunction[String, AdNewLinkup, Trade, NewDeviceTrade]#Context, out: Collector[NewDeviceTrade]): Unit = {
      val isLofter =  value.appId.isEmpty || value.appId.get == "lofter"
      if(value.newUserFlag && isLofter) {
        activateState.update((value.actionId, value.time))
      }
    }

    override def processElement2(value: Trade, ctx: KeyedCoProcessFunction[String, AdNewLinkup, Trade, NewDeviceTrade]#Context, out: Collector[NewDeviceTrade]): Unit = {
      import value._
      val activate = activateState.value()

      if(activate == null) {
        lateOrders.put(payTime, value)
        ctx.timerService().registerEventTimeTimer(payTime)
      } else {
        val (actionId, activateTime) = activate
        if(payTime > activateTime && payTime < activateTime + TRADE_PERIOD) {
          val newDeviceTrade = NewDeviceTrade(deviceId, orderId, orderType, payTime, orderAmount, actionId)
          out.collect(newDeviceTrade)
        }
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedCoProcessFunction[String, AdNewLinkup, Trade, NewDeviceTrade]#OnTimerContext, out: Collector[NewDeviceTrade]): Unit = {
      val activate = activateState.value()
      if(activate != null) {
        val (actionId, activateTime) = activate
        val trade = lateOrders.get(timestamp)

        if(trade != null && trade.payTime < activateTime + TRADE_PERIOD) {
          val newDeviceTrade = NewDeviceTrade(trade.deviceId, trade.orderId, trade.orderType, trade.payTime, trade.orderAmount, actionId)
          out.collect(newDeviceTrade)
        }
      }
    }
  }

  class DeviceActionIdSchema extends SerializationSchema[NewDeviceTrade] {
    override def serialize(v: NewDeviceTrade): Array[Byte] = {
      v.actionId.toString.getBytes("UTF-8")
    }
  }
}
