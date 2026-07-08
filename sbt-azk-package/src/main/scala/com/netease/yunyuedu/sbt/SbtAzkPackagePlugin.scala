package com.netease.yunyuedu.sbt

import sbt._
import Keys._
import sbt.plugins.JvmPlugin
import sbtassembly.AssemblyKeys._

object SbtAzkPackagePlugin extends AutoPlugin {
  object autoImport extends SbtAzkPackageKeys

  override def trigger = allRequirements

  override def requires = JvmPlugin

  import SbtAzkPackageKeys._

  override lazy val projectSettings = Seq(
    azkJobDir := "jobs",
    azkPackageOutput := { target.value / s"${name.value}-jobs.zip" },
    azkPackage := {
      val assemblyJar = assembly.value
      val azkPackageFile = (azkPackageOutput in azkPackage).value
      val jobsDir = (baseDirectory.value / (azkJobDir in azkPackage).value)

      val outputDirectory = (resourceManaged in Compile).value

      def relativeJobPath(jobFile: File): String = {
        jobFile.getPath.substring(jobsDir.getPath.length + 1)
      }

      def generateFlowProperties(flowPath: String): File = {
        val jobName = flowPath.replaceAll(java.io.File.pathSeparator, "_")
        val jobFile = outputDirectory / "jobs" / s"$jobName.properties"

        val level = jobName.count(_ == java.io.File.pathSeparatorChar) + 1
        val relativeJarPath = Seq.fill(level)("../").mkString("") + assemblyJar.getName
        val threads = jobName match {
          case "ads" => 15
          case _ => 10
        }
        val jobContent =
          s"""
             |classpath=$relativeJarPath
             |flow.num.job.threads=$threads
             |""".stripMargin

        IO.write(jobFile, jobContent)
        jobFile
      }
      def generateFlowJobFile(flowPath: String, dependencies: String): File = {
        val jobName = flowPath.replaceAll(java.io.File.pathSeparator, "_")
        val jobFile = outputDirectory / "jobs" / s"$jobName.job"

        val jobContent =
          s"""type=noop
             |dependencies=$dependencies""".stripMargin

        IO.write(jobFile, jobContent)
        jobFile
      }

      def listRecursive(parent: File): Seq[(File,String)] = IO.listFiles(parent).flatMap {
        case f if f.isDirectory =>
          val dependencies = IO.listFiles(f)
            .filter(_.isFile)
            .filter(_.getName.endsWith(".job"))
            .map(_.getName.replace(".job", ""))

          val depLinePattern = """dependencies=(.+)""".r

          val dependencyMap = IO.listFiles(f)
            .filter(_.isFile)
            .filter(_.getName.endsWith(".job"))
            .flatMap { jobFile =>
              val jobName = jobFile.getName.replace(".job", "")
              val depLine = scala.io.Source.fromFile(jobFile).getLines().filter(_.startsWith("dependencies=")).toSeq.headOption
              depLine match {
                case Some(depLinePattern(deps)) => Some(jobName -> deps)
                case _ => None
              }
            }.toMap

          val dependBySet = dependencyMap.values.flatMap(_.split(",")).map(_.trim).toSet

          val flowPath = relativeJobPath(f)
          val flowJobName = flowPath.replaceAll(java.io.File.pathSeparator, "_")

          val isFlowJobExists = dependencies.contains(flowJobName)
          if(isFlowJobExists) {
            val flowPropertiesFile = generateFlowProperties(flowPath)
            Seq(
              flowPropertiesFile -> (flowPath + "/" + flowPropertiesFile.getName)
            ) ++ listRecursive(f)
          } else {
            val flowJobDependencies = dependencies.filterNot(dependBySet)
            val flowJobFile = generateFlowJobFile(flowPath, flowJobDependencies.mkString(","))
            val flowPropertiesFile = generateFlowProperties(flowPath)
            Seq(
              flowJobFile -> (flowPath + "/" + flowJobFile.getName),
              flowPropertiesFile -> (flowPath + "/" + flowPropertiesFile.getName)
            ) ++ listRecursive(f)
          }
        case f if f.getPath.endsWith(".job")=> Seq(f -> relativeJobPath(f))
        case f if f.getPath.endsWith(".properties")=> Seq(f -> relativeJobPath(f))
        case _ => Seq.empty
      }

      val jobEntries = listRecursive(jobsDir)

      val entries = jobEntries :+ (assemblyJar -> assemblyJar.getName)

      IO.zip(entries, azkPackageFile)
      azkPackageFile
    }
  )
}
