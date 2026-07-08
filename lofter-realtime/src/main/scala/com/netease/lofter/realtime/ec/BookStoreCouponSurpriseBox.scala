package com.netease.lofter.realtime.ec

import com.alibaba.druid.pool.DruidDataSource
import com.google.common.util.concurrent.MoreExecutors
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, dbConfig, kafkaConfig}
import com.netease.wm.hubble.avro.{RecItemEvent, SurpriseBoxCoupon}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import javax.sql.DataSource
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

private class BookStoreCouponSurpriseBox{}

object BookStoreCouponSurpriseBox {
  val LOG: Logger = LoggerFactory.getLogger(classOf[BookStoreCouponSurpriseBox])

  // orderType: 文包 1001，月会员 31，周会员 7, 非首次订单 1, 其他 vipDays
  case class OrderEvent(userId: Long, orderType: Int, endTime: Long)
  case class BookStoreBrowse(userId: Long, postId: Long, blogId: Long, time: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().withHourOfDay(11).withMinuteOfHour(45).getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("bookstore_coupon_surprise_box_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val couponSurpriseBoxSink = KafkaSink.builder[SurpriseBoxCoupon]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.COMMON.BOOK_STORE_COUPON_ACTIVEPOPUP")
          .setValueSerializationSchema(new AvroJsonSerSchema[SurpriseBoxCoupon])
          .build()
      ).build()

