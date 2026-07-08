package com.netease.lofter.data.jobs.backend

import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import com.huaban.analysis.jieba.SegToken
import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.ml.feature.{CountVectorizerModel, MinHashLSHModel, RegexTokenizer}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.joda.time.DateTime

import java.net.URI

object GiftPostInductionHourly {
  val stopWords: Set[String] = Set("图片","【", "】", "（", "）","$","0","1","2","3","4","5","6","7","8","9","?","_","“","”","、","。","《","》","一","二","一些","一何","一切","一则","一方面","一旦","一来","一样","一般","一转眼","万一","上","上下","下","不","不仅","不但","不光","不单","不只","不外乎","不如","不妨","不尽","不尽然","不得","不怕","不惟","不成","不拘","不料","不是","不比","不然","不特","不独","不管","不至于","不若","不论","不过","不问","与","与其","与其说","与否","与此同时","且","且不说","且说","两者","个","个别","临","为","为了","为什么","为何","为止","为此","为着","乃","乃至","乃至于","么","之","之一","之所以","之类","乌乎","乎","乘","也","也好","也罢","了","二来","于","于是","于是乎","云云","云尔","些","亦","人","人们","人家","什么","什么样","今","介于","仍","仍旧","从","从此","从而","他","他人","他们","以","以上","以为","以便","以免","以及","以故","以期","以来","以至","以至于","以致","们","任","任何","任凭","似的","但","但凡","但是","何","何以","何况","何处","何时","余外","作为","你","你们","使","使得","例如","依","依据","依照","便于","俺","俺们","倘","倘使","倘或","倘然","倘若","借","假使","假如","假若","傥然","像","儿","先不先","光是","全体","全部","兮","关于","其","其一","其中","其二","其他","其余","其它","其次","具体地说","具体说来","兼之","内","再","再其次","再则","再有","再者","再者说","再说","冒","冲","况且","几","几时","凡","凡是","凭","凭借","出于","出来","分别","则","则甚","别","别人","别处","别是","别的","别管","别说","到","前后","前此","前者","加之","加以","即","即令","即使","即便","即如","即或","即若","却","去","又","又及","及","及其","及至","反之","反而","反过来","反过来说","受到","另","另一方面","另外","另悉","只","只当","只怕","只是","只有","只消","只要","只限","叫","叮咚","可","可以","可是","可见","各","各个","各位","各种","各自","同","同时","后","后者","向","向使","向着","吓","吗","否则","吧","吧哒","吱","呀","呃","呕","呗","呜","呜呼","呢","呵","呵呵","呸","呼哧","咋","和","咚","咦","咧","咱","咱们","咳","哇","哈","哈哈","哉","哎","哎呀","哎哟","哗","哟","哦","哩","哪","哪个","哪些","哪儿","哪天","哪年","哪怕","哪样","哪边","哪里","哼","哼唷","唉","唯有","啊","啐","啥","啦","啪达","啷当","喂","喏","喔唷","喽","嗡","嗡嗡","嗬","嗯","嗳","嘎","嘎登","嘘","嘛","嘻","嘿","嘿嘿","因","因为","因了","因此","因着","因而","固然","在","在下","在于","地","基于","处在","多","多么","多少","大","大家","她","她们","好","如","如上","如上所述","如下","如何","如其","如同","如是","如果","如此","如若","始而","孰料","孰知","宁","宁可","宁愿","宁肯","它","它们","对","对于","对待","对方","对比","将","小","尔","尔后","尔尔","尚且","就","就是","就是了","就是说","就算","就要","尽","尽管","尽管如此","岂但","己","已","已矣","巴","巴巴","并","并且","并非","庶乎","庶几","开外","开始","归","归齐","当","当地","当然","当着","彼","彼时","彼此","往","待","很","得","得了","怎","怎么","怎么办","怎么样","怎奈","怎样","总之","总的来看","总的来说","总的说来","总而言之","恰恰相反","您","惟其","慢说","我","我们","或","或则","或是","或曰","或者","截至","所","所以","所在","所幸","所有","才","才能","打","打从","把","抑或","拿","按","按照","换句话说","换言之","据","据此","接着","故","故此","故而","旁人","无","无宁","无论","既","既往","既是","既然","时候","是","是以","是的","曾","替","替代","最","有","有些","有关","有及","有时","有的","望","朝","朝着","本","本人","本地","本着","本身","来","来着","来自","来说","极了","果然","果真","某","某个","某些","某某","根据","欤","正值","正如","正巧","正是","此","此地","此处","此外","此时","此次","此间","毋宁","每","每当","比","比及","比如","比方","没奈何","沿","沿着","漫说","焉","然则","然后","然而","照","照着","犹且","犹自","甚且","甚么","甚或","甚而","甚至","甚至于","用","用来","由","由于","由是","由此","由此可见","的","的确","的话","直到","相对而言","省得","看","眨眼","着","着呢","矣","矣乎","矣哉","离","竟而","第","等","等到","等等","简言之","管","类如","紧接着","纵","纵令","纵使","纵然","经","经过","结果","给","继之","继后","继而","综上所述","罢了","者","而","而且","而况","而后","而外","而已","而是","而言","能","能否","腾","自","自个儿","自从","自各儿","自后","自家","自己","自打","自身","至","至于","至今","至若","致","般的","若","若夫","若是","若果 ","若非","莫不然","莫如","莫若","虽","虽则","虽然","虽说","被","要","要不","要不是","要不然","要么","要是","譬喻","譬如","让","许多","论","设使","设或","设若","诚如","诚然","该","说来","诸","诸位","诸如","谁","谁人","谁料","谁知","贼死","赖以","赶","起","起见","趁","趁着","越是","距","跟","较","较之","边","过","还","还是","还有","还要","这","这一来","这个","这么","这么些","这么样","这么点儿","这些","这会儿","这儿","这就是说","这时","这样","这次","这般","这边","这里","进而","连","连同","逐步","通过","遵循","遵照","那","那个","那么","那么些","那么样","那些","那会儿","那儿","那时","那样","那般","那边","那里","都","鄙人","鉴于","针对","阿","除","除了","除外","除开","除此之外","除非","随","随后","随时","随着","难道说","非但","非徒","非特","非独","靠","顺","顺着","首先","！","，","：","；","？")

