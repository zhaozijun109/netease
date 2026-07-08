package com.netease.lofter.etl.dim

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.Days

object DateJob {

  case class DimDate(id: String, week: String, month: String, year: String,
                     ts: Long, dayOfYear: Int, weekOfYear: Int, monthOfMonth: Int)


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

    val dims: Seq[DimDate] = (0 until days) map { dayDiff =>
      val d = start.plusDays(dayDiff)
      val dt = d.toString("yyyy-MM-dd")
      val week = d.withDayOfWeek(1).toString("yyyy-MM-dd")
      val month = d.toString("yyyy-MM")
      val year = d.toString("yyyy")
      val ts = d.getMillis
      val weekOfYear = d.getWeekOfWeekyear
      val monthOfYear = d.getMonthOfYear
      val dayOfMonth = d.getDayOfMonth

      DimDate(dt, week, month, year, ts, dayOfMonth, weekOfYear, monthOfYear)
    }

    val df1 = dims.toDF()

    df1.repartition(1)
      .write.mode(SaveMode.Overwrite)
      .saveAsTable("lofter.dim_date")
  }

}
