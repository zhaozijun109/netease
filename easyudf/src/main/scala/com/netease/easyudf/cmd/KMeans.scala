package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.feature.Vectorizer
import com.netease.easyml.ml.sklearn.SklearnUtils
import com.netease.easyml.ml.util.{SchemaUtils, Utils, VectorUtils}
import com.netease.easyudf.udf.util.Utils.toInt
import org.apache.commons.lang3.StringUtils
import org.apache.spark.ml.clustering.{KMeansModel, KMeans => MLKMeans}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.linalg_.BLAS
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

case class KMeansArgs(input: String, output: String = null, center: String = null, label: String = null,
                      k: Int = 0, top: Int = 0, maxIter: Int = 20)

class KMeans extends UserDefinedCmd[KMeansArgs] {
  val VECTOR: String = "vector"
  val ID: String = "id"
  val LABEL: String = "label"
  val CENTER_VIEW: String = "kmeans_center"

  def computeSimilarity(centers: Array[Vector],
                        point: Vector): Array[(Int, Double)] = {
    centers.zipWithIndex.map { case (center, i) =>
      val cosine = BLAS.dot(center, point)
      (i, cosine)
    }
  }

  def transform(model: KMeansModel, df: DataFrame, top: Int): DataFrame = {
    if (top < 1) {
      model.transform(df).withColumn(LABEL, col(LABEL).cast(StringType))
    } else {
      val centers = model.clusterCenters.map(VectorUtils.normalize(_, 2.0))
      val centersBc = df.sparkSession.sparkContext.broadcast(centers)
      val newRdd = df.toDF.rdd.map(row => {
        var vector = row.getAs[Any](VECTOR) match {
          case vector: Seq[Double] => Vectors.dense(vector.toArray)
          case any => any.asInstanceOf[Vector]
        }
        vector = VectorUtils.normalize(vector, 2.0)
        var scores = computeSimilarity(centersBc.value, vector)
        scores = scores
          .sortBy(-_._2)
          .slice(0, Math.min(scores.length, top))
        val predict = scores.map(it => "%d:%.5f" format(it._1, it._2)).mkString(",")
        Row.fromSeq(row.toSeq ++ Seq(predict))
      })
      df.sparkSession.createDataFrame(newRdd, SchemaUtils.appendColumn(df.schema, LABEL, StringType))
    }
  }

  def kmeansModelCenter(spark: SparkSession, model: KMeansModel): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(model.clusterCenters.zipWithIndex.map(it => (it._2, it._1.toArray)))
      .toDF(LABEL, VECTOR)
  }

  // consistent kmeans
  override def apply(spark: SparkSession, args: KMeansArgs): DataFrame = {
    var df = spark.table(args.input).select(ID, VECTOR)

    val model = if (StringUtils.isBlank(args.center)) {
      df = new Vectorizer()
        .setInputCol(VECTOR)
        .setOutputCol(s"tmp_$VECTOR")
        .fit(df)
        .transform(df)
        .drop(VECTOR)
        .withColumnRenamed(s"tmp_$VECTOR", VECTOR)

      new MLKMeans()
        .setMaxIter(args.maxIter)
        .setFeaturesCol(VECTOR)
        .setPredictionCol(LABEL)
        .setK(args.k)
        .fit(df)
    } else {
      val cdf = if (StringUtils.isNoneBlank(args.label)) {
        spark.sql("create temporary function vector_normalize as 'com.netease.easyudf.udf.collect.VectorNormalizeUDF'")
        //        spark.sql("create temporary function vector_union as 'com.netease.easyudf.udf.collect.VectorUnionUDAF'")
        spark.sql("create temporary function vector_avg as 'com.netease.easyudf.udf.collect.ArrayDoubleWeightAvgUDAF'")
        spark.table(args.label).selectExpr(ID, s"split(split($LABEL, ',')[0], ':')[0] as $LABEL").alias("a")
          .join(df.alias("b"), ID).selectExpr(s"b.$VECTOR", s"a.$LABEL").groupBy(LABEL).agg(
          //          expr(s"first($VECTOR) as $VECTOR")
          //          expr(s"vector_union(vector_normalize($VECTOR, 'l2'), 'avg') as $VECTOR")
          expr(s"vector_avg(vector_normalize($VECTOR, 'l2'), 1.0) as $VECTOR")
        )
      } else if (IOUtil.exists(args.center)) {
        kmeansModelCenter(spark, SklearnUtils.read(args.center, classOf[KMeansModel]))
      } else {
        spark.table(args.center).select(LABEL, VECTOR)
      }
      val clusterCenters = cdf.rdd.map(row => (toInt(row.get(0)), row.getSeq[Double](1))).collect().sortBy(_._1).map(it => Vectors.dense(it._2.toArray))
      val kmeans = Utils.newKMeansModel(clusterCenters)
      kmeans.setFeaturesCol(VECTOR)
        .setPredictionCol(LABEL)
      kmeans
    }
    kmeansModelCenter(spark, model).createOrReplaceTempView(CENTER_VIEW)

    df = transform(model, df, args.top).drop(VECTOR)

    df
  }
}
