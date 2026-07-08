package com.netease.wm.udf


import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.collection.JavaConverters._

@Description(name = "ExpandPostCategory", value = "expand category list into category l1 l2 l3 concat form")
class ExpandPostCategory extends UDF {
  def evaluate(categories: java.util.List[String]): java.util.List[String] = {
    if(categories != null && categories.size() >= 3) {
      val c1 = categories.get(0)
      val c2 = categories.get(1)
      val c3 = categories.get(2)
      java.util.Arrays.asList(c1, s"$c1-$c2", s"$c1-$c2-$c3")
    } else null
  }
}
