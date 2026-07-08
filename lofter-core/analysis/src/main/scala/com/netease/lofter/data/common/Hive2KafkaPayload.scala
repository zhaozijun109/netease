package com.netease.lofter.data.common

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.{col, lit, struct, to_json}
import org.apache.spark.sql.{SparkSession, functions}

/**
 *  Hive2KafkaPayload export hive table to kafka topic in JSON format
 *    --source <query> : 1. use "spark.source.query" env var for long query param.
 *                          pay attention to the upper/lower case
 *    --servers :  the kafka.bootstrap.servers, default value is the backend kafka bootstrap servers
 *    --topic : the topic to write data
 *    --parallel <n> : number of parallel write threads
 *    --withCount : output total row count as field _count
 */
object Hive2KafkaPayload {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val bootstrapServers = pargs.optional("servers").getOrElse(kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
    val topic = pargs.required("topic")
    val messageType = pargs.int("messageType")
    val parallel = pargs.int("parallel", 5)
    val withCount = pargs.boolean("withCount")

    val spark = SparkSession.builder()
      .appName("Lofter Hive2Kafka Task")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    if(!kafkaConfig.ENABLE_OUTPUT) return

    import spark.implicits._

    val source = spark.conf.getOption("spark.source.query").getOrElse("")

    val metaMap = pargs.optional("meta").toSeq.flatMap { s =>
      s.split(",").toSeq.map{ p => val kv = p.split("="); kv(0) -> kv(1) }
    }.toMap

    if (source.isEmpty) {
      System.err.println("source param is required, through env var 'source.query'")
      System.exit(-1)
    }

    val data = if(metaMap.isEmpty) spark.sql(source) else {
      metaMap.foldLeft(spark.sql(source)) { (df, kv) =>
        df.withColumn(kv._1, functions.lit(kv._2))
      }
    }

    val df = if(withCount) {
      data.withColumn("_count", functions.lit(spark.sql(source).count()))
    } else data

    if (data.isEmpty && (!"BI.DS.COMMON".equals(topic) && messageType != 72)
      && (!"GR_BLOG_IA".equals(topic) && messageType != 30000)) {
      System.err.println("Result data is empty, check upstream output tasks.")

      import com.netease.wm.util.mail._

      System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")

      val day = Imports.DateTime.yesterday.toString("yyyy-MM-dd")

      send a Mail(
        from = ("symbiansigned@corp.netease.com", "symbiansigned"),
        to = "zhaozijun03@corp.netease.com" :: Nil,
        bcc = "hzxiaonaitong@corp.netease.com" :: Nil,
        subject = s"$day KafkaPayload写入数据为空",
        message = s"kafka集群地址: $bootstrapServers, kafka名称: $topic, messageType: $messageType, 上游数据产出任务异常"
      )

      spark.stop()
      System.exit(-1)
    }

    val key = pargs.optional("key")

    val toJsonValueColum = to_json(struct(struct(df("*")).as("payload"), lit(messageType).as("messageType"))).as("value")

    if(key.isDefined) {
      df.select(toJsonValueColum, col(key.get))
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", bootstrapServers)
        .option("topic", topic)
        .save()
    } else {
      df.select(toJsonValueColum)
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", bootstrapServers)
        .option("topic", topic)
        .save()
    }

    spark.stop()
  }
}
