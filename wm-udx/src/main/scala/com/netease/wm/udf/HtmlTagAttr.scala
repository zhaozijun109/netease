package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

@Description(name = "HtmlTagAttr", value = "extract link tag attributes")
class HtmlTagAttr extends UDF {
  def evaluate(html: String, tag: String, attr: String): java.util.List[String] = {
    if(html == null || html.isEmpty ||
      tag == null || tag.isEmpty ||
      attr == null || attr.isEmpty
    ) {
      null
    } else {
      val doc = Jsoup.parse(html)
      val tags = doc.select(s"$tag[$attr]")
      tags.asScala.map(_.attr(attr)).asJava
    }
  }
}
