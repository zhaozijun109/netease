package com.netease.lofter.realtime.ec

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{TraceProductAggregate, TraceProductEvent}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
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

object TraceProductEventSum {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[TraceProductEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("trace.product.detail")
      .setGroupId("trace_product_sum_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[TraceProductEvent])
      .build()

    val aggregateSink = KafkaSink.builder[TraceProductAggregate]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("trace.product.aggregate_v2")
          .setValueSerializationSchema(new AvroBinarySerSchema[TraceProductAggregate])
          .build()
      ).build()

    val eventDetail = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "trace-product-detail")
      .uid("trace-product-sum-input")

    eventDetail.keyBy(_.userId)
      .process(new ActionTaggingAggregateFunction).uid("trace-product-event-tagging")
      .sinkTo(aggregateSink).uid("trace-product-sum-output")

    env.execute("trace product event aggregate v2")
  }

  class ActionTaggingAggregateFunction extends KeyedProcessFunction[Long, TraceProductEvent, TraceProductAggregate] {

    private def actionState(name: String, ttlInHours: Int): MapState[(Int, Int, String), Long] = {
      val stateDescriptor = new MapStateDescriptor[(Int, Int, String), Long](name, createTypeInformation[(Int, Int, String)], createTypeInformation[Long])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(ttlInHours)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    private def productActionState(name: String, ttlInHours: Int): MapState[(Long, Int, Int, String), Long] = {
      val stateDescriptor = new MapStateDescriptor[(Long, Int, Int, String), Long](name, createTypeInformation[(Long, Int, Int, String)], createTypeInformation[Long])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(ttlInHours)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    // state dim: (period, productType, scene)
    lazy val actionDailyBitmapState: MapState[(Int, Int, String), Long] = actionState("day-action-bitmap", 24 + 6)
    lazy val actionHourlyBitmapState: MapState[(Int, Int, String), Long] = actionState("hour-action-bitmap", 1 + 2)
    lazy val productActionDailyBitmapState: MapState[(Long, Int, Int, String), Long] = productActionState("product-day-action-bitmap", 24 + 6)
    lazy val productActionHourlyBitmapState: MapState[(Long, Int, Int, String), Long] = productActionState("product-hour-action-bitmap", 1 + 2)
    lazy val firstOrderIdState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor("first-order", createTypeInformation[Long]))

    def actionMask(action: Int): Long = 1L << (action - 1)

    def setDailyAction(dim: (Int, Int, String), action: Int): Int = {
      val state = actionDailyBitmapState.get(dim)
      val mask = actionMask(action)
      val isActionSet = (state & mask).signum
      if(isActionSet == 0) {
        actionDailyBitmapState.put(dim, state | mask)
      }
      isActionSet
    }

    def setHourlyAction(dim: (Int, Int, String), action: Int): Int = {
      val state = actionHourlyBitmapState.get(dim)
      val mask = actionMask(action)
      val isActionSet = (state & mask).signum
      if(isActionSet == 0) {
        actionHourlyBitmapState.put(dim, state | mask)
      }
      isActionSet
    }

    def setProductDailyAction(dim: (Long, Int, Int, String), action: Int): Int = {
      val state = productActionDailyBitmapState.get(dim)
      val mask = actionMask(action)
      val isActionSet = (state & mask).signum
      if(isActionSet == 0) {
        productActionDailyBitmapState.put(dim, state | mask)
      }
      isActionSet
    }

    def setProductHourlyAction(dim: (Long, Int, Int, String), action: Int): Int = {
      val state = productActionHourlyBitmapState.get(dim)
      val mask = actionMask(action)
      val isActionSet = (state & mask).signum
      if(isActionSet == 0) {
        productActionHourlyBitmapState.put(dim, state | mask)
      }
      isActionSet
    }

    def setDimensionsFlag(dim: (Int, Int, String, Long), action: Int): Long = {
      // index: [1, 2] x  [1,2]  x   [1,2]   x  [1,2,3] - 1
      // result new int: bits denoting 12 bit vector
      val (dh, productType, scene, productId) = dim
      val dt = dh / 100
      val newIndexes = for (
        (dimProductId, h) <- Seq(0L, productId).zipWithIndex;
        (dimPeriod, i) <- Seq(dt, dh).zipWithIndex;
        (dimProduct, j) <- Seq(productType, 9999).zipWithIndex;
        (dimSceneRaw, k) <- Seq(scene, "sub", "all").zipWithIndex
      ) yield {
        val dimScene = if(dimSceneRaw == "sub" ) {
          if(scene.contains("benefit_")) "benefit_page" else "main_page"
        } else dimSceneRaw

        if(h == 0) {
          val dim = (dimPeriod, dimProduct, dimScene)
          val index = 6 * i + j * 3 + k
          val isNew = if( i == 0)  {
            1 - setDailyAction(dim, action) // daily
          } else {
            1 - setHourlyAction(dim, action) // hourly
          }

          index -> isNew
        } else {
          val dim = (dimProductId, dimPeriod, dimProduct, dimScene)
          val index = 12 * h + 6 * i + j * 3 + k
          val isNew = if( i == 0)  {
            1 - setProductDailyAction(dim, action) // daily
          } else {
            1 - setProductHourlyAction(dim, action) // hourly
          }

          index -> isNew
        }
      }
      val flag = newIndexes.filter(_._2 > 0).map(_._1).foldLeft(0L) { (bitmap, index) => bitmap | (1L << index) }
      flag
    }

    def setFirstOrder(orderId: Long): (Int, Int) = {
      val currentFirstOrder = firstOrderIdState.value()
      val isSet = if(currentFirstOrder > 0) 1 else 0
      val isFirst = if(currentFirstOrder == 0 || currentFirstOrder == orderId) 1 else 0

      if(isSet == 0) {
        firstOrderIdState.update(orderId)
      }

      (isSet, isFirst)
    }

    override def processElement(e: TraceProductEvent, ctx: KeyedProcessFunction[Long, TraceProductEvent, TraceProductAggregate]#Context,
                                out: Collector[TraceProductAggregate]): Unit = {

      val (orderNew, orderNewAmount) = if(e.action == 4 && e.orderId.exists(_ > 0) && e.amount.exists(_ > 0)) {
        val (isSet, isFirst) = setFirstOrder(e.orderId.get)
        val orderNewAmount = if(isFirst > 0) e.amount.getOrElse(.0) else .0
        (1 - isSet, orderNewAmount)
      } else (0, .0)

      val newFlag = setDimensionsFlag((e.dh, e.productType, e.scene, e.productId), e.action)

      val result = TraceProductAggregate(
        e.userId, e.action, e.scene, e.dt, e.dh, e.productId, e.productType, newFlag,
        e.amount.getOrElse(0), orderNew, orderNewAmount
      )
      out.collect(result)
    }
  }
}
