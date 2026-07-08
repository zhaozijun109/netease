package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.cloud.ClientConfiguration
import com.netease.cloud.auth.{BasicCredentials, Credentials}
import com.netease.cloud.services.nos.NosClient
import com.netease.cloud.services.nos.model._
import com.netease.wm.group.platform.common.clickhouseConfig.{clickHouseDriverName, clickHousePassword, clickHouseUser}
import com.netease.wm.util.{Args, Sql}
import org.apache.hadoop.fs.permission.FsPermission
import org.apache.hadoop.fs.{CreateFlag, FileContext, Path}
import org.apache.spark.sql.SparkSession
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URI
import java.sql.{Connection, DriverManager}
import java.util
import java.util.UUID
import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

object UserGroupJob {
  val NOS_ACCESS_KEY = "591b09041dbd437fae4c481e9d54f498"
  val NOS_SECRET_KEY = "a2653379b6534bffb9149c3109667474"
  val NOS_BUCKET = "lofter"

  val USER_GROUP_DIR = "/user/da_lofter/warehouse/user_group"

  case class UserGroup(id: Long, name: String, `type`: Int, state: Int, options: String,
                       merged: Option[Int], createTime: Long, mergeLogic: Option[Int], mergeOptions: Option[String])

  case class UserGroupDailyResult(packId: Long, name: String, `type`: Int, createTime: Long, userCount: Long, resultUrl: String)

  case class ContentOption(contentForm: String, postCount: String, postTime: String)

  case class TagPostOption(postTag: String, postTagTime: String, postTagCount: String)

  case class GroupOption(
                          generateType: Int,
                          fileUrl: Option[String],
                          logic: Option[Int],
                          // common option
                          activeTimes: Option[String],
                          // creator options
                          auth: Option[Int],
                          fanCreator: Option[Int],
                          signAuth: Option[Int],
                          domainIds: Option[String],
                          contentForms: Option[String],
                          goodCategories: Option[String],
                          goodIPs: Option[String],
                          safeLevels: Option[String],
                          creatorLevels: Option[String],
                          fansCounts: Option[String],
                          actFansCounts: Option[String],
                          activeType: Option[Int],
                          postRates: Option[String],
                          postRateTimes: Option[String],
                          recomPostCounts: Option[String],
                          amounts: Option[String],
                          amountTimes: Option[String],
                          outSide: Option[Int],
                          // consumer options
                          favTags: Option[String],
                          tags: Option[String],
                          tagPostCounts: Option[String],
                          tagTimes: Option[String],
                          categories: Option[String],
                          categoryCounts: Option[String],
                          categoryTimes: Option[String],
                          IPs: Option[String],
                          IPCounts: Option[String],
                          IPTimes: Option[String],
                          CPs: Option[String],
                          CPCounts: Option[String],
                          CPTimes: Option[String],
                          derivates: Option[String],
                          derivateCounts: Option[String],
                          derivateTimes: Option[String],
                          prefer: Option[Int],
                          published: Option[Int],
                          publishTimes: Option[String],
                          regTimes: Option[String],
                          active: Option[Int],
                          lifeCycle: Option[String],
                          hotCounts: Option[String],
                          hotTimes: Option[String],
                          buy: Option[Int],
                          buyTimes: Option[String],
                          commentCounts: Option[String],
                          commentTimes: Option[String],
                          rewards: Option[String],
                          rewardTimes: Option[String],
                          giftAmounts: Option[String],
                          giftTimes: Option[String],
                          ad: Option[Int],
                          adTimes: Option[String],
                          areas: Option[String],
                          ages: Option[String],
                          favorCount: Option[String],
                          subscribeCount: Option[String],
                          recommendCount: Option[String],
                          // introduced in second iteration
                          contentFormArray: Option[String],
                          postTagArray: Option[String],
                          increaseFansTime: Option[String],
                          increaseFansCount: Option[String],
                          increaseToFansCount: Option[Long],
                          addOn: Option[Int],
                          addOnTime: Option[String],
                          preferType: Option[String],
                          securityLevel: Option[String],
                          marketConsumeAmount: Option[String],
                          marketConsumeTime: Option[String],
                          marketConsumeType: Option[String],
                          marketConsumeCategory: Option[String],
                          recomAlgoTags: Option[String],
                          deviceType: Option[String],
                          adUserGroup: Option[String]
                          )

