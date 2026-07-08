package sample

import java.io.FileOutputStream
import java.util.Date

import com.netease.wm.util.view.DateFormatter._
import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.NumberFormatter._
import com.netease.wm.util.view.excel.adapter.{TableAdapter, TableExcelRenderer}
import com.netease.wm.util.view.excel.{XColumn, XFreeze}
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.styler.NoopTableStyler

object excel3 {

  def main(args: Array[String]): Unit = {

    val headerAlign = Map("text-align" -> "center", "vertical-align" -> "middle", "font-weight" -> "bold")

    val table1 = TableAdapter(Table(
      title = Title("神之一手").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2), Cell("2014", 4), Cell("5", 4), DateCell(new Date(), DATE), Cell("环比昨天", 1, 2)),
        Row(Seq("目标", "实际完成", "完成率", "后续日均需完成", "目标", "实际完成", "完成率", "后续日均需完成", "实际完成").map(Cell(_)): _*)
      ),
      vHeaderRows = Seq(
        Row(Cell("充值（万）", 2, 2), Cell("x", 2)),
        Row(Cell("充值（万）", 1, 2), TextCell("充值（万）", 1, 2)),
        Row(Cell("充值（万）"), Cell("充值（万）"))
      ),
      dataRows = (1 to 3).map(_ => Row((1 to 10).map(Cell(_, format2f)): _*)),
      footnote = Footnote(
        Note("从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人"),
        Note("心比天高，命比纸薄"),
        Note("神之一手")
      ).some
    ).width(1280), cols = Seq(XColumn(7, 7, 20), XColumn(11, 11, 20), XColumn(12, 12, 15)), freeze = XFreeze(3, 4).some)

    val table2 = TableAdapter(Table(
      title = Title("第1部分：KPI完成情况").withStyle(
        "caption-side" -> "top",
        "text-align" -> "center",
        "font-weight" -> "bold",
        "padding" -> "2px 0 2px 10px",
        "color" -> "#fff",
        "background" -> "#8066a0").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2), Cell("2014", 4), Cell("5", 4).withStyle("background" -> "#00ff00"), DateCell(new Date(), DATE), Cell("环比昨天", 1, 2)),
        Row(Seq("目标", "实际完成", "完成率", "后续日均需完成", "目标", "实际完成", "完成率", "后续日均需完成", "实际完成").map(Cell(_)): _*)
      ).map(_.withStyle(headerAlign)),
      vHeaderRows = Seq(
        Row(Cell("充值（万）", 2, 2), Cell("x", 2)),
        Row(Cell("充值（万）", 1, 2), TextCell("充值（万）", 1, 2)),
        Row(Cell("充值（万）"), Cell("充值（万）"))
      ).map(_.withStyle(headerAlign)),
      dataRows = (1 to 3).map(_ => Row((1 to 10).map(Cell(_, format2f)): _*)),
      footnote = Footnote(
        Note("从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人"),
        Note("心比天高，命比纸薄"),
        Note("神之一手")
      ).some
    ).width(1280).withStyle("margin-bottom" -> "20px"), cols = Seq(XColumn(7, 7, 20), XColumn(11, 11, 20), XColumn(12, 12, 15)), freeze = XFreeze(3, 4).some)

    object tableStyler extends NoopTableStyler {

      override def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] = {
        cell match {
          case n@NumericCell(Some(_), _, _, _, _) if n.doubleValue.get <= 0 =>
            Map("color" -> "red")
          case TextCell(value, _, _, _) if value.startsWith("-") =>
            Map("color" -> "red")
          case _ => Map("text-decoration" -> "line-through", "background" -> "#cec")
        }
      }

      override def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any] = (hHeader, rowIndex) match {
        case (false, _) if rowIndex % 2 == 0 => Map("background" -> "#eee")
        case _ => Map.empty
      }

      override def titleStyle(title: Title): Map[String, Any] = Map("letter-spacing" -> "10px")

      override def noteStyle(note: Note, noteIndex: Int): Map[String, Any] = Map("padding-left" -> "20px")
    }

    val table3 = TableAdapter(Table(
      title = Title("第2部分：XX").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2).withStyle("font-size" -> "14px"), Cell("2014", 4), Cell("5", 4).withStyle("font-style" -> "italic", "color" -> "cyan"), TextCell("2017-01-01").withStyle("color" -> "red"), Cell("环比昨天", 1, 2)),
        Row(Seq("目标", "实际完成", "完成率", "后续日均需完成", "目标", "实际完成", "完成率", "后续日均需完成", "实际完成").map(Cell(_)): _*)
      ).map(_.withStyle(headerAlign)),
      vHeaderRows = Seq(
        Row(Cell("充值（万）", 2, 2), Cell("x", 2)),
        Row(Cell("充值（万）", 1, 2), TextCell("充值（万）", 1, 2, style = Map("background" -> "#aaFFaa url(bgimage.gif) no-repeat fixed top"))).withStyle("background" -> "yellow"),
        Row(Cell("充值（万）"), Cell("充值（万）"))
      ).map(_.withStyle(headerAlign)),
      dataRows = (1 to 3).map(_ => Row((-5 to 4).map(Cell(_, format2f)): _*)),
      footnote = Footnote(
        Note("从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人"),
        Note("心比天高，命比纸薄"),
        Note("神之一手")
      ).some
    ).width(1280).withStyle("margin-bottom" -> "20px"), cols = Seq(XColumn(7, 7, 20), XColumn(11, 11, 20), XColumn(12, 12, 15)), freeze = XFreeze(3, 4).some, tableStyler = tableStyler.some)

    val os = new FileOutputStream("d:/sample_excel3.xlsx")
    TableExcelRenderer.render(Seq(table1, table2, table3), os)
    os.close()
  }
}