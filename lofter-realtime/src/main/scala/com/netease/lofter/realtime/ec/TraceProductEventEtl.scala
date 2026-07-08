package com.netease.lofter.realtime.ec

import com.google.common.util.concurrent.MoreExecutors
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.{Mda, TraceProductEvent, TraceRequestMeta, Wda}
import com.netease.wm.hubble.common.avro.binary.{AvroBinaryDeserSchema, AvroBinarySerSchema}
import com.netease.wm.hubble.common.avro.json.AvroJsonDeserSchema
import okhttp3.HttpUrl
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

private class TraceProductEventEtl{}

object TraceProductEventEtl {
  case class TraceAction(traceId: String, userId: Long, action: Int, time: Long, amount: Option[Double] = None, orderId: Option[Long] = None )

  val LOG: Logger = LoggerFactory.getLogger(classOf[TraceProductEventEtl])

  def isValidTraceId(traceId: Option[String]): Boolean = {
    if(traceId.isEmpty || traceId.get == null || traceId.get.isEmpty) return false
    val normalized = traceId.get.toLowerCase().trim
    normalized.nonEmpty && !normalized.startsWith("lofter") && !normalized.startsWith("lft-")
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    //val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val latenessInSeconds = 60

    val traceMetaSource = KafkaSource.builder[TraceRequestMeta]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("trace.request.meta")
      .setGroupId("trace_product_event_etl_meta")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[TraceRequestMeta](ignoreErrors = true))
      .build()

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("trace_product_event_etl_mda")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val wdaSource = KafkaSource.builder[Wda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.wap.online.json")
      .setGroupId("trace_product_event_etl_wap")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[Wda](ignoreErrors = true))
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("trace_product_event_etl_binlog")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val dwdResultSink = KafkaSink.builder[TraceProductEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("trace.product.detail")
          .setValueSerializationSchema(new AvroBinarySerSchema[TraceProductEvent])
          .build()
      ).build()

    val mdaWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[Mda](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[Mda] {
        override def extractTimestamp(element: Mda, recordTimestamp: Long): Long = element.kafkaTime
      })

    val clientActions = env.fromSource(mdaSource, mdaWaterMark, "mda")
      .rebalance
      .flatMap { (e: Mda, collector: Collector[TraceAction]) =>
        val action = e.eventId match {
          case "b1-45" | "g1-40" | "f1-46" | "w1-18" =>  1 // expose
          case "b1-46" | "g1-41" | "f1-33" | "w1-19" =>  2 // click
          case _ =>  0 // ignore
        }

        if(action > 0) {
          // val productId = e.itemId.getOrElse(0L)
          val traceId = e.eventId match {
            case "w1-18" | "w1-19" => e.adTrace
            case _ =>
              e.url.flatMap{ url =>
                Option(HttpUrl.parse(url)).flatMap { parsedUrl => Option(parsedUrl.queryParameter("adTrace"))}
              }
          }
          val userId = e.userId.getOrElse(0L)
          val time = e.kafkaTime
          if(isValidTraceId(traceId)) {
            collector.collect(TraceAction(traceId.get, userId, action = action, time = time))
          }
        }
      }

    val wapWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[Wda](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[Wda] {
        override def extractTimestamp(element: Wda, recordTimestamp: Long): Long = element.occurTime
      })

    val wapActions = env.fromSource(wdaSource, wapWaterMark, "wap")
      .flatMap { (e: Wda, collector: Collector[TraceAction]) =>
        val userId = e.userId.getOrElse(0L)
        val time = e.occurTime
        val action = 3 // browse

        val traceId = e.currentUrl.flatMap{ url =>
          Option(HttpUrl.parse(url)).flatMap { parsedUrl => Option(parsedUrl.queryParameter("adTrace"))}
        }
        if(isValidTraceId(traceId) && userId > 0) {
          collector.collect(TraceAction(traceId.get, userId, action = action, time = time))
        }
      }

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val trades = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[TraceAction]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
          row.getTableName.toLowerCase match {
            case "benefit_order_product" =>
              //val productId = row.getColumn("productid").getNewValue.asInstanceOf[Long]
              val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
              val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
              val traceId = row.getColumn("adTrace").getNewValue.asInstanceOf[String]
              val paySuccess = changeType match {
                case RowChangeType.INSERT => status == 1
                case RowChangeType.UPDATE => status == 1 && oldStatus == 0
              }
              // only process benefit_order_product transition from status initiated to payed
              if(paySuccess && isValidTraceId(Some(traceId))) {
                val userId = row.getColumn("buyerId").getNewValue.asInstanceOf[String].toLong
                val orderId = row.getColumn("orderId").getNewValue.asInstanceOf[Long]
                val time = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
                val productNum = row.getColumn("productNum").getNewValue.asInstanceOf[Int]
                val storePrice = row.getColumn("storePrice").getNewValue.asInstanceOf[java.math.BigDecimal].doubleValue()
                val bountyPreferential = row.getColumn("bountyPreferential").getNewValue.asInstanceOf[java.math.BigDecimal].doubleValue()
                val newCouponPreferential = row.getColumn("newCouponPreferential").getNewValue.asInstanceOf[java.math.BigDecimal].doubleValue()
                val amount = (storePrice  * productNum) - bountyPreferential - newCouponPreferential

                collector.collect(TraceAction(traceId, userId, action = 4, time = time, amount = Some(amount), orderId = Some(orderId)))
              }
            case _ =>
          }
        }
    }

    val traceMetaWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[TraceRequestMeta](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[TraceRequestMeta] {
        override def extractTimestamp(element: TraceRequestMeta, recordTimestamp: Long): Long = element.requestTime
      })

    val traceMeta = env.fromSource(traceMetaSource, traceMetaWaterMark, "trace-request-meta")
      .keyBy(_.traceId)

    // join mda trade with trace meta
    val actions = clientActions.union(wapActions).union(trades).keyBy(_.traceId)

    // TODO process unmatched traced actions
    // maybe there is data missing in trace meta
    traceMeta.intervalJoin(actions)
      .between(Time.minutes(- 1), Time.hours(24))
      .process(new TraceActionMetaResolve).uid("trace-action-meta-resolve")
      .sinkTo(dwdResultSink)

    env.execute("trace product event etl")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class TraceActionMetaResolve extends ProcessJoinFunction[TraceRequestMeta, TraceAction, TraceProductEvent] {
    override def processElement(meta: TraceRequestMeta, value: TraceAction, ctx: ProcessJoinFunction[TraceRequestMeta,
      TraceAction, TraceProductEvent]#Context, out: Collector[TraceProductEvent]): Unit = {
      val dt = new DateTime(value.time).toString("yyyyMMdd").toInt
      val dh = new DateTime(value.time).toString("yyyyMMddHH").toInt

      out.collect(
        TraceProductEvent(value.traceId, value.userId, value.time, meta.scene, meta.itemId, meta.itemType,
          dt, dh, value.action, value.amount, value.orderId))
    }
  }

}
