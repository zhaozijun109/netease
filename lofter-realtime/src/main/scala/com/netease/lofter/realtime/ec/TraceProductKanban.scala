package com.netease.lofter.realtime.ec

import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.TraceProductAggregate
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime

import java.sql.PreparedStatement

object TraceProductKanban {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().withHourOfDay(18).withMinuteOfHour(50).getMillis

    val eventSumSource = KafkaSource.builder[TraceProductAggregate]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("trace.product.aggregate_v2")
      .setGroupId("trace_product_event_kanban_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[TraceProductAggregate])
      .build()

    val eventSum = env.fromSource(eventSumSource, WatermarkStrategy.noWatermarks(), "trace-product-sum")

    val clickhouseSink = JdbcSink.sink(
      "insert into trace_product_local_v2(userId, action, scene, dt, dh, productId, productType, new_flag, order_amount, order_new, order_new_amount) values(?,?,?,?, ?,?,?,?, ?,?,?)",
      new JdbcStatementBuilder[TraceProductAggregate] {
        override def accept(statement: PreparedStatement, value: TraceProductAggregate): Unit = {
          statement.setLong(1, value.userId)
          statement.setInt(2, value.action)
          statement.setString(3, value.scene)
          statement.setLong(4, value.dt)
          statement.setLong(5, value.dh)
          statement.setLong(6, value.productId)
          statement.setInt(7, value.productType)
          statement.setLong(8, value.new_flag)
          statement.setDouble(9, value.order_amount)
          statement.setInt(10, value.order_new)
          statement.setDouble(11, value.order_new_amount)
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

    eventSum.addSink(clickhouseSink)

    env.execute("trace product event kanban v2")
  }
}
