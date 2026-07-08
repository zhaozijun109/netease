package com.netease.lofter.etl.dwd

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserSensitiveWordDetectStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter User Sensitive Word Detect Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val history_start_date = "2022-06-14"
    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    // deal with his words and his users in T0 moment
    val sql_words_his =
      s"""
         |select concat_ws('|',COLLECT_SET(keyword)) as keyWords from
         |(select type,keyword,length(keyword) as word_size from lofter.zq_lofter_fanlaji_black_keyword
         |where type='用户' and from_unixtime(createTime,'yyyy-MM-dd')='$history_start_date'
         |order by word_size desc) a
         |""".stripMargin
    val his_words = spark.sql(sql_words_his).head().getString(0)
    val hisPornReg = s"""(?i)($his_words)""".r
    def findHisPornWords(input: String): Seq[String] = if(input == null) Seq.empty else hisPornReg.findAllIn(input.replaceAll("""(?U)[\p{Punct}\s]+""", "")).toSeq
    spark.sqlContext.udf.register("hisPornWords", (input: String) => findHisPornWords(input))

    val sql_blog_nick_name_a1b1 =
      s"""
         |insert overwrite table lofter.dwd_blog_nickname_sensitive_word_di partition(dt='$history_start_date')
         |select blogId, sensitive_word from
         |(select blogId,BlogNickName from lofter_db_dump.ods_db_blog_info_nd
         |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')<='$history_start_date') a
         |lateral view explode(hisPornWords(BlogNickName)) tt as sensitive_word
         |""".stripMargin

    val sql_blog_intro_a1b1 =
      s"""
         |insert overwrite table lofter.dwd_blog_intro_sensitive_word_di partition(dt='$history_start_date')
         |select blogId, sensitive_word from
         |(select blogId,SelfIntro from lofter_db_dump.ods_db_blog_info_nd
         |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')<='$history_start_date') a
         |lateral view explode(hisPornWords(SelfIntro)) tt as sensitive_word
         |""".stripMargin

    // after deal with the his, deal with the new porn words today for all users
    val sql_words_new =
      s"""
         |select concat_ws('|',COLLECT_LIST(keyword)) as keyWords from
         |(select type,keyword,length(keyword) as word_size from lofter.zq_lofter_fanlaji_black_keyword
         |where type='用户' and from_unixtime(createTime,'yyyy-MM-dd')='$date'
         |order by word_size desc) a
         |""".stripMargin

    val new_words = spark.sql(sql_words_new).head().getString(0)
    // pay attention that new_words is not allowed to null or empty, should judge it
    if (new_words.isEmpty) {
      println(s"there is no new sensitive_word on $date")
    } else {
      val newPornReg = s"""(?i)($new_words)""".r
      def findNewPornWords(input: String): Seq[String] = if(input == null) Seq.empty else newPornReg.findAllIn(input.replaceAll("""(?U)[\p{Punct}\s]+""", "")).toSeq
      spark.sqlContext.udf.register("newPornWords", (input: String) => findNewPornWords(input))

      val sql_blog_nick_name_a1b2 =
        s"""
           |insert overwrite table lofter.dwd_blog_nickname_sensitive_word_di partition(dt='$date')
           |select blogId, sensitive_word from
           |(select blogId,BlogNickName from lofter_db_dump.ods_db_blog_info_nd
           |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')<='$date') a
           |lateral view explode(newPornWords(BlogNickName)) tt as sensitive_word
           |""".stripMargin

      val sql_blog_intro_a1b2 =
        s"""
           |insert overwrite table lofter.dwd_blog_intro_sensitive_word_di partition(dt='$date')
           |select blogId, sensitive_word from
           |(select blogId,SelfIntro from lofter_db_dump.ods_db_blog_info_nd
           |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')<='$date') a
           |lateral view explode(newPornWords(SelfIntro)) tt as sensitive_word
           |""".stripMargin

      spark.sql(sql_blog_nick_name_a1b2)
      spark.sql(sql_blog_intro_a1b2)
    }

    // after deal with the new porn word, deal with the new user today
    val sql_words_yesterday =
      s"""
         |select concat_ws('|',COLLECT_LIST(keyword)) as keyWords from
         |(select type,keyword,length(keyword) as word_size from lofter.zq_lofter_fanlaji_black_keyword
         |where type='用户' and from_unixtime(createTime,'yyyy-MM-dd')<'$date'
         |order by word_size desc) a
         |""".stripMargin

    val yes_words = spark.sql(sql_words_yesterday).head().getString(0)

    if (yes_words.isEmpty) {
      println(s"there is no sensitive_word ")
    } else {
      val yesPornReg = s"""(?i)($yes_words)""".r
      def findYesPornWords(input: String): Seq[String] = if(input == null) Seq.empty else yesPornReg.findAllIn(input.replaceAll("""(?U)[\p{Punct}\s]+""", "")).toSeq
      spark.sqlContext.udf.register("yesPornWords", (input: String) => findYesPornWords(input))

      val sql_blog_nick_name_a2b1 =
        s"""
           |insert into lofter.dwd_blog_nickname_sensitive_word_di partition(dt='$date')
           |select blogId, sensitive_word from
           |(select blogId,BlogNickName from lofter_db_dump.ods_db_blog_info_nd
           |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')='$date') a
           |lateral view explode(yesPornWords(BlogNickName)) tt as sensitive_word
           |""".stripMargin

      val sql_blog_intro_b2b1 =
        s"""
           |insert into lofter.dwd_blog_intro_sensitive_word_di partition(dt='$date')
           |select blogId, sensitive_word from
           |(select blogId,SelfIntro from lofter_db_dump.ods_db_blog_info_nd
           |where from_unixtime(cast(blogCreateTime/1000 as bigint),'yyyy-MM-dd')='$date') a
           |lateral view explode(yesPornWords(SelfIntro)) tt as sensitive_word
           |""".stripMargin

      spark.sql(sql_blog_nick_name_a2b1)
      spark.sql(sql_blog_intro_b2b1)
    }

    spark.stop()
  }

}
