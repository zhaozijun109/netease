package com.netease.wm.hubble

import com.alibaba.druid.pool.DruidDataSource
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.lofter.rs.basic.bean.dto.upload.ActionDto
import com.netease.wm.hubble.common.{esConfig, kafkaConfig, recConfig, shuffleConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.{SerializationSchema, SimpleStringSchema}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.flink.util.Collector
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.{Logger, LoggerFactory}
import rs.basic.upload.parse.handler.ActionMessageHandler

import java.sql.Connection
import javax.sql.DataSource
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import scala.util.control.NonFatal

private class LogShuffleRec{}
/**
 * 推荐系统行为数据分流
 * 1. 根据推荐eventId列表分流相关数据
 * 2. 修复permalink参数
 * 3. 转换推荐格式数据
 */
object LogShuffleRec {
  val MAX_LOG_SIZE = 700000
  val objectMapper = new ObjectMapper()
  val gson = new Gson()

  val LOG: Logger = LoggerFactory.getLogger(classOf[LogShuffleRec])

  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  case class ParsedEvent(eventId: Option[String], deviceUdid: Option[String], appVersion: Option[String], source: String, action: Option[Int] = None)

  case class ItemEntry(itemId: Option[String], itemType: Option[String], entryType: String)

  def parseLogEvents(v: String): ParsedEvent = {
    val root = objectMapper.readTree(v)
    val eventId = Option(root.get("eventId")).map(_.asText())
    val deviceUdid = Option(root.get("deviceUdid")).map(_.asText())
    val appVersion = Option(root.get("appVersion")).map(_.asText())
    ParsedEvent(eventId, deviceUdid, appVersion, v)
  }

  case class RecEvent(eventId: String, appVersion: String, actionCode: Int)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val kafkaSource = KafkaSource.builder[String]
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setProperties(shuffleConfig.recSourceKafkaProperties)
      .setTopics(shuffleConfig.recSourceTopic)
      .setGroupId("hubble_shuffle_rec_v2")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema)
      .build

    val logSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "log-shuffle-rec-src")
      .uid("log-shuffle-rec-source")

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
      // .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setBootstrapServers(kafkaConfig.BOOTSTRAP_SERVERS)
      .setKafkaProducerConfig(shuffleConfig.recDestKafkaProperties)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
        .setTopic(shuffleConfig.recDestTopic)
        .setValueSerializationSchema(new ActionDtoSerializationSchema)
        .build()
      ).build()

    val recStream = parsed.filter(_.deviceUdid.isDefined)
      .keyBy(_.deviceUdid.get.hashCode)
      .process(new EventFilterProcessFunction)

    AsyncDataStream.unorderedWait(recStream, new ItemLookupAsyncFunction, 30, concurrent.duration.SECONDS, 100)
      .disableChaining()
      .flatMap { line =>
          try {
            if(line.eventId.exists(_ == "a2-7") && line.source.contains("blogId =") ) { // skip a2-7 json format error
              None
            } else {
              val parsedRecord: ActionDto = ActionMessageHandler.parseActionDto(line.source, line.action.getOrElse(-9999))
              Option(parsedRecord)
            }
          } catch {
            case NonFatal(e) =>
              LOG.error(s"error parsing mda event to rec event actionDto: $line", e)
              None
          }
      }
      .sinkTo(recSink).uid("log-shuffle-rec-sink")

    env.execute("lofter hubble log shuffle for rec")
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  /**
   * 异步调用填充日志博客字段
   */
  class ItemLookupAsyncFunction extends AsyncFunction[ParsedEvent, ParsedEvent] {

    @transient private lazy val postCache = PostCache
    @transient private lazy val cardCache = CardCache

    override def asyncInvoke(input: ParsedEvent, resultFuture: ResultFuture[ParsedEvent]): Unit = {
      val postItemTypeSet = Set("text", "photo", "audio", "video", "qa", "article", "answer", "cos_answer",
        "normal_comment_answer", "line_comment_answer", "photo_comment_answer", "join_nominatesign_answer", "join_score_answer")
      val cardItemTypeSet = Set("card")

      try {
        val root = objectMapper.readTree(input.source)
        val attr = root.get("attributes").asInstanceOf[ObjectNode]
        val itemId = Option(attr.get("itemId")).map(_.asText())
        val itemType = Option(attr.get("itemType")).map(_.asText().toLowerCase)

        val extOption = if(attr.get("ext") != null) Option(attr.get("ext").asText()) else None
        val nested = extOption
          .filter{ s =>
            input.eventId match {
              case Some("a2-7") => s.startsWith("{\"")
              case _ => s.startsWith("{")
            }
          }
          .map{ s => objectMapper.readTree(s).asInstanceOf[ObjectNode] }

        val nestedItemId = nested.flatMap(s => Option(s.get("itemId"))).map(_.asText())
        val nestedItemType = nested.flatMap(s => Option(s.get("itemType"))).map(_.asText().toLowerCase)

        val ext_itemId = Option(attr.get("ext_itemId")).map(_.asText())
        val ext_itemType = Option(attr.get("ext_itemType")).map(_.asText().toLowerCase)
        val add_itemId = Option(attr.get("add_itemId")).map(_.asText())
        val add_itemType = Option(attr.get("add_itemType")).map(_.asText().toLowerCase)

        val itemEntries = Seq(
          ItemEntry(itemId, itemType, "itemId"),
          ItemEntry(ext_itemId, ext_itemType, "ext_itemId"),
          ItemEntry(add_itemId, add_itemType, "add_itemId"),
          ItemEntry(nestedItemId, nestedItemType, "ext/itemId")
        ).filter(e => e.itemId.isDefined && e.itemId.get.nonEmpty)

        val postEntries = itemEntries.filter(_.itemType.exists(postItemTypeSet))
          .filter(e => e.itemId.exists(_.forall(Character.isDigit)))

        val cardEntries = itemEntries.filter(_.itemType.exists(cardItemTypeSet))
          .filterNot(e => e.itemId.exists(_.forall(Character.isDigit)))

        val posts = postEntries.map(_.itemId.get.toLong)
        val cardCodes = cardEntries.map(_.itemId.get)

        if(posts.size > 0) {
          postCache.getPermalinks(posts).foreach { m =>
            postEntries.foreach { e =>
              val itemId = e.itemId.get.toLong
              val permalink = m(itemId)
              e.entryType match {
                case "itemId" | "ext_itemId" | "add_itemId" =>
                  attr.put(e.entryType, permalink)
                case "ext/itemId" =>
                  nested.get.put("itemId", permalink)
                  attr.put("ext", objectMapper.writeValueAsString(nested.get))
                case _ =>
              }
            }

            resultFuture.complete(Seq(input.copy(source = objectMapper.writeValueAsString(root))))
          }
        } else if(cardCodes.size > 0) {
            cardCache.getActivityId(cardCodes).foreach { m =>
              cardEntries.foreach { e =>
                val activityCode = e.itemId.get
                val activityId = m(activityCode)
                e.entryType match {
                  case "itemId" | "ext_itemId" | "add_itemId" =>
                    attr.put(e.entryType, activityId)
                  case "ext/itemId" =>
                    nested.get.put("itemId", activityId)
                    attr.put("ext", objectMapper.writeValueAsString(nested.get))
                  case _ =>
                }
              }

              resultFuture.complete(Seq(input.copy(source = objectMapper.writeValueAsString(root))))

            }
        } else {
          resultFuture complete Seq(input)
        }
      } catch {
        case NonFatal(e) =>
          LOG.error(s"processing error content: ${input.source}", e)
          resultFuture complete Seq(input)
      }
    }

    override def timeout(input: ParsedEvent, resultFuture: ResultFuture[ParsedEvent]): Unit = {
      LOG.error("timeout for permalinkId replacing event: {}", input.source)
      // TODO handle lots of timeout events
    }
  }

  case class EventMeta(startVersion: Long, actionCode: Int)

  object EventFilterProcessFunction {
  }

  /**
   * 过滤推荐事件
   * （10分钟更新推荐列表)
   */
  class EventFilterProcessFunction extends KeyedProcessFunction[Int, ParsedEvent, ParsedEvent] {


    lazy val lastActions: MapState[Int, Long] = {
      val desc = new MapStateDescriptor[Int, Long]("last-action", createTypeInformation[Int], createTypeInformation[Long])
      desc.enableTimeToLive(StateTtlConfig.newBuilder(Time.minutes(15)).updateTtlOnCreateAndWrite().cleanupInRocksdbCompactFilter(200).build())
      getRuntimeContext.getMapState(desc)
    }

    val EVENT_LIST_UPDATE_INTERVAL: Long = 600 * 1000L
    val REPEAT_WINDOW: Long = 5 * 60 * 1000L

    @transient private lazy val dataSource: DataSource = {
      LOG.debug("connecting jdbcUrl: {}", recConfig.JDBC_URL)
      val ds = new DruidDataSource()
      ds.setDriverClassName(recConfig.DRIVER)
      ds.setUrl(recConfig.JDBC_URL)
      ds.setLoginTimeout(5000)
      ds.setTestOnBorrow(true)
      ds.setTestOnReturn(true)
      ds.setFailFast(true)
      ds
    }

    @volatile var eventVersionMap: Map[String, EventMeta] = _
    @volatile var lastUpdateTime = 0L

    override def open(parameters: Configuration): Unit = {
      updateEventSet()
      lastUpdateTime = System.currentTimeMillis() + EVENT_LIST_UPDATE_INTERVAL
    }

    override def processElement(value: ParsedEvent, ctx: KeyedProcessFunction[Int, ParsedEvent, ParsedEvent]#Context, out: Collector[ParsedEvent]): Unit = {
      LOG.debug("processing element: {}", value.source)

      val nextUpdateTime = (System.currentTimeMillis() / EVENT_LIST_UPDATE_INTERVAL + 1) * EVENT_LIST_UPDATE_INTERVAL
      if(nextUpdateTime > lastUpdateTime) {
        updateEventSet()
        lastUpdateTime = nextUpdateTime
      }

      val versionNum = toVersionNum(value.appVersion.getOrElse(""))
      if(value.eventId.flatMap(s => eventVersionMap.get(s)).exists(_.startVersion <= versionNum)) {
        val root = objectMapper.readTree(value.source)
        val attributes = Option(root.get("attributes"))
        val extStr = attributes.flatMap(s => Option(s.get("ext"))).map(_.asText()).getOrElse("")
        val eventId = value.eventId.get
        val eventMeta = eventVersionMap(eventId)
        val newActionCode = eventMeta.actionCode
        val scene = attributes.flatMap(a => Option(a.get("scene"))).map(_.asText().toLowerCase).getOrElse("")
        val itemTypeOption = attributes.flatMap(a => Option(a.get("itemType"))).map(_.asText().toLowerCase).filter(_.nonEmpty)
        val itemIdOption = attributes.flatMap(a => Option(a.get("itemId"))).map(_.asText().toLowerCase).filter(_.nonEmpty)
        val textOption = attributes.flatMap(a => Option(a.get("text"))).map(_.asText().toLowerCase).filter(_.nonEmpty)
        val actionOption = attributes.flatMap(a => Option(a.get("action"))).map(_.asText().toLowerCase).filter(_.nonEmpty)
        val occurTime = root.get("kafkaTime").asLong(0)


        val (itemId, itemType, text) =
          if ( extStr.nonEmpty && (itemIdOption.isEmpty || itemTypeOption.isEmpty || textOption.isEmpty)) {
            val ext = Try{objectMapper.readTree(extStr)}.toOption
            ( itemIdOption.orElse(ext.flatMap(s => Option(s.get("itemId"))).map(_.asText().toLowerCase())).getOrElse(""),
              itemTypeOption.orElse(ext.flatMap(s => Option(s.get("itemType"))).map(_.asText().toLowerCase())).getOrElse(""),
              textOption.orElse(ext.flatMap(s => Option(s.get("text"))).map(_.asText().toLowerCase())).getOrElse("") )
          } else (itemIdOption.getOrElse(""), itemTypeOption.getOrElse(""), textOption.getOrElse(""))

        val uKey = Seq(eventId, scene, itemType, itemId, text).map(_.hashCode).reduce(_ * 31 + _)
        val lastActionOccurTime = lastActions.get(uKey)

        val isRepeat = lastActionOccurTime > 0 && occurTime < lastActionOccurTime + REPEAT_WINDOW && scene.nonEmpty
        if(occurTime - lastActionOccurTime > 20000 && scene.nonEmpty) {
          lastActions.put(uKey, occurTime)
        }
        if(isRepeat) {
          root.asInstanceOf[ObjectNode].put("repeat", 1)
          out.collect(value.copy(source = objectMapper.writeValueAsString(root), action = Some(newActionCode)))
        } else out.collect(value.copy(action = Some(newActionCode)))
      }
    }

    private def updateEventSet(): Unit = {
      LOG.info("updating event set")

      implicit val conn: Connection = dataSource.getConnection()

      import com.netease.wm.util.Sql._
      try {
        val result = sql"""select eventId, appVersion, actionCode from Buried_Point where enable = 1""".query[RecEvent].map(s => (s.eventId, EventMeta(toVersionNum(s.appVersion), s.actionCode))).toMap

        if(!result.equals(eventVersionMap) && result.nonEmpty) {
          LOG.info("update rec event set to: {}", result.toString())
          eventVersionMap = result
        }

        LOG.info("updating event set finished")

        if(eventVersionMap.isEmpty) {
          throw new RuntimeException("rec event set is empty")
        }
      } catch {
        case NonFatal(e) =>
          LOG.error("failed to update event set for shuffle: {}", e)
      }
      finally {
        conn.close()
      }
    }

    val VersionRegex: scala.util.matching.Regex = """(\d+)\.(\d+)\.(\d+).*""".r

    private def toVersionNum(version: String): Long = {
      version match {
        case VersionRegex(m, n, d) => m.toInt * 10000 + n.toInt * 100 + d.toInt
        case _ => 0
      }
    }

    override def close(): Unit = {
      if(dataSource != null && dataSource.isInstanceOf[java.io.Closeable]) {
        dataSource.asInstanceOf[java.io.Closeable].close()
      }
    }
  }

  class ActionDtoSerializationSchema extends SerializationSchema[ActionDto] {
    override def serialize(element: ActionDto): Array[Byte] = {
      val source = gson.toJson(element)
      LOG.debug("log shuffle rec: {}", source)
      source.getBytes("UTF-8")
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

    lazy val postDimCache: AsyncLoadingCache[Long, String] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(1.hours)
        .buildAsyncFuture(
          (postId: Long) => fetchPostPermalinkFromEs(Seq(postId)) { m =>m.getOrElse(postId, "")},
          allLoader = Option((posts: Iterable[Long]) => fetchPostPermalinkFromEs(posts.toSeq)(identity))
        )

    private def fetchPostPermalinkFromEs[T](posts: Seq[Long])(mapFunc: Map[Long, String] => T): Future[T] = {
      LOG.debug("call getPostPermalinks({})", posts)
      val searchRequest = new SearchRequest("lofter_dim_post")

      val conditions = QueryBuilders.boolQuery()

      posts.foreach { p =>
        conditions.should(QueryBuilders.termQuery("postId", p))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("blogId", "use_field_mapping")
        .docValueField("postId", "use_field_mapping")
        .timeout(TimeValue.timeValueSeconds(1))

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
              val blogId = hit.field("blogId").getValue[String].toLong.longValue()
              val permalink = blogId.toHexString + "_" + postId.toHexString
              postId -> permalink
            }.toMap

            if(queryMap.size < posts.size) {
              val permalinkMap: Map[Long, String] = (posts map { p: Long =>  p -> queryMap.getOrElse(p, "")}).toMap
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

    def getPermalinks(posts: Seq[Long]): Future[Map[Long, String]] = {
      LOG.debug("call getPermalinks({})", posts )
      postDimCache.getAll(posts)
    }

  }

  private class CardCache{}
  private object CardCache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[CardCache])

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

    lazy val activityDimCache: AsyncLoadingCache[String, Long] =
      Scaffeine()
        .maximumSize(1000000)
        .expireAfterWrite(1.hours)
        .buildAsyncFuture(
          (activityCode: String) => fetchActivityIdFromEs(Seq(activityCode)){ m => m.getOrElse(activityCode, 0L)},
          allLoader = Option((activityCodes: Iterable[String]) => fetchActivityIdFromEs(activityCodes.toSeq)(identity))
        )

    def getActivityId(code: String): Future[Long] = activityDimCache.get(code)
    def getActivityId(codes: Seq[String]): Future[Map[String, Long]] = {
      LOG.debug("call getActivityId({})", codes )
      activityDimCache.getAll(codes)
    }

    private def fetchActivityIdFromEs[T](activityCodes: Seq[String])(mapFunc: Map[String, Long] => T): Future[T] = {
      LOG.debug("call fetchActivityIdFromEs({})", activityCodes)
      val searchRequest = new SearchRequest("lofter_dim_card_activity")

      val conditions = QueryBuilders.boolQuery()

      activityCodes.foreach { code =>
        conditions.should(QueryBuilders.termQuery("activityCode", code))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("activityCode", "use_field_mapping")
        .docValueField("activityId", "use_field_mapping")

      searchRequest.source(sourceBuilder)
      val result = Promise[T]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits()
            val queryMap = hits.map { hit =>
              val activityCode = hit.field("activityCode").getValue[String]
              val activityId = hit.field("activityId").getValue[Number].longValue()
              activityCode -> activityId
            }.toMap

            if(queryMap.size < activityCodes.size) {
              val activityCode2IdMap: Map[String, Long] = (activityCodes map { code: String =>  code -> queryMap.getOrElse(code, 0L)}).toMap
              LOG.warn("log shuffle rec can't find all activityCode {} related activityId, use 0 as activityId", activityCodes.mkString(" "))
              result success mapFunc(activityCode2IdMap)
            } else {
              result success mapFunc(queryMap)
            }
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
