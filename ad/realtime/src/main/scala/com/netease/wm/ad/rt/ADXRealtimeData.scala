package com.netease.wm.ad.rt

import com.netease.wm.ad.rt.common.dbConfig
import okhttp3.HttpUrl
import org.apache.flink.api.common.serialization.{Encoder, SimpleStringSchema}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig}
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.{Time => STime}
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import java.io.OutputStream
import java.sql.Timestamp
import java.sql.Types.{BIGINT, VARCHAR}
import java.util.Properties
import scala.util.Try
import scala.util.control.NonFatal
import scala.util.matching.Regex

private class ADXRealtimeData{}

object ADXRealtimeData {

  case class AdEvent(app: String, deviceUdid: String, deviceOs: String, adId: String, adSource: String, requestUuid: String, adPosition: Long, time: Long, click: Long, expose: Long, appVersion: String)

  case class AdEventUvEntry(app: String, deviceUdid: String, deviceOs: String, adId: String, adSource: String, adPosition: Long, wt: Timestamp,
                            click: Long, expose: Long, dayNew: Int, hourNew: Int, requestNew: Int, dt: Long, dh: Long)

  val LOG_PATTERN: Regex = """^\d+\.\d+\.\d+\.\d+ -\s+-\s+\[(.+?)] "GET (.+?) HTTP.+?".*$""".r
  val NEW_LOG_PATTERN: Regex = """^\s+-\s+-\s+\[(.+?)] "GET (.+?) HTTP.+?".*$""".r

  val SIGN_SALT: String = "lU9FonIQfQkptK8ze25LrDodECjeTjZ0FIgvO3f4L"

