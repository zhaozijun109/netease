package sample

import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.excel._

object excel1 {

  def parseDate(str: String): Date = new SimpleDateFormat("yyyyMMdd").parse(str)

  def main(args: Array[String]): Unit = {

    val out = new FileOutputStream("d:/sample_excel1.xlsx")
    val workbook = new XWorkbook(out)

    {
      case class EventData(date: Date, os: String, eventId: String, event: String, pv: Int, uv: Int)

      val headers = Seq("日期", "平台", "事件ID", "事件", "PV", "UV")
      val datum = Seq(
        EventData(parseDate("20180510"), "Android", "x-13", "启动页广告位-点击数", 6262, 4737),
        EventData(parseDate("20180510"), "Android", "x-14", "启动页广告位-曝光数", 104904, 34868),
        EventData(parseDate("20180510"), "iOS", "x-13", "启动页广告位-点击数", 1334, 994),
        EventData(parseDate("20180510"), "iOS", "x-14", "启动页广告位-曝光数", 67855, 27446),
        EventData(parseDate("20180509"), "Android", "x-13", "启动页广告位-点击数", 6230, 4608),
        EventData(parseDate("20180509"), "Android", "x-14", "启动页广告位-曝光数", 104477, 33339),
        EventData(parseDate("20180509"), "iOS", "x-13", "启动页广告位-点击数", 1158, 850),
        EventData(parseDate("20180509"), "iOS", "x-14", "启动页广告位-曝光数", 69529, 27744)
      )

      val cols = Seq(14, 10, 8, 24, 8, 8).zipWithIndex.map { case (width, index) => XColumn(index, index, width) }
      val worksheet = workbook.addWorksheet("MDA打点", cols, XFreeze(1, 1).some)

      worksheet.addRow() { row =>
        headers.foreach { header =>
          row.addCell(header)
        }
      }

      datum.foreach { data =>
        import data._
        worksheet.addRow() { row =>
          row.addCell(value = date, cellStyle = XStyle(formatCode = "yyyy-mm-dd".some))
          row.addCell(value = os)
          row.addCell(value = eventId)
          row.addCell(value = event)
          row.addCell(value = pv)
          row.addCell(value = uv)
        }
      }
    }

    {
      case class Sale(os: String, date: Date, sale: Double, users: Int, arpu: Double)
      val headers = Seq("平台", "日期", "日销售", "日付费用户", "消费ARPU")
      val hAligns = Seq("left", "right", "right", "right", "right")
      val datum = Seq(
        Sale("iPhone", parseDate("20180510"), 19701.18, 6876, 2.865209424),
        Sale("iPad", parseDate("20180510"), 578.52, 73, 7.924931507),
        Sale("Windows", parseDate("20180510"), 0.15, 1, 0.15),
        Sale("WEB", parseDate("20180510"), 5980.13, 1476, 4.051578591),
        Sale("WAP", parseDate("20180510"), 22991.69, 8777, 2.619538567),
        Sale("Android", parseDate("20180510"), 47338.43, 14993, 3.157368772),
        Sale("iPhone", parseDate("20180509"), 19887.61, 6852, 2.902453298),
        Sale("iPad", parseDate("20180509"), 485.71, 79, 6.148227848),
        Sale("Windows", parseDate("20180509"), 0.1, 1, 0.1),
        Sale("WEB", parseDate("20180509"), 9985.83, 1464, 6.820922131),
        Sale("WAP", parseDate("20180509"), 25517.8, 8728, 2.923670944),
        Sale("Android", parseDate("20180509"), 46516.55, 14868, 3.128635324)
      )

      val cols = Seq(12, 12, 10, 12, 10).zipWithIndex.map { case (width, index) => XColumn(index, index, width) }
      val worksheet = workbook.addWorksheet("分消费平台收入", cols, XFreeze(1, 1).some)

      worksheet.addRow() { row =>
        headers.zip(hAligns).foreach { case (header, hAlign) =>
          row.addCell(header, cellStyle = XStyle(hAlign = hAlign.some))
        }
      }

      datum.foreach { data =>
        import data._
        worksheet.addRow() { row =>
          row.addCell(value = os)
          row.addCell(value = date, cellStyle = XStyle(formatCode = "yyyy-mm-dd".some))
          row.addCell(value = sale, cellStyle = XStyle(formatCode = "0.00".some))
          row.addCell(value = users)
          row.addCell(value = arpu, cellStyle = XStyle(formatCode = "0.000".some))
        }
      }
    }

    {
      case class Sale(os: String, date: Date, sale: Double, users: Int, arpu: Double)
      val datum = Seq(
        Sale("iPhone", parseDate("20180510"), 19701.18, 6876, 2.865209424),
        Sale("iPad", parseDate("20180510"), 578.52, 73, 7.924931507),
        Sale("Windows", parseDate("20180510"), 0.15, 1, 0.15),
        Sale("WEB", parseDate("20180510"), 5980.13, 1476, 4.051578591),
        Sale("WAP", parseDate("20180510"), 22991.69, 8777, 2.619538567),
        Sale("Android", parseDate("20180510"), 47338.43, 14993, 3.157368772),
        Sale("iPhone", parseDate("20180509"), 19887.61, 6852, 2.902453298),
        Sale("iPad", parseDate("20180509"), 485.71, 79, 6.148227848),
        Sale("Windows", parseDate("20180509"), 0.1, 1, 0.1),
        Sale("WEB", parseDate("20180509"), 9985.83, 1464, 6.820922131),
        Sale("WAP", parseDate("20180509"), 25517.8, 8728, 2.923670944),
        Sale("Android", parseDate("20180509"), 46516.55, 14868, 3.128635324)
      )

      val cols = Seq(XColumn(0, 1, 14), XColumn(2, 10, 12))
      val worksheet = workbook.addWorksheet("分消费平台收入（转置）", cols, XFreeze(1, 1).some)

      worksheet.addRow() { row =>
        row.addCell("日期", cellStyle = XStyle(hAlign = "right".some))
        row.addCell("度量名称")
        datum.map(_.os).distinct.sorted.foreach { os =>
          row.addCell(os, cellStyle = XStyle(hAlign = "right".some))
        }
      }

      case class Label(name: String, dataFormat: String)
      val labels = Seq(Label("日销售", "0.00"), Label("日付费用户", "0"), Label("消费arpu", "0.000"))

      datum.groupBy(_.date).mapValues {
        _.sortBy(_.os).map { value =>
          Seq(value.sale, value.users, value.arpu) // same order as labels
        }.transpose
      }.toSeq.sortBy(_._1).reverse.foreach {
        case (date, dataRows) =>
          dataRows.zip(labels).foreach { case (dataRow: Seq[Double], label) =>
            worksheet.addRow() { row =>
              if (label == labels.head) {
                row.addCell(date, rowspan = 3, cellStyle = XStyle(formatCode = "yyyy-mm-dd".some, vAlign = "center".some))
              }

              row.addCell(label.name)
              dataRow.foreach { value =>
                row.addCell(value, cellStyle = XStyle(formatCode = label.dataFormat.some))
              }
            }
          }
      }
    }

    workbook.finish()
    out.close()
  }
}