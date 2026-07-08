package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.{UserDefinedCmd, VoidUserDefinedCmd}
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.util.SchemaUtils
import com.netease.easyudf.udf.util.{MilvusUtil, Utils}
import com.netease.easyudf.util.Utils.{isBlank, isNotBlank}
import io.milvus.common.clientenum.ConsistencyLevelEnum
import io.milvus.grpc.{DataType, LoadState}
import io.milvus.param.collection._
import io.milvus.param.dml.{InsertParam, SearchParam}
import io.milvus.param.index.CreateIndexParam
import io.milvus.param.partition.CreatePartitionParam
import io.milvus.param.{IndexType, MetricType}
import io.milvus.response.{GetCollStatResponseWrapper, SearchResultsWrapper}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, DoubleType, FloatType, IntegerType, LongType, StringType, StructField, StructType, DataType => SparkDataType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import java.util
import scala.collection.JavaConverters._

case class MilvusInsertArgs(input: String, collectionName: String,
                            primaryCol: String = "id", vectorCol: String = "vector", partitionCol: String = "",
                            indexType: String = IndexType.HNSW.toString, metricType: String = MetricType.L2.toString, extraParams: String = null,
                            dimension: Int = 0, replicaNumber: Int = 0, shardsNum: Int = 6, connectionNum: Int = 10,
                            batchSize: Int = 128, overwrite: Boolean = false, sleep: Int = 60, checkRowCount: Boolean = false, normalize: Boolean = true,
                            repartition: Boolean = true, load: Boolean = false)

class MilvusInsert extends VoidUserDefinedCmd[MilvusInsertArgs] {

