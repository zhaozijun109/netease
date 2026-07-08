package com.netease.bdms.ndi.client.spark

import com.netease.bdms.ndi.client.util.LogTrait

class SparkApp(val process: LineBufferedProcessBuilder) extends LogTrait {

  def start(listener: AcquireAppIdListener): Process = {
    process.start(listener)
  }

  def alive: Boolean = process.isAlive

  def waitFor: Int = process.waitFor()

  def exitValue: Int = process.exitValue()
}
