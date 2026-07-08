package com.netease.easyml.uds.examples

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.google.common.collect.Maps
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{ConvertUtil, SparkUtil}
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/2/23.
 * Josnify hive table to json string.
 * <p>
 * data schema:
 * [output] json: String
 * <p>
 * params:
 * input: input hive table
 * output: output table
 * inputCols: input cols
 * format: output table format
 * startDay
 * endDay
 */
case class JsonifyArgs(input: String, output: String, inputCols: String, format: String = "parquet", startDay: String = "", endDay: String = "")

object JsonifyUDS extends UDS[JsonifyArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val inputCols_ = if (args.inputCols.equals(NULL)) {
      "*"
    } else {
      args.inputCols.replace(";", ",")
    }
    val df = SparkUtil.loadFromTable(spark, args.input, keys = inputCols_, startDay = args.startDay, endDay = args.endDay)

    import spark.implicits._
    val columns = df.columns
    val newDf = df.toDF.rdd.map(row => {
      val map = Maps.newHashMap[String, Object]()
      columns.foreach(col => {
        var value = row.getAs[Any](col)
        value = ConvertUtil.mayNestToArray(value)
        map.put(col, value.asInstanceOf[Object])
      })
      JSON.toJSONString(map, new Array[SerializeFilter](0))
    }).toDF("json")

    SparkUtil.saveAsTable(newDf, args.output, format = args.format)
  }
}
