package com.netease.bdms.ndi.client.instance

import com.netease.bdms.ndi.client.property.{Properties, Property}
import com.netease.bdms.ndi.client.util.LogTrait
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Success, Try}

class HttpInstanceProtocol extends InstanceProtocol with LogTrait {

  private def getTaskInformation: JValue = {
    val id = Properties.properties.getProperty(Property.TASK).get
    val isDevelop = Properties.properties.getProperty(Property.IS_DEVELOP).get
    val serviceAddress = Properties.properties.getProperty(Property.SERVICE).get
    val appId = Properties.properties.getProperty(Property.APP_ID).get
    val appSecret = Properties.properties.getProperty(Property.APP_SECRET).get
    val uri = if (serviceAddress.endsWith("/")) {
      serviceAddress + "api/v1/task/client/task"
    } else {
      serviceAddress + "/api/v1/task/client/task"
    }
    implicit val formats: DefaultFormats.type = DefaultFormats
    val bodyMap = ("taskId" -> id) ~ ("develop" -> isDevelop)
    val body = compact(render(bodyMap))

    val ret = parse(Request
      .Post(uri)
      .addHeader("appId", appId)
      .addHeader("appSecret", appSecret)
      .connectTimeout(10 * 1000)
      .socketTimeout(10 * 1000)
      .bodyString(body, ContentType.APPLICATION_JSON)
      .execute()
      .returnContent()
      .asString())
    val retCode = (ret \ "code").extract[Int]
    if (retCode != 200) {
      val retMessage = (ret \ "message").extract[String]
      throw new Exception(s"The ret code is $retCode and the response message is $retMessage")
    }
    ret \ "result"
  }

  override def fetchTask(): Unit = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val result: Try[JValue] = Try {
      getTaskInformation
    }

    result match {
      case Success(json) =>
        val readerType = (json \ "readerType").extract[String]
        val readerData = json \ "reader"
        ReaderParser.parse(readerType, readerData)
        val writerType = (json \ "writerType").extract[String]
        val writerData = json \ "writer"
        WriterParser.parse(writerType, writerData)
        val handlersData = (json \ "handlers").extract[List[JValue]]
        handlersData.foreach { handlerData =>
          (handlerData \ "type").extractOpt[String].foreach {
            case "columnHandler" =>
              ColumnParser.parse(handlersData, readerType, writerType)
            case _ =>
          }
        }
      case Failure(exception) =>
        LOG.error("Fetch task failed.", exception)
        throw exception
    }
  }
}
