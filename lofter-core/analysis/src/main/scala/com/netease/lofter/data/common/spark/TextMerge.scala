package com.netease.lofter.data.common.spark

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.LoggerFactory

import java.net.URI
import java.util.UUID

private class TextMerge{}

/**
 * merge directory of small text files into large file
 */
object TextMerge {
  case class DirStat(files: Seq[String], totalSize: Long, subDirs: Seq[String])
  val log = LoggerFactory.getLogger(classOf[TextMerge])

  def dirStat(parent: String, fc: FileContext): DirStat = {
    val fileIterator = fc.listStatus(new Path(parent))
    val subs =  Iterator.continually(fileIterator.hasNext).takeWhile(identity)
      .map(_ => fileIterator.next())
      .toSeq

    val (dirs , files) = subs.partition(_.isDirectory)


    DirStat(files.map(_.getPath.toString), files.map(_.getLen).sum, dirs.map(_.getPath.toString))
  }

  def subDirectories(parent: String, fc: FileContext): Seq[String] = {
    val fileIterator = fc.listStatus(new Path(parent))

    Iterator.continually(fileIterator.hasNext).takeWhile(identity).map(_ => fileIterator.next())
      .filter(_.isDirectory)
      .map(_.getPath.toString)
      .toSeq
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val dir = pargs.optional("dir")
    val parent = pargs.optional("parent")
    val grandpa = pargs.optional("grandpa")
    val excludes = pargs.optional("exclude").toSeq.flatMap(_.split(",")).toSet
    val mergeNum = pargs.optional("num").map(_.toInt)
    val partitionFileSize = pargs.long("partSize", 256 * 1024 * 1024) // default 256m

    if(dir.isEmpty && parent.isEmpty && grandpa.isEmpty) {
      throw new RuntimeException("at least one input is needed, through --dir or --parent or --grandpa param")
    }

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/user/da_lofter"))

    val parentDirs = parent.toSeq.flatMap(path => subDirectories(path, fc).sorted.reverse)

    val grandpaDirs = grandpa.toSeq
      .flatMap(topPath => subDirectories(topPath, fc).sorted.reverse)
      .flatMap(path => subDirectories(path, fc).sorted.reverse)

    val dirs = dir.toSeq ++ parentDirs ++ grandpaDirs

    def mergeDir(path: String): Unit = {
      val tmpOutput = "/user/da_lofter/tmp/" + UUID.randomUUID().toString

      val dir = dirStat(path, fc)

      log.info("merging path {}, totalSize: {}, current files: {}, sub dirs: {}",
        path, dir.totalSize.toString, dir.files.size.toString, dir.subDirs.size.toString)

      val files = dir.files
      val totalSize = dir.totalSize
      val partitions = mergeNum.getOrElse(Math.max((totalSize + partitionFileSize / 2) / partitionFileSize, 1).toInt)

      // merge precondition:
      //   1. no sub directories
      //   2. file number > 2 * desired
      if(dir.subDirs.isEmpty && files.size > (partitions + 1) * 2) {
        log.info("merging path {} into {} files: staging path: {}", path, partitions.toString, tmpOutput)

        spark.read.text(path)
          .coalesce(partitions)
          .write.mode(SaveMode.Overwrite)
          .option("compression", "gzip")
          .text(tmpOutput)

        val currentPath = new Path(path)
        val tmpPath = new Path(tmpOutput)
        try {
          fc.delete(currentPath, true)
          fc.rename(tmpPath, currentPath)
        } catch {
          case e: Throwable =>
            log.error("exception occurred while merging {}, staging file at: {}, exception: {}", path, tmpOutput, e)
            throw e
        }
      }
    }

    dirs.foreach { path =>
      val today = DateTime.now().toString("yyyy-MM-dd")
      if(!path.contains(today) && !excludes.exists(e => path.contains(e))) { // important avoid merge today directory which may be dangerous
        mergeDir(path)
      }
    }
  }

}
