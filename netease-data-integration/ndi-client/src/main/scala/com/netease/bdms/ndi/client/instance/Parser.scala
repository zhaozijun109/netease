package com.netease.bdms.ndi.client.instance

import org.json4s.JsonAST.JValue

trait Parser {
  def parseReader(readerData: JValue)
  def parseWriter(writerData: JValue)
}
