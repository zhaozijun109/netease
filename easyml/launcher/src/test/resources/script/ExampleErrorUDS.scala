package com.netease.easyml.launcher.command

import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2020/9/4.
 */
object ExampleErrorUDS extends Serializable {

  def add(value: Int): Int = value + 1

  def run(spark: SparkSession, args: Array[String]): Unit = {
    import spark.implicits._
    1 / 0
    val length = if (args.nonEmpty) args(0).toInt else 4
    val df = spark.sparkContext.makeRDD(0 until (length)).filter(_ > 1).map(add).toDF("id")
    df.show(false)
  }
}
