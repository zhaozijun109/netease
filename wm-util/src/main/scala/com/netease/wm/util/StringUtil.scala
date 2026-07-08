package com.netease.wm.util

object StringUtil {

  implicit class UtilStringWrapper(val string: String) {

    def defaultIfBlank(default: String): String = {
      if (isBlank) default else string
    }

    def defaultString(default: String = ""): String = {
      Option(string) match {
        case Some(_) => string
        case _ => default
      }
    }

    def isEmptySafe: Boolean = {
      Option(string) match {
        case Some(x) if x.nonEmpty => false
        case _ => true
      }
    }

    def nonEmptySafe: Boolean = !isEmptySafe

    def isBlank: Boolean = {
      Option(string) match {
        case Some(x) if x.trim.nonEmpty => false
        case _ => true
      }
    }

    def isNotBlank: Boolean = !isBlank

    def xmlSafe: String = string.map {
      case '<' => "&lt;"
      case '>' => "&gt;"
      case '&' => "&amp;"
      case '"' => "&quot;"
      case '\'' => "&apos;"
      case c => c + ""
    }.mkString("")
  }

}