package com.netease.bdms.ndi.client.spark

import com.netease.bdms.ndi.client.util.LogTrait

class LineBufferedProcessBuilder(val processBuilder: ProcessBuilder,
                                 val logSize: Int) extends LogTrait {

  private[this] var process: Process = _
  private[this] var _inputStream: LineBufferedStream = _
  private[this] var _errorStream: LineBufferedStream = _

  def inputLines: IndexedSeq[String] = _inputStream.lines

  def errorLines: IndexedSeq[String] = _errorStream.lines

  def inputIterator: Iterator[String] = _inputStream.iterator

  def errorIterator: Iterator[String] = _errorStream.iterator

  def start(listener: AcquireAppIdListener): Process = {
    this.process = processBuilder.start()
    _inputStream = new LineBufferedStream(process.getInputStream, logSize, listener)
    _errorStream = new LineBufferedStream(process.getErrorStream, logSize, listener)
    process
  }

  def destroy(): Unit = {
    if (this.process != null) {
      process.destroy()
    }
  }

  /**
    * Returns if the process is still actively running.
    * If the process is null, a NullPointerException will be thrown.
    */
  def isAlive: Boolean = process.isAlive

  /**
    * Returns the exit value of the process.
    * If the process is null, a NullPointerException will be thrown.
    */
  def exitValue(): Int = process.exitValue()

  /**
    * Wait for the process finished.
    * If the process is null, a NullPointerException will be thrown.
    */
  def waitFor(): Int = {
    val returnCode = process.waitFor()
    _inputStream.waitUntilClose()
    _errorStream.waitUntilClose()
    returnCode
  }
}
