package com.netease.wm.hubble

import com.google.common.util.concurrent.MoreExecutors
import com.netease.wm.hubble.avro.{RecItemEvent, RecItemPvUserList}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import com.netease.wm.hubble.common.{esConfig, kafkaConfig, recConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.concurrent.duration.DurationInt

private class RecItemShortTermFeatureSum {}

object RecItemShortTermFeatureSum {

  case class Event(itemId: String, isNew: Int, userId: Long, eventTime: java.sql.Timestamp)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("RecItemShortTermFeature")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val aggregateSink = KafkaSink.builder[RecItemPvUserList]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("rec_upload_item_stay_user")
          .setValueSerializationSchema(new AvroJsonSerSchema[RecItemPvUserList])
          .build()
      ).build()

    val rawEvents: DataStream[Event] = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "rec-item-event-detail")
      .uid("rec-item-short-term-feature-input-new")
      .filter(e => e.itemType == "ARTICLE" && e.action == 3 && e.userId > 0 && e.itemId.contains("_"))
      .map { e =>
        val isNew = 0 // temporary set to 0
        Event(e.itemId, isNew, e.userId, new java.sql.Timestamp(e.time))
      }.rebalance

    val enrichedEvents = AsyncDataStream.unorderedWait(rawEvents, new ItemLookupAsyncFunction, 30, concurrent.duration.SECONDS, 100)
      .disableChaining()

    tableEnv.createTemporaryView("rec_item_event",
      enrichedEvents,
      Schema.newBuilder()
        .columnByExpression("rowTime", "CAST(eventTime AS TIMESTAMP_LTZ(3))")
        .watermark("rowTime", "rowTime - INTERVAL '10' SECOND")
        .build()
    )

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "10 s")
    configuration.setString("table.exec.mini-batch.size", "100000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofMinutes(15))

    tableEnv.executeSql("CREATE TEMPORARY FUNCTION collection_keys AS 'com.netease.wm.hubble.common.UdfCollectionKeys' ")

    val featureSql =
      """
        |select itemId, isNew, collection_keys(collect(userId)) as userList
        |from (
        |  select window_start, window_end, itemId, isNew, userId,
        |         row_number() over (partition by window_start, window_end, itemId, isNew order by rowTime desc) as rowNum
        |  from table(
        |    tumble(table rec_item_event, descriptor(rowTime), interval '5' minutes)
        |  )
        |)
        |where rowNum <= 10
        |group by window_start, window_end, itemId, isNew
        |""".stripMargin

    tableEnv.sqlQuery(featureSql).toAppendStream[(String,Int,String)]
      .map{ x =>
        val (itemId, isNew, userList) = x
        RecItemPvUserList(itemId, userList, isNew)
      }.sinkTo(aggregateSink).uid("rec-item-short-term-feature-sink")

    env.execute("rec item short term feature")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class ItemLookupAsyncFunction extends AsyncFunction[Event, Event] {

    @transient private lazy val postCache = PostCache

    override def asyncInvoke(e: Event, resultFuture: ResultFuture[Event]): Unit = {
      val itemIdParts = e.itemId.split("_")
      val postIdHex = itemIdParts.tail.head
      val postId = java.lang.Long.parseLong(postIdHex, 16)

      val threeDaysAgo = DateTime.now().minusDays(3).getMillis
      postCache.getPublishTime(Seq(postId)).foreach { m =>
        val isNew = m.get(postId).exists( publishTime => publishTime > threeDaysAgo)
        val processed = e.copy(isNew = if(isNew) 1 else 0)
        resultFuture complete Seq(processed)
      }
    }
  }

  private class PostCache{}
  private object PostCache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[PostCache])

    lazy val client: RestHighLevelClient = new RestHighLevelClient(
      RestClient.builder(esConfig.ES_HOSTS:_*).setHttpClientConfigCallback(
        new RestClientBuilder.HttpClientConfigCallback() {
          override def customizeHttpClient(httpAsyncClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
            val credentialsProvider: CredentialsProvider = new BasicCredentialsProvider()
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esConfig.username, esConfig.password))
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
          }
        }
      ))

    lazy val postDimCache: AsyncLoadingCache[Long, Long] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(DurationInt(1).hours)
        .buildAsyncFuture(
          (postId: Long) => fetchPostPublishTimeFromEs(Seq(postId)) { m =>m.getOrElse(postId, 0L)},
          allLoader = Option((posts: Iterable[Long]) => fetchPostPublishTimeFromEs(posts.toSeq)(identity))
        )

    private def fetchPostPublishTimeFromEs[T](posts: Seq[Long])(mapFunc: Map[Long, Long] => T): Future[T] = {
      LOG.debug("call getPostPermalinks({})", posts)
      val searchRequest = new SearchRequest("lofter_dim_post")

      val conditions = QueryBuilders.boolQuery()

      posts.foreach { p =>
        conditions.should(QueryBuilders.termQuery("postId", p))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("publishTime", "use_field_mapping")
        .docValueField("postId", "use_field_mapping")

      searchRequest.source(sourceBuilder)
      val result = Promise[T]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits()
            val queryMap = hits.map { hit =>
              val postId = hit.field("postId").getValue[String].toLong.longValue()
              val publishTime = hit.field("publishTime").getValue[String].toLong.longValue()
              postId -> publishTime
            }.toMap

            if(queryMap.size < posts.size) {
              val permalinkMap: Map[Long, Long] = (posts map { p: Long =>  p -> queryMap.getOrElse(p, 0L)}).toMap
              LOG.warn("log shuffle rec can't find all posts {} related permalink, use '' as permalink", posts.mkString(" "))
              result success mapFunc(permalinkMap)
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

    def getPublishTime(posts: Seq[Long]): Future[Map[Long, Long]] = {
      LOG.debug("call getPermalinks({})", posts )
      postDimCache.getAll(posts)
    }

  }

}