  trait LeveledTagGroup {
    def tag: String

    def cat: String

    def getLevel: Int = 1

    def isCombo: Boolean = false

    def isSet: Boolean = false

    def getCondition: String

    def getOutSetCondition: String = ""
  }

  case class TagGroupLevel1(tag: String, grp: String) extends LeveledTagGroup {
    override def cat: String = ""

    override def getLevel: Int = 1

    override def getCondition: String = s"tag = '$tag' and grp = '$grp'"
  }

  case class TagGroupLevel1Combo(tag: String, combo: Seq[String]) extends LeveledTagGroup {
    override def cat: String = ""

    override def getLevel: Int = 1

    override def isCombo: Boolean = combo.size > 1

    override def getCondition: String = {
      val groups = combo.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
      s"tag = '$tag' and grp in ($groups) "
    }
  }

  case class TagGroupLevel1Set(tag: String, grp: Seq[String], grpSet: Seq[String]) extends LeveledTagGroup {
    override def cat: String = ""

    override def getLevel: Int = 1

    override def isCombo: Boolean = false

    override def isSet: Boolean = true

    override def getCondition: String = {
      val groups = grp.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
      s"tag = '$tag' and grp in ($groups) "
    }

    override def getOutSetCondition: String = {
      val all = grpSet.map(_.trim).filter(_.nonEmpty)
      val groupSet = grp.map(_.trim).filter(_.nonEmpty).toSet
      val otherGroups = all.filterNot(groupSet).mkString("'", "','", "'")
      s"tag = '$tag' and grp in ($otherGroups) "
    }
  }

  case class TagGroupLevel2(tag: String, cat: String, grp: String) extends LeveledTagGroup {
    override def getLevel: Int = 2

    override def getCondition: String = s"tag = '$tag' and cat = '$cat' and grp = '$grp'"
  }

  case class TagGroupLevel2Combo(tag: String, cat: String, combo: Seq[String]) extends LeveledTagGroup {
    override def getLevel: Int = 2

    override def isCombo: Boolean = combo.size > 1

    override def getCondition: String = {
      val groups = combo.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
      s"tag = '$tag' and cat = '$cat' and grp in ($groups)"
    }
  }

  case class TagGroupLevel3(tag: String, cat: String, subcat: String, grp: String) extends LeveledTagGroup {
    override def getLevel: Int = 3

    override def getCondition: String = s"tag = '$tag' and cat = '$cat' and subcat ='$subcat' and grp = '$grp'"
  }

  case class TagGroupLevel3Combo(tag: String, cat: String, subcat: Seq[String], grp: Seq[String]) extends LeveledTagGroup {
    override def getLevel: Int = 3

    override def isCombo: Boolean = subcat.size * grp.size > 1

    override def getCondition: String = {
      val subcatGroup = subcat.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
      val groups = grp.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
      s"tag = '$tag' and cat = '$cat' and subcat in ($subcatGroup) and grp in ($groups) "
    }
  }

  val contentTypeMapping = Map(
    "TEXTPOST" -> "文字",
    "PHOTOPOST" -> "图片",
    "VIDEOPOST" -> "视频"
  )

