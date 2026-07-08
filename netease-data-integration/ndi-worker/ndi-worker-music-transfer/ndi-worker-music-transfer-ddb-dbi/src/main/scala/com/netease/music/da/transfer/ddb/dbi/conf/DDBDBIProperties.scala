package com.netease.music.da.transfer.ddb.dbi.conf

import com.netease.music.da.transfer.common.conf.Properties._
import com.netease.music.da.transfer.common.conf.Property

object DDBDBIProperties {
  val SAVE_MODE = Property("saveMode", Option.apply("insertInto"), toStringFunc)
  val DDB_URL_SUFFIX = Property("ddbUrlSuffix", Option.empty, toStringFunc)
}
