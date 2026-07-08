package com.netease.music.da.transfer.jdbc.vo

import com.netease.music.da.transfer.common.conf.Properties
import org.apache.spark.sql.catalyst.TableIdentifier

case class TableMeta(
                      tableIdentifier: TableIdentifier,
                      var primaryKey: ColumnMeta,
                      columnList: List[ColumnMeta],
                      comment: String,
                      properties: Properties,
                      tableRows: Long = 0L,
                      tableAvgRowLength: Long = 0L,
                      tableDataLength: Long = 0L
                    )
