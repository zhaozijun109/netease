package com.netease.easyudf.cmd

import org.scalatest.FunSuite

class SendMessageTest extends FunSuite {

  test("testRun") {
    new SendMessage().run(SendMessageArgs(phoneNumber = "13212777105", content = "test"))
  }

}
