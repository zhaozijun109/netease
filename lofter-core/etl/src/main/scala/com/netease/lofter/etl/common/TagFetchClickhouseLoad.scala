package com.netease.lofter.etl.common

import com.clickhouse.data.ClickHouseDataType
import com.clickhouse.data.value.ClickHouseBitmap
import com.clickhouse.jdbc.ClickHouseDataSource
import com.netease.wm.util.Args
import org.apache.spark.sql.types.{BinaryType, DoubleType, FloatType, IntegerType, LongType, StructField}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.roaringbitmap.longlong.Roaring64NavigableMap

import java.io.{ByteArrayInputStream, DataInputStream}
import java.sql.Connection

object TagFetchClickhouseLoad {
  val CLICKHOUSE_JDBC_BATCH_SIZE = 256
  val clickHouseJdbcUrl = "jdbc:clickhouse:http://lofter-data-common6.gy.ntes:8123/?socket_timeout=1000000"
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
      .config("spark.task.maxFailures", 1)
      .config("spark.stage.maxConsecutiveAttempts", 1)
      .config("spark.excludeOnFailure.enabled", "false")
      .enableHiveSupport()
      .getOrCreate()

    val batchSize = pargs.int("batch", CLICKHOUSE_JDBC_BATCH_SIZE)

    val partition = pargs.required("partition")
    val destTable = pargs.required("dest")
    val tmpDestTable = if(destTable == "lofter.tag_fetch_tag_action_dd") destTable else s"${destTable}_tmp_"
    val sourceQuery = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")
    val parallel = pargs.int("parallel", 3)

    if (sourceQuery.trim.isEmpty) {
      throw new RuntimeException("source query should be set through --source args or spark.source.query config")
    }

    prepareForWriting(tmpDestTable, destTable, partition)

    val df: Dataset[Row] = spark.sql(sourceQuery).repartition(parallel)

    val columns = df.schema.toList
    df.foreachPartition { iterator: Iterator[Row] =>
      iterator.grouped(batchSize).foreach { rows =>
        insertRows2ClickHouse(rows, columns, tmpDestTable)
        if (destTable == "lofter.tag_fetch_tag_action_dd") {
          Thread.sleep(1000)
        }
      }
    }

    commitWriting(tmpDestTable, destTable, partition)

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

  def createConnection(): Connection = {
    Class.forName(clickHouseDriver)
    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)
    conn
  }

  def prepareForWriting(tmpDestTable: String, destTable: String, partition: String): Unit = {
    if(destTable != "lofter.tag_fetch_tag_action_dd") {
      val conn = createConnection
      conn.prepareStatement(s"drop table if exists $tmpDestTable")
      conn.prepareStatement(s"create table if not exists $tmpDestTable as $destTable").execute()
      conn.createStatement().execute(s"alter table $tmpDestTable drop partition '$partition' ")
      conn.close()
    }
  }

  def insertRows2ClickHouse(rows: Seq[Row], columns: List[StructField], tmpDestTable: String): Unit = {
    val conn = createConnection
    val clickhouseColumnNames = columns.map(_.name).mkString(", ")
    val clickhouseColumnNameAndTypes = columns.map { col =>
      val columnName = col.name
      col.dataType match {
        case IntegerType | LongType => s"$columnName UInt64"
        case BinaryType if columnName.endsWith("_bitmap") => s"$columnName AggregateFunction(groupBitmap, UInt64)"
        case DoubleType | FloatType => s"$columnName Float64"
        case _ => s"$columnName String"
      }
    }.mkString(", ")

    val insertSql = s"insert into $tmpDestTable select $clickhouseColumnNames from input('$clickhouseColumnNameAndTypes')"
    val ps = conn.prepareStatement(insertSql)

    rows.foreach { row =>
      columns.zipWithIndex.foreach { args =>
        val (col, i) = args
        val columnName = col.name
        col.dataType match {
          case IntegerType => ps.setLong(i + 1, row.getAs[Integer](i).longValue())
          case LongType => ps.setLong(i + 1, row.getAs[Long](i))
          case BinaryType if columnName.endsWith("_bitmap") =>
            val bitmap = row.getAs[Array[Byte]](i)
            ps.setObject(i + 1, deserializeBitmap(bitmap))
          case DoubleType =>
            ps.setDouble(i + 1, row.getAs[Double](i))
          case FloatType =>
            ps.setDouble(i + 1, row.getAs[Float](i))
          case _ => s"$columnName String"
            ps.setString(i + 1, row.getAs[String](i))
        }
      }
      ps.addBatch()
    }

    ps.executeBatch()

    conn.close()
  }

  def commitWriting(tmpDestTable: String, destTable: String, partition: String): Unit = {
    if(destTable == "lofter.tag_fetch_tag_action_dd") {
      // TODO direct write, do nothing
    }
    else {
      val conn = createConnection()
      conn.createStatement().execute(s"alter table $destTable drop partition '$partition' ")
      conn.createStatement().execute(s"alter table $tmpDestTable move partition '$partition' to table $destTable ")
      conn.createStatement().execute(s"drop table $tmpDestTable")
      conn.close()
    }
  }
}
