package com.netease.music.da.transfer.jdbc.reader.split

import java.math.BigDecimal
import java.util

import com.netease.music.da.transfer.common.log.LogTrait

import scala.util.control.Breaks


object SplitUtil extends LogTrait {
  val ONE_PLACE = new BigDecimal(65536)
  val MAX_CHARS = 8
  val MIN_INCREMENT = new BigDecimal(Double.MinPositiveValue)

  /**
    * Return a BigDecimal representation of string 'str' suitable for use in a
    * numerically-sorting order.
    */
  def stringToBigDecimal(str: String): BigDecimal = { // Start with 1/65536 to compute the first digit.
    var curPlace = ONE_PLACE
    var result = BigDecimal.ZERO
    val len = Math.min(str.length, MAX_CHARS)
    var i = 0
    while ( {
      i < len
    }) {
      val codePoint = str.codePointAt(i)
      result = result.add(tryDivide(new BigDecimal(codePoint), curPlace))
      // advance to the next less significant place. e.g., 1/(65536^2) for the
      // second char.
      curPlace = curPlace.multiply(ONE_PLACE)

      {
        i += 1
        i - 1
      }
    }
    result
  }

  /**
    * Divide numerator by denominator. If impossible in exact mode, use rounding.
    */
  def tryDivide(numerator: BigDecimal, denominator: BigDecimal): BigDecimal = try
    numerator.divide(denominator)
  catch {
    case _: ArithmeticException =>
      numerator.divide(denominator, BigDecimal.ROUND_UP)
  }

  /**
    * Return the string encoded in a BigDecimal.
    * Repeatedly multiply the input value by 65536; the integer portion after
    * such a multiplication represents a single character in base 65536.
    * Convert that back into a char and create a string out of these until we
    * have no data left.
    */
  def bigDecimalToString(bd: BigDecimal): String = {
    var cur = bd.stripTrailingZeros
    val sb = new java.lang.StringBuilder

    val loop = new Breaks
    loop.breakable {
      for (_ <- 0 until MAX_CHARS) {
        cur = cur.multiply(ONE_PLACE)
        val curCodePoint = cur.intValue()
        if (0 == curCodePoint) loop.break()
        cur = cur.subtract(new BigDecimal(curCodePoint))
        val chars = Character.toChars(curCodePoint)
        sb.append(chars)
      }
    }
    sb.toString
  }

  /**
    * Returns a list of BigDecimals one element longer than the list of input
    * splits.  This represents the boundaries between input splits.  All splits
    * are open on the top end, except the last one.
    *
    * So the list [0, 5, 8, 12, 18] would represent splits capturing the
    * intervals:
    * s
    * [0, 5)
    * [5, 8)
    * [8, 12)
    * [12, 18] note the closed interval for the last split.
    */
  def split(minVal: BigDecimal,
            maxVal: BigDecimal,
            splitSize: Long,
            maxSplitNum: Int,
            splitNumber: Option[Int] = Option.empty): Iterator[String] = {
    // Use numSplits as a hint. May need an extra task if the size doesn't
    // divide cleanly.
    if (maxVal.equals(minVal)) {
      List(minVal.toString).iterator
    } else {
      var splitNum = splitNumber.getOrElse(
        Math.abs(Math.ceil(tryDivide(maxVal.subtract(minVal), BigDecimal.valueOf(splitSize)).doubleValue()).toInt))
      if (splitNum <= 0 || splitSize <= 0) {
        splitNum = 1
      }
      if (splitNum > maxSplitNum) {
        splitNum = maxSplitNum
      }
      LOG.info(s"Split number = $splitNum")
      var stepSize = tryDivide(maxVal.subtract(minVal), BigDecimal.valueOf(splitNum))
      if (stepSize.compareTo(MIN_INCREMENT) < 0) {
        stepSize = MIN_INCREMENT
        LOG.warn("Set BigDecimal splitSize to MIN_INCREMENT.")
      }
      new Iterator[String]() {
        var cursor: BigDecimal = minVal

        override def hasNext: Boolean = {
          cursor.compareTo(maxVal) <= 0
        }

        override def next(): String = {
          val result = cursor
          cursor = cursor.add(stepSize)
          if (result.compareTo(maxVal) < 0) {
            if (cursor.compareTo(maxVal) >= 0) {
              cursor = maxVal
            }
          }
          result.toString
        }
      }
    }
  }

  def splitString(minValString: String,
                  maxValString: String,
                  splitNumber: Option[Int] = Option.empty,
                  maxSplitNum: Int = 10000): Iterator[String] = {
    var commonPrefix = ""
    var sharedLen = 0
    val maxPrefixLen = Math.min(minValString.length, maxValString.length)
    if (maxPrefixLen > 0) {
      val loop = new Breaks
      loop.breakable {
        for (index <- 0 until maxPrefixLen) {
          if (minValString(index) != maxValString(index)) {
            loop.break()
          } else {
            sharedLen += 1
          }
        }
      }
      commonPrefix = minValString.substring(0, sharedLen)
    }
    val minVal = stringToBigDecimal(minValString.substring(sharedLen))
    val maxVal = stringToBigDecimal(maxValString.substring(sharedLen))

    if (maxVal.equals(minVal)) {
      List(minVal.toString).iterator
    } else {
      var splitNum = splitNumber.get
      if (splitNum <= 0) {
        splitNum = 1
        LOG.warn("Set splitNum to 1 because splitNum must be positive.")
      }
      if (splitNum > maxSplitNum) {
        splitNum = maxSplitNum
        LOG.warn(s"Set splitNum to $maxSplitNum because splitNum must be smaller than 'maxSplitNum'.")
      }
      var stepSize = tryDivide(maxVal.subtract(minVal), BigDecimal.valueOf(splitNum))
      if (stepSize.compareTo(MIN_INCREMENT) < 0) {
        stepSize = MIN_INCREMENT
        LOG.warn("Set BigDecimal splitSize to MIN_INCREMENT.")
      }
      val splitPoints: util.Iterator[BigDecimal] = new util.Iterator[BigDecimal]() {
        var cursor: BigDecimal = minVal

        override def hasNext: Boolean = {
          cursor.compareTo(maxVal) <= 0
        }

        override def next(): BigDecimal = {
          val result = cursor
          cursor = cursor.add(stepSize)
          if (result.compareTo(maxVal) < 0) {
            if (cursor.compareTo(maxVal) >= 0) {
              cursor = maxVal
            }
          }
          result
        }
      }
      import scala.collection.JavaConverters._
      val splitStrings = new util.ArrayList[String]()
      for (bd <- splitPoints.asScala) {
        splitStrings.add(commonPrefix + bigDecimalToString(bd))
      }
      if (splitStrings.isEmpty || splitStrings.get(0) != minValString) {
        splitStrings.add(0, minValString)
      }
      if (splitStrings.size == 1 || splitStrings.get(splitStrings.size() - 1) != maxValString) {
        splitStrings.add(maxValString)
      }
      splitStrings.asScala.map { string =>
        "'" + string + "'"
      }.iterator
    }
  }
}
