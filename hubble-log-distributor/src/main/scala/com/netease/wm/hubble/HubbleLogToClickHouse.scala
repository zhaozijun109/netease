package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.netease.wm.hubble.common.{ClickHouseConfig, kafkaConfig}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.runtime.state.KeyGroupRangeAssignment
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import java.sql.PreparedStatement
import java.util.Properties
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.NonFatal

private class HubbleLogToClickHouse{}

object HubbleLogToClickHouse {
  val objectMapper = new ObjectMapper()
  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
  val outKeySet = Set("kafkaTime","source","ext","abExt","deviceAndroidId","timestamp","sdkType","time","_source","1stt","1ste","2nde","3rde","4the","itemId","itemType","scene","action","recId","algInfo","module","layout","tab","pos","text","isNew")

  val LOG: Logger = LoggerFactory.getLogger(classOf[HubbleLogToClickHouse])

  case class MdaEvent(
                       dataType: String,
                       uploadNum: Int,
                       uploadTime: Long,
                       persistedTime: Long,
                       appKey: String,
                       appVersion: String,
                       appChannel: String,
                       sdkVersion: String,
                       sdkType: String,
                       deviceUdid: String,
                       devicePlatform: String,
                       deviceOs: String,
                       deviceOsVersion: String,
                       deviceModel: String,
                       deviceResolution: String,
                       deviceCarrier: String,
                       deviceNetwork: String,
                       localeLanguage: String,
                       localeCountry: String,
                       customUDID: String,
                       sessionUuid: String,
                       eventId: String,
                       occurTime: Long,
                       costTime: Long,
                       userId: Long,
                       category: String,
                       timeZone: String,
                       attributes: String,
                       ip: String,
                       kafkaTime: Long,
                       itemId: String,
                       itemType: String,
                       action: String,
                       scene: String,
                       recId: String,
                       algInfo: String,
                       module: String,
                       layout: String,
                       tab: String,
                       pos: String,
                       text: String,
                       deviceId: Long = 0L,
                       isNew: Short = 0
                     )

  @inline def asText(node: JsonNode): String = if(node == null) null else node.asText()
  @inline def asULong(node: JsonNode): Long = if(node == null) 0L else Math.max(node.asLong(), 0L)
  @inline def asUInt(node: JsonNode): Int = if(node == null) 0 else Math.max(node.asInt(), 0)
  @inline def asLong(node: JsonNode): Long = if(node == null) 0L else node.asLong()
  @inline def asInt(node: JsonNode): Int = if(node == null) 0 else node.asInt()
  @inline def textAsLong(node: JsonNode): Long = if(node == null) 0L else {
    val v = node.asText()
    if(v == "-") 0L else Try{Math.max(v.toLong, 0L)}.getOrElse(0L)
  }
  @inline def getFiledFromStr(body: mutable.Map[String,Object], params: String): String = if(body == null) "" else body.getOrElse(params,"").toString

  def parseMdaEvent(line: String): Option[MdaEvent] = {
    try {
      val root = objectMapper.readTree(line)
      val attributesNode = root.get("attributes")
      val eventParams: java.util.Map[String,Object] = objectMapper.convertValue(attributesNode,new TypeReference[java.util.Map[String,Object]](){})
      val eventParamsScala = eventParams.asScala

      val itemId = getFiledFromStr(eventParamsScala,"itemId")
      val itemType = getFiledFromStr(eventParamsScala,"itemType")
      val action = getFiledFromStr(eventParamsScala,"action")
      val scene = getFiledFromStr(eventParamsScala,"scene")
      val recId = getFiledFromStr(eventParamsScala,"recId")
      val algInfo = getFiledFromStr(eventParamsScala,"algInfo")
      val module = getFiledFromStr(eventParamsScala,"module")
      val layout = getFiledFromStr(eventParamsScala,"layout")
      val tab = getFiledFromStr(eventParamsScala,"tab")
      val pos = getFiledFromStr(eventParamsScala,"pos")
      val text = getFiledFromStr(eventParamsScala,"text")

      val newParamsStr = if (null == eventParamsScala) "" else {
        val newParams = eventParamsScala.filterKeys(k => !outKeySet(k))
        str(objectMapper.writeValueAsString(newParams.asJava))
      }

      val dataType = asText(root.get("dataType"))
      val appKey = asText(root.get("appKey"))
      val appVersion = asText(root.get("appVersion"))
      val appChannel = asText(root.get("appChannel"))

      val deviceUdid = asText(root.get("deviceUdid"))
      val devicePlatform = asText(root.get("devicePlatform"))
      val deviceOs = asText(root.get("deviceOs"))
      val deviceOsVersion = asText(root.get("deviceOsVersion"))
      val deviceModel = asText(root.get("deviceModel"))
      val eventId = asText(root.get("eventId"))
      val occurTime = asULong(root.get("occurTime"))
      val costTime = asULong(root.get("costTime"))
      val userId = textAsLong(root.get("userId"))
      val ip = asText(root.get("ip"))
      val kafkaTime = asULong(root.get("kafkaTime"))

      // val category = asText(root.get("category"))
      // val category = "universal"
      val category = new DateTime(kafkaTime).toString("yyyyMMddHH")

      val sdkVersion = ""
      val sdkType = ""
      val timeZone = ""
      val localeLanguage = ""
      val localeCountry = ""
      val customUDID = ""
      val sessionUuid = ""
      val deviceResolution = ""
      val deviceCarrier = ""
      val deviceNetwork = ""
      val uploadNum = 0
      val uploadTime = 0
      val persistedTime = 0

      if(deviceUdid == null || deviceUdid.isEmpty || eventId == "rd-2") {
        None
      } else {
        Some(MdaEvent(str(dataType),uploadNum,uploadTime,persistedTime,str(appKey),str(appVersion),str(appChannel),str(sdkVersion),str(sdkType),str(deviceUdid),str(devicePlatform),str(deviceOs),str(deviceOsVersion),str(deviceModel),str(deviceResolution),str(deviceCarrier),str(deviceNetwork),str(localeLanguage),str(localeCountry),str(customUDID),str(sessionUuid),str(eventId),occurTime,costTime,
          userId,str(category),str(timeZone),newParamsStr,str(ip),kafkaTime,itemId,itemType,action,scene,recId,algInfo,module,layout,tab,pos,text))
      }
    } catch {
      case NonFatal(e) =>
        println("Exception: parse mda events error for input: {}", line, e.printStackTrace())
        None
      case _ : Throwable =>
        println("parse mda events error for input: {}", line)
        None
    }
  }

