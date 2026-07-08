package com.netease.yuanqi.test

import com.netease.yuanqi.config.DorisConfig
import org.apache.spark.sql.functions.{col, to_json}
import org.apache.spark.sql.types.{ArrayType => SparkArrayType, MapType => SparkMapType, StructType => SparkStructType}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

class Test2 {

}
object Test2 {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[Test2])

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("SyncTest")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.sql.storeAssignmentPolicy", "ANSI")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    val df = spark.sql("select CURRENT_DATE() as dt, blogId, blog_name, post_top10_ips, post_50_top10_ips, post_top10_tags, post_50_top10_tags from lofter_tmp.ads_cp_data_creator_di limit 3")

    df.printSchema()

    df.show()

    val complexFields = df.schema.fields.filter { f =>
      f.dataType.isInstanceOf[SparkArrayType] ||
        f.dataType.isInstanceOf[SparkMapType] ||
        f.dataType.isInstanceOf[SparkStructType]
    }
    val df2 = if (complexFields.nonEmpty) {
      LOG.info(s"Converting ${complexFields.length} complex-type columns to JSON strings: " +
        s"${complexFields.map(f => s"${f.name}(${f.dataType.simpleString})").mkString(", ")}")
      complexFields.foldLeft(df) { (accDf, field) =>
        accDf.withColumn(field.name, to_json(col(field.name)))
      }
    } else {
      df
    }

    df2.printSchema()

    df2.show()

    df2.write
      .format("doris")
      .option("doris.fenodes", DorisConfig.fenodes)
      .option("doris.table.identifier", "lofter_test.test")
      .option("user", DorisConfig.user)
      .option("password", DorisConfig.password)
      .option("doris.query.port", DorisConfig.feQueryPort)
      .option("doris.fe.auto.fetch", "true")
      .option("doris.sink.auto-redirect", "true")
      .option("doris.sink.batch.size", "1000000")
      .option("doris.sink.batch.interval.ms", "5000")
      .option("doris.sink.max-retries", "3")
      .option("doris.sink.label.prefix", System.currentTimeMillis())
      .option("doris.sink.properties.format", "json")
      .option("doris.sink.properties.read_json_by_line", "true")
      .mode(SaveMode.Append)
      .save()
  }
}
