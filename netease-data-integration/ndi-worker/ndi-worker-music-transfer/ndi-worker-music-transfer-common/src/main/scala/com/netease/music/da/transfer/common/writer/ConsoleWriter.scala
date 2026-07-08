package com.netease.music.da.transfer.common.writer

import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConverters._

class ConsoleWriter(spark: SparkSession) extends AbstractDataWriter(spark) {

  override def write(data: DataFrame): Unit = {
    val consoleAccumulator = spark.sparkContext.longAccumulator("ConsoleWriter")
    val schema = data.schema
    val dataBuilder = new StringBuilder()
    data
      .map(record => {
        consoleAccumulator.add(1L)
        record
      })(RowEncoder.apply(schema))
      .collectAsList()
      .asScala
      .foreach { row =>
        dataBuilder ++= row.mkString("", ", ", "\n")
      }
    LOG.info(
      s"""
         |number: ${consoleAccumulator.value}
         |
         |schema:
         |${schema.treeString}
         |data:
         |$dataBuilder
      """.stripMargin)
  }

  override def confPrefix: String = "spark.transmit.writer.console"

}
