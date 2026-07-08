package com.netease.vc.data.common

import org.apache.http.HttpHost

object ESConfig {
  val ENABLE_OUTPUT: Boolean = true

  val ES_URL = "vcharacter-es-1.gy.ntes:7000,vcharacter-es-2.gy.ntes:7000,vcharacter-es-3.gy.ntes:7000"
  val ES_HOSTS = ES_URL.split(",").map(_.split(":")(0))
  val ES_PORT = 7200

  val ES_HTTP_HOSTS = ES_HOSTS.filter(_.nonEmpty).map { n => new HttpHost(n,ES_PORT) }

  val ES_USERNAME = "vcharacter-online"
  val ES_PASSWORD = "fL4Rd4iMtG"
}
