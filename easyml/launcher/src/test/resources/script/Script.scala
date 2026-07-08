val df = spark.sparkContext.makeRDD(Seq(1, 2, 3, 4)).map(_ + 1).toDF("id")
df.show(false)