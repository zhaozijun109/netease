package com.netease.wm.hubble.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.api.common.serialization.Encoder
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer

import java.io.OutputStream
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

case class HubbleEvent(topic: String, time: Long, content: String)

class HubbleEventEncoder extends Encoder[HubbleEvent] {
  override def encode(in: HubbleEvent, outputStream: OutputStream): Unit = {
    outputStream.write(in.content.getBytes("utf-8"))
    outputStream.write(10)
  }
}

case class HubbleEventBucketAssigner(dateFormat: String) extends BucketAssigner[HubbleEvent, String]{
  private val serialVersionUID = 1L
  @transient private var formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

  override def getBucketId(in: HubbleEvent, context: BucketAssigner.Context): String = {
    if(formatter == null) {
      formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
    }

    val eventTime = in.time

    val bucketPartDt = formatter.format(Instant.ofEpochMilli(eventTime))
    val bucketDirPart = in.topic

    s"$bucketDirPart/$bucketPartDt"
  }

  override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
}
