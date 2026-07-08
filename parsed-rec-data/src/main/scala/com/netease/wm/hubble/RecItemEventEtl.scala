package com.netease.wm.hubble

import com.google.gson.Gson
import com.lofter.rs.basic.bean.dto.upload.{ActionDto, ActionSourceDto, ExtraData}
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.wm.hubble.avro.RecItemEvent
import com.netease.wm.hubble.common.avro.binary.AvroBinarySerSchema
import com.netease.wm.hubble.common.{SubscribeEventSchema, kafkaConfig, recConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import com.alibaba.fastjson.JSON
import com.lofter.rs.basic.bean.dto.upload.ActionSourceDto

import scala.collection.JavaConverters._
import scala.util.Try

object RecItemEventEtl {
  val gson = new Gson()

  def getPermalink(postId: Long, blogId: Long): String = {
    blogId.toHexString + "_" + postId.toHexString
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    //val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val recAction = KafkaSource.builder[String]()
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setTopics(recConfig.destTopic)
      .setGroupId("RecItemEventEtl")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema)
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("RecItemEventEtl")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val recItemEventSink = KafkaSink.builder[RecItemEvent]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("rec.item.detail")
          .setValueSerializationSchema(new AvroBinarySerSchema[RecItemEvent])
          .build()
      ).build()

    val clientEvents = env.fromSource(recAction, WatermarkStrategy.noWatermarks(), "rec-action")
      .uid("rec-action")
      .flatMap { (line: String, collector: Collector[RecItemEvent]) =>
        val e: ActionDto = gson.fromJson[ActionDto](line, classOf[ActionDto])
        val eventId = Option(e.getEventId)
        val actionType = Option(e.getRating).map(_.toInt)
        val (action, actionValue) = (eventId, actionType) match {
          case (Some("b1-45" | "g1-40" | "f1-46" | "a2-69"), _) =>  (1, 1) // expose
          case (Some("b1-46" | "g1-41" | "f1-33" | "a2-5"), _) =>  (2, 1) // click
          case (Some("g1-57"), _) if e.getCost() > 3000 => (3, 1) // browse
          case (Some("l3-1"), _) if e.getCost() > 5000 => (3, 1) // browse
          case (Some("a2-9" | "g1-29" | "a2-12" | "g1-31" | "z4-1" | "z4-2" | "z4-3" | "z4-4" | "z4-5" | "z4-6" |
                     "z4-7" | "z4-8" | "z4-9" | "z4-10" | "z4-26" | "z4-20" | "g1-62" | "g1-6" | "b8-12"),
                Some(124 | 105 | 107 | 108 | 115)) =>
            (5, 1) // positive interaction
          case (Some("a2-10" | "g1-36" | "a2-13" | "g1-37" | "z4-21" | "g1-63"), Some(-124 | -109 | -108)) =>
            (5, -1) // negative interaction
          case (Some("g1-42"), Some(202)) => (5, 1) // positive interaction
          case (Some("g1-43"), Some(-202)) => (5, -1) // negative interaction
          case (Some("l1-2" | "b8-2"), _) => (6, 1) // video play
          // case (Some("g1-62" | "z4-20"), Some(115)) => (5, 1)
          case _ =>  (0, 0) // ignore
        }

        val interaction_type = eventId match {
          case Some("a2-9" | "g1-29") => Some(1)
          // case Some("g1-62" | "z4-20") => Some(2)
          case Some("a2-12" | "g1-31") => Some(3)
          case Some("g1-62" | "z4-20") => Some(4)
          case Some("g1-6" | "b8-12") => Some(5)
          case Some("z4-1" | "z4-2" | "z4-26" | "z4-3" | "z4-4" | "z4-5" | "z4-6" | "z4-7" ) => Some(6)
          case _ => None
        }

        if(action > 0) {
          val extOption = if(e.getExtraData != null && e.getExtraData.length > 0 ) {
            Try{ gson.fromJson(e.getExtraData, classOf[ExtraData]) }.toOption
          } else None

          val (itemId, itemType) = eventId match {
            case Some("g1-40" | "g1-41" | "g1-42" | "g1-43") if extOption.isDefined =>
              (Option(e.getText).getOrElse(""), Option(extOption.get.getRelatedItemType).getOrElse(""))
            case _ => (Option(e.getItemId).getOrElse(""), Option(e.getItemType).getOrElse(""))
          }

          val dt = new DateTime(e.getTime).toString("yyyyMMdd").toInt
          val userId = Option(e.getAccount).map(_.trim) match {
            case Some(s) if s.nonEmpty && s.forall(_.isDigit) => s.toLong
            case _ => 0L
          }
          val scene = if(e.getExtData != null &&
                         (e.getExtData.getTab == "最新" || e.getExtData.getTab == "最新发布" || e.getExtData.getTab == "最新评论" ) &&
                         e.getScene.toLowerCase == "unknow") {
            "tag_new"
          } else if(e.getExtData != null && e.getExtData.getPageScene == "note") {
            "related_item"
          } else Option(e.getScene).getOrElse("")

          val sourceScene = if(e.getScene == "immerse_collection" || e.getScene.toLowerCase == "collection") {
            Some("collection")
          } else {
            val sourceSceneOption = if (e.getSourceLink != null && e.getSourceLink.length > 0) {
              val outputList = JSON.parseArray(e.getSourceLink, classOf[ActionSourceDto])
              if (outputList.size() > 0 && outputList.get(0) != null ) Option(outputList.get(0).getScene) else None
            } else None
            val recommendAttention = eventId match {
              case Some("a2-69" | "a2-5") =>
                e.getExtData.getExtInfoType == "0"
              case _ => false
            }

            if(sourceSceneOption.exists(s => s == "immerse_collection" || s == "collection" || s == "floatcollection" || s == "subscribe" || s == "mysubscription" )) {
              Some("collection")
            } else if(recommendAttention) {
              Some("recommend")
            } else sourceSceneOption
          }

          val repeat = Option(e.getExtData).map(_.getRepeat.toInt)
          val event = RecItemEvent(itemId, itemType, userId, e.getTime, dt, action, actionValue, scene, interaction_type, repeat = repeat, source_scene = sourceScene)
          collector.collect(event)
        }
      }

    val tradeEvents = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .uid("ndc-binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[RecItemEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Trade_FansVipOrder" | "Trade_BlogVipOrder"  =>
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                val blogId = row.getColumn("vipBlogId").getNewValue.asInstanceOf[Long]
                val amount = Option(row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal]).map(_.doubleValue()).getOrElse(.0)
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val time = s.getTimestamp
                val dt = new DateTime(time).toString("yyyyMMdd").toInt
                val paySuccess = changeType match {
                  case RowChangeType.INSERT => status == 1
                  case RowChangeType.UPDATE => status == 1 && oldStatus == 0
                  case _ => false
                }

                if(paySuccess) {
                  val itemId = if(postId > 0) getPermalink(postId, blogId) else ""
                  collector.collect(
                    RecItemEvent(itemId, itemType = "ARTICLE", userId, time, dt,
                      action = 4, action_value = 1, pay_amount = Some(amount))
                  )
                }
              case "Trade_GiftPresentRecord" =>
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                val blogId = row.getColumn("blogId").getNewValue.asInstanceOf[Long]
                val coin = row.getColumn("coin").getNewValue.asInstanceOf[Long]
                val amount = coin * 0.1
                val userId = row.getColumn("sender").getNewValue.asInstanceOf[Long]
                val time = s.getTimestamp
                val dt = new DateTime(time).toString("yyyyMMdd").toInt
                val paySuccess = changeType match {
                  case RowChangeType.INSERT => coin > 0
                  case _ => false
                }
                if(paySuccess) {
                  val itemId = if(postId > 0) getPermalink(postId, blogId) else ""
                  collector.collect(
                    RecItemEvent(itemId, itemType = "ARTICLE", userId, time, dt,
                      action = 4, action_value = 1, pay_amount = Some(amount))
                  )
                }
              case _ =>
            }
          }
      }

    val itemEvents = clientEvents.union(tradeEvents)
    itemEvents
      .keyBy(_.userId)
      .process(new RecItemPayAttribution).uid("item-pay-attribute")
      .sinkTo(recItemEventSink).uid("item-detail-sink")

    env.execute("rec item event etl")
  }

  val PAY_ATTRIBUTION_PERIOD: Long = 30 * 60 * 1000L

  class RecItemPayAttribution extends KeyedProcessFunction[Long, RecItemEvent, RecItemEvent] {
    lazy val clickActionState: MapState[String, (String, Long)] = getClickActionState("click-events", 1)

    private def getClickActionState(name: String, ttlInHours: Int): MapState[String, (String, Long)] = {
      val stateDescriptor = new MapStateDescriptor[String, (String, Long)](name, createTypeInformation[String], createTypeInformation[(String, Long)])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(ttlInHours)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    override def processElement(e: RecItemEvent, ctx: KeyedProcessFunction[Long, RecItemEvent, RecItemEvent]#Context, collector: Collector[RecItemEvent]): Unit = {
      e.action match {
        case 2 =>
          if(e.itemId.nonEmpty && e.scene.nonEmpty) {
            clickActionState.put(e.itemId, (e.scene, e.time))
          }
          collector.collect(e)
        case 4 =>
          var attributed: Boolean = false
          if(e.itemId.nonEmpty) {
            val state = clickActionState.get(e.itemId)
            if(state != null ) {
              val (recScene, recTime) = state
              if(e.time > recTime && e.time - recTime < PAY_ATTRIBUTION_PERIOD) {
                collector.collect(e.copy(attribute_rec_scene = recScene))
                attributed = true
              }
            }
          }

          if(!attributed) {
            collector.collect(e)
          }
        case _ =>
          collector.collect(e)
      }
    }
  }
}
