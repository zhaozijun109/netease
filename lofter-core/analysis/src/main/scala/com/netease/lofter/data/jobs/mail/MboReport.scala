package com.netease.lofter.data.jobs.mail

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.NumberFormatter
import com.netease.wm.util.view.NumberFormatter._
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.renderer._
import org.apache.spark.sql.SparkSession

object MboReport {
  val format2fp: NumberFormatter = NumberFormatter("0.00%", "0.00%".some)

  case class MboEntry(dau: Long, new_device_count: Long,
                      active_retain_1d: Double, new_retain_1d: Double,
                      active_retain_30d: Double, avg_session_minutes: Double,
                      post_count: Long, post_user_count: Long, real_post_count: Long,
                      real_post_user_count: Long, post_count_surpass_1w_hot: Long,
                      dstr_text_photo_pv: Long, dstr_video_pv: Long,
                      interaction_uv: Long, comment_to_praise_ratio: Double, avg_creator_new_fans: Option[Double],
                      users_pay: Long, money_pay: Double, users_recharge: Long)

  def percentCell[T: Numeric](value: T, numberFormatter: NumberFormatter): NumericCell[T] = {
    val background = if (implicitly[Numeric[T]].toDouble(value) >  0) "#ff0000" else "#00ff00"
    Cell(value, numberFormatter)
      .withStyle("color" -> background)
  }

