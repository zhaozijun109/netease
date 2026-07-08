package com.linkedin.spark.datasources.tfrecordv2

import java.io.DataOutputStream

import com.linkedin.spark.shaded.org.tensorflow.hadoop.util.TFRecordWriter
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.{CodecStreams, OutputWriter}
import org.apache.spark.sql.types.StructType


class TFRecordOutputWriter(path: String,
                           options: Map[String, String],
                           dataSchema: StructType,
                           context: TaskAttemptContext)
  extends OutputWriter {

  private val outputStream = CodecStreams.createOutputStream(context, new Path(path))
  private val dataOutputStream = new DataOutputStream(outputStream)
  private val writer = new TFRecordWriter(dataOutputStream)
  private val recordType = options.getOrElse("recordType", "Example")
  private val serialized = options.getOrElse("serialized", "false").toBoolean

  private[this] val serializer = new TFRecordSerializer(dataSchema)

  override def write(row: InternalRow): Unit = {
    val record = if (serialized) {
      row.getBinary(0)
    } else {
      val record = recordType match {
        case "Example" =>
          serializer.serializeExample(row)
        case "SequenceExample" =>
          serializer.serializeSequenceExample(row)
        case _ =>
          throw new IllegalArgumentException(s"Unsupported recordType ${recordType}: recordType can be Example or SequenceExample")
      }
      record.toByteArray
    }
    writer.write(record)
  }

  override def close(): Unit = {
    outputStream.close()
    dataOutputStream.close()
  }
}
