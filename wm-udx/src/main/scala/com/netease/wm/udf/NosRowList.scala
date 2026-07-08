package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import java.io.IOException
import scala.collection.JavaConverters._
import scala.util.Try

@Description(name = "NosRowList", value = "fetch data list from nos link")
class NosRowList extends UDF {

  def evaluate(nosUrl: String): java.util.List[java.lang.String] = {
    if(nosUrl.contains("nos2-i.service.163.org") || nosUrl.contains("nos.netease.com") || nosUrl.contains("lf127.net") || nosUrl.contains("nosdn.127.net")) {

      val httpUrl = nosUrl.replace("https:", "http:")
      val lines = try {
        parseUrlLines(httpUrl, "UTF-8")
      } catch {
        case e1: IOException =>
          try {
            parseUrlLines(httpUrl, "GBK")
          } catch {
            case e2: IOException =>
              try {
                parseUrlLines(httpUrl, "UTF-16")
              } catch {
                case e3: IOException =>
                  throw new IOException(s"Failed to parse $httpUrl with UTF-8, GBK, and UTF-16", e3)
              }
          }
      }

      lines.flatMap {
        case data if data.length > 1 =>
          Try{
            val v: java.lang.String = data
            v
          }.toOption
        case _ => None
      }.asJava
    } else Seq.empty.asJava
  }

  private def parseUrlLines(url: String, enc: String): Seq[String] = {
    try {
      scala.io.Source.fromURL(url, enc).getLines().toSeq
    }catch {
      case ex:Exception =>
        println(s"数据错误,${ex.getMessage}")
        Seq.empty[String]
    }
  }
}
