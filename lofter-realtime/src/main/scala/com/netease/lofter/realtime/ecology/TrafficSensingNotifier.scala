package com.netease.lofter.realtime.ecology

import com.alibaba.druid.pool.DruidDataSource
import com.netease.lofter.realtime.common.kafkaConfig
import com.netease.wm.hubble.avro.{TrafficSensingAttributionNotify, TrafficSensingItemFeature, TrafficSensingPotentialNotify}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import javax.sql.DataSource
import scala.util.control.NonFatal

private class TrafficSensingNotifier{}

/**
 * 流量感知实时通知
 * 0 ~ 24H: potential, hot > top 20% percentile hot of same blog level and post ip
 * T+3 ~ T+7: attribution, 3 rules: top recommend(10%)、share(20%)、hot percentile(50%)
 */
object TrafficSensingNotifier {
  val LOG: Logger = LoggerFactory.getLogger(classOf[TrafficSensingNotifier])

  val START_TIME = DateTime.parse("2023-10-19").withHourOfDay(15).withMinuteOfHour(0).getMillis

  val BLOG_BLACK_LIST: Set[Long] = Set(1994829606L) // temporarily filter out ai blogs

  val JDBC_URL = "jdbc:mysql://lofter-rds-common-recomment-online-gz-34726.rds.cn-gz-p1.internal.:3331/recomment?useUnicode=true&characterEncoding=UTF-8&user=lofter_bi_gy&password=q4W0Kf_@I"

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getConfig.enableObjectReuse()

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().withHourOfDay(18).withMinuteOfHour(50).getMillis

