package com.netease.wm.hubble.common

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

case class JsonEventTimeBucketAssigner(dateFormat: String, eventTimeField: String) extends BucketAssigner[String, String]{
  private val serialVersionUID = 1L
  @transient private var formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
  private val objectMapper = new ObjectMapper()
  objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)

  override def getBucketId(in: String, context: BucketAssigner.Context): String = {
    if(formatter == null) {
      formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
    }

    val eventTimeNode = objectMapper.readTree(in).get(eventTimeField)
    val defaultValue = System.currentTimeMillis()
    val eventTime = if (eventTimeNode == null) defaultValue else eventTimeNode.asLong(defaultValue)

    formatter.format(Instant.ofEpochMilli(eventTime))
  }

  override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
}
