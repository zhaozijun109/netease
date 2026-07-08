package com.netease.easyudf.util

import org.apache.spark.util.AccumulatorV2

class SetAccumulator[T] extends AccumulatorV2[T, java.util.Set[T]] {
  private var _set: java.util.Set[T] = _

  private def getOrCreate: java.util.Set[T] = {
    _set = Option(_set).getOrElse(new java.util.HashSet[T]())
    _set
  }

  /**
   * Returns false if this accumulator instance has any values in it.
   */
  override def isZero: Boolean = this.synchronized(getOrCreate.isEmpty)

  override def copyAndReset(): SetAccumulator[T] = new SetAccumulator

  override def copy(): SetAccumulator[T] = {
    val newAcc = new SetAccumulator[T]
    this.synchronized {
      newAcc.getOrCreate.addAll(getOrCreate)
    }
    newAcc
  }

  override def reset(): Unit = this.synchronized {
    _set = null
  }

  override def add(v: T): Unit = this.synchronized(getOrCreate.add(v))

  override def merge(other: AccumulatorV2[T, java.util.Set[T]]): Unit = other match {
    case o: SetAccumulator[T] => this.synchronized(getOrCreate.addAll(o.value))
    case _ => throw new UnsupportedOperationException(
      s"Cannot merge ${this.getClass.getName} with ${other.getClass.getName}")
  }

  override def value: java.util.Set[T] = this.synchronized {
    java.util.Collections.unmodifiableSet(new java.util.HashSet[T](getOrCreate))
  }

  private def setValue(newValue: java.util.Set[T]): Unit = this.synchronized {
    _set = null
    getOrCreate.addAll(newValue)
  }
}