    val eventSumSource = KafkaSource.builder[TrafficSensingItemFeature]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("rec.item.feature")
      .setGroupId("traffic_sensing_notifier_v3_GY")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[TrafficSensingItemFeature])
      .build()

    val potentialNotifySink = KafkaSink.builder[TrafficSensingPotentialNotify]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter.creator.potential")
          .setValueSerializationSchema(new AvroJsonSerSchema[TrafficSensingPotentialNotify])
          .build()
      ).build()

    val attributionNotifySink = KafkaSink.builder[TrafficSensingAttributionNotify]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("lofter.creator.attribution")
          .setValueSerializationSchema(new AvroJsonSerSchema[TrafficSensingAttributionNotify])
          .build()
      ).build()

    val eventSum = env.fromSource(eventSumSource, WatermarkStrategy.noWatermarks(), "trace-product-sum")

    eventSum
      .filter { e =>
        val now = System.currentTimeMillis()
        e.isInRecommendPool > 0 && now > e.publishTime && now - e.publishTime < 24 * 3600L * 1000L && e.publishTime >= START_TIME
      }
      .keyBy(_.itemId)
      .process(new PotentialNotifyRuleExaminingFunction)
      .sinkTo(potentialNotifySink)

    eventSum
      .filter { e =>
        val now = System.currentTimeMillis()
        e.isInRecommendPool > 0 && now - e.publishTime >= 3 * 24 * 3600L * 1000L && now - e.publishTime <= 7 * 24 * 3600L * 1000L && e.publishTime >= START_TIME
      }
      .keyBy(_.itemId)
      .process(new AttributionNotifyRuleExaminingFunction)
      .sinkTo(attributionNotifySink)

    eventSum
      .filter { e =>
        val now = System.currentTimeMillis()
        e.isInRecommendPool > 0 && now - e.publishTime >= 3 * 24 * 3600L * 1000L && now - e.publishTime <= 7 * 24 * 3600L * 1000L && e.publishTime >= START_TIME
      }
      .keyBy(e => e.itemId.split("_").head)
      .process(new AttributionLightHouseFunction)
      .sinkTo(attributionNotifySink)

    env.execute("traffic sensing notifier v3")
  }

  class AttributionLightHouseFunction extends KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingAttributionNotify] {
    lazy val notifyTimeState: MapState[Long, Long] = getRuntimeContext.getMapState(new MapStateDescriptor[Long, Long]("attribution-notify-time", createTypeInformation[Long], createTypeInformation[Long]))

    lazy val blogMetricState: MapState[Int, Long] = {
      val stateDescriptor = new MapStateDescriptor[Int, Long]("user-metrics", createTypeInformation[Int], createTypeInformation[Long])
      stateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(14)).updateTtlOnCreateAndWrite().build())
      getRuntimeContext.getMapState(stateDescriptor)
    }

    override def processElement(value: TrafficSensingItemFeature, ctx: KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingAttributionNotify]#Context, out: Collector[TrafficSensingAttributionNotify]): Unit = {
      val recommendExposure: Long = value.recommendExposure.getOrElse(0)
      val response: Long = value.response.getOrElse(0)
      val collectionBrowse: Long = value.collectionBrowse.getOrElse(0)
      val itemId: String = value.itemId

      val itemIdParts = itemId.split("_")
      val blogIdHex = itemIdParts.head
      val postIdHex = itemIdParts.tail.head
      val blogId = java.lang.Long.parseLong(blogIdHex, 16)
      val postId = java.lang.Long.parseLong(postIdHex, 16)
      var attributionType: Int = 0

      if (notifyTimeState.get(postId) <= 0) {
//        if (recommendExposure >= 10 &&
//          recommendExposure >= value.recommend.toDouble * 1.2) {
//          attributionType = 5
//        }

        val newResponseHigh = if (response >= blogMetricState.get(6)) {
          blogMetricState.put(6, response)
          1
        } else 0

        if (attributionType == 0 && response >= 10 && newResponseHigh > 0) {
          attributionType = 6
        }

        val newCollectionBrowseHigh = if (collectionBrowse >= blogMetricState.get(7)) {
          blogMetricState.put(7, collectionBrowse)
          1
        } else 0

        if (attributionType == 0 && collectionBrowse >= 50 && newCollectionBrowseHigh > 0) {
          attributionType = 7
        }

        if (attributionType > 0 && !BLOG_BLACK_LIST.contains(blogId)) {
          val notify = TrafficSensingAttributionNotify(
            postId, blogId, 100, value.exposure,
            attributionType, Option(value.ip), Option(value.blogLevel), Option(ctx.timestamp()),
            Option(value.hot), Option(value.recommend), Option(value.share), value.response,
            value.collectionBrowse, value.recommendExposure
          )

          out.collect(notify)
          notifyTimeState.put(postId, ctx.timestamp())
        }
      }
    }
  }

  case class RuleEntry(blog_level: String, main_ip: String, push_type: String, action_type: String, thresholds: Long)
  case class RuleKey(blog_level: String, main_ip: String, push_type: String, action_type: String)
  case class PercentileKey(blog_level: String, main_ip: String, dataType: String)
  case class LighthouseRuleEntry(blog_level: String, click_count_threshold: Long)

  def convertRuleBlogLevel(level: String): String = {
    level match {
      case "S" | "A" | "B" => "S-B"
      case "C" | "D" | "D*" => level
      case _ => "NEW"
    }
  }

  val RULES_UPDATE_INTERVAL: Long = 30 * 60 * 1000L

  class PotentialNotifyRuleExaminingFunction extends KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingPotentialNotify] {
    lazy val notifyTimeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("potential-notify-time", createTypeInformation[Long]))
    lazy val lighthouseNotifyTimeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("lighthouse-potential-notify-time", createTypeInformation[Long]))

    override def open(parameters: Configuration): Unit = {
      updateRules()
      rulesLastUpdateTime = System.currentTimeMillis() + RULES_UPDATE_INTERVAL
    }

    def updateRuleIfNeeded(): Unit = {
      val nextUpdateTime = (System.currentTimeMillis() / RULES_UPDATE_INTERVAL + 1) * RULES_UPDATE_INTERVAL
      if(nextUpdateTime > rulesLastUpdateTime) {
        updateRules()
        rulesLastUpdateTime = nextUpdateTime
      }
    }

    def getThreshold(ruleKey: RuleKey): Long = {
      val ruleResult: Option[Long] = (ruleKey.main_ip, ruleKey.blog_level) match {
        case ("_small_ip_", "NEW" | "D" | "D*") => Some(20)
        case ("_small_ip_", _) => Some(75)
        case _ => rules.get(ruleKey)
      }
      val finalResult: Option[Long] = if(ruleResult.isEmpty) {
        ruleKey.blog_level match {
          case "NEW" => Some(20)
          case "D" | "D*" => Some(40)
          case "C" => Some(200)
          case "S-B" => Some(1000)
          case _ => None
        }
      } else ruleResult
      finalResult.getOrElse(Long.MaxValue)
    }
    override def processElement(e: TrafficSensingItemFeature, ctx: KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingPotentialNotify]#Context, out: Collector[TrafficSensingPotentialNotify]): Unit = {
      updateRuleIfNeeded()

      val itemIdParts = e.itemId.split("_")
      val blogIdHex = itemIdParts.head
      val postIdHex = itemIdParts.tail.head
      val blogId = java.lang.Long.parseLong(blogIdHex, 16)
      val postId = java.lang.Long.parseLong(postIdHex, 16)

      val ruleBlogLevel = convertRuleBlogLevel(e.blogLevel)

      val ruleKey = RuleKey(ruleBlogLevel, e.ip, "first_push", "hot_top20")
      val threshold = getThreshold(ruleKey)
      if(!BLOG_BLACK_LIST.contains(blogId)) {
        if(e.hot > threshold && notifyTimeState.value() <= 0) {
          out.collect(
            TrafficSensingPotentialNotify(
              postId, blogId, Option(e.ip), Option(e.blogLevel), Option(ctx.timestamp()), Option(e.hot),
              Option(e.exposure), Option(e.recommend), Option(e.share)
            )
          )
          notifyTimeState.update(ctx.timestamp())
        }

        // lighthouse first push sensing
        val lighthouseRuleKey = if(e.blogLevel.isEmpty) "Default" else e.blogLevel
        if(e.exposure >= 100 && lighthouseNotifyTimeState.value() <= 0 && (e.click.getOrElse(0L) / e.exposure) >= 0.01 * lighthouseRules.getOrElse(lighthouseRuleKey, Long.MaxValue)) {
          out.collect(TrafficSensingPotentialNotify(
            postId, blogId, Option(e.ip), Option(e.blogLevel), Option(ctx.timestamp()), Option(e.hot),
            Option(e.exposure), Option(e.recommend), Option(e.share), scene = Some("lighthouse")
          ))
          lighthouseNotifyTimeState.update(ctx.timestamp())
        }
      }
    }

    @volatile var rules: Map[RuleKey, Long] = _
    @volatile var lighthouseRules: Map[String, Long] = _
    @volatile var rulesLastUpdateTime = 0L
    @transient private lazy val dataSource: DataSource = {
      LOG.debug("connecting jdbcUrl: {}", JDBC_URL)
      val ds = new DruidDataSource()
      ds.setDriverClassName("com.mysql.jdbc.Driver")
      ds.setUrl(JDBC_URL)
      ds.setLoginTimeout(5000)
      ds.setTestOnBorrow(true)
      ds.setTestOnReturn(true)
      ds.setFailFast(true)
      ds
    }

    private def updateRules(): Unit = {
      LOG.info("updating traffic sensing rules")

      implicit val conn: Connection = dataSource.getConnection()

      import com.netease.wm.util.Sql._
      try {
        val queryResult = sql"""select blog_level, main_ip, push_type, action_type, thresholds from rec_data_author_push_thresholds where push_type = 'first_push' """.query[RuleEntry]
        val newRules = queryResult.map { rule =>
          RuleKey(rule.blog_level, rule.main_ip, rule.push_type, rule.action_type) -> rule.thresholds
        }.toMap

        if(!newRules.equals(rules)&& newRules.nonEmpty) {
          rules = newRules
        }

        if(rules.isEmpty) {
          throw new RuntimeException("traffic sensing rules is empty")
        }

        // lighthouse rules update
        val queryResult2 = sql"""select blog_level, click_count_threshold from rec_data_lighthouse_push_thresholds""".query[LighthouseRuleEntry]

        if(queryResult2.nonEmpty) {
          lighthouseRules = queryResult2.map { e => e.blog_level -> e.click_count_threshold}.toMap
        }

        LOG.info("updating rules finished")
      } catch {
        case NonFatal(e) =>
          LOG.error("failed to update traffic sensing rules: {}", e)
      }
      finally {
        conn.close()
      }
    }

    override def close(): Unit = {
      if(dataSource != null && dataSource.isInstanceOf[java.io.Closeable]) {
        dataSource.asInstanceOf[java.io.Closeable].close()
      }
    }
  }

  class AttributionNotifyRuleExaminingFunction extends KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingAttributionNotify] {
    lazy val notifyTimeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("attribution-notify-time", createTypeInformation[Long]))

    private def getThreshold(level: String, ip: String, pushType: String, action: String): Long = {
      val baseRuleKey = RuleKey(level, ip, pushType, action)
      val ruleResult: Option[Long] = (ip, level, action) match {
        case ("_small_ip_", "NEW" | "D" | "D*", "recommend_top10") => Some(15)
        case ("_small_ip_", "NEW" | "D" | "D*", "share_top20") => Some(5)
        case ("_small_ip_", "NEW" | "D" | "D*", "hot_top50") => Some(20)
        case ("_small_ip_", "C" | "S-B", "recommend_top10") => Some(25)
        case ("_small_ip_", "C" | "S-B", "share_top20") => Some(5)
        case ("_small_ip_", "C" | "S-B", "hot_top50") => Some(150)
        case  ("_small_ip_", "NEW" | "D" | "D*", "hot_top20") => Some(50)
        case  ("_small_ip_", "C" | "S-B", "hot_top20") => Some(200)
        case _ => rules.get(baseRuleKey)
      }

      val finalResult: Option[Long] = if(ruleResult.isEmpty) {
        (level, action) match {
          case ("NEW", "recommend_top10") => Some(5)
          case ("NEW", "share_top20") => Some(5)
          case ("NEW", "hot_top50") => Some(20)
          case ("D" | "D*", "recommend_top10") => Some(10)
          case ("D" | "D*", "share_top20") => Some(5)
          case ("D" | "D*", "hot_top50") => Some(30)
          case ("C", "recommend_top10") => Some(35)
          case ("C", "share_top20") => Some(5)
          case ("C", "hot_top50") => Some(90)
          case ("S-B", "recommend_top10") => Some(180)
          case ("S-B", "share_top20") => Some(5)
          case ("S-B", "hot_top50") => Some(500)
          case ("NEW" | "D" | "D*", "hot_top20") => Some(50)
          case ("C", "hot_top20") => Some(200)
          case ("S-B", "hot_top20") => Some(1000)
          case _ => None
        }
      } else ruleResult
      finalResult.getOrElse(Long.MaxValue)
    }

    private def getAvgPercentile(value: Long, prevPercentile: Double, prevValue: Long, nextPercentile: Double, nextValue: Long): Double = {
      if(value == nextValue) return nextPercentile
      if(prevValue == nextValue || value == prevValue) return prevPercentile
      val result = prevPercentile + ((value - prevValue) * 1.0 / (nextValue - prevValue)) * (nextPercentile - prevPercentile)
      Math.max(Math.min(1.0, result), .0)
    }

    private def getPercentile(level: String, ip: String, dataType: String, value: Long): Double = {
      if(ip == "_small_ip_") return 0.8
      val percentileKey = PercentileKey(level, ip, dataType)
      val localPercentiles = percentiles
      if(!localPercentiles.contains(percentileKey)) return 0.8
      val data = localPercentiles(percentileKey)
      val index = data.indexWhere { pair => pair._2 <= value}
      index match {
        case 0 => 1
        case -1 => 0.5
        case _ => getAvgPercentile(value, data(index)._1, data(index)._2, data(index -1)._1, data(index -1 )._2)
      }
    }

    override def open(parameters: Configuration): Unit = {
      updateRules()
      updatePercentiles()
      rulesLastUpdateTime = System.currentTimeMillis() + RULES_UPDATE_INTERVAL
    }

    def updateRuleIfNeeded(): Unit = {
      val nextUpdateTime = (System.currentTimeMillis() / RULES_UPDATE_INTERVAL + 1) * RULES_UPDATE_INTERVAL
      if(nextUpdateTime > rulesLastUpdateTime) {
        updateRules()
        updatePercentiles()
        rulesLastUpdateTime = nextUpdateTime
      }
    }

    override def processElement(e: TrafficSensingItemFeature, ctx: KeyedProcessFunction[String, TrafficSensingItemFeature, TrafficSensingAttributionNotify]#Context, out: Collector[TrafficSensingAttributionNotify]): Unit = {
      updateRuleIfNeeded()

      if(notifyTimeState.value() <= 0) {
        val ruleBlogLevel = convertRuleBlogLevel(e.blogLevel)
        val subRule1RecommendThreshold = getThreshold(ruleBlogLevel, e.ip, "second_push,a", "recommend_top10")
        val subRule1HotThreshold = getThreshold(ruleBlogLevel, e.ip, "second_push,a", "hot_top50")
        val subRule2ShareThreshold = getThreshold(ruleBlogLevel, e.ip, "second_push,b", "share_top20")
        val subRule2HotThreshold = getThreshold(ruleBlogLevel, e.ip, "second_push,b", "hot_top50")
        val subRule4HotThreshold = getThreshold(ruleBlogLevel, e.ip, "second_push,c", "hot_top20")
        val attributionType = if(e.recommend >= subRule1RecommendThreshold && e.hot >= subRule1HotThreshold) {
          1
        } else if(e.share >= subRule2ShareThreshold && e.hot >= subRule2HotThreshold) {
          2
        } else if(e.hot >= subRule4HotThreshold) {
          4
        } else 0

        if(attributionType > 0) {
          val itemIdParts = e.itemId.split("_")
          val blogIdHex = itemIdParts.head
          val postIdHex = itemIdParts.tail.head
          val blogId = java.lang.Long.parseLong(blogIdHex, 16)
          val postId = java.lang.Long.parseLong(postIdHex, 16)
          val percentile = attributionType match {
            case 1 => getPercentile(ruleBlogLevel, e.ip, "recommend", e.recommend)
            case 2 => getPercentile(ruleBlogLevel, e.ip, "share", e.share)
            case 4 => getPercentile(ruleBlogLevel, e.ip, "hot", e.hot)
            case _ => 0.8
          }

          if(!BLOG_BLACK_LIST.contains(blogId)) {
            out.collect(
              TrafficSensingAttributionNotify(
                postId, blogId, (percentile * 100).toLong , e.exposure,
                attributionType, Option(e.ip), Option(e.blogLevel), Option(ctx.timestamp()),
                Option(e.hot), Option(e.recommend), Option(e.share)
              )
            )
            notifyTimeState.update(ctx.timestamp())
          }
        }
      }
    }

    @volatile var rules: Map[RuleKey, Long] = _
    @volatile var percentiles: Map[PercentileKey, List[(Double, Long)]] = _
    @volatile var rulesLastUpdateTime = 0L
    @transient private lazy val dataSource: DataSource = {
      LOG.debug("connecting jdbcUrl: {}", JDBC_URL)
      val ds = new DruidDataSource()
      ds.setDriverClassName("com.mysql.jdbc.Driver")
      ds.setUrl(JDBC_URL)
      ds.setLoginTimeout(5000)
      ds.setTestOnBorrow(true)
      ds.setTestOnReturn(true)
      ds.setFailFast(true)
      ds
    }

    private def updateRules(): Unit = {
      LOG.info("updating traffic sensing rules")

      implicit val conn: Connection = dataSource.getConnection()

      import com.netease.wm.util.Sql._
      try {
        val queryResult = sql"""select blog_level, main_ip, push_type, action_type, thresholds from rec_data_author_push_thresholds where push_type like 'second_push%' """.query[RuleEntry]
        val newRules = queryResult.map { rule =>
          RuleKey(rule.blog_level, rule.main_ip, rule.push_type, rule.action_type) -> rule.thresholds
        }.toMap

        if(newRules.nonEmpty) {
          rules = newRules
        }

        LOG.info("updating rules finished")

        if(rules.isEmpty) {
          throw new RuntimeException("traffic sensing rules is empty")
        }
      } catch {
        case NonFatal(e) =>
          LOG.error("failed to update traffic sensing rules: {}", e)
      }
      finally {
        conn.close()
      }
    }
    private def updatePercentiles(): Unit = {
      LOG.info("updating traffic sensing percentiles")

      implicit val conn: Connection = dataSource.getConnection()

      import com.netease.wm.util.Sql._
      try {
        val queryResult = sql"""select blog_level, main_ip, push_type, action_type, thresholds from rec_data_author_push_thresholds where push_type like 'percentile%' """.query[RuleEntry]
        val parts = queryResult.map { rule =>
          PercentileKey(rule.blog_level, rule.main_ip, rule.push_type.split(":").tail.head) -> (rule.action_type.toDouble, rule.thresholds)
        }

        val newPercentiles = parts.groupBy(_._1).mapValues { vs =>
          vs.map(_._2).sortBy{ e => -e._1}.toList
        }

        if(newPercentiles.nonEmpty) {
          percentiles = newPercentiles
        }

        LOG.info("updating percentiles finished")

        LOG.info("percentiles samples: {}", percentiles.take(10))

        if(percentiles.isEmpty) {
          throw new RuntimeException("traffic sensing percentiles is empty")
        }
      } catch {
        case NonFatal(e) =>
          LOG.error("failed to update traffic sensing percentiles: {}", e)
      }
      finally {
        conn.close()
      }
    }
  }
}
