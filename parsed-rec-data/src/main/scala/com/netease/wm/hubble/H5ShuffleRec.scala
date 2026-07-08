package com.netease.wm.hubble

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.lofter.rs.basic.bean.dto.upload.{ActionDto, MessageDto}
import com.netease.wm.hubble.common.{esConfig, kafkaConfig, recConfig, shuffleConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.{SerializationSchema, SimpleStringSchema}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.{Logger, LoggerFactory}
import rs.basic.upload.parse.handler.{ActionMessageHandler, CardActionMessageHandler, PveMessageHandler, WebActionMessageHandler}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

private class H5ShuffleRec{}
/**
 * H5推荐系统行为数据分流
 * 1. 过滤 lofter.wap.online 中card-7事件
 * 2. 转activityCode到activityId (lofter.binlog.json)
 */
object H5ShuffleRec {
  val MAX_LOG_SIZE = 700000
  val objectMapper = new ObjectMapper()
  val gson = new Gson()

  val LOG: Logger = LoggerFactory.getLogger(classOf[H5ShuffleRec])

  case class ParsedEvent(eventId: Option[String], deviceUdid: Option[String], appVersion: Option[String], source: String)

  case class PermalinkEntry(itemId: Option[Long], itemType: Option[String], entryType: String)

  def parseLogEvents(v: String): ParsedEvent = {
    val root = objectMapper.readTree(v)
    val eventId = Option(root.get("eventId")).map(_.asText())
    val deviceUdid = Option(root.get("deviceUdid")).map(_.asText())
    val appVersion = Option(root.get("appVersion")).map(_.asText())
    ParsedEvent(eventId, deviceUdid, appVersion, v)
  }

  case class RecEvent(eventId: String, appVersion: String)

  val H5_EVENT_SET: Set[String] = Set("card-7", "w1-18", "w1-19", "w3-2", "w3-17", "w3-18", "w3-38", "w3-39", "w1-41", "w1-38", "w1-37", "pve-2", "pve-11", "pve-21")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val kafkaSource = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setProperties(shuffleConfig.recSourceKafkaProperties)
      .setTopics("lofter.wap.online.json")
      .setGroupId("hubble_shuffle_rec_h5")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val logSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "log-shuffle-rec-src")

    val parsed = logSource.flatMap { v =>
      try {
        Some(parseLogEvents(v))
      } catch {
        case NonFatal(e) =>
          LOG.error(s"parsing json error: $v", e)
          None
      }
    }

    val recSink = KafkaSink.builder[ActionDto]()
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      // .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setKafkaProducerConfig(shuffleConfig.recDestKafkaProperties)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(shuffleConfig.recDestTopic)
          .setValueSerializationSchema(new ActionDtoSerializationSchema)
          .build()
      ).build()

    val recStream = parsed.filter(s => s.deviceUdid.isDefined && s.eventId.exists(e => H5_EVENT_SET(e)))
      .flatMap { line =>
        try {
          val messageDto: Option[MessageDto] = line.eventId.get match {
            case "card-7" => Option(CardActionMessageHandler.parseCardMessage(line.source))
            case "w3-38" | "w1-18" =>
              Some(WebActionMessageHandler.parseWebMessage(line.source))
            case "pve-2" | "pve-11" | "pve-21" => Some(PveMessageHandler.parseMessage(line.source))
            case _ => None
          }

          messageDto.filter(e => e.getUserId != null && e.getUserId.nonEmpty).flatMap { m =>
            Option(ActionMessageHandler.parseActionDto(m,null))
          }
        } catch {
          case NonFatal(e) =>
            LOG.error(s"error parsing mda event to rec event actionDto: $line", e)
            None
        }
      }

    AsyncDataStream.unorderedWait(recStream, new ActivityLookUp, 30, concurrent.duration.SECONDS, 100)
      .disableChaining()
      .sinkTo(recSink).uid("log-shuffle-rec-sink")

    env.execute("lofter h5 log shuffle for rec")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  /**
   * 异步调用填充日志博客字段
   */
  class ActivityLookUp extends AsyncFunction[ActionDto, ActionDto] {

    @transient private lazy val cache = Cache

    override def asyncInvoke(dto: ActionDto, resultFuture: ResultFuture[ActionDto]): Unit = {
        val itemId = dto.getItemId
        if(itemId != null && itemId.length > 0 && dto.getEventId == "card-7") {
          cache.getActivityId(itemId).foreach { activityId =>
            if(activityId > 0){
              dto.setItemId(activityId.toString)
              resultFuture.complete(Seq(dto))
            } else {
              resultFuture complete Seq(dto)
            }
          }
        } else {
          resultFuture complete Seq(dto)
        }
    }

    override def timeout(input: ActionDto, resultFuture: ResultFuture[ActionDto]): Unit = {
      LOG.error("timeout for activityId replacing event: {}", input.toString)
      // TODO handle lots of timeout events
    }
  }

  class ActionDtoSerializationSchema extends SerializationSchema[ActionDto] {
    override def serialize(element: ActionDto): Array[Byte] = {
      val source = gson.toJson(element)
      LOG.debug("log shuffle rec: {}", source)
      source.getBytes("UTF-8")
    }
  }

  private class Cache{}
  private object Cache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[Cache])

    lazy val client: RestHighLevelClient = new RestHighLevelClient(RestClient.builder(esConfig.ES_HOSTS:_*))

    lazy val activityDimCache: AsyncLoadingCache[String, Long] =
      Scaffeine()
        .maximumSize(1000000)
        .expireAfterWrite(1.hours)
        .buildAsyncFuture(
          (activityCode: String) => fetchActivityIdFromEs(activityCode)
        )

    def getActivityId(code: String): Future[Long] = activityDimCache.get(code)

    private def fetchActivityIdFromEs(activityCode: String): Future[Long] = {
      LOG.debug("call fetchActivityIdFromEs({})", activityCode)
      val searchRequest = new SearchRequest("lofter_dim_card_activity")

      val conditions = QueryBuilders.boolQuery()

      conditions.should(QueryBuilders.termQuery("activityCode", activityCode))

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("activityCode", "use_field_mapping")
        .docValueField("activityId", "use_field_mapping")

      searchRequest.source(sourceBuilder)
      val result = Promise[Long]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits
            val activityId = hits.headOption.map(_.field("activityId").getValue[Number].longValue())
            result success activityId.getOrElse(0L)
          } catch {
            case NonFatal(e) =>
              LOG.warn("query es error", e)
              result failure e
          }
        }
      })

      result.future
    }
  }
}

