package com.netease.lofter.etl.common

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import java.net.URI

private class CheckPointFileTask{}

object CheckPointFileTask {

  case class DirStat(files: Seq[String], totalSize: Long, subDirs: Seq[String])

  val LOG = LoggerFactory.getLogger(classOf[CheckPointFileTask])

  def dirStat(parent: String, fc: FileContext): DirStat = {
    val fileIterator = fc.listStatus(new Path(parent))
    val subs =  Iterator.continually(fileIterator.hasNext).takeWhile(identity)
      .map(_ => fileIterator.next())
      .filterNot(_.getPath.getName.endsWith("_SUCCESS"))
      .toSeq

    val (dirs , files) = subs.partition(_.isDirectory)

    DirStat(files.map(_.getPath.toString), files.map(_.getLen).sum, dirs.map(_.getPath.toString))
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val input = pargs.required("input")

    val spark = SparkSession.builder()
      .appName("CheckPointFileTask")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/user/da_lofter"))

    var totalSize: Long = 0L

    while(totalSize <= 0L) {
      totalSize = dirStat(input, fc).totalSize
      LOG.info("check input file: {}, got size: {}", input, totalSize)

      if(totalSize > 0L) {
        LOG.info("exit file checkpoint successfully")
      }

      Thread.sleep(10000)
    }

    spark.close()
  }
}
