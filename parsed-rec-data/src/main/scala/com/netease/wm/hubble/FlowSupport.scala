package com.netease.wm.hubble

import com.fasterxml.jackson.core.JsonParser.Feature
import com.netease.wm.hubble.common.recConfig
import com.netease.wm.util.Args
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, _}
import org.apache.flink.types.{Row, RowKind}
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerRecord
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, _}
import org.slf4j.{Logger, LoggerFactory}

import java.nio.charset.StandardCharsets
import java.util.Properties
import scala.util.control.NonFatal

private class FlowSupport{}

/* function: stat the recommend dis effect
*  author:   wangjun
*  index :   http://doc.hz.netease.com/pages/viewpage.action?pageId=296032399
* */
object FlowSupport {
  private val INPUT_TOPIC = "rec_upload_action_parse"
  private val ACTION_CODE_SET = Set(0, 200, 101, 201, 107, 108, 109, 124, 202, -108, -109, -124, -202)
  private val DISCOVERY_ACTION_CODE_SET = Set(0, 107, 108, 109, 124, -108, -109, -124)
  private val NOTE_ACTION_CODE_SET = Set(107, 108, 109, 124, -108, -109, -124)
  private val RELATED_ITEM_ACTION_CODE_SET = Set(200, 202, -202)

  val logger: Logger = LoggerFactory.getLogger(classOf[FlowSupport])

  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val GROUP_ID = params.optional("group").getOrElse("lofter_rec_dis_task_scene_gy")
    val RUN_MODE = params.optional("mode").getOrElse("test")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    env.enableCheckpointing(2 * 60 * 1000L)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setCheckpointTimeout(600 * 1000L)

    // config for kafka data source
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", recConfig.BOOTSTRAP_SERVERS_ONLINE)
    properties.setProperty("group.id", GROUP_ID)
    // properties.setProperty("auto.offset.reset", "earliest")
    // set this config for kafka Topic partitions change, flink can auto discover
    properties.setProperty("flink.partition-discovery.interval-millis", "60000")

