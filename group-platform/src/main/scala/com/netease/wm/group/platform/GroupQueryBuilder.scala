package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.GroupQueryBuilder.Tag

object GroupQueryBuilder {
  val NOS_TAG: String =  "__nos__"

  case class MetricValue(time: Option[String] = None, quantity: Option[String] = None, industry: Option[String] = None) {
    def isCombination: Boolean = {
      industry.exists(_.contains("&")) || quantity.exists(_.contains("&"))
    }
  }

  case class Metric(name: String, value: MetricValue) {
    def isCombination: Boolean = value.isCombination
  }

  case class MetricGroup(logic: Logic, metrics: Seq[Metric]) {
    def isCombination: Boolean = metrics.exists(_.isCombination)
  }

  sealed trait Logic {}
  case object LogicAnd extends Logic
  case object LogicOr extends Logic
  case object LogicNot extends Logic

  def logicOf(logic: Option[Int]): Logic = {
    logic match {
      case Some(1) | None => LogicOr
      case Some(0) => LogicAnd
      case Some(2) => LogicNot
      case _ => throw new RuntimeException("unknown logic operator: " + logic.get)
    }
  }
  sealed trait Tag {
    def getName: String
    def getLevel: Int
    def getValues: Seq[Seq[String]]
    def isCombo: Boolean
    def getPeriod: String
    // generate tag query condition
    def getQueryCondition: String
  }
  trait TagWithValueSet {
    def getOutSetCondition: String
  }

  trait TagWithRawValue {
  }

  sealed trait GroupQuery {
    def execute(): (Long, Seq[Long])
  }

  case class EmptyGroupQuery() extends GroupQuery {
    override def execute(): (Long, Seq[Long]) = (0L, Seq.empty[Long])
  }

  case class JdbcQuery(summarySql: String, detailSql: String, stateSql: String) extends GroupQuery {
    override def execute(): (Long, Seq[Long]) = {
      ClickHouseHelper.queryGroupWithSummary(detailSql, summarySql, maxItems = 50000000)
    }
  }

  sealed trait TagGroup {
    def query(groupQueryBuilder: GroupQueryBuilder): JdbcQuery
  }


  case class BaseTagGroup(logic: Logic, subQueries: Seq[Tag]) extends TagGroup {
    override def query(groupQueryBuilder: GroupQueryBuilder): JdbcQuery = groupQueryBuilder.buildJdbcQuery(this)
  }

  case class CompositeTagGroup(logic: Logic, children: Seq[TagGroup]) extends TagGroup {
    override def query(groupQueryBuilder: GroupQueryBuilder): JdbcQuery = groupQueryBuilder.buildJdbcQuery(this)
  }

  case class NosTagGroup(nosUrl: String) extends TagGroup {
    def internalNosUrl: String = {
      nosUrl.replace("https://lofter.lf127.net/", "http://lofter.nos2-i.service.163.org/")
    }
    override def query(groupQueryBuilder: GroupQueryBuilder): JdbcQuery = {
      JdbcQuery(
        summarySql = s"select groupBitmap(toUInt64OrZero(userId)) as result from url('$internalNosUrl', CSV, 'userId String')",
        detailSql = s"select toUInt64OrZero(userId) as result from url('$internalNosUrl', CSV, 'userId String') where result > 0",
        stateSql = s"select groupBitmapState(toUInt64OrZero(userId)) as result from url('$internalNosUrl', CSV, 'userId String')"
      )
    }
  }

  val EMPTY_QUERY: JdbcQuery = JdbcQuery("select number as result from numbers(0)", "select number as result from numbers(0)", "select groupBitmapState(number) as result from numbers(0)")
  val EMPTY_METRIC_GROUP: MetricGroup = MetricGroup(LogicOr, Seq.empty)
  val EMPTY_TAG_GROUP: BaseTagGroup = BaseTagGroup(LogicOr, Seq.empty)

