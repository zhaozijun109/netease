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

object MboReport2024 {
  val format2fp: NumberFormatter = NumberFormatter("0.00%", "0.00%".some)

  case class PlatformValue(platform: String, value: Double) {
    def toTuple: (String, Double) = platform -> value
  }


  case class RevenueValue(module_or_business: String, value: Double) {
    def toTuple: (String, Double) = module_or_business -> value
  }

  case class AffinityEcologyEntry(active_retain_ratio: Double, new_retain_ratio: Double,new_retain_7d_ratio: Double,
                                  return_retain_ratio: Double, return_retain_7d_ratio: Double, pertime_min: Double,
                                  avg_active_days_30d: Double, middle_high_active_ratio: Double, interactive_ratio: Double)

  case class ScaleEcologyEntry(new_active: Long, return_active: Long, old_active: Long, mau: Long,
                               new_device_count: Long,natural_new_device_count: Long, content_new_device_count: Long,
                               user_new_device_count: Long,member_new_device_count: Long, else_new_device_count: Long,
                               return_device_count: Long,natural_return_device_count: Long, content_return_device_count: Long,
                               user_return_device_count: Long,member_return_device_count: Long, else_return_device_count: Long
                              ) {
    def dau: Long = new_active + return_active + old_active
  }

  case class ContentEcologyEntry(all_post_cnt_1d: Long, core_ip_post_cnt_1d: Long, new_ip_post_cnt_1d: Long,
                                 film_tv_star_ip_post_cnt_1d: Long, game_cartoon_ip_post_cnt_1d: Long, kecp_post_cnt_1d: Long,
                                 all_valid_creator: Long, film_tv_star_ip_valid_creator: Long, game_cartoon_ip_valid_creator: Long,
                                 kecp_valid_creator: Long, benchmark_creator: Long, all_premium_posts: Long, core_ip_premium_post: Long,
                                 new_ip_premium_post: Long, premium_posts_7d_ratio: Double, premium_posts_30d_ratio: Double,
                                 premium_posts_30d_realpv_ratio: Double, premium_posts_7d_realpv_ratio: Double)

