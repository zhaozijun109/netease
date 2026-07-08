package com.netease.lofter.realtime.ad

import com.alibaba.druid.pool.DruidDataSource
import com.netease.lofter.realtime.common.{dbConfig, kafkaConfig}
import com.netease.wm.hubble.avro.{MusicAdActionEvent, MusicDspRequestEvent}
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import javax.sql.DataSource
import scala.util.Try
import scala.util.control.NonFatal

private class AdDspRequestSyncMusic{}

object AdDspRequestSyncMusic {
  val LOG: Logger = LoggerFactory.getLogger(classOf[AdDspRequestSyncMusic])

  case class SlotMeta(slotId: String, bidingType: String, ecpm: Double, updateTime: Long)

  case class DspEvent(dspId: String, positionId: String, positionName: String,
                      success: Int, requestTime: Option[Long], responseTime: Option[Long],
                      winFlag: Option[Int],
                      uuid: Option[String], reqid: Option[String],
                      slotId: Option[String], price: Option[Double], blogId: Option[Long],
                      bidFactor: Option[Double])

  val SDK_AD_SOURCE: Set[Int] = Set(1016,1027,1022,1028,1037,1042)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(180000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val dspSource = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("adserver.dsp.online")
      .setGroupId("ad_dsp_request_sync_music")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema)
      .build()

    val musicDspSink = KafkaSink.builder[MusicDspRequestEvent]()
      .setBootstrapServers(kafkaConfig.MUSIC_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("ods_iad_lofter_request_record")
        .setValueSerializationSchema(new AvroJsonSerSchema[MusicDspRequestEvent])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()


    val musicAdActionSink = KafkaSink.builder[MusicDspRequestEvent]()
      .setBootstrapServers(kafkaConfig.MUSIC_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("ods_iad_lofter_action_record")
        .setValueSerializationSchema(new AvroJsonSerSchema[MusicDspRequestEvent])
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val input: DataStream[String] = env.fromSource(dspSource, WatermarkStrategy.noWatermarks(), "dsp-source")

    val dspEvents = input.flatMap { line =>
      val dsp = parseDspLog(line)
      dsp match {
        case Some(e) =>
          val isWin = if(e.winFlag.exists(_ > 0) && !SDK_AD_SOURCE(toInt(e.dspId))) 1 else 0

          val result = MusicDspRequestEvent(
            action = "request",
            req_uid = e.uuid.getOrElse(""),
            req_id = e.reqid.getOrElse(""),
            time = e.requestTime.getOrElse(System.currentTimeMillis()),
            positionId = Try{e.positionId.toInt}.getOrElse(0),
            positionName = e.positionName,
            slotId = e.slotId.getOrElse(""),
            slotType = "",
            dspId = e.dspId,
            userId = e.blogId.getOrElse(0L),
            bidPrice = e.price.getOrElse(.0),
            bidFactor = e.bidFactor.getOrElse(.0),
            ecpm = 0
          )

          if(isWin> 0) {
            Seq(result, result.copy(action = "win"))
          } else Seq(result)
        case _ => Seq.empty
      }
    }.uid("dsp-request")

    dspEvents.filter(_.action == "request")
      .rebalance
      .process(new SlotDimensionFunction)
      .uid("slot-meta-expand")
      .sinkTo(musicDspSink)

    dspEvents.filter(_.action == "win")
      .rebalance
      .sinkTo(musicAdActionSink)

    env.execute("dsp request to music")
  }

  def parseDspLog(line: String): Option[DspEvent] = {
    implicit val formats = DefaultFormats
    try {
      val jsTree = parse(line)
      val jsBody = (jsTree \ "_body").extractOpt[String]
      // for compatible with old log line
      val parsed = (if(jsBody.isDefined) parse(jsBody.get) else jsTree).extract[DspEvent]
      // workaround for missing requestTime
      if(parsed.requestTime.isDefined) {
        Some(parsed)
      } else {
        Some(parsed.copy(requestTime = parsed.responseTime))
      }
    } catch {
      case NonFatal(e) =>
        LOG.error("parse dsp log exception: {}", e)
        None
    }
  }

  def toInt(field: String): Int = Try{field.toInt}.toOption.getOrElse(0)

  class SlotDimensionFunction extends ProcessFunction[MusicDspRequestEvent, MusicDspRequestEvent] {

    val EVENT_LIST_UPDATE_INTERVAL: Long = 600 * 1000L
    val REPEAT_WINDOW: Long = 5 * 60 * 1000L

    @transient private lazy val dataSource: DataSource = {
      val ds = new DruidDataSource()
      ds.setDriverClassName(dbConfig.DRIVER)
      ds.setUrl(dbConfig.YAOLU_JDBC_URL)
      ds.setLoginTimeout(5000)
      ds.setTestOnBorrow(true)
      ds.setTestOnReturn(true)
      ds.setFailFast(true)
      ds
    }

    @volatile var slotMetaMap: Map[String, SlotMeta] = _
    @volatile var lastUpdateTime = 0L

    override def open(parameters: Configuration): Unit = {
      updateSlotMeta()
      lastUpdateTime = System.currentTimeMillis() + EVENT_LIST_UPDATE_INTERVAL
    }

    override def processElement(value: MusicDspRequestEvent, ctx: ProcessFunction[MusicDspRequestEvent, MusicDspRequestEvent]#Context, out: Collector[MusicDspRequestEvent]): Unit = {

      val nextUpdateTime = (System.currentTimeMillis() / EVENT_LIST_UPDATE_INTERVAL + 1) * EVENT_LIST_UPDATE_INTERVAL
      if(nextUpdateTime > lastUpdateTime) {
        updateSlotMeta()
        lastUpdateTime = nextUpdateTime
      }

      if(slotMetaMap != null && slotMetaMap.contains(value.slotId)) {
        val slotMeta = slotMetaMap.get(value.slotId).get
        out.collect(value.copy(slotType = "1", ecpm = slotMeta.ecpm))
      } else out.collect(value.copy(slotType = "0"))
    }

    private def updateSlotMeta(): Unit = {
      LOG.info("start updating slot meta")

      implicit val conn: Connection = dataSource.getConnection()

      import com.netease.wm.util.Sql._
      try {
        val result = sql"""select externalPositionId as slotId, JSON_EXTRACT(extJson, '$$.biddingType') as bidingType, cpmUnitPrice as ecpm, updateTime from AD_DspPosition where extJson like '%biddingType":3%' """.stripMargin.query[SlotMeta].filter(_.bidingType == "3")

        if(result.nonEmpty) {
          slotMetaMap = result.groupBy(_.slotId).map {
            case (slotId, metas) =>  slotId -> metas.sortBy(_.updateTime).reverse.head
          }

          LOG.info("complete updating slot meta: {}", slotMetaMap.toString())
        }
      } catch {
        case NonFatal(e) =>
          LOG.error("failed to update slot meta: {}", e)
      }
      finally {
        conn.close()
      }
    }

    override def close(): Unit = {
      if(dataSource != null && dataSource.isInstanceOf[java.io.Closeable]) {
        dataSource.asInstanceOf[java.io.Closeable].close()
      }
    }
  }

}
