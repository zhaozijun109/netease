package com.netease.bdms.ndi.client.spark

import java.util.concurrent.TimeUnit

import com.netease.bdms.ndi.client.property.Properties.properties
import com.netease.bdms.ndi.client.property.Property._
import com.netease.bdms.ndi.client.util.LogTrait
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.yarn.client.api.YarnClient

import scala.util.Try

private[client] trait YarnInterface {
  def yarnClient: YarnClient = YarnInterface.yarnClient
}

private[client] object YarnInterface extends LogTrait {
  val yarnClient: YarnClient = {
    val configuration = new Configuration()
    val client = YarnClient.createYarnClient()
    //read configuration from classpath
    client.init(configuration)
    client.start()
    client
  }
}
