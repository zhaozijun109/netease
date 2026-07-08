//package com.netease.easyml.uds.examples
//
//import com.alibaba.fastjson.JSON
//import com.netease.easyml.common.util.SparkUtil
//import com.redislabs.provider.redis._
//import org.apache.spark.internal.Logging
//import org.apache.spark.sql.SparkSession
//
//import scala.collection.JavaConverters._
//
///**
// * Created by linjiuning on 2020/12/31.
// * Write to redis.
// *
// * params:
// * table: input table if read is false else output table
// * keyCol: col name of key/hash
// * valueCol: col name of value
// * numPartition: control parallel
// * dataType: kv or hash, default kv
// * keyPrefix: key prefix
// * hashName: default null
// * ttl: ttl (in seconds)
// */
//object RedisUDS extends Serializable with Logging {
//
//  def run(spark: SparkSession, args: Array[String]): Unit = {
//    val Array(table, keyCol, valueCol, numPartition, dataType, keyPrefix, hashName, ttl) = args
//
//    val conf = spark.sparkContext.getConf
//
//    val numPartition_ = if (numPartition.equals("null")) {
//      SparkUtil.getDefaultParallelism(conf)
//    } else {
//      numPartition.toInt
//    }
//
//    val ttl_ = if (ttl.equals("null")) {
//      0
//    } else {
//      val ttl_ = if (ttl.endsWith("d")) {
//        ttl.slice(0, ttl.length - 1).toDouble * 24 * 60 * 60
//      } else if (ttl.endsWith("h")) {
//        ttl.slice(0, ttl.length - 1).toDouble * 60 * 60
//      } else if (ttl.endsWith("m")) {
//        ttl.slice(0, ttl.length - 1).toDouble * 60
//      } else if (ttl.endsWith("s")) {
//        ttl.slice(0, ttl.length - 1).toDouble
//      } else {
//        ttl.toDouble
//      }
//      ttl_.toInt
//    }
//
//    println(s"TTL: ${ttl_}s")
//
//    val prefix = if (keyPrefix.equals("null")) {
//      ""
//    } else {
//      keyPrefix
//    }
//
//    val sc = spark.sparkContext
//    val rdd = spark.sql(s"select $keyCol, $valueCol from $table").rdd
//    if (dataType.equals("kv")) {
//      val kvs = rdd.map(row => {
//        val key = row.getString(0)
//        val value = row.getString(1)
//        (prefix + key, value)
//      }).coalesce(numPartition_)
//      if (hashName.equals("null")) {
//        sc.toRedisKV(kvs, ttl_)
//      } else {
//        sc.toRedisHASH(kvs, hashName, ttl_)
//      }
//    } else {
//      val kvs = rdd.map(row => {
//        val hash = prefix + row.getString(0)
//        val kvs = row.get(1) match {
//          case string: String =>
//            var map = Map.empty[String, String]
//            val obj = JSON.parseObject(string)
//            obj.entrySet().asScala.foreach(it => {
//              map += (it.getKey -> it.getValue.toString)
//            })
//            map
//          case map =>
//            map.asInstanceOf[Map[String, _]].mapValues(_.toString)
//        }
//        (hash, kvs)
//      }).coalesce(numPartition_)
//      sc.toRedisHASHes(kvs, ttl_)
//    }
//  }
//}
