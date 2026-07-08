package com.netease.easyml.ml.operator

/**
 * Created by linjiuning on 2020/7/20.
 */
trait Operator[IN, OUT] {
  def compute(in: IN): OUT
}
