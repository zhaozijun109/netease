package com.netease.bdms.ndi.client

import com.netease.bdms.ndi.client.instance.InstanceProtocol
import com.netease.bdms.ndi.client.util.LogTrait

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

abstract class AbstractExecutor(val iProtocol: InstanceProtocol) extends LogTrait {

  private[this] var future: Future[Unit] = _

  def init(): Unit

  def doStart(): Unit

  def start(): Unit = {
    future = Future[Unit] {
      doStart()
    }
  }

  def waitFor: Boolean = {
    Await.result(future, Duration.Inf)
    var result = true
    future onComplete {
      case Success(_) =>
        result = true
      case Failure(exception) =>
        LOG.error("Execute task failed.", exception)
        result = false
    }
    result
  }
}
