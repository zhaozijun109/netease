package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

@Description(name = "RegexLinks", value = "extract links using regex from string")
class RegexLinks extends  UDF {
  val linkRegex: Regex = """(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[-a-zA-Z0-9()@:%_\+.~#?&//=]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[-a-zA-Z0-9()@:%_\+.~#?&//=]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[-a-zA-Z0-9()@:%_\+.~#?&//=]{2,}|[a-zA-Z0-9]+\.[-a-zA-Z0-9()@:%_\+.~#?&//=]{2,})""".stripMargin.r

  def evaluate(input: String): java.util.List[String] = {
    if(input == null || input.isEmpty) {
      null
    } else {
      val matches = linkRegex.findAllMatchIn(input).toSeq.map(_.matched)
      matches.filterNot(_.contains(".."))
        .filterNot(_.matches("""[\d\./]+"""))
        .filter(_.contains("/"))
        .filter{ s => s.exists(_.isLetterOrDigit)}
        .collect {
          case url if url != "qq.com" && url != "163.com" && url != "126.com" && url != "gmail.com" =>
            val protocolEndIndex = url.indexOf(':')
            val protocol = if(protocolEndIndex > 0) url.substring(0, protocolEndIndex).toLowerCase else ""
            if(protocol == "http" || protocol == "https") url else s"http://$url"
        }.asJava
    }
  }
}
