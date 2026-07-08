package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.Script
import com.netease.easyml.common.util.{OkHttpUtil, StringUtil}

import java.net.URLEncoder

case class SendMessageArgs(phoneNumber: String, content: String)

class SendMessage extends Script[SendMessageArgs] {

  val URL = "http://alarm.netease.com/api/sendSMS"
  val APP_NAME = "mind"
  val SECRET = "0312f003-2af8-4b51-b39d-937d95df7e4c"

  override def run(args: SendMessageArgs): Unit = {
    args.phoneNumber.split(",").foreach(phone =>
      try {
        val httpUtil = OkHttpUtil.getInstance()
        val timestamp = System.currentTimeMillis() / 1000
        val signature = StringUtil.md5(SECRET + timestamp)
        val payload = Array(s"ac_appName=$APP_NAME",
          s"ac_timestamp=$timestamp",
          s"ac_signature=$signature",
          s"content=${URLEncoder.encode(args.content)}",
          s"to=$phone",
          "isSync=1").mkString("&")
        httpUtil.get(s"$URL?$payload")
      }
      catch {
        case e: Exception =>
          log.error("send http occurs exception", e)
      }
    )
  }

}
