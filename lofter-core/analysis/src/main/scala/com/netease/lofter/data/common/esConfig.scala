package com.netease.lofter.data.common

import org.apache.http.HttpHost

object esConfig {
  val ENABLE_OUTPUT: Boolean = true

  val ES_URL = "lofter-data-common1.gy.ntes:7000,lofter-data-common2.gy.ntes:7000,lofter-data-common3.gy.ntes:7000,lofter-data-common4.gy.ntes:7000"
  val ES_HOSTS = ES_URL.split(",").map(_.split(":")(0))
  val ES_PORT = 7200

  val ES_HTTP_HOSTS = ES_HOSTS.filter(_.nonEmpty).map { n => new HttpHost(n,ES_PORT) }

  val ES_USERNAME = "data-analysis-gz"
  val ES_PASSWORD = "Dvzo@vdwTs"
}
