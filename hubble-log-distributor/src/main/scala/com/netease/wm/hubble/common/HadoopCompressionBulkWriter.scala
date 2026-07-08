package com.netease.wm.hubble.common

import org.apache.flink.api.common.serialization.{BulkWriter, Encoder}
import org.apache.hadoop.io.compress.{CompressionCodec, CompressionOutputStream}

import java.io.IOException
import org.apache.flink.core.fs.FSDataOutputStream


class HadoopCompressionBulkWriter[T](val outputStream: FSDataOutputStream, val compressionCodec: CompressionCodec, encoder: Encoder[T]) extends BulkWriter[T]{
  private val compressor: CompressionOutputStream = compressionCodec.createOutputStream(outputStream)

  @throws[IOException]
  def addElement(element: T): Unit = {
    encoder.encode(element, compressor)
  }

  @throws[IOException]
  def flush(): Unit = {
    compressor.flush()
    outputStream.flush
  }

  @throws[IOException]
  def finish(): Unit = {
    compressor.finish()
    outputStream.sync
  }
}
