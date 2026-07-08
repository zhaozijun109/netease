package com.netease.wm.ad.etl

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import okhttp3.HttpUrl
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try
import scala.util.matching.Regex

private class AdxMonitorResourceEtl{}

object AdxMonitorResourceEtl {
  case class ResourceEvent(postId: Long, userId: Long, groupId: String, deviceId: String, os: String, opTime: Long, opType: String)

  val LOG_PATTERN: Regex = """^\d+\.\d+\.\d+\.\d+ -\s+-\s+\[(.+?)] "GET (.+?) HTTP.+?".*$""".r
  val NEW_LOG_PATTERN: Regex = """^\s+-\s+-\s+\[(.+?)] "GET (.+?) HTTP.+?".*$""".r

  val SIGN_SALT: String = "lU9FonIQfQkptK8ze25LrDodECjeTjZ0FIgvO3f4L"

  val LOG: Logger = LoggerFactory.getLogger(classOf[AdxMonitorResourceEtl])

  def verifySign(event: ResourceEvent, sign: String): Boolean = {
    import event._
    val m = java.security.MessageDigest.getInstance("MD5")
    m.reset()
    val digest = m.digest(s"${SIGN_SALT}$postId$userId$groupId$deviceId$os".getBytes)
    val sb: StringBuffer = new StringBuffer
    digest.foreach { b =>
      sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3))
    }
    sb.toString.toLowerCase.equals(sign)
  }

  def asLong(input: String): Option[Long] = Try{input.toLong}.toOption

  val NGINX_TIME_FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd/MMM/YYYY:HH:mm:ss Z")

  def parseAdEvent(nginxTimeStr: String, requestUrl: String): Option[ResourceEvent] = {
    val url = HttpUrl.parse("http://ad.mh.163.com" + requestUrl)
    val nginxRequestTime = DateTime.parse(nginxTimeStr, NGINX_TIME_FORMAT).getMillis

    if(url == null) {
      println(s"parse url error for: $requestUrl")
      None
    } else {
      val path = url.encodedPath()

      // only ad events
      path match {
        case "/res/c" | "/res/e" | "/resDownload/monitor" =>
          val postId = asLong(url.queryParameter("postId"))
          val userId = asLong(url.queryParameter("userId"))
          val groupId = url.queryParameter("groupId")
          val deviceId = url.queryParameter("deviceId")
          val os = url.queryParameter("os")
          val sign = url.queryParameter("sign")

          val eventType = path match {
            case "/res/c" => "click"
            case "/res/e" => "expose"
            case "/resDownload/monitor" => url.queryParameter("type")
            case _ => "unknown"
          }

          val adGameEvent = ResourceEvent(postId.getOrElse(0L), userId.getOrElse(0L), groupId, deviceId, os, nginxRequestTime, eventType)
          if(verifySign(adGameEvent, sign)) {
            Some(adGameEvent)
          } else None
        case _ => None
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("AdxEtl")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)
    val input = pargs.required("input")

    import spark.implicits._

    spark.read.textFile(input)
      .flatMap { line =>
        line match {
          case LOG_PATTERN(nginxRequestTime, requestUrl) => parseAdEvent(nginxRequestTime, requestUrl)
          case NEW_LOG_PATTERN(nginxRequestTime, requestUrl) => parseAdEvent(nginxRequestTime, requestUrl)
          case _ => // ignore
            LOG.error("wrong format log: {}", line)
            None
        }
      }
      .withColumn("dt", lit(day))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.ods_log_ad_resource_action_di")

    spark.stop()
  }
}