  case class ContentConsumeEcologyEntry(all_realbrowseplaypv: Long, photo_realbrowseplaypv: Long, text_realbrowseplaypv: Long,
                                        video_realbrowseplaypv: Long, collection_noncentralized_realbrowseplaypv: Long,
                                        tag_noncentralized_realbrowseplaypv: Long, else_noncentralized_realbrowseplaypv: Long,
                                        avg_realbrowseplaypv: Double, valid_comment_pv: Long) {
    def noncentralized_realbrowseplaypv: Long  = collection_noncentralized_realbrowseplaypv + tag_noncentralized_realbrowseplaypv + else_noncentralized_realbrowseplaypv
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
      case "全部" | "all" | "内容付费" | "短篇业务" | "趣味电商" | "虚拟社交" => row.withStyle("font-weight" -> "bold")
      case _ => row
    }
  }

  def emphasize[T <: Cell](row: Row[T]): Row[T] = {
    row.withStyle("font-weight" -> "bold")
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val day = pargs.getOrElse("date", DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")
    val weekAgo = DateTime.parse(day).minusDays(7).toString("yyyy-MM-dd")
    val startDate = DateTime.parse(day)

    val spark: SparkSession = SparkSession.builder()
      .appName("Lofter Mbo Report 2023")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    import spark.implicits._

    implicit def convPlatformValuesToMap(data: Dataset[PlatformValue]): Map[String, Double] = {
      val values = data.collect().map(_.toTuple)
      val all = "全部" -> values.map(_._2).sum
      (all +: values).toMap.withDefaultValue(Double.NaN)
    }
    // scale
    val scaleData = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_scale_di where dt='$day'").as[ScaleEcologyEntry].collect().head
    val scaleDataPrevDay = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_scale_di where dt='$dayAgo'").as[ScaleEcologyEntry].collect().head
    val scaleDataPrevWeek = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_scale_di where dt='$weekAgo'").as[ScaleEcologyEntry].collect().head

    // affinity
    val affinityData = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_affinity_di where dt='$day'").as[AffinityEcologyEntry].collect().head
    val affinityDataPrevDay = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_affinity_di where dt='$dayAgo'").as[AffinityEcologyEntry].collect().head
    val affinityDataPrevWeek = spark.sql(s"select * from lofter_dm.ads_par_device_ecology_affinity_di where dt='$weekAgo'").as[AffinityEcologyEntry].collect().head

    val table1 = Table(
      title = Title("第1部分：用户规模").some,
      hHeaderRows = Seq(
        Row(caption(Cell("模块", 2, 1)), caption(Cell("指标")), caption(Cell("子项")), caption(TextCell("今日")), caption(Cell("环比")), caption(Cell("同比")))
      ),
      vHeaderRows = Seq(
        emphasize(Row(Cell("用户规模", 2, 15), Cell("日活", 1, 4), Cell("全部"))),
        Row(Cell("新增用户沉淀日活（30日）")),
        Row(Cell("回流用户沉淀日活（30日）")),
        Row(Cell("持续活跃设备数")),
        Row(Cell("月活", 2, 1)),
        emphasize(Row(Cell("新增设备数", 1, 5), Cell("全部"))),
        Row(Cell("自然新增")),
        Row(Cell("内容拉新")),
        Row(Cell("用户拉新")),
        Row(Cell("会员拉新")),
        emphasize(Row(Cell("回流设备数", 1, 5), Cell("全部"))),
        Row(Cell("自然回流")),
        Row(Cell("内容回流")),
        Row(Cell("用户回流")),
        Row(Cell("会员回流")),
        Row(Cell("用户粘性", 2, 9), Cell("整体日活次留", 2, 1)),
        Row(Cell("人均停留时长（min）", 2, 1)),
        Row(Cell("用户月活跃天数", 2, 1)),
        Row(Cell("中高活用户月活占比", 2, 1)),
        Row(Cell("新增次留", 2, 1)),
        Row(Cell("新增7留", 2, 1)),
        Row(Cell("回流次留", 2, 1)),
        Row(Cell("回流7留", 2, 1)),
        Row(Cell("互动渗透率", 2, 1))
      ),
      dataRows = Seq.empty
        :+ emphasize(Row( Cell(scaleData.dau, format0f), percentCell(huanbi(scaleData.dau, scaleDataPrevDay.dau), format1fp), percentCell(huanbi(scaleData.dau, scaleDataPrevWeek.dau), format1fp)))
        :+ Row( Cell(scaleData.new_active, format0f), percentCell(huanbi(scaleData.new_active, scaleDataPrevDay.new_active), format1fp), percentCell(huanbi(scaleData.new_active, scaleDataPrevWeek.new_active), format1fp))
        :+ Row( Cell(scaleData.return_active, format0f), percentCell(huanbi(scaleData.return_active, scaleDataPrevDay.return_active), format1fp), percentCell(huanbi(scaleData.return_active, scaleDataPrevWeek.return_active), format1fp))
        :+ Row( Cell(scaleData.old_active, format0f), percentCell(huanbi(scaleData.old_active, scaleDataPrevDay.old_active), format1fp), percentCell(huanbi(scaleData.old_active, scaleDataPrevWeek.old_active), format1fp))
        :+ Row( Cell(scaleData.mau, format0f), percentCell(huanbi(scaleData.mau, scaleDataPrevDay.mau), format1fp), percentCell(huanbi(scaleData.mau, scaleDataPrevWeek.mau), format1fp))
        :+ emphasize(Row( Cell(scaleData.new_device_count, format0f), percentCell(huanbi(scaleData.new_device_count, scaleDataPrevDay.new_device_count), format1fp), percentCell(huanbi(scaleData.new_device_count, scaleDataPrevWeek.new_device_count), format1fp)))
        :+ Row( Cell(scaleData.natural_new_device_count, format0f), percentCell(huanbi(scaleData.natural_new_device_count, scaleDataPrevDay.natural_new_device_count), format1fp), percentCell(huanbi(scaleData.natural_new_device_count, scaleDataPrevWeek.natural_new_device_count), format1fp))
        :+ Row( Cell(scaleData.content_new_device_count, format0f), percentCell(huanbi(scaleData.content_new_device_count, scaleDataPrevDay.content_new_device_count), format1fp), percentCell(huanbi(scaleData.content_new_device_count, scaleDataPrevWeek.content_new_device_count), format1fp))
        :+ Row( Cell(scaleData.user_new_device_count, format0f), percentCell(huanbi(scaleData.user_new_device_count, scaleDataPrevDay.user_new_device_count), format1fp), percentCell(huanbi(scaleData.user_new_device_count, scaleDataPrevWeek.user_new_device_count), format1fp))
        :+ Row( Cell(scaleData.member_new_device_count, format0f), percentCell(huanbi(scaleData.member_new_device_count, scaleDataPrevDay.member_new_device_count), format1fp), percentCell(huanbi(scaleData.member_new_device_count, scaleDataPrevWeek.member_new_device_count), format1fp))
        :+ emphasize(Row( Cell(scaleData.return_device_count, format0f), percentCell(huanbi(scaleData.return_device_count, scaleDataPrevDay.return_device_count), format1fp), percentCell(huanbi(scaleData.return_device_count, scaleDataPrevWeek.return_device_count), format1fp)))
        :+ Row( Cell(scaleData.natural_return_device_count, format0f), percentCell(huanbi(scaleData.natural_return_device_count, scaleDataPrevDay.natural_return_device_count), format1fp), percentCell(huanbi(scaleData.natural_return_device_count, scaleDataPrevWeek.natural_return_device_count), format1fp))
        :+ Row( Cell(scaleData.content_return_device_count, format0f), percentCell(huanbi(scaleData.content_return_device_count, scaleDataPrevDay.content_return_device_count), format1fp), percentCell(huanbi(scaleData.content_return_device_count, scaleDataPrevWeek.content_return_device_count), format1fp))
        :+ Row( Cell(scaleData.user_return_device_count, format0f), percentCell(huanbi(scaleData.user_return_device_count, scaleDataPrevDay.user_return_device_count), format1fp), percentCell(huanbi(scaleData.user_return_device_count, scaleDataPrevWeek.user_return_device_count), format1fp))
        :+ Row( Cell(scaleData.member_return_device_count, format0f), percentCell(huanbi(scaleData.member_return_device_count, scaleDataPrevDay.member_return_device_count), format1fp), percentCell(huanbi(scaleData.member_return_device_count, scaleDataPrevWeek.member_return_device_count), format1fp))
        :+ Row( Cell(affinityData.active_retain_ratio, format1fp), percentCell(huanbi(affinityData.active_retain_ratio, affinityDataPrevDay.active_retain_ratio), format1fp), percentCell(huanbi(affinityData.active_retain_ratio, affinityDataPrevWeek.active_retain_ratio), format1fp))
        :+ Row( Cell(affinityData.pertime_min, format2f), percentCell(huanbi(affinityData.pertime_min, affinityDataPrevDay.pertime_min), format1fp), percentCell(huanbi(affinityData.pertime_min, affinityDataPrevWeek.pertime_min), format1fp))
        :+ Row( Cell(affinityData.avg_active_days_30d, format2f), percentCell(huanbi(affinityData.avg_active_days_30d, affinityDataPrevDay.avg_active_days_30d), format1fp), percentCell(huanbi(affinityData.avg_active_days_30d, affinityDataPrevWeek.avg_active_days_30d), format1fp))
        :+ Row( Cell(affinityData.middle_high_active_ratio, format1fp), percentCell(huanbi(affinityData.middle_high_active_ratio, affinityDataPrevDay.middle_high_active_ratio), format1fp), percentCell(huanbi(affinityData.middle_high_active_ratio, affinityDataPrevWeek.middle_high_active_ratio), format1fp))
        :+ Row( Cell(affinityData.new_retain_ratio, format1fp), percentCell(huanbi(affinityData.new_retain_ratio, affinityDataPrevDay.new_retain_ratio), format1fp), percentCell(huanbi(affinityData.new_retain_ratio, affinityDataPrevWeek.new_retain_ratio), format1fp))
        :+ Row( Cell(affinityData.new_retain_7d_ratio, format1fp), percentCell(huanbi(affinityData.new_retain_7d_ratio, affinityDataPrevDay.new_retain_7d_ratio), format1fp), percentCell(huanbi(affinityData.new_retain_7d_ratio, affinityDataPrevWeek.new_retain_7d_ratio), format1fp))
        :+ Row( Cell(affinityData.return_retain_ratio, format1fp), percentCell(huanbi(affinityData.return_retain_ratio, affinityDataPrevDay.return_retain_ratio), format1fp), percentCell(huanbi(affinityData.return_retain_ratio, affinityDataPrevWeek.return_retain_ratio), format1fp))
        :+ Row( Cell(affinityData.return_retain_7d_ratio, format1fp), percentCell(huanbi(affinityData.return_retain_7d_ratio, affinityDataPrevDay.return_retain_7d_ratio), format1fp), percentCell(huanbi(affinityData.return_retain_7d_ratio, affinityDataPrevWeek.return_retain_7d_ratio), format1fp))
        :+ Row( Cell(affinityData.interactive_ratio, format1fp), percentCell(huanbi(affinityData.interactive_ratio, affinityDataPrevDay.interactive_ratio), format1fp), percentCell(huanbi(affinityData.interactive_ratio, affinityDataPrevWeek.interactive_ratio), format1fp))
      ,
      footnote = Footnote(
        Note("新增/回流用户沉淀日活：近30日新增/回流的用户在今日活跃的设备数"),
        Note("中高活用户月活占比：近30日活跃10天以上用户÷近30日活跃设备数")
      ).some
    ).width(880).withStyle("margin-bottom" -> "20px")

    val renderHtml1 = TableHtmlRenderer(table1, styler = None ).html

    // content consume
    val contentConsumeData = spark.sql(s"select * from lofter_dm.ads_par_user_ecology_content_consume_di where dt='$day'").as[ContentConsumeEcologyEntry].collect().head
    val contentConsumeDataPrevDay = spark.sql(s"select * from lofter_dm.ads_par_user_ecology_content_consume_di where dt='$dayAgo'").as[ContentConsumeEcologyEntry].collect().head
    val contentConsumeDataPrevWeek = spark.sql(s"select * from lofter_dm.ads_par_user_ecology_content_consume_di where dt='$weekAgo'").as[ContentConsumeEcologyEntry].collect().head

    // content ecology
    val contentEcologyData = spark.sql(s"select * from lofter_dm.ads_content_ecology_di where dt='$day'").as[ContentEcologyEntry].collect().head
    val contentEcologyDataPrevDay = spark.sql(s"select * from lofter_dm.ads_content_ecology_di where dt='$dayAgo'").as[ContentEcologyEntry].collect().head
    val contentEcologyDataPrevWeek = spark.sql(s"select * from lofter_dm.ads_content_ecology_di where dt='$weekAgo'").as[ContentEcologyEntry].collect().head

    val table2 = Table(
      title = Title("第2部分：内容生态").some,
      hHeaderRows = Seq(
        Row(caption(Cell("模块", 2, 1)), caption(Cell("指标")), caption(Cell("子项")), caption(TextCell("今日")), caption(Cell("环比")), caption(Cell("同比")))
      ),
      vHeaderRows = Seq(
        emphasize(Row(Cell("内容消费", 2, 10), Cell("全局内容有效PV", 1, 4), Cell("全部"))),
        Row(Cell("图片")),
        Row(Cell("文章")),
        Row(Cell("视频")),
        emphasize(Row(Cell("非中心化有效PV", 1, 4), Cell("全部"))),
        Row(Cell("合集有效PV")),
        Row(Cell("TAG有效PV")),
        Row(Cell("其它")),
        Row(Cell("人均有效pv", 2, 1)),
        Row(Cell("全局有效评论数", 2, 1)),
        emphasize(Row(Cell("内容生态", 2, 18), Cell("发文量", 1, 5), Cell("全部"))),
        Row(Cell("核心IP圈层")),
        Row(Cell("新增IP圈层")),
        Row(Cell("影视明星IP圈层")),
        Row(Cell("游戏二次元IP圈层")),
        emphasize(Row(Cell("月有效创作者规模", 1, 3), Cell("全部"))),
        Row(Cell("影视明星IP圈层")),
        Row(Cell("游戏二次元IP圈层")),
        Row(Cell("标杆创作者规模", 2, 1)),
        emphasize(Row(Cell("优质内容数", 1, 5), Cell("全部"))),
        Row(Cell("核心IP圈层")),
        Row(Cell("新增IP圈层")),
        Row(Cell("优质内容7日文章占比")),
        Row(Cell("优质内容30日文章占比")),
        emphasize(Row(Cell("优质内容流量占比", 1, 2), Cell("30日内发布流量占比"))),
        Row(Cell("7日内发布流量占比"))
      ),
      dataRows = Seq.empty
        :+ emphasize(Row( Cell(contentConsumeData.all_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.all_realbrowseplaypv, contentConsumeDataPrevDay.all_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.all_realbrowseplaypv, contentConsumeDataPrevWeek.all_realbrowseplaypv), format1fp)))
        :+ Row( Cell(contentConsumeData.photo_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.photo_realbrowseplaypv, contentConsumeDataPrevDay.photo_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.photo_realbrowseplaypv, contentConsumeDataPrevWeek.photo_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.text_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.text_realbrowseplaypv, contentConsumeDataPrevDay.text_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.text_realbrowseplaypv, contentConsumeDataPrevWeek.text_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.video_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.video_realbrowseplaypv, contentConsumeDataPrevDay.video_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.video_realbrowseplaypv, contentConsumeDataPrevWeek.video_realbrowseplaypv), format1fp))
        :+ emphasize(Row( Cell(contentConsumeData.noncentralized_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.noncentralized_realbrowseplaypv, contentConsumeDataPrevDay.noncentralized_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.noncentralized_realbrowseplaypv, contentConsumeDataPrevWeek.noncentralized_realbrowseplaypv), format1fp)))
        :+ Row( Cell(contentConsumeData.collection_noncentralized_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.collection_noncentralized_realbrowseplaypv, contentConsumeDataPrevDay.collection_noncentralized_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.collection_noncentralized_realbrowseplaypv, contentConsumeDataPrevWeek.collection_noncentralized_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.tag_noncentralized_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.tag_noncentralized_realbrowseplaypv, contentConsumeDataPrevDay.tag_noncentralized_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.tag_noncentralized_realbrowseplaypv, contentConsumeDataPrevWeek.tag_noncentralized_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.else_noncentralized_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.else_noncentralized_realbrowseplaypv, contentConsumeDataPrevDay.else_noncentralized_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.else_noncentralized_realbrowseplaypv, contentConsumeDataPrevWeek.else_noncentralized_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.avg_realbrowseplaypv, format0f), percentCell(huanbi(contentConsumeData.avg_realbrowseplaypv, contentConsumeDataPrevDay.avg_realbrowseplaypv), format1fp), percentCell(huanbi(contentConsumeData.avg_realbrowseplaypv, contentConsumeDataPrevWeek.avg_realbrowseplaypv), format1fp))
        :+ Row( Cell(contentConsumeData.valid_comment_pv, format0f), percentCell(huanbi(contentConsumeData.valid_comment_pv, contentConsumeDataPrevDay.valid_comment_pv), format1fp), percentCell(huanbi(contentConsumeData.valid_comment_pv, contentConsumeDataPrevWeek.valid_comment_pv), format1fp))
        :+ emphasize(Row( Cell(contentEcologyData.all_post_cnt_1d, format0f), percentCell(huanbi(contentEcologyData.all_post_cnt_1d, contentEcologyDataPrevDay.all_post_cnt_1d), format1fp), percentCell(huanbi(contentEcologyData.all_post_cnt_1d, contentEcologyDataPrevWeek.all_post_cnt_1d), format1fp)))
        :+ Row( Cell(contentEcologyData.core_ip_post_cnt_1d, format0f), percentCell(huanbi(contentEcologyData.core_ip_post_cnt_1d, contentEcologyDataPrevDay.core_ip_post_cnt_1d), format1fp), percentCell(huanbi(contentEcologyData.core_ip_post_cnt_1d, contentEcologyDataPrevWeek.core_ip_post_cnt_1d), format1fp))
        :+ Row( Cell(contentEcologyData.new_ip_post_cnt_1d, format0f), percentCell(huanbi(contentEcologyData.new_ip_post_cnt_1d, contentEcologyDataPrevDay.new_ip_post_cnt_1d), format1fp), percentCell(huanbi(contentEcologyData.new_ip_post_cnt_1d, contentEcologyDataPrevWeek.new_ip_post_cnt_1d), format1fp))
        :+ Row( Cell(contentEcologyData.film_tv_star_ip_post_cnt_1d, format0f), percentCell(huanbi(contentEcologyData.film_tv_star_ip_post_cnt_1d, contentEcologyDataPrevDay.film_tv_star_ip_post_cnt_1d), format1fp), percentCell(huanbi(contentEcologyData.film_tv_star_ip_post_cnt_1d, contentEcologyDataPrevWeek.film_tv_star_ip_post_cnt_1d), format1fp))
        :+ Row( Cell(contentEcologyData.game_cartoon_ip_post_cnt_1d, format0f), percentCell(huanbi(contentEcologyData.game_cartoon_ip_post_cnt_1d, contentEcologyDataPrevDay.game_cartoon_ip_post_cnt_1d), format1fp), percentCell(huanbi(contentEcologyData.game_cartoon_ip_post_cnt_1d, contentEcologyDataPrevWeek.game_cartoon_ip_post_cnt_1d), format1fp))
        :+ emphasize(Row( Cell(contentEcologyData.all_valid_creator, format0f), percentCell(huanbi(contentEcologyData.all_valid_creator, contentEcologyDataPrevDay.all_valid_creator), format1fp), percentCell(huanbi(contentEcologyData.all_valid_creator, contentEcologyDataPrevWeek.all_valid_creator), format1fp)))
        :+ Row( Cell(contentEcologyData.film_tv_star_ip_valid_creator, format0f), percentCell(huanbi(contentEcologyData.film_tv_star_ip_valid_creator, contentEcologyDataPrevDay.film_tv_star_ip_valid_creator), format1fp), percentCell(huanbi(contentEcologyData.film_tv_star_ip_valid_creator, contentEcologyDataPrevWeek.film_tv_star_ip_valid_creator), format1fp))
        :+ Row( Cell(contentEcologyData.game_cartoon_ip_valid_creator, format0f), percentCell(huanbi(contentEcologyData.game_cartoon_ip_valid_creator, contentEcologyDataPrevDay.game_cartoon_ip_valid_creator), format1fp), percentCell(huanbi(contentEcologyData.game_cartoon_ip_valid_creator, contentEcologyDataPrevWeek.game_cartoon_ip_valid_creator), format1fp))
        :+ Row( Cell(contentEcologyData.benchmark_creator, format0f), percentCell(huanbi(contentEcologyData.benchmark_creator, contentEcologyDataPrevDay.benchmark_creator), format1fp), percentCell(huanbi(contentEcologyData.benchmark_creator, contentEcologyDataPrevWeek.benchmark_creator), format1fp))
        :+ emphasize(Row( Cell(contentEcologyData.all_premium_posts, format0f), percentCell(huanbi(contentEcologyData.all_premium_posts, contentEcologyDataPrevDay.all_premium_posts), format1fp), percentCell(huanbi(contentEcologyData.all_premium_posts, contentEcologyDataPrevWeek.all_premium_posts), format1fp)))
        :+ Row( Cell(contentEcologyData.core_ip_premium_post, format0f), percentCell(huanbi(contentEcologyData.core_ip_premium_post, contentEcologyDataPrevDay.core_ip_premium_post), format1fp), percentCell(huanbi(contentEcologyData.core_ip_premium_post, contentEcologyDataPrevWeek.core_ip_premium_post), format1fp))
        :+ Row( Cell(contentEcologyData.new_ip_premium_post, format0f), percentCell(huanbi(contentEcologyData.new_ip_premium_post, contentEcologyDataPrevDay.new_ip_premium_post), format1fp), percentCell(huanbi(contentEcologyData.new_ip_premium_post, contentEcologyDataPrevWeek.new_ip_premium_post), format1fp))
        :+ Row( Cell(contentEcologyData.premium_posts_7d_ratio, format1fp), percentCell(huanbi(contentEcologyData.premium_posts_7d_ratio, contentEcologyDataPrevDay.premium_posts_7d_ratio), format1fp), percentCell(huanbi(contentEcologyData.premium_posts_7d_ratio, contentEcologyDataPrevWeek.premium_posts_7d_ratio), format1fp))
        :+ Row( Cell(contentEcologyData.premium_posts_30d_ratio, format1fp), percentCell(huanbi(contentEcologyData.premium_posts_30d_ratio, contentEcologyDataPrevDay.premium_posts_30d_ratio), format1fp), percentCell(huanbi(contentEcologyData.premium_posts_30d_ratio, contentEcologyDataPrevWeek.premium_posts_30d_ratio), format1fp))
        :+ Row( Cell(contentEcologyData.premium_posts_30d_realpv_ratio, format1fp), percentCell(huanbi(contentEcologyData.premium_posts_30d_realpv_ratio, contentEcologyDataPrevDay.premium_posts_30d_realpv_ratio), format1fp), percentCell(huanbi(contentEcologyData.premium_posts_30d_realpv_ratio, contentEcologyDataPrevWeek.premium_posts_30d_realpv_ratio), format1fp))
        :+ Row( Cell(contentEcologyData.premium_posts_7d_realpv_ratio, format1fp), percentCell(huanbi(contentEcologyData.premium_posts_7d_realpv_ratio, contentEcologyDataPrevDay.premium_posts_7d_realpv_ratio), format1fp), percentCell(huanbi(contentEcologyData.premium_posts_7d_realpv_ratio, contentEcologyDataPrevWeek.premium_posts_7d_realpv_ratio), format1fp))
      ,
      footnote = Footnote(
        Note("非中心化有效pv：合集、标签、搜索、热搜、关注、个人主页、足迹、我的喜欢等非中心化场景引导的有效pv"),
        Note("核心IP圈层：发文量top50的圈层（剔除\"绘画\"等抽象IP）"),
        Note("新增IP圈层：近30天新增入库的IP"),
        Note("影视明星IP圈层：一级类目为影视、娱乐的IP"),
        Note("游戏二次元IP圈层：一级类目为二次元、游戏的IP"),
        Note("月有效创作者：月发文2篇以上，月篇均有效pv200以上，月总有效 pv1000以上"),
        Note("标杆创作者：生产爆款内容的作者，或近30日有作品（通过规则过滤、ai低质过滤、试投成功）更新的S、A级创作者"),
        Note("优质内容：发文后30天内有效PV＞近30天日均dau/3420（该值在1000上下浮动），且扶持流量占比＜50%且（小蓝手＞近30天日均dau/114000（该值在30上下浮动） 或 有效互动率＞5%）"),
        Note("优质内容7/30日文章占比：近7/30日发文的优质内容数÷近7/30日发文的内容数"),
        Note("优质内容7/30日内发布流量占比：近7/30日发文的优质内容在今日的有效pv÷近7/30日发文的内容在今日的有效pv"),
      ).some
    ).width(880).withStyle("margin-bottom" -> "20px")

    val renderHtml2 = TableHtmlRenderer(table2, styler = None ).html

    // revenue
    implicit def convRevenueValuesToMap(data: Dataset[RevenueValue]): Map[String, Double] = {
      val values = data.collect().map(_.toTuple)
      values.toMap.withDefaultValue(Double.NaN)
    }

    val revenueAmount: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_amount as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='day' and business !='其他'").as[RevenueValue]
    val revenueUv: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_trade_uv as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='day' and business !='其他'").as[RevenueValue]

    val revenueAmountYear: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_amount as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='year' and business !='其他'").as[RevenueValue]
    val revenueUvYear: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_trade_uv as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='year' and business !='其他'").as[RevenueValue]

    val revenueAmountMonth: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_amount as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='month' and business !='其他'").as[RevenueValue]
    val revenueUvMonth: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_trade_uv as value from lofter_dm.ads_mbo_revenue_dd where dt = '$day' and period='month' and business !='其他'").as[RevenueValue]

    val revenueAmountPrevDay: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_amount as value from lofter_dm.ads_mbo_revenue_dd where dt = '$dayAgo' and period='day' and business !='其他'").as[RevenueValue]
    val revenueUvPrevDay: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_trade_uv as value from lofter_dm.ads_mbo_revenue_dd where dt = '$dayAgo' and period='day' and business !='其他'").as[RevenueValue]

    val revenueAmountPrevWeek: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_amount as value from lofter_dm.ads_mbo_revenue_dd where dt = '$weekAgo' and period='day' and business !='其他'").as[RevenueValue]
    val revenueUvPrevWeek: Map[String, Double] = spark.sql(s"select if(business = 'all', module, business) as module_or_business, revenue_trade_uv as value from lofter_dm.ads_mbo_revenue_dd where dt = '$weekAgo' and period='day' and business !='其他'").as[RevenueValue]

    val revenueAmountRows = Seq("内容付费", "礼物付费", "粉丝会员", "书城会员", "广告", "效果广告", "激励广告", "虚拟社交", "装扮商城", "虚拟陪伴").map { p => emphasize(p, Row( Cell(revenueAmount(p), format0f), percentCell(huanbi(revenueAmount(p), revenueAmountPrevDay(p)), format1fp), percentCell(huanbi(revenueAmount(p), revenueAmountPrevWeek(p)), format1fp), Cell(revenueAmountMonth(p), format0f), Cell(revenueAmountYear(p), format0f)))}
    val revenueUvRows = Seq("内容付费", "礼物付费", "粉丝会员", "书城会员", "广告", "效果广告", "激励广告", "虚拟社交", "装扮商城", "虚拟陪伴").map { p => emphasize(p, Row( Cell(revenueUv(p), format0f), percentCell(huanbi(revenueUv(p), revenueUvPrevDay(p)), format1fp), percentCell(huanbi(revenueUv(p), revenueUvPrevWeek(p)), format1fp), Cell(revenueUvMonth(p), format0f), Cell(revenueUvYear(p), format0f)))}

    val table3 = Table(
      title = Title("第3部分：营收").some,
      hHeaderRows = Seq(
        Row(caption(Cell("模块", 2, 1)), caption(Cell("二级模块")), caption(Cell("指标")), caption(TextCell("今日")), caption(Cell("环比")), caption(Cell("同比")), caption(Cell("月累计")), caption(Cell("年累计")))
      ),
      vHeaderRows = Seq(
        emphasize(Row(Cell("营收", 2, 1), Cell("/"), Cell("金额"))),

        emphasize(Row(Cell("内容付费", 2, 8), Cell("全部", 1, 2), Cell("金额"))),
        emphasize(Row(Cell("人数"))),
        Row(Cell("礼物付费", 1, 2), Cell("金额")),
        Row(Cell("人数")),
        Row(Cell("粉丝会员", 1, 2), Cell("金额")),
        Row(Cell("人数")),
        Row(Cell("书城会员", 1, 2), Cell("金额")),
        Row(Cell("人数")),

        emphasize(Row(Cell("广告", 2, 6), Cell("全部", 1, 2), Cell("金额"))),
        emphasize(Row(Cell("人数"))),
        Row(Cell("效果广告", 1, 2), Cell("金额")),
        Row(Cell("人数")),
        Row(Cell("激励广告", 1, 2), Cell("金额")),
        Row(Cell("人数")),

        emphasize(Row(Cell("虚拟社交", 2, 8), Cell("全部", 1, 2), Cell("金额"))),
        emphasize(Row(Cell("人数"))),
        Row(Cell("装扮商城", 1, 2), Cell("金额")),
        Row(Cell("人数")),
        Row(Cell("虚拟陪伴", 1, 2), Cell("金额")),
        Row(Cell("人数"))
      ),
      dataRows = Seq("all").map { p => emphasize(p, Row( Cell(revenueAmount(p), format0f), percentCell(huanbi(revenueAmount(p), revenueAmountPrevDay(p)), format1fp), percentCell(huanbi(revenueAmount(p), revenueAmountPrevWeek(p)), format1fp), Cell(revenueAmountMonth(p), format0f), Cell(revenueAmountYear(p), format0f)))}
        ++ revenueAmountRows.zip(revenueUvRows).flatMap(s => Seq(s._1, s._2))
      ,
      footnote = Footnote(
        Note("详细营收数据及广告数据见<a href=\"https://wenman.youdata.netease.com/dash/folder/450200472?rid=72547\">有数报表</a>"),
        Note("书城会员包含历史博客订阅、畅读大会员数据；粉丝会员包含官方账号会员数据"),
        Note("礼物付费包含糖果券包，图片商城等营收数据"),
        Note("虚拟陪伴包含小剧场数据"),
        Note("环比：对比上一日增幅；同比：对比上一周同期增幅")
      ).some
    ).width(880).withStyle("margin-bottom" -> "20px")

    val renderHtml3 = TableHtmlRenderer(table3, styler = None).html
    val mailBody = renderHtml1 + renderHtml2 + renderHtml3

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com")  // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "lofter-daily@list.nie.netease.com" :: Nil,
      bcc = "gzq.wmtl@list.nie.netease.com" :: "wm_data_analysis@list.nie.netease.com" :: "data.wm@list.nie.netease.com" :: Nil,
      subject = s"$day LOFTER日报",
      message = "有问题请联系 hzxiaonaitong@corp.netease.com",
      richMessage = Some(mailBody)
    )

    spark.close()
  }

}
