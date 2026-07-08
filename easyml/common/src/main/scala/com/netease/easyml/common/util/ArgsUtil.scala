package com.netease.easyml.common.util

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/6/23.
 */
object ArgsUtil {
  val KEY_VALUE_SEP = ":"

  def parse(args: Array[String]): Map[String, String] = {
    val cmdArgs = new mutable.HashMap[String, String]()
    println("parsing parameter")
    for (arg <- args) {
      val sepIdx = arg.indexOf(KEY_VALUE_SEP)
      if (sepIdx != -1) {
        val k = arg.substring(0, sepIdx).trim
        val v = arg.substring(sepIdx + 1).trim
        if (v != "" && v != "Nan" && v != null) {
          cmdArgs.put(k, v)
          println(s"param $k = $v")
        }
      }
    }
    cmdArgs.toMap
  }
}
