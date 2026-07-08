package com.netease.wm.hubble.common

import org.apache.http.HttpHost

object esConfig {

  val ES_URL = "lofter-elk1.jd.163.org:7200,lofter-elk2.jd.163.org:7200,lofter-elk3.jd.163.org:7200,lofter-elk9.jd.163.org:7200"
  val ES_URL_GY = "lofter-data-common1.gy.ntes:7000,lofter-data-common2.gy.ntes:7000,lofter-data-common3.gy.ntes:7000,lofter-data-common4.gy.ntes:7000"

  val ES_HOSTS = ES_URL_GY.split(",").map { s =>
    val hp = s.split(":")
    new HttpHost(hp(0), hp(1).toInt, "http")
  }

  val username = "data-analysis-gz"
  val password = "Dvzo@vdwTs"

}
