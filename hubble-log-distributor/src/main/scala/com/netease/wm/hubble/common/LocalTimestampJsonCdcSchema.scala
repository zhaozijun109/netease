package com.netease.wm.hubble.common

import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.connect.data.{Schema, Struct, Timestamp}
import org.apache.kafka.connect.data.Schema.Type
import org.apache.kafka.connect.source.SourceRecord

import java.util.Date
import scala.collection.convert.ImplicitConversions._

class LocalTimestampJsonCdcSchema extends JsonDebeziumDeserializationSchema {

  def fixTimestamp(value: Struct): Unit = {
    if(value == null) return
    val fields = value.schema().fields()
    val fieldSchema =  fields.toSeq.map { f => f.name() + " " + f.schema()}.mkString(",")

    fields.forEach { f =>
      val fieldSchema = f.schema()
      if(fieldSchema.name() == io.debezium.time.Timestamp.SCHEMA_NAME) {
        val time = value.get(f).asInstanceOf[Long]
        val localDateTime = time - 8*3600*1000L
        value.put(f, localDateTime)
      }

      if(fieldSchema.`type`() == Type.STRUCT) {
        fixTimestamp(value.get(f).asInstanceOf[Struct])
      }
    }
  }

  override def deserialize(record: SourceRecord, out: Collector[String]): Unit = {
    val value = record.value().asInstanceOf[Struct]
    fixTimestamp(value)
    super.deserialize(record, out)
  }
}