    val eventDetail = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "trace-product-detail")
      .uid("bookstore-coupon-surprise-box-input")
      .filter(s => s.action == 3 && s.itemId.contains("_") && !s.itemId.contains("-") )

    val bookStorePostBrowse = eventDetail.rebalance.keyBy(_.userId).process(new BookStorePostBrowseFilter).uid("bookstore-browse")

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("bookstore_coupon_surprise_box_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val bookStoreOrders = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[OrderEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Trade_StoreVipOrder" =>
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
                val vipDays = row.getColumn("vipDays").getNewValue.asInstanceOf[Int]
                val effectiveTime = row.getColumn("effectiveTime").getNewValue.asInstanceOf[Long]

                val paySuccess = if(changeType == RowChangeType.INSERT) status > 0 else (oldStatus == 0 && status > 0)
                if(paySuccess) {
                  val orderType = vipDays
                  val endTime = new DateTime(effectiveTime).plusDays(vipDays).getMillis
                  collector.collect(OrderEvent(userId, orderType, endTime))
                }

              case "Trade_PostPackOrder" =>
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]
                val finishTime = row.getColumn("finishTime").getNewValue.asInstanceOf[Long]
                val paySuccess = if(changeType == RowChangeType.INSERT) status > 0 else oldStatus == 0 && status > 0

                if(paySuccess) {
                  val orderType = 1001
                  val endTime = new DateTime(finishTime).withTimeAtStartOfDay().plusDays(8).getMillis
                  collector.collect(OrderEvent(userId, orderType, endTime))
                }
              case _ =>
            }
          }
      }

    bookStorePostBrowse.keyBy(_.userId)
      .connect(bookStoreOrders.keyBy(_.userId))
      .process(new BookStoreCouponDispatch).uid("coupon-dispatch")
      .sinkTo(couponSurpriseBoxSink)

    env.execute("bookstore coupon surprise box")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class BookStoreCouponDispatch extends KeyedCoProcessFunction[Long, BookStoreBrowse, OrderEvent, SurpriseBoxCoupon] {
    lazy val postsState: MapState[Long, Int] = {
      val stateDescriptor = new MapStateDescriptor[Long, Int]("daily-posts", createTypeInformation[Long], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    lazy val dailyPostCountState: MapState[Int, Int] = {
      val stateDescriptor = new MapStateDescriptor[Int, Int]("daily-post-count", createTypeInformation[Int], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    lazy val firstOrderEndTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("first-bookstore-order-end-time", createTypeInformation[Long]))
    lazy val orderState: ValueState[Int] = getRuntimeContext.getState[Int](new ValueStateDescriptor[Int]("first-bookstore-order-state", createTypeInformation[Int]))

    override def processElement1(e: BookStoreBrowse, ctx: KeyedCoProcessFunction[Long, BookStoreBrowse, OrderEvent, SurpriseBoxCoupon]#Context, out: Collector[SurpriseBoxCoupon]): Unit = {
      val firstOrderType = orderState.value()
      if(firstOrderType == 7 || firstOrderType == 31 || firstOrderType == 1001) {
        val postId = e.postId
        val dt = DateTime.now().toString("yyyyMMdd").toInt
        val currentPosts = dailyPostCountState.get(dt)

        currentPosts match {
          case 0 | 1 =>
            val lastDt = postsState.get(postId)
            if(dt > lastDt) { // new post browse
              postsState.put(postId, dt)
              dailyPostCountState.put(dt, currentPosts + 1)

              if(currentPosts == 1) {
                val firstOrderEndTime = firstOrderEndTimeState.value()
                val isInTriggerPeriod = firstOrderType match {
                  case 31 => firstOrderEndTime > System.currentTimeMillis() && firstOrderEndTime < DateTime.now().plusDays(5).getMillis
                  case _ => firstOrderEndTime > System.currentTimeMillis()
                }
                if(isInTriggerPeriod) {
                  out.collect(SurpriseBoxCoupon(e.userId))
                }
              }
            }
          case _ => // ignore more than 2 posts browse case
        }
      }
    }

    override def processElement2(value: OrderEvent, ctx: KeyedCoProcessFunction[Long, BookStoreBrowse, OrderEvent, SurpriseBoxCoupon]#Context, out: Collector[SurpriseBoxCoupon]): Unit = {
      val currentOrderStatus = orderState.value()
      currentOrderStatus match {
        case t if t == 1 =>
        case t if t > 1 => orderState.update(1)
        case _ =>
          orderState.update(value.orderType)
          firstOrderEndTimeState.update(value.endTime)
      }
    }
  }


  class BookStorePostBrowseFilter extends KeyedProcessFunction[Long, RecItemEvent, BookStoreBrowse] {
    @transient private lazy val blogCache = BlogCache

    override def open(parameters: Configuration): Unit = {
      blogCache.updateBookStoreBlogsIfNecessary()
    }

    override def processElement(e: RecItemEvent, ctx: KeyedProcessFunction[Long, RecItemEvent, BookStoreBrowse]#Context,
                                out: Collector[BookStoreBrowse]): Unit = {
      blogCache.updateBookStoreBlogsIfNecessary()

      val blogIdHex = e.itemId.split("_").head
      val blogId = java.lang.Long.parseLong(blogIdHex, 16)
      val postIdHex = e.itemId.split("_").tail.head
      val postId = java.lang.Long.parseLong(postIdHex, 16)
      if(blogCache.isBlogInBookStore(blogId)) {
        out.collect(BookStoreBrowse(e.userId, postId, blogId, e.time))
      }
    }
  }

  case class BlogQuery(content: String)
  private class BlogCache{}
  private object BlogCache {
    val JDBC_URL: String = dbConfig.LOFTER_DB_URL
    val DRIVER: String = dbConfig.DRIVER
    val LOG: Logger = LoggerFactory.getLogger(classOf[BlogCache])

    @transient private lazy val dataSource: DataSource = {
      LOG.debug("connecting jdbcUrl: {}", JDBC_URL)
      val ds = new DruidDataSource()
      ds.setDriverClassName(DRIVER)
      ds.setUrl(JDBC_URL)
      ds.setLoginTimeout(5000)
      ds.setTestOnReturn(true)
      ds.setTestOnReturn(true)
      ds.setTestWhileIdle(true)
      ds.setValidationQuery("select 1")
      ds.setFailFast(true)
      ds
    }

    @volatile private var bookStoreBlogs: Set[Long] = Set.empty
    @volatile private var lastUpdateTime = 0L
    private val UPDATE_INTERVAL: Long = 600 * 1000L

    def updateBookStoreBlogsIfNecessary(): Unit = {
      if(System.currentTimeMillis() - lastUpdateTime > UPDATE_INTERVAL) {
        LOG.info("updating bookstore blogs")

        implicit val conn: Connection = dataSource.getConnection()

        import com.netease.wm.util.Sql._
        try {
          val queryResult = sql"""select content from AdminPubData where name = 'book_store_blogIds'""".query[BlogQuery]

          LOG.info("book_store_blogIds: {}", queryResult.head.content)

          val result = queryResult.head.content.split(",").map(_.trim).filter(_.nonEmpty).map(_.toLong).toSet

          if(!result.equals(bookStoreBlogs) && result.nonEmpty) {
            LOG.info("update bookstore blogs set to: {}", result.toString())
            bookStoreBlogs = result
          }

          lastUpdateTime = System.currentTimeMillis()
          LOG.info("updating bookstore blogs finished")

          if(bookStoreBlogs.isEmpty) {
            throw new RuntimeException("bookstore blogs is empty")
          }
        } catch {
          case NonFatal(e) =>
            LOG.error("failed to update bookstore blogs: {}", e)
        }
        finally {
          conn.close()
        }
      }
    }

    def getLastUpdateTime(): Long = lastUpdateTime

    def isBlogInBookStore(blogId: Long): Boolean = {
      bookStoreBlogs.contains(blogId)
    }
  }
}
