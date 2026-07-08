package com.netease.wm.hubble.common

import com.google.common.base.Objects
import com.google.common.hash.Hashing

import java.util

/**
 * AppendOnlyStringMap:
 *   a simplified map implementation with little memory cost and linear collision resolving
 */
class AppendOnlyStringMap(capacity: Int = 32)
  extends scala.collection.mutable.Map[String, String] with Serializable {

  private val _capacity: Int = nextPowerOf2(capacity)
  private val _mask: Int = _capacity - 1
  private var _size: Int = 0
  private val _bitset: util.BitSet = new util.BitSet(_capacity)
  private val _data = new Array[String](2 * _capacity) // [k1 v1 k2 v2 .... kn kn+1]

  private def getPos(key: String): Int = {
    if(key == null) return -1
    val start = rehash(key.hashCode)

    var i: Int = 0
    while(i < _capacity) {
      val pos =  (start + i) & _mask
      if(!_bitset.get(pos)) {
        return -1
      } else if(Objects.equal(key, _data(2 * pos))) {
        return pos
      }
      // probe next
      i += 1
    }
    -1
  }

  private def assignPos(key: String): Int = {
    if(key == null) return -1
    val start = rehash(key.hashCode)

    var i: Int = 0
    while(i < _capacity) {
      val pos =  (start + i) & _mask
      if(!_bitset.get(pos)) {
        _size += 1
        _bitset.set(pos)
        _data(2 * pos) = key
        return pos
      } else if(Objects.equal(key, _data(2 * pos))) {
        return pos
      } else {
        i += 1
      }
    }
    -1
  }

  override def clear(): Unit = {
    _size = 0
    _bitset.clear()
    // don't clear _data
  }

  override def +=(kv: (String, String)): AppendOnlyStringMap.this.type = {
    if(_size >= _capacity) throw new RuntimeException("size exceeds capacity, can't append new element")
    val pos = assignPos(kv._1) // should always assign a valid slot here
    _bitset.set(pos)
    _data(2 * pos) = kv._1
    _data(2 * pos + 1) = kv._2
    this
  }

  override def -=(key: String): AppendOnlyStringMap.this.type = {
    throw new RuntimeException("AppendOnlyStringMap don't support remove operation")
  }

  override def get(key: String): Option[String] = {
    val pos = getPos(key)
    val value_pos = 2 * pos + 1
    if(pos >= 0) Some(_data(value_pos)) else None
  }

  override def iterator: Iterator[(String, String)] = {
    new Iterator[(String, String)] {
      private var _pos = -1
      override def hasNext: Boolean = {
        _pos = _bitset.nextSetBit(_pos + 1)
        _pos >= 0
      }

      override def next(): (String, String) = (_data(2 * _pos), _data(2 * _pos + 1))
    }
  }

  class ImmutableView extends collection.immutable.Map[String, String] {
    override def get(key: String): Option[String] = AppendOnlyStringMap.this.get(key)

    override def iterator: Iterator[(String, String)] = AppendOnlyStringMap.this.iterator

    override def +[V1 >: String](kv: (String, V1)): Map[String, V1] = (iterator ++ Iterator(kv)).toMap
    override def -(key: String): Map[String, String] = iterator.filterNot(_._1 == key).toMap
  }

  def asImmutable: collection.immutable.Map[String, String] = new ImmutableView

  private def nextPowerOf2(n: Int): Int = {
    if (n == 0) {
      1
    } else {
      val highBit = Integer.highestOneBit(n)
      if (highBit == n) n else highBit << 1
    }
  }

  private def rehash(h: Int): Int = Hashing.murmur3_32().hashInt(h).asInt()
}

object AppendOnlyStringMap {
  def main(args: Array[String]): Unit = {
    val map = new AppendOnlyStringMap(5)

    map += ("a" -> "1")
    map += ("b" -> "2")
    map += ("c" -> "3")
    map += ("d" -> "4")
    map += ("e" -> "5")
    map += ("e" -> "6")
    map += ("c" -> "7")

    println("map[\"a\"] = " + map("a"))
    println("map[\"b\"] = " + map("b"))
    println("map[\"c\"] = " + map("c"))
    println("map[\"d\"] = " + map("d"))
    println("map[\"e\"] = " + map("e"))

    println(map.toString())
  }
}
