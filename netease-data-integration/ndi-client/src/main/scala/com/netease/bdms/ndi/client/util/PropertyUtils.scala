package com.netease.bdms.ndi.client.util

import java.util.{Properties => JProperties}

import com.netease.bdms.ndi.client.property.{ArgumentMapping, Property}

import scala.collection.JavaConverters._

object PropertyUtils extends LogTrait {
  implicit def jPropertiesToMap(jProperties: JProperties): Map[String, String] = jProperties.asScala.toMap

  implicit def argsToMap(args: Array[String]): Map[String, String] = {
    import com.netease.bdms.ndi.client.property.ArgumentMapping._
    import com.netease.bdms.ndi.client.util.Precondition._

    import scala.collection.mutable

    def findMapping(key: String,
                    available: List[ArgumentMapping]): Option[ArgumentMapping] = {
      val filter = available.filter(_.argumentName == key)
      if (filter.nonEmpty) {
        Option.apply(filter.head)
      } else {
        Option.empty
      }
    }

    var idx = 0
    val map = mutable.Map[String, String]()
    while (idx < args.length) {
      val arg = args(idx)

      var mapping = findMapping(arg, options)
      if (mapping.nonEmpty) {
        check(idx != args.length - 1, Option.apply(s"Missing argument for option '$arg'."))
        idx += 1
        val value = args(idx)
        map += mapping.get.toPair(value)
      } else {
        mapping = findMapping(arg, switches)
        if (mapping.nonEmpty) {
          map += (mapping.get.propertyName -> "true")
        } else {
          LOG.warn(s"Unknown option '$arg'.")
        }
      }
      idx += 1
    }
    map.toMap
  }
}
