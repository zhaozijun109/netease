package com.github.saurfang.spark.tsne

import breeze.linalg.DenseVector
import org.apache.spark.mllib.X2PHelper._
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry, RowMatrix}
import org.apache.spark.mllib.rdd.MLPairRDDFunctions._
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.slf4j.LoggerFactory

object X2P {

  private def logger = LoggerFactory.getLogger(X2P.getClass)

  def apply(x: RowMatrix, tol: Double = 1e-5, perplexity: Double = 30.0, broadcast: Boolean = true): CoordinateMatrix = {
    require(tol >= 0, "Tolerance must be non-negative")
    require(perplexity > 0, "Perplexity must be positive")

    val mu = (3 * perplexity).toInt //TODO: Expose this as parameter
    val logU = Math.log(perplexity)
    //    val norms = x.rows.map(Vectors.norm(_, 2.0))
    //    norms.persist()
    //    val rowsWithNorm = x.rows.zip(norms).map { case (v, norm) => VectorWithNorm(v, norm) }
    //    val neighbors = rowsWithNorm.zipWithIndex()
    //      .cartesian(rowsWithNorm.zipWithIndex())
    //      .flatMap {
    //        case ((u, i), (v, j)) =>
    //          if (i < j) {
    //            val dist = fastSquaredDistance(u, v)
    //            Seq((i, (j, dist)), (j, (i, dist)))
    //          } else Seq.empty
    //      }
    //      .topByKey(mu)(Ordering.by(e => -e._2))

    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._

    val df = x.rows.zipWithIndex.map {
      case (vector, l) =>
        (l.toInt, vector, Vectors.norm(vector, 2.0))
    }.toDF("i", "vector", "norm")

    val dist = df.crossJoin(df).flatMap {
      case Row(i: Int, u: Vector, un: Double, j: Int, v: Vector, vn: Double) =>
        if (i < j) {
          val dist = fastSquaredDistance(VectorWithNorm(u, un), VectorWithNorm(v, vn))
          Seq((i, (j, dist)), (j, (i, dist)))
        } else Seq.empty
    }.rdd

//    val norms = x.rows.map(Vectors.norm(_, 2.0))
//
//    val rowsWithNorm = x.rows.zip(norms)
//      .map { case (v, norm) => VectorWithNorm(v, norm) }
//      .zipWithIndex()
//      .map {
//        case (norm, l) => (norm, l.toInt)
//      }
//    rowsWithNorm.persist(StorageLevel.MEMORY_AND_DISK)
//    val dist = if (broadcast) {
//      val array = rowsWithNorm.collect()
//      val arrayBc = rowsWithNorm.sparkContext.broadcast(array)
//      rowsWithNorm.mapPartitions(iter => {
//        val array = arrayBc.value
//        iter.flatMap {
//          case (u, i) =>
//            array.filter(i < _._2).flatMap {
//              case (v, j) =>
//                val dist = fastSquaredDistance(u, v)
//                Seq((i, (j, dist)), (j, (i, dist)))
//            }
//        }
//      })
//    } else {
//      rowsWithNorm.cartesian(rowsWithNorm)
//        .flatMap {
//          case ((u, i), (v, j)) =>
//            if (i < j) {
//              val dist = fastSquaredDistance(u, v)
//              Seq((i, (j, dist)), (j, (i, dist)))
//            } else Seq.empty
//        }
//    }

    val neighbors = dist.topByKey(mu)(Ordering.by(e => -e._2))

    val p_betas =
      neighbors.map {
        case (i, arr) =>
          var betamin = Double.NegativeInfinity
          var betamax = Double.PositiveInfinity
          var beta = 1.0

          val d = DenseVector(arr.map(_._2))
          var (h, p) = Hbeta(d, beta)

          //logInfo("data was " + d.toArray.toList)
          //logInfo("array P was " + p.toList)

          // Evaluate whether the perplexity is within tolerance
          def Hdiff = h - logU

          var tries = 0
          while (Math.abs(Hdiff) > tol && tries < 50) {
            //If not, increase or decrease precision
            if (Hdiff > 0) {
              betamin = beta
              beta = if (betamax.isInfinite) beta * 2 else (beta + betamax) / 2
            } else {
              betamax = beta
              beta = if (betamin.isInfinite) beta / 2 else (beta + betamin) / 2
            }

            // Recompute the values
            val HP = Hbeta(d, beta)
            h = HP._1
            p = HP._2
            tries = tries + 1
          }

          //logInfo("array P is " + p.toList)

          (arr.map(_._1).zip(p.toArray).map { case (j, v) => MatrixEntry(i, j, v) }, beta)
      }
    p_betas.persist(StorageLevel.MEMORY_AND_DISK)
    logger.info("Mean value of sigma: " + p_betas.map(x => math.sqrt(1 / x._2)).mean)
    new CoordinateMatrix(p_betas.flatMap(_._1))
  }
}
