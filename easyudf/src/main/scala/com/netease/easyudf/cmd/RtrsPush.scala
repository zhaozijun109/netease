package com.netease.easyudf.cmd

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.netease.cloudmusic.RtrsPushJobMsg
import com.netease.easyml.common.cmds.Script
import com.netease.easyml.common.util.OkHttpUtil
import org.apache.commons.lang3.StringUtils

import scala.util.control.Breaks

case class RtrsPushArgs(key: String, path: String, creator: String = null, appName: String = "rtrs", `type`: String = "model",
                        version: String = null, retry: Int = 4, timeout: Int = 20 * 60)

class RtrsPush extends Script[RtrsPushArgs] {
  val PUSH_SERVER = "http://music-rtrs-push.service.163.org"
  val UPDATE_URL: String = PUSH_SERVER + "/api/v1/app/file/update"
  val STATUS_URL: String = PUSH_SERVER + "/api/v1/job/status"

  val CODE_SUCCESS = 200
  val STATUS_SUCCESS = "success"
  val STATUS_INIT = "init"
  val STATUS_LOADING = "loading"
  val STATUS_DOWNLOADED = "downloaded"

  val SLEEP = 10

  override def run(args: RtrsPushArgs): Unit = {
    val version = if (StringUtils.isBlank(args.version)) (System.currentTimeMillis() / 1000).toString else args.version
    val fields = args.key.split("\\.")
    assert(args.key != null && fields.length > 3)

    val algo = String.join(".", fields(fields.length - 3), fields(fields.length - 2), fields(fields.length - 1))

    val fileInfo = new RtrsPushJobMsg.FileInfo()
    fileInfo.setFilename(args.key)
    fileInfo.setType(args.`type`)

    fileInfo.setHdfs(args.path)
    fileInfo.setVersion(version)

    val pushJobMsg = new RtrsPushJobMsg()
    pushJobMsg.setAppName(args.appName)
    pushJobMsg.setAlgorithm(algo)
    if (StringUtils.isNoneBlank(args.creator)) {
      pushJobMsg.setCreator(args.creator)
    }
    pushJobMsg.setFileInfo(fileInfo)

    val content = JSON.toJSONString(pushJobMsg, new Array[SerializeFilter](0))
    log.info(s"request RTRS PushServer $UPDATE_URL, content={}", content)

    var jobID = 0L

    val loop = new Breaks
    loop.breakable {
      for (i <- 0 until args.retry) {
        try {
          val httpUtil = OkHttpUtil.getInstance()
          val responseTxt = httpUtil.post(UPDATE_URL, content)
          if (StringUtils.isBlank(responseTxt)) {
            log.error("send http failed, with respText:{}", responseTxt)
            loop.break
          }

          val obj = JSON.parseObject(responseTxt)
          val realCode = obj.getInteger("code")
          if (realCode != CODE_SUCCESS) {
            log.error("send http failed, respText:{}", responseTxt)
            loop.break
          }
          val data = obj.getJSONObject("data")
          if (data == null) {
            log.error("send http failed, respText:{}", responseTxt)
            loop.break
          }
          jobID = data.getLong("id")
          loop.break
        } catch {
          case e: Exception =>
            if (i == args.retry - 1) {
              log.error("send http occurs exception", e)
            }
        }
      }
    }

    if (jobID != 0) {
      var status = STATUS_INIT
      var message = ""
      var i = 0

      val content = s"jobID=$jobID"
      log.info(s"request RTRS PushServer $STATUS_URL?$content")
      val startTime = System.currentTimeMillis()
      val timeout = args.timeout * 1000
      val loop = new Breaks
      loop.breakable {
        while (status == STATUS_INIT || status == STATUS_LOADING || status == STATUS_DOWNLOADED) {
          try {
            val httpUtil = OkHttpUtil.getInstance()
            val responseTxt = httpUtil.get(s"$STATUS_URL?$content")
            if (StringUtils.isBlank(responseTxt)) {
              log.error("send http failed, with respText:{}", responseTxt)
              loop.break
            }
            val obj = JSON.parseObject(responseTxt)
            val realCode = obj.getInteger("code")
            if (realCode != CODE_SUCCESS) {
              log.error("send http failed, respText:{}", responseTxt)
              loop.break
            }
            val data = obj.getJSONObject("data")
            if (data == null) {
              log.error("send http failed, respText:{}", responseTxt)
              loop.break
            }
            status = data.getString("status")
            message = data.getString("message")
            if (status == STATUS_INIT || status == STATUS_LOADING || status == STATUS_DOWNLOADED) {
              log.info(s"status=$status, message=$message, waiting ${SLEEP}s")
              Thread.sleep(SLEEP * 1000)
            }
          }
          catch {
            case e: Exception =>
              if (i == args.retry - 1) {
                log.error("send http occurs exception", e)
                loop.break
              }
              i += 1
          }
          if (timeout > 0 && System.currentTimeMillis() - startTime > timeout) {
            log.error("send http occurs timeout")
            loop.break
          }
        }
      }
      if (status != STATUS_SUCCESS) {
        throw new Exception(s"push failed, jobID=$jobID, status=$status, message=$message")
      }
    } else {
      throw new Exception(s"update failed")
    }
  }
}

