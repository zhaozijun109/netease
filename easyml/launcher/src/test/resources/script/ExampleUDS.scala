package com.netease.easyml.launcher.command

import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2020/9/4.
 */
object ExampleUDS extends Serializable {

  def run(spark: SparkSession, args: Array[String]): Unit = {
    import spark.implicits._
    val size = args(0).toInt
    val add = args(1).toInt
    val df = spark.sparkContext.makeRDD(0 until (size))
      .map(_ + add)
      .toDF("id")
    df.show(false)
  }
}
