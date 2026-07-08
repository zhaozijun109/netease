package com.netease.music.da.transfer.common.vo

import org.apache.spark.sql.DataFrame

case class WriterResult(
                         count: Long,
                         sampleData: DataFrame,
                         info: Map[String, String]
                       )

object WriterResult extends Serializable {

  def apply(data: DataFrame, info: Map[String, String], limit: Int = 10): WriterResult = {
    WriterResult(data.count(), data.limit(limit), info)
  }

}
