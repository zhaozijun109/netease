//package com.netease.easyml.uds
//
//import org.apache.spark.sql.{DataFrame, SparkSession}
//import org.scalatest.FunSuite
//
///**
// * Created by linjiuning on 2020/12/31.
// */
//class RedisUDSSuite extends FunSuite {
//  lazy val spark: SparkSession = SparkSession.builder().master("local[4]")
//    //    .config("spark.redis.host", host)
//    //    .config("spark.redis.port", port)
//    //    .config("spark.redis.auth", auth)
//    //    .config("spark.redis.timeout", timeout)
//    .getOrCreate()
//
//  val table = "input"
//
//  val keyCol = "key"
//  val valueCol = "value"
//  val numPartition = "null"
//  val prefix = "test:"
//
//  val host = "10.130.64.52"
//  val port = "6379"
//  val auth = "e3F4rs"
//  val timeout = "null"
//
//  val ttl = "0.5m"
//
//  def datasetKV(): DataFrame = {
//    import spark.implicits._
//    spark.sparkContext.parallelize(Seq(
//      ("p1", "a"),
//      ("p2", "c")
//    )).toDF(keyCol, valueCol)
//  }
//
//  def datasetHash(): DataFrame = {
//    import spark.implicits._
//    spark.sparkContext.parallelize(Seq(
//      ("p1", Map("a" -> 1, "b" -> 2)),
//      ("p2", Map("c" -> 3, "d" -> 4))
//    )).toDF(keyCol, valueCol)
//  }
//
//  def datasetHashJson(): DataFrame = {
//    import spark.implicits._
//    spark.sparkContext.parallelize(Seq(
//      ("p1", "{\"a\": 1, \"b\": 2}"),
//      ("p2", "{\"c\": 3, \"d\": 5}")
//    )).toDF(keyCol, valueCol)
//  }
//
//  test("kv") {
//    val dataType = "kv"
//    val ds = datasetKV()
//    ds.createOrReplaceTempView(table)
//    val args = Array(table, keyCol, valueCol, numPartition, dataType, prefix, "null", ttl).map(_.toString)
//    RedisUDS.run(spark, args)
//  }
//
//  test("kv_hash") {
//    val dataType = "kv"
//    val hashName = "my"
//    val ds = datasetKV()
//    ds.createOrReplaceTempView(table)
//    val args = Array(table, keyCol, valueCol, numPartition, dataType, prefix, hashName, ttl).map(_.toString)
//    RedisUDS.run(spark, args)
//  }
//
//  test("hash") {
//    val dataType = "hash"
//    val ds = datasetHash()
//    ds.createOrReplaceTempView(table)
//    val args = Array(table, keyCol, valueCol, numPartition, dataType, prefix, "null", ttl).map(_.toString)
//    RedisUDS.run(spark, args)
//  }
//
//  test("hash_json") {
//    val dataType = "hash"
//    val ds = datasetHashJson()
//    ds.createOrReplaceTempView(table)
//    val args = Array(table, keyCol, valueCol, numPartition, dataType, prefix, "null", ttl).map(_.toString)
//    RedisUDS.run(spark, args)
//  }
//}
