package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name = "VersionCompress", value = "compress version")
class VersionCompress extends UDF {
  def evaluate(maxVersion: String, version: String, maxCompressSize: Int): String = {
    if(maxCompressSize == 0 || maxVersion == null || version == null || maxVersion.matches(".*[A-Za-z]+.*") || version.matches(".*[A-Za-z]+.*")) {
      version
    } else {
      val maxVersionNums = maxVersion.split("\\.").filter(_.nonEmpty).map(_.toInt).take(maxCompressSize)
      val versionNums = version.split("\\.").filter(_.nonEmpty).map(_.toInt)

      val result = maxVersionNums.zip(versionNums)
        .map {
          case (mv1, v2) => if(v2 >= mv1) v2 else -1
        }.takeWhile(_ >= 0)

      if(result.length == maxVersionNums.length) version else versionNums.take(result.length + 1).mkString(".")
    }
  }
}
