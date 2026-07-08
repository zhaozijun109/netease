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

object MboReport2024Revenue {
  val format2fp: NumberFormatter = NumberFormatter("0.00%", "0.00%".some)

  case class ContentEntry(income_below_1k_creators: Long, income_1k_3k_creators: Long, income_3k_5k_creators: Long,income_5k_creators: Long,
                          gift_creators: Long, gift_text_creators: Long, gift_photo_creators: Long,
                          browse_uv: Long, text_browse_uv: Long, photo_browse_uv: Long,
                          gift_ecpm: Double, gift_text_ecpm: Double, gift_photo_ecpm: Double,
                          premium_browse_uv: Long, premium_text_browse_uv: Long, premium_photo_browse_uv: Long,
                          premium_gift_ecpm: Double, premium_gift_money: Double, premium_gift_text_money: Double,
                          premium_gift_photo_money: Double, premium_gift_text_ecpm: Double, premium_gift_photo_ecpm: Double,
                          publish_post_count: Long, publish_text_post_count: Long, publish_photo_post_count: Long,
                          publish_fans_post_count: Long, publish_text_fans_post_count: Long, publish_photo_fans_post_count: Long, premium_post_count: Long,
                          premium_text_post_count: Long, premium_photo_post_count: Long, new_premium_post_count: Long, new_premium_text_post_count: Long,
                          new_premium_photo_post_count: Long, fans_money: Double, total_money: Double, text_money: Double, photo_money: Double) {
    def income_creators: Long = income_below_1k_creators + income_1k_3k_creators + income_3k_5k_creators + income_5k_creators
    def premium_browse_ratio: Double = premium_browse_uv * 1.0 / browse_uv
    def premium_text_browse_ratio: Double = premium_text_browse_uv * 1.0 / text_browse_uv
    def premium_photo_browse_ratio: Double = premium_photo_browse_uv * 1.0 / photo_browse_uv
  }

  case class MarketingEntry(aggregation_collection_money: Double,aggregation_collection_uv: Long,aggregation_grain_money: Double,
                            aggregation_grain_uv: Long, coupon_uv: Long, coupon_money: Double,
                            exchange_num: Long,post_exchange_num: Long,lottery_exchange_num: Long,
                            remain_balance: Long,expire_balance: Long) {
    def aggregation_uv: Long = aggregation_grain_uv + aggregation_collection_uv
    def aggregation_money: Double = aggregation_grain_money + aggregation_collection_money
  }

  case class UserEntry(pay_uv: Long, pay_money: Double, pay_arpu: Double, first_pay_uv: Long, first_pay_money: Double,
                       first_pay_arpu: Double, high_level_pay_uv: Long, high_level_pay_money: Double, high_level_pay_arpu: Double,
                       silent_potential_uv: Long, reservoir_browse_uv: Long,reservoir_hd_uv: Long)

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

    // content
    val content = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_content_di where dt='$day'").as[ContentEntry].collect().head
    val contentPrevDay = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_content_di where dt='$dayAgo'").as[ContentEntry].collect().head
    val contentPrevWeek = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_content_di where dt='$weekAgo'").as[ContentEntry].collect().head

    // user ecology
    val user = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_user_di where dt='$day'").as[UserEntry].collect().head
    val userPrevDay = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_user_di where dt='$dayAgo'").as[UserEntry].collect().head
    val userPrevWeek = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_user_di where dt='$weekAgo'").as[UserEntry].collect().head

    // marketing
    val market = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_marketing_di where dt='$day'").as[MarketingEntry].collect().head
    val marketPrevDay = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_marketing_di where dt='$dayAgo'").as[MarketingEntry].collect().head
    val marketPrevWeek = spark.sql(s"select * from lofter_dm.ads_mbo_revenue_marketing_di where dt='$weekAgo'").as[MarketingEntry].collect().head

