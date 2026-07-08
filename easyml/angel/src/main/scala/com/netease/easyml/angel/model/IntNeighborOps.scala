package com.netease.easyml.angel.model

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

trait IntNeighborOps {
  def initNeighbors(nodeIds: Array[Int], neighbors: Array[Array[Int]])

  def initNeighbors(neighborTable: Seq[(Int, Array[Int])])

  def getNeighbors(nodeIds: Array[Int]): Int2ObjectOpenHashMap[Array[Int]]

  def sampleNeighbors(nodeIds: Array[Int]): Int2ObjectOpenHashMap[Array[Int]]
}
