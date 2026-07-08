package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

@Description(name = "HtmlImages", value = "extract image url from html content")
class HtmlImages extends UDF {
  def evaluate(html: String): java.util.List[String] = {
    if(html == null || html.isEmpty) {
      null
    } else {
      val doc = Jsoup.parse(html)
      val images = doc.select("img[src]")
      images.asScala.map(_.attr("src")).asJava
    }
  }
}
