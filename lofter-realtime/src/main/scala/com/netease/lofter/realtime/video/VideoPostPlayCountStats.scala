package com.netease.lofter.realtime.video

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{Mda, VideoPlay}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.state._
import org.apache.flink.api.common.time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration

private class VideoPostPlayCountStats{}

/**
 * 视频实时播放设备数：设备每天人次累计(每天去重设备, 多天之间不做去重)
 * sink to es的方式改为利用script增量更新的方式写入，这样不用保留历史记录在状态中。
 */
object VideoPostPlayCountStats {
  val logger: Logger = LoggerFactory.getLogger(classOf[VideoPostPlayCountStats])

  case class VideoEvent(postId: Long, deviceUdid: String, dt: Int)
  case class VideoResult(postId: Long, playNum: Long)

  val latenessInSeconds = 60
  val VIDEO_PLAY_EVENT_SET: Set[String] = Set("z2-1", "z2-2", "z2-3", "a2-4", "a2-18", "a2-59", "l3-1")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("lofter_video_post_play_count_rt_gy_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val kafkaSink = KafkaSink.builder[VideoPlay]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.COUNTER.POST_PLAYCOUNT_TO_DB")
          .setKeySerializationSchema(new VideoPlayKeySerializer)
          .setValueSerializationSchema(new AvroJsonSerSchema[VideoPlay])
          .build()
      ).build()

    val mdaWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[Mda](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[Mda] {
        override def extractTimestamp(element: Mda, recordTimestamp: Long): Long = element.kafkaTime
      })

    val mda = env.fromSource(mdaSource, mdaWaterMark, "lofter-mda-avro")

    val videoPlayCount = mda
      .filter { e =>
        e.itemId.exists(_ > 0) && VIDEO_PLAY_EVENT_SET(e.eventId)
      }
      .map { e =>
        val dt = new DateTime(e.kafkaTime).toString("yyyyMMdd").toInt
        VideoEvent(e.itemId.get, e.deviceUdid, dt)
      }
      .keyBy(_.postId)
      .window(TumblingEventTimeWindows.of(Time.seconds(5)))
      .process(new VideoPostPlayCountFunction)
      .uid("video_history")

    videoPlayCount.map { e => VideoPlay(e.postId, e.playNum)}.sinkTo(kafkaSink)

    env.execute("lofter video post play count")
  }

  class VideoPlayKeySerializer extends SerializationSchema[VideoPlay] {
    override def serialize(element: VideoPlay): Array[Byte] = {
      element.postId.toString.getBytes("UTF-8")
    }
  }

  class VideoPostPlayCountFunction extends ProcessWindowFunction[VideoEvent, VideoResult, Long, TimeWindow] {
    lazy val videoDailyState: MapState[String, String] = {
      val dailyTtlConfig: StateTtlConfig = StateTtlConfig
        .newBuilder(time.Time.hours(24))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .cleanupInRocksdbCompactFilter(1000L)
        .build

      val videoDailyStateDescriptor = new MapStateDescriptor[String,String]("video_daily", createTypeInformation[String], createTypeInformation[String])
      videoDailyStateDescriptor.enableTimeToLive(dailyTtlConfig)
      getRuntimeContext.getMapState(videoDailyStateDescriptor)
    }

    override def process(key: Long, context: Context, elements: Iterable[VideoEvent], out: Collector[VideoResult]): Unit = {
      var playCount = 0L
      for (in <- elements){
        import in._
        if (!videoDailyState.contains(deviceUdid) || videoDailyState.get(deviceUdid) < dt.toString){
          videoDailyState.put(deviceUdid, dt.toString)
          playCount += 1
        }
      }
      if(playCount > 0) {
        out.collect(VideoResult(postId = key, playCount))
      }
    }
  }
}


