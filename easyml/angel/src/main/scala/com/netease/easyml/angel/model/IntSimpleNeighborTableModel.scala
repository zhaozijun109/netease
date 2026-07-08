package com.netease.easyml.angel.model

import com.netease.easyml.angel.psf.get.IntGetIntNeighbor
import com.netease.easyml.angel.psf.result.{IntGetByteNeighbor, IntGetIntsResult}
import com.netease.easyml.angel.psf.update.IntInitNeighbors
import com.tencent.angel.graph.common.param.ModelContext
import com.tencent.angel.graph.common.psf.param.{IntKeysGetParam, IntKeysUpdateParam}
import com.tencent.angel.graph.model.ops.CommonOps
import com.tencent.angel.graph.utils.ModelContextUtils
import com.tencent.angel.ml.matrix.{MatrixContext, RowType}
import com.tencent.angel.ps.storage.vector.element.{ByteArrayElement, IElement, IntArrayElement}
import com.tencent.angel.spark.models.PSMatrix
import com.twitter.chill.ScalaKryoInstantiator
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class IntSimpleNeighborTableModel(modelContext: ModelContext) extends IntNeighborOps with CommonOps with Serializable {

  var neighborMatrix: PSMatrix = _

  override def initNeighbors(nodeIds: Array[Int], neighbors: Array[Array[Int]]): Unit = {
    var neighborElems: Array[IElement] = null
    if (modelContext.isUseBytesFormatForReadOnly) {
      neighborElems = neighbors.map(
        e => new ByteArrayElement(ScalaKryoInstantiator.defaultPool.toBytesWithoutClass(e)).asInstanceOf[IElement])
    } else {
      neighborElems = neighbors.map(e => new IntArrayElement(e))
    }
    neighborMatrix.psfUpdate(new IntInitNeighbors(new IntKeysUpdateParam(neighborMatrix.id, nodeIds, neighborElems))).get()
  }

  override def initNeighbors(neighborTable: Seq[(Int, Array[Int])]): Unit = {
    val nodeIds = new Array[Int](neighborTable.size)
    val neighborElems = new Array[IElement](neighborTable.size)
    if (modelContext.isUseBytesFormatForReadOnly) {
      neighborTable.zipWithIndex.foreach(e => {
        nodeIds(e._2) = e._1._1
        neighborElems(e._2) = new ByteArrayElement(ScalaKryoInstantiator.defaultPool.toBytesWithoutClass(e._1._2))
      })
    } else {
      neighborTable.zipWithIndex.foreach(e => {
        nodeIds(e._2) = e._1._1
        neighborElems(e._2) = new IntArrayElement(e._1._2)
      })
    }
    neighborMatrix.psfUpdate(new IntInitNeighbors(new IntKeysUpdateParam(neighborMatrix.id, nodeIds, neighborElems))).get()
  }

  override def getNeighbors(nodeIds: Array[Int]): Int2ObjectOpenHashMap[Array[Int]] = {
    if (modelContext.isUseBytesFormatForReadOnly) {
      neighborMatrix.psfGet(
        new IntGetByteNeighbor(
          new IntKeysGetParam(neighborMatrix.id, nodeIds))).asInstanceOf[IntGetIntsResult].getData
    } else {
      neighborMatrix.psfGet(
        new IntGetIntNeighbor(
          new IntKeysGetParam(neighborMatrix.id, nodeIds))).asInstanceOf[IntGetIntsResult].getData
    }
  }

  override def sampleNeighbors(nodeIds: Array[Int]): Int2ObjectOpenHashMap[Array[Int]] = {
    throw new UnsupportedOperationException("")
  }

  override def init(mc: MatrixContext): Unit = {
    val valueClass = if (modelContext.isUseBytesFormatForReadOnly) {
      classOf[ByteArrayElement]
    } else {
      classOf[IntArrayElement]
    }

    val mc = ModelContextUtils.createMatrixContext(modelContext, RowType.T_ANY_INTKEY_SPARSE, valueClass)
    neighborMatrix = PSMatrix.matrix(mc)
  }

  override def checkpoint(): Unit = {
    neighborMatrix.checkpoint(0)
  }
}
