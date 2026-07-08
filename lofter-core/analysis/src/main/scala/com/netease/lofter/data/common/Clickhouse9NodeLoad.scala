package com.netease.lofter.data.common

import com.clickhouse.data.ClickHouseDataType
import com.clickhouse.data.value.ClickHouseBitmap
import com.clickhouse.jdbc.ClickHouseDataSource
import com.netease.wm.util.Args
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.roaringbitmap.longlong.Roaring64NavigableMap

import java.io.{ByteArrayInputStream, DataInputStream}

object Clickhouse9NodeLoad {
  val CLICKHOUSE_JDBC_BATCH_SIZE = 256
  val clickHouseJdbcUrl = "jdbc:clickhouse:http://lofter-data-common9.gy.ntes:8123/?socket_timeout=1000000"
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

    val batchSize = pargs.int("batch", CLICKHOUSE_JDBC_BATCH_SIZE)

    val partition = pargs.required("partition")
    val destTable = pargs.required("dest")
    val sourceQuery = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")

    if (sourceQuery.trim.isEmpty) {
      throw new RuntimeException("source query should be set through --source args or spark.source.query config")
    }

    cleanupPartition(destTable, partition)

    val parallel: Option[Int] = pargs.optional("parallel").map(_.toInt)

    val df: Dataset[Row] = parallel match {
      case Some(n) => spark.sql(sourceQuery).repartition(n)
      case None => spark.sql(sourceQuery)
    }

    val columns = df.schema.toList
    df.foreachPartition { iterator: Iterator[Row] =>
      iterator.grouped(batchSize).foreach { rows =>
        insertRows2ClickHouse(rows, columns, destTable)
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

  def cleanupPartition(destTable: String, partition: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)

    conn.prepareStatement(s"alter table $destTable drop partition '$partition' ").execute()

    conn.close()
  }

  def insertRows2ClickHouse(rows: Seq[Row], columns: List[StructField], destTable: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)
    val clickhouseColumnNames = columns.map(_.name).mkString(", ")
    val clickhouseColumnNameAndTypes = columns.map { col =>
      val columnName = col.name
      col.dataType match {
        case IntegerType | LongType => s"$columnName Nullable(UInt64)"
        case BinaryType if columnName.endsWith("_bitmap") => s"$columnName AggregateFunction(groupBitmap, UInt64)"
        case DoubleType | FloatType => s"$columnName Nullable(Float64)"
        case _ if columnName == "dt" => s"$columnName String"
        case _ => s"$columnName Nullable(String)"
      }
    }.mkString(", ")

    val insertSql = s"insert into $destTable select $clickhouseColumnNames from input('$clickhouseColumnNameAndTypes')"
    val ps = conn.prepareStatement(insertSql)

    rows.foreach { row =>
      columns.zipWithIndex.foreach { args =>
        val (col, i) = args
        val columnName = col.name
        col.dataType match {
          case IntegerType =>
            val v = row.getAs[Integer](i)
            if (v == null) ps.setNull(i + 1, java.sql.Types.BIGINT)
            else ps.setLong(i + 1, v.longValue())
          case LongType => ps.setLong(i + 1, row.getAs[Long](i))
          case ArrayType(_, _) =>
            val arr = row.getAs[Seq[Any]](i)
            val str = if (arr == null) null else arr.mkString(",")
            ps.setString(i + 1, str)
          case BooleanType =>
            val v = row.getAs[java.lang.Boolean](i)
            val str = if (v == null) null else v.toString
            ps.setString(i + 1, str)
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
}