  abstract case class BaseTag(tag: String, dimensions: Seq[Seq[String]]) extends Tag {
    override def getName: String = tag
    override def getLevel: Int = dimensions.size
    override def getValues: Seq[Seq[String]] = dimensions
    override def isCombo: Boolean = dimensions.exists(_.size > 1)
    override def getPeriod: String = {
      if(dimensions.size > 0 && dimensions.head.size > 0) {
        dimensions.head.head match {
          case period@("DAY_1"|"DAY_3"|"DAY_7"|"DAY_15"|"DAY_30"|"DAY_90"|"DAY_180"|"DAY_ALL") => period
          case _ => ""
        }
      } else ""
    }

    protected def dimensionQuerySql(names: Seq[String], values: Seq[Seq[String]]): String = {
      names.zip(values)
        .map {
          case (dimensionName, dimensionValues) =>
            val dimensionValuesSqlParts = dimensionValues.filterNot(_.trim.isEmpty).mkString("'", "','", "'")
            s"$dimensionName in ($dimensionValuesSqlParts) "
        }.mkString(" and ")
    }

    protected def parseDimensionRange(value: String): (String, String) = {
      val result = if(value.contains("-")) {
        val index = value.indexOf('-')
        (value.substring(0, index), if(index >= value.length) "" else value.substring(index + 1))
      } else {
        (value, value)
      }

      // TODO workaround for date format
      (result._1.replaceAll("/", "-"), result._2.replaceAll("/", "-"))
    }

    protected def parseValueRange(value: String): (Double, Double) = {
      val valueArray = value.split("-").map(_.trim)

      valueArray.length match {
        case 1 =>
          if (value.endsWith("-")) {
            (valueArray(0).toDouble, 0.0)
          } else {
            (0.0, 0.0)
          }
        case 2 =>
          if (value.startsWith("-")) {
            (0.0, valueArray(1).toDouble)
          } else {
            (valueArray(0).toDouble, valueArray(1).toDouble)
          }
        case _ =>
          throw new IllegalArgumentException("Value must be in the format 'start-end' or '-end' or 'start-'.")
      }
    }

  }

  val BITMAP_DIMENSIONS = Seq("dim1", "dim2", "grp")

  class GroupBitmapTag(tag: String, dimensions: Seq[Seq[String]]) extends BaseTag(tag, dimensions) {
    override def getQueryCondition: String = {
      // TODO for level 3, there is a history mismatch between industry and quantity
      val dimensionNormalized = dimensions
//        if(dimensions.size == 3) {
//        Seq(dimensions(0),dimensions(2), dimensions(1))
//      } else dimensions

      val dimensionNames = BITMAP_DIMENSIONS.slice(0, dimensions.size - 1) ++ Seq(BITMAP_DIMENSIONS.last)

      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensionNormalized)

