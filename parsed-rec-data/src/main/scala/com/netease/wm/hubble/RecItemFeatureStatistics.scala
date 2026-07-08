package com.netease.wm.hubble

import com.alibaba.druid.pool.DruidDataSource
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.cache.{Cache, CacheBuilder}
import com.netease.wm.hubble.common.recConfig
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.{FilterFunction, RichFlatMapFunction, RichMapFunction}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.scala.{AsyncDataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.flink.util.concurrent.Executors
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.slf4j.{Logger, LoggerFactory}

import java.security.MessageDigest
import java.sql.{Connection, PreparedStatement, ResultSet}
import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util
import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


object RecItemFeatureStatistics {
  private final val LOG: Logger = LoggerFactory.getLogger(RecItemFeatureStatistics.getClass)
  private final val DF = DateTimeFormatter.ofPattern("yyyyMMdd")

  private case class RecUploadData(eventId: String,
                                   itemId: String,
                                   itemType: String,
                                   tcIds: String,
                                   scene: String,
                                   recId: String,
                                   text: String,
                                   extRelatedItemType: String,
                                   extTab: String,
                                   extRepeat: Int,
                                   rating: Int,
                                   day: String)
  private case class RecUploadAction(actionType: String,
                                     itemId: String,
                                     itemType: String,
                                     tcIds: String,
                                     day: String)
  private case class ResultData(itemId: String,
                                itemType: String,
                                day: String,
                                feature: util.HashMap[String, StatisticsResult])
  private case class StatisticsResult(frPv: Int,
                                      frClk: Int,
                                      frAct: Int,
                                      saPv: Int,
                                      saClk: Int,
                                      saAct: Int,
                                      trPv: Int,
                                      trClk: Int,
                                      trAct: Int,
                                      trNewPv: Option[Int],
                                      trNewClk: Option[Int])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.enableObjectReuse()
    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setCheckpointTimeout(600000)
    env.setStateBackend(new EmbeddedRocksDBStateBackend(true))
    env.setMaxParallelism(256)

    val recItemSource = KafkaSource.builder[String]()
      .setTopics(recConfig.destTopic)
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setGroupId("RecItemFeatureStatistics")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .setProperties(recConfig.recDestKafkaProperties)
      .build()

    val recTcItemSink = KafkaSink.builder[String]()
      .setBootstrapServers(recConfig.BOOTSTRAP_SERVERS_ONLINE)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(recConfig.recTcItemFeatureTopic)
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()
      ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val itemDataStream = env.fromSource(recItemSource, WatermarkStrategy.noWatermarks(), "Rec Item Feature Source")
      .uid("RecItemFeatureSourceNew")
      .name("RecItemFeatureSource")
      .map(new RichMapFunction[String, RecUploadData] {
        private var objectMapper: ObjectMapper = _
        private var jsonNode: JsonNode = _

        override def open(parameters: Configuration): Unit = {
          objectMapper = new ObjectMapper()
          objectMapper.registerModules(DefaultScalaModule)
        }

        override def map(msg: String): RecUploadData = {
          var eventId: String = null
          var itemId: String = null
          var itemType: String = null
          var tcIds: String = null
          var scene: String = null
          var recId: String = null
          var text: String = null
          var extRelatedItemType: String = null
          var extTab: String = null
          var extRepeat: Int = 1
          var rating: Int = 0
          var day: String = null

          try {
            jsonNode = objectMapper.readTree(msg)

            eventId = if (jsonNode.get("eventId") != null) {
              jsonNode.get("eventId").asText()
            } else {
              null
            }

            itemId = if (jsonNode.get("itemId") != null) {
              jsonNode.get("itemId").asText()
            } else {
              null
            }

            itemType = if (jsonNode.get("itemType") != null) {
              jsonNode.get("itemType").asText()
            } else {
              null
            }

            tcIds = if (jsonNode.get("algInfoExtDto") != null && jsonNode.get("algInfoExtDto").get("tcId") != null) {
              jsonNode.get("algInfoExtDto").get("tcId").asText()
            } else {
              null
            }

            scene = if (jsonNode.get("scene") != null) {
              jsonNode.get("scene").asText()
            } else {
              null
            }

            recId = if (jsonNode.get("recId") != null) {
              jsonNode.get("recId").asText()
            } else {
              null
            }

            text = if (jsonNode.get("text") != null) {
              jsonNode.get("text").asText()
            } else {
              null
            }

            if (jsonNode.get("extData") != null) {
              if (jsonNode.get("extData").get("relatedItemType") != null) {
                extRelatedItemType = jsonNode.get("extData").get("relatedItemType").asText()
              }
              if (jsonNode.get("extData").get("tab") != null) {
                extTab = jsonNode.get("extData").get("tab").asText()
              }
              if (jsonNode.get("extData").get("repeat") != null) {
                extRepeat = jsonNode.get("extData").get("repeat").asInt()
              }
            }

            rating = if (jsonNode.get("rating") != null) {
              jsonNode.get("rating").asInt()
            } else {
              0
            }

            day = if (jsonNode.get("time") != null) {
              DF.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(jsonNode.get("time").asLong()), ZoneId.systemDefault()))
            } else {
              DF.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()))
            }
          } catch {
            case e: JsonParseException => LOG.error("Json parse error ", e)
          }

          RecUploadData(eventId, itemId, itemType, tcIds, scene, recId, text, extRelatedItemType, extTab, extRepeat, rating, day)
        }
      }).uid("RecUploadDataMap")
      .name("RecUploadDataMap")

    val resultDataStream = AsyncDataStream.unorderedWait(itemDataStream, new AsyncDatabaseResult(), 5, TimeUnit.SECONDS, 32)
      .uid("ResultDataStreamAsync")
      .name("ResultDataStreamAsync")
      .filter(new FilterFunction[(Integer, RecUploadData)] {
        override def filter(value: (Integer, RecUploadData)): Boolean = {
          value._1 > 0
        }
      }).uid("ResultDataStreamFilter")
      .name("ResultDataStreamFilter")
      .flatMap(new RichFlatMapFunction[(Integer, RecUploadData), RecUploadAction] {
        override def flatMap(value: (Integer, RecUploadData), out: Collector[RecUploadAction]): Unit = {
          val recUploadData: RecUploadData = value._2
          // LOG.info("================recUploadData: {}", recUploadData.toString)
          if (recUploadData.extRepeat == 0 && recUploadData.itemId != null && recUploadData.itemType != null) {
            if ("b1-45".equals(recUploadData.eventId) && "feed_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // frPv
              out.collect(RecUploadAction(actionType = "frPv", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("b1-46".equals(recUploadData.eventId) && "feed_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // frClk
              out.collect(RecUploadAction(actionType = "frClk", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ((recUploadData.rating == 124 || recUploadData.rating == 107 || recUploadData.rating == 108 || recUploadData.rating == 115) &&
              "feed_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // frAct
              out.collect(RecUploadAction(actionType = "frAct", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("g1-40".equals(recUploadData.eventId) && recUploadData.recId != null) { // saPv
              out.collect(RecUploadAction(actionType = "saPv", itemId = recUploadData.text, itemType = recUploadData.extRelatedItemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("g1-41".equals(recUploadData.eventId) && recUploadData.recId != null) { // saClk
              out.collect(RecUploadAction(actionType = "saClk", itemId = recUploadData.text, itemType = recUploadData.extRelatedItemType, recUploadData.tcIds, recUploadData.day))
            }

            if ((recUploadData.rating == 124 || recUploadData.rating == 107 || recUploadData.rating == 108 || recUploadData.rating == 115) &&
              "related_item".equals(recUploadData.scene) && recUploadData.recId != null) { // saAct
              out.collect(RecUploadAction(actionType = "saAct", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if (recUploadData.rating == 202 && recUploadData.recId != null) { // saAct
              out.collect(RecUploadAction(actionType = "saAct", itemId = recUploadData.text, itemType = recUploadData.extRelatedItemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("f1-46".equals(recUploadData.eventId) && "tag_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // trPv
              out.collect(RecUploadAction(actionType = "trPv", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("f1-33".equals(recUploadData.eventId) && "tag_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // trClk
              out.collect(RecUploadAction(actionType = "trClk", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ((recUploadData.rating == 124 || recUploadData.rating == 107 || recUploadData.rating == 108 || recUploadData.rating == 115) &&
              "tag_rec".equals(recUploadData.scene) && recUploadData.recId != null) { // trAct
              out.collect(RecUploadAction(actionType = "trAct", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("f1-46".equals(recUploadData.eventId) && "最新发布".equals(recUploadData.extTab)) { // trNewPv
              out.collect(RecUploadAction(actionType = "trNewPv", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }

            if ("f1-33".equals(recUploadData.eventId) && "最新发布".equals(recUploadData.extTab)) { // trNewClk
              out.collect(RecUploadAction(actionType = "trNewClk", recUploadData.itemId, recUploadData.itemType, recUploadData.tcIds, recUploadData.day))
            }
          }
        }
      })
      .uid("ResultDataStreamFlatMap")
      .name("ResultDataStreamFlatMap")

    val statisticsResultDataStream = resultDataStream.keyBy(RecUploadAction => (RecUploadAction.day,
        RecUploadAction.itemId, RecUploadAction.itemType))
      .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
      .process(new ProcessWindowFunction[RecUploadAction, ResultData, (String, String, String), TimeWindow] {
        private var statisticsSumState: MapState[String, Int] = _

        override def open(parameters: Configuration): Unit = {
          val statisticsSumStateDescriptor = new MapStateDescriptor[String, Int]("StatisticsSumState", classOf[String],
            classOf[Int])
          val stateTtlConfig = StateTtlConfig.newBuilder(org.apache.flink.api.common.time.Time.hours(26))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .cleanupInRocksdbCompactFilter(100000)
            .build
          statisticsSumStateDescriptor.enableTimeToLive(stateTtlConfig)
          statisticsSumState = getRuntimeContext.getMapState(statisticsSumStateDescriptor)
        }

        override def process(key: (String, String, String), context: Context, elements: Iterable[RecUploadAction],
                             out: Collector[ResultData]): Unit = {
          val day = key._1
          val itemId = key._2
          val itemType = key._3
          val feature: util.HashMap[String, StatisticsResult] = new util.HashMap[String, StatisticsResult]()
          elements.foreach {
            recUploadAction: RecUploadAction => {
              val actionType = recUploadAction.actionType
              val tcIds = recUploadAction.tcIds
              val updateAllMd5Key = String.format("%s_%s", day, md5Hash(actionType + itemId + itemType))
              actionType match {
                case "frPv" | "frClk" | "saPv" | "saClk" | "trPv" | "trClk" | "trNewPv" | "trNewClk" |
                     "frAct" | "saAct" | "trAct" =>
                  updateStatisticsSumState(updateAllMd5Key)

                  if (tcIds != null && !"".equals(tcIds)) {
                    for (tcId <- tcIds.split(",")) {
                      val updateTcMd5Key = String.format("%s_%s", day, md5Hash(actionType + itemId + itemType + tcId))
                      updateStatisticsSumState(updateTcMd5Key)
                      val tcIdFrPvMd5Key: String = String.format("%s_%s", day, md5Hash("frPv" + itemId + itemType + tcId))
                      val tcIdFrClkMd5Key: String = String.format("%s_%s", day, md5Hash("frClk" + itemId + itemType + tcId))
                      val tcIdFrActMd5Key: String = String.format("%s_%s", day, md5Hash("frAct" + itemId + itemType + tcId))
                      val tcIdSaPvMd5Key: String = String.format("%s_%s", day, md5Hash("saPv" + itemId + itemType + tcId))
                      val tcIdSaClkMd5Key: String = String.format("%s_%s", day, md5Hash("saClk" + itemId + itemType + tcId))
                      val tcIdSaActMd5Key: String = String.format("%s_%s", day, md5Hash("saAct" + itemId + itemType + tcId))
                      val tcIdTrPvMd5Key: String = String.format("%s_%s", day, md5Hash("trPv" + itemId + itemType + tcId))
                      val tcIdTrClkMd5Key: String = String.format("%s_%s", day, md5Hash("trClk" + itemId + itemType + tcId))
                      val tcIdTrActMd5Key: String = String.format("%s_%s", day, md5Hash("trAct" + itemId + itemType + tcId))
                      feature.put(tcId, StatisticsResult(
                        frPv = if (statisticsSumState.get(tcIdFrPvMd5Key) != null) {
                          statisticsSumState.get(tcIdFrPvMd5Key)
                        } else {
                          0
                        },
                        frClk = if (statisticsSumState.get(tcIdFrClkMd5Key) != null) {
                          statisticsSumState.get(tcIdFrClkMd5Key)
                        } else {
                          0
                        },
                        frAct = if (statisticsSumState.get(tcIdFrActMd5Key) != null) {
                          statisticsSumState.get(tcIdFrActMd5Key)
                        } else {
                          0
                        },
                        saPv = if (statisticsSumState.get(tcIdSaPvMd5Key) != null) {
                          statisticsSumState.get(tcIdSaPvMd5Key)
                        } else {
                          0
                        },
                        saClk = if (statisticsSumState.get(tcIdSaClkMd5Key) != null) {
                          statisticsSumState.get(tcIdSaClkMd5Key)
                        } else {
                          0
                        },
                        saAct = if (statisticsSumState.get(tcIdSaActMd5Key) != null) {
                          statisticsSumState.get(tcIdSaActMd5Key)
                        } else {
                          0
                        },
                        trPv = if (statisticsSumState.get(tcIdTrPvMd5Key) != null) {
                          statisticsSumState.get(tcIdTrPvMd5Key)
                        } else {
                          0
                        },
                        trClk = if (statisticsSumState.get(tcIdTrClkMd5Key) != null) {
                          statisticsSumState.get(tcIdTrClkMd5Key)
                        } else {
                          0
                        },
                        trAct = if (statisticsSumState.get(tcIdTrActMd5Key) != null) {
                          statisticsSumState.get(tcIdTrActMd5Key)
                        } else {
                          0
                        },
                        trNewPv = None,
                        trNewClk = None
                      ))
                    }
                  }

                case _ =>
              }
            }
          }

          val allFrPvMd5Key: String = String.format("%s_%s", day, md5Hash("frPv" + itemId + itemType))
          val allFrClkMd5Key: String = String.format("%s_%s", day, md5Hash("frClk" + itemId + itemType))
          val allFrActMd5Key: String = String.format("%s_%s", day, md5Hash("frAct" + itemId + itemType))
          val allSaPvMd5Key: String = String.format("%s_%s", day, md5Hash("saPv" + itemId + itemType))
          val allSaClkMd5Key: String = String.format("%s_%s", day, md5Hash("saClk" + itemId + itemType))
          val allSaActMd5Key: String = String.format("%s_%s", day, md5Hash("saAct" + itemId + itemType))
          val allTrPvMd5Key: String = String.format("%s_%s", day, md5Hash("trPv" + itemId + itemType))
          val allTrClkMd5Key: String = String.format("%s_%s", day, md5Hash("trClk" + itemId + itemType))
          val allTrActMd5Key: String = String.format("%s_%s", day, md5Hash("trAct" + itemId + itemType))
          val allTrNewPvMd5Key: String = String.format("%s_%s", day, md5Hash("trNewPv" + itemId + itemType))
          val allTrNewClkMd5Key: String = String.format("%s_%s", day, md5Hash("trNewClk" + itemId + itemType))
          feature.put("all", StatisticsResult(
            frPv = if (statisticsSumState.get(allFrPvMd5Key) != null) {
              statisticsSumState.get(allFrPvMd5Key)
            } else {
              0
            },
            frClk = if (statisticsSumState.get(allFrClkMd5Key) != null) {
              statisticsSumState.get(allFrClkMd5Key)
            } else {
              0
            },
            frAct = if (statisticsSumState.get(allFrActMd5Key) != null) {
              statisticsSumState.get(allFrActMd5Key)
            } else {
              0
            },
            saPv = if (statisticsSumState.get(allSaPvMd5Key) != null) {
              statisticsSumState.get(allSaPvMd5Key)
            } else {
              0
            },
            saClk = if (statisticsSumState.get(allSaClkMd5Key) != null) {
              statisticsSumState.get(allSaClkMd5Key)
            } else {
              0
            },
            saAct = if (statisticsSumState.get(allSaActMd5Key) != null) {
              statisticsSumState.get(allSaActMd5Key)
            } else {
              0
            },
            trPv = if (statisticsSumState.get(allTrPvMd5Key) != null) {
              statisticsSumState.get(allTrPvMd5Key)
            } else {
              0
            },
            trClk = if (statisticsSumState.get(allTrClkMd5Key) != null) {
              statisticsSumState.get(allTrClkMd5Key)
            } else {
              0
            },
            trAct = if (statisticsSumState.get(allTrActMd5Key) != null) {
              statisticsSumState.get(allTrActMd5Key)
            } else {
              0
            },
            trNewPv = if (statisticsSumState.get(allTrNewPvMd5Key) != null) {
              Some(statisticsSumState.get(allTrNewPvMd5Key))
            } else {
              Some(0)
            },
            trNewClk = if (statisticsSumState.get(allTrNewClkMd5Key) != null) {
              Some(statisticsSumState.get(allTrNewClkMd5Key))
            } else {
              Some(0)
            }
          ))
          out.collect(ResultData(itemId = itemId, itemType = itemType, day = day, feature = feature))
        }

        def updateStatisticsSumState(md5Key: String): Unit = {
          val currentCount = if (statisticsSumState.get(md5Key) != null) {
            statisticsSumState.get(md5Key)
          } else {
            0
          }
          statisticsSumState.put(md5Key, currentCount + 1)
        }
      })
      .uid("ResultDataStreamProcess")
      .name("ResultDataStreamProcess")
      .map(new RichMapFunction[ResultData, String] {
        private var objectMapper: ObjectMapper = _

        override def open(parameters: Configuration): Unit = {
          objectMapper = new ObjectMapper()
          objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
          objectMapper.registerModules(DefaultScalaModule)
        }

        override def map(value: ResultData): String = {
          val resultDataJsonString = objectMapper.writeValueAsString(value)
          // LOG.info("ResultData: {}", resultDataJsonString)
          resultDataJsonString
        }
      }).uid("ResultDataStreamMap")
      .name("ResultDataStreamMap")

    statisticsResultDataStream
      .rebalance
      .sinkTo(recTcItemSink)
      .uid("RecTcItemFeatureSink")
      .name("RecTcItemFeatureSink")

    env.execute("RecItemFeatureStatisticsJob")
  }

  private def md5Hash(text: String): String = {
    val md5: MessageDigest = MessageDigest.getInstance("MD5")
    val bytes = md5.digest(text.getBytes("UTF-8"))
    val hexString = new StringBuilder(32)
    for (byte <- bytes) {
      hexString.append("%02x".format(byte & 0xff))
    }
    hexString.toString()
  }

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  private class AsyncDatabaseResult extends RichAsyncFunction[RecUploadData, (Integer, RecUploadData)] {
    @transient private var scheduledExecutorService: ScheduledExecutorService = _
    @transient private lazy val cache: Cache[String, Integer] = CacheBuilder.newBuilder()
      .maximumSize(1000000)
      .initialCapacity(32)
      .expireAfterWrite(3, TimeUnit.HOURS)
      .build()

    override def open(parameters: Configuration): Unit = {
      scheduledExecutorService = new ScheduledThreadPoolExecutor(1)
      scheduledExecutorService.scheduleWithFixedDelay(new Runnable {
        override def run(): Unit = {
          updateItemFromDb()
        }
      }, 0, 60 * 10, TimeUnit.SECONDS)
    }

    override def asyncInvoke(recUploadData: RecUploadData, resultFuture: ResultFuture[(Integer, RecUploadData)]): Unit = {
      // LOG.info("=======cacheGet: {}", recUploadData.day + "_" + recUploadData.itemId)
      var result: Integer = cache.getIfPresent(recUploadData.day + "_" + recUploadData.itemId)
      // 针对于相关文章进行特判
      if (("g1-40".equals(recUploadData.eventId) || "g1-41".equals(recUploadData.eventId) || recUploadData.rating == 202) && recUploadData.recId != null) {
        result = cache.getIfPresent(recUploadData.day + "_" + recUploadData.text)
      }

      var  flag: Integer = 0
      if (result != null) {
        flag = result
      }
      val resultFlag: Future[Integer] = Future {flag}

      resultFlag.onComplete {
        case Success(value) => resultFuture.complete(Iterable((value, recUploadData)))
        case Failure(exception) =>
          LOG.error("===========Failed fetch cache: {}", exception.getMessage)
          resultFuture.complete(Iterable((0, recUploadData)))
      }
    }

    private def updateItemFromDb(): Unit = {
      val dataSource: DruidDataSource = new DruidDataSource()
      dataSource.setDriverClassName(recConfig.DRIVER)
      dataSource.setUrl(recConfig.JDBC_URL)
      dataSource.setLoginTimeout(5000)
      dataSource.setTestOnBorrow(true)
      dataSource.setTestOnReturn(true)
      dataSource.setFailFast(true)
      val connection: Connection = dataSource.getConnection()
      var nowTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())
      val day = DF.format(nowTime)
      updateCache(connection, day)

      val judgeTime = LocalDateTime.of(nowTime.toLocalDate, LocalTime.of(23, 30))
      if (nowTime.isAfter(judgeTime) || nowTime.equals(judgeTime)) {
        nowTime = nowTime.plusDays(1)
        val newDay = DF.format(nowTime.plusDays(1))
        updateCache(connection, newDay)
      }

      connection.close()
      dataSource.close()
    }

    private def updateCache(connection: Connection, day: String): Unit = {
      var preId = Integer.MIN_VALUE
      var count = 0
      val preparedStatement: PreparedStatement = connection.prepareStatement("SELECT `id`, `itemId`, `itemType`, `day` FROM Tc_Feature_Task WHERE `day` = ? and `id` > ? ORDER BY `id` ASC LIMIT 500000")
      do {
        var res = 0
        preparedStatement.setString(1, day)
        preparedStatement.setInt(2, preId)
        val rs: ResultSet = preparedStatement.executeQuery()
        while (rs.next()) {
          preId = Math.max(rs.getInt("id"), preId)
          // LOG.info("=======key: {}", rs.getString("day") + "_" + rs.getString("itemId"))
          cache.put(rs.getString("day") + "_" + rs.getString("itemId"), rs.getInt("id"))
          res += 1
        }
        count = res
      } while (count == 500000)

      // LOG.info("========count: {}", count)

      preparedStatement.close()
    }

    override def close(): Unit = {
      scheduledExecutorService.shutdown()
    }
  }
}