  def mapOptionToTagSubQueries(op: GroupOption, dt: String): Seq[String] = {
    val result: Seq[Option[LeveledTagGroup with Product with Serializable]] = Seq(
      op.auth map { auth => TagGroupLevel1("达人", auth.toString) },
      op.fanCreator map { fanAuth => TagGroupLevel1("同人创作者", fanAuth.toString) },
      op.signAuth map { sign => TagGroupLevel1("签约创作者", sign.toString) },
      op.contentForms map { contents => TagGroupLevel1Combo("创作内容类型", contents.split(",").toSeq) },
      op.domainIds map { domains => TagGroupLevel1Combo("创作领域", domains.split(",").toSeq) },
      op.goodCategories map { cs => TagGroupLevel1Combo("擅长类目", cs.split(",").toSeq) },
      op.goodIPs map { ips => TagGroupLevel1Combo("擅长IP", ips.split(",")) },
      op.safeLevels map { levels => TagGroupLevel1Combo("安全等级", levels.split(",")) },
      op.creatorLevels map { levels => TagGroupLevel1Combo("创作者等级", levels.split(",")) },
      op.fansCounts map { fans => TagGroupLevel1Combo("累计粉丝", fans.split(",")) },
      op.actFansCounts map { actFans => TagGroupLevel1Combo("互动粉丝", actFans.split(",")) },
      (op.activeType zip op.activeTimes).headOption map { t => TagGroupLevel2("活跃情况", t._2, t._1.toString) },
      (op.postRates zip op.postRateTimes).headOption map { t => TagGroupLevel2Combo("发文频次", t._2, t._1.split(",")) },
      op.recomPostCounts map { recPost => TagGroupLevel1Combo("入池文章", recPost.split(",")) },
      (op.amounts zip op.amountTimes).headOption map { t => TagGroupLevel2Combo("打赏收礼金额", t._2, t._1.split(",")) },
      op.outSide map { outside => TagGroupLevel1("外部引入", outside.toString) },
      op.favTags map { tags => TagGroupLevel1Combo("订阅标签", tags.split(",")) },
      op.recomAlgoTags map { tags => TagGroupLevel1Combo("recomAlgoTags", tags.split(",")) },
      op.deviceType map { tags => TagGroupLevel1Combo("deviceType", tags.split(",")) },
      (op.tags zip op.tagPostCounts zip op.tagTimes).headOption map { t => TagGroupLevel3Combo("tag_consume", t._2, t._1._2.split(","), t._1._1.split(",")) },
      (op.CPs zip op.CPCounts zip op.CPTimes).headOption map { t => TagGroupLevel3Combo("tag_consume", t._2, t._1._2.split(","), t._1._1.split(",")) },
      (op.derivates zip op.derivateCounts zip op.derivateTimes).headOption map { t => TagGroupLevel3Combo("tag_consume", t._2, t._1._2.split(","), t._1._1.split(",")) },
      (op.IPs zip op.IPCounts zip op.IPTimes).headOption map { t => TagGroupLevel3Combo("ip_consume", t._2, t._1._2.split(","), t._1._1.split(",")) },
      (op.categories zip op.categoryCounts zip op.categoryTimes).headOption map { t => TagGroupLevel3Combo("category_consume", t._2, t._1._2.split(","), t._1._1.split(",")) },
      op.prefer map { pref => TagGroupLevel1("消费偏好", pref.toString) },
      (op.published zip op.publishTimes).headOption map { t => TagGroupLevel2("是否发文", t._2, t._1.toString) },
      op.regTimes map { reg => TagGroupLevel1("注册时间", reg) },
      (op.active zip op.activeTimes).headOption map { t => TagGroupLevel2("消费者活跃情况", t._2, t._1.toString) },
      op.lifeCycle map { life => TagGroupLevel1Combo("生命周期", life.split(",")) },
      (op.hotCounts zip op.hotTimes).headOption map { t => TagGroupLevel2Combo("热度贡献量", t._2, t._1.split(",")) },
      (op.buy zip op.buyTimes).headOption map { t => TagGroupLevel2("购买行为", t._2, t._1.toString) },
      (op.commentCounts zip op.commentTimes).headOption map { t => TagGroupLevel2Combo("评论量", t._2, t._1.split(",")) },
      (op.rewards zip op.rewardTimes).headOption map { t => TagGroupLevel2Combo("打赏金额", t._2, t._1.split(",")) },
      (op.giftAmounts zip op.giftTimes).headOption map { t => TagGroupLevel2Combo("送礼金额", t._2, t._1.split(",")) },
      (op.ad zip op.adTimes).headOption map { t => TagGroupLevel2("消费广告", t._2, t._1.toString) },
      op.areas map { areas => TagGroupLevel1Combo("地域", areas.split(",")) },
      op.adUserGroup map { areas => TagGroupLevel1Combo("ad_user_group", areas.split(",")) },
      op.ages map { ages => TagGroupLevel1Combo("年龄", ages.split(",")) },
      op.favorCount map { favors => TagGroupLevel1Combo("点赞量", favors.split(",")) },
      op.subscribeCount map { subs => TagGroupLevel1Combo("收藏量", subs.split("")) },
      op.recommendCount map { recs => TagGroupLevel1Combo("蓝手量", recs.split(",")) },
      (op.addOn zip op.addOnTime).headOption map { t => TagGroupLevel2("加购行为", t._2, t._1.toString) },
      op.marketConsumeCategory map { t => TagGroupLevel1Combo("市集消费类目", t.split(",")) },
      (op.marketConsumeType zip op.marketConsumeAmount zip op.marketConsumeTime).headOption map { t =>
        val groups = t._1._1.split(",").filter(_.trim.nonEmpty).map {
          case "SLOT" => "card"
          case _ => "market"
        }
        TagGroupLevel3Combo("product_type_pay", t._2, t._1._2.split(","), groups)
      },
      (op.increaseFansTime zip op.increaseFansCount).headOption map { t => TagGroupLevel2Combo("涨粉情况", t._1, t._2.split(",")) },
      (op.increaseFansTime zip op.increaseToFansCount).headOption map { t => TagGroupLevel2("涨粉情况-突破", t._1, t._2.toString) },
      // set logic
      op.preferType map { pref =>
        val contentTypes = pref.split(",").filter(contentTypeMapping.keySet).map(contentTypeMapping).toSeq
        TagGroupLevel1Set("消费偏好", contentTypes, Seq("文字", "图片", "视频"))
      }
    ) ++ op.contentFormArray.toSeq.flatMap { contentJson =>
      implicit val formats: org.json4s.Formats = DefaultFormats
      val contentOptions = read[Seq[ContentOption]](contentJson)
      contentOptions.map { opt =>
        val contentType = if (opt.contentForm == "文章") "文字" else opt.contentForm
        val result: LeveledTagGroup with Product with Serializable = TagGroupLevel3Combo("content_post", opt.postTime, opt.postCount.split(","), Seq(contentType))
        Option(result)
      }
    } ++ op.postTagArray.toSeq.flatMap { tagJson =>
      implicit val formats: org.json4s.Formats = DefaultFormats
      val tagOptions = read[Seq[TagPostOption]](tagJson)
      tagOptions.map { opt =>
        val result: LeveledTagGroup with Product with Serializable = TagGroupLevel3Combo("tag_post", opt.postTagTime, opt.postTagCount.split(","), Seq(opt.postTag))
        Option(result)
      }
    }

    val tableGroup = result.flatten.groupBy { g =>
      (g.getLevel, g.tag, g.cat) match {
        case (3, "ip_consume" | "category_consume" | "tag_consume", "DAY_90" | "DAY_180") => "lofter.user_portrait_level3_wd"
        case (level, _, _) => s"lofter.user_portrait_level$level"
      }
    }.toSeq

    val userMergeOp = if (op.logic.getOrElse(0) == 0) "groupBitmapAndState" else "groupBitmapOrState"

    val lastSaturday = DateTime.parse(dt).plusDays(1).minusWeeks(1).withDayOfWeek(6).toString("yyyy-MM-dd")

    tableGroup flatMap { g =>
      val (tagGroupTable, options) = g

      val (nonComboSetSubs, comboSubs, setSubs) = (options.filterNot(s => s.isCombo || s.isSet), options.filter(_.isCombo), options.filter(_.isSet))
      val queryTagGroupDate = if (tagGroupTable.endsWith("_wd")) lastSaturday else dt
      // non combo condition can be merged
      val nonComboQuery = {
        val conditions = nonComboSetSubs.map(_.getCondition).mkString("((", s") or (", "))")
        s"select $userMergeOp(users) as users from $tagGroupTable where dt='$queryTagGroupDate' and $conditions"
      }
      // combo condition users should be combined with groupBitmapOr firstly
      val comboQueries = comboSubs map { combo =>
        s"select groupBitmapOrState(users) as users from $tagGroupTable where dt='$queryTagGroupDate' and ${combo.getCondition}"
      }

      val setQueries = setSubs map { set =>
        s"""
           |with (
           |  select groupBitmapOrState(users) as users from $tagGroupTable where dt='$queryTagGroupDate' and (${set.getCondition})
           |) as positiveBitmap, (
           |  select groupBitmapOrState(users) as users from $tagGroupTable where dt='$queryTagGroupDate' and (${set.getOutSetCondition})
           |) as negativeBitmap
           |select bitmapAndnot(positiveBitmap, negativeBitmap) as users
           |""".stripMargin
      }

      if (nonComboSetSubs.isEmpty) comboQueries ++ setQueries else (comboQueries ++ setQueries) :+ nonComboQuery
    }
  }

