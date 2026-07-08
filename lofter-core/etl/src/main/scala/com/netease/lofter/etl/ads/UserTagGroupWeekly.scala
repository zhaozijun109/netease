package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTagGroupWeekly {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .appName("User Login Info Extract")
      .getOrCreate()

    val dt = pargs.required("date")

    if(DateTime.parse(dt).getDayOfWeek != 5) {
      spark.close()
      return
    }

    val partitionDt = DateTime.parse(dt).plusDays(1).toString("yyyy-MM-dd")

    val threeMonthAgo = DateTime.parse(dt).minusDays(90).toString("yyyy-MM-dd")
    val halfYearAgo = DateTime.parse(dt).minusDays(180).toString("yyyy-MM-dd")

    spark.sqlContext.udf.register("expand_category", (categories: Seq[String]) => {
      if(categories != null && categories.size >= 3) {
        val c1 = categories(0)
        val c2 = categories(1)
        val c3 = categories(2)
        Seq(c1, s"$c1-$c2", s"$c1-$c2-$c3")
      } else null
    })

    spark.sql("create temporary function to_bitmap as 'com.netease.wm.udf.bitmap.ToBitmapUDAF'")
    spark.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF'")
    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")

    val categoryGroup =
      s"""
         |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_wd partition(dt='$partitionDt', tag='category_consume')
         |select userId, cat, subcat, grp
         |from (
         |    select userId,
         |           'DAY_90' as cat,
         |           case when pc < 10 then 'COUNT_1_10'
         |                when pc < 100 then 'COUNT_10_100'
         |                when pc < 500 then 'COUNT_100_500'
         |                when pc < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           post_category as grp, pc
         |    from (
         |        select category as post_category, userId,
         |               bitmap_count(bitmap_union(post_bitmap)) as pc
         |        from lofter.dws_category_user_consume_di
         |        where dt <= '$dt' and dt > '$halfYearAgo'
         |        group by category, userId
         |    ) tt
         |
         |    union all
         |
         |    select userId,
         |           'DAY_180' as cat,
         |           case when pc < 10 then 'COUNT_1_10'
         |                when pc < 100 then 'COUNT_10_100'
         |                when pc < 500 then 'COUNT_100_500'
         |                when pc < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           post_category as grp, pc
         |    from (
         |        select category as post_category, userId,
         |               bitmap_count(bitmap_union(post_bitmap)) as pc
         |        from lofter.dws_category_user_consume_di
         |        where dt <= '$dt' and dt > '$halfYearAgo'
         |        group by category, userId
         |    ) tt
         |) t
         |where userId > 0
         |""".stripMargin

    val ipGroup =
      s"""
         |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_wd partition(dt='$partitionDt', tag='ip_consume')
         |select userId, cat, subcat, grp
         |from (
         |    select userId,
         |           'DAY_90' as cat,
         |           case when pc < 10 then 'COUNT_1_10'
         |                when pc < 100 then 'COUNT_10_100'
         |                when pc < 500 then 'COUNT_100_500'
         |                when pc < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           ip as grp, pc
         |    from (
         |        select ip, userId, bitmap_count(bitmap_union(post_bitmap)) as pc
         |        from lofter.dws_ip_user_consume_di
         |        where dt <= '$dt' and dt > '$threeMonthAgo'
         |        group by ip, userId
         |    ) tt
         |
         |    union all
         |
         |    select userId,
         |           'DAY_180' as cat,
         |           case when pc < 10 then 'COUNT_1_10'
         |                when pc < 100 then 'COUNT_10_100'
         |                when pc < 500 then 'COUNT_100_500'
         |                when pc < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           ip as grp, pc as pc
         |    from (
         |        select ip, userId, bitmap_count(bitmap_union(post_bitmap)) as pc
         |        from lofter.dws_ip_user_consume_di
         |        where dt <= '$dt' and dt > '$halfYearAgo'
         |        group by ip, userId
         |    ) tt
         |
         |) t
         |where userId > 0
         |""".stripMargin

    val tagGroup =
      s"""
         |insert overwrite table lofter_dm.ads_par_user_tag_group_level3_wd partition(dt='$partitionDt', tag='tag_consume')
         |select userId, cat, subcat, grp
         |from (
         |    select userId,
         |           'DAY_90' as cat,
         |           case when post_count < 10 then 'COUNT_1_10'
         |                when post_count < 100 then 'COUNT_10_100'
         |                when post_count < 500 then 'COUNT_100_500'
         |                when post_count < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           tag as grp, post_count as pc
         |   from (
         |        select tag, userId, bitmap_count(bitmap_union(post_bitmap)) as post_count
         |        from lofter.dws_tag_user_consume_di
         |        where dt <= '$dt' and dt > '$threeMonthAgo'
         |        group by tag, userId
         |    ) tt
         |    where post_count > 3
         |
         |    union all
         |
         |    select userId,
         |           'DAY_180' as cat,
         |           case when post_count < 10 then 'COUNT_1_10'
         |                when post_count < 100 then 'COUNT_10_100'
         |                when post_count < 500 then 'COUNT_100_500'
         |                when post_count < 1000 then 'COUNT_500_1k'
         |                else 'COUNT_1k' end as subcat,
         |           tag as grp, post_count as pc
         |    from (
         |        select tag, userId, bitmap_count(bitmap_union(post_bitmap)) as post_count
         |        from lofter.dws_tag_user_consume_di
         |        where dt <= '$dt' and dt > '$halfYearAgo'
         |        group by tag, userId
         |    ) tt
         |    where post_count > 3
         |) t
         |where userId > 0
         |""".stripMargin

    spark.sql(tagGroup)
    spark.sql(categoryGroup)
    spark.sql(ipGroup)
    spark.close()

  }
}
