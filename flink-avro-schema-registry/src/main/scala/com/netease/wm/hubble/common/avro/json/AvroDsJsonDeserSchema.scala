package com.netease.wm.hubble.common.avro.json

import com.netease.wm.hubble.common.avro.AvroJsonDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.specific.{SpecificDatumReader, SpecificRecordBase}
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.formats.avro.typeutils.AvroTypeInfo
import org.apache.flink.formats.avro.utils.MutableByteArrayInputStream

import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.util.matching.Regex

class AvroDsJsonDeserSchema[T <: SpecificRecordBase :ClassTag](val ignoreErrors: Boolean = false)
  extends DeserializationSchema[T] {
  @transient var datumReader: GenericDatumReader[T] = _
  @transient var inputStream: MutableByteArrayInputStream = _
  @transient var decoder: AvroJsonDecoder = _
  @transient var schema: Schema = _

  val LOG_PATTERN: Regex = """^[^\{]+(\{.*\})\s*$""".r

  def init(): Unit = {
    if (this.datumReader == null) {
      val schema = implicitly[reflect.ClassTag[T]].runtimeClass.newInstance().asInstanceOf[SpecificRecordBase].getSchema
      this.datumReader = new SpecificDatumReader(schema)
      this.schema = schema
      this.inputStream = new MutableByteArrayInputStream
      this.decoder = new AvroJsonDecoder(this.schema, this.inputStream)
    }
  }

  override def deserialize(bytes: Array[Byte]): T = {
    if (bytes == null) {
      null.asInstanceOf[T]
    } else {
      val line = new String(bytes, "UTF-8")
      line match {
        case LOG_PATTERN(json) =>
          init()
          this.inputStream.setBuffer(json.getBytes("UTF-8"))
          this.datumReader.setSchema(this.schema)
          this.decoder.configure(this.inputStream)

          try {
            this.datumReader.read(null.asInstanceOf[T], this.decoder)
          } catch {
            case NonFatal(e) =>
              if (ignoreErrors) {
                null.asInstanceOf[T]
              } else {
                throw new RuntimeException("deserialize json error", e)
              }
          }
        case _ =>
          null.asInstanceOf[T]
      }
    }
  }

  override def isEndOfStream(t: T): Boolean = false

  override def getProducedType: TypeInformation[T] = new AvroTypeInfo(implicitly[reflect.ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])
}

