package com.netease.yunyuedu.sbt

import sbt._

trait SbtDbDumpJobGeneratorKeys {

  val DEFAULT_CONNECTIONS: Int = 4
  val DEFAULT_TABLE_CONDITION = ""

  val generateJob = taskKey[File]("generate db dump azkaban job zip")
  val connect = settingKey[String => String]("connect url")
  val connectUserName = settingKey[String => String]("connect username")
  val connectPassword = settingKey[String => String]("connect password")
  val connectMode = settingKey[String => String]("connect mode dbi or qs")
  val tableMetaPath = settingKey[File]("path to file contain db meta data, by default db_meta.txt in project base directory")
  val tableFilter = settingKey[String => Boolean]("table filter to filter out to be dumped table")
  val tableJobSettingMapper = settingKey[String => String]("table specific job settings")
  val tableGroupSize = settingKey[Int]("number of table group in job flow")
  val jobConnectionsMapper = settingKey[String => Int]("mapper for how many connection needed for a table")
  val jobOutputBaseDirectory = settingKey[String]("job output base directory")
  val jobOutputDirectoryMapper = settingKey[String => String]("job output directory mapper")
  val jobSplitKeyMapper = settingKey[String => String]("job split key mapper")
  val hiveSchema = settingKey[String]("hive schema name")
  val hiveTablePartitioned = settingKey[Boolean]("generated hive table partitioned or overwrite old data")
  val hiveTableMapper = settingKey[String => String]("mapping db table name to hive table name")
  val generateJobOutput = settingKey[File]("target file for the job zip")
  val highPriorTables = settingKey[Seq[String]]("tables with high priority to import")
  val lowPriorTables = settingKey[Seq[String]]("tables with low priority to import")
  val tableZorderColumnsMapper = settingKey[String => String]("table zorder columns settings")
  val binlogTableMapper = settingKey[String => String]("mapping db table name to binlog table name")

  def snakify(name : String) = name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase

  def camelify(name : String): String = {
    def loop(x : List[Char]): List[Char] = (x: @unchecked) match {
      case '_' :: '_' :: rest => loop('_' :: rest)
      case '_' :: c :: rest => Character.toUpperCase(c) :: loop(rest)
      case '_' :: Nil => Nil
      case c :: rest => c :: loop(rest)
      case Nil => Nil
    }
    if (name == null)
      ""
    else
      loop('_' :: name.toList).mkString
  }
}

object SbtDbDumpJobGeneratorKeys extends SbtDbDumpJobGeneratorKeys
