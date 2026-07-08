name := "flink-avro-schema-registry"
organization := "com.netease.wm"
version := "0.2.0"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val scala212 = "2.12.10"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)
crossScalaVersions := supportedScalaVersions
ThisBuild / scalaVersion := scala212

val flinkVersion = "1.20.3"

Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue

libraryDependencies ++= Seq(
  "org.apache.avro" % "avro" % "1.10.1",
  "org.json4s" %% "json4s-core" % "3.6.9",
  "org.json4s" %% "json4s-jackson" % "3.6.9",
  "org.apache.flink" % "flink-avro" % flinkVersion,
  "org.apache.flink" % "flink-connector-files" % flinkVersion,
    "org.apache.flink" %% "flink-scala" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.hadoop" % "hadoop-common" % "2.7.5" % "provided"
)

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class", xs @ _* ) => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF", xs @ _* ) => MergeStrategy.discard
  case x => MergeStrategy.first
}

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some(("comic-releases" at "http://10.172.113.214:8081/content/repositories/releases").withAllowInsecureProtocol(true))
