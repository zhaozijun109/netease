package com.netease.music.da.transfer.hive.conf

import com.netease.music.da.transfer.common.conf.Properties._
import com.netease.music.da.transfer.common.conf.Property

class SaveMode

case class InsertInto() extends SaveMode

case class InsertOverwrite() extends SaveMode

object HiveProperties {

  val DATABASE: Property[String] = Property[String]("database", Option.empty, toStringFunc)
  val TABLE: Property[String] = Property[String]("table", Option.empty, toStringFunc)
  val PRE_SQL: Property[String] = Property[String]("preSql", Option.empty, toStringFunc)
  val POST_SQL: Property[String] = Property[String]("postSql", Option.empty, toStringFunc)
  val KEEP_OWNER: Property[Boolean] = Property[Boolean]("keepOwner", Option(true), toBooleanFunc)

  val SAVE_MODE: Property[SaveMode] =
    Property[SaveMode]("saveMode", Option.apply(InsertOverwrite()), {
      case "insertInto" =>
        InsertInto()
      case "insertOverwrite" =>
        InsertOverwrite()
      case _ =>
        throw new IllegalArgumentException("Unexpected parameter 'saveMode'")
    })

  val FILE_MERGE: Property[Boolean] = Property[Boolean]("fileMerge", Option.apply(false), toBooleanFunc)
  val FILE_MERGE_NUMBER: Property[Int] = Property[Int]("fileMergeNum", Option.apply(1), toIntFunc)

  val PARTITION: Property[String] = Property[String]("partition", Option.empty, toStringFunc)

  val SYNC_IMPALA: Property[Boolean] = Property[Boolean]("syncImpala", Option.apply(false), toBooleanFunc)

  val READER_TYPE: Property[String] = Property[String]("type", Option.apply("sql"), toStringFunc)
  val READER_SQL: Property[String] = Property[String]("sql", Option.empty, toStringFunc)

  val COLUMNS: Property[String] = Property[String]("columns", Option.empty, toStringFunc)
  val CONDITION: Property[String] = Property[String]("condition", Option.apply("1 = 1"), toStringFunc)

  val CACHE: Property[Boolean] = Property[Boolean]("cache", Option.apply(true), toBooleanFunc)
}