  @inline def str(in: String): String = if(in == null) "" else in

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(300000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val kafkaProperties = {
      val props = new Properties()
      props.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_MDA)
      props.setProperty("group.id", "mda_to_phy_clickhouse")
      props.setProperty("auto.offset.reset", "earliest")
      props.setProperty("flink.partition-discovery.interval-millis", "60000")
      props.setProperty("compression.type", "snappy")
      props
    }

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String]("vc.mda.online", new SimpleStringSchema(), kafkaProperties).setStartFromGroupOffsets()
    )

    val chSink = JdbcSink.sink(
      "insert into vc.mda_events_local(dataType,uploadNum,uploadTime,persistedTime,appKey,appVersion,appChannel,sdkVersion,sdkType,deviceUdid,devicePlatform,deviceOs,deviceOsVersion,deviceModel,deviceResolution,deviceCarrier,deviceNetwork,localeLanguage,localeCountry,customUDID,sessionUuid,eventId,occurTime,costTime,userId,category,timeZone,attributes,ip,kafkaTime,itemId,itemType,action,scene,recId,algInfo,module,layout,tab,pos,text,isNew) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      new JdbcStatementBuilder[MdaEvent] {
        override def accept(ps: PreparedStatement, e: MdaEvent): Unit = {
          import e._
          ps.setString(1, dataType)
          ps.setInt(2, uploadNum)
          ps.setLong(3, uploadTime)
          ps.setLong(4, persistedTime)
          ps.setString(5, appKey)
          ps.setString(6, appVersion)
          ps.setString(7, appChannel)
          ps.setString(8, sdkVersion)
          ps.setString(9, sdkType)
          ps.setString(10, deviceUdid)
          ps.setString(11, devicePlatform)
          ps.setString(12, deviceOs)
          ps.setString(13, deviceOsVersion)
          ps.setString(14, deviceModel)
          ps.setString(15, deviceResolution)
          ps.setString(16, deviceCarrier)
          ps.setString(17, deviceNetwork)
          ps.setString(18, localeLanguage)
          ps.setString(19, localeCountry)
          ps.setString(20, customUDID)
          ps.setString(21, sessionUuid)
          ps.setString(22, eventId)
          ps.setLong(23, occurTime)
          ps.setLong(24, costTime)
          ps.setLong(25, userId)
          ps.setString(26, category)
          ps.setString(27, timeZone)
          ps.setString(28, attributes)
          ps.setString(29, ip)
          ps.setLong(30, kafkaTime)
          ps.setString(31, itemId)
          ps.setString(32, itemType)
          ps.setString(33, action)
          ps.setString(34, scene)
          ps.setString(35, recId)
          ps.setString(36, algInfo)
          ps.setString(37, module)
          ps.setString(38, layout)
          ps.setString(39, tab)
          ps.setString(40, pos)
          ps.setString(41, text)
          ps.setShort(42, isNew)
        }
      },
      JdbcExecutionOptions.builder().withBatchSize(10000).withBatchIntervalMs(5000).withMaxRetries(5).build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withDriverName(ClickHouseConfig.clickHouseDriver)
        .withUrl(ClickHouseConfig.phyClickHouseJdbcUrl)
        .withUsername(ClickHouseConfig.phyClickHouseUser)
        .withPassword(ClickHouseConfig.phyClickHousePassword)
        .build()
    )

    logSource.flatMap {
      line => parseMdaEvent(line)
    }.keyBy(_.deviceUdid)
      .process(new KeyedProcessFunction[String, MdaEvent, MdaEvent] {

        private var udidState: MapState[String, Long] = _

        override def open(parameters: org.apache.flink.configuration.Configuration): Unit = {
          udidState = getRuntimeContext.getMapState(new MapStateDescriptor[String, Long]("deviceudid-dict", createTypeInformation[String], createTypeInformation[Long]))
        }
        override def processElement(e: MdaEvent, context: KeyedProcessFunction[String, MdaEvent, MdaEvent]#Context, collector: Collector[MdaEvent]): Unit = {
          if(udidState.contains(e.deviceUdid)) {
            collector.collect(e.copy())
          } else {
            udidState.put(e.deviceUdid,e.kafkaTime)
            collector.collect(e.copy(isNew = 1))
          }
        }
      })
      .uid("deviceudids")
      .disableChaining()
      .addSink(chSink).uid("clickhouse-mda-sink")

    env.execute("lofter hubble log to clickhouse distributed table sink job")
  }
}