  //sign=md5(固定密串+adCallTime+adId+adPosition+adSource+app+deviceId+os+uuid+version).toLowerCase()
  def verifySign(event: AdEvent, sign: String): Boolean = {
    import event._
    val m = java.security.MessageDigest.getInstance("MD5")
    m.reset()
    val digest = m.digest(s"${SIGN_SALT}$time$adId$adPosition$adSource$app$deviceUdid$deviceOs$requestUuid$appVersion".getBytes)
    val sb: StringBuffer = new StringBuffer
    digest.foreach { b =>
      sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3))
    }
    sb.toString.toLowerCase.equals(sign)
  }

  def parseAdEvent(requestUrl: String): Option[AdEvent] = {
    val url = HttpUrl.parse("http://ad.mh.163.com" + requestUrl)

    if(url == null) {
      LOG.error(s"parse url error for: $requestUrl")
      None
    } else {
      val path = url.encodedPath()

      // only ad events
      val (click, expose) = path match {
        case "/m/c" | "/mf/c" => (1, 0)
        case "/m/e" | "/mf/e" => (0, 1)
        case _ => (0, 0)
      }

      if(click != 0 || expose != 0) {
        try {
          val app = url.queryParameter("app")
          val adId = url.queryParameter("adId")
          val adSource = url.queryParameter("adSource")
          val adPosition = url.queryParameter("adPosition")
          val deviceOs = url.queryParameter("os").replaceAll("[^\\w]+", "")
          val deviceUdid = url.queryParameter("deviceId")
          val adCallTime = url.queryParameter("adCallTime").toLong
          val requestUuid = url.queryParameter("uuid")
          val version = url.queryParameter("version")
          val sign = url.queryParameter("sign")

          val isEventInRange = (System.currentTimeMillis() - adCallTime) < 29*3600*1000L

          if(isEventInRange && adId.startsWith("WCAD") && adPosition.matches("\\d+") &&
            app != null && deviceUdid != null && deviceOs != null && adSource != null && requestUuid != null && version != null) {
            val adEvent = AdEvent(app, deviceUdid, deviceOs, adId, adSource, requestUuid, adPosition.toLong, adCallTime, click, expose, version)
            if(verifySign(adEvent, sign)) {
              Some(adEvent)
            } else {
              LOG.warn("ignore adx request event (sign verification failed or out of range): {}", requestUrl)
              None
            }
          } else None
        } catch {
          case NonFatal(e) =>
            LOG.error("Parsing error for adx request[{}]: {} {}", requestUrl, e: Throwable, "")
            None
        }
      } else None
    }
  }
  val LOG: Logger = LoggerFactory.getLogger(classOf[ADXRealtimeData])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
    val tableEnv = StreamTableEnvironment.create(env, bsSettings)

    env.enableCheckpointing(300000)
    env.getConfig.enableObjectReuse()

    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(180000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "10.196.9.206:9092,10.196.9.207:9092,10.196.9.208:9092")
    properties.setProperty("group.id", "adx_online_v8")
    properties.setProperty("auto.offset.reset", "earliest")
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val startTimeStamp = new DateTime().withTimeAtStartOfDay().getMillis

    val adSource = env.addSource(new FlinkKafkaConsumer[String]("adx.server.online", new SimpleStringSchema(), properties).setStartFromTimestamp(startTimeStamp))

    val adEvents: DataStream[AdEvent] = adSource.flatMap { (line: String, collector: Collector[AdEvent]) =>
      line match {
        case LOG_PATTERN(_, requestUrl) => parseAdEvent(requestUrl).foreach(collector.collect _)
        case NEW_LOG_PATTERN(_, requestUrl) => parseAdEvent(requestUrl).foreach(collector.collect _)
        case _ => // ignore
          LOG.error("wrong format log: {}", line)
      }
    }.filter(_.time >= startTimeStamp)

    val dailyAdStatJdbcOutput = JDBCOutputFormat.buildJDBCOutputFormat()
      .setDrivername("com.netease.lbd.LBDriver")
      .setDBUrl(dbConfig.yaoluJdbcUrl)
      .setQuery("insert into AD_StatDaily(id, appId, statTime, adId, exposureCount, clickCount, exposureUVCount, clickUVCount, createTime, updateTime, exposureUnique, clickUnique) values(seq, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on duplicate key update exposureCount = ?, clickCount = ?, exposureUVCount = ?, clickUVCount = ?, updateTime = ?, exposureUnique = ?, clickUnique = ?")
      .setSqlTypes(Array(VARCHAR, BIGINT, VARCHAR, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT))
      .setBatchInterval(1)
      .finish()

    val dailyAdDetailStatJdbcOutput = JDBCOutputFormat.buildJDBCOutputFormat()
      .setDrivername("com.netease.lbd.LBDriver")
      .setDBUrl(dbConfig.yaoluJdbcUrl)
      .setQuery("insert into AD_StatDetailDaily(id, appId, positionId, osName, dspId, statTime, adId, exposureCount, clickCount, exposureUVCount, clickUVCount, createTime, updateTime, exposureUnique, clickUnique) values(seq, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on duplicate key update exposureCount = ?, clickCount = ?, exposureUVCount = ?, clickUVCount = ?, updateTime = ?, exposureUnique = ?, clickUnique = ?")
      .setSqlTypes(Array(VARCHAR, BIGINT, VARCHAR, BIGINT, BIGINT, VARCHAR, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT))
      .setBatchInterval(1)
      .finish()

    val hourlyAdDetailStatJdbcOutput = JDBCOutputFormat.buildJDBCOutputFormat()
      .setDrivername("com.netease.lbd.LBDriver")
      .setDBUrl(dbConfig.yaoluJdbcUrl)
      .setQuery("insert into AD_StatDetailHourly(id, appId, positionId, osName, dspId, statTime, adId, exposureCount, clickCount, exposureUVCount, clickUVCount, createTime, updateTime) values(seq, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on duplicate key update exposureCount = ?, clickCount = ?, exposureUVCount = ?, clickUVCount = ?, updateTime = ?")
      .setSqlTypes(Array(VARCHAR, BIGINT, VARCHAR, BIGINT, BIGINT, VARCHAR, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT, BIGINT))
      .setBatchInterval(1)
      .finish()

    val adBase = adEvents
      .filter(_.app.length <= 32)
      .filter(_.adId.length <= 32)
      .keyBy(_.deviceUdid)
      .process(new UvOptimizedFunction)
      .uid("uv-opt-func")

    tableEnv.createTemporaryView("ad", adBase, 'app, 'deviceUdid, 'deviceOs, 'adId, 'adSource, 'adPosition, 'click, 'expose, 'dayNew, 'hourNew, 'dt, 'dh, 'requestNew, 'pt.proctime)

    val dailySql =
      """
        |select app, adId, dt,
        |         sum(click) clickPv, sum(expose) exposePv,
        |         sum(click * dayNew) clickUv, sum(expose * dayNew) exposeUv,
        |         sum(click * requestNew) clickUnique, sum(expose * requestNew) exposureUnique
        |from ad
        |group by app, adId, dt
      """.stripMargin

    val dailyDetailSql =
      """
        |select app, adId, deviceOs, adSource, adPosition, dt,
        |         sum(click) clickPv, sum(expose) exposePv,
        |         sum(click * dayNew) clickUv, sum(expose * dayNew) exposeUv,
        |         sum(click * requestNew) clickUnique, sum(expose * requestNew) exposureUnique
        |from ad
        |group by app, adId, deviceOs, adSource, adPosition, dt
      """.stripMargin

    val hourlyDetailSql =
      """
        |select app, adId, deviceOs, adSource, adPosition, dh,
        |         sum(click) clickPv, sum(expose) exposePv,
        |         sum(click * hourNew) clickUv, sum(expose * hourNew) exposeUv
        |from ad
        |group by app, adId, deviceOs, adSource, adPosition, dh
      """.stripMargin

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "10 s")
    configuration.setString("table.exec.mini-batch.size", "10000")
    configuration.setBoolean("table.exec.emit.early-fire.enabled", true)
    configuration.setString("table.exec.emit.early-fire.delay", "60000 ms")

    tableEnv.getConfig.setIdleStateRetentionTime(Time.hours(30), Time.hours(48))

    tableEnv.sqlQuery(dailySql).toRetractStream[(String, String, Long, Long, Long, Long, Long, Long, Long)]
      .filter(_._1)
      .map {x =>
        val (_, (app, adId, day, clickPv, exposePv, clickUv, exposeUv, clickUnique, exposureUnique)) = x
        val row = new Row(18)
        row.setField(0, app)
        row.setField(1,day)
        row.setField(2, adId)
        row.setField(3, exposePv)
        row.setField(4, clickPv)
        row.setField(5, exposeUv)
        row.setField(6, clickUv)
        row.setField(7, System.currentTimeMillis())
        row.setField(8, System.currentTimeMillis())
        row.setField(9, exposureUnique)
        row.setField(10, clickUnique)
        row.setField(11, exposePv)
        row.setField(12, clickPv)
        row.setField(13, exposeUv)
        row.setField(14, clickUv)
        row.setField(15, System.currentTimeMillis())
        row.setField(16, exposureUnique)
        row.setField(17, clickUnique)
        row
      }.writeUsingOutputFormat(dailyAdStatJdbcOutput)

    tableEnv.sqlQuery(dailyDetailSql).toRetractStream[(String, String, String, String, Long, Long, Long, Long, Long, Long, Long, Long)]
      .filter(_._1)
      .map {x =>
        val (_, (app, adId, deviceOs, adSource, adPosition, day, clickPv, exposePv, clickUv, exposeUv, clickUnique, exposureUnique)) = x
        val row = new Row(21)
        val dspId = Try{adSource.toLong}.getOrElse(0L)
        row.setField(0, app)
        row.setField(1, adPosition)
        row.setField(2, deviceOs)
        row.setField(3, dspId)
        row.setField(4, day)
        row.setField(5, adId)
        row.setField(6, exposePv)
        row.setField(7, clickPv)
        row.setField(8, exposeUv)
        row.setField(9, clickUv)
        row.setField(10, System.currentTimeMillis())
        row.setField(11, System.currentTimeMillis())
        row.setField(12, exposureUnique)
        row.setField(13, clickUnique)
        row.setField(14, exposePv)
        row.setField(15, clickPv)
        row.setField(16, exposeUv)
        row.setField(17, clickUv)
        row.setField(18, System.currentTimeMillis())
        row.setField(19, exposureUnique)
        row.setField(20, clickUnique)
        row
      }.writeUsingOutputFormat(dailyAdDetailStatJdbcOutput)

    tableEnv.getConfig.setIdleStateRetentionTime(Time.hours(30), Time.hours(48))

    tableEnv.sqlQuery(hourlyDetailSql).toRetractStream[(String, String, String, String, Long, Long, Long, Long, Long, Long)]
      .filter(_._1)
      .map {x =>
        val (_, (app, adId, deviceOs, adSource, adPosition, hour, clickPv, exposePv, clickUv, exposeUv)) = x
        val row = new Row(17)
        val dspId = Try{adSource.toLong}.getOrElse(0L)
        row.setField(0, app)
        row.setField(1, adPosition)
        row.setField(2, deviceOs)
        row.setField(3, dspId)
        row.setField(4, hour)
        row.setField(5, adId)
        row.setField(6, exposePv)
        row.setField(7, clickPv)
        row.setField(8, exposeUv)
        row.setField(9, clickUv)
        row.setField(10, System.currentTimeMillis())
        row.setField(11, System.currentTimeMillis())
        row.setField(12, exposePv)
        row.setField(13, clickPv)
        row.setField(14, exposeUv)
        row.setField(15, clickUv)
        row.setField(16, System.currentTimeMillis())
        row
      }.writeUsingOutputFormat(hourlyAdDetailStatJdbcOutput)

    env.execute("adx realtime day and hour stat to backend")
  }

  class UvOptimizedFunction extends KeyedProcessFunction[String, AdEvent, AdEventUvEntry] {
    private var adClickDailyState: MapState[String, Long] = _
    private var adClickHourlyState: MapState[String, Long] = _
    private var adExposeDailyState: MapState[String, Long] = _
    private var adExposeHourlyState: MapState[String, Long] = _
    private var adClickRequestState: MapState[String, Int] = _
    private var adExposeRequestState: MapState[String, Int] = _

    val dailyTtlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.days(2))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
      .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
      .build

    val hourlyTtlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.hours(2))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
      .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
      .build


    val requestTtlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.minutes(5))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
      .build

    override def open(parameters: Configuration): Unit = {
      val adClickDailyStateDescriptor = new MapStateDescriptor[String, Long]("device-ad-click-daily", createTypeInformation[String], createTypeInformation[Long])
      val adClickHourlyStateDescriptor = new MapStateDescriptor[String, Long]("device-ad-click-hourly", createTypeInformation[String], createTypeInformation[Long])
      val adExposeDailyStateDescriptor = new MapStateDescriptor[String, Long]("device-ad-expose-daily", createTypeInformation[String], createTypeInformation[Long])
      val adExposeHourlyStateDescriptor = new MapStateDescriptor[String, Long]("device-ad-expose-hourly", createTypeInformation[String], createTypeInformation[Long])

      val adClickRequestStateDescriptor = new MapStateDescriptor[String, Int]("device-ad-click-request", createTypeInformation[String], createTypeInformation[Int])
      val adExposeRequestStateDescriptor = new MapStateDescriptor[String, Int]("device-ad-expose-request", createTypeInformation[String], createTypeInformation[Int])

      adClickDailyStateDescriptor.enableTimeToLive(dailyTtlConfig)
      adClickHourlyStateDescriptor.enableTimeToLive(hourlyTtlConfig)
      adExposeDailyStateDescriptor.enableTimeToLive(dailyTtlConfig)
      adExposeHourlyStateDescriptor.enableTimeToLive(hourlyTtlConfig)
      adClickRequestStateDescriptor.enableTimeToLive(requestTtlConfig)
      adExposeRequestStateDescriptor.enableTimeToLive(requestTtlConfig)

      adClickDailyState = getRuntimeContext.getMapState(adClickDailyStateDescriptor)
      adClickHourlyState = getRuntimeContext.getMapState(adClickHourlyStateDescriptor)
      adExposeDailyState = getRuntimeContext.getMapState(adExposeDailyStateDescriptor)
      adExposeHourlyState = getRuntimeContext.getMapState(adExposeHourlyStateDescriptor)
      adClickRequestState = getRuntimeContext.getMapState(adClickRequestStateDescriptor)
      adExposeRequestState = getRuntimeContext.getMapState(adExposeRequestStateDescriptor)
    }

    override def processElement(value: AdEvent, ctx: KeyedProcessFunction[String, AdEvent, AdEventUvEntry]#Context, out: Collector[AdEventUvEntry]): Unit = {
      import value._
      val eventTime = new DateTime(time)

      val day = eventTime.toString("yyyyMMdd").toLong
      val hour = eventTime.toString("yyyyMMddHH").toLong

      val (dayNew, hourNew, requestNew) = if(click > 0) {
        val dn = if(!adClickDailyState.contains(adId) || adClickDailyState.get(adId) < day) 1 else 0
        val hn = if(!adClickHourlyState.contains(adId) || adClickHourlyState.get(adId) < hour) 1 else 0
        val rn = if(!adClickRequestState.contains(requestUuid)) 1 else 0

        if(dn > 0) {adClickDailyState.put(adId, day)}
        if(hn > 0) {adClickHourlyState.put(adId, hour)}
        if(rn > 0) {adClickRequestState.put(requestUuid, 1)}
        (dn, hn, rn)
      } else {
        val dn = if(!adExposeDailyState.contains(adId) || adExposeDailyState.get(adId) < day) 1 else 0
        val hn = if(!adExposeHourlyState.contains(adId) || adExposeHourlyState.get(adId) < hour) 1 else 0
        val rn = if(!adExposeRequestState.contains(requestUuid)) 1 else 0

        if(dn > 0) {adExposeDailyState.put(adId, day)}
        if(hn > 0) {adExposeHourlyState.put(adId, hour)}
        if(rn > 0) {adExposeRequestState.put(requestUuid, 1)}
        (dn, hn, rn)
      }


      out.collect(AdEventUvEntry(app, deviceUdid, deviceOs, adId, adSource, adPosition, new Timestamp(time), click, expose, dayNew, hourNew,requestNew, day, hour))
    }
  }

  class RowEncoder extends Encoder[Row] {
    override def encode(element: Row, stream: OutputStream): Unit = {
      val p = element
      val line = p.toString
      stream.write(line.getBytes("UTF-8"))
      stream.write('\n')
    }
  }

  class AdEventTimeExtractor(maxOutOfOrderness: STime) extends BoundedOutOfOrdernessTimestampExtractor[AdEvent](maxOutOfOrderness) {
    override def extractTimestamp(e: AdEvent): Long = e.time
  }
}
