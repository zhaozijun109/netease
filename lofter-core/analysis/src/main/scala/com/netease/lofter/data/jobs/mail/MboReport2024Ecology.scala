package com.netease.lofter.data.jobs.mail

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.NumberFormatter
import com.netease.wm.util.view.NumberFormatter._
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.renderer._
import com.netease.wm.util.view.table.styler.{StaticTableStyler, TableStyler}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.immutable.ListMap

object MboReport2024Ecology {
  val format2fp: NumberFormatter = NumberFormatter("0.00%", "0.00%".some)

  case class IpEntry(ip: String, sum_realuv: Long, expose_pv: Long, pay_expose_pv: Long,post_cnt_1d: Long, premium_post_cnt: Long,
                     pay_post_cnt_1d: Long, pay_premium_post_cnt: Long, new_device_count: Long,retain_ratio: Double,
                     total_expose_pv: Long, total_premium_post_cnt: Long,
                     total_pay_premium_post_cnt: Long, total_pay_expose_pv: Long, post_cnt_30d: Long) {
    def free_post_cnt_1d: Long = post_cnt_1d - pay_post_cnt_1d
    def free_premium_post_cnt: Long = premium_post_cnt - pay_premium_post_cnt
    def free_premium_post_cnt_ratio: Double = free_premium_post_cnt * 1.0 / post_cnt_30d
    def pay_premium_post_cnt_ratio: Double = pay_premium_post_cnt * 1.0 / post_cnt_30d
    def free_expose_ratio: Double = (expose_pv - pay_expose_pv) * 1.0 / expose_pv
    def pay_expose_ratio: Double =  pay_expose_pv * 1.0 / expose_pv
    def supply_consume_ratio: Double = post_cnt_1d * 1.0 / sum_realuv
  }

  def percentCell[T: Numeric](value: T, numberFormatter: NumberFormatter): NumericCell[T] = {
    val background = if (implicitly[Numeric[T]].toDouble(value) >  0) "#ff0000" else "#00b050"
    Cell(value, numberFormatter)
      .withStyle("color" -> background)
  }

  def percentCell[T: Numeric](value: Option[T], numberFormatter: NumberFormatter): NumericCell[T] = {
    val background = if (value.map( v => implicitly[Numeric[T]].toDouble(v)).getOrElse(.0) >  0) "#ff0000" else "#00b050"
    Cell(value, numberFormatter)
      .withStyle("color" -> background)
  }

  def huanbi[T: Numeric](newValue: T, oldValue: T): Option[Double] = {
    val ov = implicitly[Numeric[T]].toDouble(oldValue)
    val nv = implicitly[Numeric[T]].toDouble(newValue)
    ov match {
      case 0 => None
      case _ => Some((nv - ov) / ov)
    }
  }

  def huanbiOption[T: Numeric](newValue: Option[T], oldValue: Option[T]): Option[Double] = {
    val imp = implicitly[Numeric[T]]
    val ov = imp.toDouble(oldValue.getOrElse(imp.zero))
    val nv = imp.toDouble(newValue.getOrElse(imp.zero))
    ov match {
      case 0 => None
      case _ => Some((nv - ov) / ov)
    }
  }

  def caption(cell: Cell): Cell = {
    cell.withStyle( "text-align" -> "center",
      "color" -> "#fff",
      "background" -> "#8066a0")
  }

  def emphasize[T <: Cell](platform: String, row: Row[T]): Row[T] = {
    platform match {
      case "全部" | "all" | "内容付费" | "趣味电商" | "装扮商城" => row.withStyle("font-weight" -> "bold")
      case _ => row
    }
  }

  def emphasize[T <: Cell](row: Row[T]): Row[T] = {
    row.withStyle("font-weight" -> "bold")
  }

  def huanbiCell[T: Numeric](newValue: T, oldValue: T): Cell = {
    val imp = implicitly[Numeric[T]]
    val ov = implicitly[Numeric[T]].toDouble(oldValue)
    val nv = implicitly[Numeric[T]].toDouble(newValue)
    val percent = (nv - ov) / ov

    val background = percent match {
      case v if v > 0.1 => "#ff0000"
      case v if v < -0.1 => "#00b050"
      case _ => "#000000"
    }

    val output = "%d (%.2f%%)".format(newValue, percent * 100)
    Cell(output).withStyle("color" -> background)
  }

  def huanbiCellPercent[T: Numeric](newValue: T, oldValue: T): Cell = {
    val imp = implicitly[Numeric[T]]
    val ov = implicitly[Numeric[T]].toDouble(oldValue)
    val nv = implicitly[Numeric[T]].toDouble(newValue)
    val percent = (nv - ov) / ov

    val background = percent match {
      case v if v > 0.1 => "#ff0000"
      case v if v < -0.1 => "#00b050"
      case _ => "#000000"
    }
    val output = "%.2f%% (%.2f%%)".format(nv * 100, percent * 100)
    Cell(output).withStyle("color" -> background)
  }

