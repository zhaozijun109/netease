package com.netease.vc.data.common

import com.netease.wm.util.Args
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.spark.sql.{Row, SparkSession}
import org.slf4j.LoggerFactory
import redis.clients.jedis.{HostAndPort, JedisCluster}

import java.util
import java.util.Calendar
import scala.collection.JavaConverters
import scala.util.matching.Regex

private class Hive2Redis{}

/**
 *  Hive2Redis export hive table to redis
 *  Attention! The key must be prefixed with 'vc_'.
 *    --source <query|table|file> : 1. use "spark.source.query" env var for long query param.
 *                                  2. Query and table source field is lower case
 *                                  To support upper case use parquet file source (mainly for migration)
 *    --cluster: redis cluster hosts, default redisConfig.REDIS_URL
 *    --parallel : number of parallel write threads
 *    --username: auth username
 *    --password: auth password
 *    --interval: 1s, 1m, 1h, 1d
 *
 *    TODO: Configurable in redis consumption, such as single thread, parallel thread
 */
object Hive2Redis {
  private val LOG = LoggerFactory.getLogger(classOf[Hive2Redis])
  private val TTL_TIME_PATTERN: Regex = """(?i)\b([1-9]\d*)\s*([smhd])\b""".r
  private val DATE_FORMAT_MAP = Map(
    "s" -> Calendar.SECOND,
    "m" -> Calendar.MINUTE,
    "h" -> Calendar.HOUR,
    "d" -> Calendar.DATE
  )

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val parallel = pargs.int("parallel", 10)
    val cluster = pargs.optional("cluster").orNull
    val redisHostAndPorts: util.Set[HostAndPort] = JavaConverters.setAsJavaSet(cluster.split(",").map(r => new HostAndPort(r.split(":")(0), r.split(":")(1).toInt)).toSet)
    val username = pargs.optional("username").orNull
    val password = pargs.optional("password").orNull

    val currentTimeMillis = System.currentTimeMillis()
    val (timeValue, timeUnits): (Option[Int], Option[String]) = if (pargs.optional("interval").isDefined) {
      pargs.optional("interval").getOrElse("").trim match {
        case TTL_TIME_PATTERN(value, units) => (Some(value.toInt), Some(units.toLowerCase))
        case _ => throw new Exception("Unsupported ttl format of hive2redis")
      }
    } else {
      (None, None)
    }

    val spark = SparkSession.builder()
      .appName("VC Hive2Redis Task")
      .enableHiveSupport()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val source = pargs.optional("source").orElse(spark.conf.getOption("spark.source.query")).getOrElse("")
    if (source.isEmpty) {
      System.err.println("source param is required, through --source or env var 'source.query'")
      System.exit(-1)
    }

    val df = spark.sql(source)
    val columnName: Array[String] = df.columns
    if (!columnName.contains("key") || !columnName.contains("value")) {
      System.err.println("'key' and 'value' column name is required in the source query, for example 'select id as key, user as value'")
      System.exit(-1)
    }

    df.repartition(parallel)
      .foreachPartition(
        (p: Iterator[Row]) => {
          val jedisCluster = createJedisClient(redisHostAndPorts, username, password)
          // Single thread
          // createSingleConsumer(p.toSeq, jedisCluster, currentTimeMillis, timeValue, timeUnits)

          // Parallel thread
          createParallelismConsumer(p.toSeq.par, jedisCluster, currentTimeMillis, timeValue, timeUnits)
          Thread.sleep(5000)
          jedisCluster.close()
        }
      )
    spark.stop()
  }

  private def createJedisClient(hostAndPortSet: util.Set[HostAndPort], username: String, password: String): JedisCluster = {
    val genericObjectPoolConfig = new GenericObjectPoolConfig()
    genericObjectPoolConfig.setMaxTotal(30)
    genericObjectPoolConfig.setMaxIdle(30)
    genericObjectPoolConfig.setMinIdle(1)
    new JedisCluster(hostAndPortSet, 10000, 10000, 3, username, password, null, genericObjectPoolConfig)
  }

  private def setKeyExpireTime(key: String, currentTimeMillis: Long, timeValue: Int, timeUnits: String, jedisCluster:JedisCluster): Unit = {
    try {
      val calendarInstance: Calendar = Calendar.getInstance
      calendarInstance.setTimeInMillis(currentTimeMillis)
      calendarInstance.add(DATE_FORMAT_MAP(timeUnits), timeValue)
      jedisCluster.pexpireAt(key, calendarInstance.getTimeInMillis)
    } catch {
      case e: Exception =>
        jedisCluster.del(key)
        throw new Exception("Unsupported time format of redis expire time", e)
    }
  }

  /**
   * Single thread consumer.
   * @param data
   * @param jedisCluster
   * @param currentTimeMillis
   * @param timeValue
   * @param timeUnits
   */
  private def createSingleConsumer(data: Seq[Row], jedisCluster: JedisCluster, currentTimeMillis: Long,
                                   timeValue: Option[Int], timeUnits: Option[String]): Unit = {
    data.foreach {
      row => {
        val key = if (row.getAs("key") == null) "vc_null" else row.getAs("key").toString
        val value = if (row.getAs("value") == null) "vc_null" else row.getAs("value").toString
        if (!key.startsWith("vc_")) {
          throw new Exception("The key must be prefixed with 'vc_', for example 'vc_key'")
        }
        jedisCluster.set(key, value)
        (timeValue, timeUnits) match {
          case (Some(tv), Some(tu)) => setKeyExpireTime(key, currentTimeMillis, tv, tu, jedisCluster)
          case _ =>
        }
      }
    }
  }

  /**
   * Parallel thread consumer.
   * @param parData
   * @param jedisCluster
   * @param currentTimeMillis
   * @param timeValue
   * @param timeUnits
   */
  private def createParallelismConsumer(parData: scala.collection.parallel.ParSeq[Row], jedisCluster: JedisCluster, currentTimeMillis: Long,
                                        timeValue: Option[Int], timeUnits: Option[String]): Unit = {
    val threadPool = new collection.parallel.ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(30))
    try {
      parData.tasksupport = threadPool
      parData.foreach(
        row => {
          val key = if (row.getAs("key") == null) "vc_null" else row.getAs("key").toString
          val value = if (row.getAs("value") == null) "vc_null" else row.getAs("value").toString
          if (!key.startsWith("vc_")) {
            throw new Exception("The key must be prefixed with 'vc_', for example 'vc_key'")
          }
          jedisCluster.set(key, value)
          (timeValue, timeUnits) match {
            case (Some(tv), Some(tu)) => setKeyExpireTime(key, currentTimeMillis, tv, tu, jedisCluster)
            case _ =>
          }
        }
      )
    } finally {
      threadPool.environment.shutdown()
    }
  }
}
