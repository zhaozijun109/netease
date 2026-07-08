package com.netease.wm.hubble.common

case class BinlogRow(table: String, op: Int, opTime: Long, seqno: Long, partitionId: Int, data: Map[String, Any], old: Map[String, Any])
