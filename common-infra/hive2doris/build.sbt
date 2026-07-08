name := "hive2doris"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.3.0" % "provided",
  "org.apache.spark" %% "spark-hive" % "3.3.0" % "provided",
  "org.apache.doris" % "spark-doris-connector-spark-3.3" % "25.1.0.11-MUSIC-RELEASE",
  // "org.apache.doris" % "spark-doris-connector-spark-3.3" % "25.2.0"
  "org.roaringbitmap" % "RoaringBitmap" % "0.9.45"
)

// Shade OkHttp + Okio to avoid conflicts with Spark's bundled OkHttp 3.x
assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("okhttp3.**" -> "shaded.okhttp3.@1").inAll,
  ShadeRule.rename("okio.**"    -> "shaded.okio.@1").inAll
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "services", "org.apache.spark.sql.sources.DataSourceRegister") => MergeStrategy.concat
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some(("comic-releases" at "https://maven-lofter.hz.netease.com/content/repositories/releases").withAllowInsecureProtocol(true))