package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.{KafkaPush, PopoMessage, kafkaConfig}
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.NumberFormatter
import com.netease.wm.util.view.NumberFormatter._
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.renderer._
import org.apache.spark.sql.SparkSession

object GrowthAdMaterialNotify {

  val format2fp: NumberFormatter = NumberFormatter("0.00%", "0.00%".some)

  def caption(cell: Cell): Cell = {
    cell.withStyle( "text-align" -> "center",
      "font-weight" -> "bold",
      "color" -> "#fff",
      "background" -> "#8066a0")
  }

  def percentCell[T: Numeric](value: T, numberFormatter: NumberFormatter): Cell = {
    if(value == null) Cell("") else Cell(value, numberFormatter)
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))

    val notifySql =
      s"""
         |select reason,media,proxy,photoid,aid,campaignid,advertiserid,newuv,
         |       log_ratio,retain_ratio,excellent_ratio,whiteboard_ratio,per_duration_minutes,
         |       nvl(photo_url,'') as photo_url, nvl(photo_caption,'') as photo_caption, nvl(star_name,'') as star_name, invest_amount
         |from lofter_dm.ads_growth_ad_material_notify_di
         |where dt = '$date'
         | """.stripMargin

    val data = spark.sql(notifySql).collect()
    val messages = data.map { row =>
      val reason = row.getAs[String](0)
      val media = row.getAs[String](1)
      val proxy = row.getAs[String](2)
      val photoId = row.getAs[String](3)
      val aid = row.getAs[String](4)
      val campaignId = row.getAs[String](5)
      val advertiserId = row.getAs[String](6)
      val newUv = row.getAs[Long](7)
      val photoUrl = row.getAs[String](13)
      val photoCaption = row.getAs[String](14)
      val starName = row.getAs[String](15)

      Seq(
        s"水上博主视频拉新触发${reason}预警",
        s"\t平台: ${media}",
        s"\t代理: ${proxy}",
        s"\t账户ID: ${advertiserId}",
        s"\t素材ID: ${photoId}",
        s"\t素材链接: ${photoUrl}",
        s"\t素材标题: ${photoCaption}",
        s"\t达人账号: ${starName}",
        s"\t任务id: ${campaignId}",
        s"\taid: ${aid}",
        s"\t当日新增用户数: ${newUv}",
      ).mkString("\n")
    }

    import spark.implicits._
    implicit val formats: org.json4s.Formats = DefaultFormats

    val popoMessages = messages.map{ m =>
      val message = PopoMessage(m, targetType = 1, targets = Seq("6587458"))
      KafkaPush(key = "1", value = write(message))
    }

    spark.createDataset(popoMessages)
      .selectExpr("key", """value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
      .option("topic", "LOFTER.CMBWEB.POPO")
      .save()

    val table = Table(
      hHeaderRows = Seq(
        Row(caption(Cell("命中条件",2, 1)), caption(Cell("平台", 2, 1)), caption(TextCell("代理", 2, 1)), caption(Cell("账号")),
          caption(Cell("素材ID")),caption(Cell("任务ID")), caption(Cell("aid")),
          caption(Cell("单日新增用户数")),caption(Cell("登录比例")), caption(Cell("次留率")),
          caption(Cell("优质用户占比")), caption(Cell("白板率")), caption(Cell("人均停留时长(分钟)")),
          caption(Cell("素材链接")),
          caption(Cell("素材标题")),
          caption(Cell("达人账号")),
          caption(Cell("助推花费"))
        )
      ),
      dataRows = data.map { row =>
        Row( Cell(row.getAs[String](0), 2, 1), Cell(row.getAs[String](1), 2, 1), Cell(row.getAs[String](2), 2, 1), Cell(row.getAs[String](3)),
             Cell(row.getAs[String](4)), Cell(row.getAs[String](5)), Cell(row.getAs[String](6)),
             Cell(row.getAs[Long](7), format0f),
             percentCell(row.getAs[Double](8), format2fp),
             percentCell(row.getAs[Double](9), format2fp),
             percentCell(row.getAs[Double](10), format2fp),
             percentCell(row.getAs[Double](11), format2fp),
             Cell(row.getAs[Double](12), format2f),
             Cell(row.getAs[String](13)), Cell(row.getAs[String](14)), Cell(row.getAs[String](15)),
             Cell(row.getAs[Double](16), format2f),
        )
      }
    ).width(880).withStyle("margin-bottom" -> "20px")

    val mailBody = TableHtmlRenderer(table, styler = None).html

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com")  // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "wengjiaqi@corp.netease.com" :: "wujiajia01@corp.netease.com" :: "linzi1@corp.netease.com" :: Nil,
      bcc = "hzxiaonaitong@corp.netease.com" :: Nil,
      subject = s"$date 水上博主拉新预警",
      message = "有问题请联系 hzxiaonaitong@corp.netease.com",
      richMessage = Some(mailBody)
    )

    spark.stop()
  }
}
