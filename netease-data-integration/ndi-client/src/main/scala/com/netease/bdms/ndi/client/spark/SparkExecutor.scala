package com.netease.bdms.ndi.client.spark

import java.util.concurrent.TimeUnit

import com.netease.bdms.ndi.client.AbstractExecutor
import com.netease.bdms.ndi.client.instance.InstanceProtocol
import com.netease.bdms.ndi.client.property.Properties.properties
import com.netease.bdms.ndi.client.property.Property.{KEYTAB, PRINCIPAL}
import com.netease.bdms.ndi.client.util.LogTrait
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.yarn.api.records.{ApplicationReport, FinalApplicationStatus, YarnApplicationState}
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException

import scala.util.{Failure, Success, Try}

class SparkExecutor(iProtocol: InstanceProtocol)
  extends AbstractExecutor(iProtocol) with YarnInterface with LogTrait {

  private[this] val appIdListener = new AcquireAppIdListener()
  private[this] var appReport: Option[ApplicationReport] = Option.empty

  override def init(): Unit = {
  }

  override def doStart(): Unit = {
    if (properties.contains(KEYTAB.key) && properties.contains(PRINCIPAL.key)) {
      val thread = new Thread(new Runnable {
        override def run(): Unit = {
          LOG.info("Start auto kinit thread.")
          while (true) {
            Try {
              UserGroupInformation.loginUserFromKeytab(
                properties.getProperty(PRINCIPAL).get,
                properties.getProperty(KEYTAB).get
              )
              LOG.info("Auto kinit successfully")
              TimeUnit.HOURS.sleep(3)
            }
          }
        }
      })
      thread.setDaemon(true)
      thread.start()
    }

    iProtocol.fetchTask()
    val sparkApp = new SparkProcessBuilder(new SparkProperties).build()
    sparkApp.start(appIdListener)
    val exitValue = sparkApp.waitFor
    LOG.info(s"Spark app exit with code $exitValue")
    if (!appIdListener.foundAppId) {
      throw new RuntimeException(s"Cannot found application id however the spark app has been finished.")
    }
    val appId = appIdListener.applicationId.get
    while (!applicationFinished) {
      val result = Try {
        yarnClient.getApplicationReport(appId)
      }
      result match {
        case Success(report) =>
          appReport = Option(report)
        case Failure(exception) =>
          exception match {
            case e: ApplicationNotFoundException =>
              throw new RuntimeException(e)
            case others: Exception =>
              LOG.warn("", others)
          }
      }
      if (appReport.isDefined) {
        LOG.info(s"Application state: ${appReport.get.getYarnApplicationState}")
      }
      if (!applicationFinished) {
        Try {
          TimeUnit.SECONDS.sleep(5)
        }
      }
    }


    if (appReport.get.getYarnApplicationState == YarnApplicationState.FINISHED) {
      LOG.info(s"Yarn application state: ${appReport.get.getYarnApplicationState}")
    } else {
      LOG.error(s"Yarn application state: ${appReport.get.getYarnApplicationState}")
      LOG.error(s"Diagnostics: ${appReport.get.getDiagnostics}")
      throw new RuntimeException("Application finished with state failed.")
    }

    if (appReport.get.getFinalApplicationStatus == FinalApplicationStatus.SUCCEEDED) {
      LOG.info(s"Yarn application final status: ${appReport.get.getFinalApplicationStatus}")
    } else {
      LOG.error(s"Yarn application final status: ${appReport.get.getFinalApplicationStatus}")
      LOG.error(s"Diagnostics: ${appReport.get.getDiagnostics}")
      throw new RuntimeException("Application finished with state failed.")
    }
  }

  private def applicationFinished: Boolean = {
    appReport.isDefined && {
      val state = appReport.get.getYarnApplicationState
      state == YarnApplicationState.KILLED ||
        state == YarnApplicationState.FAILED ||
        state == YarnApplicationState.FINISHED
    }
  }
}
