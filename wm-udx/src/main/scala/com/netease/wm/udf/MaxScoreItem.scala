package com.netease.wm.udf

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

@Description(name="MaxScoreItem", value="find max map key by score value")
class MaxScoreItem extends UDF {
  val ScorePairRegex = """(?U)([\w\-]*):([\d\.\-]*)""".r

  def evaluate(scoredList: String): String = {
    val scores = scoredList.split(";").toSeq.collect {
      case ScorePairRegex(tag, score) if !tag.startsWith("无") => (tag, score.toDouble)
    }
    if(scores.nonEmpty) scores.maxBy(_._2)._1 else null
  }
}