  override def run(spark: SparkSession, args: MilvusInsertArgs): Unit = {
    val milvusClient = MilvusUtil.getOrCreateClient()
    var df = spark.table(args.input)

    // drop collection
    val r = milvusClient.hasCollection(HasCollectionParam.newBuilder.withCollectionName(args.collectionName).build)
    MilvusUtil.checkStatus(r)

    if (r.getData) {
      if (args.overwrite) {
        MilvusUtil.checkStatus(milvusClient.dropCollection(
          DropCollectionParam.newBuilder
            .withCollectionName(args.collectionName)
            .build
        ))
        Thread.sleep(args.sleep * 1000)
      } else {
        throw new Exception(s"Collection ${args.collectionName} already exists.")
      }
    }

    df = df.persist(StorageLevel.MEMORY_AND_DISK)
    val count = df.count()

    // build collection
    val dimension = if (args.dimension < 1) {
      df.select(size(col(args.vectorCol))).first().getInt(0)
    } else {
      args.dimension
    }
    val builder = CreateCollectionParam.newBuilder
      .withCollectionName(args.collectionName)
      .withShardsNum(args.shardsNum)

    val names = new util.ArrayList[String]
    val dtypes = new util.ArrayList[DataType]
    df.schema.fields.foreach(field => {
      val name = field.name
      var dtype = Milvus.toMilvusDtype(field.dataType)

      if (name.equals(args.primaryCol) && dtype != DataType.VarChar) {
        dtype = DataType.Int64
      }

      names.add(name)
      dtypes.add(dtype)

      if (!name.equals(args.partitionCol)) {
        val typeBuilder = FieldType.newBuilder.withName(name).withDataType(dtype)
        if (name.equals(args.primaryCol)) {
          typeBuilder.withPrimaryKey(true).withAutoID(false)
        } else if (name.equals(args.vectorCol)) {
          typeBuilder.withDimension(dimension)
        }

        if (dtype == DataType.VarChar) {
          typeBuilder.withMaxLength(1024)
        }
        val fieldType = typeBuilder.build
        builder.addFieldType(fieldType)
      }
    })

    val createCollectionReq = builder.build

    MilvusUtil.checkStatus(milvusClient.createCollection(createCollectionReq))

    // build index
    val createIndexBuilder = CreateIndexParam.newBuilder
      .withCollectionName(args.collectionName)
      .withFieldName(args.vectorCol)
      .withIndexType(IndexType.valueOf(args.indexType))
      .withMetricType(MetricType.valueOf(args.metricType))
    val extraParams = if (args.extraParams == null) {
      Milvus.EXTRA_PARAMS.getOrElse(IndexType.valueOf(args.indexType), "")
    } else {
      args.extraParams
    }
    if (isNotBlank(extraParams)) {
      createIndexBuilder.withExtraParam(extraParams)
    }
    MilvusUtil.checkStatus(milvusClient.createIndex(createIndexBuilder.build))

    if (isNotBlank(args.partitionCol)) {
      val partitions = df.select(args.partitionCol).rdd.map(row => row.getString(0)).distinct().collect()
      partitions.foreach(p => MilvusUtil.checkStatus(milvusClient.createPartition(
        CreatePartitionParam.newBuilder
          .withCollectionName(args.collectionName)
          .withPartitionName(p)
          .build
      )))
      println(s"Num Partitions = ${partitions.length}, ${partitions.mkString(",")}")
      if (args.repartition) {
        df = df.repartition(partitions.length, col(args.partitionCol))
      }
    }

    val env = MilvusUtil.getENV
    // insert data
    if (isNotBlank(args.partitionCol)) {
      df.rdd.foreachPartition(iter => {
        MilvusUtil.setEnv(env)
        iter.grouped(args.batchSize).foreach(rows => {
          val milvusClient = MilvusUtil.getOrCreateClient(args.connectionNum)
          rows.map(row => (row.getAs[String](args.partitionCol), row)).groupBy(_._1)
            .map {
              case (partition, rows) =>
                val fields = new util.ArrayList[InsertParam.Field]
                (0 until dtypes.size()).foreach(i => {
                  var dataArray = rows.map(row => row._2.get(i))
                  if (!names.get(i).equals(args.partitionCol)) {
                    if (dtypes.get(i) == DataType.FloatVector) {
                      dataArray = dataArray.map(a => {
                        val floats = a.asInstanceOf[Seq[Any]].map(Utils.toFloat)
                        if (args.normalize)
                          Milvus.l2(floats).asJava
                        else
                          floats.map(_.toFloat).asJava
                      })
                    } else if (names.get(i).equals(args.primaryCol) && dtypes.get(i) != DataType.VarChar) {
                      dataArray = dataArray.map(Utils.toLong)
                    }
                    fields.add(new InsertParam.Field(names.get(i), dataArray.asJava))
                  }
                })

                val insertBuilder = InsertParam.newBuilder.withCollectionName(args.collectionName).withFields(fields)
                insertBuilder.withPartitionName(partition)
                val insertParam = insertBuilder.build
                MilvusUtil.checkStatus(milvusClient.insert(insertParam))
            }
        })
      })
    }
    else {
      df.rdd.foreachPartition(iter => {
        MilvusUtil.setEnv(env)
        iter.grouped(args.batchSize).foreach(rows => {
          val milvusClient = MilvusUtil.getOrCreateClient(args.connectionNum)
          val fields = new util.ArrayList[InsertParam.Field]
          (0 until dtypes.size()).foreach(i => {
            var dataArray = rows.map(row => row.get(i))
            if (dtypes.get(i) == DataType.FloatVector) {
              dataArray = dataArray.map(a => {
                val floats = a.asInstanceOf[Seq[Any]].map(Utils.toFloat)
                if (args.normalize)
                  Milvus.l2(floats).asJava
                else
                  floats.map(_.toFloat).asJava
              })
            } else if (names.get(i).equals(args.primaryCol) && dtypes.get(i) != DataType.VarChar) {
              dataArray = dataArray.map(Utils.toLong)
            }
            fields.add(new InsertParam.Field(names.get(i), dataArray.asJava))
          })

          val insertBuilder = InsertParam.newBuilder.withCollectionName(args.collectionName).withFields(fields)
          val insertParam = insertBuilder.build
          MilvusUtil.checkStatus(milvusClient.insert(insertParam))
        }
        )
      })
    }

    MilvusUtil.checkStatus(milvusClient.flush(FlushParam.newBuilder.addCollectionName(args.collectionName).build))

    if (args.replicaNumber > 0) {
      MilvusUtil.checkStatus(milvusClient.loadCollection(
        LoadCollectionParam.newBuilder
          .withCollectionName(args.collectionName)
          .withReplicaNumber(args.replicaNumber)
          .build
      ))
    }

    if (args.checkRowCount) {
      // check correctness
      var preCnt = -1L
      var rowCnt = Milvus.getRowCount(args.collectionName)
      while (rowCnt != preCnt) {
        preCnt = rowCnt
        println(s"Wait collection build finished, cur_count = $rowCnt, prev_count = $preCnt, sleep ${args.sleep}s")
        Thread.sleep(args.sleep * 1000)
        rowCnt = Milvus.getRowCount(args.collectionName)
      }

      val msg = s"Dataframe count = $count, collection count = $rowCnt"
      println(msg)
      assert(count == rowCnt, msg)
    } else {
      val msg = s"Dataframe count = $count"
      println(msg)
    }

    if (args.load) {
      Milvus.load(args.collectionName, args.replicaNumber, args.sleep)
    }
  }

}

