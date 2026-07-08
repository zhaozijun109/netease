package com.netease.wm.util

import java.nio.file.{Files, Paths}
import javax.mail.SendFailedException
import javax.mail.internet.{InternetAddress, MimeUtility}

import org.apache.commons.mail.Email

import scala.collection.JavaConverters._

/**
  * An email dsl stolen from https://gist.github.com/mariussoutier/3436111
  *
  * usage:
  *
  *   import com.netease.wm.util.mail._
  *
  *   send a new Mail (
  *     from = ("symbiansigned@corp.netease.com", "symbiansigned"),
  *     to = "hzluodawei@corp.netease.com" :: "hzmaoyinjie@corp.netease.com" :: "hzxiaonaitong@corp.netease.com" :: Nil,
  *     cc = "cuiqifan@corp.netease.com",
  *     bcc = "data.wm@list.nie.netease.com",
  *     subject = s"蜗牛H5页面访问量$date",
  *     message = "详见附件",
  *     attachment = new java.io.File(resultFile)
  *   )
  */
package object mail {
  val MAIL_OFF = "MAIL_OFF"
  val UTF_BOM = Array[Byte](239.toByte, 187.toByte, 191.toByte)

  implicit def stringToSeq(single: String): Seq[String] = Seq(single)
  implicit def fileToMap(single: java.io.File): Map[java.io.File, String] = Map(single -> single.getName)
  implicit def liftToOption[T](t: T): Option[T] = Some(t)

  sealed abstract class MailType
  case object Plain extends MailType
  case object Rich extends MailType

  case class Mail(
                   from: (String, String), // (email -> name)
                   to: Seq[String],
                   cc: Seq[String] = Seq.empty,
                   bcc: Seq[String] = Seq.empty,
                   subject: String,
                   message: String = "",
                   richMessage: Option[String] = None,
                   attachment: Map[java.io.File, String] = Map.empty,
                   attachmentWithBom: Boolean = false
                 )

  object send {

    private def generateMail(mail: Mail): Email = {
      import org.apache.commons.mail._

      val format =
        if (mail.richMessage.isDefined || mail.attachment.nonEmpty) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich =>
          val result = new HtmlEmail()

          if(mail.message.length > 0) { result.setMsg(mail.message)}
          if(mail.richMessage.isDefined) result.setHtmlMsg(mail.richMessage.get)

          mail.attachment.foreach{
            case (attach, alias) =>
              val attachment = new EmailAttachment()
              if(mail.attachmentWithBom) {
                val paddedBytes = UTF_BOM ++ Files.readAllBytes(attach.toPath)
                val tempFile = Files.createTempFile("mail_attachment", "tmp")
                Files.write(tempFile, paddedBytes)

                attachment.setPath(tempFile.toFile.getAbsolutePath)
              } else {
                attachment.setPath(attach.getAbsolutePath)
              }

              attachment.setDisposition(EmailAttachment.ATTACHMENT)
              attachment.setName(MimeUtility.encodeText(alias))
              
              result.attach(attachment)
          }
          result
      }


      commonsMail.setCharset("UTF-8")
      commonsMail.setHostName("corp.netease.com")
      commonsMail.setSSLOnConnect(true)
      commonsMail.setAuthenticator(new DefaultAuthenticator("symbiansigned@corp.netease.com", "rpmms1492"))
      commonsMail.setSSLCheckServerIdentity(false)

      commonsMail
    }

    def a(mail: Mail) {
      import org.apache.commons.mail._
      val commonsMail = generateMail(mail)
      // Can't add these via fluent API because it produces exceptions
      def resolveAdr(adr: String): String = if(adr.contains("@")) adr else s"$adr@corp.netease.com"

      mail.to foreach (adr => commonsMail.addTo(resolveAdr(adr)))
      mail.cc foreach (adr => commonsMail.addCc(resolveAdr(adr)))
      mail.bcc foreach (adr => commonsMail.addBcc(resolveAdr(adr)))

      try {
        commonsMail
          .setFrom(mail.from._1, mail.from._2)
          .setSubject(mail.subject)

        if(!sys.env.get(MAIL_OFF).isDefined) {
          commonsMail.send()
        }
      } catch {
        case e: EmailException =>
          // try to send out mail to other valid address
          e.getCause match {
            case rootCause: SendFailedException =>
              val failedAddresses = rootCause.getInvalidAddresses.map{ a => a.asInstanceOf[InternetAddress]}.toSet

              val newMail = generateMail(mail)

              val newTo = commonsMail.getToAddresses.asScala.filterNot(failedAddresses).asJava
              val newCc = commonsMail.getCcAddresses.asScala.filterNot(failedAddresses).asJava
              val newBcc = commonsMail.getBccAddresses.asScala.filterNot(failedAddresses).asJava

              if(newTo.size() > 0) newMail.setTo(newTo)
              if(newCc.size() > 0 ) newMail.setCc(newCc)
              if(newBcc.size() > 0) newMail.setBcc(newBcc)

              // try again
              if(newTo.nonEmpty) {
                newMail.
                  setFrom(mail.from._1, mail.from._2).
                  setSubject(mail.subject).
                  send()
              }
          }

          throw e
      }
    }
  }
}