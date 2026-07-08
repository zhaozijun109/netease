package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.common.clickhouseConfig.{clickHouseDriverName, clickHousePassword, clickHouseUser}
import com.netease.wm.util.Sql
import org.slf4j.{Logger, LoggerFactory}

import java.sql.{Connection, DriverManager}

private class ClickHouseHelper{}

object ClickHouseHelper {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ClickHouseHelper])

  lazy val defaultConnection: Connection = {
    val clickhouseJdbcUrl = "jdbc:clickhouse://lofter-data-common5.gy.ntes:8123/hive?socket_timeout=300000"
    val clickUser = clickHouseUser
    val clickPass = clickHousePassword
    Class.forName(clickHouseDriverName)
    DriverManager.getConnection(clickhouseJdbcUrl, clickUser, clickPass)
  }

  def queryGroupWithSummary(detailSql: String, summarySql: String, maxItems: Int): (Long, Seq[Long]) = {
    LOG.info("summary sql: {}", summarySql)
    LOG.info("detail sql: {}", detailSql)

    implicit val conn: Connection = defaultConnection
    val total = Sql.queryAll[Long](Sql.read(summarySql), "result").headOption.getOrElse(0L)

    val detail = if (total < maxItems) {
      Sql.queryAll[Long](Sql.read(detailSql), "result")
    } else {
      Sql.queryAll[Long](Sql.read(detailSql), "result").take(maxItems)
    }

    total -> detail
  }

  def isTagGroupReady(dt: String, tags: String, bline: String): Boolean = {
    var isready = false

    val tag1 = tags match {
      case x if x.contains("tag_consume2") || x.contains("tag_consume3") => x.replace("tag_consume2","tag_consume").replace("tag_consume3","tag_consume")
      case x if x.contains("登录活跃情况") => x.replace("登录活跃情况","消费者活跃情况")
      case _ => tags
    }
    val tag = tag1.split(",").map(_.trim.replace("'", "")).toSet[String].map("'" + _ + "'").mkString(",")

    if(tag == "'empty'"){
      isready = true
    }else{
      implicit val conn: Connection = defaultConnection
      val sql = s"""
                   |select ready,count(distinct tag) as nums
                   |from lofter.user_portrait_status_v3
                   |where dt = '$dt'
                   |  and tag in ($tag)
                   |  and b_lines = '$bline'
                   |group by ready
                   |""".stripMargin
      val rs = Sql.read(sql)
      println(s"ck数据校验sql:$sql")
      if(rs.next()){
        if(rs.getInt("ready") == 1 && rs.getInt("nums") == tag.split(",").length) isready = true
      }
    }
    isready
  }
}
