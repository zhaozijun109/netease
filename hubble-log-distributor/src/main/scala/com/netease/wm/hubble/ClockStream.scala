package com.netease.wm.hubble

import com.netease.wm.hubble.common.kafkaConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer

import java.util.Properties

object ClockStream {
  val CLOCK_INTERVAL = 60 * 1000
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val outTopic = args(0)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
    properties.setProperty("linger.ms", "0")
    properties.setProperty("batch.size", "1")

    val timer = env.addSource(new SourceFunction[String] {
      override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
        val currentTime = System.currentTimeMillis()
        val nextPoint = (currentTime / CLOCK_INTERVAL + 1) * CLOCK_INTERVAL
        Thread.sleep( nextPoint - currentTime)

        while(true) {
          val currentTime = System.currentTimeMillis()
          sourceContext.collect(currentTime.toString)

          val nextPoint = (currentTime / CLOCK_INTERVAL + 1) * CLOCK_INTERVAL
          Thread.sleep(nextPoint - currentTime)
        }
      }

      override def cancel(): Unit = {}
    })

    val sink = new FlinkKafkaProducer[String](outTopic, new SimpleStringSchema, properties)
    timer.addSink(sink)
    env.execute("ClockStream")
  }
}
