package com.netease.lofter.realtime.activity

import java.util.Properties

import com.fasterxml.jackson.core.JsonParser
import com.netease.wm.hubble.avro.ActivityEvent
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.common.avro.binary.AvroBinarySerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state._
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.runtime.state.KeyGroupRangeAssignment
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.NonFatal

private class ActivityEventEtl{}

object ActivityEventEtl {
  val MAX_PARALLELISM = 16
  val KEY_RAND_SALT = "73b46"
  val latenessInSeconds = 60

  val objectMapper = new ObjectMapper()
  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
  val outKeySet = Set("source","costtime","ext","abExt","deviceAndroidId","_source","recId","algInfo","layout","tab","pos",
    "text","refer","ext_itemId","ext_itemType","occurType","category","activityId","channel","value","activityCode",
    "useAppTrack","cardChannel","type","v","status","action","module","clientHubbleDeviceId",
    "abext","deviceandroidid","recid","alginfo","ext_itemid","ext_itemtype","occurtype","activityid","activitycode",
    "useapptrack","cardchannel","clienthubbledeviceid"
  )

  val LOG: Logger = LoggerFactory.getLogger(classOf[ActivityEventEtl])

//  case class ActivityEvent(
//                            platform: String,
//                            dataType: String,
//                            appKey: String,
//                            deviceUdid: String,
//                            devicePlatform: String,
//                            deviceOs: String,
//                            eventId: String,
//                            currentUrl: String,
//                            ip: String,
//                            userId: Long,
//                            occurTime: Long,
//                            costTime: Long,
//                            kafkaTime: Long,
//                            activityId: String,
//                            channel: String,
//                            value: Long,
//                            attributes: String,
//                            deviceId: Long = 0L,
//                            profileCreateTime: Long = 0L,
//                            profileCreateFrom: String = "",
//                            clientHubbleDeviceId: String = ""
//                          )

  case class ProfileRecord(userId: Long,createTime: Long, createFrom: String = "")

  @inline def asText(node: JsonNode): String = if(node == null) "" else node.asText()
  @inline def asULong(node: JsonNode): Long = if(node == null) 0L else Math.max(node.asLong(), 0L)
  @inline def asUInt(node: JsonNode): Int = if(node == null) 0 else Math.max(node.asInt(), 0)
  @inline def asLong(node: JsonNode): Long = if(node == null) 0L else node.asLong()
  @inline def asInt(node: JsonNode): Int = if(node == null) 0 else node.asInt()
  @inline def str(in: String): String = if(in == null) "" else in
  @inline def textAsLong(node: JsonNode): Long = if(node == null) 0L else {
    val v = node.asText()
    if(v == "-") 0L else Try{Math.max(v.toLong, 0L)}.getOrElse(0L)
  }
  @inline def getFiledFromStr(body: mutable.Map[String,Object], params: String): String = if(body == null) "" else body.getOrElse(params,"").toString

  @inline def textAsUserId(node: JsonNode): Long = if(node == null) 0L else {
    val v = node.asText()
    if(v == "-" || v.exists(s => !s.isDigit)) 0L else Try{Math.max(v.toLong, 0L)}.getOrElse(0L)
  }

