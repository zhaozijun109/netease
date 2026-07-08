package com.netease.bdms.ndi.client.spark

import java.io.InputStream
import java.util
import java.util.concurrent.locks.ReentrantLock

import com.netease.bdms.ndi.client.util.LogTrait
import org.apache.hadoop.yarn.api.records.ApplicationId

import scala.io.Source
import scala.util.{Failure, Success, Try}

class CircularQueue[T](val capacity: Int) extends util.LinkedList[T] {
  override def add(t: T): Boolean = {
    if (size >= capacity) {
      removeFirst()
    }
    super.add(t)
  }
}

class LineBufferedStream(inputStream: InputStream,
                         logSize: Int,
                         listener: AcquireAppIdListener) extends LogTrait {

  private[this] val pattern = "application_(\\d+)_(\\d+)".r.pattern
  private[this] val _lines: CircularQueue[String] = new CircularQueue[String](logSize)

  private[this] val _lock = new ReentrantLock()
  private[this] val _condition = _lock.newCondition()
  private[this] var _finished = false

  private val thread = new Thread {
    override def run(): Unit = {
      val lines = Source.fromInputStream(inputStream).getLines()
      for (line <- lines) {
        LOG.info(line)
        if (!listener.foundAppId) {
          val matcher = pattern.matcher(line)
          if (matcher.find()) {
            Try {
              val id = ApplicationId.newInstance(matcher.group(1).toLong, matcher.group(2).toInt)
              listener.listen(AcquireAppIdEvent(id))
              id
            } match {
              case Failure(exception) =>
                LOG.warn(s"Handle $line failed.", exception)
              case Success(id) =>
            }
          }
        }
        _lock.lock()
        try {
          _lines.add(line)
          _condition.signalAll()
        } finally {
          _lock.unlock()
        }
      }

      _lock.lock()
      try {
        _finished = true
        _condition.signalAll()
      } finally {
        _lock.unlock()
      }
    }
  }
  thread.setDaemon(true)
  thread.start()

  def lines: IndexedSeq[String] = {
    _lock.lock()
    val lines = IndexedSeq.empty[String] ++ _lines.toArray(Array.empty[String])
    _lock.unlock()
    lines
  }

  def iterator: Iterator[String] = {
    new LinesIterator
  }

  def waitUntilClose(): Unit = thread.join()

  private class LinesIterator extends Iterator[String] {

    override def hasNext: Boolean = {
      if (_lines.size > 0) {
        true
      } else {
        // Otherwise we might still have more data.
        _lock.lock()
        try {
          if (_finished) {
            false
          } else {
            _condition.await()
            _lines.size > 0
          }
        } finally {
          _lock.unlock()
        }
      }
    }

    override def next(): String = {
      _lock.lock()
      val line = _lines.poll()
      _lock.unlock()
      line
    }
  }

}
