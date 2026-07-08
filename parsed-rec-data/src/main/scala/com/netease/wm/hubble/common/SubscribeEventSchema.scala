package com.netease.wm.hubble.common

import com.netease.dts.common.subscribe.{DefaultSubscribeEventSerializer,SubscribeEvent}
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.createTypeInformation

class SubscribeEventSchema extends DeserializationSchema[SubscribeEvent] {
  @transient private lazy val serializer  = new DefaultSubscribeEventSerializer()

  override def deserialize(message: Array[Byte]): SubscribeEvent = {
    serializer.deserialize(message)
  }

  override def isEndOfStream(nextElement: SubscribeEvent): Boolean = false

  override def getProducedType: TypeInformation[SubscribeEvent] = createTypeInformation[SubscribeEvent]
}
