package sample

import java.io.FileOutputStream

import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.excel.renderer.XColumnLabelUtil
import com.netease.wm.util.view.excel._

object excel2 {

  def main(args: Array[String]): Unit = {

    val os = new FileOutputStream("d:/sample_excel2.xlsx")
    val workbook = new XWorkbook(os)

    (0 until 5).foreach { sheetIndex =>
      val cols = Seq(10, 20).zipWithIndex.map { case (width, index) =>
        XColumn(index, index, width)
      }
      val worksheet = workbook.addWorksheet(s"世界之大$sheetIndex", cols, XFreeze(1, 1).some)
      (0 until 5000).foreach { rowIndex =>
        if (rowIndex % 1000 == 0) {
          println(s"""$sheetIndex: $rowIndex""")
        }
        worksheet.addRow(30.0.some) { row =>
          (0 until 30).foreach { cellIndex =>
            val edge = XEdge(style = XBorderStyle.MEDIUM_DASH_DOT_DOT, rgb = "ff0000")
            val text = sheetIndex + ": " + XColumnLabelUtil.coordinate(rowIndex, cellIndex)
            row.addCell(text, cellStyle = XStyle().withBorderStyle(edge))
          }
        }
      }
    }

    workbook.finish()
    os.close()
  }
}