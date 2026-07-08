package com.netease.yunyuedu.sbt

import sbt._

trait SbtAzkPackageKeys {
  val azkPackage = taskKey[File]("generate azkaban project package with job and related resource")
  val azkJobDir = settingKey[String]("jobs directory")
  val azkPackageOutput = settingKey[File]("package output path")
}

object SbtAzkPackageKeys extends SbtAzkPackageKeys