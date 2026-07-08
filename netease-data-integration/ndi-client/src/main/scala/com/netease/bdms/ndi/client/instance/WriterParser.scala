package com.netease.bdms.ndi.client.instance

import com.netease.bdms.ndi.client.util.LogTrait
import org.json4s._

object WriterParser extends LogTrait {
  def combineKey(args: String*): String = args.mkString(".")

  def parse(writerType: String, writerData: JValue): Unit = {
    writerType.toLowerCase() match {
      case "hive" =>
        HiveParser.parseWriter(writerData)
      case "mysql" =>
        MySQLParser.parseWriter(writerData)
      case "ddb" =>
        DDBDBIParser.parseWriter(writerData)
      case "ddbqs" =>
        DDBQSParser.parseWriter(writerData)
      case "oracle" =>
        OracleParser.parseWriter(writerData)
      case _ =>
        throw new IllegalArgumentException(s"Unknown reader type $writerType.")
    }
  }
}
