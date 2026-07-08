package com.netease.lofter.realtime.pve

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{PveEvent, PvePageCountDelta}
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import okhttp3.HttpUrl
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import scala.util.Try


private class PvePageCount {}

object PvePageCount {
  case class PveChatPageAccess(userId: Long, roleId: Long, time: Long)

  val LOG: Logger = LoggerFactory.getLogger(classOf[PvePageCount])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val latenessInSeconds = 60

    val wdaSource = KafkaSource.builder[PveEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.wap.online.json")
      .setGroupId("pve_page_count")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[PveEvent](ignoreErrors = true))
      .build()

    val sink = KafkaSink.builder[PvePageCountDelta]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("LOFTER.PVEMAN.count.rolevisit")
        .setKeySerializationSchema(new PvePageCountDeltaKeySerializer)
        .setValueSerializationSchema(new AvroJsonSerSchema[PvePageCountDelta])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val wapWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[PveEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[PveEvent] {
        override def extractTimestamp(element: PveEvent, recordTimestamp: Long): Long = element.occurTime
      })

    val pveChatPageAccess = env.fromSource(wdaSource, wapWaterMark, "wap")
      .flatMap { (e: PveEvent, collector: Collector[PveChatPageAccess]) =>

        if(e.eventId == "pve-1") {

          val userId = e.userId.getOrElse(0L)
          val time = e.occurTime
          val roleId = e.currentUrl.map(s => HttpUrl.parse(s)).flatMap {
            parsedUrl =>
              Option(parsedUrl.queryParameter("roleId")).flatMap { roleId => Try(roleId.toLong).toOption }
          }

          if(userId > 0 && roleId.getOrElse(0L) > 0L) {
            collector.collect(PveChatPageAccess(userId, roleId.get, time))
          } else {
          }
        }
      }

    val result: DataStream[PvePageCountDelta] = pveChatPageAccess.keyBy(_.roleId)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .aggregate(new PageCountingAggregate).uid("pve-page-counting")
      .map { args: (Long, Long) =>
        val (roleId, pv) = args
        PvePageCountDelta(roleId, pv, msgId = java.util.UUID.randomUUID().toString, time = System.currentTimeMillis(), hostname = "data_group")
      }

    result.sinkTo(sink)

    env.execute("pve page count")
  }

  class PvePageCountDeltaKeySerializer extends SerializationSchema[PvePageCountDelta] {
    override def serialize(element: PvePageCountDelta): Array[Byte] = {
      element.roleId.toString.getBytes("UTF-8")
    }
  }

  class PageCountingAggregate extends AggregateFunction[PveChatPageAccess, (Long, Long), (Long, Long)] {
    override def createAccumulator() = (0L, 0L)

    override def add(value: PveChatPageAccess, accumulator: (Long, Long)) = {
      (value.roleId, accumulator._2 + 1)
    }

    override def getResult(accumulator: (Long, Long)) = accumulator

    override def merge(a: (Long, Long), b: (Long, Long)) = (a._1, a._2 + b._2)
  }

}
