package com.netease.lofter.realtime.gift

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.{GiftReturnPlanTagging, PveManUserReplyAggregate}
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.slf4j.{Logger, LoggerFactory}
import org.json4s.jackson.JsonMethods.parse

import scala.collection.JavaConverters._

private class GiftReturnPlanTaggingJob{}

object GiftReturnPlanTaggingJob {
  val logger: Logger = LoggerFactory.getLogger(classOf[GiftReturnPlanTaggingJob])

  case class PostChange(postId: Long, change: SubscribeEvent.OneRowChange)

  val PRICE_TAG = "price"
  val TRUNCATE_TAG = "truncate"

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val GIFT_TAG_SINK_TOPIC: String = "LOFTER.ReturnGift.tag"
    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("gift_return_plan_tagging_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val riskSink = KafkaSink.builder[GiftReturnPlanTagging]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder[GiftReturnPlanTagging]()
          .setTopic(GIFT_TAG_SINK_TOPIC)
          .setValueSerializationSchema(new AvroJsonSerSchema[GiftReturnPlanTagging])
          .build()
      ).build()

    val returnPostChanges = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[PostChange]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            row.getTableName match {
              case "Trade_ReturnGiftPlan" =>
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                collector.collect(PostChange(postId, row))
              case "Post" | "TextPost" =>
                val postId = row.getColumn("ID").getNewValue.asInstanceOf[Long]
                collector.collect(PostChange(postId, row))
              case _ =>
            }
          }
      }

    returnPostChanges
      .keyBy(_.postId)
      .process(new ReturnPostTaggingFunction)
      .sinkTo(riskSink)

    env.execute("return gift post plan tagging")
  }

  class ReturnPostTaggingFunction extends KeyedProcessFunction[Long, PostChange, GiftReturnPlanTagging] {
    lazy val postLengthState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor("length", createTypeInformation[Long]))
    lazy val contentTypeState: ValueState[Short] = getRuntimeContext.getState(new ValueStateDescriptor("contentType", createTypeInformation[Short]))
    lazy val tagState: MapState[String, String] = {
      val stateDescriptor = new MapStateDescriptor[String, String]("tag", createTypeInformation[String], createTypeInformation[String])
      getRuntimeContext.getMapState(stateDescriptor)
    }

    override def processElement(e: PostChange, ctx: KeyedProcessFunction[Long, PostChange, GiftReturnPlanTagging]#Context, out: Collector[GiftReturnPlanTagging]): Unit = {
      val value = e.change
      val table = value.getTableName

      table match {
        case "Post" =>
          val contentType = value.getColumn("Type").getNewValue.asInstanceOf[Int].toShort
          contentTypeState.update(contentType)
        case "TextPost" =>
          val content = value.getColumn("Content").getNewValue.asInstanceOf[String]
          val length = if(content != null) content.length else 0L
          postLengthState.update(length)
        case "Trade_ReturnGiftPlan" if value.getType != SubscribeEvent.RowChangeType.DELETE =>
          val length = postLengthState.value()
          if(length >= 8000) return
          val contentType = contentTypeState.value()
          if(contentType != 1) return

          val returnGiftId = value.getColumn("id").getNewValue.asInstanceOf[Long]
          val postId = value.getColumn("postId").getNewValue.asInstanceOf[Long]
          val blogId = value.getColumn("blogId").getNewValue.asInstanceOf[Long]
          val planType = value.getColumn("planType").getNewValue.asInstanceOf[Long]
          val returnText = value.getColumn("content").getNewValue.asInstanceOf[String]
          val returnTextLength: Int = Option(returnText).map(_.length).getOrElse(0)
          val giftJson = value.getColumn("giftJson").getNewValue.asInstanceOf[String]

          implicit val formats: org.json4s.Formats = DefaultFormats
          val giftIds = Option((parse(giftJson) \\ "giftIds").extract[Seq[Long]]).getOrElse(Seq.empty)
          val containFreeGift = giftIds.contains(3001)
          val containPayGift = giftIds.exists(_ != 3001)
          val minCoin = giftIds.map {
            case 1 => 9
            case 1001 => 19
            case 2001 => 99
            case 4001 => 29
            case 107501 => 9
            case 108501 => 19
            case 109501 => 52
            case 110501 => 199
            case 111501 => 299
            case 111502 => 19
            case 112501 => 9
            case 113501 => 19
            case _ => 0
          }.min

          val isReturnTextVeryLow = returnTextLength match {
            case v if v < 30 => true
            case v if v <= 150 => containPayGift
            case v if v <= 300 => !containFreeGift && minCoin >= 9
            case v if v <= 500 => !containFreeGift && minCoin >= 19
            case v if v <= 800 => !containFreeGift && minCoin >= 29
            case v if v <= 1500 => !containFreeGift && minCoin >= 99
            case _ => false
          }

          val isTruncate = length > 0 && length <= 500 && returnTextLength >= length * 3

          val priceTag = if((planType == 10001 || planType == 10002 || planType == 10004) && isReturnTextVeryLow) {
            "极低性价比"
          } else ""

          val truncateTag = if((planType == 10001 || planType == 10002 || planType == 10004) && isTruncate) {
            "超级截断"
          } else ""

          val oldTruncateTag = tagState.get(TRUNCATE_TAG)
          if(oldTruncateTag != truncateTag) {
            tagState.put(TRUNCATE_TAG, truncateTag)
            if(truncateTag.nonEmpty) {
              // reset new tag
              out.collect(GiftReturnPlanTagging(returnGiftId, postId, blogId, truncateTag, `type` = 1, action = 1))
            } else {
              // cancel old tag
              if(oldTruncateTag != null && oldTruncateTag.length > 0) {
                out.collect(GiftReturnPlanTagging(returnGiftId, postId, blogId, oldTruncateTag, `type` = 1, action = 0))
              }
            }
          }

          val oldPriceTag = tagState.get(PRICE_TAG)
          if(oldPriceTag != priceTag) {
            tagState.put(PRICE_TAG, priceTag)

            if(priceTag.nonEmpty) {
              // reset new tag
              out.collect(GiftReturnPlanTagging(returnGiftId, postId, blogId, priceTag, `type` = 1, action = 1))
            } else {
              // cancel old tag
              if(oldPriceTag != null && oldPriceTag.length > 0) {
                out.collect(GiftReturnPlanTagging(returnGiftId, postId, blogId, oldPriceTag, `type` = 1, action = 0))
              }
            }
          }
        case _ => // ignore
      }
    }
  }
}
