package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.{BinlogRow, FileSinkHelper, JsonHelper, LocalTimestampJsonCdcSchema}
import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.joda.time.DateTime

import java.util.Properties
import scala.collection.JavaConverters._

object CdcToHdfsLoader {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if(args.length < 2) {
      println("usage: ./CdcToHdfsLoader tables output, but got " + args.mkString(" "))
      System.exit(-1)
    }

    val tables = args(0)
    val outputPath = args(1)

    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val startTimestamp = DateTime.now().withTimeAtStartOfDay().getMillis

    val cdcSource = buildCDCSource(serverId = "5701-5800", tableList = tables.split(",").filter(_.nonEmpty).toSeq)

    val cdcStream = env.fromSource(cdcSource, WatermarkStrategy.noWatermarks(), "mysql-cdc-source")

    val sink = FileSinkHelper.createPartitionBinlogFileSink(outputPath)

    cdcStream.flatMap { (event: String,  collector: Collector[BinlogRow]) =>
      parseLogEvents(event).foreach{ e =>
        collector.collect(e)
      }
    }.sinkTo(sink)

    env.execute(s"CdcToHdfsLoader for tables: $tables")
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