  def getUserGroupDir(fc: FileContext, dt: String): Path = {
    val path = new Path(s"$USER_GROUP_DIR/dt=$dt")
    if (!fc.util.exists(path)) {
      fc.mkdir(path, FsPermission.getDirDefault, true)
    }
    path
  }

  def getUserGroupJobDir(fc: FileContext, dt: String, jobId: Long): Path = {
    val parentDir = getUserGroupDir(fc, dt)
    val jobDir = s"$parentDir/job_id=$jobId"
    val jobPath = new Path(jobDir)
    if (!fc.util().exists(jobPath)) {
      fc.mkdir(jobPath, FsPermission.getDirDefault, true)
    }

    new Path(s"$jobDir/users.txt")
  }

  def mapOptionToTagGroupQuery(op: GroupOption, extraOption: Option[GroupOption], mergeLogic: Option[Int], dt: String): (String, String) = {
    val logic = op.logic.getOrElse(0)
    val mainMergeOp = if (logic == 0) "groupBitmapAndState" else "groupBitmapOrState"
    val mainResultOp = if (logic == 0) "groupBitmapAnd" else "groupBitmapOr"

    val mainSubQueries = mapOptionToTagSubQueries(op, dt)

    if (extraOption.isDefined && mergeLogic.exists(_ >= 0) && mainSubQueries.nonEmpty) {
      val extra = extraOption.get
      val extraSubQueries = mapOptionToTagSubQueries(extra, dt)
      val extraMergeOp = if (extra.logic.getOrElse(0) == 0) "groupBitmapAndState" else "groupBitmapOrState"

      val (mergeMergeOp, mergeResultOp) = mergeLogic.get match {
        case 0 => ("bitmapAnd", "bitmapAndCardinality")
        case 2 => ("bitmapAndnot", "bitmapAndnotCardinality")
        case _ => ("bitmapOr", "bitmapOrCardinality")
      }

      val summarySql =
        s"""
           |with (
           |  select $mainMergeOp(users) as users from ${mainSubQueries.mkString("(", " union all ", ")")} t1
           |) as mainBitmap, (
           |  select $extraMergeOp(users) as users from ${extraSubQueries.mkString("(", " union all ", ")")} t2
           |) as subBitmap
           |select $mergeResultOp(mainBitmap, subBitmap) as result
           |""".stripMargin

      val detailSql =
        s"""
           |with (
           |  select $mainMergeOp(users) as users from ${mainSubQueries.mkString("(", " union all ", ")")} t1
           |) as mainBitmap, (
           |  select $extraMergeOp(users) as users from ${extraSubQueries.mkString("(", " union all ", ")")} t2
           |) as subBitmap
           |select arrayJoin(bitmapToArray($mergeMergeOp(mainBitmap, subBitmap))) as result
           |""".stripMargin

      summarySql -> detailSql
    } else if (mainSubQueries.nonEmpty) {
      val subQueries = mainSubQueries
      val summarySql = s"select $mainResultOp(users) as result from ${subQueries.mkString("(", " union all ", ")")} t"
      val detailSql = s"select arrayJoin(bitmapToArray($mainMergeOp(users))) as result from ${subQueries.mkString("(", " union all ", ")")} t"

      summarySql -> detailSql
    } else {
      "select number as result from numbers(0)" -> "select number as result from numbers(0)"
    }
  }

