package com.netease.lofter.realtime.growth

import com.github.nscala_time.time.Imports.DateTime
import com.netease.dts.common.subscribe.SubscribeEvent
import com.netease.dts.common.subscribe.SubscribeEvent.RowChangeType
import com.netease.lofter.realtime.common.{SubscribeEventSchema, kafkaConfig}
import com.netease.wm.hubble.common.avro.binary.AvroBinaryDeserSchema
import com.netease.wm.hubble.common.avro.json.AvroJsonSerSchema
import com.netease.wm.hubble.avro.{ClientMdaLogAvro => Mda}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

private class AppExchangeTask{}

object AppExchangeTask {
  val LOG: Logger = LoggerFactory.getLogger(classOf[AppExchangeTask])

  case class ExchangeTask(taskId: Long, userId: Long, deviceId: String, taskType: String, sourcePlatform: String)
  case class TaskCompletion(taskId: Long, deviceId: String)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(120000)
    env.getConfig.enableObjectReuse()
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(20000)
    env.getCheckpointConfig.setCheckpointTimeout(900000)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val mdaSource = KafkaSource.builder[Mda]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.mda.online")
      .setGroupId("app_exchange_task")
      .setProperty("flink.partition-discovery.interval-millis", "60000")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new AvroBinaryDeserSchema[Mda])
      .build()

    val binlogSource = KafkaSource.builder[SubscribeEvent]()
      .setBootstrapServers(kafkaConfig.GY_BOOTSTRAP_SERVERS)
      .setTopics("lofter.binlog.ndc")
      .setGroupId("app_exchange_task")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
      .setValueOnlyDeserializer(new SubscribeEventSchema)
      .build()

    val taskCompletionSink = KafkaSink.builder[String]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setBootstrapServers(kafkaConfig.BACKEND_BOOTSTRAP_SERVERS)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("LOFTER.EXCHANGE.BI.TASK.CALLBACK")
         .setValueSerializationSchema(new SimpleStringSchema())
         .build()
       ).setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    val exchangeTasks = env.fromSource(binlogSource, WatermarkStrategy.noWatermarks(), "binlog")
      .flatMap { (s: SubscribeEvent, collector: Collector[ExchangeTask]) =>
        s.getRowChanges.asScala
          .filter( s => s.getType == RowChangeType.INSERT || s.getType == RowChangeType.UPDATE)
          .foreach { row =>
            row.getTableName match {
              case "exchange_task" =>
                val taskId = row.getColumn("id").getNewValue.asInstanceOf[Long]
                val userId = row.getColumn("userId").getNewValue.asInstanceOf[Long]
                val deviceId = row.getColumn("deviceId").getNewValue.asInstanceOf[String]
                val taskType = row.getColumn("taskType").getNewValue.asInstanceOf[String]
                val sourcePlatform = row.getColumn("sourcePlatform").getNewValue.asInstanceOf[String]
                val updateTime = Option(row.getColumn("updateTime").getNewValue.asInstanceOf[java.sql.Timestamp]).map(_.getTime).getOrElse(0L)
                val oldUpdateTime = Option(row.getColumn("updateTime").getOldValue.asInstanceOf[java.sql.Timestamp]).map(_.getTime).getOrElse(0L)
                val status = row.getColumn("status").getNewValue.asInstanceOf[Int]

                if(sourcePlatform != "lofter" && status == 0) {
                  collector.collect(ExchangeTask(taskId, userId, deviceId, taskType, sourcePlatform))
                }
              case _ =>
            }
          }
      }

    val mdaEvents = env.fromSource(mdaSource, WatermarkStrategy.noWatermarks(), "mda")

    val taskCompletions = exchangeTasks.keyBy(_.deviceId)
      .connect(mdaEvents.filter(_.customUDID.isDefined).keyBy(_.customUDID.get))
      .process(new TaskCounting)
      .map { e =>
        implicit val format = DefaultFormats
        write(e)
      }

    taskCompletions.sinkTo(taskCompletionSink)

    env.execute("exchange task")
  }


  class TaskCounting extends KeyedCoProcessFunction[String, ExchangeTask, Mda, TaskCompletion] {
    lazy val taskState: ValueState[ExchangeTask] = getRuntimeContext.getState[ExchangeTask](new ValueStateDescriptor[ExchangeTask]("exchange-task", createTypeInformation[ExchangeTask]))
    lazy val taskTimerState: MapState[String, Long] = {
      val descriptor = new MapStateDescriptor[String, Long]("task-timer", createTypeInformation[String], createTypeInformation[Long])
      val ttl = StateTtlConfig.newBuilder(Time.hours(24)).updateTtlOnCreateAndWrite().build()
      descriptor.enableTimeToLive(ttl)
      getRuntimeContext.getMapState[String, Long](descriptor)
    }

    override def processElement1(value: ExchangeTask, ctx: KeyedCoProcessFunction[String, ExchangeTask, Mda, TaskCompletion]#Context, out: Collector[TaskCompletion]): Unit = {
      taskState.update(value)
    }

    override def processElement2(event: Mda, ctx: KeyedCoProcessFunction[String, ExchangeTask, Mda, TaskCompletion]#Context, out: Collector[TaskCompletion]): Unit = {
      val task = taskState.value()
      if(task == null) return

      task.taskType match {
        case "read_10s" =>
          task.sourcePlatform match{
            case "maoer" =>
              if (event.eventId.exists(_ == "m5-2") && event.itemId.exists (s => s == "57210295") ) {
              val currentValue = taskTimerState.get (task.taskType)
              val newValue = currentValue + event.costTime.getOrElse (0L)
              taskTimerState.put (task.taskType, newValue)

              if (newValue >= 10000) {
                taskState.update (null)
                out.collect (TaskCompletion (task.taskId, task.deviceId) )
              }
            }

            case "xiecheng" =>
              if (event.costTime.getOrElse(0L) > 0) {
                val currentValue = taskTimerState.get(task.taskType)
                val newValue = currentValue + event.costTime.getOrElse(0L)
                taskTimerState.put(task.taskType, newValue)

                if (newValue >= 10000) {
                  taskState.update(null)
                  out.collect(TaskCompletion(task.taskId, task.deviceId))
                }
              }

            case _ => // ignore
          }
        case _ => // ignore
      }
    }
  }
}
