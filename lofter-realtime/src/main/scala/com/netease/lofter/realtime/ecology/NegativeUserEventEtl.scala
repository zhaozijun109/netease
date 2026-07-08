package com.netease.lofter.realtime.ecology

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.avro.NegativeUserEvent
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy

import java.sql.PreparedStatement
import java.time.Duration
import scala.collection.JavaConverters._

object NegativeUserEventEtl {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val latenessInSeconds = 60

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("negative_user_event_etl_binlog")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

//    val dwdResultSink = KafkaSink.builder[NegativeUserEvent]()
//      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
//      .setRecordSerializer(
//        KafkaRecordSerializationSchema.builder()
//          .setTopic("negative.user.event")
//          .setValueSerializationSchema(new AvroBinarySerSchema[NegativeUserEvent])
//          .build()
//      ).build()

    val clickhouseSink = JdbcSink.sink(
      "insert into negative_user_event_local(userId, action, time, itemId) values(?,?,?,?)",
      new JdbcStatementBuilder[NegativeUserEvent] {
        override def accept(statement: PreparedStatement, value: NegativeUserEvent): Unit = {
          statement.setLong(1, value.userId)
          statement.setInt(2, value.action)
          statement.setLong(3, value.time)
          statement.setLong(4, value.itemId.getOrElse(0L))
        }
      },
      JdbcExecutionOptions.builder()
        .withBatchSize(100)
        .withBatchIntervalMs(1000)
        .withMaxRetries(5)
        .build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withUrl("jdbc:clickhouse://lofter-clickhouse1.jd.163.org:9000/lofter?socket_timeout=1000000")
        .withDriverName("com.github.housepower.jdbc.ClickHouseDriver")
        .withUsername("lofter_rw")
        .withPassword("O4nWNA9slAn8")
        .build()
    )

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val negativeUserEvents = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[NegativeUserEvent]) =>
        val time = s.getTimestamp

        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName.toLowerCase match {
              case "post" =>
                //val productId = row.getColumn("productid").getNewValue.asInstanceOf[Long]
                val userId = row.getColumn("PublisherUserId").getNewValue.asInstanceOf[Long]
                val postId = row.getColumn("ID").getNewValue.asInstanceOf[Long]
                val valid = row.getColumn("Valid").getNewValue.asInstanceOf[Int]
                val oldValid = row.getColumn("Valid").getOldValue.asInstanceOf[Int]
                val allowView = row.getColumn("AllowView").getNewValue.asInstanceOf[Int]
                val oldAllowView = row.getColumn("AllowView").getOldValue.asInstanceOf[Int]


                val action =  if(valid == 32 && oldValid == 0) {
                  1
                } else if(allowView == 100 && oldAllowView == 0) {
                  2
                }  else if(valid == 25 && oldValid == 0) {
                  3
                } else 0

                if(action > 0) {
                  collector.collect(NegativeUserEvent(userId, time, action, Some(postId)))
                }

              case "user_closeaccountlog" if changeType == RowChangeType.INSERT || changeType == RowChangeType.UPDATE =>
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
                // apply for cancellation
                status match {
                  case 0 if changeType == RowChangeType.INSERT =>
                    collector.collect(NegativeUserEvent(userId, time, action = 4, itemId = None))
                  case 1 if oldStatus != 1 =>
                    collector.collect(NegativeUserEvent(userId, time, action = 5, itemId = None))
                  case _ =>
                }

              case _ =>
            }
          }
      }

    negativeUserEvents.addSink(clickhouseSink)

    env.execute("negative user event etl")
  }
}
