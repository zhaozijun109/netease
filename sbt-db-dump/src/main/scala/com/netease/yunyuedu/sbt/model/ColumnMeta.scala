package com.netease.yunyuedu.sbt.model

case class ColumnMeta(columnName: String, columnType: String, isPrimaryKey: Boolean, comment: Option[String])