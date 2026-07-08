package com.netease.wm

import org.apache.spark.sql.{SparkSession, SparkSessionExtensions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SparkSessionInitializer extends (SparkSessionExtensions => Unit){

    Future {
      val session = SparkSession.builder().getOrCreate()

      session.sql("create temporary function decode_url as 'com.netease.wm.udf.DecodeUrl'")
      session.sql("create temporary function gcd as 'com.netease.wm.udf.Gcd'")
      session.sql("create temporary function html_images as 'com.netease.wm.udf.HtmlImages'")
      session.sql("create temporary function html_links as 'com.netease.wm.udf.HtmlLinks'")
      session.sql("create temporary function html_tag_attr as 'com.netease.wm.udf.HtmlTagAttr'")
      session.sql("create temporary function html_text as 'com.netease.wm.udf.HtmlText'")
      session.sql("create temporary function html_text_length as 'com.netease.wm.udf.HtmlTextLength'")
      session.sql("create temporary function html_paragraph as 'com.netease.wm.udf.HtmlParagraph'")
      session.sql("create temporary function parse_array as 'com.netease.wm.udf.ParseArrayJson'")
      session.sql("create temporary function regex_count as 'com.netease.wm.udf.RegexCount'")
      session.sql("create temporary function regex_links as 'com.netease.wm.udf.RegexLinks'")
      session.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")
      session.sql("create temporary function img2vec as 'com.netease.wm.udf.Img2Vec'")
      session.sql("create temporary function version_compare as 'com.netease.wm.udf.VersionCompare'")
      session.sql("create temporary function version_compress as 'com.netease.wm.udf.VersionCompress'")
      session.sql("create temporary function nos_users as 'com.netease.wm.udf.NosUserList'")
      session.sql("create temporary function max_score_item as 'com.netease.wm.udf.MaxScoreItem'")
      session.sql("create temporary function resolve_tag_ip as 'com.netease.wm.udf.ResolveTagIp'")
      session.sql("create temporary function expand_post_category as 'com.netease.wm.udf.ExpandPostCategory'")
      session.sql("create temporary function jaccard_similarity as 'com.netease.wm.udf.JaccardSimilarity'")
      session.sql("create temporary function resolve_mda_action_type as 'com.netease.wm.udf.ResolveMdaActionType'")
      session.sql("create temporary function bitmap_build as 'com.netease.wm.udf.bitmap.BitmapBuildUDF'")
      session.sql("create temporary function to_bitmap as 'com.netease.wm.udf.bitmap.ToBitmapUDAF'")
      session.sql("create temporary function bitmap_to_array as 'com.netease.wm.udf.bitmap.BitmapToArrayUDF'")
      session.sql("create temporary function bitmap_and as 'com.netease.wm.udf.bitmap.BitmapAndUDF'")
      session.sql("create temporary function bitmap_or as 'com.netease.wm.udf.bitmap.BitmapOrUDF'")
      session.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")
      session.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF'")
      session.sql("create temporary function bitmap_xor as 'com.netease.wm.udf.bitmap.BitmapXorUDF'")
      session.sql("create temporary function short_link_code2id as 'com.netease.wm.udf.ShortLinkCode2Actpwd'")
      session.sql("create temporary function parse_dialogue_content as 'com.netease.wm.udf.ParseDialogueContent'")

      println("wm udx was registered")
    }

  override def apply(v1: SparkSessionExtensions): Unit = {}
}
