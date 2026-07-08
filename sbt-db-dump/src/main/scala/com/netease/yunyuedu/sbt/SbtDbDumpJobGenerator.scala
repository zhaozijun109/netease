package com.netease.yunyuedu.sbt

import com.netease.yunyuedu.sbt.SbtDbDumpJobGeneratorKeys.snakify
import com.netease.yunyuedu.sbt.model.{GeneratorContext, TableMeta}
import sbt.{File, _}

trait SbtDbDumpJobGenerator {

  def generateConfigFile(outputDirectory: File)(implicit generatorContext: GeneratorContext): File = {
    val commonConfigFile = outputDirectory / "jobs" / "db_dump" / "common.properties"
    val commonContent =
      s"""type=spark
         |language=java
         |master=yarn
         |deploy-mode=cluster
         |jars=../lib/common.jar,../lib/db.jar,../lib/jta-spec1_0_1.jar,../lib/mysql-connector-java-5.1.48.jar,../lib/ndi-worker-music-transfer-common.jar,../lib/ndi-worker-music-transfer-ddb-dbi.jar,../lib/ndi-worker-music-transfer-ddb-qs.jar,../lib/ndi-worker-music-transfer-hive.jar,../lib/ndi-worker-music-transfer-jdbc.jar,../lib/ndi-worker-music-transfer-mysql.jar,../lib/netease-commons.jar,../lib/spymemcached-2.7.1.jar
         |execution-jar=../lib/ndi-worker-music-transfer-common.jar
         |class=com.netease.music.da.transfer.common.Worker
         |spark-version=3.3.0
         |executor-memory=12g
         |driver-memory=4g
         |conf.spark.executor.cores=4
         |conf.spark.dynamicAllocation.maxExecutors=10
         |conf.spark.task.maxFailures=8
         |conf.spark.yarn.executor.memoryOverhead=4g
         |conf.spark.yarn.driver.memoryOverhead=1g
         |conf.spark.yarn.maxAppAttempts=1
         |conf.spark.executor.extraJavaOptions=-XX:NewRatio=3 -XX:MaxDirectMemorySize=2g -XX:MaxMetaspaceSize=256m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
         |conf.spark.storage.memoryFraction=0.3
         |conf.spark.sql.catalogImplementation=hive
         |conf.spark.transmit.handler=com.netease.music.da.transfer.common.handler.ColumnHandler
         |conf.spark.transmit.writer=com.netease.music.da.transfer.hive.writer.HiveWriter
         |conf.spark.transmit.writer.hive.database=${generatorContext.hiveSchema}
         |conf.spark.transmit.writer.hive.saveMode=insertOverwrite
         |conf.spark.transmit.reader.ddb.dbi.ddbUrlSuffix=key=/home/hadoop/secret.key
         |conf.spark.transmit.writer.ddb.dbi.ddbUrlSuffix=key=/home/hadoop/secret.key
         |""".stripMargin

    IO.write(commonConfigFile, commonContent)
    commonConfigFile
  }

  def generateTableImportJobFile(table: TableMeta, outputDirectory: File, dependencies: String = "")(implicit generatorContext: GeneratorContext): File = {
    val tableName = table.tableName
    val connectMode = generatorContext.connectMode(tableName)
    connectMode match {
      case "ndc" | "ndc_rds" => generateNdcMergeJobFile(table, outputDirectory, dependencies)
      case _ => generateNdiJobFile(table, outputDirectory, dependencies)
    }
  }

