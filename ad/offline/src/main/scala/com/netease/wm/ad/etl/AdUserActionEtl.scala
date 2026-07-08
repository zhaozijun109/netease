package com.netease.wm.ad.etl

import java.net.URL

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.util.Try
import scala.util.matching.Regex

object AdUserActionEtl {
  val COMMON_PARAM_KEYS = Set("mac", "idfa", "imei", "androidid", "os", "ip", "ua", "user_agent", "clicktime")

  case class AdUserAction(channel: String, mac: Option[String], idfa: Option[String], imei: Option[String],
                          androidid: Option[String], os: Option[String], ip: Option[String], ua: Option[String],
                          clickTime: Option[Long], params: Map[String,String])

  def main(args: Array[String]): Unit = {
    val LOG_PATTERN: Regex = """^.*"GET (\S+) HTTP/.*".*$""".r

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Adx User Action Etl")
      .getOrCreate()

    val input = pargs.required("input")
    val output = pargs.required("output")

    import spark.implicits._

    spark.read.textFile(input)
      .flatMap { line =>
        line match {
          case LOG_PATTERN(requestUrl) =>
            val url = new URL("http://x.com" + requestUrl)

            if(url == null) {
              throw new RuntimeException(s"parse url error for: $line")
            }

            val path = url.getPath
            val queryParams = Option(url.getQuery).getOrElse("").split("&")
              .map(_.split("="))
              .filter(_.length == 2)
              .map{p => p(0) -> p(1)}
              .toMap

            path match {
              case x if x.endsWith("/click") =>
                val channel = x.split("/")(1)
                val mac = queryParams.get("mac")
                val idfa = queryParams.get("idfa")
                val imei = queryParams.get("imei")
                val androidid = queryParams.get("androidid")
                val os = queryParams.get("os")
                val ip = queryParams.get("ip")
                val ua = queryParams.get("ua").orElse(queryParams.get("user_agent"))
                val clickTime = queryParams.get("clicktime").flatMap{x => Try{x.toLong}.toOption}

                val keys = queryParams.keySet
                val params: Map[String, String] = queryParams.filterKeys(k => !COMMON_PARAM_KEYS(k))
                Seq(AdUserAction(channel, mac, idfa, imei, androidid, os, ip, ua, clickTime, params))
              case _ => Seq.empty
            }
          case _ =>
            println("unmatched log line: " + line)
            Seq.empty
        }
      }.write.mode(SaveMode.Overwrite).parquet(output)

    spark.stop()
  }
}
