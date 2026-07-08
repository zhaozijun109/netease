package com.netease.lofter.data.jobs.backend

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}
import java.time.LocalDate
import java.util

object PushReturnUserReward {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val date = LocalDate.now().toString

    val notifySql =
      s"""
         |select distinct(cast(userId as String))
         |from lofter_dm.ads_return_user_push_exp_di
         |where dt <= '$date' and exp_group = 'test2-a'
         | """.stripMargin

    val data = spark.sql(notifySql).collect()

    val messages = data.map { row =>
      val userId = row.getAs[String](0)

      Seq(
        s"${userId}"
      ).mkString("\n")
    }
    val javaList = new util.ArrayList[String]()
    messages.foreach(javaList.add)

    // 写入文件
    val day = date.replace("-","")

    val tempFile = Files.createTempFile(s"userIds_${day}", ".txt")


    Files.write(
      tempFile,
      javaList,
      StandardCharsets.UTF_8,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )

    val attachmentFile = tempFile.toFile

    import com.netease.wm.util.mail._

    System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
    System.setProperty("mail.smtp.ssl.enable", "true")
    System.setProperty("mail.smtp.ssl.trust", "corp.netease.com") // 信任特定主机
    System.setProperty("mail.smtp.ssl.checkserveridentity", "false") // 禁用主机名验证

    send a Mail(
      from = ("symbiansigned@corp.netease.com", "symbiansigned"),
      to = "wb.fangnaijun01@mesg.corp.netease.com" :: "chenbin12@corp.netease.com" :: Nil,
      bcc = "hzxiaonaitong@corp.netease.com" :: "wb.mazhihao02@mesg.corp.netease.com" :: Nil,
      subject = s"$date 每日测试二的用户",
      message = "有问题请联系 wb.mazhihao02@mesg.corp.netease.com",
      attachment = Map(attachmentFile -> s"userIds_${day}.txt")

    )
  }
}
