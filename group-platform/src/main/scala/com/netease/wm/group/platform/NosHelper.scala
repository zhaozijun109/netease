package com.netease.wm.group.platform

import com.netease.cloud.ClientConfiguration
import com.netease.cloud.auth.{BasicCredentials, Credentials}
import com.netease.cloud.services.nos.NosClient
import com.netease.cloud.services.nos.model._

import java.io.{ByteArrayInputStream, InputStream}
import java.util.UUID
import scala.collection.JavaConverters._
import scala.io.Source

object NosHelper {
  val NOS_ACCESS_KEY = "591b09041dbd437fae4c481e9d54f498"
  val NOS_SECRET_KEY = "a2653379b6534bffb9149c3109667474"
  val NOS_BUCKET = "lofter"

  def uploadFile(input: InputStream, rows: Long): String = {
    rows match {
      case 0 => ""
      case v if v < 5000000 => writeNosFile(input)
      case _ => writeMultiPartNosFile(input)
    }
  }

  private def writeNosFile(input: InputStream): String = {
    val credentials: Credentials = new BasicCredentials(NOS_ACCESS_KEY, NOS_SECRET_KEY)
    val conf: ClientConfiguration = new ClientConfiguration()

    val nosClient: NosClient = new NosClient(credentials, conf)

    val fileKey = s"file/${UUID.randomUUID().toString}.txt"
    nosClient.putObject(NOS_BUCKET, fileKey, input, new ObjectMetadata())
    s"https://$NOS_BUCKET.lf127.net/$fileKey"
  }

  private def writeMultiPartNosFile(input: InputStream): String = {
    val bucket = "lofter"

    val credentials: Credentials = new BasicCredentials(NOS_ACCESS_KEY, NOS_SECRET_KEY)
    val conf: ClientConfiguration = new ClientConfiguration()

    val nosClient: NosClient = new NosClient(credentials, conf)

    val fileKey = s"file/${UUID.randomUUID().toString}.txt"

    val initRequest = new InitiateMultipartUploadRequest(NOS_BUCKET, fileKey)
    val objectMetadata = new ObjectMetadata
    // objectMetadata.setContentType("text/plain")
    initRequest.setObjectMetadata(objectMetadata)
    val initResult = nosClient.initiateMultipartUpload(initRequest)
    val uploadId = initResult.getUploadId

    val buffSize = 10 * 1024 * 1024
    val buffer = new Array[Byte](buffSize)

    var readLen = input.read(buffer, 0, buffSize)
    var partIndex = 1

    while (readLen != -1) {
      val partStream = new ByteArrayInputStream(buffer)
      nosClient.uploadPart(new UploadPartRequest().withBucketName(NOS_BUCKET).withUploadId(uploadId).withInputStream(partStream).withKey(fileKey).withPartSize(readLen).withPartNumber(partIndex))
      partIndex += 1
      readLen = input.read(buffer, 0, buffSize)
    }

    val partETags = new java.util.ArrayList[PartETag]

    var nextMarker = 0
    var queryETags = 0
    while (queryETags == 0) {
      val listPartsRequest = new ListPartsRequest(NOS_BUCKET, fileKey, uploadId)
      listPartsRequest.setPartNumberMarker(nextMarker)
      val partList = nosClient.listParts(listPartsRequest)
      for (ps <- partList.getParts.asScala) {
        nextMarker += 1
        partETags.add(new PartETag(ps.getPartNumber, ps.getETag))
      }
      if (!partList.isTruncated) queryETags = 1
    }

    println("fileKey: " + fileKey)

    val completeRequest = new CompleteMultipartUploadRequest(NOS_BUCKET, fileKey, uploadId, partETags)
    val completeResult = nosClient.completeMultipartUpload(completeRequest)
    println(completeResult.getLocation)
    s"https://$bucket.lf127.net/$fileKey"
  }

  def readLines(url: String): Seq[String] = {
    val result = Seq("UTF-8", "GBK", "UTF-16").map { enc =>
      var source: Source = null
      try {
        source = scala.io.Source.fromURL(url, enc)
        source.getLines().toList
      } catch {
        case _: java.nio.charset.MalformedInputException =>
          Seq.empty
      } finally {
        if (source != null) source.close()
      }
    }.filterNot(_.isEmpty).headOption.getOrElse(Seq.empty)
    result
  }
}
