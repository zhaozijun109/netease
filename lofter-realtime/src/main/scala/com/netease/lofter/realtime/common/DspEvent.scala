package com.netease.lofter.realtime.common

case class DspEvent(adId: String, appId: String, dspId: String, os: String, positionId: String, positionName: String,
                    success: Int, requestTime: Option[Long], responseTime: Option[Long], msg: String, externalAdId: Option[String],
                    ip: Option[String], wakeupBoot: Option[Int], winFlag: Option[Int], la: Option[String], lo: Option[String],
                    version: Option[String], uuid: Option[String], banwords: Option[String], reqid: Option[String], industryId: Option[String],
                    slotId: Option[String], advertiser_type: Option[String], price: Option[Double], blogId: Option[Long],
                    ext: Option[Map[String,String]], bidFactor: Option[Double])
