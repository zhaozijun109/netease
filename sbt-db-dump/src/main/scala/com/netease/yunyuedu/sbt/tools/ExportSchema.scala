package com.netease.yunyuedu.sbt.tools

import java.sql.{Connection, DriverManager}
import com.netease.yunyuedu.sbt.model.{ColumnMeta, TableMeta}
import com.netease.yunyuedu.sbt.utils.SqlSchema

object ExportSchema {
  def main(args: Array[String]): Unit = {

    if(args.length < 1){
      println("usage: ExportSchema <jdbcUrl>")
      System.exit(1)
    }

    val jdbcUrl = args(0)
    def getDbConn: Connection = {
      Class.forName("com.mysql.jdbc.Driver")
      DriverManager.getConnection(jdbcUrl)
    }

    implicit val db: Connection = getDbConn

    val schema = SqlSchema.readSchema

    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._

    implicit val columnMetaWrites = Json.writes[ColumnMeta]
    implicit val tableMetaWrites = Json.writes[TableMeta]

    println(Json.toJson(schema))
  }

}
