package com.netease.wm.udf

import java.net.URLDecoder
import com.netease.wm.udf.common.Hashids

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.util.control.NonFatal

@Description(name = "ShortLinkCode2Actpwd", value = "Decode short url code to actpwd id")
class ShortLinkCode2Actpwd extends UDF {
  private val shortSalt = "sK7tRfG2"
  private val minHashLength = 8

  def evaluate(code: String): Long = {
    if(code == null) {
      0L
    } else {
      try {
        val hashids = new Hashids(shortSalt, minHashLength)
        val nums = hashids.decode(code)
        if(nums == null || nums.length <= 1) {
          0
        } else {
          val wordId = nums(1)
          wordId
        }
      } catch {
        case NonFatal(_) => 0L
      }
    }
  }

}