  private def generateNdcMergeJobFile(table: TableMeta, outputDirectory: File, dependencies: String = "")(implicit generatorContext: GeneratorContext): File = {
    val tableName = table.tableName
    val jobFile = outputDirectory / "jobs" / s"$tableName.job"

    val inputColumns = table.columns.map(_.columnName).map(_.toLowerCase).mkString(",")
    val primaryKeys = table.columns.filter(_.isPrimaryKey).map(_.columnName).map(_.toLowerCase).mkString(",")
    val hiveSchema = generatorContext.hiveSchema
    val hiveTableName = generatorContext.hiveTableMapper(tableName)
    val tmpHiveTableName = s"_tmp_$hiveTableName"
    val ndcBinlogTableName =  generatorContext.binlogTableMapper(tableName)

    val tableOutputDirectory =  generatorContext.jobOutputBaseDirectory + "/" + generatorContext.jobOutputDirectoryMapper(tableName)
    val prevTableOutputDirectory = tableOutputDirectory.replaceAll("azkaban.flow.1.days.ago", "azkaban.flow.2.days.ago")

    if(primaryKeys.isEmpty) {
      throw new RuntimeException(s"ndc merge import only support tables with primary key, can't find for table $tableName")
    }

    val spark3MergeJob =
     s"""
        |type=sparksql
        |
        |spark-version=3.3.0
        |master=yarn
        |deploy-mode=cluster
        |conf.spark.speculation=true
        |driver-memory=6g
        |executor-memory=10g
        |conf.spark.dynamicAllocation.maxExecutors=75
        |conf.spark.sql.shuffle.partitions=400
        |conf.spark.executor.cores=4
        |conf.spark.locality.wait=0
        |conf.spark.executor.memoryOverhead=8g
        |conf.spark.sql.parquet.compression.codec=gzip
        |conf.spark.sql.adaptive.advisoryPartitionSizeInBytes=2g
        |conf.spark.sql.finalStage.adaptive.advisoryPartitionSizeInBytes=2g
        |conf.spark.sql.adaptive.rebalanceInitialPartitionNum=400
        |conf.spark.sql.optimizer.rebalanceZorderColumns.enabled=true
        |conf.spark.sql.optimizer.twoPhaseRebalanceBeforeZorder.enabled=true
        |hive.query.01=drop table if exists $hiveSchema.$tmpHiveTableName;
        |hive.query.02=create table $hiveSchema.$tmpHiveTableName like $hiveSchema.$hiveTableName location '$prevTableOutputDirectory';
        |hive.query.03=insert overwrite table $hiveSchema.$hiveTableName \\
        |    select $inputColumns \\
        |    from ( \\
        |        select *, row_number() over (partition by $primaryKeys order by `_bin_op_time` desc, _bin_op_seqno desc) as rnk \\
        |        from ( \\
        |            select $inputColumns, \\
        |                   `_bin_op`, `_bin_op_time`, `_bin_op_seqno` \\
        |            from $ndcBinlogTableName where dt='$${azkaban.flow.1.days.ago}' \\
        |            union all \\
        |            select $inputColumns, 0 as `_bin_op`, 0 as `_bin_op_time`, 0 as `_bin_op_seqno` \\
        |            from $hiveSchema.$tmpHiveTableName \\
        |        ) t \\
        |    ) tt \\
        |    where rnk = 1 and `_bin_op` <> 1;
        |hive.query.04=drop table if exists $hiveSchema.$tmpHiveTableName
        |dependencies=$dependencies
        |""".stripMargin

    val spark3MergeJobForPostHot =
      s"""
         |type=sparksql
         |spark-version=3.3.0
         |master=yarn
         |deploy-mode=cluster
         |conf.spark.speculation=true
         |driver-memory=6g
         |executor-memory=10g
         |conf.spark.dynamicAllocation.maxExecutors=70
         |conf.spark.sql.shuffle.partitions=600
         |conf.spark.executor.cores=4
         |conf.spark.locality.wait=0
         |conf.spark.executor.memoryOverhead=8g
         |conf.spark.sql.parquet.compression.codec=gzip
         |conf.spark.hadoop.hive.exec.dynamic.partition=true
         |conf.spark.hadoop.hive.exec.dynamic.partition.mode=nonstrict
         |conf.spark.sql.hive.convertInsertingPartitionedTable=false
         |hive.query.01=\\
         |INSERT OVERWRITE TABLE lofter.stg_post_hot_dynamic_in_130d_dd PARTITION(dt = '$${azkaban.flow.1.days.ago}') \\
         |SELECT  id, \\
         |        postid, \\
         |        blogid, \\
         |        publisheruserid, \\
         |        frompostid, \\
         |        fromblogid, \\
         |        topostid, \\
         |        toblogid, \\
         |        content, \\
         |        optime, \\
         |        type, \\
         |        ip \\
         |FROM \\
         |( \\
         |	SELECT  *, \\
         |	        ROW_NUMBER() OVER (PARTITION BY $primaryKeys ORDER BY `_bin_op_time` DESC, `_bin_op_seqno` DESC ) AS rnk \\
         |	FROM \\
         |	( \\
         |    SELECT $inputColumns, \\
         |           `_bin_op`, `_bin_op_time`, `_bin_op_seqno` \\
         |    FROM $ndcBinlogTableName where dt='$${azkaban.flow.1.days.ago}' \\
         |		UNION ALL \\
         |		SELECT  id, \\
         |		        postid, \\
         |		        blogid, \\
         |		        publisheruserid, \\
         |		        frompostid, \\
         |		        fromblogid, \\
         |		        topostid, \\
         |		        toblogid, \\
         |		        content, \\
         |		        optime, \\
         |		        type, \\
         |		        ip, \\
         |		        0 AS `_bin_op`, \\
         |		        0 AS `_bin_op_time`, \\
         |            0 AS `_bin_op_seqno` \\
         |		FROM lofter.stg_post_hot_dynamic_in_130d_dd \\
         |		WHERE dt = '$${azkaban.flow.2.days.ago}' \\
         |	) t \\
         |) tt \\
         |WHERE rnk = 1 AND `_bin_op` <> 1;
         |hive.query.02=\\
         |INSERT OVERWRITE TABLE $hiveSchema.$hiveTableName \\
         |SELECT  id, \\
         |        postid, \\
         |        blogid, \\
         |        publisheruserid, \\
         |        frompostid, \\
         |        fromblogid, \\
         |        topostid, \\
         |        toblogid, \\
         |        content, \\
         |        optime, \\
         |        type, \\
         |        ip \\
         |FROM lofter.stg_post_hot_dynamic_in_130d_dd \\
         |WHERE dt = '$${azkaban.flow.1.days.ago}' \\
         |UNION ALL \\
         |SELECT  id, \\
         |        postid, \\
         |        blogid, \\
         |        publisheruserid, \\
         |        frompostid, \\
         |        fromblogid, \\
         |        topostid, \\
         |        toblogid, \\
         |        content, \\
         |        optime, \\
         |        type, \\
         |        ip \\
         |FROM lofter.stg_post_hot_static_out_130d_wd \\
         |WHERE dt >= date_sub('$${azkaban.flow.1.days.ago}',7) AND dt < '$${azkaban.flow.1.days.ago}';
         |dependencies=$dependencies
         |""".stripMargin

    val jobContent = tableName match {
      case "PostHot" => spark3MergeJobForPostHot
      case _ => spark3MergeJob
    }

    IO.write(jobFile, jobContent)
    jobFile
  }

