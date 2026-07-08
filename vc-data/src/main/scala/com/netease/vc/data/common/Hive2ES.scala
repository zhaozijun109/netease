package com.netease.vc.data.common

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest
import co.elastic.clients.elasticsearch.indices.update_aliases.{Action, AddAction, RemoveAction}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{acos, lit}
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.elasticsearch.rest.RestStatus

import java.util
import scala.util.matching.Regex

/**
 *  Hive2Es export hive table to es index
 *    --source <query|table|file> : 1. use "spark.source.query" env var for long query param.
 *                                  2. Query and table source field is lower case
 *                                  To support upper case use parquet file source (mainly for migration)
 *
 *    --id <id> : id field for index update/reindex, default 'id'
 *    --dest <index> : dest index on es
 *    --parallel <n> : number of parallel write threads
 *    --update : index update mode, if enabled merge indexed doc with update doc (aka upsert mode)
 *    --atom : if enabled, use es index alias for atom submit
 *    --partition <p> : index partition, can be used for daily partition update
 *    --partitionPrefix <p>: if not specified, use <dest> as prefix
 *    --full : if enabled remove current partition alias, direct to new partition, only support in partition mode
 *    --postfixsep: when enabled, use '-' as index postfix separator
 *    --aliases: additional alias
 *    --enableAuth: enable es auth though username and password
 *    --username: auth username
 *    --password: auth password
 */
object Hive2ES {

  val tableNamePattern: Regex = """(\w+\.)?\w+""".r
  val fileNamePattern: Regex = """/(\w|\.|\-|=|/)+""".r

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)

    val dest = pargs.required("dest")
    val partition = pargs.optional("partition")
    val partitionPrefix = pargs.optional("partitionPrefix").getOrElse(dest)

    val parallel = pargs.int("parallel", 4)
    val atom = pargs.boolean("atom")
    val update = pargs.boolean("update")
    val id = pargs.optional("id").getOrElse("id")
    val full = pargs.boolean("full")
    val indexSep = if(pargs.boolean("postfixsep")) "-" else "_"
    val mode = if(update) "upsert" else "index"
    val aliases = pargs.optional("aliases").toSeq.flatMap(_.split(",")).map(_.trim).filter(_.nonEmpty)

    val esUrl = pargs.optional("url").getOrElse(ESConfig.ES_URL)
    val esHosts = esUrl.split(",").map(_.split(":")(0))
    val esPort = esUrl.split(",").map(_.split(":")(1)).head.toInt
    val esHttpHosts = esHosts.filter(_.nonEmpty).map { n => new HttpHost(n, esPort) }

    val sparkSessionBuilder = SparkSession.builder()
      .appName("Lofter Hive2ES Task")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("es.nodes", esHosts.mkString(","))
      .config("es.port", esPort.toString)
      .config("es.write.operation", mode)
      .config("es.mapping.id", id)

    val username = pargs.optional("username").getOrElse(ESConfig.ES_USERNAME)
    val password = pargs.optional("password").getOrElse(ESConfig.ES_PASSWORD)
    sparkSessionBuilder
      .config("es.net.http.auth.user", username)
      .config("es.net.http.auth.pass", password)

    val spark = sparkSessionBuilder.getOrCreate()

    if(!ESConfig.ENABLE_OUTPUT) return

    val source = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")

    if(source.isEmpty) {
      System.err.println("source param is required, through --source or env var 'source.query'")
      System.exit(1)
    }

    val writeEsConfig = Map(
      ConfigurationOptions.ES_NODES_WAN_ONLY -> "true",
      ConfigurationOptions.ES_NODES -> esHosts.mkString(","),
      ConfigurationOptions.ES_PORT -> esPort.toString,
      ConfigurationOptions.ES_INDEX_AUTO_CREATE -> "true",
      ConfigurationOptions.ES_WRITE_OPERATION -> mode,
      ConfigurationOptions.ES_MAPPING_ID -> id,
      ConfigurationOptions.ES_BATCH_SIZE_ENTRIES -> "5000",
      ConfigurationOptions.ES_BATCH_SIZE_BYTES -> "3mb",
      ConfigurationOptions.ES_BATCH_WRITE_REFRESH -> "false"
    )

    import org.elasticsearch.spark.sql._

    val df = source match {
      case fileNamePattern(_) =>
        if(partition.isDefined) {
          // work around for spark parquet partition discovery problem of dt type inference to date type
          // spark.read.option("basePath", source).parquet(s"$source/dt=${partition.get}")
          spark.read.parquet(s"$source/dt=${partition.get}").withColumn("dt", lit(partition.get))
        } else spark.read.parquet(source)

      case tableNamePattern(_) =>
        if(partition.isDefined) {
          spark.table(source.trim).where(s"dt = '${partition.get}' ")
        } else spark.table(source.trim)

      case _ => spark.sql(source)
    }

    val filterIndex = Set()

    if (df.isEmpty && !filterIndex.exists(t => dest.contains(t))) {
      System.err.println("Result data is empty, check upstream output tasks.")

      import com.netease.wm.util.mail._

      System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")

      val day = Imports.DateTime.yesterday.toString("yyyy-MM-dd")

      send a Mail(
        from = ("symbiansigned@corp.netease.com", "symbiansigned"),
        to = "zhaozijun03@corp.netease.com" :: Nil,
        bcc = "hzxiaonaitong@corp.netease.com" :: Nil,
        subject = s"$day ES写入数据为空",
        message = s"$dest 索引，上游数据产出任务异常"
      )

      spark.stop()
      System.exit(1)
    }

    import org.elasticsearch.client.RestClient
    val credentialsProvider = new BasicCredentialsProvider()

    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))
    val clientBuilder = RestClient.builder(esHttpHosts: _*)
    clientBuilder.setHttpClientConfigCallback{ clientBuilder =>
      clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    }

    val client = new ElasticsearchClient(new RestClientTransport(clientBuilder.build(), new JacksonJsonpMapper()))

    val destIndex = partition match {
      case Some(p) if atom =>
        val partitionPostfix = p.replaceAll("-", "")
        s"""${partitionPrefix}${indexSep}$partitionPostfix"""
      case _ => dest
    }

    val destGlobs = s"""${partitionPrefix}${indexSep}*"""

    if (atom) {
      val actions: util.ArrayList[Action] = new util.ArrayList[Action]()
      df.repartition(parallel).saveToEs(s"$destIndex", writeEsConfig)
      val finalAliases = (aliases :+ dest).toSet
      finalAliases foreach { alias =>
        if (full) { // replace old partitions with the new partition through alias replacing
          actions.add(new Action.Builder().remove(new RemoveAction.Builder().index(destGlobs).alias(alias).build()).build())
        }
        actions.add(new Action.Builder().add(new AddAction.Builder().index(destIndex).alias(alias).build()).build())
      }

      val esRequest = UpdateAliasesRequest.of(_.actions(actions))

      try {
        val response = client.indices().updateAliases(esRequest)
        if (!response.acknowledged()) {
          System.exit(1)
        }
      } catch {
        case e: ElasticsearchStatusException =>
          if (RestStatus.NOT_FOUND.equals(e.status())) {
            // ignore index not found
          } else {
            throw e
          }
      }
    } else {
      df.repartition(parallel).saveToEs(s"$destIndex", writeEsConfig)
    }

    spark.stop()
  }
}