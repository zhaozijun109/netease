package com.netease.yunyuedu.sbt

import java.io.InputStream
import java.nio.file.{CopyOption, Files, StandardCopyOption}

import com.netease.yunyuedu.sbt.utils.SqlSchema
import com.netease.yunyuedu.sbt.model.GeneratorContext
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object SbtDbDumpJobGeneratorPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def requires: Plugins = JvmPlugin

  object autoImport extends SbtDbDumpJobGeneratorKeys

  import SbtDbDumpJobGenerator._
  import SbtDbDumpJobGeneratorKeys._


  def copyJarFile(source: InputStream, outputDirectory: File, outputPath: String): (File, String)= {
    val targetFile = outputDirectory / outputPath
    Files.copy(source, targetFile.toPath, StandardCopyOption.REPLACE_EXISTING)

    targetFile -> outputPath
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    connect := { _: String => ""},
    connectUserName := { _: String => ""},
    connectPassword := { _: String => ""},
    connectMode := { _: String => "dbi"},
    tableMetaPath := { baseDirectory.value / "meta.txt" },
    jobConnectionsMapper := { _: String => DEFAULT_CONNECTIONS },
    tableJobSettingMapper := { _: String => DEFAULT_TABLE_CONDITION },
    tableFilter := { _:String => true },
    highPriorTables := Seq.empty[String],
    lowPriorTables := Seq.empty[String],
    tableGroupSize := -1,
    jobOutputBaseDirectory := "/tmp",
    jobOutputDirectoryMapper := { tableName: String => tableName },
    hiveSchema := "default",
    hiveTablePartitioned := false,
    hiveTableMapper := { table: String => table},
    jobSplitKeyMapper := {_: String => null},
    generateJobOutput := { target.value / s"${name.value}.zip" },
    tableZorderColumnsMapper := {_:String => null},
    binlogTableMapper := { table: String => s"default.ods_binlog_${snakify(table)}_di"},
    generateJob := {
      implicit val generatorContext: GeneratorContext = GeneratorContext(
        (connect in generateJob).value,
        (connectUserName in generateJob).value,
        (connectPassword in generateJob).value,
        (connectMode in generateJob).value,
        SqlSchema.readSchema((tableMetaPath in generateJob).value, (tableFilter in generateJob).value),
        (jobConnectionsMapper in generateJob).value,
        (tableJobSettingMapper in generateJob).value,
        (jobOutputBaseDirectory in generateJob).value,
        (jobOutputDirectoryMapper in generateJob).value,
        (jobSplitKeyMapper in generateJob).value,
        (hiveSchema in generateJob).value,
        (hiveTablePartitioned in generateJob).value,
        (hiveTableMapper in generateJob).value,
        (tableZorderColumnsMapper in generateJob).value,
        (binlogTableMapper in generateJob).value
      )

      val flowName = name.value
      val resource = (resourceManaged in Compile).value
      val outputZipFile = (generateJobOutput in generateJob).value

      val highPriorityTables =(highPriorTables in generateJob).value
      val lowPriorityTables = (lowPriorTables in generateJob).value
      val highPriorityIndex = highPriorityTables.zipWithIndex.toMap
      val lowPriorityIndex = lowPriorityTables.zipWithIndex.toMap

      val tables = generatorContext.tables
      val batch1 = tables.filter(t => highPriorityTables.contains(t.tableName)).sortBy(t => highPriorityIndex(t.tableName))
      val batch2 = tables.filterNot(t => highPriorityTables.contains(t.tableName) || lowPriorityTables.contains(t.tableName))
      val batch3 = tables.filter(t => lowPriorityTables.contains(t.tableName)).sortBy(t => lowPriorityIndex(t.tableName))

      val tableGroupSizeValue = (tableGroupSize in generateJob).value
      val numOfGroup = if(tableGroupSizeValue <= 0) tables.size else tableGroupSizeValue

      val sortedTables = (batch1 ++ batch2 ++ batch3)
      val groups = sortedTables.zipWithIndex
        .groupBy(_._2 % numOfGroup)
        .map(g => g._2.sortBy(_._2).map(_._1))
        .toSeq

      val tableDependencyMap = groups.flatMap( g =>
        g.sliding(2).collect {
          case Seq(prev, t) => t.tableName -> s"${prev.tableName}"
        }
      ).toMap.withDefaultValue("")

      val jobEntries = tables.flatMap{ table =>
        Seq(
          generateHiveImportJobFile(table, resource, tableDependencyMap(table.tableName)) -> s"jobs/hive/${table.tableName}-hive.job",
          generateTableImportJobFile(table, resource, s"${table.tableName}-hive") -> s"jobs/db_dump/${table.tableName}.job"
        )
      }

      val flowDependencies = groups.map(_.last).map { table => s"${table.tableName}"}.mkString(",")

      val flowEntries = Seq(
        generateFlowJobFile(tables, resource, flowDependencies) -> s"jobs/$flowName.job",
        generateFlowPropertiesFile(tables, resource) -> "jobs/common.properties"
      )
      val commonConfigEntries = Seq(generateConfigFile(resource) -> "jobs/db_dump/common.properties")

      (resource / "jobs" / "lib").mkdirs()

      val libEntries = Seq(
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/common.jar"), resource, "jobs/lib/common.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/db.jar"), resource, "jobs/lib/db.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/jta-spec1_0_1.jar"), resource, "jobs/lib/jta-spec1_0_1.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/mysql-connector-java-5.1.48.jar"), resource, "jobs/lib/mysql-connector-java-5.1.48.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-common.jar"), resource, "jobs/lib/ndi-worker-music-transfer-common.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-ddb-dbi.jar"), resource, "jobs/lib/ndi-worker-music-transfer-ddb-dbi.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-ddb-qs.jar"), resource, "jobs/lib/ndi-worker-music-transfer-ddb-qs.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-hive.jar"), resource, "jobs/lib/ndi-worker-music-transfer-hive.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-jdbc.jar"), resource, "jobs/lib/ndi-worker-music-transfer-jdbc.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/ndi-worker-music-transfer-mysql.jar"), resource, "jobs/lib/ndi-worker-music-transfer-mysql.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/netease-commons.jar"), resource, "jobs/lib/netease-commons.jar"),
        copyJarFile(getClass.getClassLoader.getResourceAsStream("lib/spymemcached-2.7.1.jar"), resource, "jobs/lib/spymemcached-2.7.1.jar")
      )

      val zipEntries = (jobEntries ++ commonConfigEntries ++ flowEntries ++ libEntries).sortBy(_._2)

      IO.zip(zipEntries, outputZipFile)
      outputZipFile
    }
  )

}
