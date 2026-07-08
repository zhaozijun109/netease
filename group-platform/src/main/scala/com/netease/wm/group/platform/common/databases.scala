package com.netease.wm.group.platform.common

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

  def getTestDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val url = "jdbc:mysql:ddb://10.57.60.11:6000/public?user=public_test&password=eThNXWMVirg&connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8"
    val conn = DriverManager.getConnection(url)
    conn
  }

  def getRecFlowDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.lofterRecFlowOnLineUrl)
    conn
  }

  def getRecDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.recDDBUrl)
    conn
  }

  def getRecTestDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.recDDBTestUrl)
    conn
  }

}
