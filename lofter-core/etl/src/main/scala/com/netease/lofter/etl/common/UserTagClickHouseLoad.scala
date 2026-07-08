package com.netease.lofter.etl.common

import com.clickhouse.data.ClickHouseDataType
import com.clickhouse.data.value.ClickHouseBitmap
import com.clickhouse.jdbc.ClickHouseDataSource
import com.netease.wm.util.Args
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.roaringbitmap.longlong.Roaring64NavigableMap

import java.io.{ByteArrayInputStream, DataInputStream}

object UserTagClickHouseLoad {
  val CLICKHOUSE_JDBC_BATCH_SIZE = 256
  val clickHouseJdbcUrl = "jdbc:clickhouse:http://lofter-data-common5.gy.ntes:8123/?socket_timeout=1000000"
  val clickHousePassword = "O4nWNA9slAn8"
  val clickHouseUser = "lofter_rw"
  val clickHouseDriver = "com.clickhouse.jdbc.ClickHouseDriver"

  case class UserTag(tags: Seq[String], bitmap: ClickHouseBitmap)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .appName("Hive2Clickhouse")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    val parallel = pargs.int("parallel", 3)
    val isReady = pargs.boolean("ready")
    if(isReady) {
      val dt = pargs.required("dt")
      val flagTable = pargs.required("flagTable")
      insertReadyForDate(dt, flagTable)
    } else {
      val tagCount = pargs.int("tagCount")
      val destTable = pargs.required("dest")
      val sourceQuery = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")

      if (sourceQuery.trim.isEmpty) {
        throw new RuntimeException("source query should be set through --source args or spark.source.query config")
      }
      val df: Dataset[Row] = spark.sql(sourceQuery).repartition(parallel)
      df.foreachPartition { iterator: Iterator[Row] =>
        iterator.grouped(CLICKHOUSE_JDBC_BATCH_SIZE).foreach { rows =>
          val userTags = rows.map { row =>
            val tags = (0 until tagCount).map(i => row.getAs[String](i))
            val bitmap = row.getAs[Array[Byte]](tagCount)
            UserTag(tags, deserializeBitmap(bitmap))
          }
          insertBitmapValue2ClickHouse(userTags, destTable, tagCount)
        }
      }
    }

    spark.close()
  }

  private def deserializeBitmap(bytes: Array[Byte]): ClickHouseBitmap = {
    Roaring64NavigableMap.SERIALIZATION_MODE = Roaring64NavigableMap.SERIALIZATION_MODE_PORTABLE
    if(bytes == null) {
      ClickHouseBitmap.wrap(new Roaring64NavigableMap(), ClickHouseDataType.UInt64)
    } else {
      val in = new DataInputStream(new ByteArrayInputStream(bytes))
      val bitmap = new Roaring64NavigableMap()
      bitmap.deserialize(in)
      ClickHouseBitmap.wrap(bitmap, ClickHouseDataType.UInt64)
    }
  }

  def insertBitmapValue2ClickHouse(userTags: Seq[UserTag], destTable: String, tagCount: Int): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)
    val tagNames = (0 until tagCount).map { i => s"tag_$i" }.mkString(", ")
    val tagNameAndTypes = (0 until tagCount).map { i => s"tag_$i String" }.mkString(", ")
    val insertSql = s"insert into $destTable select $tagNames, bitmap from input('$tagNameAndTypes, bitmap AggregateFunction(groupBitmap, UInt64)')"
    val ps = conn.prepareStatement(insertSql)

    userTags.foreach { entry =>
      if (entry.tags.size != tagCount) {
        throw new RuntimeException("write clickhouse tag data with tag count " + entry.tags.size + ", but should be " + tagCount)
      }
      entry.tags.zipWithIndex.foreach {
        case (tag, index) => ps.setString(index + 1, tag)
      }
      ps.setObject(tagCount + 1, entry.bitmap)
      ps.addBatch()
    }
    ps.executeBatch()

    conn.close()
  }

  def insertReadyForDate(dt: String, flagTable: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)
    val insertSql = s"insert into $flagTable values('$dt', 1)"
    conn.prepareStatement(insertSql).execute()
  }
}
