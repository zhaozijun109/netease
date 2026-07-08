package com.netease.wm.hubble

import com.netease.wm.hubble.avro.Mda
import com.netease.wm.hubble.common.JsonParseHelper._
import com.netease.wm.hubble.common.avro.binary.AvroBinarySerSchema
import com.netease.wm.hubble.common.{LessShufflePartitioner, kafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.json4s.DefaultFormats
import org.json4s.native.JsonParser._
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.util.control.NonFatal
import scala.util.matching.Regex

private class LogShuffleAvro{}

object LogShuffleAvro {
  val LOG: Logger = LoggerFactory.getLogger(classOf[LogShuffle])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val source = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.online")
      .setGroupId("lofter_mda_avro_online")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val sinkKafkaProperties = {
      val props = new Properties()
      props.setProperty("compression.type", "snappy")
      props
    }

    val avroSink = KafkaSink.builder[Mda]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setKafkaProducerConfig(sinkKafkaProperties)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter.mda.avro")
          .setValueSerializationSchema(new AvroBinarySerSchema[Mda])
          .setPartitioner(new LessShufflePartitioner[Mda](expands = 1))
          .build()
      ).build()

    env.fromSource(source, WatermarkStrategy.noWatermarks(), "mda")
      .flatMap { v =>
        parseLogEvents(v)
      }.sinkTo(avroSink)

    env.execute("lofter mda avro")
  }

  val userPattern: Regex = """\((\d+),(\d+)\)""".r
  val numPattern: Regex = """(\d+)""".r

  def ignoreJsonValue(p: Parser, tokenOption: Option[Token] = None): Token = {
    var token = tokenOption.getOrElse(p.nextToken)
    token match {
      case StringVal(_) =>
      case IntVal(_) =>
      case LongVal(_) =>
      case DoubleVal(_) =>
      case BigDecimalVal(_) =>
      case BoolVal(_) =>
      case NullVal =>
      case FieldStart(_) => p.fail("expect json value but get field start")
      case OpenObj =>
        // continue to CloseObj
        do {
          token = p.nextToken
          token match {
            case FieldStart(_) => ignoreJsonValue(p)
            case CloseObj =>
            case _ => p.fail("expect embedded fields")
          }
        } while(token != CloseObj)

      case OpenArr =>
        // continue to CloseArr
        do {
          token = p.nextToken
          token match {
            case CloseArr =>
            case _ => ignoreJsonValue(p, Some(token))
          }
        } while(token != CloseArr)

      case CloseObj => p.fail("expect json value but get object end")
      case CloseArr => p.fail("expect json value but get object end")
      case End => p.fail("expect json value but get end")
    }
    token
  }

  def parseLogEvents(line: String): Option[Mda] = {
    try {
      implicit val formats = DefaultFormats
      val result = new Mda()
      var dataType: String = "e"
      var parseDone: Boolean = false
      val parser = (p: Parser) => {
        if(p.nextToken != OpenObj) {
          p.fail("expect first token of open object")
        }
        while (!parseDone) {
          p.nextToken match {
            case OpenObj => p.fail("unexpected '{' while parsing")
            case FieldStart("kafkaTime") => getLongValue(p).foreach{ v => result.kafkaTime = v }
            case FieldStart("occurTime") => getLongValue(p).foreach{ v => result.occurTime = v }
            case FieldStart("costTime") => result.costTime = getLongValue(p)
            case FieldStart("dataType") => dataType = getStringValue(p).getOrElse("e")
            case FieldStart("deviceUdid") => getStringValue(p).foreach{ v => result.deviceUdid = v }
            case FieldStart("eventId") => getStringValue(p).foreach{ v => result.eventId = v }
            case FieldStart("appKey") => getStringValue(p).foreach{ v => result.appKey = v }
            case FieldStart("appVersion") => result.appVersion = getStringValue(p)
            case FieldStart("appChannel") => result.appChannel = getStringValue(p)
            case FieldStart("sessionUuid") => result.sessionUuid = getStringValue(p)
            case FieldStart("ip") => result.ip = getStringValue(p)
            case FieldStart("customUDID") => result.customUdid = getStringValue(p)
            case FieldStart("deviceIMEI") => result.imei = getStringValue(p)
            case FieldStart("deviceAndroidId") => result.androidid = getStringValue(p)
            case FieldStart("oaid") =>
              getStringValue(p) match {
                case Some("00000000-0000-0000-0000-000000000000" | "" | "null") => result.oaid = None
                case v => result.oaid = v
              }
            case FieldStart("userId") =>
              val (userId, userType, userName) = getUserIdTypeName(p)
              result.userId = userId
            case FieldStart("attributes") =>
              var token = p.nextToken
              if(token == OpenObj) {
                do {
                  token = p.nextToken
                  token match {
                    case CloseObj => // just go off
                    case FieldStart("text") => result.text = getStringValue(p)
                    case FieldStart("adTrace") => result.adTrace = getStringValue(p)
                    case FieldStart("URL" | "url") => result.url = getStringValue(p)
                    case FieldStart("so64") => result.so64 = getStringValue(p)
                    case FieldStart("action") => result.action = getStringValue(p)
                    case FieldStart("scene") => result.scene = getStringValue(p)
                    case FieldStart("itemType") => result.itemType = getStringValue(p)
                    case FieldStart("itemId") =>
                      getStringValue(p) match {
                        case Some(numPattern(num)) => result.itemId = Some(num.toLong)
                        case _ => // ignore
                      }
                    case FieldStart("页面类型") =>
                      val oldScene = getStringValue(p) match {
                        case Some("单日志页") => "note"
                        case Some("关注页") => "attention"
                        case Some("视频流页") => "videolist"
                        case Some("个人主页") => "homepage"
                        case Some("视频详情页") => "videodetail"
                        case Some("我的喜欢页") => "mylove"
                        case Some("收藏页") => "collection"
                        case Some("其他") => "other"
                        case _ => ""
                      }
                      if(oldScene.nonEmpty && result.scene.isEmpty) {
                        result.scene = Some(oldScene)
                      }
                    case FieldStart("卡片类型") =>
                      val oldItemType = getStringValue(p) match {
                        case Some("文本") => "TEXT"
                        case Some("图文") => "PHOTO"
                        case Some("视频") => "VIDEO"
                        case Some("其他") => "OTHER"
                        case _ => ""
                      }

                      if(oldItemType.nonEmpty && result.itemType.isEmpty)  result.itemType = Some(oldItemType)
                    case FieldStart("文章ID") =>
                      val oldPostId = getStringValue(p) match {
                        case Some(numPattern(num)) => Some(num.toLong)
                        case _ => None
                      }

                      if(oldPostId.nonEmpty && result.itemId.isEmpty) result.itemId = oldPostId
                    case FieldStart("tabId") =>
                      getStringValue(p) match {
                        case Some(numPattern(num)) => result.tabId = Some(num.toLong)
                        case _ => // ignore
                      }
                    case FieldStart("blogId") =>
                      getStringValue(p) match {
                        case Some(numPattern(num)) => result.blogId = Some(num.toLong)
                        case _ => // ignore
                      }
                    case FieldStart("type") => result.actionType = getStringValue(p)
                    case FieldStart(_) => ignoreJsonValue(p)
                    case _ => p.fail("expect embedded object fields before end")
                  }
                } while(token != CloseObj)
              } else if(token != NullVal) {
                p.fail("expect embedded object of attributes but found: " + token)
              }
            case FieldStart(_) => ignoreJsonValue(p)
            case CloseObj =>
              parseDone = true
            case End =>
              if(!parseDone) {
                p.fail("reach doc end without complete parsing")
              }
          }
        }
        if ((dataType == "e" || dataType == "ie") && result.eventId.nonEmpty && result.deviceUdid.nonEmpty) {
          result.deviceOs = result.appKey match {
            case "MA-A4FE-A88932E7A98F" => Some("Android")
            case _ => Some("iOS")
          }
          Some(result)
        } else None
      }

      parse(line, parser)
    } catch {
      case NonFatal(e) =>
        LOG.error("mda parse error for source: {}", line)
        LOG.error("exception detail: ", e)
        None
    }
  }
}
