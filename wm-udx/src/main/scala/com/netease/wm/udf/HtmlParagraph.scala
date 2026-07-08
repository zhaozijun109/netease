package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

@Description(name = "HtmlParagraph", value = "extract paragraph from html with pid")
class HtmlParagraph extends UDF {
  def evaluate(html: String, pid: String): String = {
    if(html == null || html.isEmpty ||
      pid == null || pid.isEmpty || pid.contains('"')
    ) {
      null
    } else {
      val doc = Jsoup.parse(html)
      val paragraph = doc.select(s"p#$pid")
      paragraph.html()
    }
  }
}
