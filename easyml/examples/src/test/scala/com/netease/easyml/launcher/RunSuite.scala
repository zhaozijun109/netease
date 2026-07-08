package com.netease.easyml.launcher

import com.holdenkarau.spark.testing.SharedSparkContext
import com.intel.imllib.ffm.classification.{FFMModel, FFMWithAdag}
import com.netease.easyml.common.util.IOUtil
import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/7/7.
 */
class RunSuite extends FunSuite with SharedSparkContext {

  /** node2vec example */
  test("train node2vec") {
    val cmd = "fit"
    val args = Array("-c conf/node2vec.json", "-o target/tmp/node2vec", "-f")

    Run.run(cmd, args)
  }

  /** crf predict example */
  test("predict crf") {
    val cmd = "transform"
    val args = Array("-a conf/crf_predict.json", "-r target/tmp/crf")

    Run.run(cmd, args)
  }

  /** xgboost example */
  test("train xgboost") {
    val cmd = "fit"
    val args = Array("-c conf/xgboost.json", "-o target/tmp/xgboost", "-f")

    Run.run(cmd, args)
  }

  test("evaluation xgboost") {
    val cmd = "evaluate"
    val args = Array("-a target/tmp/xgboost")

    Run.run(cmd, args)
  }

  test("predict xgboost") {
    val cmd = "transform"
    val args = Array(
      "-a target/tmp/xgboost",
      "-t toy_dataset/iris/iris.data",
      "-r target/tmp/xgboost/test"
    )

    Run.run(cmd, args)
  }

  test("feature xgboost") {
    val cmd = "transform"
    val args = Array(
      "-a conf/xgboost-feature.json",
      "-t toy_dataset/iris/iris.data",
      "-r ontmp.test"
    )

    Run.run(cmd, args)
  }

  test("metric xgboost") {
    val cmd = "metric"
    val args = Array("-c target/tmp/xgboost/config.json", "-r target/tmp/xgboost/test")

    Run.run(cmd, args)
  }

  /** fm example */
  test("train fm") {
    val cmd = "fit"
    val args = Array("-c conf/fm.json", "-o target/tmp/fm", "-f")

    Run.run(cmd, args)
  }

  test("evaluation fm") {
    val cmd = "evaluate"
    val args = Array("-a target/tmp/fm")

    Run.run(cmd, args)
  }

  test("predict fm") {
    val cmd = "transform"
    val args = Array(
      "-a target/tmp/fm",
      "-r target/tmp/fm/test"
    )

    Run.run(cmd, args)
  }

  /** ffm example */

  test("origin ffm") {
    val path = "toy_dataset/ffm/a9a_ffm"
    val vectorSize = 2
    val maxIter = 3
    val eta = 0.01
    val lambda = 0.00002
    val fitIntercept = false
    val fitLinear = false
    val data = sc.textFile(path).map(_.split("\\s")).map(x => {
      val y = if (x(0).toInt > 0) 1.0 else -1.0
      val nodeArray: Array[(Int, Int, Double)] = x.drop(1).map(_.split(":")).map(x => {
        (x(0).toInt, x(1).toInt, x(2).toDouble)
      })
      (y, nodeArray)
    }).repartition(4)
    val splits = data.randomSplit(Array(0.7, 0.3))
    val (training: RDD[(Double, Array[(Int, Int, Double)])], testing) = (splits(0), splits(1))

    //sometimes the max feature/field number would be different in training/testing dataset,
    // so use the whole dataset to get the max feature/field number
    val m = data.flatMap(x => x._2).map(_._1).collect.max //+ 1
    val n = data.flatMap(x => x._2).map(_._2).collect.max //+ 1

    val ffm: FFMModel = FFMWithAdag.train(training, m, n, dim = (fitIntercept, fitLinear, vectorSize), n_iters = maxIter,
      eta = eta, lambda = lambda, normalization = false, false, "adagrad")

    val scores: RDD[(Double, Double)] = training.map(x => {
      val p = ffm.predict(x._2)
      val ret = if (p >= 0.5) 1.0 else -1.0
      (ret, x._1)
    })
    val accuracy = scores.filter(x => x._1 == x._2).count().toDouble / scores.count()
    println(s"accuracy = $accuracy")
  }

  test("dataset ffm") {
    val path = "toy_dataset/ffm/a9a_ffm"
    val outTrainPath = "toy_dataset/ffm/a9a_ffm.train"
    val outValidPath = "toy_dataset/ffm/a9a_ffm.valid"
    val ratio = 0.7

    import scala.collection.JavaConversions._
    var data = IOUtil.readLines(path).map(_.split("\\s")).map(x => {
      val y = if (x(0).toInt > 0) 1.0 else -1.0
      val feature = x.drop(1).mkString(" ")
      y + "," + feature
    })

    data = scala.util.Random.shuffle(data)
    val split = (data.length * ratio).toInt
    val training = data.slice(0, split)
    val testing = data.slice(split, data.length)


    IOUtil.writeLines(outTrainPath, training)
    IOUtil.writeLines(outValidPath, testing)
  }

  test("train ffm") {
    val cmd = "fit"
    val args = Array("-c conf/ffm.json", "-o target/tmp/ffm", "-f")

    Run.run(cmd, args)
  }

  test("evaluation ffm") {
    val cmd = "evaluate"
    val args = Array("-a target/tmp/ffm")

    Run.run(cmd, args)
  }

  test("predict ffm") {
    val cmd = "transform"
    val args = Array(
      "-a target/tmp/ffm",
      "-r target/tmp/ffm/test"
    )

    Run.run(cmd, args)
  }
}
