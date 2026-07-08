package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.sklearn.feature_extraction.TfidfVectorizer
import org.apache.spark.ml.Model
import org.apache.spark.ml.classification._
import org.apache.spark.ml.feature_.StringIndexer
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer;

/**
 * Created by linjiuning on 2020/12/29.
 * Miner important feature based on tf-idf and lr / nb.
 * <p>
 * data schema:
 * [input] label: String, feature: String(split by ' ')
 * [output] label: String, feature: String, score: Double
 * <p>
 * params:
 * input: input table
 * output: output table
 * labelCol: col name of label
 * featureCol: col name of feature
 * model: lr or nb(naive bayes)
 * minDf: min df
 * oneVsRest: if num class > 1, whether train as OneVsRest.
 * keepPositive: whether keep positive only, only work for lr
 */
case class TfidfFeatureMinerArgs(input: String, output: String, labelCol: String, featureCol: String, model: String,
                                 minDf: Int = 1, oneVsRest: Boolean = false, keepPositive: Boolean = false)

object TfidfFeatureMinerUDS extends UDS[TfidfFeatureMinerArgs] {

  def miner(model: Model[_], vocabulary: Array[String], labels: Array[String], index: Int = -1): Seq[(String, String, Double)] = {
    val coef = model match {
      case m: LogisticRegressionModel =>
        m.coefficientMatrix
      case m: NaiveBayesModel =>
        m.theta
    }

    val buffer = ArrayBuffer.empty[(String, String, Double)]
    if (index >= 0) {
      // one vs rest
      val label = labels(index)
      val array = coef.rowIter.toArray.last.toArray
      for (i <- array.indices) {
        buffer.append((label, vocabulary(i), array(i)))
      }
    } else {
      val numRows = coef.numRows
      if (numRows == 1) {
        val array = coef.rowIter.next().toArray
        for (i <- array.indices) {
          buffer.append((labels(1), vocabulary(i), array(i)))
          buffer.append((labels(0), vocabulary(i), -array(i)))
        }
      } else {
        var k = 0
        for (row <- coef.rowIter) {
          val label = labels(k)
          val array = row.toArray
          for (i <- array.indices) {
            buffer.append((label, vocabulary(i), array(i)))
          }
          k += 1
        }
      }
    }
    buffer
  }

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select ${args.labelCol}, ${args.featureCol} from ${args.input} where ${args.featureCol} is not null and ${args.labelCol} is not null")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val tfidf = new TfidfVectorizer()
      .setInputCol(args.featureCol)
      .setLowercase(true)
      .setMinDF(args.minDf)

    val tfidfModel = tfidf.fit(df)

    df = tfidfModel.transform(df)

    val indexer = new StringIndexer()
      .setInputCol(args.labelCol)

    val indexerModel = indexer.fit(df)
    df = indexerModel.transform(df)

    val est = if (args.model.equals("lr")) {
      new LogisticRegression()
        .setFeaturesCol(tfidf.getOutputCol)
        .setLabelCol(indexer.getOutputCol)
        .setElasticNetParam(1.0)
    } else {
      new NaiveBayes()
        .setFeaturesCol(tfidf.getOutputCol)
        .setLabelCol(indexer.getOutputCol)
    }

    val vocabulary = tfidfModel.cntVec.vocabulary
    val labels = indexerModel.labels

    val doOneVsRest = labels.length > 2 && args.oneVsRest

    var result = if (doOneVsRest) {
      val models = new OneVsRest()
        .setClassifier(est)
        .setFeaturesCol(est.getFeaturesCol)
        .setLabelCol(est.getLabelCol)
        .fit(df)
      models.models.toSeq.zipWithIndex.flatMap {
        case (model, index) => miner(model, vocabulary, labels, index)
      }
    } else {
      val model = est.fit(df)
      miner(model, vocabulary, labels)
    }

    if (args.model.equals("nb")) {
      result = result.map(it => (it._1, it._2, Math.exp(it._3)))
    } else if (args.keepPositive) {
      result = result.filter(_._3 > 0)
    }

    //    result.filter(_._1.equals("1"))
    //      .sortBy(-_._3).take(100).foreach(println)

    import spark.implicits._
    df = spark.sparkContext.parallelize(result)
      .toDF("label", "feature", "score")

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
