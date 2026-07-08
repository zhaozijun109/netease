package com.netease.wm.hubble.common

import org.apache.flink.streaming.api.functions.sink.filesystem.{PartFileInfo, RollingPolicy}
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy

/**
 * Delegate RollingPolicy as CheckpointRollingPolicy
 * @param delegate
 */
class DelegateCheckpointRollingPolicy[T](delegate: RollingPolicy[T, String]) extends CheckpointRollingPolicy[T, String] {

  override def shouldRollOnEvent(partFileState: PartFileInfo[String], element: T): Boolean = {
    delegate.shouldRollOnEvent(partFileState, element)
  }

  override def shouldRollOnProcessingTime(partFileState: PartFileInfo[String], currentTime: Long): Boolean = {
    delegate.shouldRollOnProcessingTime(partFileState, currentTime)
  }
}
