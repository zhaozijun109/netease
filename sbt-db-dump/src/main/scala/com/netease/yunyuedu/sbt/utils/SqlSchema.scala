package com.netease.yunyuedu.sbt.utils

import java.sql.Connection

import com.netease.yunyuedu.sbt.model.{ColumnMeta, TableMeta}
import sbt._

object SqlSchema {
  def readSchema(implicit conn: Connection): Seq[TableMeta] = {
    val metaData = conn.getMetaData
    val tablesResultSet = metaData.getTables(null, null, null, Array("TABLE"))
    val tables = Iterator.continually{ tablesResultSet.next() }
      .takeWhile(identity)
      .map{ _ => tablesResultSet.getString("TABLE_NAME")}
      .toSeq

    val result = tables.map{ table =>
      val columnsResultSet = metaData.getColumns(null, null,table, null)
      val columns = Iterator.continually{ columnsResultSet.next() }
        .takeWhile(identity)
        .map{ _ => (columnsResultSet.getString("COLUMN_NAME"), columnsResultSet.getString("TYPE_NAME"), columnsResultSet.getString("REMARKS"))}
        .toSeq

      val pkResultSet = metaData.getPrimaryKeys(null, null, table)
      val primaryKeys = Iterator.continually{ pkResultSet.next() }
        .takeWhile(identity)
        .map{ _ => pkResultSet.getString("COLUMN_NAME")}
        .toSet

      val columnsMeta = columns.map {
        case (column, columnType, comment) => ColumnMeta(column, columnType, primaryKeys.contains(column), Option(comment))
      }.toList

      columnsResultSet.close()
      pkResultSet.close()

      TableMeta(table, columnsMeta)
    }.toList

    tablesResultSet.close()

    result
  }

  def readSchema(metaFile: File, tableFilter: String => Boolean): Seq[TableMeta] = {
    import play.api.libs.json._
    import play.api.libs.json.Reads._

    implicit val columnMetaReads = Json.reads[ColumnMeta]
    implicit val tableMetaReads = Json.reads[TableMeta]

    val result = Json.fromJson[Seq[TableMeta]](Json.parse(IO.read(metaFile))).get
    result.filter(t => tableFilter(t.tableName))
  }
}
