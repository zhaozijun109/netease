package sample

import java.io.{File, PrintWriter}

import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.NumberFormatter._
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.renderer._
import com.netease.wm.util.view.table.styler._

object html {

  def main(args: Array[String]): Unit = {

    save(new File("d:/sample_table.html"), renderDefaultHtml() + renderHtml() + renderHtmlWithStyler())
    sys.exit()
  }

  def save(file: File, text: String): Unit = {
    val writer = new PrintWriter(file, "UTF-8")
    writer.print(text)
    writer.flush()
    writer.close()
  }

  def renderDefaultHtml(): String = {
    val table = Table(
      title = Title("第1部分：KPI完成情况").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2), Cell("2014", 4), Cell("5", 4), TextCell("2017-01-01"), Cell("环比昨天", 1, 2)),
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
    ).width(1280).withStyle("margin-bottom" -> "20px")

    TableHtmlRenderer(table, styler = None).html
  }

  def renderHtml(): String = {
    val table = Table(
      title = Title("第1部分：KPI完成情况").withStyle("background" -> "black").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2), Cell("2014", 4), Cell("5", 4).withStyle("font-style" -> "italic", "color" -> "cyan"), TextCell("2017-01-01").withStyle("color" -> "red"), Cell("环比昨天", 1, 2)),
        Row(Seq("目标", "实际完成", "完成率", "后续日均需完成", "目标", "实际完成", "完成率", "后续日均需完成", "实际完成").map(Cell(_)): _*)
      ),
      vHeaderRows = Seq(
        Row(Cell("充值（万）", 2, 2), Cell("x", 2)),
        Row(Cell("充值（万）", 1, 2), TextCell("充值（万）", 1, 2, style = Map("background" -> "blue"))).withStyle("background" -> "yellow"),
        Row(Cell("充值（万）"), Cell("充值（万）"))
      ),
      dataRows = (1 to 3).map(_ => Row((1 to 10).map(Cell(_, format2f)): _*)).zipWithIndex.map {
        case (row, i) =>
          val background = if (i % 2 == 0) "#eee" else "#fff"
          row.withStyle("background" -> background)
      }
    ).width(1280).withStyle("margin-bottom" -> "20px")

    TableHtmlRenderer(table, styler = None).html
  }

  def renderHtmlWithStyler(): String = {
    val table = Table(
      title = Title("第1部分：KPI完成情况").withStyle("background" -> "black").some,
      hHeaderRows = Seq(
        Row(Cell("阅读KPI", 4, 2), Cell("2014", 4), Cell("5", 4).withStyle("font-style" -> "italic", "color" -> "cyan"), TextCell("2017-01-01").withStyle("color" -> "red"), Cell("环比昨天", 1, 2)),
        Row(Seq("目标", "实际完成", "完成率", "后续日均需完成", "目标", "实际完成", "完成率", "后续日均需完成", "实际完成").map(Cell(_)): _*)
      ),
      vHeaderRows = Seq(
        Row(Cell("充值（万）", 2, 2), Cell("x", 2)),
        Row(Cell("充值（万）", 1, 2), TextCell("充值（万）", 1, 2, style = Map("background" -> "blue"))).withStyle("background" -> "yellow"),
        Row(Cell("充值（万）"), Cell("充值（万）"))
      ),
      dataRows = (1 to 3).map(_ => Row((-5 to 4).map(Cell(_, format2f)): _*)),
      footnote = Footnote(
        Note("从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人从前叫人家小甜甜，现在新人胜旧人，叫人家牛夫人"),
        Note("心比天高，命比纸薄").withStyle("color" -> "red"),
        Note("神之一手")
      ).withStyle("border" -> "1px solid red").some
    ).width(1280)

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

    TableHtmlRenderer(table, styler = tableStyler.some).html
  }
}
