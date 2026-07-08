package com.netease.lofter.data.common

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
    val url = "jdbc:mysql:ddb://10.194.225.157:6000,10.194.225.220:6000,10.194.225.72:6000/public?user=public_test&password=eThNXWMVirg&connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8"
    val conn = DriverManager.getConnection(url)
    conn
  }

  def getRecDDBConn: Connection = {
    Class.forName(dbConfig.LBDRIVER)
    val conn = DriverManager.getConnection(dbConfig.recDDBUrl)
    conn
  }

}
