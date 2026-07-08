package com.netease.easyml.ml.transform

import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.param.HasNumPartitions
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/23.
 */
class Coalesce(override val uid: String) extends Transformer
  with HasNumPartitions with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("coalesce"))

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  setDefault(numPartitions, 0)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val nPartitions = if ($(numPartitions) == 0) {
      val conf = dataset.sparkSession.sparkContext.getConf
      val nPartitions = SparkUtil.getNumCoresPerExecutor(conf) * SparkUtil.getNumExecutors(conf)
      log.info(s"Auto set numPartitions = $nPartitions")
      nPartitions
    } else {
      $(numPartitions)
    }
    dataset.coalesce(nPartitions).toDF()
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    schema
  }
}

object Coalesce extends DefaultParamsReadable[Coalesce] {
  override def load(path: String): Coalesce = super.load(path)
}
