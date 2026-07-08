package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.GroupJobManager.GroupJobResult
import com.netease.wm.group.platform.GroupQueryBuilder.GroupBitmapTag
import com.netease.wm.group.platform.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.lit
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import java.sql.Connection

object ContentGroupJob {

  case class KafkaPush(key: String, value: String)
  case class PackageCompleteNotify(packId: Long, createTime: Long, routingKey: String = "ContentPackageSyncMessage")
  case class PopoMessage(content: String, targetType: Int, targets: Seq[String])
  case class GrowthPostDetail(url: String, title: String, new1d: Long, new7d: Long)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.optional("dt").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")

    val userGroupQueryBuilder = GroupQueryBuilder(dt)
      .withTagResolve {
        case "内容标签" => "标签"
        case "alg-内容文字品类" => "alg-内容特征"
        case "alg-内容图片品类" => "alg-内容特征"
        case tag => tag
      }
      .withTagValueSetResolve {
        case "消费偏好" => Set("文字", "图片", "视频")
        case _ => Set.empty
      }
      .withTagValueResolve {
        case ("内容类型", "PHOTOPOST") => "图片"
        case ("内容类型", "VIDEOPOST") => "视频"
        case ("内容类型", "TEXTPOST") => "文字"
        case ("alg-内容特征", "搞笑沙雕") => "shadiao"
        case ("alg-内容特征", "虐文") => "nuewen"
        case ("alg-内容特征", "小甜饼") => "xiaotianbing"
        case ("alg-内容特征", "论坛体") => "luntanti"
        case ("创作者等级", level) => level.replace("LEVEL_", "")
        // time
        case ("发文时间", v) => v.replace("/", "-")
        case (_, "近1天") => "DAY_1"
        case (_, "近7天") => "DAY_7"
        case (_, "近15天") => "DAY_15"
        case (_, "近30天") => "DAY_30"
        case (_, "近90天") => "DAY_90"
        case (_, "近180天") => "DAY_180"
        case (_, "累计") => "DAY_ALL"
        // quantity
        case (_, v) => v
      }
      .withTagValueComplementResolve {
        case ("是否签约内容" | "是否优质内容" | "是否优质创作者" | "是否合集" | "是否设置回礼" |
              "高粉人数" | "会员人数" | "付费礼物人数" | "付费礼物金额" | "是否正向长评内容" | "是否达人博客" | "是否短内容动态", "0") => true
        case ("曝光量" | "浏览uv" | "拉新量" | "长评数量" | "是否cp" | "是否衍生" | "是否同人" | "图片数量" | "互动量", "0") => true
        case _ => false
      }
      .withTagValueRangeResolve {
        case ("付费点击转化率" | "千次曝光收益", _) => true
        case _ => false
      }
      .withWarnTag(new GroupBitmapTag("拉新等级", Seq(Seq("S"))))
      .withTagGroupTableResolve { (tag, level, time) =>
        (level, tag, time) match {
          case (1, "付费点击转化率" | "千次曝光收益", _) => "lofter.post_portrait_level1_raw"
          case _ => s"lofter.post_portrait_level$level"
        }
      }
    val jobStore = new HdfsJobStateStore("/user/da_lofter/warehouse/content_group", "/user/da_lofter/warehouse/content_group_extra")
    val jobManager = new GroupJobManager(userGroupQueryBuilder, jobStore, jobType = 1)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import com.netease.wm.group.platform.common.databases.getDDBConn
    implicit val conn: Connection = getDDBConn

    var errors: Int = 0

    implicit val formats: org.json4s.Formats = DefaultFormats
    import spark.implicits._

