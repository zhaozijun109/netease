package com.netease.lofter.etl.ods

import java.net.URI

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Options, Path}
import org.apache.hadoop.io.NullWritable
import com.hadoop.compression.lzo.LzopCodec
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

import scala.collection.immutable.TreeMap
import scala.util.control.NonFatal

private class AppServerEtlJob {}

object AppServerEtlJob {

  val log = LoggerFactory.getLogger(classOf[AppServerEtlJob])

  class RDDMultipleTextOutputFormat extends MultipleTextOutputFormat[Any, Any] {
    override def generateActualKey(key: Any, value: Any): Any =
      NullWritable.get()

    override def generateFileNameForKeyValue(key: Any, value: Any, name: String): String =
      "lofter_ds_" + key.asInstanceOf[String]
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter AppServer Etl Job")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.required("date")
    val output = "/user/da_lofter/tmp/ds_partition"
    val destDir = "/user/da_lofter/hive/"

    import spark.implicits._

    val clickEvents = Set("register", "publishpost", "commend", "share", "recommend", "praise", "message", "follow", "followcancel", "subscribe", "subscribecancel", "participatetopic", "launchtopic", "visitdomain", "labelrecommend", "bund", "unbund", "viewabtest", "praisecancel", "superwoman", "bindphone", "unbindphone", "pushnotice", "searchPChome", "searchPCperson", "searchmob", "registerback", "registerpath", "nickname", "domainset", "authenticate", "appregister", "taxiupublish", "sharepost", "sharelabel", "shareblog", "shieldrecommend", "double11neteaseprofile", "reproduce", "exposinterestman", "flpaysucess", "flpayfailed", "subscribecollection", "unsubscribecollection", "subscribepost")
    val pageEvents = Set("HomePage", "BrowsePage", "TopicPage", "labelPagepc", "DomainDarenPage", "LabelDarenPage", "SpecialPage", "FollowPage", "DynamicPage", "subscribePage", "FindPage", "SeRecPage", "LaRecPage", "DarenRecPage", "labeldetailPage", "personalPage", "singlelogpage", "specialdetailpage", "taxiuhomepage", "taxiudetailpage", "searchmob", "videoPublishSuccess", "videoPublishReguest", "videoPublishFaile").map(_.toLowerCase)
    val ACTION_SLICE_MAP = TreeMap("exposinterestman" -> 4, "behavior" -> 4, "searchmob" -> 3, "personalpage" -> 7, "labeldetailpage" -> 11, "tomcat" -> 47)
    val appEventRex = "\\{+.*\\}$".r

    // delete old partition files
    {
      val datePattern = """\d\d\d\d-\d\d-\d\d"""
      if (!dt.matches(datePattern)) {
        log.error("need valid date argument of format yyyy-MM-dd, but got {}", dt)
        System.exit(-1)
      }

      val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/"))
      fc.delete(new Path(output), true)

      val globPath = s"$destDir/lofter_ds_*/dt=$dt"

      log.warn("delete old files: {}", globPath)

      val oldFiles = fc.util().globStatus(new Path(globPath))
      for (f <- oldFiles) {
        if (!fc.delete(f.getPath, true)) {
          log.warn("can't delete old file: {}", f.getPath)
        }
      }
    }

    val parsed = spark.read.textFile(s"/user/da_lofter/datastream/tomcat/online/$dt")
      .flatMap {
        line: String =>
          implicit val formats = DefaultFormats

          try {
            val json = parse(line)
            val bodyJsonStrOrg = (json \ "body").extract[String]
            var bodyJsonStr = bodyJsonStrOrg

            if (bodyJsonStrOrg.startsWith("[")) {
              bodyJsonStr = appEventRex.findFirstMatchIn(bodyJsonStrOrg) match {
                case Some(str) => str.toString()
                case None => ""
              }
            }

            val bodyJson = parse(bodyJsonStr)
            val action = (bodyJson \ "action").extractOrElse("").toLowerCase
            val typ = (bodyJson \ "type").extractOrElse("").toLowerCase

            val originActionType = (bodyJson \ "actiontype").extractOpt[String]
              .orElse((bodyJson \ "actionType").extractOpt[String])
              .getOrElse("").toLowerCase.trim

            val actionType = (action, typ) match {
              case ("click", event) if clickEvents.contains(event) => event
              case ("page", event) if pageEvents.contains(event) => event
              case _ if originActionType.nonEmpty => "behavior"
              case _ => "tomcat"
            }

            val sliceStart = ACTION_SLICE_MAP.until(actionType).map(_._2).sum
            val sliceNum = ACTION_SLICE_MAP.getOrElse(actionType, 1)

            val keyPostfix = sliceStart + (bodyJsonStr.hashCode % sliceNum)

            Some(s"${actionType}-part-$keyPostfix" -> bodyJsonStr)
          } catch {
            case NonFatal(_) => None
          }
      }

    parsed.rdd.partitionBy(new HashPartitioner(30))
      .saveAsHadoopFile(output, classOf[String], classOf[String],
        classOf[RDDMultipleTextOutputFormat], codec = Some(classOf[LzopCodec]))

    // move files into event partitions
    val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/"))
    val fileIterator = fc.listStatus(new Path(output))
    val files = Iterator.continually(fileIterator.hasNext).takeWhile(identity).map(_ => fileIterator.next())
      .filter(_.isFile)
      .map(_.getPath.toString)
      .filter(_.endsWith(".lzo"))
      .toSeq

    files.foreach { f =>
      val src = new Path(f)
      val filename = src.getName
      val event = filename.split("-").head
      val dest = new Path(s"$destDir/$event/dt=$dt")
      val destFile = new Path(s"$destDir/$event/dt=$dt/$filename")

      if (!fc.util().exists(dest)) {
        fc.mkdir(dest, null, true)
      }

      log.info(s"commit ${src.toString} to ${destFile.toString}")

      fc.rename(src, destFile)
    }

    fc.delete(new Path(output), true)
    spark.close()
  }
}
