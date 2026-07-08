package com.netease.wm.hubble.common

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner

/**
 * This Partitioner can avoid too much producer requests for each partition
 * Better compression and linear IO
 *
 * mapping current operator to next operator
 * for example mapping source parallel 3 with 2 expand to target 2 parallel
 *  expand  sourceId  targetId
 *   0         1           1
 *   0         2           2
 *   0         3           1
 *   1         1           2
 *   1         2           1
 *   1         3           2
 *
 * based on org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner
 */
@SerialVersionUID(1L)
class LessShufflePartitioner[T](val expands: Int = 1) extends FlinkKafkaPartitioner[T] {
  var sourceInstanceId: Int = _
  var sourceInstances: Int = _

  private val rand = new scala.util.Random

  override def open(parallelInstanceId: Int, parallelInstances: Int): Unit = {
    this.sourceInstanceId = parallelInstanceId
    this.sourceInstances = parallelInstances
  }

  override def partition(record: T, key: Array[Byte], value: Array[Byte], topic: String, partitions: Array[Int]): Int = {
    val keyHash = if(key == null) rand.nextInt() else java.util.Arrays.hashCode(key)
    val targetId = Math.abs(keyHash % expands) * sourceInstances + sourceInstanceId
    partitions(targetId % partitions.length)
  }
}

