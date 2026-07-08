package com.netease.wm.hubble.common

import org.json4s.native.JsonParser._

import scala.util.matching.Regex

object JsonParseHelper {
  val userPattern: Regex = """\((\d+),(\d+)\)""".r
  val numPattern: Regex = """(\d+)""".r
  val userIdPattern: Regex = """(\d+)""".r
  val iosAnonymousUserPattern: Regex = """\((\w+),0\)""".r
  val oldUserPattern: Regex = """(\w+)""".r

  @inline def getLongValue(p: Parser): Option[Long] = {
    p.nextToken match {
      case IntVal(v) => Some(v.toLong)
      case LongVal(v) => Some(v)
      case NullVal => None // ignore
      case _ => p.fail("expect long value")
    }
  }

  @inline def getStringValue(p: Parser): Option[String] = {
    p.nextToken match {
      case IntVal(v) => Some(v.toString)
      case LongVal(v) => Some(v.toString)
      case DoubleVal(v) => Some(v.toString)
      case BoolVal(v) => Some(v.toString)
      case StringVal(v) => Some(v)
      case NullVal => None
      case _ => p.fail("expect string value")
    }
  }

  @inline def ignoreJsonValue(p: Parser, tokenOption: Option[Token] = None): Token = {
    var token = tokenOption.getOrElse(p.nextToken)
    token match {
      case StringVal(_) =>
      case IntVal(_) =>
      case LongVal(_) =>
      case DoubleVal(_) =>
      case BoolVal(_) =>
      case NullVal =>
      case FieldStart(_) => p.fail("expect json value but get field start")
      case OpenObj =>
        // continue to CloseObj
        do {
          token = p.nextToken
          token match {
            case FieldStart(_) => ignoreJsonValue(p)
            case CloseObj =>
            case _ => p.fail("expect embedded fields")
          }
        } while (token != CloseObj)

      case OpenArr =>
        // continue to CloseArr
        do {
          token = p.nextToken
          token match {
            case CloseArr =>
            case _ => ignoreJsonValue(p, Some(token))
          }
        } while (token != CloseArr)

      case CloseObj => p.fail("expect json value but get object end")
      case CloseArr => p.fail("expect json value but get object end")
      case End => p.fail("expect json value but get end")
      case _ => p.fail("unknown json value")
    }
    token
  }


  @inline def getUserIdTypeName(p: Parser): (Option[Long], Option[Int], Option[String]) = {
    p.nextToken match {
      case IntVal(id) => (Some(id.toLong), None, None)
      case LongVal(id) => (Some(id), None, None)
      case NullVal => (None, None, None)
      case StringVal(s) =>
        s match {
          case userPattern(a, b) => (Some(a.toLong), Some(b.toInt), None)
          case numPattern(u) => (Some(u.toLong), None, None)
          case iosAnonymousUserPattern(deviceId) if deviceId.length == 32 => (None, None, Option(deviceId))
          case oldUserPattern(name) => (None, None, Option(name))
          case _ => (None, None, None)
        }
      case _ => p.fail("expect long/string value")
    }
  }
}
