package com.netease.wm.hubble.common

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.hubble.avro.MdaHive
import com.netease.wm.hubble.common.JsonParseHelper._
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import org.json4s.DefaultFormats
import org.json4s.native.JsonParser._
import org.slf4j.{Logger, LoggerFactory}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.util.control.NonFatal
import scala.util.matching.Regex

private class MdaHelper {}

object MdaHelper {
  val LOG: Logger = LoggerFactory.getLogger(classOf[MdaHelper])
  val MDA_MODEL = new MdaHive()
  val MDA_LENGTH = MDA_MODEL.productArity

  val eventPattern = """^([a-zA-Z0-9]+-[0-9]+|da_.*)$""".r
  val kvPattern: Regex = """(\w+):(.*)""".r

  @inline def clearMdaEvent(in: MdaHive): Unit = {
    for(i <- 0 until MDA_LENGTH) {
      in.put(i, MDA_MODEL.get(i))
    }
  }

  def parseMdaEvent(input: String, result: MdaHive, params: AppendOnlyStringMap): Option[MdaHive] = {
    try {
      implicit val formats = DefaultFormats
      var dataType: String = "e"
      var sessionStartTime: Long = 0L
      var sessionCloseTime: Long = 0L
      var errorMsg: String =  null

      var parseDone: Boolean = false
      val parser = (p: Parser) => {
        if (p.nextToken != OpenObj) {
          errorMsg = "expect first token of open object"
          parseDone = true
        }
        while (!parseDone) {
          p.nextToken match {
            case OpenObj => errorMsg = "unexpected '{' while parsing"; parseDone = true
            // unused fields just ignore
            case FieldStart("uploadTime" | "uploadNum" | "persistedTime" | "timestamp" | "time") => ignoreJsonValue(p)
            case FieldStart("dataType") => getStringValue(p).foreach { v => dataType = v }
            case FieldStart("kafkaTime") => getLongValue(p).foreach { v => result.kafkaTime = v }
            case FieldStart("occurTime") => getLongValue(p).foreach { v => result.occurTime = v }
            case FieldStart("costTime" | "costtime") => result.costTime = getLongValue(p)
            case FieldStart("sessionStartTime") => getLongValue(p).foreach { v => sessionStartTime = v }
            case FieldStart("sessionCloseTime") => getLongValue(p).foreach { v => sessionCloseTime = v }
            case FieldStart("deviceUdid") => getStringValue(p).foreach { v => result.deviceUdid = v }
            case FieldStart("eventId") => getStringValue(p).foreach { v => result.eventId = v }
            case FieldStart("appVersion") => getStringValue(p).foreach { v => result.appVersion = v }
            case FieldStart("appChannel") => getStringValue(p).foreach { v => result.appChannel = v }
            case FieldStart("category") => getStringValue(p).foreach { v => result.category = v }
            case FieldStart("label") => getStringValue(p).foreach { v => result.label = v }
            case FieldStart("customUDID") => getStringValue(p).foreach { v => result.customUDID = v }
            case FieldStart("sessionUuid") => getStringValue(p).foreach { v => result.sessionUuid = v }
            case FieldStart("sdkVersion") => getStringValue(p).foreach { v => result.sdkVersion = v }
            case FieldStart("sdkType") => result.sdkType = getStringValue(p)
            case FieldStart("ip") => getStringValue(p).foreach { v => result.ip = v }
            case FieldStart("deviceAndroidId") => result.deviceAndroidId = getStringValue(p)
            case FieldStart("devicePlatform") => getStringValue(p).foreach { v => result.ip = v }
            case FieldStart("deviceOs") => getStringValue(p).foreach { v => result.deviceOs = v }
            case FieldStart("deviceOsVersion") => getStringValue(p).foreach { v => result.deviceOsVersion = v }
            case FieldStart("deviceModel") => getStringValue(p).foreach { v => result.deviceModel = v }
            case FieldStart("deviceResolution") => getStringValue(p).foreach { v => result.deviceResolution = v }
            case FieldStart("deviceOldMacAddr") => getStringValue(p).foreach { v => result.deviceOldMacAddr = v }
            case FieldStart("deviceMacAddr") => getStringValue(p).foreach { v => result.deviceMacAddr = v }
            case FieldStart("deviceIdfv") => getStringValue(p).foreach { v => result.deviceIdfv = v }
            case FieldStart("deviceAdid") => getStringValue(p).foreach { v => result.deviceAdid = v }
            case FieldStart("deviceIMEI") => getStringValue(p).foreach { v => result.deviceImei = v }
            case FieldStart("deviceNetwork") => getStringValue(p).foreach { v => result.deviceNetwork = v }
            case FieldStart("deviceCarrier") => getStringValue(p).foreach { v => result.deviceCarrier = v }
            case FieldStart("appKey") => getStringValue(p).foreach { v => result.appKey = v }
            case FieldStart("city") => getStringValue(p).foreach { v => result.city = v }
            case FieldStart("timeZone") => getStringValue(p).foreach { v => result.timeZone = v }
            case FieldStart("localeLanguage") => getStringValue(p).foreach { v => result.localeLanguage = v }
            case FieldStart("localeCountry") => getStringValue(p).foreach { v => result.localeCountry = v }
            case FieldStart("wifiBssid") => getStringValue(p).foreach { v => result.wifiBssid = v }
            case FieldStart("wifiSsid") => getStringValue(p).foreach {
              case "<unknown ssid>" => result.wifiSsid = ""
              case v => result.wifiSsid = v
            }
            case FieldStart("oaid") => result.oaid = getStringValue(p)
            case FieldStart("isBeta") => result.isBeta = getStringValue(p)
            case FieldStart("_source") => result._source = getStringValue(p)
            case FieldStart("userId") =>
              val (userId, userType, userName) = getUserIdTypeName(p)
              result.userId = userId
              result.userType = userType
              result.userName = userName
            case FieldStart("attributes") =>
              var token = p.nextToken
              if(token == OpenObj) {
                do {
                  token = p.nextToken
                  token match {
                    case CloseObj => // just go off
                    case FieldStart("UserTypeDesc" | "UserType" | "$type" | "$userProfile") =>
                      // TODO
                      ignoreJsonValue(p)
                    case FieldStart("source") => result.source = getStringValue(p)
                    case FieldStart("deviceAndroidId") => result.deviceAndroidId = getStringValue(p)
                    case FieldStart("recId") => result.recId = getStringValue(p)
                    case FieldStart("action") => result.action = getStringValue(p)
                    case FieldStart("tagName") => result.tagName = getStringValue(p)
                    case FieldStart("layout") => result.layout = getStringValue(p)
                    case FieldStart("algInfo") => result.algInfo = getStringValue(p)
                    case FieldStart("scene") => result.scene= getStringValue(p)
                    case FieldStart("itemType") => result.itemType = getStringValue(p)
                    case FieldStart("itemId") => result.itemId = getStringValue(p)
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
                      val oldPostId = getStringValue(p)
                      if(oldPostId.nonEmpty && result.itemId.isEmpty) result.itemId = oldPostId
                    case FieldStart(name) =>
                      getStringValue(p).filter(_.nonEmpty).foreach{ value => params += name -> value }
                    case _ => errorMsg = "expect embedded object fields before end"; parseDone = true
                  }
                } while(token != CloseObj)
              } else if(token != NullVal) {
                errorMsg = "expect embedded object of attributes but found: " + token; parseDone = true
              }
            case FieldStart(name) =>
              getStringValue(p).filter(_.nonEmpty).foreach{ value => params += name -> value }
            case CloseObj =>
              parseDone = true
            case End =>
              if (!parseDone) {
                errorMsg= "reach doc end without complete parsing"; parseDone = true
              }
          }
        }

        if(errorMsg == null) {
          dataType match {
            case "s" if sessionStartTime > 0 => result.occurTime = sessionStartTime * 1000L
            case "c" if sessionCloseTime > 0 => result.occurTime = sessionCloseTime * 1000L
            case _ => result.occurTime = fixTimeUnit(result.occurTime)
          }

          if ((dataType == "e" || dataType == "ie" || dataType == "s" || dataType == "c") && result.deviceUdid != null) {
            result.params = params.asImmutable
            Some(result)
          } else None
        } else {
          LOG.error("parse mda error: {}", errorMsg)
          None
        }
      }

      parse(input, parser)
    } catch {
      case NonFatal(e) =>
        LOG.error("exception detail: ", e)
        None
    }

  }

  private val NORM_TIME_MIN = DateTime.parse("2000-01-01").getMillis
  private val NORM_TIME_MAX = DateTime.parse("2050-01-01").getMillis

  // try to fix time unit from seconds to millisecond if possible
  @inline private def fixTimeUnit(time: Long): Long = {
    if(time < NORM_TIME_MIN && time * 1000 < NORM_TIME_MAX) {
      time * 1000
    } else {
      time
    }
  }

  case class MdaHiveBucketAssigner(dateFormat: String) extends BucketAssigner[MdaHive, String] {
    @transient var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

    override def getBucketId(element: MdaHive, context: BucketAssigner.Context): String = {
      if(formatter == null) {
        formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
      }

      "dt=" + formatter.format(Instant.ofEpochMilli(element.kafkaTime))
    }

    override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
  }
}
