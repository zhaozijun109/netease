package com.netease.easyml.angel.graph.swing

import com.tencent.angel.graph.common.param.ModelContext
import com.tencent.angel.graph.model.neighbor.simple.SimpleNeighborTableModel
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2021/7/17.
 */
class SwingV2(override val uid: String) extends Transformer with SwingParams {

  def this() = this(Identifiable.randomUID("swing_v2"))

  override def transform(dataset: Dataset[_]): DataFrame = {
    val sc = dataset.sparkSession.sparkContext

    val psPartitionNum = getPSPartitionNum
    val batchSize = getBatchSize
    val pullBatchSize = getPullBatchSize
    val alpha = getAlpha
    val eps = getEps
    val power = getPower

    val u = $(srcNodeIdCol)
    val i = $(dstNodeIdCol)

    val uiDf = dataset.select(u, i)

    uiDf.persist(StorageLevel.DISK_ONLY)

    val u2isDf = uiDf.groupBy(u)
      .agg(collect_list(i).as(i))
      .select(col(u), sort_array(col(i)).as(i))

    u2isDf.persist($(storageLevel))

    val (minUserId, maxUserId, numUsers) = u2isDf.select(min(col(u)), max(col(u)), count("*"))
      .rdd.map(row => (row.getLong(0), row.getLong(1), row.getLong(2))).first()

    println(s"maxUserId: $maxUserId, minUserId: $minUserId, numUsers: $numUsers")

    println(s"push neighbor tables to ps")
    val initTableStartTime = System.currentTimeMillis()
    val modelContext = new ModelContext(psPartitionNum, minUserId, maxUserId + 1, numUsers,
      "neighborTable", sc.hadoopConfiguration)
    val psModel = new SimpleNeighborTableModel(modelContext)
    psModel.init()
    u2isDf.rdd
      .map(row => (row.getLong(0), row.getSeq[Long](1).toArray))
      .mapPartitions {
        iter =>
          iter.grouped(batchSize).map(pairs => psModel.initNeighbors(pairs))
      }.count()
    println(s"initializing the neighbor table costs ${
      System.currentTimeMillis() - initTableStartTime
    } ms")
    //    val cpTableStartTime = System.currentTimeMillis()
    //    psModel.checkpoint()
    //    println(s"checkpoint of neighbor table costs ${System.currentTimeMillis() - cpTableStartTime} ms")

    // start calculating
    val uuDf = uiDf.alias("t1").join(uiDf.alias("t2"), "i")
      .where("t1.u < t2.u")
      .select(col("t1.u").alias("u1"), col("t2.u").alias("u2"))
      .groupBy("u1", "u2")
      .agg(col("u1"), col("u2"))

    val retRDD = uuDf.rdd.map(row => (row.getLong(0), row.getLong(1)))
      .mapPartitionsWithIndex {
        case (partId, iter) =>
          SwingV2.runNeighborPartition(partId, iter, psModel, pullBatchSize,
            alpha, eps, power
          )
      }.filter(_._3 > 0)
      .map(it => ((it._1, it._2), it._3))
      .reduceByKey(_ + _)
      .flatMap(it => Array((it._1._1, it._1._2, it._2), (it._1._2, it._1._1, it._2)))
      .map {
        case (itemI, itemJ, score) =>
          Row.fromSeq(Seq[Any](itemI, itemJ, score))
      }

    dataset.sparkSession.createDataFrame(retRDD, transformSchema(dataset.schema))
  }

  override def transformSchema(schema: StructType): StructType = {
    StructType(Seq(
      StructField("item_i", LongType, nullable = false),
      StructField("item_j", LongType, nullable = false),
      StructField("score", FloatType, nullable = false)
    ))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

}

object SwingV2 {

  def runNeighborPartition(partId: Int, iter: Iterator[(Long, Long)],
                           psModel: SimpleNeighborTableModel, batchSize: Int,
                           alpha: Float, eps: Float, power: Float): Iterator[(Long, Long, Float)] = {
    var startTs = System.currentTimeMillis()
    var computeStartTs = System.currentTimeMillis()

    iter.grouped(batchSize).flatMap { batch =>
      println(s"partition $partId: last batch cost ${System.currentTimeMillis() - startTs} ms, " +
        s"last computation cost ${System.currentTimeMillis() - computeStartTs} ms")
      startTs = System.currentTimeMillis()
      val pullNodes = batch.flatMap(it => Array(it._1, it._2)).toSet
      val beforePullTs = System.currentTimeMillis()
      val psNeighborTable = psModel.getNeighbors(pullNodes.toArray)
      println(s"partition $partId: process ${batch.length} neighbor tables, " +
        s"pull ${pullNodes.size} nodes from ps, " +
        s"cost ${System.currentTimeMillis() - beforePullTs} ms")

      computeStartTs = System.currentTimeMillis()
      batch.flatMap { case (userI, userJ) =>
        val itemsI = psNeighborTable.get(userI)
        val itemsJ = psNeighborTable.get(userJ)
        val res = swing(itemsI, itemsJ, alpha, eps, power)
        res.toIterator
      }
    }
  }

  def swing(uItems: Array[Long], vItems: Array[Long], alpha: Float, eps: Float, power: Float): Array[(Long, Long, Float)] = {
    val result = new ArrayBuffer[(Long, Long, Float)]()

    val vItemsSet = vItems.toSet
    val common = uItems.filter(vItemsSet.contains).sorted
    val wij = math.pow(uItems.length + eps, power) * math.pow(vItems.length + eps, power)
    val score = wij.toFloat / (alpha + common.length)
    var i = 0
    while (i < common.length) {
      var j = i + 1
      while (j < common.length) {
        result.append((common(i), common(j), score))
        j += 1
      }
      i += 1
    }
    result.toArray
  }

}

