package com.netease.wm.util.view.table

import com.netease.wm.util.view.Styling

class Row[+T <: Cell](val cells: T*)(val style: Map[String, Any] = Map.empty) extends Styling {

  type SelfType <: Row[T]

  def withNewStyle(style: Map[String, Any]): SelfType = new Row(cells: _*)(style).asInstanceOf[SelfType]

  def map[U <: Cell](f: T => U): Row[U] = Row(cells.map(f): _*)
}

object Row {
  def apply[T <: Cell](cells: T*): Row[T] = {
    new Row(cells: _*)()
  }
}