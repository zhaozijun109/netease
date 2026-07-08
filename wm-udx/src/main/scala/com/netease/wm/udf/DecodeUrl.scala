package com.netease.wm.udf

import java.net.URLDecoder

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.util.control.NonFatal

@Description(name = "DecodeUrl", value = "Decode url string")
class DecodeUrl extends UDF {
  def evaluate(input: String): String = {
    if(input == null) {
      null
    } else {
      try {
        URLDecoder.decode(input, "UTF-8")
      } catch {
        case NonFatal(_) => null
      }
    }
  }
}
