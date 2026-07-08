package com.netease.easyml.graphx

import java.io.Serializable

import com.netease.easyml.common.util.{ConvertUtil, SparkUtil}
import org.apache.spark.graphx.{Edge, EdgeTriplet, Graph}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasOutputCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable
import scala.util.Try

/**
 * Created by linjiuning on 2020/6/22.
 * Random walk of node2vec, based on https://github.com/aditya-grover/node2vec/tree/master/node2vec_spark
 */

case class NodeAttr(var neighbors: Array[(Int, Double)] = Array.empty[(Int, Double)],
                    var path: Array[Int] = Array.empty[Int]) extends Serializable

case class EdgeAttr(var dstNeighbors: Array[Int] = Array.empty[Int],
                    var J: Array[Int] = Array.empty[Int],
                    var Q: Array[Double] = Array.empty[Double]) extends Serializable

trait RandomWalkParams extends Params with HasOutputCol with HasWeightCol {

  val srcCol: Param[String] = new Param[String](this, "srcCol", "src column name")

  def getSrcCol: String = $(srcCol)

  val dstCol: Param[String] = new Param[String](this, "dstCol", "dst column name")

  def getDstCol: String = $(dstCol)

  val walkLength: IntParam = new IntParam(this, "walkLength", "walk length")

  def getWalkLength: Int = $(walkLength)

  val numWalks: IntParam = new IntParam(this, "numWalks", "num walks")

  def getNumWalks: Int = $(numWalks)

  val p: DoubleParam = new DoubleParam(this, "p", "return parameter p")

  def getP: Double = $(p)

  val q: DoubleParam = new DoubleParam(this, "q", "in-out parameter q")

  def getQ: Double = $(q)

  val degree: IntParam = new IntParam(this, "degree", "max degree of vertex")

  def getDegree: Int = $(degree)

  val directed: BooleanParam = new BooleanParam(this, "directed", "whether graph is directed or not")

  def getDirected: Boolean = $(directed)

  val numPartition: IntParam = new IntParam(this, "numPartition", "num partition")

  def getNumPartition: Int = $(numPartition)

  val indexed: BooleanParam = new BooleanParam(this, "indexed", "whether nodes are indexed or not")

  def getIndexed: Boolean = $(indexed)

  val mergeEdge: BooleanParam = new BooleanParam(this, "mergeEdge", "whether to merge same edge or not")

  def getMergeEdge: Boolean = $(mergeEdge)

  val memoryOnly: BooleanParam = new BooleanParam(this, "memoryOnly", "whether persist on memory only")

  def getMemoryOnly: Boolean = $(memoryOnly)

  setDefault(srcCol -> "src", dstCol -> "dst", weightCol -> "weight", outputCol -> "path",
    walkLength -> 80, numWalks -> 10, p -> 1.0, q -> 1.0, degree -> 30,
    directed -> false, numPartition -> 200, indexed -> false, mergeEdge -> true, memoryOnly -> true)
}

