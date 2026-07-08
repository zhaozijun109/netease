package com.netease.easyml.ml.util

import breeze.linalg.{sum, DenseVector => BDV, SparseVector => BSV, Vector => BV}
import breeze.numerics.sqrt
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}

/**
 * Created by linjiuning on 2020/8/4.
 */
object VectorUtils {

  def normalize(vector: Vector, p: Double): Vector = {
    val norm = Vectors.norm(vector, p)

    if (norm != 0.0) {
      // For dense vector, we've to allocate new memory for new output vector.
      // However, for sparse vector, the `index` array will not be changed,
      // so we can re-use it to save memory.
      vector match {
        case DenseVector(vs) =>
          val values = vs.clone()
          val size = values.length
          var i = 0
          while (i < size) {
            values(i) /= norm
            i += 1
          }
          Vectors.dense(values)
        case SparseVector(size, ids, vs) =>
          val values = vs.clone()
          val nnz = values.length
          var i = 0
          while (i < nnz) {
            values(i) /= norm
            i += 1
          }
          Vectors.sparse(size, ids, values)
        case v => throw new IllegalArgumentException("Do not support vector type " + v.getClass)
      }
    } else {
      // Since the norm is zero, return the input vector object itself.
      // Note that it's safe since we always assume that the data in RDD
      // should be immutable.
      vector
    }
  }

  def normalizeMax(vector: Vector): Vector = {
    vector match {
      case DenseVector(vs) =>
        val values = vs.clone()
        var maxVal = if (values.isEmpty) 1.0 else values.max
        if (maxVal == 0) {
          maxVal = 1.0
        }
        val size = values.length
        var i = 0
        while (i < size) {
          values(i) /= maxVal
          i += 1
        }
        Vectors.dense(values)
      case SparseVector(size, ids, vs) =>
        val values = vs.clone()
        var maxVal = if (values.isEmpty) 1.0 else values.max
        if (maxVal == 0) {
          maxVal = 1.0
        }
        val nnz = values.length
        var i = 0
        while (i < nnz) {
          values(i) /= maxVal
          i += 1
        }
        Vectors.sparse(size, ids, values)
      case v => throw new IllegalArgumentException("Do not support vector type " + v.getClass)
    }
  }


  def fromBreeze(breezeVector: BV[Double]): Vector = {
    breezeVector match {
      case v: BDV[Double] =>
        if (v.offset == 0 && v.stride == 1 && v.length == v.data.length) {
          new DenseVector(v.data)
        } else {
          new DenseVector(v.toArray) // Can't use underlying array directly, so make a new one
        }
      case v: BSV[Double] =>
        if (v.index.length == v.used) {
          new SparseVector(v.length, v.index, v.data)
        } else {
          new SparseVector(v.length, v.index.slice(0, v.used), v.data.slice(0, v.used))
        }
      case v: BV[_] =>
        sys.error("Unsupported Breeze vector type: " + v.getClass.getName)
    }
  }

  def asBreeze(vector: Vector): BV[Double] = {
    vector match {
      case v: DenseVector =>
        new BDV[Double](v.values)
      case v: SparseVector =>
        new BSV[Double](v.indices, v.values, v.size)
      case _ =>
        throw new IllegalArgumentException("Unsupported vector format. Expected " +
          s"SparseVector or DenseVector. Instead got: ${vector.getClass}")
    }
  }

  def addVectors(v1: Vector, v2: Vector): Vector = {
    fromBreeze(asBreeze(v1) + asBreeze(v2))
  }

  def subtractionVectors(v1: Vector, v2: Vector): Vector = {
    fromBreeze(asBreeze(v1) - asBreeze(v2))
  }

  def divisionVectors(v1: Vector, v2: Vector): Vector = {
    fromBreeze(asBreeze(v1) / asBreeze(v2))
  }

  def sqrtVector(v1: Vector): Vector = {
    fromBreeze(sqrt(asBreeze(v1)))
  }

  def sumVector(v: Vector): Double = {
    sum(asBreeze(v))
  }

  def subtractionVector(v: Vector, scalar: Double): Vector = {
    fromBreeze(asBreeze(v) - scalar)
  }

  def isEmptyVector(v: Vector): Boolean = {
    v match {
      case v: DenseVector =>
        v.toSparse.numNonzeros == 0
      case v: SparseVector =>
        v.numNonzeros == 0
      case v => throw new IllegalArgumentException("Do not support vector type " + v.getClass)
    }
  }
}