    val table1 = Table(
      title = Title("内容付费").some,
      hHeaderRows = Seq(
        Row(caption(Cell("模块", 2, 1)), caption(Cell("指标")), caption(Cell("端")), caption(TextCell("今日")), caption(Cell("环比")), caption(Cell("同比")))
      ),
      vHeaderRows = Seq(
        Row(Cell("营收", 2, 5), Cell("单篇礼物付费金额", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("高级粉丝金额", 1, 1), Cell("全部")),
        Row(Cell("糖果券购买金额", 1, 1), Cell("全部")),
        Row(Cell("消费", 2, 12), Cell("付费内容有效浏览量", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("付费内容千曝礼物收益", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("优质付费内容有效浏览量", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("优质付费内容有效浏览占比", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("内容供给", 2, 23), Cell("付费礼物回礼文章数", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("高级粉丝专属文章数", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("优质付费内容数日新增", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("月有效付费创作者", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("有收益创作者数量", 1, 5), Cell("全部")),
        Row(Cell("月入1000以下")),
        Row(Cell("月入1000~3000")),
        Row(Cell("月入3000~5000")),
        Row(Cell("月入5000以上")),
        Row(Cell("优质付费内容礼物金额", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),
        Row(Cell("优质付费内容千曝礼物收益", 1, 3), Cell("全部")),
        Row(Cell("文字类")),
        Row(Cell("图片类")),

        Row(Cell("用户", 2, 9), Cell("付费用户数", 1, 3), Cell("全部")),
        Row(Cell("首次付费用户")),
        Row(Cell("高净值付费用户")),
        Row(Cell("30日付费用户arppu", 1, 3), Cell("全部")),
        Row(Cell("首次付费用户")),
        Row(Cell("高净值付费用户")),
        Row(Cell("沉默潜在用户库存", 1, 1), Cell("全部")),
        Row(Cell("蓄水池用户数", 1, 2), Cell("当日浏览")),
        Row(Cell("当日互动")),
        Row(Cell("营销", 2, 13), Cell("糖果券购买金额", 1, 1), Cell("全部")),
        Row(Cell("糖果券购买人数", 1, 1), Cell("全部")),
        Row(Cell("糖果券消耗量",1, 3), Cell("全部")),
        Row(Cell("抽奖")),
        Row(Cell("单篇礼物解锁")),
        Row(Cell("糖果券过期总量", 1, 1), Cell("全部")),
        Row(Cell("糖果券剩余总量", 1, 1), Cell("全部")),
        Row(Cell("聚合支付人数",1, 3), Cell("全部")),
        Row(Cell("合集聚合")),
        Row(Cell("粮单聚合")),
        Row(Cell("聚合支付金额",1, 3), Cell("全部")),
        Row(Cell("合集聚合")),
        Row(Cell("粮单聚合"))
      ),
      dataRows = Seq.empty

        :+ Row( Cell(content.total_money, format2f), percentCell(huanbi(content.total_money, contentPrevDay.total_money), format1fp), percentCell(huanbi(content.total_money, contentPrevWeek.total_money), format1fp))
        :+ Row( Cell(content.text_money, format2f), percentCell(huanbi(content.text_money, contentPrevDay.text_money), format1fp), percentCell(huanbi(content.text_money, contentPrevWeek.text_money), format1fp))
        :+ Row( Cell(content.photo_money, format2f), percentCell(huanbi(content.photo_money, contentPrevDay.photo_money), format1fp), percentCell(huanbi(content.photo_money, contentPrevWeek.photo_money), format1fp))
        :+ Row( Cell(content.fans_money, format2f), percentCell(huanbi(content.fans_money, contentPrevDay.fans_money), format1fp), percentCell(huanbi(content.fans_money, contentPrevWeek.fans_money), format1fp))
        :+ Row( Cell(market.coupon_money, format0f), percentCell(huanbi(market.coupon_money, marketPrevDay.coupon_money), format1fp), percentCell(huanbi(market.coupon_money, marketPrevWeek.coupon_money), format1fp))
        :+ Row( Cell(content.browse_uv, format0f), percentCell(huanbi(content.browse_uv, contentPrevDay.browse_uv), format1fp), percentCell(huanbi(content.browse_uv, contentPrevWeek.browse_uv), format1fp))
        :+ Row( Cell(content.text_browse_uv, format0f), percentCell(huanbi(content.text_browse_uv, contentPrevDay.text_browse_uv), format1fp), percentCell(huanbi(content.text_browse_uv, contentPrevWeek.text_browse_uv), format1fp))
        :+ Row( Cell(content.photo_browse_uv, format0f), percentCell(huanbi(content.photo_browse_uv, contentPrevDay.photo_browse_uv), format1fp), percentCell(huanbi(content.photo_browse_uv, contentPrevWeek.photo_browse_uv), format1fp))
        :+ Row( Cell(content.gift_ecpm, format2f), percentCell(huanbi(content.gift_ecpm, contentPrevDay.gift_ecpm), format1fp), percentCell(huanbi(content.gift_ecpm, contentPrevWeek.gift_ecpm), format1fp))
        :+ Row( Cell(content.gift_text_ecpm, format2f), percentCell(huanbi(content.gift_text_ecpm, contentPrevDay.gift_text_ecpm), format1fp), percentCell(huanbi(content.gift_text_ecpm, contentPrevWeek.gift_text_ecpm), format1fp))
        :+ Row( Cell(content.gift_photo_ecpm, format2f), percentCell(huanbi(content.gift_photo_ecpm, contentPrevDay.gift_photo_ecpm), format1fp), percentCell(huanbi(content.gift_photo_ecpm, contentPrevWeek.gift_photo_ecpm), format1fp))
        :+ Row( Cell(content.premium_browse_uv, format0f), percentCell(huanbi(content.premium_browse_uv, contentPrevDay.premium_browse_uv), format1fp), percentCell(huanbi(content.premium_browse_uv, contentPrevWeek.premium_browse_uv), format1fp))
        :+ Row( Cell(content.premium_text_browse_uv, format0f), percentCell(huanbi(content.premium_text_browse_uv, contentPrevDay.premium_text_browse_uv), format1fp), percentCell(huanbi(content.premium_text_browse_uv, contentPrevWeek.premium_text_browse_uv), format1fp))
        :+ Row( Cell(content.premium_photo_browse_uv, format0f), percentCell(huanbi(content.premium_photo_browse_uv, contentPrevDay.premium_photo_browse_uv), format1fp), percentCell(huanbi(content.premium_photo_browse_uv, contentPrevWeek.premium_photo_browse_uv), format1fp))
        :+ Row( Cell(content.premium_browse_ratio, format1fp), percentCell(huanbi(content.premium_browse_ratio, contentPrevDay.premium_browse_ratio), format1fp), percentCell(huanbi(content.premium_browse_ratio, contentPrevWeek.premium_browse_ratio), format1fp))
        :+ Row( Cell(content.premium_text_browse_ratio, format1fp), percentCell(huanbi(content.premium_text_browse_ratio, contentPrevDay.premium_text_browse_ratio), format1fp), percentCell(huanbi(content.premium_text_browse_ratio, contentPrevWeek.premium_text_browse_ratio), format1fp))
        :+ Row( Cell(content.premium_photo_browse_ratio, format1fp), percentCell(huanbi(content.premium_photo_browse_ratio, contentPrevDay.premium_photo_browse_ratio), format1fp), percentCell(huanbi(content.premium_photo_browse_ratio, contentPrevWeek.premium_photo_browse_ratio), format1fp))
        :+ Row( Cell(content.publish_post_count, format0f), percentCell(huanbi(content.publish_post_count, contentPrevDay.publish_post_count), format1fp), percentCell(huanbi(content.publish_post_count, contentPrevWeek.publish_post_count), format1fp))
        :+ Row( Cell(content.publish_text_post_count, format0f), percentCell(huanbi(content.publish_text_post_count, contentPrevDay.publish_text_post_count), format1fp), percentCell(huanbi(content.publish_text_post_count, contentPrevWeek.publish_text_post_count), format1fp))
        :+ Row( Cell(content.publish_photo_post_count, format0f), percentCell(huanbi(content.publish_photo_post_count, contentPrevDay.publish_photo_post_count), format1fp), percentCell(huanbi(content.publish_photo_post_count, contentPrevWeek.publish_photo_post_count), format1fp))
        :+ Row( Cell(content.publish_fans_post_count, format0f), percentCell(huanbi(content.publish_fans_post_count, contentPrevDay.publish_fans_post_count), format1fp), percentCell(huanbi(content.publish_fans_post_count, contentPrevWeek.publish_fans_post_count), format1fp))
        :+ Row( Cell(content.publish_text_fans_post_count, format0f), percentCell(huanbi(content.publish_text_fans_post_count, contentPrevDay.publish_text_fans_post_count), format1fp), percentCell(huanbi(content.publish_text_fans_post_count, contentPrevWeek.publish_text_fans_post_count), format1fp))
        :+ Row( Cell(content.publish_photo_fans_post_count, format0f), percentCell(huanbi(content.publish_photo_fans_post_count, contentPrevDay.publish_photo_fans_post_count), format1fp), percentCell(huanbi(content.publish_photo_fans_post_count, contentPrevWeek.publish_photo_fans_post_count), format1fp))
        :+ Row( Cell(content.new_premium_post_count, format0f), percentCell(huanbi(content.new_premium_post_count, contentPrevDay.new_premium_post_count), format1fp), percentCell(huanbi(content.new_premium_post_count, contentPrevWeek.new_premium_post_count), format1fp))
        :+ Row( Cell(content.new_premium_text_post_count, format0f), percentCell(huanbi(content.new_premium_text_post_count, contentPrevDay.new_premium_text_post_count), format1fp), percentCell(huanbi(content.new_premium_text_post_count, contentPrevWeek.new_premium_text_post_count), format1fp))
        :+ Row( Cell(content.new_premium_photo_post_count, format0f), percentCell(huanbi(content.new_premium_photo_post_count, contentPrevDay.new_premium_photo_post_count), format1fp), percentCell(huanbi(content.new_premium_photo_post_count, contentPrevWeek.new_premium_photo_post_count), format1fp))
        :+ Row( Cell(content.gift_creators, format0f), percentCell(huanbi(content.gift_creators, contentPrevDay.gift_creators), format1fp), percentCell(huanbi(content.gift_creators, contentPrevWeek.gift_creators), format1fp))
        :+ Row( Cell(content.gift_text_creators, format0f), percentCell(huanbi(content.gift_text_creators, contentPrevDay.gift_text_creators), format1fp), percentCell(huanbi(content.gift_text_creators, contentPrevWeek.gift_text_creators), format1fp))
        :+ Row( Cell(content.gift_photo_creators, format0f), percentCell(huanbi(content.gift_photo_creators, contentPrevDay.gift_photo_creators), format1fp), percentCell(huanbi(content.gift_photo_creators, contentPrevWeek.gift_photo_creators), format1fp))
        :+ Row( Cell(content.income_creators, format0f), percentCell(huanbi(content.income_creators, contentPrevDay.income_creators), format1fp), percentCell(huanbi(content.income_creators, contentPrevWeek.income_creators), format1fp))
        :+ Row( Cell(content.income_below_1k_creators, format0f), percentCell(huanbi(content.income_below_1k_creators, contentPrevDay.income_below_1k_creators), format1fp), percentCell(huanbi(content.income_below_1k_creators, contentPrevWeek.income_below_1k_creators), format1fp))
        :+ Row( Cell(content.income_1k_3k_creators, format0f), percentCell(huanbi(content.income_1k_3k_creators, contentPrevDay.income_1k_3k_creators), format1fp), percentCell(huanbi(content.income_1k_3k_creators, contentPrevWeek.income_1k_3k_creators), format1fp))
        :+ Row( Cell(content.income_3k_5k_creators, format0f), percentCell(huanbi(content.income_3k_5k_creators, contentPrevDay.income_3k_5k_creators), format1fp), percentCell(huanbi(content.income_3k_5k_creators, contentPrevWeek.income_3k_5k_creators), format1fp))
        :+ Row( Cell(content.income_5k_creators, format0f), percentCell(huanbi(content.income_5k_creators, contentPrevDay.income_5k_creators), format1fp), percentCell(huanbi(content.income_5k_creators, contentPrevWeek.income_5k_creators), format1fp))
        :+ Row( Cell(content.premium_gift_money, format2f), percentCell(huanbi(content.premium_gift_money, contentPrevDay.premium_gift_money), format1fp), percentCell(huanbi(content.premium_gift_money, contentPrevWeek.premium_gift_money), format1fp))
        :+ Row( Cell(content.premium_gift_text_money, format2f), percentCell(huanbi(content.premium_gift_text_money, contentPrevDay.premium_gift_text_money), format1fp), percentCell(huanbi(content.premium_gift_text_money, contentPrevWeek.premium_gift_text_money), format1fp))
        :+ Row( Cell(content.premium_gift_photo_money, format2f), percentCell(huanbi(content.premium_gift_photo_money, contentPrevDay.premium_gift_photo_money), format1fp), percentCell(huanbi(content.premium_gift_photo_money, contentPrevWeek.premium_gift_photo_money), format1fp))
        :+ Row( Cell(content.premium_gift_ecpm, format2f), percentCell(huanbi(content.premium_gift_ecpm, contentPrevDay.premium_gift_ecpm), format1fp), percentCell(huanbi(content.premium_gift_ecpm, contentPrevWeek.premium_gift_ecpm), format1fp))
        :+ Row( Cell(content.premium_gift_text_ecpm, format2f), percentCell(huanbi(content.premium_gift_text_ecpm, contentPrevDay.premium_gift_text_ecpm), format1fp), percentCell(huanbi(content.premium_gift_text_ecpm, contentPrevWeek.premium_gift_text_ecpm), format1fp))
        :+ Row( Cell(content.premium_gift_photo_ecpm, format2f), percentCell(huanbi(content.premium_gift_photo_ecpm, contentPrevDay.premium_gift_photo_ecpm), format1fp), percentCell(huanbi(content.premium_gift_photo_ecpm, contentPrevWeek.premium_gift_photo_ecpm), format1fp))

        :+ Row( Cell(user.pay_uv, format0f), percentCell(huanbi(user.pay_uv, userPrevDay.pay_uv), format1fp), percentCell(huanbi(user.pay_uv, userPrevWeek.pay_uv), format1fp))
        :+ Row( Cell(user.first_pay_uv, format0f), percentCell(huanbi(user.first_pay_uv, userPrevDay.first_pay_uv), format1fp), percentCell(huanbi(user.first_pay_uv, userPrevWeek.first_pay_uv), format1fp))
        :+ Row( Cell(user.high_level_pay_uv, format0f), percentCell(huanbi(user.high_level_pay_uv, userPrevDay.high_level_pay_uv), format1fp), percentCell(huanbi(user.high_level_pay_uv, userPrevWeek.high_level_pay_uv), format1fp))
        :+ Row( Cell(user.pay_arpu, format2f), percentCell(huanbi(user.pay_arpu, userPrevDay.pay_arpu), format1fp), percentCell(huanbi(user.pay_arpu, userPrevWeek.pay_arpu), format1fp))
        :+ Row( Cell(user.first_pay_arpu, format2f), percentCell(huanbi(user.first_pay_arpu, userPrevDay.first_pay_arpu), format1fp), percentCell(huanbi(user.first_pay_arpu, userPrevWeek.first_pay_arpu), format1fp))
        :+ Row( Cell(user.high_level_pay_arpu, format2f), percentCell(huanbi(user.high_level_pay_arpu, userPrevDay.high_level_pay_arpu), format1fp), percentCell(huanbi(user.high_level_pay_arpu, userPrevWeek.high_level_pay_arpu), format1fp))
        :+ Row( Cell(user.silent_potential_uv, format0f), percentCell(huanbi(user.silent_potential_uv, userPrevDay.silent_potential_uv), format1fp), percentCell(huanbi(user.silent_potential_uv, userPrevWeek.silent_potential_uv), format1fp))
        :+ Row( Cell(user.reservoir_browse_uv, format0f), percentCell(huanbi(user.reservoir_browse_uv, userPrevDay.reservoir_browse_uv), format1fp), percentCell(huanbi(user.reservoir_browse_uv, userPrevWeek.reservoir_browse_uv), format1fp))
        :+ Row( Cell(user.reservoir_hd_uv, format0f), percentCell(huanbi(user.reservoir_hd_uv, userPrevDay.reservoir_hd_uv), format1fp), percentCell(huanbi(user.reservoir_hd_uv, userPrevWeek.reservoir_hd_uv), format1fp))

        :+ Row( Cell(market.coupon_money, format0f), percentCell(huanbi(market.coupon_money, marketPrevDay.coupon_money), format1fp), percentCell(huanbi(market.coupon_money, marketPrevWeek.coupon_money), format1fp))
        :+ Row( Cell(market.coupon_uv, format0f), percentCell(huanbi(market.coupon_uv, marketPrevDay.coupon_uv), format1fp), percentCell(huanbi(market.coupon_uv, marketPrevWeek.coupon_uv), format1fp))
        :+ Row( Cell(market.exchange_num, format0f), percentCell(huanbi(market.exchange_num, marketPrevDay.exchange_num), format1fp), percentCell(huanbi(market.exchange_num, marketPrevWeek.exchange_num), format1fp))
        :+ Row( Cell(market.lottery_exchange_num, format0f), percentCell(huanbi(market.lottery_exchange_num, marketPrevDay.lottery_exchange_num), format1fp), percentCell(huanbi(market.lottery_exchange_num, marketPrevWeek.lottery_exchange_num), format1fp))
        :+ Row( Cell(market.post_exchange_num, format0f), percentCell(huanbi(market.post_exchange_num, marketPrevDay.post_exchange_num), format1fp), percentCell(huanbi(market.post_exchange_num, marketPrevWeek.post_exchange_num), format1fp))
        :+ Row( Cell(market.expire_balance, format0f), percentCell(huanbi(market.expire_balance, marketPrevDay.expire_balance), format1fp), percentCell(huanbi(market.expire_balance, marketPrevWeek.expire_balance), format1fp))
        :+ Row( Cell(market.remain_balance, format0f), percentCell(huanbi(market.remain_balance, marketPrevDay.remain_balance), format1fp), percentCell(huanbi(market.remain_balance, marketPrevWeek.remain_balance), format1fp))
        :+ Row( Cell(market.aggregation_uv, format0f), percentCell(huanbi(market.aggregation_uv, marketPrevDay.aggregation_uv), format1fp), percentCell(huanbi(market.aggregation_uv, marketPrevWeek.aggregation_uv), format1fp))
        :+ Row( Cell(market.aggregation_collection_uv, format0f), percentCell(huanbi(market.aggregation_collection_uv, marketPrevDay.aggregation_collection_uv), format1fp), percentCell(huanbi(market.aggregation_collection_uv, marketPrevWeek.aggregation_collection_uv), format1fp))
        :+ Row( Cell(market.aggregation_grain_uv, format0f), percentCell(huanbi(market.aggregation_grain_uv, marketPrevDay.aggregation_grain_uv), format1fp), percentCell(huanbi(market.aggregation_grain_uv, marketPrevWeek.aggregation_grain_uv), format1fp))
        :+ Row( Cell(market.aggregation_money, format2f), percentCell(huanbi(market.aggregation_money, marketPrevDay.aggregation_money), format1fp), percentCell(huanbi(market.aggregation_money, marketPrevWeek.aggregation_money), format1fp))
        :+ Row( Cell(market.aggregation_collection_money, format2f), percentCell(huanbi(market.aggregation_collection_money, marketPrevDay.aggregation_collection_money), format1fp), percentCell(huanbi(market.aggregation_collection_money, marketPrevWeek.aggregation_collection_money), format1fp))
        :+ Row( Cell(market.aggregation_grain_money, format2f), percentCell(huanbi(market.aggregation_grain_money, marketPrevDay.aggregation_grain_money), format1fp), percentCell(huanbi(market.aggregation_grain_money, marketPrevWeek.aggregation_grain_money), format1fp))
      ,
      footnote = Footnote(
        Note("单篇礼物付费金额：\tUGC+PGC内容，仅付费礼物解锁和图片商城，不含糖果券解锁"),
        Note("高级粉丝金额：\t不含官方账号会员"),
        Note("有效浏览量：\t一个人浏览相同文章去重统计"),
        Note("优质付费内容:\t统计日往前90日内数据符合优质付费标准的内容"),
        Note("优质付费内容数日新增：\t按照统计日第一次进入优质付费标准的日期计算"),
        Note("月有效付费创作者：	统计日往前30日窗口设置付费回礼创作者"),
        Note("有收益创作者数量：	统计日往前30日窗口礼物及粉丝会员合并创作者"),
        Note("高净值付费用户：	含统计日前30日礼物及粉丝会员合并付费25元以上为高净值用户"),
        Note("沉默潜在用户：	乐乎币账户存在余额近且30日有过活跃但是未进行内容付费"),
        Note("聚合支付：	仅统计乐乎币消耗金额，不含糖果券解锁")
      ).some
    ).width(880).withStyle("margin-bottom" -> "20px")

    val renderHtml1 = TableHtmlRenderer(table1, styler = None ).html
    val mailBody = renderHtml1

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com")  // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "lofter_content_paid@list.nie.netease.com" :: Nil,
      bcc = "gzq.wmtl@list.nie.netease.com" :: "wm_data_analysis@list.nie.netease.com" :: "data.wm@list.nie.netease.com" :: Nil,
      subject = s"$day LOFTER内容付费二级日报",
      message = "有问题请联系 hzxiaonaitong@corp.netease.com",
      richMessage = Some(mailBody)
    )

    spark.close()
  }

}
