package com.netease.lofter.realtime.stimulus

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import org.apache.flink.api.common.eventtime.TimestampAssigner
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.{Logger, LoggerFactory}

import java.lang
import java.nio.charset.StandardCharsets
import java.util.Properties
import scala.collection.JavaConverters._

private class RegisterFirstPostStimulus{}

/**
 * 新用户注册及首次发文激励
 * {"userId": 123, "stimulateType": "register", "data":  {}}
 * {"userId": 123, "stimulateType": "register_first_post", "data":  {"postId": 999}}
 */
object RegisterFirstPostStimulus {
  case class UserAction(userId: Long, action: String, time: Long, postId: Option[Long] = None)
  case class PostDetail(postId: Option[Long])
  case class StimulusPlan(userId: Long, time: Long, stimulateType: String, data: PostDetail)

  val LOG: Logger = LoggerFactory.getLogger(classOf[RegisterFirstPostStimulus])

  class SubscribeEventTimestampAssigner extends TimestampAssigner[SubscribeEvent] {
    override def extractTimestamp(event: SubscribeEvent, recordTimestamp: Long): Long = event.getTimestamp
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    // config for kafka data source
    val properties = new Properties()
    properties.setProperty("bootstrap.servers",  kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "lofter_register_post_stimulus_online_gy")
    properties.setProperty("auto.offset.reset", "latest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val ndcSource: FlinkKafkaConsumer[SubscribeEvent] = new FlinkKafkaConsumer[SubscribeEvent]("lofter.binlog.ndc",new SubscribeEventSchema(), properties)
      .setStartFromTimestamp(startTimeStamp)
      .asInstanceOf[FlinkKafkaConsumer[SubscribeEvent]]

    val postActions = env.addSource(
      ndcSource.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[SubscribeEvent](Time.seconds(5)) {
        override def extractTimestamp(t: SubscribeEvent): Long = t.getTimestamp
      })
    ).uid("binlog-gy")
      .flatMap{ (s: SubscribeEvent, collector: Collector[UserAction]) =>
        s.getRowChanges.asScala
          .foreach { row =>
            row.getTableName.toLowerCase match {
              case "post"
                if row.getType == SubscribeEvent.RowChangeType.INSERT ||
                  row.getType == SubscribeEvent.RowChangeType.UPDATE =>

                val userId = row.getColumn("PublisherUserId").getNewValue.asInstanceOf[Long]
                val postId = row.getColumn("ID").getNewValue.asInstanceOf[Long]
                val isPublished = row.getColumn("IsPublished").getNewValue.asInstanceOf[Int] > 0
                val isAllowView = row.getColumn("AllowView").getNewValue.asInstanceOf[Int] == 0
                val isNoteCited = row.getColumn("CiteParentPostId").getNewValue.asInstanceOf[Long] == 0
                val valid = row.getColumn("Valid").getNewValue.asInstanceOf[Int]
                val time = Math.min(s.getTimestamp, row.getColumn("PublishTime").getNewValue.asInstanceOf[Long])
                if( isPublished && isNoteCited && isAllowView && (valid == 0 || valid == 12 || valid == 15 || valid == 16)) {
                  collector.collect(UserAction(userId, "post", time, Some(postId)))
                }

              case "profile" if row.getType == SubscribeEvent.RowChangeType.INSERT =>
                val userId = row.getColumn("UserID").getNewValue.asInstanceOf[Long]
                val time = Math.min(s.getTimestamp, row.getColumn("ProfileCreateTime").getNewValue.asInstanceOf[Long])
                collector.collect(UserAction(userId, "register", time, None))

              case _ =>
            }
          }
      }

    val outputTopic = "lofter.creator-stimulus-pm.staging"
    val stimulusSink = new FlinkKafkaProducer(outputTopic, new StimulusPlanSerialization(outputTopic), properties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)

    postActions.keyBy(_.userId)(createTypeInformation[Long])
      .process(new RegisterPostStimulusProcessFunction)
      .uid("user-register-first-post")
      .addSink(stimulusSink)
      .uid("gy-output")

    env.execute("lofter register first post stimulus")
  }

  class StimulusPlanSerialization(val defaultTopic: String) extends KafkaSerializationSchema[StimulusPlan] {
    def serialize(element: StimulusPlan, timestamp: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      implicit val format = DefaultFormats
      new ProducerRecord[Array[Byte], Array[Byte]](defaultTopic,  write(element).getBytes(StandardCharsets.UTF_8))
    }
  }

  class RegisterPostStimulusProcessFunction extends KeyedProcessFunction[Long, UserAction, StimulusPlan] {
    lazy val registerTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("register-time", createTypeInformation[Long], 0L))
    lazy val postTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("first-post-time", createTypeInformation[Long], 0L))

    override def processElement(item: UserAction, context: KeyedProcessFunction[Long, UserAction, StimulusPlan]#Context, collector: Collector[StimulusPlan]): Unit = {
      import item._

      LOG.debug("receive event: {}", item.toString)

      val registerTime = registerTimeState.value()

      action match {
        case "post" =>
          if(registerTime > 0 && postTimeState.value() == 0) {
            postTimeState.update(time)

            val isInStimulusPeriod = time > registerTime && time - registerTime < 30 * 24 * 3600 * 1000L

            if(isInStimulusPeriod) {
              val data = PostDetail(postId)
              val stimulusPlan = StimulusPlan(userId, time, "register_first_post", data)

              LOG.debug("stimulate plan: ", stimulusPlan.toString)
              collector.collect(stimulusPlan)
            }
          }
        case "register" =>
          if(registerTime == 0) {
            registerTimeState.update(time)
            collector.collect(StimulusPlan(userId, time, "register", PostDetail(None)))
          }
      }
    }
  }
}
