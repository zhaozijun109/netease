package com.netease.bdms.ndi.client.util

trait Event

trait EventListener[T <: Event] {
  def listen(event: T)
}