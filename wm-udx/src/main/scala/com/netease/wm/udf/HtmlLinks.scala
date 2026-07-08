package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

@Description(name = "HtmlLinks", value = "extract link url from html link anchor")
class HtmlLinks extends UDF {
  def evaluate(html: String): java.util.List[String] = {
    if(html == null || html.isEmpty) {
      null
    } else {
      val doc = Jsoup.parse(html)
      val links = doc.select("a[href]")
      links.asScala.map(_.attr("href")).asJava
    }
  }
}
