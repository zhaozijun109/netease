package com.netease.bdms.ndi.client.instance

import org.json4s._

object ReaderParser {

  def parse(readerType: String, readerData: JValue): Unit = {
    readerType.toLowerCase() match {
      case "mysql" =>
        MySQLParser.parseReader(readerData)
      case "hive" =>
        HiveParser.parseReader(readerData)
      case "ddb" =>
        DDBDBIParser.parseReader(readerData)
      case "ddbqs" =>
        DDBQSParser.parseReader(readerData)
      case "oracle" =>
        OracleParser.parseReader(readerData)
      case _ =>
        throw new IllegalArgumentException(s"Unknown reader type $readerType.")
    }

  }
}
