package com.netease.lofter.realtime.risk

import com.google.common.util.concurrent.MoreExecutors
import com.netease.lofter.realtime.common.{esConfig, kafkaConfig}
import com.netease.wm.hubble.avro.RecItemEvent
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types.RowKind
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

/**
 * 48h post interaction data
 */
object RiskNewPostInteraction {
  case class Event(postId: Long, blogId: Long, time: Long, action: Int, action_value: Int,
                   interaction_type: Int, publishTime: Long)

  case class NewPostFeature(postId: Long, blogId: Long, publishTime: Long, pv: Long, favoriteCount: Long,
                            reblogCount: Long, subscribeCount: Long, responseCount: Long, reportNum: Long = 0)

  case class RiskMessage(messageType: Int, payLoad: NewPostFeature)

  val RISK_SINK_TOPIC: String = "GR_POST"

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val eventDetailSource = KafkaSource.builder[RecItemEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("rec.item.detail")
      .setGroupId("risk_new_post_interaction")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[RecItemEvent])
      .build()

    val riskSink = KafkaSink.builder[RiskMessage]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.RISK_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder[RiskMessage]()
          .setTopic(RISK_SINK_TOPIC)
          .setValueSerializationSchema(new RiskMessageSerSchema)
          .build()
      ).build()

    val eventDetail: DataStream[RecItemEvent] = env.fromSource(eventDetailSource, WatermarkStrategy.noWatermarks(), "rec-item-event-detail")
      .uid("risk-new-post-interaction-input")
      .filter(e => e.itemId.contains("_") && e.repeat.getOrElse(0L) == 0L && (e.action == 3 || e.action == 5))

    val newPostEventDetail = AsyncDataStream.unorderedWait(eventDetail, new ResolveNewPost, 30, concurrent.duration.SECONDS, 100)

    tableEnv.createTemporaryView("new_post_event", newPostEventDetail)

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "300 s")
    configuration.setString("table.exec.mini-batch.size", "1000000")

    tableEnv.getConfig.setIdleStateRetention(Duration.ofDays(3))

    val featureSql =
      """
        |select postId, blogId, publishTime,
        |       count(1) filter (where action = 3) as pv,
        |       sum(action_value) filter (where action = 5 and interaction_type = 1 ) as praise,
        |       sum(action_value) filter (where action = 5 and interaction_type = 3 ) as recommend,
        |       sum(action_value) filter (where action = 5 and interaction_type = 4 ) as subscribe,
        |       sum(action_value) filter (where action = 5 and interaction_type = 5) as response
        |from new_post_event
        |group by postId, blogId, publishTime
        |""".stripMargin

    tableEnv.sqlQuery(featureSql).toChangelogStream
      .filter(row => row.getKind == RowKind.INSERT || row.getKind == RowKind.UPDATE_AFTER)
      .flatMap { x =>
        val maxMetric = Seq(x.getFieldAs("pv").asInstanceOf[Long], x.getFieldAs("praise").asInstanceOf[Int], x.getFieldAs("recommend").asInstanceOf[Int],
          x.getFieldAs("subscribe").asInstanceOf[Int], x.getFieldAs("response").asInstanceOf[Int]).max
        if(maxMetric >= 10) {
          Some(RiskMessage(10000, NewPostFeature(x.getFieldAs("postId").asInstanceOf[Long], x.getFieldAs("blogId").asInstanceOf[Long],
            x.getFieldAs("publishTime").asInstanceOf[Long], x.getFieldAs("pv").asInstanceOf[Long], x.getFieldAs("praise").asInstanceOf[Int],
            x.getFieldAs("recommend").asInstanceOf[Int], x.getFieldAs("subscribe").asInstanceOf[Int], x.getFieldAs("response").asInstanceOf[Int])))
        } else None
      }.sinkTo(riskSink).uid("risk-new-post-interaction-sink")

    env.execute("risk new post interaction")

  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class RiskMessageSerSchema extends SerializationSchema[RiskMessage]{
    override def serialize(element: RiskMessage): Array[Byte] = {
      implicit val format = DefaultFormats
      val content = write(element)
      content.getBytes("UTF-8")
    }
  }

  class ResolveNewPost extends AsyncFunction[RecItemEvent, Event] {

    @transient private lazy val postCache = PostCache

    override def asyncInvoke(e: RecItemEvent, resultFuture: ResultFuture[Event]): Unit = {
      val postIdHex = e.itemId.split("_").tail.head
      val blogIdHex = e.itemId.split("_").head

      try {
        val postId = java.lang.Long.parseLong(postIdHex, 16)
        val blogId = java.lang.Long.parseLong(blogIdHex, 16)
        PostCache.getPostIpMeta(postId).foreach { meta =>
          val twoDaysAgo = DateTime.now().minusDays(2).getMillis

          if(meta.publishTime > twoDaysAgo && e.time - meta.publishTime < 48 * 3600 * 1000L) {
            val result = Event(
              postId, blogId, e.time, e.action, e.action_value,
              e.interaction_type.getOrElse(0), meta.publishTime
            )
            resultFuture complete Seq(result)
          } else {
            resultFuture complete Seq.empty// ignore old post
          }
        }
      } catch {
        case NonFatal(e) =>
          e.printStackTrace()
          resultFuture complete Seq.empty
      }
    }
  }
  case class Post(postId: Long, publishTime: Long)
  private class PostCache{}
  private object PostCache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[Post])

    lazy val client: RestHighLevelClient = new RestHighLevelClient(RestClient.builder(esConfig.ES_HOSTS:_*))

    lazy val postDimCache: AsyncLoadingCache[Long, Post] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(30.minutes)
        .buildAsyncFuture(
          (postId: Long) => fetchPostIpMeta(Seq(postId)) { m => m.getOrElse(postId, getDefaultPost(postId))},
          allLoader = Option((posts: Iterable[Long]) => fetchPostIpMeta(posts.toSeq)(identity))
        )

    private def fetchPostIpMeta[T](posts: Seq[Long])(mapFunc: Map[Long, Post] => T): Future[T] = {
      val searchRequest = new SearchRequest("lofter_dim_ip_new_post")

      val conditions = QueryBuilders.boolQuery()

      posts.foreach { p =>
        conditions.should(QueryBuilders.termQuery("postId", p))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("postId", "use_field_mapping")
        .docValueField("publishTime", "use_field_mapping")

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
              val publishTime = hit.field("publishTime").getValue[Number].longValue()
              postId -> Post(postId, publishTime)
            }.toMap

            if(queryMap.size < posts.size) {
              val permalinkMap: Map[Long, Post] = (posts map { p: Long =>  p -> queryMap.getOrElse(p, getDefaultPost(p))}).toMap
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

    def getDefaultPost(postId: Long): Post = Post(postId, 0L)

    def getPostIpMeta(posts: Seq[Long]): Future[Map[Long, Post]] = {
      postDimCache.getAll(posts)
    }

    def getPostIpMeta(postId: Long): Future[Post] = {
      postDimCache.get(postId)
    }
  }
}