  def writeHdfsFile(fc: FileContext, outPath: Path, content: Seq[String]): Unit = {
    val out = fc.create(outPath, util.EnumSet.of(CreateFlag.CREATE, CreateFlag.OVERWRITE))

    content.map(_.trim).filter(_.nonEmpty).foreach { line =>
      out.writeBytes(line)
      out.writeBytes("\n")
    }
    out.close()
  }

  def copyStaticUserGroupFile(fc: FileContext, userGroup: UserGroup, dt: String): Unit = {
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")
    val outPath = getUserGroupJobDir(fc, dt, userGroup.id)
    val inPath = getUserGroupJobDir(fc, dayAgo, userGroup.id)

    if (!fc.util().exists(inPath)) {
      implicit val formats: org.json4s.Formats = DefaultFormats
      val option = read[GroupOption](userGroup.options)
      if (option.fileUrl.isDefined) {
        val userList = parseNosUserList(option.fileUrl.get)
        val fc = FileContext.getFileContext(new URI("hdfs://hz-cluster10/user/da_lofter"))
        writeHdfsFile(fc, inPath, userList)
      }
    }

    if (!fc.util().exists(outPath)) {
      fc.util().copy(inPath, outPath, false, true)
    }
  }

  def writeNosFile(input: InputStream): String = {
    val credentials: Credentials = new BasicCredentials(NOS_ACCESS_KEY, NOS_SECRET_KEY)
    val conf: ClientConfiguration = new ClientConfiguration()

    val nosClient: NosClient = new NosClient(credentials, conf)

    val fileKey = s"file/${UUID.randomUUID().toString}.txt"
    nosClient.putObject(NOS_BUCKET, fileKey, input, new ObjectMetadata())
    s"https://$NOS_BUCKET.lf127.net/$fileKey"
  }