  def percentCell[T: Numeric](value: Option[T], numberFormatter: NumberFormatter): NumericCell[T] = {
    val background = if (value.map( v => implicitly[Numeric[T]].toDouble(v)).getOrElse(.0) >  0) "#ff0000" else "#00ff00"
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
      "font-weight" -> "bold",
      "color" -> "#fff",
      "background" -> "#8066a0")
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.getOrElse("date", DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")
    val dayDayAgo = DateTime.parse(dt).minusDays(2).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(dt).minusDays(7).toString("yyyy-MM-dd")

    val spark: SparkSession = SparkSession.builder()
      .appName("Lofter Mbo Report")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    import spark.implicits._

    val data = spark.sql(s"select * from lofter_dm.ads_mbo_di where dt='$dt'").as[MboEntry].collect().head
    val dayAgoData = spark.sql(s"select * from lofter_dm.ads_mbo_di where dt='$dayAgo'").as[MboEntry].collect().head
    val dayDayAgoData = spark.sql(s"select * from lofter_dm.ads_mbo_di where dt='$dayDayAgo'").as[MboEntry].collect().head
    val weekAgoData = spark.sql(s"select * from lofter_dm.ads_mbo_di where dt='$weekAgo'").as[MboEntry].collect().head

    val table = Table(
      hHeaderRows = Seq(
        Row(caption(Cell("模块", 2, 1)), caption(Cell("指标")), caption(TextCell("今日")), caption(Cell("昨日")), caption(Cell("前日")), caption(Cell("环比")), caption(Cell("同比")))
      ),
      vHeaderRows = Seq(
        Row(Cell("增长", 2, 6), Cell("DAU")),
        Row(Cell("日新增")),
        Row(Cell("日活次留")),
        Row(Cell("新增次留")),
        Row(Cell("日活30日留存")),
        Row(Cell("人均停留时长")),
        Row(Cell("内容", 2, 5), Cell("发布量")),
        Row(Cell("发布UV")),
        Row(Cell("发文量")),
        Row(Cell("发文UV")),
        Row(Cell("日新增高热内容量")),
        Row(Cell("分发", 2, 2), Cell("图文浏览量")),
        Row(Cell("视频播放量")),
        Row(Cell("氛围(互动/关系)",2, 3), Cell("互动UV")),
        Row(Cell("评赞比")),
        Row(Cell("创作者人均涨粉数")),
        Row(Cell("营收",2, 3), Cell("收入金额")),
        Row(Cell("付费人数")),
        Row(Cell("充值人数"))
      ),
      dataRows = Row( Cell(data.dau, format0f), Cell(dayAgoData.dau, format0f), Cell(dayDayAgoData.dau, format0f), percentCell(huanbi(data.dau, dayAgoData.dau), format1fp), percentCell(huanbi(data.dau, weekAgoData.dau), format1fp)) ::
        Row( Cell(data.new_device_count, format0f), Cell(dayAgoData.new_device_count, format0f), Cell(dayDayAgoData.new_device_count, format0f), percentCell(huanbi(data.new_device_count, dayAgoData.new_device_count), format1fp), percentCell(huanbi(data.new_device_count, weekAgoData.new_device_count), format1fp)) ::
        Row( Cell(data.active_retain_1d, format1fp), Cell(dayAgoData.active_retain_1d, format1fp), Cell(dayDayAgoData.active_retain_1d, format1fp), percentCell(huanbi(data.active_retain_1d, dayAgoData.active_retain_1d), format1fp), percentCell(huanbi(data.active_retain_1d, weekAgoData.active_retain_1d), format1fp)) ::
        Row( Cell(data.new_retain_1d, format1fp), Cell(dayAgoData.new_retain_1d, format1fp), Cell(dayDayAgoData.new_retain_1d, format1fp), percentCell(huanbi(data.new_retain_1d, dayAgoData.new_retain_1d), format1fp), percentCell(huanbi(data.new_retain_1d, weekAgoData.new_retain_1d), format1fp)) ::
        Row( Cell(data.active_retain_30d, format1fp), Cell(dayAgoData.active_retain_30d, format1fp), Cell(dayDayAgoData.active_retain_30d, format1fp), percentCell(huanbi(data.active_retain_30d, dayAgoData.active_retain_30d), format1fp), percentCell(huanbi(data.active_retain_30d, weekAgoData.active_retain_30d), format1fp)) ::
        Row( Cell(data.avg_session_minutes, format2f), Cell(dayAgoData.avg_session_minutes, format2f), Cell(dayDayAgoData.avg_session_minutes, format2f), percentCell(huanbi(data.avg_session_minutes, dayAgoData.avg_session_minutes), format1fp), percentCell(huanbi(data.avg_session_minutes, weekAgoData.avg_session_minutes), format1fp)) ::
        Row( Cell(data.post_count, format0f), Cell(dayAgoData.post_count, format0f), Cell(dayDayAgoData.post_count, format0f), percentCell(huanbi(data.post_count, dayAgoData.post_count), format1fp), percentCell(huanbi(data.post_count, weekAgoData.post_count), format1fp)) ::
        Row( Cell(data.post_user_count, format0f), Cell(dayAgoData.post_user_count, format0f), Cell(dayDayAgoData.post_user_count, format0f), percentCell(huanbi(data.post_user_count, dayAgoData.post_user_count), format1fp), percentCell(huanbi(data.post_user_count, weekAgoData.post_user_count), format1fp)) ::
        Row( Cell(data.real_post_count, format0f), Cell(dayAgoData.real_post_count, format0f), Cell(dayDayAgoData.real_post_count, format0f), percentCell(huanbi(data.real_post_count, dayAgoData.real_post_count), format1fp), percentCell(huanbi(data.real_post_count, weekAgoData.real_post_count), format1fp)) ::
        Row( Cell(data.real_post_user_count, format0f), Cell(dayAgoData.real_post_user_count, format0f), Cell(dayDayAgoData.real_post_user_count, format0f), percentCell(huanbi(data.real_post_user_count, dayAgoData.real_post_user_count), format1fp), percentCell(huanbi(data.real_post_user_count, weekAgoData.real_post_user_count), format1fp)) ::
        Row( Cell(data.post_count_surpass_1w_hot, format0f), Cell(dayAgoData.post_count_surpass_1w_hot, format0f), Cell(dayDayAgoData.post_count_surpass_1w_hot, format0f), percentCell(huanbi(data.post_count_surpass_1w_hot, dayAgoData.post_count_surpass_1w_hot), format1fp), percentCell(huanbi(data.post_count_surpass_1w_hot, weekAgoData.post_count_surpass_1w_hot), format1fp)) ::
        Row( Cell(data.dstr_text_photo_pv, format0f), Cell(dayAgoData.dstr_text_photo_pv, format0f), Cell(dayDayAgoData.dstr_text_photo_pv, format0f), percentCell(huanbi(data.dstr_text_photo_pv, dayAgoData.dstr_text_photo_pv), format1fp), percentCell(huanbi(data.dstr_text_photo_pv, weekAgoData.dstr_text_photo_pv), format1fp)) ::
        Row( Cell(data.dstr_video_pv, format0f), Cell(dayAgoData.dstr_video_pv, format0f), Cell(dayDayAgoData.dstr_video_pv, format0f), percentCell(huanbi(data.dstr_video_pv, dayAgoData.dstr_video_pv), format1fp), percentCell(huanbi(data.dstr_video_pv, weekAgoData.dstr_video_pv), format1fp)) ::
        Row( Cell(data.interaction_uv, format0f), Cell(dayAgoData.interaction_uv, format0f), Cell(dayDayAgoData.interaction_uv, format0f), percentCell(huanbi(data.interaction_uv, dayAgoData.interaction_uv), format1fp), percentCell(huanbi(data.interaction_uv, weekAgoData.interaction_uv), format1fp)) ::
        Row( Cell(data.comment_to_praise_ratio, format1fp), Cell(dayAgoData.comment_to_praise_ratio, format1fp), Cell(dayDayAgoData.comment_to_praise_ratio, format1fp), percentCell(huanbi(data.comment_to_praise_ratio, dayAgoData.comment_to_praise_ratio), format1fp), percentCell(huanbi(data.comment_to_praise_ratio, weekAgoData.comment_to_praise_ratio), format1fp)) ::
        Row( Cell(data.avg_creator_new_fans, format2f), Cell(dayAgoData.avg_creator_new_fans, format2f), Cell(dayDayAgoData.avg_creator_new_fans, format2f), percentCell(huanbiOption(data.avg_creator_new_fans, dayAgoData.avg_creator_new_fans), format1fp), percentCell(huanbiOption(data.avg_creator_new_fans, weekAgoData.avg_creator_new_fans), format1fp)) ::
        Row( Cell(data.money_pay, format2f), Cell(dayAgoData.money_pay, format2f), Cell(dayDayAgoData.money_pay, format2f), percentCell(huanbi(data.money_pay, dayAgoData.money_pay), format1fp), percentCell(huanbi(data.money_pay, weekAgoData.money_pay), format1fp)) ::
        Row( Cell(data.users_pay, format0f), Cell(dayAgoData.users_pay, format0f), Cell(dayDayAgoData.users_pay, format0f), percentCell(huanbi(data.users_pay, dayAgoData.users_pay), format1fp), percentCell(huanbi(data.users_pay, weekAgoData.users_pay), format1fp)) ::
        Row( Cell(data.users_recharge, format0f), Cell(dayAgoData.users_recharge, format0f), Cell(dayDayAgoData.users_recharge, format0f), percentCell(huanbi(data.users_recharge, dayAgoData.users_recharge), format1fp), percentCell(huanbi(data.users_recharge, weekAgoData.users_recharge), format1fp)) :: Nil
      ,
      footnote = Footnote(
        Note("环比: 环比昨日"),
        Note("同比: 同比上周同期"),
        Note("分发口径: 图文浏览时长 >= 3 秒，视频播放时长 >= 5秒"),
        Note("人均涨粉量：平均每个创作者增加的涨粉丝，创作者指C级及以上创作者"),
        Note("发文量：图文、视频、问答等（不含聊聊），且公开、未屏蔽、非导入、非引入"),
        Note("发布量：图文、视频、问答等（含聊聊），且公开、未屏蔽、非导入、非引入"),
        Note("-- 代表内容尚未产出")
      ).some
    ).width(880).withStyle("margin-bottom" -> "20px")

    val mailBody = TableHtmlRenderer(table, styler = None).html

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com")  // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "lofter-daily@list.nie.netease.com" :: "lofter-op@hz.netease.com" :: Nil,
      bcc = "gzq.wmtl@list.nie.netease.com" :: "wm_data_analysis@list.nie.netease.com" :: "data.wm@list.nie.netease.com" :: Nil,
      subject = s"$dt LOFTER核心看板",
      message = "有问题请联系 hzxiaonaitong@corp.netease.com",
      richMessage = Some(mailBody)
    )

    spark.close()
  }
}
