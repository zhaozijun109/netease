package com.netease.lofter.data.jobs.usergroup

import org.apache.hadoop.fs.permission.FsPermission
import org.apache.hadoop.fs.{CreateFlag, FileContext, Path}

import java.io.{ByteArrayInputStream, File}
import java.net.URI

class HdfsJobStateStore(val baseDir: String, val extraDir: String) extends JobStateStore {

  lazy val fileStore: FileContext = FileContext.getFileContext(new URI("hdfs://gy-cluster8/user/da_lofter"))

  override def saveJobState(dt: String, jobId: Long, content: Seq[String]): Unit = {
    val outPath = getJobStatePath(dt, jobId)
    val out = fileStore.create(outPath, java.util.EnumSet.of(CreateFlag.CREATE, CreateFlag.OVERWRITE))

    content.map(_.trim).filter(_.nonEmpty).foreach { line =>
      out.writeBytes(line)
      out.writeBytes("\n")
    }
    out.close()
  }

  override def copyJobState(dt: String, jobId: Long, fromDate: String): Unit = {
    val outPath = getJobStatePath(dt, jobId)
    val inPath = getJobStatePath(fromDate, jobId)

    if (fileStore.util().exists(inPath) && !fileStore.util().exists(outPath)) {
      fileStore.util().copy(inPath, outPath, false, true)
    }
  }

  override def uploadJobStateToNos(dt: String, jobId: Long): String = {
    val jobStatePath = getJobStatePath(dt, jobId)
    val contentSize = fileStore.getFileStatus(jobStatePath).getLen
    val estimateRows = if(contentSize == 0) 0 else (contentSize / 16 + 1)
    NosHelper.uploadFile(fileStore.open(jobStatePath), estimateRows)
  }

  override def uploadNos(content: Seq[String]): String = {
    val rows = content.size
    val bis = new ByteArrayInputStream(content.mkString("\n").getBytes("UTF-8"))
    NosHelper.uploadFile(bis, rows)
  }

  private def createBaseDir(dt: String): Path = {
    val path = new Path(s"$baseDir/dt=$dt")
    if (!fileStore.util.exists(path)) {
      fileStore.mkdir(path, FsPermission.getDirDefault, true)
    }
    path
  }

  private def createExtraBaseDir(dt: String): Path = {
    val path = new Path(s"$extraDir/dt=$dt")
    if (!fileStore.util.exists(path)) {
      fileStore.mkdir(path, FsPermission.getDirDefault, true)
    }
    path
  }

  private def getJobStatePath(dt: String, jobId: Long): Path = {
    val parentDir = createBaseDir(dt)
    val jobDir = s"$parentDir/job_id=$jobId"
    val jobPath = new Path(jobDir)
    if (!fileStore.util().exists(jobPath)) {
      fileStore.mkdir(jobPath, FsPermission.getDirDefault, true)
    }

    new Path(s"$jobDir/users.txt")
  }

  override def getJobState(dt: String, jobId: Long): String = getJobStatePath(dt, jobId).toString

  override def uploadNos(path: String, size: Int): String = {
    val fileIterator = fileStore.listStatus(new Path(path))
    val files = Iterator.continually(fileIterator.hasNext).takeWhile(identity).map(_ => fileIterator.next())
      .filter(_.isFile)
      .filter(_.getPath.toString.endsWith(".csv"))
      .map(_.getPath)
      .toSeq

    NosHelper.uploadFile(fileStore.open(files.head), size)
  }

  override def getJobExtraState(dt: String, jobId: Long): String = {
    val parentDir = createExtraBaseDir(dt)
    val jobDir = s"$parentDir/job_id=$jobId"
    val jobPath = new Path(jobDir)
    if (!fileStore.util().exists(jobPath)) {
      fileStore.mkdir(jobPath, FsPermission.getDirDefault, true)
    }

    new Path(s"$jobDir").toString
  }
}
