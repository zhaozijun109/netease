package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import java.io.IOException
import java.net.{URI, URL}
import scala.io.Codec
import scala.collection.JavaConverters._
import scala.util.Try

@Description(name = "NosUserList", value = "fetch user list from nos link")
class NosUserList extends UDF {

  val userIdRegex = """^(\d+).*""".r
  //implicit val defaultCodec: Codec = Codec.UTF8

  def evaluate(nosUrl: String): java.util.List[java.lang.Long] = {
    if(nosUrl.contains("nos.netease.com") || nosUrl.contains("lf127.net")) {
      val lines = try {
        parseUrlLines(nosUrl.replace("https:", "http:"), "UTF-8")
      } catch {
        case _: IOException =>
          parseUrlLines(nosUrl.replace("https:", "http:"), "UTF-16")
      }

      lines.flatMap {
        case userIdRegex(userId) =>
          Try{
            val v: java.lang.Long = userId.toLong
            v
          }.toOption
        case _ => None
      }.asJava
    } else Seq.empty.asJava
  }

  private def parseUrlLines(url: String, enc: String): Seq[String] = {
    scala.io.Source.fromURL(url, enc).getLines().toSeq
  }
}
