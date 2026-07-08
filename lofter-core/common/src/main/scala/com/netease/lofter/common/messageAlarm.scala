package com.netease.lofter.common

object messageAlarm {
  def mail(title: String, message: String): Unit = {
    import com.netease.wm.util.mail._

    send a new Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "wangjun02@corp.netease.com" :: Nil,
      subject = title,
      message = message
    )
  }
}