  def isNotEmptyVector(vector: org.apache.spark.ml.linalg.Vector): Boolean = {
    vector.numNonzeros > 0
  }

  lazy val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/"))

  def getLatest(path: String): String = {
    val date = DateTime.now().minusDays(1).toString("yyyy-MM-dd")
    val dayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")

    val candidate1 = s"$path/dt=$date"
    val candidate2 = s"$path/dt=$dayAgo"
    if (fc.util().exists(new Path(candidate1))) {
      candidate1
    } else candidate2
  }


  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val dayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")
    val hour = DateTime.now().minusHours(1).toString("HH")

    spark.sql("create temporary function html_text as 'com.netease.wm.udf.HtmlText'")

    val segment = udf { sentence: String =>
      if(sentence == null) {
        null
      } else {
        val localJieba = Jieba.segmenter
        val words = localJieba.process(sentence.replaceAll("\\n", ""), SegMode.INDEX)
          .toArray().map(_.asInstanceOf[SegToken].word)
          .filter(s => !stopWords(s) && s.trim.nonEmpty)
        if(words.length > 2) words.mkString(",") else null
      }
    }

    val isNoneZeroVector = udf[Boolean, org.apache.spark.ml.linalg.Vector](isNotEmptyVector)

    val candidatePosts =
      s"""
         |select *
         |from (
         |    select a.id, a.blogId, a.digest, a.publishTime
         |    from (
         |        select id, html_text(digest) as digest, blogId, publishTime
         |        from (
         |            select *, row_number() over (partition by id order by _bin_op_time desc, _bin_op_seqno desc) as rk
         |            from lofter.ods_binlog_post_di
         |            where dt = '$date'
         |        ) t
         |        where rk = 1 and _bin_op in (0, 2) and
         |              type = 1 and valid = 0 and allowview = 0 and
         |    	         isPublished = 1 and citeRootPostid = 0 and
         |    		       from_unixtime(cast(publishTime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$date' and
         |               from_unixtime(cast(publishTime / 1000 AS BIGINT), 'HH') >= '$hour'
         |    ) a
         |    left join (
         |        select postId
         |        from lofter.dim_gift_post_return_dd
         |        where dt='$date'
         |        group by 1
         |    ) b on a.id = b.postId
         |    left join (
         |        select blogId from lofter.dim_miniprogram_post_dd where dt='$date' group by 1
         |    ) d on a.blogId = d.blogId
         |    left join (
         |        select blogId
         |        from lofter.dim_gift_post_return_dd
         |        where dt = '$date' and blog_channel <> 0
         |        group by 1
         |    ) e on a.blogId = e.blogId
         |    where b.postId is null and d.blogId is null and e.blogId is null
         |) t
         |where length(digest) >= 100
         |""".stripMargin

    val candidateSentences = spark.sql(candidatePosts).cache()
      .withColumnRenamed("digest", "sentence")
      .withColumn("segment", segment(col("sentence")))
      .filter("length(segment) > 0 ")
      .repartition(100)

    val regTokenizer = new RegexTokenizer()
      .setInputCol("segment")
      .setOutputCol("words")
      .setPattern(",")

    val regTokenized = regTokenizer.transform(candidateSentences)

    val cvModel = CountVectorizerModel.load(getLatest("/user/da_lofter/warehouse/gift_post_similarity_cv"))

    val candidatePostRawFeatures = cvModel.transform(regTokenized)
      .withColumn("featuresNotEmpty", isNoneZeroVector(col("features")))
      .filter("featuresNotEmpty")

    val model = MinHashLSHModel.load(getLatest("/user/da_lofter/warehouse/gift_post_similarity_model"))
    val giftPostFeatures: Dataset[Row] = spark.read.parquet(getLatest("/user/da_lofter/warehouse/gift_post_similarity_base_posts"))
    val candidateFeature: Dataset[Row] = model.transform(candidatePostRawFeatures)

    val threshold = 0.8
    val distThreshold = 0.3

    val simiDistance = model.approxSimilarityJoin(candidateFeature, giftPostFeatures, threshold)
      .select(col("datasetA.id").alias("postId"),
        col("datasetA.blogId").alias(s"blogId"),
        col("datasetA.sentence").alias(s"post_digest"),
        col("datasetB.id").alias("origin_post_id"),
        col("datasetB.blogId").alias(s"origin_blogId"),
        col("datasetB.sentence").alias(s"origin_post_digest"),
        col("distCol")
      ).where(s"datasetA.blogId != datasetB.blogId and datasetA.publishTime > datasetB.publishTime and distCol < $distThreshold")

    simiDistance.createOrReplaceTempView("result")

    spark.sql(s"insert overwrite table lofter_dm.ads_gift_post_induction_hi partition (dt = '$date', hour = $hour) select * from result")
  }
}
