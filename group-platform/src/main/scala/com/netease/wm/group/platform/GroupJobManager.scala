package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.GroupQueryBuilder._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.{DefaultFormats, JValue}
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import scala.util.control.NonFatal

object GroupJobManager {

  case class GroupJob(id: Long, name: String, `type`: Int, state: Int, options: String,
                      merged: Option[Int], createTime: Long, mergeLogic: Option[Int], mergeOptions: Option[String],
                      packageFilter: Option[String], genericConfig: Option[String], syncStatus: Option[Int],
                      businessName: String,runSql: Option[String], tagList: Option[String])

  sealed trait GroupOption{}
  case class GroupOptionItem(generateType: Int, logic: Option[Int], fileUrl: Option[String], config: JValue) extends GroupOption
  case class GroupOptionSet(logic: Logic, options: Seq[GroupOption]) extends GroupOption

  case class PackageFilter(whiteList: Seq[Long], blackList: Seq[Long],
                           compressType: Option[String], compressVal: Option[String])

  case class GroupJobQueryResult(jobId: Long, total: Long, nosUrl: String, warnCount: Long, warnNosUrl: String, extraUrl: Option[String] = None, runSql: String)
  case class GroupJobResult(packId: Long, name: String, `type`: Int, createTime: Long, userCount: Long, resultUrl: String, packageType: Int, ext: String, syncStatus: Option[Int] = None)
  case class WarnResult(packageId: Long, count: Long, fileUrl: String)
  case class WarnNotify(`type`: String = "CONTENT_PACKAGE_DAILY_SLEVEL_WARN", data: WarnResult)
  case class ExtraResult(value: String)
  case class GroupJobResultExt(warn: Option[WarnResult], resultLinkUrl: Option[ExtraResult] = None)
  case class ReturnResult(packageid:Long, dailyid:Long, version:Int, status:Int, userCount: Long, resultUrl: String, ext: String, syncStatus: Option[Int] = None, runSql: String, createTime: Long)


  def parseJobOption(optionItem: GroupOptionItem): MetricGroup = {
    implicit val formats: org.json4s.Formats = DefaultFormats

    if(optionItem.fileUrl.nonEmpty) {
      MetricGroup(LogicOr, Seq(Metric(NOS_TAG, MetricValue(industry = optionItem.fileUrl))))
    } else {
        val metricValues: Map[String, MetricValue] = optionItem.config.extract[Map[String, MetricValue]]
        val logic = logicOf(optionItem.logic)
        val metrics = metricValues.map{ e => Metric(e._1, e._2)}.toSeq
        MetricGroup(logic, metrics)
    }
  }

  def parseJobOptionItem(options: String): GroupOptionItem = {
    implicit val formats: org.json4s.Formats = DefaultFormats
    read[GroupOptionItem](options)
  }

  def parseJobPackageFilter(options: String): PackageFilter = {
    implicit val formats: org.json4s.Formats = DefaultFormats
    read[PackageFilter](options)
  }

  def applyFilters(groupItems: Seq[Long], filter: PackageFilter): Seq[Long] = {
    val whiteList = if(filter.whiteList != null) filter.whiteList.toSet else Set.empty[Long]
    val blackList = if(filter.blackList != null) filter.blackList.toSet else Set.empty[Long]

    val whiteListRepeated = groupItems.filter(i => whiteList(i)).toSet
    val base = (whiteList.filterNot(whiteListRepeated).toSeq ++ groupItems).filterNot(blackList)

    filter.compressType.map(_.toLowerCase) match {
      case Some("percent") =>
        val baseSize = base.size
        val size = filter.compressVal.map(_.toDouble * baseSize / 100.0).getOrElse(baseSize.toDouble).toInt
        scala.util.Random.shuffle(base).take(size)
      case Some("tail") =>
        val tailSet = filter.compressVal.toSeq.flatMap(_.split(",").toSeq).map(_.toLong).toSet
        base.filter(n => tailSet(n % 10))
      case Some("max") =>
        val maxItems = filter.compressVal.map(_.toInt).getOrElse(0)
        base.takeRight(maxItems)
      case _ => base
    }
  }