    val startTime = System.currentTimeMillis()
//    while (System.currentTimeMillis() - startTime < 500 * 1000) {
//      errors += jobManager.runJobs(dt, { warn =>
//        spark.createDataset(Seq(KafkaPush(warn.data.packageId.toString, write(warn))))
//          .withColumn("time", lit(System.currentTimeMillis()))
//          .selectExpr("key", """value""")
//          .write
//          .format("kafka")
//          .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
//          .option("topic", "LOFTER.cmb")
//          .save()
//      }, { (jobId, hdfsPath, jobType) =>
//        val destUrlPath = jobManager.jobStore.getJobExtraState(dt, jobId)
//        val maxUrlRows = 1000000
//        val queryPostUrls = s"""
//                               |select a.url as url, a.title as title, nvl(c.new_devices_1d,0) as new_devices_1d, nvl(c.new_devices_7d,0) as new_devices_7d
//                               |from lofter.dim_post a
//                               |     join job_detail b on a.id = b._c0
//                               |     left join (
//                               |         select content_id as postId, new_devices as new_devices_1d, new_devices_7d
//                               |         from lofter_dm.ads_growth_content_di
//                               |         where dt = '$dt' and content_type in ('图片', '文字', '视频')
//                               |     ) c on a.id = c.postId
//                               |limit $maxUrlRows
//                               |""".stripMargin
//
//        spark.read.csv(hdfsPath).createOrReplaceTempView("job_detail")
//        val df = spark.sql(queryPostUrls).repartition(1).cache()
//        val rows = df.count()
//        df.write.mode(SaveMode.Overwrite).option("header", false).option("delimiter", "\t").csv(destUrlPath)
//
//        // push to popo when content size no large 100 for growth package
//        if(jobType == 1 && rows < 100 && jobId == 54049 ) {
//          val posts = df.collect().map { row =>
//            val url = row.getAs[String]("url")
//            val title = row.getAs[String]("title")
//            val new1d = row.getAs[Long]("new_devices_1d")
//            val new7d = row.getAs[Long]("new_devices_7d")
//            GrowthPostDetail(url, title, new1d, new7d)
//          }
//          val messageBatch = posts.map { p =>
//            Seq(
//              "【新增潜力拉新内容】",
//              "内容标题：" + p.title,
//              "文章链接：https://" + p.url,
//              "近1天拉新量：" + p.new1d,
//              "近7日拉新量：" + p.new7d)
//          }.grouped(20)
//
//          messageBatch.foreach { rows =>
//            val message = rows.flatMap(identity).mkString("\n")
//            val popoMessage = PopoMessage(message, targetType = 1, targets = Seq("5695360"))
//            spark.createDataset(Seq(KafkaPush(key = jobId.toString, write(popoMessage))))
//              .selectExpr("key", """value""")
//              .write
//              .format("kafka")
//              .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
//              .option("topic", "LOFTER.CMBWEB.POPO")
//              .save()
//          }
//        }
//
//        // publish to backend kafka
//        if( jobId == 123) {
//          val jobPublishSql =
//            s"""
//               |select cast(_c0 as bigint) as postId,
//               |       $jobId as packId,
//               |       3 as expireDays,
//               |       0 as type
//               |from job_detail
//               |""".stripMargin
//
//          spark.sql(jobPublishSql)
//            .toJSON
//            .write
//            .format("kafka")
//            .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
//            .option("topic", "LOFTER.trade.private.traffic.post")
//            .save()
//        }
//
//        jobManager.jobStore.uploadNos(destUrlPath, rows.toInt)
//      },
//        { (jobResult: GroupJobResult) =>
//          val message = write(PackageCompleteNotify(jobResult.packId, createTime = System.currentTimeMillis()))
//          val push = KafkaPush(key = jobResult.packId.toString, value = message)
//
//          spark.createDataset(Seq(push))
//          .withColumn("time", lit(System.currentTimeMillis()))
//          .selectExpr("key", """value""")
//          .write
//          .format("kafka")
//          .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
//          .option("topic", "LOFTER.KOL.CONTENT_PACKAGE.SYNC")
//          .save()
//    })
//      Thread.sleep(1000)
//    }

    if (errors > 0) {
      throw new RuntimeException("execute content group jobs failed: " + errors)
    }

    spark.close()
  }
}