      Seq(s"tag = '$tag'", dimensionsSql).filter(_.nonEmpty).mkString(" and ")
    }
  }

  class GroupBitmapTagSet(tag: String, dimensions: Seq[Seq[String]], set: Set[String]) extends GroupBitmapTag(tag, dimensions) with TagWithValueSet {
    override def getOutSetCondition: String = {
      val dimensionNames = BITMAP_DIMENSIONS.slice(0, dimensions.size - 1) ++ Seq(BITMAP_DIMENSIONS.last)

      val all = set.map(_.trim).filter(_.nonEmpty)
      val groupSet = dimensions.last.map(_.trim).filter(_.nonEmpty).toSet
      val otherGroups = all.filterNot(groupSet).toSeq

      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensions.dropRight(1) ++ Seq(otherGroups))
      Seq(s"tag = '$tag'", dimensionsSql).filter(_.nonEmpty).mkString(" and ")
    }
  }

  class GroupBitmapTagRange(tag: String, dimensions: Seq[Seq[String]]) extends GroupBitmapTag(tag, dimensions) {

    override def isCombo: Boolean = true

    override def getQueryCondition: String = {
      val dimensionNames = BITMAP_DIMENSIONS.slice(0, dimensions.size - 1)

      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensions.dropRight(1))

      val valueField = BITMAP_DIMENSIONS.last

      val valueParts = dimensions.last.head
      assert(valueParts.contains("-"), "raw value part should contain 2 parts separated by -, got :" + valueParts )
      val (startValue, endValue) = parseDimensionRange(valueParts)

      val valueSql = (startValue, endValue) match {
        case ("", end) if end.trim.forall(_.isDigit) => toNumberRangeQuery(valueField, end = Option(end.toLong))
        case (start, "") if start.trim.forall(_.isDigit) => toNumberRangeQuery(valueField, start = Option(start.toLong))
        case (start, end) if start.trim.forall(_.isDigit) && end.trim.forall(_.isDigit) => toNumberRangeQuery(valueField, start = Option(start.toLong), end = Option(end.toLong))
        case ("", end) => s"$valueField <= '$end' "
        case (start, "") => s"$valueField >= '$start' "
        case _ => s"$valueField >= '$startValue' and $valueField <= '$endValue' "
      }

      Seq(s"tag = '$tag'", dimensionsSql, valueSql).filter(_.nonEmpty).mkString(" and ")
    }

    private def toNumberRangeQuery(valueField: String, start: Option[Long] = None, end: Option[Long] = None): String = {
      (start, end) match {
        case (None, Some(endValue)) => s"toInt64OrZero($valueField) <= $endValue "
        case (Some(startValue), None) => s"toInt64OrZero($valueField) >= $startValue "
        case (Some(startValue), Some(endValue)) => s"toInt64OrZero($valueField) >= $startValue and toInt64OrZero($valueField) <= $endValue"
        case _ => "1=0"
      }

    }

  }

  class GroupBitmapTagComplement(tag: String, dimensions: Seq[Seq[String]]) extends GroupBitmapTag(tag, dimensions) with TagWithValueSet {
    override def getQueryCondition: String = {
      " tag = 'ALL' "
    }

    override def getLevel: Int = super.getLevel + 1 // fix level

    override def getOutSetCondition: String = {
//      val dimensionNormalized = if(dimensions.size == 3) {
//        Seq(dimensions(0),dimensions(2), dimensions(1))
//      } else dimensions
      val dimensionNormalized = dimensions
      val dimensionNames = BITMAP_DIMENSIONS.slice(0, dimensions.size)
      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensionNormalized)

      Seq(s"tag = '$tag'", dimensionsSql).filter(_.nonEmpty).mkString(" and ")
    }
  }

  val RAW_DIMENSIONS = Seq("dim1", "dim2", "value")
  class RawValueTag(tag: String, dimensions: Seq[Seq[String]]) extends BaseTag(tag, dimensions) with TagWithRawValue {
    override def getQueryCondition: String = {
      val dimensionNames = RAW_DIMENSIONS.slice(0, dimensions.size - 1)
      val dimensionsValues = dimensions.dropRight(1)
      val valueField = RAW_DIMENSIONS.last

      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensions)

      val valueParts = dimensions.last.head
      assert(valueParts.contains("-"), "raw value part should contain 2 parts separated by -, got: " + valueParts)
      val (startValue, endValue) = parseValueRange(valueParts)

      val valueSql = (startValue, endValue) match {
        case (0.0, 0.0) => s"$valueField = 0.0"
        case (startValue, 0.0) => s"$valueField >= $startValue"
        case (0.0, endValue) => s"$valueField <= $endValue"
        case (startValue, endValue) => s"$valueField >= $startValue and $valueField <= $endValue"
      }

      Seq(s"tag = '$tag'", dimensionsSql, valueSql).filter(_.nonEmpty).mkString(" and ")
    }

  }

  class RawValueTagComplement(tag: String, dimensions: Seq[Seq[String]]) extends RawValueTag(tag, dimensions) with TagWithValueSet with TagWithRawValue {
    override def getQueryCondition: String = {
      " tag = 'ALL' "
    }

    override def getLevel: Int = super.getLevel + 1 // fix level

    override def getOutSetCondition: String = {
      val dimensionNames = RAW_DIMENSIONS.slice(0, dimensions.size)
      val dimensionsSql = dimensionQuerySql(dimensionNames, dimensions)

      Seq(s"tag = '$tag'", dimensionsSql).filter(_.nonEmpty).mkString(" and ")
    }
  }

}

