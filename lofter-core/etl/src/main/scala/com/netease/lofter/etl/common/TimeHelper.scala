package com.netease.lofter.etl.common

import org.joda.time.DateTime

object TimeHelper {
  private val NORM_TIME_MIN = DateTime.parse("2000-01-01").getMillis
  private val NORM_TIME_MAX = DateTime.parse("2050-01-01").getMillis

  // try to fix time unit from seconds to millisecond if possible
  def fixTimeUnit(time: Long): Long = {
    if(time < NORM_TIME_MIN && time * 1000 < NORM_TIME_MAX) {
      time * 1000
    } else {
      time
    }
  }
}
