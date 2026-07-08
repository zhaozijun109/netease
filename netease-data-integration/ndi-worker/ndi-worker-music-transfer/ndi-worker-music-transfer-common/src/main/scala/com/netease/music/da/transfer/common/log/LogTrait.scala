package com.netease.music.da.transfer.common.log

import org.apache.log4j.{ConsoleAppender, PatternLayout}
import org.slf4j.{Logger, LoggerFactory}


trait LogTrait {

  @transient lazy protected val LOG: Logger = {
    val logName = this.getClass.getName.stripSuffix("$")
    val logger = org.apache.log4j.Logger.getLogger(logName)
    val layout = new PatternLayout("%d{yy/MM/dd HH:mm:ss} %p %c{1}: %m%n")
    val appender = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT)
    logger.addAppender(appender)
    logger.setAdditivity(false)
    LoggerFactory.getLogger(logName)
  }
}
