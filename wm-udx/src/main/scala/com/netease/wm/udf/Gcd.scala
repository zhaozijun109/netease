package com.netease.wm.udf

import java.math.BigInteger

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name = "Gcd", value = "find gcd of two number")
class Gcd extends UDF {
  def evaluate(input1: Long, input2: Long): Long = {
    BigInteger.valueOf(input1).gcd(BigInteger.valueOf(input2)).longValue()
  }
}
