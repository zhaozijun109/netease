package com.netease.music.da.transfer.common.metrics

import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.scheduler._
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Success, Try}

case class Metrics(recordNumber: Long, byteNumber: Long)

object Metrics extends LogTrait {
  private[metrics] var inputMetrics = Metrics(0L, 0L)
  private val inputLock = new Object()
  private[metrics] var outputMetrics = Metrics(0L, 0L)
  private val outputLock = new Object()
  private[metrics] val startTime: Long = System.currentTimeMillis()
  private[metrics] var endTime: Long = _
  private var succeeded = true

  def addListener(spark: SparkContext): Unit = {
    if (enableMetrics(spark.getConf)) {
      spark.addSparkListener(new SparkListener {

        override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
          endTime = System.currentTimeMillis()
          if (succeeded) {
            record(spark.getConf)
          }
        }

        override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
          jobEnd.jobResult match {
            case JobSucceeded =>
              succeeded = true
            case _ =>
              succeeded = false
          }
        }

        override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
          Try {
            inputLock.synchronized {
              inputMetrics = Metrics(taskEnd.taskMetrics.inputMetrics.recordsRead + inputMetrics.recordNumber,
                taskEnd.taskMetrics.inputMetrics.bytesRead + inputMetrics.byteNumber)
            }
            outputLock.synchronized {
              outputMetrics = Metrics(taskEnd.taskMetrics.outputMetrics.recordsWritten + outputMetrics.recordNumber,
                taskEnd.taskMetrics.outputMetrics.bytesWritten + outputMetrics.byteNumber)
            }
          } match {
            case Success(_) =>
            case Failure(exception) =>
              LOG.warn(s"Accumulate metrics of task ${taskEnd.taskInfo.taskId} failed, skip it.", exception)
          }
        }
      })
    }
  }

  private def record(sparkConf: SparkConf): Unit = {
    if (enableMetrics(sparkConf)) {
      sparkConf.get("spark.transmit.metrics.recorder") match {
        case "mysql" =>
          MySQLMetricsRecorder.record(sparkConf)
        case other =>
          LOG.warn(s"Unknown metrics recorder $other")
      }
    }
  }

  private def enableMetrics(sparkConf: SparkConf): Boolean = {
    sparkConf.getBoolean("spark.transmit.metrics.enable", defaultValue = false)
  }
}
