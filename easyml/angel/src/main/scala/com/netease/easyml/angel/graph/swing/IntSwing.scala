package com.netease.easyml.angel.graph.swing

import com.netease.easyml.angel.model.IntSimpleNeighborTableModel
import com.tencent.angel.graph.common.param.ModelContext
import com.tencent.angel.graph.utils.BatchIter
import com.tencent.angel.spark.context.PSContext
import com.tencent.angel.utils.ArrayUtils
import org.apache.spark.SparkContext
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.collection.OpenHashSet

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2021/7/17.
 */
class IntSwing(override val uid: String) extends Transformer with SwingParams {

  def this() = this(Identifiable.randomUID("swing"))

  override def transform(dataset: Dataset[_]): DataFrame = {
    val sc = dataset.sparkSession.sparkContext

    val psPartitionNum = getPSPartitionNum
    val partitionNum = getPartitionNum
    val batchSize = getBatchSize
    val pullBatchSize = getPullBatchSize
    val alpha = getAlpha
    val eps = getEps
    val power = getPower

    val u = $(srcNodeIdCol)
    val i = $(dstNodeIdCol)

    //read edges
    // user -> item pairs, no need to filter edges with the same id
    val edges = dataset.select(u, i).rdd.map(row => (row.getInt(0), row.getInt(1)))

    edges.persist(StorageLevel.DISK_ONLY)

    // push userItem neighbor table to ps
    val userItemNeighborTable = IntSwing.userItem2NeighborTable(edges, partitionNum)

    val (minUserId, maxUserId, numUsers, numUserItemEdges, maxUserItemDegree, minUserItemDegree) =
      IntSwing.stats(userItemNeighborTable)
    println(s"maxUserId: $maxUserId, minUserId: $minUserId, " +
      s"numUsers: $numUsers, numUserItemEdges: $numUserItemEdges, " +
      s"maxUserItemDegree: $maxUserItemDegree, " +
      s"minUserItemDegree: $minUserItemDegree")

    userItemNeighborTable.persist($(storageLevel))

    // discard certain items
    val itemUserNeighborTable = IntSwing.userItem2NeighborTable(edges.map(e => (e._2, e._1)), partitionNum)
    val (minItemId, maxItemId, numItems, numItemUserEdges, maxItemUserDegree, minItemUserDegree) =
      IntSwing.stats(itemUserNeighborTable)
    println(s"maxItemId: $maxItemId, minItemId: $minItemId, " +
      s"numItems: $numItems, numItemUserEdges: $numItemUserEdges, " +
      s"maxItemUserDegree: $maxItemUserDegree, " +
      s"minItemUserDegree: $minItemUserDegree")

    // Start PS and init the model
    println("start to run ps")
    val beforeStartPS = System.currentTimeMillis()
    PSContext.getOrCreate(SparkContext.getOrCreate())
    println(s"Starting ps cost ${System.currentTimeMillis() - beforeStartPS} ms")

    println(s"push neighbor tables to ps")
    val initTableStartTime = System.currentTimeMillis()
    val modelContext = new ModelContext(psPartitionNum, minUserId, maxUserId + 1, numUsers,
      "neighborTable", sc.hadoopConfiguration)
    val psModel = new IntSimpleNeighborTableModel(modelContext)
    psModel.init()
    userItemNeighborTable.mapPartitions { iter =>
      iter.grouped(batchSize).map(pairs => psModel.initNeighbors(pairs))
    }.count()
    println(s"initializing the neighbor table costs ${System.currentTimeMillis() - initTableStartTime} ms")
    //    val cpTableStartTime = System.currentTimeMillis()
    //    psModel.checkpoint()
    //    println(s"checkpoint of neighbor table costs ${System.currentTimeMillis() - cpTableStartTime} ms")

    // start calculating
    val result = itemUserNeighborTable.mapPartitionsWithIndex { case (partId, iter) =>
      IntSwing.runNeighborPartition(partId, iter, psModel, pullBatchSize, alpha, eps, power)
    }

    val retRDD = result.map { case (itemI, itemJ, score) =>
      Row.fromSeq(Seq[Any](itemI, itemJ, score))
    }

    dataset.sparkSession.createDataFrame(retRDD, transformSchema(dataset.schema))
  }

  override def transformSchema(schema: StructType): StructType = {
    StructType(Seq(
      StructField("item_i", IntegerType, nullable = false),
      StructField("item_j", IntegerType, nullable = false),
      StructField("score", FloatType, nullable = false)
    ))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

}

object IntSwing {

  def userItem2NeighborTable(edges: RDD[(Int, Int)],
                             partitionNum: Int): RDD[(Int, Array[Int])] = {
    edges.groupByKey(partitionNum).mapPartitionsWithIndex { (partId, iter) =>
      if (iter.nonEmpty) {
        iter.flatMap { case (src, group) =>
          Iterator.single((src, group.toArray.distinct.sorted))
        }
      } else {
        Iterator.empty
      }
    }
  }

  def userItem2NeighborTable(edges: RDD[(Int, Int)],
                             partitionNum: Int,
                             itemHashSet: OpenHashSet[Int]): RDD[(Int, Array[Int])] = {
    edges.groupByKey(partitionNum).mapPartitionsWithIndex { (partId, iter) =>
      if (iter.nonEmpty) {
        iter.flatMap { case (src, group) =>
          val g = group.toArray.distinct.map { x => if (itemHashSet.contains(x)) x else x * -1 }.sorted
          Iterator.single((src, g))
        }
      } else {
        Iterator.empty
      }
    }
  }

