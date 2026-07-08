package com.netease.vc.data.common

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.http.HttpHost
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.util.matching.Regex

/**
 * Hive2Es export hive table to es index
 * --source <query|table|file> : 1. use "spark.source.query" env var for long query param.
 * 2. Query and table source field is lower case
 * To support upper case use parquet file source (mainly for migration)
 *
 * --id <id> : id field for index update/reindex, default 'id'
 * --dest <index> : dest index on es
 * --parallel <n> : number of parallel write threads
 * --update : index update mode, if enabled merge indexed doc with update doc (aka upsert mode)
 * --atom : if enabled, use es index alias for atom submit
 * --partition <p> : index partition, can be used for daily partition update
 * --partitionPrefix <p>: if not specified, use <dest> as prefix
 * --full : if enabled remove current partition alias, direct to new partition, only support in partition mode
 * --postfixsep: when enabled, use '-' as index postfix separator
 * --aliases: additional alias
 * --enableAuth: enable es auth though username and password
 * --username: auth username
 * --password: auth password
 */
object Hive2ESV2 {

  val tableNamePattern: Regex = """(\w+\.)?\w+""".r
  val fileNamePattern: Regex = """/(\w|\.|\-|=|/)+""".r

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)

    val index = pargs.optional("index").getOrElse("vc_index")
    val partition = pargs.optional("partition")
    val save_mode = pargs.optional("savemode").getOrElse("append")
    val parallel = pargs.int("parallel",4)
    val update = pargs.boolean("update")
    val id = pargs.optional("id").getOrElse("id")
    val mode = if (update) "upsert" else "index"

    val esUrl = pargs.optional("url").getOrElse(ESConfig.ES_URL)
    val esHosts = esUrl.split(",").map(_.split(":")(0))
    val esPort = esUrl.split(",").map(_.split(":")(1)).head.toInt
    val username = pargs.optional("username").getOrElse(ESConfig.ES_USERNAME)
    val password = pargs.optional("password").getOrElse(ESConfig.ES_PASSWORD)

    val sparkSessionBuilder = SparkSession.builder()
      .appName("VC Hive2ES Task")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("es.nodes", esHosts.mkString(","))
      .config("es.port", esPort.toString)
      .config("es.write.operation", mode)
      .config("es.net.http.auth.user", username)
      .config("es.net.http.auth.pass", password)

    val spark = sparkSessionBuilder.getOrCreate()

    if (!ESConfig.ENABLE_OUTPUT) return

    val source = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")

    if (source.isEmpty) {
      System.err.println("source param is required, through --source or env var 'source.query'")
      System.exit(1)
    }

    val df = source match {
      case fileNamePattern(_) =>
        if (partition.isDefined) {
          spark.read.parquet(s"$source/dt=${partition.get}").withColumn("dt", lit(partition.get))
        } else spark.read.parquet(source)

      case tableNamePattern(_) =>
        if (partition.isDefined) {
          spark.table(source.trim).where(s"dt = '${partition.get}' ")
        } else spark.table(source.trim)

      case _ => spark.sql(source)
    }

    if (df.isEmpty) {
      System.err.println("Result data is empty, check upstream output tasks.")

      import com.netease.wm.util.mail._
      System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")

      val day = Imports.DateTime.yesterday.toString("yyyy-MM-dd")
      send a Mail(
        from = ("symbiansigned@corp.netease.com", "symbiansigned"),
        to = "zhaozijun03@corp.netease.com" :: Nil,
        bcc = "hzxiaonaitong@corp.netease.com" :: Nil,
        subject = s"$day VC ES写入数据为空",
        message = s"VC $index 索引，上游数据产出任务异常"
      )

      spark.stop()
      System.exit(1)
    }

    df.repartition(parallel)
      .write
      .format("org.elasticsearch.spark.sql")
      .option("es.mapping.id", id) // 指定文档ID字段
      .option("es.resource", index) // 索引名称
      .option("es.batch.size.entries", "1000") // 每批次1000条
      .option("es.batch.write.retry.count", "20") // 重试20次
      .option("es.batch.write.retry.wait", "10s") // 每次重试等待10秒
      .option("es.batch.write.refresh", "false") // 不刷新
      .mode(save_mode) // 写入模式
      .save()

    spark.stop()
  }
}
