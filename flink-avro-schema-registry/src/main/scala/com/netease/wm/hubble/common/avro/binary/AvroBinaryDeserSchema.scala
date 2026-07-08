package com.netease.wm.hubble.common.avro.binary

import com.netease.wm.hubble.common.avro.InnerBinaryDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.specific.{SpecificDatumReader, SpecificRecordBase}
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.formats.avro.typeutils.AvroTypeInfo

import scala.reflect.ClassTag

class AvroBinaryDeserSchema[T <: SpecificRecordBase :ClassTag] extends DeserializationSchema[T] {
  @transient var datumReader: GenericDatumReader[T] = _
  @transient var schema: Schema = _

  def init(): Unit = {
    if (datumReader == null) {
      schema = implicitly[reflect.ClassTag[T]].runtimeClass.newInstance().asInstanceOf[SpecificRecordBase].getSchema
      datumReader = new SpecificDatumReader(schema)
    }
  }

  override def deserialize(bytes: Array[Byte]): T = {
    if (bytes == null) {
      null.asInstanceOf[T]
    } else {
      init()
      datumReader.setSchema(schema)
      val decoder = new InnerBinaryDecoder(bytes, 0, bytes.length)
      val result = datumReader.read(null.asInstanceOf[T], decoder)
      result
    }
  }

  override def isEndOfStream(t: T): Boolean = false

  override def getProducedType: TypeInformation[T] = new AvroTypeInfo(implicitly[reflect.ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])
}

