package com.netease.wm.ad.etl

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.control.NonFatal

object AdDspEtl {
  case class DspEvent(adId: String, appId: String, dspId: String, os: String, positionId: String, positionName: String,
                      success: Int, requestTime: Option[Long], responseTime: Option[Long], msg: String, externalAdId: Option[String],
                      ip: Option[String], wakeupBoot: Option[Int], winFlag: Option[Int], la: Option[String], lo: Option[String],
                      version: Option[String], uuid: Option[String], banwords: Option[String], reqid: Option[String], industryId: Option[String],
                      slotId: Option[String], advertiser_type: Option[String], price: Option[Double], blogId: Option[Long],
                      ext: Option[Map[String,String]], bidFactor: Option[Double])

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    import spark.implicits._

    val dspRequest = spark.read
      .textFile(s"/user/da_lofter/datastream/adserver/$day")
      .flatMap { line =>
        implicit val formats = DefaultFormats
        try {
          val jsTree = parse(line)
          val jsBody = (jsTree \ "_body").extractOpt[String]
          // for compatible with old log line
          val parsed = (if(jsBody.isDefined) parse(jsBody.get) else jsTree).extract[DspEvent]
          // workaround for missing requestTime
          if(parsed.requestTime.isDefined) {
            Some(parsed)
          } else {
            Some(parsed.copy(requestTime = parsed.responseTime))
          }
        } catch {
          case NonFatal(_) => None
        }
      }

    dspRequest.createOrReplaceTempView("dsp")
    spark.sql(
      s"""
         |insert overwrite table lofter.ods_log_ad_dsp_di partition(dt = '$day')
         |select * from dsp
         |""".stripMargin
    )

    spark.close()
  }
}
