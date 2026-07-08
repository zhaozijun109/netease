package com.netease.lofter.realtime.ad

import com.google.common.util.concurrent.MoreExecutors
import com.netease.lofter.realtime.common.{esConfig, kafkaConfig}
import com.netease.wm.hubble.avro.{AdNewLinkup, NewDeviceRegisterRetain}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.{AvroDsJsonDeserSchema, AvroJsonSerSchema}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
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
import com.netease.wm.hubble.avro.{ClientMdaLogAvro => Mda}

import java.time.Duration
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

object AdNewLinkupRetainRegister {
  case class DeviceEvent(deviceUdid: String, userId: Long, userRegisterTime: Long, time: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)

    val latenessInSeconds = 60

    val adNewLinkupSource = KafkaSource.builder[AdNewLinkup]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("adx.newlinkup.online")
      .setGroupId("ad_new_linkup_retain")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroDsJsonDeserSchema[AdNewLinkup](ignoreErrors = true))
      .build()

    val registerSink = KafkaSink.builder[NewDeviceRegisterRetain]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.COMMON.ATTRIBUTION.regsiter")
          .setKeySerializationSchema(new DeviceActionIdSchema)
          .setValueSerializationSchema(new AvroJsonSerSchema[NewDeviceRegisterRetain])
          .build()
      ).build()

    val retainSink = KafkaSink.builder[NewDeviceRegisterRetain]()
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("LOFTER.COMMON.retention")
          .setKeySerializationSchema(new DeviceActionIdSchema)
          .setValueSerializationSchema(new AvroJsonSerSchema[NewDeviceRegisterRetain])
          .build()
      ).build()

    val adNewLinkupWaterMark = WatermarkStrategy
      .forBoundedOutOfOrderness[AdNewLinkup](Duration.ofSeconds(latenessInSeconds))
      .withTimestampAssigner(new SerializableTimestampAssigner[AdNewLinkup] {
        override def extractTimestamp(element: AdNewLinkup, recordTimestamp: Long): Long = element.time
      })

    val adNewDevices = env.fromSource(adNewLinkupSource, adNewLinkupWaterMark, "ad-new-linkup")
      .uid("ad-new-linkup-input")
      .filter(_.newUserFlag)


    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.online")
      .setGroupId("ad_new_linkup_retain_v2")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val mda = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "lofter-mda-avro")
      .uid("lofter-mda-online-gy2")
      .filter(_.eventId.getOrElse("") == "da_user_profile")
      .map{ e => DeviceEvent(e.deviceUdid.getOrElse(""), e.userId.getOrElse(0L), 0L, e.kafkaTime.getOrElse(0L))}

    val deviceEvents = AsyncDataStream.unorderedWait(mda, new DeviceUserResolveFunc, 30, concurrent.duration.SECONDS, 100)

    val result = adNewDevices.keyBy(_.dadeviceId)
      .connect(deviceEvents.keyBy(_.deviceUdid))
      .process(new NewDeviceRegisterRetainProcess)
      .uid("ad-new-linkup-retain")

    result.filter(s => s.retain > 0).sinkTo(retainSink)
    result.filter(s => s.registerUserId > 0).sinkTo(registerSink)

    env.execute("ad new linkup retain and register")
  }

  class NewDeviceRegisterRetainProcess extends KeyedCoProcessFunction[String, AdNewLinkup, DeviceEvent, NewDeviceRegisterRetain] {
    lazy val activateState: ValueState[(Long, Long, Long)] = getRuntimeContext.getState[(Long, Long, Long)](new ValueStateDescriptor[(Long, Long, Long)]("activate-retain", createTypeInformation[(Long, Long, Long)]))
    lazy val registerTimeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("register-time", createTypeInformation[Long]))

    override def processElement1(e: AdNewLinkup, ctx: KeyedCoProcessFunction[String, AdNewLinkup, DeviceEvent, NewDeviceRegisterRetain]#Context, out: Collector[NewDeviceRegisterRetain]): Unit = {
      val isLofter =  e.appId.isEmpty || e.appId.get == "lofter"
      if(e.newUserFlag && isLofter) {
        activateState.update((e.actionId, e.time, 0))
      }
    }

    private val DAY_IN_MILLIS: Long = 24 * 3600 * 1000L

    override def processElement2(e: DeviceEvent, ctx: KeyedCoProcessFunction[String, AdNewLinkup, DeviceEvent, NewDeviceRegisterRetain]#Context, out: Collector[NewDeviceRegisterRetain]): Unit = {
      val activate = activateState.value()
      val eventTime = e.time

      if(activate != null) {
        val (actionId, activateTime, retainTime) = activate

        val nextDayStart = new DateTime(activateTime).withTimeAtStartOfDay().plusDays(1).getMillis
        val nextDayEnd = new DateTime(activateTime).withTimeAtStartOfDay().plusDays(2).getMillis
        val maxRetainDayEnd = new DateTime(activateTime).withTimeAtStartOfDay().plusDays(31).getMillis
        val lastRetainDayStart = new DateTime(retainTime).withTimeAtStartOfDay().getMillis
        val currentDayStart = new DateTime(eventTime).withTimeAtStartOfDay().getMillis

        if(eventTime >= nextDayStart && eventTime < maxRetainDayEnd && currentDayStart != lastRetainDayStart) {
          val retain = (currentDayStart - nextDayStart) / DAY_IN_MILLIS + 1
          activateState.update((actionId, activateTime, eventTime))
          if(retain >= 1 && retain <= 30) {
            out.collect(NewDeviceRegisterRetain(e.deviceUdid, actionId, e.time, registerUserId = 0, retain = retain.toInt))
          }
        }

        if(e.userRegisterTime > 0 && e.userRegisterTime < nextDayEnd && registerTimeState.value() == 0) {
          registerTimeState.update(e.userRegisterTime)
          out.collect(NewDeviceRegisterRetain(e.deviceUdid, actionId, e.time, registerUserId = e.userId, retain = 0))
        }


        if(registerTimeState.value() > 0 & e.userRegisterTime > 0) {
          val registerTime = registerTimeState.value()
          val regNextDayStart = new DateTime(registerTime).withTimeAtStartOfDay().plusDays(1).getMillis
          val regNextDayEnd = new DateTime(registerTime).withTimeAtStartOfDay().plusDays(2).getMillis

          if(eventTime >= regNextDayStart && eventTime < regNextDayEnd) {
            registerTimeState.update(-1L)
            out.collect(NewDeviceRegisterRetain(e.deviceUdid, actionId, e.time, registerUserId = e.userId, retain = 1))
          }
        }
      }

    }
  }

  class DeviceActionIdSchema extends SerializationSchema[NewDeviceRegisterRetain] {
    override def serialize(v: NewDeviceRegisterRetain): Array[Byte] = {
      v.actionId.toString.getBytes("UTF-8")
    }
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())

  class DeviceUserResolveFunc extends AsyncFunction[DeviceEvent, DeviceEvent] {

    @transient private lazy val userCache = UserCache

    override def asyncInvoke(e: DeviceEvent, resultFuture: ResultFuture[DeviceEvent]): Unit = {
      if(e.userId <= 0) {
        resultFuture complete Seq(e)
      } else {

      }
      userCache.getRegisterTime(e.userId).foreach { registerTime =>
        resultFuture complete Seq(e.copy(userRegisterTime = registerTime))
      }
    }
  }

  private class UserCache{}
  object UserCache {
    import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

    val LOG: Logger = LoggerFactory.getLogger(classOf[UserCache])

    lazy val client: RestHighLevelClient = new RestHighLevelClient(RestClient.builder(esConfig.ES_HOSTS:_*))

    lazy val userCache: AsyncLoadingCache[Long, Long] =
      Scaffeine()
        .maximumSize(2000000)
        .expireAfterWrite(30.minutes)
        .buildAsyncFuture(
          (userId: Long) => fetchUserRegisterProfile(Seq(userId)) { m => m.getOrElse(userId, 0L)},
          allLoader = Option((users: Iterable[Long]) => fetchUserRegisterProfile(users.toSeq)(identity))
        )

    private def fetchUserRegisterProfile[T](users: Seq[Long])(mapFunc: Map[Long, Long] => T): Future[T] = {
      val searchRequest = new SearchRequest("lofter_dim_user")

      val conditions = QueryBuilders.boolQuery()

      users.foreach { p =>
        conditions.should(QueryBuilders.termQuery("userId", p))
      }

      val sourceBuilder = new SearchSourceBuilder()
        .query(conditions)
        .fetchSource(false)
        .docValueField("userId", "use_field_mapping")
        .docValueField("createTime", "use_field_mapping")
        .docValueField("isAnonymous", "use_field_mapping")

      searchRequest.source(sourceBuilder)
      val result = Promise[T]()

      client.searchAsync(searchRequest, new ActionListener[SearchResponse] {

        override def onFailure(e: Exception): Unit = result.failure(e)

        override def onResponse(searchResponse: SearchResponse): Unit = {
          LOG.debug("get es response: {}", searchResponse)
          try {
            val hits = searchResponse.getHits.getHits()
            val queryMap = hits.map { hit =>
              val userId = hit.field("userId").getValue[Number].longValue()
              val createTime = hit.field("createTime").getValue[Number].longValue()
              val isAnonymous = hit.field("isAnonymous").getValue[Number].intValue()
              val registerTime = if(isAnonymous > 0) 0L else createTime
              userId -> registerTime
            }.toMap

            if(queryMap.size < users.size) {
              val userRegisterTime: Map[Long, Long] = (users map { p: Long =>  p -> queryMap.getOrElse(p, 0L)}).toMap
              result success mapFunc(userRegisterTime)
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

    def getRegisterTime(postId: Long): Future[Long] = {
      userCache.get(postId)
    }
  }
}