  private def parseEmbeddedConfig(value: JValue): JValue = {
    implicit val formats: org.json4s.Formats = DefaultFormats
    if(value != null){
      value.extractOpt[String].map(s => parse(s)).orNull
    } else null
  }

  def parseGroupJobV1(groupJob: GroupJob): GroupOption = {
    implicit val formats: org.json4s.Formats = DefaultFormats

    val optionText = groupJob.options
    val mainOption = read[GroupOptionItem](optionText)
    val mergeLogic = logicOf(groupJob.mergeLogic)

    val extraOption = if (groupJob.merged.exists(_ > 0)) {
      groupJob.mergeOptions map { s => read[GroupOptionItem](s) }
    } else None

    val finalMainOption = mainOption.copy(config = parseEmbeddedConfig(mainOption.config))
    val finalExtraOption = extraOption.map { e =>
      e.copy(config = parseEmbeddedConfig(e.config))
    }

    if(finalExtraOption.isEmpty) {
      finalMainOption
    } else {
      GroupOptionSet(mergeLogic, Seq(finalMainOption, finalExtraOption.get))
    }
  }

  def parseGroupJobV2(groupJob: GroupJob): GroupOption = {
    val config = groupJob.genericConfig.get
    val root = parse(config)
    parseGenericConfig(root)
  }

  private def parseGenericConfig(root: JValue): GroupOption = {
    implicit val formats: org.json4s.Formats = DefaultFormats
    val optionType = (root \ "type").extract[String]
    optionType match {
      case "option-item" =>
        root.extract[GroupOptionItem]
      case "option-group" =>
        val logic = logicOf((root \ "logic").extractOpt[Int])
        val options = root \ "options"
        val subGroups = options.extract[Seq[JValue]] map { e => parseGenericConfig(e) }

        GroupOptionSet(logic, subGroups)
      case _ => throw new RuntimeException("unsupported group job type: " + optionType)
    }
  }
}

