package com.netease.lofter.realtime.ec

import com.fasterxml.jackson.core.JsonParser.Feature
import com.netease.dts.common.subscribe.SubscribeEvent
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
import org.json4s.jackson.JsonMethods._
import org.json4s._

import java.sql.PreparedStatement
import java.util.Properties
import scala.collection.JavaConverters._
import scala.util.control.NonFatal

object CardConsumptionV2Stat {
  case class BenefitTrade(tradeId: Long, activityCode: String, userId: Long, productType: Int, status: Int,
                          orderFrom: String, payMethod: String, money: Double, newCouponPreferential: Double,
                          bountyPreferential: Double, tradeTime: Long, payTime: Long, opTime: Long)

  case class BenefitOrder(tradeId: Long, orderId: Long, orderTime: Long, supplierId: Long)

  case class OrderProduct(productUniqueId:Long, orderId: Long, productId: Long, productName: String, productNum: Int, productStatus: Int, productGroupId: Long,
                          storePrice: Double, marketPrice: Double, slotNum: Int, productNewCouponPreferential: Double, productTime: Long, productOpTime: Long)

  case class BenefitUserCardBag(cardId: Long, cardStatus: Int, chanceTradeId: Long, cardProductId: Long, cardSkuId: Long, cardTime: Long, cardOpTime: Long)

  case class BenefitOrderInfo(tradeId: Long, activityCode: String, userId: Long, productType: Int, status: Int,
                          orderFrom: String, payMethod: String, money: Double, newCouponPreferential: Double,
                          bountyPreferential: Double, tradeTime: Long, payTime: Long, opTime: Long, orderTime: Long, supplierId: Long,
                          productUniqueId: Long, orderId: Long, productId: Long, productName: String, productNum: Int, productStatus: Int, productGroupId: Long,
                          storePrice: Double, marketPrice: Double, slotNum: Int, productNewCouponPreferential: Double, productTime: Long, productOpTime: Long,
                          cardId: Long, cardStatus: Int, cardProductId: Long, cardSkuId: Long, cardTime: Long, cardOpTime: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val bsSettings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val tableEnv = StreamTableEnvironment.create(env, bsSettings)

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // set this to eventTime, then waterMarkInterval is 200ms
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

    // config for kafka data source
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.GY_BOOTSTRAP_SERVERS)
    properties.setProperty("group.id", "lofter_card_consumption_v2")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis
    val ndcSource: FlinkKafkaConsumer[SubscribeEvent] = new FlinkKafkaConsumer[SubscribeEvent]("lofter.binlog.ndc", new SubscribeEventSchema(), properties)

