package org.apache.spark.mllib.linalg_

import breeze.linalg.{Matrix => BM}
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.distributed.BlockMatrix
import org.apache.spark.rdd.RDD

/**
 * Created by linjiuning on 2021/6/26.
 */
object Matrices {

  def updateDial(matrix: BlockMatrix, v: Double): BlockMatrix = {
    val rowsPerBlock = matrix.rowsPerBlock
    val colsPerBlock = matrix.colsPerBlock
    val blocks = matrix.blocks.map { case ((blockRowIndex, blockColIndex), mat) =>
      val rowStart = blockRowIndex.toLong * rowsPerBlock
      val colStart = blockColIndex.toLong * colsPerBlock

      mat.foreachActive { (i, j, _) =>
        if (rowStart + i == colStart + j) mat.update(i, j, v)
      }
      ((blockRowIndex, blockColIndex), mat)
    }
    setBlocks(matrix, blocks)
  }

  def setBlocks(matrix: BlockMatrix, blocks: RDD[((Int, Int), Matrix)]): BlockMatrix = {
    new BlockMatrix(blocks, matrix.rowsPerBlock, matrix.colsPerBlock, matrix.numRows, matrix.numCols)
  }

  /** Converts to a breeze matrix. */
  def asBreeze(matrix: Matrix): BM[Double] = {
    matrix.asBreeze
  }

  /** Return the index for the (i, j)-th element in the backing array. */
  def index(matrix: Matrix, i: Int, j: Int): Int = {
    matrix.index(i, j)
  }

  /** Update element at (i, j) */
  def update(matrix: Matrix, i: Int, j: Int, v: Double): Unit = {
    matrix.update(i, j, v)
  }

  /**
   * Map the values of this matrix using a function. Generates a new matrix. Performs the
   * function on only the backing array. For example, an operation such as addition or
   * subtraction will only be performed on the non-zero values in a `SparseMatrix`.
   */
  def map(matrix: Matrix, f: Double => Double): Matrix = {
    matrix.map(f)
  }

  /**
   * Update all the values of this matrix using the function f. Performed in-place on the
   * backing array. For example, an operation such as addition or subtraction will only be
   * performed on the non-zero values in a `SparseMatrix`.
   */
  def update(matrix: Matrix, f: Double => Double): Matrix = {
    matrix.update(f)
  }

  /**
   * Applies a function `f` to all the active elements of dense and sparse matrix. The ordering
   * of the elements are not defined.
   *
   * @param f the function takes three parameters where the first two parameters are the row
   *          and column indices respectively with the type `Int`, and the final parameter is the
   *          corresponding value in the matrix with type `Double`.
   */
  def foreachActive(matrix: Matrix, f: (Int, Int, Double) => Unit): Unit = {
    matrix.foreachActive(f)
  }
}
