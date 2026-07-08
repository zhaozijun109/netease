package com.netease.easyml.ml.sklearn.model_selection

/**
 * Created by linjiuning on 2020/8/10.
 */
object Utils {

  def validateShuffleSplit(nSamples: Long, testSize: Option[Double], trainSize: Option[Double], defaultTestSize: Option[Double] = None): (Long, Long) = {
    val nTestSize = if (testSize.isEmpty && trainSize.isEmpty) {
      defaultTestSize
    } else {
      testSize
    }

    var nTest = if (nTestSize.isDefined) {
      val v = nTestSize.get
      if (v > 0 && v < 1) {
        Math.ceil(nSamples * v)
      } else {
        v
      }
    } else {
      -1.0
    }

    var nTrain = if (trainSize.isDefined) {
      val v = trainSize.get
      if (v > 0 && v < 1) {
        Math.ceil(nSamples * v)
      } else {
        v
      }
    } else {
      -1.0
    }
    if (nTrain < 0) {
      nTrain = nSamples - nTest
    } else if (nTest < 0) {
      nTest = nSamples - nTrain
    }
    if (nTrain + nTest > nSamples) {
      throw new IllegalArgumentException(s"The sum of train_size and test_size = ${nTrain + nTest}, should be smaller than the number of samples $nSamples. Reduce test_size and/or train_size.")
    }
    if (nTrain == 0) {
      throw new IllegalArgumentException(s"With n_samples=$nSamples, test_size=$nTest and train_size=$nTrain, the resulting train set will be empty. Adjust any of the aforementioned parameters.")
    }
    (nTrain.toLong, nTest.toLong)
  }

}