  def writeMultiPartNosFile(input: InputStream): String = {
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

    val completeRequest = new CompleteMultipartUploadRequest(NOS_BUCKET, fileKey, uploadId, partETags)
    val completeResult = nosClient.completeMultipartUpload(completeRequest)
    println(completeResult.getLocation)
    s"https://$bucket.lf127.net/$fileKey"
  }

  def queryUserGroup(connection: Connection, summarySql: String, detailSql: String): (Long, Seq[Long]) = {
    println("summary sql: " + summarySql)
    println("detail sql: " + detailSql)

    implicit val conn: Connection = connection
    val total = Sql.queryAll[Long](Sql.read(summarySql), "result").headOption.getOrElse(0L)

    val maxItems = 50000000
    val detail = if (total < maxItems) {
      Sql.queryAll[Long](Sql.read(detailSql), "result")
    } else {
      // TODO for user groups with more than 10 million users, don't output detail user list
      Seq.empty
    }

    total -> detail
  }

  def parseNosUserList(url: String): Seq[String] = {
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

  def executeUserGroupJob(userGroupJob: UserGroup, dt: String, conn: Connection): (Long, String) = {
    val optionText = userGroupJob.options

    implicit val formats: org.json4s.Formats = DefaultFormats
    val option = read[GroupOption](optionText)
    val extraOption = if (userGroupJob.merged.exists(_ > 0)) {
      userGroupJob.mergeOptions map { s => read[GroupOption](s) }
    } else None

    println("execute job: " + userGroupJob)

    val fc = FileContext.getFileContext(new URI("hdfs://hz-cluster10/user/da_lofter"))

    (option.generateType, extraOption.map(_.generateType)) match {
      case (0, None | Some(0)) =>
        val (summarySql, detailSql) = mapOptionToTagGroupQuery(option, extraOption, userGroupJob.mergeLogic, dt)
        val (userCount, userList) = queryUserGroup(conn, summarySql, detailSql)

        val jobOutPath = getUserGroupJobDir(fc, dt, userGroupJob.id)
        writeHdfsFile(fc, jobOutPath, userList.map(_.toString))

        val userListNosFile = if (userCount > 6000000) {
          writeMultiPartNosFile(fc.open(jobOutPath))
        } else {
          writeNosFile(fc.open(jobOutPath))
        }

        userCount -> userListNosFile
      case (1, None | Some(1)) =>
        // user upload group
        val otherFileUrl = extraOption.flatMap(_.fileUrl)
        val userList: Set[String] =
          if (otherFileUrl.isDefined) {
            val setA = parseNosUserList(option.fileUrl.get)
            val setB = parseNosUserList(otherFileUrl.get)
            userGroupJob.mergeLogic match {
              case Some(0) =>
                setA.toSet.intersect(setB.toSet)
              case Some(2) =>
                setA.toSet.filterNot(setB.toSet)
              case _ =>
                Seq(setA, setB).flatten.toSet
            }
          } else parseNosUserList(option.fileUrl.get).toSet

        val userCount = userList.size.toLong

        val jobOutPath = getUserGroupJobDir(fc, dt, userGroupJob.id)
        writeHdfsFile(fc, jobOutPath, userList.toSeq)

        val resultNosFile = if (otherFileUrl.isDefined) writeNosFile(fc.open(jobOutPath)) else option.fileUrl.get
        userCount -> resultNosFile
      case (1, Some(0)) | (0, Some(1)) =>
        val uploadUserSet = (option.generateType match {
          case 1 => parseNosUserList(option.fileUrl.get)
          case _ => parseNosUserList(extraOption.get.fileUrl.get)
        }).flatMap { s => Try {
          s.toLong
        }.toOption
        }.toSet

        val (_, queryUsers) = option.generateType match {
          case 1 =>
            val (summarySql, detailSql) = mapOptionToTagGroupQuery(extraOption.get, None, None, dt)
            queryUserGroup(conn, summarySql, detailSql)
          case _ =>
            val (summarySql, detailSql) = mapOptionToTagGroupQuery(option, None, None, dt)
            queryUserGroup(conn, summarySql, detailSql)
        }

        val queryUsersSet = queryUsers.toSet
        val setA = if (option.generateType == 1) uploadUserSet else queryUsersSet
        val setB = if (option.generateType == 1) queryUsersSet else uploadUserSet

        val resultUsers = userGroupJob.mergeLogic match {
          case Some(0) =>
            setA.filter(setB)
          case Some(2) =>
            setA.filterNot(setB)
          case _ =>
            (setA ++ setB.filterNot(setA))
        }

        val jobOutPath = getUserGroupJobDir(fc, dt, userGroupJob.id)
        writeHdfsFile(fc, jobOutPath, resultUsers.toSeq.map(_.toString))

        val userCount = resultUsers.size.toLong
        val userListNosFile = if (userCount > 10000000) {
          writeMultiPartNosFile(fc.open(jobOutPath))
        } else {
          writeNosFile(fc.open(jobOutPath))
        }

        userCount -> userListNosFile
      case _ => 0L -> ""
    }
  }

  def isTagGroupReady(dt: String, clickhouseConn: Connection): Boolean = {
    implicit val conn: Connection = clickhouseConn
    val isReady = Sql.queryAll[Long](Sql.read(s" select ready from lofter.user_portrait_status where dt='$dt'"), "ready")
      .headOption.getOrElse(0L) > 0

    isReady
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.optional("dt").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")

    val clickhouseConn: Connection = {
      val clickhouseJdbcUrl = "jdbc:clickhouse://lofter-data-common5.gy.ntes:8123/hive?socket_timeout=1000000"
      val clickUser = clickHouseUser
      val clickPass = clickHousePassword
      Class.forName(clickHouseDriverName)
      DriverManager.getConnection(clickhouseJdbcUrl, clickUser, clickPass)
    }

    import com.netease.wm.group.platform.common.databases.getDDBConn
    implicit val conn: Connection = getDDBConn

    var errors: Int = 0
    // firstly try to retry yesterday uncompleted jobs
    errors += runJobs(clickhouseConn, dayAgo)

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < 500 * 1000) {
      errors += runJobs(clickhouseConn, dt)
      Thread.sleep(1000)
    }

    if (errors > 0) {
      throw new RuntimeException("execute user group jobs failed: " + errors)
    }

    spark.close()
  }

