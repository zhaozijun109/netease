package com.netease.lofter.data.common

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SparkSession, functions}

/**
 *  Hive2Kafka export hive table to kafka topic in JSON format
 *    --source <query> : 1. use "spark.source.query" env var for long query param.
 *                          pay attention to the upper/lower case
 *    --servers :  the kafka.bootstrap.servers, default value is the backend kafka bootstrap servers
 *    --topic : the topic to write data
 *    --parallel <n> : number of parallel write threads
 *    --withCount : output total row count as field _count
 */
object Hive2KafkaRaw {

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val bootstrapServers = pargs.optional("servers").getOrElse(kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
    val topic = pargs.required("topic")
    val parallel = pargs.int("parallel", 5)

    val spark = SparkSession.builder()
      .appName("Lofter Hive2Kafka Task")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    if(!kafkaConfig.ENABLE_OUTPUT) return

    val source = spark.conf.getOption("spark.source.query").getOrElse("")

    if (source.isEmpty) {
      System.err.println("source param is required, through env var 'source.query'")
      System.exit(-1)
    }

    val data = spark.sql(source)

    if (data.isEmpty) {
      System.err.println("Result data is empty, check upstream output tasks.")

      import com.netease.wm.util.mail._

      System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")

      val day = Imports.DateTime.yesterday.toString("yyyy-MM-dd")

      send a Mail(
        from = ("symbiansigned@corp.netease.com", "symbiansigned"),
        to = "zhaozijun03@corp.netease.com" :: Nil,
        bcc = "hzxiaonaitong@corp.netease.com" :: Nil,
        subject = s"$day KafkaRow写入数据为空",
        message = s"kafka集群地址: $bootstrapServers, kafka名称: $topic，上游数据产出任务异常"
      )

      spark.stop()
      System.exit(-1)
    }

    val userName = pargs.optional("username")
    val password = pargs.optional("password")

    import spark.implicits._
    if(userName.isDefined && password.isDefined) {
      data.repartition(parallel)
        .select("value").as[String]
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", bootstrapServers)
        .option("topic", topic)
        .option("kafka.sasl.jaas.config",s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="${userName.get}" password="${password.get}";""")
        .option("kafka.security.protocol", "SASL_PLAINTEXT")
        .option("kafka.sasl.mechanism", "PLAIN")
        .save()
    } else {
      data.repartition(parallel)
        .select("value").as[String]
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", bootstrapServers)
        .option("topic", topic)
        .save()
    }

    spark.stop()
  }
}
