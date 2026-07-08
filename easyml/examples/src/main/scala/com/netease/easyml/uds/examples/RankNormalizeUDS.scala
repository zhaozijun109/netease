package com.netease.easyml.uds.examples

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.google.common.collect.Maps
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, size, when}
import org.apache.spark.sql.types.StructType
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2021/3/9.
 * Calculate normalize parameters
 *
 * params:
 * input: input table
 * output: output dir
 * config: feature config file
 */
case class RankNormalizeArgs(input: String, output: String, config: String,
                             startDay: String = "", endDay: String = "",
                             saveAsFile: Boolean = true, recordType: String = EXAMPLE)

object RankNormalizeUDS extends UDS[RankNormalizeArgs] {
  val NORM_FNS: Array[String] = Array("min_max", "standard")

  def run(spark: SparkSession, args: RankNormalizeArgs): Unit = {
    val configs = readConfig(args.config, nameAsEmbedName = true).filter(it => NORM_FNS.contains(it.normalizer))

    val keys = configs.map(_.name)
    val isTfrecord = IOUtil.isDirectory(args.input)
    val df = if (isTfrecord) {
      loadTfRecords(spark, args.input, startDay = args.startDay, endDay = args.endDay, recordType = args.recordType)
        .select(keys.map(col): _*)
    } else {
      SparkUtil.loadFromTable(spark, args.input, keys = keys.mkString(","), startDay = args.startDay, endDay = args.endDay)
    }

    val groups = configs.groupBy(_.embedName)
    val counts = groups.mapValues(_.length).toArray.map(it => (it._2, it._1)).groupBy(_._1).mapValues(_.map(_._2))

    if (counts.size > 1) {
      df.persist(StorageLevel.MEMORY_AND_DISK)
    }

    val summary = counts.toArray.flatMap { case (colCnt, embedNames) =>
      val names = embedNames.map(bdName => groups(bdName).map(_.name))
      val newRdd = spark.sparkContext.union((0 until colCnt).map(j => names.indices.map(i => col(names(i)(j))))
        .map(cols => df.select(cols: _*).rdd)
      )

      val configs = embedNames.map(bdName => groups(bdName).head)
      val ntypes = configs.map(_.normalizer)

      val schema = StructType(embedNames.map(df.schema(_)))
      var newDf = spark.createDataFrame(newRdd, schema)

      configs.foreach(config => {
        val name = config.name
        val format = config.format
        val dim = config.dim
        val col = newDf(name)
        format match {
          case "int" | "float" =>
            newDf = newDf.withColumn(name, when(col.isNull, 0.0).otherwise(col))
          case "list_float" =>
            newDf = newDf.withColumn(name, when(col.isNull.or(size(col).lt(1)), Array.fill(dim)(0.0)).otherwise(col))
          case _ =>
        }
      })

      val assemblerCol = "__assembler__"
      val dummyCol = "__dummy__"

      if (ntypes.distinct.length > 1) {
        newDf.persist(StorageLevel.MEMORY_AND_DISK)
      }

      ntypes.zip(configs).groupBy(_._1).flatMap(it => {
        val ntype = it._1
        val configs = it._2.map(_._2)
        val names = configs.map(_.embedName)
        val assembler = new VectorAssembler()
          .setInputCols(names)
          .setOutputCol(assemblerCol)
        val scaler = ntype match {
          case "min_max" =>
            new MinMaxScaler()
              .setInputCol(assemblerCol)
              .setOutputCol(dummyCol)
          case _ =>
            new StandardScaler()
              .setWithMean(true)
              .setWithStd(true)
              .setInputCol(assemblerCol)
              .setOutputCol(dummyCol)
        }
        val stages = Array(assembler, scaler)

        val pipeline = new Pipeline()
          .setStages(stages)

        val pipelineModel = pipeline.fit(newDf)

        val model = pipelineModel.stages.last
        ntype match {
          case "min_max" =>
            val scaler = model.asInstanceOf[MinMaxScalerModel]
            val minValues = scaler.originalMin.toArray
            val maxValues = scaler.originalMax.toArray

            var i = 0
            configs.map(config => {
              val format = config.format
              val dim = config.dim
              val map = Maps.newHashMap[String, Object]()
              map.put("type", ntype)
              if (format.equals("float") || format.equals("int")) {
                map.put("min_value", minValues(i).asInstanceOf[Object])
                map.put("max_value", maxValues(i).asInstanceOf[Object])
                i += 1
              } else {
                map.put("min_value", minValues.slice(i, i + dim).asInstanceOf[Object])
                map.put("max_value", maxValues.slice(i, i + dim).asInstanceOf[Object])
                i += config.dim
              }

              (config.embedName, JSON.toJSONString(map, new Array[SerializeFilter](0)))
            })
          case _ =>
            val scaler = model.asInstanceOf[StandardScalerModel]
            val mean = scaler.mean.toArray
            val std = scaler.std.toArray
            var i = 0
            configs.map(config => {
              val format = config.format
              val dim = config.dim
              val map = Maps.newHashMap[String, Object]()
              map.put("type", ntype)
              if (format.equals("float") || format.equals("int")) {
                map.put("mean", mean(i).asInstanceOf[Object])
                map.put("std", std(i).asInstanceOf[Object])
                i += 1
              } else {
                map.put("mean", mean.slice(i, i + dim).asInstanceOf[Object])
                map.put("std", std.slice(i, i + dim).asInstanceOf[Object])
                i += config.dim
              }

              (config.embedName, JSON.toJSONString(map, new Array[SerializeFilter](0)))
            })
        }
      })
    }

    if (IOUtil.exists(args.output)) {
      IOUtil.delete(args.output)
    }

    val results = summary.map(it => {
      it._1 + "=" + it._2
    })

    results.foreach(println)
    if (args.saveAsFile) {
      IOUtil.writeLines(args.output, results.toList.asJava)
    } else {
      spark.sparkContext.parallelize(results, 1).saveAsTextFile(args.output)
    }
  }
}
