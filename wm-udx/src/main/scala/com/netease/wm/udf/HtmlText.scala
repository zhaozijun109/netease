package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup

@Description(name = "HtmlText", value = "extract html text")
class HtmlText extends UDF {
  def evaluate(html: String): String = {
    if(html == null|| html.isEmpty) {
      ""
    } else {
      val doc = Jsoup.parse(html)
      doc.select("br").before("\n")
      doc.select("p").before("\n")
      doc.text()
    }
  }
}
