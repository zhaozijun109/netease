package com.netease.music.da.transfer.mysql.conf

import com.netease.music.da.transfer.common.conf.Properties._
import com.netease.music.da.transfer.common.conf.Property

object MySQLProperties {
  val SAVE_MODE = Property("saveMode", Option.apply("insert"), toStringFunc)
}
