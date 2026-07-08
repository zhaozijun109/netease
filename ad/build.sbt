name := "ad"
organization := "com.netease.yaolu"
version := "0.1.1"

lazy val commonSettings = Seq(
  organization := "com.netease.yaolu",
  version := "0.1.0",
  scalaVersion := "2.12.10"
)

lazy val offline= (project in file("offline"))
  .settings(commonSettings)

lazy val realtime= (project in file("realtime"))
  .settings(commonSettings)

lazy val root = (project in file("."))
  .aggregate(offline, realtime)
