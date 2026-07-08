package com.netease.music.da.transfer.jdbc.util

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import org.apache.spark.sql.catalyst.TableIdentifier

object JDBCUtils {
  def getQueryTableName(tableIdentifier: TableIdentifier, useQuote: Boolean): String = if (useQuote) {
    tableIdentifier.quotedString
  } else {
    tableIdentifier.unquotedString
  }

  def getQueryTableName(tableIdentifier: TableIdentifier, properties: Properties): String = {
    getQueryTableName(tableIdentifier, properties.getProperty(JDBCProperties.USE_QUOTE).get)
  }
}
