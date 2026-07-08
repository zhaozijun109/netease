package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common._
import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.json4s.DefaultFormats

import java.sql.PreparedStatement
import java.util.Properties
import scala.collection.JavaConverters._


object CdcToClickHouse {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 1) {
      println("usage: ./CdcToClickHouse mysql-tables, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val tables = args(0)

    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "cdc_to_ck")
    properties.setProperty("auto.offset.reset", "latest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")
    properties.setProperty("max.request.size", "20000000")

    val startTimestamp = DateTime.now().withTimeAtStartOfDay().getMillis

    val cdcSource = buildCDCSource(serverId = "5801-5900", tableList = tables.split(",").filter(_.nonEmpty).toSeq)

    val cdcStream = env.fromSource(cdcSource, WatermarkStrategy.noWatermarks(), "mysql-cdc-source")

    val chSink = JdbcSink.sink(
      "insert into vc.db_binlog_local(tableName,op,opTime,seqno,partitionId,data,old) values(?,?,?,?,?,?,?)",
      new JdbcStatementBuilder[BinlogRow] {
        override def accept(ps: PreparedStatement, e: BinlogRow): Unit = {
          import e._
          import org.json4s.jackson.Serialization.write
          implicit val format = DefaultFormats

          val data = if(e.data == null) Map.empty[String, Any] else e.data
          val old = if(e.old == null) Map.empty[String, Any] else e.old

          ps.setString(1, table)
          ps.setInt(2, op)
          ps.setLong(3, opTime)
          ps.setLong(4, seqno)
          ps.setInt(5, partitionId)
          ps.setString(6, write(data))
          ps.setString(7, write(old))
        }
      },
      JdbcExecutionOptions.builder().withBatchSize(10000).withBatchIntervalMs(5000).withMaxRetries(5).build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withDriverName(ClickHouseConfig.clickHouseDriver)
        .withUrl("jdbc:clickhouse://lofter-data-common8.gy.ntes:9000/lofter?socket_timeout=1000000")
        .withUsername(ClickHouseConfig.phyClickHouseUser)
        .withPassword(ClickHouseConfig.phyClickHousePassword)
        .build()
    )

    cdcStream.flatMap { (event: String,  collector: Collector[BinlogRow]) =>
      parseLogEvents(event).foreach{ e =>
        collector.collect(e)
      }
    }.addSink(chSink)

    env.execute(s"CdcToKafka for tables: $tables")
  }

  private val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
    mapper
  }

  def parseLogEvents(v: String): Seq[BinlogRow] = {
    val root = objectMapper.readTree(v)
    val before = JsonHelper.parseMap(objectMapper, root.get("before"))
    val after = JsonHelper.parseMap(objectMapper, root.get("after"))
    val source = root.get("source")
    val op = root.get("op").asText() match {
      case "c" | "r" => 0
      case "d" => 1
      case "u" => 2
      case _ => -1
    }

    val opTime = root.get("ts_ms").asLong()
    val seqno: Long = 0L
    val partitionId: Int = 0
    val tableName = source.get("table").asText()

    val columns: Map[String, Any] = (if(op == 1) before.asScala else after.asScala).toMap

    val old: Map[String, Any] = if( op == 2 ) {
      before.asScala
        .filter{ e => e._2 != after.get(e._1)}
        .toMap
    } else Map.empty

    Seq(BinlogRow(tableName, op, opTime, seqno, partitionId, columns, old))
  }

  class MysqlCDCConfig() {
    val host = "vcharacter-mysql-rw-online.db.gy.ntes"
    val port = 4331
    val db = "vcharacter"
    val userName = "online_vc_data_factory"
    val password = "9F@5#UfIk"
    val jdbcUrl = "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true"
    val fetchSize = 1024
  }

  def buildCDCSource(tableList: Seq[String], serverId: String = "5400", startTimestamp: Long = 0L): MySqlSource[String] = {
    val conf = new MysqlCDCConfig()
    import conf._
    val debeziumProperties = new Properties()
    debeziumProperties.setProperty("decimal.handling.mode", "double")

    val cdcSource = MySqlSource.builder[String]()
      .hostname(host)
      .port(port)
      .databaseList(db)
      .scanNewlyAddedTableEnabled(true)
      .tableList(tableList.map(x => db + s".$x"): _*)
      .username(userName)
      .password(password)
      // 默认分配为5400-6400之间, 但建议手动分配
      .serverId(serverId)
      .deserializer(new LocalTimestampJsonCdcSchema)
      .debeziumProperties(debeziumProperties)
      .serverTimeZone("Asia/Shanghai")
      .fetchSize(fetchSize)
    // 如果设置了起始时间戳, 从指定时间戳开始消费
    if (startTimestamp > 0) {
      cdcSource.startupOptions(StartupOptions.timestamp(startTimestamp))
    } else {
      cdcSource.startupOptions(StartupOptions.earliest())
    }

    cdcSource.build()
  }

}