  val ips = Seq(
    "第五人格","原神","崩坏星穹铁道","时代少年团","恋与深空","火影忍者","哈利波特","文豪野犬","凹凸世界","咒术回战",
    "明日方舟","魔道祖师","排球少年","盗墓笔记","王者荣耀","名侦探柯南","蓝色监狱","重返未来1999","mbti",
    "如鸢","tf四代","大梦归离","恋与制作人","光与夜之恋",
    "头七怪谈","偶像梦幻祭","光遇","seventeen","变形金刚","oc","名侦探学院","奥特曼","女神异闻录",
    "全职高手","鬼灭之刃","海贼王","种地吧少年","忘川风华录","假面骑士","project sekai","我在精神病院学斩神","初音未来","蛋仔派对","诡秘之主",
    "少年歌行", "士兵突击", "燕云十六声", "防风少年"
  )

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val day = pargs.getOrElse("date", DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(day).minusDays(7).toString("yyyy-MM-dd")
    val startDate = DateTime.parse(day)

    val spark: SparkSession = SparkSession.builder()
      .appName("Lofter Mbo Ecology Report 2024")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    import spark.implicits._

    val ipsCondition = ips.mkString("'", "','", "'")
    // ecology ip
    val data = spark.sql(s"select * from lofter_dm.ads_mbo_ip_di where dt='$day' and (ip in ($ipsCondition) or real_uv_rank <= 10) ").as[IpEntry].collect().map { e =>
      e.ip -> e
    }.toMap

    val dataWeekAgo = spark.sql(s"select * from lofter_dm.ads_mbo_ip_di where dt='$weekAgo' ").as[IpEntry].collect().map { e =>
      e.ip -> e
    }.toMap

    val dataIps = data.keys.toSeq
    val totalIps = ips.filter(s => dataIps.contains(s)) ++ dataIps.filterNot(s => ips.contains(s))

    val table1 = Table(
      title = Title("重点圈层监控").some,
      hHeaderRows = Seq(
        Row(
          caption(Cell("日期", 1, 2)), caption(Cell("圈层")),
          caption(Cell("免费发文量")),
          caption(Cell("付费发文量")),
          caption(Cell("供需比")), caption(Cell("供需比环比变动量")),
          caption(Cell("免费优质内容数")),
          caption(Cell("免费优质内容占比")),
          caption(Cell("付费优质内容数")),
          caption(Cell("付费优质内容占比")),
          caption(Cell("圈层日有效uv")),
          caption(Cell("免费内容曝光占比")),
          caption(Cell("付费内容曝光占比")),
          caption(Cell("日新增设备数")),
          caption(Cell("新用户次留")),
        )
      ),
      dataRows = totalIps.map { ip =>
        val row = data(ip)
        val prev = dataWeekAgo.getOrElse(ip, IpEntry(ip,0,0,0,0,0,0,0,0,0,0,0,0,0,0))

        Row(
          Cell(day), Cell(ip),
          huanbiCell(row.free_post_cnt_1d, prev.free_post_cnt_1d),
          huanbiCell(row.pay_post_cnt_1d, prev.pay_post_cnt_1d),
          Cell(row.supply_consume_ratio, format2fp), Cell(row.supply_consume_ratio - prev.supply_consume_ratio, format2fp),
          huanbiCell(row.free_premium_post_cnt, prev.free_premium_post_cnt),
          huanbiCellPercent(row.free_premium_post_cnt_ratio, prev.free_premium_post_cnt_ratio),
          huanbiCell(row.pay_premium_post_cnt, prev.pay_premium_post_cnt),
          huanbiCellPercent(row.pay_premium_post_cnt_ratio, prev.pay_premium_post_cnt_ratio),
          huanbiCell(row.sum_realuv, prev.sum_realuv),
          huanbiCellPercent(row.free_expose_ratio, prev.free_expose_ratio),
          huanbiCellPercent(row.pay_expose_ratio, prev.pay_expose_ratio),
          huanbiCell(row.new_device_count, prev.new_device_count),
          huanbiCellPercent(row.retain_ratio, prev.retain_ratio)
        )
      }
    ).width(1536).withStyle("margin-bottom" -> "20px")

    val renderHtml1 = TableHtmlRenderer(table1, styler = None ).html
    val mailBody = renderHtml1

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com")  // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to =  "wengjiaqi@corp.netease.com" :: "yanganning@corp.netease.com" :: "meinan@corp.netease.com" :: "liuxiyuan@corp.netease.com" :: "qinrui03@corp.netease.com" :: "wujiajia01@corp.netease.com" :: "zhouxian02@corp.netease.com" :: Nil,
      cc = "lofterupup@list.nie.netease.com" :: Nil,
      bcc = "gzq.wmtl@list.nie.netease.com" :: "wm_data_analysis@list.nie.netease.com" :: "data.wm@list.nie.netease.com" :: Nil,
      subject = s"$day LOFTER重点圈层生态日报",
      message = "有问题请联系 hzxiaonaitong@corp.netease.com",
      richMessage = Some(mailBody)
    )

    spark.close()
  }
}
