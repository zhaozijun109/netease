package com.netease.vc.data.common

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

private class Hive2ClickhouseTableSync{}
object Hive2ClickhouseTableSync {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2ClickhouseAutoSync])

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

    val dt = pargs.required("date")
    val table = pargs.optional("table").getOrElse("")
    val fullMode = pargs.boolean("full")

    val sourceDB = "vc"
    val destDB = "hive"

    val synchronizer = new Hive2ClickhouseSynchronizer(dt)

     val tables = table.split(",").filter(_.nonEmpty).map(_.trim)

        if(fullMode) {
          tables.foreach{ sourceTable =>
            synchronizer.syncTable(sourceDB, sourceTable, destDB, spark, fullMode = true)
          }
        } else {
          tables.foreach { sourceTable =>
            synchronizer.syncTable(sourceDB, sourceTable, destDB, spark)
          }
        }

    spark.close()
  }
}
