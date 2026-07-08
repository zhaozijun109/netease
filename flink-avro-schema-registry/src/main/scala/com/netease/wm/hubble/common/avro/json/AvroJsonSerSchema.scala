package com.netease.wm.hubble.common.avro.json

import com.netease.wm.hubble.common.avro.AvroJsonEncoder
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.specific.{SpecificDatumWriter, SpecificRecordBase}
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.util.WrappingRuntimeException

import java.io.{ByteArrayOutputStream, IOException}
import scala.reflect.ClassTag

class AvroJsonSerSchema[T <: SpecificRecordBase :ClassTag] extends SerializationSchema[T] {
  @transient var datumWriter: GenericDatumWriter[T] = _
  @transient var arrayOutputStream: ByteArrayOutputStream = _
  @transient var encoder: AvroJsonEncoder = _
  @transient var schema: Schema = _

  def init(): Unit = {
    if(this.datumWriter == null) {
      val schema = implicitly[reflect.ClassTag[T]].runtimeClass.newInstance().asInstanceOf[SpecificRecordBase].getSchema
      this.datumWriter = new SpecificDatumWriter(schema)
      this.schema = schema
      this.arrayOutputStream = new ByteArrayOutputStream
      this.encoder = new AvroJsonEncoder(this.schema, this.arrayOutputStream)
    }
  }

  override def serialize(e: T): Array[Byte] = {
    init()
    if (e == null) {
      null
    } else {
      try {
        this.datumWriter.write(e, this.encoder)
        this.encoder.flush
        val bytes = this.arrayOutputStream.toByteArray
        this.arrayOutputStream.reset
        bytes
      } catch {
        case var3: IOException =>
          throw new WrappingRuntimeException("Failed to serialize schema registry.", var3)
      }
    }
  }
}
