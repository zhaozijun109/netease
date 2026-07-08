package com.netease.wm.udf

import com.netease.wm.udf.common.SignHelper.getSign
import org.apache.hadoop.hive.ql.exec.{Description, UDF}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scalaj.http._

import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.control.NonFatal

@Description(name = "Img2Vec", value = "vectorizing image using netease openai")
class Img2Vec extends UDF {
  val APP_ID = "lofter-rec-image-feature-test"
  val APP_KEY = "OqG7sd0jTY2OW0P5"
  val RETRIES = 10

  implicit val formats = DefaultFormats

  def evaluate(img: String): java.util.List[java.lang.Double] = {
    if(img == null || img.isEmpty) {
      return null
    }

    val result = try {
      retry(RETRIES) {
        img2vec(img).map(_.asInstanceOf[java.lang.Double]).asJava
      }
    } catch {
      case NonFatal(_) =>
        println(s"failed convert img to vec for $img")
        null
    }

    if(result == null || result.isEmpty) null else result
  }

  def retry[T](n: Int)(fn: => T): T = {
    try {
      fn
    } catch {
      case NonFatal(e) =>
        if (n > 1) retry(n - 1)(fn)
        else throw e
    }
  }


  /**
   * extract feature vector of img using ai platform
   *
   * @param imgPath
   * @return
   */
  def img2vec(imgPath: String, id: Long = 0): Seq[Double] = {
    val timestamp = System.currentTimeMillis() / 1000
    val nonce = Random.nextLong()
    val params = Map("appId" -> APP_ID, "timestamp" -> timestamp.toString, "nonce" -> nonce.toString)
    val sign = getSign(params.asJava, APP_KEY)

    val requestBody = s"""[{
                         |"name": "$id",
                         |"type": "1",
                         |"data": "$imgPath"
                         |}]""".stripMargin

    val response = Http("http://openai-api.service.163.org/image-feature/detector")
      .header("sign", sign)
      .header("nonce", nonce.toString())
      .header("version", "v2")
      .header("appId", APP_ID)
      .header("timestamp", timestamp.toString)
      .header("Content-Type", "application/json")
      .postData(requestBody)
      .timeout(connTimeoutMs = 1000,readTimeoutMs = 10000)
      .asString

    response.code match {
      case 200 =>
        val json = parse(response.body)
        val status = (json \ "status").extract[String]
        status match {
          case "000000" =>
            (json \ "detail" \\ "feature").extract[Seq[Double]]
          case _ => throw new RuntimeException(s"163yun openai error code: $status, " + response.toString)
        }
      case _ =>
        throw new RuntimeException(response.toString)
    }
  }
}
