package com.netease.lofter.data.jobs.backend

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, lit, struct, to_json}

object RiskDataSend {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sourceTable = pargs.required("table")
    val topic = "BI.DS.COMMON"
    val taskIdRaw = pargs.required("taskId")

    if(sourceTable.isEmpty || taskIdRaw.isEmpty) {
      println("请在输入框填上必填参数！！！")
    } else {
      val messageType = 73
      //val messageType = 81 //仅自己可见
      val taskId = taskIdRaw.toLong

      val df = spark.table(sourceTable)
        .select(col("id").alias("postId"), col("blogId"))
        .withColumn("taskId", lit(taskId))

      val toJsonValueColum = to_json(struct(struct(df("*")).as("payload"), lit(messageType).as("messageType"))).as("value")

      df.select(toJsonValueColum)
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", "lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092,lofter-kafka-bi-risk4.gy.ntes:9092,lofter-kafka-bi-risk5.gy.ntes:9092,lofter-kafka-bi-risk6.gy.ntes:9092,lofter-kafka-bi-risk7.gy.ntes:9092,lofter-kafka-bi-risk8.gy.ntes:9092")
        .option("topic", topic)
        .save()
    }

  }
}
