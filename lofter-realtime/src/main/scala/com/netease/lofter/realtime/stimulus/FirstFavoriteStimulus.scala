package com.netease.lofter.realtime.stimulus

import java.lang
import java.nio.charset.StandardCharsets
import java.util.Properties

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.timestamps.{AscendingTimestampExtractor, BoundedOutOfOrdernessTimestampExtractor}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object UserFavoriteState {
  val NO_POST = 0
  val FIRST_POST = 1
  val SECOND_POST_OR_FAVORIED = 2
}

private class FirstFavoriteStimulus {}

/**
 * 首次发文互动激励
 * 用户第一篇文章在发布后72h内获得了第一个喜欢，且该喜欢在用户发布第二篇文章前获得
 */
object FirstFavoriteStimulus {
  case class BlogAction(blogId: Long, action: String, time: Long, fromUserId: Option[Long] = None)
  case class FavoriteDetail(fromUserId: Long)
  case class StimulusPlan(userId: Long, time: Long, stimulateType: String, data: FavoriteDetail)

  val LOG = LoggerFactory.getLogger(classOf[FirstFavoriteStimulus])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // set this to eventTime, then waterMarkInterval is 200ms
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    // config for kafka data source
    val properties = new Properties()
    properties.setProperty("bootstrap.servers",  kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "lofter_first_favorite_stimulus_online_gy")
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
      .flatMap{ (s: SubscribeEvent, collector: Collector[BlogAction]) =>
        s.getRowChanges.asScala
          .filter(_.getType == SubscribeEvent.RowChangeType.INSERT)
          .foreach { row =>
            row.getTableName.toLowerCase match {
              case "post" =>
                val blogId = row.getColumn("BlogID").getNewValue.asInstanceOf[Long]
                val time = Math.min(s.getTimestamp, row.getColumn("PublishTime").getNewValue.asInstanceOf[Long])
                collector.collect(BlogAction(blogId, "post", time))
              case "posthot" =>
                if(row.getColumn("Type").getNewValue.asInstanceOf[Int] == 1) {
                  val blogId = row.getColumn("BlogID").getNewValue.asInstanceOf[Long]
                  val time = Math.min(s.getTimestamp, row.getColumn("OpTime").getNewValue.asInstanceOf[Long])
                  val fromUserId = row.getColumn("PublisherUserID").getNewValue.asInstanceOf[Long]
                  collector.collect(BlogAction(blogId, "favorite", time, Some(fromUserId)))
                }
              case _ =>
            }
          }
      }

    val outputTopic = "lofter.creator-stimulus-pm.staging"
    val stimulusSink = new FlinkKafkaProducer(outputTopic, new StimulusPlanSerialization(outputTopic), properties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)

    postActions.keyBy(_.blogId)(createTypeInformation[Long])
      .process(new FirstFavoriteStimulusProcessFunction)
      .uid("user-first-post-favorite")
      .addSink(stimulusSink).uid("first-favor-output")

    env.execute("lofter first favorite stimulus")
  }

  class StimulusPlanSerialization(val defaultTopic: String) extends KafkaSerializationSchema[StimulusPlan] {
    def serialize(element: StimulusPlan, timestamp: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      implicit val format = DefaultFormats
      new ProducerRecord[Array[Byte], Array[Byte]](defaultTopic,  write(element).getBytes(StandardCharsets.UTF_8))
    }
  }

  class FirstFavoriteStimulusProcessFunction extends KeyedProcessFunction[Long, BlogAction, StimulusPlan] {
    lazy val postFavoriteState: ValueState[Int] = getRuntimeContext.getState[Int](new ValueStateDescriptor[Int]("first-post-favorite", createTypeInformation[Int]))
    lazy val firstPostTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("first-post-time", createTypeInformation[Long]))

    override def processElement(item: BlogAction, context: KeyedProcessFunction[Long, BlogAction, StimulusPlan]#Context, collector: Collector[StimulusPlan]): Unit = {
      import UserFavoriteState._
      import item._
      implicit val format = DefaultFormats

      LOG.debug("receive event: {}", item.toString)

      action match {
        case "post" =>
          postFavoriteState.value() match {
            case NO_POST =>
              postFavoriteState.update(FIRST_POST)
              firstPostTimeState.update(time)
            case FIRST_POST => postFavoriteState.update(SECOND_POST_OR_FAVORIED)
            case _ =>
          }
        case "favorite" =>
          if(postFavoriteState.value() != SECOND_POST_OR_FAVORIED &&
            fromUserId.isDefined && fromUserId.get != context.getCurrentKey ) {
            val isInStimulusPeriod = time > firstPostTimeState.value() && time - firstPostTimeState.value() < 72 * 3600 * 1000L
            if(isInStimulusPeriod) {
              val data = FavoriteDetail(fromUserId.get)
              val stimulusPlan = StimulusPlan(blogId, time, "first_interaction", data)

              LOG.debug("stimulate plan: ", stimulusPlan.toString)
              collector.collect(stimulusPlan)
            }
            postFavoriteState.update(SECOND_POST_OR_FAVORIED)
          }
      }
    }
  }
}
