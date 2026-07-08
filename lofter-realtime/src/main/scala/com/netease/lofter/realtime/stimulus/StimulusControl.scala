package com.netease.lofter.realtime.stimulus

import com.netease.lofter.realtime.common.kafkaConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.joda.time.{DateTime, Days}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.slf4j.{Logger, LoggerFactory}

import java.nio.charset.StandardCharsets
import java.util.Properties
import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.control.NonFatal

private class StimulusControl{}
/**
 * 需要对私信激励离线和实时任务产生的数据做用户去重以及做概率发送逻辑， 防止多次私信同一用户
 *
 *  实现步骤如下：
 *  1. 消费私信staging topic内容，
 *  2. 概率发送处理，记录用户私信发送状态
 *  3. 写入实现sending topic
 */
object StimulusControl {
  val LOG: Logger = LoggerFactory.getLogger(classOf[StimulusControl])

  case class StimulusPlan(userId: Long, stimulateType: String, stagingPlan: String)
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "lofter_tag_post_stimulus_online_v2_gy")
    properties.setProperty("auto.offset.reset", "latest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val stagingTopic = "lofter.creator-stimulus-pm.staging"
    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](Seq(stagingTopic).asJava, new SimpleStringSchema(), properties)
    )

    val onlineTopic = "lofter.creator-stimulus-pm.online"
    val onlineSink = new FlinkKafkaProducer(onlineTopic, new MyKafkaSerialization(onlineTopic), properties, FlinkKafkaProducer.Semantic.EXACTLY_ONCE)

    val plans: DataStream[StimulusPlan] = logSource
      .flatMap{line => parseStimulusPlan(line)}
      .uid("stimulus-plans-gy")

    plans.keyBy(_.userId)(createTypeInformation[Long])
      .process(new ControlFunction).uid("lucky-pick")
      .addSink(onlineSink).uid("stimulus-control-sink-gy")

    env.execute("lofter message stimulus control")
  }

  class ControlFunction extends KeyedProcessFunction[Long, StimulusPlan, String] {
    lazy val stimulusHistoryState: MapState[String, Long] = getRuntimeContext.getMapState(new MapStateDescriptor[String, Long]("stimulus-plan-history", createTypeInformation[String], createTypeInformation[Long]))

    val rand: Random = scala.util.Random

    override def processElement(plan: StimulusPlan, context: KeyedProcessFunction[Long, StimulusPlan, String]#Context, collector: Collector[String]): Unit = {
      import plan._
      stimulateType match {
        case "traffic_aid_feedback" =>
          implicit val formats = DefaultFormats
          val json = parse(plan.stagingPlan)
          val postId = (json \ "data" \ "postId").extractOpt[Long].getOrElse(0L)
          val feedBackKey = s"fed-$postId"
          if (postId > 0 && !stimulusHistoryState.contains(feedBackKey)) {
            stimulusHistoryState.put(feedBackKey, 1)
            collector.collect(stagingPlan)
          }
        case "fans_accomplish" =>
          implicit val formats = DefaultFormats
          val json = parse(plan.stagingPlan)
          val fans = (json \ "data" \ "fans").extractOpt[Long].getOrElse(0L)
          val accomplish = (json \ "data" \ "fans_accomplish").extractOpt[Long].getOrElse(0L)
          val accomplishKey = s"fans_accomplish_$accomplish"
          if (!stimulusHistoryState.contains(accomplishKey)) {
            stimulusHistoryState.put(accomplishKey, fans)
            collector.collect(stagingPlan)
          }
        case "flow_traffic_sense" =>
          implicit val formats = DefaultFormats
          val json = parse(plan.stagingPlan.replaceAll("\\s+", " "))
          val itemId = (json \ "data" \ "itemId").extractOpt[Long].getOrElse(0L)
          val itemType = (json \ "data" \ "itemType").extractOpt[String].getOrElse("")
          val msgType = (json \ "data" \ "msgType").extractOpt[String].getOrElse("")
          val flowTaskId = (json \ "data" \ "flowTaskId").extractOpt[String].getOrElse("")
          val flowTaskType =  (json \ "data" \ "flowTaskType").extractOpt[String].getOrElse("")
          val uniqueKey = s"flow_sense_${flowTaskId}_${flowTaskType}_${itemId}_${itemType}_$msgType"
          if (!stimulusHistoryState.contains(uniqueKey)) {
            stimulusHistoryState.put(uniqueKey, 1)
            collector.collect(stagingPlan)
          }
        case "creator_post_aid" =>
          implicit val formats = DefaultFormats
          val json = parse(plan.stagingPlan.replaceAll("\\s+", " "))
          val postId = (json \ "data" \ "postId").extractOpt[Long].getOrElse(0L).toString
          if (!stimulusHistoryState.contains(postId)) {
            stimulusHistoryState.put(postId, 1)
            collector.collect(stagingPlan)
          }
        case "creator_callback" =>
          implicit val formats = DefaultFormats
          val luck = rand.nextDouble()
          val json = parse(plan.stagingPlan.replaceAll("\\s+", " "))
          val callBackType = (json \ "data" \ "callback_type").extractOpt[String]
          val sendItOrNot = callBackType match {
            case Some("emotion") =>
              (!stimulusHistoryState.contains("creator_callback")) &&
                stimulusHistoryState.get("traffic_aid_creator") == 0 &&
                luck < 0.2
            case Some("hot" | "interaction") =>
              val lastSendTime = stimulusHistoryState.get("creator_callback_hot_interaction")
              val sinceLastSendDays = Days.daysBetween(
                new DateTime(lastSendTime).withTimeAtStartOfDay(),
                DateTime.now().withTimeAtStartOfDay()
              ).getDays
              val isLastPushMonthAgo = sinceLastSendDays >= 30
              if(isLastPushMonthAgo) {
                stimulusHistoryState.put("creator_callback_hot_interaction", System.currentTimeMillis())
              }
              isLastPushMonthAgo
            case _ => false
          }

          if(sendItOrNot) {
            stimulusHistoryState.put(stimulateType, System.currentTimeMillis())
            collector.collect(stagingPlan)
          }
        case _ => // one shot message
          if(!stimulusHistoryState.contains(stimulateType)) {
            val luck = rand.nextDouble()
            val isLucky = stimulateType match {
              case "first_interaction" => stimulusHistoryState.get("first_post") > 0 || luck < 0.5
              case "first_post" => luck < 0.1
              case "traffic_aid_creator" => stimulusHistoryState.get("creator_callback") == 0
              case "traffic_aid_sense" => true
              case "callback_post" => true
              case "register" => true
              case "register_first_post" => true
              case _ =>
                LOG.warn("unknown stimulate type {}, just ignore", stimulateType)
                false
            }
            if(isLucky) {
              stimulusHistoryState.put(stimulateType, System.currentTimeMillis())
              collector.collect(stagingPlan)
            } else {
              stimulusHistoryState.put(stimulateType, 0L) // don't repeat unlucky pm
            }
          }
      }
    }
  }

  class MyKafkaSerialization(val defaultSinkTopic: String) extends KafkaSerializationSchema[String] {
    override def serialize(element: String, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      new ProducerRecord[Array[Byte], Array[Byte]](defaultSinkTopic, null, element.getBytes(StandardCharsets.UTF_8))
    }
  }

  def parseStimulusPlan(content: String): Option[StimulusPlan] = {
    implicit val formats = DefaultFormats
    try {
      val json = parse(content.replaceAll("\\s+", " "))
      val userId = (json \ "userId").extract[Long]
      val stimulateType = (json \ "stimulateType").extract[String]
      Some(StimulusPlan(userId, stimulateType, content))
    } catch {
      case NonFatal(_) =>
        LOG.error("failed to parse staging stimulate plan: {}", content)
        None
    }
  }
}
