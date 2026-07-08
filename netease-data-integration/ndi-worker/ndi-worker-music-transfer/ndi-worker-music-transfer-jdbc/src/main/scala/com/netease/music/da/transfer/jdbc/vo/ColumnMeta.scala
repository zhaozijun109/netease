package com.netease.music.da.transfer.jdbc.vo

import org.apache.spark.sql.catalyst.TableIdentifier

case class ColumnMeta(
                       tableIdentifier: TableIdentifier,
                       name: String,
                       cType: String,
                       comment: String,
                       position: Int,
                       pri: Boolean
                     )
