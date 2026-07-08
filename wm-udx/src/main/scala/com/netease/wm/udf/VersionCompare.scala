package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name = "VersionCompare", value = "compare version")
class VersionCompare extends UDF {
  def evaluate(v1: String, v2: String): Integer = {
    if(v1 == null || v2 == null || v1.matches(".*[A-Za-z]+.*") || v2.matches(".*[A-Za-z]+.*")) {
      null.asInstanceOf[Integer]
    } else if(v1.isEmpty || v2.isEmpty) {
      Integer.valueOf(v1.compareTo(v2))
    } else {
      val result = v1.split("\\.").filter(_.nonEmpty)
        .zip(v2.split("\\.").filter(_.nonEmpty))
        .filterNot(s => s._1 == s._2)
        .headOption
        .map {
          case (m1, m2) => if(m1.toInt > m2.toInt) 1 else -1
        }.getOrElse(0)
      Integer.valueOf(result)
    }
  }
}
