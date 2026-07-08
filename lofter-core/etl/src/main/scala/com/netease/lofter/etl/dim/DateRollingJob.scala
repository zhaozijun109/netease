package com.netease.lofter.etl.dim

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.Days

object DateRollingJob {

  case class DimDateRolling(id: String, rollingDate: String, period: Int)


  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val startDate = pargs.required("startDate")
    val endDate = pargs.required("endDate")
    val output = pargs.required("output")

    val start = DateTime.parse(startDate)
    val end = DateTime.parse(endDate)

    val days = Days.daysBetween(start, end).getDays

    import spark.implicits._

    val dims: Seq[DimDateRolling] = (0 to days) flatMap { dayDiff =>
      val d = start.plusDays(dayDiff)
      val dt = d.toString("yyyy-MM-dd")
      (0 until 3).map { n => DimDateRolling(dt, d.minusDays(n).toString("yyyy-MM-dd"), 3) } ++
        (0 until 7).map { n => DimDateRolling(dt, d.minusDays(n).toString("yyyy-MM-dd"), 7) } ++
        (0 until 15).map { n => DimDateRolling(dt, d.minusDays(n).toString("yyyy-MM-dd"), 15) } ++
        (0 until 30).map { n => DimDateRolling(dt, d.minusDays(n).toString("yyyy-MM-dd"), 30) }
    }

    val df1 = dims.toDF()

    df1.repartition(1)
      .write.mode(SaveMode.Overwrite)
      .saveAsTable("lofter.dim_date_rolling")
  }

}
