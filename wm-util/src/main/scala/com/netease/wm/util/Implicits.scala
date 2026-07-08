package com.netease.wm.util

import scala.collection.immutable.ListMap

object Implicits {

  implicit class AnyWrapper[T](value: T) {

    def some: Option[T] = Option(value)

    def cas(compare: T, set: T): T = {
      value match {
        case `compare` => set
        case _ => value
      }
    }
  }

  implicit class MapWrapper[K, V](map: Map[K, V]) {

    def toListMap: ListMap[K, V] = map match {
      case _: ListMap[_, _] => map.asInstanceOf[ListMap[K, V]]
      case _ => ListMap(map.toSeq: _*)
    }
  }

  implicit class BooleanWrapper(value: Boolean) {
    def option: Option[Unit] = if (value) Some(()) else None

    def option[U](u: U): Option[U] = if (value) Some(u) else None
  }

  implicit class SeqMapper[T](seq: Seq[T]) {
    def lastPossible(indexes: Int*): Option[T] = {
      indexes.sorted.reverse.find(_ < seq.size).map(seq)
    }
  }

  implicit class OrderingSeqMapper[T: Ordering](self: Seq[T]) {
    def maxOption: Option[T] = if (self.isEmpty) None else Some(self.max)
  }

}
