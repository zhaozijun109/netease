package com.netease.wm.util.view.excel.renderer

import scala.collection.mutable

object XColumnLabelUtil {

  private[this] val columnLabelCache: mutable.Map[Int, String] = mutable.Map.empty

  def columnLabel(colIndex: Int): String = {
    def tuple(x: Int): (Int, Int, Int) = {
      val mod = x % 26
      mod match {
        case 0 => (x, x / 26 - 1, 26)
        case _ => (x, x / 26, mod)
      }
    }

    columnLabelCache.get(colIndex) match {
      case Some(label) => label
      case _ =>
        val cellOrdinal = colIndex + 1
        lazy val stream: Stream[(Int, Int, Int)] = tuple(cellOrdinal) #:: stream.map { case (_, y, _) => tuple(y) }
        val label = stream.takeWhile(_._1 > 0).map(_._3).toList.reverse.map(_ + 'A' - 1).map(_.toChar).mkString("")
        columnLabelCache += colIndex -> label
        label
    }

  }

  def coordinate(rowIndex: Int, cellIndex: Int): String = columnLabel(cellIndex) + (rowIndex + 1)
}