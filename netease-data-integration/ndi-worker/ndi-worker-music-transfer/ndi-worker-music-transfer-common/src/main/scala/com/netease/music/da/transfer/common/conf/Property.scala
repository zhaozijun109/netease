package com.netease.music.da.transfer.common.conf

case class Property[T](key: String, default: Option[T], convertFunc: String => T)