  def parseActivityEvent(line: String): Option[ActivityEvent] = {
    try {
      if(line.contains("activityId") || line.contains("activityid")) {
        val root = objectMapper.readTree(line)
        val attributesNode = root.get("attributes")
        val eventParams: java.util.Map[String,Object] = objectMapper.convertValue(attributesNode,new TypeReference[java.util.Map[String,Object]](){})
        val eventParamsScala = eventParams.asScala

        val activityId = if (getFiledFromStr(eventParamsScala, "activityId").nonEmpty) {
          getFiledFromStr(eventParamsScala, "activityId")
        } else if (getFiledFromStr(eventParamsScala, "activityid").nonEmpty) {
          getFiledFromStr(eventParamsScala,"activityid")
        } else {
          ""
        }
        if(activityId.nonEmpty) {
          val channel = getFiledFromStr(eventParamsScala,"channel")
          val clientHubbleDeviceId = if (getFiledFromStr(eventParamsScala,"clientHubbleDeviceId").nonEmpty) {
            getFiledFromStr(eventParamsScala,"clientHubbleDeviceId")
          } else if(getFiledFromStr(eventParamsScala,"clienthubbledeviceid").nonEmpty) {
            getFiledFromStr(eventParamsScala,"clienthubbledeviceid")
          } else {
            ""
          }
          val temValue = getFiledFromStr(eventParamsScala,"value")
          val value = if(temValue.isEmpty) 0L else temValue.toLong

          val newParamsStr = if (null == eventParamsScala) "" else {
            val newParams = eventParamsScala.filterKeys(k => !outKeySet(k))
            str(objectMapper.writeValueAsString(newParams.asJava))
          }

          val dataType = asText(root.get("dataType"))
          val appKey = asText(root.get("appKey"))
          val platform = appKey match {
            case "MA-BFD7-963BF6846668" => "web"
            case "MA-B4E8-3BEB9540671E" => "wap"
            case "MA-A4FE-A88932E7A98F" | "MA-9A4C-437494F370B3" | "MA-88DF-03AA6989372E" => "mda"
            case _ => "unknown"
          }

          val deviceUdid = if(clientHubbleDeviceId != null && clientHubbleDeviceId.nonEmpty) {
            clientHubbleDeviceId
          } else  asText(root.get("deviceUdid"))

          val devicePlatform = asText(root.get("devicePlatform"))
          val deviceOs = asText(root.get("deviceOs"))
          val eventId = asText(root.get("eventId"))
          val occurTime = asULong(root.get("occurTime"))
          val costTime = asULong(root.get("costTime"))
          val userId = textAsUserId(root.get("userId"))
          val ip = asText(root.get("ip"))
          val kafkaTime = asULong(root.get("kafkaTime"))
          val currentUrl = asText(root.get("currentUrl"))
          val clientDeviceId = if (platform.equalsIgnoreCase("mda")) "" else clientHubbleDeviceId

          if(deviceUdid == null || deviceUdid.isEmpty) {
            None
          } else {
            Some(
              ActivityEvent(
                platform, dataType,appKey,deviceUdid,devicePlatform,deviceOs,eventId,currentUrl,
                ip,userId,occurTime,costTime,kafkaTime,activityId,channel,value,newParamsStr,0L,0L,"",clientHubbleDeviceId=clientDeviceId))
          }
        } else None
      } else None
    } catch {
      case NonFatal(e) =>
        LOG.info("parse mda events error for input: {}", line)
        None
    }
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val kafkaProperties = {
      val props = new Properties()
      props.setProperty("bootstrap.servers", kafkaConfig.GY_BOOTSTRAP_SERVERS)
      props.setProperty("group.id", "activity_event_etl_gy")
      props.setProperty("auto.offset.reset", "latest")
      props.setProperty("flink.partition-discovery.interval-millis", "60000")
      props.setProperty("compression.type", "snappy")
      props
    }

    // deal with 3 data source
    val hubbleSource = KafkaSource.builder[String]()
      .setTopics("lofter.web.online.json", "lofter.wap.online.json", "lofter.mda.online.json", "vc.wap.online")
      .setProperties(kafkaProperties)
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setTopics(kafkaConfig.NDC_TOPIC)
      .setProperties(kafkaProperties)
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val dwdResultSink = KafkaSink.builder[ActivityEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("activity.event.dwd")
          .setValueSerializationSchema(new AvroBinarySerSchema[ActivityEvent])
          .build()
      ).build()

    val profileStream: DataStream[ProfileRecord] =  env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap{(line,collector: Collector[ProfileRecord]) =>
        line.getRowChanges.asScala
          .filter(_.getTableName == "Profile")
          .filter(_.getType == SubscribeEvent.RowChangeType.INSERT)
          .foreach { row =>
            val userId = row.getColumn("UserID").getNewValue.asInstanceOf[Long]
            val createTime = row.getColumn("ProfileCreateTime").getNewValue.asInstanceOf[Long]
            val createFrom = row.getColumn("ProfileCreateFrom").getNewValue.asInstanceOf[String]
            val profileRecord = if (null != createFrom) ProfileRecord(userId, createTime, createFrom) else ProfileRecord(userId, createTime)
            collector.collect(profileRecord)
          }
      }.filter(e=> e.userId > 0L)

    env.fromSource(hubbleSource, WatermarkStrategy.noWatermarks(),"hubble")
      .flatMap {
        line => parseActivityEvent(line) }
      .connect(profileStream).keyBy(_.userId,_.userId).process(new joinFunction)
      .keyBy{ value =>
        val keyGroup = KeyGroupRangeAssignment.assignToKeyGroup(value.deviceUdid, MAX_PARALLELISM)
        s"$KEY_RAND_SALT$keyGroup"
      }
      .process(new DeviceIdDictMapping).uid("device-dict-mapping")
      .disableChaining()
      .sinkTo(dwdResultSink)
      .uid("clickhouse-activity-sink")

    env.execute("lofter hubble activity etl job")
  }

