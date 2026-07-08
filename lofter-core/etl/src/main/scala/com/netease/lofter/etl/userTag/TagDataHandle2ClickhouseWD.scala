package com.netease.lofter.etl.userTag

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeConfig
import com.clickhouse.data.ClickHouseDataType
import com.clickhouse.data.value.ClickHouseBitmap
import com.clickhouse.jdbc.ClickHouseDataSource
import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.commons.codec.binary.Base64
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.roaringbitmap.longlong.Roaring64NavigableMap

import java.io.{ByteArrayInputStream, DataInputStream}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TagDataHandle2ClickhouseWD {
  val CLICKHOUSE_JDBC_BATCH_SIZE = 256
  val clickHousePassword = "O4nWNA9slAn8"
  val clickHouseUser = "lofter_rw"
  val clickHouseDriver = "com.clickhouse.jdbc.ClickHouseDriver"
  val clickHouseJdbcUrl = "jdbc:clickhouse:http://lofter-data-common5.gy.ntes:8123/?socket_timeout=1000000"

  case class UserTag(tags: Seq[String], bitmap: ClickHouseBitmap)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .appName("TagDataHandle2Clickhouse")
      //      .master("local[*]")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .config("spark.hadoop.hive.exec.dynamic.partition","true")
      .config("spark.hadoop.hive.exec.dynamic.partition.mode","nonstrict")
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("create temporary function to_bitmap as 'com.netease.wm.udf.bitmap.ToBitmapUDAF';")
    spark.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF'")
    spark.sql("create temporary function bitmap_to_array as 'com.netease.wm.udf.bitmap.BitmapToArrayUDF'")
    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")

    val batchSize = pargs.int("batch", CLICKHOUSE_JDBC_BATCH_SIZE)

    val dt1= pargs.required("dt")
    val dt = DateTime.parse(dt1).plusDays(1).toString("yyyy-MM-dd")
    val tag = pargs.required("tag")
    val b_line = pargs.required("b_line")
    val tag_type = pargs.required("tag_type")
    val bitmap = pargs.boolean("bitmap")
    val only2ck = !pargs.boolean("only2ck")
    var destTable = s"$b_line.user_portrait_datas_wd"

    println(s"dt:$dt")
    val query ={
      import scala.language.existentials
      s"""
         |INSERT OVERWRITE TABLE lofter_dm.ads_lofter_tags_data_wd PARTITION (dt = '$dt', tag = '${stringToBase64(tag)}')
         |${spark.conf.getOption("spark.execute.sql").getOrElse("")}
         |""".stripMargin
    }
    println(s"执行sql：$query")
    if(only2ck) spark.sql(query)

    var sourceQuery = s"select dt, '$tag' as tag, dim1, dim2, dim3, dim4, grp, value, userId from lofter_dm.ads_lofter_tags_data_wd where dt = '$dt' and tag = '${stringToBase64(tag)}'"
    if(bitmap){
      destTable = s"$b_line.user_portrait_datas_bitmap_wd"
      val bitmap_sql =
        s"""
           |INSERT OVERWRITE TABLE lofter_dm.ads_lofter_tags_data_bitmap_wd PARTITION (dt, tag)
           |select max(tag_name) as tag_name,
           |dim1,dim2,dim3,dim4,grp,
           |to_bitmap(userId) as bitmap,
           |max(dt) as dt,
           |max(tag) as tag
           |from lofter_dm.ads_lofter_tags_data_wd
           |where dt='$dt' and tag = '${stringToBase64(tag)}'
           |group by dim1,dim2,dim3,dim4,grp
           |""".stripMargin
      println(bitmap_sql)
      if(only2ck) spark.sql(bitmap_sql)
      sourceQuery = s"select dt, '$tag' as tag, dim1, dim2, dim3, dim4, grp, bitmap as users_bitmap from lofter_dm.ads_lofter_tags_data_bitmap_wd where dt = '$dt' and tag = '${stringToBase64(tag)}'"
    }

    cleanupPartition(destTable, dt, tag, clickHouseJdbcUrl)

    val df: Dataset[Row] = spark.sql(sourceQuery)
    val columns = df.schema.toList
    df.coalesce(50).foreachPartition { iterator: Iterator[Row] =>
      iterator.grouped(batchSize).foreach { rows =>
        insertRows2ClickHouse(rows, columns, destTable, clickHouseJdbcUrl)
      }
    }

    val currentDateTime: LocalDateTime = LocalDateTime.now()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val time = currentDateTime.format(formatter)

    insertReadyForDate(dt, b_line,tag_type, tag, time)

    val tag1 = tag match {
      case "tag_consume" => "tag_consume,tag_consume2,tag_consume3"
      case "消费者活跃情况" => "消费者活跃情况,登录活跃情况"
      case _ => tag
    }

    for(t <- tag1.split(",")){
      var message = Map.empty[String, String]
      message += ("message_type" -> "generic.tag")
      message += ("period" -> "week")
      message += ("b_line" -> b_line)
      message += ("crowd_type" -> "user")
      message += ("tag" -> t)
      message += ("dt" -> dt)
      message += ("sendtime" -> time)

      import scala.collection.JavaConverters._
      val datajson = JSON.toJSONString(message.asJava, SerializeConfig.globalInstance)

//      todo 周标签暂时不发kafka数据  后面需要了再打开
//      import spark.implicits._
//      Seq(datajson).toDF("datajson")
//        .selectExpr("CAST(datajson as string) as value")
//        .write
//        .format("kafka")
//        .option("kafka.bootstrap.servers","lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092")
//        .option("topic","YQ.CMB.CROWD_FLOW")
//        .save()
    }

    spark.close()
  }

  def stringToBase64(str: String): String = {
    "base64-"+Base64.encodeBase64URLSafeString(str.getBytes("UTF-8"))
  }

  def base64ToString(base64Str: String): String = {
    new String(Base64.decodeBase64(base64Str.replace("base64-","")), "UTF-8")
  }

  def insertReadyForDate(dt: String, b_line: String, tagType: String, tag: String, time: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(clickHouseJdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)

    val insertSql = s"insert into lofter.user_portrait_status_v3 values('$dt','$b_line','$tagType','$tag',1,'$time')"

    conn.prepareStatement(insertSql).execute()
  }

  private def deserializeBitmap(bytes: Array[Byte]): ClickHouseBitmap = {
    Roaring64NavigableMap.SERIALIZATION_MODE = Roaring64NavigableMap.SERIALIZATION_MODE_PORTABLE
    if (bytes == null) {
      ClickHouseBitmap.wrap(new Roaring64NavigableMap(), ClickHouseDataType.UInt64)
    } else {
      val in = new DataInputStream(new ByteArrayInputStream(bytes))
      val bitmap = new Roaring64NavigableMap()
      bitmap.deserialize(in)
      ClickHouseBitmap.wrap(bitmap, ClickHouseDataType.UInt64)
    }
  }

  def cleanupPartition(destTable: String, dt: String, tag: String, jdbcUrl: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(jdbcUrl)
    val conn = dataSource.getConnection(clickHouseUser, clickHousePassword)

    val sql = s"alter table $destTable drop partition ('$dt','$tag') "

    conn.prepareStatement(sql).execute()

    conn.close()
  }

  def insertRows2ClickHouse(rows: Seq[Row], columns: List[StructField], destTable: String, jdbcUrl: String): Unit = {
    Class.forName(clickHouseDriver)

    val dataSource = new ClickHouseDataSource(jdbcUrl)
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
}
