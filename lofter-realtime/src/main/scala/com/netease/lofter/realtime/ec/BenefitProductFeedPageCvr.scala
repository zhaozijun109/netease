package com.netease.lofter.realtime.ec

import com.netease.lofter.realtime.common.{dbConfig, kafkaConfig}
import com.netease.wm.hubble.avro.Mda
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.RowKind
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.sql.PreparedStatement
import java.time.Duration

private class BenefitProductFeedPageCvr{}

object BenefitProductFeedPageCvr {

  case class ProductEvent(productId: Long, statTime: Long, eventId: String)
  case class ProductCvr(productId: Long, statTime: Long, exposePv: Long, clickPv: Long)

  val logger: Logger = LoggerFactory.getLogger(classOf[BenefitProductFeedPageCvr])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().withHourOfDay(12).getMillis
    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("lofter_benefit_feed_page_cvr_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val jdbcSink = JdbcSink.sink(
      "insert into Benefit_FeedRollingData(id, productId, statTime, exposeCount, clickCount, createTime) values(seq, ?, ?, ?, ?, ?) on duplicate key update exposeCount = ?, clickCount = ?",
      new JdbcStatementBuilder[ProductCvr] {
        override def accept(statement: PreparedStatement, productCvr: ProductCvr): Unit = {
          statement.setLong(1, productCvr.productId)
          statement.setLong(2, productCvr.statTime)
          statement.setLong(3, productCvr.exposePv)
          statement.setLong(4, productCvr.clickPv)
          statement.setLong(5, System.currentTimeMillis())
          statement.setLong(6, productCvr.exposePv)
          statement.setLong(7, productCvr.clickPv)
        }
      },
      JdbcExecutionOptions.builder()
        .withBatchSize(100)
        .withBatchIntervalMs(1000)
        .withMaxRetries(5)
        .build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withUrl(dbConfig.LOFTER_MALL_DB_URL)
        .withDriverName("com.netease.lbd.LBDriver")
        .build()
    )

    val productEvents: DataStream[ProductEvent] = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .rebalance
      .flatMap { e =>
        e.eventId match {
          case "w1-18" | "w1-19" if e.itemId.exists(_ > 0) =>
            val statTime = new DateTime(e.kafkaTime).toString("yyyyMMddHH0000").toLong
            Some(ProductEvent(e.itemId.get, statTime, e.eventId))
          case _ => None
        }
      }

    val hourSql =
      """
        |select productId,statTime,
        |    count(1) filter (where eventId in('w1-18')) as exposeCount,
        |    count(1) filter (where eventId in('w1-19')) as clickPv
        |from benefitProductFeed
        |group by productId,statTime
      """.stripMargin

    tableEnv.createTemporaryView("benefitProductFeed",
      productEvents,
      Schema.newBuilder()
        .column("productId", DataTypes.BIGINT())
        .column("statTime", DataTypes.BIGINT())
        .column("eventId", DataTypes.STRING())
        .build()
    )

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "10 s")
    configuration.setString("table.exec.mini-batch.size", "10000")
    configuration.setBoolean("table.exec.emit.early-fire.enabled", true)
    configuration.setString("table.exec.emit.early-fire.delay", "60000 ms")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofHours(25))

    tableEnv.sqlQuery(hourSql)
      .toChangelogStream
      .filter(row => row.getKind == RowKind.INSERT || row.getKind == RowKind.UPDATE_AFTER)
      .map{ x =>
        ProductCvr(x.getFieldAs("productId").asInstanceOf[Long], x.getFieldAs("statTime").asInstanceOf[Long], x.getFieldAs("exposeCount").asInstanceOf[Long], x.getFieldAs("clickPv").asInstanceOf[Long])
      }.addSink(jdbcSink)

    env.execute(s"lofter benefit product feed cvr")
  }
}
