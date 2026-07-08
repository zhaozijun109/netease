package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.IOUtil
import com.netease.easyudf.pojo.FESliceWindowConfig
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._

case class FEFlattenTableArgs(input: String, primaryKeys: String, config: String,
                              entity: String = null, persistEvery: Int = 0, libsvm: Boolean = true)

class FEFlattenTable extends UserDefinedCmd[FEFlattenTableArgs] {
  val PREFIX_NAME = "PREFIX_NAME"

  override def apply(spark: SparkSession, args: FEFlattenTableArgs): DataFrame = {
    val config = IOUtil.smartReadConfig(args.config, classOf[FESliceWindowConfig])
    val primaryKeys = args.primaryKeys.split(",")
    var entities = config.getEntity.asScala.map(it => {
      (it.getAs, it.getExpr.split(",").filterNot(primaryKeys.contains))
    }).toMap
    var df = spark.table(args.input).drop(FESliceWindowConfig.DAY)
    val fields = df.schema.fields

    val entityKeys = entities.values.flatten.toSet ++ primaryKeys.toSet
    val featureNames = fields.filterNot(it => entityKeys.contains(it.name) ||
      it.name.equals(FESliceWindowConfig.ENTITY)).map(_.name)

    val selectKeys = primaryKeys ++ featureNames
    if (StringUtils.isNoneBlank(args.entity)) {
      val selEntity = args.entity.split(",")
      entities = entities.filter(it => selEntity.contains(it._1))
    }
    val cond = entities.keySet.map(name => col(FESliceWindowConfig.ENTITY) === lit(name)).reduce((it1, it2) => it1.or(it2))
    df = df.filter(cond)

    val newSchema = StructType(fields :+ StructField(PREFIX_NAME, StringType, nullable = false))
    val rdd = df.rdd.map(row => {
      val entity = row.getAs[String](FESliceWindowConfig.ENTITY)
      val colName = entities(entity).map(key => row.getAs[Any](key).toString).mkString("_").toLowerCase
      Row.fromSeq(row.toSeq :+ colName)
    })

    val newDf = spark.createDataFrame(rdd, newSchema).select((selectKeys :+ PREFIX_NAME).map(col): _*)
    newDf.persist(StorageLevel.DISK_ONLY)
    newDf.count()

    val prefixNames = newDf.select(col(PREFIX_NAME)).distinct().rdd.map(row => row.getString(0)).collect().sorted

    println(s"PREFIX NAMES: num ${prefixNames.length}, ${prefixNames.mkString(",")}")
    var retDf: DataFrame = null
    prefixNames.zipWithIndex.map(it => {
      val persist = if (it._2 > 0 && it._2 < prefixNames.length - 1
        && args.persistEvery > 0 && (it._2 + 2) % args.persistEvery == 0) true else false
      (it._1, persist)
    }).foreach(it => {
      val (name, persist) = it
      var entDf = newDf.filter(col(PREFIX_NAME) === lit(name)).drop(PREFIX_NAME)
      if (name.nonEmpty) {
        featureNames.foreach(it => entDf = entDf.withColumnRenamed(it, name + "_" + it))
      }
      if (retDf == null) {
        retDf = entDf
      } else {
        val joinDf = retDf.alias("a").join(entDf.alias("b"), primaryKeys, "left")
          .select("a.*", entDf.columns.filterNot(primaryKeys.contains): _*)
        if (persist) {
          joinDf.persist(StorageLevel.DISK_ONLY)
          joinDf.count()
          retDf.unpersist()
        }
        retDf = joinDf
      }
    })
    if (args.libsvm) {
      //      retDf.persist(StorageLevel.DISK_ONLY)
      val columns = retDf.columns.filterNot(primaryKeys.contains)
      println(s"COLUMNS: num ${columns.length}, ${columns.mkString(",")}")
      retDf = LibsvmEncoder.encode(retDf, columns, "features")
    }
    retDf
  }

}
