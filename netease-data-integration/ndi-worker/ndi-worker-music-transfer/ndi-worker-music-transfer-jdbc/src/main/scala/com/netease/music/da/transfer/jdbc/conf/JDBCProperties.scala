package com.netease.music.da.transfer.jdbc.conf

import java.sql.Connection

import com.netease.music.da.transfer.common.conf.Properties._
import com.netease.music.da.transfer.common.conf.Property

object JDBCProperties {

  val DATABASE = Property("database", Option.empty, toStringFunc)
  val TABLE = Property("table", Option.empty, toStringFunc)
  val COLUMNS = Property("columns", Option(List("*")), toListFunc)
  val PARTITION_NUM = Property("partitionNum", Option(1), toIntFunc)
  val MUST_SPLIT = Property("mustSplit", Option(false), toBooleanFunc)
  val SPLIT_NUM = Property("splitNum", Option.empty, toIntFunc)
  val SPLIT_SIZE = Property("splitSize", Option(1000000L), toLongFunc)
  val MAX_SPLIT_NUM = Property("maxSplitNum", Option(10000), toIntFunc)
  val DRIVER = Property("driver", Option.empty, toStringFunc)

  /**
    * reader
    */
  val FETCH_SIZE = Property("fetchSize", Option(1000), toIntFunc)
  val CONDITION = Property("condition", Option.apply("1 = 1"), toStringFunc)
  val SPLIT = Property("split", Option.empty, toStringFunc)
  val PARTITION_SIZE = Property("partitionSize", Option(toBytesFunc("1024m")), toBytesFunc)
  val ITERATOR_SIZE = Property("iteratorSize", Option(1000000.toLong), toLongFunc)

  val SOURCES = Property("sources", Option.empty, toListFunc)
  val TABLES = Property("tables", Option.empty, toListFunc)
  val TABLE_REGEX = Property("tableRegex", Option.empty, toStringFunc)
  val URL = Property("url", Option.empty, toStringFunc)
  val URL_SUFFIX = Property("urlSuffix", Option.empty, toStringFunc)
  val USER = Property("user", Option.empty, toStringFunc)
  val PASSWORD = Property("password", Option.empty, toStringFunc)


  val PRE_SQL = Property("preSql", Option.empty, toStringFunc)
  val POST_SQL = Property("postSql", Option.empty, toStringFunc)

  val BATCH_SIZE = Property("batchSize", Option(1000), toIntFunc)
  val ISOLATION_LEVEL = Property("isolationLevel", Option(Connection.TRANSACTION_NONE), {
    case "none" => Connection.TRANSACTION_NONE
    case "readUncommitted" => Connection.TRANSACTION_READ_UNCOMMITTED
    case "readCommitted" => Connection.TRANSACTION_READ_COMMITTED
    case "repeatableRead" => Connection.TRANSACTION_REPEATABLE_READ
    case "serializable" => Connection.TRANSACTION_SERIALIZABLE
    case _ => throw new IllegalArgumentException(s"Unexpected value of `isolationLevel`")
  })
  val NEED_REPARTITION = Property("needRepartition", Option(false), toBooleanFunc)
  val REPARTITION_NUM = Property("repartitionNum", Option(1), toIntFunc)

  val USE_QUOTE = Property("useQuote", Option(true), toBooleanFunc)
}