case class GroupJobManager(groupQueryBuilder: GroupQueryBuilder, jobStore: JobStateStore, jobType: Int = 1) {
  import GroupJobManager._

  private val prevDayGroupQueryBuilder = groupQueryBuilder.copy(
    dt = DateTime.parse(groupQueryBuilder.dt).minusDays(1).toString("yyyy-MM-dd")
  )

  val LOG: Logger = LoggerFactory.getLogger(classOf[GroupJobManager])

  private def translate(groupOption: GroupOption): TagGroup = {
    groupOption match {
      case groupOptionItem: GroupOptionItem =>
        groupQueryBuilder.translate(parseJobOption(groupOptionItem))
      case GroupOptionSet(logic, options) =>
        val tagGroups = options.map(option => translate(option))
        CompositeTagGroup(logic, tagGroups)
    }
  }

  def compileGroupJobQuery(groupJob: GroupJob): (GroupQuery, GroupQuery) = {
    val groupOption = if(groupJob.genericConfig.nonEmpty) {
      parseGroupJobV2(groupJob)
    } else {
      parseGroupJobV1(groupJob)
    }

    val compositeTagGroup = translate(groupOption)
    val mainGroupQuery = compositeTagGroup.query(groupQueryBuilder)
    val warnQuery = groupQueryBuilder.buildWarnQuery(compositeTagGroup)
    (mainGroupQuery, warnQuery)
  }

  def executeGroupJob(groupJob: GroupJob, dt: String, itemsExtraProcess: (Long, String, Int) => String): GroupJobQueryResult = {
    val jobId = groupJob.id
    val (groupQuery, warnQuery) = compileGroupJobQuery(groupJob)

    val runSql = groupQuery match {
      case jdbcQuery: JdbcQuery => jdbcQuery.detailSql.replace("\n"," ")
      case _ => null
    }

    val (total, groupItems) = groupQuery.execute()
    val (_, warnItems) = warnQuery.execute()

    val hasPackageFilter = groupJob.packageFilter.exists(_.nonEmpty)
    val (finalGroupItems, finalWarnItems) = if(hasPackageFilter) {
      val filter = parseJobPackageFilter(groupJob.packageFilter.get)
      (applyFilters(groupItems, filter),
       applyFilters(warnItems, filter))
    } else (groupItems, warnItems)

    jobStore.saveJobState(dt, jobId, finalGroupItems.map(_.toString))

    val nosUrl = jobStore.uploadJobStateToNos(dt, jobId)

    if(finalWarnItems.nonEmpty) {
      LOG.info("job {} warn items: {}", jobId, finalWarnItems.mkString(","))
    }

    val warnNosUrl = jobStore.uploadNos(finalWarnItems.map(_.toString))

    val finalTotal = finalGroupItems.size
    val extraNosUrl = if(finalTotal > 0) {
      val extra = itemsExtraProcess(jobId, jobStore.getJobState(dt, jobId), groupJob.`type`)
      if(extra.nonEmpty) Some(extra) else None
    } else None

    GroupJobQueryResult(jobId, finalTotal, nosUrl, finalWarnItems.size, warnNosUrl, extraNosUrl, runSql)
  }

  def runJobs(dt: String,
              notifyWarn: (WarnNotify) => Unit = {_ => },
              jobExtraProcess: (Long, String, Int) => String = (_, _, _) => "",
              completeNotify: (ReturnResult) => Unit = {_ => },
              packageid: Int ,
              dailyid:Int,
              tags: String,
              bline: String
             )(implicit conn: Connection): Int = {
    import com.netease.wm.util.Sql._
    implicit val formats: org.json4s.Formats = DefaultFormats

    var errors: Int = 0

    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")
    val dtTime = DateTime.parse(dt).withTimeAtStartOfDay().getMillis
    val createDtTime = DateTime.parse(dt).plusDays(1).withTimeAtStartOfDay().getMillis
    val endDtTime = DateTime.parse(dt).plusDays(2).withTimeAtStartOfDay().getMillis
    val oldVersionJobTime = DateTime.parse("2023-11-02").withTimeAtStartOfDay().getMillis

    if(ClickHouseHelper.isTagGroupReady(dt, tags = tags, bline) && packageid != 0){
      val jobs = sql"select * from Cmb_CrowdPackage where status = 0 and id = ${0}".query[GroupJob](param(packageid))

      jobs foreach { job =>
        try {
          val jobId = job.id
          val dailyResult = sql"select * from Cmb_PackDailyStatistic where packId = ${0} and version = 2 and status = 1 and createTime < ${1} and createTime > ${2}".query[GroupJobResult](jobId,createDtTime,endDtTime).headOption
          println(s"2.开始计算,当日是否计算:${!dailyResult.isEmpty},packid=$packageid,dailyid=$dailyid")
          if (dailyResult.isEmpty) {
            val queryResult = executeGroupJob(job, dt, jobExtraProcess)
            val warn = if(queryResult.warnCount > 0) Some(WarnResult(jobId, queryResult.warnCount, queryResult.warnNosUrl)) else None
            val groupJobResultExt = GroupJobResultExt(warn = warn, resultLinkUrl = queryResult.extraUrl.map(url => ExtraResult(url)))

            val result = ReturnResult(
              packageid= jobId,
              dailyid = dailyid,
              version = 2,
              status = if(queryResult.runSql != null) 1 else 0,
              ext = write(groupJobResultExt),
              syncStatus = job.syncStatus,
              userCount = queryResult.total,
              resultUrl=queryResult.nosUrl,
              runSql = queryResult.runSql,
              createTime = createDtTime
            )

            sql"update Cmb_PackDailyStatistic set status = ${"status"},userCount=${"userCount"},resultUrl=${"resultUrl"} where id = ${"dailyid"}".update(result)

            sql"insert into Cmb_CrowdDailyRunRecord(id, packId, dailyStatisticId, runSql, createTime) values(seq, ${"packageid"}, ${"dailyid"}, ${"runSql"}, ${"createTime"})".update(result)

            println(s"3.执行完成，result:$result")
            warn.foreach { data =>
              notifyWarn(WarnNotify(data = data))
            }

            completeNotify(result)
          } else if (job.state == 0) {
            jobStore.copyJobState(dt, jobId, dayAgo)
          }
        } catch {
          case NonFatal(e) =>
            LOG.error("error while executing task: {}", job.toString)
            e.printStackTrace()
            errors += 1
        }
      }
    } else {
      LOG.error("clickhouse tag group for {} is not ready", dt)
    }
    errors
  }
}
