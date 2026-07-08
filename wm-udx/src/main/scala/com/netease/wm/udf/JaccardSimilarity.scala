package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name = "JaccardSimilarity", value = "compute jaccard similarity of two inputs")
class JaccardSimilarity extends UDF {
  def evaluate(a: String, b: String): Double = {
    if(a == null || b == null || a.isEmpty || b.isEmpty) {
      .0
    } else {
      val setA = a.toSet[Char]
      val setB = b.toSet[Char]
      val all =  (setA ++ setB)
      val intersect = setA.intersect(setB)
      intersect.size * 1.0 / all.size
    }
  }
}
