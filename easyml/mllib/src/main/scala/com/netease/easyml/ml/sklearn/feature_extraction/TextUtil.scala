package com.netease.easyml.ml.sklearn.feature_extraction

import java.util.regex.Pattern

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

/**
 * Created by linjiuning on 2020/8/4.
 */
object TextUtil {
  lazy val whiteSpaces: Pattern = Pattern.compile("\\s\\s+")

  def wordNgram(tokens: Seq[String], minN: Int, maxN: Int, stopWords: Option[Set[String]] = None): Seq[String] = {
    val tokens_ = if (stopWords.isDefined) {
      val stopWords_ = stopWords.get
      tokens.filterNot(stopWords_.contains)
    } else {
      tokens
    }

    val ngrams = ArrayBuffer[String]()
    var minN_ = minN
    if (minN == 1) {
      ngrams.appendAll(tokens_)
      minN_ += 1
    }

    val textLen = tokens_.length
    for (n <- minN_ until Math.min(maxN + 1, textLen + 1)) {
      for (i <- 0 until textLen - n + 1) {
        ngrams.append(tokens_.slice(i, i + n).mkString(" "))
      }
    }
    ngrams
  }

  def charNgram(text: String, minN: Int, maxN: Int): Seq[String] = {
    val text_ = whiteSpaces.matcher(text).replaceAll(" ")
    val ngrams = ArrayBuffer[String]()
    var minN_ = minN
    if (minN == 1) {
      text_.toCharArray.foreach(ch => ngrams.append(ch.toString))
      minN_ += 1
    }

    val textLen = text_.length
    for (n <- minN_ until Math.min(maxN + 1, textLen + 1)) {
      for (i <- 0 until textLen - n + 1) {
        ngrams.append(text_.substring(i, i + n))
      }
    }
    ngrams
  }

  def charWbNgram(text: String, minN: Int, maxN: Int): Seq[String] = {
    val text_ = whiteSpaces.matcher(text).replaceAll(" ")
    val ngrams = ArrayBuffer[String]()

    for (w <- text_.split(" ")) {
      val w_ = " " + w + " "
      val wLen = w_.length
      val loop = new Breaks
      loop.breakable {
        for (n <- minN until maxN + 1) {
          var offset = 0
          ngrams.append(w_.substring(offset, offset + n))
          while (offset + n < wLen) {
            offset += 1
            ngrams.append(w_.substring(offset, offset + n))
          }
          // count a short word (w_len < n) only once
          if (offset == 0) {
            loop.break
          }
        }
      }
    }
    ngrams
  }

  def analyze(text: String, analyzer: String, minN: Int = 1, maxN: Int = 1, lowercase: Boolean = true,
              stopWords: Option[Set[String]] = None, tokenizer: Option[String => Seq[String]] = None): Seq[String] = {
    val text_ = if (lowercase) text.toLowerCase else text
    analyzer match {
      case "char" =>
        charNgram(text_, minN, maxN)
      case "char_wb" =>
        charWbNgram(text_, minN, maxN)
      case "word" =>
        val tokens = if (tokenizer.isDefined) {
          tokenizer.get(text_)
        } else {
          text_.split(" ").toSeq
        }
        wordNgram(tokens, minN, maxN, stopWords)
    }
  }

  def buildTokenizer(tokenPattern: String): (String => Seq[String]) = {
    val pattern = Pattern.compile(tokenPattern)
    (text: String) => {
      val tokens = new ArrayBuffer[String]
      val m = pattern.matcher(text)
      while (m.find) {
        tokens.append(m.group)
      }
      tokens
    }
  }
}
