package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.netease.wm.hubble.common.shuffleConfig
import org.apache.flink.api.common.functions.Partitioner
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.metrics.Counter
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}

import java.util.{Arrays, ArrayList}
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.NonFatal
import scala.util.matching.Regex

private class LogShuffle{}

object LogShuffle {
  val MAX_LOG_SIZE = 700000
  val THIRTY_MINUTES = 30*60*1000L
  val SEVEN_DAYS = 7*24*60*60*1000L

  val HEADER_OBSOLETE_FIELDS = new ArrayList(Arrays.asList("uploadNum", "uploadTime", "persistedTime", "sdkVersion", "sdkType", "deviceMacAddr",
    "deviceOldMacAddr", "wifiSsid", "wifiBssid", "timeZone", "deviceResolution", "deviceCarrier",
    "deviceNetwork", "localeLanguage", "localeCountry"))

  val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  val LOG: Logger = LoggerFactory.getLogger(classOf[LogShuffle])

  abstract sealed class EventType { def getTopic(appKey: String): String}
  case object PushEvent extends EventType {
    override def getTopic(appKey: String): String = "lofter.push.reach"
  }

  case object ExceptionEvent extends EventType {
    override def getTopic(appKey: String): String = "lofter_exception_log"
  }

  case object MonitorEvent extends EventType {
    override def getTopic(appKey: String): String = "lofter_simulation_log"
  }

  case object AdEvent extends EventType {
    // TODO: setup ad topic
    override def getTopic(appKey: String): String = "lofter_ad_log"
  }

  case object NormalEvent extends EventType {
    val key2TopicMap: Map[String, String] = shuffleConfig.appKeyDispatchRules.flatMap {
      case (topic, appKeys) => appKeys.split(",").map(_.trim).filter(_.nonEmpty).map{ key => key -> topic}
    }.toMap.withDefaultValue(shuffleConfig.defaultDestTopic)

    override def getTopic(appKey: String): String = key2TopicMap(appKey)
  }

  case class ParsedEvent(appKey: String, deviceUdid: String, eventId: Option[String], source: String, eventType: EventType)

