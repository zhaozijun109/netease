package com.netease.wm.hubble

import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.netease.wm.hubble.common.{BinlogRow, FileSinkHelper, JsonHelper, LocalTimestampJsonCdcSchema}
import com.netease.wm.util.Args
import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.collection.JavaConverters._

object CdcToHdfsAndKafkaLoader {
  case class NewBinlogRow(table:String,op:Int,opTime:Long,seqno:Long,partitionId:Int,data:Map[String,Any],old:Map[String,Any],_tbl:String,_bin_op:Int,_bin_op_time:Long,_bin_op_seqno:Long,_bin_old:Map[String,Any])

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val t_hdfs = pargs.optional("tohdfs")
    val t_kafka = pargs.optional("tokafka")
    val t_both = pargs.optional("both")
    val hdfs_path = pargs.optional("hdfs_path").getOrElse("/user/virtual_character/binlog")
    val kafka_servers = pargs.optional("kafka_servers").getOrElse("lofter-kafka-bi-risk1.gy.ntes:9092,lofter-kafka-bi-risk2.gy.ntes:9092,lofter-kafka-bi-risk3.gy.ntes:9092,lofter-kafka-bi-risk4.gy.ntes:9092,lofter-kafka-bi-risk5.gy.ntes:9092")
    val topic = pargs.optional("topic").getOrElse("vc.binlog.online")

    val h_tables = if (t_hdfs.isDefined) {t_hdfs.get.split(",")} else {Array[String]()}
    val k_tables = if (t_kafka.isDefined) {t_kafka.get.split(",")} else {Array[String]()}
    val b_tables = if (t_both.isDefined) {t_both.get.split(",")} else {Array[String]()}

    val all_tables = (h_tables ++ k_tables ++ b_tables).toSet.toSeq
    val hdfs_tables = (h_tables ++ b_tables).toSet.toSeq
    val kafka_tables = (k_tables ++ b_tables).toSet.toSeq

    val startTimestamp = DateTime.now().withTimeAtStartOfDay().getMillis

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(10 * 60 * 1000L)
    env.getCheckpointConfig.setCheckpointTimeout(15 * 60 * 1000L)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5 * 60 * 1000L)
    env.getConfig.enableObjectReuse()

    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    val cdcSource = buildCDCSource(serverId = "5901-6000", tableList = all_tables)

    val cdcStream = env.fromSource(cdcSource, WatermarkStrategy.noWatermarks(), "mysql-cdc-source")
    val outPutDS = cdcStream.flatMap { (event: String,  collector: Collector[BinlogRow]) =>
      parseLogEvents(event).foreach{ e =>
        collector.collect(e)
      }
    }

    val hdfs_tag = OutputTag[BinlogRow]("hdfs")
    val kafka_tag = OutputTag[BinlogRow]("kafka")
    val both_tag = OutputTag[BinlogRow]("both")

    val processedStream = outPutDS.process(
      new ProcessFunction[BinlogRow, BinlogRow] {
        override def processElement(binlogRow: BinlogRow,
                                    ctx: ProcessFunction[BinlogRow, BinlogRow]#Context,
                                    out: Collector[BinlogRow]
                                   ): Unit = {
          binlogRow.table match {
            case table if hdfs_tables.contains(table) && kafka_tables.contains(table) => ctx.output(both_tag,binlogRow)
            case table if hdfs_tables.contains(table) && !kafka_tables.contains(table) => ctx.output(hdfs_tag, binlogRow)
            case table if kafka_tables.contains(table) && !hdfs_tables.contains(table) => ctx.output(kafka_tag, binlogRow)
          }
        }
      }
    )

    val hdfs_stream = processedStream.getSideOutput(hdfs_tag).union(processedStream.getSideOutput(both_tag))
    val kafka_stream = processedStream.getSideOutput(kafka_tag).union(processedStream.getSideOutput(both_tag))

    val hdfsSink = FileSinkHelper.createPartitionBinlogFileSink(hdfs_path)
    hdfs_stream.sinkTo(hdfsSink).name("hdfs-sink")
//    hdfs_stream.print("hdfs输出：")

    val kafkaSink = KafkaSink.builder[String]()
      .setBootstrapServers(kafka_servers)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(topic)
          .setValueSerializationSchema(new SimpleStringSchema())
          .build()
      ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    kafka_stream.map(row => {
      val newbinlogrow = NewBinlogRow(row.table, row.op, row.opTime, row.seqno, row.partitionId, row.data, row.old, row.table, row.op, row.opTime, row.seqno, row.old)
      toJsonObject(newbinlogrow).toJSONString
    }).sinkTo(kafkaSink).name("kafka-sink")
//    kafka_stream.print("kafka输出：")

    env.execute(s"CdcToHdfsLoader for tables: $all_tables")
  }

  def toJsonObject(row: NewBinlogRow): JSONObject = {
    val json = new JSONObject()

    // 添加所有字段
    json.put("table", row.table)
    json.put("op", row.op)
    json.put("opTime", row.opTime)
    json.put("seqno", row.seqno)
    json.put("partitionId", row.partitionId)
    json.put("data", convertMap(row.data))
    json.put("old", convertMap(row.old))
    json.put("_tbl", row._tbl)
    json.put("_bin_op", row._bin_op)
    json.put("_bin_op_time", row._bin_op_time)
    json.put("_bin_op_seqno", row._bin_op_seqno)
    json.put("_bin_old", convertMap(row._bin_old))

    json
  }

  def convertMap(map: Map[String, Any]): java.util.Map[String, Any] = {
    map.map { case (k, v) =>
      k -> (v match {
        case m: Map[String, Any] => convertMap(m)
        case l: List[_] => l.asJava
        case other => other
      })
    }.asJava
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

  def buildCDCSource(tableList: Seq[String], serverId: String = "5902", startTimestamp: Long = 0L): MySqlSource[String] = {
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
      cdcSource.startupOptions(StartupOptions.latest())
    }

    cdcSource.build()
  }

}
