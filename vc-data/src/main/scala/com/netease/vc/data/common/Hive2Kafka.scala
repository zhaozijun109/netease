package com.netease.vc.data.common

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{AnalysisException, Row, SparkSession, functions}

/**
 *  Hive2Kafka export hive table to kafka topic in JSON format
 *    --source <query> : 1. use "spark.source.query" env var for long query param.
 *                          pay attention to the upper/lower case
 *    --servers :  the kafka.bootstrap.servers, default value is the backend kafka bootstrap servers
 *    --topic : the topic to write data
 *    --parallel <n> : number of parallel write threads
 *    --withCount : output total row count as field _count
 *    --username: kafka username
 *    --password: kafka password
 *    --lingerMs: unit<ms> batch sending time interval
 *    --batchSize: unit<b> batch sending data size
 */
object Hive2Kafka {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val bootstrapServers = pargs.optional("servers").getOrElse(kafkaConfig.BOOTSTRAP_SERVERS)
    val topic = pargs.required("topic")
    val parallel = pargs.int("parallel", 5)
    val withCount = pargs.boolean("withCount")
    val lingerMs = pargs.int("lingerMs", 0)
    val batchSize = pargs.int("batchSize", 16384)

    val spark = SparkSession.builder()
      .appName("Lofter Hive2Kafka Task")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val source = spark.conf.getOption("spark.source.query").getOrElse("")

    val metaMap = pargs.optional("meta").toSeq.flatMap { s =>
      s.split(",").toSeq.map{ p => val kv = p.split("="); kv(0) -> kv(1) }
    }.toMap

    if(source.isEmpty) {
      System.err.println("source param is required, through env var 'source.query'")
      System.exit(1)
    }

    val data = try {
      if(metaMap.isEmpty) spark.sql(source) else {
        metaMap.foldLeft(spark.sql(source)) { (df, kv) =>
          df.withColumn(kv._1, functions.lit(kv._2))
        }
      }
    } catch {
      case e: AnalysisException =>
        if (e.message.contains("Table or view not found")) {
          spark.emptyDataFrame
        } else {
          throw e
        }
    }

    val dataFinal = if(withCount) {
      data.withColumn("_count", functions.lit(spark.sql(source).count()))
    } else data

    val filterTopic = Set("")

    if (data.isEmpty && topic == "vc_data_push_msg" && source.contains("50 as msgType")) {
      System.err.println("Result data is empty, check ads_vc_character_package_rank_virall_a_d output task.")
    } else {
      if (data.isEmpty && !filterTopic.exists(t => topic.contains(t))) {
        System.err.println("Result data is empty, check upstream output tasks.")

        import com.netease.wm.util.mail._

        System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")

        val day = Imports.DateTime.yesterday.toString("yyyy-MM-dd")

        send a Mail(
          from = ("symbiansigned@corp.netease.com", "symbiansigned"),
          to = "zhaozijun03@corp.netease.com" :: "hzxiaonaitong@corp.netease.com" ::  Nil,
          subject = s"$day Kafka写入数据为空",
          message = s"kafka集群地址: $bootstrapServers, kafka名称: $topic，上游数据产出任务异常"
        )

        spark.stop()
        System.exit(1)
      }

      val userName = pargs.optional("username")
      val password = pargs.optional("password")
      val key = pargs.optional("key")

      if(userName.isDefined && password.isDefined) {
        dataFinal.repartition(parallel)
          .toJSON
          .write
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("topic", topic)
          .option("kafka.sasl.jaas.config",s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="${userName.get}" password="${password.get}";""")
          .option("kafka.security.protocol", "SASL_PLAINTEXT")
          .option("kafka.sasl.mechanism", "PLAIN")
          .option("kafka.linger.ms",s"$lingerMs")
          .option("kafka.batch.size", s"$batchSize")
          .save()
      } else if(key.isDefined) {
        dataFinal.repartition(parallel)
          .selectExpr(s"cast(${key.get} as string) as key", "to_json(struct(*)) as value")
          .write
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("topic", topic)
          .option("kafka.linger.ms",s"$lingerMs")
          .option("kafka.batch.size", s"$batchSize")
          .save()
      } else {
        dataFinal.repartition(parallel)
          .toJSON
          .write
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("topic", topic)
          .option("kafka.linger.ms",s"$lingerMs")
          .option("kafka.batch.size", s"$batchSize")
          .save()
      }
    }

    spark.stop()
  }
}
