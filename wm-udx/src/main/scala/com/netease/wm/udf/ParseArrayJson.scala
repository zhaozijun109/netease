package com.netease.wm.udf

import java.util

import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.json.{JSONArray, JSONException}

@Description(name = "ParseArrayJson", value = "extract json from Array String")
class ParseArrayJson extends UDF {
  def evaluate(str: String): java.util.List[String] = {
    if(str == null || str.isEmpty) {
      null
    } else {
      try {
        val extractObject = new JSONArray(str)
        val result = new util.ArrayList[String]()
        for(i <- 0 until extractObject.length()) {
          result.add(extractObject.get(i).toString)
        }
        result
      } catch  {
        case ex: JSONException => {
          println(s"parse $str throws a JsonException")
          null
        }
        case ex: NumberFormatException => {
          println(s"parse $str throws a NumberFormatException")
          null
        }
        case _ => {
          println(s"parse $str throws a unexpected Exception")
          null
        }
      }
    }
  }
}
