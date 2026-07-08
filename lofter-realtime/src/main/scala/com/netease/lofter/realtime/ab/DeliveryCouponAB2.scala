package com.netease.lofter.realtime.ab

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.cache.{Cache, CacheBuilder}
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.Mda
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.MapStateDescriptor
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}

import java.time.{Duration, Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import scala.collection.JavaConverters.asScalaBufferConverter

private class DeliveryCouponAB2 {}
object DeliveryCouponAB2 {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[DeliveryCouponAB2])
  private val DF = DateTimeFormatter.ofPattern("yyyyMMdd")

  private case class DimUser(userId: Long, createTime: Long, isAnonymous: Int)
  private case class UserSource(eventId: String, userId: Long, tag: String, postId: Long, kafkaTime: Long, createTime: Long)
  private case class UserDeliveryCoupon(userId: Long, browseTimes: Long)
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("delivery_coupon_ab")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("delivery_coupon_ab")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val userDeliveryCouponSink = KafkaSink.builder[String]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("LOFTER.COUPON.NEWUSER.FREE_LIMITED")
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()
      ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(60))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val dimUserStream = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .setParallelism(1)
      .name("BinlogSourceStream")
      .uid("BinlogSourceStream")
      .flatMap { (s: SubscribeEvent, collector: Collector[DimUser]) =>
        s.getRowChanges.asScala
          .filter(_.getType == SubscribeEvent.RowChangeType.INSERT)
          .foreach { row =>
            row.getTableName.toLowerCase() match {
              case "profile" =>
                val userId = row.getColumn("UserID").getNewValue.asInstanceOf[Long]
                val email = row.getColumn("Email").getNewValue.asInstanceOf[String]
                val createTime = Math.min(s.getTimestamp, row.getColumn("ProfileCreateTime").getNewValue.asInstanceOf[Long])
                val isAnonymous = if (email != null && email.startsWith("100#")) 1 else 0
                val isMiniProgram = if (email != null && (email.startsWith("101#%") || email.startsWith("102#%"))) 1 else 0
                if (isMiniProgram == 0 && isAnonymous == 0) {
                  collector.collect(DimUser(userId, createTime, isAnonymous))
                }
              case _ =>
            }
          }
      }
      .setParallelism(1)
      .name("DimUserStream")
      .uid("DimUserStream")

    val userStateDescriptor = new MapStateDescriptor[String, Long]("DimUserState", classOf[String], classOf[Long])
    val dimUserBroadcastStream = dimUserStream.broadcast(userStateDescriptor)

    val newUserStream = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .setParallelism(4)
      .name("MdaSourceStream")
      .uid("MdaSourceStream")
      .filter(e => e.eventId == "g1-8" && e.userId.getOrElse(0L) > 0)
      .setParallelism(4)
      .name("UserSourceStreamFilter")
      .uid("UserSourceStreamFilter")
      .connect(dimUserBroadcastStream)
      .process(new BroadcastProcessFunction[Mda, DimUser, UserSource] {
        @transient private var scheduledExecutorService: ScheduledExecutorService = _
        private lazy val newUserCache: Cache[String, java.lang.Long] = CacheBuilder.newBuilder()
          .maximumSize(200000)
          .initialCapacity(100)
          .expireAfterWrite(1, TimeUnit.DAYS)
          .build()

        override def processElement(mda: Mda, ctx: BroadcastProcessFunction[Mda, DimUser, UserSource]#ReadOnlyContext, out: Collector[UserSource]): Unit = {
          val day = DF.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()))
          // val dimUserState = ctx.getBroadcastState(userStateDescriptor)
          val userId = mda.userId.getOrElse(0L)
          val eventId = mda.eventId
          val tag = mda.text.getOrElse("")
          val postId = mda.itemId.getOrElse(0L)
          val kafkaTime = mda.kafkaTime
          val key = day + "_" + userId

          if (newUserCache.getIfPresent(key) != null) {
            out.collect(UserSource(eventId, userId, tag, postId, kafkaTime, newUserCache.getIfPresent(key)))
          }
        }

        override def processBroadcastElement(dimUser: DimUser, ctx: BroadcastProcessFunction[Mda, DimUser, UserSource]#Context, out: Collector[UserSource]): Unit = {
          // val dimUserState = ctx.getBroadcastState(userStateDescriptor)
          val day = DF.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(dimUser.createTime), ZoneId.systemDefault()))
          // dimUserState.put(day + "_" + dimUser.userId, dimUser.createTime)
          newUserCache.put(day + "_" + dimUser.userId, dimUser.createTime)
        }
      })
      .setParallelism(4)
      .name("NewUserStreamProcessBroadcast")
      .uid("NewUserStreamProcessBroadcast")

    val deliveryCouponUserStream = newUserStream
      .keyBy(_.userId)
      .flatMap(new RichFlatMapFunction[UserSource, String] {
        @transient private var scheduledExecutorService: ScheduledExecutorService = _
        private lazy val userDeliveryCache: Cache[java.lang.Long, Integer] = CacheBuilder.newBuilder()
          .maximumSize(200000)
          .initialCapacity(100)
          .expireAfterWrite(1, TimeUnit.DAYS)
          .build()
        private var objectMapper: ObjectMapper = _

        override def open(parameters: Configuration): Unit = {
          objectMapper = new ObjectMapper()
          objectMapper.registerModules(DefaultScalaModule)
        }

        override def flatMap(value: UserSource, out: Collector[String]): Unit = {
          val oldState = userDeliveryCache.getIfPresent(value.userId)
          if (oldState != null) {
            val newState = oldState + 1
            userDeliveryCache.put(value.userId, newState)
            // LOG.info("=========当前userId: {}, flag: {}", value.userId, newState)
            if (newState >= 5) {
              //out.collect(objectMapper.writeValueAsString(UserDeliveryCoupon(userId = value.userId, browseTimes = newState)))
              out.collect(value.userId.toString)
            }
          } else {
            userDeliveryCache.put(value.userId, 1)
          }
        }
      })
      .setParallelism(4)
      .name("DeliveryCouponUserStreamFlatMap")
      .uid("DeliveryCouponUserStreamFlatMap")

    deliveryCouponUserStream
      .rebalance
      .sinkTo(userDeliveryCouponSink)
      .uid("DeliveryCouponUserStreamSink")
      .setParallelism(1)
      .name("DeliveryCouponUserStreamSink")

    env.execute("DeliveryCouponABTestJob")
  }
}
