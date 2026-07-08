name := "lofter-root"
organization := "com.netease.yaolu"
version := "0.1.0"
scalaVersion := "2.12.10"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint")

lazy val commonSettings = Seq(
  organization := "com.netease.yaolu",
  version := "0.0.1",
  scalaVersion := "2.12.10",
  libraryDependencies := Seq(
    "com.github.nscala-time" %% "nscala-time" % "2.26.0",
    "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
    "org.apache.hadoop" % "hadoop-common" % "2.7.3" % "provided",
    "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.3" % "provided")
)

lazy val common = (project in file("common"))
  .settings(
  commonSettings
)

lazy val etl = (project in file("etl"))
  .settings(
    commonSettings
  ).dependsOn(common)

lazy val analysis = (project in file("analysis"))
  .settings(
  commonSettings
).dependsOn(common)

lazy val dbDump = (project in file("db-dump"))

lazy val dbDumpNdc = (project in file("db-dump-ndc"))

lazy val root = (project in file("."))
  .aggregate(dbDump, dbDumpNdc, etl, analysis)

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some(("comic-releases" at "https://maven-lofter.hz.netease.com/content/repositories/releases").withAllowInsecureProtocol(true))