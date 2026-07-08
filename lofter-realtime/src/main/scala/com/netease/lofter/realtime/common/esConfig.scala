package com.netease.lofter.realtime.common

import org.apache.http.HttpHost

object esConfig {
  val ES_URL = "lofter-data-common1.gy.ntes:7000,lofter-data-common2.gy.ntes:7000,lofter-data-common3.gy.ntes:7000,lofter-data-common4.gy.ntes:7000"

  val ES_HOSTS = ES_URL.split(",").map { s =>
    val hp = s.split(":")
    new HttpHost(hp(0), hp(1).toInt, "http")
  }

}