  private def generateNdiJobFile(table: TableMeta, outputDirectory: File, dependencies: String = "")(implicit generatorContext: GeneratorContext): File = {
    val tableName = table.tableName
    val jobFile = outputDirectory / "jobs" / s"$tableName.job"

    val connect = generatorContext.connect(tableName)
    val connectUserName = generatorContext.connectUserName(tableName)
    val connectPassword = generatorContext.connectPassword(tableName)
    val connectMode = generatorContext.connectMode(tableName)

    val inputColumns = table.columns.map(_.columnName).mkString(",")
    val outputColumns =  table.columns.map(_.columnName).map(_.toLowerCase).mkString(",")

    val columnMappings = table.columns.map{ col =>
      col.columnType match {
        case "TIMESTAMP" =>
          // s"conf.spark.transmit.handler.column.outputColumns.${col.columnName.toLowerCase}.inputColumn=UNIX_TIMESTAMP(${col.columnName}) * 1000"
          s"conf.spark.transmit.handler.column.outputColumns.${col.columnName.toLowerCase}.inputColumn=${col.columnName}"
        case _ =>
          s"conf.spark.transmit.handler.column.outputColumns.${col.columnName.toLowerCase}.inputColumn=${col.columnName}"
      }
    }.filter(_.nonEmpty).mkString("\n")

    def isNumberType(columnType: String): Boolean = columnType.toUpperCase() match {
      case "BIGINT" | "INT" | "TINYINT" | "SMALLINT" | "DOUBLE" | "FLOAT" => true
      case _ => false
    }
    val numPk = table.columns.filter(_.isPrimaryKey).filter(c => isNumberType(c.columnType)).map(_.columnName)
    val splitNumLine =
      if(numPk.nonEmpty) "" else {
        if(connectMode == "rds") s"conf.spark.transmit.reader.mysql.splitNum=5\n" else
          s"conf.spark.transmit.reader.ddb.${connectMode}.splitNum=5\n"
      }

    val hiveTable = generatorContext.hiveTableMapper(tableName)

    val jobSettings = generatorContext.tableJobSettingMapper(tableName)

    val jobReader = connectMode match {
      case "qs" =>
        s"""
           |conf.spark.transmit.reader=com.netease.music.da.transfer.ddb.qs.reader.DDBQSReader
           |conf.spark.transmit.reader.ddb.${connectMode}.maxSplitNum=20000
           |conf.spark.transmit.reader.ddb.${connectMode}.splitSize=500000
           |conf.spark.transmit.reader.ddb.${connectMode}.url=${connect}
           |conf.spark.transmit.reader.ddb.${connectMode}.user=${connectUserName}
           |conf.spark.transmit.reader.ddb.${connectMode}.password=${connectPassword}
           |conf.spark.transmit.reader.ddb.${connectMode}.table=$tableName
           |conf.spark.transmit.reader.ddb.${connectMode}.columns=$inputColumns
           |""".stripMargin

      case "rds" =>
        s"""
          |conf.spark.transmit.reader=com.netease.music.da.transfer.mysql.reader.MySQLReader
          |conf.spark.transmit.reader.mysql.sources=0
          |conf.spark.transmit.reader.mysql.sources.0.url=${connect}
          |conf.spark.transmit.reader.mysql.sources.0.user=${connectUserName}
          |conf.spark.transmit.reader.mysql.sources.0.password=${connectPassword}
          |conf.spark.transmit.reader.mysql.sources.0.tables=$tableName
          |conf.spark.transmit.reader.mysql.columns=$inputColumns
          |conf.spark.transmit.reader.mysql.urlSuffix=useUnicode=true&characterEncoding=UTF-8
          |""".stripMargin

      case _ /* ddb default */=>
        s"""
           |conf.spark.transmit.reader=com.netease.music.da.transfer.ddb.dbi.reader.DDBDBIReader
           |conf.spark.transmit.reader.ddb.${connectMode}.maxSplitNum=20000
           |conf.spark.transmit.reader.ddb.${connectMode}.splitSize=500000
           |conf.spark.transmit.reader.ddb.${connectMode}.url=${connect}
           |conf.spark.transmit.reader.ddb.${connectMode}.user=${connectUserName}
           |conf.spark.transmit.reader.ddb.${connectMode}.password=${connectPassword}
           |conf.spark.transmit.reader.ddb.${connectMode}.table=$tableName
           |conf.spark.transmit.reader.ddb.${connectMode}.columns=$inputColumns
           |""".stripMargin
    }

    val jobContent = (if(jobSettings.nonEmpty) jobSettings + "\n" else "") +
      splitNumLine + jobReader +
      s"""
         |$columnMappings
         |conf.spark.transmit.writer.hive.table=$hiveTable
         |conf.spark.transmit.writer.hive.keepOwner=false
         |conf.spark.transmit.handler.column.outputColumns=$outputColumns
         |dependencies=$dependencies""".stripMargin

    IO.write(jobFile, jobContent)
    jobFile
  }