case class MilvusSearchArgs(input: String, collectionName: String,
                            primaryCol: String = "id", vectorCol: String = "vector", partitionCol: String = "", filterNotInCol: String = "", fieldCol: String = "",
                            topK: Int = 100, metricType: String = MetricType.L2.toString, params: String = "", threshold: Double = 0.0,
                            batchSize: Int = 128, replicaNumber: Int = 6, numPartitions: Int = 0,
                            sleep: Int = 60, connectionNum: Int = 10, ignoreError: Boolean = true, normalize: Boolean = true)

class MilvusSearch extends UserDefinedCmd[MilvusSearchArgs] {

  override def apply(spark: SparkSession, args: MilvusSearchArgs): DataFrame = {
    var df = spark.table(args.input)

    if (args.numPartitions > 0) {
      df = SparkUtil.repartition(df, args.numPartitions)
    }

    // load collection
    Milvus.load(args.collectionName, args.replicaNumber, args.sleep)

    // search
    val vectorIdx = df.columns.indexOf(args.vectorCol)
    val metricType = MetricType.valueOf(args.metricType)

    var fields = df.schema.fields
    fields = fields.filterNot(it => it.name.equals(args.vectorCol))
    if (isNotBlank(args.partitionCol)) {
      fields = fields.filterNot(it => it.name.equals(args.partitionCol))
    }
    if (isNotBlank(args.filterNotInCol)) {
      fields = fields.filterNot(it => it.name.equals(args.filterNotInCol))
    }

    val fieldCol = if (isBlank(args.fieldCol)) args.primaryCol else args.fieldCol
    val env = MilvusUtil.getENV
    if (isBlank(args.partitionCol) && isBlank(args.filterNotInCol)) {
      val rdd = df.rdd.mapPartitions(iter => {
        MilvusUtil.setEnv(env)
        Logger.getLogger("io.milvus.client").setLevel(Level.ERROR)
        iter.grouped(args.batchSize).flatMap(rows => {
          val milvusClient = MilvusUtil.getOrCreateClient(args.connectionNum)
          val vectors = rows.map(row => row.getAs[Seq[Any]](vectorIdx)).map(it => {
            val floats = it.map(Utils.toFloat)
            if (args.normalize)
              Milvus.l2(floats).asJava
            else
              floats.map(_.toFloat).asJava
          }).asJava

          val builder = SearchParam.newBuilder
            .withCollectionName(args.collectionName)
            .withMetricType(metricType)
            .withTopK(args.topK)
            .withVectors(vectors)
            .withVectorFieldName(args.vectorCol)
            .withOutFields(Seq(fieldCol).asJava)
            .withConsistencyLevel(ConsistencyLevelEnum.EVENTUALLY)

          if (isNotBlank(args.params)) {
            builder.withParams(args.params)
          }
          val searchParam = builder.build
          val respSearch = milvusClient.search(searchParam)
          MilvusUtil.checkStatus(respSearch, args.ignoreError)

          val wrapperSearch = new SearchResultsWrapper(respSearch.getData.getResults)

          (0 until vectors.size()).map(i => {
            val ids = wrapperSearch.getFieldData(fieldCol, i)
            val scores = wrapperSearch.getIDScore(i)
            val labels = ids.asScala.zip(scores.asScala.map(_.getScore))
              .map(it => (it._1, Milvus.convertScore(metricType, it._2)))
              .filter(_._2 >= args.threshold).map(it => it._1 + ":" + it._2)
            Row.fromSeq(rows(i).toSeq.zipWithIndex.filterNot(_._2 == vectorIdx).map(_._1) :+ labels)
          })
        })
      })
      spark.createDataFrame(rdd, StructType(fields :+ StructField("labels", ArrayType(StringType))))
    } else {
      val ptIdx = df.columns.indexOf(args.partitionCol)
      val filterNotIdx = df.columns.indexOf(args.filterNotInCol)
      var stringType = true
      if (isNotBlank(args.filterNotInCol)) {
        SchemaUtils.checkArrayType(df.schema, args.filterNotInCol)
        stringType = SchemaUtils.isStringArrayType(df.schema, args.filterNotInCol)
      }
      val rdd = df.rdd.flatMap(row => {
        MilvusUtil.setEnv(env)
        Logger.getLogger("io.milvus.client").setLevel(Level.ERROR)
        val milvusClient = MilvusUtil.getOrCreateClient()
        var size = 1

        val floats = row.getAs[Seq[Any]](vectorIdx).map(Utils.toFloat)
        val vector = if (args.normalize) Milvus.l2(floats).asJava else floats.map(_.toFloat).asJava

        val partitionNames = if (isNotBlank(args.partitionCol)) {
          val partitionNames = row.getString(ptIdx).split(",").toSeq.asJava
          size = partitionNames.size()
          partitionNames
        } else {
          null
        }

        val vectors = (0 until size).map(_ => vector).asJava

        val builder = SearchParam.newBuilder
          .withCollectionName(args.collectionName)
          .withMetricType(metricType)
          .withTopK(args.topK)
          .withVectors(vectors)
          .withVectorFieldName(args.vectorCol)
          .withOutFields(Seq(fieldCol).asJava)
          .withConsistencyLevel(ConsistencyLevelEnum.EVENTUALLY)

        if (isNotBlank(args.filterNotInCol)) {
          val seq = row.getAs[Seq[Any]](filterNotIdx)
          if (seq != null && seq.nonEmpty) {
            val expr = seq.map(it => if (stringType) s"'$it'" else it.toString).mkString(",")
            builder.withExpr(s"${args.primaryCol} not in [$expr]")
          }
        }

        if (isNotBlank(args.partitionCol)) {
          builder.withPartitionNames(partitionNames)
        }
        if (isNotBlank(args.params)) {
          builder.withParams(args.params)
        }
        val searchParam = builder.build
        val respSearch = milvusClient.search(searchParam)
        MilvusUtil.checkStatus(respSearch)

        val wrapperSearch = new SearchResultsWrapper(respSearch.getData.getResults)
        val seq = row.toSeq.zipWithIndex.filterNot(it => it._2 == vectorIdx || it._2 == filterNotIdx || it._2 == ptIdx).map(_._1)
        (0 until size).map(i => {
          val ids = wrapperSearch.getFieldData(fieldCol, i)
          val scores = wrapperSearch.getIDScore(i)
          val labels = ids.asScala.zip(scores.asScala.map(_.getScore))
            .map(it => (it._1, Milvus.convertScore(metricType, it._2)))
            .filter(_._2 >= args.threshold).map(it => it._1 + ":" + it._2)
          if (isNotBlank(args.partitionCol)) {
            Row.fromSeq(seq ++ Seq(partitionNames.get(i), labels))
          } else {
            Row.fromSeq(seq :+ labels)
          }
        })
      })
      var structType = StructType(fields)
      if (isNotBlank(args.partitionCol)) {
        structType = SchemaUtils.appendColumn(structType, StructField(args.partitionCol, StringType))
      }
      structType = SchemaUtils.appendColumn(structType, StructField("labels", ArrayType(StringType)))
      spark.createDataFrame(rdd, structType).filter(size(col("labels")).gt(0))
    }
  }
}


