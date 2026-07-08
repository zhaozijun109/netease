package com.hankcs.hanlp.corpus.io

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{InputStream, OutputStream}
import java.net.URI


class HadoopFileIOAdapter extends IIOAdapter {
  val conf: Configuration = new Configuration()

  override def create(path: String): OutputStream = {
    val fs: FileSystem = FileSystem.get(URI.create(path), conf)
    fs.create(new Path(path))
  }

  override def open(path: String): InputStream = {
    val fs: FileSystem = FileSystem.get(URI.create(path), conf)
    fs.open(new Path(path))
  }
}