  def stats(neighborTable: RDD[(Int, Array[Int])]): (Long, Long, Long, Long, Long, Long) = {
    neighborTable.mapPartitions { iter =>
      var min = Long.MaxValue
      var max = Long.MinValue
      var numEdges = 0L
      var numNodes = 0L
      var maxOutDegree = Long.MinValue
      var minOutDegree = Long.MaxValue
      iter.foreach { case (src, neighbors) =>
        maxOutDegree = math.max(maxOutDegree, neighbors.length)
        minOutDegree = math.min(minOutDegree, neighbors.length)
        min = math.min(min, src)
        max = math.max(max, src)
        numNodes += 1
        numEdges += neighbors.length
      }
      Iterator.single((min, max, numNodes, numEdges, maxOutDegree, minOutDegree))
    }.reduce { case (c1, c2) =>
      (c1._1 min c2._1, c1._2 max c2._2, c1._3 + c2._3, c1._4 + c2._4, c1._5 max c2._5, c1._6 min c2._6)
    }
  }

  def runNeighborPartition(partId: Int, iter: Iterator[(Int, Array[Int])],
                           psModel: IntSimpleNeighborTableModel, batchSize: Int,
                           alpha: Float, eps: Float, power: Float): Iterator[(Int, Int, Float)] = {
    var startTs = System.currentTimeMillis()
    var computeStartTs = System.currentTimeMillis()

    BatchIter(iter, batchSize).flatMap { batchIter =>
      println(s"partition $partId: last batch cost ${System.currentTimeMillis() - startTs} ms, " +
        s"last computation cost ${System.currentTimeMillis() - computeStartTs} ms")
      startTs = System.currentTimeMillis()
      val pullNodes = new mutable.HashSet[Int]()
      batchIter.foreach { case (_, users) => pullNodes ++= users
      }
      val beforePullTs = System.currentTimeMillis()
      val psNeighborTable = psModel.getNeighbors(pullNodes.toArray)
      println(s"partition $partId: process ${batchIter.length} neighbor tables, " +
        s"pull ${pullNodes.size} nodes from ps, " +
        s"cost ${System.currentTimeMillis() - beforePullTs} ms")

      computeStartTs = System.currentTimeMillis()
      batchIter.flatMap { case (src, srcNbrs) =>
        if (srcNbrs.length > 10000) {
          println(s"partition $partId: item = $src, numBoughtUsers = ${srcNbrs.length}")
        }
        val user2Items = new Array[(Int, Array[Int])](srcNbrs.length)
        var i = 0
        while (i < srcNbrs.length) {
          user2Items(i) = (srcNbrs(i), psNeighborTable.get(srcNbrs(i)))
          i += 1
        }
        val res = swing(src, user2Items, alpha, eps, power)
        res.toIterator
      }
    }
  }

  def swing(item_i: Int, user2items: Array[(Int, Array[Int])], alpha: Float,
            eps: Float, power: Float): Array[(Int, Int, Float)] = {
    val itemSet = new mutable.HashSet[Int]()
    val itemFreq = new mutable.HashMap[Int, Int]()
    val userIndex = new mutable.HashMap[Int, Int]()
    val itemJ2Users = new mutable.HashMap[Int, mutable.HashSet[Int]]()
    var j = 0
    user2items.foreach { case (user, userItems) =>
      userItems.map { f =>
        if (f > item_i || f < 0) {
          itemSet.add(f)
          itemFreq.put(f, itemFreq.getOrElse(f, 0) + 1)
        }
      }
      userIndex.put(user, j)
      j += 1
    }

    val candidate = itemFreq.filter(_._2 > 1)
    user2items.map { case (user, userItems) =>
      userItems.map { f =>
        if (candidate.contains(f)) {
          if (itemJ2Users.contains(f)) {
            itemJ2Users(f).add(user)
          } else {
            val userSet = new mutable.HashSet[Int]()
            userSet.add(user)
            itemJ2Users.put(f, userSet)
          }
        }
      }
    }
    val result = new ArrayBuffer[(Int, Int, Float)]()
    for ((item, userSet) <- itemJ2Users) {
      var i = 0
      val users = userSet.toArray
      var simScore = 0f
      while (i < users.length - 1) {
        var j = i + 1
        val uItems = user2items(userIndex(users(i)))._2
        val wi = math.pow(uItems.length + eps, power)
        while (j < users.length) {
          val vItems = user2items(userIndex(users(j)))._2
          val wj = wi * math.pow(vItems.length + eps, power)
          val score = Score(uItems, vItems, alpha, wj.toFloat)
          simScore += score
          j += 1
        }
        i += 1
      }
      if (item < 0) {
        result += ((item_i, item * -1, simScore))
      } else {
        result += ((item_i, item, simScore))
        result += ((item, item_i, simScore))
      }
    }

    result.toArray
  }

  def Score(Iu: Array[Int], Iv: Array[Int], alpha: Float, weight: Float = 1.0f): Float = {
    //    1.0f / (alpha+Iu.toSet.intersect(Iv.toSet).size)
    weight / (alpha + ArrayUtils.intersectCount(Iu, Iv))
  }
}

