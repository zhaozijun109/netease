package com.netease.lofter.realtime.push

import com.github.nscala_time.time.Imports.DateTime
import com.google.common.util.concurrent.MoreExecutors
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, esConfig, kafkaConfig}
import com.netease.wm.hubble.avro.NewPostFansPush
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
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
import org.slf4j.{Logger, LoggerFactory}

import java.time.Duration
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.concurrent.duration.DurationInt

private class NewPostFansPushNotifier{}

/**
 * new post notify 30 days browsing fans
 */
object NewPostFansPushNotifier {

  case class NewPost(postId: Long, blogId: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val latenessInSeconds = 60

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("new_post_fans_push_gy")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val binlogWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[SubscribeEvent](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[SubscribeEvent] {
        override def extractTimestamp(element: SubscribeEvent, recordTimestamp: Long): Long = element.getTimestamp
      })

    val newPostFansPushSink = KafkaSink.builder[NewPostFansPush]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter.push.publish.notice")
          .setValueSerializationSchema(new AvroJsonSerSchema[NewPostFansPush])
          .build()
      ).build()

    val newPosts = env.fromSource(binlogSource, binlogWaterMark, "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[NewPost]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT)
          .foreach { row =>
            val changeType = row.getType
            row.getTableName match {
              case "RecommendReviewPost" =>
                val postId = row.getColumn("postId").getNewValue.asInstanceOf[Long]
                val blogId = row.getColumn("blogId").getNewValue.asInstanceOf[Long]
                collector.collect(NewPost(postId, blogId))
              case _ =>
            }
          }
      }

    val newPostFansPushEvents = AsyncDataStream.unorderedWait(newPosts, new NewPostFansPushFunction, 30, concurrent.duration.SECONDS, 100)

    newPostFansPushEvents
      .disableChaining()
      .keyBy(_.userId)
      .process(new PushRateLimiter)
      .sinkTo(newPostFansPushSink)

    env.execute("new post fans push")
  }

  class PushRateLimiter extends KeyedProcessFunction[Long, NewPostFansPush, NewPostFansPush] {
    lazy val lastPushTimeState: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("last-push-time", createTypeInformation[Long]))

    val RATE_LIMIT_PERIOD: Long = 3600L * 1000L

    override def processElement(value: NewPostFansPush, ctx: KeyedProcessFunction[Long, NewPostFansPush, NewPostFansPush]#Context, out: Collector[NewPostFansPush]): Unit = {
      val now = ctx.timestamp()
      if(now > lastPushTimeState.value() + RATE_LIMIT_PERIOD) {
        out.collect(value)
        lastPushTimeState.update(now)
      }
    }
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class NewPostFansPushFunction extends AsyncFunction[NewPost, NewPostFansPush] {


    override def asyncInvoke(e: NewPost, resultFuture: ResultFuture[NewPostFansPush]): Unit = {
      val hour = DateTime.now().getHourOfDay
      if(hour >= 1 && hour < 7) {
        // no disturbing period
        resultFuture complete Seq.empty
      } else {
        BlogFans.fetchBlogFans(e.blogId).foreach { fans =>
          val result = fans.map { userId => NewPostFansPush(e.postId, e.blogId, userId) }.toList

          resultFuture complete result
        }
      }
    }
  }

  class BlogFans{}
  object BlogFans {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
    lazy val client: RestHighLevelClient = new RestHighLevelClient(RestClient.builder(esConfig.ES_HOSTS:_*))

    val LOG: Logger = LoggerFactory.getLogger(classOf[BlogFans])

    lazy val blogFansCache: AsyncLoadingCache[Long, Seq[Long]] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(1.hours)
        .buildAsyncFuture(
          (blogId: Long) => fetchBlogFansEs(blogId)
        )

    def fetchBlogFansEs(blogId: Long): Future[Seq[Long]] = {
      val searchRequest = new SearchRequest("lofter_blog_active_fans")

      val conditions = QueryBuilders.boolQuery()
      conditions.should(QueryBuilders.termQuery("blogId", blogId))

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("userId", "use_field_mapping")
        .size(10000)

      searchRequest.source(sourceBuilder)
      val result = Promise[Seq[Long]]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits()
            val fans = hits.map { hit =>
              hit.field("userId").getValue[Number].longValue()
            }.toList

            result success fans
          } catch {
            case NonFatal(e) =>
              LOG.warn("query es error", e)
              result failure(e)
          }
        }
      })

      result.future
    }

    def fetchBlogFans(blogId: Long): Future[Seq[Long]] = {
      blogFansCache.get(blogId)
    }
  }
}
