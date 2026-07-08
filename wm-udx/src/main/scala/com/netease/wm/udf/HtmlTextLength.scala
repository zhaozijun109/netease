package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.jsoup.Jsoup

@Description(name = "CharacterLength", value = "compute html text length")
class HtmlTextLength extends UDF {
  def evaluate(html: String): Int = {
    if(html == null|| html.isEmpty) {
      0
    } else {
      val content = Jsoup.parse(html).text()
      content.codePointCount(0, content.length())
    }
  }
}
