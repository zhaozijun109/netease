package com.netease.easyudf.cmd

import org.scalatest.FunSuite

import java.util

class RtrsPushTest extends FunSuite {

  val path = "target/rtrs_serializer"

  test("testRun") {
    val params = new util.HashMap[String, String]()
    params.put("path", path)
    params.put("key", "sorting.item_fea_test.951.2000.0")

    new RtrsPush().run(params)
  }

}
