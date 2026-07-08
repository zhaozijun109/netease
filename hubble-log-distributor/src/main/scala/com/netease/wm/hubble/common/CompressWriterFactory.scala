package com.netease.wm.hubble.common

import org.apache.flink.api.common.serialization.{BulkWriter, Encoder}
import org.apache.flink.core.fs.FSDataOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.compress.CompressionCodecFactory

class CompressWriterFactory[T](codecName: String, encoder: Encoder[T]) extends BulkWriter.Factory[T]{

  override def create(out: FSDataOutputStream): BulkWriter[T] = {
    val hadoopCodec = new CompressionCodecFactory(new Configuration()).getCodecByName(codecName)
    new HadoopCompressionBulkWriter(out, hadoopCodec, encoder)
  }
}
