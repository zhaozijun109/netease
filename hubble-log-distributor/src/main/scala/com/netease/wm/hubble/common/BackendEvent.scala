package com.netease.wm.hubble.common

import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import org.apache.flink.api.common.serialization.Encoder
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer

case class BackendEvent(tableName: String, time: Long, content: String)

class BackendEventEncoder extends Encoder[BackendEvent] {
  override def encode(in: BackendEvent, outputStream: OutputStream): Unit = {
    outputStream.write(in.content.getBytes("utf-8"))
    outputStream.write(10)
  }
}

case class BackendEventBucketAssigner(dateFormat: String) extends BucketAssigner[BackendEvent, String]{
  private val serialVersionUID = 1L
  @transient private var formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

  override def getBucketId(in: BackendEvent, context: BucketAssigner.Context): String = {
    if(formatter == null) {
      formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
    }

    val eventTime = in.time

    val bucketPartDt = formatter.format(Instant.ofEpochMilli(eventTime))
    val bucketDirPart = in.tableName

    s"$bucketDirPart/dt=$bucketPartDt"
  }

  override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
}
