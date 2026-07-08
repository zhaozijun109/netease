package com.netease.bdms.ndi.client.spark

import com.netease.bdms.ndi.client.util.{Event, EventListener, LogTrait}
import org.apache.hadoop.yarn.api.records.ApplicationId

private[client] case class AcquireAppIdEvent(applicationId: ApplicationId, update: Boolean = false) extends Event


private[client] class AcquireAppIdListener extends EventListener[AcquireAppIdEvent] with LogTrait {

  private[this] var _applicationId: Option[ApplicationId] = Option.empty

  override def listen(event: AcquireAppIdEvent): Unit = {
    if (!foundAppId || (event.update && event.applicationId != applicationId.get)) {
      findAppId(event.applicationId)
    }
  }

  def findAppId(id: ApplicationId): Unit = {
    LOG.info(s"Found application id: $id")
    this._applicationId = Option.apply(id)
  }

  def applicationId: Option[ApplicationId] = _applicationId

  def foundAppId: Boolean = applicationId.isDefined
}
