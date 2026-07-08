name := "wm-udx"
organization := "com.netease.wm"
version := "0.3.16"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val scala212 = "2.12.10"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)
crossScalaVersions := supportedScalaVersions

ThisBuild / scalaVersion := scala212

lazy val sparkVersion = SettingKey[String]("sparkVersion")
lazy val jacksonVersion = SettingKey[String]("jacksonVersion")
lazy val jacksonAnnVersion = SettingKey[String]("jacksonAnnVersion")

sparkVersion := (scalaBinaryVersion.value match {
  case "2.11" => "2.3.2"
  case "2.12" => "2.4.5"
})

jacksonVersion := (scalaBinaryVersion.value match {
  case "2.11" => "2.7.9"
  case "2.12" => "2.8.4"
})

jacksonAnnVersion := (scalaBinaryVersion.value match {
  case "2.11" => "2.7.0"
  case "2.12" => "2.8.0"
})

libraryDependencies ++= Seq(
  "org.roaringbitmap" % "RoaringBitmap" % "0.9.39",
  "com.github.mjakubowski84" %% "parquet4s-core" % "1.0.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.6",
  "org.json4s" %% "json4s-core" % "3.4.2",
  "org.json4s" %% "json4s-jackson" % "3.4.2",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.twitter" %% "util-collection" % "6.45.0",
  "com.maxmind.db" % "maxmind-db" % "1.2.2",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.json" % "json" % "20190722",
  "org.apache.hadoop" % "hadoop-common" % "2.7.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2" % "provided",
  "org.apache.hive" % "hive-exec" % "1.2.1" % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion.value % "provided",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
)

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.fasterxml.jackson.**" -> "shaded.com.fasterxml.jackson.@1")
    .inLibrary("org.scalaj" %% "scalaj-http" % "2.3.0")
    .inLibrary("org.json4s" %% "json4s-jackson" % "3.4.2")
    .inLibrary("org.json4s" %% "json4s-core" % "3.4.2")
    .inLibrary("com.maxmind.db" % "maxmind-db" % "1.2.2")
    .inLibrary("com.fasterxml.jackson.core" % "jackson-annotations" % jacksonAnnVersion.value)
    .inLibrary("com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion.value)
    .inLibrary("com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion.value)
    .inProject,
  ShadeRule.rename("org.roaringbitmap.**" -> "shaded.org.roaringbitmap.@1")
    .inLibrary("org.roaringbitmap" % "RoaringBitmap" % "0.9.39")
    .inProject
)
