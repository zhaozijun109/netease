package com.netease.easyml.common.util

import com.linkedin.spark.datasources.tfrecordv2.TFRecordSerializer
import org.apache.http.conn.util.InetAddressUtils
import org.apache.spark.internal.Logging
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql._
import org.apache.spark.sql.types.{BinaryType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkEnv}

import java.net.InetAddress
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/6/23.
 */
object SparkUtil extends Logging {
  val COMPRESS_MODE = "org.apache.hadoop.io.compress.GzipCodec"
  val NULL = "null"
  val SQL_FILE = ".sql"
  val SQL_SPLIT = ";"

  def getSparkClassLoader: ClassLoader = getClass.getClassLoader

  def getContextOrSparkClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getSparkClassLoader)

  // scalastyle:off classforname

  /** Preferred alternative to Class.forName(className) */
  def classForName(className: String): Class[_] = {
    Class.forName(className, true, getContextOrSparkClassLoader)
    // scalastyle:on classforname
  }

  def getNumExecutors(conf: SparkConf): Int = {
    if (conf.getBoolean("spark.dynamicAllocation.enabled", false))
      conf.getInt("spark.dynamicAllocation.maxExecutors", 10)
    else {
      var executors = conf.getInt("spark.executor.instances", 0)
      if (executors == 0) {
        executors = conf.getInt("spark.dynamicAllocation.maxExecutors", 10)
      }
      executors
    }
  }

  def getNumCores(conf: SparkConf): Int = {
    val numExecutors = getNumExecutors(conf)
    val coresForExecutor = conf.getInt("spark.executor.cores", 1)
    numExecutors * coresForExecutor
  }

  def getNumTaskCpus(conf: SparkConf): Int = {
    conf.getInt("spark.task.cpus", 1)
  }

  def getNumCoresPerExecutor(conf: SparkConf): Int = {
    val confTaskCpus = getNumTaskCpus(conf)
    val confCores = conf.getInt("spark.executor.cores", 1)
    val coresPerExec = confCores / confTaskCpus
    coresPerExec
  }

  def getParallelism(conf: SparkConf): Int = {
    SparkUtil.getNumExecutors(conf) * SparkUtil.getNumCoresPerExecutor(conf)
  }

  def getDefaultParallelism(conf: SparkConf): Int = {
    conf.getInt("spark.default.parallelism", getParallelism(conf))
  }

  def getNumAngelPSInstance(conf: SparkConf): Int = {
    conf.getInt("spark.ps.instances", 1)
  }

  def isLocalMaster(conf: SparkConf): Boolean = {
    val master = conf.get("spark.master", "")
    master == "local" || master.startsWith("local[")
  }

  def getLatestPart(spark: SparkSession, table: String): Option[(String, String)] = {
    val rows = spark.sql(s"show partitions $table").collect()
    if (rows.isEmpty) {
      None
    } else {
      val tuple = rows.map {
        case Row(nameDate: String) =>
          val arr = nameDate.split("/")(0).split("=")
          (arr(0), arr(1))
      }.maxBy(_._2)
      Some(tuple)
    }
  }

  def getAs[T](row: Row, fieldName: String, defaultValue: T): T = {
    Option(row.getAs[T](fieldName)).getOrElse(defaultValue)
  }

  def getAs[T](row: Row, i: Int, defaultValue: T): T = {
    Option(row.getAs[T](i)).getOrElse(defaultValue)
  }

  def tableName(table: String): String = {
    if (table.startsWith("@")) {
      table.substring(1)
    } else {
      table
    }
  }

  def saveAsTable(df: DataFrame, table: String, format: String = "parquet"): DataFrame = {
    val spark = df.sparkSession
    val nTable = tableName(table)
    if (!table.equals(nTable) || !nTable.contains(".") || isLocalMaster(spark.sparkContext.getConf)) {
      df.createOrReplaceTempView(nTable)
      df
    } else {
      spark.sql(s"drop table if exists $nTable ")
      val tmpName = Identifiable.randomUID("tmp_easyml_table")
      df.createOrReplaceTempView(tmpName)
      spark.sql(s"create table if not exists $nTable stored as $format as select * from $tmpName")
    }
  }

  def getSparkConf(builder: SparkSession.Builder): SparkConf = {
    val conf = new SparkConf()
    val field = classOf[SparkSession.Builder].getDeclaredFields.find(_.getName.endsWith("options")).get
    field.setAccessible(true)
    val options = field.get(builder).asInstanceOf[mutable.HashMap[String, String]]
    options.foreach {
      case (k, v) => conf.set(k, v)
    }
    conf
  }

  def getSparkEnv(spark: SparkSession): SparkEnv = {
    val method = spark.sparkContext.getClass.getDeclaredMethod("env")
    method.invoke(spark.sparkContext).asInstanceOf[SparkEnv]
  }

  def getDriverHost(spark: SparkSession): String = {
    val blockManager = getSparkEnv(spark).blockManager
    blockManager.master.getMemoryStatus.toList.flatMap({ case (blockManagerId, _) =>
      if (blockManagerId.executorId == "driver") Some(getHostToIP(blockManagerId.host))
      else None
    }).head
  }

  def getHostToIP(hostname: String): String = {
    if (InetAddressUtils.isIPv4Address(hostname) || InetAddressUtils.isIPv6Address(hostname))
      hostname
    else
      InetAddress.getByName(hostname).getHostAddress
  }

  def unpersist[T](dataset: Dataset[T], blocking: Boolean = false): Dataset[T] = {
    // https://blog.csdn.net/Code_LT/article/details/88758220
    if (dataset.sparkSession.version >= "2.4.0") {
      dataset.unpersist(blocking)
    } else {
      logWarning("Spark dataset unpersist call has fatal bug, so that it will clear all linage cache, this bug is fixed until version 2.4.0.")
      dataset
    }
  }

  def repartition[T](dataset: Dataset[T], numPartitions: Int = 0): Dataset[T] = {
    val newNumPartitions = if (numPartitions == 0) {
      val numPartitions = SparkUtil.getDefaultParallelism(dataset.sparkSession.sparkContext.getConf)

      logInfo(s"Set numPartitions = $numPartitions")
      numPartitions
    } else {
      numPartitions
    }
    val partitions = dataset.rdd.getNumPartitions
    if (partitions < newNumPartitions) {
      dataset.repartition(newNumPartitions)
    } else if (partitions > newNumPartitions) {
      dataset.coalesce(newNumPartitions)
    } else {
      dataset
    }
  }

  def saveAsTfRecord(df: DataFrame, output: String, recordType: String = "Example",
                     gzip: Boolean = false, saveMode: SaveMode = SaveMode.Overwrite,
                     partitionBy: Option[Seq[String]] = None, source: String = "tfrecord"): Unit = {
    var writer = df.write.format(source).option("recordType", recordType)
    if (gzip) {
      writer = writer.option("codec", COMPRESS_MODE)
    }
    if (partitionBy.isDefined) {
      writer.partitionBy(partitionBy.get: _*)
    }
    writer.mode(saveMode).save(output)
  }

  def loadFromTfRecord(spark: SparkSession, input: String, keys: String = "*", recordType: String = "Example", source: String = "tfrecord", options: Option[Map[String, String]] = None): DataFrame = {
    var reader = spark.read.format(source).option("recordType", recordType)
    reader = reader.option("keys", keys)
    options.foreach(opt => reader.options(opt))
    var df = reader.load(input)
    if (!keys.equals("*")) {
      df = df.select(keys.split(",").map(_.trim).map(functions.col): _*)
    }
    df
  }

  def loadFromTfRecords(spark: SparkSession, inputs: Array[String], keys: String = "*", recordType: String = "Example", source: String = "tfrecord"): DataFrame = {
    inputs.map(input => loadFromTfRecord(spark, input, keys = keys, recordType = recordType, source = source))
      .reduce(_ union _)
  }

  private def isEmptyDay(day: String): Boolean = {
    day.isEmpty || day.equals(NULL)
  }

  def loadFromTfRecordOfDays(spark: SparkSession, input: String, keys: String = "*", recordType: String = "Example",
                             startDay: String = "", endDay: String = "", source: String = "tfrecord"): DataFrame = {
    var days = IOUtil.listDirectory(input).asScala.toArray.map(IOUtil.baseName)
    if (days.isEmpty) {
      loadFromTfRecord(spark, input, recordType = recordType, source = source)
    } else {
      if (!isEmptyDay(startDay)) {
        days = days.filter(_ >= startDay)
      }
      if (!isEmptyDay(endDay)) {
        days = days.filter(_ <= endDay)
      }
      val inputs = days.map(day => IOUtil.join(input, day))
      loadFromTfRecords(spark, inputs, keys = keys, recordType = recordType, source = source)
    }
  }

  def loadFromTable(spark: SparkSession, input: String, keys: String = "*",
                    dayKey: String = "day", startDay: String = "", endDay: String = ""): DataFrame = {
    if (isEmptyDay(startDay) && isEmptyDay(endDay)) {
      spark.sql(s"select $keys from $input")
    } else if (isEmptyDay(endDay)) {
      spark.sql(s"select $keys from $input where $dayKey>='$startDay'")
    } else if (isEmptyDay(startDay)) {
      spark.sql(s"select $keys from $input where $dayKey<='$endDay'")
    } else {
      spark.sql(s"select $keys from $input where $dayKey>='$startDay' and $dayKey<='$endDay'")
    }
  }

  def loadFromPartitionTable(spark: SparkSession, input: String, keys: String = "*"): DataFrame = {
    val fields = input.split("/")
    val name = fields(0)
    val filter = fields.slice(1, fields.length).map(it => {
      val tuple = it.split("=")
      s"${tuple(0)}='${tuple(1)}'"
    }).mkString(" ")
    if (filter.isEmpty) {
      spark.sql(s"select $keys from $name")
    } else {
      spark.sql(s"select $keys from $name where $filter")
    }
  }

  def columnsToLowerCase(df: DataFrame): DataFrame = {
    var newDf = df
    df.columns.foreach(col => {
      if (!col.toLowerCase.equals(col)) {
        newDf = newDf.withColumnRenamed(col, col.toLowerCase)
      }
    })
    newDf
  }

  def withSerializeExampleColumn(df: DataFrame, columns: Array[String], newName: String = "examples", keepColumns: Array[String] = Array()): DataFrame = {
    val columnIndices = columns.filter(df.columns.contains).map(it => df.columns.indexOf(it))
    val outputColumns = (df.columns.filterNot(columns.contains) ++ keepColumns.filter(df.columns.contains)).distinct
    val indices = outputColumns.map(it => df.columns.indexOf(it))
    val fields = df.schema.fields
    val serFields = columnIndices.map(i => fields(i))
    if (!serFields.isEmpty) {
      val inputSchema = StructType(serFields)
      val serializer = new TFRecordSerializer(inputSchema)

      val outputSchema = StructType(indices.map(i => fields(i)) :+ StructField(newName, BinaryType))

      val rdd = df.rdd.map(row => {
        val seq = row.toSeq
        val oSeq = indices.map(i => seq(i))
        val serSeq = columnIndices.map(i => seq(i))
        val nRow = Row.fromSeq(serSeq)
        val example = serializer.serializeExample(nRow).toByteArray
        Row.fromSeq(oSeq :+ example)
      })

      df.sparkSession.createDataFrame(rdd, outputSchema)
    } else {
      df
    }
  }

  def createTableSql(table: String, structType: StructType, partition: Seq[String] = Seq(), format: String = "parquet"): String = {
    val fields = structType.fields.filterNot(it => partition.contains(it.name))
      .map(f => s"${f.name} ${f.dataType.sql}").mkString(",\n")
    var sql =
      s"""create table if not exists $table(
         |${fields.toLowerCase}
         |)""".stripMargin
    if (partition.nonEmpty) {
      val fields = structType.fields.filter(it => partition.contains(it.name)).map(f => (f.name, s"${f.name} ${f.dataType.sql}")).toMap
      val partitionSql = partition.map(it => fields(it)).mkString(", ").toLowerCase
      sql += s"partitioned by ($partitionSql) stored as $format;"
    }
    sql
  }

  def setDynamicPartition(spark: SparkSession): Unit = {
    val hiveContext = spark.sqlContext
    hiveContext.setConf("hive.exec.dynamic.partition", "true")
    hiveContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
  }

  def setGzipOutput(spark: SparkSession): Unit = {
    val hiveContext = spark.sqlContext
    hiveContext.setConf("hive.exec.compress.output", "true")
    hiveContext.setConf("mapred.output.compress", "true")
    hiveContext.setConf("mapred.output.compression.codec", "org.apache.hadoop.io.compress.GzipCodec")
  }

  def parseEnv(env: String): Option[Map[String, String]] = {
    if (env == null || env.equals(NULL) || env.isEmpty) {
      None
    } else {
      Some(env.split("[,;]").map(it => {
        val Array(k, v) = it.split("=")
        (k, v)
      }).toMap)
    }
  }

  def sql(spark: SparkSession, sql: String, env: Option[Map[String, String]] = None): DataFrame = {
    if (sql.endsWith(SQL_FILE)) {
      sqlFile(spark, sql, env)
    } else {
      sqlText(spark, sql, env)
    }
  }

  def sqlFile(spark: SparkSession, file: String, env: Option[Map[String, String]] = None): DataFrame = {
    val lines = if (IOUtil.exists(file)) {
      IOUtil.readLines(file)
    } else {
      IOUtil.getResource(file)
    }
    val text = lines.asScala.mkString("\n")
    sqlText(spark, text, env)
  }

  def sqlText(spark: SparkSession, sqlText: String, env: Option[Map[String, String]] = None): DataFrame = {
    val hiveContext = spark.sqlContext
    var nSqlText = sqlText
    env.foreach(kv => kv.foreach(it => {
      nSqlText = nSqlText.replaceAll("\\$\\{" + it._1 + "\\}", it._2)
      Cmds.VARS = Cmds.VARS + (it._1 -> it._2)
    }))
    nSqlText = nSqlText.split("\\n").filterNot(it => it.trim.startsWith("--"))
      .map(it => StringUtil.safeSplit(it, "--").asScala.head)
      .mkString("\n").replaceAll("[\\s\\n]+", " ")
    val sqls = StringUtil.safeSplit(nSqlText, SQL_SPLIT).asScala.toArray
      .map(StringUtil.strip)
      .filterNot(_.isEmpty)
    var df: DataFrame = null
    sqls.foreach(text => {
      if (text.toLowerCase.startsWith("set ")) {
        val Array(key, value) = text.substring(4).split("=")
        val nkey = StringUtil.strip(key)
        val nValue = StringUtil.strip(value)
        hiveContext.setConf(nkey, nValue)
        println(s"EXECUTE SET: $nkey=$nValue")
      } else {
        df = Cmds.run(spark, text)
      }
    })
    df
  }

  case class Field(name: String, dtype: String, comment: String)

  case class HiveTableInfo(table: String, location: String, fields: Array[Field], partitionFields: Array[Field])

  def getHiveTableInfo(spark: SparkSession, table: String): HiveTableInfo = {
    val desc = spark.sql(s"desc formatted $table")
      .rdd
      .map(row => (row.getString(0), row.getString(1), row.getString(2)))
      .collect()

    val locations = desc.filter(it => it._1 != null && it._1.startsWith("Location"))
    val location = if (locations.nonEmpty) locations.head._2 else null

    val schema = spark.sql(s"desc $table")
      .rdd
      .map(row => (row.getString(0).trim, row.getString(1), row.getString(2)))
      .filter(it => it != null && it._1.nonEmpty)
      .collect()

    var isPartition = false
    var cols = ArrayBuffer.empty[Field]
    val ptCols = ArrayBuffer.empty[Field]
    schema.foreach {
      case (col, dtype, comment) =>
        if (!col.startsWith("#") && dtype != null && dtype.nonEmpty) {
          if (isPartition) {
            ptCols.append(Field(col, dtype, comment))
          } else {
            cols.append(Field(col, dtype, comment))
          }
        }
        if (col.equals("# Partition Information")) {
          isPartition = true
        }
    }
    val pt = ptCols.map(_.name).toSet
    cols = cols.filterNot(it => pt.contains(it.name))

    HiveTableInfo(table = table, location = location, fields = cols.toArray, partitionFields = ptCols.toArray)
  }
}
