package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.matching.Regex

@Description(name = "RegexCount", value = "count occurrence of regex in string")
class RegexCount extends UDF {
  val regexCache: mutable.Map[String, Regex] = new mutable.ListMap[String, Regex]

  def evaluate(input: String, pattern: String): Int = {
    val subRegex = regexCache.getOrElseUpdate(input, new Regex(pattern))

    if(input == null|| pattern == null) {
      0
    } else {
      try {
        subRegex.findAllMatchIn(input).size
      } catch {
        case NonFatal(_) => 0
      }
    }
  }
}