    val mdaSource: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](INPUT_TOPIC, new SimpleStringSchema(), properties)

    val recEvents: DataStream[RecommendEvent] = env.addSource(mdaSource.setStartFromGroupOffsets())
      .flatMap(line => parseRecommendEvent(line))
      .filter(e=> e.flowTaskId >0 && e.sceneType >= 0)
      .keyBy(e => e.flowTaskId -> e.flowTaskType)
      .process(new RecommendItemTagProcessFunction)
      .uid("rec_event_dis")

    tableEnv.createTemporaryView("recEventDis", recEvents, 'flowTaskId, 'flowTaskType, 'sceneType,'actionCode,'text,'dt,'dh,'isProjectNewItem, 'isSceneNewItem,'pt.proctime)

    val hourSql =
      """
        |select flowTaskId,flowTaskType,sceneType,dt,dh,
        |    sum(case when actionCode in(0, 200) then 1 else 0 end) as exposurePv,
        |    sum(case when (actionCode in(101) and text='else') or actionCode in(201) then 1 else 0 end) as clickPv,
        |    sum(case when actionCode in(107, 108, 109, 124, 202)  then 1
        |            when actionCode in(-108, -109, -124, -202)  then -1
        |            else 0 end) as realHotCount,
        |    sum(isProjectNewItem) projectItemCount,
        |    sum(isSceneNewItem) projectSceneItemCount
        |from recEventDis
        |group by flowTaskId,flowTaskType,sceneType,dt,dh
      """.stripMargin

    val configuration = tableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "10 s")
    configuration.setString("table.exec.mini-batch.size", "10000")
    configuration.setBoolean("table.exec.emit.early-fire.enabled", true)
    configuration.setString("table.exec.emit.early-fire.delay", "60000 ms")

    tableEnv.getConfig.setIdleStateRetentionTime(Time.hours(30), Time.hours(48))

    val outTopic = "rec_dispatch_realtime_data"
    val flowHourlyEffectSink = new FlinkKafkaProducer[FlowHourlyEffect](outTopic, new FlowHourlyEffectSerialization(outTopic), properties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)

    val flowEffects = tableEnv.sqlQuery(hourSql).toChangelogStream
      .filter(new FilterFunction[Row] {
        override def filter(value: Row): Boolean = {
          value.getKind == RowKind.INSERT || value.getKind == RowKind.UPDATE_AFTER
        }
      })
      .map{ x =>
        FlowHourlyEffect(x.getField("flowTaskId").asInstanceOf[Long],x.getField("flowTaskType").asInstanceOf[Int],
          x.getField("sceneType").asInstanceOf[Int],x.getField("dt").asInstanceOf[Long],x.getField("dh").asInstanceOf[Int],
          x.getField("exposurePv").asInstanceOf[Int],x.getField("clickPv").asInstanceOf[Int],x.getField("realHotCount").asInstanceOf[Int],
          x.getField("projectItemCount").asInstanceOf[Int], x.getField("projectSceneItemCount").asInstanceOf[Int])
      }

    flowEffects.addSink(flowHourlyEffectSink)
    env.execute(s"lofter recommend dis stat $RUN_MODE")
  }

  case class RecommendEvent(flowTaskId: Long,
                            flowTaskType: Int, // 0: normal flow task  1: creator support task
                            sceneType: Int,
                            actionCode: Int,
                            text: String,
                            occurTime: Long,
                            dt: Long,
                            dh: Int,
                            isProjectNewItem: Int = 0,
                            isSceneNewItem: Int = 0
                           )

  case class FlowHourlyEffect(flowTaskId: Long,
                              flowTaskType: Int, // 0: normal flow task  1: creator support task
                              sceneType: Int,
                              dt: Long,
                              dh: Int,
                              exposurePv: Long,
                              clickPv: Long,
                              realHotCount: Long,
                              projectItemCount: Long,
                              projectSceneItemCount: Long
                             )


  def parseRecommendEvent(line: String): Option[RecommendEvent] = {
    implicit val formats: DefaultFormats = DefaultFormats
    mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)

    try {
      val bodyJson = parse(line)
      val actionCode = (bodyJson \ "rating").extractOrElse(-1)
      val algInfo = (bodyJson \ "algInfo").extractOrElse("else")

      if (ACTION_CODE_SET.contains(actionCode) && (algInfo.contains("taskId") || algInfo.contains("supportId"))){

        val extraData = (bodyJson \ "extraData").extractOrElse("")
        val repeat = (parse(extraData) \ "repeat").extractOrElse(2)
        val relatedItemType = (parse(extraData) \ "relatedItemType").extractOrElse("tag")
        val appVersion = (bodyJson \ "appVersion").extractOrElse("0")

        if (repeat == 0  && versionCompare(appVersion,"6.11.2") >= 0) {
          val scene = (bodyJson \ "scene").extractOpt[String].filter(_.nonEmpty).getOrElse("else")
          // For click actions, when click on tag of card, text value is the tag name.
          // tag click action should be filtered out, we only need post click actions
          val text = (bodyJson \ "text").extractOpt[String].filter(_.nonEmpty).getOrElse("else")
          val taskId = (parse(algInfo) \ "taskId").extractOrElse(0L)
          val supportId = (parse(algInfo) \ "supportId").extractOrElse(0L)
          val itemId = (bodyJson \ "itemId").extractOpt[String].filter(_.nonEmpty).getOrElse("else")

          val kafkaTime = (bodyJson \ "time").extractOrElse(0L)
          val eventTime = new DateTime(kafkaTime)
          val dt = eventTime.toString("yyyyMMdd").toLong
          val dh = eventTime.toString("yyyyMMdd-HH").split("-")(1).toInt

          val sceneType = if (scene.equalsIgnoreCase("feed_rec") && (DISCOVERY_ACTION_CODE_SET.contains(actionCode)
            || (actionCode==101 && text.equalsIgnoreCase("else")))) {
            0
          } else if (RELATED_ITEM_ACTION_CODE_SET.contains(actionCode) || (actionCode==201 && !relatedItemType.equalsIgnoreCase("tag"))
            || (NOTE_ACTION_CODE_SET.contains(actionCode) && scene.equalsIgnoreCase("related_item")) ) {
            1
          } else -1

          val recPostItemId = if(sceneType != 1 && supportId > 0) itemId else text

          val flowTaskId = if(supportId > 0) supportId else taskId
          val flowTaskType = if(supportId > 0) 1 else 0

          val res = RecommendEvent(flowTaskId,flowTaskType,sceneType,actionCode,recPostItemId,kafkaTime,dt,dh)
          Some(res)
        } else {
          None
        }

      } else {
        None
      }
    } catch {
      case NonFatal(_) =>
        logger.info(s"error parsing mda recommend behavior log event: $line")
        None
    }
  }

  def versionCompare(v1: String, v2: String): Integer = {
    if(v1 == null || v2 == null || v1.matches(".*[A-Za-z]+.*") || v2.matches(".*[A-Za-z]+.*")) {
      -1
    } else if(v1.isEmpty || v2.isEmpty) {
      Integer.valueOf(v1.compareTo(v2))
    } else {
      val result = v1.split("\\.").filter(_.nonEmpty)
        .zip(v2.split("\\.").filter(_.nonEmpty))
        .filterNot(s => s._1 == s._2)
        .headOption
        .map {
          case (m1, m2) => if(m1.toInt > m2.toInt) 1 else -1
        }.getOrElse(0)
      Integer.valueOf(result)
    }
  }

  class FlowHourlyEffectSerialization(val defaultTopic: String) extends KafkaSerializationSchema[FlowHourlyEffect] {
    override def serialize(element: FlowHourlyEffect, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      implicit val formats = DefaultFormats
      new ProducerRecord[Array[Byte], Array[Byte]](defaultTopic, write(element).getBytes(StandardCharsets.UTF_8))
    }
  }

  class RecommendItemTagProcessFunction extends KeyedProcessFunction[(Long, Int), RecommendEvent, RecommendEvent] {
    lazy val itemState: MapState[String, Int] = getRuntimeContext.getMapState(new MapStateDescriptor[String, Int]("items", createTypeInformation[String], createTypeInformation[Int]))

    override def processElement(event: RecommendEvent, ctx: KeyedProcessFunction[(Long, Int), RecommendEvent, RecommendEvent]#Context, out: Collector[RecommendEvent]): Unit = {
      // sceneType: 0 1 -1, encoded into itemState value by mapping to 1 << (v + 1)
      val itemId = event.text
      val scene = event.sceneType
      val result = if(itemId.length > 0 && itemId != "else") {

        val state = itemState.get(itemId)
        val isProjectNewItem = if(state == 0) 1 else 0
        val isSceneNewItem = if((state & 1 << (scene + 1)) == 0) 1 else 0
        val newState = 1 << (scene + 1) | state

        if(newState != state) itemState.put(itemId, newState)
        event.copy(isProjectNewItem = isProjectNewItem, isSceneNewItem = isSceneNewItem)
      } else event

      out.collect(result)
    }
  }

}