  class joinFunction extends KeyedCoProcessFunction[Long,ActivityEvent,ProfileRecord,ActivityEvent]{
    lazy val profileState:ValueState[(Long,String)] = getRuntimeContext.getState(new ValueStateDescriptor[(Long,String)]("profile", createTypeInformation[(Long,String)]))
    override def processElement1(in1: ActivityEvent, context: KeyedCoProcessFunction[Long, ActivityEvent, ProfileRecord, ActivityEvent]#Context, collector: Collector[ActivityEvent]): Unit = {
      val state = profileState.value()
      if (null != state) {
        val createTime = state._1
        val createFrom = state._2
        collector.collect(in1.copy(profileCreateTime = createTime, profileCreateFrom = createFrom))
      } else {
        collector.collect(in1)
      }
    }

    override def processElement2(in2: ProfileRecord, context: KeyedCoProcessFunction[Long, ActivityEvent, ProfileRecord, ActivityEvent]#Context, collector: Collector[ActivityEvent]): Unit = {
      import in2._
      profileState.update(createTime, createFrom)
    }
  }

  val LOW_MASK = (1 << 20) - 1
  val HIGH_MASK = ~LOW_MASK

  class DeviceIdDictMapping extends KeyedProcessFunction[String, ActivityEvent, ActivityEvent] {
    lazy val deviceNum: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("device-num", createTypeInformation[Long], 0L))
    lazy val dict: MapState[String, Long] = {
      val descriptor = new MapStateDescriptor[String, Long]("device-dict", createTypeInformation[String], createTypeInformation[Long])
      descriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(7)).build())
      getRuntimeContext.getMapState(descriptor)
    }

    override def processElement(e: ActivityEvent, context: KeyedProcessFunction[String, ActivityEvent, ActivityEvent]#Context, collector: Collector[ActivityEvent]): Unit = {
      val keyGroup = KeyGroupRangeAssignment.assignToKeyGroup(e.deviceUdid, MAX_PARALLELISM)

      if(dict.contains(e.deviceUdid)) {
        collector.collect(e.copy(deviceId = dict.get(e.deviceUdid)))
      } else {
        val dn = deviceNum.value() + 1
        val newId = (dn & LOW_MASK) | ((dn & HIGH_MASK) << 4) | (keyGroup << 20)

        deviceNum.update(dn)
        dict.put(e.deviceUdid, newId)
        collector.collect(e.copy(deviceId = newId))
      }
    }
  }
}


