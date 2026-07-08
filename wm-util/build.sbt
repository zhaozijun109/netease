name := "wm-util"
organization := "com.netease.wm"
version := "0.2.2"
javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint")

lazy val scala212 = "2.12.10"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)
crossScalaVersions := supportedScalaVersions
ThisBuild / scalaVersion := scala212

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some("comic-releases" at "http://10.172.113.214:8081/content/repositories/releases")

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "org.apache.commons" % "commons-email" % "1.6.0"
)
