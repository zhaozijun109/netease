package com.netease.lofter.etl.common

import java.sql.{Connection, DriverManager}

object databases {

  def getDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.lofterDDBOnLineUrl)
    conn
  }

  def getMallDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.mallOnLineUrl)
    conn
  }

  def getRecFlowDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.lofterRecFlowOnLineUrl)
    conn
  }

}