  def generateHiveImportJobFile(table: TableMeta, outputDirectory: File, dependencies: String = "")(implicit generatorContext: GeneratorContext): File = {
    val tableName = table.tableName
    val jobName = s"$tableName-hive"
    val jobFile = outputDirectory / "jobs" / s"$jobName.job"
    val tableOutputDirectory =  generatorContext.jobOutputBaseDirectory + "/" + generatorContext.jobOutputDirectoryMapper(tableName)
    val hiveSchema = generatorContext.hiveSchema
    val hiveTableName = generatorContext.hiveTableMapper(tableName)
    val connectMode = generatorContext.connectMode(tableName)
    val zorderColumns = generatorContext.tableZorderColumnsMapper(tableName)

    val columns = table.columns.map{ col =>
      val hiveType = convertColumnTypeToHive(col.columnType, connectMode)

      if(col.comment.nonEmpty) {
        val comment = col.comment.get.replaceAll("'", "\\'").replaceAll(";", "").replaceAll("\n","")
        s"`${col.columnName}` ${hiveType} comment '${comment}'"
      } else s"`${col.columnName}` ${hiveType}"

    }.mkString(", ")

    val zorderSettings = if(zorderColumns != null && zorderColumns.trim.nonEmpty) {
      s"""TBLPROPERTIES ('kyuubi.zorder.cols'='${zorderColumns}','kyuubi.zorder.enabled'='true')""".stripMargin
    } else ""

    val jobContent = if(generatorContext.hiveTablePartitioned) {
      s"""type=hive
         |azk.hive.action=execute.query
         |hive.query.01=use $hiveSchema;
         |hive.query.02=drop table if exists `$hiveTableName`;
         |hive.query.03=create external table if not exists `$hiveTableName` ($columns) partitioned by (dt string) stored as parquet location '$tableOutputDirectory' $zorderSettings;
         |hive.query.04=alter table $hiveTableName add if not exists partition (dt='$${azkaban.flow.1.days.ago}') location '$tableOutputDirectory';
         |dependencies=$dependencies""".stripMargin
    } else {
      s"""type=hive
         |azk.hive.action=execute.query
         |hive.query.01=use $hiveSchema;
         |hive.query.02=drop table if exists `$hiveTableName`;
         |hive.query.03=create external table if not exists `$hiveTableName` ($columns) stored as parquet location '$tableOutputDirectory' $zorderSettings;
         |dependencies=$dependencies""".stripMargin
    }

    IO.write(jobFile, jobContent)
    jobFile
  }