case class MilvusCollectionArgs(collectionName: String)

class MilvusDropCollection extends VoidUserDefinedCmd[MilvusCollectionArgs] {

  override def run(spark: SparkSession, args: MilvusCollectionArgs): Unit = {
    val milvusClient = MilvusUtil.getOrCreateClient
    MilvusUtil.checkStatus(
      milvusClient.dropCollection(DropCollectionParam.newBuilder.withCollectionName(args.collectionName).build)
    )
  }
}


class MilvusReleaseCollection extends VoidUserDefinedCmd[MilvusCollectionArgs] {

  override def run(spark: SparkSession, args: MilvusCollectionArgs): Unit = {
    val milvusClient = MilvusUtil.getOrCreateClient
    MilvusUtil.checkStatus(
      milvusClient.releaseCollection(ReleaseCollectionParam.newBuilder.withCollectionName(args.collectionName).build)
    )
  }
}


case class MilvusSettingArgs(env: String)

class MilvusSetting extends VoidUserDefinedCmd[MilvusSettingArgs] {

  override def run(spark: SparkSession, args: MilvusSettingArgs): Unit = {
    MilvusUtil.setEnv(args.env)
  }
}


object Milvus {

  val EXTRA_PARAMS: Map[IndexType, String] = Map(IndexType.HNSW -> "{\"M\":32,\"efConstruction\":40}")