  def parseLogEvents(v: String): Seq[ParsedEvent] = {
    val root = objectMapper.readTree(v)
    val ip = if(root.get("ip") != null) root.get("ip").asText() else ""
    val receiveTime = if(root.get("receiveTime") != null) root.get("receiveTime").asLong() else System.currentTimeMillis()
    val data = root.get("dataBody")

    var headerNode: ObjectNode = null
    val events = new ArrayBuffer[JsonNode]
    if(data.isArray) {
      for {
        dataElement: JsonNode <- data.iterator().asScala
      } {
        val eventBody = dataElement.asText()
        val event = objectMapper.readTree(eventBody)

        event.get("dataType").asText() match {
          case "h" =>
            headerNode = event.asInstanceOf[ObjectNode].without(HEADER_OBSOLETE_FIELDS)
          case _ =>
            val node = if(headerNode != null) {
              headerNode.deepCopy().setAll(event.asInstanceOf[ObjectNode]).asInstanceOf[ObjectNode]
            } else event.asInstanceOf[ObjectNode]

            node.put("ip", ip)
            node.put("kafkaTime", receiveTime)

            // convert dataType of da events to ie
            val eventId = Option(node.get("eventId")).map(_.asText())

            eventId match {
              case Some(e) if e != "da_screen" && e.startsWith("da_")=>
                node.put("dataType", "ie")

              case _ =>
            }

            // normalize occurTime
            val occurTimeOption = Try{node.get("occurTime").asLong()}.toOption
            // time is more accurate than occurTime, who's unit is second
            val occurTime = Try{node.get("time").asLong()}.toOption.orElse(occurTimeOption.map(_ * 1000L)).getOrElse(0L)
            val now = System.currentTimeMillis()
            if(now - occurTime > SEVEN_DAYS || occurTime - now > THIRTY_MINUTES ) {
              node.put("occurTime", receiveTime)
            } else {
              node.put("occurTime", occurTime)
            }
            // normalize costTime
            if(node.get("costTime") != null && node.get("costTime").asLong() < 0) {
              node.put("costTime", -1)
            }
            events.append(node)
        }
      }
    }

    val appKey = if(headerNode != null) {
      headerNode.get("appKey").asText()
    }  else {
      events.head.get("appKey").asText()
    }

    events.flatMap { event =>
      val eventId = Option(event.get("eventId")).map(_.asText())
      val deviceUdid = Option(event.get("deviceUdid")).map(_.asText()).getOrElse("")
      val category = Option(event.get("category")).map(_.asText())

      val eventType = (eventId, category) match {
        case (Some("rd-2"), _) => PushEvent
        case (_, Some("lofter_exception") | Some("lofter_apm")) => ExceptionEvent
        case (_, Some("log_monitor")) => MonitorEvent
        case _ => NormalEvent
      }

      val extraEventType = eventId match {
        case Some("b1-45") => Some(AdEvent)
        case _ => None
      }

      if(extraEventType.isEmpty) {
        Seq(ParsedEvent(appKey, deviceUdid, eventId, objectMapper.writeValueAsString(event), eventType))
      } else {
        Seq(
          ParsedEvent(appKey, deviceUdid, eventId, objectMapper.writeValueAsString(event), eventType),
          ParsedEvent(appKey, deviceUdid, eventId, objectMapper.writeValueAsString(event), extraEventType.get)
        )
      }

    }
  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val logSource = env.addSource(
      new FlinkKafkaConsumer[String](shuffleConfig.srcTopic, new SimpleStringSchema(), shuffleConfig.kafkaProperties).setStartFromGroupOffsets()
    )



    val parsed = logSource.rebalance.process(new HubbleLogParsing)

    val appSink = new FlinkKafkaProducer[ParsedEvent](shuffleConfig.defaultDestTopic, new AppKeyDispatchSerializationSchema, shuffleConfig.kafkaProperties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)

    parsed.filter(e => e.deviceUdid != null && e.appKey != null)
      .partitionCustom(new Partitioner[String]{
        override def partition(k: String, partitions: Int): Int = Math.abs(k.hashCode % partitions)
      }, e => e.deviceUdid)
      .addSink(appSink)
      .uid("log-shuffle-sink")

    env.execute("lofter hubble log shuffle")
  }

  val LOG_PATTERN: Regex = """^\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d,\d+  INFO \(.*?\)\[[\w\$]+\] - dataBody:(.*)""".r
  class HubbleLogParsing extends ProcessFunction[String, ParsedEvent] {
    @transient private var logParseErrorCounter: Counter = _

    override def open(parameters: Configuration): Unit = {
      logParseErrorCounter = getRuntimeContext()
        .getMetricGroup()
        .counter("logParseErrors")
    }

    override def processElement(line: String, context: ProcessFunction[String, ParsedEvent]#Context, collector: Collector[ParsedEvent]): Unit = {
        line match {
          case LOG_PATTERN(v) =>
            try {
              if(v.length > MAX_LOG_SIZE) {
                logParseErrorCounter.inc() // just ignore very very long log line which should be truncated
                return
              }

              parseLogEvents(v).foreach { event =>
                if(event != null){
                  collector.collect(event)
                }
              }
            } catch {
              case NonFatal(_) =>
                LOG.error("log parsing error: {}", v)
                logParseErrorCounter.inc()
              case e =>
                LOG.error("log parsing error: {}", v)
                logParseErrorCounter.inc()
                throw new RuntimeException(e)
            }

          case log =>
            LOG.error("log parsing error: {}", log)
            logParseErrorCounter.inc()
        }
    }
  }

  class AppKeyDispatchSerializationSchema extends KafkaSerializationSchema[ParsedEvent] {

    override def serialize(element: ParsedEvent, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      import element._
      val topic = eventType.getTopic(appKey)
      new ProducerRecord[Array[Byte], Array[Byte]](topic, deviceUdid.getBytes("UTF-8"),source.getBytes("UTF-8"))
    }
  }
}
