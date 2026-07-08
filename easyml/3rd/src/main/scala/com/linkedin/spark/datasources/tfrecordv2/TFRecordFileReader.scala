package com.linkedin.spark.datasources.tfrecordv2

import com.linkedin.spark.datasources.tfrecord.{TFRecordFileReader => TFRecordFileReader_}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.PartitionedFile
import org.apache.spark.sql.types.StructType

object TFRecordFileReader {
  def readFile(conf: Configuration,
               options: Map[String, String],
               file: PartitionedFile,
               schema: StructType): Iterator[InternalRow] = {
    val keys = options.getOrElse("keys", "").split("[,;:]").map(_.trim).filterNot(_.equals("*"))
    var newSchema = schema
    if (!keys.isEmpty) {
      newSchema = StructType(schema.fields.filter(it => keys.contains(it.name)))
    }
    TFRecordFileReader_.readFile(conf, options, file, newSchema)
  }
}