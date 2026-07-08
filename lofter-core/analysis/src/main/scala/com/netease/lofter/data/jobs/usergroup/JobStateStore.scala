package com.netease.lofter.data.jobs.usergroup

import java.io.{ByteArrayInputStream, File}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}

trait JobStateStore {
  def getJobState(dt: String, jobId: Long): String
  def getJobExtraState(dt: String, jobId: Long): String
  def saveJobState(dt: String, jobId: Long, content: Seq[String]): Unit
  def copyJobState(dt: String, jobId: Long, fromDate: String): Unit
  def uploadJobStateToNos(dt: String, jobId: Long): String
  def uploadNos(content: Seq[String]): String
  def uploadNos(path: String, size: Int): String
}

class LocalFileJobStateStore(val baseDir: String, extraDir: String) extends JobStateStore {
  override def saveJobState(dt: String, jobId: Long, content: Seq[String]): Unit = {
    val outPath = getJobStatePath(dt, jobId)
    val out = Files.newBufferedWriter(outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)

    content.map(_.trim).filter(_.nonEmpty).foreach { line =>
      out.write(line)
      out.write("\n")
    }
    out.flush()
    out.close()
  }

  override def copyJobState(dt: String, jobId: Long, fromDate: String): Unit = {
    val outPath = getJobStatePath(dt, jobId)
    val inPath = getJobStatePath(fromDate, jobId)

    if (!Files.exists(outPath)) {
      Files.copy(inPath, outPath, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  override def uploadJobStateToNos(dt: String, jobId: Long): String = {
    val jobStatePath = getJobStatePath(dt, jobId)
    val rows = scala.io.Source.fromFile(jobStatePath.toFile).getLines().size
    NosHelper.uploadFile(Files.newInputStream(jobStatePath), rows)
  }

  override def uploadNos(content: Seq[String]): String = {
    val bis = new ByteArrayInputStream(content.mkString("\n").getBytes("UTF-8"))
    NosHelper.uploadFile(bis, content.size)
  }

  private def createBaseDir(dt: String): Path = {
    val path = new File(s"$baseDir/dt=$dt").toPath
    if (!Files.exists(path)) {
      Files.createDirectory(path)
    }
    path
  }

  private def createExtraBaseDir(dt: String): Path = {
    val path = new File(s"$extraDir/dt=$dt").toPath
    if (!Files.exists(path)) {
      Files.createDirectory(path)
    }
    path
  }

  private def getJobStatePath(dt: String, jobId: Long): Path = {
    val parent = createBaseDir(dt)
    val jobPath = parent.resolve(s"job_id=$jobId")

    if (!Files.exists(jobPath)) {
      Files.createDirectory(jobPath)
    }

    jobPath.resolve("users.txt")
  }

  override def getJobState(dt: String, jobId: Long): String = getJobStatePath(dt, jobId).toString

  override def uploadNos(path: String, size: Int): String = {
    NosHelper.uploadFile(Files.newInputStream(new File(path).toPath), size)
  }

  override def getJobExtraState(dt: String, jobId: Long): String = {
    val parent = createExtraBaseDir(dt)
    val jobExtraPath = parent.resolve(s"job_id=$jobId")

    if (!Files.exists(jobExtraPath)) {
      Files.createDirectory(jobExtraPath)
    }

    jobExtraPath.resolve("users.txt").toString
  }
}
