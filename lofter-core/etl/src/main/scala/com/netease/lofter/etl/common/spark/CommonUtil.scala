package com.netease.lofter.etl.common.spark

object CommonUtil {

  def versionCompare(v1: String, v2: String): Option[Int] = {
    if(v1 == null || v2 == null || v1.matches(".*[A-Za-z]+.*") || v2.matches(".*[A-Za-z]+.*")) {
      None
    } else if(v1.isEmpty || v2.isEmpty) {
      return Some(v1.compareTo(v2))
    } else {
      v1.split("\\.").filter(_.nonEmpty)
        .zip(v2.split("\\.").filter(_.nonEmpty))
        .filterNot(s => s._1 == s._2)
        .headOption
        .map {
          case (m1, m2) => if(m1.toInt > m2.toInt) 1 else -1
        }.orElse(Some(0))
    }
  }

}
