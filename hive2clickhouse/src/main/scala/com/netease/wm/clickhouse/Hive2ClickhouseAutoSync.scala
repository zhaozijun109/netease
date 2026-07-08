package com.netease.wm.clickhouse

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

private class Hive2ClickhouseAutoSync{}
object Hive2ClickhouseAutoSync {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2ClickhouseAutoSync])
  private val INC_POSTFIX_PATTERN = """.*_(di|wi|mi|dd|wd|md|[0-9]+d)$""".r
  private val DF = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Hive2Clickhouse")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .config("spark.sql.catalog.clickhouse", "com.clickhouse.spark.ClickHouseCatalog")
      .config("spark.sql.catalog.clickhouse.host", clickhouseConfig.clickHouseTableHost)
      .config("spark.sql.catalog.clickhouse.protocol", "http")
      .config("spark.sql.catalog.clickhouse.http_port", "8123")
      .config("spark.sql.catalog.clickhouse.user", "lofter_rw")
      .config("spark.sql.catalog.clickhouse.password", "O4nWNA9slAn8")
      .config("spark.sql.catalog.clickhouse.database", "hive")
      .config("spark.clickhouse.write.format", "arrow")
      .config("spark.clickhouse.ignoreUnsupportedTransform", "true")
      .config("spark.clickhouse.write.batchSize", "5000")
      .enableHiveSupport()
      .getOrCreate()

    val database = clickhouseConfig.hiveDatabase
    val excludes = pargs.optional("excludes").getOrElse("").split(",").filterNot(_.isEmpty).toSet
    val includes = pargs.optional("includes").getOrElse("").split(",").filterNot(_.isEmpty).toSet

    val sourceDB = clickhouseConfig.hiveDatabase
    val destDB = "hive"

    val startTime = System.currentTimeMillis()
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())
    while (System.currentTimeMillis() - startTime < 440 * 1000 && localDateTime.getHour > 4) {
      val synchronizer = new Hive2ClickhouseSynchronizer(DF.format(localDateTime.minusDays(1)), autoFullSyncFallback = true)

      val tableList = spark.catalog.listTables(database).collect().map(_.name)
      val importTableList: Set[String] = tableList
        .filter(t => includes.exists(e => t.contains(e)) || t.contains("_report_") || t.contains("_ab_")
          || t.contains("_ad_"))
        .filterNot(t => excludes.exists(e => t.contains(e)) || t.contains("_wide_table_"))
        .toSet

      val globTables = (importTableList ++ clickhouseConfig.historyLoadJobs)
        .filter {
          case INC_POSTFIX_PATTERN(_) => true
          case _ => false
        }

      val tables = globTables ++ synchronizer.getConfiguredTables
      val tablesForSync = tables.filter(table => !synchronizer.isTableSynced(table) && synchronizer.isTableReadyForSync(table))

      LOG.info("hive2clickhouse auto sync tables: {}", tablesForSync.toSet[String].mkString(" "))

      val failedTables = new scala.collection.mutable.ArrayBuffer[String]

      tablesForSync.foreach { table =>
        try {
          synchronizer.syncTable(sourceDB, table, destDB, spark)
        } catch {
          case NonFatal(e) =>
            LOG.error("exception while sync table to clickhouse: " + table, e)
            failedTables.append(table)
        }
      }
      if(failedTables.nonEmpty) {
        throw new RuntimeException("failed to sync tables: " + failedTables.mkString(" "))
      }
      Thread.sleep(1000)
    }

    spark.close()
  }
}