    val dataStream = env.addSource(
      ndcSource.setStartFromTimestamp(startTimeStamp).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[SubscribeEvent](Time.minutes(2)) {
        override def extractTimestamp(t: SubscribeEvent): Long = t.getTimestamp
      })
    )
    val benefitTrade: DataStream[BenefitTrade] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[BenefitTrade]) =>
      s.getRowChanges.asScala
        .filter(_.getType == SubscribeEvent.RowChangeType.UPDATE)
        .foreach { row =>
          row.getTableName.toLowerCase match {
            case "benefit_trade" =>
              val tradeId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val activityCode = Option(row.getColumn("thirdpartyNum").getNewValue.asInstanceOf[String]).getOrElse("")
              val userId = row.getColumn("buyerId").getNewValue.asInstanceOf[String].toLong
              val productType = row.getColumn("productType").getNewValue.asInstanceOf[Int]
              val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
              val orderFrom = Option(row.getColumn("orderFrom").getNewValue.asInstanceOf[String]).getOrElse("")
              val payMethod = Option(row.getColumn("payMethod").getNewValue.asInstanceOf[String]).getOrElse("")
              val money = row.getColumn("amount").getNewValue.asInstanceOf[java.math.BigDecimal]
              val newCouponPreferential = row.getColumn("newCouponPreferential").getNewValue.asInstanceOf[java.math.BigDecimal]
              val bountyPreferential = row.getColumn("bountyPreferential").getNewValue.asInstanceOf[java.math.BigDecimal]
              val tradeTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val payTime = row.getColumn("payTime").getNewValue.asInstanceOf[Long]
              val opTime = s.getTimestamp
              collector.collect(BenefitTrade(tradeId, activityCode, userId, productType, status, orderFrom, payMethod, money.doubleValue(), newCouponPreferential.doubleValue(), bountyPreferential.doubleValue(), tradeTime, payTime, opTime))
            case _ =>
          }
        }
    }

    val benefitOrder: DataStream[BenefitOrder] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[BenefitOrder]) =>
      s.getRowChanges.asScala
        .filter(_.getType == SubscribeEvent.RowChangeType.INSERT)
        .foreach { row =>
          row.getTableName.toLowerCase match {
            case "benefit_order" =>
              val orderId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val tradeId = row.getColumn("tradeId").getNewValue.asInstanceOf[Long]
              val orderTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val supplierId = row.getColumn("supplierId").getNewValue.asInstanceOf[Long]
              collector.collect(BenefitOrder(tradeId, orderId, orderTime, supplierId))
            case _ =>
          }
        }
    }

    val orderProduct: DataStream[OrderProduct] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[OrderProduct]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName.toLowerCase match {
            case "benefit_order_product" =>
              implicit val formats = DefaultFormats
              mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
              val orderId = row.getColumn("orderId").getNewValue.asInstanceOf[Long]
              val productUniqueId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val productTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val productId = row.getColumn("productId").getNewValue.asInstanceOf[Long]
              val productName = Option(row.getColumn("productName").getNewValue.asInstanceOf[String]).getOrElse("")
              val productNum = row.getColumn("productNum").getNewValue.asInstanceOf[Int]
              val productStatus = row.getColumn("status").getNewValue.asInstanceOf[Int]
              val storePrice = row.getColumn("storePrice").getNewValue.asInstanceOf[java.math.BigDecimal]
              val marketPrice = row.getColumn("marketPrice").getNewValue.asInstanceOf[java.math.BigDecimal]
              val productNewCouponPreferential = row.getColumn("newCouponPreferential").getNewValue.asInstanceOf[java.math.BigDecimal]
              val productGroupId = row.getColumn("attrGroupId").getNewValue.asInstanceOf[Long]
              val productOpTime = s.getTimestamp
              val attrGroupExt = Option(row.getColumn("attrGroupExt").getNewValue.asInstanceOf[String]).getOrElse("")
              try {
                val ext = (parse(attrGroupExt) \ "ext").extractOrElse("{\"slotNum\":\"0\"}")
                val slotNum = (parse(ext) \ "slotNum").extractOrElse("0").toInt
                collector.collect(OrderProduct(productUniqueId, orderId, productId, productName, productNum, productStatus, productGroupId, storePrice.doubleValue(), marketPrice.doubleValue(), slotNum, productNewCouponPreferential.doubleValue(),productTime,productOpTime))
              } catch {
                case NonFatal(_) =>
                  println(s"error parsing benefit_order_product log event: $attrGroupExt")
                  None
              }

            case _ =>
          }
        }
    }

    val benefitUserCardBag: DataStream[BenefitUserCardBag] = dataStream.flatMap { (s: SubscribeEvent, collector: Collector[BenefitUserCardBag]) =>
      s.getRowChanges.asScala
        .foreach { row =>
          row.getTableName match {
            case "Benefit_UserCardBag" =>
              val chanceTradeId = row.getColumn("chanceTradeId").getNewValue.asInstanceOf[Long]
              val cardProductId = row.getColumn("productId").getNewValue.asInstanceOf[Long]
              val cardSkuId = row.getColumn("attributeId").getNewValue.asInstanceOf[Long]
              val cardTime = row.getColumn("createTime").getNewValue.asInstanceOf[Long]
              val cardStatus = row.getColumn("status").getNewValue.asInstanceOf[Int]
              val cardId = row.getColumn("id").getNewValue.asInstanceOf[Long]
              val cardOpTime = s.getTimestamp
              collector.collect(BenefitUserCardBag(cardId, cardStatus, chanceTradeId, cardProductId, cardSkuId, cardTime, cardOpTime))
            case _ =>
          }
        }
    }

    tableEnv.createTemporaryView("BenefitTrade", benefitTrade)
    tableEnv.createTemporaryView("BenefitOrder", benefitOrder)
    tableEnv.createTemporaryView("OrderProduct", orderProduct)
    tableEnv.createTemporaryView("CardBag", benefitUserCardBag)

    val sql_query =
      s"""
         |select a.*, b.orderTime, b.supplierId, c.*,
         |      COALESCE(d.cardId,0) as cardId, COALESCE(d.cardStatus,-10) as cardStatus, COALESCE(d.cardProductId,0) as cardProductId,
         |      COALESCE(d.cardSkuId,0) as cardSkuId, COALESCE(d.cardTime,0) as cardTime, COALESCE(d.cardOpTime,0) as cardOpTime
         |from BenefitTrade a
         |INNER JOIN BenefitOrder b
         |ON a.tradeId = b.tradeId
         |INNER JOIN OrderProduct c
         |on b.orderId = c.orderId
         |LEFT JOIN CardBag d
         |on a.tradeId = d.chanceTradeId
         |""".stripMargin.replaceAll(raw"[\n\r\s]+", " ")

    val chSink = JdbcSink.sink(
      "insert into dwd_benefit_order_card_info_local(tradeId, activityCode, userId, productType, status, orderFrom, payMethod, money, newCouponPreferential, bountyPreferential, tradeTime, payTime, opTime, orderTime, supplierId, productUniqueId, orderId, productId, productName, productNum, productStatus, productGroupId, storePrice, marketPrice, slotNum, productNewCouponPreferential, productTime, productOpTime, cardId, cardStatus, cardProductId, cardSkuId, cardTime, cardOpTime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      new JdbcStatementBuilder[BenefitOrderInfo] {
        override def accept(ps: PreparedStatement, e: BenefitOrderInfo): Unit = {
          import e._
          ps.setLong(1, tradeId)
          ps.setString(2, activityCode)
          ps.setLong(3, userId)
          ps.setInt(4, productType)
          ps.setInt(5, status)
          ps.setString(6, orderFrom)
          ps.setString(7, payMethod)
          ps.setDouble(8, money)
          ps.setDouble(9, newCouponPreferential)
          ps.setDouble(10, bountyPreferential)
          ps.setLong(11, tradeTime)
          ps.setLong(12, payTime)
          ps.setLong(13, opTime)
          ps.setLong(14, orderTime)
          ps.setLong(15, supplierId)
          ps.setLong(16, productUniqueId)
          ps.setLong(17, orderId)
          ps.setLong(18, productId)
          ps.setString(19, productName)
          ps.setInt(20, productNum)
          ps.setInt(21, productStatus)
          ps.setLong(22, productGroupId)
          ps.setDouble(23, storePrice)
          ps.setDouble(24, marketPrice)
          ps.setInt(25, slotNum)
          ps.setDouble(26, productNewCouponPreferential)
          ps.setLong(27, productTime)
          ps.setLong(28, productOpTime)
          ps.setLong(29, cardId)
          ps.setInt(30, cardStatus)
          ps.setLong(31, cardProductId)
          ps.setLong(32, cardSkuId)
          ps.setLong(33, cardTime)
          ps.setLong(34, cardOpTime)
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
      .map{ x =>
        BenefitOrderInfo(x.getFieldAs("tradeId").asInstanceOf[Long],x.getFieldAs("activityCode").asInstanceOf[String], x.getFieldAs("userId").asInstanceOf[Long],
          x.getFieldAs("productType").asInstanceOf[Int], x.getFieldAs("status").asInstanceOf[Int], x.getFieldAs("orderFrom").asInstanceOf[String],
          x.getFieldAs("payMethod").asInstanceOf[String], x.getFieldAs("money").asInstanceOf[Double], x.getFieldAs("newCouponPreferential").asInstanceOf[Double],
          x.getFieldAs("bountyPreferential").asInstanceOf[Double], x.getFieldAs("tradeTime").asInstanceOf[Long], x.getFieldAs("payTime").asInstanceOf[Long], x.getFieldAs("opTime").asInstanceOf[Long],
          x.getFieldAs("orderTime").asInstanceOf[Long], x.getFieldAs("supplierId").asInstanceOf[Long], x.getFieldAs("productUniqueId").asInstanceOf[Long], x.getFieldAs("orderId").asInstanceOf[Long],
          x.getFieldAs("productId").asInstanceOf[Long], x.getFieldAs("productName").asInstanceOf[String], x.getFieldAs("productNum").asInstanceOf[Int],
          x.getFieldAs("productStatus").asInstanceOf[Int], x.getFieldAs("productGroupId").asInstanceOf[Long], x.getFieldAs("storePrice").asInstanceOf[Double],
          x.getFieldAs("marketPrice").asInstanceOf[Double], x.getFieldAs("slotNum").asInstanceOf[Int], x.getFieldAs("productNewCouponPreferential").asInstanceOf[Double],
          x.getFieldAs("productTime").asInstanceOf[Long], x.getFieldAs("productOpTime").asInstanceOf[Long], x.getFieldAs("cardId").asInstanceOf[Long], x.getFieldAs("cardStatus").asInstanceOf[Int],
          x.getFieldAs("cardProductId").asInstanceOf[Long], x.getFieldAs("cardSkuId").asInstanceOf[Long], x.getFieldAs("cardTime").asInstanceOf[Long], x.getFieldAs("cardOpTime").asInstanceOf[Long])
    }.addSink(chSink).uid("benefit_order_card_info")

    env.execute("lofter card consumption job")

  }
}
