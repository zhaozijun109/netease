name := "common-infra"
organization := "com.netease.yuanqi"
version := "0.0.1"
scalaVersion := "2.12.17"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint")

lazy val commonSettings = Seq(
  organization := "com.netease.yuanqi",
  version := "0.0.1",
  scalaVersion := "2.12.17",
  libraryDependencies := Seq(
    "org.slf4j" % "slf4j-api" % "1.7.36",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.2",
    "org.apache.logging.log4j" % "log4j-api" % "2.17.2",
    "org.apache.logging.log4j" % "log4j-core" % "2.17.2",
    "org.apache.logging.log4j" % "log4j-1.2-api" % "2.17.2",
    "com.github.nscala-time" %% "nscala-time" % "2.26.0",
    "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
    "org.apache.hadoop" % "hadoop-common" % "2.7.3" % "provided",
    "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.3" % "provided",
    "mysql" % "mysql-connector-java" % "8.0.33"
  )
)

lazy val commonModule = (project in file("common"))
  .settings(
    commonSettings
  )

lazy val doris = (project in file("hive2doris"))
  .settings(
    commonSettings
  ).dependsOn(commonModule)

lazy val root = (project in file("."))
  .aggregate(commonModule, doris)
