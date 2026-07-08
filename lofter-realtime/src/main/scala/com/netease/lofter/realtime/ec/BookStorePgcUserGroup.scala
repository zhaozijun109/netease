package com.netease.lofter.realtime.ec

import com.google.common.util.concurrent.MoreExecutors
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, esConfig, kafkaConfig}
import com.netease.wm.hubble.avro.{AcwEvent, Mda, PgcUserGroup, RecItemEvent}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.{AvroJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal

private class BookStorePgcUserGroup{}

object BookStorePgcUserGroup {
  val LOG: Logger = LoggerFactory.getLogger(classOf[BookStorePgcUserGroup])

  // action 1 browse 2 order
  case class PostEvent(userId: Long, postId: Long, action: Int)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().withHourOfDay(11).withMinuteOfHour(45).getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("bookstore_pgc_user_group_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val acwSource = KafkaSource.builder[AcwEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.acw.front.log")
      .setGroupId("bookstore_pgc_user_group_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroJsonDeserSchema[AcwEvent](ignoreErrors = true))
      .build()

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.avro")
      .setGroupId("bookstore_pgc_user_group_gy")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val pgcUserGroup = KafkaSink.builder[PgcUserGroup]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.bookstore.crowd")
          .setValueSerializationSchema(new AvroJsonSerSchema[PgcUserGroup])
          .build()
      ).build()

    val postBrowses = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "post-browse-detail")
      .uid("bookstore-pgc-user-group-input")
      .filter(s => s.action == 3 && !s.itemId.contains("-") &&  s.itemId.contains("_") && s.userId > 0)
      .map { e =>
        val postIdHex = e.itemId.split("_").tail.head
        val postId = java.lang.Long.parseLong(postIdHex, 16)
        PostEvent(e.userId, postId, action = 1)
      }

    val acwSearch = env.fromSource(acwSource, WatermarkStrategy.noWatermarks(), "user-actpwd-search")
      .uid("bookstore-pgc-user-group-input2")
      .filter(s => s.userId > 0 && s.actPwd.nonEmpty && s.channel.isDefined && s.channel.get.contains("米良") && s.settlementType.exists(_ == "CPS"))
      .map { e =>
        PostEvent(e.userId, 0L, action = 3)
      }

    val bookStoreEnter = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .filter(e => (e.eventId.startsWith("sc-")) && e.userId.getOrElse(0L) > 0L)
      .map { e: Mda =>
        PostEvent(e.userId.get, 0L, action = 4)
      }.uid("bookstore-pgc-user-group-input3")

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("bookstore_pgc_user_group_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val postOrders = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[PostEvent]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "Trade_GiftPresentRecord" =>
                val userId = row.getColumn("sender").getNewValue.asInstanceOf[Long]
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                val giftType = row.getColumn("giftType").getNewValue.asInstanceOf[Long]
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]
                val oldStatus = row.getColumn("status").getOldValue.asInstanceOf[Int]

                val paySuccess = if(changeType == RowChangeType.INSERT) status == 0 else (oldStatus != 0 && status == 0)
                if(paySuccess && giftType == 1) {
                  collector.collect(PostEvent(userId, postId, action = 2))
                }

              case _ =>
            }
          }
      }

    val postEvents = postBrowses.union(postOrders, acwSearch, bookStoreEnter)

    val pgcPostEvents = AsyncDataStream.unorderedWait(postEvents, new PgcPostFilterFunc, 30, concurrent.duration.SECONDS, 100)

      // async pgc post filter
    pgcPostEvents
      .keyBy(_.userId)
      .process(new PgcUserGroupSelect).uid("pgc-user-group-select")
      .sinkTo(pgcUserGroup)

    env.execute("bookstore pgc user group")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class PgcPostFilterFunc extends AsyncFunction[PostEvent, PostEvent] {

    @transient private lazy val postCache = PostCache

    override def asyncInvoke(e: PostEvent, resultFuture: ResultFuture[PostEvent]): Unit = {
      val postId = e.postId
      if(postId > 0) {
        PostCache.getPgcGiftTime(postId).foreach { isPgc =>
          if(isPgc > 0) {
            resultFuture complete Seq(e)
          } else {
            resultFuture complete Seq.empty// ignore
          }
        }
      } else {
        resultFuture complete Seq(e)
      }
    }
  }

  class PgcUserGroupSelect extends KeyedProcessFunction[Long, PostEvent, PgcUserGroup] {
    lazy val postsState: MapState[Long, Int] = {
      val stateDescriptor = new MapStateDescriptor[Long, Int]("daily-posts", createTypeInformation[Long], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(30)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    lazy val dailyPostCountState: MapState[Int, Int] = {
      val stateDescriptor = new MapStateDescriptor[Int, Int]("daily-post-count", createTypeInformation[Int], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(30)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    lazy val dailyPgcOrdersState: MapState[Int, Int] = {
      val stateDescriptor = new MapStateDescriptor[Int, Int]("daily-pgc-orders", createTypeInformation[Int], createTypeInformation[Int])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.hours(30)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    override def processElement(e: PostEvent, ctx: KeyedProcessFunction[Long, PostEvent, PgcUserGroup]#Context, out: Collector[PgcUserGroup]): Unit = {
      val dt = DateTime.now().toString("yyyyMMdd").toInt
      e.action match {
        case 1 =>
          val currentPosts = dailyPostCountState.get(dt)

          if(currentPosts < 3) {
            val lastDt = postsState.get(e.postId)
            if(dt > lastDt) { // new post browse
              postsState.put(e.postId, dt)
              dailyPostCountState.put(dt, currentPosts + 1)
              if(currentPosts + 1 >= 3) {
                out.collect(PgcUserGroup(e.userId))
              }
            }
          }
        case 2 =>
          if(dailyPgcOrdersState.get(dt) == 0) {
            dailyPgcOrdersState.put(dt, 1)
            out.collect(PgcUserGroup(e.userId))
          }
        case 3 | 4 =>
          out.collect(PgcUserGroup(e.userId))

        case _ => // ignore
      }
    }
  }

  private class PostCache{}
  object PostCache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[PostCache])

    lazy val client: RestHighLevelClient = new RestHighLevelClient(RestClient.builder(esConfig.ES_HOSTS:_*))

    lazy val pgcGiftTimeCache: AsyncLoadingCache[Long, Long] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(30.minutes)
        .buildAsyncFuture(
          (postId: Long) => fetchPgcGiftTime(Seq(postId)) { m => m.getOrElse(postId, 0L)},
          allLoader = Option((posts: Iterable[Long]) => fetchPgcGiftTime(posts.toSeq)(identity))
        )

    private def fetchPgcGiftTime[T](posts: Seq[Long])(mapFunc: Map[Long, Long] => T): Future[T] = {
      val searchRequest = new SearchRequest("lofter_dim_gift_post")

      val conditions = QueryBuilders.boolQuery()

      posts.foreach { p =>
        conditions.should(QueryBuilders.termQuery("postId", p))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("postId", "use_field_mapping")
        .docValueField("giftTime", "use_field_mapping")
        .docValueField("isPgc", "use_field_mapping")

      searchRequest.source(sourceBuilder)
      val result = Promise[T]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits()
            val queryMap = hits.map { hit =>
              val postId = hit.field("postId").getValue[Number].longValue()
              val giftTime = hit.field("giftTime").getValue[Number].longValue()
              val isPgc = hit.field("isPgc").getValue[Number].longValue()
              postId -> (if(isPgc > 0) giftTime else 0L)
            }.toMap

            if(queryMap.size < posts.size) {
              val pgcGiftTime: Map[Long, Long] = (posts map { p: Long =>  p -> queryMap.getOrElse(p, 0L)}).toMap
              result success mapFunc(pgcGiftTime)
            } else {
              result success mapFunc(queryMap)
            }
          } catch {
            case NonFatal(e) =>
              LOG.warn("query es error", e)
              result failure(e)
          }
        }
      })

      result.future
    }

    def getPgcGiftTime(postId: Long): Future[Long] = {
      pgcGiftTimeCache.get(postId)
    }
  }
}
