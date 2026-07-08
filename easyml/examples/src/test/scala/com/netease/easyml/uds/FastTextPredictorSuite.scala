package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{FastTextPredictorArgs, FastTextPredictorUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/11.
 */
class FastTextPredictorSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"
  val inputCol = "text"

  val model = "/Users/linjiuning/workspace/git/netease/py_scripts/msg/data/sample/test/train_0/model.bin"

  def dataset(): DataFrame = {
    import spark.implicits._
    val rawInput = spark.sparkContext
      .parallelize(Seq(
        "064705是您的Wish验证码。",
        "哇！你的宝藏歌单《极致温柔》又被更多人播放了，粉丝们都在喊你回来更新歌单啦>>退订TD",
        "验证码：241221，网易用户，您正在使用手机帐号登入服务[验证码告知他人将导致帐号被盗，请勿泄露]",
        "一杯奶茶钱开启第二副业，5天淘宝电商训练营来啦，千万级店铺导师手把手带班，5月22日正式开营，点击占座退订回复N",
        "你的4.00元优惠券将于2019-08-0411:37过期，适用范围为：C语言不挂科-4小时学完C语言，请尽快使用。",
        "你参加的课程《网易Java系列高级直播课（9月）》即将在2020年09月26日20:00开始直播-抛弃Session，用JWT的注意事项，快来围观->退订回复N"
      ))
      .toDF(inputCol)

    rawInput
  }

  test("fasttext") {
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = FastTextPredictorArgs(input, output, inputCol, model)
    FastTextPredictorUDS.run(spark, args)
  }
}