  def generateFlowJobFile(tables: Seq[TableMeta], outputDirectory: File, dependencies: String = "")(implicit generatorContext: GeneratorContext): File = {
    val jobName = "flow"
    val jobFile = outputDirectory / "jobs" / s"$jobName.job"

    val jobContent =
      s"""type=noop
        |dependencies=$dependencies""".stripMargin

    IO.write(jobFile, jobContent)
    jobFile
  }

  def generateFlowPropertiesFile(tables: Seq[TableMeta], outputDirectory: File)(implicit generatorContext: GeneratorContext): File = {
    val pFile = outputDirectory / "jobs" / "common.properties"

    val content =
      s"""flow.num.job.threads=30
        |retries=2
        |retry.backoff=10000""".stripMargin

    IO.write(pFile, content)
    pFile
  }

  private def convertColumnTypeToHive(columnType: String, mode: String): String = {
    mode match {
      case "rds" | "ndc_rds" => convertRdsColumnTypeToHive(columnType)
      case _ => convertDdbColumnTypeToHive(columnType)
    }
  }

  private def convertDdbColumnTypeToHive(columnType: String): String = {
    columnType.toUpperCase match {
      case "TINYINT" | "TINYINT UNSIGNED" | "SMALLINT" | "SMALLINT UNSIGNED" | "SMALLINT UNSIGNED" | "BIT" => "int"
      case "INTEGER" | "INT" | "INT UNSIGNED" | "BIGINT" | "BIGINT UNSIGNED" | "DATETIME" | "TIMESTAMP" => "bigint"
      case "VARCHAR" | "TEXT" | "LONGTEXT" | "MEDIUMTEXT" | "BLOB" | "MEDIUMBLOB" | "CHAR" | "DATE" | "JSON" => "string"
      case "FLOAT" | "DOUBLE" | "DECIMAL" => "double"
      case "VARBINARY" => "varbinary"
      case _ => throw new RuntimeException("unsupported column type: " + columnType)
    }
  }

  private def convertRdsColumnTypeToHive(columnType: String): String = {
    columnType.replaceAll("\\(.*\\)", "").toUpperCase match {
      case "TINYINT" | "TINYINT UNSIGNED" | "SMALLINT" | "SMALLINT UNSIGNED" | "SMALLINT UNSIGNED" | "BIT" => "int"
      case "INTEGER" | "INT" | "INT UNSIGNED" | "BIGINT" | "BIGINT UNSIGNED" | "DATETIME" | "TIMESTAMP" => "bigint"
      case "VARCHAR" | "TEXT" | "LONGTEXT" | "MEDIUMTEXT" | "BLOB" | "MEDIUMBLOB" | "CHAR" | "DATE" | "JSON" => "string"
      case "FLOAT" | "DOUBLE" | "DECIMAL" => "double"
      case "VARBINARY" => "varbinary"
      case _ => throw new RuntimeException("unsupported column type: " + columnType)
    }
  }

}

object SbtDbDumpJobGenerator extends SbtDbDumpJobGenerator