case class GroupQueryBuilder(dt: String,
                             tagResolve: String => String = identity,
                             tagValueResolve: (String, String) => String = (_: String, value: String) => value,
                             tagValueSetResolve: String => Set[String] = (_: String) => Set.empty[String],
                             tagValueComplementResolve: (String, String) => Boolean = (_: String, _: String) => false,
                             tagValueRangeResolve: (String, String) => Boolean = (_: String, _: String) => false,
                             warnTag: Option[Tag] = None,
                             tagGroupTableResolve: (String, Int, String) => String = (_: String, level: Int, _: String) => s"lofter.user_portrait_level$level",
                             tagRawResolve: String => Boolean = (_: String) => false
                            ) {
  import GroupQueryBuilder._

  def withWarnTag(warnTag: Tag): GroupQueryBuilder = {
    this.copy(warnTag = Some(warnTag))
  }

  def withTagResolve(tagResolve: String => String): GroupQueryBuilder = {
    this.copy(tagResolve = tagResolve)
  }

  def withTagValueResolve(tagValueResolve: (String, String) => String): GroupQueryBuilder = {
    this.copy(tagValueResolve = tagValueResolve)
  }

  def withTagValueSetResolve(tagValueSetResolve: String => Set[String]): GroupQueryBuilder = {
    this.copy(tagValueSetResolve = tagValueSetResolve)
  }

  def withTagValueComplementResolve(tagValueComplementResolve : (String, String) => Boolean): GroupQueryBuilder = {
    this.copy(tagValueComplementResolve = tagValueComplementResolve)
  }

  def withTagValueRangeResolve(tagValueRangeResolve : (String, String) => Boolean): GroupQueryBuilder = {
    this.copy(tagValueRangeResolve = tagValueRangeResolve)
  }

  def withTagGroupTableResolve(tagGroupTableResolve: (String, Int, String) => String): GroupQueryBuilder = {
    this.copy(tagGroupTableResolve = tagGroupTableResolve)
  }

  def withTagRawResolve(tagRawResolve: String => Boolean): GroupQueryBuilder = {
    this.copy(tagRawResolve = tagRawResolve)
  }

  def translate(metricGroup: MetricGroup): TagGroup = {
    val firstMetric = metricGroup.metrics.head
    // upload metric group should only contain 1 element
    if(firstMetric.name == NOS_TAG) return NosTagGroup(firstMetric.value.industry.get)

    if(metricGroup.isCombination) {
      val metricQueries = metricGroup.metrics.map { metric =>
        if(metric.isCombination) {
          val mv = metric.value
          val quantityParts = mv.quantity.toSeq.flatMap(_.split("&"))
          val industryParts = mv.industry.toSeq.flatMap(_.split("&"))

          val combinationMetricGroup = (quantityParts.size > 1, industryParts.size > 1) match {
            case (true, true) =>
              val combinations = for (
                quantity <- quantityParts;
                industry <- industryParts
              ) yield metric.copy(value = mv.copy(quantity = Option(quantity), industry = Option(industry)))

              MetricGroup(LogicAnd, combinations)
            case (true, false) =>
              val combinations = quantityParts.map { quantity =>
                metric.copy(value = mv.copy(quantity = Option(quantity)))
              }

              MetricGroup(LogicAnd, combinations)
            case (false, true) =>
              val combinations = industryParts.map { industry =>
                metric.copy(value = mv.copy(industry = Option(industry)))
              }

              MetricGroup(LogicAnd, combinations)
            case _ => throw new RuntimeException("non combination metric group parsing: " + metric.toString)
          }

          translate(combinationMetricGroup)
        } else {
          val tag = translateMetric(metric)
          BaseTagGroup(LogicOr, Seq(tag))
        }
      }

      CompositeTagGroup(metricGroup.logic, metricQueries)
    } else {
      val metricQueries = metricGroup.metrics.map(m => translateMetric(m))
      BaseTagGroup(metricGroup.logic, metricQueries)
    }
  }

  def translateMetric(metric: Metric): Tag = {
    val tag =  tagResolve(metric.name)

    val tagTime = metric.value.time.toSeq.flatMap{ s => s.split(",").toSeq.map(mv => tagValueResolve(tag, mv)).filter(_.nonEmpty)}
    val tagIndustry = metric.value.industry.toSeq.flatMap{ s => s.split(",").toSeq.map(mv => tagValueResolve(tag, mv)).filter(_.nonEmpty)}
    val tagQuantity = metric.value.quantity.toSeq.flatMap{ s => s.split(",").toSeq.map(mv => tagValueResolve(tag, mv)).filter(_.nonEmpty)}

    // dimension value priority: time > industry > quantity
    val dimensions =Seq(tagTime, tagIndustry, tagQuantity).filter(_.nonEmpty)

    val dimensionTail = dimensions.last
    val isRaw = tagRawResolve(tag)
    val isSet = tagValueSetResolve(tag).nonEmpty

    val isComplement = dimensionTail.exists(v => tagValueComplementResolve(tag, v))
    val isRange = dimensionTail.exists(v => tagValueRangeResolve(tag, v))

    // complement, set, range processing
    isRaw match {
      case true if isComplement => new RawValueTagComplement(tag, dimensions.dropRight(1))
      case true => new RawValueTag(tag, dimensions)
      case _ if isSet => new GroupBitmapTagSet(tag, dimensions, tagValueSetResolve(tag))
      case _ if isComplement => new GroupBitmapTagComplement(tag, dimensions.dropRight(1))
      case _ if isRange => new GroupBitmapTagRange(tag, dimensions)
      case _ => new GroupBitmapTag(tag, dimensions)
    }
  }

  def buildJdbcQuery(tagGroup: BaseTagGroup): JdbcQuery = {
    val tableGroup = tagGroup.subQueries.groupBy { g => tagGroupTableResolve(g.getName, g.getLevel, g.getPeriod)}.toSeq
    val groupLogic = tagGroup.logic

    val (mergeOp, mergeResultOp) = tagGroup.logic match {
      case LogicAnd => ("groupBitmapAndState", "groupBitmapAnd")
      case LogicOr => ("groupBitmapOrState", "groupBitmapOr")
      case LogicNot => throw new RuntimeException("unsupported logic for metric group")
    }

    val lastSaturday = DateTime.parse(dt).plusDays(1).minusWeeks(1).withDayOfWeek(6).toString("yyyy-MM-dd")

    val tableQueries = tableGroup map { g =>
      val (table, tableMetrics) = g
      val isSetQueryExists = tableMetrics.exists(q => q.isInstanceOf[TagWithValueSet])
      val isRawQueryExists = tableMetrics.exists(q => q.isInstanceOf[TagWithRawValue])
      val tablePartition = if(table.endsWith("_wd")) lastSaturday else dt

      groupLogic match {
        case LogicOr if !isSetQueryExists && !isRawQueryExists =>
          // merge all conditions then query
          val conditions = tableMetrics.map(_.getQueryCondition).mkString("((", s") or (", "))")
          val summarySql = s"select $mergeResultOp(users) as result from $table where dt='$tablePartition' and $conditions"
          val detailSql = s"select arrayJoin(bitmapToArray($mergeOp(users))) as result from $table where dt='$tablePartition' and $conditions"
          val stateSql = s"select $mergeOp(users) as result from $table where dt='$tablePartition' and $conditions"

          JdbcQuery(summarySql, detailSql, stateSql)

        case LogicAnd | LogicOr =>
          val setSubs = tableMetrics.filter(q => q.isInstanceOf[TagWithValueSet] && !q.isInstanceOf[TagWithRawValue])
          val rawSubs = tableMetrics.filter(q => q.isInstanceOf[TagWithRawValue])
          val rawSetSubs = tableMetrics.filter(q => q.isInstanceOf[TagWithRawValue] && q.isInstanceOf[TagWithValueSet])
          val nonComboSetSubs = tableMetrics.filter(q => !q.isCombo && !q.isInstanceOf[TagWithValueSet] && !q.isInstanceOf[TagWithRawValue])
          val comboSubs = tableMetrics.filter(q => q.isCombo && !q.isInstanceOf[TagWithValueSet] && !q.isInstanceOf[TagWithRawValue])

          val nonComboQueries = if(nonComboSetSubs.nonEmpty) {
            val conditions = nonComboSetSubs.map(_.getQueryCondition).mkString("((", s") or (", "))")
            if(groupLogic == LogicAnd) {
              val size = nonComboSetSubs.size
              Seq(s"select if(count(1) >= $size, $mergeOp(users), bitmapBuild(emptyArrayUInt64())) as result from $table where dt='$tablePartition' and $conditions")
            } else {
              Seq(s"select $mergeOp(users) as result from $table where dt='$tablePartition' and $conditions")
            }
          } else Seq.empty
          // combo condition users should be combined with groupBitmapOr firstly
          val comboQueries = comboSubs map { combo =>
            s"select groupBitmapOrState(users) as result from $table where dt='$tablePartition' and ${combo.getQueryCondition}"
          }

          val setQueries = setSubs map { set =>
            s"""
               |with (
               |  select groupBitmapOrState(users) as result from $table where dt='$tablePartition' and (${set.getQueryCondition})
               |) as positiveBitmap, (
               |  select groupBitmapOrState(users) as result from $table where dt='$tablePartition' and (${set.asInstanceOf[TagWithValueSet].getOutSetCondition})
               |) as negativeBitmap
               |select bitmapAndnot(positiveBitmap, negativeBitmap) as result
               |""".stripMargin
          }

          val rawSetQueries = rawSetSubs map { set =>
            s"""
               |with (
               |  select groupBitmapState(userId) as result from $table where dt='$tablePartition' and (${set.getQueryCondition})
               |) as positiveBitmap, (
               |  select groupBitmapState(userId) as result from $table where dt='$tablePartition' and (${set.asInstanceOf[TagWithValueSet].getOutSetCondition})
               |) as negativeBitmap
               |select bitmapAndnot(positiveBitmap, negativeBitmap) as result
               |""".stripMargin
          }

          val rawQueries = rawSubs map { raw =>
              s"""
                 |select groupBitmapState(userId) as result from $table where dt='$tablePartition' and (${raw.getQueryCondition})
                 |""".stripMargin
          }

          val subQueries = nonComboQueries ++ comboQueries ++ setQueries ++ rawQueries ++ rawSetQueries
          val summarySql = s"select $mergeResultOp(result) as result from ${subQueries.mkString("(", " union all ", ")")} t"
          val detailSql = s"select arrayJoin(bitmapToArray($mergeOp(result))) as result from ${subQueries.mkString("(", " union all ", ")")} t"
          val stateSql = s"select $mergeOp(result) as result from ${subQueries.mkString("(", " union all ", ")")} t"

          JdbcQuery(summarySql, detailSql, stateSql)

        case LogicNot => throw new RuntimeException("unsupported logic for metric group")
      }
    }

    tableQueries.size match {
      case 0 => EMPTY_QUERY
      case 1 => tableQueries.head
      case _ =>
        val summarySql = s"select $mergeResultOp(result) as result from ${tableQueries.map(_.stateSql).mkString("(", " union all ", ")")} t"
        val detailSql = s"select arrayJoin(bitmapToArray($mergeOp(result))) as result from ${tableQueries.map(_.stateSql).mkString("(", " union all ", ")")} t"
        val stateSql = s"select $mergeOp(result) as result from ${tableQueries.map(_.stateSql).mkString("(", " union all ", ")")} t"

        JdbcQuery(summarySql, detailSql, stateSql)
    }
  }

  def buildJdbcQuery(compositeTagGroup: CompositeTagGroup): JdbcQuery = {
    import compositeTagGroup._
    val size = children.size
    size match {
      case 0 => EMPTY_QUERY
      case 1 =>
        children.head match {
          case baseTagGroup: BaseTagGroup => buildJdbcQuery(baseTagGroup)
          case nosTagGroup: NosTagGroup => nosTagGroup.query(this)
          case compositeTagGroup: CompositeTagGroup => buildJdbcQuery(compositeTagGroup)
        }
      case _ =>
        val mergeOp = logic match {
          case LogicAnd => "bitmapAnd"
          case LogicOr => "bitmapOr"
          case LogicNot => "bitmapAndnot"
        }

        val queries = children
          .map(child => child.query(this))
          .zipWithIndex.map { case (query, index) =>
          val field = s"bitmap_group$index"
          val sql = query.stateSql
          field -> sql
        }

        val cteQueries = queries.map { case (field, sql) => s"($sql) as $field" }.mkString(",")
        val finalMerge = queries.map(_._1).reduce { (a, b) => s"$mergeOp($a, $b)" }

        val summarySql =
          s"""
             |with $cteQueries
             |select bitmapCardinality($finalMerge) as result
             |""".stripMargin

        val detailSql =
          s"""
             |with $cteQueries
             |select arrayJoin(bitmapToArray($finalMerge)) as result
             |""".stripMargin

        val stateSql =
          s"""
             |with $cteQueries
             |select $finalMerge as result
             |""".stripMargin

        JdbcQuery(summarySql, detailSql, stateSql)
    }
  }

  def buildWarnQuery(tagGroup: TagGroup): JdbcQuery = {
    if(warnTag.nonEmpty) {
      val warnTagGroup = BaseTagGroup(LogicOr, warnTag.toSeq)
      val compositeTagGroup = CompositeTagGroup(LogicAnd, Seq(tagGroup, warnTagGroup))
      buildJdbcQuery(compositeTagGroup)
    } else EMPTY_QUERY
  }

}