class RandomWalk(override val uid: String) extends Transformer with RandomWalkParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("randomWalk"))

  @transient private var vocab: Array[String] = _
  @transient private var vocabHash = mutable.HashMap.empty[String, Int]
  @transient private var storageLevel: StorageLevel = _

  def setSrcCol(value: String): this.type = set(srcCol, value)

  def setDstCol(value: String): this.type = set(dstCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setWalkLength(value: Int): this.type = set(walkLength, value)

  def setNumWalks(value: Int): this.type = set(numWalks, value)

  def setP(value: Double): this.type = set(p, value)

  def setQ(value: Double): this.type = set(q, value)

  def setDegree(value: Int): this.type = set(degree, value)

  def setDirected(value: Boolean): this.type = set(directed, value)

  def setNumPartition(value: Int): this.type = set(numPartition, value)

  def setIndexed(value: Boolean): this.type = set(indexed, value)

  def setMergeEdge(value: Boolean): this.type = set(mergeEdge, value)

  def setMemoryOnly(value: Boolean): this.type = set(memoryOnly, value)

  // src[String|Long], dst[String|Long], weight(opt)[String|Double] --> path[Array[String]]
  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema)
    val spark = dataset.sparkSession

    if (getMemoryOnly) {
      storageLevel = StorageLevel.MEMORY_ONLY
    } else {
      storageLevel = StorageLevel.MEMORY_AND_DISK
    }

    var graph = createGraph(dataset)
    graph = initTransitionProb(graph)
    val randomWalkPaths = randomWalk(spark, graph)
    val rdd = if (vocab != null) {
      val bcVocab = dataset.sparkSession.sparkContext.broadcast(vocab)
      val rdd = randomWalkPaths.toDF().rdd.mapPartitions(iter => {
        iter.map(row => row.getSeq[Int](0).map(i => Try(bcVocab.value(i)).getOrElse("null")))
      })
      rdd
    } else {
      randomWalkPaths.toDF().rdd.map(row => row.getSeq[Int](0).map(i => i.toString))
    }
    spark.createDataFrame(rdd.map(it => Row(it)), outputSchema)
  }

  private def learnVocab(dataset: RDD[(String, String)]): Unit = {
    val words = dataset.flatMap(x => Array(x._1, x._2))

    vocab = words
      .distinct()
      .collect()
    println(s"Vocab size = ${vocab.length}")

    val vocabSize = vocab.length
    var a = 0
    while (a < vocabSize) {
      vocabHash += vocab(a) -> a
      a += 1
    }
  }

  private def indexGraph(dataset: Dataset[_]): RDD[(Int, Int, Double)] = {
    val df = dataset.select(getSrcCol, getDstCol, getWeightCol)
    if (getIndexed) {
      df.rdd.map(row => {
        val src = ConvertUtil.toInt(row.get(0))
        val dst = ConvertUtil.toInt(row.get(1))
        val weight = ConvertUtil.toDouble(row.get(2))
        (src, dst, weight)
      })
    } else {
      val rdd = df.rdd.map(row => {
        val src = ConvertUtil.toString(row.get(0))
        val dst = ConvertUtil.toString(row.get(1))
        val weight = ConvertUtil.toDouble(row.get(2))
        (src, dst, weight)
      }).persist(storageLevel)
      learnVocab(rdd.map(it => (it._1, it._2)))
      val bcVocabHash = dataset.sparkSession.sparkContext.broadcast(vocabHash)
      rdd.mapPartitions(iter => {
        iter.map(it => (bcVocabHash.value(it._1), bcVocabHash.value(it._2), it._3))
      })
    }
  }

  private def createGraph(dataset: Dataset[_]): Graph[NodeAttr, EdgeAttr] = {
    val maxDegree = getDegree
    val edgeCreator = if (getDirected) {
      GraphOps.createDirectedEdge
    } else {
      GraphOps.createUndirectedEdge
    }

    val newDs = if (!dataset.columns.contains(getWeightCol)) {
      dataset.withColumn(getWeightCol, lit(1.0))
    } else {
      dataset
    }

    var inputTriplets = indexGraph(newDs)

    if (getMergeEdge) {
      // TODO: this procedure is different from origin implement
      inputTriplets = inputTriplets.map { case (srcId, dstId, weight) => ((srcId, dstId), weight) }
        .reduceByKey(_ + _).map { case ((srcId, dstId), weight) => (srcId, dstId, weight) }
    }

    val indexedNodes = inputTriplets.flatMap { case (srcId, dstId, weight) =>
      edgeCreator.apply(srcId, dstId, weight)
    }.reduceByKey(_ ++ _).map { case (nodeId, neighbors: Array[(Int, Double)]) =>
      var neighbors_ = neighbors
      if (neighbors_.length > maxDegree) {
        neighbors_ = neighbors.sortWith { case (left, right) => left._2 > right._2 }.slice(0, maxDegree)
      }

      (nodeId.toLong, NodeAttr(neighbors = neighbors_.distinct))
    }.repartition(getNumPartition)

    val indexedEdges = indexedNodes.flatMap { case (srcId, clickNode) =>
      clickNode.neighbors.map { case (dstId, _) =>
        Edge(srcId, dstId, EdgeAttr())
      }
    }.repartition(getNumPartition)

    Graph(indexedNodes, indexedEdges)
  }

  private def initTransitionProb(graph: Graph[NodeAttr, EdgeAttr]): Graph[NodeAttr, EdgeAttr] = {
    val p_ = getP
    val q_ = getQ
    graph.mapVertices[NodeAttr] { case (vertexId, clickNode) =>
      val (j, q) = GraphOps.setupAlias(clickNode.neighbors)
      val nextNodeIndex = GraphOps.drawAlias(j, q)
      clickNode.path = Array(vertexId.toInt, clickNode.neighbors(nextNodeIndex)._1)

      clickNode
    }.mapTriplets { edgeTriplet: EdgeTriplet[NodeAttr, EdgeAttr] =>
      val (j, q) = GraphOps.setupEdgeAlias(p_, q_)(edgeTriplet.srcId.toInt, edgeTriplet.srcAttr.neighbors, edgeTriplet.dstAttr.neighbors)
      edgeTriplet.attr.J = j
      edgeTriplet.attr.Q = q
      edgeTriplet.attr.dstNeighbors = edgeTriplet.dstAttr.neighbors.map(_._1)

      edgeTriplet.attr
    }
  }

  private def randomWalk(spark: SparkSession, graph: Graph[NodeAttr, EdgeAttr]): DataFrame = {
    import spark.implicits._
    val edge2attr = graph.triplets.map { edgeTriplet =>
      val attr = edgeTriplet.attr
      val J = attr.J
      val Q = attr.Q
      val neighbors = attr.dstNeighbors
      (s"${edgeTriplet.srcId}_${edgeTriplet.dstId}", J, Q, neighbors)
    }.repartition(getNumPartition)
      .toDF("edge", "J", "Q", "neighbors")
      .persist(storageLevel)
    edge2attr.count()

    val initRandomWalk = graph.vertices.map { case (_, clickNode) =>
      clickNode.path
    }.toDF("path")
      .persist(storageLevel)
    initRandomWalk.count()

    graph.unpersist()
    graph.edges.unpersist()
    graph.vertices.unpersist()

    val edgeUdf = udf(RandomWalk.edgeFromPathUdf _)

    var randomWalks: DataFrame = null
    val numWalks = getNumWalks
    val walkLength = getWalkLength
    for (epoch <- 0 until numWalks) {
      println(s"NumWalks: ${epoch + 1} / $numWalks")
      var prevWalk: DataFrame = null
      var randomWalk = initRandomWalk
      for (i <- 0 until walkLength) {
        prevWalk = randomWalk
        val randomWalkDf = randomWalk.withColumn("edge", edgeUdf(col("path")))
        randomWalk = edge2attr.join(randomWalkDf, edge2attr("edge") === randomWalkDf("edge"))
          .rdd
          .map(row => {
            val pathArr = row.getAs[Seq[Int]]("path").toArray
            val J = row.getAs[Seq[Int]]("J").toArray
            val Q = row.getAs[Seq[Double]]("Q").toArray
            val neighbors = row.getAs[Seq[Int]]("neighbors")

            try {
              val nextNodeIndex = GraphOps.drawAlias(J, Q)
              val nextNodeId = neighbors(nextNodeIndex)

              pathArr :+ nextNodeId
            } catch {
              case e: Exception => throw new RuntimeException(e.getMessage)
            }
          }).toDF("path")
        //        randomWalk = randomWalk.map { case (srcNodeId, pathArr) =>
        //          val prevNodeId = pathArr(pathArr.length - 2)
        //          val currentNodeId = pathArr.last
        //
        //          (s"${prevNodeId}_$currentNodeId", (srcNodeId, pathArr))
        //        }.join(edge2attr).map { case (_, ((srcNodeId, pathArr), attr)) =>
        //          try {
        //            val nextNodeIndex = GraphOps.drawAlias(attr.J, attr.Q)
        //            val nextNodeId = attr.dstNeighbors(nextNodeIndex)
        //
        //            (srcNodeId, pathArr :+ nextNodeId)
        //          } catch {
        //            case e: Exception => throw new RuntimeException(e.getMessage)
        //          }
        //        }
        randomWalk.persist(storageLevel)
        randomWalk.count()
        // avoid unpersist initRandomWalk
        if (i > 0) {
          SparkUtil.unpersist(prevWalk)
        }
      }
      if (randomWalks == null) {
        randomWalks = randomWalk
      } else {
        val preRandomWalks = randomWalks
        randomWalks = randomWalks.union(randomWalk)
        randomWalks.persist(storageLevel)
        randomWalks.count()
        SparkUtil.unpersist(preRandomWalks)
      }
    }
    randomWalks
  }

  override def copy(extra: ParamMap): RandomWalk = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val pathCol = StructField(getOutputCol, ArrayType(StringType, containsNull = false), nullable = false)
    StructType(Array(pathCol))
  }

}

object RandomWalk extends DefaultParamsReadable[RandomWalk] {
  def edgeFromPathUdf(pathArr: Seq[Int]): String = {
    val prevNodeId = pathArr(pathArr.length - 2)
    val currentNodeId = pathArr.last

    s"${prevNodeId}_$currentNodeId"
  }

  override def load(path: String): RandomWalk = super.load(path)
}