  private def runJobs(clickhouseConn: Connection, dt: String)(implicit conn: Connection): Int = {
    import com.netease.wm.util.Sql._

    var errors: Int = 0
    val dtTime = DateTime.parse(dt).withTimeAtStartOfDay().getMillis
    // only include jobs of next day
    val endDtTime = DateTime.parse(dt).plusDays(2).withTimeAtStartOfDay().getMillis
    val oldVersionJobTime = DateTime.parse("2023-11-02").withTimeAtStartOfDay().getMillis

    if (isTagGroupReady(dt, clickhouseConn)) { // check clickhouse tag group ready
      val jobs = sql"select * from Cmb_CrowdPackage where status = 0 and packageType = 2 and createTime < ${0} and endTime >= ${1} and createTime <= ${2} ".query[UserGroup](param(endDtTime, dtTime, oldVersionJobTime))

      jobs foreach { job =>
        try {
          val jobId = job.id
          val dailyResult = if (job.state == 0) {
            sql"select * from Cmb_PackDailyStatistic where packId = ${0}".query[UserGroupDailyResult](jobId).headOption
          } else {
            sql"select * from Cmb_PackDailyStatistic where packId = ${0} and createTime = ${1}".query[UserGroupDailyResult](jobId -> dtTime).headOption
          }

          if (dailyResult.isEmpty) {
            val (userCount, userListNosFile) = executeUserGroupJob(job, dt, clickhouseConn)
            val result = UserGroupDailyResult(jobId, job.name, job.`type`, dtTime, userCount, userListNosFile)
            sql"insert into Cmb_PackDailyStatistic(id, packId, name, type, createTime, userCount, resultUrl) values(seq, ${"packId"}, ${"name"}, ${"type"}, ${"createTime"}, ${"userCount"}, ${"resultUrl"})".update(result)
          } else if (job.state == 0) {
            val fc = FileContext.getFileContext(new URI("hdfs://hz-cluster10/user/da_lofter"))
            copyStaticUserGroupFile(fc, job, dt)
          }
        } catch {
          case NonFatal(e) =>
            println(s"error while executing task " + job.toString)
            e.printStackTrace()
            errors += 1
        }
      }
    } else {
      println(s"clickhouse tag group for $dt is not ready")
    }
    errors
  }
}