  def l2(vector: Seq[java.lang.Float]): Seq[Float] = {
    val square = vector.map(it => it * it)
    val sqrt = Math.sqrt(square.sum)
    if (sqrt == 0) {
      vector.map(_.toFloat)
    } else {
      vector.map(it => (it / sqrt).toFloat)
    }
  }


  def toMilvusDtype(dataType: SparkDataType): DataType = {
    dataType match {
      case IntegerType => DataType.Int32
      case LongType => DataType.Int64
      case FloatType => DataType.Float
      case DoubleType => DataType.Double
      case ArrayType(_, _) => DataType.FloatVector
      case _ => DataType.VarChar
    }
  }

  def convertScore(metricType: MetricType, score: Float): Float = {
    if (metricType == MetricType.L2) {
      1 - score / 2
    } else {
      score
    }
  }

  def getLoadState(collectionName: String): LoadState = {
    val milvusClient = MilvusUtil.getOrCreateClient()
    val param = GetLoadStateParam.newBuilder.withCollectionName(collectionName).build
    val response = milvusClient.getLoadState(param)
    MilvusUtil.checkStatus(response)
    response.getData.getState
  }

  def getRowCount(collectionName: String): Long = {
    val milvusClient = MilvusUtil.getOrCreateClient()
    val respCollectionStatistics = milvusClient.getCollectionStatistics(
      GetCollectionStatisticsParam.newBuilder.withCollectionName(collectionName).build)
    val wrapperCollectionStatistics = new GetCollStatResponseWrapper(respCollectionStatistics.getData)
    wrapperCollectionStatistics.getRowCount
  }

  def load(collectionName: String, replicaNumber: Int, sleep: Int): Unit = {
    val milvusClient = MilvusUtil.getOrCreateClient()
    var state = Milvus.getLoadState(collectionName)
    if (state == LoadState.LoadStateNotLoad) {
      MilvusUtil.checkStatus(milvusClient.loadCollection(
        LoadCollectionParam.newBuilder
          .withCollectionName(collectionName)
          .withReplicaNumber(replicaNumber)
          .build
      ))
      state = Milvus.getLoadState(collectionName)
      while (state != LoadState.LoadStateLoaded) {
        println(s"Collection load state = $state, sleep ${sleep}s")
        Thread.sleep(sleep * 1000)
        state = Milvus.getLoadState(collectionName)
      }
    }
    assert(state == LoadState.LoadStateLoaded, s"Unexpected load state $state")
  }
}
