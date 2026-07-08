package com.netease.lofter.realtime.revenue

import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, clickhouseConfig, kafkaConfig}
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.RowKind
import org.apache.flink.util.Collector
import org.joda.time.DateTime

import java.sql.PreparedStatement
import java.util.Properties
import scala.collection.JavaConverters._

object UserRevenueTradeDetail {
  case class UserRevenueTrade(tradeId: Long, business_type: String, userId: Long, product_id: Long, product_num: Int,
                               blogId: Long,  postId: Long, giftId: Long, status: Int, money: Double,
                               createTime: Long, finishTime: Long, opTime: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val bsSettings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val tableEnv = StreamTableEnvironment.create(env, bsSettings)

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    // config for kafka data source
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "lofter_user_revenue_trade_detail")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val ndcSource: FlinkKafkaConsumer[SubscribeEvent] = new FlinkKafkaConsumer[SubscribeEvent]("lofter.binlog.ndc", new SubscribeEventSchema(), properties)

    val dataStream = env.addSource(
      ndcSource.setStartFromTimestamp(startTimeStamp).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[SubscribeEvent](Time.minutes(2)) {
        override def extractTimestamp(t: SubscribeEvent): Long = t.getTimestamp
      })
    )

    def statusNormalize(business: String, status: Int): Int = {
      business match {
        case "礼物付费" => if (status == 0) 1 else status
        case "书城会员" => if (status == 0) 2 else status
        case "博客订阅" | "粉丝会员" => if (status == 1) 1 else 2
        case "直播"  => 1
        case "打赏" => if(status == 10) 1 else 2
        case _ => status
      }
    }

    val giftTrade: DataStream[UserRevenueTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[UserRevenueTrade]) =>
      s.getRowChanges.asScala
        .filter(_.getType == SubscribeEvent.RowChangeType.INSERT)
        .foreach { row =>
          row.getTableName match {
            case "Trade_GiftPresentRecord" =>
              val tradeId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val business_type = "礼物付费"
              val userId = row.getColumn("sender").getNewValue.asInstanceOf[Long]
              val product_id = row.getColumn("postId").getNewValue.asInstanceOf[Long]
              val product_num = row.getColumn("count").getNewValue.asInstanceOf[Int]
              val blogId = row.getColumn("blogId").getNewValue.asInstanceOf[Long]
              val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
              val giftId = row.getColumn("giftId").getNewValue.asInstanceOf[Long]
              val status = statusNormalize(business_type, row.getColumn("status").getNewValue.asInstanceOf[Int])
              val money = row.getColumn("coin").getNewValue.asInstanceOf[Long]
              val createTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val finishTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val opTime = s.getTimestamp
              collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money.doubleValue() * 0.1, createTime, finishTime, opTime))
            case _ =>
          }
        }
    }

    val bookstoreTrade: DataStream[UserRevenueTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[UserRevenueTrade]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName match {
            case "Trade_StoreVipOrder" =>
              val tradeId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val business_type = "书城会员"
              val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
              val product_id = row.getColumn("productId").getNewValue.asInstanceOf[Long]
              val product_num = row.getColumn("vipDays").getNewValue.asInstanceOf[Int]
              val blogId = 0L
              val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
              val giftId = 0L
              val status = statusNormalize(business_type, row.getColumn("status").getNewValue.asInstanceOf[Int])
              val money = row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal]
              val finishTime = row.getColumn("finishTime").getNewValue.asInstanceOf[Long]
              val createTime = finishTime
              val opTime = s.getTimestamp
              collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money.doubleValue(), createTime, finishTime, opTime))
            case _ =>
          }
        }
    }

    val fansVipTrade: DataStream[UserRevenueTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[UserRevenueTrade]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName match {
            case "Trade_FansVipOrder" =>
              val tradeId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val business_type = "粉丝会员"
              val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
              val product_id = row.getColumn("vipBlogId").getNewValue.asInstanceOf[Long]
              val product_num = row.getColumn("vipDays").getNewValue.asInstanceOf[Int]
              val blogId = row.getColumn("vipBlogId").getNewValue.asInstanceOf[Long]
              val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
              val giftId = 0L
              val status = statusNormalize(business_type, row.getColumn("status").getNewValue.asInstanceOf[Int])
              val money = row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal]
              val createTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val finishTime = row.getColumn("finishTime").getNewValue.asInstanceOf[Long]
              val opTime = s.getTimestamp
              collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money.doubleValue(), createTime, finishTime, opTime))
            case _ =>
          }
        }
    }

    val pveTradeOrder: DataStream[UserRevenueTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[UserRevenueTrade]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName match {
            case "Trade_PVEStaminaOrder" =>
              val tradeId = row.getColumn("tradeId").getNewValue.asInstanceOf[Long]
              val business_type = "虚拟恋人"
              val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
              val product_id = row.getColumn("productId").getNewValue.asInstanceOf[Long]
              val product_num = 0
              val blogId = 0L
              val postId = 0L
              val giftId = 0L
              val status = statusNormalize(business_type, row.getColumn("status").getNewValue.asInstanceOf[Int])
              val money = row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal]
              val createTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val finishTime = row.getColumn("finishTime").getNewValue.asInstanceOf[Long]
              val opTime = s.getTimestamp
              collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money.doubleValue(), createTime, finishTime, opTime))

            case table@("Trade_Coupon_Order" | "Trade_CouponCardOrder" | "Trade_Coupon_Coin_Order") if row.getType == RowChangeType.INSERT || row.getType == RowChangeType.UPDATE =>
              val tradeId = row.getColumn("tradeId").getNewValue.asInstanceOf[Long]
              val business_type = "糖果券"
              val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
              val product_id = row.getColumn("productId").getNewValue.asInstanceOf[Long]
              val product_num = 1
              val blogId = 0L
              val postId = 0L
              val giftId = 0L
              val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
              val oldStatus = Option(row.getColumn("status").getOldValue).map(_.asInstanceOf[Int]).getOrElse(status)
              val money = if(table == "Trade_Coupon_Coin_Order") {
                row.getColumn("coin").getNewValue.asInstanceOf[Long] * 0.1
              } else {
                row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal].doubleValue()
              }
              val createTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val finishTime = if(table == "Trade_Coupon_Coin_Order") {
                createTime
              } else row.getColumn("createTime").getNewValue.asInstanceOf[Long]

              val opTime = s.getTimestamp

              val isSuccess = if(row.getType == SubscribeEvent.RowChangeType.INSERT) status == 1 else
                status == 1 && oldStatus != 1

              if(isSuccess) {
                collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money, createTime, finishTime, opTime))
              }

            case _ =>
          }
        }
    }

    val c2cPayOrder: DataStream[UserRevenueTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[UserRevenueTrade]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName match {
            case "C2C_PayOrder" =>
              val tradeId = row.getColumn("tradeId").getNewValue.asInstanceOf[Long]
              val business_type = "小黄车"
              val userId = row.getColumn("buyerId").getNewValue.asInstanceOf[Long]
              val product_id = 0L
              val product_num = 0
              val blogId = 0L
              val postId = 0L
              val giftId = 0L
              val status = statusNormalize(business_type, row.getColumn("status").getNewValue.asInstanceOf[Int])
              val money = row.getColumn("payAmount").getNewValue.asInstanceOf[Long] * 0.01
              val createTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val finishTime = row.getColumn("payTime").getNewValue.asInstanceOf[Long]
              val opTime = s.getTimestamp
              collector.collect(UserRevenueTrade(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money.doubleValue(), createTime, finishTime, opTime))
            case _ =>
          }
        }
    }

    tableEnv.createTemporaryView("GiftOrder", giftTrade)
    tableEnv.createTemporaryView("bookstoreOrder", bookstoreTrade)
    tableEnv.createTemporaryView("fansVipOrder", fansVipTrade)
    tableEnv.createTemporaryView("pveTradeOrder", pveTradeOrder)
    tableEnv.createTemporaryView("c2cPayOrder", c2cPayOrder)

    val sql_query =
      s"""
         |select * from GiftOrder
         |union all
         |select * from bookstoreOrder
         |union all
         |select * from fansVipOrder
         |union all
         |select * from pveTradeOrder
         |union all
         |select * from c2cPayOrder
         |""".stripMargin.replaceAll(raw"[\n\r\s]+", " ")

    val chSink = JdbcSink.sink(
      "insert into dwd_user_revenue_trade_local(tradeId, business_type, userId, product_id, product_num, blogId, postId, giftId, status, money, createTime, finishTime, opTime) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
      new JdbcStatementBuilder[UserRevenueTrade] {
        override def accept(ps: PreparedStatement, e: UserRevenueTrade): Unit = {
          import e._
          ps.setLong(1, tradeId)
          ps.setString(2, business_type)
          ps.setLong(3, userId)
          ps.setLong(4, product_id)
          ps.setInt(5, product_num)
          ps.setLong(6, blogId)
          ps.setLong(7, postId)
          ps.setLong(8, giftId)
          ps.setInt(9, status)
          ps.setDouble(10, money)
          ps.setLong(11, createTime)
          ps.setLong(12, finishTime)
          ps.setLong(13, opTime)
        }
      },
      JdbcExecutionOptions.builder().withBatchSize(100).withBatchIntervalMs(5000).build(),
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withDriverName(clickhouseConfig.clickHouseDriver)
        .withUrl("jdbc:clickhouse://lofter-data-common6.gy.ntes:9000/lofter?socket_timeout=1000000")
        .withUsername(clickhouseConfig.phyClickHouseUser)
        .withPassword(clickhouseConfig.phyClickHousePassword)
        .build()
    )

    tableEnv.sqlQuery(sql_query)
      .toChangelogStream
      .filter(row => row.getKind == RowKind.INSERT || row.getKind == RowKind.UPDATE_AFTER)
      .map(x =>
        UserRevenueTrade(x.getFieldAs("tradeId").asInstanceOf[Long], x.getFieldAs("business_type").asInstanceOf[String], x.getFieldAs("userId").asInstanceOf[Long],
          x.getFieldAs("product_id").asInstanceOf[Long], x.getFieldAs("product_num").asInstanceOf[Int], x.getFieldAs("blogId").asInstanceOf[Long],
          x.getFieldAs("postId").asInstanceOf[Long], x.getFieldAs("giftId").asInstanceOf[Long], x.getFieldAs("status").asInstanceOf[Int], x.getFieldAs("money").asInstanceOf[Double],
          x.getFieldAs("createTime").asInstanceOf[Long], x.getFieldAs("finishTime").asInstanceOf[Long], x.getFieldAs("opTime").asInstanceOf[Long])
      )
      .addSink(chSink).uid("user_revenue_trade")

    env.execute("lofter user revenue trade detail job")

  }
}
