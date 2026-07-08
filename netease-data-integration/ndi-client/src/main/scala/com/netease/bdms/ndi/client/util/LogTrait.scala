package com.netease.bdms.ndi.client.util

import org.slf4j.{Logger, LoggerFactory}

trait LogTrait {
  protected val LOG: Logger = LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))
}
