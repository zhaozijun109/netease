package com.netease.easyml.ml.util

/**
 * Created by linjiuning on 2020/9/28.
 */
class FastSigmoid(val expTableSize: Int, val maxExp: Int) extends Serializable {
  val expTable: Array[Float] = FastSigmoid.fastSigmoidTable(expTableSize, maxExp)
  val logTable: Array[Float] = expTable.map(f => Math.log(f).toFloat)

  private def index(f: Float): Int = {
    ((f + maxExp) * (expTableSize / maxExp / 2.0)).toInt
  }

  def sigmoid(f: Float): Option[Float] = {
    if (f > -maxExp && f < maxExp) {
      val ind = index(f)
      Some(expTable(ind))
    } else {
      None
    }
  }

  def apply(f: Float): Option[Float] = {
    sigmoid(f)
  }

  def log(f: Float): Option[Float] = {
    if (f > -maxExp && f < maxExp) {
      val ind = index(f)
      Some(logTable(ind))
    } else {
      None
    }
  }
}

object FastSigmoid {
  val EXP_TABLE_SIZE: Int = 1000
  val MAX_EXP: Int = 6

  def fastSigmoidTable(expTableSize: Int = EXP_TABLE_SIZE, maxExp: Int = MAX_EXP): Array[Float] = {
    val expTable = new Array[Float](expTableSize)
    var i = 0
    while (i < expTableSize) {
      val tmp = math.exp((2.0 * i / expTableSize - 1.0) * maxExp)
      expTable(i) = (tmp / (tmp + 1.0)).toFloat
      i += 1
    }
    expTable
  }

}
